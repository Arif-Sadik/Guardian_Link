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
 * Organization Admin (Caregiver) dashboard — Figma-matched.
 * Sidebar pages: Dashboard, Child Profiles, Sponsorships, Alerts, Reports.
 */
public class OrgAdminController {

    private final Stage stage;
    private final User user;
    private BorderPane root;
    private VBox sidebar;
    private String activePage = "dashboard";

    private static final String PRIMARY = "#2563eb";
    private static final String SECONDARY = "#16a34a";
    private static final String WARNING = "#f59e0b";
    private static final String DESTRUCTIVE = "#dc2626";
    private static final String BG = "#f8f9fa";
    private static final String CARD = "#ffffff";
    private static final String BORDER = "#e2e8f0";
    private static final String MUTED = "#f1f5f9";
    private static final String MUTED_FG = "#64748b";

    public OrgAdminController(Stage stage, User user) {
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
        stage.setTitle("GuardianLink \u2014 Organization Admin");
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
        Label uRole = new Label("Organization Admin");
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
                sidebarBtn("\u2764  Sponsorships", "sponsorship"),
                sidebarBtn("\uD83D\uDD14  Alerts", "alerts"),
                sidebarBtn("\uD83D\uDCC4  Reports & Audit", "reports"));
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
                    "-fx-background-color: transparent; -fx-text-fill: #1a1a1a; -fx-background-radius: 4; -fx-padding: 8 12; -fx-cursor: hand; -fx-font-size: 13px;");
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
        sp.setStyle("-fx-background: " + BG + "; -fx-background-color: " + BG + ";");
        return sp;
    }

    private VBox statCard(String label, String value, String detail, String detailColor) {
        VBox card = new VBox(4);
        card.setPadding(new Insets(16));
        card.setStyle("-fx-background-color: " + CARD + "; -fx-border-color: " + BORDER
                + "; -fx-border-width: 1; -fx-background-radius: 8; -fx-border-radius: 8;");
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

    // ═══════════ DASHBOARD PAGE ═══════════
    private ScrollPane buildDashboardPage() {
        VBox page = new VBox(20);
        page.setPadding(new Insets(24));

        Label title = new Label("Organization Dashboard");
        title.setFont(Font.font("Segoe UI", FontWeight.MEDIUM, 20));
        Label sub = new Label("Child welfare monitoring and case management");
        sub.setFont(Font.font("Segoe UI", 13));
        sub.setTextFill(Color.web(MUTED_FG));

        HBox stats = new HBox(16);
        stats.getChildren().addAll(
                statCard("Total Children", "248", "+12 this month", SECONDARY),
                statCard("Active Cases", "45", "Currently monitored", PRIMARY),
                statCard("Pending Alerts", "8", "Requires attention", WARNING),
                statCard("Appointments", "12", "This week", MUTED_FG));

        // Quick Actions
        Label qaTitle = new Label("Quick Actions");
        qaTitle.setFont(Font.font("Segoe UI", FontWeight.MEDIUM, 17));
        HBox qaCards = new HBox(12);
        String[][] qas = {
                { "\uD83D\uDC64", "Add Child Profile", "Register new child" },
                { "\uD83D\uDD14", "View Alerts", "Check pending alerts" },
                { "\uD83D\uDCC4", "Generate Report", "Create welfare report" }
        };
        for (String[] qa : qas) {
            VBox card = new VBox(4);
            card.setPadding(new Insets(16));
            card.setStyle("-fx-background-color: " + CARD + "; -fx-border-color: " + BORDER
                    + "; -fx-border-width: 1; -fx-background-radius: 8; -fx-border-radius: 8; -fx-cursor: hand;");
            HBox.setHgrow(card, Priority.ALWAYS);
            Label ic = new Label(qa[0]);
            ic.setFont(Font.font("Segoe UI Emoji", 18));
            Label t = new Label(qa[1]);
            t.setFont(Font.font("Segoe UI", FontWeight.MEDIUM, 13));
            Label d = new Label(qa[2]);
            d.setFont(Font.font("Segoe UI", 11));
            d.setTextFill(Color.web(MUTED_FG));
            card.getChildren().addAll(ic, t, d);
            qaCards.getChildren().add(card);
        }

        // Assigned Children table
        VBox tableCard = buildChildrenTable("Assigned Children", new String[][] {
                { "CH-1024", "Tahmid Rahman", "10", "Active", "Jan 25, 2026" },
                { "CH-1025", "Mugdho Hossain", "8", "Active", "Jan 24, 2026" },
                { "CH-1026", "Sokina Akter", "12", "Active", "Jan 23, 2026" },
                { "CH-1027", "Nabil Khan", "9", "Pending Review", "Jan 22, 2026" },
                { "CH-1028", "Faiza Ahmed", "11", "Active", "Jan 21, 2026" },
        });

        page.getChildren().addAll(new VBox(4, title, sub), stats, qaTitle, qaCards, tableCard);
        return wrapScroll(page);
    }

    private VBox buildChildrenTable(String heading, String[][] rows) {
        VBox card = new VBox(0);
        card.setStyle("-fx-background-color: " + CARD + "; -fx-border-color: " + BORDER
                + "; -fx-border-width: 1; -fx-background-radius: 8; -fx-border-radius: 8;");

        HBox hdr = new HBox();
        hdr.setPadding(new Insets(16));
        hdr.setStyle("-fx-border-color: " + BORDER + "; -fx-border-width: 0 0 1 0;");
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
            h.setStyle("-fx-background-color: " + MUTED + ";");
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
                    w.setStyle("-fx-border-color: " + BORDER + "; -fx-border-width: 0 0 1 0;");
                    grid.add(w, c, r + 1);
                    continue;
                }
                Label cell = new Label(rows[r][c]);
                cell.setFont(Font.font(c == 0 ? "Consolas" : "Segoe UI", c == 0 ? 12 : 13));
                if (c == 1)
                    cell.setFont(Font.font("Segoe UI", FontWeight.MEDIUM, 13));
                if (c == 4)
                    cell.setTextFill(Color.web(MUTED_FG));
                cell.setPadding(new Insets(12, 16, 12, 16));
                cell.setStyle("-fx-border-color: " + BORDER + "; -fx-border-width: 0 0 1 0;");
                grid.add(cell, c, r + 1);
            }
            Hyperlink link = new Hyperlink("View Profile");
            link.setFont(Font.font("Segoe UI", 13));
            link.setTextFill(Color.web(PRIMARY));
            link.setStyle("-fx-border-color: " + BORDER + "; -fx-border-width: 0 0 1 0; -fx-padding: 12 16;");
            grid.add(link, 5, r + 1);
        }
        card.getChildren().addAll(hdr, grid);
        return card;
    }

    // ═══════════ CHILD PROFILES PAGE (with tabs) ═══════════
    private ScrollPane buildChildrenPage() {
        VBox page = new VBox(20);
        page.setPadding(new Insets(24));

        Label title = new Label("Child Profile Management");
        title.setFont(Font.font("Segoe UI", FontWeight.MEDIUM, 20));
        Label sub = new Label("View and update child information");
        sub.setFont(Font.font("Segoe UI", 13));
        sub.setTextFill(Color.web(MUTED_FG));

        // Profile header
        VBox profileHeader = new VBox(8);
        profileHeader.setPadding(new Insets(24));
        profileHeader.setStyle("-fx-background-color: " + CARD + "; -fx-border-color: " + BORDER
                + "; -fx-border-width: 1; -fx-background-radius: 8; -fx-border-radius: 8;");

        HBox phRow = new HBox(16);
        phRow.setAlignment(Pos.CENTER_LEFT);
        StackPane avatar = new StackPane();
        avatar.setPrefSize(80, 80);
        avatar.setStyle("-fx-background-color: " + PRIMARY + "1A; -fx-background-radius: 40;");
        Label initials = new Label("TR");
        initials.setFont(Font.font("Segoe UI", FontWeight.SEMI_BOLD, 24));
        initials.setTextFill(Color.web(PRIMARY));
        avatar.getChildren().add(initials);

        VBox info = new VBox(4);
        HBox.setHgrow(info, Priority.ALWAYS);
        Label name = new Label("Tahmid Rahman");
        name.setFont(Font.font("Segoe UI", FontWeight.SEMI_BOLD, 18));
        Label cid = new Label("Child ID: CH-1024");
        cid.setFont(Font.font("Segoe UI", 12));
        cid.setTextFill(Color.web(MUTED_FG));
        HBox meta = new HBox(16);
        meta.getChildren().addAll(
                metaLabel("Age:", "10 years"),
                metaLabel("Location:", "Dhaka, Bangladesh"),
                statusBadge("Active"));
        info.getChildren().addAll(name, cid, meta);

        Button saveBtn = new Button("\uD83D\uDCBE  Save Changes");
        saveBtn.setStyle("-fx-background-color: " + PRIMARY
                + "; -fx-text-fill: white; -fx-background-radius: 4; -fx-padding: 8 16; -fx-font-size: 13px; -fx-cursor: hand;");

        phRow.getChildren().addAll(avatar, info, saveBtn);
        profileHeader.getChildren().add(phRow);

        // Tab pane
        TabPane tabs = new TabPane();
        tabs.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);
        tabs.setStyle("-fx-background-color: " + CARD + ";");

        tabs.getTabs().addAll(
                makeTab("Personal Information", buildPersonalTab()),
                makeTab("Medical Records", buildMedicalTab()),
                makeTab("Education Records", buildEducationTab()),
                makeTab("Welfare & Case History", buildWelfareTab()));

        page.getChildren().addAll(new VBox(4, title, sub), profileHeader, tabs);
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
        l.setTextFill(Color.web(MUTED_FG));
        Label v = new Label(value);
        v.setFont(Font.font("Segoe UI", FontWeight.MEDIUM, 12));
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
        HBox r1 = new HBox(24);
        r1.getChildren().addAll(formField("Full Name", "Tahmid Rahman"), formField("Date of Birth", "2015-03-15"));
        HBox r2 = new HBox(24);
        r2.getChildren().addAll(formField("Gender", "Male"), formField("Nationality", "Bangladeshi"));
        VBox addr = formFieldArea("Address", "Motijheel, Dhaka-1000, Bangladesh");
        HBox r3 = new HBox(24);
        r3.getChildren().addAll(formField("Guardian Name", "Kamal Rahman"),
                formField("Guardian Contact", "+880 1712-345678"));
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

        GridPane grid = new GridPane();
        String[] cols = { "Date", "Type", "Description", "Provider" };
        for (int i = 0; i < cols.length; i++) {
            Label h = new Label(cols[i]);
            h.setFont(Font.font("Segoe UI", FontWeight.SEMI_BOLD, 11));
            h.setPadding(new Insets(8, 12, 8, 12));
            h.setStyle("-fx-background-color: " + MUTED + ";");
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
                cl.setPadding(new Insets(8, 12, 8, 12));
                cl.setStyle("-fx-border-color: " + BORDER + "; -fx-border-width: 0 0 1 0;");
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

        GridPane grid = new GridPane();
        String[] cols = { "Subject", "Term 1", "Term 2", "Term 3", "Average" };
        for (int i = 0; i < cols.length; i++) {
            Label h = new Label(cols[i]);
            h.setFont(Font.font("Segoe UI", FontWeight.SEMI_BOLD, 11));
            h.setPadding(new Insets(8, 12, 8, 12));
            h.setStyle("-fx-background-color: " + MUTED + ";");
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
                cl.setStyle("-fx-border-color: " + BORDER + "; -fx-border-width: 0 0 1 0;");
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
            entry.setStyle("-fx-background-color: " + CARD + "; -fx-border-color: " + BORDER
                    + "; -fx-border-width: 1; -fx-background-radius: 8; -fx-border-radius: 8;");
            HBox top = new HBox();
            VBox left = new VBox(2);
            Label type = new Label(c[1]);
            type.setFont(Font.font("Segoe UI", FontWeight.MEDIUM, 14));
            Label date = new Label(c[0]);
            date.setFont(Font.font("Segoe UI", 11));
            date.setTextFill(Color.web(MUTED_FG));
            left.getChildren().addAll(type, date);
            Region sp = new Region();
            HBox.setHgrow(sp, Priority.ALWAYS);
            Label by = new Label("By: " + c[3]);
            by.setFont(Font.font("Segoe UI", 11));
            by.setTextFill(Color.web(MUTED_FG));
            top.getChildren().addAll(left, sp, by);
            Label desc = new Label(c[2]);
            desc.setFont(Font.font("Segoe UI", 13));
            desc.setTextFill(Color.web(MUTED_FG));
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

    // ═══════════ SPONSORSHIP PAGE ═══════════
    private ScrollPane buildSponsorshipPage() {
        VBox page = new VBox(20);
        page.setPadding(new Insets(24));

        Label title = new Label("Sponsorship & Digital Wallet Management");
        title.setFont(Font.font("Segoe UI", FontWeight.MEDIUM, 20));
        Label sub = new Label("Track donations and fund utilization");
        sub.setFont(Font.font("Segoe UI", 13));
        sub.setTextFill(Color.web(MUTED_FG));

        HBox stats = new HBox(16);
        stats.getChildren().addAll(
                statCard("Current Balance", "$450", "Available funds", SECONDARY),
                statCard("Total Received", "$2,850", "All time", MUTED_FG),
                statCard("Total Spent", "$2,400", "All time", MUTED_FG),
                statCard("Active Sponsor", "Emily Johnson", "Since Jan 2024", MUTED_FG));

        // Fund Utilization
        VBox fundCard = new VBox(16);
        fundCard.setPadding(new Insets(16));
        fundCard.setStyle("-fx-background-color: " + CARD + "; -fx-border-color: " + BORDER
                + "; -fx-border-width: 1; -fx-background-radius: 8; -fx-border-radius: 8;");
        Label fundTitle = new Label("Fund Utilization by Category");
        fundTitle.setFont(Font.font("Segoe UI", FontWeight.MEDIUM, 17));
        fundCard.getChildren().add(fundTitle);

        String[][] funds = {
                { "Education", "$1,200", "50", "#2563eb" },
                { "Medical Care", "$600", "25", "#16a34a" },
                { "Food & Nutrition", "$360", "15", "#f59e0b" },
                { "Clothing", "$180", "7.5", "#8b5cf6" },
                { "Other", "$60", "2.5", "#ec4899" },
        };
        for (String[] f : funds) {
            VBox row = new VBox(4);
            HBox labels = new HBox();
            Label cat = new Label(f[0]);
            cat.setFont(Font.font("Segoe UI", 13));
            Region sp = new Region();
            HBox.setHgrow(sp, Priority.ALWAYS);
            Label amt = new Label(f[1] + " (" + f[2] + "%)");
            amt.setFont(Font.font("Segoe UI", FontWeight.SEMI_BOLD, 13));
            labels.getChildren().addAll(cat, sp, amt);

            StackPane barBg = new StackPane();
            barBg.setPrefHeight(8);
            barBg.setMaxHeight(8);
            barBg.setStyle("-fx-background-color: " + MUTED + "; -fx-background-radius: 4;");
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

        // Action buttons
        HBox actions = new HBox(12);
        Button recDon = new Button("\uD83D\uDCB2  Record Donation");
        recDon.setStyle("-fx-background-color: " + SECONDARY
                + "; -fx-text-fill: white; -fx-background-radius: 4; -fx-padding: 8 16; -fx-font-size: 13px; -fx-cursor: hand;");
        Button recExp = new Button("\uD83D\uDCC9  Record Expense");
        recExp.setStyle("-fx-background-color: " + PRIMARY
                + "; -fx-text-fill: white; -fx-background-radius: 4; -fx-padding: 8 16; -fx-font-size: 13px; -fx-cursor: hand;");
        actions.getChildren().addAll(recDon, recExp);

        page.getChildren().addAll(new VBox(4, title, sub), stats, fundCard, actions);
        return wrapScroll(page);
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
                statCard("Total Active", "5", "Requires attention", WARNING),
                statCard("Critical", "2", "Immediate action needed", DESTRUCTIVE),
                statCard("Warnings", "2", "Review soon", WARNING),
                statCard("Resolved Today", "2", "Completed", SECONDARY));

        Label activeTitle = new Label("Active Alerts");
        activeTitle.setFont(Font.font("Segoe UI", FontWeight.MEDIUM, 17));

        VBox alertsList = new VBox(12);
        String[][] alerts = {
                { "critical", "Low Wallet Balance", "ALT-001", "2 hours ago",
                        "Child CH-1024 has wallet balance below threshold" },
                { "warning", "Missed Medical Appointment", "ALT-003", "1 day ago",
                        "Child CH-1024 missed scheduled checkup" },
                { "info", "New Donor Registration", "ALT-005", "2 days ago", "New donor pending verification" },
        };
        for (String[] a : alerts) {
            String borderC = a[0].equals("critical") ? DESTRUCTIVE : BORDER;
            String bgC = a[0].equals("critical") ? DESTRUCTIVE + "1A"
                    : a[0].equals("warning") ? WARNING + "1A" : PRIMARY + "1A";
            String iconC = a[0].equals("critical") ? DESTRUCTIVE : a[0].equals("warning") ? WARNING : PRIMARY;

            VBox card = new VBox(8);
            card.setPadding(new Insets(16));
            card.setStyle("-fx-background-color: " + CARD + "; -fx-border-color: " + borderC
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
            Label aDesc = new Label(a[4]);
            aDesc.setFont(Font.font("Segoe UI", 13));
            aDesc.setTextFill(Color.web(MUTED_FG));
            HBox btns = new HBox(8);
            Button vd = new Button("View Details");
            vd.setStyle("-fx-background-color: " + PRIMARY
                    + "; -fx-text-fill: white; -fx-background-radius: 4; -fx-padding: 6 12; -fx-font-size: 12px; -fx-cursor: hand;");
            Button mr = new Button("Mark Resolved");
            mr.setStyle("-fx-background-color: " + SECONDARY
                    + "; -fx-text-fill: white; -fx-background-radius: 4; -fx-padding: 6 12; -fx-font-size: 12px; -fx-cursor: hand;");
            btns.getChildren().addAll(vd, mr);
            info2.getChildren().addAll(aTitle, aDesc, btns);

            Label time = new Label(a[3]);
            time.setFont(Font.font("Segoe UI", 11));
            time.setTextFill(Color.web(MUTED_FG));
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
        Label sub = new Label("Generate welfare reports and view audit trails");
        sub.setFont(Font.font("Segoe UI", 13));
        sub.setTextFill(Color.web(MUTED_FG));

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
        rtCombo.getItems().addAll("Child Welfare Summary", "Donation & Financial Report");
        rtCombo.setValue("Child Welfare Summary");
        rtCombo.setMaxWidth(Double.MAX_VALUE);
        col1.getChildren().addAll(rtLabel, rtCombo);
        HBox.setHgrow(col1, Priority.ALWAYS);

        VBox col2 = new VBox(8);
        Label drLabel = new Label("Date Range");
        drLabel.setFont(Font.font("Segoe UI", FontWeight.MEDIUM, 13));
        ComboBox<String> drCombo = new ComboBox<>();
        drCombo.getItems().addAll("Last 7 Days", "Last 30 Days", "Last 3 Months");
        drCombo.setValue("Last 30 Days");
        drCombo.setMaxWidth(Double.MAX_VALUE);
        col2.getChildren().addAll(drLabel, drCombo);
        HBox.setHgrow(col2, Priority.ALWAYS);
        row1.getChildren().addAll(col1, col2);

        HBox buttons = new HBox(12);
        Button genBtn = new Button("Generate Report");
        genBtn.setStyle("-fx-background-color: " + PRIMARY
                + "; -fx-text-fill: white; -fx-background-radius: 4; -fx-padding: 8 16; -fx-font-size: 13px; -fx-cursor: hand;");
        Button csvBtn = new Button("Export to CSV");
        csvBtn.setStyle("-fx-background-color: " + SECONDARY
                + "; -fx-text-fill: white; -fx-background-radius: 4; -fx-padding: 8 16; -fx-font-size: 13px; -fx-cursor: hand;");
        buttons.getChildren().addAll(genBtn, csvBtn);

        genCard.getChildren().addAll(genTitle, row1, buttons);
        page.getChildren().addAll(new VBox(4, title, sub), genCard);
        return wrapScroll(page);
    }

    // ── Form helpers ────────────────
    private VBox formField(String label, String value) {
        VBox box = new VBox(8);
        HBox.setHgrow(box, Priority.ALWAYS);
        Label l = new Label(label);
        l.setFont(Font.font("Segoe UI", FontWeight.MEDIUM, 13));
        TextField f = new TextField(value);
        f.setStyle("-fx-background-color: " + CARD + "; -fx-border-color: " + BORDER
                + "; -fx-border-width: 1; -fx-border-radius: 4; -fx-background-radius: 4; -fx-padding: 8 12; -fx-font-size: 13px;");
        box.getChildren().addAll(l, f);
        return box;
    }

    private VBox formFieldArea(String label, String value) {
        VBox box = new VBox(8);
        Label l = new Label(label);
        l.setFont(Font.font("Segoe UI", FontWeight.MEDIUM, 13));
        TextArea a = new TextArea(value);
        a.setPrefRowCount(3);
        a.setStyle("-fx-background-color: " + CARD + "; -fx-border-color: " + BORDER
                + "; -fx-border-width: 1; -fx-border-radius: 4; -fx-background-radius: 4; -fx-padding: 8 12; -fx-font-size: 13px;");
        box.getChildren().addAll(l, a);
        return box;
    }

    private VBox vitalCard(String label, String value) {
        VBox box = new VBox(4);
        box.setPadding(new Insets(12));
        box.setStyle("-fx-background-color: " + MUTED + "; -fx-background-radius: 8;");
        HBox.setHgrow(box, Priority.ALWAYS);
        Label l = new Label(label);
        l.setFont(Font.font("Segoe UI", 11));
        l.setTextFill(Color.web(MUTED_FG));
        Label v = new Label(value);
        v.setFont(Font.font("Segoe UI", FontWeight.SEMI_BOLD, 18));
        v.setTextFill(Color.web(SECONDARY));
        box.getChildren().addAll(l, v);
        return box;
    }
}
