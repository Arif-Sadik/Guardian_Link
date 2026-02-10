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
import model.user.User;

/**
 * System Administrator dashboard — Figma-matched.
 * Sidebar pages: Dashboard, Child Profiles, System Admin, Reports.
 */
public class AdminController {

    private final Stage stage;
    private final User user;
    private BorderPane root;
    private VBox sidebar;
    private String activePage = "dashboard";

    // Figma tokens
    private static final String PRIMARY = "#2563eb";
    private static final String PRIMARY_FG = "#ffffff";
    private static final String SECONDARY = "#16a34a";
    private static final String WARNING = "#f59e0b";
    private static final String DESTRUCTIVE = "#dc2626";
    private static final String INFO = "#0ea5e9";
    private static final String BG = "#f8f9fa";
    private static final String CARD = "#ffffff";
    private static final String BORDER = "#e2e8f0";
    private static final String MUTED = "#f1f5f9";
    private static final String MUTED_FG = "#64748b";
    private static final String CHART1 = "#2563eb";
    private static final String CHART2 = "#16a34a";
    private static final String CHART3 = "#f59e0b";
    private static final String CHART4 = "#8b5cf6";
    private static final String CHART5 = "#ec4899";

    public AdminController(Stage stage, User user) {
        this.stage = stage;
        this.user = user;
    }

    public void show() {
        root = new BorderPane();
        root.setTop(buildHeader());
        root.setLeft(buildSidebar());
        root.setCenter(buildDashboardPage());
        root.setStyle("-fx-background-color: " + BG + ";");

        Scene scene = new Scene(root, 1280, 800);
        stage.setScene(scene);
        stage.setTitle("GuardianLink \u2014 System Administrator");
        stage.show();
    }

    // ═══════════ HEADER ═══════════
    private HBox buildHeader() {
        HBox header = new HBox();
        header.setPadding(new Insets(0, 24, 0, 24));
        header.setAlignment(Pos.CENTER_LEFT);
        header.setPrefHeight(64);
        header.setStyle(
                "-fx-background-color: " + CARD + "; -fx-border-color: " + BORDER + "; -fx-border-width: 0 0 1 0;");

        StackPane logo = new StackPane();
        logo.setPrefSize(40, 40);
        logo.setStyle("-fx-background-color: " + PRIMARY + "; -fx-background-radius: 4;");
        Label gl = new Label("GL");
        gl.setFont(Font.font("Segoe UI", FontWeight.SEMI_BOLD, 14));
        gl.setTextFill(Color.WHITE);
        logo.getChildren().add(gl);

        VBox titleBox = new VBox(2);
        titleBox.setPadding(new Insets(0, 0, 0, 12));
        Label t1 = new Label("GuardianLink");
        t1.setFont(Font.font("Segoe UI", FontWeight.MEDIUM, 15));
        Label t2 = new Label("NGO Welfare Management System");
        t2.setFont(Font.font("Segoe UI", 11));
        t2.setTextFill(Color.web(MUTED_FG));
        titleBox.getChildren().addAll(t1, t2);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        VBox userBox = new VBox(2);
        userBox.setAlignment(Pos.CENTER_RIGHT);
        Label uName = new Label(user.getUsername());
        uName.setFont(Font.font("Segoe UI", FontWeight.MEDIUM, 13));
        Label uRole = new Label("System Administrator");
        uRole.setFont(Font.font("Segoe UI", 11));
        uRole.setTextFill(Color.web(MUTED_FG));
        userBox.getChildren().addAll(uName, uRole);

        Button logoutBtn = new Button("\u23FB");
        logoutBtn.setStyle(
                "-fx-background-color: " + MUTED + "; -fx-background-radius: 4; -fx-padding: 6 10; -fx-cursor: hand;");
        logoutBtn.setOnAction(e -> new AuthController(stage).show());

        header.getChildren().addAll(logo, titleBox, spacer, userBox, logoutBtn);
        HBox.setMargin(logoutBtn, new Insets(0, 0, 0, 16));
        return header;
    }

