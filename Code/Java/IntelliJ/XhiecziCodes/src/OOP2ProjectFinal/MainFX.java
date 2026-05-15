package OOP2ProjectFinal;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import javafx.util.Duration;
import javafx.scene.web.WebView;
import java.io.InputStream;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class MainFX extends Application {

    private boolean darkMode = true;
    private Label headerTitle;
    private Label headerUser;
    private VBox headerBox;
    private BorderPane dashboardRoot;
    private final List<DataStore.SavedUser> savedUsers = new ArrayList<>();
    private User currentUser;

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

    @Override
    public void start(Stage stage) {
        mainStage = stage;

        if (DataStore.dataExists()) {
            DataStore.loadAll(auth, monitoring, savedUsers);

            if (!auth.usernameExists("operator")) {
                auth.addUser(new Operator("U-001", "Default Operator", "operator", "1234"));
                savedUsers.add(new DataStore.SavedUser("U-001", "Default Operator", "operator", "1234", "Operator"));
            }

            if (!auth.usernameExists("commuter")) {
                auth.addUser(new Commuter("U-002", "Default Commuter", "commuter", "1234"));
                savedUsers.add(new DataStore.SavedUser("U-002", "Default Commuter", "commuter", "1234", "Commuter"));
            }

            DataStore.saveAll(auth, monitoring, savedUsers);
        } else {
            seedData();
            DataStore.saveAll(auth, monitoring, savedUsers);
        }

        ensureDefaultCebuRoutesAndVehicles();
        DataStore.saveAll(auth, monitoring, savedUsers);

        showLoginScreen();
    }

    private ImageView loadLogo() {
        InputStream stream = getClass().getResourceAsStream("/assets/logo.jpeg");

        if (stream == null) {
            return new ImageView();
        }

        Image logo = new Image(stream);
        ImageView logoView = new ImageView(logo);
        logoView.setFitWidth(140);
        logoView.setFitHeight(140);
        logoView.setPreserveRatio(false);

        javafx.scene.shape.Circle clip = new javafx.scene.shape.Circle();
        clip.setCenterX(70);
        clip.setCenterY(70);
        clip.setRadius(70);
        logoView.setClip(clip);

        return logoView;
    }

    private void showLoginScreen() {
        ImageView logoView = loadLogo();

        Label title = new Label("Cebu Public Vehicle Monitoring System");
        title.setWrapText(true);
        title.setMaxWidth(520);
        title.setAlignment(Pos.CENTER);
        title.setStyle("-fx-font-size: 31px; -fx-font-weight: bold; -fx-text-fill: white;");

        Label subtitle = new Label("Smart route, vehicle, and alert monitoring");
        subtitle.setStyle("-fx-font-size: 14px; -fx-text-fill: #cbd5e1;");

        TextField usernameField = input("Username");

        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("Password");
        passwordField.setMaxWidth(340);
        passwordField.setPrefHeight(44);
        passwordField.setStyle(inputStyle());

        Label message = new Label();
        message.setStyle("-fx-text-fill: #f87171; -fx-font-weight: bold;");

        Button loginButton = primaryButton("Login");
        Button registerButton = secondaryButton("Create Account");

        ToggleButton darkModeToggle = new ToggleButton("Light Mode");
        darkModeToggle.setStyle("-fx-background-color: #334155; -fx-text-fill: #e2e8f0; -fx-background-radius: 14; -fx-font-weight: bold;");

        loginButton.setOnAction(e -> {
            User user = auth.login(usernameField.getText().trim(), passwordField.getText().trim());

            if (user == null) {
                message.setText("Invalid username or password.");
                return;
            }
            currentUser = user;

            if (user instanceof Operator) {
                showOperatorDashboard(user);
            } else {
                showCommuterDashboard(user);
            }
        });

        registerButton.setOnAction(e -> showRegisterScreen());

        Label helper = new Label("Default: operator / 1234 or commuter / 1234");
        helper.setStyle("-fx-font-size: 12px; -fx-text-fill: #94a3b8;");

        VBox card = new VBox(14, logoView, title, subtitle, usernameField, passwordField, loginButton, registerButton, darkModeToggle, helper, message);
        card.setAlignment(Pos.CENTER);
        card.setPadding(new Insets(42));
        card.setMaxWidth(640);
        card.setStyle(cardStyle());

        StackPane root = new StackPane(card);
        root.setStyle(darkRootStyle());

        darkModeToggle.setOnAction(e -> {
            if (darkModeToggle.isSelected()) {
                darkMode = false;
                darkModeToggle.setText("Dark Mode");
                root.setStyle("-fx-background-color: linear-gradient(to bottom right, #dbeafe, #f8fafc, #e0f2fe);");
                card.setStyle("-fx-background-color: white; -fx-background-radius: 24; -fx-effect: dropshadow(gaussian, rgba(15,23,42,0.18), 28, 0, 0, 8);");
                title.setStyle("-fx-font-size: 31px; -fx-font-weight: bold; -fx-text-fill: #0f172a;");
                subtitle.setStyle("-fx-font-size: 14px; -fx-text-fill: #475569;");
                helper.setStyle("-fx-font-size: 12px; -fx-text-fill: #64748b;");
            } else {
                darkMode = true;
                darkModeToggle.setText("Light Mode");
                root.setStyle(darkRootStyle());
                card.setStyle(cardStyle());
                title.setStyle("-fx-font-size: 31px; -fx-font-weight: bold; -fx-text-fill: white;");
                subtitle.setStyle("-fx-font-size: 14px; -fx-text-fill: #cbd5e1;");
                helper.setStyle("-fx-font-size: 12px; -fx-text-fill: #94a3b8;");
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
        passwordField.setMaxWidth(340);
        passwordField.setPrefHeight(44);
        passwordField.setStyle(inputStyle());

        PasswordField confirmField = new PasswordField();
        confirmField.setPromptText("Confirm Password");
        confirmField.setMaxWidth(340);
        confirmField.setPrefHeight(44);
        confirmField.setStyle(inputStyle());

        ComboBox<String> roleBox = new ComboBox<>();
        roleBox.getItems().addAll("Operator", "Commuter");
        roleBox.setPromptText("Select Role");
        roleBox.setMaxWidth(340);
        roleBox.setPrefHeight(44);
        styleComboBox(roleBox);

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

        VBox card = new VBox(13, title, userIdField, nameField, usernameField, passwordField, confirmField, roleBox, createButton, backButton, message);
        card.setAlignment(Pos.CENTER);
        card.setPadding(new Insets(40));
        card.setMaxWidth(540);
        card.setStyle(cardStyle());

        StackPane root = new StackPane(card);
        root.setStyle(darkRootStyle());

        mainStage.setScene(new Scene(root, 1050, 720));
    }

    private void showOperatorDashboard(User user) {
        buildDashboard("Operator Dashboard", user.name());

        addSidebarSection("MAIN");
        addSidebarButton("🏠 Dashboard", () -> showWelcomePanel("Welcome, " + user.name(), "Choose an action from the sidebar."));
        addSidebarButton("📊 Route Dashboard", this::showRouteDashboardTable);
        addSidebarButton("🗺 Live Map", this::showLiveMapPanel);

        addSidebarSection("MANAGEMENT");
        addSidebarButton("➕ Add Data", this::showAddPanel);
        addSidebarButton("🛣 Assign Vehicle", this::showAssignVehiclePanel);
        addSidebarButton("🗑 Remove Data", this::showRemovePanel);
        addSidebarButton("🚐 Vehicles", this::showVehiclesTable);

        addSidebarSection("MONITORING");
        addSidebarButton("📡 Send Ping", this::showSendPingPanel);
        addSidebarButton("▶ Start Simulation", this::startLiveSimulation);
        addSidebarButton("■ Stop Simulation", this::stopLiveSimulation);
        addSidebarButton("🚨 Alerts", this::showAlertsTable);
        addSidebarButton("🔔 Notifications", this::showNotificationCenterPanel);

        addSidebarSection("ACCOUNT");
        addSidebarButton("👤 Profile", this::showProfilePanel);
        addSidebarButton("⚙ Settings", this::showSettingsPanel);
        addSidebarButton("ℹ About", this::showAboutPanel);
        addSidebarButton("❔ Help", this::showHelpPanel);

        addLogoutButton();

        showWelcomePanel("Welcome, " + user.name(), "Choose an action from the sidebar.");
    }

    private void showCommuterDashboard(User user) {
        buildDashboard("Commuter Dashboard", user.name());

        addSidebarSection("MAIN");
        addSidebarButton("🏠 Dashboard", () -> showWelcomePanel("Welcome, " + user.name(), "View route updates and alerts from the sidebar."));
        addSidebarButton("📊 Route Status", this::showRouteDashboardTable);
        addSidebarButton("🗺 Live Map", this::showLiveMapPanel);
        addSidebarButton("⏱ ETA Tracker", this::showEtaTrackerPanel);

        addSidebarSection("ROUTES");
        addSidebarButton("🔎 Search by Route", this::showSearchByRoutePanel);
        addSidebarButton("📌 Route Stops", () -> showTextPanel("Route Stops", getRouteStopsText()));
        addSidebarButton("🚨 Recent Alerts", this::showAlertsTable);
        addSidebarButton("🔔 Notifications", this::showNotificationCenterPanel);

        addSidebarSection("ACCOUNT");
        addSidebarButton("👤 Profile", this::showProfilePanel);
        addSidebarButton("⚙ Settings", this::showSettingsPanel);
        addSidebarButton("ℹ About", this::showAboutPanel);
        addSidebarButton("❔ Help", this::showHelpPanel);

        addLogoutButton();

        showWelcomePanel("Welcome, " + user.name(), "View route updates and alerts from the sidebar.");
    }

    private void buildDashboard(String title, String username) {
        dashboardRoot = new BorderPane();

        headerTitle = new Label(title);
        headerTitle.setStyle("-fx-font-size: 25px; -fx-font-weight: bold; -fx-text-fill: white;");

        headerUser = new Label("Logged in as: " + username);
        headerUser.setStyle("-fx-font-size: 13px; -fx-text-fill: #cbd5e1;");

        ToggleButton themeToggle = new ToggleButton(darkMode ? "Light Mode" : "Dark Mode");
        themeToggle.setPrefWidth(120);
        themeToggle.setStyle("-fx-background-color: #334155; -fx-text-fill: white; -fx-background-radius: 12; -fx-font-weight: bold;");
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
        sidebar.setPrefWidth(245);

        contentArea = new VBox(15);
        contentArea.setPadding(new Insets(28));

        dashboardRoot.setTop(headerBox);
        ScrollPane sidebarScroll = new ScrollPane(sidebar);
        sidebarScroll.setFitToWidth(true);
        sidebarScroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        sidebarScroll.setStyle("-fx-background: transparent; -fx-background-color: transparent;");

        dashboardRoot.setLeft(sidebarScroll);
        ScrollPane contentScroll = new ScrollPane(contentArea);
        contentScroll.setFitToWidth(true);
        contentScroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        contentScroll.setStyle("-fx-background: transparent; -fx-background-color: transparent;");

        dashboardRoot.setCenter(contentScroll);

        applyDashboardTheme();

        mainStage.setScene(new Scene(dashboardRoot, 1150, 760));
    }

    private void applyDashboardTheme() {
        if (dashboardRoot == null) return;

        if (darkMode) {
            dashboardRoot.setStyle("-fx-background-color: #020617;");
            headerBox.setStyle("-fx-background-color: linear-gradient(to right, #0f172a, #1e293b, #312e81);");
            sidebar.setStyle("-fx-background-color: #020617; -fx-border-color: #1e293b; -fx-border-width: 0 1 0 0;");
            contentArea.setStyle("-fx-background-color: linear-gradient(to bottom right, #020617, #0f172a);");
            headerTitle.setStyle("-fx-font-size: 25px; -fx-font-weight: bold; -fx-text-fill: white;");
            headerUser.setStyle("-fx-font-size: 13px; -fx-text-fill: #cbd5e1;");
        } else {
            dashboardRoot.setStyle("-fx-background-color: #f8fafc;");
            headerBox.setStyle("-fx-background-color: linear-gradient(to right, #1e3a8a, #2563eb);");
            sidebar.setStyle("-fx-background-color: #0f172a;");
            contentArea.setStyle("-fx-background-color: #e2e8f0;");
            headerTitle.setStyle("-fx-font-size: 25px; -fx-font-weight: bold; -fx-text-fill: white;");
            headerUser.setStyle("-fx-font-size: 13px; -fx-text-fill: #dbeafe;");
        }
    }

    private void showProfilePanel() {
        contentArea.getChildren().clear();

        Label title = pageTitle("My Profile");

        if (currentUser == null) {
            Label message = new Label();
            showError(message, "No user is currently logged in.");
            contentArea.getChildren().add(card(title, message));
            return;
        }

        Label avatar = new Label(currentUser.name().substring(0, 1).toUpperCase());
        avatar.setAlignment(Pos.CENTER);
        avatar.setMinSize(90, 90);
        avatar.setMaxSize(90, 90);
        avatar.setStyle(
                "-fx-background-color: linear-gradient(to bottom right, #2563eb, #7c3aed);" +
                        "-fx-text-fill: white;" +
                        "-fx-font-size: 36px;" +
                        "-fx-font-weight: bold;" +
                        "-fx-background-radius: 50;"
        );

        Label nameLabel = new Label(currentUser.name());
        nameLabel.setStyle("-fx-text-fill: white; -fx-font-size: 24px; -fx-font-weight: bold;");

        Label usernameLabel = new Label("@" + currentUser.username());
        usernameLabel.setStyle("-fx-text-fill: #cbd5e1; -fx-font-size: 14px;");

        Label roleBadge = new Label(currentUser.role());
        roleBadge.setStyle(
                "-fx-background-color: #1d4ed8;" +
                        "-fx-text-fill: white;" +
                        "-fx-font-weight: bold;" +
                        "-fx-padding: 6 14;" +
                        "-fx-background-radius: 20;"
        );

        VBox profileHeader = new VBox(8, avatar, nameLabel, usernameLabel, roleBadge);
        profileHeader.setAlignment(Pos.CENTER);

        int totalRoutes = monitoring.getRoutes().size();
        int totalVehicles = monitoring.getAllVehicles().size();
        int totalAlerts = monitoring.getAlerts().size();

        HBox stats = new HBox(15,
                statCard("Routes", String.valueOf(totalRoutes)),
                statCard("Vehicles", String.valueOf(totalVehicles)),
                statCard("Alerts", String.valueOf(totalAlerts))
        );
        stats.setAlignment(Pos.CENTER);

        Label accountTitle = new Label("Account Details");
        accountTitle.setStyle("-fx-text-fill: white; -fx-font-size: 18px; -fx-font-weight: bold;");

        TextField nameField = input("Full Name");
        nameField.setText(currentUser.name());

        TextField usernameField = input("Username");
        usernameField.setText(currentUser.username());

        Label userIdLabel = new Label("User ID: " + currentUser.userId());
        userIdLabel.setStyle("-fx-text-fill: #cbd5e1; -fx-font-size: 14px;");

        Label roleLabel = new Label("Access Level: " + currentUser.role());
        roleLabel.setStyle("-fx-text-fill: #cbd5e1; -fx-font-size: 14px;");

        Label securityTitle = new Label("Security");
        securityTitle.setStyle("-fx-text-fill: white; -fx-font-size: 18px; -fx-font-weight: bold;");

        Label securityText = new Label("Password is protected and hidden for account safety.");
        securityText.setStyle("-fx-text-fill: #cbd5e1; -fx-font-size: 14px;");

        Label message = new Label();

        Button save = primaryButton("Save Profile Changes");
        save.setOnAction(e -> {
            String newName = nameField.getText().trim();
            String newUsername = usernameField.getText().trim();

            if (newName.isEmpty() || newUsername.isEmpty()) {
                showError(message, "Name and username cannot be empty.");
                return;
            }

            boolean updated = auth.updateUserProfile(currentUser, newName, newUsername);

            if (!updated) {
                showError(message, "Username already exists.");
                return;
            }

            updateSavedUserRecord(currentUser.userId(), newName, newUsername, null);
            DataStore.saveAll(auth, monitoring, savedUsers);

            headerUser.setText("Logged in as: " + newName);
            showSuccess(message, "Profile updated successfully.");
            showProfilePanel();
        });

        VBox accountCard = card(
                accountTitle,
                userIdLabel,
                roleLabel,
                nameField,
                usernameField,
                save,
                message
        );

        VBox securityCard = card(
                securityTitle,
                securityText
        );

        contentArea.getChildren().addAll(
                card(title, profileHeader),
                stats,
                accountCard,
                securityCard
        );
    }

    private void showSettingsPanel() {
        contentArea.getChildren().clear();

        Label title = pageTitle("Settings");

        ToggleButton themeToggle = new ToggleButton(darkMode ? "Switch to Light Mode" : "Switch to Dark Mode");
        themeToggle.setPrefHeight(44);
        themeToggle.setMinWidth(220);
        themeToggle.setStyle("-fx-background-color: #334155; -fx-text-fill: white; -fx-background-radius: 14; -fx-font-weight: bold;");

        Label themeMessage = new Label();

        themeToggle.setOnAction(e -> {
            darkMode = !darkMode;
            applyDashboardTheme();
            showSuccess(themeMessage, "Theme updated successfully.");
            showSettingsPanel();
        });

        Label passwordTitle = new Label("Change Password");
        passwordTitle.setStyle("-fx-text-fill: white; -fx-font-size: 18px; -fx-font-weight: bold;");

        PasswordField currentPassword = new PasswordField();
        currentPassword.setPromptText("Current Password");
        currentPassword.setMaxWidth(350);
        currentPassword.setPrefHeight(44);
        currentPassword.setStyle(inputStyle());

        PasswordField newPassword = new PasswordField();
        newPassword.setPromptText("New Password");
        newPassword.setMaxWidth(350);
        newPassword.setPrefHeight(44);
        newPassword.setStyle(inputStyle());

        PasswordField confirmPassword = new PasswordField();
        confirmPassword.setPromptText("Confirm New Password");
        confirmPassword.setMaxWidth(350);
        confirmPassword.setPrefHeight(44);
        confirmPassword.setStyle(inputStyle());

        Label passwordMessage = new Label();

        Button changePasswordBtn = primaryButton("Change Password");

        changePasswordBtn.setOnAction(e -> {
            if (currentUser == null) {
                showError(passwordMessage, "No user is logged in.");
                return;
            }

            String current = currentPassword.getText().trim();
            String newPass = newPassword.getText().trim();
            String confirm = confirmPassword.getText().trim();

            if (current.isEmpty() || newPass.isEmpty() || confirm.isEmpty()) {
                showError(passwordMessage, "Please fill in all password fields.");
                return;
            }

            if (newPass.length() < 4) {
                showError(passwordMessage, "New password must be at least 4 characters.");
                return;
            }

            if (!newPass.equals(confirm)) {
                showError(passwordMessage, "New passwords do not match.");
                return;
            }

            boolean changed = auth.changePassword(currentUser, current, newPass);

            if (!changed) {
                showError(passwordMessage, "Current password is incorrect.");
                return;
            }

            updateSavedUserRecord(currentUser.userId(), null, null, newPass);
            DataStore.saveAll(auth, monitoring, savedUsers);

            currentPassword.clear();
            newPassword.clear();
            confirmPassword.clear();

            showSuccess(passwordMessage, "Password changed successfully.");
        });

        Label appInfo = new Label(
                "Application: Cebu Public Vehicle Monitoring System\n" +
                        "Version: 1.0\n" +
                        "Mode: JavaFX GUI\n" +
                        "Purpose: Route, vehicle, alert, and commuter monitoring"
        );
        appInfo.setStyle("-fx-text-fill: white; -fx-font-size: 14px;");

        contentArea.getChildren().add(card(
                title,
                themeToggle,
                themeMessage,
                passwordTitle,
                currentPassword,
                newPassword,
                confirmPassword,
                changePasswordBtn,
                passwordMessage,
                appInfo
        ));
    }

    private void showLiveMapPanel() {
        contentArea.getChildren().clear();

        Label title = pageTitle("Live Vehicle Map");

        ComboBox<PublicVehicle> vehicleBox = new ComboBox<>();
        vehicleBox.getItems().addAll(monitoring.getAllVehicles());
        vehicleBox.setPromptText("Select Vehicle");
        vehicleBox.setMaxWidth(350);
        vehicleBox.setPrefHeight(44);
        styleComboBox(vehicleBox);

        Label message = new Label();
        Button showMapBtn = primaryButton("Show Vehicle");

        WebView webView = new WebView();
        webView.setPrefSize(850, 430);
        webView.setMinHeight(430);

        showMapBtn.setOnAction(e -> {
            PublicVehicle selected = vehicleBox.getValue();

            if (selected == null) {
                showError(message, "Please select a vehicle.");
                return;
            }

            double lat = selected.hasLocation() ? selected.lat() : 10.3157;
            double lon = selected.hasLocation() ? selected.lon() : 123.8854;

            String vehicleLabel = selected.getVehicleType() + " " + selected.vehicleId();

            String html = """
            <!DOCTYPE html>
            <html>
            <head>
                <meta charset="utf-8">
                <meta name="viewport" content="width=device-width, initial-scale=1.0">

                <link rel="stylesheet" href="https://unpkg.com/leaflet@1.9.4/dist/leaflet.css">
                <script src="https://unpkg.com/leaflet@1.9.4/dist/leaflet.js"></script>

                <style>
                    html, body, #map {
                        width: 100%%;
                        height: 100%%;
                        margin: 0;
                        padding: 0;
                        overflow: hidden;
                        background: #e5e7eb;
                    }

                    .vehicle-marker {
                        width: 44px;
                        height: 44px;
                        background: linear-gradient(135deg, #2563eb, #7c3aed);
                        color: white;
                        border: 4px solid white;
                        border-radius: 50%%;
                        display: flex;
                        align-items: center;
                        justify-content: center;
                        font-size: 18px;
                        font-weight: bold;
                        font-family: Arial, sans-serif;
                        box-shadow: 0 4px 14px rgba(0,0,0,0.35);
                    }

                    .popup {
                        font-family: Arial, sans-serif;
                        font-size: 14px;
                        line-height: 1.4;
                    }
                </style>
            </head>

            <body>
                <div id="map"></div>

                <script>
                    const lat = %f;
                    const lon = %f;

                    const map = L.map('map', {
                        zoomControl: true,
                        attributionControl: true
                    }).setView([lat, lon], 14);

                    L.tileLayer('https://basemaps.cartocdn.com/rastertiles/voyager/{z}/{x}/{y}.png', {
                        maxZoom: 19,
                        attribution: '&copy; OpenStreetMap &copy; CARTO'
                    }).addTo(map);

                    const vehicleIcon = L.divIcon({
                        html: '<div class="vehicle-marker">V</div>',
                        className: '',
                        iconSize: [52, 52],
                        iconAnchor: [26, 26]
                    });

                    L.marker([lat, lon], { icon: vehicleIcon })
                        .addTo(map)
                        .bindPopup('<div class="popup"><b>%s</b><br>Lat: %f<br>Lon: %f</div>')
                        .openPopup();

                    L.circle([lat, lon], {
                        radius: 150,
                        color: '#2563eb',
                        fillColor: '#3b82f6',
                        fillOpacity: 0.18
                    }).addTo(map);

                    setTimeout(function() {
                        map.invalidateSize(true);
                        map.setView([lat, lon], 14);
                    }, 1000);

                    setTimeout(function() {
                        map.invalidateSize(true);
                        map.setView([lat, lon], 14);
                    }, 2000);
                </script>
            </body>
            </html>
            """.formatted(lat, lon, vehicleLabel, lat, lon);

            webView.getEngine().loadContent(html);
            showSuccess(message, "Live map loaded.");
        });

        contentArea.getChildren().add(card(title, vehicleBox, showMapBtn, message, webView));
    }

    private int lonToTileX(double lon, int zoom) {
        return (int) Math.floor((lon + 180.0) / 360.0 * Math.pow(2.0, zoom));
    }

    private int latToTileY(double lat, int zoom) {
        double latRad = Math.toRadians(lat);
        return (int) Math.floor(
                (1.0 - Math.log(Math.tan(latRad) + 1.0 / Math.cos(latRad)) / Math.PI)
                        / 2.0 * Math.pow(2.0, zoom)
        );
    }

    private void addSidebarButton(String text, Runnable action) {
        Button button = new Button(text);
        button.setMaxWidth(Double.MAX_VALUE);
        button.setPrefHeight(44);
        button.setAlignment(Pos.CENTER_LEFT);
        button.setStyle(sidebarButtonStyle());
        button.setOnAction(e -> action.run());
        sidebar.getChildren().add(button);
    }
    private void addSidebarSection(String text) {
        Label section = new Label(text);

        String textColor = darkMode ? "#f8fafc" : "#0f172a";
        String bgColor = darkMode ? "#1e293b" : "#dbeafe";

        section.setStyle(
                "-fx-text-fill: " + textColor + ";" +
                        "-fx-font-size: 11px;" +
                        "-fx-font-weight: bold;" +
                        "-fx-letter-spacing: 1px;" +
                        "-fx-background-color: " + bgColor + ";" +
                        "-fx-padding: 8 12 8 12;" +
                        "-fx-background-radius: 10;"
        );

        section.setMaxWidth(Double.MAX_VALUE);

        sidebar.getChildren().add(section);
    }

    private void addLogoutButton() {
        Region spacer = new Region();
        VBox.setVgrow(spacer, Priority.ALWAYS);

        Button logout = new Button("Logout");
        logout.setMaxWidth(Double.MAX_VALUE);
        logout.setPrefHeight(44);
        logout.setStyle("-fx-background-color: #dc2626; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 14;");
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
        subtitleLabel.setStyle(darkMode ? "-fx-font-size: 15px; -fx-text-fill: #cbd5e1;" : "-fx-font-size: 15px; -fx-text-fill: #475569;");

        HBox stats = new HBox(15,
                statCard("Routes", String.valueOf(monitoring.getRoutes().size())),
                statCard("Vehicles", String.valueOf(monitoring.getAllVehicles().size())),
                statCard("Alerts", String.valueOf(monitoring.getAlerts().size()))
        );

        contentArea.getChildren().addAll(card(titleLabel, subtitleLabel), stats);
    }

    private VBox statCard(String label, String value) {
        Label valueLabel = new Label(value);
        valueLabel.setStyle("-fx-font-size: 31px; -fx-font-weight: bold; -fx-text-fill: #60a5fa;");

        Label labelText = new Label(label);
        labelText.setStyle(darkMode ? "-fx-font-size: 14px; -fx-text-fill: #cbd5e1;" : "-fx-font-size: 14px; -fx-text-fill: #64748b;");

        VBox box = new VBox(5, valueLabel, labelText);
        box.setPadding(new Insets(22));
        box.setPrefWidth(185);
        box.setStyle(cardStyle());
        return box;
    }

    private void showAddPanel() {
        contentArea.getChildren().clear();

        Label title = pageTitle("Add Data");

        ComboBox<String> addTypeBox = new ComboBox<>();
        addTypeBox.getItems().addAll("Route", "Stop", "Vehicle");
        addTypeBox.setPromptText("Select what to add");
        addTypeBox.setMaxWidth(350);
        addTypeBox.setPrefHeight(44);
        styleComboBox(addTypeBox);

        TextField routeIdField = input("Route ID");
        TextField routeNameField = input("Route Name");

        ComboBox<Route> routeBox = new ComboBox<>();
        routeBox.getItems().addAll(monitoring.getRoutes());
        routeBox.setPromptText("Select Route");
        routeBox.setMaxWidth(350);
        routeBox.setPrefHeight(44);
        styleComboBox(routeBox);

        TextField stopIdField = input("Stop ID");
        TextField stopNameField = input("Stop Name");
        TextField latField = input("Latitude");
        TextField lonField = input("Longitude");

        ComboBox<String> vehicleTypeBox = new ComboBox<>();
        vehicleTypeBox.getItems().addAll("Jeepney", "Modern Jeep", "Bus");
        vehicleTypeBox.setPromptText("Vehicle Type");
        vehicleTypeBox.setMaxWidth(350);
        vehicleTypeBox.setPrefHeight(44);
        styleComboBox(vehicleTypeBox);

        TextField vehicleIdField = input("Vehicle ID");
        TextField plateField = input("Plate Number");
        TextField capacityField = input("Capacity");

        Label message = new Label();
        Button addButton = primaryButton("Add");

        javafx.scene.Node[] routeFields = {routeIdField, routeNameField};
        javafx.scene.Node[] stopFields = {routeBox, stopIdField, stopNameField, latField, lonField};
        javafx.scene.Node[] vehicleFields = {vehicleTypeBox, vehicleIdField, plateField, capacityField};

        hideNodes(routeFields);
        hideNodes(stopFields);
        hideNodes(vehicleFields);

        addTypeBox.setOnAction(e -> {
            String selected = addTypeBox.getValue();

            hideNodes(routeFields);
            hideNodes(stopFields);
            hideNodes(vehicleFields);
            message.setText("");

            if ("Route".equals(selected)) {
                showNodes(routeFields);
            } else if ("Stop".equals(selected)) {
                routeBox.getItems().setAll(monitoring.getRoutes());
                showNodes(stopFields);
            } else if ("Vehicle".equals(selected)) {
                showNodes(vehicleFields);
            }
        });

        addButton.setOnAction(e -> {
            String selected = addTypeBox.getValue();

            if (selected == null) {
                showError(message, "Please select what to add.");
                return;
            }

            if ("Route".equals(selected)) {
                String routeId = routeIdField.getText().trim();
                String routeName = routeNameField.getText().trim();

                if (routeId.isEmpty() || routeName.isEmpty()) {
                    showError(message, "Please fill in all route fields.");
                    return;
                }

                if (monitoring.routeExists(routeId)) {
                    showError(message, "Route ID already exists.");
                    return;
                }

                monitoring.addRoute(new Route(routeId, routeName));
                DataStore.saveAll(auth, monitoring, savedUsers);

                routeIdField.clear();
                routeNameField.clear();
                showSuccess(message, "Route added successfully.");
            }

            else if ("Stop".equals(selected)) {
                try {
                    Route route = routeBox.getValue();
                    String stopId = stopIdField.getText().trim();
                    String stopName = stopNameField.getText().trim();

                    if (route == null || stopId.isEmpty() || stopName.isEmpty()
                            || latField.getText().trim().isEmpty()
                            || lonField.getText().trim().isEmpty()) {
                        showError(message, "Please fill in all stop fields.");
                        return;
                    }

                    if (route.stopExists(stopId)) {
                        showError(message, "Stop ID already exists in this route.");
                        return;
                    }

                    route.addStop(new Stop(
                            stopId,
                            stopName,
                            Double.parseDouble(latField.getText().trim()),
                            Double.parseDouble(lonField.getText().trim())
                    ));

                    DataStore.saveAll(auth, monitoring, savedUsers);

                    stopIdField.clear();
                    stopNameField.clear();
                    latField.clear();
                    lonField.clear();
                    showSuccess(message, "Stop added successfully.");
                } catch (Exception ex) {
                    showError(message, "Invalid latitude or longitude.");
                }
            }

            else if ("Vehicle".equals(selected)) {
                try {
                    String type = vehicleTypeBox.getValue();
                    String vehicleId = vehicleIdField.getText().trim();
                    String plate = plateField.getText().trim();
                    String capacityText = capacityField.getText().trim();

                    if (type == null || vehicleId.isEmpty() || plate.isEmpty() || capacityText.isEmpty()) {
                        showError(message, "Please fill in all vehicle fields.");
                        return;
                    }

                    if (monitoring.vehicleExists(vehicleId)) {
                        showError(message, "Vehicle ID already exists.");
                        return;
                    }

                    int capacity = Integer.parseInt(capacityText);

                    if (capacity <= 0) {
                        showError(message, "Capacity must be positive.");
                        return;
                    }

                    PublicVehicle vehicle;

                    if (type.equals("Jeepney")) {
                        vehicle = new Jeepney(vehicleId, plate, capacity);
                    } else if (type.equals("Modern Jeep")) {
                        vehicle = new ModernJeep(vehicleId, plate, capacity);
                    } else {
                        vehicle = new Bus(vehicleId, plate, capacity);
                    }

                    monitoring.registerVehicle(vehicle);
                    DataStore.saveAll(auth, monitoring, savedUsers);

                    vehicleTypeBox.setValue(null);
                    vehicleIdField.clear();
                    plateField.clear();
                    capacityField.clear();
                    showSuccess(message, "Vehicle added successfully.");
                } catch (Exception ex) {
                    showError(message, "Invalid capacity.");
                }
            }
        });

        contentArea.getChildren().add(card(
                title,
                addTypeBox,
                routeIdField,
                routeNameField,
                routeBox,
                stopIdField,
                stopNameField,
                latField,
                lonField,
                vehicleTypeBox,
                vehicleIdField,
                plateField,
                capacityField,
                addButton,
                message
        ));
    }

    private void showNotificationCenterPanel() {
        contentArea.getChildren().clear();

        Label title = pageTitle("Notification Center");

        List<Alert> alerts = monitoring.getAlerts();

        TextArea output = new TextArea();
        output.setEditable(false);
        output.setWrapText(true);
        output.setStyle(textAreaStyle());

        if (alerts.isEmpty()) {
            output.setText("No notifications yet.");
        } else {
            StringBuilder sb = new StringBuilder();

            sb.append("Recent System Notifications\n");
            sb.append("===========================\n\n");

            for (Alert alert : alerts) {
                sb.append("Type: ").append(alert.type()).append("\n");
                sb.append("Severity: ").append(alert.severity()).append("\n");
                sb.append("Message: ").append(alert.message()).append("\n");
                sb.append("Time: ").append(timeFormatter.format(alert.timestamp())).append("\n");
                sb.append("--------------------------------\n");
            }

            output.setText(sb.toString());
        }

        Button refresh = primaryButton("Refresh Notifications");
        refresh.setOnAction(e -> showNotificationCenterPanel());

        contentArea.getChildren().add(card(title, refresh, output));
    }

    private void showAboutPanel() {
        contentArea.getChildren().clear();

        Label title = pageTitle("About the System");

        TextArea about = new TextArea();
        about.setEditable(false);
        about.setWrapText(true);
        about.setStyle(textAreaStyle());

        about.setText(
                "CEBU PUBLIC VEHICLE MONITORING SYSTEM\n\n" +
                        "This system is a Java-based public transportation monitoring application designed " +
                        "to help operators manage public vehicles and help commuters view route-related updates.\n\n" +

                        "MAIN PURPOSE:\n" +
                        "- Monitor public vehicles such as jeepneys, modern jeeps, and buses\n" +
                        "- Track vehicle speed, passenger count, and location\n" +
                        "- Detect transport issues such as overcapacity, overspeeding, idle vehicles, traffic, off-route movement, and bunching\n" +
                        "- Provide route dashboards, alerts, and live map visualization\n\n" +

                        "TECHNOLOGIES USED:\n" +
                        "- Java\n" +
                        "- JavaFX\n" +
                        "- Object-Oriented Programming\n" +
                        "- CSV File Storage\n" +
                        "- WebView + Leaflet Map\n\n" +

                        "OOP CONCEPTS USED:\n" +
                        "- Encapsulation through private fields and getter methods\n" +
                        "- Inheritance through User, Operator, Commuter, and PublicVehicle classes\n" +
                        "- Abstraction through abstract classes such as User, PublicVehicle, and Alert\n" +
                        "- Polymorphism through different vehicle and alert types"
        );

        contentArea.getChildren().add(card(title, about));
    }

    private void showHelpPanel() {
        contentArea.getChildren().clear();

        Label title = pageTitle("Help / User Guide");

        TextArea help = new TextArea();
        help.setEditable(false);
        help.setWrapText(true);
        help.setStyle(textAreaStyle());

        help.setText(
                "HOW TO USE THE SYSTEM\n\n" +

                        "FOR OPERATORS:\n" +
                        "1. Use Add to create routes, stops, and vehicles.\n" +
                        "2. Use Assign Vehicle to connect a vehicle to a route.\n" +
                        "3. Use Send Ping to update a vehicle's location, speed, and passenger count.\n" +
                        "4. Use Live Simulation to automatically generate vehicle movement data.\n" +
                        "5. Use Route Dashboard to monitor current vehicle status.\n" +
                        "6. Use Alerts Log or Notification Center to view detected problems.\n" +
                        "7. Use Live Map to see the selected vehicle's latest location.\n" +
                        "8. Use Remove if routes, stops, or vehicles need to be deleted.\n\n" +

                        "FOR COMMUTERS:\n" +
                        "1. Use Route Status to view vehicle information.\n" +
                        "2. Use Search by Route to find available vehicles on a selected route.\n" +
                        "3. Use Live Map to view the latest vehicle location.\n" +
                        "4. Use Recent Alerts to check transport warnings.\n" +
                        "5. Use Route Stops to view stops under each route.\n\n" +

                        "NOTES:\n" +
                        "- A vehicle needs a ping before it has live GPS data.\n" +
                        "- Live Simulation automatically sends fake GPS pings for demo purposes.\n" +
                        "- Alerts are generated when the system detects risky or abnormal vehicle conditions."
        );

        contentArea.getChildren().add(card(title, help));
    }

    private void updateSavedUserRecord(String userId, String newName, String newUsername, String newPassword) {
        for (int i = 0; i < savedUsers.size(); i++) {
            DataStore.SavedUser saved = savedUsers.get(i);

            if (saved.userId().equals(userId)) {
                String updatedName = newName != null ? newName : saved.name();
                String updatedUsername = newUsername != null ? newUsername : saved.username();
                String updatedPassword = newPassword != null ? newPassword : saved.password();

                savedUsers.set(i, new DataStore.SavedUser(
                        saved.userId(),
                        updatedName,
                        updatedUsername,
                        updatedPassword,
                        saved.role()
                ));

                return;
            }
        }
    }

    private void showAssignVehiclePanel() {
        contentArea.getChildren().clear();

        Label title = pageTitle("Assign Vehicle to Route");

        ComboBox<PublicVehicle> vehicleBox = new ComboBox<>();
        vehicleBox.getItems().addAll(monitoring.getAllVehicles());
        vehicleBox.setPromptText("Select Vehicle");
        vehicleBox.setMaxWidth(350);
        vehicleBox.setPrefHeight(44);
        styleComboBox(vehicleBox);

        ComboBox<Route> routeBox = new ComboBox<>();
        routeBox.getItems().addAll(monitoring.getRoutes());
        routeBox.setPromptText("Select Route");
        routeBox.setMaxWidth(350);
        routeBox.setPrefHeight(44);
        styleComboBox(routeBox);

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

    private void showRemovePanel() {
        contentArea.getChildren().clear();

        Label title = pageTitle("Remove Data");

        ComboBox<String> removeTypeBox = new ComboBox<>();
        removeTypeBox.getItems().addAll("Route", "Stop", "Vehicle");
        removeTypeBox.setPromptText("Select what to remove");
        removeTypeBox.setMaxWidth(350);
        removeTypeBox.setPrefHeight(44);
        styleComboBox(removeTypeBox);

        ComboBox<Route> routeBox = new ComboBox<>();
        routeBox.getItems().addAll(monitoring.getRoutes());
        routeBox.setPromptText("Select Route");
        routeBox.setMaxWidth(350);
        routeBox.setPrefHeight(44);
        styleComboBox(routeBox);
        routeBox.setVisible(false);
        routeBox.setManaged(false);

        ComboBox<PublicVehicle> vehicleBox = new ComboBox<>();
        vehicleBox.getItems().addAll(monitoring.getAllVehicles());
        vehicleBox.setPromptText("Select Vehicle");
        vehicleBox.setMaxWidth(350);
        vehicleBox.setPrefHeight(44);
        styleComboBox(vehicleBox);
        vehicleBox.setVisible(false);
        vehicleBox.setManaged(false);

        TextField stopIdField = input("Stop ID");
        stopIdField.setVisible(false);
        stopIdField.setManaged(false);

        Label message = new Label();
        Button removeButton = primaryButton("Remove");

        removeTypeBox.setOnAction(e -> {
            String selected = removeTypeBox.getValue();

            routeBox.setVisible(false);
            routeBox.setManaged(false);
            vehicleBox.setVisible(false);
            vehicleBox.setManaged(false);
            stopIdField.setVisible(false);
            stopIdField.setManaged(false);
            message.setText("");

            if ("Route".equals(selected)) {
                routeBox.getItems().setAll(monitoring.getRoutes());
                routeBox.setPromptText("Select Route to Remove");
                routeBox.setVisible(true);
                routeBox.setManaged(true);
            } else if ("Stop".equals(selected)) {
                routeBox.getItems().setAll(monitoring.getRoutes());
                routeBox.setPromptText("Select Route of the Stop");
                routeBox.setVisible(true);
                routeBox.setManaged(true);
                stopIdField.setVisible(true);
                stopIdField.setManaged(true);
            } else if ("Vehicle".equals(selected)) {
                vehicleBox.getItems().setAll(monitoring.getAllVehicles());
                vehicleBox.setVisible(true);
                vehicleBox.setManaged(true);
            }
        });

        removeButton.setOnAction(e -> {
            String selected = removeTypeBox.getValue();

            if (selected == null) {
                showError(message, "Please select what to remove.");
                return;
            }

            if ("Route".equals(selected)) {
                Route route = routeBox.getValue();

                if (route == null) {
                    showError(message, "Please select a route.");
                    return;
                }

                if (monitoring.removeRoute(route.routeId())) {
                    DataStore.saveAll(auth, monitoring, savedUsers);
                    routeBox.getItems().setAll(monitoring.getRoutes());
                    showSuccess(message, "Route removed successfully.");
                } else {
                    showError(message, "Route not found.");
                }
            }

            else if ("Stop".equals(selected)) {
                Route route = routeBox.getValue();
                String stopId = stopIdField.getText().trim();

                if (route == null || stopId.isEmpty()) {
                    showError(message, "Please select a route and enter stop ID.");
                    return;
                }

                if (monitoring.removeStopFromRoute(route.routeId(), stopId)) {
                    DataStore.saveAll(auth, monitoring, savedUsers);
                    stopIdField.clear();
                    showSuccess(message, "Stop removed successfully.");
                } else {
                    showError(message, "Stop not found.");
                }
            }

            else if ("Vehicle".equals(selected)) {
                PublicVehicle vehicle = vehicleBox.getValue();

                if (vehicle == null) {
                    showError(message, "Please select a vehicle.");
                    return;
                }

                if (monitoring.removeVehicle(vehicle.vehicleId())) {
                    DataStore.saveAll(auth, monitoring, savedUsers);
                    vehicleBox.getItems().setAll(monitoring.getAllVehicles());
                    showSuccess(message, "Vehicle removed successfully.");
                } else {
                    showError(message, "Vehicle not found.");
                }
            }
        });

        contentArea.getChildren().add(card(title, removeTypeBox, routeBox, vehicleBox, stopIdField, removeButton, message));
    }

    private void showSendPingPanel() {
        contentArea.getChildren().clear();

        Label title = pageTitle("Send Vehicle Ping");

        ComboBox<PublicVehicle> vehicleBox = new ComboBox<>();
        vehicleBox.getItems().addAll(monitoring.getAllVehicles());
        vehicleBox.setPromptText("Select Vehicle");
        vehicleBox.setMaxWidth(350);
        vehicleBox.setPrefHeight(44);
        styleComboBox(vehicleBox);

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

        ComboBox<String> filterBox = new ComboBox<>();
        filterBox.getItems().addAll("ALL", "INFO", "WARN", "CRITICAL");
        filterBox.setValue("ALL");
        filterBox.setMaxWidth(250);
        filterBox.setPrefHeight(44);
        styleComboBox(filterBox);

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

        Button applyFilter = primaryButton("Apply Filter");
        applyFilter.setOnAction(e -> {
            String selected = filterBox.getValue();

            if ("ALL".equals(selected)) {
                table.setItems(FXCollections.observableArrayList(monitoring.getAlerts()));
            } else {
                table.setItems(FXCollections.observableArrayList(monitoring.getAlertsBySeverity(selected)));
            }
        });

        Button refresh = primaryButton("Refresh");
        refresh.setOnAction(e -> {
            filterBox.setValue("ALL");
            table.setItems(FXCollections.observableArrayList(monitoring.getAlerts()));
        });

        HBox controls = new HBox(12, filterBox, applyFilter, refresh);
        controls.setAlignment(Pos.CENTER_LEFT);

        VBox box = card(title, controls, table);
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
                        etaText = eta >= 0 ? Math.round(eta * 10.0) / 10.0 + " mins" : "N/A";
                        break;
                    }
                }
            }

            String speed = v.hasLocation() ? Math.round(v.speedKmh() * 10.0) / 10.0 + " km/h" : "N/A";
            String passengers = v.hasLocation() ? v.passengerCount() + "/" + v.capacity() : "N/A";

            return new DashboardRow(v.vehicleId(), v.getVehicleType(), routeText, speed, passengers, monitoring.getVehicleStatus(v), nearestStop, etaText);
        }).toList();
    }
    private void showClearAlertsPanel() {
        contentArea.getChildren().clear();

        Label title = pageTitle("Clear Alerts");

        Label warning = new Label(
                "This will remove all current alert records from the system.\n" +
                        "Use this only after reviewing the alerts."
        );
        warning.setStyle("-fx-text-fill: white; -fx-font-size: 15px;");

        Label message = new Label();

        Button clear = primaryButton("Clear All Alerts");

        clear.setOnAction(e -> {
            monitoring.clearAlerts();
            DataStore.saveAll(auth, monitoring, savedUsers);
            showSuccess(message, "All alerts cleared successfully.");
        });

        contentArea.getChildren().add(card(title, warning, clear, message));
    }

    private void showStatusOverviewPanel() {
        contentArea.getChildren().clear();

        Label title = pageTitle("Vehicle Status Overview");

        int total = monitoring.getAllVehicles().size();
        int normal = 0;
        int noData = 0;
        int idle = 0;
        int traffic = 0;
        int overspeed = 0;
        int overload = 0;

        for (PublicVehicle vehicle : monitoring.getAllVehicles()) {
            String status = monitoring.getVehicleStatus(vehicle);

            if (status.equals("NORMAL")) {
                normal++;
            } else if (status.equals("NO DATA")) {
                noData++;
            } else if (status.equals("IDLE")) {
                idle++;
            } else if (status.equals("SLOW/TRAFFIC")) {
                traffic++;
            } else if (status.equals("OVERSPEED")) {
                overspeed++;
            } else if (status.equals("FULL/OVERLOAD")) {
                overload++;
            }
        }

        HBox row1 = new HBox(15,
                statCard("Total Vehicles", String.valueOf(total)),
                statCard("Normal", String.valueOf(normal)),
                statCard("No Data", String.valueOf(noData))
        );

        HBox row2 = new HBox(15,
                statCard("Idle", String.valueOf(idle)),
                statCard("Traffic", String.valueOf(traffic)),
                statCard("Overspeed", String.valueOf(overspeed)),
                statCard("Overload", String.valueOf(overload))
        );

        Button refresh = primaryButton("Refresh Overview");
        refresh.setOnAction(e -> showStatusOverviewPanel());

        contentArea.getChildren().add(card(title, row1, row2, refresh));
    }

    private void showEtaTrackerPanel() {
        contentArea.getChildren().clear();

        Label title = pageTitle("ETA Tracker");

        ComboBox<Route> routeBox = new ComboBox<>();
        routeBox.getItems().addAll(monitoring.getRoutes());
        routeBox.setPromptText("Select Route");
        routeBox.setMaxWidth(350);
        routeBox.setPrefHeight(44);
        styleComboBox(routeBox);

        ComboBox<Stop> stopBox = new ComboBox<>();
        stopBox.setPromptText("Select Destination Stop");
        stopBox.setMaxWidth(350);
        stopBox.setPrefHeight(44);
        styleComboBox(stopBox);

        routeBox.setOnAction(e -> {
            Route selectedRoute = routeBox.getValue();
            stopBox.getItems().clear();

            if (selectedRoute != null) {
                stopBox.getItems().addAll(selectedRoute.stops());
            }
        });

        Button checkEta = primaryButton("Check ETA");

        TextArea output = new TextArea();
        output.setEditable(false);
        output.setWrapText(true);
        output.setStyle(textAreaStyle());

        checkEta.setOnAction(e -> {
            Route route = routeBox.getValue();
            Stop destination = stopBox.getValue();

            if (route == null || destination == null) {
                output.setText("Please select a route and destination stop.");
                return;
            }

            List<PublicVehicle> vehicles = monitoring.getVehiclesByRoute(route.routeId());

            if (vehicles.isEmpty()) {
                output.setText("No vehicles are currently assigned to this route.");
                return;
            }

            StringBuilder result = new StringBuilder();

            result.append("Route: ")
                    .append(route.routeId())
                    .append(" - ")
                    .append(route.routeName())
                    .append("\n");

            result.append("Destination Stop: ")
                    .append(destination.stopName())
                    .append("\n\n");

            boolean hasAvailableVehicle = false;

            for (PublicVehicle vehicle : vehicles) {
                if (!vehicle.hasLocation()) {
                    continue;
                }

                hasAvailableVehicle = true;

                double eta = monitoring.calculateEtaMinutes(vehicle, destination);
                Stop nearest = monitoring.getNearestStopOnRoute(vehicle, route);

                result.append("Vehicle ID: ").append(vehicle.vehicleId()).append("\n");
                result.append("Type: ").append(vehicle.getVehicleType()).append("\n");
                result.append("Plate: ").append(vehicle.plateNumber()).append("\n");
                result.append("Status: ").append(monitoring.getVehicleStatus(vehicle)).append("\n");
                result.append("Current Nearest Stop: ")
                        .append(nearest != null ? nearest.stopName() : "N/A")
                        .append("\n");
                result.append("Speed: ").append(Math.round(vehicle.speedKmh() * 10.0) / 10.0).append(" km/h\n");
                result.append("Passengers: ").append(vehicle.passengerCount()).append("/").append(vehicle.capacity()).append("\n");
                result.append("ETA to Destination: ")
                        .append(eta >= 0 ? Math.round(eta * 10.0) / 10.0 + " minutes" : "N/A")
                        .append("\n");
                result.append("--------------------------------\n");
            }

            if (!hasAvailableVehicle) {
                output.setText("Vehicles are assigned to this route, but no GPS data is available yet. Send ping or start live simulation first.");
                return;
            }

            output.setText(result.toString());
        });

        contentArea.getChildren().add(card(title, routeBox, stopBox, checkEta, output));
    }

    private void showSearchVehiclePanel() {
        contentArea.getChildren().clear();

        Label title = pageTitle("Search Vehicle");

        TextField vehicleId = input("Vehicle ID");
        Button search = primaryButton("Search");

        TextArea output = new TextArea();
        output.setEditable(false);
        output.setWrapText(true);
        output.setStyle(textAreaStyle());

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

    private void showSearchByRoutePanel() {
        contentArea.getChildren().clear();

        Label title = pageTitle("Search Vehicles by Route");

        ComboBox<Route> routeBox = new ComboBox<>();
        routeBox.getItems().addAll(monitoring.getRoutes());
        routeBox.setPromptText("Select Route");
        routeBox.setMaxWidth(350);
        routeBox.setPrefHeight(44);
        styleComboBox(routeBox);

        Button search = primaryButton("Search Route");

        TextArea output = new TextArea();
        output.setEditable(false);
        output.setWrapText(true);
        output.setStyle(textAreaStyle());

        search.setOnAction(e -> {
            Route route = routeBox.getValue();

            if (route == null) {
                output.setText("Please select a route.");
                return;
            }

            List<PublicVehicle> vehicles = monitoring.getVehiclesByRoute(route.routeId());

            if (vehicles.isEmpty()) {
                output.setText("No vehicles assigned to this route.");
                return;
            }

            StringBuilder result = new StringBuilder();

            result.append("Route: ").append(route.routeId()).append(" - ").append(route.routeName()).append("\n\n");

            for (PublicVehicle vehicle : vehicles) {
                Stop nearest = monitoring.getNearestStopOnRoute(vehicle, route);
                double eta = monitoring.calculateEtaMinutes(vehicle, nearest);

                result.append("Vehicle ID: ").append(vehicle.vehicleId()).append("\n");
                result.append("Type: ").append(vehicle.getVehicleType()).append("\n");
                result.append("Plate Number: ").append(vehicle.plateNumber()).append("\n");
                result.append("Status: ").append(monitoring.getVehicleStatus(vehicle)).append("\n");
                result.append("Speed: ").append(vehicle.hasLocation() ? vehicle.speedKmh() + " km/h" : "No data").append("\n");
                result.append("Passengers: ").append(vehicle.hasLocation() ? vehicle.passengerCount() + "/" + vehicle.capacity() : "No data").append("\n");
                result.append("Nearest Stop: ").append(nearest != null ? nearest.stopName() : "N/A").append("\n");
                result.append("ETA: ").append(eta >= 0 ? Math.round(eta * 10.0) / 10.0 + " minutes" : "N/A").append("\n");
                result.append("--------------------------------\n");
            }

            output.setText(result.toString());
        });

        contentArea.getChildren().add(card(title, routeBox, search, output));
    }

    private String getRouteStopsText() {
        StringBuilder sb = new StringBuilder();

        for (Route r : monitoring.getRoutes()) {
            sb.append(r.routeId()).append(" - ").append(r.routeName()).append("\n");

            for (Stop s : r.stops()) {
                sb.append("   ").append(s.stopId()).append(" - ").append(s.stopName()).append(" (").append(s.lat()).append(", ").append(s.lon()).append(")\n");
            }

            sb.append("\n");
        }

        return sb.length() == 0 ? "No routes available." : sb.toString();
    }

    private void showTextPanel(String title, String text) {
        contentArea.getChildren().clear();

        Label titleLabel = pageTitle(title);

        TextArea output = new TextArea(text);
        output.setEditable(false);
        output.setWrapText(false);
        output.setStyle(textAreaStyle());
        VBox.setVgrow(output, Priority.ALWAYS);

        VBox card = card(titleLabel, output);
        VBox.setVgrow(card, Priority.ALWAYS);

        contentArea.getChildren().add(card);
    }

    private void startLiveSimulation() {
        if (liveSimulation != null) {
            liveSimulation.stop();
        }

        liveSimulation = new Timeline(new KeyFrame(Duration.seconds(3), e -> simulateVehiclePing()));
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

        if (vehicles.isEmpty()) return;

        for (PublicVehicle vehicle : vehicles) {
            double baseLat = vehicle.hasLocation() ? vehicle.lat() : 10.3270;
            double baseLon = vehicle.hasLocation() ? vehicle.lon() : 123.9063;

            double newLat = baseLat + ((random.nextDouble() - 0.5) * 0.002);
            double newLon = baseLon + ((random.nextDouble() - 0.5) * 0.002);

            double speed = 10 + random.nextDouble() * 50;
            int passengers = random.nextInt(vehicle.capacity() + 6);

            VehiclePing ping = new VehiclePing(vehicle.vehicleId(), Instant.now(), newLat, newLon, speed, passengers);
            monitoring.receivePing(ping);
        }

        DataStore.saveAll(auth, monitoring, savedUsers);
    }

    private void ensureDefaultCebuRoutesAndVehicles() {
        if (!monitoring.routeExists("R-04L")) {
            Route r04l = new Route("R-04L", "IT Park - Ayala - Fuente - Colon");
            r04l.addStop(new Stop("S-001", "IT Park", 10.3270, 123.9063));
            r04l.addStop(new Stop("S-002", "Ayala Center Cebu", 10.3187, 123.9056));
            r04l.addStop(new Stop("S-003", "Fuente Osmeña", 10.3090, 123.8929));
            r04l.addStop(new Stop("S-004", "Colon", 10.2965, 123.8988));
            monitoring.addRoute(r04l);
        }

        if (!monitoring.routeExists("R-17B")) {
            Route r17b = new Route("R-17B", "CIT-U - Colon - Carbon");
            r17b.addStop(new Stop("S-101", "CIT-U", 10.2949, 123.8816));
            r17b.addStop(new Stop("S-102", "Pardo", 10.2837, 123.8679));
            r17b.addStop(new Stop("S-103", "Colon", 10.2965, 123.8988));
            r17b.addStop(new Stop("S-104", "Carbon Market", 10.2943, 123.9017));
            monitoring.addRoute(r17b);
        }

        if (!monitoring.routeExists("R-13C")) {
            Route r13c = new Route("R-13C", "Talamban - Banilad - Ayala");
            r13c.addStop(new Stop("S-201", "Talamban", 10.3697, 123.9141));
            r13c.addStop(new Stop("S-202", "Banilad", 10.3435, 123.9110));
            r13c.addStop(new Stop("S-203", "Country Mall", 10.3374, 123.9114));
            r13c.addStop(new Stop("S-204", "Ayala Center Cebu", 10.3187, 123.9056));
            monitoring.addRoute(r13c);
        }

        if (!monitoring.routeExists("R-12L")) {
            Route r12l = new Route("R-12L", "Labangon - Capitol - Ayala");
            r12l.addStop(new Stop("S-301", "Labangon", 10.2994, 123.8755));
            r12l.addStop(new Stop("S-302", "Capitol", 10.3140, 123.8919));
            r12l.addStop(new Stop("S-303", "Fuente Osmeña", 10.3090, 123.8929));
            r12l.addStop(new Stop("S-304", "Ayala Center Cebu", 10.3187, 123.9056));
            monitoring.addRoute(r12l);
        }

        if (!monitoring.vehicleExists("V-001")) {
            monitoring.registerVehicle(new Jeepney("V-001", "ABC-123", 20));
            monitoring.assignVehicleToRoute("V-001", "R-04L");
        }

        if (!monitoring.vehicleExists("V-002")) {
            monitoring.registerVehicle(new ModernJeep("V-002", "XYZ-456", 30));
            monitoring.assignVehicleToRoute("V-002", "R-04L");
        }

        if (!monitoring.vehicleExists("V-003")) {
            monitoring.registerVehicle(new Jeepney("V-003", "CIT-789", 20));
            monitoring.assignVehicleToRoute("V-003", "R-17B");
        }

        if (!monitoring.vehicleExists("V-004")) {
            monitoring.registerVehicle(new Bus("V-004", "BUS-321", 45));
            monitoring.assignVehicleToRoute("V-004", "R-13C");
        }

        if (!monitoring.vehicleExists("V-005")) {
            monitoring.registerVehicle(new Jeepney("V-005", "LAB-555", 20));
            monitoring.assignVehicleToRoute("V-005", "R-12L");
        }
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
            label.setStyle("-fx-font-size: 26px; -fx-font-weight: bold; -fx-text-fill: white;");
        } else {
            label.setStyle("-fx-font-size: 26px; -fx-font-weight: bold; -fx-text-fill: #0f172a;");
        }

        return label;
    }

    private TextField input(String prompt) {
        TextField field = new TextField();
        field.setPromptText(prompt);
        field.setMaxWidth(350);
        field.setPrefHeight(44);
        field.setStyle(inputStyle());
        return field;
    }

    private Button primaryButton(String text) {
        Button button = new Button(text);
        button.setPrefHeight(43);
        button.setMinWidth(180);
        button.setStyle("-fx-background-color: linear-gradient(to right, #2563eb, #7c3aed);" +
                "-fx-text-fill: white;" +
                "-fx-font-weight: bold;" +
                "-fx-background-radius: 14;" +
                "-fx-cursor: hand;");
        return button;
    }

    private Button secondaryButton(String text) {
        Button button = new Button(text);
        button.setPrefHeight(43);
        button.setMinWidth(180);
        button.setStyle("-fx-background-color: #334155;" +
                "-fx-text-fill: #e2e8f0;" +
                "-fx-font-weight: bold;" +
                "-fx-background-radius: 14;" +
                "-fx-cursor: hand;");
        return button;
    }

    private void styleTable(TableView<?> table) {
        table.setStyle(
                "-fx-background-color: white;" +
                        "-fx-control-inner-background: white;" +
                        "-fx-table-cell-border-color: #d1d5db;" +
                        "-fx-border-color: #cbd5e1;" +
                        "-fx-border-radius: 12;" +
                        "-fx-background-radius: 12;"
        );

        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        table.lookupAll(".column-header").forEach(header ->
                header.setStyle(
                        "-fx-background-color: #f8fafc;" +
                                "-fx-border-color: #d1d5db;"
                )
        );

        table.lookupAll(".column-header .label").forEach(label ->
                label.setStyle(
                        "-fx-text-fill: black;" +
                                "-fx-font-weight: bold;" +
                                "-fx-font-size: 14px;"
                )
        );

        table.lookupAll(".table-row-cell").forEach(row ->
                row.setStyle(
                        "-fx-background-color: white;" +
                                "-fx-text-fill: black;"
                )
        );
    }

    private String inputStyle() {
        if (darkMode) {
            return
                    "-fx-background-color: #1e293b;" +
                            "-fx-text-fill: white;" +
                            "-fx-prompt-text-fill: #94a3b8;" +
                            "-fx-background-radius: 14;" +
                            "-fx-border-color: #334155;" +
                            "-fx-border-radius: 14;" +
                            "-fx-font-size: 13px;" +
                            "-fx-font-weight: bold;";
        } else {
            return
                    "-fx-background-color: white;" +
                            "-fx-text-fill: #0f172a;" +
                            "-fx-prompt-text-fill: #64748b;" +
                            "-fx-background-radius: 14;" +
                            "-fx-border-color: #cbd5e1;" +
                            "-fx-border-radius: 14;" +
                            "-fx-font-size: 13px;" +
                            "-fx-font-weight: bold;";
        }
    }

    private <T> void styleComboBox(ComboBox<T> comboBox) {
        comboBox.setStyle(inputStyle());

        comboBox.setButtonCell(new ListCell<T>() {
            @Override
            protected void updateItem(T item, boolean empty) {
                super.updateItem(item, empty);

                if (empty || item == null) {
                    setText(comboBox.getPromptText());
                } else {
                    setText(item.toString());
                }

                if (darkMode) {
                    setStyle("-fx-text-fill: white; -fx-background-color: #1e293b; -fx-font-weight: bold;");
                } else {
                    setStyle("-fx-text-fill: #0f172a; -fx-background-color: white; -fx-font-weight: bold;");
                }
            }
        });

        comboBox.setCellFactory(listView -> new ListCell<T>() {
            @Override
            protected void updateItem(T item, boolean empty) {
                super.updateItem(item, empty);

                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item.toString());
                }

                if (darkMode) {
                    setStyle("-fx-text-fill: white; -fx-background-color: #1e293b;");
                } else {
                    setStyle("-fx-text-fill: #0f172a; -fx-background-color: white;");
                }
            }
        });
    }

    private String cardStyle() {
        if (darkMode) {
            return "-fx-background-color: rgba(15, 23, 42, 0.94);" +
                    "-fx-background-radius: 24;" +
                    "-fx-border-color: rgba(148, 163, 184, 0.28);" +
                    "-fx-border-radius: 24;" +
                    "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.45), 30, 0, 0, 10);";
        }

        return "-fx-background-color: white; -fx-background-radius: 22; -fx-effect: dropshadow(gaussian, rgba(15,23,42,0.15), 24, 0, 0, 7);";
    }

    private String sidebarButtonStyle() {
        if (darkMode) {
            return
                    "-fx-background-color: #0f172a;" +
                            "-fx-text-fill: #f8fafc;" +
                            "-fx-font-weight: bold;" +
                            "-fx-font-size: 13px;" +
                            "-fx-background-radius: 14;" +
                            "-fx-border-color: #334155;" +
                            "-fx-border-radius: 14;" +
                            "-fx-cursor: hand;";
        } else {
            return
                    "-fx-background-color: white;" +
                            "-fx-text-fill: #0f172a;" +
                            "-fx-font-weight: bold;" +
                            "-fx-font-size: 13px;" +
                            "-fx-background-radius: 14;" +
                            "-fx-border-color: #cbd5e1;" +
                            "-fx-border-radius: 14;" +
                            "-fx-cursor: hand;";
        }
    }

    private String textAreaStyle() {
        if (darkMode) {
            return "-fx-control-inner-background: #0f172a;" +
                    "-fx-background-color: #0f172a;" +
                    "-fx-text-fill: white;" +
                    "-fx-prompt-text-fill: #94a3b8;" +
                    "-fx-border-color: #334155;" +
                    "-fx-border-radius: 12;" +
                    "-fx-background-radius: 12;" +
                    "-fx-font-size: 14px;";
        }

        return "-fx-background-radius: 12; -fx-font-size: 14px;";
    }

    private String darkRootStyle() {
        return "-fx-background-color: radial-gradient(center 50% 15%, radius 80%, #1e3a8a, transparent), linear-gradient(to bottom right, #020617, #0f172a, #1e293b);";
    }

    private void hideNodes(javafx.scene.Node... nodes) {
        for (javafx.scene.Node node : nodes) {
            node.setVisible(false);
            node.setManaged(false);
        }
    }

    private void showNodes(javafx.scene.Node... nodes) {
        for (javafx.scene.Node node : nodes) {
            node.setVisible(true);
            node.setManaged(true);
        }
    }

    private void showSuccess(Label label, String text) {
        label.setStyle("-fx-text-fill: #22c55e; -fx-font-weight: bold;");
        label.setText(text);
    }

    private void showError(Label label, String text) {
        label.setStyle("-fx-text-fill: #f87171; -fx-font-weight: bold;");
        label.setText(text);
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