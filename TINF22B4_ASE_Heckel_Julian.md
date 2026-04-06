# LeihBar - Technische Dokumentation

**Kurs:** TINF22B4
**Student:** Julian Heckel
**Prüfungsleistung:** Programmentwurf - Advanced Software Engineering

---

## Inhaltsverzeichnis

1. [Domain-Driven Design](#1-domain-driven-design)
2. [Clean Architecture](#2-clean-architecture)
3. [Programming Principles](#3-programming-principles)
4. [Unit Tests](#4-unit-tests)
5. [Refactoring](#5-refactoring)
6. [Entwurfsmuster](#6-entwurfsmuster)

---

## 1. Domain-Driven Design

### 1.1 Ubiquitous Language

Die LeihBar-Anwendung verwendet eine einheitliche Fachsprache, die sowohl im Code als auch in der Kommunikation konsistent verwendet wird:

| Begriff | Bedeutung | Verwendung im Code |
|---------|-----------|-------------------|
| **Gegenstand** | Ein ausleihbarer Artikel im Bestand | `Gegenstand` (Aggregate) |
| **Ausleiher** | Person, die Gegenstände ausleiht | `Ausleiher` (Entity) |
| **Ausleihe** | Der Vorgang des Ausleihens mit Zeitraum | `Ausleihe` (Aggregate) |
| **Inventarnummer** | Eindeutige Kennung eines Gegenstands | `InventarNummer` (Value Object) |
| **Zeitraum** | Start- und Enddatum einer Ausleihe | `Zeitraum` (Value Object) |
| **Kategorie** | Klassifizierung von Gegenständen | `Kategorie` (Value Object) |
| **Verfügbarkeit** | Status, ob ausleihbar | `VerfuegbarkeitsStatus` (Value Object) |
| **Rückgabe** | Beendigung einer Ausleihe | `zurueckgeben()` Methode |
| **Stornierung** | Abbruch einer Ausleihe | `stornieren()` Methode |
| **Überfällig** | Ausleihe über geplantem Rückgabedatum | `UEBERFAELLIG` Status |

### 1.2 Fachliche Regeln

1. **Ausleihregel:** Ein Gegenstand kann nur ausgeliehen werden, wenn er den Status `VERFUEGBAR` hat
2. **Zeitraumregel:** Das Enddatum einer Ausleihe muss nach oder am Startdatum liegen
3. **Kategorieregel:** Jede Kategorie hat eine maximale Ausleihdauer (z.B. Werkzeug: 30 Tage)
4. **Rückgaberegel:** Nur aktive Ausleihen können zurückgegeben werden
5. **Inventarnummer-Format:** Muss dem Pattern `INV-XXXX` entsprechen (X = Ziffer)

### 1.3 Taktische Muster

#### 1.3.1 Value Objects

Value Objects sind unveränderliche Objekte ohne eigene Identität, deren Gleichheit über ihre Werte definiert wird.

**Beispiel: InventarNummer** (`domain/valueobjects/InventarNummer.java`)

```java
public final class InventarNummer {
    private static final Pattern VALID_PATTERN = Pattern.compile("^INV-\\d{4}$");
    private final String value;

    public InventarNummer(String value) {
        Objects.requireNonNull(value, "Inventarnummer darf nicht null sein");
        String normalized = value.toUpperCase().trim();
        if (!VALID_PATTERN.matcher(normalized).matches()) {
            throw new IllegalArgumentException(
                "Ungültiges Inventarnummer-Format. Erwartet: INV-XXXX"
            );
        }
        this.value = normalized;
    }

    // Factory-Methode zur Erzeugung aus fortlaufender Nummer
    public static InventarNummer of(int nummer) {
        if (nummer < 0 || nummer > 9999) {
            throw new IllegalArgumentException("Nummer muss zwischen 0 und 9999 liegen");
        }
        return new InventarNummer("INV-" + String.format("%04d", nummer));
    }

    public String getValue() {
        return value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        return value.equals(((InventarNummer) o).value);
    }
}
```

**Begründung:** Die Inventarnummer ist ein perfektes Value Object, weil:
- Sie hat keine eigene Identität - zwei Inventarnummern mit gleichem Wert sind gleich
- Sie ist unveränderlich (immutable) - einmal erstellt, ändert sich der Wert nicht
- Sie validiert sich selbst im Konstruktor
- Sie kapselt das Format-Wissen

**Weitere Value Objects:**
- `Zeitraum`: Kapselt Start- und Enddatum (`von`/`bis`) mit Validierung und Überfälligkeitsprüfung
- `Kontaktdaten`: E-Mail (required) und Telefon (optional) mit Validierung
- `Kategorie`: Klasse mit Name und maximaler Ausleihdauer (Factory-Methode `of()`)
- `VerfuegbarkeitsStatus`: Enum für Gegenstandsstatus
- `AusleiheStatus`: Enum für Ausleihstatus

#### 1.3.2 Entities

Entities haben eine eindeutige Identität und einen Lebenszyklus.

**Beispiel: Ausleiher** (`domain/entities/Ausleiher.java`)

```java
public class Ausleiher {
    private final UUID id;
    private String vorname;
    private String nachname;
    private Kontaktdaten kontaktdaten;

    private Ausleiher(UUID id, String vorname, String nachname,
                      Kontaktdaten kontaktdaten) {
        this.id = Objects.requireNonNull(id);
        setVorname(vorname);
        setNachname(nachname);
        this.kontaktdaten = Objects.requireNonNull(kontaktdaten);
    }

    public static Ausleiher neu(String vorname, String nachname,
                                 Kontaktdaten kontaktdaten) {
        return new Ausleiher(UUID.randomUUID(), vorname, nachname, kontaktdaten);
    }

    // Gleichheit basiert auf ID, nicht auf Attributen
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        return Objects.equals(id, ((Ausleiher) o).id);
    }
}
```

**Begründung:** Ausleiher ist eine Entity, weil:
- Jeder Ausleiher hat eine eindeutige UUID als Identität
- Zwei Ausleiher mit gleichem Namen sind trotzdem verschiedene Personen
- Der Ausleiher hat einen Lebenszyklus (Anlegen, Ändern, Löschen)
- Attribute können sich ändern (z.B. Adressänderung), die Identität bleibt

#### 1.3.3 Aggregates

Aggregates sind Cluster von Entities und Value Objects mit einem Aggregate Root.

**Beispiel: Gegenstand** (`domain/aggregates/Gegenstand.java`)

```java
public class Gegenstand {
    private final UUID id;
    private final InventarNummer inventarNummer;
    private String name;
    private String beschreibung;
    private Kategorie kategorie;
    private VerfuegbarkeitsStatus status;

    // Factory-Methode zur Erzeugung eines neuen Gegenstandes
    public static Gegenstand neu(InventarNummer inventarNummer, String name,
                                  String beschreibung, Kategorie kategorie) {
        return new Gegenstand(UUID.randomUUID(), inventarNummer, name, beschreibung, kategorie);
    }

    // Statusübergänge werden vom Aggregate Root kontrolliert
    public void ausleihen() {
        if (!istVerfuegbar()) {
            throw new IllegalStateException(
                "Gegenstand kann nicht ausgeliehen werden. Status: " + status.getBezeichnung()
            );
        }
        this.status = VerfuegbarkeitsStatus.AUSGELIEHEN;
    }

    public void zurueckgeben() {
        if (status != VerfuegbarkeitsStatus.AUSGELIEHEN) {
            throw new IllegalStateException(
                "Gegenstand ist nicht ausgeliehen. Status: " + status.getBezeichnung()
            );
        }
        this.status = VerfuegbarkeitsStatus.VERFUEGBAR;
    }
}
```

**Begründung:** Gegenstand ist ein Aggregate, weil:
- Es ist die Transaktionsgrenze für Gegenstandsoperationen
- Es kontrolliert die Konsistenz seines Status (Invarianten)
- Externe Zugriffe erfolgen nur über die definierten Methoden
- Die InventarNummer ist Teil des Aggregates und wird nicht separat verwaltet

**Beispiel: Ausleihe** (`domain/aggregates/Ausleihe.java`)

```java
public class Ausleihe {
    private final UUID id;
    private final UUID gegenstandId;  // Referenz über ID, nicht Objekt!
    private final UUID ausleiherId;
    private final Zeitraum zeitraum;
    private AusleiheStatus status;
    private LocalDate tatsaechlichesRueckgabedatum;
    private String zustandsbericht;

    // Builder Pattern für komplexe Konstruktion
    public static class Builder {
        private Gegenstand gegenstand;
        private Ausleiher ausleiher;
        private Zeitraum zeitraum;

        public Builder gegenstand(Gegenstand gegenstand) {
            this.gegenstand = gegenstand;
            return this;
        }

        public Ausleihe build() {
            if (gegenstand == null || ausleiher == null || zeitraum == null) {
                throw new IllegalStateException("Pflichtfelder fehlen");
            }
            return new Ausleihe(this);
        }
    }
}
```

**Begründung:** Ausleihe referenziert andere Aggregates nur über IDs, nicht über Objektreferenzen. Dies entspricht der DDD-Regel zur Aggregate-Isolation.

#### 1.3.4 Repositories

Repositories abstrahieren den Datenzugriff und verwenden die Domänensprache.

**Beispiel: AusleiheRepository** (`domain/repositories/AusleiheRepository.java`)

```java
public interface AusleiheRepository {
    Ausleihe speichern(Ausleihe ausleihe);
    Optional<Ausleihe> findeNachId(UUID id);
    List<Ausleihe> findeAlle();
    List<Ausleihe> findeAktive();
    List<Ausleihe> findeUeberfaellige();
    List<Ausleihe> findeNachGegenstandId(UUID gegenstandId);
    Optional<Ausleihe> findeAktiveAusleiheVonGegenstand(UUID gegenstandId);
    void loeschen(UUID id);
    long zaehleAlle();
}
```

**Begründung:** Das Repository:
- Ist ein Interface in der Domain-Schicht (Dependency Inversion)
- Verwendet deutsche Fachbegriffe (Ubiquitous Language)
- Bietet domänenspezifische Abfragen (`findeUeberfaellige`, `findeAktive`)
- Abstrahiert die Persistenzdetails von der Domäne

#### 1.3.5 Domain Services

Domain Services enthalten Geschäftslogik, die nicht zu einer einzelnen Entity gehört.

**Beispiel: VerfuegbarkeitService** (`domain/services/VerfuegbarkeitService.java`)

```java
public class VerfuegbarkeitService {
    private final AusleiheRepository ausleiheRepository;

    public VerfuegbarkeitErgebnis pruefeVerfuegbarkeit(
            Gegenstand gegenstand, Zeitraum zeitraum) {
        // Prüfe Status
        if (!gegenstand.istVerfuegbar()) {
            return VerfuegbarkeitErgebnis.nichtVerfuegbar(
                "Gegenstand ist nicht verfügbar. Status: " +
                gegenstand.getStatus().getBezeichnung()
            );
        }

        // Prüfe bestehende Ausleihen
        var aktiveAusleihe = ausleiheRepository
            .findeAktiveAusleiheVonGegenstand(gegenstand.getId());
        if (aktiveAusleihe.isPresent()) {
            return VerfuegbarkeitErgebnis.nichtVerfuegbar(
                "Gegenstand ist bereits ausgeliehen"
            );
        }

        // Prüfe Kategorieregeln
        if (!gegenstand.getKategorie().istZeitraumErlaubt(zeitraum)) {
            return VerfuegbarkeitErgebnis.nichtVerfuegbar(
                "Ausleihdauer überschreitet Maximum"
            );
        }

        return VerfuegbarkeitErgebnis.verfuegbar();
    }
}
```

**Begründung:** Der VerfuegbarkeitService ist ein Domain Service, weil:
- Die Verfügbarkeitsprüfung benötigt Informationen aus mehreren Aggregates
- Sie gehört nicht zu einem einzelnen Gegenstand oder einer Ausleihe
- Sie kapselt komplexe Geschäftslogik (Status, bestehende Ausleihen, Kategorieregeln)

---

## 2. Clean Architecture

### 2.1 Schichtenarchitektur

Die LeihBar-Anwendung implementiert eine 4-Schichten-Architektur nach Clean Architecture:

```
┌─────────────────────────────────────────────────────────────┐
│                      UI Layer (JavaFX)                       │
│                         MainView                             │
├─────────────────────────────────────────────────────────────┤
│                   Application Layer                          │
│        AusleiheService, GegenstandService, AusleiherService  │
├─────────────────────────────────────────────────────────────┤
│                   Infrastructure Layer                       │
│     JpaAusleiheRepository, JpaGegenstandRepository, ...     │
├─────────────────────────────────────────────────────────────┤
│                      Domain Layer                            │
│   Entities, Value Objects, Aggregates, Repository-Interfaces │
└─────────────────────────────────────────────────────────────┘
                              ▲
                              │
                    Dependency Rule:
               Abhängigkeiten zeigen nach innen
```

### 2.2 Domain Layer (Innerste Schicht)

**Aufgabe:** Enthält die Kerngeschäftslogik, unabhängig von technischen Details.

**Komponenten:**
- `domain/valueobjects/` - Unveränderliche Wertobjekte
- `domain/entities/` - Entitäten mit Identität
- `domain/aggregates/` - Aggregate mit Konsistenzgrenzen
- `domain/repositories/` - Repository-Interfaces
- `domain/services/` - Domain Services
- `domain/events/` - Domain Events

**Beispiel-Package-Struktur:**
```
de.dhbw.leihbar.domain
├── aggregates
│   ├── Ausleihe.java
│   └── Gegenstand.java
├── entities
│   └── Ausleiher.java
├── valueobjects
│   ├── InventarNummer.java
│   ├── Zeitraum.java
│   ├── Kontaktdaten.java
│   ├── Kategorie.java
│   ├── VerfuegbarkeitsStatus.java
│   └── AusleiheStatus.java
├── repositories
│   ├── AusleiheRepository.java
│   ├── AusleiherRepository.java
│   └── GegenstandRepository.java
└── services
    ├── VerfuegbarkeitService.java
    └── UeberfaelligkeitService.java
```

### 2.3 Application Layer

**Aufgabe:** Orchestriert die Anwendungsfälle (Use Cases) und koordiniert Domain-Objekte.

**Komponenten:**
- `application/services/` - Application Services

**Beispiel: AusleiheService** (`application/services/AusleiheService.java`)

```java
public class AusleiheService {
    private final AusleiheRepository ausleiheRepository;
    private final GegenstandRepository gegenstandRepository;
    private final AusleiherRepository ausleiherRepository;
    private final VerfuegbarkeitService verfuegbarkeitService;

    public Ausleihe ausleihen(UUID gegenstandId, UUID ausleiherId,
                              LocalDate rueckgabedatum) {
        // 1. Lade Domänenobjekte
        Gegenstand gegenstand = gegenstandRepository.findeNachId(gegenstandId)
            .orElseThrow(() -> new IllegalArgumentException("Nicht gefunden"));

        // 2. Prüfe Geschäftsregeln (delegiert an Domain Service)
        var ergebnis = verfuegbarkeitService.pruefeVerfuegbarkeit(
            gegenstand, new Zeitraum(LocalDate.now(), rueckgabedatum));
        if (!ergebnis.istVerfuegbar()) {
            throw new IllegalStateException(ergebnis.getGrund());
        }

        // 3. Führe Domänenoperation aus
        gegenstand.ausleihen();
        Ausleihe ausleihe = new Ausleihe.Builder()
            .gegenstand(gegenstand)
            .ausleiher(ausleiher)
            .zeitraum(zeitraum)
            .build();

        // 4. Persistiere Änderungen
        gegenstandRepository.speichern(gegenstand);
        return ausleiheRepository.speichern(ausleihe);
    }
}
```

**Begründung:** Der Application Service:
- Kennt keine technischen Details (Datenbank, UI)
- Koordiniert mehrere Domain-Objekte
- Definiert Transaktionsgrenzen
- Delegiert Geschäftslogik an Domain Services

### 2.4 Infrastructure Layer

**Aufgabe:** Implementiert technische Details wie Datenbankzugriff.

**Komponenten:**
- `infrastructure/persistence/` - JPA Entities und Repository-Implementierungen

**Beispiel: JpaAusleiheRepository** (`infrastructure/persistence/JpaAusleiheRepository.java`)

```java
public class JpaAusleiheRepository implements AusleiheRepository {
    private final EntityManager entityManager;

    @Override
    public Ausleihe speichern(Ausleihe ausleihe) {
        entityManager.getTransaction().begin();
        AusleiheJpaEntity entity = AusleiheJpaEntity.fromDomain(ausleihe);
        entityManager.merge(entity);
        entityManager.getTransaction().commit();
        return ausleihe;
    }

    @Override
    public List<Ausleihe> findeAktive() {
        return entityManager.createQuery(
            "SELECT a FROM AusleiheJpaEntity a WHERE a.status IN :status",
            AusleiheJpaEntity.class)
            .setParameter("status", List.of("AKTIV", "UEBERFAELLIG"))
            .getResultList()
            .stream()
            .map(AusleiheJpaEntity::toDomain)
            .toList();
    }
}
```

**Begründung:**
- Implementiert das Domain-Interface
- Wandelt zwischen Domain-Objekten und JPA-Entities um
- Kapselt alle JPA/Hibernate-Details
- Die Domain-Schicht bleibt frei von Persistenz-Annotationen

### 2.5 UI Layer (Äußerste Schicht)

**Aufgabe:** Präsentationslogik und Benutzerinteraktion.

**Komponenten:**
- `ui/views/` - JavaFX Views
- `LeihBarApplication.java` - Hauptanwendung

**Beispiel: MainView** (`ui/views/MainView.java`)

```java
public class MainView extends BorderPane {
    private final GegenstandService gegenstandService;
    private final AusleiherService ausleiherService;
    private final AusleiheService ausleiheService;

    public MainView(GegenstandService gegenstandService,
                    AusleiherService ausleiherService,
                    AusleiheService ausleiheService) {
        this.gegenstandService = gegenstandService;
        this.ausleiherService = ausleiherService;
        this.ausleiheService = ausleiheService;
        initializeUI();
    }
}
```

**Begründung:**
- Kennt nur Application Services, nicht die Domain direkt
- Enthält keine Geschäftslogik
- Transformiert Benutzeraktionen in Service-Aufrufe

#### 2.5.1 UI-Funktionen

**Gegenstände-Verwaltung:**
- Erweiterte Suche: Sucht nach Name, Beschreibung, Kategorie und Inventarnummer
- Status-Filter: Dropdown zur Filterung nach Verfügbarkeitsstatus
- Zurücksetzen-Button: Setzt Suche und Filter zurück

```java
// Such- und Filterfunktion mit kombinierter Logik
Runnable filterAction = () -> {
    List<Gegenstand> results;
    if (suchbegriff != null && !suchbegriff.isBlank()) {
        results = gegenstandService.sucheGegenstaende(suchbegriff);
    } else {
        results = gegenstandService.alleGegenstaende();
    }
    // Status-Filter anwenden
    if (!"Alle Status".equals(selectedStatus)) {
        results = results.stream()
            .filter(g -> g.getStatus().getBezeichnung().equals(selectedStatus))
            .toList();
    }
    gegenstaendeTabelle.setItems(FXCollections.observableArrayList(results));
};
```

**Ausleihe-Dialog mit Validierung:**
- Zeigt maximale Ausleihdauer der gewählten Kategorie an
- Fehlerbehandlung mit Alert-Dialogen bei Validierungsfehlern
- Benutzerfreundliche Fehlermeldungen aus Domain-Exceptions

```java
try {
    return ausleiheService.ausleihen(gegenstandId, ausleiherId, rueckgabedatum);
} catch (IllegalStateException ex) {
    showError("Ausleihe nicht möglich", ex.getMessage());
    return null;
}
```

**Fehlerbehandlung:**
- Zentrale `showError()`-Methode für einheitliche Fehlerdialoge
- Domain-Exceptions werden dem Benutzer verständlich angezeigt
- Validierungsfehler (z.B. Ausleihdauer überschritten) werden abgefangen

### 2.6 Dependency Rule

Die Abhängigkeiten zeigen immer nach innen:

```
UI → Application → Infrastructure
         ↓              ↓
      Domain ←──────────┘
```

- **UI** hängt von Application Services ab
- **Application** hängt von Domain und Repository-Interfaces ab
- **Infrastructure** implementiert Domain-Interfaces
- **Domain** hat keine Abhängigkeiten nach außen

---

## 3. Programming Principles

### 3.1 Single Responsibility Principle (SRP)

**Das Prinzip besagt:** Eine Klasse sollte nur einen Grund zur Änderung haben.

**Anwendung in `VerfuegbarkeitService.java`:**

```java
public class VerfuegbarkeitService {
    // Einzige Verantwortung: Verfügbarkeitsprüfung
    public VerfuegbarkeitErgebnis pruefeVerfuegbarkeit(
            Gegenstand gegenstand, Zeitraum zeitraum) {
        // Prüft nur Verfügbarkeit - keine Persistenz, keine UI
    }
}
```

**Begründung:** Der VerfuegbarkeitService hat genau eine Verantwortung: Die Prüfung, ob ein Gegenstand ausgeliehen werden kann. Er speichert keine Daten, zeigt keine UI an und versendet keine Benachrichtigungen. Wenn sich die Verfügbarkeitsregeln ändern, ändert sich nur diese Klasse.

### 3.2 Open/Closed Principle (OCP)

**Das Prinzip besagt:** Klassen sollten offen für Erweiterung, aber geschlossen für Modifikation sein.

**Anwendung in `Kategorie.java`:**

```java
public final class Kategorie {
    private final String name;
    private final int maxAusleihdauerTage;

    // Factory-Methode für einfache Erstellung
    public static Kategorie of(String name, int maxAusleihdauerTage) {
        return new Kategorie(name, maxAusleihdauerTage);
    }

    // Prüft, ob ein Zeitraum die maximale Ausleihdauer überschreitet
    public boolean istZeitraumErlaubt(Zeitraum zeitraum) {
        return zeitraum.getDauerInTagen() <= maxAusleihdauerTage;
    }
}

// Verwendung:
Kategorie werkzeug = Kategorie.of("Werkzeug", 30);
Kategorie elektronik = Kategorie.of("Elektronik", 14);
```

**Begründung:** Kategorien werden als Value Object mit Factory-Methode modelliert. Neue Kategorien können flexibel erstellt werden, ohne den Code ändern zu müssen. Die `istZeitraumErlaubt`-Methode funktioniert für alle Kategorien gleich.

### 3.3 Dependency Inversion Principle (DIP)

**Das Prinzip besagt:** Hochrangige Module sollten nicht von niederrangigen abhängen. Beide sollten von Abstraktionen abhängen.

**Anwendung in der Repository-Struktur:**

```java
// Domain-Schicht definiert das Interface
public interface AusleiheRepository {
    Ausleihe speichern(Ausleihe ausleihe);
    Optional<Ausleihe> findeNachId(UUID id);
}

// Application-Schicht nutzt das Interface
public class AusleiheService {
    private final AusleiheRepository ausleiheRepository;

    public AusleiheService(AusleiheRepository ausleiheRepository) {
        this.ausleiheRepository = ausleiheRepository;
    }
}

// Infrastructure-Schicht implementiert das Interface
public class JpaAusleiheRepository implements AusleiheRepository {
    // JPA-spezifische Implementierung
}
```

**Begründung:** Die Domain-Schicht (hochrangig) hängt nicht von der Infrastructure-Schicht (niederrangig) ab. Stattdessen definiert die Domain ein Interface, das die Infrastructure implementiert. Dies ermöglicht den Austausch der Persistenz-Technologie ohne Änderung der Geschäftslogik.

### 3.4 Information Expert (GRASP)

**Das Prinzip besagt:** Weise eine Verantwortung der Klasse zu, die die notwendigen Informationen hat.

**Anwendung in `Gegenstand.java`:**

```java
public class Gegenstand {
    private VerfuegbarkeitsStatus status;
    private Kategorie kategorie;

    // Gegenstand kennt seinen Status - er ist der Information Expert
    public boolean istVerfuegbar() {
        return status == VerfuegbarkeitsStatus.VERFUEGBAR;
    }

    // Statusänderungen werden vom Gegenstand selbst kontrolliert
    public void ausleihen() {
        if (!istVerfuegbar()) {
            throw new IllegalStateException("Nicht verfügbar");
        }
        this.status = VerfuegbarkeitsStatus.AUSGELIEHEN;
    }
}
```

**Begründung:** Der Gegenstand selbst weiß am besten über seinen Status Bescheid. Daher liegt die Logik zur Statusprüfung und -änderung beim Gegenstand, nicht in einem externen Service. Dies vermeidet "Feature Envy" und führt zu hoher Kohäsion.

### 3.5 Don't Repeat Yourself (DRY)

**Das Prinzip besagt:** Jedes Stück Wissen sollte eine einzige, eindeutige Repräsentation im System haben.

**Anwendung in `Zeitraum.java`:**

```java
public final class Zeitraum {
    private final LocalDate von;
    private final LocalDate bis;

    // Überfälligkeitslogik ist zentral definiert
    public boolean istUeberfaellig() {
        return LocalDate.now().isAfter(bis);
    }

    public long getDauerInTagen() {
        return ChronoUnit.DAYS.between(von, bis);
    }
}
```

**Begründung:** Die Berechnung der Überfälligkeit ist im `Zeitraum` Value Object zentral definiert. Jede Stelle, die wissen muss ob ein Zeitraum überfällig ist, ruft `zeitraum.istUeberfaellig()` auf. Die Logik existiert nur einmal und kann bei Bedarf zentral angepasst werden.

---

## 4. Unit Tests

### 4.1 ATRIP-Regeln

Die Unit Tests folgen den ATRIP-Regeln:

| Regel | Umsetzung |
|-------|-----------|
| **A**utomatic | Tests werden mit JUnit 5 automatisch ausgeführt |
| **T**horough | Positive und negative Testfälle, Grenzwerte |
| **R**epeatable | Keine externen Abhängigkeiten, deterministische Tests |
| **I**ndependent | Jeder Test ist unabhängig (@BeforeEach Setup) |
| **P**rofessional | Aussagekräftige Namen, Arrange-Act-Assert Pattern |

### 4.2 Testübersicht

| Testklasse | Anzahl Tests | Getestete Klasse |
|------------|--------------|------------------|
| `InventarNummerTest` | 17 | Value Object InventarNummer |
| `ZeitraumTest` | 8 | Value Object Zeitraum |
| `KontaktdatenTest` | 12 | Value Object Kontaktdaten |
| `GegenstandTest` | 10 | Aggregate Gegenstand |
| `AusleiheTest` | 11 | Aggregate Ausleihe |
| `AusleiherTest` | 10 | Entity Ausleiher |
| `VerfuegbarkeitServiceTest` | 7 | Domain Service mit Mocks |
| `AusleiheServiceTest` | 7 | Application Service mit Mocks |
| `GegenstandServiceTest` | 7 | Application Service mit Mocks |

**Gesamt: 89 Tests, davon 21 mit Mocks**

### 4.3 Beispiel: Value Object Test

```java
@DisplayName("InventarNummer Value Object Tests")
class InventarNummerTest {

    @Test
    @DisplayName("Sollte gültige Inventarnummer erstellen")
    void sollteGueltigeInventarnummerErstellen() {
        // Arrange & Act
        InventarNummer nummer = InventarNummer.of(1234);

        // Assert
        assertNotNull(nummer);
        assertEquals("INV-1234", nummer.getValue());
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "INV", "INV-", "INV-123", "ABC-1234"})
    @DisplayName("Sollte ungültige Formate ablehnen")
    void sollteUngueltigeFormateAblehnen(String value) {
        assertThrows(IllegalArgumentException.class,
            () -> new InventarNummer(value));
    }
}
```

### 4.4 Beispiel: Test mit Mocks

```java
@ExtendWith(MockitoExtension.class)
@DisplayName("AusleiheService Tests")
class AusleiheServiceTest {

    @Mock
    private AusleiheRepository ausleiheRepository;
    @Mock
    private GegenstandRepository gegenstandRepository;
    @Mock
    private VerfuegbarkeitService verfuegbarkeitService;

    @Test
    @DisplayName("Sollte Ausleihe erfolgreich erstellen")
    void sollteAusleiheErfolgreichErstellen() {
        // Arrange
        when(gegenstandRepository.findeNachId(gegenstandId))
            .thenReturn(Optional.of(gegenstand));
        when(verfuegbarkeitService.pruefeVerfuegbarkeit(any(), any()))
            .thenReturn(VerfuegbarkeitErgebnis.verfuegbar());
        when(ausleiheRepository.speichern(any()))
            .thenAnswer(inv -> inv.getArgument(0));

        // Act
        Ausleihe ausleihe = ausleiheService.ausleihen(
            gegenstandId, ausleiherId, rueckgabedatum);

        // Assert
        assertNotNull(ausleihe);
        verify(verfuegbarkeitService).pruefeVerfuegbarkeit(any(), any());
        verify(ausleiheRepository).speichern(any(Ausleihe.class));
    }
}
```

**Begründung für Mocks:** Die Mocks ermöglichen:
- Isolierte Tests der Geschäftslogik ohne Datenbank
- Kontrolle über das Verhalten der Abhängigkeiten
- Verifizierung der Interaktionen zwischen Komponenten

---

## 5. Refactoring

### 5.1 Identifizierte Code Smells

#### 5.1.1 Primitive Obsession

**Beschreibung:** Verwendung primitiver Datentypen anstelle von kleinen Objekten.

**Beispiel (vor Refactoring):**
```java
public class Gegenstand {
    private String inventarnummer; // Primitive!
    private String email;          // Primitive!
}
```

**Problem:** Keine Validierung, Duplikation der Validierungslogik, keine Typsicherheit.

#### 5.1.2 Feature Envy

**Beschreibung:** Eine Methode nutzt mehr Daten einer anderen Klasse als der eigenen.

**Beispiel (vor Refactoring):**
```java
public class AusleiheService {
    public boolean istGegenstandVerfuegbar(Gegenstand g) {
        return g.getStatus() == VerfuegbarkeitsStatus.VERFUEGBAR
            && !hatAktiveAusleihe(g.getId());
    }
}
```

**Problem:** Die Logik gehört zum Gegenstand, nicht zum Service.

#### 5.1.3 Long Parameter List

**Beschreibung:** Methoden mit vielen Parametern.

**Beispiel (vor Refactoring):**
```java
public Ausleihe erstelleAusleihe(UUID gegenstandId, UUID ausleiherId,
    LocalDate startdatum, LocalDate enddatum, String notiz) {
    // ...
}
```

**Problem:** Schwer lesbar, fehleranfällig, Parameter könnten verwechselt werden.

#### 5.1.4 Divergent Change

**Beschreibung:** Eine Klasse muss bei verschiedenen Änderungsgründen angepasst werden.

**Beispiel (vor Refactoring):**
```java
public class GegenstandManager {
    public void speichern(Gegenstand g) { /* DB-Logik */ }
    public void pruefeVerfuegbarkeit(Gegenstand g) { /* Business-Logik */ }
    public String formatiereFuerAnzeige(Gegenstand g) { /* UI-Logik */ }
}
```

**Problem:** Änderungen an DB, Geschäftsregeln oder UI erfordern Änderungen in derselben Klasse.

### 5.2 Durchgeführte Refactorings

#### 5.2.1 Replace Primitive with Object

**Anwendung:** Inventarnummer und Kontaktdaten wurden zu Value Objects.

**Vorher:**
```java
public class Gegenstand {
    private String inventarnummer;
}
```

**Nachher:**
```java
public class Gegenstand {
    private final InventarNummer inventarNummer;
}

public final class InventarNummer {
    private static final Pattern VALID_PATTERN = Pattern.compile("^INV-\\d{4}$");
    private final String value;

    public InventarNummer(String value) {
        if (!VALID_PATTERN.matcher(value).matches()) {
            throw new IllegalArgumentException("Ungültiges Format");
        }
        this.value = value;
    }

    public static InventarNummer of(int nummer) {
        return new InventarNummer("INV-" + String.format("%04d", nummer));
    }
}
```

**Begründung:**
- Validierung ist gekapselt und zentral
- Typsicherheit: Compiler verhindert Verwechslung
- Self-Documenting Code: `InventarNummer` statt `String`
- Gleichheitslogik ist korrekt implementiert

#### 5.2.2 Introduce Parameter Object (Builder Pattern)

**Anwendung:** Ausleihe-Erstellung mit Builder Pattern.

**Vorher:**
```java
public Ausleihe(UUID gegenstandId, UUID ausleiherId,
    LocalDate start, LocalDate ende, String notiz) {
    this.gegenstandId = gegenstandId;
    this.ausleiherId = ausleiherId;
    // ...
}
```

**Nachher:**
```java
public class Ausleihe {
    private Ausleihe(Builder builder) {
        this.gegenstandId = builder.gegenstand.getId();
        this.ausleiherId = builder.ausleiher.getId();
        this.zeitraum = builder.zeitraum;
    }

    public static class Builder {
        private Gegenstand gegenstand;
        private Ausleiher ausleiher;
        private Zeitraum zeitraum;

        public Builder gegenstand(Gegenstand g) {
            this.gegenstand = g;
            return this;
        }

        public Ausleihe build() {
            validate();
            return new Ausleihe(this);
        }
    }
}

// Verwendung:
Ausleihe ausleihe = new Ausleihe.Builder()
    .gegenstand(gegenstand)
    .ausleiher(ausleiher)
    .zeitraum(zeitraum)
    .build();
```

**Begründung:**
- Lesbare, selbstdokumentierende API
- Validierung im `build()` - inkonsistente Objekte unmöglich
- Optionale Parameter ohne Teleskop-Konstruktoren
- Immutable Objekt nach Erstellung

### 5.3 Test-Code Refactoring: API-Alignment

Während der Test-Entwicklung wurden mehrere Inkonsistenzen zwischen Test-Code und Domain-API identifiziert und behoben. Diese Refactorings dokumentieren den iterativen Prozess der API-Stabilisierung.

#### 5.3.1 Value Object API-Inkonsistenzen

**Problem: Inkonsistente Getter-Namen in Zeitraum**

| Test-Code (vorher) | Domain-API (korrekt) | Begründung |
|-------------------|---------------------|------------|
| `getStartdatum()` | `getVon()` | Kürzere, prägnantere Benennung |
| `getEnddatum()` | `getBis()` | Konsistenz mit `getVon()` |
| `ueberschneidetSich()` | `ueberschneidetSichMit()` | Grammatikalisch korrekte Reflexivform |

**Refactoring in ZeitraumTest.java:**
```java
// Vorher
assertEquals(start, zeitraum.getStartdatum());
assertEquals(ende, zeitraum.getEnddatum());
assertTrue(zeitraum1.ueberschneidetSich(zeitraum2));

// Nachher
assertEquals(start, zeitraum.getVon());
assertEquals(ende, zeitraum.getBis());
assertTrue(zeitraum1.ueberschneidetSichMit(zeitraum2));
```

**Problem: Optional vs. direkter Rückgabetyp bei Kontaktdaten**

| Test-Code (vorher) | Domain-API (korrekt) | Begründung |
|-------------------|---------------------|------------|
| `getTelefon().isPresent()` | `hatTelefon()` | Explizite Prüfmethode statt Optional-Unwrapping |
| `getTelefon().orElse(null)` | `getTelefon()` (nullable) | Einfachere API für optionale Werte |

**Refactoring in KontaktdatenTest.java:**
```java
// Vorher
assertTrue(kontakt.getTelefon().isPresent());
assertFalse(kontakt.getTelefon().isPresent());

// Nachher
assertTrue(kontakt.hatTelefon());
assertFalse(kontakt.hatTelefon());
assertNull(kontakt.getTelefon());
```

**Problem: Fehlende Factory-Methode bei InventarNummer**

| Test-Code (vorher) | Domain-API (korrekt) | Begründung |
|-------------------|---------------------|------------|
| `InventarNummer.generiere()` | `InventarNummer.of(int)` | Kontrollierte Nummernvergabe statt Zufallsgenerierung |
| `getWert()` | `getValue()` | Englische Konvention für technische API |

**Refactoring in allen Test-Dateien:**
```java
// Vorher
InventarNummer nummer = InventarNummer.generiere();
assertEquals("INV-1234", nummer.getWert());

// Nachher
InventarNummer nummer = InventarNummer.of(1);
assertEquals("INV-0001", nummer.getValue());
```

#### 5.3.2 Entity API-Inkonsistenzen

**Problem: Inkonsistente Methoden bei Ausleiher**

| Test-Code (vorher) | Domain-API (korrekt) | Begründung |
|-------------------|---------------------|------------|
| `getVollstaendigerName()` | `getVollerName()` | Kürzere, gebräuchlichere Form |
| `Ausleiher.mitId(id, ...)` | `new Ausleiher(id, ...)` | Direkter Konstruktor für Rekonstruktion |

**Refactoring in AusleiherTest.java:**
```java
// Vorher
assertEquals("Max Mustermann", ausleiher.getVollstaendigerName());
Ausleiher ausleiher = Ausleiher.mitId(id, "Anna", "Schmidt", kontakt);

// Nachher
assertEquals("Max Mustermann", ausleiher.getVollerName());
Ausleiher ausleiher = new Ausleiher(id, "Anna", "Schmidt", kontakt);
```

#### 5.3.3 Aggregate API-Inkonsistenzen

**Problem: Inkonsistente Methoden bei Gegenstand**

| Test-Code (vorher) | Domain-API (korrekt) | Begründung |
|-------------------|---------------------|------------|
| `getBezeichnung()` | `getName()` | Konsistenz mit Entity-Konvention |
| `inWartung()` | `inWartungSetzen()` | Aktive Verbform für Statusänderung |
| `wartungAbschliessen()` | `wartungBeenden()` | Konsistenz mit `inWartungSetzen()` |
| `Gegenstand.neu(name, ...)` | `Gegenstand.neu(inventarNr, name, ...)` | InventarNummer ist Pflichtparameter |

**Refactoring in GegenstandTest.java:**
```java
// Vorher
Gegenstand g = Gegenstand.neu("Bohrmaschine", "Bosch", kategorie);
assertEquals("Bohrmaschine", g.getBezeichnung());
g.inWartung();
g.wartungAbschliessen();

// Nachher
Gegenstand g = Gegenstand.neu(InventarNummer.of(1), "Bohrmaschine", "Bosch", kategorie);
assertEquals("Bohrmaschine", g.getName());
g.inWartungSetzen();
g.wartungBeenden();
```

**Problem: Kategorie als Enum vs. Klasse**

| Test-Code (vorher) | Domain-API (korrekt) | Begründung |
|-------------------|---------------------|------------|
| `Kategorie.WERKZEUG` | `Kategorie.of("Werkzeug", 30)` | Flexiblere Kategorienverwaltung |

**Refactoring in allen Test-Dateien:**
```java
// Vorher
Kategorie kategorie = Kategorie.WERKZEUG;

// Nachher
Kategorie kategorie = Kategorie.of("Werkzeug", 30);
```

**Problem: Ausleihe Zeitraum-Zugriff**

| Test-Code (vorher) | Domain-API (korrekt) | Begründung |
|-------------------|---------------------|------------|
| `getZeitraum()` | `getGeplanterZeitraum()` | Unterscheidung zu tatsächlichem Zeitraum |
| `getZustandsbericht().get()` | `getZustandsbericht()` | Direkte Rückgabe statt Optional |

**Refactoring in AusleiheServiceTest.java:**
```java
// Vorher
assertEquals(LocalDate.now(), ausleihe.getZeitraum().getStartdatum());
assertEquals("Gut erhalten", ausleihe.getZustandsbericht().get());

// Nachher
assertEquals(LocalDate.now(), ausleihe.getGeplanterZeitraum().getVon());
assertEquals("Gut erhalten", ausleihe.getZustandsbericht());
```

#### 5.3.4 Zusammenfassung der Refactorings

| Kategorie | Anzahl Fixes | Betroffene Dateien |
|-----------|-------------|-------------------|
| Value Object Getter | 8 | ZeitraumTest, KontaktdatenTest, InventarNummerTest |
| Entity Methoden | 3 | AusleiherTest |
| Aggregate Methoden | 12 | GegenstandTest, AusleiheTest |
| Service Tests | 15 | VerfuegbarkeitServiceTest, AusleiheServiceTest, GegenstandServiceTest |
| **Gesamt** | **38** | **9 Test-Dateien** |

**Ergebnis:** Nach den Refactorings kompilieren alle 89 Tests erfolgreich und laufen fehlerfrei durch.

---

## 6. Entwurfsmuster

### 6.1 Builder Pattern

**Kategorie:** Erzeugungsmuster (Creational Pattern)

**Implementierung in:** `domain/aggregates/Ausleihe.java`

```java
public class Ausleihe {
    private final UUID id;
    private final UUID gegenstandId;
    private final UUID ausleiherId;
    private final Zeitraum zeitraum;
    private AusleiheStatus status;

    private Ausleihe(Builder builder) {
        this.id = UUID.randomUUID();
        this.gegenstandId = builder.gegenstand.getId();
        this.ausleiherId = builder.ausleiher.getId();
        this.zeitraum = builder.zeitraum;
        this.status = AusleiheStatus.AKTIV;
    }

    public static class Builder {
        private Gegenstand gegenstand;
        private Ausleiher ausleiher;
        private Zeitraum zeitraum;

        public Builder gegenstand(Gegenstand gegenstand) {
            this.gegenstand = Objects.requireNonNull(gegenstand);
            return this;
        }

        public Builder ausleiher(Ausleiher ausleiher) {
            this.ausleiher = Objects.requireNonNull(ausleiher);
            return this;
        }

        public Builder zeitraum(Zeitraum zeitraum) {
            this.zeitraum = Objects.requireNonNull(zeitraum);
            return this;
        }

        public Ausleihe build() {
            if (gegenstand == null) {
                throw new IllegalStateException("Gegenstand ist erforderlich");
            }
            if (ausleiher == null) {
                throw new IllegalStateException("Ausleiher ist erforderlich");
            }
            if (zeitraum == null) {
                throw new IllegalStateException("Zeitraum ist erforderlich");
            }
            return new Ausleihe(this);
        }
    }
}
```

### 6.2 Warum Builder Pattern?

**Problemstellung:**
Die `Ausleihe` ist ein komplexes Aggregate mit mehreren Pflichtfeldern. Ein Konstruktor mit vielen Parametern wäre:
- Schwer lesbar
- Fehleranfällig (Parameter verwechselbar)
- Nicht erweiterbar (neue optionale Felder = neue Konstruktoren)

**Vorteile des Builder Patterns:**

1. **Lesbarkeit:** Der Code ist selbstdokumentierend
   ```java
   new Ausleihe.Builder()
       .gegenstand(bohrmaschine)
       .ausleiher(maxMustermann)
       .zeitraum(naechsteWoche)
       .build();
   ```

2. **Validierung:** Die `build()`-Methode stellt sicher, dass nur gültige Objekte entstehen

3. **Immutabilität:** Nach der Erstellung kann das Objekt nicht mehr ungültig werden

4. **Erweiterbarkeit:** Neue optionale Felder können ohne Breaking Changes hinzugefügt werden

**Nachteile:**
- Mehr Code (Builder-Klasse)
- Etwas höhere Komplexität

**Abwägung:** Die Vorteile überwiegen für ein Domain-Aggregate, das eine zentrale Rolle im System spielt und dessen Konsistenz kritisch ist.

---

## Anhang: Projektstruktur

```
leihbar/
├── src/
│   ├── main/
│   │   ├── java/de/dhbw/leihbar/
│   │   │   ├── domain/
│   │   │   │   ├── aggregates/
│   │   │   │   │   ├── Ausleihe.java
│   │   │   │   │   └── Gegenstand.java
│   │   │   │   ├── entities/
│   │   │   │   │   └── Ausleiher.java
│   │   │   │   ├── valueobjects/
│   │   │   │   │   ├── InventarNummer.java
│   │   │   │   │   ├── Zeitraum.java
│   │   │   │   │   ├── Kontaktdaten.java
│   │   │   │   │   ├── Kategorie.java
│   │   │   │   │   ├── VerfuegbarkeitsStatus.java
│   │   │   │   │   └── AusleiheStatus.java
│   │   │   │   ├── repositories/
│   │   │   │   │   ├── AusleiheRepository.java
│   │   │   │   │   ├── AusleiherRepository.java
│   │   │   │   │   └── GegenstandRepository.java
│   │   │   │   ├── services/
│   │   │   │   │   ├── VerfuegbarkeitService.java
│   │   │   │   │   └── UeberfaelligkeitService.java
│   │   │   │   └── events/
│   │   │   │       ├── DomainEvent.java
│   │   │   │       ├── AusleiheErstelltEvent.java
│   │   │   │       └── AusleiheZurueckgegebenEvent.java
│   │   │   ├── application/
│   │   │   │   └── services/
│   │   │   │       ├── AusleiheService.java
│   │   │   │       ├── AusleiherService.java
│   │   │   │       └── GegenstandService.java
│   │   │   ├── infrastructure/
│   │   │   │   └── persistence/
│   │   │   │       ├── AusleiheJpaEntity.java
│   │   │   │       ├── AusleiherJpaEntity.java
│   │   │   │       ├── GegenstandJpaEntity.java
│   │   │   │       ├── JpaAusleiheRepository.java
│   │   │   │       ├── JpaAusleiherRepository.java
│   │   │   │       └── JpaGegenstandRepository.java
│   │   │   ├── ui/
│   │   │   │   └── views/
│   │   │   │       └── MainView.java
│   │   │   └── LeihBarApplication.java
│   │   └── resources/
│   │       └── META-INF/
│   │           └── persistence.xml
│   └── test/
│       └── java/de/dhbw/leihbar/
│           ├── domain/
│           │   ├── aggregates/
│           │   ├── entities/
│           │   ├── valueobjects/
│           │   └── services/
│           └── application/
│               └── services/
└── pom.xml
```

---

*Erstellt für den Programmentwurf im Modul Advanced Software Engineering (ASE)*
*DHBW Mannheim - TINF22B4*
