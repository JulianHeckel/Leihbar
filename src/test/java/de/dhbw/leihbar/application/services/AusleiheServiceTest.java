package de.dhbw.leihbar.application.services;

import de.dhbw.leihbar.domain.aggregates.Ausleihe;
import de.dhbw.leihbar.domain.aggregates.Gegenstand;
import de.dhbw.leihbar.domain.entities.Ausleiher;
import de.dhbw.leihbar.domain.repositories.AusleiheRepository;
import de.dhbw.leihbar.domain.repositories.AusleiherRepository;
import de.dhbw.leihbar.domain.repositories.GegenstandRepository;
import de.dhbw.leihbar.domain.services.VerfuegbarkeitService;
import de.dhbw.leihbar.domain.services.VerfuegbarkeitService.VerfuegbarkeitErgebnis;
import de.dhbw.leihbar.domain.valueobjects.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Supplier;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit Tests für den Application Service AusleiheService.
 * Verwendet Mockito für alle Dependencies (Repositories und Domain Services).
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("AusleiheService Application Service Tests")
class AusleiheServiceTest {

    @Mock
    private AusleiheRepository ausleiheRepository;

    @Mock
    private GegenstandRepository gegenstandRepository;

    @Mock
    private AusleiherRepository ausleiherRepository;

    @Mock
    private VerfuegbarkeitService verfuegbarkeitService;

    @Mock
    private TransactionRunner transactionRunner;

    private AusleiheService ausleiheService;
    private Gegenstand gegenstand;
    private Ausleiher ausleiher;
    private UUID gegenstandId;
    private UUID ausleiherId;
    private Kategorie werkzeug;

    @BeforeEach
    void setUp() {
        ausleiheService = new AusleiheService(
            ausleiheRepository,
            gegenstandRepository,
            ausleiherRepository,
            verfuegbarkeitService,
            transactionRunner
        );

        // Der TransactionRunner führt im Test die übergebene Arbeit direkt aus.
        // lenient(), weil die Fehler-Pfade (Gegenstand/Ausleiher fehlt, nicht
        // verfügbar) die Transaktionsklammer gar nicht erreichen.
        lenient().when(transactionRunner.execute(any(Supplier.class)))
            .thenAnswer(invocation -> ((Supplier<?>) invocation.getArgument(0)).get());

        werkzeug = Kategorie.of("Werkzeug", 30);
        gegenstand = Gegenstand.neu(
            InventarNummer.of(1),
            "Bohrmaschine",
            "Bosch Professional",
            werkzeug
        );
        gegenstandId = gegenstand.getId();

        ausleiher = Ausleiher.neu(
            "Max",
            "Mustermann",
            Kontaktdaten.nurEmail("max@example.com")
        );
        ausleiherId = ausleiher.getId();
    }

    @Test
    @DisplayName("Sollte Ausleihe erfolgreich erstellen")
    void sollteAusleiheErfolgreichErstellen() {
        // Arrange
        LocalDate rueckgabedatum = LocalDate.now().plusDays(7);

        when(gegenstandRepository.findeNachId(gegenstandId))
            .thenReturn(Optional.of(gegenstand));
        when(ausleiherRepository.findeNachId(ausleiherId))
            .thenReturn(Optional.of(ausleiher));
        when(verfuegbarkeitService.pruefeVerfuegbarkeit(any(), any()))
            .thenReturn(VerfuegbarkeitErgebnis.verfuegbar());
        when(ausleiheRepository.speichern(any(Ausleihe.class)))
            .thenAnswer(invocation -> invocation.getArgument(0));
        when(gegenstandRepository.speichern(any(Gegenstand.class)))
            .thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        Ausleihe ausleihe = ausleiheService.ausleihen(gegenstandId, ausleiherId, rueckgabedatum);

        // Assert
        assertNotNull(ausleihe);
        assertEquals(gegenstandId, ausleihe.getGegenstandId());
        assertEquals(ausleiherId, ausleihe.getAusleiherId());
        assertEquals(AusleiheStatus.AKTIV, ausleihe.getStatus());

        verify(verfuegbarkeitService).pruefeVerfuegbarkeit(any(), any());
        verify(ausleiheRepository).speichern(any(Ausleihe.class));
        verify(gegenstandRepository).speichern(gegenstand);
    }

    @Test
    @DisplayName("Sollte Fehler werfen wenn Gegenstand nicht gefunden")
    void sollteFehlerWerfenWennGegenstandNichtGefunden() {
        // Arrange
        when(gegenstandRepository.findeNachId(gegenstandId))
            .thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () ->
            ausleiheService.ausleihen(gegenstandId, ausleiherId, LocalDate.now().plusDays(7))
        );

