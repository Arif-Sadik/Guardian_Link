package controller;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import model.entity.Child;
import model.entity.Donation;
import model.entity.SystemLog;
import model.user.User;
import model.user.UserRole;
import model.user.SystemAdmin;
import model.user.OrganizationAdmin;
import model.user.Donor;
import model.user.Caregiver;
import model.user.Support;
import service.ChildService;
import service.DonationService;
import service.SystemLogService;
import service.UserService;
import service.RolePermissionsService;
import util.ThemeManager;
import util.PasswordUtil;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * System Administrator dashboard — Figma-matched.
 * Sidebar pages: Dashboard, Child Profiles, System Admin, Reports.
 */
public class AdminController {

    private final Stage stage;
    private final User user;
    private final UserService userService = new UserService();
    private final ChildService childService = new ChildService();
    private final DonationService donationService = new DonationService();
    private final SystemLogService systemLogService = new SystemLogService();
    private final RolePermissionsService rolePermissionsService = new RolePermissionsService();
    private BorderPane root;
    private VBox sidebar;
    private String activePage = "dashboard";

    private java.util.List<String[]> activeAlerts = new java.util.ArrayList<>(java.util.Arrays.asList(
            new String[] { "critical", "Low Wallet Balance", "ALT-001", "2 hours ago",
                    "Child CH-1024 has wallet balance below threshold", "CH-1024" },
            new String[] { "critical", "Emergency Medical Situation", "ALT-002", "3 hours ago",
                    "Child CH-1087 requires immediate medical attention", "CH-1087" },
            new String[] { "warning", "Missed Medical Appointment", "ALT-003", "1 day ago",
                    "Child CH-1024 missed scheduled medical checkup", "CH-1024" },
            new String[] { "warning", "Low Attendance Rate", "ALT-004", "1 day ago",
                    "Child CH-1045 attendance dropped to 78%",
                    "CH-1045" },
            new String[] { "info", "New Donor Registration", "ALT-005", "2 days ago",
                    "New donor pending verification: John Smith", "" }));

    // Figma tokens - using ThemeManager for theme-aware colors
    private static final String PRIMARY = ThemeManager.PRIMARY;
    private static final String PRIMARY_FG = ThemeManager.PRIMARY_FG;
    private static final String SECONDARY = ThemeManager.SECONDARY;
    private static final String WARNING = ThemeManager.WARNING;
    private static final String DESTRUCTIVE = ThemeManager.DESTRUCTIVE;
    private static final String INFO = ThemeManager.INFO;
    private static final String CHART1 = "#2563eb";
    private static final String CHART2 = "#16a34a";
    private static final String CHART3 = "#f59e0b";
    private static final String CHART4 = "#8b5cf6";
    private static final String CHART5 = "#ec4899";

    // Theme-aware color getters
    private String BG() {
        return ThemeManager.getBg();
    }

    private String CARD() {
        return ThemeManager.getCard();
    }

    private String BORDER() {
        return ThemeManager.getBorder();
    }

    private String MUTED() {
        return ThemeManager.getMuted();
    }

    private String MUTED_FG() {
        return ThemeManager.getMutedFg();
    }

    private String TEXT() {
        return ThemeManager.getText();
    }

    public AdminController(Stage stage, User user) {
        this.stage = stage;
        this.user = user;
    }

    public void show() {
        System.out.println("AdminController: showing dashboard");
        root = new BorderPane();
        root.setTop(buildHeader());
        root.setLeft(buildSidebar());
        root.setCenter(buildDashboardPage());
        root.setStyle("-fx-background-color: " + BG() + ";");

        Scene scene = new Scene(root, 1280, 800);
        stage.setScene(scene);
        stage.setTitle("GuardianLink \u2014 System Administrator");
        stage.show();
    }

    private void refreshTheme() {
        root.setStyle("-fx-background-color: " + BG() + ";");
        root.setTop(buildHeader());
        root.setLeft(buildSidebar());
        // Refresh current page
        switch (activePage) {
            case "dashboard" -> root.setCenter(buildDashboardPage());
            case "children" -> root.setCenter(buildChildrenPage());
            case "alerts" -> root.setCenter(buildAlertsPage());
            case "reports" -> root.setCenter(buildReportsPage());
            case "admin" -> root.setCenter(buildAdminPage());
        }
    }

    // ═══════════ HEADER ═══════════
    private HBox buildHeader() {
        HBox header = new HBox();
        header.setPadding(new Insets(0, 24, 0, 24));
        header.setAlignment(Pos.CENTER_LEFT);
        header.setPrefHeight(64);
        header.setStyle(
                "-fx-background-color: " + CARD() + "; -fx-border-color: " + BORDER() + "; -fx-border-width: 0 0 1 0;");

        // Shield Icon Logo
        Label logoIcon = new Label("\uD83D\uDEE1");
        logoIcon.setFont(Font.font("Segoe UI Emoji", 28));
        logoIcon.setTextFill(Color.web(PRIMARY));

        VBox titleBox = new VBox(0);
        titleBox.setPadding(new Insets(0, 0, 0, 14));
        Label t1 = new Label("GuardianLink");
        t1.setFont(Font.font("Segoe UI", FontWeight.BOLD, 20));
        t1.setTextFill(Color.web(TEXT()));
        Label t2 = new Label("NGO Welfare Management System");
        t2.setFont(Font.font("Segoe UI", FontWeight.SEMI_BOLD, 12));
        t2.setTextFill(Color.web(MUTED_FG()));
        titleBox.getChildren().addAll(t1, t2);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        // User type display box
        HBox userTypeBox = new HBox(8);
        userTypeBox.setAlignment(Pos.CENTER_RIGHT);
        userTypeBox.setPadding(new Insets(8, 16, 8, 16));
        userTypeBox.setStyle("-fx-background-color: " + MUTED() + "; -fx-background-radius: 8; -fx-border-color: "
                + BORDER() + "; -fx-border-radius: 8;");

        Label userIcon = new Label("");
        userIcon.setFont(Font.font("Segoe UI", 14));

        VBox userInfo = new VBox(2);
        Label uName = new Label(user.getUsername());
        uName.setFont(Font.font("Segoe UI", FontWeight.MEDIUM, 13));
        uName.setTextFill(Color.web(TEXT()));
        Label uRole = new Label("System Administrator");
        uRole.setFont(Font.font("Segoe UI", FontWeight.BOLD, 11));
        uRole.setTextFill(Color.web(PRIMARY));
        userInfo.getChildren().addAll(uName, uRole);

        userTypeBox.getChildren().addAll(userIcon, userInfo);

        header.getChildren().addAll(logoIcon, titleBox, spacer, userTypeBox);
        return header;
    }

    // ═══════════ SIDEBAR ═══════════
    private VBox buildSidebar() {
        sidebar = new VBox(4);
        sidebar.setPrefWidth(240);
        sidebar.setStyle(
                "-fx-background-color: " + CARD() + "; -fx-border-color: " + BORDER() + "; -fx-border-width: 0 1 0 0;");

        Label navLabel = new Label("Navigation");
        navLabel.setFont(Font.font("Segoe UI", 11));
        navLabel.setTextFill(Color.web(MUTED_FG()));
        navLabel.setPadding(new Insets(16, 16, 8, 16));
        sidebar.getChildren().add(navLabel);

        VBox navItems = new VBox(2);
        navItems.setPadding(new Insets(0, 8, 0, 8));
        navItems.getChildren().addAll(
                sidebarBtn("Dashboard", "dashboard"),
                sidebarBtn("Child Profiles", "children"),
                sidebarBtn("Alerts", "alerts"),
                sidebarBtn("Reports & Audit", "reports"),
                sidebarBtn("System Admin", "admin"));
        sidebar.getChildren().add(navItems);

        Region spacer = new Region();
        VBox.setVgrow(spacer, Priority.ALWAYS);

        // Theme toggle section
        VBox themeSection = new VBox(8);
        themeSection.setPadding(new Insets(12, 8, 12, 8));
        themeSection.setStyle("-fx-border-color: " + BORDER() + "; -fx-border-width: 1 0 0 0;");

        Label themeLabel = new Label("Theme");
        themeLabel.setFont(Font.font("Segoe UI", 11));
        themeLabel.setTextFill(Color.web(MUTED_FG()));
        themeLabel.setPadding(new Insets(0, 8, 0, 8));

        HBox themeToggle = new HBox(8);
        themeToggle.setAlignment(Pos.CENTER_LEFT);
        themeToggle.setPadding(new Insets(0, 8, 0, 8));

        Button lightBtn = new Button("Light");
        Button darkBtn = new Button("Dark");

        String activeStyle = "-fx-background-color: " + PRIMARY
                + "; -fx-text-fill: white; -fx-background-radius: 4; -fx-padding: 6 12; -fx-cursor: hand; -fx-font-size: 11px;";
        String inactiveStyle = "-fx-background-color: " + MUTED() + "; -fx-text-fill: " + TEXT()
                + "; -fx-background-radius: 4; -fx-padding: 6 12; -fx-cursor: hand; -fx-font-size: 11px;";

        lightBtn.setStyle(ThemeManager.isDarkMode() ? inactiveStyle : activeStyle);
        darkBtn.setStyle(ThemeManager.isDarkMode() ? activeStyle : inactiveStyle);

        lightBtn.setOnAction(e -> {
            ThemeManager.setDarkMode(false);
            refreshTheme();
        });
        darkBtn.setOnAction(e -> {
            ThemeManager.setDarkMode(true);
            refreshTheme();
        });

        themeToggle.getChildren().addAll(lightBtn, darkBtn);
        themeSection.getChildren().addAll(themeLabel, themeToggle);

        // Logout button
        VBox logoutSection = new VBox(8);
        logoutSection.setPadding(new Insets(8, 8, 8, 8));

        Button logoutBtn = new Button("Logout");
        logoutBtn.setMaxWidth(Double.MAX_VALUE);
        logoutBtn.setAlignment(Pos.CENTER_LEFT);
        logoutBtn.setFont(Font.font("Segoe UI", 13));
        logoutBtn.setStyle("-fx-background-color: " + DESTRUCTIVE
                + "; -fx-text-fill: white; -fx-background-radius: 4; -fx-padding: 8 12; -fx-cursor: hand;");
        logoutBtn.setOnAction(e -> new AuthController(stage).show());
        logoutSection.getChildren().add(logoutBtn);

        // Version label
        Label ver = new Label("v1.0.0 | Academic Project");
        ver.setFont(Font.font("Segoe UI", 11));
        ver.setTextFill(Color.web(MUTED_FG()));
        ver.setPadding(new Insets(8, 16, 16, 16));

        sidebar.getChildren().addAll(spacer, themeSection, logoutSection, ver);

        return sidebar;
    }

