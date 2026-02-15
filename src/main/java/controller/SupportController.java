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
import javafx.stage.Stage;
import model.entity.SystemLog;
import model.user.User;
import service.SystemLogService;
import util.ThemeManager;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Support staff dashboard.
 * Sidebar pages: Dashboard, Alerts, Incident Reports.
 */
public class SupportController {

    private final Stage stage;
    private final User user;
    private BorderPane root;
    private VBox sidebar;
    private String activePage = "dashboard";

    private final SystemLogService systemLogService = new SystemLogService();

    // Theme constants
    private static final String PRIMARY = ThemeManager.PRIMARY;
    private static final String SECONDARY = ThemeManager.SECONDARY;
    private static final String WARNING = ThemeManager.WARNING;
    private static final String DESTRUCTIVE = ThemeManager.DESTRUCTIVE;
    private static final String INFO = ThemeManager.INFO;

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

    public SupportController(Stage stage, User user) {
        this.stage = stage;
        this.user = user;
    }

    public void show() {
        root = new BorderPane();
        root.setTop(buildHeader());
        root.setLeft(buildSidebar());
        root.setCenter(buildDashboardPage());
        root.setStyle("-fx-background-color: " + BG() + ";");
        Scene scene = new Scene(root, 1280, 800);
        stage.setScene(scene);
        stage.setTitle("GuardianLink \u2014 Support Dashboard");
        stage.show();
    }

    private void refreshTheme() {
        root.setStyle("-fx-background-color: " + BG() + ";");
        root.setTop(buildHeader());
        root.setLeft(buildSidebar());
        switch (activePage) {
            case "dashboard" -> root.setCenter(buildDashboardPage());
            case "alerts" -> root.setCenter(buildAlertsPage());
            case "reports" -> root.setCenter(buildReportsPage());
        }
    }

    // ═══════════ HEADER ═══════════
    private HBox buildHeader() {
        HBox header = new HBox();
        header.setPadding(new Insets(0, 24, 0, 24));
        header.setAlignment(Pos.CENTER_LEFT);
        header.setPrefHeight(64);
        header.setStyle("-fx-background-color: " + CARD() + "; -fx-border-color: " + BORDER()
                + "; -fx-border-width: 0 0 1 0;");

        Label logoIcon = new Label("\uD83D\uDEE1");
        logoIcon.setFont(Font.font("Segoe UI Emoji", 28));
        logoIcon.setTextFill(Color.web(PRIMARY));

        VBox titleBox = new VBox(0);
        titleBox.setPadding(new Insets(0, 0, 0, 14));
        Label t1 = new Label("GuardianLink");
        t1.setFont(Font.font("Segoe UI", FontWeight.BOLD, 20));
        t1.setTextFill(Color.web(TEXT()));
        Label t2 = new Label("Support Portal");
        t2.setFont(Font.font("Segoe UI", FontWeight.SEMI_BOLD, 12));
        t2.setTextFill(Color.web(MUTED_FG()));
        titleBox.getChildren().addAll(t1, t2);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        HBox userBox = new HBox(8);
        userBox.setAlignment(Pos.CENTER_RIGHT);
        userBox.setPadding(new Insets(8, 16, 8, 16));
        userBox.setStyle("-fx-background-color: " + MUTED() + "; -fx-background-radius: 8; -fx-border-color: "
                + BORDER() + "; -fx-border-radius: 8;");
        VBox userInfo = new VBox(2);
        Label uName = new Label(user.getUsername());
        uName.setFont(Font.font("Segoe UI", FontWeight.MEDIUM, 13));
        uName.setTextFill(Color.web(TEXT()));
        Label uRole = new Label("Support Staff");
        uRole.setFont(Font.font("Segoe UI", FontWeight.BOLD, 11));
        uRole.setTextFill(Color.web(PRIMARY));
        userInfo.getChildren().addAll(uName, uRole);
        userBox.getChildren().addAll(userInfo);

        header.getChildren().addAll(logoIcon, titleBox, spacer, userBox);
        return header;
    }

