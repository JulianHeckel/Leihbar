package de.dhbw.leihbar.domain.services;

import de.dhbw.leihbar.domain.aggregates.Ausleihe;
import de.dhbw.leihbar.domain.aggregates.Gegenstand;
import de.dhbw.leihbar.domain.entities.Ausleiher;
import de.dhbw.leihbar.domain.repositories.AusleiheRepository;
import de.dhbw.leihbar.domain.valueobjects.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Unit Tests für den Domain Service VerfuegbarkeitService.
 * Verwendet Mockito für das AusleiheRepository.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("VerfuegbarkeitService Domain Service Tests")
class VerfuegbarkeitServiceTest {

    @Mock
    private AusleiheRepository ausleiheRepository;

    private VerfuegbarkeitService verfuegbarkeitService;
    private Gegenstand gegenstand;
    private Ausleiher ausleiher;
    private Zeitraum zeitraum;
    private Kategorie werkzeug;

    @BeforeEach
    void setUp() {
        verfuegbarkeitService = new VerfuegbarkeitService(ausleiheRepository);
        werkzeug = Kategorie.of("Werkzeug", 30);
        gegenstand = Gegenstand.neu(
            InventarNummer.of(1),
            "Bohrmaschine",
            "Bosch Professional",
            werkzeug
        );
        ausleiher = Ausleiher.neu(
            "Max",
            "Mustermann",
            Kontaktdaten.nurEmail("max@example.com")
        );
        zeitraum = new Zeitraum(
            LocalDate.now(),
            LocalDate.now().plusDays(7)
        );
    }

    @Test
    @DisplayName("Sollte verfügbaren Gegenstand als verfügbar erkennen")
    void sollteVerfuegbarenGegenstandAlsVerfuegbarErkennen() {
        // Arrange
        when(ausleiheRepository.findeAktiveAusleiheVonGegenstand(gegenstand.getId()))
            .thenReturn(Optional.empty());

        // Act
        VerfuegbarkeitService.VerfuegbarkeitErgebnis ergebnis =
            verfuegbarkeitService.pruefeVerfuegbarkeit(gegenstand, zeitraum);

        // Assert
        assertTrue(ergebnis.istVerfuegbar());
        verify(ausleiheRepository).findeAktiveAusleiheVonGegenstand(gegenstand.getId());
    }

    @Test
    @DisplayName("Sollte nicht verfügbaren Gegenstand erkennen")
    void sollteNichtVerfuegbarenGegenstandErkennen() {
        // Arrange
        gegenstand.ausleihen(); // Status auf AUSGELIEHEN setzen

        // Act
        VerfuegbarkeitService.VerfuegbarkeitErgebnis ergebnis =
            verfuegbarkeitService.pruefeVerfuegbarkeit(gegenstand, zeitraum);

        // Assert
        assertFalse(ergebnis.istVerfuegbar());
        assertNotNull(ergebnis.getGrund());
        assertTrue(ergebnis.getGrund().contains("nicht verfügbar"));
    }

    @Test
    @DisplayName("Sollte Gegenstand in Wartung als nicht verfügbar erkennen")
    void sollteGegenstandInWartungAlsNichtVerfuegbarErkennen() {
        // Arrange
        gegenstand.inWartungSetzen();

        // Act
        VerfuegbarkeitService.VerfuegbarkeitErgebnis ergebnis =
            verfuegbarkeitService.pruefeVerfuegbarkeit(gegenstand, zeitraum);

        // Assert
        assertFalse(ergebnis.istVerfuegbar());
        assertTrue(ergebnis.getGrund().contains("nicht verfügbar"));
    }

    @Test
    @DisplayName("Sollte ausgemusterten Gegenstand als nicht verfügbar erkennen")
    void sollteAusgemustertenGegenstandAlsNichtVerfuegbarErkennen() {
        // Arrange
        gegenstand.ausmustern();

        // Act
        VerfuegbarkeitService.VerfuegbarkeitErgebnis ergebnis =
            verfuegbarkeitService.pruefeVerfuegbarkeit(gegenstand, zeitraum);

        // Assert
        assertFalse(ergebnis.istVerfuegbar());
        assertTrue(ergebnis.getGrund().contains("nicht verfügbar"));
    }

    @Test
    @DisplayName("Sollte Zeitraumüberschreitung der Kategorie erkennen")
    void sollteZeitraumueberschreitungErkennen() {
        // Arrange - Zeitraum länger als Kategorie erlaubt
        Zeitraum langerZeitraum = new Zeitraum(
            LocalDate.now(),
            LocalDate.now().plusDays(100) // Länger als Werkzeug erlaubt (30 Tage)
        );
        when(ausleiheRepository.findeAktiveAusleiheVonGegenstand(gegenstand.getId()))
            .thenReturn(Optional.empty());

        // Act
        VerfuegbarkeitService.VerfuegbarkeitErgebnis ergebnis =
            verfuegbarkeitService.pruefeVerfuegbarkeit(gegenstand, langerZeitraum);

        // Assert
        assertFalse(ergebnis.istVerfuegbar());
        assertTrue(ergebnis.getGrund().contains("Maximum"));
    }

    @Test
    @DisplayName("Sollte bereits ausgeliehenen Gegenstand erkennen")
    void sollteBereitsAusgeliehenErkennen() {
        // Arrange
        Ausleihe bestehendeAusleihe = new Ausleihe.Builder()
            .gegenstand(gegenstand)
            .ausleiher(ausleiher)
            .zeitraum(new Zeitraum(
                LocalDate.now().minusDays(2),
                LocalDate.now().plusDays(5)
            ))
            .build();

        when(ausleiheRepository.findeAktiveAusleiheVonGegenstand(gegenstand.getId()))
            .thenReturn(Optional.of(bestehendeAusleihe));

        // Act
        VerfuegbarkeitService.VerfuegbarkeitErgebnis ergebnis =
            verfuegbarkeitService.pruefeVerfuegbarkeit(gegenstand, zeitraum);

        // Assert
        assertFalse(ergebnis.istVerfuegbar());
        assertTrue(ergebnis.getGrund().contains("bereits ausgeliehen"));
    }

    @Test
    @DisplayName("Sollte Repository-Methode korrekt aufrufen")
    void sollteRepositoryKorrektAufrufen() {
        // Arrange
        when(ausleiheRepository.findeAktiveAusleiheVonGegenstand(gegenstand.getId()))
            .thenReturn(Optional.empty());

        // Act
        verfuegbarkeitService.pruefeVerfuegbarkeit(gegenstand, zeitraum);

        // Assert
        verify(ausleiheRepository, times(1)).findeAktiveAusleiheVonGegenstand(gegenstand.getId());
        verifyNoMoreInteractions(ausleiheRepository);
    }
}
