package de.dhbw.leihbar.application.services;

import de.dhbw.leihbar.domain.aggregates.Gegenstand;
import de.dhbw.leihbar.domain.repositories.GegenstandRepository;
import de.dhbw.leihbar.domain.valueobjects.InventarNummer;
import de.dhbw.leihbar.domain.valueobjects.Kategorie;
import de.dhbw.leihbar.domain.valueobjects.VerfuegbarkeitsStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit Tests für den Application Service GegenstandService.
 * Verwendet Mockito für das GegenstandRepository.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("GegenstandService Application Service Tests")
class GegenstandServiceTest {

    @Mock
    private GegenstandRepository gegenstandRepository;

    private GegenstandService gegenstandService;
    private Kategorie werkzeug;
    private Kategorie elektronik;
    private Kategorie sonstiges;

    @BeforeEach
    void setUp() {
        gegenstandService = new GegenstandService(gegenstandRepository);
        werkzeug = Kategorie.of("Werkzeug", 30);
        elektronik = Kategorie.of("Elektronik", 14);
        sonstiges = Kategorie.of("Sonstiges", 21);
    }

    @Test
    @DisplayName("Sollte neuen Gegenstand anlegen")
    void sollteNeuenGegenstandAnlegen() {
        // Arrange
        InventarNummer inventarNummer = InventarNummer.of(1);
        when(gegenstandRepository.naechsteFreieInventarNummer())
            .thenReturn(inventarNummer);
        when(gegenstandRepository.speichern(any(Gegenstand.class)))
            .thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        Gegenstand gegenstand = gegenstandService.gegenstandAnlegen(
            "Bohrmaschine",
            "Bosch Professional",
            werkzeug
        );

        // Assert
        assertNotNull(gegenstand);
        assertEquals("Bohrmaschine", gegenstand.getName());
        assertEquals("Bosch Professional", gegenstand.getBeschreibung());
        assertEquals(werkzeug, gegenstand.getKategorie());
        assertEquals(VerfuegbarkeitsStatus.VERFUEGBAR, gegenstand.getStatus());

        verify(gegenstandRepository).speichern(any(Gegenstand.class));
    }

    @Test
    @DisplayName("Sollte Gegenstand aktualisieren")
    void sollteGegenstandAktualisieren() {
        // Arrange
        Gegenstand existierend = Gegenstand.neu(
            InventarNummer.of(2),
            "Alt",
            "Alte Beschreibung",
            werkzeug
        );
        UUID id = existierend.getId();

        when(gegenstandRepository.findeNachId(id))
            .thenReturn(Optional.of(existierend));
        when(gegenstandRepository.speichern(any(Gegenstand.class)))
            .thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        Gegenstand aktualisiert = gegenstandService.gegenstandAktualisieren(
            id,
            "Neu",
            "Neue Beschreibung",
            elektronik
        );

        // Assert
        assertEquals("Neu", aktualisiert.getName());
        assertEquals("Neue Beschreibung", aktualisiert.getBeschreibung());
        assertEquals(elektronik, aktualisiert.getKategorie());

        verify(gegenstandRepository).speichern(existierend);
    }

    @Test
    @DisplayName("Sollte Fehler bei nicht existierendem Gegenstand werfen")
    void sollteFehlerBeiNichtExistierendemGegenstandWerfen() {
        // Arrange
        UUID id = UUID.randomUUID();
        when(gegenstandRepository.findeNachId(id))
            .thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () ->
            gegenstandService.gegenstandAktualisieren(id, "Test", "Test", sonstiges)
        );
    }

    @Test
    @DisplayName("Sollte verfügbare Gegenstände filtern")
    void sollteVerfuegbareGegenstaendeFiltern() {
        // Arrange
        Gegenstand g1 = Gegenstand.neu(InventarNummer.of(3), "G1", "B1", werkzeug);
        Gegenstand g2 = Gegenstand.neu(InventarNummer.of(4), "G2", "B2", elektronik);
        g2.ausleihen();

        when(gegenstandRepository.findeVerfuegbare())
            .thenReturn(List.of(g1));

        // Act
        List<Gegenstand> verfuegbare = gegenstandService.verfuegbareGegenstaende();

        // Assert
        assertEquals(1, verfuegbare.size());
        assertEquals("G1", verfuegbare.get(0).getName());

        verify(gegenstandRepository).findeVerfuegbare();
    }

    @Test
    @DisplayName("Sollte Gegenstand in Wartung setzen")
    void sollteGegenstandInWartungSetzen() {
        // Arrange
        Gegenstand gegenstand = Gegenstand.neu(InventarNummer.of(5), "Test", "Test", werkzeug);
        UUID id = gegenstand.getId();

        when(gegenstandRepository.findeNachId(id))
            .thenReturn(Optional.of(gegenstand));
        when(gegenstandRepository.speichern(any(Gegenstand.class)))
            .thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        Gegenstand result = gegenstandService.inWartungSetzen(id);

        // Assert
        assertEquals(VerfuegbarkeitsStatus.IN_WARTUNG, result.getStatus());
        verify(gegenstandRepository).speichern(gegenstand);
    }

    @Test
    @DisplayName("Sollte Gegenstand löschen")
    void sollteGegenstandLoeschen() {
        // Arrange
        Gegenstand gegenstand = Gegenstand.neu(InventarNummer.of(6), "Test", "Test", werkzeug);
        UUID id = gegenstand.getId();

        when(gegenstandRepository.findeNachId(id))
            .thenReturn(Optional.of(gegenstand));
        doNothing().when(gegenstandRepository).loeschen(id);

        // Act
        gegenstandService.gegenstandLoeschen(id);

        // Assert
        verify(gegenstandRepository).loeschen(id);
    }

    @Test
    @DisplayName("Sollte korrekten Suchbegriff an Repository übergeben")
    void sollteKorrektenSuchbegriffAnRepositoryUebergeben() {
        // Arrange
        String suchbegriff = "Bohr";
        when(gegenstandRepository.suche(suchbegriff))
            .thenReturn(List.of());

        // Act
        gegenstandService.sucheGegenstaende(suchbegriff);

        // Assert
        ArgumentCaptor<String> captor = ArgumentCaptor.forClass(String.class);
        verify(gegenstandRepository).suche(captor.capture());
        assertEquals("Bohr", captor.getValue());
    }

    @Test
    @DisplayName("Sollte alle Kategorienamen zurückgeben")
    void sollteAlleKategorienamenZurueckgeben() {
        // Arrange
        when(gegenstandRepository.findeAlleKategorienamen())
            .thenReturn(List.of("Werkzeug", "Elektronik"));

        // Act
        List<String> namen = gegenstandService.alleKategorienamen();

        // Assert
        assertEquals(2, namen.size());
        verify(gegenstandRepository).findeAlleKategorienamen();
    }
}