    // ═══════════ SIDEBAR ═══════════
    private VBox buildSidebar() {
        sidebar = new VBox(4);
        sidebar.setPrefWidth(240);
        sidebar.setStyle(
                "-fx-background-color: " + CARD + "; -fx-border-color: " + BORDER + "; -fx-border-width: 0 1 0 0;");

        Label navLabel = new Label("Navigation");
        navLabel.setFont(Font.font("Segoe UI", 11));
        navLabel.setTextFill(Color.web(MUTED_FG));
        navLabel.setPadding(new Insets(16, 16, 8, 16));
        sidebar.getChildren().add(navLabel);

        VBox navItems = new VBox(2);
        navItems.setPadding(new Insets(0, 8, 0, 8));
        navItems.getChildren().addAll(
                sidebarBtn("\uD83D\uDCCA  Dashboard", "dashboard"),
                sidebarBtn("\uD83D\uDC64  Child Profiles", "children"),
                sidebarBtn("\uD83D\uDD14  Alerts", "alerts"),
                sidebarBtn("\uD83D\uDCC4  Reports & Audit", "reports"),
                sidebarBtn("\u2699  System Admin", "admin"));
        sidebar.getChildren().add(navItems);

        Region spacer = new Region();
        VBox.setVgrow(spacer, Priority.ALWAYS);
        Label ver = new Label("v1.0.0 | Academic Project");
        ver.setFont(Font.font("Segoe UI", 11));
        ver.setTextFill(Color.web(MUTED_FG));
        ver.setPadding(new Insets(16));
        ver.setStyle("-fx-border-color: " + BORDER + "; -fx-border-width: 1 0 0 0;");
        sidebar.getChildren().addAll(spacer, ver);

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
                    "-fx-background-color: transparent; -fx-text-fill: #1a1a1a; -fx-background-radius: 4; -fx-padding: 8 12; -fx-cursor: hand; -fx-font-size: 13px;");
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
        sub.setTextFill(Color.web(MUTED_FG));
        VBox header = new VBox(4, title, sub);