    // ═══════════ SIDEBAR ═══════════
    private VBox buildSidebar() {
        sidebar = new VBox(4);
        sidebar.setPrefWidth(240);
        sidebar.setStyle("-fx-background-color: " + CARD() + "; -fx-border-color: " + BORDER()
                + "; -fx-border-width: 0 1 0 0;");

        Label navLabel = new Label("Navigation");
        navLabel.setFont(Font.font("Segoe UI", 11));
        navLabel.setTextFill(Color.web(MUTED_FG()));
        navLabel.setPadding(new Insets(16, 16, 8, 16));
        sidebar.getChildren().add(navLabel);

        VBox navItems = new VBox(2);
        navItems.setPadding(new Insets(0, 8, 0, 8));
        navItems.getChildren().addAll(
                sidebarBtn("Dashboard", "dashboard"),
                sidebarBtn("Alerts", "alerts"),
                sidebarBtn("Incident Reports", "reports"));
        sidebar.getChildren().add(navItems);

        Region spacer = new Region();
        VBox.setVgrow(spacer, Priority.ALWAYS);

        // Theme toggle
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
        String active = "-fx-background-color: " + PRIMARY
                + "; -fx-text-fill: white; -fx-background-radius: 4; -fx-padding: 6 12; -fx-cursor: hand; -fx-font-size: 11px;";
        String inactive = "-fx-background-color: " + MUTED() + "; -fx-text-fill: " + TEXT()
                + "; -fx-background-radius: 4; -fx-padding: 6 12; -fx-cursor: hand; -fx-font-size: 11px;";
        lightBtn.setStyle(ThemeManager.isDarkMode() ? inactive : active);
        darkBtn.setStyle(ThemeManager.isDarkMode() ? active : inactive);
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

        // Logout
        VBox logoutSection = new VBox(8);
        logoutSection.setPadding(new Insets(8));
        Button logoutBtn = new Button("Logout");
        logoutBtn.setMaxWidth(Double.MAX_VALUE);
        logoutBtn.setAlignment(Pos.CENTER_LEFT);
        logoutBtn.setFont(Font.font("Segoe UI", 13));
        logoutBtn.setStyle("-fx-background-color: " + DESTRUCTIVE
                + "; -fx-text-fill: white; -fx-background-radius: 4; -fx-padding: 8 12; -fx-cursor: hand;");
        logoutBtn.setOnAction(e -> new AuthController(stage).show());
        logoutSection.getChildren().add(logoutBtn);

        sidebar.getChildren().addAll(spacer, themeSection, logoutSection);
        return sidebar;
    }

    private Button sidebarBtn(String text, String pageId) {
        Button btn = new Button(text);
        btn.setMaxWidth(Double.MAX_VALUE);
        btn.setAlignment(Pos.CENTER_LEFT);
        btn.setFont(Font.font("Segoe UI", 13));
        styleSidebarBtn(btn, pageId.equals(activePage));
        btn.setOnAction(e -> {
            activePage = pageId;
            refreshSidebar();
            switch (pageId) {
                case "dashboard" -> root.setCenter(buildDashboardPage());
                case "alerts" -> root.setCenter(buildAlertsPage());
                case "reports" -> root.setCenter(buildReportsPage());
            }
        });
        return btn;
    }

    private void styleSidebarBtn(Button btn, boolean isActive) {
        if (isActive)
            btn.setStyle("-fx-background-color: " + PRIMARY
                    + "; -fx-text-fill: white; -fx-background-radius: 4; -fx-padding: 8 12; -fx-cursor: hand; -fx-font-size: 13px;");
        else
            btn.setStyle("-fx-background-color: transparent; -fx-text-fill: " + TEXT()
                    + "; -fx-background-radius: 4; -fx-padding: 8 12; -fx-cursor: hand; -fx-font-size: 13px;");
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
        if (text.contains("Alerts"))
            return "alerts";
        if (text.contains("Incident") || text.contains("Reports"))
            return "reports";
        return "";
    }

