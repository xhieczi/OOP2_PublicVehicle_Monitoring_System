package OOP2ProjectFinal;

import javafx.application.Application;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import java.util.ArrayList;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;

import javafx.animation.Timeline;
import javafx.animation.KeyFrame;
import javafx.util.Duration;
import java.util.Random;



public class MainFX extends Application {

    private boolean darkMode = false;
    private Label headerTitle;
    private Label headerUser;
    private VBox headerBox;
    private BorderPane dashboardRoot;
    private final List<DataStore.SavedUser> savedUsers = new ArrayList<>();

    private Timeline liveSimulation;
    private final Random random = new Random();

    private final AuthenticationService auth = new AuthenticationService();
    private final MonitoringService monitoring = new MonitoringService();

    private Stage mainStage;
    private VBox sidebar;
    private VBox contentArea;

    private final DateTimeFormatter timeFormatter =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
                    .withZone(ZoneId.systemDefault());

    private final String PRIMARY = "#2563eb";
    private final String PRIMARY_DARK = "#1e3a8a";
    private final String DARK = "#0f172a";
    private final String BG = "#f8fafc";
    private final String CARD = "white";

    @Override
    public void start(Stage stage) {
        mainStage = stage;

        if (DataStore.dataExists()) {
            DataStore.loadAll(auth, monitoring, savedUsers);

            if (!auth.usernameExists("operator")) {
                auth.addUser(new Operator(
                        "U-001",
                        "Default Operator",
                        "operator",
                        "1234"
                ));

                savedUsers.add(new DataStore.SavedUser(
                        "U-001",
                        "Default Operator",
                        "operator",
                        "1234",
                        "Operator"
                ));
            }

            if (!auth.usernameExists("commuter")) {
                auth.addUser(new Commuter(
                        "U-002",
                        "Default Commuter",
                        "commuter",
                        "1234"
                ));

                savedUsers.add(new DataStore.SavedUser(
                        "U-002",
                        "Default Commuter",
                        "commuter",
                        "1234",
                        "Commuter"
                ));
            }

            DataStore.saveAll(auth, monitoring, savedUsers);

        } else {
            seedData();
            DataStore.saveAll(auth, monitoring, savedUsers);
        }

        showLoginScreen();
    }

    private void showLoginScreen() {
        Label title = new Label("Cebu Public Vehicle Monitoring System");
        title.setWrapText(true);
        title.setMaxWidth(520);
        title.setAlignment(Pos.CENTER);
        title.setStyle("-fx-font-size: 30px; -fx-font-weight: bold; -fx-text-fill: #0f172a;");

        Label subtitle = new Label("Smart route, vehicle, and alert monitoring");
        subtitle.setStyle("-fx-font-size: 14px; -fx-text-fill: #64748b;");

        TextField usernameField = input("Username");

        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("Password");
        passwordField.setMaxWidth(320);
        passwordField.setPrefHeight(40);
        passwordField.setStyle(inputStyle());

        Label message = new Label();
        message.setStyle("-fx-text-fill: #ef4444; -fx-font-weight: bold;");

        Button loginButton = primaryButton("Login");
        Button registerButton = secondaryButton("Create Account");
        ToggleButton darkModeToggle = new ToggleButton("Dark Mode");
        darkModeToggle.setStyle("-fx-background-radius: 12; -fx-font-weight: bold;");

        loginButton.setOnAction(e -> {
            User user = auth.login(usernameField.getText().trim(), passwordField.getText().trim());

            if (user == null) {
                message.setText("Invalid username or password.");
                return;
            }

            if (user instanceof Operator) {
                showOperatorDashboard(user);
            } else {
                showCommuterDashboard(user);
            }
        });

        registerButton.setOnAction(e -> showRegisterScreen());

        Label helper = new Label("Default: operator / 1234 or commuter / 1234");
        helper.setStyle("-fx-font-size: 12px; -fx-text-fill: #64748b;");

        VBox card = new VBox(14, title, subtitle, usernameField, passwordField, loginButton, registerButton, darkModeToggle, helper, message);
        card.setAlignment(Pos.CENTER);
        card.setPadding(new Insets(40));
        card.setMaxWidth(620);
        card.setStyle(cardStyle());

        StackPane root = new StackPane(card);
        root.setStyle("-fx-background-color: linear-gradient(to bottom right, #dbeafe, #f8fafc, #eff6ff);");

        darkModeToggle.setOnAction(e -> {
            if (darkModeToggle.isSelected()) {
                darkModeToggle.setText("Light Mode");
                root.setStyle("-fx-background-color: linear-gradient(to bottom right, #020617, #0f172a, #1e293b);");
                card.setStyle("-fx-background-color: #111827; -fx-background-radius: 24; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.45), 25, 0, 0, 8);");
                title.setStyle("-fx-font-size: 30px; -fx-font-weight: bold; -fx-text-fill: white;");
                subtitle.setStyle("-fx-font-size: 14px; -fx-text-fill: #cbd5e1;");
                helper.setStyle("-fx-font-size: 12px; -fx-text-fill: #cbd5e1;");
            } else {
                darkModeToggle.setText("Dark Mode");
                root.setStyle("-fx-background-color: linear-gradient(to bottom right, #dbeafe, #f8fafc, #eff6ff);");
                card.setStyle(cardStyle());
                title.setStyle("-fx-font-size: 30px; -fx-font-weight: bold; -fx-text-fill: #0f172a;");
                subtitle.setStyle("-fx-font-size: 14px; -fx-text-fill: #64748b;");
                helper.setStyle("-fx-font-size: 12px; -fx-text-fill: #64748b;");
            }
        });

        mainStage.setTitle("Vehicle Monitoring System");
        mainStage.setScene(new Scene(root, 1200, 760));
        mainStage.show();
    }

