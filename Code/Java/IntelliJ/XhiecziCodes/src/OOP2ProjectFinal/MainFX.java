package OOP2ProjectFinal;

import javafx.animation.FadeTransition;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

import javafx.stage.Stage;
import javafx.util.Duration;
import javafx.scene.image.Image;
import java.util.Objects;

import java.io.InputStream;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.shape.Line;

import javafx.scene.web.WebView;
import javafx.animation.Timeline;
import javafx.animation.KeyFrame;
import javafx.util.Duration;

public class MainFX extends Application {

    // Modern Brand Colors
    private static final String GREEN_PRIMARY = "#00C853";
    private static final String GREEN_DARK = "#009624";
    private static final String DARK_BG = "#121212";
    private static final String DARKER_BG = "#0A0A0A";
    private static final String CARD_BG = "#1E1E1E";
    private static final String CARD_HOVER = "#2A2A2A";
    private static final String BORDER_COLOR = "#2C2C2C";
    private static final String TEXT_PRIMARY = "#FFFFFF";
    private static final String TEXT_SECONDARY = "#B0B0B0";
    private static final String TEXT_MUTED = "#6B6B6B";
    private static final String ACCENT_RED = "#FF5252";
    private static final String ACCENT_ORANGE = "#FF9800";
    private static final String ACCENT_BLUE = "#448AFF";

    // State
    private final List<DataStore.SavedUser> savedUsers = new ArrayList<>();
    private final List<String> activityLogs = new ArrayList<>();
    private final AuthenticationService auth = new AuthenticationService();
    private final MonitoringService monitoring = new MonitoringService();
    private final Random random = new Random();
    private final DateTimeFormatter timeFormatter =
            DateTimeFormatter.ofPattern("hh:mm:ss a, MMM dd").withZone(ZoneId.systemDefault());

    private User currentUser;
    private Timeline liveSimulation;
    private Timeline mapAutoRefresh;
    private Stage mainStage;
    private Canvas liveMapCanvas;
    private Image cebuMapImage;
    private ComboBox<String> currentMapRouteFilter;
    private boolean isRealGPSMode = false;

    private BorderPane dashboardRoot;
    private VBox sidebar;
    private VBox contentArea;
    private javafx.scene.control.Label headerTitle;

    private final Map<String, Integer> vehicleRouteIndexes = new HashMap<>();
    private final Map<String, List<double[]>> routePaths = new HashMap<>();

    // GPS Bridge
    public class GPSBridge {
    public void receiveGPS(double lat, double lng, double accuracy, double speed) {
        Platform.runLater(() -> {
            if (currentUser instanceof Operator && isRealGPSMode) {
                PublicVehicle showcase = monitoring.findVehicleById("V-SHOWCASE");
                if (showcase != null) {
                    VehiclePing ping = new VehiclePing(
                        "V-SHOWCASE", 
                        Instant.now(), 
                        lat, lng, 
                        speed > 5 ? speed : 18 + random.nextInt(25), 
                        8 + random.nextInt(15)
                    );
                    monitoring.receivePing(ping);
                    addActivityLog("📍 Real GPS: " + String.format("%.5f, %.5f", lat, lng));
                    refreshLiveMap();
                }
            }
        });
    }
}

    @Override
    public void start(Stage stage) {
        mainStage = stage;
        initializeRoutePaths();

        if (DataStore.dataExists()) {
            DataStore.loadAll(auth, monitoring, savedUsers);
            ensureDefaultAccounts();
            DataStore.saveAll(auth, monitoring, savedUsers);
        } else {
            seedData();
            DataStore.saveAll(auth, monitoring, savedUsers);
        }
        
        ensureDefaultCebuRoutesAndVehicles();
        addShowcaseVehicle();
        DataStore.saveAll(auth, monitoring, savedUsers);
        showLoginScreen();
    }

    private void initializeRoutePaths() {
        List<double[]> r04l = new ArrayList<>();
        r04l.add(new double[]{10.3285, 123.9068});
        r04l.add(new double[]{10.3282, 123.9071});
        r04l.add(new double[]{10.3278, 123.9078});
        r04l.add(new double[]{10.3269, 123.9090});
        r04l.add(new double[]{10.3255, 123.9108});
        r04l.add(new double[]{10.3241, 123.9124});
        r04l.add(new double[]{10.3227, 123.9145});
        r04l.add(new double[]{10.3215, 123.9112});
        r04l.add(new double[]{10.3198, 123.9065});
        r04l.add(new double[]{10.3180, 123.9000});
        r04l.add(new double[]{10.3157, 123.8937});
        r04l.add(new double[]{10.3122, 123.8955});
        r04l.add(new double[]{10.3088, 123.8972});
        r04l.add(new double[]{10.3022, 123.8994});
        r04l.add(new double[]{10.2954, 123.9019});
        routePaths.put("R-04L", r04l);

        List<double[]> r17b = new ArrayList<>();
        r17b.add(new double[]{10.2949, 123.8816});
        r17b.add(new double[]{10.2935, 123.8842});
        r17b.add(new double[]{10.2918, 123.8880});
        r17b.add(new double[]{10.2940, 123.8925});
        r17b.add(new double[]{10.2965, 123.8988});
        r17b.add(new double[]{10.2955, 123.9005});
        r17b.add(new double[]{10.2943, 123.9017});
        routePaths.put("R-17B", r17b);

        List<double[]> r13c = new ArrayList<>();
        r13c.add(new double[]{10.3697, 123.9141});
        r13c.add(new double[]{10.3632, 123.9132});
        r13c.add(new double[]{10.3568, 123.9125});
        r13c.add(new double[]{10.3435, 123.9110});
        r13c.add(new double[]{10.3374, 123.9114});
        r13c.add(new double[]{10.3280, 123.9095});
        r13c.add(new double[]{10.3187, 123.9056});
        routePaths.put("R-13C", r13c);

        List<double[]> r12l = new ArrayList<>();
        r12l.add(new double[]{10.2994, 123.8755});
        r12l.add(new double[]{10.3035, 123.8802});
        r12l.add(new double[]{10.3080, 123.8865});
        r12l.add(new double[]{10.3140, 123.8919});
        r12l.add(new double[]{10.3157, 123.8937});
        r12l.add(new double[]{10.3187, 123.9056});
        routePaths.put("R-12L", r12l);
    }

    private void addShowcaseVehicle() {
    if (!monitoring.vehicleExists("V-SHOWCASE")) {
        PublicVehicle showcase = new ModernJeep("V-SHOWCASE", "SAKAY-001", 25);
        monitoring.registerVehicle(showcase);
        monitoring.assignVehicleToRoute("V-SHOWCASE", "R-04L");
        
        // Your requested location
        monitoring.receivePing(new VehiclePing(
            "V-SHOWCASE", 
            Instant.now(), 
            10.32925, 123.90744,  // IT Park - Qualfon area
            15, 12
        ));
    }
}

    private void ensureDefaultAccounts() {
        if (!auth.usernameExists("operator")) {
            auth.addUser(new Operator("U-001", "John Dela Cruz", "operator", "1234"));
            savedUsers.add(new DataStore.SavedUser("U-001", "John Dela Cruz", "operator", "1234", "Operator"));
        }
        if (!auth.usernameExists("commuter")) {
            auth.addUser(new Commuter("U-002", "Maria Santos", "commuter", "1234"));
            savedUsers.add(new DataStore.SavedUser("U-002", "Maria Santos", "commuter", "1234", "Commuter"));
        }
    }

    private void showLoginScreen() {
        ImageView logoView = loadLogo();

        Label appName = new Label("SAKAY CEBU");
        appName.setFont(Font.font("System", FontWeight.BOLD, 32));
        appName.setStyle("-fx-text-fill: " + GREEN_PRIMARY + ";");
        
        Label tagline = new Label("Real-time Public Vehicle Monitoring");
        tagline.setStyle("-fx-font-size: 14px; -fx-text-fill: " + TEXT_SECONDARY + ";");

        TextField usernameField = styledInput("Username", 340);
        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("Password");
        passwordField.setMaxWidth(340);
        passwordField.setPrefHeight(48);
        passwordField.setStyle(inputCss());

        Label errorLabel = new Label();
        errorLabel.setStyle("-fx-text-fill: " + ACCENT_RED + "; -fx-font-size: 13px;");

        ToggleButton gpsModeToggle = new ToggleButton("📍 Use Device GPS (Operator)");
        gpsModeToggle.setStyle("-fx-background-color: " + CARD_BG + "; -fx-text-fill: " + TEXT_SECONDARY + "; -fx-background-radius: 20; -fx-padding: 8 16;");
        gpsModeToggle.setVisible(false);
        gpsModeToggle.setMaxWidth(340);

        Button loginBtn = modernButton("Log In", GREEN_PRIMARY, 340);
        Button registerBtn = outlineButton("Create Account", 340);

        loginBtn.setOnAction(e -> {
            User user = auth.login(usernameField.getText().trim(), passwordField.getText().trim());
            if (user == null) {
                errorLabel.setText("Invalid username or password.");
                return;
            }
            currentUser = user;
            if (user instanceof Operator) {
                gpsModeToggle.setVisible(true);
                showOperatorDashboard();
            } else {
                showCommuterDashboard();
            }
        });
        
        gpsModeToggle.setOnAction(e -> {
            isRealGPSMode = gpsModeToggle.isSelected();
            addActivityLog(isRealGPSMode ? "Real GPS tracking enabled" : "Real GPS tracking disabled");
            if (liveMapCanvas != null) refreshLiveMap();
        });

        registerBtn.setOnAction(e -> showRegisterScreen());
        passwordField.setOnAction(e -> loginBtn.fire());

        VBox card = new VBox(16, logoView, appName, tagline, separator(), usernameField, passwordField,
                loginBtn, registerBtn, gpsModeToggle, errorLabel);
        card.setAlignment(Pos.CENTER);
        card.setPadding(new Insets(48, 56, 48, 56));
        card.setMaxWidth(480);
        card.setStyle(cardStyle());

        StackPane root = new StackPane(card);
        root.setStyle("-fx-background-color: radial-gradient(center 50% 30%, radius 80%, #1a2a1a, " + DARKER_BG + ");");
        fadeIn(card);

        mainStage.setTitle("Sakay Cebu - Vehicle Monitoring System");
        mainStage.setScene(new Scene(root, 1200, 760));
        mainStage.show();
    }

