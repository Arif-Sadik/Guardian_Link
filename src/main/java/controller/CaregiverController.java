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
import model.entity.Child;
import model.entity.SystemLog;
import model.user.User;
import service.ChildService;
import service.SystemLogService;
import model.entity.MedicalRecord;
import model.entity.EducationRecord;
import service.MedicalRecordService;
import service.EducationRecordService;
import util.ThemeManager;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Caregiver dashboard.
 * Sidebar pages: Dashboard, My Children, Alerts, Reports.
 */
public class CaregiverController {

    private final Stage stage;
    private final User user;
    private BorderPane root;
    private VBox sidebar;
    private String activePage = "dashboard";

    private final ChildService childService = new ChildService();
    private final SystemLogService systemLogService = new SystemLogService();
    private final MedicalRecordService medicalRecordService = new MedicalRecordService();
    private final EducationRecordService educationRecordService = new EducationRecordService();

    // Theme tokens
    private static final String PRIMARY = ThemeManager.PRIMARY;
    private static final String SECONDARY = ThemeManager.SECONDARY;
    private static final String WARNING = ThemeManager.WARNING;
    private static final String DESTRUCTIVE = ThemeManager.DESTRUCTIVE;
    private static final String INFO = ThemeManager.INFO;

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

    public CaregiverController(Stage stage, User user) {
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
        stage.setTitle("GuardianLink \u2014 Caregiver Dashboard");
        stage.show();
    }

