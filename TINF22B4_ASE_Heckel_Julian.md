# LeihBar – Technische Dokumentation

**Kurs:** TINF22B4
**Student:** Julian Heckel
**Prüfungsleistung:** Programmentwurf – Advanced Software Engineering

---

## Inhaltsverzeichnis

1. [Domain-Driven Design](#1-domain-driven-design)
2. [Clean Architecture](#2-clean-architecture)
3. [Programming Principles](#3-programming-principles)
4. [Unit Tests](#4-unit-tests)
5. [Refactoring](#5-refactoring)
6. [Entwurfsmuster](#6-entwurfsmuster)
7. [Anhang – Projektstruktur und Kennzahlen](#anhang--projektstruktur-und-kennzahlen)

---

## Einleitung

LeihBar ist eine Desktop-Anwendung zur Verwaltung einer Ausleihe. Fachlich geht es um die typischen Abläufe: Gegenstände anlegen, an Ausleiher verleihen, Rückgaben erfassen, Zustand und Fälligkeiten im Blick behalten. Die Problemdomäne orientiert sich an dem Themenvorschlag „Bibliothek-Verwaltung / Musikverein-Verwaltung mit Instrumenten-Ausleihe", ist aber generischer gehalten, sodass beliebige Gegenstände (nicht nur Bücher oder Instrumente) verwaltbar sind.

Die Anwendung ist in Java 21 mit JavaFX als UI-Framework und Hibernate/JPA (H2 als Datenbank) umgesetzt. Die Kennzahlen im Überblick:

| Metrik | Wert |
|--------|------|
| Klassen (main) | 32 |
| Gesamte Zeilen (main+test) | ≈ 5800 |
| Testmethoden | 84 (davon 4 parametrisiert, dadurch 95 Testausführungen) |
| Testklassen | 11 (davon 2 Integrationstests) |

---

## 1. Domain-Driven Design

### 1.1 Ubiquitous Language

Die Fachsprache der Domäne ist an der Anwendungsoberfläche, in den Tests und im Code konsistent dieselbe. Sie ist bewusst auf Deutsch gehalten, weil sich die Domäne im deutschsprachigen Raum bewegt und die Übersetzung fachlicher Begriffe („Ausleihe", „Ausleiher", „Rückgabe", „Überfällig") in Anglizismen nichts gewonnen hätte.

| Begriff | Bedeutung | Code-Ausdruck |
|---------|-----------|---------------|
| Gegenstand | Ein ausleihbarer Artikel im Bestand | `Gegenstand` (Aggregate) |
| Ausleiher | Person, die Gegenstände ausleiht | `Ausleiher` (Entity) |
| Ausleihe | Vorgang des Ausleihens mit Zeitraum | `Ausleihe` (Aggregate) |
| Inventarnummer | Eindeutige Kennung eines Gegenstandes | `InventarNummer` (Value Object) |
| Zeitraum | Geplante Ausleih-Spanne (Von – Bis) | `Zeitraum` (Value Object) |
| Kategorie | Klassifizierung inkl. maximaler Ausleihdauer | `Kategorie` (Value Object) |
| Verfügbarkeit | Zustand eines Gegenstandes (ausleihbar?) | `VerfuegbarkeitsStatus` (Enum) |
| Rückgabe | Beenden einer aktiven Ausleihe | `Ausleihe.zurueckgeben(...)` |
| Stornierung | Abbruch einer Ausleihe vor Rückgabe | `Ausleihe.stornieren()` |
| Überfällig | Aktive Ausleihe mit überschrittenem Enddatum | `AusleiheStatus.UEBERFAELLIG` |
| Zustandsbericht | Bemerkung bei der Rückgabe | `Ausleihe.getZustandsbericht()` |

### 1.2 Fachliche Regeln

Die folgenden Regeln sind im Domänenmodell durchgesetzt und nicht nur in der UI-Schicht:

1. **Ausleih-Voraussetzung:** Nur Gegenstände mit Status `VERFUEGBAR` können ausgeliehen werden. Durchgesetzt in `Gegenstand.ausleihen()` (wirft `IllegalStateException`, wenn der Status nicht passt).
2. **Zeitraum-Validität:** Das Enddatum eines Zeitraums darf nicht vor dem Startdatum liegen. Durchgesetzt im Konstruktor von `Zeitraum`.
3. **Maximale Ausleihdauer:** Jede Kategorie hat eine Obergrenze (`maxAusleihdauerTage`). Der `VerfuegbarkeitService` verweigert längere Zeiträume.
4. **Rückgabe nur bei aktiver Ausleihe:** `Ausleihe.zurueckgeben(String)` wirft, wenn der Status nicht mehr aktiv ist.
5. **Ausgeliehene Gegenstände nicht löschen/ausmustern:** Sowohl `Gegenstand.inWartungSetzen()` als auch `ausmustern()` sowie `GegenstandService.gegenstandLoeschen(UUID)` blocken das.
6. **Inventarnummer-Format:** Muss `INV-XXXX` (vier Ziffern) sein. Validierung im Konstruktor von `InventarNummer`.
7. **E-Mail-Eindeutigkeit:** `AusleiherService.ausleiherAnlegen` verweigert doppelte E-Mail-Adressen.

### 1.3 Taktische Muster

Alle im Kurs geforderten taktischen DDD-Muster sind vorhanden. Jedes wird im Folgenden an einem konkreten Beispiel aus dem Code begründet.

#### 1.3.1 Value Objects

Ein Value Object hat keine eigene Identität; zwei Objekte mit gleichem Inhalt sind per Definition gleich. Im Projekt gibt es vier Value-Object-Klassen (`InventarNummer`, `Zeitraum`, `Kontaktdaten`, `Kategorie`) sowie zwei Status-Enums (`VerfuegbarkeitsStatus`, `AusleiheStatus`), die fachlich ebenfalls als Value Objects auftreten.

**Beispiel `InventarNummer`** (`domain/valueobjects/InventarNummer.java`):

```java
public final class InventarNummer {
    private static final Pattern VALID_PATTERN = Pattern.compile("^INV-\\d{4}$");
    private final String value;

    public InventarNummer(String value) {
        Objects.requireNonNull(value, "Inventarnummer darf nicht null sein");
        String normalized = value.toUpperCase().trim();
        if (!VALID_PATTERN.matcher(normalized).matches()) {
            throw new IllegalArgumentException(
                "Ungültiges Inventarnummer-Format. Erwartet: INV-XXXX, erhalten: " + value
            );
        }
        this.value = normalized;
    }

    public static InventarNummer of(int nummer) {
        if (nummer < 0 || nummer > 9999) {
            throw new IllegalArgumentException("Nummer muss zwischen 0 und 9999 liegen");
        }
        return new InventarNummer("INV-" + String.format("%04d", nummer));
    }
    // equals/hashCode über value, keine Setter, final
}
```

**Warum Value Object und nicht Entity?** Die Inventarnummer hat keine Lebensgeschichte. Zwei Instanzen mit demselben Wert sind nicht nur „gleich", sie sind aus fachlicher Sicht dasselbe. Eine ID wäre redundant, weil die Nummer selbst bereits die Identifikation übernimmt. Kombiniert mit der Selbstvalidierung im Konstruktor ist sichergestellt, dass es im System keine ungültige Inventarnummer geben kann – das Domain-Modell bleibt nur mit gültigen Werten belegt (Fail-Fast).

**Beispiel `Zeitraum`:** Startdatum und Enddatum gehören fachlich untrennbar zusammen. Ein Datum ohne das jeweils andere hat keinen Sinn. Der Zeitraum stellt außerdem eigene fachliche Operationen bereit (`istUeberfaellig()`, `ueberschneidetSichMit(Zeitraum)`, `getDauerInTagen()`), die sonst auf zwei lose LocalDate-Werten als externe Logik verteilt wären – ein klassischer Fall von Primitive Obsession.

#### 1.3.2 Entities

Eine Entity hat eine Identität, die von ihren Attributen unabhängig ist. Auch wenn sich Name oder Kontaktdaten ändern, bleibt es derselbe Ausleiher.

**Beispiel `Ausleiher`** (`domain/entities/Ausleiher.java`):

```java
public class Ausleiher {
    private final UUID id;
    private String vorname;
    private String nachname;
    private Kontaktdaten kontaktdaten;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        return id.equals(((Ausleiher) o).id);
    }
}
```

**Warum Entity und nicht Value Object?** Ein Ausleiher kann seine Kontaktdaten aktualisieren oder den Nachnamen ändern (Heirat), ohne dass er zu einer „anderen Person" wird. Die Gleichheit hängt ausschließlich an der `id`. Genau das unterscheidet ihn von einem Value Object.

Änderungen laufen nicht über technische Setter, sondern über intentionsbasierte Methoden, die die fachliche Absicht ausdrücken: `umbenennen(vorname, nachname)` und `kontaktdatenAendern(kontaktdaten)`. Die Validierung steckt in privaten Hilfsmethoden, sodass auch nach einer Änderung keine inkonsistenten Zustände (leerer Nachname, null-Kontaktdaten) entstehen können. Die Umstellung von öffentlichen Settern auf diese Methoden ist als Refactoring in Kapitel 5.2 dokumentiert.

#### 1.3.3 Aggregates

Ein Aggregate fasst zusammengehörige Objekte unter einer Wurzel zusammen und schützt deren Konsistenzgrenzen.

**Beispiel `Gegenstand` (Aggregate Root):**

Der `Gegenstand` kapselt `InventarNummer`, `Kategorie` und `VerfuegbarkeitsStatus`. Zugriff auf den Status ist nur über Domänenmethoden möglich:

```java
public void ausleihen() {
    if (!istVerfuegbar()) {
        throw new IllegalStateException(
            "Gegenstand '" + name + "' kann nicht ausgeliehen werden. " +
            "Aktueller Status: " + status.getBezeichnung());
    }
    this.status = VerfuegbarkeitsStatus.AUSGELIEHEN;
}
```

**Warum Aggregate?** Der Status eines Gegenstands darf nicht von außen beliebig manipuliert werden – sonst entstehen widersprüchliche Zustände (ein AUSGEMUSTERTER Gegenstand wird plötzlich wieder VERFUEGBAR). Die Zustandsübergänge werden über Methoden (`ausleihen`, `zurueckgeben`, `inWartungSetzen`, `wartungBeenden`, `ausmustern`) erzwungen, die jede fachlich unerlaubte Transition blockieren. Der Aufrufer muss die Logik nicht selbst kennen – er ruft die Intention auf, das Aggregate entscheidet über die Zulässigkeit.

**Beispiel `Ausleihe` (Aggregate Root):** Noch stärker gekapselt, weil mehr Felder (`status`, `tatsaechlichesRueckgabedatum`, `zustandsbericht`) gleichzeitig konsistent zueinander gehalten werden müssen. Eine Rückgabe ist nur über `zurueckgeben(String zustandsbericht)` möglich, welches in einem Schritt den Status auf `ZURUECKGEGEBEN` setzt, das aktuelle Datum einträgt und den Zustandsbericht speichert.

#### 1.3.4 Repositories

Repositories abstrahieren die Persistenz in der Sprache der Domäne. Die Interfaces liegen bewusst im Domain-Package, nicht bei der Infrastruktur.

**Beispiel `GegenstandRepository`** (`domain/repositories/GegenstandRepository.java`):

```java
public interface GegenstandRepository {
    Gegenstand speichern(Gegenstand gegenstand);
    Optional<Gegenstand> findeNachId(UUID id);
    Optional<Gegenstand> findeNachInventarNummer(InventarNummer inventarNummer);
    List<Gegenstand> findeVerfuegbare();
    List<Gegenstand> suche(String suchbegriff);
    InventarNummer naechsteFreieInventarNummer();
    // ...
}
```

Die Methodennamen (`findeNachId`, `findeVerfuegbare`, `naechsteFreieInventarNummer`) sprechen die Fachsprache, nicht SQL. Der Domain Service `VerfuegbarkeitService` arbeitet ausschließlich gegen dieses Interface und weiß nichts von JPA, Entitymanagern oder Queries.

**Warum Repository?** Die Alternative wäre, die Datenbank direkt aus den Services zu verwenden. Das würde die Domäne an das Persistenz-Framework koppeln und Unit-Tests unmöglich machen, ohne eine echte Datenbank zu starten. Durch das Repository lässt sich die Persistenz sauber ersetzen – und genau das tun die Service-Tests: Sie verwenden Mockito-Mocks der Repository-Interfaces (siehe Kapitel 4).

#### 1.3.5 Domain Services

Wenn eine fachliche Operation über mehrere Aggregates hinwegarbeitet oder nicht sinnvoll in ein einzelnes Objekt passt, gehört sie in einen Domain Service.

**Beispiel `VerfuegbarkeitService`** (`domain/services/VerfuegbarkeitService.java`):

```java
public VerfuegbarkeitErgebnis pruefeVerfuegbarkeit(Gegenstand gegenstand, Zeitraum zeitraum) {
    if (!gegenstand.istVerfuegbar()) {
        return VerfuegbarkeitErgebnis.nichtVerfuegbar(
            "Gegenstand ist nicht verfügbar. Status: " + gegenstand.getStatus());
    }
    var aktiveAusleihe = ausleiheRepository.findeAktiveAusleiheVonGegenstand(gegenstand.getId());
    if (aktiveAusleihe.isPresent()) {
        return VerfuegbarkeitErgebnis.nichtVerfuegbar(
            "Gegenstand ist bereits ausgeliehen bis " +
            aktiveAusleihe.get().getGeplantesRueckgabedatum());
    }
    if (!gegenstand.getKategorie().istZeitraumErlaubt(zeitraum)) {
        return VerfuegbarkeitErgebnis.nichtVerfuegbar(
            "Ausleihdauer überschreitet das Maximum von " +
            gegenstand.getKategorie().getMaxAusleihdauerTage() + " Tagen");
    }
    return VerfuegbarkeitErgebnis.verfuegbar();
}
```

**Warum Domain Service und nicht Methode auf dem Gegenstand?** Die Prüfung braucht Informationen, die der `Gegenstand` selbst nicht hat: Existieren bereits aktive Ausleihen? Diese Information liegt im `AusleiheRepository`. Würde man das Repository in den Gegenstand injizieren, zerstört man die Reinheit des Aggregates und koppelt es an die Infrastruktur. Der Domain Service vermeidet diesen Kompromiss: Er orchestriert Gegenstand, Zeitraum und Repository, ohne selbst Zustand zu besitzen.

Der zweite Domain Service `UeberfaelligkeitService` folgt derselben Logik: Er analysiert Listen von Ausleihen aus dem Repository, sortiert und filtert sie – keine einzelne Ausleihe kann das für sich alleine entscheiden.

#### 1.3.6 Domain Events (Bonus)

Zusätzlich zu den Pflichtmustern wurden zwei Domain Events angelegt (`AusleiheErstelltEvent`, `AusleiheZurueckgegebenEvent`) mit einer gemeinsamen Oberschnittstelle `DomainEvent`. Sie sind als `record`-Typen realisiert und damit immutable. Die Events werden aktuell noch nicht aktiv publiziert, sind aber als Grundlage für eine spätere Event-Verarbeitung vorbereitet (z. B. für Mahnungen oder Statistik). Die Infrastruktur bewusst schlank zu halten ist eine Designentscheidung – ein voll ausgebauter Event-Bus wäre für den jetzigen Funktionsumfang Overengineering.

---

## 2. Clean Architecture

### 2.1 Schichten der Anwendung

Die Anwendung ist in vier Schichten gegliedert, die exakt den Java-Packages entsprechen:

```
ui           →   application   →   domain
                                       ↑
                                   infrastructure  (implementiert Domain-Interfaces)
```

| Schicht | Package | Aufgabe |
|---------|---------|---------|
| UI | `ui/views` | Darstellung, Eingaben, Anzeige von Fehlern |
| Application | `application/services` | Koordination der Use Cases |
| Domain | `domain/*` | Fachlogik, Regeln, Value Objects, Aggregates, Domain Services, Repository-Interfaces |
| Infrastructure | `infrastructure/persistence` | JPA-Entities und Repository-Implementierungen |

### 2.2 Dependency Rule

Die Richtung der Abhängigkeiten ist entscheidend und wird konsequent eingehalten:

- **UI → Application → Domain**: Die UI kennt nur `GegenstandService`, `AusleiherService` und `AusleiheService`, niemals JPA-Entities oder Repository-Implementierungen.
- **Infrastructure → Domain**: Die JPA-Implementierungen (`JpaGegenstandRepository` etc.) implementieren die Interfaces aus `domain/repositories`. Sie hängen von der Domäne ab, nicht umgekehrt.
- **Domain → nichts**: Die Domain-Schicht enthält keinen einzigen Import aus `infrastructure` oder `ui`. Das lässt sich direkt im Code nachprüfen.

Der wichtige Punkt: Es gibt **keinen Pfeil von Application zu Infrastructure**. Die Application Services kennen nur das Interface `GegenstandRepository`; welche konkrete Implementierung sie bekommen, entscheidet das Composition Root in `LeihBarApplication.init()`. Das ist Dependency Inversion in Reinform.

### 2.3 Domain Layer

Der innere Kern der Anwendung. Enthält Aggregates (`Gegenstand`, `Ausleihe`), die Entity `Ausleiher`, alle Value Objects, Repository-Interfaces, die Domain Services und die Events. Keine Framework-Abhängigkeit: Ein Austausch von JPA durch ein anderes Persistenz-Framework würde keine einzige Datei in `domain/` verändern.

### 2.4 Application Layer

Drei Application Services: `GegenstandService`, `AusleiherService`, `AusleiheService`. Sie orchestrieren Use Cases, halten aber selbst keine Geschäftsregeln. Beispiel aus `AusleiheService.ausleihen(UUID, UUID, LocalDate)`:

```java
Gegenstand gegenstand = gegenstandRepository.findeNachId(gegenstandId)
    .orElseThrow(() -> new IllegalArgumentException("Gegenstand nicht gefunden: " + gegenstandId));
Ausleiher ausleiher = ausleiherRepository.findeNachId(ausleiherId)
    .orElseThrow(() -> new IllegalArgumentException("Ausleiher nicht gefunden: " + ausleiherId));

Zeitraum zeitraum = new Zeitraum(LocalDate.now(), rueckgabedatum);
var ergebnis = verfuegbarkeitService.pruefeVerfuegbarkeit(gegenstand, zeitraum);
if (!ergebnis.istVerfuegbar()) {
    throw new IllegalStateException("Ausleihe nicht möglich: " + ergebnis.getGrund());
}

Ausleihe ausleihe = new Ausleihe.Builder()
    .gegenstand(gegenstand)
    .ausleiher(ausleiher)
    .zeitraum(zeitraum)
    .build();

// Status des Gegenstands und neue Ausleihe atomar speichern
return transactionRunner.execute(() -> {
    gegenstand.ausleihen();
    gegenstandRepository.speichern(gegenstand);
    return ausleiheRepository.speichern(ausleihe);
});
```

Der Service lädt die beteiligten Objekte, ruft den Domain Service für die Prüfung auf, delegiert die eigentlichen Zustandsübergänge an die Aggregates und persistiert das Ergebnis. Die Regeln selbst („Ist der Gegenstand verfügbar?", „Ist der Zeitraum erlaubt?") stehen nicht hier, sondern bleiben in der Domäne.

#### 2.4.1 Transaktionsgrenze als Application-Aufgabe

Eine Ausleihe verändert zwei Aggregate gleichzeitig: der `Gegenstand` wechselt auf `AUSGELIEHEN` und es entsteht eine neue `Ausleihe`. Beide Schreibvorgänge müssen gemeinsam gelingen oder gemeinsam scheitern – andernfalls bliebe nach einem Fehler beim Speichern der Ausleihe ein Gegenstand zurück, der als ausgeliehen markiert ist, ohne dass eine zugehörige Ausleihe existiert.

Das Setzen der Transaktionsgrenze ist eine Aufgabe der Application-Schicht (sie kennt den Use Case), die konkrete Umsetzung gehört aber in die Infrastruktur (sie kennt JPA). Gelöst ist das über den Port `TransactionRunner` (`application/services/TransactionRunner.java`), implementiert durch `JpaTransactionRunner` in der Infrastruktur. Der Service klammert die mehrstufige Schreiboperation:

```java
return transactionRunner.execute(() -> {
    gegenstand.ausleihen();
    gegenstandRepository.speichern(gegenstand);
    return ausleiheRepository.speichern(ausleihe);
});
```

Die Repository-Methoden nutzen eine bereits geöffnete Transaktion mit, statt eine eigene zu starten (siehe `AbstractJpaRepository` in Kapitel 5.2). Dadurch funktionieren sie sowohl eigenständig (Einzelspeicherung) als auch innerhalb dieser äußeren Klammer. Der Integrationstest `TransaktionsklammerIntegrationTest` belegt, dass bei einem Fehler innerhalb der Klammer nichts persistiert wird.

Die Abhängigkeitsrichtung bleibt dabei korrekt: Application definiert den Port `TransactionRunner`, Infrastructure implementiert ihn – dasselbe Inversionsprinzip wie bei den Repositories.

### 2.5 Infrastructure Layer

Drei JPA-Entity-Klassen (`GegenstandJpaEntity`, `AusleiherJpaEntity`, `AusleiheJpaEntity`) mit statischen `fromDomain(...)`- und Instanz-`toDomain()`-Methoden, sowie drei Repository-Implementierungen (`JpaGegenstandRepository`, `JpaAusleiherRepository`, `JpaAusleiheRepository`).

Die bewusste Trennung zwischen Domain-Aggregates und JPA-Entities ist ein Kernpunkt der Architektur: Der Domain-`Gegenstand` kennt keine `@Entity`, `@Id` oder `@Column`-Annotationen. Die Persistenzdetails (Column-Namen, Mapping-Strategien, Enum-Serialisierung) bleiben in den JpaEntity-Klassen. Beim Speichern wird per `GegenstandJpaEntity.fromDomain(gegenstand)` übersetzt, beim Lesen über `entity.toDomain()` zurück.

Der Preis dafür ist ein gewisser Mapping-Overhead. Der Gewinn: Die Domäne bleibt frei von Framework-Spezifika und die Aggregate-Konstruktoren müssen keine Rücksicht auf Hibernate-Anforderungen nehmen (z. B. zwingender No-Args-Konstruktor auf `@Entity`).

### 2.6 UI Layer

`MainView` ist eine JavaFX-`BorderPane`, die drei Application Services per Konstruktor bekommt:

```java
public MainView(GegenstandService gegenstandService,
                AusleiherService ausleiherService,
                AusleiheService ausleiheService) { ... }
```

Die UI transformiert Benutzereingaben in Service-Aufrufe und zeigt Ergebnisse an. Geschäftslogik – etwa die Prüfung, ob ein Gegenstand überhaupt ausleihbar ist – steht nicht in der UI. Fehler aus der Domäne (`IllegalStateException` etc.) werden zentral abgefangen und in `showError(...)`-Dialogen dargestellt:

```java
try {
    return ausleiheService.ausleihen(gegenstandId, ausleiherId, rueckgabedatum);
} catch (IllegalStateException ex) {
    showError("Ausleihe nicht möglich", ex.getMessage());
    return null;
}
```

Die UI enthält zusätzlich Komfortfunktionen (farbige Statusmarkierung in den Tabellen, klickbare Dashboard-Karten mit Tab-Navigation, Auto-Refresh beim Tab-Wechsel, editierbare Kategorie-ComboBox), die aber rein präsentationsbezogen sind und keine fachlichen Regeln duplizieren.

#### 2.6.1 Bewusster Verzicht auf eine DTO-Schicht

Die `MainView` arbeitet direkt mit den Domain-Objekten (`Gegenstand`, `Ausleihe`, `Ausleiher`), statt diese über eigene Präsentations-DTOs zu entkoppeln. In einer verteilten Anwendung (z. B. mit einer REST-Grenze zwischen Server und Client) wären DTOs angebracht, um interne Struktur und Serialisierungsformat zu trennen.

Hier wurde bewusst darauf verzichtet, und zwar aus zwei Gründen. Erstens laufen UI und Application im selben Prozess; es gibt keine Serialisierungs- oder Netzgrenze, an der ein DTO entkoppeln müsste. Zweitens lesen die Tabellen die Domain-Objekte nur über deren öffentliche Lese-Methoden (`getName()`, `getStatus()`); sie verändern keinen Domänenzustand und umgehen keine Invarianten – schreibende Operationen laufen ausschließlich über die Application Services. Eine vollständige DTO-Schicht plus Mapping würde für jeden Anzeigewert eine zusätzliche Klasse und Übersetzung bedeuten, ohne in diesem Szenario einen realen Schutz zu liefern – also Overengineering. Die Kopplung bleibt zudem gerichtet: Die UI hängt von der Domäne ab, die Domäne nicht von der UI.

Sollte später eine Web- oder Remote-Schnittstelle hinzukommen, wäre das Einführen von Read-Model-DTOs der nächste konsequente Schritt; die Application Services wären der natürliche Ort für das Mapping.

### 2.7 Composition Root

Das Zusammenstecken der Abhängigkeiten passiert an einer einzigen Stelle, in `LeihBarApplication.init()`:

```java
EntityManager entityManager = ...;
GegenstandRepository gegenstandRepository = new JpaGegenstandRepository(entityManager);
AusleiherRepository ausleiherRepository = new JpaAusleiherRepository(entityManager);
AusleiheRepository ausleiheRepository = new JpaAusleiheRepository(entityManager);

VerfuegbarkeitService verfuegbarkeitService = new VerfuegbarkeitService(ausleiheRepository);
TransactionRunner transactionRunner = new JpaTransactionRunner(entityManager);

GegenstandService gegenstandService = new GegenstandService(gegenstandRepository);
AusleiherService ausleiherService = new AusleiherService(ausleiherRepository);
AusleiheService ausleiheService = new AusleiheService(
    ausleiheRepository, gegenstandRepository, ausleiherRepository,
    verfuegbarkeitService, transactionRunner);
```

Manuelle Dependency Injection, kein DI-Framework. Für einen Programmentwurf dieser Größe wäre Spring oder Guice Overkill – die wenigen Zeilen sind übersichtlicher als jede XML- oder Annotation-Konfiguration. Wichtig für die Transaktionsklammer: `transactionRunner` und alle Repositories teilen sich denselben `EntityManager`, damit eine im Runner geöffnete Transaktion von den Repository-Aufrufen mitgenutzt wird.

---

## 3. Programming Principles

Fünf Prinzipien, jeweils mit konkretem Beleg im Code.

### 3.1 Single Responsibility Principle (SRP)

Eine Klasse hat genau einen Grund zur Änderung.

**Angewendet in:** `domain/valueobjects/InventarNummer.java`

Die Klasse macht ausschließlich das, was zu einer Inventarnummer gehört: Format validieren, normalisieren, Wert bereitstellen, numerischen Teil extrahieren. Sie formatiert keine UI-Strings und kennt keine Datenbank. Würde sich zum Beispiel das Inventarnummer-Format von `INV-XXXX` auf `INV-XX-XXXX` ändern, bleibt die Änderung vollständig in dieser einen Klasse. Andere Klassen behandeln Inventarnummern nur noch als Objekte und bekommen die Änderung gar nicht mit.

**Gegenbeispiel aus früheren Iterationen:** Bevor `InventarNummer` als eigenes Value Object existierte, war die Validierung verstreut in `Gegenstand`, im Application Service und in der UI – jede dieser Stellen hätte bei einer Formatänderung angefasst werden müssen. Das ist genau der SRP-Bruch, den das Refactoring in Kapitel 5 behebt.

### 3.2 Dependency Inversion Principle (DIP)

Hochgelegene Module dürfen nicht von tiefen Modulen abhängen; beide hängen von Abstraktionen ab.

**Angewendet in:** `application/services/AusleiheService.java`

Der `AusleiheService` (Application, „oben") hängt nicht direkt an `JpaAusleiheRepository` (Infrastructure, „unten"), sondern nur am Interface `AusleiheRepository`:

```java
public class AusleiheService {
    private final AusleiheRepository ausleiheRepository; // Interface
    ...
    public AusleiheService(AusleiheRepository ausleiheRepository, ...) { ... }
}
```

Die konkrete Implementierung wird im Composition Root zugewiesen. Der praktische Nutzen zeigt sich in den Tests: `AusleiheServiceTest` ersetzt das Repository durch einen Mockito-Mock und braucht weder Hibernate noch H2.

### 3.3 Information Expert (GRASP)

Verantwortlichkeiten dort ansiedeln, wo das nötige Wissen bereits vorhanden ist.

**Angewendet in:** `domain/valueobjects/Zeitraum.java`

Die Frage „Ist dieser Zeitraum überfällig?" wird vom Zeitraum selbst beantwortet, nicht von einem externen Service, der `von` und `bis` nochmals vergleicht:

```java
public boolean istUeberfaellig() {
    return LocalDate.now().isAfter(bis);
}

public long getUeberfaelligeTage() {
    LocalDate heute = LocalDate.now();
    if (!heute.isAfter(bis)) {
        return 0;
    }
    return ChronoUnit.DAYS.between(bis, heute);
}
```

Der Zeitraum ist der „Information Expert" – er hat alle Daten, die zur Beantwortung nötig sind. `Ausleihe.istUeberfaellig()` delegiert an diesen Aufruf, anstatt die Datum-Vergleiche selbst zu machen. Ohne dieses Prinzip stünden dieselben Vergleiche an fünf Stellen im Code mit dem Risiko, dass einer davon beim nächsten Bugfix vergessen wird.

### 3.4 Open/Closed Principle (OCP)

Klassen sollen offen für Erweiterung, aber geschlossen für Modifikation sein.

**Angewendet in:** `domain/repositories/GegenstandRepository.java` (und Konsorten)

Die Application Services sind gegen das Repository-Interface programmiert. Eine neue Persistenzform (etwa MongoDB, In-Memory für Tests, REST-Adapter an einen anderen Dienst) bedeutet: Eine neue Klasse `InMemoryGegenstandRepository implements GegenstandRepository` – und fertig. `GegenstandService` wird nicht angefasst.

Konkret lebt das Prinzip bereits im Test-Setup: Die Service-Tests nutzen über Mockito eine generierte Implementation des Interfaces, der produktive Code nutzt `JpaGegenstandRepository`. Zwei komplett andere Implementierungen, beide funktionieren am selben Service.

### 3.5 Don't Repeat Yourself (DRY)

Wissen soll nur an einer Stelle liegen.

**Angewendet in:** `domain/valueobjects/Kategorie.java`

Die Regel „Wie lange darf ein Gegenstand dieser Kategorie maximal ausgeliehen werden?" steht nur einmal im Code, in `Kategorie.istZeitraumErlaubt(Zeitraum)`:

```java
public boolean istZeitraumErlaubt(Zeitraum zeitraum) {
    Objects.requireNonNull(zeitraum, "Zeitraum darf nicht null sein");
    return zeitraum.getDauerInTagen() <= maxAusleihdauerTage;
}
```

Sowohl der `VerfuegbarkeitService` bei Neuausleihen als auch die UI bei der Zeitraum-Vorschau rufen dieselbe Methode auf. Es gibt keine zweite Implementierung der Regel, die bei Änderungen mitgepflegt werden müsste.

Ein zweites Beispiel auf Infrastruktur-Ebene: Die Transaktionsbehandlung (begin/commit/rollback) für schreibende Operationen lag früher in jeder `speichern`- und `loeschen`-Methode aller drei JPA-Repositories dupliziert. Sie ist inzwischen in `AbstractJpaRepository.inTransaction(...)` zentralisiert (siehe Kapitel 5.2), sodass es nur noch eine Stelle gibt, an der Transaktionen gehandhabt werden.

---

## 4. Unit Tests

### 4.1 Rahmen

Die Testsuite umfasst **84 Testmethoden in 11 Testklassen** (80 mit `@Test`, 4 mit `@ParameterizedTest`). Durch die Parametrisierung ergeben sich beim Lauf insgesamt **95 Einzelausführungen**. Neun Klassen sind reine Unit-Tests (JUnit 5, für die Application-/Domain-Services mit Mockito), zwei sind Integrationstests gegen eine echte H2-Datenbank (siehe 4.6), in denen AssertJ für die fluent Assertions auf Query-Ergebnissen verwendet wird. Der Testlauf wird über Maven gestartet (`mvn test`) und meldet 0 Failures, 0 Errors.

### 4.2 ATRIP-Regeln

| Regel | Umsetzung im Projekt |
|-------|----------------------|
| **A**utomatic | Alle Tests laufen ohne Benutzereingaben; Maven/JUnit führt sie aus. |
| **T**horough | Neben Positiv-Fällen sind gezielt Randfälle abgedeckt: ungültige Inventarnummern, Enddatum vor Startdatum, Ausleihe eines bereits ausgeliehenen Gegenstandes, Rückgabe einer nicht-aktiven Ausleihe usw. |
| **R**epeatable | Die Unit-Tests binden sich an keine externe Ressource. Wo `LocalDate.now()` als Bezugspunkt dient (z. B. in `ZeitraumTest`), sind die Assertions relativ formuliert (`plusDays(7)` statt fixer Datumswerte). Die Integrationstests nutzen eine flüchtige In-Memory-H2 mit Schema-Neuanlage (`create-drop`) je Testlauf, sodass auch sie deterministisch und ohne Seiteneffekte auf die Produktiv-Datenbank wiederholbar sind. |
| **I**ndependent | Jede Testklasse hat ihr eigenes `@BeforeEach`-Setup; keine Testmethode ist auf eine andere angewiesen. |
| **P**rofessional | Durchgängig Arrange-Act-Assert, sprechende `@DisplayName`-Bezeichnungen auf Deutsch, aussagekräftige Fehlermeldungen. |

### 4.3 Testübersicht

| Testklasse | @Test + @ParameterizedTest | Getestete Einheit |
|------------|---------------------------:|-------------------|
| `InventarNummerTest` | 8 + 2 | Value Object InventarNummer |
| `ZeitraumTest` | 8 + 0 | Value Object Zeitraum |
| `KontaktdatenTest` | 6 + 2 | Value Object Kontaktdaten |
| `GegenstandTest` | 10 + 0 | Aggregate Gegenstand |
| `AusleiheTest` | 11 + 0 | Aggregate Ausleihe |
| `AusleiherTest` | 10 + 0 | Entity Ausleiher |
| `VerfuegbarkeitServiceTest` | 7 + 0 | Domain Service (mit Mock) |
| `AusleiheServiceTest` | 7 + 0 | Application Service (mit Mocks) |
| `GegenstandServiceTest` | 8 + 0 | Application Service (mit Mocks) |
| `JpaGegenstandRepositoryIntegrationTest` | 4 + 0 | Persistenz (echtes H2, AssertJ) |
| `TransaktionsklammerIntegrationTest` | 1 + 0 | Transaktions-Atomarität (echtes H2) |
| **Summe** | **80 + 4** | **11 Testklassen, 95 Testausführungen** |

### 4.4 Beispiel: Domänenlogik-Test

Aus `GegenstandTest`:

```java
@Test
@DisplayName("Sollte Ausleihe eines nicht verfügbaren Gegenstands ablehnen")
void sollteAusleiheNichtVerfuegbarerGegenstaendeAblehnen() {
    Gegenstand gegenstand = Gegenstand.neu(InventarNummer.of(1), "Hammer", "", Kategorie.of("Werkzeug", 30));
    gegenstand.ausleihen();                     // Status jetzt AUSGELIEHEN

    assertThrows(IllegalStateException.class, gegenstand::ausleihen);
}
```

Der Test zeigt, dass die Regel „Zweimal Ausleihen nicht möglich" tatsächlich im Aggregate greift. Ohne diese Regel wäre der Zustand doppelt vergeben.

### 4.5 Beispiel: Test mit Mocks

Aus `AusleiheServiceTest`:

```java
@ExtendWith(MockitoExtension.class)
class AusleiheServiceTest {

    @Mock private AusleiheRepository ausleiheRepository;
    @Mock private GegenstandRepository gegenstandRepository;
    @Mock private AusleiherRepository ausleiherRepository;
    @Mock private VerfuegbarkeitService verfuegbarkeitService;
    @Mock private TransactionRunner transactionRunner;

    @BeforeEach
    void setUp() {
        ausleiheService = new AusleiheService(ausleiheRepository, gegenstandRepository,
            ausleiherRepository, verfuegbarkeitService, transactionRunner);
        // Der gemockte Runner führt die übergebene Arbeit im Test direkt aus.
        lenient().when(transactionRunner.execute(any(Supplier.class)))
            .thenAnswer(inv -> ((Supplier<?>) inv.getArgument(0)).get());
        // ... Testdaten ...
    }

    @Test
    @DisplayName("Sollte Ausleihe erfolgreich erstellen")
    void sollteAusleiheErfolgreichErstellen() {
        when(gegenstandRepository.findeNachId(gegenstandId)).thenReturn(Optional.of(gegenstand));
        when(ausleiherRepository.findeNachId(ausleiherId)).thenReturn(Optional.of(ausleiher));
        when(verfuegbarkeitService.pruefeVerfuegbarkeit(any(), any()))
            .thenReturn(VerfuegbarkeitErgebnis.verfuegbar());
        when(ausleiheRepository.speichern(any(Ausleihe.class)))
            .thenAnswer(inv -> inv.getArgument(0));
        when(gegenstandRepository.speichern(any(Gegenstand.class)))
            .thenAnswer(inv -> inv.getArgument(0));

        Ausleihe ausleihe = ausleiheService.ausleihen(gegenstandId, ausleiherId, rueckgabedatum);

        assertNotNull(ausleihe);
        verify(verfuegbarkeitService).pruefeVerfuegbarkeit(any(), any());
        verify(ausleiheRepository).speichern(any(Ausleihe.class));
    }
}
```

**Warum Mocks und nicht echte Repositories?** Der `AusleiheService` ist eine Orchestrierung – er delegiert an fünf Kollaborateure (drei Repositories, den `VerfuegbarkeitService` und den `TransactionRunner`). Mit echten JPA-Repositories müsste man Hibernate starten, H2 hochfahren, Testdaten einspielen und nach dem Test aufräumen. Der Testfokus („Wird die Reihenfolge der Aufrufe und die Fehlerbehandlung korrekt eingehalten?") wäre im Setup untergegangen. Mit Mocks bleibt der Test auf das Verhalten der Klasse konzentriert, die gerade getestet wird. Der `TransactionRunner`-Mock wird `lenient()` gestubbt, weil ihn die Fehler-Pfade (Gegenstand/Ausleiher fehlt, nicht verfügbar) gar nicht erreichen.

Mocks werden in drei Unit-Test-Klassen eingesetzt: `VerfuegbarkeitServiceTest` (Mock auf `AusleiheRepository`), `AusleiheServiceTest` (fünf Mocks: alle drei Repositories, `VerfuegbarkeitService` und `TransactionRunner`) und `GegenstandServiceTest` (Mock auf `GegenstandRepository`). Wo echtes Persistenzverhalten geprüft werden muss, kommen stattdessen die Integrationstests aus 4.6 zum Einsatz.

### 4.6 Integrationstests für die Persistenzschicht

Mocks prüfen das Zusammenspiel der Schichten, nicht aber, ob das JPA-Mapping und die Queries tatsächlich funktionieren. Diese Lücke schließen zwei Integrationstests (hinzugefügt im Commit „Integrationstests fuer die Persistenzschicht mit AssertJ"), die gegen eine echte – aber flüchtige – H2-Datenbank laufen. Die produktive Persistence Unit `leihbar-pu` wird wiederverwendet, ihre JDBC-URL aber per Property-Override auf eine In-Memory-Datenbank mit `create-drop` umgebogen, sodass die Datei-Datenbank der Anwendung unberührt bleibt:

```java
Map<String, String> overrides = Map.of(
    "jakarta.persistence.jdbc.url", "jdbc:h2:mem:leihbar-it;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE",
    "hibernate.hbm2ddl.auto", "create-drop");
emf = Persistence.createEntityManagerFactory("leihbar-pu", overrides);
```

`JpaGegenstandRepositoryIntegrationTest` prüft den Round-Trip Domain ↔ JpaEntity: Speichern und Wiederfinden, Suche nach Inventarnummer, Filterung nach Status sowie die numerische Vergabe der nächsten Inventarnummer. Hier werden die fluent Assertions von AssertJ genutzt, die bei Query-Ergebnissen besonders gut lesbar sind:

```java
assertThat(repository.findeVerfuegbare())
    .hasSize(2)
    .extracting(Gegenstand::getName)
    .containsExactlyInAnyOrder("Hammer", "Laptop");
```

`TransaktionsklammerIntegrationTest` belegt die in 2.4.1 beschriebene Atomarität: Wird innerhalb einer `transactionRunner.execute(...)`-Klammer nach dem ersten Speichern eine Ausnahme geworfen, darf nichts persistiert werden:

```java
assertThatThrownBy(() -> runner.execute(() -> {
    repository.speichern(gegenstand);
    throw new IllegalStateException("Simulierter Fehler nach dem Speichern");
})).isInstanceOf(IllegalStateException.class);

// Frischer EntityManager: nichts wurde committet
assertThat(pruefRepository.findeNachId(gegenstand.getId())).isEmpty();
assertThat(pruefRepository.zaehleAlle()).isZero();
```

---

## 5. Refactoring

Dieses Kapitel trennt sauber zwischen **Code Smells, die identifiziert wurden**, den **tatsächlich durchgeführten Refactorings** und einer **offenen Verbesserung, die sich anbietet**, aber bewusst (noch) nicht umgesetzt wurde.

### 5.1 Identifizierte Code Smells

#### 5.1.1 Primitive Obsession

**Beobachtung:** In einer früheren Version war die Inventarnummer schlicht ein `String`, die E-Mail ein `String`, der Zeitraum zwei separate `LocalDate`-Felder. Die Validierung lag an jeder aufrufenden Stelle (Service, UI, Tests) erneut vor.

**Warum ein Smell:** Das System kannte keinen Begriff für diese Konzepte – nur das Stringformat. Bei jeder neuen Verwendung stand die Validierungslogik zur Debatte („Habe ich den Regex auch dieses Mal richtig?") und in Tests tauchte regelmäßig `"INV-" + String.format(...)` als Ad-hoc-Konstrukt auf.

#### 5.1.2 Long Parameter List

**Beobachtung:** Der Konstruktor von `Ausleihe` hatte in einer früheren Variante sieben bis acht Parameter: Gegenstand, Ausleiher, Von-Datum, Bis-Datum, Status, erstelltAm, optionales Rückgabedatum, optionaler Zustandsbericht.

**Warum ein Smell:** Aufrufer mussten die Parameterreihenfolge auswendig kennen. Beim Erweitern um ein weiteres Feld (z. B. Zustandsbericht) mussten alle Aufrufstellen angepasst werden. Die Tests waren besonders schwer zu lesen, weil aus sieben positionsbasierten Argumenten nicht hervorging, was jeweils gemeint war.

#### 5.1.3 Feature Envy in der Verfügbarkeitsprüfung

**Beobachtung:** Eine frühere Prüfmethode im Application Service griff intensiv auf `gegenstand.getStatus()`, `gegenstand.getKategorie().getMaxAusleihdauerTage()`, `gegenstand.getId()` und eigene Repository-Calls zurück, ohne eigene Daten zu verwenden.

**Warum ein Smell:** Die Methode „neidete" dem Gegenstand seine Daten – ein Hinweis, dass die Logik entweder zum Gegenstand selbst oder in einen Domain Service gehört, der die Orchestrierung explizit macht.

#### 5.1.4 Fragile Cast-Chain im UI

**Beobachtung (aus `MainView`):**

```java
VBox content = (VBox) ((TabPane) getCenter()).getTabs().get(0).getContent();
HBox statsBox = (HBox) content.getChildren().get(1);
statsBox.getChildren().setAll(...);
```

Jede dieser Casts ist typunsicher. Jede Änderung am Szenengraphen hätte eine `ClassCastException` zur Laufzeit zur Folge.

**Warum ein Smell:** Der Code koppelt sich an die Struktur des UI-Baums, nicht an benannte Referenzen. Refactoring-Sicherheit gleich null.

#### 5.1.5 Duplizierter Validierungs-Boilerplate (weiter bestehend)

**Beobachtung:** In `Ausleiher.setVorname` und `setNachname` sowie in `Gegenstand.setName` steht dreimal fast identischer Code: Null-Check, `trim()`, Leer-Check, Längen-Check, Feld-Zuweisung. Das ist ein leichter DRY-Verstoß.

**Warum als Smell benannt:** Wenn sich das Validierungsmuster ändert (etwa eine Mindestlänge hinzukommt), müssen mehrere Stellen mitgepflegt werden. Siehe 5.3 für den Hinweis, warum die Deduplizierung bewusst offen geblieben ist.

#### 5.1.6 Fehlende Transaktionsklammer (Verletzung der Atomarität)

**Beobachtung:** Im `AusleiheService.ausleihen(...)` wurden der Statuswechsel des Gegenstands und das Anlegen der Ausleihe nacheinander gespeichert. Da jede `speichern`-Methode der Repositories ihre eigene Transaktion öffnete und committete, waren das zwei getrennte Transaktionen.

**Warum ein Smell:** Schlägt das Speichern der Ausleihe fehl, nachdem der Gegenstand bereits als `AUSGELIEHEN` committet wurde, bleibt ein inkonsistenter Zustand zurück: ein ausgeliehener Gegenstand ohne zugehörige Ausleihe. Use Cases, die mehrere Aggregate ändern, brauchen eine gemeinsame Transaktionsgrenze. (Höchste Priorität.)

#### 5.1.7 Duplizierte Transaktionsbehandlung in den Repositories

**Beobachtung:** Der identische Block `getTransaction().begin() / try / commit / catch / rollback` stand in jeder schreibenden Methode aller drei JPA-Repositories – insgesamt sechsmal nahezu wortgleich.

**Warum ein Smell:** Klassischer DRY-Verstoß auf Infrastruktur-Ebene. Eine Änderung der Fehlerbehandlung hätte an sechs Stellen erfolgen müssen.

#### 5.1.8 N+1-Abfragen in den UI-Tabellen

**Beobachtung:** In den Ausleihen- und Überfällig-Tabellen ermittelten die Spalten „Gegenstand" und „Ausleiher" den Anzeigenamen pro Zeile über einen eigenen Service-Aufruf (`findeGegenstand(id)` bzw. `findeAusleiher(id)`), der jeweils eine Datenbankabfrage auslöste.

**Warum ein Smell:** Bei N Zeilen entstehen so 2·N Einzelabfragen zusätzlich zur Abfrage der Ausleihen selbst – das bekannte N+1-Problem. Für eine kleine lokale Datenbank unkritisch, als Muster aber falsch und bei wachsenden Datenmengen ein Performance-Risiko.

#### 5.1.9 Fragile Bestimmung der nächsten Inventarnummer

**Beobachtung:** `naechsteFreieInventarNummer()` ermittelte die höchste vergebene Nummer über `ORDER BY g.inventarNummer DESC` – also eine lexikografische String-Sortierung – und zählte eins hinauf.

**Warum ein Smell:** Solange alle Nummern das Format `INV-XXXX` mit vier festen Stellen haben, stimmt die String-Reihenfolge zufällig mit der numerischen überein. Bei einem späteren Formatwechsel (z. B. fünfstellig) würde die Sortierung still falsche Ergebnisse liefern (`INV-9999` vor `INV-10000`). Eine latente Korrektheitsfalle.

#### 5.1.10 Öffentliche Setter statt intentionsbasierter Methoden

**Beobachtung:** Die Entity `Ausleiher` bot `setVorname`, `setNachname` und `setKontaktdaten` öffentlich an; der `Gegenstand` analog `setName`, `setBeschreibung`, `setKategorie`.

**Warum ein Smell:** Setter drücken keine fachliche Absicht aus und laden dazu ein, ein Aggregate Feld für Feld in einen Zwischenzustand zu versetzen. In DDD sollten Zustandsänderungen über Methoden laufen, die das fachliche „Warum" benennen (Anemic-Domain-Model vermeiden).

### 5.2 Durchgeführte Refactorings

#### 5.2.1 Replace Primitive with Object

**Ausgangssituation:** `String inventarnummer`, `String email`, `LocalDate von + LocalDate bis` in den Domänenklassen.

**Vorgehen:** Jedes Primitive, das fachliche Bedeutung trägt, wurde in ein Value Object überführt. `InventarNummer`, `Kontaktdaten`, `Zeitraum`, `Kategorie`. Jedes Value Object validiert im Konstruktor und bietet Operationen, die vorher als freie Funktionen verstreut waren.

**Wirkung:**
- Das Typsystem verhindert jetzt, dass eine E-Mail statt einer Inventarnummer übergeben wird – der Compiler fängt das ab.
- Die Validierungslogik steht genau einmal im Code (siehe `InventarNummer.java:19-30`).
- Domänenoperationen wie `ueberschneidetSichMit` oder `hatTelefon` lassen sich direkt am Objekt ausdrücken, anstatt lose Helper-Funktionen zu bauen.

Der Refactoring-Schritt ist in der Commit-Historie unter „Refactoring: Primitive Obsession beheben" sichtbar; die begleitenden Tests wurden anschließend unter „Refactoring: Tests an Domain-API angleichen" auf die neue API umgestellt.

#### 5.2.2 Introduce Builder

**Ausgangssituation:** Der `Ausleihe`-Konstruktor hatte mehrere Parameter mit teils optionalen Feldern, die Aufrufer mussten Positionen zählen.

**Vorgehen:** Einführung einer inneren `Builder`-Klasse mit benannten Methoden:

```java
Ausleihe ausleihe = new Ausleihe.Builder()
    .gegenstand(gegenstand)
    .ausleiher(ausleiher)
    .zeitraum(zeitraum)
    .build();
```

Der Konstruktor der Ausleihe wurde auf `private` gesetzt; Neuanlagen gehen ausschließlich über den Builder. Ein separater, öffentlicher Rekonstruktions-Konstruktor (mit voller Parameterliste) bleibt nur für die Persistenzschicht, weil die JPA-Entity den kompletten gespeicherten Zustand zurückbringen können muss.

**Wirkung:**
- Aufrufe sind selbsterklärend; man sieht sofort, welches Argument welche Rolle hat.
- Optionale Parameter lassen sich hinzufügen, ohne bestehende Aufrufstellen zu ändern.
- Validierung („Gegenstand muss gesetzt sein, Zeitraum muss gesetzt sein") passiert zentral im Konstruktor beim `build()`-Aufruf.

Sichtbar im Commit „Refactoring: Builder Pattern und API-Bereinigung". Details zum Builder als gewähltes Entwurfsmuster folgen in Kapitel 6.

#### 5.2.3 Extract Field (UI-Refactoring)

**Ausgangssituation:** Die Dashboard-Statistiken und die Tabelle „Überfällige Ausleihen" in der `MainView` waren lokale Variablen. Um sie nach Aktionen neu zu laden, durchquerte der Code den Szenengraphen per Cast (siehe Smell 5.1.4).

**Vorgehen:** Die referenzierten Container wurden in Felder der `MainView` hochgezogen:

```java
private TabPane tabPane;
private HBox dashboardStatsBox;
private TableView<Ausleihe> ueberfaelligTabelle;
```

Eine zusätzliche `refreshAllData()`-Methode bündelt das Neuladen.

**Wirkung:**
- Typsicherer Zugriff ohne Casts.
- Tabellen und Statistiken werden nach jeder Aktion (Ausleihe, Rückgabe, Wartung) zuverlässig aktualisiert.
- Ein Tab-Wechsel-Listener am `tabPane` ruft `refreshAllData()` auf, sodass immer die aktuellen Daten sichtbar sind.

Sichtbar im Commit „Refactoring: Datenaktualisierung und Tab-Refresh".

#### 5.2.4 Introduce Intention-Revealing Methods (behebt Smell 5.1.10)

**Ausgangssituation:** Öffentliche Setter auf `Ausleiher` und `Gegenstand`.

**Vorgehen:** Die Setter wurden auf `private` reduziert (Validierung bleibt dort) und durch fachlich benannte Methoden ersetzt:

```java
// Ausleiher
public void umbenennen(String vorname, String nachname) { ... }
public void kontaktdatenAendern(Kontaktdaten kontaktdaten) { ... }

// Gegenstand
public void aktualisiereStammdaten(String name, String beschreibung, Kategorie kategorie) {
    Objects.requireNonNull(kategorie, "Kategorie darf nicht null sein");
    setName(name);              // validiert, bevor das erste Feld geändert wird
    setBeschreibung(beschreibung);
    this.kategorie = kategorie;
}
```

**Wirkung:**
- Die API drückt die fachliche Absicht aus (`umbenennen` statt zweier Setter-Aufrufe).
- `aktualisiereStammdaten` prüft alle Argumente, bevor das erste Feld geändert wird – ein ungültiger Aufruf lässt den Gegenstand unverändert (keine halben Updates).
- Die Aufrufer (`AusleiherService`, `GegenstandService`) wurden entsprechend angepasst, ebenso die Tests (`AusleiherTest`, `GegenstandTest`).

Sichtbar im Commit „DDD: Intentionsbasierte Methoden statt oeffentlicher Setter".

#### 5.2.5 Extract Superclass + Transaktionsklammer (behebt Smells 5.1.6 und 5.1.7)

**Ausgangssituation:** Dupliziertes Transaktions-Handling in jeder Schreibmethode (5.1.7) und keine gemeinsame Transaktion über mehrere Repository-Aufrufe (5.1.6).

**Vorgehen in zwei Schritten:**

1. *Extract Superclass:* Der begin/commit/rollback-Block wurde in `AbstractJpaRepository.inTransaction(Runnable)` gezogen, von dem alle drei Repositories erben. Entscheidend: Läuft bereits eine Transaktion, wird sie mitgenutzt, statt eine neue zu öffnen.

```java
protected void inTransaction(Runnable work) {
    EntityTransaction tx = entityManager.getTransaction();
    if (tx.isActive()) { work.run(); return; }   // an äußerer Klammer teilnehmen
    tx.begin();
    try { work.run(); tx.commit(); }
    catch (RuntimeException e) { if (tx.isActive()) tx.rollback(); throw e; }
}
```

2. *Introduce Transaction Boundary:* Der Port `TransactionRunner` (Application) mit der Implementierung `JpaTransactionRunner` (Infrastructure) erlaubt dem `AusleiheService`, mehrere Schreibvorgänge zu klammern. Die mehrstufigen Use Cases `ausleihen`, `zurueckgeben` und `stornieren` nutzen das jetzt.

**Wirkung:**
- Das Transaktions-Handling steht nur noch an einer Stelle (DRY).
- `ausleihen`/`zurueckgeben`/`stornieren` sind atomar; der in 5.1.6 beschriebene inkonsistente Zustand kann nicht mehr entstehen.
- Belegt durch `TransaktionsklammerIntegrationTest` (siehe 4.6).

Sichtbar im Commit „Atomare Use Cases: Transaktionsklammer und Repository-Basisklasse".

#### 5.2.6 Replace lexikografische Sortierung durch numerische Auswertung (behebt Smell 5.1.9)

**Vorgehen:** `naechsteFreieInventarNummer()` sortiert nicht mehr per String, sondern liest die vorhandenen Nummern und bestimmt das Maximum über den numerischen Teil:

```java
int hoechste = query.getResultList().stream()
    .mapToInt(wert -> InventarNummer.of(wert).getNumericPart())
    .max()
    .orElse(0);
return InventarNummer.of(hoechste + 1);
```

**Wirkung:** Die Vergabe ist unabhängig von der Stellenzahl korrekt und bleibt auch bei einem späteren Formatwechsel robust. Abgedeckt durch `JpaGegenstandRepositoryIntegrationTest`. Teil desselben Commits wie 5.2.5 („Atomare Use Cases: Transaktionsklammer und Repository-Basisklasse"), da im selben Repository umgesetzt.

#### 5.2.7 N+1-Abfragen durch Lookup-Cache ersetzt (behebt Smell 5.1.8)

**Vorgehen:** In `MainView.refreshAllData()` werden die Stammdaten einmal geladen und in zwei `Map<UUID, String>` (Gegenstand- und Ausleiher-Namen) abgelegt. Die Tabellenspalten lesen den Anzeigenamen aus diesen Maps statt pro Zeile einen Service-Aufruf abzusetzen:

```java
gegenstandCol.setCellValueFactory(data -> new SimpleStringProperty(
    gegenstandNamen.getOrDefault(data.getValue().getGegenstandId(), "Unbekannt")));
```

**Wirkung:** Statt 2·N Einzelabfragen genügen zwei Listenabfragen pro Aktualisierung – unabhängig von der Zeilenzahl.

Sichtbar im Commit „Performance: N+1-Abfragen in den Ausleihen-Tabellen beseitigt".

### 5.3 Offene Verbesserungen

**Extract Method für die Namen-Validierung:** Der Validierungscode in den privaten Methoden `Ausleiher.setVorname`/`setNachname` und `Gegenstand.setName` folgt demselben Muster (Null-Check, `trim()`, Leer-Check, Längen-Check). Er ließe sich in eine Utility-Methode `validierterName(String input, String feldName, int maxLaenge)` auslagern.

Warum bewusst nicht umgesetzt: Die Stellen unterscheiden sich nur in der maximalen Länge (100 bzw. 200 Zeichen). Eine Utility-Methode dafür wäre vertretbar, aber für drei kurze Blöcke eine leichte Überabstraktion. Die Entscheidung ist hier „akzeptierte Duplikation" – beim nächsten Feld, das dasselbe Muster braucht, sollte die Auslagerung angepackt werden.

**Keine DTO-Schicht zwischen UI und Domäne:** Die `MainView` arbeitet direkt mit Domain-Objekten. Das ist eine bewusste Entscheidung gegen Overengineering und in Abschnitt 2.6.1 ausführlich begründet. Sobald eine Remote- oder Web-Schnittstelle hinzukäme, wären Read-Model-DTOs der nächste konsequente Schritt.

---

## 6. Entwurfsmuster

### 6.1 Builder Pattern im Aggregate Ausleihe

Als Entwurfsmuster wurde das **Builder Pattern** gewählt. Kein DDD-Muster, kein Singleton.

**Code (`domain/aggregates/Ausleihe.java`):**

```java
public class Ausleihe {

    private final UUID id;
    private final UUID gegenstandId;
    private final UUID ausleiherId;
    private final Zeitraum geplanterzeitraum;
    // ... weitere Felder

    private Ausleihe(Builder builder) {
        this.id = UUID.randomUUID();
        this.gegenstandId = Objects.requireNonNull(builder.gegenstandId);
        this.ausleiherId = Objects.requireNonNull(builder.ausleiherId);
        this.geplanterzeitraum = Objects.requireNonNull(builder.zeitraum);
        this.erstelltAm = LocalDateTime.now();
        this.status = AusleiheStatus.AKTIV;
    }

    public static class Builder {
        private UUID gegenstandId;
        private UUID ausleiherId;
        private Zeitraum zeitraum;

        public Builder gegenstand(Gegenstand gegenstand) {
            this.gegenstandId = Objects.requireNonNull(gegenstand).getId();
            return this;
        }

        public Builder ausleiher(Ausleiher ausleiher) {
            this.ausleiherId = Objects.requireNonNull(ausleiher).getId();
            return this;
        }

        public Builder zeitraum(Zeitraum zeitraum) {
            this.zeitraum = zeitraum;
            return this;
        }

        public Builder fuer(int tage) {
            this.zeitraum = Zeitraum.abHeute(tage);
            return this;
        }

        public Ausleihe build() {
            return new Ausleihe(this);
        }
    }
}
```

Der Builder akzeptiert komfortabel ein `Gegenstand`- oder `Ausleiher`-Objekt (Extraktion der UUID passiert intern), alternativ aber auch direkt eine UUID – das vereinfacht den Code aufrufender Services erheblich. Eine Convenience-Methode `fuer(int tage)` erzeugt den Zeitraum mit Heute als Startdatum.

### 6.2 Warum genau dieses Muster an dieser Stelle

Die `Ausleihe` hat drei Pflichtfelder und über die Zeit potenziell weitere optionale (aktuell `zustandsbericht` und `tatsaechlichesRueckgabedatum`, beide werden erst nach Abschluss gesetzt). Ein klassischer Konstruktor mit allen Parametern läuft in zwei Richtungen schief:

1. **Teleskop-Konstruktoren:** Für jede Kombination optionaler Parameter ein eigener Konstruktor → Wartungs-Alptraum.
2. **Setter-basierter Ansatz (JavaBeans):** Objekt kann zwischendurch in ungültigem Zustand sein. Bei einem Aggregate Root, das fachliche Invarianten durchsetzen soll, ist das inakzeptabel.

Der Builder löst beides: Pflichtfelder werden im `build()` geprüft (`Objects.requireNonNull` im Konstruktor), optional bleibt optional, das Ergebnis ist ein vollständig konsistentes Objekt – und lesbar wird der Call-Site-Code noch dazu.

### 6.3 Vorteile

- **Lesbarkeit:** `new Ausleihe.Builder().gegenstand(g).ausleiher(a).zeitraum(z).build()` sagt sich selbst.
- **Konsistenzgarantie:** Pflichtfelder werden im Konstruktor geprüft. Objekte ohne Pflichtfelder existieren nicht.
- **Erweiterbarkeit:** Neues optionales Feld → neue Builder-Methode, kein Konstruktor-Umbau.
- **Flexibilität bei Eingaben:** `gegenstand(Gegenstand)` und `gegenstandId(UUID)` koexistieren; Aufrufer mit unterschiedlichen Informationsständen werden beide bedient.

### 6.4 Nachteile

- **Mehr Boilerplate:** Innere Klasse, neun Methoden statt eines Konstruktors.
- **Komplexität:** Für ein Domain-Objekt mit zwei Feldern wäre das Overengineering. Bei `Ausleihe` mit acht Feldern inkl. Statusmanagement zahlt es sich aus.

### 6.5 Vergleich: Ohne Muster

Ein öffentlicher Konstruktor mit sieben Parametern wäre die naheliegende Alternative. Der Aufruf wäre

```java
new Ausleihe(gegenstandId, ausleiherId, zeitraum,
             LocalDateTime.now(), AusleiheStatus.AKTIV, null, null);
```

Das ist an der Aufrufstelle weder lesbar noch refactoring-freundlich. Jede Änderung an der Feldzusammensetzung bricht alle Aufrufstellen.

Ein zweiter Weg wäre ein Factory-Method-Pattern (`Ausleihe.neu(gegenstand, ausleiher, zeitraum)`). Für die aktuelle Feldmenge wäre das fast gleichwertig. Sobald ein weiterer optionaler Parameter hinzukommt, bräuchte man eine zweite Factory-Methode oder müsste zum Builder wechseln. Das Builder Pattern ist damit die zukunftsfähigere Entscheidung.

### 6.6 Weitere Patterns im Code

Das Builder Pattern ist das Hauptmuster. Daneben finden sich im Code kleinere, gut etablierte Konstrukte, die aber nicht explizit als „das" Entwurfsmuster gezählt werden:

- Factory-Methoden (`InventarNummer.of(int)`, `Zeitraum.abHeute(int)`, `Kategorie.of(String, int)`, `Ausleiher.neu(...)`, `Gegenstand.neu(...)`) für bequemere Objekterzeugung.
- Das Nested-Result-Pattern im `VerfuegbarkeitService` mit `VerfuegbarkeitErgebnis.verfuegbar()` / `nichtVerfuegbar(String)` – ein leichtgewichtiges „Result"-Objekt anstelle von booleschen Rückgaben mit Out-Parametern.

---

## Anhang – Projektstruktur und Kennzahlen

```
leihbar/
├── src/
│   ├── main/
│   │   ├── java/de/dhbw/leihbar/
│   │   │   ├── domain/
│   │   │   │   ├── aggregates/        Ausleihe.java, Gegenstand.java
│   │   │   │   ├── entities/          Ausleiher.java
│   │   │   │   ├── valueobjects/      InventarNummer, Zeitraum, Kontaktdaten,
│   │   │   │   │                      Kategorie, VerfuegbarkeitsStatus, AusleiheStatus
│   │   │   │   ├── repositories/      (Interfaces) GegenstandRepository,
│   │   │   │   │                      AusleiherRepository, AusleiheRepository
│   │   │   │   ├── services/          VerfuegbarkeitService, UeberfaelligkeitService
│   │   │   │   └── events/            DomainEvent, AusleiheErstelltEvent,
│   │   │   │                          AusleiheZurueckgegebenEvent
│   │   │   ├── application/services/  GegenstandService, AusleiherService, AusleiheService,
│   │   │   │                          TransactionRunner (Port)
│   │   │   ├── infrastructure/
│   │   │   │   └── persistence/       AbstractJpaRepository, Jpa*Repository-Implementierungen,
│   │   │   │                          Jpa*Entities, JpaTransactionRunner
│   │   │   ├── ui/views/              MainView
│   │   │   ├── LeihBarApplication.java  (Composition Root, JavaFX-Entry)
│   │   │   └── Launcher.java            (Fat-JAR Entry-Workaround)
│   │   └── resources/
│   │       ├── META-INF/persistence.xml
│   │       └── logback.xml
│   └── test/java/de/dhbw/leihbar/
│       ├── domain/
│       │   ├── aggregates/    GegenstandTest, AusleiheTest
│       │   ├── entities/      AusleiherTest
│       │   ├── valueobjects/  InventarNummerTest, ZeitraumTest, KontaktdatenTest
│       │   └── services/      VerfuegbarkeitServiceTest
│       ├── application/services/  AusleiheServiceTest, GegenstandServiceTest
│       └── infrastructure/persistence/  JpaGegenstandRepositoryIntegrationTest,
│                                        TransaktionsklammerIntegrationTest
└── pom.xml
```

**Kennzahlen der Abgabe**

| Kriterium aus der Aufgabenstellung | Wert im Projekt |
|-----------------------------------|-----------------|
| Objektorientierte JVM-Sprache | Java 21 |
| Zeilen Code (main + test) | ≈ 5800 |
| Klassen im Hauptcode | 32 |
| Persistenzschicht vorhanden | H2 via Hibernate/JPA |
| Ausführbar | JavaFX-Desktop-Anwendung |
| Testbar | `mvn test`, 95 Testausführungen, 0 Fails |
| Versionskontrolle | git, vollständige Historie |

---

*Erstellt für den Programmentwurf im Modul Advanced Software Engineering (ASE)*
*DHBW – TINF22B4*