    private ScrollPane wrapScroll(VBox page) {
        ScrollPane sp = new ScrollPane(page);
        sp.setFitToWidth(true);
        sp.setStyle("-fx-background: " + BG() + "; -fx-background-color: " + BG() + ";");
        return sp;
    }

    private VBox statCard(String label, String value, String detail, String color) {
        VBox card = new VBox(4);
        card.setPadding(new Insets(16));
        card.setStyle("-fx-background-color: " + CARD() + "; -fx-border-color: " + BORDER()
                + "; -fx-border-width: 1; -fx-background-radius: 8; -fx-border-radius: 8;");
        HBox.setHgrow(card, Priority.ALWAYS);
        Label l = new Label(label);
        l.setFont(Font.font("Segoe UI", 11));
        l.setTextFill(Color.web(MUTED_FG()));
        Label v = new Label(value);
        v.setFont(Font.font("Segoe UI", FontWeight.SEMI_BOLD, 24));
        v.setTextFill(Color.web(TEXT()));
        Label d = new Label(detail);
        d.setFont(Font.font("Segoe UI", 11));
        d.setTextFill(Color.web(color));
        card.getChildren().addAll(l, v, d);
        return card;
    }

    private VBox createQuickActionCard(String title, String description) {
        VBox card = new VBox(4);
        card.setPadding(new Insets(16));
        card.setStyle("-fx-background-color: " + CARD() + "; -fx-border-color: " + BORDER()
                + "; -fx-border-width: 1; -fx-background-radius: 8; -fx-border-radius: 8; -fx-cursor: hand;");
        HBox.setHgrow(card, Priority.ALWAYS);
        Label t = new Label(title);
        t.setFont(Font.font("Segoe UI", FontWeight.MEDIUM, 13));
        t.setTextFill(Color.web(TEXT()));
        Label d = new Label(description);
        d.setFont(Font.font("Segoe UI", 11));
        d.setTextFill(Color.web(MUTED_FG()));
        card.getChildren().addAll(t, d);
        card.setOnMouseEntered(e -> card.setStyle("-fx-background-color: " + MUTED() + "; -fx-border-color: " + PRIMARY
                + "; -fx-border-width: 1; -fx-background-radius: 8; -fx-border-radius: 8; -fx-cursor: hand;"));
        card.setOnMouseExited(e -> card.setStyle("-fx-background-color: " + CARD() + "; -fx-border-color: " + BORDER()
                + "; -fx-border-width: 1; -fx-background-radius: 8; -fx-border-radius: 8; -fx-cursor: hand;"));
        return card;
    }

