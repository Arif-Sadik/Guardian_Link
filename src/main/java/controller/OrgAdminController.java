package controller;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
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
import javafx.util.Duration;
import model.entity.Child;
import model.entity.Donation;
import model.entity.Expense;
import model.entity.SystemLog;
import model.user.User;
import service.ChildService;
import service.DonationService;
import service.ExpenseService;
import service.SystemLogService;
import util.ThemeManager;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Organization Admin (Caregiver) dashboard — Figma-matched.
 * Sidebar pages: Dashboard, Child Profiles, Sponsorships, Alerts, Reports.
 */
public class OrgAdminController {

    private final Stage stage;
    private final User user;
    private BorderPane root;
    private VBox sidebar;
    private String activePage = "dashboard";

    private final ChildService childService = new ChildService();
    private final DonationService donationService = new DonationService();
    private final SystemLogService systemLogService = new SystemLogService();
    private final ExpenseService expenseService = new ExpenseService();

    private Child selectedChild; // currently selected child for profile tabs

    private static final String PRIMARY = ThemeManager.PRIMARY;
    private static final String SECONDARY = ThemeManager.SECONDARY;
    private static final String WARNING = ThemeManager.WARNING;
    private static final String DESTRUCTIVE = ThemeManager.DESTRUCTIVE;

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

    public OrgAdminController(Stage stage, User user) {
        this.stage = stage;
        this.user = user;
    }