    private void showRegisterScreen() {
        Label title = pageTitle("Create New Account");

        TextField userIdField = input("User ID");
        TextField nameField = input("Full Name");
        TextField usernameField = input("Username");

        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("Password");
        passwordField.setMaxWidth(320);
        passwordField.setPrefHeight(40);
        passwordField.setStyle(inputStyle());

        PasswordField confirmField = new PasswordField();
        confirmField.setPromptText("Confirm Password");
        confirmField.setMaxWidth(320);
        confirmField.setPrefHeight(40);
        confirmField.setStyle(inputStyle());

        ComboBox<String> roleBox = new ComboBox<>();
        roleBox.getItems().addAll("Operator", "Commuter");
        roleBox.setPromptText("Select Role");
        roleBox.setMaxWidth(320);
        roleBox.setPrefHeight(40);
        roleBox.setStyle(inputStyle());

        Label message = new Label();

        Button createButton = primaryButton("Create Account");
        Button backButton = secondaryButton("Back to Login");

        createButton.setOnAction(e -> {
            String userId = userIdField.getText().trim();
            String name = nameField.getText().trim();
            String username = usernameField.getText().trim();
            String password = passwordField.getText().trim();
            String confirm = confirmField.getText().trim();
            String role = roleBox.getValue();

            if (userId.isEmpty() || name.isEmpty() || username.isEmpty() || password.isEmpty() || role == null) {
                showError(message, "Please fill in all fields.");
                return;
            }

            if (auth.userIdExists(userId)) {
                showError(message, "User ID already exists.");
                return;
            }

            if (auth.usernameExists(username)) {
                showError(message, "Username already exists.");
                return;
            }

            if (password.length() < 4) {
                showError(message, "Password must be at least 4 characters.");
                return;
            }

            if (!password.equals(confirm)) {
                showError(message, "Passwords do not match.");
                return;
            }

            User newUser = role.equals("Operator")
                    ? new Operator(userId, name, username, password)
                    : new Commuter(userId, name, username, password);

            auth.addUser(newUser);
            savedUsers.add(new DataStore.SavedUser(userId, name, username, password, role));
            DataStore.saveAll(auth, monitoring, savedUsers);
            showSuccess(message, "Account created successfully!");
        });

        backButton.setOnAction(e -> showLoginScreen());

        VBox card = new VBox(12, title, userIdField, nameField, usernameField, passwordField, confirmField, roleBox, createButton, backButton, message);
        card.setAlignment(Pos.CENTER);
        card.setPadding(new Insets(40));
        card.setMaxWidth(500);
        card.setStyle(cardStyle());

        StackPane root = new StackPane(card);
        root.setStyle("-fx-background-color: linear-gradient(to bottom right, #dbeafe, #f8fafc, #eff6ff);");

        mainStage.setScene(new Scene(root, 1050, 720));
    }

    private void showOperatorDashboard(User user) {
        buildDashboard("Operator Dashboard", user.name());

        addSidebarButton("➕ Add Route", this::showAddRoutePanel);
        addSidebarButton("📍 Add Stop", this::showAddStopPanel);
        addSidebarButton("🚌 Register Vehicle", this::showRegisterVehiclePanel);
        addSidebarButton("🛣 Assign Vehicle", this::showAssignVehiclePanel);
        addSidebarButton("📡 Send Ping", this::showSendPingPanel);
        addSidebarButton("▶ Live Simulation", this::startLiveSimulation);
        addSidebarButton("■ Stop Simulation", this::stopLiveSimulation);
        addSidebarButton("📊 Route Dashboard", this::showRouteDashboardTable);
        addSidebarButton("📌 Route Stops", () -> showTextPanel("Route Stops", getRouteStopsText()));
        addSidebarButton("🚐 All Vehicles", this::showVehiclesTable);
        addSidebarButton("🚨 Alerts Log", this::showAlertsTable);
        addSidebarButton("🔎 Search Vehicle", this::showSearchVehiclePanel);
        addLogoutButton();

        showWelcomePanel("Welcome, " + user.name(), "Choose an action from the sidebar.");
    }