    private void showRegisterScreen() {
        Label title = new Label("Create Account");
        title.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: " + TEXT_PRIMARY + ";");

        TextField userIdField = styledInput("User ID", 360);
        TextField nameField = styledInput("Full Name", 360);
        TextField usernameField = styledInput("Username", 360);

        PasswordField passField = pwField("Password");
        PasswordField confirmField = pwField("Confirm Password");

        ComboBox<String> roleBox = new ComboBox<>();
        roleBox.getItems().addAll("Commuter", "Operator");
        roleBox.setPromptText("Select Role");
        roleBox.setMaxWidth(360);
        roleBox.setPrefHeight(48);
        styleComboBox(roleBox);

        Label msg = new Label();
        Button createBtn = modernButton("Create Account", GREEN_PRIMARY, 360);
        Button backBtn = outlineButton("Back to Login", 360);

        createBtn.setOnAction(e -> {
            String uid = userIdField.getText().trim();
            String name = nameField.getText().trim();
            String user = usernameField.getText().trim();
            String pass = passField.getText().trim();
            String conf = confirmField.getText().trim();
            String role = roleBox.getValue();

            if (uid.isEmpty() || name.isEmpty() || user.isEmpty() || pass.isEmpty() || role == null) {
                showError(msg, "Please fill in all fields.");
                return;
            }
            if (auth.userIdExists(uid)) { showError(msg, "User ID already exists."); return; }
            if (auth.usernameExists(user)) { showError(msg, "Username already taken."); return; }
            if (pass.length() < 4) { showError(msg, "Password must be at least 4 characters."); return; }
            if (!pass.equals(conf)) { showError(msg, "Passwords do not match."); return; }

            User newUser = role.equals("Operator") ? new Operator(uid, name, user, pass) : new Commuter(uid, name, user, pass);
            auth.addUser(newUser);
            savedUsers.add(new DataStore.SavedUser(uid, name, user, pass, role));
            DataStore.saveAll(auth, monitoring, savedUsers);
            showSuccess(msg, "Account created! You can now log in.");
        });

        backBtn.setOnAction(e -> showLoginScreen());

        VBox card = new VBox(12, title, separator(), userIdField, nameField, usernameField,
                passField, confirmField, roleBox, createBtn, backBtn, msg);
        card.setAlignment(Pos.CENTER);
        card.setPadding(new Insets(40, 48, 40, 48));
        card.setMaxWidth(460);
        card.setStyle(cardStyle());

        StackPane root = new StackPane(card);
        root.setStyle("-fx-background-color: radial-gradient(center 50% 30%, radius 80%, #1a2a1a, " + DARKER_BG + ");");
        fadeIn(card);
        mainStage.setScene(new Scene(root, 1200, 760));
    }

    private void showOperatorDashboard() {
        buildDashboard("Operator Dashboard", currentUser.name());

        sidebarSection("MAIN");
        sidebarBtn("🏠 Home", this::showWelcomePanel);
        sidebarBtn("📊 Route Dashboard", this::showModernRouteDashboard);
        sidebarBtn("🗺 Live Map", this::showLiveMapPanel);
        sidebarBtn("📍 GPS Tracking Mode", this::showGPSTrackingPanel);

        sidebarSection("MANAGEMENT");
        sidebarBtn("➕ Add Data", this::showAddPanel);
        sidebarBtn("🚐 Vehicles", this::showVehiclesTable);
        sidebarBtn("🛣 Assign Vehicle", this::showAssignVehiclePanel);
        sidebarBtn("🗑 Remove Data", this::showRemovePanel);

        sidebarSection("MONITORING");
        sidebarBtn("📡 Send Ping", this::showSendPingPanel);
        sidebarBtn("▶ Start Simulation", this::startLiveSimulation);
        sidebarBtn("■ Stop Simulation", this::stopLiveSimulation);
        sidebarBtn("🚨 Alerts", this::showAlertsTable);
        sidebarBtn("🔔 Notifications", this::showNotificationCenterPanel);
        sidebarBtn("🧾 Activity Log", this::showActivityLogPanel);

        sidebarSection("ACCOUNT");
        sidebarBtn("👤 Profile", this::showModernProfilePanel);
        sidebarBtn("⚙ Settings", this::showSettingsPanel);
        sidebarBtn("ℹ About", this::showAboutPanel);
        sidebarBtn("❔ Help", this::showHelpPanel);
        addLogoutButton();

        showWelcomePanel();
    }

    private void showCommuterDashboard() {
        buildDashboard("Commuter Dashboard", currentUser.name());

        sidebarSection("MAIN");
        sidebarBtn("🏠 Home", this::showWelcomePanel);
        sidebarBtn("📊 Route Status", this::showModernRouteDashboard);
        sidebarBtn("🗺 Live Map", this::showLiveMapPanel);
        sidebarBtn("⏱ ETA Tracker", this::showEtaTrackerPanel);

        sidebarSection("ROUTES");
        sidebarBtn("🔎 Search Route", this::showModernSearchByRoute);
        sidebarBtn("📌 Route Stops", () -> showTextPanel("Route Stops", getRouteStopsText()));
        sidebarBtn("🚨 Recent Alerts", this::showAlertsTable);
        sidebarBtn("🔔 Notifications", this::showNotificationCenterPanel);

        sidebarSection("ACCOUNT");
        sidebarBtn("👤 Profile", this::showModernProfilePanel);
        sidebarBtn("⚙ Settings", this::showSettingsPanel);
        sidebarBtn("ℹ About", this::showAboutPanel);
        sidebarBtn("❔ Help", this::showHelpPanel);
        addLogoutButton();

        showWelcomePanel();
    }

    private void buildDashboard(String roleLabel, String username) {
        dashboardRoot = new BorderPane();
        dashboardRoot.setStyle("-fx-background-color: " + DARKER_BG + ";");

        Label logo = new Label("SAKAY CEBU");
        logo.setFont(Font.font("System", FontWeight.BOLD, 18));
        logo.setStyle("-fx-text-fill: " + GREEN_PRIMARY + ";");
        
        Label roleBadge = new Label(roleLabel.toUpperCase());
        roleBadge.setStyle("-fx-background-color: " + GREEN_PRIMARY + "20; -fx-text-fill: " + GREEN_PRIMARY + "; -fx-font-size: 10px; -fx-font-weight: bold; -fx-padding: 4 12; -fx-background-radius: 20;");
        
        headerTitle = new Label(username);
        headerTitle.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: " + TEXT_PRIMARY + ";");
        
        Label statusDot = new Label("●");
        statusDot.setStyle("-fx-text-fill: " + GREEN_PRIMARY + "; -fx-font-size: 10px;");
        Label statusText = new Label("Connected");
        statusText.setStyle("-fx-text-fill: " + TEXT_SECONDARY + "; -fx-font-size: 11px;");
        HBox statusBox = new HBox(4, statusDot, statusText);
        
        Circle avatar = new Circle(20, Color.web(GREEN_PRIMARY));
        Label avatarInit = new Label(username.substring(0, 1).toUpperCase());
        avatarInit.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: white;");
        StackPane avatarPane = new StackPane(avatar, avatarInit);

        javafx.scene.layout.Region spacer = new javafx.scene.layout.Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        HBox headerContent = new HBox(16, logo, roleBadge, spacer, headerTitle, statusBox, avatarPane);
        headerContent.setAlignment(Pos.CENTER_LEFT);

        VBox headerBox = new VBox(headerContent);
        headerBox.setPadding(new Insets(16, 24, 16, 24));
        headerBox.setStyle("-fx-background-color: " + DARK_BG + "; -fx-border-color: " + BORDER_COLOR + "; -fx-border-width: 0 0 1 0;");
        dashboardRoot.setTop(headerBox);

        sidebar = new VBox(6);
        sidebar.setPadding(new Insets(20, 12, 20, 12));
        sidebar.setPrefWidth(240);
        sidebar.setStyle("-fx-background-color: " + DARK_BG + "; -fx-border-color: " + BORDER_COLOR + "; -fx-border-width: 0 1 0 0;");
        
        ScrollPane sidebarScroll = new ScrollPane(sidebar);
        sidebarScroll.setFitToWidth(true);
        sidebarScroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        sidebarScroll.setStyle("-fx-background: transparent; -fx-background-color: transparent;");
        dashboardRoot.setLeft(sidebarScroll);

        contentArea = new VBox(20);
        contentArea.setPadding(new Insets(24));
        contentArea.setStyle("-fx-background-color: " + DARKER_BG + ";");
        
        ScrollPane contentScroll = new ScrollPane(contentArea);
        contentScroll.setFitToWidth(true);
        contentScroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        contentScroll.setStyle("-fx-background: transparent; -fx-background-color: transparent;");
        dashboardRoot.setCenter(contentScroll);

        mainStage.setScene(new Scene(dashboardRoot, 1280, 760));
    }

    private void showWelcomePanel() {
        contentArea.getChildren().clear();

        Label greeting = new Label("Good " + getTimeOfDay() + ", " + currentUser.name() + " 👋");
        greeting.setFont(Font.font("System", FontWeight.BOLD, 28));
        greeting.setStyle("-fx-text-fill: " + TEXT_PRIMARY + ";");

        Label sub = new Label("Here's your real-time transport snapshot for Cebu City.");
        sub.setStyle("-fx-font-size: 14px; -fx-text-fill: " + TEXT_SECONDARY + ";");

        Label simStatus = new Label((liveSimulation != null ? "●" : "○") + " Simulation " + (liveSimulation != null ? "ACTIVE" : "INACTIVE"));
        simStatus.setStyle("-fx-font-size: 12px; -fx-text-fill: " + (liveSimulation != null ? GREEN_PRIMARY : TEXT_MUTED) + "; -fx-font-weight: bold;");

        HBox stats = new HBox(20,
                statCard("🛣 Routes", String.valueOf(monitoring.getRoutes().size())),
                statCard("🚐 Vehicles", String.valueOf(monitoring.getAllVehicles().size())),
                statCard("📍 Active GPS", String.valueOf(monitoring.getAllVehicles().stream().filter(PublicVehicle::hasLocation).count())),
                statCard("🚨 Alerts", String.valueOf(monitoring.getAlerts().size()))
        );

        VBox top = createModernCard(new VBox(8, greeting, sub, simStatus), stats);
        contentArea.getChildren().add(top);
    }

    private VBox statCard(String label, String value) {
        VBox box = new VBox(8);
        box.setAlignment(Pos.CENTER);
        box.setPadding(new Insets(20));
        box.setMinWidth(140);
        box.setStyle("-fx-background-color: " + CARD_BG + "; -fx-background-radius: 16; -fx-border-color: " + BORDER_COLOR + "; -fx-border-radius: 16;");
        
        Label valueLabel = new Label(value);
        valueLabel.setFont(Font.font("System", FontWeight.BOLD, 28));
        valueLabel.setStyle("-fx-text-fill: " + GREEN_PRIMARY + ";");
        
        Label labelText = new Label(label);
        labelText.setStyle("-fx-text-fill: " + TEXT_SECONDARY + "; -fx-font-size: 12px;");
        
        box.getChildren().addAll(valueLabel, labelText);
        return box;
    }

    private String getTimeOfDay() {
        int hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
        if (hour < 12) return "morning";
        if (hour < 18) return "afternoon";
        return "evening";
    }