    private Button sidebarBtn(String text, String pageId) {
        Button btn = new Button(text);
        btn.setMaxWidth(Double.MAX_VALUE);
        btn.setAlignment(Pos.CENTER_LEFT);
        btn.setFont(Font.font("Segoe UI", 13));
        boolean active = pageId.equals(activePage);
        styleSidebarBtn(btn, active);
        btn.setOnAction(e -> {
            activePage = pageId;
            refreshSidebar();
            switch (pageId) {
                case "dashboard" -> root.setCenter(buildDashboardPage());
                case "children" -> root.setCenter(buildChildrenPage());
                case "alerts" -> root.setCenter(buildAlertsPage());
                case "reports" -> root.setCenter(buildReportsPage());
                case "admin" -> root.setCenter(buildAdminPage());
            }
        });
        return btn;
    }

    private void styleSidebarBtn(Button btn, boolean active) {
        if (active) {
            btn.setStyle("-fx-background-color: " + PRIMARY
                    + "; -fx-text-fill: white; -fx-background-radius: 4; -fx-padding: 8 12; -fx-cursor: hand; -fx-font-size: 13px;");
        } else {
            btn.setStyle(
                    "-fx-background-color: transparent; -fx-text-fill: " + TEXT()
                            + "; -fx-background-radius: 4; -fx-padding: 8 12; -fx-cursor: hand; -fx-font-size: 13px;");
        }
    }

    private void refreshSidebar() {
        VBox navItems = (VBox) sidebar.getChildren().get(1);
        for (Node n : navItems.getChildren()) {
            if (n instanceof Button b) {
                String id = getPageId(b.getText());
                styleSidebarBtn(b, id.equals(activePage));
            }
        }
    }

    private String getPageId(String text) {
        if (text.contains("Dashboard"))
            return "dashboard";
        if (text.contains("Child"))
            return "children";
        if (text.contains("Alerts"))
            return "alerts";
        if (text.contains("Reports"))
            return "reports";
        if (text.contains("System"))
            return "admin";
        return "";
    }

    // ═══════════ DASHBOARD PAGE ═══════════
    private ScrollPane buildDashboardPage() {
        VBox page = new VBox(20);
        page.setPadding(new Insets(24));

        Label title = new Label("System Administrator Dashboard");
        title.setFont(Font.font("Segoe UI", FontWeight.MEDIUM, 20));
        Label sub = new Label("Full system overview and configuration");
        sub.setFont(Font.font("Segoe UI", 13));
        sub.setTextFill(Color.web(MUTED_FG()));
        VBox header = new VBox(4, title, sub);

        // Load real-time user stats from database
        java.util.List<User> allUsers = userService.getAllUsers();
        int totalUsers = allUsers.size();
        int activeUsers = (int) allUsers.stream().filter(User::isApproved).count();

        // 4 stat cards
        HBox stats = new HBox(16);
        stats.getChildren().addAll(
                statCard("Total Users", String.valueOf(totalUsers), activeUsers + " active", PRIMARY, SECONDARY),
                statCard("Database Size", "N/A", "System storage", INFO, MUTED_FG()),
                statCard("System Health", "100%", "All systems operational", SECONDARY, SECONDARY),
                statCard("Active Sessions", String.valueOf(activeUsers), "Current active users", WARNING, MUTED_FG()));

        // 2-column metrics
        HBox metrics = new HBox(16);
        metrics.getChildren().addAll(buildUserDistribution(), buildSystemActivity());
        HBox.setHgrow(metrics.getChildren().get(0), Priority.ALWAYS);
        HBox.setHgrow(metrics.getChildren().get(1), Priority.ALWAYS);

        // Quick Admin Actions
        Label qaTitle = new Label("Quick Admin Actions");
        qaTitle.setFont(Font.font("Segoe UI", FontWeight.MEDIUM, 17));
        HBox qaCards = new HBox(12);
        qaCards.getChildren().addAll(
                actionCard("\uD83D\uDC65", "Manage Users", "View all users", "admin"),
                actionCard("\u2699", "System Config", "Configure settings", "admin"),
                actionCard("\uD83D\uDDC4", "Audit Logs", "View system logs", "reports"),
                actionCard("\uD83D\uDCCA", "Monitor Alerts", "Check system alerts", "alerts"));

        // System Events table
        VBox eventsCard = buildSystemEventsTable();

        page.getChildren().addAll(header, stats, metrics, qaTitle, qaCards, eventsCard);
        ScrollPane sp = new ScrollPane(page);
        sp.setFitToWidth(true);
        sp.setStyle("-fx-background: " + BG() + "; -fx-background-color: " + BG() + ";");
        return sp;
    }

    private VBox statCard(String label, String value, String detail, String iconColor, String detailColor) {
        VBox card = new VBox(4);
        card.setPadding(new Insets(16));
        card.setStyle("-fx-background-color: " + CARD() + "; -fx-border-color: " + BORDER()
                + "; -fx-border-width: 1; -fx-background-radius: 8; -fx-border-radius: 8;");
        card.setPrefWidth(200);
        HBox.setHgrow(card, Priority.ALWAYS);

        Label l = new Label(label);
        l.setFont(Font.font("Segoe UI", 11));
        l.setTextFill(Color.web(MUTED_FG()));

        Label v = new Label(value);
        v.setFont(Font.font("Segoe UI", FontWeight.SEMI_BOLD, 24));
        v.setTextFill(Color.web(TEXT()));

        Label d = new Label(detail);
        d.setFont(Font.font("Segoe UI", 11));
        d.setTextFill(Color.web(detailColor));

        card.getChildren().addAll(l, v, d);
        return card;
    }

    private VBox buildUserDistribution() {
        VBox card = new VBox(12);
        card.setPadding(new Insets(16));
        card.setStyle("-fx-background-color: " + CARD() + "; -fx-border-color: " + BORDER()
                + "; -fx-border-width: 1; -fx-background-radius: 8; -fx-border-radius: 8;");

        Label t = new Label("User Distribution by Role");
        t.setFont(Font.font("Segoe UI", FontWeight.MEDIUM, 17));
        card.getChildren().add(t);

        // Load real-time user distribution from database
        java.util.List<User> allUsers = userService.getAllUsers();
        int totalUsers = Math.max(1, allUsers.size());

        long systemAdminCount = allUsers.stream().filter(u -> u.getRole().toString().equals("SYSTEM_ADMIN")).count();
        long orgAdminCount = allUsers.stream().filter(u -> u.getRole().toString().equals("ORGANIZATION_ADMIN")).count();
        long donorCount = allUsers.stream().filter(u -> u.getRole().toString().equals("DONOR")).count();

        String[][] data = {
                { "System Admins", String.valueOf(systemAdminCount),
                        String.valueOf((systemAdminCount * 100) / totalUsers), CHART1 },
                { "Organization Admins", String.valueOf(orgAdminCount),
                        String.valueOf((orgAdminCount * 100) / totalUsers), CHART2 },
                { "Donors", String.valueOf(donorCount), String.valueOf((donorCount * 100) / totalUsers), CHART3 }
        };
        for (String[] row : data) {
            VBox item = new VBox(4);
            HBox labels = new HBox();
            Label name = new Label(row[0]);
            name.setFont(Font.font("Segoe UI", 13));
            Region sp = new Region();
            HBox.setHgrow(sp, Priority.ALWAYS);
            Label count = new Label(row[1]);
            count.setFont(Font.font("Segoe UI", FontWeight.SEMI_BOLD, 13));
            labels.getChildren().addAll(name, sp, count);

            StackPane barBg = new StackPane();
            barBg.setPrefHeight(8);
            barBg.setMaxHeight(8);
            barBg.setStyle("-fx-background-color: " + MUTED() + "; -fx-background-radius: 4;");
            StackPane barFill = new StackPane();
            barFill.setPrefHeight(8);
            barFill.setMaxHeight(8);
            barFill.setMaxWidth(Double.parseDouble(row[2]) * 3);
            barFill.setStyle("-fx-background-color: " + row[3] + "; -fx-background-radius: 4;");
            barFill.setAlignment(Pos.CENTER_LEFT);
            StackPane barContainer = new StackPane(barBg, barFill);
            barContainer.setAlignment(Pos.CENTER_LEFT);

            item.getChildren().addAll(labels, barContainer);
            card.getChildren().add(item);
        }
        return card;
    }

    private VBox buildSystemActivity() {
        VBox card = new VBox(12);
        card.setPadding(new Insets(16));
        card.setStyle("-fx-background-color: " + CARD() + "; -fx-border-color: " + BORDER()
                + "; -fx-border-width: 1; -fx-background-radius: 8; -fx-border-radius: 8;");

        Label t = new Label("Recent System Activity");
        t.setFont(Font.font("Segoe UI", FontWeight.MEDIUM, 17));
        t.setTextFill(Color.web(TEXT()));
        card.getChildren().add(t);

        java.util.List<SystemLog> recentLogs = systemLogService.getRecent(5);
        if (recentLogs.isEmpty()) {
            Label empty = new Label("No recent activity");
            empty.setFont(Font.font("Segoe UI", 13));
            empty.setTextFill(Color.web(MUTED_FG()));
            card.getChildren().add(empty);
        } else {
            for (SystemLog log : recentLogs) {
                HBox item = new HBox();
                item.setPadding(new Insets(8, 0, 8, 0));
                item.setStyle("-fx-border-color: " + BORDER() + "; -fx-border-width: 0 0 1 0;");
                Label name = new Label(log.getDescription());
                name.setFont(Font.font("Segoe UI", 13));
                name.setTextFill(Color.web(TEXT()));
                Region sp = new Region();
                HBox.setHgrow(sp, Priority.ALWAYS);
                Label time = new Label(log.getTimestamp() != null ? log.getTimestamp() : "");
                time.setFont(Font.font("Segoe UI", 11));
                time.setTextFill(Color.web(MUTED_FG()));
                item.getChildren().addAll(name, sp, time);
                card.getChildren().add(item);
            }
        }
        return card;
    }

    private VBox actionCard(String icon, String title, String desc, String targetPage) {
        VBox card = new VBox(4);
        card.setPadding(new Insets(16));
        card.setStyle("-fx-background-color: " + CARD() + "; -fx-border-color: " + BORDER()
                + "; -fx-border-width: 1; -fx-background-radius: 8; -fx-border-radius: 8; -fx-cursor: hand;");
        HBox.setHgrow(card, Priority.ALWAYS);

        Label ic = new Label(icon);
        ic.setFont(Font.font("Segoe UI Emoji", 18));
        ic.setTextFill(Color.web(PRIMARY));
        Label t = new Label(title);
        t.setFont(Font.font("Segoe UI", FontWeight.MEDIUM, 13));
        Label d = new Label(desc);
        d.setFont(Font.font("Segoe UI", 11));
        d.setTextFill(Color.web(MUTED_FG()));

        card.getChildren().addAll(ic, t, d);
        card.setOnMouseClicked(e -> {
            activePage = targetPage;
            refreshSidebar();
            switch (targetPage) {
                case "admin" -> root.setCenter(buildAdminPage());
                case "reports" -> root.setCenter(buildReportsPage());
                case "alerts" -> root.setCenter(buildAlertsPage());
            }
        });
        return card;
    }