    private void showCommuterDashboard(User user) {
        buildDashboard("Commuter Dashboard", user.name());

        addSidebarButton("📊 Route Status", this::showRouteDashboardTable);
        addSidebarButton("🚨 Recent Alerts", this::showAlertsTable);
        addSidebarButton("📌 Route Stops", () -> showTextPanel("Route Stops", getRouteStopsText()));
        addSidebarButton("🔎 Search Vehicle", this::showSearchVehiclePanel);
        addLogoutButton();

        showWelcomePanel("Welcome, " + user.name(), "View route updates and alerts from the sidebar.");
    }

    private void buildDashboard(String title, String username) {
        dashboardRoot = new BorderPane();

        headerTitle = new Label(title);
        headerTitle.setStyle("-fx-font-size: 24px; -fx-font-weight: bold;");

        headerUser = new Label("Logged in as: " + username);
        headerUser.setStyle("-fx-font-size: 13px;");

        ToggleButton themeToggle = new ToggleButton(darkMode ? "Light Mode" : "Dark Mode");
        themeToggle.setPrefWidth(120);
        themeToggle.setOnAction(e -> {
            darkMode = !darkMode;
            applyDashboardTheme();
            themeToggle.setText(darkMode ? "Light Mode" : "Dark Mode");
        });

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        VBox titleBox = new VBox(5, headerTitle, headerUser);
        HBox headerContent = new HBox(15, titleBox, spacer, themeToggle);
        headerContent.setAlignment(Pos.CENTER_LEFT);

        headerBox = new VBox(headerContent);
        headerBox.setPadding(new Insets(20));

        sidebar = new VBox(10);
        sidebar.setPadding(new Insets(18));
        sidebar.setPrefWidth(240);

        contentArea = new VBox(15);
        contentArea.setPadding(new Insets(28));

        dashboardRoot.setTop(headerBox);
        dashboardRoot.setLeft(sidebar);
        dashboardRoot.setCenter(contentArea);

        applyDashboardTheme();

        mainStage.setScene(new Scene(dashboardRoot, 1150, 760));
    }

    private void applyDashboardTheme() {
        if (dashboardRoot == null) return;

        if (darkMode) {
            dashboardRoot.setStyle("-fx-background-color: #020617;");
            headerBox.setStyle("-fx-background-color: linear-gradient(to right, #020617, #1e293b);");
            sidebar.setStyle("-fx-background-color: #020617;");
            contentArea.setStyle("-fx-background-color: #0f172a;");
            headerTitle.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: white;");
            headerUser.setStyle("-fx-font-size: 13px; -fx-text-fill: #cbd5e1;");
        } else {
            dashboardRoot.setStyle("-fx-background-color: #f8fafc;");
            headerBox.setStyle("-fx-background-color: linear-gradient(to right, #1e3a8a, #2563eb);");
            sidebar.setStyle("-fx-background-color: #0f172a;");
            contentArea.setStyle("-fx-background-color: #f8fafc;");
            headerTitle.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: white;");
            headerUser.setStyle("-fx-font-size: 13px; -fx-text-fill: #dbeafe;");
        }
    }

    private void addSidebarButton(String text, Runnable action) {
        Button button = new Button(text);
        button.setMaxWidth(Double.MAX_VALUE);
        button.setPrefHeight(42);
        button.setAlignment(Pos.CENTER_LEFT);
        button.setStyle(sidebarButtonStyle());
        button.setOnAction(e -> action.run());
        sidebar.getChildren().add(button);
    }

    private void addLogoutButton() {
        Region spacer = new Region();
        VBox.setVgrow(spacer, Priority.ALWAYS);

        Button logout = new Button("Logout");
        logout.setMaxWidth(Double.MAX_VALUE);
        logout.setPrefHeight(42);
        logout.setStyle("-fx-background-color: #ef4444; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 12;");
        logout.setOnAction(e -> {
            auth.logout();
            showLoginScreen();
        });

        sidebar.getChildren().addAll(spacer, logout);
    }