    private void showModernRouteDashboard() {
        contentArea.getChildren().clear();

        Label title = new Label("Route Dashboard");
        title.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: " + TEXT_PRIMARY + ";");
        
        ComboBox<String> routeFilter = new ComboBox<>();
        routeFilter.getItems().add("All Routes");
        monitoring.getRoutes().forEach(r -> routeFilter.getItems().add(r.routeId() + " – " + r.routeName()));
        routeFilter.setValue("All Routes");
        routeFilter.setMaxWidth(280);
        routeFilter.setPrefHeight(40);
        styleComboBox(routeFilter);

        Button refreshBtn = modernButton("Refresh", GREEN_PRIMARY, 140);
        FlowPane cardsContainer = new FlowPane();
        cardsContainer.setHgap(16);
        cardsContainer.setVgap(16);
        cardsContainer.setPadding(new Insets(16, 0, 16, 0));

        Runnable loadCards = () -> {
            cardsContainer.getChildren().clear();
            String filter = routeFilter.getValue();
            String filterRouteId = "All Routes".equals(filter) ? null : filter.split("–")[0].trim();
            
            for (PublicVehicle v : monitoring.getAllVehicles()) {
                if (filterRouteId != null && !filterRouteId.equalsIgnoreCase(v.routeId())) continue;
                if (v.routeId() == null && filterRouteId != null) continue;
                cardsContainer.getChildren().add(createVehicleCard(v));
            }
        };

        refreshBtn.setOnAction(e -> loadCards.run());
        routeFilter.setOnAction(e -> loadCards.run());
        loadCards.run();

        ScrollPane scrollPane = new ScrollPane(cardsContainer);
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background: transparent; -fx-background-color: transparent;");

        VBox headerBox = new VBox(8, title, new HBox(12, routeFilter, refreshBtn));
        VBox card = createModernCard(headerBox, scrollPane);
        VBox.setVgrow(scrollPane, Priority.ALWAYS);
        contentArea.getChildren().add(card);
    }

    private VBox createVehicleCard(PublicVehicle v) {
        VBox card = new VBox(12);
        card.setPadding(new Insets(18));
        card.setPrefWidth(280);
        card.setStyle("-fx-background-color: " + CARD_BG + "; -fx-background-radius: 16; -fx-border-color: " + BORDER_COLOR + "; -fx-border-radius: 16;");
        
        String vehicleIcon = v instanceof Bus ? "🚌" : (v instanceof ModernJeep ? "🚎" : "🚐");
        HBox header = new HBox(10);
        Label iconLabel = new Label(vehicleIcon);
        iconLabel.setStyle("-fx-font-size: 28px;");
        
        VBox infoBox = new VBox(2);
        Label idLabel = new Label(v.vehicleId());
        idLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: " + TEXT_PRIMARY + ";");
        Label typeLabel = new Label(v.getVehicleType());
        typeLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: " + GREEN_PRIMARY + ";");
        infoBox.getChildren().addAll(idLabel, typeLabel);
        
        javafx.scene.layout.Region spacer = new javafx.scene.layout.Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        
        String status = monitoring.getVehicleStatus(v);
        String statusColor = status.equals("NORMAL") ? GREEN_PRIMARY : (status.equals("OVERSPEED") || status.equals("FULL/OVERLOAD") ? ACCENT_RED : ACCENT_ORANGE);
        Label statusBadge = new Label("● " + status);
        statusBadge.setStyle("-fx-text-fill: " + statusColor + "; -fx-font-size: 11px; -fx-font-weight: bold;");
        
        header.getChildren().addAll(iconLabel, infoBox, spacer, statusBadge);
        
        Label routeLabel = new Label(v.routeId() != null ? v.routeId() + " – " + monitoring.getRouteById(v.routeId()).routeName() : "Unassigned");
        routeLabel.setStyle("-fx-text-fill: " + TEXT_SECONDARY + "; -fx-font-size: 12px;");
        
        HBox stats = new HBox(16);
        stats.setAlignment(Pos.CENTER);
        
        VBox speedBox = new VBox(2);
        Label speedVal = new Label(v.hasLocation() ? String.format("%.0f", v.speedKmh()) : "--");
        speedVal.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: " + TEXT_PRIMARY + ";");
        Label speedLabel = new Label("km/h");
        speedLabel.setStyle("-fx-text-fill: " + TEXT_MUTED + "; -fx-font-size: 10px;");
        speedBox.getChildren().addAll(speedVal, speedLabel);
        speedBox.setAlignment(Pos.CENTER);
        
        VBox paxBox = new VBox(2);
        int pct = v.capacity() > 0 ? (int)((double)v.passengerCount() / v.capacity() * 100) : 0;
        String pctColor = pct >= 90 ? ACCENT_RED : (pct >= 60 ? ACCENT_ORANGE : GREEN_PRIMARY);
        Label paxVal = new Label(v.passengerCount() + "/" + v.capacity());
        paxVal.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: " + pctColor + ";");
        Label paxLabel = new Label("passengers");
        paxLabel.setStyle("-fx-text-fill: " + TEXT_MUTED + "; -fx-font-size: 10px;");
        paxBox.getChildren().addAll(paxVal, paxLabel);
        paxBox.setAlignment(Pos.CENTER);
        
        stats.getChildren().addAll(speedBox, paxBox);
        
        Label plateLabel = new Label("Plate: " + v.plateNumber());
        plateLabel.setStyle("-fx-text-fill: " + TEXT_MUTED + "; -fx-font-size: 11px;");
        
        card.getChildren().addAll(header, routeLabel, separator(), stats, plateLabel);
        return card;
    }

    private void showLiveMapPanel() {
        contentArea.getChildren().clear();

        Label title = new Label("Live Vehicle Map");
        title.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: " + TEXT_PRIMARY + ";");

        Label subtitle = new Label("Embedded live map • Cebu City");
        subtitle.setStyle("-fx-font-size: 13px; -fx-text-fill: " + TEXT_SECONDARY + ";");

        ComboBox<String> routeFilter = new ComboBox<>();
        routeFilter.getItems().add("All Routes");
        monitoring.getRoutes().forEach(r -> routeFilter.getItems().add(r.routeId() + " – " + r.routeName()));
        routeFilter.setValue("All Routes");
        routeFilter.setMaxWidth(320);
        routeFilter.setPrefHeight(40);
        styleComboBox(routeFilter);

        Button refreshBtn = modernButton("Refresh Map", GREEN_PRIMARY, 140);

        WebView mapView = new WebView();
        mapView.setPrefSize(1100, 580);
        mapView.setMinHeight(500);

        Runnable loadMap = () -> {
            String filter = routeFilter.getValue();
            String filterRouteId = "All Routes".equals(filter) ? null : filter.split("–")[0].trim();

            StringBuilder markers = new StringBuilder();

            for (PublicVehicle v : monitoring.getAllVehicles()) {
                if (!v.hasLocation()) continue;
                if (filterRouteId != null && !filterRouteId.equalsIgnoreCase(v.routeId())) continue;

                String vehicleType = v instanceof Bus ? "Bus" : v instanceof ModernJeep ? "Modern Jeep" : "Jeepney";

                markers.append(String.format(
                        """
                        L.marker([%f, %f])
                            .addTo(map)
                            .bindPopup("<b>%s</b><br>Type: %s<br>Plate: %s<br>Route: %s<br>Speed: %.0f km/h<br>Passengers: %d/%d");
                        """,
                        v.lat(),
                        v.lon(),
                        v.vehicleId(),
                        vehicleType,
                        v.plateNumber(),
                        v.routeId() != null ? v.routeId() : "Unassigned",
                        v.speedKmh(),
                        v.passengerCount(),
                        v.capacity()
                ));
            }

            String html = """
                <!DOCTYPE html>
                <html>
                <head>
                    <meta charset="utf-8">
                    <link rel="stylesheet" href="https://unpkg.com/leaflet@1.9.4/dist/leaflet.css">
                    <script src="https://unpkg.com/leaflet@1.9.4/dist/leaflet.js"></script>
                    <style>
                        html, body, #map {
                            width: 100%%;
                            height: 100%%;
                            margin: 0;
                            padding: 0;
                        }
                    </style>
                </head>
                <body>
                    <div id="map"></div>

                    <script>
                        var map = L.map('map').setView([10.3157, 123.8854], 13);

                        L.tileLayer('https://tile.openstreetmap.org/{z}/{x}/{y}.png', {
                            maxZoom: 19
                        }).addTo(map);

                        %s

                        setTimeout(function() {
                            map.invalidateSize();
                        }, 500);
                    </script>
                </body>
                </html>
                """.formatted(markers.toString());

            mapView.getEngine().loadContent(html);
        };

        refreshBtn.setOnAction(e -> loadMap.run());
        routeFilter.setOnAction(e -> loadMap.run());

        loadMap.run();

        if (mapAutoRefresh != null) mapAutoRefresh.stop();
        mapAutoRefresh = new Timeline(new KeyFrame(Duration.seconds(4), e -> loadMap.run()));
        mapAutoRefresh.setCycleCount(Timeline.INDEFINITE);
        mapAutoRefresh.play();

        HBox controls = new HBox(12, routeFilter, refreshBtn);
        controls.setAlignment(Pos.CENTER_LEFT);

        VBox card = createModernCard(title, subtitle, controls, mapView);
        VBox.setVgrow(mapView, Priority.ALWAYS);
        contentArea.getChildren().add(card);
    }

    private void drawJavaFXLiveMap() {
        if (liveMapCanvas == null) return;

        GraphicsContext gc = liveMapCanvas.getGraphicsContext2D();
        double w = liveMapCanvas.getWidth();
        double h = liveMapCanvas.getHeight();

        if (cebuMapImage == null) {
            cebuMapImage = new Image(Objects.requireNonNull(
                    getClass().getResourceAsStream("/assets/cebu-map.png")
            ));
        }

        gc.drawImage(cebuMapImage, 0, 0, w, h);

        gc.setFill(Color.rgb(0, 0, 0, 0.18));
        gc.fillRect(0, 0, w, h);

        gc.setFill(Color.web("#00C853"));
        gc.fillOval(w - 155, 24, 10, 10);

        gc.setFill(Color.WHITE);
        gc.setFont(Font.font("System", FontWeight.BOLD, 14));
        gc.fillText("LIVE TRACKING", w - 138, 35);

        gc.setFill(Color.web(TEXT_SECONDARY));
        gc.setFont(Font.font("System", FontWeight.NORMAL, 11));
        gc.fillText("Last updated: " + java.time.LocalTime.now().withNano(0), w - 185, 55);

        String filter = currentMapRouteFilter == null ? "All Routes" : currentMapRouteFilter.getValue();
        String filterRouteId = "All Routes".equals(filter) ? null : filter.split("–")[0].trim();

        for (Route route : monitoring.getRoutes()) {
            if (filterRouteId != null && !route.routeId().equalsIgnoreCase(filterRouteId)) continue;

            List<double[]> path = routePaths.get(route.routeId());
            if (path == null || path.size() < 2) continue;

            gc.setStroke(Color.rgb(0, 200, 83, 0.35));
            gc.setLineWidth(10);

            for (int i = 0; i < path.size() - 1; i++) {
                double[] p1 = path.get(i);
                double[] p2 = path.get(i + 1);

                double x1 = lonToX(p1[1], w);
                double y1 = latToY(p1[0], h);
                double x2 = lonToX(p2[1], w);
                double y2 = latToY(p2[0], h);

                gc.strokeLine(x1, y1, x2, y2);
            }

            gc.setStroke(Color.web(GREEN_PRIMARY));
            gc.setLineWidth(4);

            for (int i = 0; i < path.size() - 1; i++) {
                double[] p1 = path.get(i);
                double[] p2 = path.get(i + 1);

                double x1 = lonToX(p1[1], w);
                double y1 = latToY(p1[0], h);
                double x2 = lonToX(p2[1], w);
                double y2 = latToY(p2[0], h);

                gc.strokeLine(x1, y1, x2, y2);
            }

            double[] first = path.get(0);
            gc.setFill(Color.web(GREEN_PRIMARY));
            gc.setFont(Font.font("System", FontWeight.BOLD, 12));
            gc.fillText(route.routeId(), lonToX(first[1], w) + 8, latToY(first[0], h) - 8);
        }

        double pulse = 6 + (System.currentTimeMillis() % 1000) / 1000.0 * 10;

        for (PublicVehicle v : monitoring.getAllVehicles()) {
            if (!v.hasLocation()) continue;
            if (filterRouteId != null && !filterRouteId.equalsIgnoreCase(v.routeId())) continue;

            double x = lonToX(v.lon(), w);
            double y = latToY(v.lat(), h);

            String color = v instanceof Bus ? ACCENT_BLUE : v instanceof ModernJeep ? ACCENT_ORANGE : GREEN_PRIMARY;
            String icon = v instanceof Bus ? "B" : v instanceof ModernJeep ? "MJ" : "J";

            gc.setStroke(Color.rgb(0, 200, 83, 0.35));
            gc.setLineWidth(3);
            gc.strokeOval(x - 24 - pulse / 2, y - 24 - pulse / 2, 48 + pulse, 48 + pulse);

            gc.setFill(Color.web(color));
            gc.fillOval(x - 22, y - 22, 44, 44);

            gc.setStroke(Color.WHITE);
            gc.setLineWidth(3);
            gc.strokeOval(x - 22, y - 22, 44, 44);

            gc.setFill(Color.WHITE);
            gc.setFont(Font.font("System", FontWeight.BOLD, 12));
            gc.fillText(icon, x - 10, y + 4);

            gc.setFill(Color.WHITE);
            gc.setFont(Font.font("System", FontWeight.BOLD, 11));
            gc.fillText(v.vehicleId(), x + 28, y - 6);

            gc.setFill(Color.web(TEXT_SECONDARY));
            gc.setFont(Font.font("System", FontWeight.NORMAL, 10));
            gc.fillText(String.format("%.0f km/h | %d/%d pax",
                    v.speedKmh(),
                    v.passengerCount(),
                    v.capacity()
            ), x + 28, y + 9);
        }
    }