    private VBox buildSystemEventsTable() {
        VBox card = new VBox(0);
        card.setStyle("-fx-background-color: " + CARD() + "; -fx-border-color: " + BORDER()
                + "; -fx-border-width: 1; -fx-background-radius: 8; -fx-border-radius: 8;");

        HBox hdr = new HBox();
        hdr.setPadding(new Insets(16));
        hdr.setAlignment(Pos.CENTER_LEFT);
        hdr.setStyle("-fx-border-color: " + BORDER() + "; -fx-border-width: 0 0 1 0;");
        Label t = new Label("Recent System Events");
        t.setFont(Font.font("Segoe UI", FontWeight.MEDIUM, 17));
        Region sp = new Region();
        HBox.setHgrow(sp, Priority.ALWAYS);
        Button viewAll = new Button("View All Logs");
        viewAll.setStyle("-fx-background-color: " + PRIMARY
                + "; -fx-text-fill: white; -fx-background-radius: 4; -fx-padding: 6 12; -fx-font-size: 12px; -fx-cursor: hand;");
        viewAll.setOnAction(e -> {
            activePage = "reports";
            refreshSidebar();
            root.setCenter(buildReportsPage());
        });
        hdr.getChildren().addAll(t, sp, viewAll);

        GridPane grid = new GridPane();
        grid.setHgap(0);
        grid.setVgap(0);
        grid.setPadding(new Insets(0));

        // Header row
        String[] cols = { "Timestamp", "Event Type", "User", "Description", "Status" };
        for (int i = 0; i < cols.length; i++) {
            Label h = new Label(cols[i]);
            h.setFont(Font.font("Segoe UI", FontWeight.SEMI_BOLD, 11));
            h.setPadding(new Insets(8, 16, 8, 16));
            h.setMaxWidth(Double.MAX_VALUE);
            h.setStyle("-fx-background-color: " + MUTED() + ";");
            grid.add(h, i, 0);
        }

        java.util.List<SystemLog> logs = systemLogService.getRecent(5);
        for (int r = 0; r < logs.size(); r++) {
            SystemLog log = logs.get(r);
            String[] rowData = {
                    log.getTimestamp() != null ? log.getTimestamp() : "",
                    log.getEventType() != null ? log.getEventType() : "",
                    log.getActor() != null ? log.getActor() : "",
                    log.getDescription() != null ? log.getDescription() : "",
                    "Success"
            };
            for (int c = 0; c < rowData.length; c++) {
                Label cell = new Label(rowData[c]);
                cell.setFont(Font.font(c == 0 ? "Consolas" : "Segoe UI", c == 0 ? 11 : 13));
                cell.setTextFill(Color.web(c == 0 ? MUTED_FG() : TEXT()));
                cell.setPadding(new Insets(12, 16, 12, 16));
                cell.setMaxWidth(Double.MAX_VALUE);
                cell.setStyle("-fx-border-color: " + BORDER() + "; -fx-border-width: 0 0 1 0;");

                if (c == 4) {
                    Label badge = new Label(rowData[c]);
                    badge.setFont(Font.font("Segoe UI", 11));
                    String bgC = SECONDARY + "1A";
                    String fgC = SECONDARY;
                    badge.setStyle("-fx-background-color: " + bgC + "; -fx-text-fill: " + fgC
                            + "; -fx-background-radius: 4; -fx-padding: 2 8;");
                    HBox wrapper = new HBox(badge);
                    wrapper.setPadding(new Insets(12, 16, 12, 16));
                    wrapper.setStyle("-fx-border-color: " + BORDER() + "; -fx-border-width: 0 0 1 0;");
                    grid.add(wrapper, c, r + 1);
                    continue;
                }
                grid.add(cell, c, r + 1);
            }
        }

        card.getChildren().addAll(hdr, grid);
        return card;
    }

    // ═══════════ CHILDREN PAGE ═══════════
    private ScrollPane buildChildrenPage() {
        VBox page = new VBox(20);
        page.setPadding(new Insets(24));

        Label title = new Label("Child Profiles");
        title.setFont(Font.font("Segoe UI", FontWeight.MEDIUM, 20));
        title.setTextFill(Color.web(TEXT()));
        Label sub = new Label("View and manage child profiles");
        sub.setFont(Font.font("Segoe UI", 13));
        sub.setTextFill(Color.web(MUTED_FG()));

        VBox tableCard = new VBox(0);
        tableCard.setStyle("-fx-background-color: " + CARD() + "; -fx-border-color: " + BORDER()
                + "; -fx-border-width: 1; -fx-background-radius: 8; -fx-border-radius: 8;");

        HBox hdr = new HBox(12);
        hdr.setPadding(new Insets(16));
        hdr.setAlignment(Pos.CENTER_LEFT);
        hdr.setStyle("-fx-border-color: " + BORDER() + "; -fx-border-width: 0 0 1 0;");
        Label tl = new Label("All Children");
        tl.setFont(Font.font("Segoe UI", FontWeight.MEDIUM, 17));
        tl.setTextFill(Color.web(TEXT()));
        Region hdrSpacer = new Region();
        HBox.setHgrow(hdrSpacer, Priority.ALWAYS);
        Button addChildBtn = new Button("+ Add Child");
        addChildBtn.setStyle("-fx-background-color: " + PRIMARY
                + "; -fx-text-fill: white; -fx-background-radius: 4; -fx-padding: 6 16; -fx-font-size: 12px; -fx-cursor: hand;");
        addChildBtn.setOnAction(e -> root.setCenter(buildAddChildForm()));
        hdr.getChildren().addAll(tl, hdrSpacer, addChildBtn);

        GridPane grid = new GridPane();
        String[] cols = { "Child ID", "Name", "Age", "Gender", "Status", "Organization", "Actions" };
        for (int i = 0; i < cols.length; i++) {
            Label h = new Label(cols[i]);
            h.setFont(Font.font("Segoe UI", FontWeight.SEMI_BOLD, 11));
            h.setPadding(new Insets(8, 16, 8, 16));
            h.setMaxWidth(Double.MAX_VALUE);
            h.setStyle("-fx-background-color: " + MUTED() + ";");
            h.setTextFill(Color.web(TEXT()));
            grid.add(h, i, 0);
        }

        java.util.List<Child> children = childService.getAllChildren();
        for (int r = 0; r < children.size(); r++) {
            Child child = children.get(r);
            String[] rowData = {
                    "CH-" + (1000 + child.getId()),
                    child.getName(),
                    String.valueOf(child.getAge()),
                    child.getGender() != null ? child.getGender() : "",
                    child.getStatus() != null ? child.getStatus() : "Active",
                    child.getOrganization() != null ? child.getOrganization() : ""
            };
            for (int c = 0; c < rowData.length; c++) {
                Label cell = new Label(rowData[c]);
                cell.setFont(Font.font(c == 0 ? "Consolas" : "Segoe UI", c == 0 ? 12 : 13));
                cell.setTextFill(Color.web(TEXT()));
                if (c == 1)
                    cell.setFont(Font.font("Segoe UI", FontWeight.MEDIUM, 13));
                cell.setPadding(new Insets(12, 16, 12, 16));
                cell.setStyle("-fx-border-color: " + BORDER() + "; -fx-border-width: 0 0 1 0;");

                if (c == 4) {
                    Label badge = new Label(rowData[c]);
                    badge.setFont(Font.font("Segoe UI", 11));
                    String bgC = rowData[c].equals("Active") ? SECONDARY + "1A" : WARNING + "1A";
                    String fgC = rowData[c].equals("Active") ? SECONDARY : WARNING;
                    badge.setStyle("-fx-background-color: " + bgC + "; -fx-text-fill: " + fgC
                            + "; -fx-background-radius: 4; -fx-padding: 2 8;");
                    HBox w = new HBox(badge);
                    w.setPadding(new Insets(12, 16, 12, 16));
                    w.setStyle("-fx-border-color: " + BORDER() + "; -fx-border-width: 0 0 1 0;");
                    grid.add(w, c, r + 1);
                    continue;
                }
                if (c == 5)
                    cell.setTextFill(Color.web(MUTED_FG()));
                grid.add(cell, c, r + 1);
            }
            // Action column
            HBox actions = new HBox(12);
            actions.setAlignment(Pos.CENTER_LEFT);
            actions.setStyle("-fx-border-color: " + BORDER() + "; -fx-border-width: 0 0 1 0; -fx-padding: 12 16;");

            final int childId = child.getId();
            final String childName = child.getName();

            Hyperlink viewLink = new Hyperlink("View");
            viewLink.setFont(Font.font("Segoe UI", 13));
            viewLink.setTextFill(Color.web(PRIMARY));
            viewLink.setOnAction(e -> root.setCenter(buildChildProfileDetailView(childId)));

            Hyperlink editLink = new Hyperlink("Edit");
            editLink.setFont(Font.font("Segoe UI", 13));
            editLink.setTextFill(Color.web(PRIMARY));
            editLink.setOnAction(e -> root.setCenter(buildEditChildForm(childId)));

            Hyperlink deleteLink = new Hyperlink("Delete");
            deleteLink.setFont(Font.font("Segoe UI", 13));
            deleteLink.setTextFill(Color.web(DESTRUCTIVE));
            deleteLink.setOnAction(e -> {
                Alert confirm = new Alert(Alert.AlertType.CONFIRMATION,
                        "Are you sure you want to delete " + childName + "?",
                        ButtonType.YES, ButtonType.NO);
                confirm.showAndWait().ifPresent(bt -> {
                    if (bt == ButtonType.YES) {
                        childService.deleteChild(childId);
                        systemLogService.save(new SystemLog("Data Update",
                                "Deleted child profile: " + childName, user.getUsername(),
                                LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))));
                        root.setCenter(buildChildrenPage());
                    }
                });
            });