    private void showWelcomePanel(String title, String subtitle) {
        contentArea.getChildren().clear();

        Label titleLabel = pageTitle(title);
        Label subtitleLabel = new Label(subtitle);
        subtitleLabel.setStyle("-fx-font-size: 15px; -fx-text-fill: #475569;");

        HBox stats = new HBox(15,
                statCard("Routes", String.valueOf(monitoring.getRoutes().size())),
                statCard("Vehicles", String.valueOf(monitoring.getAllVehicles().size())),
                statCard("Alerts", String.valueOf(monitoring.getAlerts().size()))
        );

        contentArea.getChildren().addAll(card(titleLabel, subtitleLabel), stats);
    }

    private VBox statCard(String label, String value) {
        Label valueLabel = new Label(value);
        valueLabel.setStyle("-fx-font-size: 30px; -fx-font-weight: bold; -fx-text-fill: #2563eb;");

        Label labelText = new Label(label);
        labelText.setStyle("-fx-font-size: 14px; -fx-text-fill: #64748b;");

        VBox box = new VBox(5, valueLabel, labelText);
        box.setPadding(new Insets(22));
        box.setPrefWidth(180);
        box.setStyle(cardStyle());
        return box;
    }

    private void showTextPanel(String title, String text) {
        contentArea.getChildren().clear();

        Label titleLabel = pageTitle(title);

        TextArea output = new TextArea(text);
        output.setEditable(false);
        output.setWrapText(false);
        output.setStyle("-fx-font-family: monospace; -fx-font-size: 13px; -fx-background-radius: 12;");
        VBox.setVgrow(output, Priority.ALWAYS);

        VBox card = card(titleLabel, output);
        VBox.setVgrow(card, Priority.ALWAYS);

        contentArea.getChildren().add(card);
    }