    private double latToY(double lat, double height) {
        double minLat = 10.2850;
        double maxLat = 10.3750;
        return height - ((lat - minLat) / (maxLat - minLat)) * height;
    }

    private double lonToX(double lon, double width) {
        double minLon = 123.8700;
        double maxLon = 123.9300;
        return ((lon - minLon) / (maxLon - minLon)) * width;
    }


    private void refreshLiveMap() {
        if (liveMapCanvas != null) {
            drawJavaFXLiveMap();
        }
    }

    private void showGPSTrackingPanel() {
        contentArea.getChildren().clear();

        Label title = new Label("GPS Tracking Mode");
        title.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: " + TEXT_PRIMARY + ";");

        VBox infoCard = new VBox(16);
        infoCard.setPadding(new Insets(24));
        infoCard.setStyle("-fx-background-color: " + CARD_BG + "; -fx-background-radius: 16; -fx-border-color: " + BORDER_COLOR + "; -fx-border-radius: 16;");

        Label modeStatus = new Label(isRealGPSMode ? "● GPS Tracking is ACTIVE" : "○ GPS Tracking is INACTIVE");
        modeStatus.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: " + (isRealGPSMode ? GREEN_PRIMARY : TEXT_SECONDARY) + ";");

        ToggleButton enableGPS = new ToggleButton(isRealGPSMode ? "Disable GPS Tracking" : "Enable GPS Tracking");
        enableGPS.setStyle("-fx-background-color: " + (isRealGPSMode ? ACCENT_RED : GREEN_PRIMARY) + "; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 24; -fx-padding: 12 24;");
        
        enableGPS.setOnAction(e -> {
            isRealGPSMode = enableGPS.isSelected();
            enableGPS.setText(isRealGPSMode ? "Disable GPS Tracking" : "Enable GPS Tracking");
            enableGPS.setStyle("-fx-background-color: " + (isRealGPSMode ? ACCENT_RED : GREEN_PRIMARY) + "; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 24; -fx-padding: 12 24;");
            modeStatus.setText(isRealGPSMode ? "● GPS Tracking is ACTIVE" : "○ GPS Tracking is INACTIVE");
            modeStatus.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: " + (isRealGPSMode ? GREEN_PRIMARY : TEXT_SECONDARY) + ";");
            addActivityLog(isRealGPSMode ? "Enabled real GPS tracking" : "Disabled real GPS tracking");
            if (liveMapCanvas != null) refreshLiveMap();
        });

        Label instruction = new Label("When enabled, your device's GPS will be used to track the showcase vehicle (V-SHOWCASE) in real-time.\n\nMake sure to allow location permissions when prompted.");
        instruction.setStyle("-fx-text-fill: " + TEXT_SECONDARY + "; -fx-font-size: 13px; -fx-wrap-text: true;");

        infoCard.getChildren().addAll(modeStatus, enableGPS, separator(), instruction);
        contentArea.getChildren().add(createModernCard(title, infoCard));
    }

    private void showModernProfilePanel() {
        contentArea.getChildren().clear();

        Label title = new Label("My Profile");
        title.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: " + TEXT_PRIMARY + ";");

        if (currentUser == null) return;

        VBox profileHeader = new VBox(16);
        profileHeader.setAlignment(Pos.CENTER);
        profileHeader.setPadding(new Insets(24));
        profileHeader.setStyle("-fx-background-color: " + CARD_BG + "; -fx-background-radius: 20; -fx-border-color: " + BORDER_COLOR + "; -fx-border-radius: 20;");

        Circle avatarBg = new Circle(55, Color.web(GREEN_PRIMARY));
        Label avatarInitial = new Label(currentUser.name().substring(0, 1).toUpperCase());
        avatarInitial.setFont(Font.font("System", FontWeight.BOLD, 42));
        avatarInitial.setTextFill(Color.WHITE);
        StackPane avatarPane = new StackPane(avatarBg, avatarInitial);

        Label nameLabel = new Label(currentUser.name());
        nameLabel.setFont(Font.font("System", FontWeight.BOLD, 22));
        nameLabel.setStyle("-fx-text-fill: " + TEXT_PRIMARY + ";");

        Label usernameLabel = new Label("@" + currentUser.username());
        usernameLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: " + TEXT_SECONDARY + ";");

        Label roleBadge = new Label(currentUser.role());
        roleBadge.setStyle("-fx-background-color: " + GREEN_PRIMARY + "20; -fx-text-fill: " + GREEN_PRIMARY + "; -fx-font-size: 12px; -fx-font-weight: bold; -fx-padding: 6 16; -fx-background-radius: 20;");

        profileHeader.getChildren().addAll(avatarPane, nameLabel, usernameLabel, roleBadge);

        HBox statsRow = new HBox(20);
        statsRow.setAlignment(Pos.CENTER);
        statsRow.setPadding(new Insets(20));
        statsRow.setStyle("-fx-background-color: " + CARD_BG + "; -fx-background-radius: 16; -fx-border-color: " + BORDER_COLOR + "; -fx-border-radius: 16;");

        int totalRoutes = monitoring.getRoutes().size();
        int totalVehicles = monitoring.getAllVehicles().size();
        int totalAlerts = monitoring.getAlerts().size();

        statsRow.getChildren().addAll(statBox("Routes", String.valueOf(totalRoutes)), statBox("Vehicles", String.valueOf(totalVehicles)), statBox("Alerts", String.valueOf(totalAlerts)));

        VBox editForm = new VBox(16);
        editForm.setPadding(new Insets(24));
        editForm.setStyle("-fx-background-color: " + CARD_BG + "; -fx-background-radius: 16; -fx-border-color: " + BORDER_COLOR + "; -fx-border-radius: 16;");

        Label editTitle = new Label("Edit Profile");
        editTitle.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: " + TEXT_PRIMARY + ";");

        TextField nameField = styledInput("Full Name", 400);
        nameField.setText(currentUser.name());
        TextField usernameFieldEdit = styledInput("Username", 400);
        usernameFieldEdit.setText(currentUser.username());

        Label profileMsg = new Label();
        Button saveProfile = modernButton("Save Changes", GREEN_PRIMARY, 200);

        saveProfile.setOnAction(e -> {
            String newName = nameField.getText().trim();
            String newUser = usernameFieldEdit.getText().trim();
            if (newName.isEmpty() || newUser.isEmpty()) { showError(profileMsg, "Fields cannot be empty."); return; }
            boolean ok = auth.updateUserProfile(currentUser, newName, newUser);
            if (!ok) { showError(profileMsg, "Username already taken."); return; }
            updateSavedUserRecord(currentUser.userId(), newName, newUser, null);
            DataStore.saveAll(auth, monitoring, savedUsers);
            showSuccess(profileMsg, "Profile updated successfully!");
            headerTitle.setText(newName);
        });

        editForm.getChildren().addAll(editTitle, nameField, usernameFieldEdit, saveProfile, profileMsg);

        VBox passwordSection = new VBox(16);
        passwordSection.setPadding(new Insets(24));
        passwordSection.setStyle("-fx-background-color: " + CARD_BG + "; -fx-background-radius: 16; -fx-border-color: " + BORDER_COLOR + "; -fx-border-radius: 16;");

        Label passTitle = new Label("Security");
        passTitle.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: " + TEXT_PRIMARY + ";");

        PasswordField currPass = new PasswordField();
        currPass.setPromptText("Current Password");
        currPass.setMaxWidth(400);
        currPass.setPrefHeight(48);
        currPass.setStyle(inputCss());

        PasswordField newPass = new PasswordField();
        newPass.setPromptText("New Password");
        newPass.setMaxWidth(400);
        newPass.setPrefHeight(48);
        newPass.setStyle(inputCss());

        PasswordField confPass = new PasswordField();
        confPass.setPromptText("Confirm New Password");
        confPass.setMaxWidth(400);
        confPass.setPrefHeight(48);
        confPass.setStyle(inputCss());

        Label passMsg = new Label();
        Button changePassBtn = modernButton("Change Password", GREEN_PRIMARY, 200);

        changePassBtn.setOnAction(e -> {
            String curr = currPass.getText().trim();
            String nw = newPass.getText().trim();
            String conf = confPass.getText().trim();
            if (curr.isEmpty() || nw.isEmpty() || conf.isEmpty()) { showError(passMsg, "Fill all password fields."); return; }
            if (nw.length() < 4) { showError(passMsg, "Password must be at least 4 characters."); return; }
            if (!nw.equals(conf)) { showError(passMsg, "Passwords do not match."); return; }
            boolean ok = auth.changePassword(currentUser, curr, nw);
            if (!ok) { showError(passMsg, "Current password is incorrect."); return; }
            updateSavedUserRecord(currentUser.userId(), null, null, nw);
            DataStore.saveAll(auth, monitoring, savedUsers);
            currPass.clear(); newPass.clear(); confPass.clear();
            showSuccess(passMsg, "Password changed successfully!");
        });

        passwordSection.getChildren().addAll(passTitle, currPass, newPass, confPass, changePassBtn, passMsg);

        contentArea.getChildren().addAll(createModernCard(title, profileHeader), statsRow, editForm, passwordSection);
    }