            actions.getChildren().addAll(viewLink, editLink, deleteLink);
            grid.add(actions, 6, r + 1);
        }

        tableCard.getChildren().addAll(hdr, grid);
        page.getChildren().addAll(new VBox(4, title, sub), tableCard);
        ScrollPane sp = new ScrollPane(page);
        sp.setFitToWidth(true);
        sp.setStyle("-fx-background: " + BG() + "; -fx-background-color: " + BG() + ";");
        return sp;
    }

    // ═══════════ ALERTS PAGE ═══════════
    private ScrollPane buildAlertsPage() {
        VBox page = new VBox(20);
        page.setPadding(new Insets(24));

        Label title = new Label("Alerts & Notifications");
        title.setFont(Font.font("Segoe UI", FontWeight.MEDIUM, 20));
        title.setTextFill(Color.web(TEXT()));
        Label sub = new Label("Monitor system alerts and emergencies");
        sub.setFont(Font.font("Segoe UI", 13));
        sub.setTextFill(Color.web(MUTED_FG()));

        long criticalCount = activeAlerts.stream().filter(a -> a[0].equals("critical")).count();
        long warningCount = activeAlerts.stream().filter(a -> a[0].equals("warning")).count();
        long totalActive = activeAlerts.size();

        HBox stats = new HBox(16);
        stats.getChildren().addAll(
                statCard("Total Active", String.valueOf(totalActive), "Requires attention", WARNING, MUTED_FG()),
                statCard("Critical", String.valueOf(criticalCount), "Immediate action needed", DESTRUCTIVE,
                        DESTRUCTIVE),
                statCard("Warnings", String.valueOf(warningCount), "Review soon", WARNING, WARNING),
                statCard("Resolved Today", "0", "Completed", SECONDARY, SECONDARY));

        Label activeTitle = new Label("Active Alerts");
        activeTitle.setFont(Font.font("Segoe UI", FontWeight.MEDIUM, 17));
        activeTitle.setTextFill(Color.web(TEXT()));

        VBox alertsList = new VBox(12);
        for (String[] a : activeAlerts) {
            String status = a[0];
            String borderC = status.equals("critical") ? DESTRUCTIVE : status.equals("warning") ? WARNING : PRIMARY;
            String bgC = status.equals("critical") ? DESTRUCTIVE + "0D"
                    : status.equals("warning") ? WARNING + "0D" : PRIMARY + "0D";
            String iconC = status.equals("critical") ? DESTRUCTIVE : status.equals("warning") ? WARNING : PRIMARY;
            String iconChar = status.equals("critical") ? "\u2757" : status.equals("warning") ? "\u26A0" : "\u2139";

            VBox alertCard = new VBox(8);
            alertCard.setPadding(new Insets(16));
            alertCard.setStyle("-fx-background-color: " + CARD() + "; -fx-border-color: " + borderC
                    + "; -fx-border-width: " + (status.equals("critical") ? "2" : "1")
                    + "; -fx-background-radius: 8; -fx-border-radius: 8;");

            HBox top = new HBox(12);
            top.setAlignment(Pos.TOP_LEFT);
            StackPane iconBox = new StackPane();
            iconBox.setPrefSize(40, 40);
            iconBox.setStyle("-fx-background-color: " + bgC + "; -fx-background-radius: 4;");
            Label icon = new Label(iconChar);
            icon.setFont(Font.font("Segoe UI Emoji", 18));
            icon.setTextFill(Color.web(iconC));
            iconBox.getChildren().add(icon);

            VBox info = new VBox(4);
            HBox.setHgrow(info, Priority.ALWAYS);
            Label aTitle = new Label(a[1]);
            aTitle.setFont(Font.font("Segoe UI", FontWeight.BOLD, 14));
            aTitle.setTextFill(Color.web(iconC));

            Label aId = new Label(a[2]);
            aId.setFont(Font.font("Segoe UI", 11));
            aId.setTextFill(Color.web(MUTED_FG()));
            Label aDesc = new Label(a[4]);
            aDesc.setFont(Font.font("Segoe UI", 13));
            aDesc.setTextFill(Color.web(TEXT()));
            aDesc.setWrapText(true);

            info.getChildren().addAll(aTitle, aId, aDesc);
            if (!a[5].isEmpty()) {
                Label related = new Label("Related Child: " + a[5]);
                related.setFont(Font.font("Consolas", 11));
                related.setTextFill(Color.web(MUTED_FG()));
                info.getChildren().add(related);
            }

            HBox btns = new HBox(12);
            btns.setPadding(new Insets(8, 0, 0, 0));
            Button vd = new Button("View Details");
            vd.setStyle("-fx-background-color: " + PRIMARY
                    + "; -fx-text-fill: white; -fx-background-radius: 4; -fx-padding: 6 16; -fx-font-size: 12px; -fx-cursor: hand; -fx-font-weight: bold;");
            final String[] alertData = a;
            vd.setOnAction(e -> root.setCenter(buildAlertDetailView(alertData)));

            Button mr = new Button("Mark Resolved");
            mr.setStyle("-fx-background-color: transparent; -fx-text-fill: " + SECONDARY
                    + "; -fx-border-color: " + SECONDARY
                    + "; -fx-border-radius: 4; -fx-padding: 6 16; -fx-font-size: 12px; -fx-cursor: hand; -fx-font-weight: bold;");

            mr.setOnAction(e -> {
                activeAlerts.remove(alertData);
                root.setCenter(buildAlertsPage());
            });

            btns.getChildren().addAll(vd, mr);
            info.getChildren().add(btns);

            Label time = new Label(a[3]);
            time.setFont(Font.font("Segoe UI", 11));
            time.setTextFill(Color.web(MUTED_FG()));

            top.getChildren().addAll(iconBox, info, time);
            alertCard.getChildren().add(top);
            alertsList.getChildren().add(alertCard);
        }

        page.getChildren().addAll(new VBox(4, title, sub), stats, activeTitle, alertsList);
        ScrollPane sp = new ScrollPane(page);
        sp.setFitToWidth(true);
        sp.setStyle("-fx-background: " + BG() + "; -fx-background-color: " + BG() + ";");
        return sp;
    }

    // ═══════════ REPORTS PAGE ═══════════
    private ScrollPane buildReportsPage() {
        VBox page = new VBox(20);
        page.setPadding(new Insets(24));

        Label title = new Label("Reports & Audit Logs");
        title.setFont(Font.font("Segoe UI", FontWeight.MEDIUM, 20));
        Label sub = new Label("Generate reports and view system audit trails");
        sub.setFont(Font.font("Segoe UI", 13));
        sub.setTextFill(Color.web(MUTED_FG()));

        // Report Generator controls
        VBox genCard = new VBox(16);
        genCard.setPadding(new Insets(16));
        genCard.setStyle("-fx-background-color: " + CARD() + "; -fx-border-color: " + BORDER()
                + "; -fx-border-width: 1; -fx-background-radius: 8; -fx-border-radius: 8;");
        Label genTitle = new Label("Report Generator");
        genTitle.setFont(Font.font("Segoe UI", FontWeight.MEDIUM, 17));
        genTitle.setTextFill(Color.web(TEXT()));

        HBox row1 = new HBox(24);
        VBox col1 = new VBox(8);
        Label rtLabel = new Label("Report Type");
        rtLabel.setFont(Font.font("Segoe UI", FontWeight.MEDIUM, 13));
        rtLabel.setTextFill(Color.web(TEXT()));
        ComboBox<String> rtCombo = new ComboBox<>();
        rtCombo.getItems().addAll("Donation & Financial Report", "Child Welfare Summary", "System Audit Log",
                "Performance Analytics");
        rtCombo.setValue("Donation & Financial Report");
        rtCombo.setMaxWidth(Double.MAX_VALUE);
        col1.getChildren().addAll(rtLabel, rtCombo);
        HBox.setHgrow(col1, Priority.ALWAYS);

        VBox col2 = new VBox(8);
        Label drLabel = new Label("Date Range");
        drLabel.setFont(Font.font("Segoe UI", FontWeight.MEDIUM, 13));
        drLabel.setTextFill(Color.web(TEXT()));
        ComboBox<String> drCombo = new ComboBox<>();
        drCombo.getItems().addAll("Last 7 Days", "Last 30 Days", "Last 3 Months", "Last Year");
        drCombo.setValue("Last 30 Days");
        drCombo.setMaxWidth(Double.MAX_VALUE);
        col2.getChildren().addAll(drLabel, drCombo);
        HBox.setHgrow(col2, Priority.ALWAYS);
        row1.getChildren().addAll(col1, col2);

        // Load donations from DB
        java.util.List<Donation> allDonations = donationService.getAll();
        java.util.List<Child> allChildren = childService.getAllChildren();
        java.util.List<User> allUsersForReport = userService.getAllUsers();
        java.util.Map<Integer, String> childNames = new java.util.HashMap<>();
        for (Child ch : allChildren)
            childNames.put(ch.getId(), ch.getName());
        java.util.Map<Integer, String> userNames = new java.util.HashMap<>();
        for (User u : allUsersForReport)
            userNames.put(u.getId(), u.getUsername());

        double totalDonated = allDonations.stream().mapToDouble(Donation::getAmount).sum();

        HBox buttons = new HBox(12);
        Button genBtn = new Button("\uD83D\uDCC4  Generate Report");
        genBtn.setStyle("-fx-background-color: " + PRIMARY
                + "; -fx-text-fill: white; -fx-background-radius: 4; -fx-padding: 8 16; -fx-font-size: 13px; -fx-cursor: hand;");
        genBtn.setOnAction(e -> {
            systemLogService.save(new SystemLog("Report", "Generated " + rtCombo.getValue(), user.getUsername(),
                    LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))));
            root.setCenter(buildReportsPage());
        });
        Button csvBtn = new Button("\u2B07  Export to CSV");
        csvBtn.setStyle("-fx-background-color: " + SECONDARY
                + "; -fx-text-fill: white; -fx-background-radius: 4; -fx-padding: 8 16; -fx-font-size: 13px; -fx-cursor: hand;");
        csvBtn.setOnAction(e -> {
            FileChooser fc = new FileChooser();
            fc.setTitle("Export CSV");
            fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("CSV Files", "*.csv"));
            fc.setInitialFileName("donation_report.csv");
            File file = fc.showSaveDialog(stage);
            if (file != null) {
                try (FileWriter fw = new FileWriter(file)) {
                    fw.write("Donor,Child,Amount,Date,Purpose\n");
                    for (Donation d : allDonations) {
                        fw.write(String.format("%s,%s,%.0f,%s,%s\n",
                                userNames.getOrDefault(d.getDonorId(), "Unknown"),
                                childNames.getOrDefault(d.getChildId(), "Unknown"),
                                d.getAmount(), d.getDate(), d.getPurpose()));
                    }
                    systemLogService.save(new SystemLog("Export", "Exported donation CSV", user.getUsername(),
                            LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))));
                    new Alert(Alert.AlertType.INFORMATION, "CSV exported successfully!").show();
                } catch (IOException ex) {
                    new Alert(Alert.AlertType.ERROR, "Export failed: " + ex.getMessage()).show();
                }
            }
        });
        buttons.getChildren().addAll(genBtn, csvBtn);

        genCard.getChildren().addAll(genTitle, row1, buttons);

        // Donation Report table
        VBox reportCard = new VBox(0);
        reportCard.setStyle("-fx-background-color: " + CARD() + "; -fx-border-color: " + BORDER()
                + "; -fx-border-width: 1; -fx-background-radius: 8; -fx-border-radius: 8;");

        HBox rHdr = new HBox();
        rHdr.setPadding(new Insets(16));
        rHdr.setStyle("-fx-border-color: " + BORDER() + "; -fx-border-width: 0 0 1 0;");
        Label rTitle = new Label("Report Preview - Donation & Financial Report");
        rTitle.setFont(Font.font("Segoe UI", FontWeight.MEDIUM, 17));
        rTitle.setTextFill(Color.web(TEXT()));
        rHdr.getChildren().add(rTitle);

        // Summary stats from DB
        HBox summaryStats = new HBox(16);
        summaryStats.setPadding(new Insets(16));
        String totalStr = String.format("\u09F3%,.0f", totalDonated);
        for (String[] s : new String[][] {
                { "Total Donations", totalStr, null },
                { "Total Records", String.valueOf(allDonations.size()), null },
                { "Children Covered", String.valueOf(childNames.size()), SECONDARY }
        }) {
            VBox sBox = new VBox(4);
            sBox.setPadding(new Insets(12));
            sBox.setStyle("-fx-background-color: " + MUTED() + "; -fx-background-radius: 4;");
            HBox.setHgrow(sBox, Priority.ALWAYS);
            Label sl = new Label(s[0]);
            sl.setFont(Font.font("Segoe UI", 11));
            sl.setTextFill(Color.web(MUTED_FG()));
            Label sv = new Label(s[1]);
            sv.setFont(Font.font("Segoe UI", FontWeight.SEMI_BOLD, 20));
            sv.setTextFill(Color.web(TEXT()));
            if (s[2] != null)
                sv.setTextFill(Color.web(s[2]));
            sBox.getChildren().addAll(sl, sv);
            summaryStats.getChildren().add(sBox);
        }

        GridPane donGrid = new GridPane();
        String[] donCols = { "Donor", "Child", "Amount", "Date", "Purpose" };
        for (int i = 0; i < donCols.length; i++) {
            Label h = new Label(donCols[i]);
            h.setFont(Font.font("Segoe UI", FontWeight.SEMI_BOLD, 11));
            h.setPadding(new Insets(8, 12, 8, 12));
            h.setStyle("-fx-background-color: " + MUTED() + ";");
            h.setMaxWidth(Double.MAX_VALUE);
            h.setTextFill(Color.web(TEXT()));
            donGrid.add(h, i, 0);
        }
        for (int r = 0; r < allDonations.size(); r++) {
            Donation don = allDonations.get(r);
            String[] rowData = {
                    userNames.getOrDefault(don.getDonorId(), "Donor #" + don.getDonorId()),
                    childNames.getOrDefault(don.getChildId(), "Child #" + don.getChildId()),
                    String.format("\u09F3%,.0f", don.getAmount()),
                    don.getDate() != null ? don.getDate() : "",
                    don.getPurpose() != null ? don.getPurpose() : ""
            };
            for (int c = 0; c < rowData.length; c++) {
                Label cell = new Label(rowData[c]);
                cell.setFont(Font.font("Segoe UI", 13));
                cell.setTextFill(Color.web(TEXT()));
                cell.setPadding(new Insets(8, 12, 8, 12));
                cell.setStyle("-fx-border-color: " + BORDER() + "; -fx-border-width: 0 0 1 0;");
                if (c == 2) {
                    cell.setTextFill(Color.web(SECONDARY));
                    cell.setFont(Font.font("Segoe UI", FontWeight.SEMI_BOLD, 13));
                }
                if (c == 3)
                    cell.setTextFill(Color.web(MUTED_FG()));
                donGrid.add(cell, c, r + 1);
            }
        }

        reportCard.getChildren().addAll(rHdr, summaryStats, donGrid);

        // Quick stats at bottom
        int totalLogCount = systemLogService.getCount();
        HBox qStats = new HBox(16);
        qStats.getChildren().addAll(
                statCard("Reports Generated", String.valueOf(systemLogService.getRecent(100).stream()
                        .filter(l -> l.getEventType() != null && l.getEventType().equals("Report")).count()),
                        "All time", PRIMARY, MUTED_FG()),
                statCard("Total Donations", String.valueOf(allDonations.size()), "Records", SECONDARY, SECONDARY),
                statCard("Audit Entries", String.valueOf(totalLogCount), "Total logged events", PRIMARY, MUTED_FG()),
                statCard("Children", String.valueOf(allChildren.size()), "In system", PRIMARY, MUTED_FG()));

        page.getChildren().addAll(new VBox(4, title, sub), genCard, reportCard, qStats);
        ScrollPane sp = new ScrollPane(page);
        sp.setFitToWidth(true);
        sp.setStyle("-fx-background: " + BG() + "; -fx-background-color: " + BG() + ";");
        return sp;
    }

    // ═══════════ ADMIN (USER MANAGEMENT) PAGE ═══════════
    private ScrollPane buildAdminPage() {
        return buildAdminPage("");
    }

    private ScrollPane buildAdminPage(String searchQuery) {
        VBox page = new VBox(20);
        page.setPadding(new Insets(24));

        Label title = new Label("System Administration");
        title.setFont(Font.font("Segoe UI", FontWeight.MEDIUM, 20));
        Label sub = new Label("Manage users, roles, and system configuration");
        sub.setFont(Font.font("Segoe UI", 13));
        sub.setTextFill(Color.web(MUTED_FG()));

        // Load users from database
        java.util.List<User> dbUsers = userService.getAllUsers();
        int totalUsers = dbUsers.size();
        int pendingUsers = (int) dbUsers.stream().filter(u -> !u.isApproved()).count();
        int activeUsers = totalUsers - pendingUsers;

        HBox stats = new HBox(16);
        stats.getChildren().addAll(
                statCard("Total Users", String.valueOf(totalUsers), pendingUsers + " pending approval", PRIMARY,
                        SECONDARY),
                statCard("Active Users", String.valueOf(activeUsers),
                        ((activeUsers * 100 / Math.max(1, totalUsers)) + "% of total"), SECONDARY, MUTED_FG()),
                statCard("Pending Approval", String.valueOf(pendingUsers), "Awaiting verification", WARNING, WARNING),
                statCard("Roles Configured", "3", "System roles", PRIMARY, MUTED_FG()));

        // User Management table
        VBox userCard = new VBox(0);
        userCard.setStyle("-fx-background-color: " + CARD() + "; -fx-border-color: " + BORDER()
                + "; -fx-border-width: 1; -fx-background-radius: 8; -fx-border-radius: 8;");

        HBox hdr = new HBox(12);
        hdr.setPadding(new Insets(16));
        hdr.setAlignment(Pos.CENTER_LEFT);
        hdr.setStyle("-fx-border-color: " + BORDER() + "; -fx-border-width: 0 0 1 0;");
        Label tl = new Label("User Management");
        tl.setFont(Font.font("Segoe UI", FontWeight.MEDIUM, 17));
        Region sp1 = new Region();
        HBox.setHgrow(sp1, Priority.ALWAYS);
        TextField search = new TextField(searchQuery);
        search.setPromptText("Search users...");
        search.setPrefWidth(256);
        search.setStyle("-fx-background-color: " + CARD() + "; -fx-border-color: " + BORDER()
                + "; -fx-border-width: 1; -fx-border-radius: 4; -fx-background-radius: 4; -fx-padding: 6 12; -fx-font-size: 12px;");
        search.setOnKeyReleased(e -> {
            if (e.getCode().toString().equals("ENTER") || search.getText().isEmpty()) {
                root.setCenter(buildAdminPage(search.getText()));
            }
        });

        Button addUser = new Button("+ Add User");
        addUser.setStyle("-fx-background-color: " + PRIMARY
                + "; -fx-text-fill: white; -fx-background-radius: 4; -fx-padding: 6 16; -fx-font-size: 12px; -fx-cursor: hand;");
        addUser.setOnAction(e -> root.setCenter(buildAddUserForm(null)));
        hdr.getChildren().addAll(tl, sp1, search, addUser);

        GridPane grid = new GridPane();
        String[] cols = { "User ID", "Name", "Email", "Role", "Status", "Last Login", "Actions" };
        for (int i = 0; i < cols.length; i++) {
            Label h = new Label(cols[i]);
            h.setFont(Font.font("Segoe UI", FontWeight.SEMI_BOLD, 11));
            h.setPadding(new Insets(8, 16, 8, 16));
            h.setMaxWidth(Double.MAX_VALUE);
            h.setStyle("-fx-background-color: " + MUTED() + ";");
            grid.add(h, i, 0);
        }

        int rowCount = 1;
        for (User dbUser : dbUsers) {
            // Convert User object to display format
            String userId = "USR-" + String.format("%03d", dbUser.getId());
            String name = dbUser.getUsername();
            String email = dbUser.getUsername() + "@example.com"; // placeholder
            String role = dbUser.getRole().toString().replace("_", " ");
            String status = dbUser.isApproved() ? "Active" : "Pending";
            String lastLogin = "N/A";

            String[] u = new String[] { userId, name, email, role, status, lastLogin };

            // Filter logic
            if (!searchQuery.isEmpty()) {
                boolean match = false;
                for (String s : u) {
                    if (s.toLowerCase().contains(searchQuery.toLowerCase())) {
                        match = true;
                        break;
                    }
                }
                if (!match)
                    continue;
            }

            for (int c = 0; c < u.length; c++) {
                Label cell = new Label(u[c]);
                cell.setFont(Font.font(c == 0 ? "Consolas" : "Segoe UI", c == 0 ? 12 : 13));
                if (c == 1)
                    cell.setFont(Font.font("Segoe UI", FontWeight.MEDIUM, 13));
                if (c == 2 || c == 5)
                    cell.setTextFill(Color.web(MUTED_FG()));
                cell.setPadding(new Insets(12, 16, 12, 16));
                cell.setStyle("-fx-border-color: " + BORDER() + "; -fx-border-width: 0 0 1 0;");

                if (c == 4) {
                    Label badge = new Label(u[c]);
                    badge.setFont(Font.font("Segoe UI", 11));
                    String bc = u[c].equals("Active") ? SECONDARY + "1A" : WARNING + "1A";
                    String fc = u[c].equals("Active") ? SECONDARY : WARNING;
                    badge.setStyle("-fx-background-color: " + bc + "; -fx-text-fill: " + fc
                            + "; -fx-background-radius: 4; -fx-padding: 2 8;");
                    HBox w = new HBox(badge);
                    w.setPadding(new Insets(12, 16, 12, 16));
                    w.setStyle("-fx-border-color: " + BORDER() + "; -fx-border-width: 0 0 1 0;");
                    grid.add(w, c, rowCount);
                    continue;
                }
                grid.add(cell, c, rowCount);
            }
            // Actions
            HBox actions = new HBox(8);
            actions.setPadding(new Insets(12, 16, 12, 16));
            actions.setStyle("-fx-border-color: " + BORDER() + "; -fx-border-width: 0 0 1 0;");
            Button edit = new Button("\u270E");
            edit.setStyle("-fx-background-color: transparent; -fx-text-fill: " + PRIMARY
                    + "; -fx-padding: 4; -fx-cursor: hand;");

            final int userIdToEdit = dbUser.getId();
            edit.setOnAction(e -> root.setCenter(buildAddUserForm(dbUser)));

            Button del = new Button("\u2716");
            del.setStyle("-fx-background-color: transparent; -fx-text-fill: " + DESTRUCTIVE
                    + "; -fx-padding: 4; -fx-cursor: hand;");
            final int userIdToDelete = dbUser.getId();
            del.setOnAction(e -> {
                userService.deleteUser(userIdToDelete);
                root.setCenter(buildAdminPage(searchQuery));
            });

            actions.getChildren().addAll(edit, del);

            // Add Approve button if user is Pending
            if (u[4].equals("Pending")) {
                Button approve = new Button("\u2714");
                approve.setStyle("-fx-background-color: transparent; -fx-text-fill: " + SECONDARY
                        + "; -fx-padding: 4; -fx-cursor: hand;");
                approve.setTooltip(new Tooltip("Approve User"));
                final int userIdToApprove = dbUser.getId();
                approve.setOnAction(e -> {
                    userService.approveUser(userIdToApprove);
                    root.setCenter(buildAdminPage(searchQuery));
                });
                actions.getChildren().add(0, approve);
            }

            grid.add(actions, 6, rowCount);
            rowCount++;
        }

        userCard.getChildren().addAll(hdr, grid);

        // Role Configuration
        VBox roleCard = new VBox(0);
        roleCard.setStyle("-fx-background-color: " + CARD() + "; -fx-border-color: " + BORDER()
                + "; -fx-border-width: 1; -fx-background-radius: 8; -fx-border-radius: 8;");
        HBox rHdr = new HBox();
        rHdr.setPadding(new Insets(16));
        rHdr.setStyle("-fx-border-color: " + BORDER() + "; -fx-border-width: 0 0 1 0;");
        Label rTitle = new Label("Role Configuration & Permissions");
        rTitle.setFont(Font.font("Segoe UI", FontWeight.MEDIUM, 17));
        rHdr.getChildren().add(rTitle);

        VBox rolesContent = new VBox(12);
        rolesContent.setPadding(new Insets(16));

        // Load real-time user counts from database
        java.util.List<User> allUsers = userService.getAllUsers();
        java.util.Map<String, Long> roleCounts = new java.util.HashMap<>();
        for (User u : allUsers) {
            String roleName = u.getRole().toString();
            roleCounts.put(roleName, roleCounts.getOrDefault(roleName, 0L) + 1);
        }

        // Load role permissions from database
        java.util.Map<String, String> rolePermissions = rolePermissionsService.getAllPermissions();

        // Create role cards with real-time counts
        for (String roleName : rolePermissions.keySet()) {
            VBox roleBox = new VBox(8);
            roleBox.setPadding(new Insets(16));
            roleBox.setStyle("-fx-background-color: " + MUTED() + "; -fx-background-radius: 8;");

            HBox roleHdr = new HBox();
            VBox roleInfo = new VBox(2);

            // Display role name in readable format
            String displayName = roleName.replace("_", " ");
            Label rn = new Label(displayName);
            rn.setFont(Font.font("Segoe UI", FontWeight.MEDIUM, 14));

            // Show real-time user count
            long userCount = roleCounts.getOrDefault(roleName, 0L);
            Label ru = new Label(userCount + " users");
            ru.setFont(Font.font("Segoe UI", 11));
            ru.setTextFill(Color.web(MUTED_FG()));
            roleInfo.getChildren().addAll(rn, ru);

            Region rsp = new Region();
            HBox.setHgrow(rsp, Priority.ALWAYS);
            Button ep = new Button("Edit Permissions");
            ep.setStyle("-fx-background-color: " + PRIMARY
                    + "; -fx-text-fill: white; -fx-background-radius: 4; -fx-padding: 6 12; -fx-font-size: 12px; -fx-cursor: hand;");
            ep.setOnAction(e -> root.setCenter(buildEditPermissionsView(roleName, rolePermissions.get(roleName))));
            roleHdr.getChildren().addAll(roleInfo, rsp, ep);

            FlowPane perms = new FlowPane(8, 8);
            String permissions = rolePermissions.get(roleName);
            for (String p : permissions.split(", ")) {
                Label pLabel = new Label(p);
                pLabel.setFont(Font.font("Segoe UI", 11));
                pLabel.setPadding(new Insets(4, 8, 4, 8));
                pLabel.setStyle("-fx-background-color: " + CARD() + "; -fx-background-radius: 4;");
                perms.getChildren().add(pLabel);
            }

            roleBox.getChildren().addAll(roleHdr, perms);
            rolesContent.getChildren().add(roleBox);
        }
        roleCard.getChildren().addAll(rHdr, rolesContent);

        page.getChildren().addAll(new VBox(4, title, sub), stats, userCard, roleCard);
        ScrollPane scp = new ScrollPane(page);
        scp.setFitToWidth(true);
        scp.setStyle("-fx-background: " + BG() + "; -fx-background-color: " + BG() + ";");
        return scp;
    }

    private ScrollPane buildAddUserForm(User userToEdit) {
        VBox page = new VBox(24);
        page.setPadding(new Insets(32));
        page.setMaxWidth(600);

        Button backBtn = new Button("\u2190 Back to User Management");
        backBtn.setStyle("-fx-background-color: transparent; -fx-text-fill: " + PRIMARY + "; -fx-cursor: hand;");
        backBtn.setOnAction(e -> root.setCenter(buildAdminPage()));

        Label title = new Label(userToEdit == null ? "Add New User" : "Edit User");
        title.setFont(Font.font("Segoe UI", FontWeight.BOLD, 24));
        title.setTextFill(Color.web(TEXT()));

        GridPane form = new GridPane();
        form.setHgap(20);
        form.setVgap(20);

        String fieldStyle = "-fx-background-color: " + CARD() + "; -fx-border-color: " + BORDER() +
                "; -fx-border-radius: 4; -fx-padding: 8; -fx-text-fill: " + TEXT() +
                "; -fx-prompt-text-fill: " + MUTED_FG() + ";";

        // Username field
        TextField nameTf = new TextField(userToEdit != null ? userToEdit.getUsername() : "");
        nameTf.setPromptText("Enter username");
        nameTf.setStyle(fieldStyle);

        Label lbl1 = new Label("Username");
        lbl1.setTextFill(Color.web(TEXT()));
        form.add(lbl1, 0, 0);
        form.add(nameTf, 1, 0);

        // Password fields (required for new user, optional for edit)
        PasswordField passTf = new PasswordField();
        passTf.setPromptText(
                userToEdit == null ? "Enter password" : "Enter new password (leave blank to keep current)");
        passTf.setStyle(fieldStyle);

        PasswordField confirmTf = new PasswordField();
        confirmTf.setPromptText(userToEdit == null ? "Confirm password" : "Confirm new password");
        confirmTf.setStyle(fieldStyle);

        Label lbl2 = new Label("Password");
        lbl2.setTextFill(Color.web(TEXT()));
        Label lbl3 = new Label("Confirm Password");
        lbl3.setTextFill(Color.web(TEXT()));

        form.add(lbl2, 0, 1);
        form.add(passTf, 1, 1);
        form.add(lbl3, 0, 2);
        form.add(confirmTf, 1, 2);

        // Email field
        TextField emailTf = new TextField(
                userToEdit != null && userToEdit.getEmail() != null ? userToEdit.getEmail() : "");
        emailTf.setPromptText("Enter email address");
        emailTf.setStyle(fieldStyle);

        Label lblEmail = new Label("Email");
        lblEmail.setTextFill(Color.web(TEXT()));
        form.add(lblEmail, 0, 3);
        form.add(emailTf, 1, 3);

        // Role selection (only for new user)
        int currentRow = 4;
        ComboBox<String> roleCombo = null;
        if (userToEdit == null) {
            Label lbl4 = new Label("Role");
            lbl4.setTextFill(Color.web(TEXT()));

            ComboBox<String> roleComboBox = new ComboBox<>();
            roleComboBox.getItems().addAll("System Admin", "Organization Admin", "Donor", "Caregiver", "Support");
            roleComboBox.setPromptText("Select role");
            roleComboBox.setStyle(fieldStyle + " -fx-background-radius: 4;");

            // Theme the ComboBox button cell and dropdown
            roleComboBox.setButtonCell(new ListCell<String>() {
                @Override
                protected void updateItem(String item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || item == null) {
                        setText(null);
                        setStyle("-fx-text-fill: " + MUTED_FG() + ";");
                    } else {
                        setText(item);
                        setStyle("-fx-text-fill: " + TEXT() + ";");
                    }
                }
            });

            roleComboBox.setCellFactory(lv -> new ListCell<String>() {
                @Override
                protected void updateItem(String item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || item == null) {
                        setText(null);
                    } else {
                        setText(item);
                        setStyle("-fx-text-fill: " + TEXT() + "; -fx-background-color: " + CARD() + ";");
                    }
                }
            });

            // Style dropdown background - use final local variable for lambda
            final ComboBox<String> finalRoleComboBox = roleComboBox;
            roleComboBox.skinProperty().addListener((obs, oldSkin, newSkin) -> {
                if (newSkin != null) {
                    Node popup = finalRoleComboBox.lookup(".list-view");
                    if (popup != null) {
                        popup.setStyle("-fx-background-color: " + CARD() + "; -fx-border-color: " + BORDER() + ";");
                    }
                }
            });

            form.add(lbl4, 0, currentRow);
            form.add(roleComboBox, 1, currentRow);
            roleCombo = roleComboBox; // Assign to outer variable for later access
            currentRow++;
        } else {
            // Show role as read-only for existing users
            Label roleLbl = new Label("Role: " + userToEdit.getRole().toString().replace("_", " "));
            roleLbl.setFont(Font.font("Segoe UI", 13));
            roleLbl.setTextFill(Color.web(MUTED_FG()));

            Label lbl4 = new Label("");
            form.add(lbl4, 0, currentRow);
            form.add(roleLbl, 1, currentRow);
            currentRow++;
        }

        // Approval status
        CheckBox approvedCb = new CheckBox("Approved");
        approvedCb.setSelected(userToEdit != null && userToEdit.isApproved());
        approvedCb.setTextFill(Color.web(TEXT()));

        Label lbl5 = new Label("Status");
        lbl5.setTextFill(Color.web(TEXT()));
        form.add(lbl5, 0, currentRow);
        form.add(approvedCb, 1, currentRow);

        // Error label for validation messages
        Label errorLabel = new Label();
        errorLabel.setTextFill(Color.web(DESTRUCTIVE));
        errorLabel.setWrapText(true);
        errorLabel.setVisible(false);

        // Submit button
        ComboBox<String> finalRoleCombo = roleCombo;
        Button submit = new Button(userToEdit == null ? "Create User Account" : "Update User Account");
        submit.setMaxWidth(Double.MAX_VALUE);
        submit.setStyle("-fx-background-color: " + PRIMARY
                + "; -fx-text-fill: white; -fx-background-radius: 4; -fx-padding: 12; -fx-font-weight: bold; -fx-cursor: hand;");
        submit.setOnAction(e -> {
            String username = nameTf.getText().trim();
            String password = passTf.getText();
            String confirmPass = confirmTf.getText();
            String email = emailTf.getText().trim();

            // Hide previous errors
            errorLabel.setVisible(false);

            // Validation
            if (username.isEmpty()) {
                errorLabel.setText("Username is required");
                errorLabel.setVisible(true);
                return;
            }

            if (userToEdit == null) {
                // Creating new user - password and role are required
                if (password.isEmpty()) {
                    errorLabel.setText("Password is required");
                    errorLabel.setVisible(true);
                    return;
                }

                if (!password.equals(confirmPass)) {
                    errorLabel.setText("Passwords do not match");
                    errorLabel.setVisible(true);
                    return;
                }

                if (finalRoleCombo.getValue() == null) {
                    errorLabel.setText("Please select a role");
                    errorLabel.setVisible(true);
                    return;
                }

                // Create new user based on selected role
                String hashedPassword = PasswordUtil.hash(password);
                User newUser = null;
                String selectedRole = finalRoleCombo.getValue();

                if (selectedRole.equals("System Admin")) {
                    newUser = new SystemAdmin(username, hashedPassword);
                } else if (selectedRole.equals("Organization Admin")) {
                    newUser = new OrganizationAdmin(username, hashedPassword);
                } else if (selectedRole.equals("Donor")) {
                    newUser = new Donor(username, hashedPassword);
                } else if (selectedRole.equals("Caregiver")) {
                    newUser = new Caregiver(username, hashedPassword);
                } else if (selectedRole.equals("Support")) {
                    newUser = new Support(username, hashedPassword);
                }

                if (newUser != null) {
                    newUser.setEmail(email);
                    newUser.setApproved(approvedCb.isSelected());
                    boolean created = userService.createUser(newUser);
                    if (created) {
                        root.setCenter(buildAdminPage());
                    } else {
                        errorLabel.setText("Username already exists or failed to create user");
                        errorLabel.setVisible(true);
                    }
                }
            } else {
                // Updating existing user
                userToEdit.setUsername(username);
                userToEdit.setEmail(email);
                userToEdit.setApproved(approvedCb.isSelected());

                // Update password only if new password is provided
                if (!password.isEmpty()) {
                    if (!password.equals(confirmPass)) {
                        errorLabel.setText("Passwords do not match");
                        errorLabel.setVisible(true);
                        return;
                    }
                    userToEdit.setPassword(PasswordUtil.hash(password));
                }

                userService.updateUser(userToEdit);
                root.setCenter(buildAdminPage());
            }
        });

        page.getChildren().addAll(backBtn, title, form, errorLabel, submit);
        ScrollPane scp = new ScrollPane(page);
        scp.setFitToWidth(true);
        scp.setStyle("-fx-background: " + BG() + "; -fx-background-color: " + BG() + ";");
        return scp;
    }

    private ScrollPane buildEditPermissionsView(String roleName, String currentPerms) {
        VBox page = new VBox(24);
        page.setPadding(new Insets(32));
        page.setMaxWidth(800);

        Button backBtn = new Button("\u2190 Back to System Admin");
        backBtn.setStyle("-fx-background-color: transparent; -fx-text-fill: " + PRIMARY + "; -fx-cursor: hand;");
        backBtn.setOnAction(e -> root.setCenter(buildAdminPage()));

        Label title = new Label("Edit Permissions: " + roleName);
        title.setFont(Font.font("Segoe UI", FontWeight.BOLD, 24));

        VBox permsList = new VBox(12);
        String[] allPossiblePerms = {
                "View Dashboard", "Manage Users", "Edit Roles", "View Reports", "Configure System",
                "Delete Records", "Approve Transactions", "Audit Logs", "Manage Backup",
                "View own profile", "View sponsorship status", "View wallet balance",
                "Manage child profiles", "Update medical records", "Update education records", "View all children",
                "View sponsored children", "Make donations", "View transaction history", "Generate reports",
                "User verification", "Monitor alerts", "View activity logs", "Resolve issues",
                "Full system access", "User management", "Role configuration", "System settings"
        };

        java.util.List<CheckBox> checkBoxes = new java.util.ArrayList<>();
        for (String p : allPossiblePerms) {
            CheckBox cb = new CheckBox(p);
            cb.setSelected(currentPerms.contains(p));
            cb.setFont(Font.font("Segoe UI", 14));
            cb.setTextFill(Color.web(TEXT()));
            permsList.getChildren().add(cb);
            checkBoxes.add(cb);
        }

        Button save = new Button("Save Permission Changes");
        save.setStyle("-fx-background-color: " + PRIMARY
                + "; -fx-text-fill: white; -fx-background-radius: 4; -fx-padding: 12 24; -fx-font-weight: bold; -fx-cursor: hand;");
        save.setOnAction(e -> {
            // Collect selected perms
            StringBuilder sb = new StringBuilder();
            for (CheckBox cb : checkBoxes) {
                if (cb.isSelected()) {
                    if (sb.length() > 0)
                        sb.append(", ");
                    sb.append(cb.getText());
                }
            }

            // Save permissions to database permanently
            rolePermissionsService.updatePermissions(roleName, sb.toString());

            // Return to admin page and refresh to show updated permissions
            root.setCenter(buildAdminPage());
        });

        page.getChildren().addAll(backBtn, title, new Label("Select the permissions for this role:"), permsList, save);
        ScrollPane scp = new ScrollPane(page);
        scp.setFitToWidth(true);
        scp.setStyle("-fx-background: " + BG() + "; -fx-background-color: " + BG() + ";");
        return scp;
    }

    private ScrollPane buildChildProfileDetailView(int childId) {
        Child child = childService.getChildById(childId);
        if (child == null) {
            VBox err = new VBox(new Label("Child not found."));
            err.setPadding(new Insets(24));
            return wrapScroll(err);
        }
        String name = child.getName();
        VBox page = new VBox(20);
        page.setPadding(new Insets(24));

        Button backBtn = new Button("\u2190 Back to Profiles");
        backBtn.setStyle("-fx-background-color: transparent; -fx-text-fill: " + PRIMARY + "; -fx-cursor: hand;");
        backBtn.setOnAction(e -> root.setCenter(buildChildrenPage()));

        Label title = new Label("Child Profile: " + name);
        title.setFont(Font.font("Segoe UI", FontWeight.BOLD, 22));
        title.setTextFill(Color.web(TEXT()));

        VBox card = new VBox(20);
        card.setPadding(new Insets(24));
        card.setStyle("-fx-background-color: " + CARD() + "; -fx-border-color: " + BORDER()
                + "; -fx-border-width: 1; -fx-background-radius: 8; -fx-border-radius: 8;");

        HBox hdr = new HBox(20);
        hdr.setAlignment(Pos.CENTER_LEFT);
        Label avatarLetter = new Label(name.substring(0, 1));
        avatarLetter.setTextFill(Color.web(PRIMARY));
        avatarLetter.setFont(Font.font("Segoe UI", FontWeight.BOLD, 24));
        StackPane avatar = new StackPane(avatarLetter);
        avatar.setPrefSize(64, 64);
        avatar.setStyle("-fx-background-color: " + PRIMARY + "1A; -fx-background-radius: 32;");

        VBox info = new VBox(4);
        Label n = new Label(name);
        n.setFont(Font.font("Segoe UI", FontWeight.BOLD, 18));
        n.setTextFill(Color.web(TEXT()));
        Label idLabel = new Label("ID: CH-" + (1000 + child.getId()));
        idLabel.setTextFill(Color.web(MUTED_FG()));
        info.getChildren().addAll(n, idLabel);
        hdr.getChildren().addAll(avatar, info);

        GridPane details = new GridPane();
        details.setHgap(40);
        details.setVgap(15);
        String[][] data = {
                { "Age", child.getAge() + " years" },
                { "Gender", child.getGender() != null ? child.getGender() : "N/A" },
                { "Organization", child.getOrganization() != null ? child.getOrganization() : "N/A" },
                { "Date of Birth", child.getDateOfBirth() != null ? child.getDateOfBirth() : "N/A" },
                { "Status", child.getStatus() != null ? child.getStatus() : "Active" }
        };
        for (int r = 0; r < data.length; r++) {
            Label l = new Label(data[r][0]);
            l.setTextFill(Color.web(MUTED_FG()));
            Label v = new Label(data[r][1]);
            v.setFont(Font.font("Segoe UI", FontWeight.MEDIUM, 13));
            v.setTextFill(Color.web(TEXT()));
            details.add(l, 0, r);
            details.add(v, 1, r);
        }

        // Admin Actions Section
        VBox adminActions = new VBox(12);
        adminActions.setPadding(new Insets(20));
        adminActions.setStyle("-fx-background-color: " + MUTED() + "80; -fx-background-radius: 8;");

        Label adminLabel = new Label("Administrative Controls");
        adminLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 14));
        adminLabel.setTextFill(Color.web(TEXT()));

        HBox actionButtons = new HBox(12);
        Button editBtn = new Button("\u270E Edit Profile");
        Button delBtn = new Button("\u2716 Delete Profile");

        String btnStyle = "-fx-background-color: " + CARD() + "; -fx-border-color: " + BORDER()
                + "; -fx-border-radius: 4; -fx-padding: 8 16; -fx-cursor: hand; -fx-text-fill: " + TEXT() + ";";
        editBtn.setStyle(btnStyle);
        editBtn.setOnAction(e -> root.setCenter(buildEditChildForm(childId)));
        delBtn.setStyle(btnStyle + "-fx-text-fill: " + DESTRUCTIVE + ";");
        delBtn.setOnAction(e -> {
            Alert confirm = new Alert(Alert.AlertType.CONFIRMATION,
                    "Are you sure you want to delete " + name + "?",
                    ButtonType.YES, ButtonType.NO);
            confirm.showAndWait().ifPresent(bt -> {
                if (bt == ButtonType.YES) {
                    childService.deleteChild(childId);
                    systemLogService.save(new SystemLog("Data Update",
                            "Deleted child profile: " + name, user.getUsername(),
                            LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))));
                    root.setCenter(buildChildrenPage());
                }
            });
        });

        actionButtons.getChildren().addAll(editBtn, delBtn);
        adminActions.getChildren().addAll(adminLabel, actionButtons);

        card.getChildren().addAll(hdr, new Separator(), details, adminActions);
        page.getChildren().addAll(backBtn, title, card);
        return wrapScroll(page);
    }

    private ScrollPane buildAlertDetailView(String[] alert) {
        VBox page = new VBox(20);
        page.setPadding(new Insets(24));

        Button backBtn = new Button("\u2190 Back to Alerts");
        backBtn.setStyle("-fx-background-color: transparent; -fx-text-fill: " + util.ThemeManager.PRIMARY
                + "; -fx-cursor: hand;");
        backBtn.setOnAction(e -> root.setCenter(buildAlertsPage()));

        Label title = new Label("Alert Detail: " + alert[1]);
        title.setFont(Font.font("Segoe UI", FontWeight.BOLD, 22));

        VBox card = new VBox(20);
        card.setPadding(new Insets(24));
        card.setStyle("-fx-background-color: " + CARD() + "; -fx-border-color: " + BORDER()
                + "; -fx-border-width: 1; -fx-background-radius: 8; -fx-border-radius: 8;");

        Label typeLabel = new Label(alert[0].toUpperCase() + " ALERT");
        typeLabel.setTextFill(
                Color.web(alert[0].equals("critical") ? util.ThemeManager.DESTRUCTIVE : util.ThemeManager.WARNING));
        typeLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 12));

        Label desc = new Label(alert[4]);
        desc.setFont(Font.font("Segoe UI", 16));
        desc.setWrapText(true);

        GridPane details = new GridPane();
        details.setHgap(30);
        details.setVgap(10);
        details.add(new Label("Timestamp:"), 0, 0);
        details.add(new Label(alert[3]), 1, 0);
        details.add(new Label("Related Entity:"), 0, 1);
        details.add(new Label(alert[5]), 1, 1);

        Button resolve = new Button("Mark as Resolved");
        resolve.setStyle("-fx-background-color: " + util.ThemeManager.SECONDARY
                + "; -fx-text-fill: white; -fx-background-radius: 4; -fx-padding: 10 20; -fx-font-weight: bold; -fx-cursor: hand;");
        resolve.setOnAction(e -> {
            activeAlerts.remove(alert);
            root.setCenter(buildAlertsPage());
        });

        card.getChildren().addAll(typeLabel, title, desc, new Separator(), details, resolve);
        page.getChildren().addAll(backBtn, card);
        return wrapScroll(page);
    }

    // ═══════════ ADD CHILD FORM ═══════════
    private ScrollPane buildAddChildForm() {
        VBox page = new VBox(20);
        page.setPadding(new Insets(24));

        Button backBtn = new Button("\u2190 Back to Profiles");
        backBtn.setStyle("-fx-background-color: transparent; -fx-text-fill: " + PRIMARY + "; -fx-cursor: hand;");
        backBtn.setOnAction(e -> root.setCenter(buildChildrenPage()));

        Label title = new Label("Add New Child");
        title.setFont(Font.font("Segoe UI", FontWeight.BOLD, 22));
        title.setTextFill(Color.web(TEXT()));

        VBox form = new VBox(16);
        form.setPadding(new Insets(24));
        form.setStyle("-fx-background-color: " + CARD() + "; -fx-border-color: " + BORDER()
                + "; -fx-border-width: 1; -fx-background-radius: 8; -fx-border-radius: 8;");

        TextField nameField = new TextField();
        nameField.setPromptText("Full Name");
        TextField ageField = new TextField();
        ageField.setPromptText("Age");
        ComboBox<String> genderBox = new ComboBox<>();
        genderBox.getItems().addAll("Male", "Female");
        genderBox.setPromptText("Gender");
        TextField orgField = new TextField();
        orgField.setPromptText("Organization");
        TextField dobField = new TextField();
        dobField.setPromptText("Date of Birth (YYYY-MM-DD)");

        String fieldStyle = "-fx-background-color: " + CARD() + "; -fx-border-color: " + BORDER()
                + "; -fx-border-width: 1; -fx-border-radius: 4; -fx-background-radius: 4; -fx-padding: 8 12; -fx-text-fill: "
                + TEXT() + ";";
        nameField.setStyle(fieldStyle);
        ageField.setStyle(fieldStyle);
        orgField.setStyle(fieldStyle);
        dobField.setStyle(fieldStyle);

        Button saveBtn = new Button("Save Child");
        saveBtn.setStyle("-fx-background-color: " + PRIMARY
                + "; -fx-text-fill: white; -fx-background-radius: 4; -fx-padding: 10 24; -fx-font-size: 13px; -fx-cursor: hand; -fx-font-weight: bold;");
        saveBtn.setOnAction(e -> {
            String name = nameField.getText().trim();
            String ageText = ageField.getText().trim();
            if (name.isEmpty() || ageText.isEmpty()) {
                new Alert(Alert.AlertType.WARNING, "Name and Age are required.").show();
                return;
            }
            int age;
            try {
                age = Integer.parseInt(ageText);
            } catch (NumberFormatException ex) {
                new Alert(Alert.AlertType.WARNING, "Age must be a number.").show();
                return;
            }
            Child child = new Child(name, age, orgField.getText().trim(),
                    genderBox.getValue(), dobField.getText().trim(), "Active");
            childService.addChild(child);
            systemLogService.save(new SystemLog("Data Update",
                    "Added new child: " + name, user.getUsername(),
                    LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))));
            root.setCenter(buildChildrenPage());
        });

        form.getChildren().addAll(
                formRow("Name", nameField), formRow("Age", ageField),
                formRow("Gender", genderBox), formRow("Organization", orgField),
                formRow("Date of Birth", dobField), saveBtn);

        page.getChildren().addAll(backBtn, title, form);
        return wrapScroll(page);
    }

    // ═══════════ EDIT CHILD FORM ═══════════
    private ScrollPane buildEditChildForm(int childId) {
        Child child = childService.getChildById(childId);
        if (child == null) {
            VBox err = new VBox(new Label("Child not found."));
            err.setPadding(new Insets(24));
            return wrapScroll(err);
        }

        VBox page = new VBox(20);
        page.setPadding(new Insets(24));

        Button backBtn = new Button("\u2190 Back to Profile");
        backBtn.setStyle("-fx-background-color: transparent; -fx-text-fill: " + PRIMARY + "; -fx-cursor: hand;");
        backBtn.setOnAction(e -> root.setCenter(buildChildProfileDetailView(childId)));

        Label title = new Label("Edit Child: " + child.getName());
        title.setFont(Font.font("Segoe UI", FontWeight.BOLD, 22));
        title.setTextFill(Color.web(TEXT()));

        VBox form = new VBox(16);
        form.setPadding(new Insets(24));
        form.setStyle("-fx-background-color: " + CARD() + "; -fx-border-color: " + BORDER()
                + "; -fx-border-width: 1; -fx-background-radius: 8; -fx-border-radius: 8;");

        TextField nameField = new TextField(child.getName());
        TextField ageField = new TextField(String.valueOf(child.getAge()));
        ComboBox<String> genderBox = new ComboBox<>();
        genderBox.getItems().addAll("Male", "Female");
        genderBox.setValue(child.getGender());
        TextField orgField = new TextField(child.getOrganization() != null ? child.getOrganization() : "");
        TextField dobField = new TextField(child.getDateOfBirth() != null ? child.getDateOfBirth() : "");
        ComboBox<String> statusBox = new ComboBox<>();
        statusBox.getItems().addAll("Active", "Pending", "Inactive");
        statusBox.setValue(child.getStatus() != null ? child.getStatus() : "Active");

        String fieldStyle = "-fx-background-color: " + CARD() + "; -fx-border-color: " + BORDER()
                + "; -fx-border-width: 1; -fx-border-radius: 4; -fx-background-radius: 4; -fx-padding: 8 12; -fx-text-fill: "
                + TEXT() + ";";
        nameField.setStyle(fieldStyle);
        ageField.setStyle(fieldStyle);
        orgField.setStyle(fieldStyle);
        dobField.setStyle(fieldStyle);

        Button saveBtn = new Button("Update Child");
        saveBtn.setStyle("-fx-background-color: " + PRIMARY
                + "; -fx-text-fill: white; -fx-background-radius: 4; -fx-padding: 10 24; -fx-font-size: 13px; -fx-cursor: hand; -fx-font-weight: bold;");
        saveBtn.setOnAction(e -> {
            String name = nameField.getText().trim();
            String ageText = ageField.getText().trim();
            if (name.isEmpty() || ageText.isEmpty()) {
                new Alert(Alert.AlertType.WARNING, "Name and Age are required.").show();
                return;
            }
            int age;
            try {
                age = Integer.parseInt(ageText);
            } catch (NumberFormatException ex) {
                new Alert(Alert.AlertType.WARNING, "Age must be a number.").show();
                return;
            }
            child.setName(name);
            child.setAge(age);
            child.setGender(genderBox.getValue());
            child.setOrganization(orgField.getText().trim());
            child.setDateOfBirth(dobField.getText().trim());
            child.setStatus(statusBox.getValue());
            childService.updateChild(child);
            systemLogService.save(new SystemLog("Data Update",
                    "Updated child profile: " + name, user.getUsername(),
                    LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))));
            root.setCenter(buildChildProfileDetailView(childId));
        });

        form.getChildren().addAll(
                formRow("Name", nameField), formRow("Age", ageField),
                formRow("Gender", genderBox), formRow("Organization", orgField),
                formRow("Date of Birth", dobField), formRow("Status", statusBox), saveBtn);

        page.getChildren().addAll(backBtn, title, form);
        return wrapScroll(page);
    }

    private HBox formRow(String label, Node field) {
        HBox row = new HBox(12);
        row.setAlignment(Pos.CENTER_LEFT);
        Label l = new Label(label);
        l.setFont(Font.font("Segoe UI", FontWeight.MEDIUM, 13));
        l.setTextFill(Color.web(TEXT()));
        l.setPrefWidth(120);
        HBox.setHgrow(field instanceof Region ? (Region) field : new Region(), Priority.ALWAYS);
        if (field instanceof TextField tf)
            tf.setMaxWidth(Double.MAX_VALUE);
        if (field instanceof ComboBox<?> cb)
            cb.setMaxWidth(Double.MAX_VALUE);
        row.getChildren().addAll(l, field);
        return row;
    }

    private ScrollPane wrapScroll(VBox page) {
        ScrollPane sp = new ScrollPane(page);
        sp.setFitToWidth(true);
        sp.setStyle("-fx-background: " + BG() + "; -fx-background-color: " + BG() + ";");
        return sp;
    }
}