    private void showVehiclesTable() {
        contentArea.getChildren().clear();

        Label title = pageTitle("All Registered Vehicles");

        TableView<PublicVehicle> table = new TableView<>();
        table.setItems(FXCollections.observableArrayList(monitoring.getAllVehicles()));
        styleTable(table);

        TableColumn<PublicVehicle, String> idCol = new TableColumn<>("Vehicle ID");
        idCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().vehicleId()));

        TableColumn<PublicVehicle, String> typeCol = new TableColumn<>("Type");
        typeCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getVehicleType()));

        TableColumn<PublicVehicle, String> plateCol = new TableColumn<>("Plate");
        plateCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().plateNumber()));

        TableColumn<PublicVehicle, String> capacityCol = new TableColumn<>("Capacity");
        capacityCol.setCellValueFactory(data -> new SimpleStringProperty(String.valueOf(data.getValue().capacity())));

        TableColumn<PublicVehicle, String> routeCol = new TableColumn<>("Route");
        routeCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().routeId() != null ? data.getValue().routeId() : "Unassigned"));

        TableColumn<PublicVehicle, String> statusCol = new TableColumn<>("Status");
        statusCol.setCellValueFactory(data -> new SimpleStringProperty(monitoring.getVehicleStatus(data.getValue())));

        table.getColumns().addAll(idCol, typeCol, plateCol, capacityCol, routeCol, statusCol);
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        Button refresh = primaryButton("Refresh");
        refresh.setOnAction(e -> table.setItems(FXCollections.observableArrayList(monitoring.getAllVehicles())));

        VBox box = card(title, table, refresh);
        VBox.setVgrow(table, Priority.ALWAYS);
        VBox.setVgrow(box, Priority.ALWAYS);

        contentArea.getChildren().add(box);
    }

    private void showAlertsTable() {
        contentArea.getChildren().clear();

        Label title = pageTitle("Alerts Log");

        TableView<Alert> table = new TableView<>();
        table.setItems(FXCollections.observableArrayList(monitoring.getAlerts()));
        styleTable(table);

        TableColumn<Alert, String> timeCol = new TableColumn<>("Time");
        timeCol.setCellValueFactory(data -> new SimpleStringProperty(timeFormatter.format(data.getValue().timestamp())));

        TableColumn<Alert, String> severityCol = new TableColumn<>("Severity");
        severityCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().severity()));

        TableColumn<Alert, String> typeCol = new TableColumn<>("Type");
        typeCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().type()));

        TableColumn<Alert, String> messageCol = new TableColumn<>("Message");
        messageCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().message()));

        table.getColumns().addAll(timeCol, severityCol, typeCol, messageCol);
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        Button refresh = primaryButton("Refresh");
        refresh.setOnAction(e -> table.setItems(FXCollections.observableArrayList(monitoring.getAlerts())));

        VBox box = card(title, table, refresh);
        VBox.setVgrow(table, Priority.ALWAYS);
        VBox.setVgrow(box, Priority.ALWAYS);

        contentArea.getChildren().add(box);
    }

    private void showRouteDashboardTable() {
        contentArea.getChildren().clear();

        Label title = pageTitle("Route Dashboard");

        TableView<DashboardRow> table = new TableView<>();
        table.setItems(FXCollections.observableArrayList(getDashboardRows()));
        styleTable(table);

        TableColumn<DashboardRow, String> vehicleCol = new TableColumn<>("Vehicle");
        vehicleCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().vehicleId()));

        TableColumn<DashboardRow, String> typeCol = new TableColumn<>("Type");
        typeCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().type()));

        TableColumn<DashboardRow, String> routeCol = new TableColumn<>("Route");
        routeCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().route()));

        TableColumn<DashboardRow, String> speedCol = new TableColumn<>("Speed");
        speedCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().speed()));

        TableColumn<DashboardRow, String> paxCol = new TableColumn<>("Passengers");
        paxCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().passengers()));

        TableColumn<DashboardRow, String> statusCol = new TableColumn<>("Status");
        statusCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().status()));

        TableColumn<DashboardRow, String> nearestCol = new TableColumn<>("Nearest Stop");
        nearestCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().nearestStop()));

        TableColumn<DashboardRow, String> etaCol = new TableColumn<>("ETA");
        etaCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().eta()));

        table.getColumns().addAll(vehicleCol, typeCol, routeCol, speedCol, paxCol, statusCol, nearestCol, etaCol);
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        Button refresh = primaryButton("Refresh");
        refresh.setOnAction(e -> table.setItems(FXCollections.observableArrayList(getDashboardRows())));

        VBox box = card(title, table, refresh);
        VBox.setVgrow(table, Priority.ALWAYS);
        VBox.setVgrow(box, Priority.ALWAYS);

        contentArea.getChildren().add(box);
    }

    private List<DashboardRow> getDashboardRows() {
        return monitoring.getAllVehicles().stream().map(v -> {
            String routeText = v.routeId() != null ? v.routeId() : "Unassigned";
            String nearestStop = "N/A";
            String etaText = "N/A";

            if (v.routeId() != null) {
                for (Route r : monitoring.getRoutes()) {
                    if (r.routeId().equals(v.routeId())) {
                        Stop nearest = monitoring.getNearestStopOnRoute(v, r);
                        double eta = monitoring.calculateEtaMinutes(v, nearest);

                        nearestStop = nearest != null ? nearest.stopName() : "N/A";
                        etaText = eta >= 0 ? String.format("%.1f mins", eta) : "N/A";
                        break;
                    }
                }
            }

            String speed = v.hasLocation() ? String.format("%.1f km/h", v.speedKmh()) : "N/A";
            String passengers = v.hasLocation() ? v.passengerCount() + "/" + v.capacity() : "N/A";

            return new DashboardRow(
                    v.vehicleId(),
                    v.getVehicleType(),
                    routeText,
                    speed,
                    passengers,
                    monitoring.getVehicleStatus(v),
                    nearestStop,
                    etaText
            );
        }).toList();
    }

    private void showAddRoutePanel() {
        contentArea.getChildren().clear();

        Label title = pageTitle("Add Route");

        TextField routeId = input("Route ID");
        TextField routeName = input("Route Name");
        Label message = new Label();

        Button save = primaryButton("Save Route");

        save.setOnAction(e -> {
            String id = routeId.getText().trim();
            String name = routeName.getText().trim();

            if (id.isEmpty() || name.isEmpty()) {
                showError(message, "Please fill in all fields.");
                return;
            }

            if (monitoring.routeExists(id)) {
                showError(message, "Route ID already exists.");
                return;
            }

            monitoring.addRoute(new Route(id, name));
            DataStore.saveAll(auth, monitoring, savedUsers);
            routeId.clear();
            routeName.clear();
            showSuccess(message, "Route added successfully.");
        });

        contentArea.getChildren().add(card(title, routeId, routeName, save, message));
    }

    private void startLiveSimulation() {
        if (liveSimulation != null) {
            liveSimulation.stop();
        }

        liveSimulation = new Timeline(
                new KeyFrame(Duration.seconds(3), e -> simulateVehiclePing())
        );

        liveSimulation.setCycleCount(Timeline.INDEFINITE);
        liveSimulation.play();

        showWelcomePanel("Live Simulation Started", "Vehicle pings will update automatically every 3 seconds.");
    }

    private void stopLiveSimulation() {
        if (liveSimulation != null) {
            liveSimulation.stop();
            liveSimulation = null;
        }

        showWelcomePanel("Live Simulation Stopped", "Automatic vehicle updates have been paused.");
    }

    private void simulateVehiclePing() {
        List<PublicVehicle> vehicles = monitoring.getAllVehicles();

        if (vehicles.isEmpty()) {
            return;
        }

        for (PublicVehicle vehicle : vehicles) {
            double baseLat = vehicle.hasLocation() ? vehicle.lat() : 10.3270;
            double baseLon = vehicle.hasLocation() ? vehicle.lon() : 123.9063;

            double newLat = baseLat + ((random.nextDouble() - 0.5) * 0.002);
            double newLon = baseLon + ((random.nextDouble() - 0.5) * 0.002);

            double speed = 10 + random.nextDouble() * 50;
            int passengers = random.nextInt(vehicle.capacity() + 6);

            VehiclePing ping = new VehiclePing(
                    vehicle.vehicleId(),
                    Instant.now(),
                    newLat,
                    newLon,
                    speed,
                    passengers
            );

            monitoring.receivePing(ping);
        }

        DataStore.saveAll(auth, monitoring, savedUsers);
    }

    private void showAddStopPanel() {
        contentArea.getChildren().clear();

        Label title = pageTitle("Add Stop");

        ComboBox<Route> routeBox = new ComboBox<>();
        routeBox.getItems().addAll(monitoring.getRoutes());
        routeBox.setPromptText("Select Route");
        routeBox.setMaxWidth(350);
        routeBox.setPrefHeight(40);
        routeBox.setStyle(inputStyle());

        TextField stopId = input("Stop ID");
        TextField stopName = input("Stop Name");
        TextField lat = input("Latitude");
        TextField lon = input("Longitude");

        Label message = new Label();
        Button save = primaryButton("Save Stop");

        save.setOnAction(e -> {
            try {
                Route route = routeBox.getValue();

                if (route == null || stopId.getText().trim().isEmpty() || stopName.getText().trim().isEmpty()) {
                    showError(message, "Please fill in all fields.");
                    return;
                }

                if (route.stopExists(stopId.getText().trim())) {
                    showError(message, "Stop ID already exists in this route.");
                    return;
                }

                route.addStop(new Stop(
                        stopId.getText().trim(),
                        stopName.getText().trim(),
                        Double.parseDouble(lat.getText().trim()),
                        Double.parseDouble(lon.getText().trim())
                ));

                DataStore.saveAll(auth, monitoring, savedUsers);

                stopId.clear();
                stopName.clear();
                lat.clear();
                lon.clear();
                showSuccess(message, "Stop added successfully.");
            } catch (Exception ex) {
                showError(message, "Invalid latitude or longitude.");
            }
        });

        contentArea.getChildren().add(card(title, routeBox, stopId, stopName, lat, lon, save, message));
    }

    private void showRegisterVehiclePanel() {
        contentArea.getChildren().clear();

        Label title = pageTitle("Register Vehicle");

        ComboBox<String> typeBox = new ComboBox<>();
        typeBox.getItems().addAll("Jeepney", "Modern Jeep", "Bus");
        typeBox.setPromptText("Vehicle Type");
        typeBox.setMaxWidth(350);
        typeBox.setPrefHeight(40);
        typeBox.setStyle(inputStyle());

        TextField vehicleId = input("Vehicle ID");
        TextField plate = input("Plate Number");
        TextField capacity = input("Capacity");

        Label message = new Label();
        Button save = primaryButton("Register Vehicle");

        save.setOnAction(e -> {
            try {
                String type = typeBox.getValue();
                String vid = vehicleId.getText().trim();

                if (type == null || vid.isEmpty() || plate.getText().trim().isEmpty() || capacity.getText().trim().isEmpty()) {
                    showError(message, "Please fill in all fields.");
                    return;
                }

                if (monitoring.vehicleExists(vid)) {
                    showError(message, "Vehicle ID already exists.");
                    return;
                }

                int cap = Integer.parseInt(capacity.getText().trim());

                if (cap <= 0) {
                    showError(message, "Capacity must be positive.");
                    return;
                }

                PublicVehicle vehicle;

                if (type.equals("Jeepney")) {
                    vehicle = new Jeepney(vid, plate.getText().trim(), cap);
                } else if (type.equals("Modern Jeep")) {
                    vehicle = new ModernJeep(vid, plate.getText().trim(), cap);
                } else {
                    vehicle = new Bus(vid, plate.getText().trim(), cap);
                }

                monitoring.registerVehicle(vehicle);
                DataStore.saveAll(auth, monitoring, savedUsers);

                vehicleId.clear();
                plate.clear();
                capacity.clear();
                typeBox.setValue(null);

                showSuccess(message, "Vehicle registered successfully.");
            } catch (Exception ex) {
                showError(message, "Invalid capacity.");
            }
        });

        contentArea.getChildren().add(card(title, typeBox, vehicleId, plate, capacity, save, message));
    }

    private void showAssignVehiclePanel() {
        contentArea.getChildren().clear();

        Label title = pageTitle("Assign Vehicle to Route");

        ComboBox<PublicVehicle> vehicleBox = new ComboBox<>();
        vehicleBox.getItems().addAll(monitoring.getAllVehicles());
        vehicleBox.setPromptText("Select Vehicle");
        vehicleBox.setMaxWidth(350);
        vehicleBox.setPrefHeight(40);
        vehicleBox.setStyle(inputStyle());

        ComboBox<Route> routeBox = new ComboBox<>();
        routeBox.getItems().addAll(monitoring.getRoutes());
        routeBox.setPromptText("Select Route");
        routeBox.setMaxWidth(350);
        routeBox.setPrefHeight(40);
        routeBox.setStyle(inputStyle());

        Label message = new Label();
        Button save = primaryButton("Assign Vehicle");

        save.setOnAction(e -> {
            PublicVehicle vehicle = vehicleBox.getValue();
            Route route = routeBox.getValue();

            if (vehicle == null || route == null) {
                showError(message, "Please select vehicle and route.");
                return;
            }

            monitoring.assignVehicleToRoute(vehicle.vehicleId(), route.routeId());
            DataStore.saveAll(auth, monitoring, savedUsers);
            showSuccess(message, "Vehicle assigned successfully.");
        });

        contentArea.getChildren().add(card(title, vehicleBox, routeBox, save, message));
    }

    private void showSendPingPanel() {
        contentArea.getChildren().clear();

        Label title = pageTitle("Send Vehicle Ping");

        ComboBox<PublicVehicle> vehicleBox = new ComboBox<>();
        vehicleBox.getItems().addAll(monitoring.getAllVehicles());
        vehicleBox.setPromptText("Select Vehicle");
        vehicleBox.setMaxWidth(350);
        vehicleBox.setPrefHeight(40);
        vehicleBox.setStyle(inputStyle());

        TextField lat = input("Latitude");
        TextField lon = input("Longitude");
        TextField speed = input("Speed km/h");
        TextField passengers = input("Passenger Count");

        Label message = new Label();
        Button save = primaryButton("Send Ping");

        save.setOnAction(e -> {
            try {
                PublicVehicle v = vehicleBox.getValue();

                if (v == null) {
                    showError(message, "Please select a vehicle.");
                    return;
                }

                double speedValue = Double.parseDouble(speed.getText().trim());
                int passengerValue = Integer.parseInt(passengers.getText().trim());

                if (speedValue < 0 || passengerValue < 0) {
                    showError(message, "Speed and passengers cannot be negative.");
                    return;
                }

                VehiclePing ping = new VehiclePing(
                        v.vehicleId(),
                        Instant.now(),
                        Double.parseDouble(lat.getText().trim()),
                        Double.parseDouble(lon.getText().trim()),
                        speedValue,
                        passengerValue
                );

                monitoring.receivePing(ping);
                DataStore.saveAll(auth, monitoring, savedUsers);

                lat.clear();
                lon.clear();
                speed.clear();
                passengers.clear();

                showSuccess(message, "Ping sent successfully.");
            } catch (Exception ex) {
                showError(message, "Invalid input.");
            }
        });

        contentArea.getChildren().add(card(title, vehicleBox, lat, lon, speed, passengers, save, message));
    }

    private void showSearchVehiclePanel() {
        contentArea.getChildren().clear();

        Label title = pageTitle("Search Vehicle");

        TextField vehicleId = input("Vehicle ID");
        Button search = primaryButton("Search");

        TextArea output = new TextArea();
        output.setEditable(false);
        output.setWrapText(true);
        output.setStyle("-fx-background-radius: 12; -fx-font-size: 14px;");

        search.setOnAction(e -> {
            PublicVehicle v = monitoring.findVehicleById(vehicleId.getText().trim());

            if (v == null) {
                output.setText("Vehicle not found.");
                return;
            }

            output.setText(
                    "Vehicle ID: " + v.vehicleId() +
                            "\nType: " + v.getVehicleType() +
                            "\nPlate Number: " + v.plateNumber() +
                            "\nCapacity: " + v.capacity() +
                            "\nAssigned Route: " + (v.routeId() != null ? v.routeId() : "Unassigned") +
                            "\nLocation: " + (v.hasLocation() ? "(" + v.lat() + ", " + v.lon() + ")" : "No GPS data yet") +
                            "\nSpeed: " + (v.hasLocation() ? v.speedKmh() + " km/h" : "N/A") +
                            "\nPassengers: " + (v.hasLocation() ? v.passengerCount() + "/" + v.capacity() : "N/A") +
                            "\nStatus: " + monitoring.getVehicleStatus(v)
            );
        });

        contentArea.getChildren().add(card(title, vehicleId, search, output));
    }

    private String getRouteStopsText() {
        StringBuilder sb = new StringBuilder();

        for (Route r : monitoring.getRoutes()) {
            sb.append(r.routeId()).append(" - ").append(r.routeName()).append("\n");

            for (Stop s : r.stops()) {
                sb.append("   ")
                        .append(s.stopId())
                        .append(" - ")
                        .append(s.stopName())
                        .append(" (")
                        .append(s.lat())
                        .append(", ")
                        .append(s.lon())
                        .append(")\n");
            }

            sb.append("\n");
        }

        return sb.length() == 0 ? "No routes available." : sb.toString();
    }

    private VBox card(javafx.scene.Node... nodes) {
        VBox box = new VBox(14);
        box.getChildren().addAll(nodes);
        box.setPadding(new Insets(26));
        box.setStyle(cardStyle());
        return box;
    }

    private Label pageTitle(String text) {
        Label label = new Label(text);

        if (darkMode) {
            label.setStyle("-fx-font-size: 25px; -fx-font-weight: bold; -fx-text-fill: white;");
        } else {
            label.setStyle("-fx-font-size: 25px; -fx-font-weight: bold; -fx-text-fill: #0f172a;");
        }

        return label;
    }

    private TextField input(String prompt) {
        TextField field = new TextField();
        field.setPromptText(prompt);
        field.setMaxWidth(350);
        field.setPrefHeight(40);
        field.setStyle(inputStyle());
        return field;
    }

    private Button primaryButton(String text) {
        Button button = new Button(text);
        button.setPrefHeight(40);
        button.setMinWidth(170);
        button.setStyle("-fx-background-color: #2563eb; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 12;");
        return button;
    }

    private Button secondaryButton(String text) {
        Button button = new Button(text);
        button.setPrefHeight(40);
        button.setMinWidth(170);
        button.setStyle("-fx-background-color: #e2e8f0; -fx-text-fill: #1e293b; -fx-font-weight: bold; -fx-background-radius: 12;");
        return button;
    }

    private void styleTable(TableView<?> table) {
        table.setStyle("-fx-background-color: white; -fx-background-radius: 14; -fx-border-color: #e2e8f0; -fx-border-radius: 14;");
        table.setPrefHeight(520);
    }

    private String inputStyle() {
        return "-fx-background-color: white; -fx-border-color: #cbd5e1; -fx-border-radius: 10; -fx-background-radius: 10; -fx-padding: 8;";
    }

    private String cardStyle() {
        if (darkMode) {
            return "-fx-background-color: #1e293b; -fx-background-radius: 20; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.35), 22, 0, 0, 6);";
        }

        return "-fx-background-color: white; -fx-background-radius: 20; -fx-effect: dropshadow(gaussian, rgba(15,23,42,0.13), 22, 0, 0, 6);";
    }

    private String sidebarButtonStyle() {
        return "-fx-background-color: transparent; -fx-text-fill: #e2e8f0; -fx-font-weight: bold; -fx-font-size: 13px; -fx-background-radius: 12; -fx-padding: 10 14;";
    }

    private void showSuccess(Label label, String text) {
        label.setStyle("-fx-text-fill: #16a34a; -fx-font-weight: bold;");
        label.setText(text);
    }

    private void showError(Label label, String text) {
        label.setStyle("-fx-text-fill: #dc2626; -fx-font-weight: bold;");
        label.setText(text);
    }

    private void seedData() {
        auth.addUser(new Operator("U-001", "Default Operator", "operator", "1234"));
        auth.addUser(new Commuter("U-002", "Default Commuter", "commuter", "1234"));

        Route r = new Route("R-04L", "IT Park - Ayala - Fuente - Colon");
        r.addStop(new Stop("S-001", "IT Park", 10.3270, 123.9063));
        r.addStop(new Stop("S-002", "Ayala Center Cebu", 10.3187, 123.9056));
        r.addStop(new Stop("S-003", "Fuente Osmeña", 10.3090, 123.8929));
        r.addStop(new Stop("S-004", "Colon", 10.2965, 123.8988));
        monitoring.addRoute(r);

        PublicVehicle jeepney = new Jeepney("V-001", "ABC-123", 20);
        monitoring.registerVehicle(jeepney);
        monitoring.assignVehicleToRoute("V-001", "R-04L");
    }

    private record DashboardRow(
            String vehicleId,
            String type,
            String route,
            String speed,
            String passengers,
            String status,
            String nearestStop,
            String eta
    ) {}

    public static void main(String[] args) {
        launch(args);
    }
}