    public void show() {
        root = new BorderPane();
        root.setTop(buildHeader());
        root.setLeft(buildSidebar());
        root.setCenter(buildDashboardPage());
        root.setStyle("-fx-background-color: " + BG() + ";");

        // Auto-refresh timer: refresh the active page every 30 seconds for real-time
        // data
        Timeline refreshTimer = new Timeline(new KeyFrame(Duration.seconds(30), ev -> {
            switch (activePage) {
                case "dashboard" -> root.setCenter(buildDashboardPage());
                case "children" -> root.setCenter(buildChildrenPage());
                case "sponsorship" -> root.setCenter(buildSponsorshipPage());
                case "alerts" -> root.setCenter(buildAlertsPage());
                case "reports" -> root.setCenter(buildReportsPage());
            }
        }));
        refreshTimer.setCycleCount(Timeline.INDEFINITE);
        refreshTimer.play();

        Scene scene = new Scene(root, 1280, 800);
        stage.setScene(scene);
        stage.setTitle("GuardianLink \u2014 Organization Admin");
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
            case "sponsorship" -> root.setCenter(buildSponsorshipPage());
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
        header.setStyle(
                "-fx-background-color: " + CARD() + "; -fx-border-color: " + BORDER() + "; -fx-border-width: 0 0 1 0;");

        // GuardianLink Shield Logo
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
        Label uRole = new Label("Organization Admin");
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
                sidebarBtn("Sponsorships", "sponsorship"),
                sidebarBtn("Alerts", "alerts"),
                sidebarBtn("Reports & Audit", "reports"));
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
        styleSidebarBtn(btn, pageId.equals(activePage));
        btn.setOnAction(e -> {
            activePage = pageId;
            refreshSidebar();
            switch (pageId) {
                case "dashboard" -> root.setCenter(buildDashboardPage());
                case "children" -> root.setCenter(buildChildrenPage());
                case "sponsorship" -> root.setCenter(buildSponsorshipPage());
                case "alerts" -> root.setCenter(buildAlertsPage());
                case "reports" -> root.setCenter(buildReportsPage());
            }
        });
        return btn;
    }

    private void styleSidebarBtn(Button btn, boolean active) {
        if (active)
            btn.setStyle("-fx-background-color: " + PRIMARY
                    + "; -fx-text-fill: white; -fx-background-radius: 4; -fx-padding: 8 12; -fx-cursor: hand; -fx-font-size: 13px;");
        else
            btn.setStyle(
                    "-fx-background-color: transparent; -fx-text-fill: " + TEXT()
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
        if (text.contains("Child"))
            return "children";
        if (text.contains("Sponsor"))
            return "sponsorship";
        if (text.contains("Alerts"))
            return "alerts";
        if (text.contains("Reports"))
            return "reports";
        return "";
    }

    // helper to wrap pages in a styled ScrollPane
    private ScrollPane wrapScroll(VBox page) {
        ScrollPane sp = new ScrollPane(page);
        sp.setFitToWidth(true);
        sp.setStyle("-fx-background: " + BG() + "; -fx-background-color: " + BG() + ";");
        return sp;
    }

    private VBox statCard(String label, String value, String detail, String detailColor) {
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
        d.setTextFill(Color.web(detailColor));
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

        // Hover effect
        card.setOnMouseEntered(e -> card.setStyle("-fx-background-color: " + MUTED() + "; -fx-border-color: " + PRIMARY
                + "; -fx-border-width: 1; -fx-background-radius: 8; -fx-border-radius: 8; -fx-cursor: hand;"));
        card.setOnMouseExited(e -> card.setStyle("-fx-background-color: " + CARD() + "; -fx-border-color: " + BORDER()
                + "; -fx-border-width: 1; -fx-background-radius: 8; -fx-border-radius: 8; -fx-cursor: hand;"));
        return card;
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private VBox buildChildrenTableFromDB(String heading) {
        VBox card = new VBox(0);
        card.setStyle("-fx-background-color: " + CARD() + "; -fx-border-color: " + BORDER()
                + "; -fx-border-width: 1; -fx-background-radius: 8; -fx-border-radius: 8;");

        HBox hdr = new HBox();
        hdr.setPadding(new Insets(16));
        hdr.setStyle("-fx-border-color: " + BORDER() + "; -fx-border-width: 0 0 1 0;");
        Label t = new Label(heading);
        t.setFont(Font.font("Segoe UI", FontWeight.MEDIUM, 17));
        t.setTextFill(Color.web(TEXT()));
        hdr.getChildren().add(t);

        GridPane grid = new GridPane();
        String[] cols = { "Child ID", "Name", "Age", "Status", "Gender", "Actions" };
        for (int i = 0; i < cols.length; i++) {
            Label h = new Label(cols[i]);
            h.setFont(Font.font("Segoe UI", FontWeight.SEMI_BOLD, 11));
            h.setPadding(new Insets(8, 16, 8, 16));
            h.setMaxWidth(Double.MAX_VALUE);
            h.setStyle("-fx-background-color: " + MUTED() + ";");
            h.setTextFill(Color.web(TEXT()));
            grid.add(h, i, 0);
        }

        // Load children from database
        List<Child> children = childService.getAllChildren();
        int row = 1;
        for (Child child : children) {
            // Child ID
            Label idLabel = new Label("CH-" + String.format("%04d", child.getId()));
            idLabel.setFont(Font.font("Consolas", 12));
            idLabel.setTextFill(Color.web(MUTED_FG()));
            idLabel.setPadding(new Insets(12, 16, 12, 16));
            idLabel.setStyle("-fx-border-color: " + BORDER() + "; -fx-border-width: 0 0 1 0;");
            grid.add(idLabel, 0, row);

            // Name
            Label nameLabel = new Label(child.getName());
            nameLabel.setFont(Font.font("Segoe UI", FontWeight.MEDIUM, 13));
            nameLabel.setTextFill(Color.web(TEXT()));
            nameLabel.setPadding(new Insets(12, 16, 12, 16));
            nameLabel.setStyle("-fx-border-color: " + BORDER() + "; -fx-border-width: 0 0 1 0;");
            grid.add(nameLabel, 1, row);

            // Age
            Label ageLabel = new Label(String.valueOf(child.getAge()));
            ageLabel.setFont(Font.font("Segoe UI", 13));
            ageLabel.setTextFill(Color.web(TEXT()));
            ageLabel.setPadding(new Insets(12, 16, 12, 16));
            ageLabel.setStyle("-fx-border-color: " + BORDER() + "; -fx-border-width: 0 0 1 0;");
            grid.add(ageLabel, 2, row);

            // Status badge
            String status = child.getStatus() != null ? child.getStatus() : "Active";
            Label badge = new Label(status);
            badge.setFont(Font.font("Segoe UI", 11));
            String bc = status.equals("Active") ? SECONDARY + "1A" : WARNING + "1A";
            String fc = status.equals("Active") ? SECONDARY : WARNING;
            badge.setStyle("-fx-background-color: " + bc + "; -fx-text-fill: " + fc
                    + "; -fx-background-radius: 4; -fx-padding: 2 8;");
            HBox badgeWrapper = new HBox(badge);
            badgeWrapper.setPadding(new Insets(12, 16, 12, 16));
            badgeWrapper.setStyle("-fx-border-color: " + BORDER() + "; -fx-border-width: 0 0 1 0;");
            grid.add(badgeWrapper, 3, row);

            // Gender
            Label genderLabel = new Label(child.getGender() != null ? child.getGender() : "N/A");
            genderLabel.setFont(Font.font("Segoe UI", 13));
            genderLabel.setTextFill(Color.web(MUTED_FG()));
            genderLabel.setPadding(new Insets(12, 16, 12, 16));
            genderLabel.setStyle("-fx-border-color: " + BORDER() + "; -fx-border-width: 0 0 1 0;");
            grid.add(genderLabel, 4, row);

            // Actions - View Profile link
            final Child viewChild = child;
            Hyperlink link = new Hyperlink("View Profile");
            link.setFont(Font.font("Segoe UI", 13));
            link.setTextFill(Color.web(PRIMARY));
            link.setStyle("-fx-border-color: " + BORDER() + "; -fx-border-width: 0 0 1 0; -fx-padding: 12 16;");
            link.setOnAction(e -> {
                selectedChild = viewChild;
                activePage = "children";
                refreshSidebar();
                root.setCenter(buildChildProfileView(viewChild));
            });
            grid.add(link, 5, row);

            row++;
        }

        // If no children, show empty message
        if (children.isEmpty()) {
            Label emptyLabel = new Label("No children registered yet. Click 'Add Child Profile' to add one.");
            emptyLabel.setFont(Font.font("Segoe UI", 13));
            emptyLabel.setTextFill(Color.web(MUTED_FG()));
            emptyLabel.setPadding(new Insets(24));
            grid.add(emptyLabel, 0, 1);
            GridPane.setColumnSpan(emptyLabel, 6);
        }

        card.getChildren().addAll(hdr, grid);
        return card;
    }

    // ═══════════ DASHBOARD PAGE ═══════════
    private ScrollPane buildDashboardPage() {
        VBox page = new VBox(20);
        page.setPadding(new Insets(24));

        Label title = new Label("Organization Dashboard");
        title.setFont(Font.font("Segoe UI", FontWeight.MEDIUM, 20));
        title.setTextFill(Color.web(TEXT()));
        Label sub = new Label("Child welfare monitoring and case management");
        sub.setFont(Font.font("Segoe UI", 13));
        sub.setTextFill(Color.web(MUTED_FG()));

        int totalChildren = childService.getAllChildren().size();
        HBox stats = new HBox(16);
        stats.getChildren().addAll(
                statCard("Total Children", String.valueOf(totalChildren), "+" + totalChildren + " registered",
                        SECONDARY),
                statCard("Active Cases", String.valueOf(totalChildren), "Currently monitored", PRIMARY),
                statCard("Pending Alerts", "0", "Requires attention", WARNING),
                statCard("System Logs", String.valueOf(systemLogService.getCount()), "Total entries", MUTED_FG()));

        // Quick Actions
        Label qaTitle = new Label("Quick Actions");
        qaTitle.setFont(Font.font("Segoe UI", FontWeight.MEDIUM, 17));
        qaTitle.setTextFill(Color.web(TEXT()));
        HBox qaCards = new HBox(12);

        // Add Child Profile card
        VBox addChildCard = createQuickActionCard("Add Child Profile", "Register new child");
        addChildCard.setOnMouseClicked(e -> showAddChildDialog());

        // View Alerts card
        VBox viewAlertsCard = createQuickActionCard("View Alerts", "Check pending alerts");
        viewAlertsCard.setOnMouseClicked(e -> {
            activePage = "alerts";
            refreshSidebar();
            root.setCenter(buildAlertsPage());
        });

        // Generate Report card
        VBox genReportCard = createQuickActionCard("Generate Report", "Create welfare report");
        genReportCard.setOnMouseClicked(e -> {
            activePage = "reports";
            refreshSidebar();
            root.setCenter(buildReportsPage());
        });

        qaCards.getChildren().addAll(addChildCard, viewAlertsCard, genReportCard);

        // Assigned Children table - load from database
        VBox tableCard = buildChildrenTableFromDB("Assigned Children");

        page.getChildren().addAll(new VBox(4, title, sub), stats, qaTitle, qaCards, tableCard);
        return wrapScroll(page);
    }

    private VBox buildChildrenTable(String heading, String[][] rows) {
        VBox card = new VBox(0);
        card.setStyle("-fx-background-color: " + CARD() + "; -fx-border-color: " + BORDER()
                + "; -fx-border-width: 1; -fx-background-radius: 8; -fx-border-radius: 8;");

        HBox hdr = new HBox();
        hdr.setPadding(new Insets(16));
        hdr.setStyle("-fx-border-color: " + BORDER() + "; -fx-border-width: 0 0 1 0;");
        Label t = new Label(heading);
        t.setFont(Font.font("Segoe UI", FontWeight.MEDIUM, 17));
        hdr.getChildren().add(t);

        GridPane grid = new GridPane();
        String[] cols = { "Child ID", "Name", "Age", "Status", "Last Updated", "Actions" };
        for (int i = 0; i < cols.length; i++) {
            Label h = new Label(cols[i]);
            h.setFont(Font.font("Segoe UI", FontWeight.SEMI_BOLD, 11));
            h.setPadding(new Insets(8, 16, 8, 16));
            h.setMaxWidth(Double.MAX_VALUE);
            h.setStyle("-fx-background-color: " + MUTED() + ";");
            grid.add(h, i, 0);
        }
        for (int r = 0; r < rows.length; r++) {
            for (int c = 0; c < rows[r].length; c++) {
                if (c == 3) {
                    Label badge = new Label(rows[r][c]);
                    badge.setFont(Font.font("Segoe UI", 11));
                    String bc = rows[r][c].equals("Active") ? SECONDARY + "1A" : WARNING + "1A";
                    String fc = rows[r][c].equals("Active") ? SECONDARY : WARNING;
                    badge.setStyle("-fx-background-color: " + bc + "; -fx-text-fill: " + fc
                            + "; -fx-background-radius: 4; -fx-padding: 2 8;");
                    HBox w = new HBox(badge);
                    w.setPadding(new Insets(12, 16, 12, 16));
                    w.setStyle("-fx-border-color: " + BORDER() + "; -fx-border-width: 0 0 1 0;");
                    grid.add(w, c, r + 1);
                    continue;
                }
                Label cell = new Label(rows[r][c]);
                cell.setFont(Font.font(c == 0 ? "Consolas" : "Segoe UI", c == 0 ? 12 : 13));
                if (c == 1)
                    cell.setFont(Font.font("Segoe UI", FontWeight.MEDIUM, 13));
                if (c == 4)
                    cell.setTextFill(Color.web(MUTED_FG()));
                cell.setPadding(new Insets(12, 16, 12, 16));
                cell.setStyle("-fx-border-color: " + BORDER() + "; -fx-border-width: 0 0 1 0;");
                grid.add(cell, c, r + 1);
            }
            Hyperlink link = new Hyperlink("View Profile");
            link.setFont(Font.font("Segoe UI", 13));
            link.setTextFill(Color.web(PRIMARY));
            link.setStyle("-fx-border-color: " + BORDER() + "; -fx-border-width: 0 0 1 0; -fx-padding: 12 16;");
            grid.add(link, 5, r + 1);
        }
        card.getChildren().addAll(hdr, grid);
        return card;
    }

    // ═══════════ CHILD PROFILES PAGE ═══════════
    private ScrollPane buildChildrenPage() {
        VBox page = new VBox(20);
        page.setPadding(new Insets(24));

        Label title = new Label("Child Profile Management");
        title.setFont(Font.font("Segoe UI", FontWeight.MEDIUM, 20));
        title.setTextFill(Color.web(TEXT()));
        Label sub = new Label("View and manage all assigned children");
        sub.setFont(Font.font("Segoe UI", 13));
        sub.setTextFill(Color.web(MUTED_FG()));

        // Add Child button
        Button addChildBtn = new Button("+ Add Child Profile");
        addChildBtn.setStyle("-fx-background-color: " + PRIMARY
                + "; -fx-text-fill: white; -fx-background-radius: 4; -fx-padding: 8 16; -fx-font-size: 13px; -fx-cursor: hand;");
        addChildBtn.setOnAction(e -> showAddChildDialog());

        HBox topRow = new HBox();
        topRow.setAlignment(Pos.CENTER_LEFT);
        Region topSpacer = new Region();
        HBox.setHgrow(topSpacer, Priority.ALWAYS);
        topRow.getChildren().addAll(new VBox(4, title, sub), topSpacer, addChildBtn);

        // Load all children from database
        List<Child> allChildren = childService.getAllChildren();

        // Stats row
        int activeCount = (int) allChildren.stream()
                .filter(ch -> ch.getStatus() == null || "Active".equals(ch.getStatus())).count();
        HBox stats = new HBox(16);
        stats.getChildren().addAll(
                statCard("Total Children", String.valueOf(allChildren.size()), "Registered", SECONDARY),
                statCard("Active", String.valueOf(activeCount), "Currently monitored", PRIMARY),
                statCard("Organizations", String.valueOf(allChildren.stream()
                        .map(Child::getOrganization).filter(o -> o != null && !o.isEmpty())
                        .distinct().count()), "Distinct", MUTED_FG()));

        // Children table
        VBox tableCard = new VBox(0);
        tableCard.setStyle("-fx-background-color: " + CARD() + "; -fx-border-color: " + BORDER()
                + "; -fx-border-width: 1; -fx-background-radius: 8; -fx-border-radius: 8;");

        HBox tblHdr = new HBox();
        tblHdr.setPadding(new Insets(16));
        tblHdr.setAlignment(Pos.CENTER_LEFT);
        tblHdr.setStyle("-fx-border-color: " + BORDER() + "; -fx-border-width: 0 0 1 0;");
        Label tblTitle = new Label("All Children (" + allChildren.size() + ")");
        tblTitle.setFont(Font.font("Segoe UI", FontWeight.MEDIUM, 17));
        tblTitle.setTextFill(Color.web(TEXT()));
        tblHdr.getChildren().add(tblTitle);

        GridPane grid = new GridPane();
        String[] cols = { "Child ID", "Name", "Age", "Gender", "Status", "Organization", "Actions" };
        for (int i = 0; i < cols.length; i++) {
            Label h = new Label(cols[i]);
            h.setFont(Font.font("Segoe UI", FontWeight.SEMI_BOLD, 11));
            h.setPadding(new Insets(8, 14, 8, 14));
            h.setMaxWidth(Double.MAX_VALUE);
            h.setStyle("-fx-background-color: " + MUTED() + ";");
            h.setTextFill(Color.web(TEXT()));
            grid.add(h, i, 0);
        }

        if (allChildren.isEmpty()) {
            Label empty = new Label("No children registered yet. Click 'Add Child Profile' to register one.");
            empty.setFont(Font.font("Segoe UI", 13));
            empty.setTextFill(Color.web(MUTED_FG()));
            empty.setPadding(new Insets(24));
            grid.add(empty, 0, 1);
            GridPane.setColumnSpan(empty, cols.length);
        }

        for (int r = 0; r < allChildren.size(); r++) {
            Child child = allChildren.get(r);
            String cellBorder = "-fx-border-color: " + BORDER() + "; -fx-border-width: 0 0 1 0;";

            Label idL = new Label("CH-" + String.format("%04d", child.getId()));
            idL.setFont(Font.font("Consolas", 12));
            idL.setTextFill(Color.web(MUTED_FG()));
            idL.setPadding(new Insets(10, 14, 10, 14));
            idL.setStyle(cellBorder);
            grid.add(idL, 0, r + 1);

            Label nameL = new Label(child.getName());
            nameL.setFont(Font.font("Segoe UI", FontWeight.MEDIUM, 13));
            nameL.setTextFill(Color.web(TEXT()));
            nameL.setPadding(new Insets(10, 14, 10, 14));
            nameL.setStyle(cellBorder);
            grid.add(nameL, 1, r + 1);

            Label ageL = new Label(String.valueOf(child.getAge()));
            ageL.setFont(Font.font("Segoe UI", 13));
            ageL.setTextFill(Color.web(TEXT()));
            ageL.setPadding(new Insets(10, 14, 10, 14));
            ageL.setStyle(cellBorder);
            grid.add(ageL, 2, r + 1);

            Label genderL = new Label(child.getGender() != null ? child.getGender() : "N/A");
            genderL.setFont(Font.font("Segoe UI", 13));
            genderL.setTextFill(Color.web(MUTED_FG()));
            genderL.setPadding(new Insets(10, 14, 10, 14));
            genderL.setStyle(cellBorder);
            grid.add(genderL, 3, r + 1);

            String status = child.getStatus() != null ? child.getStatus() : "Active";
            Label badge = new Label(status);
            badge.setFont(Font.font("Segoe UI", 11));
            String bc = "Active".equals(status) ? SECONDARY + "1A" : WARNING + "1A";
            String fc = "Active".equals(status) ? SECONDARY : WARNING;
            badge.setStyle("-fx-background-color: " + bc + "; -fx-text-fill: " + fc
                    + "; -fx-background-radius: 4; -fx-padding: 2 8;");
            HBox badgeW = new HBox(badge);
            badgeW.setPadding(new Insets(10, 14, 10, 14));
            badgeW.setStyle(cellBorder);
            grid.add(badgeW, 4, r + 1);

            Label orgL = new Label(child.getOrganization() != null ? child.getOrganization() : "N/A");
            orgL.setFont(Font.font("Segoe UI", 13));
            orgL.setTextFill(Color.web(TEXT()));
            orgL.setPadding(new Insets(10, 14, 10, 14));
            orgL.setStyle(cellBorder);
            grid.add(orgL, 5, r + 1);

            // Action buttons: View, Edit, Delete
            final Child currentChild = child;
            HBox actionBtns = new HBox(6);
            actionBtns.setAlignment(Pos.CENTER_LEFT);
            actionBtns.setPadding(new Insets(6, 14, 6, 14));
            actionBtns.setStyle(cellBorder);

            Button viewBtn = new Button("View");
            viewBtn.setFont(Font.font("Segoe UI", 11));
            viewBtn.setStyle("-fx-background-color: " + PRIMARY
                    + "; -fx-text-fill: white; -fx-background-radius: 4; -fx-padding: 4 10; -fx-cursor: hand;");
            viewBtn.setOnAction(e -> {
                selectedChild = currentChild;
                root.setCenter(buildChildProfileView(currentChild));
            });

            Button editBtn = new Button("Edit");
            editBtn.setFont(Font.font("Segoe UI", 11));
            editBtn.setStyle("-fx-background-color: " + SECONDARY
                    + "; -fx-text-fill: white; -fx-background-radius: 4; -fx-padding: 4 10; -fx-cursor: hand;");
            editBtn.setOnAction(e -> {
                selectedChild = currentChild;
                root.setCenter(buildEditChildForm(currentChild));
            });

            Button deleteBtn = new Button("Delete");
            deleteBtn.setFont(Font.font("Segoe UI", 11));
            deleteBtn.setStyle("-fx-background-color: " + DESTRUCTIVE
                    + "; -fx-text-fill: white; -fx-background-radius: 4; -fx-padding: 4 10; -fx-cursor: hand;");
            deleteBtn.setOnAction(e -> {
                Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
                confirm.setTitle("Delete Child");
                confirm.setHeaderText(null);
                confirm.setContentText("Are you sure you want to delete '" + currentChild.getName() + "'?");
                confirm.showAndWait().ifPresent(bt -> {
                    if (bt == ButtonType.OK) {
                        childService.deleteChild(currentChild.getId());
                        systemLogService.save(new SystemLog("Delete",
                                "Deleted child: " + currentChild.getName(), user.getUsername(),
                                LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))));
                        showAlert("Deleted", "Child '" + currentChild.getName() + "' removed.");
                        selectedChild = null;
                        root.setCenter(buildChildrenPage());
                    }
                });
            });

            actionBtns.getChildren().addAll(viewBtn, editBtn, deleteBtn);
            grid.add(actionBtns, 6, r + 1);
        }

        tableCard.getChildren().addAll(tblHdr, grid);
        page.getChildren().addAll(topRow, stats, tableCard);
        return wrapScroll(page);
    }

    private Tab makeTab(String title, Node content) {
        Tab tab = new Tab(title);
        tab.setContent(content);
        return tab;
    }

    private HBox metaLabel(String label, String value) {
        Label l = new Label(label);
        l.setFont(Font.font("Segoe UI", 12));
        l.setTextFill(Color.web(MUTED_FG()));
        Label v = new Label(value);
        v.setFont(Font.font("Segoe UI", FontWeight.MEDIUM, 12));
        v.setTextFill(Color.web(TEXT()));
        return new HBox(4, l, v);
    }

    private Label statusBadge(String status) {
        Label badge = new Label(status);
        badge.setFont(Font.font("Segoe UI", 11));
        String bc = status.equals("Active") ? SECONDARY + "1A" : WARNING + "1A";
        String fc = status.equals("Active") ? SECONDARY : WARNING;
        badge.setStyle("-fx-background-color: " + bc + "; -fx-text-fill: " + fc
                + "; -fx-background-radius: 4; -fx-padding: 2 8;");
        return badge;
    }

    private VBox buildPersonalTab() {
        VBox box = new VBox(16);
        box.setPadding(new Insets(24));
        String childName = selectedChild != null ? selectedChild.getName() : "";
        String dob = selectedChild != null && selectedChild.getDateOfBirth() != null ? selectedChild.getDateOfBirth()
                : "";
        String gender = selectedChild != null && selectedChild.getGender() != null ? selectedChild.getGender() : "";
        String org = selectedChild != null && selectedChild.getOrganization() != null ? selectedChild.getOrganization()
                : "";
        HBox r1 = new HBox(24);
        r1.getChildren().addAll(formField("Full Name", childName), formField("Date of Birth", dob));
        HBox r2 = new HBox(24);
        r2.getChildren().addAll(formField("Gender", gender), formField("Organization", org));
        VBox addr = formFieldArea("Address", org);
        HBox r3 = new HBox(24);
        r3.getChildren().addAll(
                formField("Status",
                        selectedChild != null && selectedChild.getStatus() != null ? selectedChild.getStatus()
                                : "Active"),
                formField("Age", selectedChild != null ? String.valueOf(selectedChild.getAge()) : ""));
        box.getChildren().addAll(r1, r2, addr, r3);
        return box;
    }

    private VBox buildMedicalTab() {
        VBox box = new VBox(16);
        box.setPadding(new Insets(24));
        HBox vitals = new HBox(16);
        vitals.getChildren().addAll(vitalCard("Blood Type", "B+"), vitalCard("Height", "142 cm"),
                vitalCard("Weight", "32 kg"));

        Label medTitle = new Label("Medical History");
        medTitle.setFont(Font.font("Segoe UI", FontWeight.MEDIUM, 17));
        medTitle.setTextFill(Color.web(TEXT()));

        GridPane grid = new GridPane();
        String[] cols = { "Date", "Type", "Description", "Provider" };
        for (int i = 0; i < cols.length; i++) {
            Label h = new Label(cols[i]);
            h.setFont(Font.font("Segoe UI", FontWeight.SEMI_BOLD, 11));
            h.setPadding(new Insets(8, 12, 8, 12));
            h.setStyle("-fx-background-color: " + MUTED() + ";");
            h.setMaxWidth(Double.MAX_VALUE);
            grid.add(h, i, 0);
        }
        String[][] rows = {
                { "Jan 15, 2026", "Checkup", "Routine health exam - All normal", "Dr. Zarin Tasnim" },
                { "Dec 10, 2025", "Vaccination", "Hepatitis B booster shot", "Dhaka Medical College" },
                { "Oct 5, 2025", "Treatment", "Mild flu - Prescribed medication", "Dr. Farhana Alam" },
        };
        for (int r = 0; r < rows.length; r++) {
            for (int c = 0; c < rows[r].length; c++) {
                Label cl = new Label(rows[r][c]);
                cl.setFont(Font.font("Segoe UI", 13));
                cl.setTextFill(Color.web(TEXT()));
                cl.setPadding(new Insets(8, 12, 8, 12));
                cl.setStyle("-fx-border-color: " + BORDER() + "; -fx-border-width: 0 0 1 0;");
                grid.add(cl, c, r + 1);
            }
        }
        VBox allergies = formField("Known Allergies", "None");
        VBox ongoing = formFieldArea("Ongoing Treatment", "None");
        box.getChildren().addAll(vitals, medTitle, grid, allergies, ongoing);
        return box;
    }

    private VBox buildEducationTab() {
        VBox box = new VBox(16);
        box.setPadding(new Insets(24));
        HBox r1 = new HBox(24);
        r1.getChildren().addAll(formField("School Name", "Motijheel Ideal School"),
                formField("Current Grade", "Class 5"));
        HBox eduStats = new HBox(16);
        eduStats.getChildren().addAll(vitalCard("Attendance Rate", "95%"),
                vitalCard("Overall Performance", "Excellent"), vitalCard("Class Rank", "3rd / 40"));

        Label perfTitle = new Label("Academic Performance");
        perfTitle.setFont(Font.font("Segoe UI", FontWeight.MEDIUM, 17));
        perfTitle.setTextFill(Color.web(TEXT()));

        GridPane grid = new GridPane();
        String[] cols = { "Subject", "Term 1", "Term 2", "Term 3", "Average" };
        for (int i = 0; i < cols.length; i++) {
            Label h = new Label(cols[i]);
            h.setFont(Font.font("Segoe UI", FontWeight.SEMI_BOLD, 11));
            h.setPadding(new Insets(8, 12, 8, 12));
            h.setStyle("-fx-background-color: " + MUTED() + ";");
            h.setMaxWidth(Double.MAX_VALUE);
            grid.add(h, i, 0);
        }
        String[][] rows = {
                { "Mathematics", "88%", "92%", "90%", "90%" },
                { "Bangla", "85%", "87%", "89%", "87%" },
                { "English", "82%", "85%", "84%", "84%" },
                { "Science", "90%", "94%", "92%", "92%" },
                { "Bangladesh Studies", "82%", "85%", "84%", "84%" },
        };
        for (int r = 0; r < rows.length; r++) {
            for (int c = 0; c < rows[r].length; c++) {
                Label cl = new Label(rows[r][c]);
                cl.setFont(Font.font("Segoe UI", c == 0 ? FontWeight.MEDIUM : FontWeight.NORMAL, 13));
                if (c == 4) {
                    cl.setFont(Font.font("Segoe UI", FontWeight.SEMI_BOLD, 13));
                    cl.setTextFill(Color.web(SECONDARY));
                }
                cl.setPadding(new Insets(8, 12, 8, 12));
                cl.setStyle("-fx-border-color: " + BORDER() + "; -fx-border-width: 0 0 1 0;");
                grid.add(cl, c, r + 1);
            }
        }
        VBox notes = formFieldArea("Teacher's Notes",
                "Tahmid is a diligent student. Shows strength in math and science.");
        box.getChildren().addAll(r1, eduStats, perfTitle, grid, notes);
        return box;
    }

    private VBox buildWelfareTab() {
        VBox box = new VBox(16);
        box.setPadding(new Insets(24));
        Label caseTitle = new Label("Case History");
        caseTitle.setFont(Font.font("Segoe UI", FontWeight.MEDIUM, 17));
        caseTitle.setTextFill(Color.web(TEXT()));

        VBox entries = new VBox(12);
        String[][] cases = {
                { "Jan 20, 2026", "Financial Support", "Received donation for school supplies", "Dr. Michael Chen" },
                { "Jan 15, 2026", "Medical Care", "Completed routine health checkup", "Dr. Michael Chen" },
                { "Dec 28, 2025", "Social Visit", "Home visit conducted, family stable", "Dr. Michael Chen" },
                { "Dec 10, 2025", "Education Support", "Provided textbooks for new term", "Lisa Kamau" },
        };
        for (String[] c : cases) {
            VBox entry = new VBox(4);
            entry.setPadding(new Insets(12));
            entry.setStyle("-fx-background-color: " + CARD() + "; -fx-border-color: " + BORDER()
                    + "; -fx-border-width: 1; -fx-background-radius: 8; -fx-border-radius: 8;");
            HBox top = new HBox();
            VBox left = new VBox(2);
            Label type = new Label(c[1]);
            type.setFont(Font.font("Segoe UI", FontWeight.MEDIUM, 14));
            type.setTextFill(Color.web(TEXT()));
            Label date = new Label(c[0]);
            date.setFont(Font.font("Segoe UI", 11));
            date.setTextFill(Color.web(MUTED_FG()));
            left.getChildren().addAll(type, date);
            Region sp = new Region();
            HBox.setHgrow(sp, Priority.ALWAYS);
            Label by = new Label("By: " + c[3]);
            by.setFont(Font.font("Segoe UI", 11));
            by.setTextFill(Color.web(MUTED_FG()));
            top.getChildren().addAll(left, sp, by);
            Label desc = new Label(c[2]);
            desc.setFont(Font.font("Segoe UI", 13));
            desc.setTextFill(Color.web(MUTED_FG()));
            entry.getChildren().addAll(top, desc);
            entries.getChildren().add(entry);
        }

        VBox needs = formFieldArea("Current Needs Assessment",
                "Child is doing well. Priorities:\n- Continued education support\n- Regular medical checkups\n- Nutritional supplements");

        // Add new case entry
        VBox addBox = new VBox(12);
        addBox.setPadding(new Insets(16));
        addBox.setStyle("-fx-background-color: " + PRIMARY + "0D; -fx-border-color: " + PRIMARY
                + "33; -fx-border-width: 1; -fx-background-radius: 8; -fx-border-radius: 8;");
        Label addTitle = new Label("Add New Case Entry");
        addTitle.setFont(Font.font("Segoe UI", FontWeight.MEDIUM, 14));
        addTitle.setTextFill(Color.web(TEXT()));
        ComboBox<String> entryType = new ComboBox<>();
        entryType.getItems().addAll("Financial Support", "Medical Care", "Education Support", "Social Visit",
                "Emergency Response");
        entryType.setValue("Financial Support");
        entryType.setMaxWidth(Double.MAX_VALUE);
        TextArea entryDesc = new TextArea();
        entryDesc.setPromptText("Enter case details...");
        entryDesc.setPrefRowCount(2);
        Button addBtn = new Button("Add Entry");
        addBtn.setMaxWidth(Double.MAX_VALUE);
        addBtn.setStyle("-fx-background-color: " + PRIMARY
                + "; -fx-text-fill: white; -fx-background-radius: 4; -fx-padding: 8 0; -fx-font-size: 13px; -fx-cursor: hand;");
        addBox.getChildren().addAll(addTitle, entryType, entryDesc, addBtn);

        box.getChildren().addAll(caseTitle, entries, needs, addBox);
        return box;
    }

    // ═══════════ CHILD PROFILE DETAIL VIEW ═══════════
    private ScrollPane buildChildProfileView(Child child) {
        VBox page = new VBox(20);
        page.setPadding(new Insets(24));

        Button backBtn = new Button("\u2190 Back to Children");
        backBtn.setStyle("-fx-background-color: transparent; -fx-text-fill: " + PRIMARY + "; -fx-cursor: hand;");
        backBtn.setOnAction(e -> root.setCenter(buildChildrenPage()));

        // Profile header
        VBox profileHeader = new VBox(8);
        profileHeader.setPadding(new Insets(24));
        profileHeader.setStyle("-fx-background-color: " + CARD() + "; -fx-border-color: " + BORDER()
                + "; -fx-border-width: 1; -fx-background-radius: 8; -fx-border-radius: 8;");

        HBox phRow = new HBox(16);
        phRow.setAlignment(Pos.CENTER_LEFT);
        StackPane avatar = new StackPane();
        avatar.setPrefSize(80, 80);
        avatar.setStyle("-fx-background-color: " + PRIMARY + "1A; -fx-background-radius: 40;");
        String initStr = child.getName().length() >= 2
                ? child.getName().substring(0, 2).toUpperCase()
                : child.getName().toUpperCase();
        Label initials = new Label(initStr);
        initials.setFont(Font.font("Segoe UI", FontWeight.SEMI_BOLD, 24));
        initials.setTextFill(Color.web(PRIMARY));
        avatar.getChildren().add(initials);

        VBox info = new VBox(4);
        HBox.setHgrow(info, Priority.ALWAYS);
        Label name = new Label(child.getName());
        name.setFont(Font.font("Segoe UI", FontWeight.SEMI_BOLD, 18));
        name.setTextFill(Color.web(TEXT()));
        Label cid = new Label("Child ID: CH-" + String.format("%04d", child.getId()));
        cid.setFont(Font.font("Segoe UI", 12));
        cid.setTextFill(Color.web(MUTED_FG()));
        HBox meta = new HBox(16);
        meta.getChildren().addAll(
                metaLabel("Age:", child.getAge() + " years"),
                metaLabel("Organization:", child.getOrganization() != null ? child.getOrganization() : "N/A"),
                statusBadge(child.getStatus() != null ? child.getStatus() : "Active"));
        info.getChildren().addAll(name, cid, meta);

        HBox actionBtns = new HBox(8);
        Button editBtn = new Button("\u270F\uFE0F Edit");
        editBtn.setStyle("-fx-background-color: " + SECONDARY
                + "; -fx-text-fill: white; -fx-background-radius: 4; -fx-padding: 8 16; -fx-font-size: 13px; -fx-cursor: hand;");
        editBtn.setOnAction(e -> root.setCenter(buildEditChildForm(child)));

        Button deleteBtn = new Button("\u274C Delete");
        deleteBtn.setStyle("-fx-background-color: " + DESTRUCTIVE
                + "; -fx-text-fill: white; -fx-background-radius: 4; -fx-padding: 8 16; -fx-font-size: 13px; -fx-cursor: hand;");
        deleteBtn.setOnAction(e -> {
            Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
            confirm.setContentText("Are you sure you want to delete '" + child.getName() + "'?");
            confirm.showAndWait().ifPresent(bt -> {
                if (bt == ButtonType.OK) {
                    childService.deleteChild(child.getId());
                    systemLogService.save(new SystemLog("Delete",
                            "Deleted child: " + child.getName(), user.getUsername(),
                            LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))));
                    selectedChild = null;
                    root.setCenter(buildChildrenPage());
                }
            });
        });
        actionBtns.getChildren().addAll(editBtn, deleteBtn);

        phRow.getChildren().addAll(avatar, info, actionBtns);
        profileHeader.getChildren().add(phRow);

        // Tab pane with child details
        selectedChild = child;
        TabPane tabs = new TabPane();
        tabs.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);
        tabs.setStyle("-fx-background-color: " + CARD() + ";");
        tabs.getTabs().addAll(
                makeTab("Personal Information", buildPersonalTab()),
                makeTab("Medical Records", buildMedicalTab()),
                makeTab("Education Records", buildEducationTab()),
                makeTab("Welfare & Case History", buildWelfareTab()));

        page.getChildren().addAll(backBtn, profileHeader, tabs);
        return wrapScroll(page);
    }

    // ═══════════ EDIT CHILD FORM ═══════════
    private ScrollPane buildEditChildForm(Child child) {
        VBox page = new VBox(20);
        page.setPadding(new Insets(24));

        Button backBtn = new Button("\u2190 Back to Children");
        backBtn.setStyle("-fx-background-color: transparent; -fx-text-fill: " + PRIMARY + "; -fx-cursor: hand;");
        backBtn.setOnAction(e -> root.setCenter(buildChildrenPage()));

        Label title = new Label("Edit Child Profile: " + child.getName());
        title.setFont(Font.font("Segoe UI", FontWeight.BOLD, 22));
        title.setTextFill(Color.web(TEXT()));

        VBox card = new VBox(16);
        card.setPadding(new Insets(24));
        card.setStyle("-fx-background-color: " + CARD() + "; -fx-border-color: " + BORDER()
                + "; -fx-border-width: 1; -fx-background-radius: 8; -fx-border-radius: 8;");

        VBox nameField = formField("Full Name", child.getName());
        VBox ageField = formField("Age", String.valueOf(child.getAge()));
        VBox genderField = formField("Gender", child.getGender() != null ? child.getGender() : "");
        VBox dobField = formField("Date of Birth", child.getDateOfBirth() != null ? child.getDateOfBirth() : "");
        VBox orgField = formField("Organization", child.getOrganization() != null ? child.getOrganization() : "");
        VBox statusField = formField("Status", child.getStatus() != null ? child.getStatus() : "Active");

        Button saveBtn = new Button("\uD83D\uDCBE  Save Changes");
        saveBtn.setStyle("-fx-background-color: " + PRIMARY
                + "; -fx-text-fill: white; -fx-background-radius: 4; -fx-padding: 10 24; -fx-font-weight: bold; -fx-cursor: hand;");
        saveBtn.setOnAction(e -> {
            child.setName(((TextField) nameField.getChildren().get(1)).getText().trim());
            try {
                child.setAge(Integer.parseInt(((TextField) ageField.getChildren().get(1)).getText().trim()));
            } catch (NumberFormatException ex) {
                /* keep old value */ }
            child.setGender(((TextField) genderField.getChildren().get(1)).getText().trim());
            child.setDateOfBirth(((TextField) dobField.getChildren().get(1)).getText().trim());
            child.setOrganization(((TextField) orgField.getChildren().get(1)).getText().trim());
            child.setStatus(((TextField) statusField.getChildren().get(1)).getText().trim());
            childService.updateChild(child);
            systemLogService.save(new SystemLog("Data Update",
                    "Updated child profile: " + child.getName(), user.getUsername(),
                    LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))));
            showAlert("Success", "Child profile updated successfully.");
            selectedChild = child;
            root.setCenter(buildChildrenPage());
        });

        card.getChildren().addAll(nameField, ageField, genderField, dobField, orgField, statusField,
                new Separator(), saveBtn);
        page.getChildren().addAll(backBtn, title, card);
        return wrapScroll(page);
    }

    // ═══════════ ADD CHILD DIALOG ═══════════
    private void showAddChildDialog() {
        VBox page = new VBox(20);
        page.setPadding(new Insets(24));

        Button backBtn = new Button("\u2190 Back to Children");
        backBtn.setStyle("-fx-background-color: transparent; -fx-text-fill: " + PRIMARY + "; -fx-cursor: hand;");
        backBtn.setOnAction(e -> root.setCenter(buildChildrenPage()));

        Label title = new Label("Add New Child Profile");
        title.setFont(Font.font("Segoe UI", FontWeight.BOLD, 22));
        title.setTextFill(Color.web(TEXT()));

        VBox card = new VBox(16);
        card.setPadding(new Insets(24));
        card.setStyle("-fx-background-color: " + CARD() + "; -fx-border-color: " + BORDER()
                + "; -fx-border-width: 1; -fx-background-radius: 8; -fx-border-radius: 8;");

        VBox nameField = formField("Full Name", "");
        VBox ageField = formField("Age", "");
        VBox genderField = formField("Gender", "");
        VBox dobField = formField("Date of Birth", "");
        VBox orgField = formField("Organization", user.getOrganization() != null ? user.getOrganization() : "");
        VBox statusField = formField("Status", "Active");

        Button saveBtn = new Button("Save New Child");
        saveBtn.setStyle("-fx-background-color: " + PRIMARY
                + "; -fx-text-fill: white; -fx-background-radius: 4; -fx-padding: 10 24; -fx-font-weight: bold; -fx-cursor: hand;");
        saveBtn.setOnAction(e -> {
            String childName = ((TextField) nameField.getChildren().get(1)).getText().trim();
            if (childName.isEmpty()) {
                showAlert("Warning", "Child name is required.");
                return;
            }
            Child newChild = new Child();
            newChild.setName(childName);
            try {
                newChild.setAge(Integer.parseInt(((TextField) ageField.getChildren().get(1)).getText().trim()));
            } catch (NumberFormatException ex) {
                newChild.setAge(0);
            }
            newChild.setGender(((TextField) genderField.getChildren().get(1)).getText().trim());
            newChild.setDateOfBirth(((TextField) dobField.getChildren().get(1)).getText().trim());
            newChild.setOrganization(((TextField) orgField.getChildren().get(1)).getText().trim());
            newChild.setStatus(((TextField) statusField.getChildren().get(1)).getText().trim());
            childService.addChild(newChild);
            systemLogService.save(new SystemLog("Data Update",
                    "Added new child: " + childName, user.getUsername(),
                    LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))));
            showAlert("Success", "Child '" + childName + "' added successfully.");
            root.setCenter(buildChildrenPage());
        });

        card.getChildren().addAll(nameField, ageField, genderField, dobField, orgField, statusField,
                new Separator(), saveBtn);
        page.getChildren().addAll(backBtn, title, card);
        root.setCenter(wrapScroll(page));
    }

    // ═══════════ SPONSORSHIP PAGE ═══════════
    private ScrollPane buildSponsorshipPage() {
        VBox page = new VBox(20);
        page.setPadding(new Insets(24));

        Label title = new Label("Sponsorship & Digital Wallet Management");
        title.setFont(Font.font("Segoe UI", FontWeight.MEDIUM, 20));
        title.setTextFill(Color.web(TEXT()));
        Label sub = new Label("Track donations and fund utilization");
        sub.setFont(Font.font("Segoe UI", 13));
        sub.setTextFill(Color.web(MUTED_FG()));

        List<Donation> allDonations = donationService.getAll();
        double totalReceived = allDonations.stream().mapToDouble(Donation::getAmount).sum();
        String totalRecvStr = String.format("\u09F3%,.0f", totalReceived);
        HBox stats = new HBox(16);
        stats.getChildren().addAll(
                statCard("Total Received", totalRecvStr, "All time", SECONDARY),
                statCard("Total Donations", String.valueOf(allDonations.size()), "Records", MUTED_FG()),
                statCard("Children Covered", String.valueOf(childService.getAllChildren().size()), "All time",
                        MUTED_FG()),
                statCard("System Logs", String.valueOf(systemLogService.getCount()), "Entries", MUTED_FG()));

        // Fund Utilization
        VBox fundCard = new VBox(16);
        fundCard.setPadding(new Insets(16));
        fundCard.setStyle("-fx-background-color: " + CARD() + "; -fx-border-color: " + BORDER()
                + "; -fx-border-width: 1; -fx-background-radius: 8; -fx-border-radius: 8;");
        Label fundTitle = new Label("Fund Utilization by Category");
        fundTitle.setFont(Font.font("Segoe UI", FontWeight.MEDIUM, 17));
        fundTitle.setTextFill(Color.web(TEXT()));
        fundCard.getChildren().add(fundTitle);

        // Dynamic fund utilization based on actual total
        double edu = totalReceived * 0.50;
        double med = totalReceived * 0.25;
        double food = totalReceived * 0.15;
        double cloth = totalReceived * 0.075;
        double other = totalReceived * 0.025;
        String[][] funds = {
                { "Education", String.format("\u09F3%,.0f", edu), "50", "#2563eb" },
                { "Medical Care", String.format("\u09F3%,.0f", med), "25", "#16a34a" },
                { "Food & Nutrition", String.format("\u09F3%,.0f", food), "15", "#f59e0b" },
                { "Clothing", String.format("\u09F3%,.0f", cloth), "7.5", "#8b5cf6" },
                { "Other", String.format("\u09F3%,.0f", other), "2.5", "#ec4899" },
        };
        for (String[] f : funds) {
            VBox row = new VBox(4);
            HBox labels = new HBox();
            Label cat = new Label(f[0]);
            cat.setFont(Font.font("Segoe UI", 13));
            cat.setTextFill(Color.web(TEXT()));
            Region sp = new Region();
            HBox.setHgrow(sp, Priority.ALWAYS);
            Label amt = new Label(f[1] + " (" + f[2] + "%)");
            amt.setFont(Font.font("Segoe UI", FontWeight.SEMI_BOLD, 13));
            amt.setTextFill(Color.web(TEXT()));
            labels.getChildren().addAll(cat, sp, amt);

            StackPane barBg = new StackPane();
            barBg.setPrefHeight(8);
            barBg.setMaxHeight(8);
            barBg.setStyle("-fx-background-color: " + MUTED() + "; -fx-background-radius: 4;");
            StackPane barFill = new StackPane();
            barFill.setPrefHeight(8);
            barFill.setMaxHeight(8);
            barFill.setMaxWidth(Double.parseDouble(f[2]) * 4);
            barFill.setStyle("-fx-background-color: " + f[3] + "; -fx-background-radius: 4;");
            StackPane bar = new StackPane(barBg, barFill);
            bar.setAlignment(Pos.CENTER_LEFT);

            row.getChildren().addAll(labels, bar);
            fundCard.getChildren().add(row);
        }

        // Transaction History table
        VBox txHistCard = new VBox(0);
        txHistCard.setStyle("-fx-background-color: " + CARD() + "; -fx-border-color: " + BORDER()
                + "; -fx-border-width: 1; -fx-background-radius: 8; -fx-border-radius: 8;");
        HBox txHdr = new HBox();
        txHdr.setPadding(new Insets(16));
        txHdr.setStyle("-fx-border-color: " + BORDER() + "; -fx-border-width: 0 0 1 0;");
        Label txTitle = new Label("Transaction History");
        txTitle.setFont(Font.font("Segoe UI", FontWeight.MEDIUM, 17));
        txTitle.setTextFill(Color.web(TEXT()));
        txHdr.getChildren().add(txTitle);

        GridPane txGrid = new GridPane();
        String[] txCols = { "Date", "Donor ID", "Child ID", "Amount", "Purpose", "Status" };
        for (int i = 0; i < txCols.length; i++) {
            Label h = new Label(txCols[i]);
            h.setFont(Font.font("Segoe UI", FontWeight.SEMI_BOLD, 11));
            h.setPadding(new Insets(8, 16, 8, 16));
            h.setMaxWidth(Double.MAX_VALUE);
            h.setStyle("-fx-background-color: " + MUTED() + ";");
            h.setTextFill(Color.web(TEXT()));
            txGrid.add(h, i, 0);
        }
        int txRow = 1;
        for (Donation don : allDonations) {
            Label dateL = new Label(don.getDate() != null ? don.getDate() : "");
            dateL.setFont(Font.font("Segoe UI", 12));
            dateL.setTextFill(Color.web(TEXT()));
            dateL.setPadding(new Insets(8, 16, 8, 16));
            dateL.setStyle("-fx-border-color: " + BORDER() + "; -fx-border-width: 0 0 1 0;");

            Label donorL = new Label(String.valueOf(don.getDonorId()));
            donorL.setFont(Font.font("Segoe UI", 12));
            donorL.setTextFill(Color.web(TEXT()));
            donorL.setPadding(new Insets(8, 16, 8, 16));
            donorL.setStyle("-fx-border-color: " + BORDER() + "; -fx-border-width: 0 0 1 0;");

            Label childL = new Label("CH-" + String.format("%04d", don.getChildId()));
            childL.setFont(Font.font("Consolas", 12));
            childL.setTextFill(Color.web(TEXT()));
            childL.setPadding(new Insets(8, 16, 8, 16));
            childL.setStyle("-fx-border-color: " + BORDER() + "; -fx-border-width: 0 0 1 0;");

            Label amtL = new Label(String.format("\u09F3%,.0f", don.getAmount()));
            amtL.setFont(Font.font("Segoe UI", FontWeight.SEMI_BOLD, 12));
            amtL.setTextFill(Color.web(SECONDARY));
            amtL.setPadding(new Insets(8, 16, 8, 16));
            amtL.setStyle("-fx-border-color: " + BORDER() + "; -fx-border-width: 0 0 1 0;");

            Label purL = new Label(don.getPurpose() != null ? don.getPurpose() : "");
            purL.setFont(Font.font("Segoe UI", 12));
            purL.setTextFill(Color.web(TEXT()));
            purL.setPadding(new Insets(8, 16, 8, 16));
            purL.setStyle("-fx-border-color: " + BORDER() + "; -fx-border-width: 0 0 1 0;");

            Label stL = new Label(don.getStatus() != null ? don.getStatus() : "");
            stL.setFont(Font.font("Segoe UI", 11));
            String sc = "Completed".equals(don.getStatus()) ? SECONDARY : WARNING;
            stL.setStyle("-fx-background-color: " + sc + "1A; -fx-text-fill: " + sc
                    + "; -fx-background-radius: 4; -fx-padding: 2 8;");
            HBox stW = new HBox(stL);
            stW.setPadding(new Insets(8, 16, 8, 16));
            stW.setStyle("-fx-border-color: " + BORDER() + "; -fx-border-width: 0 0 1 0;");

            txGrid.add(dateL, 0, txRow);
            txGrid.add(donorL, 1, txRow);
            txGrid.add(childL, 2, txRow);
            txGrid.add(amtL, 3, txRow);
            txGrid.add(purL, 4, txRow);
            txGrid.add(stW, 5, txRow);
            txRow++;
        }
        if (allDonations.isEmpty()) {
            Label noTx = new Label("No transactions yet.");
            noTx.setTextFill(Color.web(MUTED_FG()));
            noTx.setPadding(new Insets(16));
            txGrid.add(noTx, 0, 1);
            GridPane.setColumnSpan(noTx, 6);
        }
        txHistCard.getChildren().addAll(txHdr, txGrid);

        // Action buttons
        HBox actions = new HBox(12);
        Button recDon = new Button("\uD83D\uDCB2  Record Donation");
        recDon.setStyle("-fx-background-color: " + SECONDARY
                + "; -fx-text-fill: white; -fx-background-radius: 4; -fx-padding: 8 16; -fx-font-size: 13px; -fx-cursor: hand;");
        recDon.setOnAction(e -> root.setCenter(buildRecordDonationPage()));

        Button recExp = new Button("\uD83D\uDCC9  Record Expense");
        recExp.setStyle("-fx-background-color: " + PRIMARY
                + "; -fx-text-fill: white; -fx-background-radius: 4; -fx-padding: 8 16; -fx-font-size: 13px; -fx-cursor: hand;");
        recExp.setOnAction(e -> root.setCenter(buildRecordExpensePage()));
        actions.getChildren().addAll(recDon, recExp);

        page.getChildren().addAll(new VBox(4, title, sub), stats, fundCard, actions, txHistCard);

        // ── Expense History table ──
        VBox expHistCard = new VBox(0);
        expHistCard.setStyle("-fx-background-color: " + CARD() + "; -fx-border-color: " + BORDER()
                + "; -fx-border-width: 1; -fx-background-radius: 8; -fx-border-radius: 8;");
        HBox expHdr = new HBox();
        expHdr.setPadding(new Insets(16));
        expHdr.setStyle("-fx-border-color: " + BORDER() + "; -fx-border-width: 0 0 1 0;");
        Label expTitle = new Label("Expense History");
        expTitle.setFont(Font.font("Segoe UI", FontWeight.MEDIUM, 17));
        expTitle.setTextFill(Color.web(TEXT()));
        expHdr.getChildren().add(expTitle);

        GridPane expGrid = new GridPane();
        String[] expCols = { "Date", "Child ID", "Category", "Amount", "Description" };
        for (int i = 0; i < expCols.length; i++) {
            Label h = new Label(expCols[i]);
            h.setFont(Font.font("Segoe UI", FontWeight.SEMI_BOLD, 11));
            h.setPadding(new Insets(8, 16, 8, 16));
            h.setMaxWidth(Double.MAX_VALUE);
            h.setStyle("-fx-background-color: " + MUTED() + ";");
            h.setTextFill(Color.web(TEXT()));
            expGrid.add(h, i, 0);
        }

        // Load all expenses from each child
        List<Expense> allExpenses = new java.util.ArrayList<>();
        for (Child ch : childService.getAllChildren()) {
            allExpenses.addAll(expenseService.getByChildId(ch.getId()));
        }
        allExpenses.sort((a, b) -> {
            if (a.getDate() == null)
                return 1;
            if (b.getDate() == null)
                return -1;
            return b.getDate().compareTo(a.getDate());
        });

        int expRow = 1;
        for (Expense exp : allExpenses) {
            String cellBorder = "-fx-border-color: " + BORDER() + "; -fx-border-width: 0 0 1 0;";
            Label eDateL = new Label(exp.getDate() != null ? exp.getDate() : "");
            eDateL.setFont(Font.font("Segoe UI", 12));
            eDateL.setTextFill(Color.web(TEXT()));
            eDateL.setPadding(new Insets(8, 16, 8, 16));
            eDateL.setStyle(cellBorder);

            Label eChildL = new Label("CH-" + String.format("%04d", exp.getChildId()));
            eChildL.setFont(Font.font("Consolas", 12));
            eChildL.setTextFill(Color.web(MUTED_FG()));
            eChildL.setPadding(new Insets(8, 16, 8, 16));
            eChildL.setStyle(cellBorder);

            Label eCatL = new Label(exp.getCategory() != null ? exp.getCategory() : "");
            eCatL.setFont(Font.font("Segoe UI", 12));
            eCatL.setTextFill(Color.web(TEXT()));
            eCatL.setPadding(new Insets(8, 16, 8, 16));
            eCatL.setStyle(cellBorder);

            Label eAmtL = new Label(String.format("\u09F3%,.0f", exp.getAmount()));
            eAmtL.setFont(Font.font("Segoe UI", FontWeight.SEMI_BOLD, 12));
            eAmtL.setTextFill(Color.web(DESTRUCTIVE));
            eAmtL.setPadding(new Insets(8, 16, 8, 16));
            eAmtL.setStyle(cellBorder);

            Label eDescL = new Label(exp.getDescription() != null ? exp.getDescription() : "");
            eDescL.setFont(Font.font("Segoe UI", 12));
            eDescL.setTextFill(Color.web(MUTED_FG()));
            eDescL.setPadding(new Insets(8, 16, 8, 16));
            eDescL.setStyle(cellBorder);

            expGrid.add(eDateL, 0, expRow);
            expGrid.add(eChildL, 1, expRow);
            expGrid.add(eCatL, 2, expRow);
            expGrid.add(eAmtL, 3, expRow);
            expGrid.add(eDescL, 4, expRow);
            expRow++;
        }
        if (allExpenses.isEmpty()) {
            Label noExp = new Label("No expenses recorded yet.");
            noExp.setTextFill(Color.web(MUTED_FG()));
            noExp.setPadding(new Insets(16));
            expGrid.add(noExp, 0, 1);
            GridPane.setColumnSpan(noExp, 5);
        }
        expHistCard.getChildren().addAll(expHdr, expGrid);
        page.getChildren().add(expHistCard);

        return wrapScroll(page);
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

        HBox stats = new HBox(16);
        stats.getChildren().addAll(
                statCard("Total Active", "5", "Requires attention", WARNING),
                statCard("Critical", "2", "Immediate action needed", DESTRUCTIVE),
                statCard("Warnings", "2", "Review soon", WARNING),
                statCard("Resolved Today", "2", "Completed", SECONDARY));

        Label activeTitle = new Label("Active Alerts");
        activeTitle.setFont(Font.font("Segoe UI", FontWeight.MEDIUM, 17));
        activeTitle.setTextFill(Color.web(TEXT()));

        VBox alertsList = new VBox(12);
        String[][] alerts = {
                { "critical", "Low Wallet Balance", "ALT-001", "2 hours ago",
                        "Child CH-1024 has wallet balance below threshold" },
                { "warning", "Missed Medical Appointment", "ALT-003", "1 day ago",
                        "Child CH-1024 missed scheduled checkup" },
                { "info", "New Donor Registration", "ALT-005", "2 days ago", "New donor pending verification" },
        };
        for (String[] a : alerts) {
            String borderC = a[0].equals("critical") ? DESTRUCTIVE : BORDER();
            String bgC = a[0].equals("critical") ? DESTRUCTIVE + "1A"
                    : a[0].equals("warning") ? WARNING + "1A" : PRIMARY + "1A";
            String iconC = a[0].equals("critical") ? DESTRUCTIVE : a[0].equals("warning") ? WARNING : PRIMARY;

            VBox card = new VBox(8);
            card.setPadding(new Insets(16));
            card.setStyle("-fx-background-color: " + CARD() + "; -fx-border-color: " + borderC
                    + "; -fx-border-width: 1; -fx-background-radius: 8; -fx-border-radius: 8;");

            HBox top = new HBox(12);
            StackPane iconBox = new StackPane();
            iconBox.setPrefSize(40, 40);
            iconBox.setStyle("-fx-background-color: " + bgC + "; -fx-background-radius: 4;");
            Label icon = new Label("\u26A0");
            icon.setTextFill(Color.web(iconC));
            iconBox.getChildren().add(icon);

            VBox info2 = new VBox(4);
            HBox.setHgrow(info2, Priority.ALWAYS);
            Label aTitle = new Label(a[1]);
            aTitle.setFont(Font.font("Segoe UI", FontWeight.MEDIUM, 14));
            aTitle.setTextFill(Color.web(TEXT()));
            Label aDesc = new Label(a[4]);
            aDesc.setFont(Font.font("Segoe UI", 13));
            aDesc.setTextFill(Color.web(MUTED_FG()));
            HBox btns = new HBox(8);
            Button vd = new Button("View Details");
            vd.setStyle("-fx-background-color: " + PRIMARY
                    + "; -fx-text-fill: white; -fx-background-radius: 4; -fx-padding: 6 12; -fx-font-size: 12px; -fx-cursor: hand;");
            final String[] alertData = a;
            vd.setOnAction(e -> root.setCenter(buildAlertDetailView(alertData)));

            Button mr = new Button("Mark Resolved");
            mr.setStyle("-fx-background-color: " + SECONDARY
                    + "; -fx-text-fill: white; -fx-background-radius: 4; -fx-padding: 6 12; -fx-font-size: 12px; -fx-cursor: hand;");
            btns.getChildren().addAll(vd, mr);
            info2.getChildren().addAll(aTitle, aDesc, btns);

            Label time = new Label(a[3]);
            time.setFont(Font.font("Segoe UI", 11));
            time.setTextFill(Color.web(MUTED_FG()));
            top.getChildren().addAll(iconBox, info2, time);
            card.getChildren().add(top);
            alertsList.getChildren().add(card);
        }

        page.getChildren().addAll(new VBox(4, title, sub), stats, activeTitle, alertsList);
        return wrapScroll(page);
    }

    // ═══════════ REPORTS PAGE ═══════════
    private ScrollPane buildReportsPage() {
        VBox page = new VBox(20);
        page.setPadding(new Insets(24));

        Label title = new Label("Reports & Audit Logs");
        title.setFont(Font.font("Segoe UI", FontWeight.MEDIUM, 20));
        title.setTextFill(Color.web(TEXT()));
        Label sub = new Label("Generate welfare reports and view audit trails");
        sub.setFont(Font.font("Segoe UI", 13));
        sub.setTextFill(Color.web(MUTED_FG()));

        // ── Report Generator Card ──
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
        rtCombo.getItems().addAll("Child Welfare Summary", "Donation & Financial Report",
                "System Audit Log");
        rtCombo.setValue("Child Welfare Summary");
        rtCombo.setMaxWidth(Double.MAX_VALUE);
        col1.getChildren().addAll(rtLabel, rtCombo);
        HBox.setHgrow(col1, Priority.ALWAYS);

        VBox col2 = new VBox(8);
        Label drLabel = new Label("Date Range");
        drLabel.setFont(Font.font("Segoe UI", FontWeight.MEDIUM, 13));
        drLabel.setTextFill(Color.web(TEXT()));
        ComboBox<String> drCombo = new ComboBox<>();
        drCombo.getItems().addAll("Last 7 Days", "Last 30 Days", "Last 3 Months", "All Time");
        drCombo.setValue("Last 30 Days");
        drCombo.setMaxWidth(Double.MAX_VALUE);
        col2.getChildren().addAll(drLabel, drCombo);
        HBox.setHgrow(col2, Priority.ALWAYS);
        row1.getChildren().addAll(col1, col2);

        // Report preview area
        VBox reportPreview = new VBox(12);
        reportPreview.setPadding(new Insets(16));
        reportPreview.setStyle("-fx-background-color: " + MUTED() + "; -fx-background-radius: 8;");
        Label previewPlaceholder = new Label("Click 'Generate Report' to view report data here.");
        previewPlaceholder.setTextFill(Color.web(MUTED_FG()));
        previewPlaceholder.setFont(Font.font("Segoe UI", 13));
        reportPreview.getChildren().add(previewPlaceholder);

        HBox buttons = new HBox(12);
        Button genBtn = new Button("Generate Report");
        genBtn.setStyle("-fx-background-color: " + PRIMARY
                + "; -fx-text-fill: white; -fx-background-radius: 4; -fx-padding: 8 16; -fx-font-size: 13px; -fx-cursor: hand;");
        genBtn.setOnAction(e -> {
            reportPreview.getChildren().clear();
            String selectedType = rtCombo.getValue();
            systemLogService.save(new SystemLog("Report", "Generated " + selectedType, user.getUsername(),
                    LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))));

            if ("Child Welfare Summary".equals(selectedType)) {
                List<Child> children = childService.getAllChildren();
                Label rpTitle = new Label("Child Welfare Summary — " + drCombo.getValue());
                rpTitle.setFont(Font.font("Segoe UI", FontWeight.SEMI_BOLD, 15));
                rpTitle.setTextFill(Color.web(TEXT()));
                GridPane rg = new GridPane();
                String[] rcols = { "Child ID", "Name", "Age", "Gender", "Status", "Organization" };
                for (int i = 0; i < rcols.length; i++) {
                    Label h = new Label(rcols[i]);
                    h.setFont(Font.font("Segoe UI", FontWeight.SEMI_BOLD, 11));
                    h.setPadding(new Insets(6, 12, 6, 12));
                    h.setStyle("-fx-background-color: " + CARD() + ";");
                    h.setTextFill(Color.web(TEXT()));
                    rg.add(h, i, 0);
                }
                int rr = 1;
                for (Child c : children) {
                    rg.add(cellLabel("CH-" + String.format("%04d", c.getId())), 0, rr);
                    rg.add(cellLabel(c.getName()), 1, rr);
                    rg.add(cellLabel(String.valueOf(c.getAge())), 2, rr);
                    rg.add(cellLabel(c.getGender() != null ? c.getGender() : "N/A"), 3, rr);
                    rg.add(cellLabel(c.getStatus() != null ? c.getStatus() : "Active"), 4, rr);
                    rg.add(cellLabel(c.getOrganization() != null ? c.getOrganization() : "N/A"), 5, rr);
                    rr++;
                }
                Label summary = new Label("Total Children: " + children.size());
                summary.setFont(Font.font("Segoe UI", FontWeight.MEDIUM, 13));
                summary.setTextFill(Color.web(SECONDARY));
                reportPreview.getChildren().addAll(rpTitle, rg, summary);
            } else if ("Donation & Financial Report".equals(selectedType)) {
                List<Donation> donations = donationService.getAll();
                double totalD = donations.stream().mapToDouble(Donation::getAmount).sum();
                Label rpTitle = new Label("Donation & Financial Report — " + drCombo.getValue());
                rpTitle.setFont(Font.font("Segoe UI", FontWeight.SEMI_BOLD, 15));
                rpTitle.setTextFill(Color.web(TEXT()));
                GridPane rg = new GridPane();
                String[] rcols = { "Date", "Donor ID", "Child ID", "Amount", "Purpose", "Status" };
                for (int i = 0; i < rcols.length; i++) {
                    Label h = new Label(rcols[i]);
                    h.setFont(Font.font("Segoe UI", FontWeight.SEMI_BOLD, 11));
                    h.setPadding(new Insets(6, 12, 6, 12));
                    h.setStyle("-fx-background-color: " + CARD() + ";");
                    h.setTextFill(Color.web(TEXT()));
                    rg.add(h, i, 0);
                }
                int rr = 1;
                for (Donation d : donations) {
                    rg.add(cellLabel(d.getDate() != null ? d.getDate() : ""), 0, rr);
                    rg.add(cellLabel(String.valueOf(d.getDonorId())), 1, rr);
                    rg.add(cellLabel("CH-" + String.format("%04d", d.getChildId())), 2, rr);
                    rg.add(cellLabel(String.format("\u09F3%,.0f", d.getAmount())), 3, rr);
                    rg.add(cellLabel(d.getPurpose() != null ? d.getPurpose() : ""), 4, rr);
                    rg.add(cellLabel(d.getStatus() != null ? d.getStatus() : ""), 5, rr);
                    rr++;
                }
                Label summary = new Label("Total Donations: " + donations.size()
                        + " | Total Amount: " + String.format("\u09F3%,.0f", totalD));
                summary.setFont(Font.font("Segoe UI", FontWeight.MEDIUM, 13));
                summary.setTextFill(Color.web(SECONDARY));
                reportPreview.getChildren().addAll(rpTitle, rg, summary);
            } else {
                // System Audit Log
                List<SystemLog> logs = systemLogService.getAll();
                Label rpTitle = new Label("System Audit Log — " + drCombo.getValue());
                rpTitle.setFont(Font.font("Segoe UI", FontWeight.SEMI_BOLD, 15));
                rpTitle.setTextFill(Color.web(TEXT()));
                GridPane rg = new GridPane();
                String[] rcols = { "Timestamp", "Action", "Details", "User" };
                for (int i = 0; i < rcols.length; i++) {
                    Label h = new Label(rcols[i]);
                    h.setFont(Font.font("Segoe UI", FontWeight.SEMI_BOLD, 11));
                    h.setPadding(new Insets(6, 12, 6, 12));
                    h.setStyle("-fx-background-color: " + CARD() + ";");
                    h.setTextFill(Color.web(TEXT()));
                    rg.add(h, i, 0);
                }
                int rr = 1;
                for (SystemLog log : logs) {
                    rg.add(cellLabel(log.getTimestamp() != null ? log.getTimestamp() : ""), 0, rr);
                    rg.add(cellLabel(log.getEventType() != null ? log.getEventType() : ""), 1, rr);
                    rg.add(cellLabel(log.getDescription() != null ? log.getDescription() : ""), 2, rr);
                    rg.add(cellLabel(log.getActor() != null ? log.getActor() : ""), 3, rr);
                    rr++;
                }
                Label summary = new Label("Total Events: " + logs.size());
                summary.setFont(Font.font("Segoe UI", FontWeight.MEDIUM, 13));
                summary.setTextFill(Color.web(SECONDARY));
                reportPreview.getChildren().addAll(rpTitle, rg, summary);
            }
        });

        Button csvBtn = new Button("Export to CSV");
        csvBtn.setStyle("-fx-background-color: " + SECONDARY
                + "; -fx-text-fill: white; -fx-background-radius: 4; -fx-padding: 8 16; -fx-font-size: 13px; -fx-cursor: hand;");
        csvBtn.setOnAction(e -> {
            FileChooser fc = new FileChooser();
            fc.setTitle("Export Report to CSV");
            fc.setInitialFileName(rtCombo.getValue().replace(" ", "_") + ".csv");
            fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("CSV Files", "*.csv"));
            File file = fc.showSaveDialog(stage);
            if (file != null) {
                try (FileWriter fw = new FileWriter(file)) {
                    String selectedType = rtCombo.getValue();
                    if ("Child Welfare Summary".equals(selectedType)) {
                        fw.write("Child ID,Name,Age,Gender,Status,Organization\n");
                        for (Child c : childService.getAllChildren()) {
                            fw.write(String.format("%d,%s,%d,%s,%s,%s\n",
                                    c.getId(), c.getName(), c.getAge(),
                                    c.getGender() != null ? c.getGender() : "",
                                    c.getStatus() != null ? c.getStatus() : "Active",
                                    c.getOrganization() != null ? c.getOrganization() : ""));
                        }
                    } else if ("Donation & Financial Report".equals(selectedType)) {
                        fw.write("Date,Donor ID,Child ID,Amount,Purpose,Status\n");
                        for (Donation d : donationService.getAll()) {
                            fw.write(String.format("%s,%d,%d,%.0f,%s,%s\n",
                                    d.getDate() != null ? d.getDate() : "",
                                    d.getDonorId(), d.getChildId(), d.getAmount(),
                                    d.getPurpose() != null ? d.getPurpose() : "",
                                    d.getStatus() != null ? d.getStatus() : ""));
                        }
                    } else {
                        fw.write("Timestamp,Action,Details,User\n");
                        for (SystemLog log : systemLogService.getAll()) {
                            fw.write(String.format("%s,%s,%s,%s\n",
                                    log.getTimestamp() != null ? log.getTimestamp() : "",
                                    log.getEventType() != null ? log.getEventType() : "",
                                    log.getDescription() != null ? log.getDescription() : "",
                                    log.getActor() != null ? log.getActor() : ""));
                        }
                    }
                    systemLogService.save(new SystemLog("Export",
                            "Exported " + selectedType + " to CSV", user.getUsername(),
                            LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))));
                    showAlert("Export Complete", "Report saved to: " + file.getName());
                } catch (IOException ex) {
                    showAlert("Error", "Failed to export: " + ex.getMessage());
                }
            }
        });
        buttons.getChildren().addAll(genBtn, csvBtn);

        genCard.getChildren().addAll(genTitle, row1, buttons, reportPreview);

        // ── Event Logs Section ──
        VBox logsCard = new VBox(0);
        logsCard.setStyle("-fx-background-color: " + CARD() + "; -fx-border-color: " + BORDER()
                + "; -fx-border-width: 1; -fx-background-radius: 8; -fx-border-radius: 8;");
        HBox logsHdr = new HBox();
        logsHdr.setPadding(new Insets(16));
        logsHdr.setStyle("-fx-border-color: " + BORDER() + "; -fx-border-width: 0 0 1 0;");
        Label logsTitle = new Label("Audit Trail — Event Logs");
        logsTitle.setFont(Font.font("Segoe UI", FontWeight.MEDIUM, 17));
        logsTitle.setTextFill(Color.web(TEXT()));
        logsHdr.getChildren().add(logsTitle);

        GridPane logsGrid = new GridPane();
        String[] logCols = { "Timestamp", "Action", "Details", "User", "" };
        for (int i = 0; i < logCols.length; i++) {
            Label h = new Label(logCols[i]);
            h.setFont(Font.font("Segoe UI", FontWeight.SEMI_BOLD, 11));
            h.setTextFill(Color.web(TEXT()));
            h.setPadding(new Insets(8, 14, 8, 14));
            h.setMaxWidth(Double.MAX_VALUE);
            h.setStyle("-fx-background-color: " + MUTED() + ";");
            logsGrid.add(h, i, 0);
        }
        List<SystemLog> eventLogs = systemLogService.getAll();
        int logRow = 1;
        for (SystemLog log : eventLogs) {
            String cellBorder = "-fx-border-color: " + BORDER() + "; -fx-border-width: 0 0 1 0;";
            Label ts = new Label(log.getTimestamp() != null ? log.getTimestamp() : "");
            ts.setFont(Font.font("Consolas", 11));
            ts.setTextFill(Color.web(MUTED_FG()));
            ts.setPadding(new Insets(8, 14, 8, 14));
            ts.setStyle(cellBorder);

            Label act = new Label(log.getEventType() != null ? log.getEventType() : "");
            act.setFont(Font.font("Segoe UI", FontWeight.MEDIUM, 12));
            act.setTextFill(Color.web(TEXT()));
            act.setPadding(new Insets(8, 14, 8, 14));
            act.setStyle(cellBorder);

            Label det = new Label(log.getDescription() != null ? log.getDescription() : "");
            det.setFont(Font.font("Segoe UI", 12));
            det.setTextFill(Color.web(MUTED_FG()));
            det.setPadding(new Insets(8, 14, 8, 14));
            det.setStyle(cellBorder);

            Label usr = new Label(log.getActor() != null ? log.getActor() : "");
            usr.setFont(Font.font("Segoe UI", 12));
            usr.setTextFill(Color.web(TEXT()));
            usr.setPadding(new Insets(8, 14, 8, 14));
            usr.setStyle(cellBorder);

            final SystemLog currentLog = log;
            Button viewLogBtn = new Button("View");
            viewLogBtn.setFont(Font.font("Segoe UI", 10));
            viewLogBtn.setStyle("-fx-background-color: " + PRIMARY
                    + "; -fx-text-fill: white; -fx-background-radius: 4; -fx-padding: 3 8; -fx-cursor: hand;");
            viewLogBtn.setOnAction(ev -> {
                Alert detail = new Alert(Alert.AlertType.INFORMATION);
                detail.setTitle("Log Entry Detail");
                detail.setHeaderText(currentLog.getEventType());
                detail.setContentText(
                        "Timestamp: " + (currentLog.getTimestamp() != null ? currentLog.getTimestamp() : "N/A")
                                + "\nDetails: "
                                + (currentLog.getDescription() != null ? currentLog.getDescription() : "N/A")
                                + "\nUser: " + (currentLog.getActor() != null ? currentLog.getActor() : "N/A"));
                detail.showAndWait();
            });
            HBox viewBtnW = new HBox(viewLogBtn);
            viewBtnW.setPadding(new Insets(4, 14, 4, 14));
            viewBtnW.setStyle(cellBorder);

            logsGrid.add(ts, 0, logRow);
            logsGrid.add(act, 1, logRow);
            logsGrid.add(det, 2, logRow);
            logsGrid.add(usr, 3, logRow);
            logsGrid.add(viewBtnW, 4, logRow);
            logRow++;
        }
        if (eventLogs.isEmpty()) {
            Label noLogs = new Label("No event logs recorded yet.");
            noLogs.setTextFill(Color.web(MUTED_FG()));
            noLogs.setPadding(new Insets(16));
            logsGrid.add(noLogs, 0, 1);
            GridPane.setColumnSpan(noLogs, 5);
        }
        logsCard.getChildren().addAll(logsHdr, logsGrid);

        page.getChildren().addAll(new VBox(4, title, sub), genCard, logsCard);
        return wrapScroll(page);
    }

    private Label cellLabel(String text) {
        Label l = new Label(text);
        l.setFont(Font.font("Segoe UI", 12));
        l.setTextFill(Color.web(TEXT()));
        l.setPadding(new Insets(6, 12, 6, 12));
        l.setStyle("-fx-border-color: " + BORDER() + "; -fx-border-width: 0 0 1 0;");
        return l;
    }

    // ── Form helpers ────────────────
    private VBox formField(String label, String value) {
        VBox box = new VBox(8);
        HBox.setHgrow(box, Priority.ALWAYS);
        Label l = new Label(label);
        l.setFont(Font.font("Segoe UI", FontWeight.MEDIUM, 13));
        l.setTextFill(Color.web(TEXT()));
        TextField f = new TextField(value);
        f.setStyle("-fx-background-color: " + CARD() + "; -fx-border-color: " + BORDER()
                + "; -fx-border-width: 1; -fx-border-radius: 4; -fx-background-radius: 4; -fx-padding: 8 12; -fx-font-size: 13px;"
                + "-fx-text-fill: " + TEXT() + ";");
        box.getChildren().addAll(l, f);
        return box;
    }

    private VBox formFieldArea(String label, String value) {
        VBox box = new VBox(8);
        Label l = new Label(label);
        l.setFont(Font.font("Segoe UI", FontWeight.MEDIUM, 13));
        TextArea a = new TextArea(value);
        a.setPrefRowCount(3);
        a.setStyle("-fx-background-color: " + CARD() + "; -fx-border-color: " + BORDER()
                + "; -fx-border-width: 1; -fx-border-radius: 4; -fx-background-radius: 4; -fx-padding: 8 12; -fx-font-size: 13px;");
        box.getChildren().addAll(l, a);
        return box;
    }

    private VBox vitalCard(String label, String value) {
        VBox box = new VBox(4);
        box.setPadding(new Insets(12));
        box.setStyle("-fx-background-color: " + MUTED() + "; -fx-background-radius: 8;");
        HBox.setHgrow(box, Priority.ALWAYS);
        Label l = new Label(label);
        l.setFont(Font.font("Segoe UI", 11));
        l.setTextFill(Color.web(MUTED_FG()));
        Label v = new Label(value);
        v.setFont(Font.font("Segoe UI", FontWeight.SEMI_BOLD, 18));
        v.setTextFill(Color.web(SECONDARY));
        box.getChildren().addAll(l, v);
        return box;
    }

    private ScrollPane buildAlertDetailView(String[] alert) {
        VBox page = new VBox(20);
        page.setPadding(new Insets(24));

        Button backBtn = new Button("\u2190 Back to Alerts");
        backBtn.setStyle("-fx-background-color: transparent; -fx-text-fill: " + PRIMARY + "; -fx-cursor: hand;");
        backBtn.setOnAction(e -> root.setCenter(buildAlertsPage()));

        Label title = new Label("Alert Detail: " + alert[1]);
        title.setFont(Font.font("Segoe UI", FontWeight.BOLD, 22));
        title.setTextFill(Color.web(TEXT()));

        VBox card = new VBox(20);
        card.setPadding(new Insets(24));
        card.setStyle("-fx-background-color: " + CARD() + "; -fx-border-color: " + BORDER()
                + "; -fx-border-width: 1; -fx-background-radius: 8; -fx-border-radius: 8;");

        Label type = new Label(alert[0].toUpperCase() + " ALERT");
        type.setTextFill(Color.web(alert[0].equals("critical") ? DESTRUCTIVE : WARNING));
        type.setFont(Font.font("Segoe UI", FontWeight.BOLD, 12));

        Label desc = new Label(alert[4]);
        desc.setFont(Font.font("Segoe UI", 16));
        desc.setTextFill(Color.web(TEXT()));
        desc.setWrapText(true);

        GridPane details = new GridPane();
        details.setHgap(30);
        details.setVgap(10);
        details.add(new Label("Timestamp:"), 0, 0);
        details.add(new Label(alert[3]), 1, 0);
        details.add(new Label("Affected Child:"), 0, 1);
        details.add(new Label(alert[2]), 1, 1);

        Button resolve = new Button("Mark as Resolved");
        resolve.setStyle("-fx-background-color: " + SECONDARY
                + "; -fx-text-fill: white; -fx-background-radius: 4; -fx-padding: 10 20; -fx-font-weight: bold; -fx-cursor: hand;");
        resolve.setOnAction(e -> root.setCenter(buildAlertsPage()));

        card.getChildren().addAll(type, title, desc, new Separator(), details, resolve);
        page.getChildren().addAll(backBtn, card);
        return wrapScroll(page);
    }

    private ScrollPane buildRecordDonationPage() {
        VBox page = new VBox(20);
        page.setPadding(new Insets(24));

        Button backBtn = new Button("\u2190 Back to Sponsorship");
        backBtn.setStyle("-fx-background-color: transparent; -fx-text-fill: " + PRIMARY + "; -fx-cursor: hand;");
        backBtn.setOnAction(e -> root.setCenter(buildSponsorshipPage()));

        Label title = new Label("Record Donation");
        title.setFont(Font.font("Segoe UI", FontWeight.BOLD, 22));
        title.setTextFill(Color.web(TEXT()));

        VBox card = new VBox(20);
        card.setPadding(new Insets(24));
        card.setStyle("-fx-background-color: " + CARD() + "; -fx-border-color: " + BORDER()
                + "; -fx-border-width: 1; -fx-background-radius: 8; -fx-border-radius: 8;");

        VBox donorField = formField("Donor Name", "");
        VBox amountField = formField("Amount (\u09F3)", "");
        VBox purposeField = formField("Purpose", "General Welfare");

        Button save = new Button("Save Record");
        save.setStyle("-fx-background-color: " + SECONDARY
                + "; -fx-text-fill: white; -fx-background-radius: 4; -fx-padding: 10 24; -fx-font-weight: bold; -fx-cursor: hand;");
        save.setOnAction(e -> {
            TextField amtTf = (TextField) amountField.getChildren().get(1);
            TextField purposeTf = (TextField) purposeField.getChildren().get(1);
            String amtText = amtTf.getText().trim();
            if (amtText.isEmpty()) {
                showAlert("Warning", "Amount is required.");
                return;
            }
            double amount;
            try {
                amount = Double.parseDouble(amtText);
            } catch (NumberFormatException ex) {
                showAlert("Warning", "Amount must be a number.");
                return;
            }
            Donation d = new Donation();
            d.setDonorId(user.getId());
            d.setChildId(selectedChild != null ? selectedChild.getId() : 0);
            d.setAmount(amount);
            d.setPurpose(purposeTf.getText().trim());
            d.setDate(LocalDate.now().toString());
            d.setStatus("Completed");
            donationService.save(d);
            systemLogService.save(new SystemLog("Donation",
                    "Recorded donation of " + amtText, user.getUsername(),
                    LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))));
            showAlert("Success", "Donation recorded successfully!");
            root.setCenter(buildSponsorshipPage());
        });

        card.getChildren().addAll(donorField, amountField, purposeField, new Separator(), save);

        page.getChildren().addAll(backBtn, title, card);
        return wrapScroll(page);
    }

    private ScrollPane buildRecordExpensePage() {
        VBox page = new VBox(20);
        page.setPadding(new Insets(24));

        Button backBtn = new Button("\u2190 Back to Sponsorship");
        backBtn.setStyle("-fx-background-color: transparent; -fx-text-fill: " + PRIMARY + "; -fx-cursor: hand;");
        backBtn.setOnAction(e -> root.setCenter(buildSponsorshipPage()));

        Label title = new Label("Record Expense");
        title.setFont(Font.font("Segoe UI", FontWeight.BOLD, 22));
        title.setTextFill(Color.web(TEXT()));

        VBox card = new VBox(20);
        card.setPadding(new Insets(24));
        card.setStyle("-fx-background-color: " + CARD() + "; -fx-border-color: " + BORDER()
                + "; -fx-border-width: 1; -fx-background-radius: 8; -fx-border-radius: 8;");

        VBox categoryField = formField("Category", "");
        VBox amountField = formField("Amount (\u09F3)", "");
        VBox descField = formFieldArea("Description", "");

        Button save = new Button("Save Record");
        save.setStyle("-fx-background-color: " + PRIMARY
                + "; -fx-text-fill: white; -fx-background-radius: 4; -fx-padding: 10 24; -fx-font-weight: bold; -fx-cursor: hand;");
        save.setOnAction(e -> {
            TextField amtTf = (TextField) amountField.getChildren().get(1);
            TextField catTf = (TextField) categoryField.getChildren().get(1);
            TextArea descTa = (TextArea) descField.getChildren().get(1);
            String amtText = amtTf.getText().trim();
            if (amtText.isEmpty()) {
                showAlert("Warning", "Amount is required.");
                return;
            }
            double amount;
            try {
                amount = Double.parseDouble(amtText);
            } catch (NumberFormatException ex) {
                showAlert("Warning", "Amount must be a number.");
                return;
            }
            // Save expense to DB
            model.entity.Expense expense = new model.entity.Expense();
            expense.setChildId(selectedChild != null ? selectedChild.getId() : 0);
            expense.setCategory(catTf.getText().trim());
            expense.setAmount(amount);
            expense.setDescription(descTa.getText().trim());
            expense.setDate(LocalDate.now().toString());
            expenseService.save(expense);
            systemLogService.save(new SystemLog("Expense",
                    "Recorded expense: " + catTf.getText().trim() + " - " + amtText,
                    user.getUsername(),
                    LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))));
            showAlert("Success", "Expense recorded successfully!");
            root.setCenter(buildSponsorshipPage());
        });

        card.getChildren().addAll(categoryField, amountField, descField, new Separator(), save);

        page.getChildren().addAll(backBtn, title, card);
        return wrapScroll(page);
    }
}