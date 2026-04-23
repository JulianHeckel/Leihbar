package de.dhbw.leihbar.ui.views;

import de.dhbw.leihbar.application.services.AusleiheService;
import de.dhbw.leihbar.application.services.AusleiherService;
import de.dhbw.leihbar.application.services.GegenstandService;
import de.dhbw.leihbar.domain.aggregates.Ausleihe;
import de.dhbw.leihbar.domain.aggregates.Gegenstand;
import de.dhbw.leihbar.domain.entities.Ausleiher;
import de.dhbw.leihbar.domain.valueobjects.Kategorie;
import de.dhbw.leihbar.domain.valueobjects.VerfuegbarkeitsStatus;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.Cursor;
import javafx.scene.layout.*;
import javafx.beans.property.SimpleStringProperty;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Hauptansicht der LeihBar-Anwendung.
 * Implementiert eine Tab-basierte Oberfläche für alle Funktionen.
 */
public class MainView extends BorderPane {

    private final GegenstandService gegenstandService;
    private final AusleiherService ausleiherService;
    private final AusleiheService ausleiheService;

    private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy");

    // UI Komponenten
    private TabPane tabPane;
    private TableView<Gegenstand> gegenstaendeTabelle;
    private TableView<Ausleiher> ausleiherTabelle;
    private TableView<Ausleihe> ausleihenTabelle;
    private TableView<Ausleihe> ueberfaelligTabelle;
    private HBox dashboardStatsBox;
    private Label statusLabel;

    public MainView(GegenstandService gegenstandService,
                   AusleiherService ausleiherService,
                   AusleiheService ausleiheService) {
        this.gegenstandService = gegenstandService;
        this.ausleiherService = ausleiherService;
        this.ausleiheService = ausleiheService;

        initializeUI();
        refreshAllData();
    }

    private void initializeUI() {
        // Header
        Label headerLabel = new Label("LeihBar - Ausleihe-Verwaltungssystem");
        headerLabel.setStyle("-fx-font-size: 24px; -fx-font-weight: bold;");
        HBox header = new HBox(headerLabel);
        header.setPadding(new Insets(15));
        header.setStyle("-fx-background-color: #2196F3;");
        headerLabel.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: white;");
        setTop(header);

        // Tab-Pane
        this.tabPane = new TabPane();
        tabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);

        Tab dashboardTab = new Tab("Dashboard", createDashboardPane());
        Tab gegenstaendeTab = new Tab("Gegenstände", createGegenstaendePane());
        Tab ausleiherTab = new Tab("Ausleiher", createAusleiherPane());
        Tab ausleihenTab = new Tab("Ausleihen", createAusleihenPane());
        Tab ueberfaelligTab = new Tab("Überfällig", createUeberfaelligPane());

        tabPane.getTabs().addAll(dashboardTab, gegenstaendeTab, ausleiherTab, ausleihenTab, ueberfaelligTab);

        // Daten beim Tab-Wechsel automatisch aktualisieren
        tabPane.getSelectionModel().selectedItemProperty().addListener(
            (obs, oldTab, newTab) -> refreshAllData()
        );

        setCenter(tabPane);