    private VBox statBox(String label, String value) {
        VBox box = new VBox(4);
        box.setAlignment(Pos.CENTER);
        box.setPadding(new Insets(12, 24, 12, 24));
        box.setStyle("-fx-background-color: " + CARD_HOVER + "; -fx-background-radius: 12;");
        
        Label valueLabel = new Label(value);
        valueLabel.setFont(Font.font("System", FontWeight.BOLD, 28));
        valueLabel.setStyle("-fx-text-fill: " + GREEN_PRIMARY + ";");
        
        Label labelText = new Label(label);
        labelText.setStyle("-fx-text-fill: " + TEXT_SECONDARY + "; -fx-font-size: 12px;");
        
        box.getChildren().addAll(valueLabel, labelText);
        return box;
    }

    private void showModernSearchByRoute() {
        contentArea.getChildren().clear();

        Label title = new Label("Search by Route");
        title.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: " + TEXT_PRIMARY + ";");

        ComboBox<Route> routeBox = new ComboBox<>();
        routeBox.getItems().addAll(monitoring.getRoutes());
        routeBox.setPromptText("Select a route");
        routeBox.setMaxWidth(400);
        routeBox.setPrefHeight(48);
        styleComboBox(routeBox);

        Button searchBtn = modernButton("Search Route", GREEN_PRIMARY, 180);
        Label msg = new Label();

        VBox resultsContainer = new VBox(16);
        resultsContainer.setPadding(new Insets(16, 0, 0, 0));

        searchBtn.setOnAction(e -> {
            resultsContainer.getChildren().clear();
            Route route = routeBox.getValue();
            if (route == null) { showError(msg, "Please select a route."); return; }
            msg.setText("");

            List<PublicVehicle> vehicles = monitoring.getVehiclesByRoute(route.routeId());
            if (vehicles.isEmpty()) {
                Label none = new Label("No vehicles assigned to this route.");
                none.setStyle("-fx-text-fill: " + TEXT_SECONDARY + ";");
                resultsContainer.getChildren().add(none);
                return;
            }

            for (PublicVehicle v : vehicles) {
                VBox vehicleCard = new VBox(8);
                vehicleCard.setPadding(new Insets(14));
                vehicleCard.setStyle("-fx-background-color: " + CARD_BG + "; -fx-background-radius: 12; -fx-border-color: " + BORDER_COLOR + "; -fx-border-radius: 12;");
                
                HBox header = new HBox(12);
                Label icon = new Label(v instanceof Bus ? "🚌" : (v instanceof ModernJeep ? "🚎" : "🚐"));
                icon.setStyle("-fx-font-size: 24px;");
                Label vehicleId = new Label(v.vehicleId());
                vehicleId.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: " + TEXT_PRIMARY + ";");
                header.getChildren().addAll(icon, vehicleId);
                
                Label plateLabel = new Label("Plate: " + v.plateNumber());
                plateLabel.setStyle("-fx-text-fill: " + TEXT_SECONDARY + "; -fx-font-size: 12px;");
                
                String status = monitoring.getVehicleStatus(v);
                String statusColor = status.equals("NORMAL") ? GREEN_PRIMARY : ACCENT_ORANGE;
                Label statusLabel = new Label(status);
                statusLabel.setStyle("-fx-text-fill: " + statusColor + "; -fx-font-size: 12px; -fx-font-weight: bold;");
                
                vehicleCard.getChildren().addAll(header, plateLabel, statusLabel);
                resultsContainer.getChildren().add(vehicleCard);
            }
        });

        VBox card = createModernCard(title, routeBox, searchBtn, msg, resultsContainer);
        contentArea.getChildren().add(card);
    }

    private void showEtaTrackerPanel() {
        contentArea.getChildren().clear();
        Label title = new Label("ETA Tracker");
        title.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: " + TEXT_PRIMARY + ";");

        ComboBox<Route> routeBox = new ComboBox<>();
        routeBox.getItems().addAll(monitoring.getRoutes());
        routeBox.setPromptText("Select Route");
        routeBox.setMaxWidth(400);
        routeBox.setPrefHeight(48);
        styleComboBox(routeBox);

        ComboBox<Stop> stopBox = new ComboBox<>();
        stopBox.setPromptText("Select Destination Stop");
        stopBox.setMaxWidth(400);
        stopBox.setPrefHeight(48);
        styleComboBox(stopBox);

        routeBox.setOnAction(e -> {
            stopBox.getItems().clear();
            Route selected = routeBox.getValue();
            if (selected != null) stopBox.getItems().addAll(selected.stops());
        });

        Button calcBtn = modernButton("Calculate ETA", GREEN_PRIMARY, 180);
        Label msg = new Label();
        VBox resultsBox = new VBox(12);

        calcBtn.setOnAction(e -> {
            resultsBox.getChildren().clear();
            Route route = routeBox.getValue();
            Stop destination = stopBox.getValue();
            if (route == null || destination == null) { showError(msg, "Select both route and destination stop."); return; }
            msg.setText("");

            List<PublicVehicle> vehicles = monitoring.getVehiclesByRoute(route.routeId());
            if (vehicles.isEmpty()) {
                Label none = new Label("No vehicles assigned to this route.");
                none.setStyle("-fx-text-fill: " + TEXT_SECONDARY + ";");
                resultsBox.getChildren().add(none);
                return;
            }

            for (PublicVehicle v : vehicles) {
                if (!v.hasLocation()) continue;
                double eta = monitoring.calculateEtaMinutes(v, destination);
                resultsBox.getChildren().add(createEtaCard(v, eta, destination));
            }
        });

        VBox card = createModernCard(title, routeBox, stopBox, calcBtn, msg, resultsBox);
        contentArea.getChildren().add(card);
    }

    private VBox createEtaCard(PublicVehicle v, double eta, Stop destination) {
        VBox card = new VBox(8);
        card.setPadding(new Insets(14));
        card.setStyle("-fx-background-color: " + CARD_BG + "; -fx-background-radius: 12;");
        
        HBox header = new HBox(12);
        Label icon = new Label(v instanceof Bus ? "🚌" : (v instanceof ModernJeep ? "🚎" : "🚐"));
        icon.setStyle("-fx-font-size: 20px;");
        Label vehicleId = new Label(v.vehicleId());
        vehicleId.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: " + TEXT_PRIMARY + ";");
        header.getChildren().addAll(icon, vehicleId);
        
        Label etaLabel = new Label("ETA to " + destination.stopName() + ": " + (eta >= 0 ? String.format("%.0f minutes", eta) : "N/A"));
        etaLabel.setStyle("-fx-text-fill: " + ACCENT_BLUE + "; -fx-font-size: 14px; -fx-font-weight: bold;");
        
        card.getChildren().addAll(header, etaLabel);
        return card;
    }