        // 4 stat cards
        HBox stats = new HBox(16);
        stats.getChildren().addAll(
                statCard("Total Users", "1,247", "+24 this week", PRIMARY, SECONDARY),
                statCard("Database Size", "2.4 GB", "78% capacity", INFO, MUTED_FG),
                statCard("System Health", "98%", "All systems operational", SECONDARY, SECONDARY),
                statCard("Active Sessions", "84", "Current active users", WARNING, MUTED_FG));

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
        sp.setStyle("-fx-background: " + BG + "; -fx-background-color: " + BG + ";");
        return sp;
    }

    private VBox statCard(String label, String value, String detail, String iconColor, String detailColor) {
        VBox card = new VBox(4);
        card.setPadding(new Insets(16));
        card.setStyle("-fx-background-color: " + CARD + "; -fx-border-color: " + BORDER
                + "; -fx-border-width: 1; -fx-background-radius: 8; -fx-border-radius: 8;");
        card.setPrefWidth(200);
        HBox.setHgrow(card, Priority.ALWAYS);

        Label l = new Label(label);
        l.setFont(Font.font("Segoe UI", 11));
        l.setTextFill(Color.web(MUTED_FG));

        Label v = new Label(value);
        v.setFont(Font.font("Segoe UI", FontWeight.SEMI_BOLD, 24));

        Label d = new Label(detail);
        d.setFont(Font.font("Segoe UI", 11));
        d.setTextFill(Color.web(detailColor));

        card.getChildren().addAll(l, v, d);
        return card;
    }

    private VBox buildUserDistribution() {
        VBox card = new VBox(12);
        card.setPadding(new Insets(16));
        card.setStyle("-fx-background-color: " + CARD + "; -fx-border-color: " + BORDER
                + "; -fx-border-width: 1; -fx-background-radius: 8; -fx-border-radius: 8;");

        Label t = new Label("User Distribution by Role");
        t.setFont(Font.font("Segoe UI", FontWeight.MEDIUM, 17));
        card.getChildren().add(t);

        String[][] data = {
                { "Children", "248", "20", CHART1 },
                { "Donors", "532", "43", CHART2 },
                { "Caregivers", "156", "13", CHART3 },
                { "Support Reps", "28", "2", CHART4 },
                { "Administrators", "8", "1", CHART5 }
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
            barBg.setStyle("-fx-background-color: " + MUTED + "; -fx-background-radius: 4;");
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
        card.setStyle("-fx-background-color: " + CARD + "; -fx-border-color: " + BORDER
                + "; -fx-border-width: 1; -fx-background-radius: 8; -fx-border-radius: 8;");

        Label t = new Label("System Activity (Last 7 Days)");
        t.setFont(Font.font("Segoe UI", FontWeight.MEDIUM, 17));
        card.getChildren().add(t);

        String[][] data = {
                { "New Registrations", "24" },
                { "Profile Updates", "187" },
                { "Donations Processed", "92" },
                { "Reports Generated", "45" },
                { "Alerts Resolved", "31" }
        };
        for (String[] row : data) {
            HBox item = new HBox();
            item.setPadding(new Insets(8, 0, 8, 0));
            item.setStyle("-fx-border-color: " + BORDER + "; -fx-border-width: 0 0 1 0;");
            Label name = new Label(row[0]);
            name.setFont(Font.font("Segoe UI", 13));
            Region sp = new Region();
            HBox.setHgrow(sp, Priority.ALWAYS);
            Label count = new Label(row[1]);
            count.setFont(Font.font("Segoe UI", FontWeight.SEMI_BOLD, 13));
            count.setTextFill(Color.web(PRIMARY));
            item.getChildren().addAll(name, sp, count);
            card.getChildren().add(item);
        }
        return card;
    }

    private VBox actionCard(String icon, String title, String desc, String targetPage) {
        VBox card = new VBox(4);
        card.setPadding(new Insets(16));
        card.setStyle("-fx-background-color: " + CARD + "; -fx-border-color: " + BORDER
                + "; -fx-border-width: 1; -fx-background-radius: 8; -fx-border-radius: 8; -fx-cursor: hand;");
        HBox.setHgrow(card, Priority.ALWAYS);

        Label ic = new Label(icon);
        ic.setFont(Font.font("Segoe UI Emoji", 18));
        ic.setTextFill(Color.web(PRIMARY));
        Label t = new Label(title);
        t.setFont(Font.font("Segoe UI", FontWeight.MEDIUM, 13));
        Label d = new Label(desc);
        d.setFont(Font.font("Segoe UI", 11));
        d.setTextFill(Color.web(MUTED_FG));

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
        card.setStyle("-fx-background-color: " + CARD + "; -fx-border-color: " + BORDER
                + "; -fx-border-width: 1; -fx-background-radius: 8; -fx-border-radius: 8;");

        HBox hdr = new HBox();
        hdr.setPadding(new Insets(16));
        hdr.setAlignment(Pos.CENTER_LEFT);
        hdr.setStyle("-fx-border-color: " + BORDER + "; -fx-border-width: 0 0 1 0;");
        Label t = new Label("Recent System Events");
        t.setFont(Font.font("Segoe UI", FontWeight.MEDIUM, 17));
        Region sp = new Region();
        HBox.setHgrow(sp, Priority.ALWAYS);
        Button viewAll = new Button("View All Logs");
        viewAll.setStyle("-fx-background-color: " + PRIMARY
                + "; -fx-text-fill: white; -fx-background-radius: 4; -fx-padding: 6 12; -fx-font-size: 12px; -fx-cursor: hand;");
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
            h.setStyle("-fx-background-color: " + MUTED + ";");
            grid.add(h, i, 0);
        }

        String[][] rows = {
                { "Jan 26, 2026 14:23", "User Action", "Dr. Rafiqul Islam", "Updated child profile CH-1024",
                        "Success" },
                { "Jan 26, 2026 14:15", "System", "System", "Database backup completed", "Success" },
                { "Jan 26, 2026 13:45", "User Action", "Ashab Raiyan", "Donation processed: \u09F3 6,000", "Success" },
                { "Jan 26, 2026 13:20", "Authentication", "Fahim Ahmed", "User login successful", "Success" },
                { "Jan 26, 2026 12:58", "Alert", "System", "Low wallet balance detected", "Warning" },
        };
        for (int r = 0; r < rows.length; r++) {
            for (int c = 0; c < rows[r].length; c++) {
                Label cell = new Label(rows[r][c]);
                cell.setFont(Font.font(c == 0 ? "Consolas" : "Segoe UI", c == 0 ? 11 : 13));
                if (c == 0)
                    cell.setTextFill(Color.web(MUTED_FG));
                cell.setPadding(new Insets(12, 16, 12, 16));
                cell.setMaxWidth(Double.MAX_VALUE);
                cell.setStyle("-fx-border-color: " + BORDER + "; -fx-border-width: 0 0 1 0;");

                if (c == 4) {
                    Label badge = new Label(rows[r][c]);
                    badge.setFont(Font.font("Segoe UI", 11));
                    String bgC = rows[r][c].equals("Success") ? SECONDARY + "1A" : WARNING + "1A";
                    String fgC = rows[r][c].equals("Success") ? SECONDARY : WARNING;
                    badge.setStyle("-fx-background-color: " + bgC + "; -fx-text-fill: " + fgC
                            + "; -fx-background-radius: 4; -fx-padding: 2 8;");
                    HBox wrapper = new HBox(badge);
                    wrapper.setPadding(new Insets(12, 16, 12, 16));
                    wrapper.setStyle("-fx-border-color: " + BORDER + "; -fx-border-width: 0 0 1 0;");
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
        Label sub = new Label("View and manage child profiles");
        sub.setFont(Font.font("Segoe UI", 13));
        sub.setTextFill(Color.web(MUTED_FG));

        VBox tableCard = new VBox(0);
        tableCard.setStyle("-fx-background-color: " + CARD + "; -fx-border-color: " + BORDER
                + "; -fx-border-width: 1; -fx-background-radius: 8; -fx-border-radius: 8;");

        HBox hdr = new HBox();
        hdr.setPadding(new Insets(16));
        hdr.setAlignment(Pos.CENTER_LEFT);
        hdr.setStyle("-fx-border-color: " + BORDER + "; -fx-border-width: 0 0 1 0;");
        Label tl = new Label("Assigned Children");
        tl.setFont(Font.font("Segoe UI", FontWeight.MEDIUM, 17));
        hdr.getChildren().add(tl);

        GridPane grid = new GridPane();
        String[] cols = { "Child ID", "Name", "Age", "Status", "Last Updated", "Actions" };
        for (int i = 0; i < cols.length; i++) {
            Label h = new Label(cols[i]);
            h.setFont(Font.font("Segoe UI", FontWeight.SEMI_BOLD, 11));
            h.setPadding(new Insets(8, 16, 8, 16));
            h.setMaxWidth(Double.MAX_VALUE);
            h.setStyle("-fx-background-color: " + MUTED + ";");
            grid.add(h, i, 0);
        }

        String[][] rows = {
                { "CH-1024", "Tahmid Rahman", "10", "Active", "Jan 25, 2026" },
                { "CH-1025", "Mugdho Hossain", "8", "Active", "Jan 24, 2026" },
                { "CH-1026", "Sokina Akter", "12", "Active", "Jan 23, 2026" },
                { "CH-1027", "Nabil Khan", "9", "Pending", "Jan 22, 2026" },
                { "CH-1028", "Faiza Ahmed", "11", "Active", "Jan 21, 2026" },
        };
        for (int r = 0; r < rows.length; r++) {
            for (int c = 0; c < 5; c++) {
                Label cell = new Label(rows[r][c]);
                cell.setFont(Font.font(c == 0 ? "Consolas" : "Segoe UI", c == 0 ? 12 : 13));
                if (c == 1)
                    cell.setFont(Font.font("Segoe UI", FontWeight.MEDIUM, 13));
                cell.setPadding(new Insets(12, 16, 12, 16));
                cell.setStyle("-fx-border-color: " + BORDER + "; -fx-border-width: 0 0 1 0;");

                if (c == 3) {
                    Label badge = new Label(rows[r][c]);
                    badge.setFont(Font.font("Segoe UI", 11));
                    String bgC = rows[r][c].equals("Active") ? SECONDARY + "1A" : WARNING + "1A";
                    String fgC = rows[r][c].equals("Active") ? SECONDARY : WARNING;
                    badge.setStyle("-fx-background-color: " + bgC + "; -fx-text-fill: " + fgC
                            + "; -fx-background-radius: 4; -fx-padding: 2 8;");
                    HBox w = new HBox(badge);
                    w.setPadding(new Insets(12, 16, 12, 16));
                    w.setStyle("-fx-border-color: " + BORDER + "; -fx-border-width: 0 0 1 0;");
                    grid.add(w, c, r + 1);
                    continue;
                }
                if (c == 4)
                    cell.setTextFill(Color.web(MUTED_FG));
                grid.add(cell, c, r + 1);
            }
            // Action column
            Hyperlink viewLink = new Hyperlink("View Profile");
            viewLink.setFont(Font.font("Segoe UI", 13));
            viewLink.setTextFill(Color.web(PRIMARY));
            viewLink.setStyle("-fx-border-color: " + BORDER + "; -fx-border-width: 0 0 1 0; -fx-padding: 12 16;");
            grid.add(viewLink, 5, r + 1);
        }

        tableCard.getChildren().addAll(hdr, grid);
        page.getChildren().addAll(new VBox(4, title, sub), tableCard);
        ScrollPane sp = new ScrollPane(page);
        sp.setFitToWidth(true);
        sp.setStyle("-fx-background: " + BG + "; -fx-background-color: " + BG + ";");
        return sp;
    }

    // ═══════════ ALERTS PAGE ═══════════
    private ScrollPane buildAlertsPage() {
        VBox page = new VBox(20);
        page.setPadding(new Insets(24));

        Label title = new Label("Alerts & Notifications");
        title.setFont(Font.font("Segoe UI", FontWeight.MEDIUM, 20));
        Label sub = new Label("Monitor system alerts and emergencies");
        sub.setFont(Font.font("Segoe UI", 13));
        sub.setTextFill(Color.web(MUTED_FG));

        HBox stats = new HBox(16);
        stats.getChildren().addAll(
                statCard("Total Active", "5", "Requires attention", WARNING, MUTED_FG),
                statCard("Critical", "2", "Immediate action needed", DESTRUCTIVE, DESTRUCTIVE),
                statCard("Warnings", "2", "Review soon", WARNING, WARNING),
                statCard("Resolved Today", "2", "Completed", SECONDARY, SECONDARY));

        Label activeTitle = new Label("Active Alerts");
        activeTitle.setFont(Font.font("Segoe UI", FontWeight.MEDIUM, 17));

        VBox alertsList = new VBox(12);
        String[][] alerts = {
                { "critical", "Low Wallet Balance", "ALT-001", "2 hours ago",
                        "Child CH-1024 has wallet balance below threshold", "CH-1024" },
                { "critical", "Emergency Medical Situation", "ALT-002", "3 hours ago",
                        "Child CH-1087 requires immediate medical attention", "CH-1087" },
                { "warning", "Missed Medical Appointment", "ALT-003", "1 day ago",
                        "Child CH-1024 missed scheduled medical checkup", "CH-1024" },
                { "warning", "Low Attendance Rate", "ALT-004", "1 day ago", "Child CH-1045 attendance dropped to 78%",
                        "CH-1045" },
                { "info", "New Donor Registration", "ALT-005", "2 days ago",
                        "New donor pending verification: John Smith", "" },
        };
        for (String[] a : alerts) {
            String borderC = a[0].equals("critical") ? DESTRUCTIVE : BORDER;
            String bgC = a[0].equals("critical") ? DESTRUCTIVE + "1A"
                    : a[0].equals("warning") ? WARNING + "1A" : PRIMARY + "1A";
            String iconC = a[0].equals("critical") ? DESTRUCTIVE : a[0].equals("warning") ? WARNING : PRIMARY;

            VBox alertCard = new VBox(8);
            alertCard.setPadding(new Insets(16));
            alertCard.setStyle("-fx-background-color: " + CARD + "; -fx-border-color: " + borderC
                    + "; -fx-border-width: 1; -fx-background-radius: 8; -fx-border-radius: 8;");

            HBox top = new HBox(12);
            top.setAlignment(Pos.TOP_LEFT);
            StackPane iconBox = new StackPane();
            iconBox.setPrefSize(40, 40);
            iconBox.setStyle("-fx-background-color: " + bgC + "; -fx-background-radius: 4;");
            Label icon = new Label("\u26A0");
            icon.setTextFill(Color.web(iconC));
            iconBox.getChildren().add(icon);

            VBox info = new VBox(4);
            HBox.setHgrow(info, Priority.ALWAYS);
            Label aTitle = new Label(a[1]);
            aTitle.setFont(Font.font("Segoe UI", FontWeight.MEDIUM, 14));
            Label aId = new Label(a[2]);
            aId.setFont(Font.font("Segoe UI", 11));
            aId.setTextFill(Color.web(MUTED_FG));
            Label aDesc = new Label(a[4]);
            aDesc.setFont(Font.font("Segoe UI", 13));
            aDesc.setTextFill(Color.web(MUTED_FG));
            aDesc.setWrapText(true);
            info.getChildren().addAll(aTitle, aId, aDesc);
            if (!a[5].isEmpty()) {
                Label related = new Label("Related Child: " + a[5]);
                related.setFont(Font.font("Consolas", 11));
                related.setTextFill(Color.web(MUTED_FG));
                info.getChildren().add(related);
            }

            HBox btns = new HBox(8);
            Button vd = new Button("View Details");
            vd.setStyle("-fx-background-color: " + PRIMARY
                    + "; -fx-text-fill: white; -fx-background-radius: 4; -fx-padding: 6 12; -fx-font-size: 12px; -fx-cursor: hand;");
            Button mr = new Button("Mark Resolved");
            mr.setStyle("-fx-background-color: " + SECONDARY
                    + "; -fx-text-fill: white; -fx-background-radius: 4; -fx-padding: 6 12; -fx-font-size: 12px; -fx-cursor: hand;");
            btns.getChildren().addAll(vd, mr);
            info.getChildren().add(btns);

            Label time = new Label(a[3]);
            time.setFont(Font.font("Segoe UI", 11));
            time.setTextFill(Color.web(MUTED_FG));

            top.getChildren().addAll(iconBox, info, time);
            alertCard.getChildren().add(top);
            alertsList.getChildren().add(alertCard);
        }

        page.getChildren().addAll(new VBox(4, title, sub), stats, activeTitle, alertsList);
        ScrollPane sp = new ScrollPane(page);
        sp.setFitToWidth(true);
        sp.setStyle("-fx-background: " + BG + "; -fx-background-color: " + BG + ";");
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
        sub.setTextFill(Color.web(MUTED_FG));

        // Report Generator controls
        VBox genCard = new VBox(16);
        genCard.setPadding(new Insets(16));
        genCard.setStyle("-fx-background-color: " + CARD + "; -fx-border-color: " + BORDER
                + "; -fx-border-width: 1; -fx-background-radius: 8; -fx-border-radius: 8;");
        Label genTitle = new Label("Report Generator");
        genTitle.setFont(Font.font("Segoe UI", FontWeight.MEDIUM, 17));

        HBox row1 = new HBox(24);
        VBox col1 = new VBox(8);
        Label rtLabel = new Label("Report Type");
        rtLabel.setFont(Font.font("Segoe UI", FontWeight.MEDIUM, 13));
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
        ComboBox<String> drCombo = new ComboBox<>();
        drCombo.getItems().addAll("Last 7 Days", "Last 30 Days", "Last 3 Months", "Last Year");
        drCombo.setValue("Last 30 Days");
        drCombo.setMaxWidth(Double.MAX_VALUE);
        col2.getChildren().addAll(drLabel, drCombo);
        HBox.setHgrow(col2, Priority.ALWAYS);
        row1.getChildren().addAll(col1, col2);

        HBox buttons = new HBox(12);
        Button genBtn = new Button("\uD83D\uDCC4  Generate Report");
        genBtn.setStyle("-fx-background-color: " + PRIMARY
                + "; -fx-text-fill: white; -fx-background-radius: 4; -fx-padding: 8 16; -fx-font-size: 13px; -fx-cursor: hand;");
        Button csvBtn = new Button("\u2B07  Export to CSV");
        csvBtn.setStyle("-fx-background-color: " + SECONDARY
                + "; -fx-text-fill: white; -fx-background-radius: 4; -fx-padding: 8 16; -fx-font-size: 13px; -fx-cursor: hand;");
        buttons.getChildren().addAll(genBtn, csvBtn);

        genCard.getChildren().addAll(genTitle, row1, buttons);

        // Donation Report table
        VBox reportCard = new VBox(0);
        reportCard.setStyle("-fx-background-color: " + CARD + "; -fx-border-color: " + BORDER
                + "; -fx-border-width: 1; -fx-background-radius: 8; -fx-border-radius: 8;");

        HBox rHdr = new HBox();
        rHdr.setPadding(new Insets(16));
        rHdr.setStyle("-fx-border-color: " + BORDER + "; -fx-border-width: 0 0 1 0;");
        Label rTitle = new Label("Report Preview - Donation & Financial Report");
        rTitle.setFont(Font.font("Segoe UI", FontWeight.MEDIUM, 17));
        rHdr.getChildren().add(rTitle);

        // Summary stats
        HBox summaryStats = new HBox(16);
        summaryStats.setPadding(new Insets(16));
        for (String[] s : new String[][] {
                { "Total Donations", "$124,850", null },
                { "Total Disbursed", "$118,200", null },
                { "Available Balance", "$6,650", SECONDARY }
        }) {
            VBox sBox = new VBox(4);
            sBox.setPadding(new Insets(12));
            sBox.setStyle("-fx-background-color: " + MUTED + "; -fx-background-radius: 4;");
            HBox.setHgrow(sBox, Priority.ALWAYS);
            Label sl = new Label(s[0]);
            sl.setFont(Font.font("Segoe UI", 11));
            sl.setTextFill(Color.web(MUTED_FG));
            Label sv = new Label(s[1]);
            sv.setFont(Font.font("Segoe UI", FontWeight.SEMI_BOLD, 20));
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
            h.setStyle("-fx-background-color: " + MUTED + ";");
            h.setMaxWidth(Double.MAX_VALUE);
            donGrid.add(h, i, 0);
        }
        String[][] donRows = {
                { "Emily Johnson", "Sarah Williams", "$150", "Jan 20, 2026", "Education" },
                { "Emily Johnson", "Michael Omondi", "$200", "Jan 20, 2026", "Medical" },
                { "John Smith", "Amina Hassan", "$180", "Jan 19, 2026", "General Welfare" },
                { "Emily Johnson", "Sarah Williams", "$150", "Jan 15, 2026", "Food & Nutrition" },
                { "Maria Garcia", "David Kimani", "$220", "Jan 14, 2026", "Education" },
        };
        for (int r = 0; r < donRows.length; r++) {
            for (int c = 0; c < donRows[r].length; c++) {
                Label cell = new Label(donRows[r][c]);
                cell.setFont(Font.font("Segoe UI", 13));
                cell.setPadding(new Insets(8, 12, 8, 12));
                cell.setStyle("-fx-border-color: " + BORDER + "; -fx-border-width: 0 0 1 0;");
                if (c == 2) {
                    cell.setTextFill(Color.web(SECONDARY));
                    cell.setFont(Font.font("Segoe UI", FontWeight.SEMI_BOLD, 13));
                }
                if (c == 3)
                    cell.setTextFill(Color.web(MUTED_FG));
                donGrid.add(cell, c, r + 1);
            }
        }

        reportCard.getChildren().addAll(rHdr, summaryStats, donGrid);

        // Quick stats at bottom
        HBox qStats = new HBox(16);
        qStats.getChildren().addAll(
                statCard("Reports Generated", "1,247", "All time", PRIMARY, MUTED_FG),
                statCard("This Month", "45", "+12% from last month", SECONDARY, SECONDARY),
                statCard("Audit Entries", "28,394", "Total logged events", PRIMARY, MUTED_FG),
                statCard("Data Retention", "2 Years", "Compliance period", PRIMARY, MUTED_FG));

        page.getChildren().addAll(new VBox(4, title, sub), genCard, reportCard, qStats);
        ScrollPane sp = new ScrollPane(page);
        sp.setFitToWidth(true);
        sp.setStyle("-fx-background: " + BG + "; -fx-background-color: " + BG + ";");
        return sp;
    }

    // ═══════════ ADMIN (USER MANAGEMENT) PAGE ═══════════
    private ScrollPane buildAdminPage() {
        VBox page = new VBox(20);
        page.setPadding(new Insets(24));

        Label title = new Label("System Administration");
        title.setFont(Font.font("Segoe UI", FontWeight.MEDIUM, 20));
        Label sub = new Label("Manage users, roles, and system configuration");
        sub.setFont(Font.font("Segoe UI", 13));
        sub.setTextFill(Color.web(MUTED_FG));

        HBox stats = new HBox(16);
        stats.getChildren().addAll(
                statCard("Total Users", "1,247", "+24 this week", PRIMARY, SECONDARY),
                statCard("Active Users", "1,198", "96% of total", SECONDARY, MUTED_FG),
                statCard("Pending Approval", "12", "Awaiting verification", WARNING, WARNING),
                statCard("Roles Configured", "5", "System roles", PRIMARY, MUTED_FG));

        // User Management table
        VBox userCard = new VBox(0);
        userCard.setStyle("-fx-background-color: " + CARD + "; -fx-border-color: " + BORDER
                + "; -fx-border-width: 1; -fx-background-radius: 8; -fx-border-radius: 8;");

        HBox hdr = new HBox(12);
        hdr.setPadding(new Insets(16));
        hdr.setAlignment(Pos.CENTER_LEFT);
        hdr.setStyle("-fx-border-color: " + BORDER + "; -fx-border-width: 0 0 1 0;");
        Label tl = new Label("User Management");
        tl.setFont(Font.font("Segoe UI", FontWeight.MEDIUM, 17));
        Region sp1 = new Region();
        HBox.setHgrow(sp1, Priority.ALWAYS);
        TextField search = new TextField();
        search.setPromptText("Search users...");
        search.setPrefWidth(256);
        search.setStyle("-fx-background-color: " + CARD + "; -fx-border-color: " + BORDER
                + "; -fx-border-width: 1; -fx-border-radius: 4; -fx-background-radius: 4; -fx-padding: 6 12; -fx-font-size: 12px;");
        Button addUser = new Button("+ Add User");
        addUser.setStyle("-fx-background-color: " + PRIMARY
                + "; -fx-text-fill: white; -fx-background-radius: 4; -fx-padding: 6 16; -fx-font-size: 12px; -fx-cursor: hand;");
        hdr.getChildren().addAll(tl, sp1, search, addUser);

        GridPane grid = new GridPane();
        String[] cols = { "User ID", "Name", "Email", "Role", "Status", "Last Login", "Actions" };
        for (int i = 0; i < cols.length; i++) {
            Label h = new Label(cols[i]);
            h.setFont(Font.font("Segoe UI", FontWeight.SEMI_BOLD, 11));
            h.setPadding(new Insets(8, 16, 8, 16));
            h.setMaxWidth(Double.MAX_VALUE);
            h.setStyle("-fx-background-color: " + MUTED + ";");
            grid.add(h, i, 0);
        }

        String[][] users = {
                { "USR-001", "Sarah Williams", "sarah@example.com", "Child", "Active", "Jan 25, 2026" },
                { "USR-002", "Dr. Michael Chen", "mchen@guardianlink.org", "Caregiver", "Active", "Jan 26, 2026" },
                { "USR-003", "Emily Johnson", "ejohnson@email.com", "Donor", "Active", "Jan 26, 2026" },
                { "USR-004", "James Rodriguez", "jrodriguez@guardianlink.org", "Support", "Active", "Jan 26, 2026" },
                { "USR-005", "Admin User", "admin@guardianlink.org", "Admin", "Active", "Jan 26, 2026" },
                { "USR-006", "John Smith", "jsmith@email.com", "Donor", "Pending", "Never" },
                { "USR-007", "Maria Garcia", "mgarcia@guardianlink.org", "Caregiver", "Pending", "Never" },
        };
        for (int r = 0; r < users.length; r++) {
            for (int c = 0; c < users[r].length; c++) {
                Label cell = new Label(users[r][c]);
                cell.setFont(Font.font(c == 0 ? "Consolas" : "Segoe UI", c == 0 ? 12 : 13));
                if (c == 1)
                    cell.setFont(Font.font("Segoe UI", FontWeight.MEDIUM, 13));
                if (c == 2 || c == 5)
                    cell.setTextFill(Color.web(MUTED_FG));
                cell.setPadding(new Insets(12, 16, 12, 16));
                cell.setStyle("-fx-border-color: " + BORDER + "; -fx-border-width: 0 0 1 0;");

                if (c == 4) {
                    Label badge = new Label(users[r][c]);
                    badge.setFont(Font.font("Segoe UI", 11));
                    String bc = users[r][c].equals("Active") ? SECONDARY + "1A" : WARNING + "1A";
                    String fc = users[r][c].equals("Active") ? SECONDARY : WARNING;
                    badge.setStyle("-fx-background-color: " + bc + "; -fx-text-fill: " + fc
                            + "; -fx-background-radius: 4; -fx-padding: 2 8;");
                    HBox w = new HBox(badge);
                    w.setPadding(new Insets(12, 16, 12, 16));
                    w.setStyle("-fx-border-color: " + BORDER + "; -fx-border-width: 0 0 1 0;");
                    grid.add(w, c, r + 1);
                    continue;
                }
                grid.add(cell, c, r + 1);
            }
            // Actions
            HBox actions = new HBox(8);
            actions.setPadding(new Insets(12, 16, 12, 16));
            actions.setStyle("-fx-border-color: " + BORDER + "; -fx-border-width: 0 0 1 0;");
            Button edit = new Button("\u270E");
            edit.setStyle("-fx-background-color: transparent; -fx-text-fill: " + PRIMARY
                    + "; -fx-padding: 4; -fx-cursor: hand;");
            Button del = new Button("\u2716");
            del.setStyle("-fx-background-color: transparent; -fx-text-fill: " + DESTRUCTIVE
                    + "; -fx-padding: 4; -fx-cursor: hand;");
            actions.getChildren().addAll(edit, del);
            grid.add(actions, 6, r + 1);
        }

        userCard.getChildren().addAll(hdr, grid);

        // Role Configuration
        VBox roleCard = new VBox(0);
        roleCard.setStyle("-fx-background-color: " + CARD + "; -fx-border-color: " + BORDER
                + "; -fx-border-width: 1; -fx-background-radius: 8; -fx-border-radius: 8;");
        HBox rHdr = new HBox();
        rHdr.setPadding(new Insets(16));
        rHdr.setStyle("-fx-border-color: " + BORDER + "; -fx-border-width: 0 0 1 0;");
        Label rTitle = new Label("Role Configuration & Permissions");
        rTitle.setFont(Font.font("Segoe UI", FontWeight.MEDIUM, 17));
        rHdr.getChildren().add(rTitle);

        VBox rolesContent = new VBox(12);
        rolesContent.setPadding(new Insets(16));
        String[][] roles = {
                { "Child", "248", "View own profile, View sponsorship status, View wallet balance" },
                { "Caregiver", "156",
                        "Manage child profiles, Update medical records, Update education records, View all children" },
                { "Donor", "532",
                        "View sponsored children, Make donations, View transaction history, Generate reports" },
                { "Support", "28", "User verification, Monitor alerts, View activity logs, Resolve issues" },
                { "Admin", "8", "Full system access, User management, Role configuration, System settings" },
        };
        for (String[] role : roles) {
            VBox roleBox = new VBox(8);
            roleBox.setPadding(new Insets(16));
            roleBox.setStyle("-fx-background-color: " + MUTED + "; -fx-background-radius: 8;");

            HBox roleHdr = new HBox();
            VBox roleInfo = new VBox(2);
            Label rn = new Label(role[0]);
            rn.setFont(Font.font("Segoe UI", FontWeight.MEDIUM, 14));
            Label ru = new Label(role[1] + " users");
            ru.setFont(Font.font("Segoe UI", 11));
            ru.setTextFill(Color.web(MUTED_FG));
            roleInfo.getChildren().addAll(rn, ru);
            Region rsp = new Region();
            HBox.setHgrow(rsp, Priority.ALWAYS);
            Button ep = new Button("Edit Permissions");
            ep.setStyle("-fx-background-color: " + PRIMARY
                    + "; -fx-text-fill: white; -fx-background-radius: 4; -fx-padding: 6 12; -fx-font-size: 12px; -fx-cursor: hand;");
            roleHdr.getChildren().addAll(roleInfo, rsp, ep);

            FlowPane perms = new FlowPane(8, 8);
            for (String p : role[2].split(", ")) {
                Label pLabel = new Label(p);
                pLabel.setFont(Font.font("Segoe UI", 11));
                pLabel.setPadding(new Insets(4, 8, 4, 8));
                pLabel.setStyle("-fx-background-color: " + CARD + "; -fx-background-radius: 4;");
                perms.getChildren().add(pLabel);
            }

            roleBox.getChildren().addAll(roleHdr, perms);
            rolesContent.getChildren().add(roleBox);
        }
        roleCard.getChildren().addAll(rHdr, rolesContent);

        page.getChildren().addAll(new VBox(4, title, sub), stats, userCard, roleCard);
        ScrollPane scp = new ScrollPane(page);
        scp.setFitToWidth(true);
        scp.setStyle("-fx-background: " + BG + "; -fx-background-color: " + BG + ";");
        return scp;
    }
}
