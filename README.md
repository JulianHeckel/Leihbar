# LeihBar - Ausleihe-Verwaltungssystem

Programmentwurf im Rahmen der Vorlesung **Advanced Software Engineering** (DHBW, TINF22B4).

LeihBar ist eine Desktop-Anwendung zur Verwaltung eines Ausleihe-Systems. Gegenstände können angelegt, verliehen und zurückgegeben werden. Das System trackt Fälligkeiten, Verfügbarkeit und den Zustand der Gegenstände.

## Features

- **Gegenstandsverwaltung** mit Kategorien, Status-Tracking und editierbarer Kategorie-ComboBox (Vorschläge + freie Eingabe)
- **Ausleihen** mit Zeitraum-Validierung, Zustandsbericht und automatischer Überfälligkeits-Erkennung
- **Dashboard** mit klickbaren Kennzahl-Karten für schnelle Navigation
- **Auto-Refresh** beim Tab-Wechsel — alle Daten bleiben konsistent
- **Farbkodierte Statusanzeige** in Gegenstände- und Ausleihen-Tabellen
- **Bearbeiten von Gegenständen** per Context-Menü oder Doppelklick
- **Erweiterte Suche** über Name, Beschreibung, Kategorie und Inventarnummer

## Technologien

| Bereich | Technologie |
|---------|-------------|
| Sprache | Java 21 |
| UI | JavaFX 21 |
| Persistenz | Hibernate 6 / JPA 3 |
| Datenbank | H2 (File-basiert) |
| Logging | Logback |
| Build | Maven |
| Testing | JUnit 5, Mockito, AssertJ |

## Projektstruktur

```
src/main/java/de/dhbw/leihbar/
├── domain/                  # Fachlogik (DDD)
│   ├── aggregates/          #   Gegenstand, Ausleihe
│   ├── entities/            #   Ausleiher
│   ├── valueobjects/        #   InventarNummer, Zeitraum, Kontaktdaten, ...
│   ├── repositories/        #   Repository-Interfaces
│   ├── services/            #   VerfuegbarkeitService, UeberfaelligkeitService
│   └── events/              #   Domain Events
├── application/services/    # Use Cases
├── infrastructure/persistence/  # JPA-Implementierung
└── ui/views/                # JavaFX UI
```

## Voraussetzungen

- **JDK 21** (z.B. Microsoft OpenJDK 21)
- **Maven 3.9+**

## Build & Start

**Empfohlen (Windows):** Das mitgelieferte Skript setzt JAVA_HOME auf ein installiertes JDK 21 und startet das Fat-JAR:

```bat
start_leihbar.bat
```

Voraussetzung für das Skript: Ein zuvor erstelltes Fat-JAR unter `target\leihbar-1.0.0.jar`. Falls noch nicht vorhanden:

```bash
mvn clean package
```

**Alternative (ohne Paketierung):** Über das JavaFX-Maven-Plugin direkt starten:

```bash
mvn javafx:run
```

**Hinweis:** Ein direkter Aufruf `java -jar target/leihbar-1.0.0.jar` funktioniert nur, wenn `java` im PATH tatsächlich auf ein JDK 21 zeigt. Wird dort eine ältere Java-Version genutzt, bricht der Start mit `UnsupportedClassVersionError` ab. In diesem Fall `start_leihbar.bat` verwenden oder JAVA_HOME entsprechend setzen.

## Schwerpunkte

- **Domain-Driven Design** mit Ubiquitous Language, Aggregates, Value Objects, Entities, Repositories und Domain Events
- **Clean Architecture** mit strikter Schichtentrennung (Domain → Application → Infrastructure → UI)
- **95 Tests** nach ATRIP-Regeln: Unit-Tests mit Mockito plus Integrationstests gegen eine H2-Datenbank
- **Entwurfsmuster:** Builder Pattern für die Ausleihe-Erstellung
- **Refactoring:** Dokumentierte Code Smells und durchgeführte Refactorings (Replace Primitive with Object, Introduce Builder, Extract Field, Extract Superclass, intentionsbasierte Methoden)
- **Atomare Use Cases:** Transaktionsklammer über mehrere Aggregate via `TransactionRunner`