    private void showVehiclesTable() {
        contentArea.getChildren().clear();
        Label title = new Label("Registered Vehicles");
        title.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: " + TEXT_PRIMARY + ";");

        TableView<PublicVehicle> table = new TableView<>();
        table.setItems(FXCollections.observableArrayList(monitoring.getAllVehicles()));
        styleTable(table);

        TableColumn<PublicVehicle, String> idCol = new TableColumn<>("Vehicle ID");
        idCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().vehicleId()));
        TableColumn<PublicVehicle, String> typeCol = new TableColumn<>("Type");
        typeCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getVehicleType()));
        TableColumn<PublicVehicle, String> plateCol = new TableColumn<>("Plate");
        plateCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().plateNumber()));
        TableColumn<PublicVehicle, String> routeCol = new TableColumn<>("Route");
        routeCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().routeId() != null ? data.getValue().routeId() : "Unassigned"));
        TableColumn<PublicVehicle, String> statusCol = new TableColumn<>("Status");
        statusCol.setCellValueFactory(data -> new SimpleStringProperty(monitoring.getVehicleStatus(data.getValue())));

        table.getColumns().addAll(idCol, typeCol, plateCol, routeCol, statusCol);
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        Button refresh = modernButton("Refresh", GREEN_PRIMARY, 140);
        refresh.setOnAction(e -> table.setItems(FXCollections.observableArrayList(monitoring.getAllVehicles())));

        VBox card = createModernCard(title, refresh, table);
        VBox.setVgrow(table, Priority.ALWAYS);
        contentArea.getChildren().add(card);
    }

    private void showAddPanel() {
        contentArea.getChildren().clear();
        Label title = new Label("Add Data");
        title.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: " + TEXT_PRIMARY + ";");
        
        ComboBox<String> addTypeBox = new ComboBox<>();
        addTypeBox.getItems().addAll("Route", "Stop", "Vehicle");
        addTypeBox.setPromptText("What to add?");
        addTypeBox.setMaxWidth(400);
        addTypeBox.setPrefHeight(48);
        styleComboBox(addTypeBox);

        TextField routeIdField = styledInput("Route ID", 380);
        TextField routeNameField = styledInput("Route Name", 380);

        ComboBox<Route> routeBoxStop = new ComboBox<>();
        routeBoxStop.getItems().addAll(monitoring.getRoutes());
        routeBoxStop.setPromptText("Select Route");
        routeBoxStop.setMaxWidth(380);
        routeBoxStop.setPrefHeight(48);
        styleComboBox(routeBoxStop);
        
        TextField stopIdField = styledInput("Stop ID", 380);
        TextField stopNameField = styledInput("Stop Name", 380);
        TextField latField = styledInput("Latitude", 380);
        TextField lonField = styledInput("Longitude", 380);

        ComboBox<String> vehicleTypeBox = new ComboBox<>();
        vehicleTypeBox.getItems().addAll("Jeepney", "Modern Jeep", "Bus");
        vehicleTypeBox.setPromptText("Vehicle Type");
        vehicleTypeBox.setMaxWidth(380);
        vehicleTypeBox.setPrefHeight(48);
        styleComboBox(vehicleTypeBox);
        
        TextField vehicleIdField = styledInput("Vehicle ID", 380);
        TextField plateField = styledInput("Plate Number", 380);
        TextField capacityField = styledInput("Capacity", 380);

        Label message = new Label();
        Button addBtn = modernButton("Add", GREEN_PRIMARY, 160);

        javafx.scene.Node[] routeFields = {routeIdField, routeNameField};
        javafx.scene.Node[] stopFields = {routeBoxStop, stopIdField, stopNameField, latField, lonField};
        javafx.scene.Node[] vehicleFields = {vehicleTypeBox, vehicleIdField, plateField, capacityField};

        hideNodes(routeFields); hideNodes(stopFields); hideNodes(vehicleFields);

        addTypeBox.setOnAction(e -> {
            String sel = addTypeBox.getValue();
            hideNodes(routeFields); hideNodes(stopFields); hideNodes(vehicleFields);
            message.setText("");
            if ("Route".equals(sel)) showNodes(routeFields);
            else if ("Stop".equals(sel)) { routeBoxStop.getItems().setAll(monitoring.getRoutes()); showNodes(stopFields); }
            else if ("Vehicle".equals(sel)) showNodes(vehicleFields);
        });

        addBtn.setOnAction(e -> {
            String sel = addTypeBox.getValue();
            if (sel == null) { showError(message, "Choose what to add."); return; }

            if ("Route".equals(sel)) {
                String rid = routeIdField.getText().trim(), rname = routeNameField.getText().trim();
                if (rid.isEmpty() || rname.isEmpty()) { showError(message, "Fill all route fields."); return; }
                if (monitoring.routeExists(rid)) { showError(message, "Route ID exists."); return; }
                monitoring.addRoute(new Route(rid, rname));
                DataStore.saveAll(auth, monitoring, savedUsers);
                routeIdField.clear(); routeNameField.clear();
                showSuccess(message, "Route added.");
                routePaths.put(rid, new ArrayList<>());
            } else if ("Stop".equals(sel)) {
                try {
                    Route route = routeBoxStop.getValue();
                    String sid = stopIdField.getText().trim();
                    String sname = stopNameField.getText().trim();
                    if (route == null || sid.isEmpty() || sname.isEmpty()) { showError(message, "Fill all stop fields."); return; }
                    if (route.stopExists(sid)) { showError(message, "Stop ID exists on this route."); return; }
                    route.addStop(new Stop(sid, sname, Double.parseDouble(latField.getText().trim()), Double.parseDouble(lonField.getText().trim())));
                    DataStore.saveAll(auth, monitoring, savedUsers);
                    stopIdField.clear(); stopNameField.clear(); latField.clear(); lonField.clear();
                    showSuccess(message, "Stop added.");
                } catch (NumberFormatException ex) { showError(message, "Invalid lat/lon."); }
            } else if ("Vehicle".equals(sel)) {
                try {
                    String vtype = vehicleTypeBox.getValue();
                    String vid = vehicleIdField.getText().trim();
                    String plate = plateField.getText().trim();
                    int capVal = Integer.parseInt(capacityField.getText().trim());
                    if (vtype == null || vid.isEmpty() || plate.isEmpty()) { showError(message, "Fill all vehicle fields."); return; }
                    if (monitoring.vehicleExists(vid)) { showError(message, "Vehicle ID exists."); return; }
                    PublicVehicle v = switch (vtype) {
                        case "Bus" -> new Bus(vid, plate, capVal);
                        case "Modern Jeep" -> new ModernJeep(vid, plate, capVal);
                        default -> new Jeepney(vid, plate, capVal);
                    };
                    monitoring.registerVehicle(v);
                    DataStore.saveAll(auth, monitoring, savedUsers);
                    vehicleIdField.clear(); plateField.clear(); capacityField.clear();
                    showSuccess(message, "Vehicle registered.");
                } catch (NumberFormatException ex) { showError(message, "Invalid capacity."); }
            }
        });

        contentArea.getChildren().add(createModernCard(title, addTypeBox, routeIdField, routeNameField, routeBoxStop, stopIdField, stopNameField, latField, lonField, vehicleTypeBox, vehicleIdField, plateField, capacityField, addBtn, message));
    }

    private void showAssignVehiclePanel() {
        contentArea.getChildren().clear();
        Label title = new Label("Assign Vehicle to Route");
        title.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: " + TEXT_PRIMARY + ";");

        ComboBox<PublicVehicle> vehicleBox = new ComboBox<>();
        vehicleBox.getItems().addAll(monitoring.getAllVehicles());
        vehicleBox.setPromptText("Select Vehicle");
        vehicleBox.setMaxWidth(400);
        vehicleBox.setPrefHeight(48);
        styleComboBox(vehicleBox);

        ComboBox<Route> routeBox = new ComboBox<>();
        routeBox.getItems().addAll(monitoring.getRoutes());
        routeBox.setPromptText("Select Route");
        routeBox.setMaxWidth(400);
        routeBox.setPrefHeight(48);
        styleComboBox(routeBox);

        Label msg = new Label();
        Button save = modernButton("Assign", GREEN_PRIMARY, 160);

        save.setOnAction(e -> {
            PublicVehicle v = vehicleBox.getValue();
            Route r = routeBox.getValue();
            if (v == null || r == null) { showError(msg, "Select vehicle and route."); return; }
            monitoring.assignVehicleToRoute(v.vehicleId(), r.routeId());
            DataStore.saveAll(auth, monitoring, savedUsers);
            addActivityLog("Assigned " + v.vehicleId() + " → " + r.routeId());
            showSuccess(msg, "Vehicle assigned to " + r.routeName() + ".");
        });

        contentArea.getChildren().add(createModernCard(title, vehicleBox, routeBox, save, msg));
    }

    private void showRemovePanel() {
        contentArea.getChildren().clear();
        Label title = new Label("Remove Data");
        title.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: " + TEXT_PRIMARY + ";");

        ComboBox<String> removeTypeBox = new ComboBox<>();
        removeTypeBox.getItems().addAll("Route", "Stop", "Vehicle");
        removeTypeBox.setPromptText("What to remove?");
        removeTypeBox.setMaxWidth(400);
        removeTypeBox.setPrefHeight(48);
        styleComboBox(removeTypeBox);

        ComboBox<Route> routeBox = new ComboBox<>();
        routeBox.getItems().addAll(monitoring.getRoutes());
        routeBox.setPromptText("Select Route");
        routeBox.setMaxWidth(400);
        routeBox.setPrefHeight(48);
        styleComboBox(routeBox);
        routeBox.setVisible(false);
        routeBox.setManaged(false);

        ComboBox<PublicVehicle> vehicleBox = new ComboBox<>();
        vehicleBox.getItems().addAll(monitoring.getAllVehicles());
        vehicleBox.setPromptText("Select Vehicle");
        vehicleBox.setMaxWidth(400);
        vehicleBox.setPrefHeight(48);
        styleComboBox(vehicleBox);
        vehicleBox.setVisible(false);
        vehicleBox.setManaged(false);

        TextField stopIdField = styledInput("Stop ID to remove", 400);
        stopIdField.setVisible(false);
        stopIdField.setManaged(false);

        Label msg = new Label();
        Button removeBtn = new Button("Remove");
        removeBtn.setPrefHeight(48);
        removeBtn.setMinWidth(160);
        removeBtn.setStyle("-fx-background-color: " + ACCENT_RED + "; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 12;");

        removeTypeBox.setOnAction(e -> {
            String sel = removeTypeBox.getValue();
            routeBox.setVisible(false); routeBox.setManaged(false);
            vehicleBox.setVisible(false); vehicleBox.setManaged(false);
            stopIdField.setVisible(false); stopIdField.setManaged(false);
            if ("Route".equals(sel)) { routeBox.getItems().setAll(monitoring.getRoutes()); routeBox.setVisible(true); routeBox.setManaged(true); }
            else if ("Stop".equals(sel)) { routeBox.getItems().setAll(monitoring.getRoutes()); routeBox.setVisible(true); routeBox.setManaged(true); stopIdField.setVisible(true); stopIdField.setManaged(true); }
            else if ("Vehicle".equals(sel)) { vehicleBox.getItems().setAll(monitoring.getAllVehicles()); vehicleBox.setVisible(true); vehicleBox.setManaged(true); }
        });

        removeBtn.setOnAction(e -> {
            String sel = removeTypeBox.getValue();
            if (sel == null) { showError(msg, "Choose what to remove."); return; }
            if ("Route".equals(sel)) {
                Route r = routeBox.getValue();
                if (r == null) { showError(msg, "Select a route."); return; }
                if (monitoring.removeRoute(r.routeId())) { DataStore.saveAll(auth, monitoring, savedUsers); routeBox.getItems().setAll(monitoring.getRoutes()); showSuccess(msg, "Route removed."); }
                else showError(msg, "Route not found.");
            } else if ("Stop".equals(sel)) {
                Route r = routeBox.getValue();
                String sid = stopIdField.getText().trim();
                if (r == null || sid.isEmpty()) { showError(msg, "Select route and enter stop ID."); return; }
                if (monitoring.removeStopFromRoute(r.routeId(), sid)) { DataStore.saveAll(auth, monitoring, savedUsers); stopIdField.clear(); showSuccess(msg, "Stop removed."); }
                else showError(msg, "Stop not found.");
            } else if ("Vehicle".equals(sel)) {
                PublicVehicle v = vehicleBox.getValue();
                if (v == null) { showError(msg, "Select a vehicle."); return; }
                if (monitoring.removeVehicle(v.vehicleId())) { DataStore.saveAll(auth, monitoring, savedUsers); vehicleBox.getItems().setAll(monitoring.getAllVehicles()); showSuccess(msg, "Vehicle removed."); }
                else showError(msg, "Vehicle not found.");
            }
        });

        contentArea.getChildren().add(createModernCard(title, removeTypeBox, routeBox, vehicleBox, stopIdField, removeBtn, msg));
    }

    private void showSendPingPanel() {
        contentArea.getChildren().clear();
        Label title = new Label("Send Vehicle Ping");
        title.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: " + TEXT_PRIMARY + ";");

        ComboBox<PublicVehicle> vehicleBox = new ComboBox<>();
        vehicleBox.getItems().addAll(monitoring.getAllVehicles());
        vehicleBox.setPromptText("Select Vehicle");
        vehicleBox.setMaxWidth(400);
        vehicleBox.setPrefHeight(48);
        styleComboBox(vehicleBox);

        TextField latF = styledInput("Latitude", 400);
        TextField lonF = styledInput("Longitude", 400);
        TextField spd = styledInput("Speed km/h", 400);
        TextField pax = styledInput("Passenger Count", 400);

        Label msg = new Label();
        Button save = modernButton("Send Ping", GREEN_PRIMARY, 160);

        save.setOnAction(e -> {
            try {
                PublicVehicle v = vehicleBox.getValue();
                if (v == null) { showError(msg, "Select a vehicle."); return; }
                VehiclePing ping = new VehiclePing(v.vehicleId(), Instant.now(),
                        Double.parseDouble(latF.getText().trim()),
                        Double.parseDouble(lonF.getText().trim()),
                        Double.parseDouble(spd.getText().trim()),
                        Integer.parseInt(pax.getText().trim()));
                monitoring.receivePing(ping);
                DataStore.saveAll(auth, monitoring, savedUsers);
                addActivityLog("Ping sent to " + v.vehicleId());
                latF.clear(); lonF.clear(); spd.clear(); pax.clear();
                showSuccess(msg, "Ping sent successfully.");
            } catch (Exception ex) { showError(msg, "Invalid input values."); }
        });

        contentArea.getChildren().add(createModernCard(title, vehicleBox, latF, lonF, spd, pax, save, msg));
    }

    private void showAlertsTable() {
        contentArea.getChildren().clear();
        Label title = new Label("Alerts Log");
        title.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: " + TEXT_PRIMARY + ";");

        ComboBox<String> filterBox = new ComboBox<>();
        filterBox.getItems().addAll("ALL", "INFO", "WARN", "CRITICAL");
        filterBox.setValue("ALL");
        filterBox.setMaxWidth(200);
        filterBox.setPrefHeight(40);
        styleComboBox(filterBox);

        TableView<Alert> table = new TableView<>();
        table.setItems(FXCollections.observableArrayList(monitoring.getAlerts()));
        styleTable(table);

        TableColumn<Alert, String> timeCol = new TableColumn<>("Time");
        timeCol.setCellValueFactory(data -> new SimpleStringProperty(timeFormatter.format(data.getValue().timestamp())));
        TableColumn<Alert, String> sevCol = new TableColumn<>("Severity");
        sevCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().severity()));
        TableColumn<Alert, String> typeCol = new TableColumn<>("Type");
        typeCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().type()));
        TableColumn<Alert, String> messageCol = new TableColumn<>("Message");
        messageCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().message()));

        table.getColumns().addAll(timeCol, sevCol, typeCol, messageCol);

        Button applyFilter = modernButton("Filter", GREEN_PRIMARY, 120);
        Button refreshBtn = outlineButton("Refresh", 120);

        applyFilter.setOnAction(e -> {
            String sel = filterBox.getValue();
            table.setItems(FXCollections.observableArrayList("ALL".equals(sel) ? monitoring.getAlerts() : monitoring.getAlertsBySeverity(sel)));
        });
        refreshBtn.setOnAction(e -> { filterBox.setValue("ALL"); table.setItems(FXCollections.observableArrayList(monitoring.getAlerts())); });

        HBox controls = new HBox(12, filterBox, applyFilter, refreshBtn);
        VBox card = createModernCard(title, controls, table);
        VBox.setVgrow(table, Priority.ALWAYS);
        contentArea.getChildren().add(card);
    }

    private void showNotificationCenterPanel() {
        contentArea.getChildren().clear();
        Label title = new Label("Notification Center");
        title.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: " + TEXT_PRIMARY + ";");

        List<Alert> alerts = monitoring.getAlerts();
        VBox list = new VBox(10);

        if (alerts.isEmpty()) {
            Label none = new Label("No notifications.");
            none.setStyle("-fx-text-fill: " + TEXT_SECONDARY + ";");
            list.getChildren().add(none);
        } else {
            for (int i = alerts.size() - 1; i >= Math.max(0, alerts.size() - 30); i--) {
                Alert a = alerts.get(i);
                String color = switch (a.severity()) {
                    case "CRITICAL" -> ACCENT_RED;
                    case "WARN" -> ACCENT_ORANGE;
                    default -> GREEN_PRIMARY;
                };
                
                HBox row = new HBox(12);
                row.setPadding(new Insets(14, 18, 14, 18));
                row.setStyle("-fx-background-color: " + CARD_BG + "; -fx-background-radius: 12;");
                Label dot = new Label("●");
                dot.setStyle("-fx-text-fill: " + color + ";");
                Label content = new Label("[" + a.severity() + "] " + a.type() + ": " + a.message());
                content.setStyle("-fx-text-fill: " + TEXT_SECONDARY + ";");
                content.setWrapText(true);
                HBox.setHgrow(content, Priority.ALWAYS);
                row.getChildren().addAll(dot, content);
                list.getChildren().add(row);
            }
        }

        Button refresh = modernButton("Refresh", GREEN_PRIMARY, 140);
        refresh.setOnAction(e -> showNotificationCenterPanel());

        VBox card = createModernCard(title, refresh, list);
        contentArea.getChildren().add(card);
    }

    private void showActivityLogPanel() {
        contentArea.getChildren().clear();
        Label title = new Label("Activity Log");
        title.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: " + TEXT_PRIMARY + ";");

        TextArea output = new TextArea();
        output.setEditable(false);
        output.setWrapText(true);
        output.setStyle(textAreaCss());

        if (activityLogs.isEmpty()) {
            output.setText("No activities recorded yet.");
        } else {
            StringBuilder sb = new StringBuilder();
            for (int i = activityLogs.size() - 1; i >= 0; i--) sb.append(activityLogs.get(i)).append("\n");
            output.setText(sb.toString());
        }

        Button refresh = modernButton("Refresh", GREEN_PRIMARY, 140);
        refresh.setOnAction(e -> showActivityLogPanel());

        VBox card = createModernCard(title, refresh, output);
        VBox.setVgrow(output, Priority.ALWAYS);
        contentArea.getChildren().add(card);
    }

    private void showSettingsPanel() {
        contentArea.getChildren().clear();
        Label title = new Label("Settings");
        title.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: " + TEXT_PRIMARY + ";");

        VBox settingsCard = new VBox(16);
        settingsCard.setPadding(new Insets(24));
        settingsCard.setStyle("-fx-background-color: " + CARD_BG + "; -fx-background-radius: 16;");

        Label themeLabel = new Label("Theme: Dark Mode");
        themeLabel.setStyle("-fx-text-fill: " + TEXT_PRIMARY + ";");
        Label versionLabel = new Label("Sakay Cebu v2.0");
        versionLabel.setStyle("-fx-text-fill: " + TEXT_SECONDARY + ";");
        Label buildLabel = new Label("Build: January 2025 | OOP2 Final Project");
        buildLabel.setStyle("-fx-text-fill: " + TEXT_MUTED + ";");

        settingsCard.getChildren().addAll(themeLabel, versionLabel, buildLabel);
        contentArea.getChildren().add(createModernCard(title, settingsCard));
    }

    private void showAboutPanel() {
        contentArea.getChildren().clear();
        Label title = new Label("About");
        title.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: " + TEXT_PRIMARY + ";");

        VBox aboutCard = new VBox(16);
        aboutCard.setPadding(new Insets(24));
        aboutCard.setStyle("-fx-background-color: " + CARD_BG + "; -fx-background-radius: 16;");

        Label appName = new Label("SAKAY CEBU");
        appName.setFont(Font.font("System", FontWeight.BOLD, 28));
        appName.setStyle("-fx-text-fill: " + GREEN_PRIMARY + ";");

        Label description = new Label("Cebu's Public Vehicle Monitoring System\n\nReal-time tracking for jeepneys, modern jeeps, and buses across Cebu City.\n\nBuilt with JavaFX | OOP2 Final Project");
        description.setStyle("-fx-text-fill: " + TEXT_SECONDARY + "; -fx-wrap-text: true;");

        aboutCard.getChildren().addAll(appName, separator(), description);
        contentArea.getChildren().add(createModernCard(title, aboutCard));
    }

    private void showHelpPanel() {
        contentArea.getChildren().clear();
        Label title = new Label("Help Center");
        title.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: " + TEXT_PRIMARY + ";");

        VBox helpCard = new VBox(20);
        helpCard.setPadding(new Insets(24));
        helpCard.setStyle("-fx-background-color: " + CARD_BG + "; -fx-background-radius: 16;");

        Label guide = new Label("""
            OPERATOR GUIDE:
            1. Add routes, stops, and vehicles under Add Data
            2. Assign vehicles to routes using Assign Vehicle
            3. Send manual GPS pings via Send Ping
            4. Start Simulation for automatic movement
            5. Enable GPS Tracking Mode for real device location
            
            COMMUTER GUIDE:
            1. Use Route Status to see all vehicle information
            2. Use Search Route to find vehicles on specific routes
            3. Use Live Map to see real-time vehicle positions
            4. Use ETA Tracker for estimated arrival times
            """);
        guide.setStyle("-fx-text-fill: " + TEXT_SECONDARY + "; -fx-font-size: 13px;");

        helpCard.getChildren().addAll(guide);
        contentArea.getChildren().add(createModernCard(title, helpCard));
    }

    private void showTextPanel(String titleStr, String text) {
        contentArea.getChildren().clear();
        Label title = new Label(titleStr);
        title.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: " + TEXT_PRIMARY + ";");

        TextArea output = new TextArea(text);
        output.setEditable(false);
        output.setWrapText(false);
        output.setStyle(textAreaCss());

        VBox card = createModernCard(title, output);
        VBox.setVgrow(output, Priority.ALWAYS);
        contentArea.getChildren().add(card);
    }

    private String getRouteStopsText() {
        StringBuilder sb = new StringBuilder();
        for (Route r : monitoring.getRoutes()) {
            sb.append(r.routeId()).append(" – ").append(r.routeName()).append("\n");
            for (Stop s : r.stops()) {
                sb.append("  ● ").append(s.stopName()).append(" (").append(String.format("%.4f", s.lat())).append(", ").append(String.format("%.4f", s.lon())).append(")\n");
            }
            sb.append("\n");
        }
        return sb.length() == 0 ? "No routes available." : sb.toString();
    }

    private void startLiveSimulation() {
        if (liveSimulation != null) liveSimulation.stop();
        liveSimulation = new Timeline(new KeyFrame(Duration.seconds(2.5), e -> simulateVehiclePing()));
        liveSimulation.setCycleCount(Timeline.INDEFINITE);
        liveSimulation.play();
        addActivityLog("Live simulation started.");
        showWelcomePanel();
    }

    private void stopLiveSimulation() {
        if (liveSimulation != null) { liveSimulation.stop(); liveSimulation = null; }
        addActivityLog("Live simulation stopped.");
        showWelcomePanel();
    }

    private void simulateVehiclePing() {
        for (PublicVehicle vehicle : monitoring.getAllVehicles()) {
            if ("V-SHOWCASE".equals(vehicle.vehicleId()) && isRealGPSMode) continue;
            if (vehicle.routeId() == null) continue;
            
            List<double[]> path = routePaths.get(vehicle.routeId());
            if (path == null || path.isEmpty()) continue;
            
            int currentIndex = vehicleRouteIndexes.getOrDefault(vehicle.vehicleId(), 0);
            double[] point = path.get(currentIndex);
            
            monitoring.receivePing(new VehiclePing(vehicle.vehicleId(), Instant.now(), point[0], point[1], 20 + random.nextDouble() * 30, 5 + random.nextInt(vehicle.capacity() - 2)));
            
            currentIndex++;
            if (currentIndex >= path.size()) currentIndex = 0;
            vehicleRouteIndexes.put(vehicle.vehicleId(), currentIndex);
        }
        DataStore.saveAll(auth, monitoring, savedUsers);
        if (liveMapCanvas != null) refreshLiveMap();
    }

    private void seedData() {
        auth.addUser(new Operator("U-001", "John Dela Cruz", "operator", "1234"));
        auth.addUser(new Commuter("U-002", "Maria Santos", "commuter", "1234"));
        savedUsers.add(new DataStore.SavedUser("U-001", "John Dela Cruz", "operator", "1234", "Operator"));
        savedUsers.add(new DataStore.SavedUser("U-002", "Maria Santos", "commuter", "1234", "Commuter"));

        Route r04l = new Route("R-04L", "IT Park - Ayala - Fuente - Colon");
        r04l.addStop(new Stop("S-001", "IT Park", 10.3270, 123.9063));
        r04l.addStop(new Stop("S-002", "Ayala Center Cebu", 10.3187, 123.9056));
        r04l.addStop(new Stop("S-003", "Fuente Osmeña", 10.3090, 123.8929));
        r04l.addStop(new Stop("S-004", "Colon", 10.2965, 123.8988));
        monitoring.addRoute(r04l);

        monitoring.registerVehicle(new Jeepney("V-001", "ABC-123", 20));
        monitoring.assignVehicleToRoute("V-001", "R-04L");
    }

    private void ensureDefaultCebuRoutesAndVehicles() {
        if (!monitoring.routeExists("R-04L")) {
            Route r = new Route("R-04L", "IT Park - Ayala - Fuente - Colon");
            r.addStop(new Stop("S-001", "IT Park", 10.3270, 123.9063));
            r.addStop(new Stop("S-002", "Ayala Center Cebu", 10.3187, 123.9056));
            r.addStop(new Stop("S-003", "Fuente Osmeña", 10.3090, 123.8929));
            r.addStop(new Stop("S-004", "Colon", 10.2965, 123.8988));
            monitoring.addRoute(r);
        }
        if (!monitoring.routeExists("R-17B")) {
            Route r = new Route("R-17B", "CIT-U - Colon - Carbon");
            r.addStop(new Stop("S-101", "CIT-U", 10.2949, 123.8816));
            r.addStop(new Stop("S-102", "Pardo", 10.2837, 123.8679));
            r.addStop(new Stop("S-103", "Colon", 10.2965, 123.8988));
            r.addStop(new Stop("S-104", "Carbon Market", 10.2943, 123.9017));
            monitoring.addRoute(r);
        }
        if (!monitoring.routeExists("R-13C")) {
            Route r = new Route("R-13C", "Talamban - Banilad - Ayala");
            r.addStop(new Stop("S-201", "Talamban", 10.3697, 123.9141));
            r.addStop(new Stop("S-202", "Banilad", 10.3435, 123.9110));
            r.addStop(new Stop("S-203", "Country Mall", 10.3374, 123.9114));
            r.addStop(new Stop("S-204", "Ayala Center Cebu", 10.3187, 123.9056));
            monitoring.addRoute(r);
        }
        if (!monitoring.routeExists("R-12L")) {
            Route r = new Route("R-12L", "Labangon - Capitol - Ayala");
            r.addStop(new Stop("S-301", "Labangon", 10.2994, 123.8755));
            r.addStop(new Stop("S-302", "Capitol", 10.3140, 123.8919));
            r.addStop(new Stop("S-303", "Fuente Osmeña", 10.3090, 123.8929));
            r.addStop(new Stop("S-304", "Ayala Center Cebu", 10.3187, 123.9056));
            monitoring.addRoute(r);
        }

        if (!monitoring.vehicleExists("V-001")) { monitoring.registerVehicle(new Jeepney("V-001", "ABC-123", 20)); monitoring.assignVehicleToRoute("V-001", "R-04L"); }
        if (!monitoring.vehicleExists("V-002")) { monitoring.registerVehicle(new ModernJeep("V-002", "XYZ-456", 30)); monitoring.assignVehicleToRoute("V-002", "R-04L"); }
        if (!monitoring.vehicleExists("V-003")) { monitoring.registerVehicle(new Jeepney("V-003", "CIT-789", 20)); monitoring.assignVehicleToRoute("V-003", "R-17B"); }
        if (!monitoring.vehicleExists("V-004")) { monitoring.registerVehicle(new Bus("V-004", "BUS-321", 45)); monitoring.assignVehicleToRoute("V-004", "R-13C"); }
        if (!monitoring.vehicleExists("V-005")) { monitoring.registerVehicle(new Jeepney("V-005", "LAB-555", 20)); monitoring.assignVehicleToRoute("V-005", "R-12L"); }
    }

    // Sidebar Helpers
    private void sidebarSection(String text) {
        Label label = new Label(text);
        label.setStyle("-fx-text-fill: " + TEXT_MUTED + "; -fx-font-size: 10px; -fx-font-weight: bold; -fx-padding: 16 8 6 12;");
        sidebar.getChildren().add(label);
    }

    private void sidebarBtn(String text, Runnable action) {
        Button btn = new Button(text);
        btn.setMaxWidth(Double.MAX_VALUE);
        btn.setPrefHeight(42);
        btn.setAlignment(Pos.CENTER_LEFT);
        btn.setStyle("-fx-background-color: transparent; -fx-text-fill: " + TEXT_SECONDARY + "; -fx-font-size: 13px; -fx-background-radius: 10; -fx-padding: 0 12;");
        btn.setOnMouseEntered(e -> btn.setStyle("-fx-background-color: " + CARD_HOVER + "; -fx-text-fill: " + TEXT_PRIMARY + ";"));
        btn.setOnMouseExited(e -> btn.setStyle("-fx-background-color: transparent; -fx-text-fill: " + TEXT_SECONDARY + ";"));
        btn.setOnAction(e -> action.run());
        sidebar.getChildren().add(btn);
    }

    private void addLogoutButton() {
        javafx.scene.layout.Region spacer = new javafx.scene.layout.Region();
        VBox.setVgrow(spacer, Priority.ALWAYS);

        Button logout = new Button("Log Out");
        logout.setMaxWidth(Double.MAX_VALUE);
        logout.setPrefHeight(44);
        logout.setStyle("-fx-background-color: rgba(255,82,82,0.12); -fx-text-fill: " + ACCENT_RED + "; -fx-font-weight: bold; -fx-background-radius: 10;");
        logout.setOnAction(e -> {
            if (mapAutoRefresh != null) mapAutoRefresh.stop();
            if (liveSimulation != null) liveSimulation.stop();
            auth.logout();
            showLoginScreen();
        });
        sidebar.getChildren().addAll(spacer, logout);
    }

    // UI Component Factories
    // UI Component Factories
