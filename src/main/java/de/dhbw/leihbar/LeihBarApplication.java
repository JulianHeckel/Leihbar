package de.dhbw.leihbar;

import de.dhbw.leihbar.application.services.AusleiheService;
import de.dhbw.leihbar.application.services.AusleiherService;
import de.dhbw.leihbar.application.services.GegenstandService;
import de.dhbw.leihbar.application.services.TransactionRunner;
import de.dhbw.leihbar.domain.repositories.AusleiheRepository;
import de.dhbw.leihbar.domain.repositories.AusleiherRepository;
import de.dhbw.leihbar.domain.repositories.GegenstandRepository;
import de.dhbw.leihbar.domain.services.VerfuegbarkeitService;
import de.dhbw.leihbar.infrastructure.persistence.JpaAusleiheRepository;
import de.dhbw.leihbar.infrastructure.persistence.JpaAusleiherRepository;
import de.dhbw.leihbar.infrastructure.persistence.JpaGegenstandRepository;
import de.dhbw.leihbar.infrastructure.persistence.JpaTransactionRunner;
import de.dhbw.leihbar.ui.views.MainView;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Hauptklasse der LeihBar-Anwendung.
 * Initialisiert die Infrastruktur und startet die JavaFX-Oberfläche.
 */
public class LeihBarApplication extends Application {

    private static final Logger logger = LoggerFactory.getLogger(LeihBarApplication.class);
    private static final String PERSISTENCE_UNIT = "leihbar-pu";

    private EntityManagerFactory entityManagerFactory;
    private EntityManager entityManager;

    // Services (werden bei Start initialisiert)
    private static GegenstandService gegenstandService;
    private static AusleiherService ausleiherService;
    private static AusleiheService ausleiheService;

    public static void main(String[] args) {
        logger.info("Starte LeihBar-Anwendung...");
        launch(args);
    }

    @Override
    public void init() {
        logger.info("Initialisiere Datenbank und Services...");

        // EntityManager erstellen
        entityManagerFactory = Persistence.createEntityManagerFactory(PERSISTENCE_UNIT);
        entityManager = entityManagerFactory.createEntityManager();

        // Repositories erstellen
        GegenstandRepository gegenstandRepository = new JpaGegenstandRepository(entityManager);
        AusleiherRepository ausleiherRepository = new JpaAusleiherRepository(entityManager);
        AusleiheRepository ausleiheRepository = new JpaAusleiheRepository(entityManager);

        // Domain Services erstellen
        VerfuegbarkeitService verfuegbarkeitService = new VerfuegbarkeitService(ausleiheRepository);

        // Transaktionssteuerung (teilt sich den EntityManager mit den Repositories)
        TransactionRunner transactionRunner = new JpaTransactionRunner(entityManager);

        // Application Services erstellen
        gegenstandService = new GegenstandService(gegenstandRepository);
        ausleiherService = new AusleiherService(ausleiherRepository);
        ausleiheService = new AusleiheService(
            ausleiheRepository,
            gegenstandRepository,
            ausleiherRepository,
            verfuegbarkeitService,
            transactionRunner
        );

        logger.info("Services erfolgreich initialisiert");
    }

    @Override
    public void start(Stage primaryStage) {
        logger.info("Starte Benutzeroberfläche...");

        MainView mainView = new MainView(gegenstandService, ausleiherService, ausleiheService);
        Scene scene = new Scene(mainView, 1200, 800);

        primaryStage.setTitle("LeihBar - Ausleihe-Verwaltungssystem");
        primaryStage.setScene(scene);
        primaryStage.setMinWidth(800);
        primaryStage.setMinHeight(600);
        primaryStage.show();

        logger.info("Anwendung gestartet");
    }

    @Override
    public void stop() {
        logger.info("Beende Anwendung...");

        if (entityManager != null && entityManager.isOpen()) {
            entityManager.close();
        }
        if (entityManagerFactory != null && entityManagerFactory.isOpen()) {
            entityManagerFactory.close();
        }

        logger.info("Anwendung beendet");
    }

    // Getter für Services (für Tests und UI)
    public static GegenstandService getGegenstandService() {
        return gegenstandService;
    }

    public static AusleiherService getAusleiherService() {
        return ausleiherService;
    }

    public static AusleiheService getAusleiheService() {
        return ausleiheService;
    }
}
