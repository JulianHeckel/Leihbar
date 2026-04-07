# LeihBar - Ausleihe-Verwaltungssystem

Programmentwurf im Rahmen der Vorlesung **Advanced Software Engineering** (DHBW, TINF22B4).

LeihBar ist eine Desktop-Anwendung zur Verwaltung eines Ausleihe-Systems. Gegenstände können angelegt, verliehen und zurückgegeben werden. Das System trackt Fälligkeiten, Verfügbarkeit und den Zustand der Gegenstände.

## Technologien

| Bereich | Technologie |
|---------|-------------|
| Sprache | Java 21 |
| UI | JavaFX 21 |
| Persistenz | Hibernate 6 / JPA 3 |
| Datenbank | H2 (File-basiert) |
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

## Build & Start

```bash
# Build (Fat-JAR)
mvn clean package -q

# Starten
java -jar target/leihbar-1.0.0.jar
```

Alternativ über die mitgelieferten Skripte:
```bash
./start_leihbar.bat
```

## Schwerpunkte

- **Domain-Driven Design** mit Ubiquitous Language, Aggregates, Value Objects, Entities, Repositories und Domain Events
- **Clean Architecture** mit strikter Schichtentrennung (Domain → Application → Infrastructure → UI)
- **89 Unit Tests** nach ATRIP-Regeln, inkl. Mocking
- **Entwurfsmuster:** Builder Pattern für die Ausleihe-Erstellung
- **Refactoring:** Dokumentierte Code Smells und durchgeführte Refactorings (Replace Primitive with Object, Introduce Parameter Object)