private ImageView loadLogo() {
    // Try multiple possible image formats
    InputStream stream = getClass().getResourceAsStream("/assets/logo.jpeg");
    
    if (stream == null) {
        stream = getClass().getResourceAsStream("/assets/logo.png");
    }
    if (stream == null) {
        stream = getClass().getResourceAsStream("/assets/logo.jpg");
    }
    if (stream == null) {
        stream = getClass().getResourceAsStream("/assets/logo.gif");
    }

    if (stream == null) {
        System.out.println("❌ Logo not found! Please check if the image is in: src/assets/logo.jpeg");
        return new ImageView(); // fallback empty image
    }

    Image logo = new Image(stream, 90, 90, true, true); // smoother scaling
    ImageView logoView = new ImageView(logo);
    
    logoView.setFitWidth(90);
    logoView.setFitHeight(90);
    logoView.setPreserveRatio(true);

    // Make it circular (nice look)
    Circle clip = new Circle(45, 45, 45);
    logoView.setClip(clip);

    // Optional: Add subtle shadow
    DropShadow shadow = new DropShadow();
    shadow.setRadius(8);
    shadow.setColor(Color.rgb(0, 0, 0, 0.4));
    logoView.setEffect(shadow);

    return logoView;
}

    private TextField styledInput(String prompt, double maxWidth) {
        TextField field = new TextField();
        field.setPromptText(prompt);
        field.setMaxWidth(maxWidth);
        field.setPrefHeight(48);
        field.setStyle(inputCss());
        return field;
    }

    private PasswordField pwField(String prompt) {
        PasswordField f = new PasswordField();
        f.setPromptText(prompt);
        f.setMaxWidth(360);
        f.setPrefHeight(48);
        f.setStyle(inputCss());
        return f;
    }

    private Button modernButton(String text, String color, double minWidth) {
        Button btn = new Button(text);
        btn.setPrefHeight(46);
        btn.setMinWidth(minWidth);
        btn.setStyle("-fx-background-color: " + color + "; -fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 14px; -fx-background-radius: 12;");
        btn.setOnMouseEntered(e -> btn.setStyle("-fx-background-color: " + (color.equals(GREEN_PRIMARY) ? GREEN_DARK : color) + "; -fx-text-fill: white;"));
        btn.setOnMouseExited(e -> btn.setStyle("-fx-background-color: " + color + "; -fx-text-fill: white;"));
        return btn;
    }

    private Button outlineButton(String text, double minWidth) {
        Button btn = new Button(text);
        btn.setPrefHeight(46);
        btn.setMinWidth(minWidth);
        btn.setStyle("-fx-background-color: transparent; -fx-text-fill: " + TEXT_SECONDARY + "; -fx-font-weight: bold; -fx-border-color: " + BORDER_COLOR + "; -fx-border-radius: 12;");
        return btn;
    }

    private VBox createModernCard(javafx.scene.Node... nodes) {
        VBox box = new VBox(16);
        box.getChildren().addAll(nodes);
        box.setPadding(new Insets(24));
        box.setStyle("-fx-background-color: " + CARD_BG + "; -fx-background-radius: 20; -fx-border-color: " + BORDER_COLOR + "; -fx-border-radius: 20;");
        return box;
    }

    private javafx.scene.layout.Region separator() {
    javafx.scene.layout.Region r = new javafx.scene.layout.Region();
    r.setPrefHeight(1);
    r.setStyle("-fx-background-color: " + BORDER_COLOR + ";");
    return r;
}

    private void styleTable(TableView table) {
    table.setStyle("-fx-background-color: " + CARD_BG + "; -fx-control-inner-background: " + CARD_BG + "; -fx-border-color: " + BORDER_COLOR + ";");
}

    private <T> void styleComboBox(ComboBox<T> cb) {
        cb.setStyle(inputCss());
        cb.setButtonCell(new ListCell<T>() {
            @Override
            protected void updateItem(T item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? cb.getPromptText() : item.toString());
            }
        });
    }

    private String inputCss() {
        return "-fx-background-color: " + CARD_HOVER + "; -fx-text-fill: " + TEXT_PRIMARY + "; -fx-prompt-text-fill: " + TEXT_MUTED + "; -fx-background-radius: 12; -fx-border-color: " + BORDER_COLOR + ";";
    }

    private String textAreaCss() {
        return "-fx-control-inner-background: " + CARD_HOVER + "; -fx-text-fill: " + TEXT_PRIMARY + "; -fx-border-color: " + BORDER_COLOR + ";";
    }

    private String cardStyle() {
        return "-fx-background-color: " + CARD_BG + "; -fx-background-radius: 24; -fx-border-color: " + BORDER_COLOR + "; -fx-border-radius: 24;";
    }

    private void showSuccess(Label label, String text) {
        label.setStyle("-fx-text-fill: " + GREEN_PRIMARY + ";");
        label.setText(text);
    }

    private void showError(Label label, String text) {
        label.setStyle("-fx-text-fill: " + ACCENT_RED + ";");
        label.setText(text);
    }

    private void hideNodes(javafx.scene.Node... nodes) {
        for (javafx.scene.Node n : nodes) { n.setVisible(false); n.setManaged(false); }
    }

    private void showNodes(javafx.scene.Node... nodes) {
        for (javafx.scene.Node n : nodes) { n.setVisible(true); n.setManaged(true); }
    }

    private void addActivityLog(String action) {
        String user = currentUser != null ? currentUser.name() : "System";
        activityLogs.add("[" + timeFormatter.format(Instant.now()) + "] " + user + " – " + action);
    }

    private void fadeIn(javafx.scene.Node node) {
        FadeTransition ft = new FadeTransition(Duration.millis(400), node);
        ft.setFromValue(0);
        ft.setToValue(1);
        ft.play();
    }

    private void updateSavedUserRecord(String userId, String newName, String newUsername, String newPassword) {
        for (int i = 0; i < savedUsers.size(); i++) {
            DataStore.SavedUser s = savedUsers.get(i);
            if (s.userId().equals(userId)) {
                savedUsers.set(i, new DataStore.SavedUser(s.userId(), newName != null ? newName : s.name(), newUsername != null ? newUsername : s.username(), newPassword != null ? newPassword : s.password(), s.role()));
                return;
            }
        }
    }

    @Override
    public void stop() {
        if (mapAutoRefresh != null) mapAutoRefresh.stop();
        if (liveSimulation != null) liveSimulation.stop();
    }

    public static void main(String[] args) {
        launch(args);
    }
}