    // ═══════════ DASHBOARD ═══════════
    private ScrollPane buildDashboardPage() {
        VBox page = new VBox(20);
        page.setPadding(new Insets(24));

        Label title = new Label("Support Dashboard");
        title.setFont(Font.font("Segoe UI", FontWeight.MEDIUM, 20));
        title.setTextFill(Color.web(TEXT()));
        Label sub = new Label("System health, tickets, and alerts overview");
        sub.setFont(Font.font("Segoe UI", 13));
        sub.setTextFill(Color.web(MUTED_FG()));

        // Real stats
        int logCount = systemLogService.getCount();
        List<SystemLog> recentLogs = systemLogService.getRecent(50);
        long incidentCount = recentLogs.stream()
                .filter(l -> l.getEventType() != null && l.getEventType().equals("Incident"))
                .count();
        long alertCount = recentLogs.stream()
                .filter(l -> l.getEventType() != null &&
                        (l.getEventType().contains("Error") || l.getEventType().contains("Warning")
                                || l.getEventType().contains("Delete")))
                .count();

        HBox stats = new HBox(16);
        stats.getChildren().addAll(
                statCard("Total Logs", String.valueOf(logCount), "System-wide", MUTED_FG()),
                statCard("Incidents", String.valueOf(incidentCount), "Reported", DESTRUCTIVE),
                statCard("Alerts", String.valueOf(alertCount), "Active", WARNING),
                statCard("System Status", "Online", "All services running", SECONDARY));

        Label qaTitle = new Label("Quick Actions");
        qaTitle.setFont(Font.font("Segoe UI", FontWeight.MEDIUM, 17));
        qaTitle.setTextFill(Color.web(TEXT()));

        HBox qaCards = new HBox(12);
        VBox qaAlerts = createQuickActionCard("View Alerts", "Check system alerts");
        qaAlerts.setOnMouseClicked(e -> {
            activePage = "alerts";
            refreshSidebar();
            root.setCenter(buildAlertsPage());
        });
        VBox qaIncident = createQuickActionCard("Create Incident", "Report a new incident");
        qaIncident.setOnMouseClicked(e -> {
            activePage = "reports";
            refreshSidebar();
            root.setCenter(buildNewIncidentForm());
        });
        VBox qaReports = createQuickActionCard("View Reports", "See incident history");
        qaReports.setOnMouseClicked(e -> {
            activePage = "reports";
            refreshSidebar();
            root.setCenter(buildReportsPage());
        });
        qaCards.getChildren().addAll(qaAlerts, qaIncident, qaReports);

        // Recent Activity
        VBox recentCard = new VBox(0);
        recentCard.setStyle("-fx-background-color: " + CARD() + "; -fx-border-color: " + BORDER()
                + "; -fx-border-width: 1; -fx-background-radius: 8; -fx-border-radius: 8;");
        HBox rHdr = new HBox();
        rHdr.setPadding(new Insets(16));
        rHdr.setStyle("-fx-border-color: " + BORDER() + "; -fx-border-width: 0 0 1 0;");
        Label rTitle = new Label("Recent System Activity");
        rTitle.setFont(Font.font("Segoe UI", FontWeight.MEDIUM, 17));
        rTitle.setTextFill(Color.web(TEXT()));
        rHdr.getChildren().add(rTitle);

        VBox rList = new VBox(0);
        List<SystemLog> recent5 = systemLogService.getRecent(5);
        if (recent5.isEmpty()) {
            Label noLogs = new Label("No recent activity.");
            noLogs.setTextFill(Color.web(MUTED_FG()));
            noLogs.setPadding(new Insets(16));
            rList.getChildren().add(noLogs);
        } else {
            for (SystemLog log : recent5) {
                HBox logRow = new HBox(12);
                logRow.setPadding(new Insets(12, 16, 12, 16));
                logRow.setStyle("-fx-border-color: " + BORDER() + "; -fx-border-width: 0 0 1 0;");
                logRow.setAlignment(Pos.CENTER_LEFT);

                Label typeBadge = new Label(log.getEventType() != null ? log.getEventType() : "Info");
                typeBadge.setFont(Font.font("Segoe UI", 11));
                typeBadge.setStyle("-fx-background-color: " + PRIMARY + "1A; -fx-text-fill: " + PRIMARY
                        + "; -fx-background-radius: 4; -fx-padding: 2 8;");

                Label desc = new Label(log.getDescription() != null ? log.getDescription() : "");
                desc.setFont(Font.font("Segoe UI", 13));
                desc.setTextFill(Color.web(TEXT()));
                HBox.setHgrow(desc, Priority.ALWAYS);

                Label time = new Label(log.getTimestamp() != null ? log.getTimestamp() : "");
                time.setFont(Font.font("Segoe UI", 11));
                time.setTextFill(Color.web(MUTED_FG()));

                logRow.getChildren().addAll(typeBadge, desc, time);
                rList.getChildren().add(logRow);
            }
        }
        recentCard.getChildren().addAll(rHdr, rList);

        page.getChildren().addAll(new VBox(4, title, sub), stats, qaTitle, qaCards, recentCard);
        return wrapScroll(page);
    }