    private void refreshTheme() {
        root.setStyle("-fx-background-color: " + BG() + ";");
        root.setTop(buildHeader());
        root.setLeft(buildSidebar());
        switch (activePage) {
            case "dashboard" -> root.setCenter(buildDashboardPage());
            case "children" -> root.setCenter(buildChildrenPage());
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

        Label logoIcon = new Label("\uD83D\uDEE1");
        logoIcon.setFont(Font.font("Segoe UI Emoji", 28));
        logoIcon.setTextFill(Color.web(PRIMARY));

        VBox titleBox = new VBox(0);
        titleBox.setPadding(new Insets(0, 0, 0, 14));
        Label t1 = new Label("GuardianLink");
        t1.setFont(Font.font("Segoe UI", FontWeight.BOLD, 20));
        t1.setTextFill(Color.web(TEXT()));
        Label t2 = new Label("Caregiver Portal");
        t2.setFont(Font.font("Segoe UI", FontWeight.SEMI_BOLD, 12));
        t2.setTextFill(Color.web(MUTED_FG()));
        titleBox.getChildren().addAll(t1, t2);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        HBox userTypeBox = new HBox(8);
        userTypeBox.setAlignment(Pos.CENTER_RIGHT);
        userTypeBox.setPadding(new Insets(8, 16, 8, 16));
        userTypeBox.setStyle("-fx-background-color: " + MUTED() + "; -fx-background-radius: 8; -fx-border-color: "
                + BORDER() + "; -fx-border-radius: 8;");

        VBox userInfo = new VBox(2);
        Label uName = new Label(user.getUsername());
        uName.setFont(Font.font("Segoe UI", FontWeight.MEDIUM, 13));
        uName.setTextFill(Color.web(TEXT()));
        Label uRole = new Label("Caregiver");
        uRole.setFont(Font.font("Segoe UI", FontWeight.BOLD, 11));
        uRole.setTextFill(Color.web(PRIMARY));
        userInfo.getChildren().addAll(uName, uRole);

        userTypeBox.getChildren().addAll(userInfo);
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
                sidebarBtn("My Children", "children"),
                sidebarBtn("Alerts", "alerts"),
                sidebarBtn("Daily Reports", "reports"));
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

        // Logout
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

        sidebar.getChildren().addAll(spacer, themeSection, logoutSection);
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
            }
        });
        return btn;
    }

    private void styleSidebarBtn(Button btn, boolean active) {
        if (active)
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
        if (text.contains("Children"))
            return "children";
        if (text.contains("Alerts"))
            return "alerts";
        if (text.contains("Reports"))
            return "reports";
        return "";
    }

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

    // ═══════════ PAGES ═══════════

    // -- DASHBOARD --
    private ScrollPane buildDashboardPage() {
        VBox page = new VBox(20);
        page.setPadding(new Insets(24));

        Label title = new Label("Caregiver Dashboard");
        title.setFont(Font.font("Segoe UI", FontWeight.MEDIUM, 20));
        title.setTextFill(Color.web(TEXT()));
        Label sub = new Label("Daily overview of care activities");
        sub.setFont(Font.font("Segoe UI", 13));
        sub.setTextFill(Color.web(MUTED_FG()));

        // Real-time stats from DB
        List<Child> allChildren = childService.getAllChildren();
        int childCount = allChildren.size();
        int logCount = systemLogService.getCount();

        HBox stats = new HBox(16);
        stats.getChildren().addAll(
                statCard("Assigned Children", String.valueOf(childCount),
                        childCount > 0 ? "All registered" : "No children yet", SECONDARY),
                statCard("System Logs", String.valueOf(logCount), "Total entries", WARNING),
                statCard("Upcoming Events", String.valueOf(Math.min(childCount, 3)), "Scheduled activities", INFO));

        Label qaTitle = new Label("Quick Actions");
        qaTitle.setFont(Font.font("Segoe UI", FontWeight.MEDIUM, 17));
        qaTitle.setTextFill(Color.web(TEXT()));

        HBox qaCards = new HBox(12);
        VBox qaViewChildren = createQuickActionCard("View Children", "See assigned children");
        qaViewChildren.setOnMouseClicked(e -> {
            activePage = "children";
            refreshSidebar();
            root.setCenter(buildChildrenPage());
        });
        VBox qaSubmitReport = createQuickActionCard("Submit Report", "Log daily activities");
        qaSubmitReport.setOnMouseClicked(e -> {
            activePage = "reports";
            refreshSidebar();
            root.setCenter(buildReportsPage());
        });
        VBox qaViewAlerts = createQuickActionCard("View Alerts", "Check notifications");
        qaViewAlerts.setOnMouseClicked(e -> {
            activePage = "alerts";
            refreshSidebar();
            root.setCenter(buildAlertsPage());
        });
        qaCards.getChildren().addAll(qaViewChildren, qaSubmitReport, qaViewAlerts);

        // Recent activity from system logs
        VBox recentCard = new VBox(0);
        recentCard.setStyle("-fx-background-color: " + CARD() + "; -fx-border-color: " + BORDER()
                + "; -fx-border-width: 1; -fx-background-radius: 8; -fx-border-radius: 8;");
        HBox recentHdr = new HBox();
        recentHdr.setPadding(new Insets(16));
        recentHdr.setStyle("-fx-border-color: " + BORDER() + "; -fx-border-width: 0 0 1 0;");
        Label recentTitle = new Label("Recent Activity");
        recentTitle.setFont(Font.font("Segoe UI", FontWeight.MEDIUM, 17));
        recentTitle.setTextFill(Color.web(TEXT()));
        recentHdr.getChildren().add(recentTitle);

        VBox recentList = new VBox(0);
        List<SystemLog> recentLogs = systemLogService.getRecent(5);
        if (recentLogs.isEmpty()) {
            Label noLogs = new Label("No recent activity.");
            noLogs.setTextFill(Color.web(MUTED_FG()));
            noLogs.setPadding(new Insets(16));
            recentList.getChildren().add(noLogs);
        } else {
            for (SystemLog log : recentLogs) {
                HBox logRow = new HBox(12);
                logRow.setPadding(new Insets(12, 16, 12, 16));
                logRow.setStyle("-fx-border-color: " + BORDER() + "; -fx-border-width: 0 0 1 0;");
                logRow.setAlignment(Pos.CENTER_LEFT);

                Label typeBadge = new Label(log.getEventType());
                typeBadge.setFont(Font.font("Segoe UI", 11));
                typeBadge.setStyle("-fx-background-color: " + PRIMARY + "1A; -fx-text-fill: " + PRIMARY
                        + "; -fx-background-radius: 4; -fx-padding: 2 8;");

                Label desc = new Label(log.getDescription());
                desc.setFont(Font.font("Segoe UI", 13));
                desc.setTextFill(Color.web(TEXT()));
                HBox.setHgrow(desc, Priority.ALWAYS);

                Label time = new Label(log.getTimestamp() != null ? log.getTimestamp() : "");
                time.setFont(Font.font("Segoe UI", 11));
                time.setTextFill(Color.web(MUTED_FG()));

                logRow.getChildren().addAll(typeBadge, desc, time);
                recentList.getChildren().add(logRow);
            }
        }
        recentCard.getChildren().addAll(recentHdr, recentList);

        page.getChildren().addAll(new VBox(4, title, sub), stats, qaTitle, qaCards, recentCard);
        return wrapScroll(page);
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

    // -- CHILDREN --
    private ScrollPane buildChildrenPage() {
        VBox page = new VBox(20);
        page.setPadding(new Insets(24));
        Label title = new Label("My Assigned Children");
        title.setFont(Font.font("Segoe UI", FontWeight.MEDIUM, 20));
        title.setTextFill(Color.web(TEXT()));

        VBox list = new VBox(12);
        List<Child> children = childService.getAllChildren();
        if (children.isEmpty()) {
            Label empty = new Label("No children assigned.");
            empty.setTextFill(Color.web(MUTED_FG()));
            list.getChildren().add(empty);
        } else {
            for (Child c : children) {
                HBox row = new HBox(16);
                row.setPadding(new Insets(16));
                row.setStyle("-fx-background-color: " + CARD() + "; -fx-border-color: " + BORDER()
                        + "; -fx-border-radius: 8; -fx-background-radius: 8;");
                row.setAlignment(Pos.CENTER_LEFT);

                Label avatarLetter = new Label(c.getName().substring(0, 1));
                avatarLetter.setTextFill(Color.web(PRIMARY));
                avatarLetter.setFont(Font.font("Segoe UI", FontWeight.BOLD, 16));
                StackPane avatar = new StackPane(avatarLetter);
                avatar.setPrefSize(40, 40);
                avatar.setStyle("-fx-background-color: " + PRIMARY + "20; -fx-background-radius: 20;");

                VBox info = new VBox(2);
                Label name = new Label(c.getName());
                name.setFont(Font.font("Segoe UI", FontWeight.SEMI_BOLD, 14));
                name.setTextFill(Color.web(TEXT()));
                Label age = new Label(c.getAge() + " years \u2022 " + (c.getGender() == null ? "N/A" : c.getGender()));
                age.setFont(Font.font("Segoe UI", 12));
                age.setTextFill(Color.web(MUTED_FG()));

                Label statusBadge = new Label(c.getStatus() != null ? c.getStatus() : "Active");
                statusBadge.setFont(Font.font("Segoe UI", 11));
                String st = c.getStatus() != null ? c.getStatus() : "Active";
                String bc = st.equals("Active") ? SECONDARY + "1A" : WARNING + "1A";
                String fc = st.equals("Active") ? SECONDARY : WARNING;
                statusBadge.setStyle("-fx-background-color: " + bc + "; -fx-text-fill: " + fc
                        + "; -fx-background-radius: 4; -fx-padding: 2 8;");

                info.getChildren().addAll(name, age);

                Region r = new Region();
                HBox.setHgrow(r, Priority.ALWAYS);

                Button view = new Button("View Details");
                styleBtn(view, PRIMARY);
                final int childId = c.getId();
                view.setOnAction(e -> root.setCenter(buildChildDetailPage(childId)));

                row.getChildren().addAll(avatar, info, statusBadge, r, view);
                list.getChildren().add(row);
            }
        }

        page.getChildren().addAll(title, list);
        return wrapScroll(page);
    }

    // Child detail page (replaces the old placeholder Alert dialog)
    private ScrollPane buildChildDetailPage(int childId) {
        Child child = childService.getChildById(childId);
        if (child == null) {
            VBox err = new VBox(new Label("Child not found."));
            err.setPadding(new Insets(24));
            return wrapScroll(err);
        }

        VBox page = new VBox(20);
        page.setPadding(new Insets(24));

        Button backBtn = new Button("\u2190 Back to My Children");
        backBtn.setStyle("-fx-background-color: transparent; -fx-text-fill: " + PRIMARY + "; -fx-cursor: hand;");
        backBtn.setOnAction(e -> root.setCenter(buildChildrenPage()));

        Label title = new Label("Child Profile: " + child.getName());
        title.setFont(Font.font("Segoe UI", FontWeight.BOLD, 22));
        title.setTextFill(Color.web(TEXT()));

        // Profile header card
        VBox profileCard = new VBox(20);
        profileCard.setPadding(new Insets(24));
        profileCard.setStyle("-fx-background-color: " + CARD() + "; -fx-border-color: " + BORDER()
                + "; -fx-border-width: 1; -fx-background-radius: 8; -fx-border-radius: 8;");

        HBox hdr = new HBox(20);
        hdr.setAlignment(Pos.CENTER_LEFT);
        Label avatarLetter = new Label(child.getName().substring(0, 1));
        avatarLetter.setTextFill(Color.web(PRIMARY));
        avatarLetter.setFont(Font.font("Segoe UI", FontWeight.BOLD, 24));
        StackPane avatar = new StackPane(avatarLetter);
        avatar.setPrefSize(64, 64);
        avatar.setStyle("-fx-background-color: " + PRIMARY + "1A; -fx-background-radius: 32;");

        VBox info = new VBox(4);
        Label n = new Label(child.getName());
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

        profileCard.getChildren().addAll(hdr, new Separator(), details);

        // Medical Records section
        VBox medCard = new VBox(12);
        medCard.setPadding(new Insets(20));
        medCard.setStyle("-fx-background-color: " + CARD() + "; -fx-border-color: " + BORDER()
                + "; -fx-border-width: 1; -fx-background-radius: 8; -fx-border-radius: 8;");
        Label medTitle = new Label("Medical Records");
        medTitle.setFont(Font.font("Segoe UI", FontWeight.MEDIUM, 17));
        medTitle.setTextFill(Color.web(TEXT()));

        List<MedicalRecord> medRecords = medicalRecordService.getRecordsByChildId(childId);
        if (medRecords.isEmpty()) {
            Label noMed = new Label("No medical records available.");
            noMed.setTextFill(Color.web(MUTED_FG()));
            medCard.getChildren().addAll(medTitle, noMed);
        } else {
            VBox medList = new VBox(8);
            for (MedicalRecord rec : medRecords) {
                HBox recRow = new HBox(12);
                recRow.setPadding(new Insets(8));
                recRow.setStyle("-fx-background-color: " + MUTED() + "; -fx-background-radius: 4;");
                Label recDate = new Label(rec.getLastCheckup() != null ? rec.getLastCheckup() : "");
                recDate.setFont(Font.font("Segoe UI", 12));
                recDate.setTextFill(Color.web(MUTED_FG()));
                Label recType = new Label(rec.getBloodGroup() != null ? rec.getBloodGroup() : "");
                recType.setFont(Font.font("Segoe UI", FontWeight.MEDIUM, 13));
                recType.setTextFill(Color.web(TEXT()));
                Label recDesc = new Label(rec.getMedicalCondition() != null ? rec.getMedicalCondition() : "");
                recDesc.setFont(Font.font("Segoe UI", 13));
                recDesc.setTextFill(Color.web(TEXT()));
                recRow.getChildren().addAll(recDate, recType, recDesc);
                medList.getChildren().add(recRow);
            }
            medCard.getChildren().addAll(medTitle, medList);
        }

        // Education Records section
        VBox eduCard = new VBox(12);
        eduCard.setPadding(new Insets(20));
        eduCard.setStyle("-fx-background-color: " + CARD() + "; -fx-border-color: " + BORDER()
                + "; -fx-border-width: 1; -fx-background-radius: 8; -fx-border-radius: 8;");
        Label eduTitle = new Label("Education Records");
        eduTitle.setFont(Font.font("Segoe UI", FontWeight.MEDIUM, 17));
        eduTitle.setTextFill(Color.web(TEXT()));

        List<EducationRecord> eduRecords = educationRecordService.getRecordsByChildId(childId);
        if (eduRecords.isEmpty()) {
            Label noEdu = new Label("No education records available.");
            noEdu.setTextFill(Color.web(MUTED_FG()));
            eduCard.getChildren().addAll(eduTitle, noEdu);
        } else {
            VBox eduList = new VBox(8);
            for (EducationRecord rec : eduRecords) {
                HBox recRow = new HBox(12);
                recRow.setPadding(new Insets(8));
                recRow.setStyle("-fx-background-color: " + MUTED() + "; -fx-background-radius: 4;");
                Label recSchool = new Label(rec.getSchoolName() != null ? rec.getSchoolName() : "");
                recSchool.setFont(Font.font("Segoe UI", FontWeight.MEDIUM, 13));
                recSchool.setTextFill(Color.web(TEXT()));
                Label recGrade = new Label(rec.getGrade() != null ? rec.getGrade() : "");
                recGrade.setFont(Font.font("Segoe UI", 13));
                recGrade.setTextFill(Color.web(TEXT()));
                Label recPerf = new Label(String.format("%.0f%% attendance", rec.getAttendancePercentage()));
                recPerf.setFont(Font.font("Segoe UI", 13));
                recPerf.setTextFill(Color.web(SECONDARY));
                recRow.getChildren().addAll(recSchool, recGrade, recPerf);
                eduList.getChildren().add(recRow);
            }
            eduCard.getChildren().addAll(eduTitle, eduList);
        }

        page.getChildren().addAll(backBtn, title, profileCard, medCard, eduCard);
        return wrapScroll(page);
    }

    // -- ALERTS --
    private ScrollPane buildAlertsPage() {
        VBox page = new VBox(20);
        page.setPadding(new Insets(24));
        Label title = new Label("Care Alerts");
        title.setFont(Font.font("Segoe UI", FontWeight.MEDIUM, 20));
        title.setTextFill(Color.web(TEXT()));
        Label sub = new Label("Notifications and system alerts");
        sub.setFont(Font.font("Segoe UI", 13));
        sub.setTextFill(Color.web(MUTED_FG()));

        // Load real alerts from system logs
        List<SystemLog> logs = systemLogService.getRecent(20);

        HBox stats = new HBox(16);
        stats.getChildren().addAll(
                statCard("Total Alerts", String.valueOf(logs.size()), "From system logs", WARNING),
                statCard("System Logs", String.valueOf(systemLogService.getCount()), "All time", MUTED_FG()));

        VBox alertsList = new VBox(12);
        if (logs.isEmpty()) {
            Label noAlerts = new Label("No alerts at this time.");
            noAlerts.setTextFill(Color.web(MUTED_FG()));
            noAlerts.setPadding(new Insets(16));
            alertsList.getChildren().add(noAlerts);
        } else {
            for (SystemLog log : logs) {
                String eventType = log.getEventType() != null ? log.getEventType() : "Info";
                String borderC = eventType.contains("Error") || eventType.contains("Delete") ? DESTRUCTIVE : BORDER();
                String iconColor = eventType.contains("Error") || eventType.contains("Delete") ? DESTRUCTIVE
                        : eventType.contains("Warning") ? WARNING : INFO;

                VBox card = new VBox(8);
                card.setPadding(new Insets(12));
                card.setStyle("-fx-background-color: " + CARD() + "; -fx-border-color: " + borderC
                        + "; -fx-border-radius: 8; -fx-background-radius: 8;");

                HBox top = new HBox(12);
                top.setAlignment(Pos.CENTER_LEFT);

                Label typeBadge = new Label(eventType);
                typeBadge.setFont(Font.font("Segoe UI", FontWeight.BOLD, 11));
                typeBadge.setStyle("-fx-background-color: " + iconColor + "1A; -fx-text-fill: " + iconColor
                        + "; -fx-background-radius: 4; -fx-padding: 2 8;");

                Label desc = new Label(log.getDescription() != null ? log.getDescription() : "");
                desc.setFont(Font.font("Segoe UI", 13));
                desc.setTextFill(Color.web(TEXT()));
                desc.setWrapText(true);
                HBox.setHgrow(desc, Priority.ALWAYS);

                Region sp = new Region();
                HBox.setHgrow(sp, Priority.ALWAYS);

                Label tm = new Label(log.getTimestamp() != null ? log.getTimestamp() : "");
                tm.setFont(Font.font("Segoe UI", 11));
                tm.setTextFill(Color.web(MUTED_FG()));

                top.getChildren().addAll(typeBadge, desc, sp, tm);

                HBox bottom = new HBox(8);
                Label actor = new Label("By: " + (log.getActor() != null ? log.getActor() : "System"));
                actor.setFont(Font.font("Segoe UI", 11));
                actor.setTextFill(Color.web(MUTED_FG()));
                bottom.getChildren().add(actor);

                card.getChildren().addAll(top, bottom);
                alertsList.getChildren().add(card);
            }
        }

        page.getChildren().addAll(new VBox(4, title, sub), stats, alertsList);
        return wrapScroll(page);
    }

    // -- REPORTS --
    private ScrollPane buildReportsPage() {
        VBox page = new VBox(20);
        page.setPadding(new Insets(24));
        Label title = new Label("Daily Reports");
        title.setFont(Font.font("Segoe UI", FontWeight.MEDIUM, 20));
        title.setTextFill(Color.web(TEXT()));

        // Submit new report form
        VBox content = new VBox(16);
        content.setPadding(new Insets(16));
        content.setStyle(
                "-fx-background-color: " + CARD() + "; -fx-border-color: " + BORDER()
                        + "; -fx-border-radius: 8; -fx-background-radius: 8;");

        Label formTitle = new Label("Submit Daily Report");
        formTitle.setFont(Font.font("Segoe UI", FontWeight.MEDIUM, 17));
        formTitle.setTextFill(Color.web(TEXT()));

        Label l1 = new Label("Report Date");
        l1.setTextFill(Color.web(TEXT()));
        DatePicker datePicker = new DatePicker(java.time.LocalDate.now());

        Label l2 = new Label("Summary of Activities");
        l2.setTextFill(Color.web(TEXT()));
        TextArea summary = new TextArea();
        summary.setPrefRowCount(5);
        summary.setPromptText("Describe daily activities, observations, and any concerns...");

        Button submit = new Button("Submit Daily Report");
        styleBtn(submit, PRIMARY);
        submit.setOnAction(e -> {
            String summaryText = summary.getText().trim();
            if (summaryText.isEmpty()) {
                Alert a = new Alert(Alert.AlertType.WARNING, "Please enter a summary of activities.");
                a.show();
                return;
            }
            String dateStr = datePicker.getValue() != null ? datePicker.getValue().toString() : "";
            SystemLog report = new SystemLog("Daily Report",
                    "Caregiver report (" + dateStr + "): " + summaryText,
                    user.getUsername(),
                    LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
            systemLogService.save(report);
            Alert a = new Alert(Alert.AlertType.INFORMATION, "Report submitted successfully!");
            a.show();
            summary.clear();
            // Refresh page to show the new report in history
            root.setCenter(buildReportsPage());
        });

        content.getChildren().addAll(formTitle, l1, datePicker, l2, summary, submit);

        // Report History
        VBox historyCard = new VBox(0);
        historyCard.setStyle("-fx-background-color: " + CARD() + "; -fx-border-color: " + BORDER()
                + "; -fx-border-width: 1; -fx-background-radius: 8; -fx-border-radius: 8;");

        HBox histHdr = new HBox();
        histHdr.setPadding(new Insets(16));
        histHdr.setStyle("-fx-border-color: " + BORDER() + "; -fx-border-width: 0 0 1 0;");
        Label histTitle = new Label("Report History");
        histTitle.setFont(Font.font("Segoe UI", FontWeight.MEDIUM, 17));
        histTitle.setTextFill(Color.web(TEXT()));
        histHdr.getChildren().add(histTitle);

        VBox histList = new VBox(0);
        // Filter system logs for daily reports
        List<SystemLog> allLogs = systemLogService.getRecent(50);
        boolean hasReports = false;
        for (SystemLog log : allLogs) {
            if (log.getEventType() != null && log.getEventType().equals("Daily Report")) {
                hasReports = true;
                HBox logRow = new HBox(12);
                logRow.setPadding(new Insets(12, 16, 12, 16));
                logRow.setStyle("-fx-border-color: " + BORDER() + "; -fx-border-width: 0 0 1 0;");
                logRow.setAlignment(Pos.CENTER_LEFT);

                Label badge = new Label("Report");
                badge.setFont(Font.font("Segoe UI", 11));
                badge.setStyle("-fx-background-color: " + SECONDARY + "1A; -fx-text-fill: " + SECONDARY
                        + "; -fx-background-radius: 4; -fx-padding: 2 8;");

                Label desc = new Label(log.getDescription() != null ? log.getDescription() : "");
                desc.setFont(Font.font("Segoe UI", 13));
                desc.setTextFill(Color.web(TEXT()));
                desc.setWrapText(true);
                HBox.setHgrow(desc, Priority.ALWAYS);

                Label time = new Label(log.getTimestamp() != null ? log.getTimestamp() : "");
                time.setFont(Font.font("Segoe UI", 11));
                time.setTextFill(Color.web(MUTED_FG()));

                logRow.getChildren().addAll(badge, desc, time);
                histList.getChildren().add(logRow);
            }
        }
        if (!hasReports) {
            Label noHist = new Label("No reports submitted yet.");
            noHist.setTextFill(Color.web(MUTED_FG()));
            noHist.setPadding(new Insets(16));
            histList.getChildren().add(noHist);
        }

        historyCard.getChildren().addAll(histHdr, histList);

        page.getChildren().addAll(title, content, historyCard);
        return wrapScroll(page);
    }

    private void styleBtn(Button b, String color) {
        b.setStyle("-fx-background-color: " + color
                + "; -fx-text-fill: white; -fx-background-radius: 4; -fx-padding: 8 16; -fx-cursor: hand;");
    }
}