        // Status Bar
        statusLabel = new Label("Bereit");
        HBox statusBar = new HBox(statusLabel);
        statusBar.setPadding(new Insets(5, 10, 5, 10));
        statusBar.setStyle("-fx-background-color: #e0e0e0;");
        setBottom(statusBar);
    }

    private Pane createDashboardPane() {
        VBox pane = new VBox(20);
        pane.setPadding(new Insets(20));

        Label titleLabel = new Label("Übersicht");
        titleLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");

        // Statistik-Karten
        dashboardStatsBox = new HBox(20);
        refreshDashboard();

        Button refreshButton = new Button("Aktualisieren");
        refreshButton.setOnAction(e -> refreshAllData());

        pane.getChildren().addAll(titleLabel, dashboardStatsBox, refreshButton);
        return pane;
    }

    private VBox createStatCard(String title, String value, String color, int targetTabIndex) {
        VBox card = new VBox(5);
        card.setPadding(new Insets(15));
        card.setStyle("-fx-background-color: " + color + "; -fx-background-radius: 5;");
        card.setPrefWidth(150);
        card.setCursor(Cursor.HAND);

        Label titleLbl = new Label(title);
        titleLbl.setStyle("-fx-text-fill: white; -fx-font-size: 14px;");

        Label valueLbl = new Label(value);
        valueLbl.setStyle("-fx-text-fill: white; -fx-font-size: 28px; -fx-font-weight: bold;");

        card.getChildren().addAll(titleLbl, valueLbl);

        // Klick navigiert zum entsprechenden Tab
        if (targetTabIndex >= 0) {
            card.setOnMouseClicked(e -> tabPane.getSelectionModel().select(targetTabIndex));
        }

        return card;
    }

    private Pane createGegenstaendePane() {
        VBox pane = new VBox(10);
        pane.setPadding(new Insets(10));

        // Toolbar
        HBox toolbar = new HBox(10);
        Button addButton = new Button("Neuer Gegenstand");
        addButton.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white;");
        addButton.setOnAction(e -> showGegenstandDialog(null));

        TextField searchField = new TextField();
        searchField.setPromptText("Suchen (Name, Kategorie, INV-Nr.)...");
        searchField.setPrefWidth(250);

        ComboBox<String> statusFilter = new ComboBox<>();
        statusFilter.getItems().add("Alle Status");
        for (VerfuegbarkeitsStatus status : VerfuegbarkeitsStatus.values()) {
            statusFilter.getItems().add(status.getBezeichnung());
        }
        statusFilter.setValue("Alle Status");

        // Such- und Filterfunktion
        Runnable filterAction = () -> {
            String suchbegriff = searchField.getText();
            String selectedStatus = statusFilter.getValue();

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

        searchField.setOnAction(e -> filterAction.run());
        statusFilter.setOnAction(e -> filterAction.run());

        Button resetButton = new Button("Zurücksetzen");
        resetButton.setOnAction(e -> {
            searchField.clear();
            statusFilter.setValue("Alle Status");
            gegenstaendeTabelle.setItems(FXCollections.observableArrayList(gegenstandService.alleGegenstaende()));
        });

        toolbar.getChildren().addAll(addButton, searchField, statusFilter, resetButton);

        // Tabelle
        gegenstaendeTabelle = new TableView<>();
        gegenstaendeTabelle.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        TableColumn<Gegenstand, String> invNrCol = new TableColumn<>("Inventar-Nr.");
        invNrCol.setCellValueFactory(data ->
            new SimpleStringProperty(data.getValue().getInventarNummer().getValue()));

        TableColumn<Gegenstand, String> nameCol = new TableColumn<>("Name");
        nameCol.setCellValueFactory(data ->
            new SimpleStringProperty(data.getValue().getName()));

        TableColumn<Gegenstand, String> kategorieCol = new TableColumn<>("Kategorie");
        kategorieCol.setCellValueFactory(data ->
            new SimpleStringProperty(data.getValue().getKategorie().getName()));

        TableColumn<Gegenstand, String> statusCol = new TableColumn<>("Status");
        statusCol.setCellValueFactory(data ->
            new SimpleStringProperty(data.getValue().getStatus().getBezeichnung()));

        gegenstaendeTabelle.getColumns().addAll(invNrCol, nameCol, kategorieCol, statusCol);

        // Farbliche Hervorhebung nach Status
        gegenstaendeTabelle.setRowFactory(tv -> new TableRow<>() {
            @Override
            protected void updateItem(Gegenstand item, boolean empty) {
                super.updateItem(item, empty);
                if (item == null || empty) {
                    setStyle("");
                } else {
                    switch (item.getStatus()) {
                        case VERFUEGBAR -> setStyle("-fx-background-color: #E8F5E9;");
                        case AUSGELIEHEN -> setStyle("-fx-background-color: #FFF3E0;");
                        case IN_WARTUNG -> setStyle("-fx-background-color: #F3E5F5;");
                        case AUSGEMUSTERT -> setStyle("-fx-background-color: #EFEBE9;");
                    }
                }
            }
        });

        // Kontextmenü für Statusänderungen
        ContextMenu contextMenu = new ContextMenu();

        MenuItem inWartungItem = new MenuItem("In Wartung setzen");
        inWartungItem.setOnAction(e -> {
            Gegenstand selected = gegenstaendeTabelle.getSelectionModel().getSelectedItem();
            if (selected != null) {
                try {
                    gegenstandService.inWartungSetzen(selected.getId());
                    refreshAllData();
                    setStatus("Gegenstand in Wartung gesetzt: " + selected.getInventarNummer());
                } catch (IllegalStateException ex) {
                    showError("Aktion nicht möglich", ex.getMessage());
                }
            }
        });

        MenuItem wartungBeendenItem = new MenuItem("Wartung beenden");
        wartungBeendenItem.setOnAction(e -> {
            Gegenstand selected = gegenstaendeTabelle.getSelectionModel().getSelectedItem();
            if (selected != null) {
                try {
                    gegenstandService.wartungBeenden(selected.getId());
                    refreshAllData();
                    setStatus("Wartung beendet: " + selected.getInventarNummer());
                } catch (IllegalStateException ex) {
                    showError("Aktion nicht möglich", ex.getMessage());
                }
            }
        });

        MenuItem ausmusternItem = new MenuItem("Ausmustern");
        ausmusternItem.setOnAction(e -> {
            Gegenstand selected = gegenstaendeTabelle.getSelectionModel().getSelectedItem();
            if (selected != null) {
                Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
                confirm.setTitle("Ausmustern bestätigen");
                confirm.setHeaderText("Gegenstand ausmustern?");
                confirm.setContentText("Der Gegenstand '" + selected.getName() +
                    "' wird endgültig ausgemustert und kann nicht mehr ausgeliehen werden.");
                confirm.showAndWait().ifPresent(response -> {
                    if (response == ButtonType.OK) {
                        try {
                            gegenstandService.ausmustern(selected.getId());
                            refreshAllData();
                            setStatus("Gegenstand ausgemustert: " + selected.getInventarNummer());
                        } catch (IllegalStateException ex) {
                            showError("Aktion nicht möglich", ex.getMessage());
                        }
                    }
                });
            }
        });

        MenuItem loeschenItem = new MenuItem("Löschen");
        loeschenItem.setOnAction(e -> {
            Gegenstand selected = gegenstaendeTabelle.getSelectionModel().getSelectedItem();
            if (selected != null) {
                Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
                confirm.setTitle("Löschen bestätigen");
                confirm.setHeaderText("Gegenstand löschen?");
                confirm.setContentText("Der Gegenstand '" + selected.getName() + "' wird unwiderruflich gelöscht.");
                confirm.showAndWait().ifPresent(response -> {
                    if (response == ButtonType.OK) {
                        try {
                            gegenstandService.gegenstandLoeschen(selected.getId());
                            refreshAllData();
                            setStatus("Gegenstand gelöscht: " + selected.getInventarNummer());
                        } catch (IllegalStateException ex) {
                            showError("Aktion nicht möglich", ex.getMessage());
                        }
                    }
                });
            }
        });

        MenuItem bearbeitenItem = new MenuItem("Bearbeiten");
        bearbeitenItem.setOnAction(e -> {
            Gegenstand selected = gegenstaendeTabelle.getSelectionModel().getSelectedItem();
            if (selected != null) {
                showGegenstandDialog(selected);
            }
        });

        contextMenu.getItems().addAll(bearbeitenItem, new SeparatorMenuItem(),
            inWartungItem, wartungBeendenItem,
            new SeparatorMenuItem(), ausmusternItem, loeschenItem);

        // Kontextmenü dynamisch anpassen basierend auf Status
        gegenstaendeTabelle.setOnContextMenuRequested(event -> {
            Gegenstand selected = gegenstaendeTabelle.getSelectionModel().getSelectedItem();
            if (selected != null) {
                VerfuegbarkeitsStatus status = selected.getStatus();
                bearbeitenItem.setDisable(status == VerfuegbarkeitsStatus.AUSGEMUSTERT);
                inWartungItem.setDisable(status != VerfuegbarkeitsStatus.VERFUEGBAR);
                wartungBeendenItem.setDisable(status != VerfuegbarkeitsStatus.IN_WARTUNG);
                ausmusternItem.setDisable(status == VerfuegbarkeitsStatus.AUSGELIEHEN ||
                                          status == VerfuegbarkeitsStatus.AUSGEMUSTERT);
                loeschenItem.setDisable(status == VerfuegbarkeitsStatus.AUSGELIEHEN);
            }
        });

        gegenstaendeTabelle.setContextMenu(contextMenu);

        // Doppelklick zum Bearbeiten
        gegenstaendeTabelle.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2) {
                Gegenstand selected = gegenstaendeTabelle.getSelectionModel().getSelectedItem();
                if (selected != null) {
                    showGegenstandDialog(selected);
                }
            }
        });

        pane.getChildren().addAll(toolbar, gegenstaendeTabelle);
        VBox.setVgrow(gegenstaendeTabelle, Priority.ALWAYS);
        return pane;
    }

    private Pane createAusleiherPane() {
        VBox pane = new VBox(10);
        pane.setPadding(new Insets(10));

        // Toolbar
        HBox toolbar = new HBox(10);
        Button addButton = new Button("Neuer Ausleiher");
        addButton.setOnAction(e -> showAusleiherDialog(null));
        toolbar.getChildren().add(addButton);

        // Tabelle
        ausleiherTabelle = new TableView<>();
        ausleiherTabelle.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        TableColumn<Ausleiher, String> nameCol = new TableColumn<>("Name");
        nameCol.setCellValueFactory(data ->
            new SimpleStringProperty(data.getValue().getVollerName()));

        TableColumn<Ausleiher, String> emailCol = new TableColumn<>("E-Mail");
        emailCol.setCellValueFactory(data ->
            new SimpleStringProperty(data.getValue().getKontaktdaten().getEmail()));

        TableColumn<Ausleiher, String> telefonCol = new TableColumn<>("Telefon");
        telefonCol.setCellValueFactory(data ->
            new SimpleStringProperty(
                data.getValue().getKontaktdaten().getTelefon() != null
                    ? data.getValue().getKontaktdaten().getTelefon()
                    : "-"
            ));

        ausleiherTabelle.getColumns().addAll(nameCol, emailCol, telefonCol);

        pane.getChildren().addAll(toolbar, ausleiherTabelle);
        VBox.setVgrow(ausleiherTabelle, Priority.ALWAYS);
        return pane;
    }

    private Pane createAusleihenPane() {
        VBox pane = new VBox(10);
        pane.setPadding(new Insets(10));

        // Toolbar
        HBox toolbar = new HBox(10);
        Button addButton = new Button("Neue Ausleihe");
        addButton.setStyle("-fx-background-color: #2196F3; -fx-text-fill: white;");
        addButton.setOnAction(e -> showAusleiheDialog());
        Button returnButton = new Button("Rückgabe");
        returnButton.setStyle("-fx-background-color: #FF9800; -fx-text-fill: white;");
        returnButton.setOnAction(e -> {
            Ausleihe selected = ausleihenTabelle.getSelectionModel().getSelectedItem();
            if (selected != null && selected.istAktiv()) {
                showRueckgabeDialog(selected);
            }
        });
        toolbar.getChildren().addAll(addButton, returnButton);

        // Tabelle
        ausleihenTabelle = new TableView<>();
        ausleihenTabelle.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        TableColumn<Ausleihe, String> gegenstandCol = new TableColumn<>("Gegenstand");
        gegenstandCol.setCellValueFactory(data -> {
            var gegenstand = gegenstandService.findeGegenstand(data.getValue().getGegenstandId());
            return new SimpleStringProperty(gegenstand.map(Gegenstand::getName).orElse("Unbekannt"));
        });

        TableColumn<Ausleihe, String> ausleiherCol = new TableColumn<>("Ausleiher");
        ausleiherCol.setCellValueFactory(data -> {
            var ausleiher = ausleiherService.findeAusleiher(data.getValue().getAusleiherId());
            return new SimpleStringProperty(ausleiher.map(Ausleiher::getVollerName).orElse("Unbekannt"));
        });

        TableColumn<Ausleihe, String> vonCol = new TableColumn<>("Von");
        vonCol.setCellValueFactory(data ->
            new SimpleStringProperty(data.getValue().getAusleihdatum().format(dateFormatter)));

        TableColumn<Ausleihe, String> bisCol = new TableColumn<>("Bis");
        bisCol.setCellValueFactory(data ->
            new SimpleStringProperty(data.getValue().getGeplantesRueckgabedatum().format(dateFormatter)));

        TableColumn<Ausleihe, String> statusCol = new TableColumn<>("Status");
        statusCol.setCellValueFactory(data ->
            new SimpleStringProperty(data.getValue().getStatus().getBezeichnung()));

        TableColumn<Ausleihe, String> zustandsberichtCol = new TableColumn<>("Zustandsbericht");
        zustandsberichtCol.setCellValueFactory(data ->
            new SimpleStringProperty(
                data.getValue().getZustandsbericht() != null
                    ? data.getValue().getZustandsbericht()
                    : "-"
            ));

        ausleihenTabelle.getColumns().addAll(gegenstandCol, ausleiherCol, vonCol, bisCol, statusCol, zustandsberichtCol);

        // Farbliche Hervorhebung nach Ausleihe-Status
        ausleihenTabelle.setRowFactory(tv -> new TableRow<>() {
            @Override
            protected void updateItem(Ausleihe item, boolean empty) {
                super.updateItem(item, empty);
                if (item == null || empty) {
                    setStyle("");
                } else {
                    switch (item.getStatus()) {
                        case AKTIV -> setStyle("-fx-background-color: #E8F5E9;");
                        case UEBERFAELLIG -> setStyle("-fx-background-color: #FFEBEE;");
                        case ZURUECKGEGEBEN -> setStyle("-fx-background-color: #F5F5F5;");
                        case STORNIERT -> setStyle("-fx-background-color: #ECEFF1;");
                    }
                }
            }
        });

        pane.getChildren().addAll(toolbar, ausleihenTabelle);
        VBox.setVgrow(ausleihenTabelle, Priority.ALWAYS);
        return pane;
    }

    private Pane createUeberfaelligPane() {
        VBox pane = new VBox(10);
        pane.setPadding(new Insets(10));

        Label titleLabel = new Label("Überfällige Ausleihen");
        titleLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #f44336;");

        this.ueberfaelligTabelle = new TableView<>();
        ueberfaelligTabelle.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        TableColumn<Ausleihe, String> gegenstandCol = new TableColumn<>("Gegenstand");
        gegenstandCol.setCellValueFactory(data -> {
            var gegenstand = gegenstandService.findeGegenstand(data.getValue().getGegenstandId());
            return new SimpleStringProperty(gegenstand.map(Gegenstand::getName).orElse("Unbekannt"));
        });

        TableColumn<Ausleihe, String> ausleiherCol = new TableColumn<>("Ausleiher");
        ausleiherCol.setCellValueFactory(data -> {
            var ausleiher = ausleiherService.findeAusleiher(data.getValue().getAusleiherId());
            return new SimpleStringProperty(ausleiher.map(Ausleiher::getVollerName).orElse("Unbekannt"));
        });

        TableColumn<Ausleihe, String> faelligCol = new TableColumn<>("Fällig seit");
        faelligCol.setCellValueFactory(data ->
            new SimpleStringProperty(data.getValue().getGeplantesRueckgabedatum().format(dateFormatter)));

        TableColumn<Ausleihe, String> tageCol = new TableColumn<>("Überfällig (Tage)");
        tageCol.setCellValueFactory(data ->
            new SimpleStringProperty(String.valueOf(data.getValue().getUeberfaelligeTage())));

        ueberfaelligTabelle.getColumns().addAll(gegenstandCol, ausleiherCol, faelligCol, tageCol);

        pane.getChildren().addAll(titleLabel, ueberfaelligTabelle);
        VBox.setVgrow(ueberfaelligTabelle, Priority.ALWAYS);
        return pane;
    }

    private void showGegenstandDialog(Gegenstand existing) {
        Dialog<Gegenstand> dialog = new Dialog<>();
        dialog.setTitle(existing == null ? "Neuer Gegenstand" : "Gegenstand bearbeiten");

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20));

        TextField nameField = new TextField();
        nameField.setPromptText("Name");
        TextArea beschreibungField = new TextArea();
        beschreibungField.setPromptText("Beschreibung");
        beschreibungField.setPrefRowCount(3);
        ComboBox<String> kategorieCombo = new ComboBox<>();
        kategorieCombo.setEditable(true);
        kategorieCombo.getItems().addAll(gegenstandService.alleKategorienamen());
        kategorieCombo.setPromptText("Kategorie eingeben oder waehlen");
        Spinner<Integer> maxTageSpinner = new Spinner<>(1, 365, 14);

        // Felder vorbelegen bei Bearbeitung
        if (existing != null) {
            nameField.setText(existing.getName());
            beschreibungField.setText(existing.getBeschreibung());
            kategorieCombo.setValue(existing.getKategorie().getName());
            maxTageSpinner.getValueFactory().setValue(existing.getKategorie().getMaxAusleihdauerTage());
        }

        grid.add(new Label("Name:"), 0, 0);
        grid.add(nameField, 1, 0);
        grid.add(new Label("Beschreibung:"), 0, 1);
        grid.add(beschreibungField, 1, 1);
        grid.add(new Label("Kategorie:"), 0, 2);
        grid.add(kategorieCombo, 1, 2);
        grid.add(new Label("Max. Ausleihtage:"), 0, 3);
        grid.add(maxTageSpinner, 1, 3);

        dialog.getDialogPane().setContent(grid);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        dialog.setResultConverter(button -> {
            if (button == ButtonType.OK) {
                try {
                    Kategorie kategorie = Kategorie.of(kategorieCombo.getEditor().getText(), maxTageSpinner.getValue());
                    if (existing != null) {
                        return gegenstandService.gegenstandAktualisieren(
                            existing.getId(),
                            nameField.getText(),
                            beschreibungField.getText(),
                            kategorie
                        );
                    } else {
                        return gegenstandService.gegenstandAnlegen(
                            nameField.getText(),
                            beschreibungField.getText(),
                            kategorie
                        );
                    }
                } catch (IllegalArgumentException ex) {
                    showError("Ungültige Eingabe", ex.getMessage());
                    return null;
                }
            }
            return null;
        });

        dialog.showAndWait().ifPresent(g -> {
            refreshAllData();
            setStatus(existing != null
                ? "Gegenstand aktualisiert: " + g.getInventarNummer()
                : "Gegenstand angelegt: " + g.getInventarNummer());
        });
    }

    private void showAusleiherDialog(Ausleiher existing) {
        Dialog<Ausleiher> dialog = new Dialog<>();
        dialog.setTitle("Neuer Ausleiher");

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20));

        TextField vornameField = new TextField();
        TextField nachnameField = new TextField();
        TextField emailField = new TextField();
        TextField telefonField = new TextField();

        grid.add(new Label("Vorname:"), 0, 0);
        grid.add(vornameField, 1, 0);
        grid.add(new Label("Nachname:"), 0, 1);
        grid.add(nachnameField, 1, 1);
        grid.add(new Label("E-Mail:"), 0, 2);
        grid.add(emailField, 1, 2);
        grid.add(new Label("Telefon:"), 0, 3);
        grid.add(telefonField, 1, 3);

        dialog.getDialogPane().setContent(grid);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        dialog.setResultConverter(button -> {
            if (button == ButtonType.OK) {
                return ausleiherService.ausleiherAnlegen(
                    vornameField.getText(),
                    nachnameField.getText(),
                    emailField.getText(),
                    telefonField.getText().isBlank() ? null : telefonField.getText()
                );
            }
            return null;
        });

        dialog.showAndWait().ifPresent(a -> {
            refreshAllData();
            setStatus("Ausleiher angelegt: " + a.getVollerName());
        });
    }

    private void showAusleiheDialog() {
        Dialog<Ausleihe> dialog = new Dialog<>();
        dialog.setTitle("Neue Ausleihe");

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20));

        ComboBox<Gegenstand> gegenstandCombo = new ComboBox<>();
        gegenstandCombo.setItems(FXCollections.observableArrayList(gegenstandService.verfuegbareGegenstaende()));

        ComboBox<Ausleiher> ausleiherCombo = new ComboBox<>();
        ausleiherCombo.setItems(FXCollections.observableArrayList(ausleiherService.alleAusleiher()));

        DatePicker rueckgabePicker = new DatePicker(LocalDate.now().plusDays(14));

        // Info-Label für max. Ausleihtage
        Label maxTageInfo = new Label("");
        maxTageInfo.setStyle("-fx-text-fill: #666;");
        gegenstandCombo.setOnAction(e -> {
            Gegenstand selected = gegenstandCombo.getValue();
            if (selected != null) {
                maxTageInfo.setText("Max. " + selected.getKategorie().getMaxAusleihdauerTage() +
                    " Tage für Kategorie '" + selected.getKategorie().getName() + "'");
            }
        });

        grid.add(new Label("Gegenstand:"), 0, 0);
        grid.add(gegenstandCombo, 1, 0);
        grid.add(new Label("Ausleiher:"), 0, 1);
        grid.add(ausleiherCombo, 1, 1);
        grid.add(new Label("Rückgabe bis:"), 0, 2);
        grid.add(rueckgabePicker, 1, 2);
        grid.add(maxTageInfo, 1, 3);

        dialog.getDialogPane().setContent(grid);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        dialog.setResultConverter(button -> {
            if (button == ButtonType.OK && gegenstandCombo.getValue() != null
                && ausleiherCombo.getValue() != null) {
                try {
                    return ausleiheService.ausleihen(
                        gegenstandCombo.getValue().getId(),
                        ausleiherCombo.getValue().getId(),
                        rueckgabePicker.getValue()
                    );
                } catch (IllegalStateException ex) {
                    showError("Ausleihe nicht möglich", ex.getMessage());
                    return null;
                } catch (IllegalArgumentException ex) {
                    showError("Ungültige Eingabe", ex.getMessage());
                    return null;
                }
            }
            return null;
        });

        dialog.showAndWait().ifPresent(a -> {
            refreshAllData();
            setStatus("Ausleihe erstellt");
        });
    }

    private void showError(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Fehler");
        alert.setHeaderText(title);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showRueckgabeDialog(Ausleihe ausleihe) {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Rückgabe");
        dialog.setHeaderText("Zustandsbericht (optional)");
        dialog.setContentText("Bemerkungen:");

        dialog.showAndWait().ifPresent(zustandsbericht -> {
            ausleiheService.zurueckgeben(ausleihe.getId(), zustandsbericht);
            refreshAllData();
            setStatus("Rückgabe erfasst");
        });
    }

    private void refreshAllData() {
        gegenstaendeTabelle.setItems(FXCollections.observableArrayList(gegenstandService.alleGegenstaende()));
        ausleiherTabelle.setItems(FXCollections.observableArrayList(ausleiherService.alleAusleiher()));
        ausleihenTabelle.setItems(FXCollections.observableArrayList(ausleiheService.aktiveAusleihen()));
        refreshDashboard();
        refreshUeberfaellig();
    }

    private void refreshDashboard() {
        if (dashboardStatsBox != null) {
            dashboardStatsBox.getChildren().setAll(
                createStatCard("Gegenstände", String.valueOf(gegenstandService.zaehleAlle()), "#4CAF50", 1),
                createStatCard("Verfügbar", String.valueOf(gegenstandService.zaehleNachStatus(VerfuegbarkeitsStatus.VERFUEGBAR)), "#2196F3", 1),
                createStatCard("Ausgeliehen", String.valueOf(gegenstandService.zaehleNachStatus(VerfuegbarkeitsStatus.AUSGELIEHEN)), "#FF9800", 3),
                createStatCard("In Wartung", String.valueOf(gegenstandService.zaehleNachStatus(VerfuegbarkeitsStatus.IN_WARTUNG)), "#9C27B0", 1),
                createStatCard("Überfällig", String.valueOf(ausleiheService.zaehleUeberfaellige()), "#f44336", 4)
            );
        }
    }

    private void refreshUeberfaellig() {
        if (ueberfaelligTabelle != null) {
            ueberfaelligTabelle.setItems(FXCollections.observableArrayList(ausleiheService.ueberfaelligeAusleihen()));
        }
    }

    private <T> void mitAusgewaehltem(TableView<T> tabelle, java.util.function.Consumer<T> action) {
        T selected = tabelle.getSelectionModel().getSelectedItem();
        if (selected != null) {
            action.accept(selected);
        }
    }

    private void setStatus(String message) {
        statusLabel.setText(message);
    }
}