    // ═══════════ ALERTS PAGE ═══════════
    private ScrollPane buildAlertsPage() {
        VBox page = new VBox(20);
        page.setPadding(new Insets(24));

        Label title = new Label("System Alerts");
        title.setFont(Font.font("Segoe UI", FontWeight.MEDIUM, 20));
        title.setTextFill(Color.web(TEXT()));
        Label sub = new Label("Monitor and manage system alerts");
        sub.setFont(Font.font("Segoe UI", 13));
        sub.setTextFill(Color.web(MUTED_FG()));

        List<SystemLog> allLogs = systemLogService.getRecent(30);

        HBox stats = new HBox(16);
        stats.getChildren().addAll(
                statCard("Total Entries", String.valueOf(allLogs.size()), "Showing recent", MUTED_FG()),
                statCard("System Logs", String.valueOf(systemLogService.getCount()), "All time", SECONDARY));

        VBox alertsList = new VBox(12);
        if (allLogs.isEmpty()) {
            Label noAlerts = new Label("No alerts at this time.");
            noAlerts.setTextFill(Color.web(MUTED_FG()));
            noAlerts.setPadding(new Insets(16));
            alertsList.getChildren().add(noAlerts);
        } else {
            for (SystemLog log : allLogs) {
                String eventType = log.getEventType() != null ? log.getEventType() : "Info";
                boolean isError = eventType.contains("Error") || eventType.contains("Delete");
                boolean isWarning = eventType.contains("Warning");
                String borderC = isError ? DESTRUCTIVE : BORDER();
                String badgeColor = isError ? DESTRUCTIVE : isWarning ? WARNING : INFO;

                VBox card = new VBox(8);
                card.setPadding(new Insets(12));
                card.setStyle("-fx-background-color: " + CARD() + "; -fx-border-color: " + borderC
                        + "; -fx-border-radius: 8; -fx-background-radius: 8;");

                HBox top = new HBox(12);
                top.setAlignment(Pos.CENTER_LEFT);

                Label typeBadge = new Label(eventType);
                typeBadge.setFont(Font.font("Segoe UI", FontWeight.BOLD, 11));
                typeBadge.setStyle("-fx-background-color: " + badgeColor + "1A; -fx-text-fill: " + badgeColor
                        + "; -fx-background-radius: 4; -fx-padding: 2 8;");

                Label desc = new Label(log.getDescription() != null ? log.getDescription() : "");
                desc.setFont(Font.font("Segoe UI", 13));
                desc.setTextFill(Color.web(TEXT()));
                desc.setWrapText(true);
                HBox.setHgrow(desc, Priority.ALWAYS);

                Label tm = new Label(log.getTimestamp() != null ? log.getTimestamp() : "");
                tm.setFont(Font.font("Segoe UI", 11));
                tm.setTextFill(Color.web(MUTED_FG()));

                top.getChildren().addAll(typeBadge, desc, tm);

                HBox bottom = new HBox(12);
                Label actor = new Label("By: " + (log.getActor() != null ? log.getActor() : "System"));
                actor.setFont(Font.font("Segoe UI", 11));
                actor.setTextFill(Color.web(MUTED_FG()));

                Button resolveBtn = new Button("Resolve");
                resolveBtn.setStyle("-fx-background-color: " + SECONDARY
                        + "; -fx-text-fill: white; -fx-background-radius: 4; -fx-padding: 4 12; -fx-cursor: hand; -fx-font-size: 11px;");
                resolveBtn.setOnAction(e -> {
                    systemLogService.save(new SystemLog("Resolution",
                            "Resolved: " + log.getDescription(), user.getUsername(),
                            LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))));
                    showAlert("Resolved", "Alert has been marked as resolved.");
                    root.setCenter(buildAlertsPage());
                });

                bottom.getChildren().addAll(actor, resolveBtn);
                card.getChildren().addAll(top, bottom);
                alertsList.getChildren().add(card);
            }
        }

        page.getChildren().addAll(new VBox(4, title, sub), stats, alertsList);
        return wrapScroll(page);
    }

    // ═══════════ REPORTS (INCIDENT HISTORY) ═══════════
    private ScrollPane buildReportsPage() {
        VBox page = new VBox(20);
        page.setPadding(new Insets(24));

        Label title = new Label("Incident Reports");
        title.setFont(Font.font("Segoe UI", FontWeight.MEDIUM, 20));
        title.setTextFill(Color.web(TEXT()));
        Label sub = new Label("View incident history and create new reports");
        sub.setFont(Font.font("Segoe UI", 13));
        sub.setTextFill(Color.web(MUTED_FG()));

        // Create New Incident button
        Button createBtn = new Button("+ Create New Incident Report");
        createBtn.setStyle("-fx-background-color: " + PRIMARY
                + "; -fx-text-fill: white; -fx-background-radius: 4; -fx-padding: 10 20; -fx-font-weight: bold; -fx-cursor: hand;");
        createBtn.setOnAction(e -> root.setCenter(buildNewIncidentForm()));

        // Incident history table
        VBox tableCard = new VBox(0);
        tableCard.setStyle("-fx-background-color: " + CARD() + "; -fx-border-color: " + BORDER()
                + "; -fx-border-width: 1; -fx-background-radius: 8; -fx-border-radius: 8;");

        HBox tHdr = new HBox();
        tHdr.setPadding(new Insets(16));
        tHdr.setStyle("-fx-border-color: " + BORDER() + "; -fx-border-width: 0 0 1 0;");
        Label tTitle = new Label("Incident History");
        tTitle.setFont(Font.font("Segoe UI", FontWeight.MEDIUM, 17));
        tTitle.setTextFill(Color.web(TEXT()));
        tHdr.getChildren().add(tTitle);

        // Table header
        GridPane grid = new GridPane();
        String[] cols = { "Event Type", "Description", "Reported By", "Timestamp" };
        for (int i = 0; i < cols.length; i++) {
            Label h = new Label(cols[i]);
            h.setFont(Font.font("Segoe UI", FontWeight.SEMI_BOLD, 11));
            h.setPadding(new Insets(8, 16, 8, 16));
            h.setMaxWidth(Double.MAX_VALUE);
            h.setStyle("-fx-background-color: " + MUTED() + ";");
            h.setTextFill(Color.web(TEXT()));
            grid.add(h, i, 0);
        }

        // Table rows from system logs (filter for incidents)
        List<SystemLog> allLogs = systemLogService.getRecent(50);
        int row = 1;
        boolean hasIncidents = false;
        for (SystemLog log : allLogs) {
            if (log.getEventType() != null && log.getEventType().equals("Incident")) {
                hasIncidents = true;
                Label typeL = new Label(log.getEventType());
                typeL.setFont(Font.font("Segoe UI", 13));
                typeL.setTextFill(Color.web(TEXT()));
                typeL.setPadding(new Insets(12, 16, 12, 16));
                typeL.setStyle("-fx-border-color: " + BORDER() + "; -fx-border-width: 0 0 1 0;");

                Label descL = new Label(log.getDescription() != null ? log.getDescription() : "");
                descL.setFont(Font.font("Segoe UI", 13));
                descL.setTextFill(Color.web(TEXT()));
                descL.setPadding(new Insets(12, 16, 12, 16));
                descL.setStyle("-fx-border-color: " + BORDER() + "; -fx-border-width: 0 0 1 0;");
                descL.setWrapText(true);

                Label actorL = new Label(log.getActor() != null ? log.getActor() : "System");
                actorL.setFont(Font.font("Segoe UI", 13));
                actorL.setTextFill(Color.web(MUTED_FG()));
                actorL.setPadding(new Insets(12, 16, 12, 16));
                actorL.setStyle("-fx-border-color: " + BORDER() + "; -fx-border-width: 0 0 1 0;");

                Label timeL = new Label(log.getTimestamp() != null ? log.getTimestamp() : "");
                timeL.setFont(Font.font("Segoe UI", 11));
                timeL.setTextFill(Color.web(MUTED_FG()));
                timeL.setPadding(new Insets(12, 16, 12, 16));
                timeL.setStyle("-fx-border-color: " + BORDER() + "; -fx-border-width: 0 0 1 0;");

                grid.add(typeL, 0, row);
                grid.add(descL, 1, row);
                grid.add(actorL, 2, row);
                grid.add(timeL, 3, row);
                row++;
            }
        }
        if (!hasIncidents) {
            Label noRows = new Label("No incidents reported yet. Click \"Create New Incident Report\" to log one.");
            noRows.setFont(Font.font("Segoe UI", 13));
            noRows.setTextFill(Color.web(MUTED_FG()));
            noRows.setPadding(new Insets(24));
            grid.add(noRows, 0, 1);
            GridPane.setColumnSpan(noRows, 4);
        }

        tableCard.getChildren().addAll(tHdr, grid);
        page.getChildren().addAll(new VBox(4, title, sub), createBtn, tableCard);
        return wrapScroll(page);
    }

    // ═══════════ NEW INCIDENT FORM ═══════════
    private ScrollPane buildNewIncidentForm() {
        VBox page = new VBox(20);
        page.setPadding(new Insets(24));

        Button backBtn = new Button("\u2190 Back to Incident Reports");
        backBtn.setStyle("-fx-background-color: transparent; -fx-text-fill: " + PRIMARY + "; -fx-cursor: hand;");
        backBtn.setOnAction(e -> root.setCenter(buildReportsPage()));

        Label title = new Label("Create New Incident Report");
        title.setFont(Font.font("Segoe UI", FontWeight.BOLD, 22));
        title.setTextFill(Color.web(TEXT()));

        VBox formCard = new VBox(16);
        formCard.setPadding(new Insets(24));
        formCard.setStyle("-fx-background-color: " + CARD() + "; -fx-border-color: " + BORDER()
                + "; -fx-border-width: 1; -fx-background-radius: 8; -fx-border-radius: 8;");

        Label catLabel = new Label("Category");
        catLabel.setFont(Font.font("Segoe UI", FontWeight.MEDIUM, 13));
        catLabel.setTextFill(Color.web(TEXT()));
        ComboBox<String> category = new ComboBox<>();
        category.getItems().addAll("Security Issue", "System Error", "User Complaint", "Data Issue", "Other");
        category.setValue("Security Issue");
        category.setMaxWidth(Double.MAX_VALUE);

        Label sevLabel = new Label("Severity");
        sevLabel.setFont(Font.font("Segoe UI", FontWeight.MEDIUM, 13));
        sevLabel.setTextFill(Color.web(TEXT()));
        ComboBox<String> severity = new ComboBox<>();
        severity.getItems().addAll("Critical", "High", "Medium", "Low");
        severity.setValue("Medium");
        severity.setMaxWidth(Double.MAX_VALUE);

        Label descLabel = new Label("Description");
        descLabel.setFont(Font.font("Segoe UI", FontWeight.MEDIUM, 13));
        descLabel.setTextFill(Color.web(TEXT()));
        TextArea descArea = new TextArea();
        descArea.setPromptText("Describe the incident in detail...");
        descArea.setPrefRowCount(5);

        Button submit = new Button("Submit Incident Report");
        submit.setStyle("-fx-background-color: " + PRIMARY
                + "; -fx-text-fill: white; -fx-background-radius: 4; -fx-padding: 10 24; -fx-font-weight: bold; -fx-cursor: hand;");
        submit.setOnAction(e -> {
            String descText = descArea.getText().trim();
            if (descText.isEmpty()) {
                showAlert("Warning", "Please enter a description.");
                return;
            }
            String logDesc = "[" + severity.getValue() + "] " + category.getValue() + ": " + descText;
            systemLogService.save(new SystemLog("Incident", logDesc, user.getUsername(),
                    LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))));
            showAlert("Success", "Incident report submitted successfully!");
            root.setCenter(buildReportsPage());
        });

        formCard.getChildren().addAll(catLabel, category, sevLabel, severity, descLabel, descArea, new Separator(),
                submit);
        page.getChildren().addAll(backBtn, title, formCard);
        return wrapScroll(page);
    }

    private void showAlert(String titleStr, String msg) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(titleStr);
        alert.setHeaderText(null);
        alert.setContentText(msg);
        alert.showAndWait();
    }
}