        verify(gegenstandRepository).findeNachId(gegenstandId);
        verifyNoInteractions(ausleiheRepository);
    }

    @Test
    @DisplayName("Sollte Fehler werfen wenn Ausleiher nicht gefunden")
    void sollteFehlerWerfenWennAusleiherNichtGefunden() {
        // Arrange
        when(gegenstandRepository.findeNachId(gegenstandId))
            .thenReturn(Optional.of(gegenstand));
        when(ausleiherRepository.findeNachId(ausleiherId))
            .thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () ->
            ausleiheService.ausleihen(gegenstandId, ausleiherId, LocalDate.now().plusDays(7))
        );

        verifyNoInteractions(ausleiheRepository);
    }

    @Test
    @DisplayName("Sollte Fehler werfen wenn Gegenstand nicht verfügbar")
    void sollteFehlerWerfenWennGegenstandNichtVerfuegbar() {
        // Arrange
        when(gegenstandRepository.findeNachId(gegenstandId))
            .thenReturn(Optional.of(gegenstand));
        when(ausleiherRepository.findeNachId(ausleiherId))
            .thenReturn(Optional.of(ausleiher));
        when(verfuegbarkeitService.pruefeVerfuegbarkeit(any(), any()))
            .thenReturn(VerfuegbarkeitErgebnis.nichtVerfuegbar("Bereits ausgeliehen"));

        // Act & Assert
        IllegalStateException exception = assertThrows(IllegalStateException.class, () ->
            ausleiheService.ausleihen(gegenstandId, ausleiherId, LocalDate.now().plusDays(7))
        );

        assertTrue(exception.getMessage().contains("Bereits ausgeliehen"));
        verifyNoInteractions(ausleiheRepository);
    }

    @Test
    @DisplayName("Sollte Rückgabe erfolgreich verarbeiten")
    void sollteRueckgabeErfolgreichVerarbeiten() {
        // Arrange
        Ausleihe ausleihe = new Ausleihe.Builder()
            .gegenstand(gegenstand)
            .ausleiher(ausleiher)
            .zeitraum(new Zeitraum(LocalDate.now(), LocalDate.now().plusDays(7)))
            .build();
        UUID ausleiheId = ausleihe.getId();

        // Gegenstand auf ausgeliehen setzen
        gegenstand.ausleihen();

        when(ausleiheRepository.findeNachId(ausleiheId))
            .thenReturn(Optional.of(ausleihe));
        when(gegenstandRepository.findeNachId(ausleihe.getGegenstandId()))
            .thenReturn(Optional.of(gegenstand));
        when(ausleiheRepository.speichern(any(Ausleihe.class)))
            .thenAnswer(invocation -> invocation.getArgument(0));
        when(gegenstandRepository.speichern(any(Gegenstand.class)))
            .thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        Ausleihe zurueckgegeben = ausleiheService.zurueckgeben(ausleiheId, "Gut erhalten");

        // Assert
        assertEquals(AusleiheStatus.ZURUECKGEGEBEN, zurueckgegeben.getStatus());
        assertNotNull(zurueckgegeben.getZustandsbericht());
        assertEquals("Gut erhalten", zurueckgegeben.getZustandsbericht());

        verify(gegenstandRepository).speichern(gegenstand);
        verify(ausleiheRepository).speichern(ausleihe);
    }

    @Test
    @DisplayName("Sollte Stornierung erfolgreich verarbeiten")
    void sollteStornierungErfolgreichVerarbeiten() {
        // Arrange
        Ausleihe ausleihe = new Ausleihe.Builder()
            .gegenstand(gegenstand)
            .ausleiher(ausleiher)
            .zeitraum(new Zeitraum(LocalDate.now(), LocalDate.now().plusDays(7)))
            .build();
        UUID ausleiheId = ausleihe.getId();

        gegenstand.ausleihen();

        when(ausleiheRepository.findeNachId(ausleiheId))
            .thenReturn(Optional.of(ausleihe));
        when(gegenstandRepository.findeNachId(ausleihe.getGegenstandId()))
            .thenReturn(Optional.of(gegenstand));
        when(ausleiheRepository.speichern(any(Ausleihe.class)))
            .thenAnswer(invocation -> invocation.getArgument(0));
        when(gegenstandRepository.speichern(any(Gegenstand.class)))
            .thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        Ausleihe storniert = ausleiheService.stornieren(ausleiheId);

        // Assert
        assertEquals(AusleiheStatus.STORNIERT, storniert.getStatus());
        verify(gegenstandRepository).speichern(gegenstand);
    }

    @Test
    @DisplayName("Sollte Zeitraum korrekt an Repository übergeben")
    void sollteZeitraumKorrektAnRepositoryUebergeben() {
        // Arrange
        LocalDate rueckgabedatum = LocalDate.now().plusDays(14);

        when(gegenstandRepository.findeNachId(gegenstandId))
            .thenReturn(Optional.of(gegenstand));
        when(ausleiherRepository.findeNachId(ausleiherId))
            .thenReturn(Optional.of(ausleiher));
        when(verfuegbarkeitService.pruefeVerfuegbarkeit(any(), any()))
            .thenReturn(VerfuegbarkeitErgebnis.verfuegbar());
        when(ausleiheRepository.speichern(any(Ausleihe.class)))
            .thenAnswer(invocation -> invocation.getArgument(0));
        when(gegenstandRepository.speichern(any(Gegenstand.class)))
            .thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        ausleiheService.ausleihen(gegenstandId, ausleiherId, rueckgabedatum);

        // Assert
        ArgumentCaptor<Ausleihe> ausleiheCaptor = ArgumentCaptor.forClass(Ausleihe.class);
        verify(ausleiheRepository).speichern(ausleiheCaptor.capture());

        Ausleihe gespeicherteAusleihe = ausleiheCaptor.getValue();
        assertEquals(LocalDate.now(), gespeicherteAusleihe.getGeplanterZeitraum().getVon());
        assertEquals(rueckgabedatum, gespeicherteAusleihe.getGeplanterZeitraum().getBis());
    }
}
