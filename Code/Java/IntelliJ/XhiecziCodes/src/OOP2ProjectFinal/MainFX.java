package OOP2ProjectFinal;

import javafx.animation.FadeTransition;
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
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
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

    // ─── Brand palette (Grab-inspired) ──────────────────────────────────────
    private static final String GREEN        = "#00B14F";
    private static final String GREEN_DARK   = "#009640";
    private static final String GREEN_GLOW   = "rgba(0,177,79,0.18)";
    private static final String BG_DARKEST   = "#0A0F0D";
    private static final String BG_DARK      = "#111714";
    private static final String BG_CARD      = "#161D19";
    private static final String BG_CARD2     = "#1C2620";
    private static final String BORDER       = "#243028";
    private static final String TEXT_PRIMARY = "#F0FDF4";
    private static final String TEXT_MUTED   = "#7EA891";
    private static final String TEXT_DIM     = "#3D5448";
    private static final String ACCENT_RED   = "#EF4444";
    private static final String ACCENT_AMBER = "#F59E0B";
    private static final String ACCENT_BLUE  = "#3B82F6";

    // ─── State ───────────────────────────────────────────────────────────────
    private final List<DataStore.SavedUser> savedUsers = new ArrayList<>();
    private final List<String>              activityLogs = new ArrayList<>();
    private final AuthenticationService     auth = new AuthenticationService();
    private final MonitoringService         monitoring = new MonitoringService();
    private final Random                    random = new Random();
    private final DateTimeFormatter         timeFormatter =
            DateTimeFormatter.ofPattern("HH:mm:ss dd/MM/yy").withZone(ZoneId.systemDefault());

    private User     currentUser;
    private Timeline liveSimulation;
    private Stage    mainStage;

    // Dashboard widgets
    private BorderPane dashboardRoot;
    private VBox       sidebar;
    private VBox       contentArea;
    private Label      headerTitle;
    private Label      headerUser;
    private VBox       headerBox;

    // ═══════════════════════════════════════════════════════════════════════
    //  LIFECYCLE
    // ═══════════════════════════════════════════════════════════════════════

    @Override
    public void start(Stage stage) {
        mainStage = stage;
        if (DataStore.dataExists()) {
            DataStore.loadAll(auth, monitoring, savedUsers);
            ensureDefaultAccounts();
            DataStore.saveAll(auth, monitoring, savedUsers);
        } else {
            seedData();
            DataStore.saveAll(auth, monitoring, savedUsers);
        }
        ensureDefaultCebuRoutesAndVehicles();
        DataStore.saveAll(auth, monitoring, savedUsers);
        showLoginScreen();
    }

    private void ensureDefaultAccounts() {
        if (!auth.usernameExists("operator")) {
            auth.addUser(new Operator("U-001", "Default Operator", "operator", "1234"));
            savedUsers.add(new DataStore.SavedUser("U-001", "Default Operator", "operator", "1234", "Operator"));
        }
        if (!auth.usernameExists("commuter")) {
            auth.addUser(new Commuter("U-002", "Default Commuter", "commuter", "1234"));
            savedUsers.add(new DataStore.SavedUser("U-002", "Default Commuter", "commuter", "1234", "Commuter"));
        }
    }

    // ═══════════════════════════════════════════════════════════════════════
    //  LOGIN SCREEN
    // ═══════════════════════════════════════════════════════════════════════

    private void showLoginScreen() {
        // ── Logo ──
        ImageView logoView = loadLogo();

        // ── App name badge ──
        Label appName = new Label("SAKAY CEBU");
        appName.setStyle(
                "-fx-font-size: 28px;" +
                "-fx-font-weight: bold;" +
                "-fx-text-fill: " + GREEN + ";" +
                "-fx-font-family: 'Arial Black';"
        );
        Label tagline = new Label("Public Vehicle Monitoring System");
        tagline.setStyle("-fx-font-size: 13px; -fx-text-fill: " + TEXT_MUTED + ";");

        // ── Fields ──
        TextField usernameField = styledInput("Username", 360);
        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("Password");
        passwordField.setMaxWidth(360);
        passwordField.setPrefHeight(48);
        passwordField.setStyle(inputCss());

        Label errorLabel = new Label();
        errorLabel.setStyle("-fx-text-fill: " + ACCENT_RED + "; -fx-font-size: 13px;");

        // ── Buttons ──
        Button loginBtn = greenButton("Log In", 360);
        Button registerBtn = ghostButton("Create Account", 360);

        loginBtn.setOnAction(e -> {
            User user = auth.login(usernameField.getText().trim(), passwordField.getText().trim());
            if (user == null) { errorLabel.setText("Invalid username or password."); return; }
            currentUser = user;
            if (user instanceof Operator) showOperatorDashboard(user);
            else                          showCommuterDashboard(user);
        });
        registerBtn.setOnAction(e -> showRegisterScreen());

        // Allow pressing Enter
        passwordField.setOnAction(e -> loginBtn.fire());
        usernameField.setOnAction(e -> passwordField.requestFocus());

        Label hint = new Label("Demo:  operator / 1234   or   commuter / 1234");
        hint.setStyle("-fx-font-size: 11px; -fx-text-fill: " + TEXT_DIM + ";");

        // ── Card ──
        VBox card = new VBox(14, logoView, appName, tagline,
                             sep(), usernameField, passwordField,
                             loginBtn, registerBtn, errorLabel, hint);
        card.setAlignment(Pos.CENTER);
        card.setPadding(new Insets(44, 48, 44, 48));
        card.setMaxWidth(460);
        card.setStyle(
                "-fx-background-color: " + BG_CARD + ";" +
                "-fx-background-radius: 20;" +
                "-fx-border-color: " + BORDER + ";" +
                "-fx-border-radius: 20;" +
                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.7), 48, 0, 0, 16);"
        );

        // ── Root with map-like dark background ──
        StackPane root = new StackPane(card);
        root.setStyle("-fx-background-color: radial-gradient(center 40% 20%, radius 70%, #0D2018, " + BG_DARKEST + ");");

        fadeIn(card);

        mainStage.setTitle("Sakay Cebu – Vehicle Monitoring");
        mainStage.setScene(new Scene(root, 1200, 760));
        mainStage.show();
    }

    // ═══════════════════════════════════════════════════════════════════════
    //  REGISTER SCREEN
    // ═══════════════════════════════════════════════════════════════════════

    private void showRegisterScreen() {
        Label title = new Label("Create Account");
        title.setStyle("-fx-font-size: 22px; -fx-font-weight: bold; -fx-text-fill: " + TEXT_PRIMARY + ";");

        TextField userIdField   = styledInput("User ID", 360);
        TextField nameField     = styledInput("Full Name", 360);
        TextField usernameField = styledInput("Username", 360);

        PasswordField passField    = pwField("Password");
        PasswordField confirmField = pwField("Confirm Password");

        ComboBox<String> roleBox = new ComboBox<>();
        roleBox.getItems().addAll("Commuter", "Operator");
        roleBox.setPromptText("Select Role");
        roleBox.setMaxWidth(360);
        roleBox.setPrefHeight(48);
        styleComboBox(roleBox);

        Label msg = new Label();

        Button createBtn = greenButton("Create Account", 360);
        Button backBtn   = ghostButton("Back to Login", 360);

        createBtn.setOnAction(e -> {
            String uid  = userIdField.getText().trim();
            String name = nameField.getText().trim();
            String user = usernameField.getText().trim();
            String pass = passField.getText().trim();
            String conf = confirmField.getText().trim();
            String role = roleBox.getValue();

            if (uid.isEmpty()||name.isEmpty()||user.isEmpty()||pass.isEmpty()||role==null) {
                showError(msg, "Please fill in all fields."); return;
            }
            if (auth.userIdExists(uid))     { showError(msg, "User ID already exists."); return; }
            if (auth.usernameExists(user))  { showError(msg, "Username already taken."); return; }
            if (pass.length() < 4)          { showError(msg, "Password must be ≥ 4 chars."); return; }
            if (!pass.equals(conf))         { showError(msg, "Passwords do not match."); return; }

            User newUser = role.equals("Operator")
                    ? new Operator(uid, name, user, pass)
                    : new Commuter(uid, name, user, pass);
            auth.addUser(newUser);
            savedUsers.add(new DataStore.SavedUser(uid, name, user, pass, role));
            DataStore.saveAll(auth, monitoring, savedUsers);
            showSuccess(msg, "Account created! You can now log in.");
        });

        backBtn.setOnAction(e -> showLoginScreen());

        VBox card = new VBox(12, title, sep(),
                             userIdField, nameField, usernameField,
                             passField, confirmField, roleBox,
                             createBtn, backBtn, msg);
        card.setAlignment(Pos.CENTER);
        card.setPadding(new Insets(40, 48, 40, 48));
        card.setMaxWidth(460);
        card.setStyle(
                "-fx-background-color: " + BG_CARD + ";" +
                "-fx-background-radius: 20;" +
                "-fx-border-color: " + BORDER + ";" +
                "-fx-border-radius: 20;" +
                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.7), 48, 0, 0, 16);"
        );

        StackPane root = new StackPane(card);
        root.setStyle("-fx-background-color: radial-gradient(center 40% 20%, radius 70%, #0D2018, " + BG_DARKEST + ");");
        fadeIn(card);
        mainStage.setScene(new Scene(root, 1200, 760));
    }

    // ═══════════════════════════════════════════════════════════════════════
    //  DASHBOARD BUILDER
    // ═══════════════════════════════════════════════════════════════════════

    private void showOperatorDashboard(User user) {
        buildDashboard("Operator", user.name());

        sidebarSection("OVERVIEW");
        sidebarBtn("🏠  Home",             () -> showWelcomePanel(user));
        sidebarBtn("📊  Route Dashboard",  this::showRouteDashboardTable);
        sidebarBtn("🗺  Live Map",          this::showLiveMapPanel);

        sidebarSection("MANAGEMENT");
        sidebarBtn("➕  Add Data",          this::showAddPanel);
        sidebarBtn("🚐  Vehicles",          this::showVehiclesTable);
        sidebarBtn("🛣  Assign Vehicle",    this::showAssignVehiclePanel);
        sidebarBtn("🗑  Remove Data",       this::showRemovePanel);

        sidebarSection("MONITORING");
        sidebarBtn("📡  Send Ping",         this::showSendPingPanel);
        sidebarBtn("▶   Start Simulation", this::startLiveSimulation);
        sidebarBtn("■   Stop Simulation",  this::stopLiveSimulation);
        sidebarBtn("🚨  Alerts",            this::showAlertsTable);
        sidebarBtn("🔔  Notifications",     this::showNotificationCenterPanel);
        sidebarBtn("🧾  Activity Log",      this::showActivityLogPanel);

        sidebarSection("ACCOUNT");
        sidebarBtn("👤  Profile",           this::showProfilePanel);
        sidebarBtn("⚙   Settings",          this::showSettingsPanel);
        sidebarBtn("ℹ   About",             this::showAboutPanel);
        sidebarBtn("❔  Help",              this::showHelpPanel);
        addLogoutButton();

        showWelcomePanel(user);
    }

    private void showCommuterDashboard(User user) {
        buildDashboard("Commuter", user.name());

        sidebarSection("OVERVIEW");
        sidebarBtn("🏠  Home",             () -> showWelcomePanel(user));
        sidebarBtn("📊  Route Status",     this::showRouteDashboardTable);
        sidebarBtn("🗺  Live Map",          this::showLiveMapPanel);
        sidebarBtn("⏱  ETA Tracker",       this::showEtaTrackerPanel);

        sidebarSection("ROUTES");
        sidebarBtn("🔎  Search Route",     this::showSearchByRoutePanel);
        sidebarBtn("📌  Route Stops",      () -> showTextPanel("Route Stops", getRouteStopsText()));
        sidebarBtn("🚨  Recent Alerts",    this::showAlertsTable);
        sidebarBtn("🔔  Notifications",    this::showNotificationCenterPanel);

        sidebarSection("ACCOUNT");
        sidebarBtn("👤  Profile",          this::showProfilePanel);
        sidebarBtn("⚙   Settings",         this::showSettingsPanel);
        sidebarBtn("ℹ   About",            this::showAboutPanel);
        sidebarBtn("❔  Help",             this::showHelpPanel);
        addLogoutButton();

        showWelcomePanel(user);
    }

    private void buildDashboard(String roleLabel, String username) {
        dashboardRoot = new BorderPane();
        dashboardRoot.setStyle("-fx-background-color: " + BG_DARKEST + ";");

        // ── Header ──
        Label logo = new Label("SAKAY CEBU");
        logo.setStyle(
                "-fx-font-size: 18px; -fx-font-weight: bold;" +
                "-fx-text-fill: " + GREEN + ";" +
                "-fx-font-family: 'Arial Black';"
        );
        Label roleBadge = new Label(roleLabel.toUpperCase());
        roleBadge.setStyle(
                "-fx-background-color: " + GREEN_GLOW + ";" +
                "-fx-text-fill: " + GREEN + ";" +
                "-fx-font-size: 10px;" +
                "-fx-font-weight: bold;" +
                "-fx-padding: 3 10 3 10;" +
                "-fx-background-radius: 20;"
        );
        headerTitle = new Label(username);
        headerTitle.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: " + TEXT_PRIMARY + ";");

        Circle avatar = new Circle(18, Color.web(GREEN));
        Label avatarInit = new Label(username.substring(0, 1).toUpperCase());
        avatarInit.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: white;");
        StackPane avatarPane = new StackPane(avatar, avatarInit);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        HBox headerContent = new HBox(14,
                logo, roleBadge, spacer, headerTitle, avatarPane);
        headerContent.setAlignment(Pos.CENTER_LEFT);

        headerBox = new VBox(headerContent);
        headerBox.setPadding(new Insets(16, 22, 16, 22));
        headerBox.setStyle(
                "-fx-background-color: " + BG_DARK + ";" +
                "-fx-border-color: " + BORDER + ";" +
                "-fx-border-width: 0 0 1 0;"
        );
        dashboardRoot.setTop(headerBox);

        // ── Sidebar ──
        sidebar = new VBox(4);
        sidebar.setPadding(new Insets(16, 12, 16, 12));
        sidebar.setPrefWidth(220);
        sidebar.setStyle(
                "-fx-background-color: " + BG_DARK + ";" +
                "-fx-border-color: " + BORDER + ";" +
                "-fx-border-width: 0 1 0 0;"
        );
        ScrollPane sidebarScroll = new ScrollPane(sidebar);
        sidebarScroll.setFitToWidth(true);
        sidebarScroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        sidebarScroll.setStyle("-fx-background: transparent; -fx-background-color: transparent;");
        dashboardRoot.setLeft(sidebarScroll);

        // ── Content ──
        contentArea = new VBox(18);
        contentArea.setPadding(new Insets(24));
        contentArea.setStyle("-fx-background-color: " + BG_DARKEST + ";");
        ScrollPane contentScroll = new ScrollPane(contentArea);
        contentScroll.setFitToWidth(true);
        contentScroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        contentScroll.setStyle("-fx-background: transparent; -fx-background-color: transparent;");
        dashboardRoot.setCenter(contentScroll);

        mainStage.setScene(new Scene(dashboardRoot, 1200, 760));
    }

    // ═══════════════════════════════════════════════════════════════════════
    //  PANELS
    // ═══════════════════════════════════════════════════════════════════════

    private void showWelcomePanel(User user) {
        contentArea.getChildren().clear();

        Label greeting = new Label("Good day, " + user.name() + " 👋");
        greeting.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: " + TEXT_PRIMARY + ";");

        Label sub = new Label("Here's the latest transport snapshot for Cebu City.");
        sub.setStyle("-fx-font-size: 14px; -fx-text-fill: " + TEXT_MUTED + ";");

        HBox stats = new HBox(16,
                statCard("🛣  Routes",   String.valueOf(monitoring.getRoutes().size()),   ACCENT_BLUE),
                statCard("🚌 Vehicles",  String.valueOf(monitoring.getAllVehicles().size()), GREEN),
                statCard("🚨 Alerts",    String.valueOf(monitoring.getAlerts().size()),    ACCENT_RED)
        );

        // Live-ish status strip
        Label statusLabel = new Label("● Simulation " + (liveSimulation != null ? "RUNNING" : "IDLE"));
        statusLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: " + (liveSimulation != null ? GREEN : TEXT_MUTED) + "; -fx-font-weight: bold;");

        VBox top = new VBox(8, greeting, sub, statusLabel);
        top.setPadding(new Insets(24, 26, 24, 26));
        top.setStyle(
                "-fx-background-color: " + BG_CARD + ";" +
                "-fx-background-radius: 14;" +
                "-fx-border-color: " + BORDER + ";" +
                "-fx-border-radius: 14;"
        );

        contentArea.getChildren().addAll(top, stats);
    }

    private VBox statCard(String label, String value, String accent) {
        Label valueLabel = new Label(value);
        valueLabel.setStyle("-fx-font-size: 34px; -fx-font-weight: bold; -fx-text-fill: " + accent + ";");

        Label labelText = new Label(label);
        labelText.setStyle("-fx-font-size: 13px; -fx-text-fill: " + TEXT_MUTED + ";");

        VBox box = new VBox(6, valueLabel, labelText);
        box.setPadding(new Insets(22, 26, 22, 26));
        box.setMinWidth(160);
        box.setStyle(
                "-fx-background-color: " + BG_CARD + ";" +
                "-fx-background-radius: 14;" +
                "-fx-border-color: " + BORDER + ";" +
                "-fx-border-radius: 14;"
        );
        HBox.setHgrow(box, Priority.ALWAYS);
        return box;
    }

    // ── LIVE MAP ─────────────────────────────────────────────────────────────

    private void showLiveMapPanel() {
        contentArea.getChildren().clear();

        Label title = panelTitle("Live Vehicle Map");

        Label note = new Label("Showing all vehicles with known GPS positions · Cebu City");
        note.setStyle("-fx-font-size: 12px; -fx-text-fill: " + TEXT_MUTED + ";");

        // Filter by route
        ComboBox<String> routeFilter = new ComboBox<>();
        routeFilter.getItems().add("All Routes");
        monitoring.getRoutes().forEach(r -> routeFilter.getItems().add(r.routeId() + " – " + r.routeName()));
        routeFilter.setValue("All Routes");
        routeFilter.setMaxWidth(320);
        routeFilter.setPrefHeight(44);
        styleComboBox(routeFilter);

        Button refreshBtn = greenButton("Refresh Map", 160);
        Label  message    = new Label();

        WebView webView = new WebView();
        webView.setPrefSize(900, 500);
        webView.setMinHeight(500);

        Runnable loadMap = () -> {
            String filter = routeFilter.getValue();
            String filterRouteId = "All Routes".equals(filter) ? null
                    : filter.split("–")[0].trim();

            StringBuilder vehicleMarkers = new StringBuilder();
            StringBuilder routePolylines = new StringBuilder();

            // Draw route stop polylines
            for (Route route : monitoring.getRoutes()) {
                if (filterRouteId != null && !route.routeId().equalsIgnoreCase(filterRouteId)) continue;
                if (route.stops().size() < 2) continue;

                vehicleMarkers.append("// stops for ").append(route.routeId()).append("\n");
                for (Stop stop : route.stops()) {
                    vehicleMarkers.append(String.format(
                            "L.circleMarker([%f,%f],{radius:5,color:'#00B14F',fillColor:'#00B14F',fillOpacity:0.8})" +
                            ".addTo(map).bindTooltip('%s');%n",
                            stop.lat(), stop.lon(), stop.stopName().replace("'", "\\'")
                    ));
                }

                // polyline
                StringBuilder latlngs = new StringBuilder("[");
                for (Stop stop : route.stops()) {
                    latlngs.append(String.format("[%f,%f],", stop.lat(), stop.lon()));
                }
                latlngs.deleteCharAt(latlngs.length()-1).append("]");
                routePolylines.append(String.format(
                        "L.polyline(%s,{color:'#00B14F',weight:3,opacity:0.45,dashArray:'6,8'}).addTo(map);%n",
                        latlngs
                ));
            }

            // Vehicle markers
            int vehicleCount = 0;
            for (PublicVehicle v : monitoring.getAllVehicles()) {
                if (!v.hasLocation()) continue;
                if (filterRouteId != null && !filterRouteId.equalsIgnoreCase(v.routeId())) continue;

                String type  = v.getVehicleType();
                String emoji = type.contains("Bus") ? "🚌" : type.contains("Modern") ? "🚎" : "🚐";
                String color = type.contains("Bus") ? "#3B82F6" : type.contains("Modern") ? "#F59E0B" : "#00B14F";

                int pct = v.capacity() > 0 ? (int)((double)v.passengerCount()/v.capacity()*100) : 0;
                String status = pct >= 90 ? "FULL" : pct >= 60 ? "BUSY" : "OK";

                vehicleMarkers.append(String.format(
                        "L.marker([%f,%f],{icon:L.divIcon({" +
                        "html:'<div style=\"background:%s;width:44px;height:44px;border-radius:50%%;display:flex;" +
                              "align-items:center;justify-content:center;font-size:20px;" +
                              "border:3px solid rgba(255,255,255,0.25);box-shadow:0 4px 16px rgba(0,0,0,0.5);\">" +
                              "%s</div>'," +
                        "className:'',iconSize:[44,44],iconAnchor:[22,22]})}).addTo(map)" +
                        ".bindPopup('<b>%s %s</b><br>Plate: %s<br>Route: %s<br>Speed: %.1f km/h<br>Passengers: %d/%d<br>Status: %s');%n",
                        v.lat(), v.lon(),
                        color, emoji,
                        emoji, v.vehicleId(),
                        v.plateNumber(),
                        v.routeId() != null ? v.routeId() : "Unassigned",
                        v.speedKmh(),
                        v.passengerCount(), v.capacity(),
                        status
                ));
                vehicleCount++;
            }

            if (vehicleCount == 0 && filterRouteId == null) {
                showError(message, "No vehicles have GPS data yet. Start the simulation first.");
                return;
            }

            message.setText("");
            double centerLat = 10.3157, centerLon = 123.8854;

            String html = """
            <!DOCTYPE html>
            <html>
            <head>
                <meta charset="utf-8">
                <link rel="stylesheet" href="https://unpkg.com/leaflet@1.9.4/dist/leaflet.css">
                <script src="https://unpkg.com/leaflet@1.9.4/dist/leaflet.js"></script>
                <style>
                    html,body,#map{width:100%%;height:100%%;margin:0;padding:0;background:#111714;}
                    .leaflet-popup-content-wrapper{background:#1C2620;color:#F0FDF4;border:1px solid #243028;border-radius:12px;}
                    .leaflet-popup-tip{background:#1C2620;}
                    .leaflet-popup-content b{color:#00B14F;}
                    .legend{position:absolute;bottom:24px;left:14px;background:rgba(17,23,20,0.92);
                             color:#7EA891;padding:10px 14px;border-radius:10px;font:12px/1.6 sans-serif;
                             border:1px solid #243028;z-index:1000;}
                </style>
            </head>
            <body>
                <div id="map"></div>
                <div class="legend">
                    🚐 Jeepney &nbsp; 🚎 Modern Jeep &nbsp; 🚌 Bus<br>
                    ─ Route path &nbsp; ● Stop
                </div>
                <script>
                    const map = L.map('map',{zoomControl:true,attributionControl:false}).setView([%f,%f],14);
                    L.tileLayer('https://{s}.basemaps.cartocdn.com/dark_all/{z}/{x}/{y}{r}.png',{
                        subdomains:'abcd',maxZoom:19
                    }).addTo(map);
                    %s
                    %s
                    setTimeout(()=>map.invalidateSize(true),600);
                </script>
            </body>
            </html>
            """.formatted(centerLat, centerLon, routePolylines, vehicleMarkers);

            webView.getEngine().loadContent(html);
        };

        refreshBtn.setOnAction(e -> loadMap.run());

        // Auto-load on open
        loadMap.run();

        HBox controls = new HBox(12, routeFilter, refreshBtn, message);
        controls.setAlignment(Pos.CENTER_LEFT);

        VBox card = card(title, note, controls, webView);
        VBox.setVgrow(webView, Priority.ALWAYS);
        VBox.setVgrow(card, Priority.ALWAYS);
        contentArea.getChildren().add(card);
    }

    // ── ETA TRACKER ──────────────────────────────────────────────────────────

    private void showEtaTrackerPanel() {
        contentArea.getChildren().clear();

        Label title = panelTitle("ETA Tracker");

        ComboBox<Route> routeBox = new ComboBox<>();
        routeBox.getItems().addAll(monitoring.getRoutes());
        routeBox.setPromptText("Select Route");
        routeBox.setMaxWidth(360);
        routeBox.setPrefHeight(44);
        styleComboBox(routeBox);

        Button calc = greenButton("Calculate ETA", 200);
        Label  msg  = new Label();

        VBox resultsBox = new VBox(10);

        calc.setOnAction(e -> {
            resultsBox.getChildren().clear();
            Route route = routeBox.getValue();
            if (route == null) { showError(msg, "Select a route."); return; }
            msg.setText("");

            List<PublicVehicle> vehicles = monitoring.getVehiclesOnRoute(route.routeId());
            if (vehicles.isEmpty()) {
                showError(msg, "No vehicles found on this route."); return;
            }

            for (PublicVehicle v : vehicles) {
                double speed = v.speedKmh() > 0 ? v.speedKmh() : 20;
                String nearestStop = "N/A";
                double minDist = Double.MAX_VALUE;

                if (v.hasLocation()) {
                    for (Stop stop : route.stops()) {
                        double dist = haversine(v.lat(), v.lon(), stop.lat(), stop.lon());
                        if (dist < minDist) { minDist = dist; nearestStop = stop.stopName(); }
                    }
                }

                String eta = v.hasLocation()
                        ? String.format("~%.0f min", (minDist / speed) * 60)
                        : "No GPS yet";

                int pct = v.capacity() > 0 ? (int)((double)v.passengerCount()/v.capacity()*100) : 0;
                String capColor = pct >= 90 ? ACCENT_RED : pct >= 60 ? ACCENT_AMBER : GREEN;

                HBox row = new HBox(16);
                row.setPadding(new Insets(14, 18, 14, 18));
                row.setStyle(
                        "-fx-background-color: " + BG_CARD2 + ";" +
                        "-fx-background-radius: 10;" +
                        "-fx-border-color: " + BORDER + ";" +
                        "-fx-border-radius: 10;"
                );
                Label vId    = styledLabel(v.getVehicleType() + " " + v.vehicleId(), TEXT_PRIMARY, 13, true);
                Label vSpeed = styledLabel("Speed: " + String.format("%.0f km/h", v.speedKmh()), TEXT_MUTED, 12, false);
                Label vStop  = styledLabel("Near: " + nearestStop, TEXT_MUTED, 12, false);
                Label vEta   = styledLabel("ETA: " + eta, GREEN, 13, true);
                Label vCap   = styledLabel("Passengers: " + v.passengerCount() + "/" + v.capacity(), capColor, 12, false);

                VBox info = new VBox(3, vId, vSpeed, vStop, vCap);
                Region sp = new Region(); HBox.setHgrow(sp, Priority.ALWAYS);
                row.getChildren().addAll(info, sp, vEta);
                row.setAlignment(Pos.CENTER_LEFT);
                resultsBox.getChildren().add(row);
            }
        });

        contentArea.getChildren().add(card(title, routeBox, calc, msg, resultsBox));
    }

    // ── SEARCH BY ROUTE ──────────────────────────────────────────────────────

    private void showSearchByRoutePanel() {
        contentArea.getChildren().clear();

        Label title = panelTitle("Search by Route");

        ComboBox<Route> routeBox = new ComboBox<>();
        routeBox.getItems().addAll(monitoring.getRoutes());
        routeBox.setPromptText("Select Route");
        routeBox.setMaxWidth(360);
        routeBox.setPrefHeight(44);
        styleComboBox(routeBox);

        Button searchBtn = greenButton("Search", 160);
        Label  msg       = new Label();

        TableView<PublicVehicle> table = new TableView<>();
        styleTable(table);

        TableColumn<PublicVehicle, String> idCol   = col("Vehicle ID",   v -> v.vehicleId());
        TableColumn<PublicVehicle, String> typeCol = col("Type",         v -> v.getVehicleType());
        TableColumn<PublicVehicle, String> plateCol= col("Plate",        v -> v.plateNumber());
        TableColumn<PublicVehicle, String> speedCol= col("Speed",        v -> String.format("%.0f km/h", v.speedKmh()));
        TableColumn<PublicVehicle, String> capCol  = col("Passengers",   v -> v.passengerCount() + "/" + v.capacity());
        TableColumn<PublicVehicle, String> statCol = col("Status",       v -> monitoring.getVehicleStatus(v));
        table.getColumns().addAll(idCol, typeCol, plateCol, speedCol, capCol, statCol);
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        searchBtn.setOnAction(e -> {
            Route r = routeBox.getValue();
            if (r == null) { showError(msg, "Select a route."); return; }
            List<PublicVehicle> found = monitoring.getVehiclesOnRoute(r.routeId());
            if (found.isEmpty()) { showError(msg, "No vehicles on this route."); return; }
            msg.setText("");
            table.setItems(FXCollections.observableArrayList(found));
        });

        VBox box = card(title, routeBox, searchBtn, msg, table);
        VBox.setVgrow(table, Priority.ALWAYS);
        VBox.setVgrow(box, Priority.ALWAYS);
        contentArea.getChildren().add(box);
    }

    // ── ROUTE DASHBOARD TABLE ─────────────────────────────────────────────────

    private void showRouteDashboardTable() {
        contentArea.getChildren().clear();

        Label title = panelTitle("Route Dashboard");

        TableView<DashboardRow> table = new TableView<>();
        table.setItems(FXCollections.observableArrayList(getDashboardRows()));
        styleTable(table);

        TableColumn<DashboardRow, String> vehicleCol  = col("Vehicle",      r -> r.vehicleId());
        TableColumn<DashboardRow, String> typeCol     = col("Type",         r -> r.type());
        TableColumn<DashboardRow, String> routeCol    = col("Route",        r -> r.route());
        TableColumn<DashboardRow, String> speedCol    = col("Speed",        r -> r.speed());
        TableColumn<DashboardRow, String> passCol     = col("Passengers",   r -> r.passengers());
        TableColumn<DashboardRow, String> statusCol   = col("Status",       r -> r.status());
        TableColumn<DashboardRow, String> stopCol     = col("Nearest Stop", r -> r.nearestStop());
        TableColumn<DashboardRow, String> etaCol      = col("ETA",          r -> r.eta());
        table.getColumns().addAll(vehicleCol, typeCol, routeCol, speedCol, passCol, statusCol, stopCol, etaCol);
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        Button refresh = greenButton("Refresh", 140);
        refresh.setOnAction(e -> table.setItems(FXCollections.observableArrayList(getDashboardRows())));

        VBox box = card(title, refresh, table);
        VBox.setVgrow(table, Priority.ALWAYS);
        VBox.setVgrow(box, Priority.ALWAYS);
        contentArea.getChildren().add(box);
    }

    // ── VEHICLES TABLE ────────────────────────────────────────────────────────

    private void showVehiclesTable() {
        contentArea.getChildren().clear();
        Label title = panelTitle("All Registered Vehicles");

        TableView<PublicVehicle> table = new TableView<>();
        table.setItems(FXCollections.observableArrayList(monitoring.getAllVehicles()));
        styleTable(table);

        TableColumn<PublicVehicle, String> idCol     = col("Vehicle ID", v -> v.vehicleId());
        TableColumn<PublicVehicle, String> typeCol   = col("Type",       v -> v.getVehicleType());
        TableColumn<PublicVehicle, String> plateCol  = col("Plate",      v -> v.plateNumber());
        TableColumn<PublicVehicle, String> capCol    = col("Capacity",   v -> String.valueOf(v.capacity()));
        TableColumn<PublicVehicle, String> routeCol  = col("Route",      v -> v.routeId() != null ? v.routeId() : "Unassigned");
        TableColumn<PublicVehicle, String> statusCol = col("Status",     v -> monitoring.getVehicleStatus(v));
        table.getColumns().addAll(idCol, typeCol, plateCol, capCol, routeCol, statusCol);
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        Button refresh = greenButton("Refresh", 140);
        refresh.setOnAction(e -> table.setItems(FXCollections.observableArrayList(monitoring.getAllVehicles())));

        VBox box = card(title, refresh, table);
        VBox.setVgrow(table, Priority.ALWAYS);
        VBox.setVgrow(box, Priority.ALWAYS);
        contentArea.getChildren().add(box);
    }

    // ── ALERTS TABLE ──────────────────────────────────────────────────────────

    private void showAlertsTable() {
        contentArea.getChildren().clear();
        Label title = panelTitle("Alerts Log");

        ComboBox<String> filterBox = new ComboBox<>();
        filterBox.getItems().addAll("ALL", "INFO", "WARN", "CRITICAL");
        filterBox.setValue("ALL");
        filterBox.setMaxWidth(220);
        filterBox.setPrefHeight(44);
        styleComboBox(filterBox);

        TableView<Alert> table = new TableView<>();
        table.setItems(FXCollections.observableArrayList(monitoring.getAlerts()));
        styleTable(table);

        TableColumn<Alert, String> timeCol     = col("Time",     a -> timeFormatter.format(a.timestamp()));
        TableColumn<Alert, String> sevCol      = col("Severity", a -> a.severity());
        TableColumn<Alert, String> typeCol     = col("Type",     a -> a.type());
        TableColumn<Alert, String> messageCol  = col("Message",  a -> a.message());
        table.getColumns().addAll(timeCol, sevCol, typeCol, messageCol);
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        Button applyFilter = greenButton("Filter", 120);
        Button refreshBtn  = ghostButton("Refresh", 120);

        applyFilter.setOnAction(e -> {
            String sel = filterBox.getValue();
            table.setItems(FXCollections.observableArrayList(
                    "ALL".equals(sel) ? monitoring.getAlerts()
                                      : monitoring.getAlertsBySeverity(sel)));
        });
        refreshBtn.setOnAction(e -> {
            filterBox.setValue("ALL");
            table.setItems(FXCollections.observableArrayList(monitoring.getAlerts()));
        });

        HBox controls = new HBox(10, filterBox, applyFilter, refreshBtn);
        controls.setAlignment(Pos.CENTER_LEFT);

        VBox box = card(title, controls, table);
        VBox.setVgrow(table, Priority.ALWAYS);
        VBox.setVgrow(box, Priority.ALWAYS);
        contentArea.getChildren().add(box);
    }

    // ── ADD DATA ──────────────────────────────────────────────────────────────

    private void showAddPanel() {
        contentArea.getChildren().clear();
        Label title = panelTitle("Add Data");

        ComboBox<String> addTypeBox = new ComboBox<>();
        addTypeBox.getItems().addAll("Route", "Stop", "Vehicle");
        addTypeBox.setPromptText("What to add?");
        addTypeBox.setMaxWidth(360);
        addTypeBox.setPrefHeight(44);
        styleComboBox(addTypeBox);

        // Route fields
        TextField routeIdField   = styledInput("Route ID", 350);
        TextField routeNameField = styledInput("Route Name", 350);

        // Stop fields
        ComboBox<Route> routeBoxStop = new ComboBox<>();
        routeBoxStop.getItems().addAll(monitoring.getRoutes());
        routeBoxStop.setPromptText("Select Route");
        routeBoxStop.setMaxWidth(350); routeBoxStop.setPrefHeight(44);
        styleComboBox(routeBoxStop);
        TextField stopIdField   = styledInput("Stop ID", 350);
        TextField stopNameField = styledInput("Stop Name", 350);
        TextField latField      = styledInput("Latitude", 350);
        TextField lonField      = styledInput("Longitude", 350);

        // Vehicle fields
        ComboBox<String> vehicleTypeBox = new ComboBox<>();
        vehicleTypeBox.getItems().addAll("Jeepney", "Modern Jeep", "Bus");
        vehicleTypeBox.setPromptText("Vehicle Type");
        vehicleTypeBox.setMaxWidth(350); vehicleTypeBox.setPrefHeight(44);
        styleComboBox(vehicleTypeBox);
        TextField vehicleIdField = styledInput("Vehicle ID", 350);
        TextField plateField     = styledInput("Plate Number", 350);
        TextField capacityField  = styledInput("Capacity", 350);

        Label message = new Label();
        Button addBtn = greenButton("Add", 160);

        javafx.scene.Node[] routeFields  = {routeIdField, routeNameField};
        javafx.scene.Node[] stopFields   = {routeBoxStop, stopIdField, stopNameField, latField, lonField};
        javafx.scene.Node[] vehicleFields= {vehicleTypeBox, vehicleIdField, plateField, capacityField};

        hideNodes(routeFields); hideNodes(stopFields); hideNodes(vehicleFields);

        addTypeBox.setOnAction(e -> {
            String sel = addTypeBox.getValue();
            hideNodes(routeFields); hideNodes(stopFields); hideNodes(vehicleFields);
            message.setText("");
            if ("Route".equals(sel))   showNodes(routeFields);
            else if ("Stop".equals(sel)) {
                routeBoxStop.getItems().setAll(monitoring.getRoutes());
                showNodes(stopFields);
            }
            else if ("Vehicle".equals(sel)) showNodes(vehicleFields);
        });

        addBtn.setOnAction(e -> {
            String sel = addTypeBox.getValue();
            if (sel == null) { showError(message, "Choose what to add."); return; }

            if ("Route".equals(sel)) {
                String rid = routeIdField.getText().trim(), rname = routeNameField.getText().trim();
                if (rid.isEmpty()||rname.isEmpty()) { showError(message, "Fill all route fields."); return; }
                if (monitoring.routeExists(rid))    { showError(message, "Route ID exists."); return; }
                monitoring.addRoute(new Route(rid, rname));
                DataStore.saveAll(auth, monitoring, savedUsers);
                routeIdField.clear(); routeNameField.clear();
                showSuccess(message, "Route added.");

            } else if ("Stop".equals(sel)) {
                try {
                    Route route = routeBoxStop.getValue();
                    String sid  = stopIdField.getText().trim();
                    String sname= stopNameField.getText().trim();
                    if (route==null||sid.isEmpty()||sname.isEmpty()||latField.getText().isEmpty()||lonField.getText().isEmpty()) {
                        showError(message, "Fill all stop fields."); return;
                    }
                    if (route.stopExists(sid)) { showError(message, "Stop ID exists on this route."); return; }
                    route.addStop(new Stop(sid, sname,
                            Double.parseDouble(latField.getText().trim()),
                            Double.parseDouble(lonField.getText().trim())));
                    DataStore.saveAll(auth, monitoring, savedUsers);
                    stopIdField.clear(); stopNameField.clear(); latField.clear(); lonField.clear();
                    showSuccess(message, "Stop added.");
                } catch (NumberFormatException ex) { showError(message, "Invalid lat/lon."); }

            } else if ("Vehicle".equals(sel)) {
                try {
                    String vtype = vehicleTypeBox.getValue();
                    String vid   = vehicleIdField.getText().trim();
                    String plate = plateField.getText().trim();
                    String cap   = capacityField.getText().trim();
                    if (vtype==null||vid.isEmpty()||plate.isEmpty()||cap.isEmpty()) {
                        showError(message, "Fill all vehicle fields."); return;
                    }
                    if (monitoring.vehicleExists(vid)) { showError(message, "Vehicle ID exists."); return; }
                    int capVal = Integer.parseInt(cap);
                    PublicVehicle v = switch (vtype) {
                        case "Bus"          -> new Bus(vid, plate, capVal);
                        case "Modern Jeep"  -> new ModernJeep(vid, plate, capVal);
                        default             -> new Jeepney(vid, plate, capVal);
                    };
                    monitoring.registerVehicle(v);
                    DataStore.saveAll(auth, monitoring, savedUsers);
                    vehicleIdField.clear(); plateField.clear(); capacityField.clear();
                    showSuccess(message, "Vehicle registered.");
                } catch (NumberFormatException ex) { showError(message, "Invalid capacity."); }
            }
        });

        contentArea.getChildren().add(card(title, addTypeBox,
                routeIdField, routeNameField,
                routeBoxStop, stopIdField, stopNameField, latField, lonField,
                vehicleTypeBox, vehicleIdField, plateField, capacityField,
                addBtn, message));
    }

    // ── ASSIGN VEHICLE ────────────────────────────────────────────────────────

    private void showAssignVehiclePanel() {
        contentArea.getChildren().clear();
        Label title = panelTitle("Assign Vehicle to Route");

        ComboBox<PublicVehicle> vehicleBox = new ComboBox<>();
        vehicleBox.getItems().addAll(monitoring.getAllVehicles());
        vehicleBox.setPromptText("Select Vehicle");
        vehicleBox.setMaxWidth(360); vehicleBox.setPrefHeight(44);
        styleComboBox(vehicleBox);

        ComboBox<Route> routeBox = new ComboBox<>();
        routeBox.getItems().addAll(monitoring.getRoutes());
        routeBox.setPromptText("Select Route");
        routeBox.setMaxWidth(360); routeBox.setPrefHeight(44);
        styleComboBox(routeBox);

        Label msg = new Label();
        Button save = greenButton("Assign", 160);

        save.setOnAction(e -> {
            PublicVehicle v = vehicleBox.getValue();
            Route r = routeBox.getValue();
            if (v==null||r==null) { showError(msg, "Select vehicle and route."); return; }
            monitoring.assignVehicleToRoute(v.vehicleId(), r.routeId());
            DataStore.saveAll(auth, monitoring, savedUsers);
            addActivityLog("Assigned " + v.vehicleId() + " → " + r.routeId());
            showSuccess(msg, "Vehicle assigned to " + r.routeName() + ".");
        });

        contentArea.getChildren().add(card(title, vehicleBox, routeBox, save, msg));
    }

    // ── REMOVE DATA ───────────────────────────────────────────────────────────

    private void showRemovePanel() {
        contentArea.getChildren().clear();
        Label title = panelTitle("Remove Data");

        ComboBox<String> removeTypeBox = new ComboBox<>();
        removeTypeBox.getItems().addAll("Route", "Stop", "Vehicle");
        removeTypeBox.setPromptText("What to remove?");
        removeTypeBox.setMaxWidth(360); removeTypeBox.setPrefHeight(44);
        styleComboBox(removeTypeBox);

        ComboBox<Route> routeBox = new ComboBox<>();
        routeBox.getItems().addAll(monitoring.getRoutes());
        routeBox.setPromptText("Select Route");
        routeBox.setMaxWidth(360); routeBox.setPrefHeight(44);
        styleComboBox(routeBox);
        routeBox.setVisible(false); routeBox.setManaged(false);

        ComboBox<PublicVehicle> vehicleBox = new ComboBox<>();
        vehicleBox.getItems().addAll(monitoring.getAllVehicles());
        vehicleBox.setPromptText("Select Vehicle");
        vehicleBox.setMaxWidth(360); vehicleBox.setPrefHeight(44);
        styleComboBox(vehicleBox);
        vehicleBox.setVisible(false); vehicleBox.setManaged(false);

        TextField stopIdField = styledInput("Stop ID to remove", 360);
        stopIdField.setVisible(false); stopIdField.setManaged(false);

        Label msg = new Label();
        Button removeBtn = new Button("Remove");
        removeBtn.setPrefHeight(44); removeBtn.setMinWidth(160);
        removeBtn.setStyle("-fx-background-color: " + ACCENT_RED + "; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 10; -fx-cursor: hand;");

        removeTypeBox.setOnAction(e -> {
            String sel = removeTypeBox.getValue();
            routeBox.setVisible(false); routeBox.setManaged(false);
            vehicleBox.setVisible(false); vehicleBox.setManaged(false);
            stopIdField.setVisible(false); stopIdField.setManaged(false);
            msg.setText("");
            if ("Route".equals(sel))   { routeBox.getItems().setAll(monitoring.getRoutes()); routeBox.setVisible(true); routeBox.setManaged(true); }
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
                Route r = routeBox.getValue(); String sid = stopIdField.getText().trim();
                if (r==null||sid.isEmpty()) { showError(msg, "Select route and enter stop ID."); return; }
                if (monitoring.removeStopFromRoute(r.routeId(), sid)) { DataStore.saveAll(auth, monitoring, savedUsers); stopIdField.clear(); showSuccess(msg, "Stop removed."); }
                else showError(msg, "Stop not found.");
            } else if ("Vehicle".equals(sel)) {
                PublicVehicle v = vehicleBox.getValue();
                if (v == null) { showError(msg, "Select a vehicle."); return; }
                if (monitoring.removeVehicle(v.vehicleId())) { DataStore.saveAll(auth, monitoring, savedUsers); vehicleBox.getItems().setAll(monitoring.getAllVehicles()); showSuccess(msg, "Vehicle removed."); }
                else showError(msg, "Vehicle not found.");
            }
        });

        contentArea.getChildren().add(card(title, removeTypeBox, routeBox, vehicleBox, stopIdField, removeBtn, msg));
    }

    // ── SEND PING ─────────────────────────────────────────────────────────────

    private void showSendPingPanel() {
        contentArea.getChildren().clear();
        Label title = panelTitle("Send Vehicle Ping");

        ComboBox<PublicVehicle> vehicleBox = new ComboBox<>();
        vehicleBox.getItems().addAll(monitoring.getAllVehicles());
        vehicleBox.setPromptText("Select Vehicle");
        vehicleBox.setMaxWidth(360); vehicleBox.setPrefHeight(44);
        styleComboBox(vehicleBox);

        TextField latF  = styledInput("Latitude  (e.g. 10.3270)", 360);
        TextField lonF  = styledInput("Longitude (e.g. 123.9063)", 360);
        TextField spd   = styledInput("Speed km/h", 360);
        TextField pax   = styledInput("Passenger Count", 360);

        Label msg = new Label();
        Button save = greenButton("Send Ping", 160);

        save.setOnAction(e -> {
            try {
                PublicVehicle v = vehicleBox.getValue();
                if (v == null) { showError(msg, "Select a vehicle."); return; }
                double speed = Double.parseDouble(spd.getText().trim());
                int    passengers = Integer.parseInt(pax.getText().trim());
                if (speed < 0 || passengers < 0) { showError(msg, "Values cannot be negative."); return; }
                VehiclePing ping = new VehiclePing(v.vehicleId(), Instant.now(),
                        Double.parseDouble(latF.getText().trim()),
                        Double.parseDouble(lonF.getText().trim()),
                        speed, passengers);
                monitoring.receivePing(ping);
                DataStore.saveAll(auth, monitoring, savedUsers);
                addActivityLog("Ping sent to " + v.vehicleId());
                latF.clear(); lonF.clear(); spd.clear(); pax.clear();
                showSuccess(msg, "Ping sent successfully.");
            } catch (Exception ex) { showError(msg, "Invalid input values."); }
        });

        contentArea.getChildren().add(card(title, vehicleBox, latF, lonF, spd, pax, save, msg));
    }

    // ── NOTIFICATIONS ─────────────────────────────────────────────────────────

    private void showNotificationCenterPanel() {
        contentArea.getChildren().clear();
        Label title = panelTitle("Notification Center");

        List<Alert> alerts = monitoring.getAlerts();
        VBox list = new VBox(8);

        if (alerts.isEmpty()) {
            Label none = new Label("No notifications.");
            none.setStyle("-fx-text-fill: " + TEXT_MUTED + "; -fx-font-size: 14px;");
            list.getChildren().add(none);
        } else {
            int show = Math.min(alerts.size(), 30);
            for (int i = alerts.size()-1; i >= alerts.size()-show; i--) {
                Alert a = alerts.get(i);
                String color = switch (a.severity()) {
                    case "CRITICAL" -> ACCENT_RED;
                    case "WARN"     -> ACCENT_AMBER;
                    default         -> ACCENT_BLUE;
                };
                HBox row = new HBox(12);
                row.setPadding(new Insets(12, 16, 12, 16));
                row.setStyle("-fx-background-color:" + BG_CARD2 + ";-fx-background-radius:10;-fx-border-color:" + BORDER + ";-fx-border-radius:10;");

                Label dot = new Label("●");
                dot.setStyle("-fx-text-fill:" + color + ";-fx-font-size:18px;");

                Label body = new Label("[" + a.severity() + " · " + a.type() + "]\n" + a.message());
                body.setStyle("-fx-text-fill:" + TEXT_PRIMARY + ";-fx-font-size:13px;");
                body.setWrapText(true);

                Label ts = new Label(timeFormatter.format(a.timestamp()));
                ts.setStyle("-fx-text-fill:" + TEXT_MUTED + ";-fx-font-size:11px;");

                Region sp = new Region(); HBox.setHgrow(sp, Priority.ALWAYS);
                row.getChildren().addAll(dot, body, sp, ts);
                row.setAlignment(Pos.CENTER_LEFT);
                list.getChildren().add(row);
            }
        }

        contentArea.getChildren().add(card(title, list));
    }

    // ── ACTIVITY LOG ──────────────────────────────────────────────────────────

    private void showActivityLogPanel() {
        contentArea.getChildren().clear();
        Label title = panelTitle("Activity Log");

        TextArea output = new TextArea();
        output.setEditable(false);
        output.setWrapText(true);
        output.setStyle(textAreaCss());

        if (activityLogs.isEmpty()) {
            output.setText("No activities recorded yet.");
        } else {
            StringBuilder sb = new StringBuilder();
            for (int i = activityLogs.size()-1; i >= 0; i--)
                sb.append(activityLogs.get(i)).append("\n");
            output.setText(sb.toString());
        }

        Button refresh = greenButton("Refresh", 140);
        refresh.setOnAction(e -> showActivityLogPanel());

        VBox box = card(title, refresh, output);
        VBox.setVgrow(output, Priority.ALWAYS);
        VBox.setVgrow(box, Priority.ALWAYS);
        contentArea.getChildren().add(box);
    }

    // ── PROFILE ───────────────────────────────────────────────────────────────

    private void showProfilePanel() {
        contentArea.getChildren().clear();
        Label title = panelTitle("My Profile");

        if (currentUser == null) return;

        Label roleLabel = styledLabel(currentUser.role(), GREEN, 12, true);
        roleLabel.setStyle(roleLabel.getStyle() +
                "-fx-background-color:" + GREEN_GLOW + ";-fx-background-radius:20;-fx-padding:3 10 3 10;");

        TextField nameField = styledInput("Full Name", 360);
        nameField.setText(currentUser.name());
        TextField usernameField = styledInput("Username", 360);
        usernameField.setText(currentUser.username());

        Label profileMsg = new Label();
        Button saveProfile = greenButton("Save Changes", 200);

        saveProfile.setOnAction(e -> {
            String newName = nameField.getText().trim();
            String newUser = usernameField.getText().trim();
            if (newName.isEmpty() || newUser.isEmpty()) { showError(profileMsg, "Fields cannot be empty."); return; }
            boolean ok = auth.updateUserProfile(currentUser, newName, newUser);
            if (!ok) { showError(profileMsg, "Username already taken."); return; }
            updateSavedUserRecord(currentUser.userId(), newName, newUser, null);
            DataStore.saveAll(auth, monitoring, savedUsers);
            showSuccess(profileMsg, "Profile updated.");
        });

        // Password change
        Label passTitle = styledLabel("Change Password", TEXT_PRIMARY, 16, true);
        PasswordField currPass = new PasswordField(); currPass.setPromptText("Current Password"); currPass.setMaxWidth(360); currPass.setPrefHeight(44); currPass.setStyle(inputCss());
        PasswordField newPass  = new PasswordField(); newPass.setPromptText("New Password");      newPass.setMaxWidth(360); newPass.setPrefHeight(44); newPass.setStyle(inputCss());
        PasswordField confPass = new PasswordField(); confPass.setPromptText("Confirm New Password"); confPass.setMaxWidth(360); confPass.setPrefHeight(44); confPass.setStyle(inputCss());
        Label passMsg = new Label();
        Button changePassBtn = greenButton("Change Password", 200);

        changePassBtn.setOnAction(e -> {
            String curr = currPass.getText().trim();
            String nw   = newPass.getText().trim();
            String conf = confPass.getText().trim();
            if (curr.isEmpty()||nw.isEmpty()||conf.isEmpty()) { showError(passMsg, "Fill all password fields."); return; }
            if (nw.length() < 4) { showError(passMsg, "Password must be ≥ 4 chars."); return; }
            if (!nw.equals(conf)) { showError(passMsg, "Passwords do not match."); return; }
            boolean ok = auth.changePassword(currentUser, curr, nw);
            if (!ok) { showError(passMsg, "Current password is incorrect."); return; }
            updateSavedUserRecord(currentUser.userId(), null, null, nw);
            DataStore.saveAll(auth, monitoring, savedUsers);
            currPass.clear(); newPass.clear(); confPass.clear();
            showSuccess(passMsg, "Password changed.");
        });

        contentArea.getChildren().add(card(title, roleLabel, sep(),
                nameField, usernameField, saveProfile, profileMsg, sep(),
                passTitle, currPass, newPass, confPass, changePassBtn, passMsg));
    }

    // ── SETTINGS ──────────────────────────────────────────────────────────────

    private void showSettingsPanel() {
        contentArea.getChildren().clear();
        Label title = panelTitle("Settings");

        Label themeLabel = styledLabel("Theme: Dark Mode (fixed for optimal map viewing)", TEXT_MUTED, 13, false);
        Label versionInfo = styledLabel("Sakay Cebu v1.0  ·  JavaFX  ·  OOP2 Project", TEXT_MUTED, 13, false);

        contentArea.getChildren().add(card(title, themeLabel, versionInfo));
    }

    // ── ABOUT ─────────────────────────────────────────────────────────────────

    private void showAboutPanel() {
        contentArea.getChildren().clear();
        Label title = panelTitle("About");

        Label info = new Label(
                "Sakay Cebu – Public Vehicle Monitoring System\n\n" +
                "A real-time tracking application for jeepneys, modern jeeps,\n" +
                "and buses across Cebu City.\n\n" +
                "Built with JavaFX · OOP2 Final Project\n" +
                "Inspired by Grab's clean, commuter-focused design."
        );
        info.setStyle("-fx-text-fill:" + TEXT_MUTED + ";-fx-font-size:14px;");
        info.setWrapText(true);

        contentArea.getChildren().add(card(title, info));
    }

    // ── HELP ──────────────────────────────────────────────────────────────────

    private void showHelpPanel() {
        contentArea.getChildren().clear();
        Label title = panelTitle("Help");

        Label help = new Label(
                "OPERATOR GUIDE\n" +
                "1. Add routes, stops, and vehicles under Add Data.\n" +
                "2. Assign a vehicle to a route using Assign Vehicle.\n" +
                "3. Send a GPS ping manually via Send Ping.\n" +
                "4. Use Start Simulation for automatic live demo pings.\n" +
                "5. Monitor all vehicles in Route Dashboard or Live Map.\n\n" +
                "COMMUTER GUIDE\n" +
                "1. Use Route Status to see all vehicle info.\n" +
                "2. Use Search Route to find vehicles on a specific route.\n" +
                "3. Use Live Map to see vehicle positions.\n" +
                "4. Use ETA Tracker for estimated arrival times.\n" +
                "5. Check Recent Alerts for transport warnings.\n\n" +
                "NOTE: Vehicles need at least one ping to appear on the map."
        );
        help.setStyle("-fx-text-fill:" + TEXT_MUTED + ";-fx-font-size:13px;-fx-line-spacing:4;");
        help.setWrapText(true);

        contentArea.getChildren().add(card(title, help));
    }

    // ── ROUTE STOPS TEXT ──────────────────────────────────────────────────────

    private void showTextPanel(String titleStr, String text) {
        contentArea.getChildren().clear();
        Label title = panelTitle(titleStr);
        TextArea output = new TextArea(text);
        output.setEditable(false);
        output.setWrapText(false);
        output.setStyle(textAreaCss());
        VBox.setVgrow(output, Priority.ALWAYS);
        VBox card = card(title, output);
        VBox.setVgrow(card, Priority.ALWAYS);
        contentArea.getChildren().add(card);
    }

    // ═══════════════════════════════════════════════════════════════════════
    //  SIMULATION
    // ═══════════════════════════════════════════════════════════════════════

    private void startLiveSimulation() {
        if (liveSimulation != null) liveSimulation.stop();
        liveSimulation = new Timeline(new KeyFrame(Duration.seconds(3), e -> simulateVehiclePing()));
        liveSimulation.setCycleCount(Timeline.INDEFINITE);
        liveSimulation.play();
        addActivityLog("Simulation started.");
        showWelcomePanel(currentUser);
    }

    private void stopLiveSimulation() {
        if (liveSimulation != null) { liveSimulation.stop(); liveSimulation = null; }
        addActivityLog("Simulation stopped.");
        showWelcomePanel(currentUser);
    }

    private void simulateVehiclePing() {
        for (PublicVehicle v : monitoring.getAllVehicles()) {
            double baseLat = v.hasLocation() ? v.lat() : 10.3270;
            double baseLon = v.hasLocation() ? v.lon() : 123.9063;
            double newLat  = baseLat + (random.nextDouble()-0.5)*0.002;
            double newLon  = baseLon + (random.nextDouble()-0.5)*0.002;
            double speed   = 10 + random.nextDouble()*50;
            int    pax     = random.nextInt(v.capacity()+6);
            monitoring.receivePing(new VehiclePing(v.vehicleId(), Instant.now(), newLat, newLon, speed, pax));
        }
        DataStore.saveAll(auth, monitoring, savedUsers);
    }

    // ═══════════════════════════════════════════════════════════════════════
    //  SEED & DEFAULT DATA
    // ═══════════════════════════════════════════════════════════════════════

    private void seedData() {
        auth.addUser(new Operator("U-001", "Default Operator", "operator", "1234"));
        auth.addUser(new Commuter("U-002", "Default Commuter", "commuter", "1234"));
        savedUsers.add(new DataStore.SavedUser("U-001", "Default Operator", "operator", "1234", "Operator"));
        savedUsers.add(new DataStore.SavedUser("U-002", "Default Commuter", "commuter", "1234", "Commuter"));

        Route r = new Route("R-04L", "IT Park - Ayala - Fuente - Colon");
        r.addStop(new Stop("S-001", "IT Park",           10.3270, 123.9063));
        r.addStop(new Stop("S-002", "Ayala Center Cebu", 10.3187, 123.9056));
        r.addStop(new Stop("S-003", "Fuente Osmeña",     10.3090, 123.8929));
        r.addStop(new Stop("S-004", "Colon",             10.2965, 123.8988));
        monitoring.addRoute(r);

        monitoring.registerVehicle(new Jeepney("V-001", "ABC-123", 20));
        monitoring.assignVehicleToRoute("V-001", "R-04L");
    }

    private void ensureDefaultCebuRoutesAndVehicles() {
        if (!monitoring.routeExists("R-04L")) {
            Route r = new Route("R-04L", "IT Park - Ayala - Fuente - Colon");
            r.addStop(new Stop("S-001","IT Park",10.3270,123.9063));
            r.addStop(new Stop("S-002","Ayala Center Cebu",10.3187,123.9056));
            r.addStop(new Stop("S-003","Fuente Osmeña",10.3090,123.8929));
            r.addStop(new Stop("S-004","Colon",10.2965,123.8988));
            monitoring.addRoute(r);
        }
        if (!monitoring.routeExists("R-17B")) {
            Route r = new Route("R-17B", "CIT-U - Colon - Carbon");
            r.addStop(new Stop("S-101","CIT-U",10.2949,123.8816));
            r.addStop(new Stop("S-102","Pardo",10.2837,123.8679));
            r.addStop(new Stop("S-103","Colon",10.2965,123.8988));
            r.addStop(new Stop("S-104","Carbon Market",10.2943,123.9017));
            monitoring.addRoute(r);
        }
        if (!monitoring.routeExists("R-13C")) {
            Route r = new Route("R-13C", "Talamban - Banilad - Ayala");
            r.addStop(new Stop("S-201","Talamban",10.3697,123.9141));
            r.addStop(new Stop("S-202","Banilad",10.3435,123.9110));
            r.addStop(new Stop("S-203","Country Mall",10.3374,123.9114));
            r.addStop(new Stop("S-204","Ayala Center Cebu",10.3187,123.9056));
            monitoring.addRoute(r);
        }
        if (!monitoring.routeExists("R-12L")) {
            Route r = new Route("R-12L", "Labangon - Capitol - Ayala");
            r.addStop(new Stop("S-301","Labangon",10.2994,123.8755));
            r.addStop(new Stop("S-302","Capitol",10.3140,123.8919));
            r.addStop(new Stop("S-303","Fuente Osmeña",10.3090,123.8929));
            r.addStop(new Stop("S-304","Ayala Center Cebu",10.3187,123.9056));
            monitoring.addRoute(r);
        }
        if (!monitoring.vehicleExists("V-001")) { monitoring.registerVehicle(new Jeepney("V-001","ABC-123",20)); monitoring.assignVehicleToRoute("V-001","R-04L"); }
        if (!monitoring.vehicleExists("V-002")) { monitoring.registerVehicle(new ModernJeep("V-002","XYZ-456",30)); monitoring.assignVehicleToRoute("V-002","R-04L"); }
        if (!monitoring.vehicleExists("V-003")) { monitoring.registerVehicle(new Jeepney("V-003","CIT-789",20)); monitoring.assignVehicleToRoute("V-003","R-17B"); }
        if (!monitoring.vehicleExists("V-004")) { monitoring.registerVehicle(new Bus("V-004","BUS-321",45)); monitoring.assignVehicleToRoute("V-004","R-13C"); }
        if (!monitoring.vehicleExists("V-005")) { monitoring.registerVehicle(new Jeepney("V-005","LAB-555",20)); monitoring.assignVehicleToRoute("V-005","R-12L"); }
    }

    // ═══════════════════════════════════════════════════════════════════════
    //  SIDEBAR HELPERS
    // ═══════════════════════════════════════════════════════════════════════

    private void sidebarSection(String text) {
        Label label = new Label(text);
        label.setStyle(
                "-fx-text-fill: " + TEXT_DIM + ";" +
                "-fx-font-size: 10px;" +
                "-fx-font-weight: bold;" +
                "-fx-padding: 12 8 4 8;"
        );
        label.setMaxWidth(Double.MAX_VALUE);
        sidebar.getChildren().add(label);
    }

    private void sidebarBtn(String text, Runnable action) {
        Button btn = new Button(text);
        btn.setMaxWidth(Double.MAX_VALUE);
        btn.setPrefHeight(40);
        btn.setAlignment(Pos.CENTER_LEFT);
        btn.setStyle(
                "-fx-background-color: transparent;" +
                "-fx-text-fill: " + TEXT_MUTED + ";" +
                "-fx-font-size: 13px;" +
                "-fx-background-radius: 8;" +
                "-fx-cursor: hand;" +
                "-fx-padding: 0 10 0 10;"
        );
        btn.setOnMouseEntered(e -> btn.setStyle(
                "-fx-background-color: " + GREEN_GLOW + ";" +
                "-fx-text-fill: " + GREEN + ";" +
                "-fx-font-size: 13px;" +
                "-fx-background-radius: 8;" +
                "-fx-cursor: hand;" +
                "-fx-padding: 0 10 0 10;"
        ));
        btn.setOnMouseExited(e -> btn.setStyle(
                "-fx-background-color: transparent;" +
                "-fx-text-fill: " + TEXT_MUTED + ";" +
                "-fx-font-size: 13px;" +
                "-fx-background-radius: 8;" +
                "-fx-cursor: hand;" +
                "-fx-padding: 0 10 0 10;"
        ));
        btn.setOnAction(e -> action.run());
        sidebar.getChildren().add(btn);
    }

    private void addLogoutButton() {
        Region spacer = new Region();
        VBox.setVgrow(spacer, Priority.ALWAYS);

        Button logout = new Button("Log Out");
        logout.setMaxWidth(Double.MAX_VALUE);
        logout.setPrefHeight(42);
        logout.setStyle(
                "-fx-background-color: rgba(239,68,68,0.12);" +
                "-fx-text-fill: " + ACCENT_RED + ";" +
                "-fx-font-weight: bold;" +
                "-fx-background-radius: 8;" +
                "-fx-cursor: hand;"
        );
        logout.setOnAction(e -> { auth.logout(); showLoginScreen(); });
        sidebar.getChildren().addAll(spacer, logout);
    }

    // ═══════════════════════════════════════════════════════════════════════
    //  UI COMPONENT FACTORIES
    // ═══════════════════════════════════════════════════════════════════════

    private ImageView loadLogo() {
        InputStream stream = getClass().getResourceAsStream("/assets/logo.jpeg");
        if (stream == null) return new ImageView();
        Image logo = new Image(stream);
        ImageView logoView = new ImageView(logo);
        logoView.setFitWidth(90);
        logoView.setFitHeight(90);
        logoView.setPreserveRatio(false);
        Circle clip = new Circle(45, 45, 45);
        logoView.setClip(clip);
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

    private Button greenButton(String text, double minWidth) {
        Button btn = new Button(text);
        btn.setPrefHeight(46);
        btn.setMinWidth(minWidth);
        btn.setStyle(
                "-fx-background-color: " + GREEN + ";" +
                "-fx-text-fill: white;" +
                "-fx-font-weight: bold;" +
                "-fx-font-size: 14px;" +
                "-fx-background-radius: 10;" +
                "-fx-cursor: hand;"
        );
        btn.setOnMouseEntered(e -> btn.setStyle(
                "-fx-background-color: " + GREEN_DARK + ";" +
                "-fx-text-fill: white;" +
                "-fx-font-weight: bold;" +
                "-fx-font-size: 14px;" +
                "-fx-background-radius: 10;" +
                "-fx-cursor: hand;"
        ));
        btn.setOnMouseExited(e -> btn.setStyle(
                "-fx-background-color: " + GREEN + ";" +
                "-fx-text-fill: white;" +
                "-fx-font-weight: bold;" +
                "-fx-font-size: 14px;" +
                "-fx-background-radius: 10;" +
                "-fx-cursor: hand;"
        ));
        return btn;
    }

    private Button ghostButton(String text, double minWidth) {
        Button btn = new Button(text);
        btn.setPrefHeight(46);
        btn.setMinWidth(minWidth);
        btn.setStyle(
                "-fx-background-color: transparent;" +
                "-fx-text-fill: " + TEXT_MUTED + ";" +
                "-fx-font-weight: bold;" +
                "-fx-font-size: 14px;" +
                "-fx-background-radius: 10;" +
                "-fx-border-color: " + BORDER + ";" +
                "-fx-border-radius: 10;" +
                "-fx-cursor: hand;"
        );
        return btn;
    }

    private Label panelTitle(String text) {
        Label label = new Label(text);
        label.setStyle("-fx-font-size: 22px; -fx-font-weight: bold; -fx-text-fill: " + TEXT_PRIMARY + ";");
        return label;
    }

    private Label styledLabel(String text, String color, int size, boolean bold) {
        Label l = new Label(text);
        l.setStyle(
                "-fx-font-size:" + size + "px;" +
                (bold ? "-fx-font-weight:bold;" : "") +
                "-fx-text-fill:" + color + ";"
        );
        return l;
    }

    private VBox card(javafx.scene.Node... nodes) {
        VBox box = new VBox(14);
        box.getChildren().addAll(nodes);
        box.setPadding(new Insets(24));
        box.setStyle(
                "-fx-background-color: " + BG_CARD + ";" +
                "-fx-background-radius: 14;" +
                "-fx-border-color: " + BORDER + ";" +
                "-fx-border-radius: 14;"
        );
        return box;
    }

    private Region sep() {
        Region r = new Region();
        r.setPrefHeight(1);
        r.setStyle("-fx-background-color: " + BORDER + ";");
        return r;
    }

    private <T> TableColumn<T, String> col(String header, java.util.function.Function<T, String> extractor) {
        TableColumn<T, String> c = new TableColumn<>(header);
        c.setCellValueFactory(data -> new SimpleStringProperty(extractor.apply(data.getValue())));
        return c;
    }

    private void styleTable(TableView<?> table) {
        table.setStyle(
                "-fx-background-color: " + BG_CARD + ";" +
                "-fx-control-inner-background: " + BG_CARD + ";" +
                "-fx-table-cell-border-color: " + BORDER + ";" +
                "-fx-border-color: " + BORDER + ";" +
                "-fx-border-radius: 10;" +
                "-fx-background-radius: 10;" +
                "-fx-text-fill: " + TEXT_PRIMARY + ";"
        );
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
    }

    private <T> void styleComboBox(ComboBox<T> cb) {
        cb.setStyle(inputCss());
        cb.setButtonCell(new ListCell<T>() {
            @Override protected void updateItem(T item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty||item==null ? cb.getPromptText() : item.toString());
                setStyle("-fx-text-fill:" + TEXT_PRIMARY + ";-fx-background-color:" + BG_CARD2 + ";");
            }
        });
        cb.setCellFactory(lv -> new ListCell<T>() {
            @Override protected void updateItem(T item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty||item==null ? null : item.toString());
                setStyle("-fx-text-fill:" + TEXT_PRIMARY + ";-fx-background-color:" + BG_CARD2 + ";-fx-padding:6 10;");
            }
        });
    }

    // ═══════════════════════════════════════════════════════════════════════
    //  CSS STRINGS
    // ═══════════════════════════════════════════════════════════════════════

    private String inputCss() {
        return
            "-fx-background-color: " + BG_CARD2 + ";" +
            "-fx-text-fill: " + TEXT_PRIMARY + ";" +
            "-fx-prompt-text-fill: " + TEXT_MUTED + ";" +
            "-fx-background-radius: 10;" +
            "-fx-border-color: " + BORDER + ";" +
            "-fx-border-radius: 10;" +
            "-fx-font-size: 13px;";
    }

    private String textAreaCss() {
        return
            "-fx-control-inner-background: " + BG_CARD2 + ";" +
            "-fx-background-color: " + BG_CARD2 + ";" +
            "-fx-text-fill: " + TEXT_PRIMARY + ";" +
            "-fx-prompt-text-fill: " + TEXT_MUTED + ";" +
            "-fx-border-color: " + BORDER + ";" +
            "-fx-border-radius: 10;" +
            "-fx-background-radius: 10;" +
            "-fx-font-size: 13px;";
    }

    // ═══════════════════════════════════════════════════════════════════════
    //  UTILITY
    // ═══════════════════════════════════════════════════════════════════════

    private void showSuccess(Label label, String text) {
        label.setStyle("-fx-text-fill: " + GREEN + "; -fx-font-weight: bold; -fx-font-size: 13px;");
        label.setText(text);
    }

    private void showError(Label label, String text) {
        label.setStyle("-fx-text-fill: " + ACCENT_RED + "; -fx-font-weight: bold; -fx-font-size: 13px;");
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
        ft.setFromValue(0); ft.setToValue(1); ft.play();
    }

    private double haversine(double lat1, double lon1, double lat2, double lon2) {
        final double R = 6371;
        double dLat = Math.toRadians(lat2-lat1);
        double dLon = Math.toRadians(lon2-lon1);
        double a = Math.sin(dLat/2)*Math.sin(dLat/2) +
                   Math.cos(Math.toRadians(lat1))*Math.cos(Math.toRadians(lat2))*
                   Math.sin(dLon/2)*Math.sin(dLon/2);
        return R * 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a));
    }

    private void updateSavedUserRecord(String userId, String newName, String newUsername, String newPassword) {
        for (int i = 0; i < savedUsers.size(); i++) {
            DataStore.SavedUser s = savedUsers.get(i);
            if (s.userId().equals(userId)) {
                savedUsers.set(i, new DataStore.SavedUser(
                        s.userId(),
                        newName     != null ? newName     : s.name(),
                        newUsername != null ? newUsername : s.username(),
                        newPassword != null ? newPassword : s.password(),
                        s.role()
                ));
                return;
            }
        }
    }

    private String getRouteStopsText() {
        StringBuilder sb = new StringBuilder();
        for (Route r : monitoring.getRoutes()) {
            sb.append(r.routeId()).append(" – ").append(r.routeName()).append("\n");
            for (Stop s : r.stops())
                sb.append("   ● ").append(s.stopName()).append(" (").append(s.lat()).append(", ").append(s.lon()).append(")\n");
            sb.append("\n");
        }
        return sb.length() == 0 ? "No routes available." : sb.toString();
    }

    private List<DashboardRow> getDashboardRows() {
        List<DashboardRow> rows = new ArrayList<>();
        for (PublicVehicle v : monitoring.getAllVehicles()) {
            String nearestStop = "N/A";
            String eta = "N/A";
            if (v.hasLocation() && v.routeId() != null) {
                Route route = monitoring.getRouteById(v.routeId());
                if (route != null) {
                    double minDist = Double.MAX_VALUE;
                    for (Stop s : route.stops()) {
                        double d = haversine(v.lat(), v.lon(), s.lat(), s.lon());
                        if (d < minDist) { minDist = d; nearestStop = s.stopName(); }
                    }
                    double spd = v.speedKmh() > 0 ? v.speedKmh() : 20;
                    eta = String.format("~%.0f min", (minDist / spd) * 60);
                }
            }
            rows.add(new DashboardRow(
                    v.vehicleId(),
                    v.getVehicleType(),
                    v.routeId() != null ? v.routeId() : "Unassigned",
                    String.format("%.0f km/h", v.speedKmh()),
                    v.passengerCount() + "/" + v.capacity(),
                    monitoring.getVehicleStatus(v),
                    nearestStop, eta
            ));
        }
        return rows;
    }

    private record DashboardRow(
            String vehicleId, String type, String route,
            String speed, String passengers, String status,
            String nearestStop, String eta) {}

    public static void main(String[] args) { launch(args); }
}