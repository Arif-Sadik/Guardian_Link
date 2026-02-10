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
import util.ThemeManager;

/**
 * Donor dashboard — Figma-matched.
 * Sidebar pages: Dashboard, Sponsorships, Donations, Reports.
 */
public class DonorController {

    private final Stage stage;
    private final User user;
    private BorderPane root;
    private VBox sidebar;
    private String activePage = "dashboard";

    private static final String PRIMARY = ThemeManager.PRIMARY;
    private static final String SECONDARY = ThemeManager.SECONDARY;
    private static final String WARNING = ThemeManager.WARNING;

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

    private static final String DESTRUCTIVE = ThemeManager.DESTRUCTIVE;

    public DonorController(Stage stage, User user) {
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
        stage.setTitle("GuardianLink \u2014 Donor Dashboard");
        stage.show();
    }

    private void refreshTheme() {
        root.setStyle("-fx-background-color: " + BG() + ";");
        root.setTop(buildHeader());
        root.setLeft(buildSidebar());
        // Refresh current page
        switch (activePage) {
            case "dashboard" -> root.setCenter(buildDashboardPage());
            case "sponsorship" -> root.setCenter(buildSponsorshipPage());
            case "donations" -> root.setCenter(buildDonationsPage());
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

        // Gradient circle logo
        StackPane logo = new StackPane();
        logo.setPrefSize(48, 48);
        logo.setStyle(
                "-fx-background-color: linear-gradient(to bottom right, #667eea, #764ba2); -fx-background-radius: 24;");
        Label gl = new Label("GL");
        gl.setFont(Font.font("Segoe UI", FontWeight.BOLD, 16));
        gl.setTextFill(Color.WHITE);
        logo.getChildren().add(gl);

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
        Label uRole = new Label("Donor");
        uRole.setFont(Font.font("Segoe UI", FontWeight.BOLD, 11));
        uRole.setTextFill(Color.web(PRIMARY));
        userInfo.getChildren().addAll(uName, uRole);

        userTypeBox.getChildren().addAll(userIcon, userInfo);

        header.getChildren().addAll(logo, titleBox, spacer, userTypeBox);
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
                sidebarBtn("Sponsorships", "sponsorship"),
                sidebarBtn("Donations", "donations"),
                sidebarBtn("Reports", "reports"));
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
                case "sponsorship" -> root.setCenter(buildSponsorshipPage());
                case "donations" -> root.setCenter(buildDonationsPage());
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
        if (text.contains("Sponsor"))
            return "sponsorship";
        if (text.contains("Donation"))
            return "donations";
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

        Label title = new Label("Donor Dashboard");
        title.setFont(Font.font("Segoe UI", FontWeight.MEDIUM, 20));
        Label sub = new Label("Track your impact and manage sponsorships");
        sub.setFont(Font.font("Segoe UI", 13));
        sub.setTextFill(Color.web(MUTED_FG()));

        HBox stats = new HBox(16);
        stats.getChildren().addAll(
                statCard("Sponsored Children", "3", "Active sponsorships", PRIMARY),
                statCard("Total Donated", "৳456,000", "Since Jan 2024", SECONDARY),
                statCard("This Month", "৳38,000", "+15% from last month", SECONDARY),
                statCard("Impact Score", "92", "Excellent rating", SECONDARY));

        // Sponsored Children cards
        Label scTitle = new Label("Your Sponsored Children");
        scTitle.setFont(Font.font("Segoe UI", FontWeight.MEDIUM, 17));
        HBox childCards = new HBox(16);
        String[][] children = {
                { "TR", "Tahmid Rahman", "10 years", "Dhaka, Bangladesh", "৳48,000" },
                { "MH", "Mugdho Hossain", "8 years", "Chittagong, Bangladesh", "৳66,000" },
                { "SA", "Sokina Akter", "12 years", "Sylhet, Bangladesh", "৳40,000" },
        };
        for (String[] c : children) {
            VBox card = new VBox(12);
            card.setPadding(new Insets(20));
            card.setStyle("-fx-background-color: " + CARD() + "; -fx-border-color: " + BORDER()
                    + "; -fx-border-width: 1; -fx-background-radius: 8; -fx-border-radius: 8; -fx-cursor: hand;");
            HBox.setHgrow(card, Priority.ALWAYS);

            StackPane avatar = new StackPane();
            avatar.setPrefSize(56, 56);
            avatar.setStyle("-fx-background-color: " + PRIMARY + "1A; -fx-background-radius: 28;");
            Label initials = new Label(c[0]);
            initials.setFont(Font.font("Segoe UI", FontWeight.SEMI_BOLD, 18));
            initials.setTextFill(Color.web(PRIMARY));
            avatar.getChildren().add(initials);

            Label name = new Label(c[1]);
            name.setFont(Font.font("Segoe UI", FontWeight.MEDIUM, 14));
            Label age = new Label(c[2] + " \u2022 " + c[3]);
            age.setFont(Font.font("Segoe UI", 11));
            age.setTextFill(Color.web(MUTED_FG()));

            HBox walletRow = new HBox();
            Label wl = new Label("Wallet Balance:");
            wl.setFont(Font.font("Segoe UI", 11));
            wl.setTextFill(Color.web(MUTED_FG()));
            Region sp2 = new Region();
            HBox.setHgrow(sp2, Priority.ALWAYS);
            Label wv = new Label(c[4]);
            wv.setFont(Font.font("Segoe UI", FontWeight.SEMI_BOLD, 14));
            wv.setTextFill(Color.web(SECONDARY));
            walletRow.getChildren().addAll(wl, sp2, wv);

            Button viewBtn = new Button("View Details \u2192");
            viewBtn.setMaxWidth(Double.MAX_VALUE);
            viewBtn.setStyle("-fx-background-color: " + PRIMARY
                    + "; -fx-text-fill: white; -fx-background-radius: 4; -fx-padding: 8 0; -fx-font-size: 12px; -fx-cursor: hand;");
            viewBtn.setOnAction(e -> {
                activePage = "sponsorship";
                refreshSidebar();
                root.setCenter(buildSponsorshipPage());
            });

            card.getChildren().addAll(avatar, name, age, walletRow, viewBtn);
            childCards.getChildren().add(card);
        }

        // Recent Donations table
        VBox donTable = new VBox(0);
        donTable.setStyle("-fx-background-color: " + CARD() + "; -fx-border-color: " + BORDER()
                + "; -fx-border-width: 1; -fx-background-radius: 8; -fx-border-radius: 8;");
        HBox donHdr = new HBox();
        donHdr.setPadding(new Insets(16));
        donHdr.setStyle("-fx-border-color: " + BORDER() + "; -fx-border-width: 0 0 1 0;");
        Label donTitle = new Label("Recent Donations");
        donTitle.setFont(Font.font("Segoe UI", FontWeight.MEDIUM, 17));
        Region dsp = new Region();
        HBox.setHgrow(dsp, Priority.ALWAYS);
        Hyperlink viewAll = new Hyperlink("View All");
        viewAll.setFont(Font.font("Segoe UI", 13));
        viewAll.setTextFill(Color.web(PRIMARY));
        viewAll.setOnAction(e -> {
            activePage = "donations";
            refreshSidebar();
            root.setCenter(buildDonationsPage());
        });
        donHdr.getChildren().addAll(donTitle, dsp, viewAll);

        GridPane grid = new GridPane();
        String[] cols = { "Date", "Child", "Amount", "Purpose", "Status" };
        for (int i = 0; i < cols.length; i++) {
            Label h = new Label(cols[i]);
            h.setFont(Font.font("Segoe UI", FontWeight.SEMI_BOLD, 11));
            h.setPadding(new Insets(8, 16, 8, 16));
            h.setMaxWidth(Double.MAX_VALUE);
            h.setStyle("-fx-background-color: " + MUTED() + ";");
            grid.add(h, i, 0);
        }
        String[][] rows = {
                { "Jan 20, 2026", "Tahmid Rahman", "$150", "Education", "Completed" },
                { "Jan 20, 2026", "Mugdho Hossain", "$200", "Medical", "Completed" },
                { "Jan 15, 2026", "Sokina Akter", "$180", "General Welfare", "Completed" },
                { "Jan 5, 2026", "Tahmid Rahman", "$150", "Food & Nutrition", "Completed" },
        };
        for (int r = 0; r < rows.length; r++) {
            for (int c = 0; c < rows[r].length; c++) {
                if (c == 4) {
                    Label badge = new Label(rows[r][c]);
                    badge.setFont(Font.font("Segoe UI", 11));
                    badge.setStyle("-fx-background-color: " + SECONDARY + "1A; -fx-text-fill: " + SECONDARY
                            + "; -fx-background-radius: 4; -fx-padding: 2 8;");
                    HBox w = new HBox(badge);
                    w.setPadding(new Insets(10, 16, 10, 16));
                    w.setStyle("-fx-border-color: " + BORDER() + "; -fx-border-width: 0 0 1 0;");
                    grid.add(w, c, r + 1);
                    continue;
                }
                Label cell = new Label(rows[r][c]);
                cell.setFont(Font.font("Segoe UI", 13));
                if (c == 0) {
                    cell.setFont(Font.font("Segoe UI", 12));
                    cell.setTextFill(Color.web(MUTED_FG()));
                }
                if (c == 2) {
                    cell.setFont(Font.font("Segoe UI", FontWeight.SEMI_BOLD, 13));
                    cell.setTextFill(Color.web(SECONDARY));
                }
                cell.setPadding(new Insets(10, 16, 10, 16));
                cell.setStyle("-fx-border-color: " + BORDER() + "; -fx-border-width: 0 0 1 0;");
                grid.add(cell, c, r + 1);
            }
        }
        donTable.getChildren().addAll(donHdr, grid);

        page.getChildren().addAll(new VBox(4, title, sub), stats, scTitle, childCards, donTable);
        return wrapScroll(page);
    }

    // ═══════════ SPONSORSHIP PAGE ═══════════
    private ScrollPane buildSponsorshipPage() {
        VBox page = new VBox(20);
        page.setPadding(new Insets(24));

        Label title = new Label("Sponsorship & Digital Wallet Management");
        title.setFont(Font.font("Segoe UI", FontWeight.MEDIUM, 20));
        Label sub = new Label("Track donations and fund utilization");
        sub.setFont(Font.font("Segoe UI", 13));
        sub.setTextFill(Color.web(MUTED_FG()));

        // Child selector
        VBox selBox = new VBox(8);
        Label selLabel = new Label("Select Child");
        selLabel.setFont(Font.font("Segoe UI", FontWeight.MEDIUM, 13));
        ComboBox<String> selCombo = new ComboBox<>();
        selCombo.getItems().addAll("Tahmid Rahman (CH-1024)", "Mugdho Hossain (CH-1025)", "Sokina Akter (CH-1026)");
        selCombo.setValue("Tahmid Rahman (CH-1024)");
        selCombo.setPrefWidth(320);
        selBox.getChildren().addAll(selLabel, selCombo);

        HBox stats = new HBox(16);
        stats.getChildren().addAll(
                statCard("Current Balance", "$450", "Available funds", SECONDARY),
                statCard("Total Received", "$2,850", "All time", MUTED_FG()),
                statCard("Total Spent", "$2,400", "All time", MUTED_FG()),
                statCard("Active Sponsor", "You", "Since Jan 2024", MUTED_FG()));

        // Fund Utilization
        VBox fundCard = new VBox(16);
        fundCard.setPadding(new Insets(16));
        fundCard.setStyle("-fx-background-color: " + CARD() + "; -fx-border-color: " + BORDER()
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

        // Make Donation button
        Button makeDon = new Button("\uD83D\uDCB2  Make a Donation");
        makeDon.setStyle("-fx-background-color: " + SECONDARY
                + "; -fx-text-fill: white; -fx-background-radius: 4; -fx-padding: 10 24; -fx-font-size: 14px; -fx-cursor: hand;");

        page.getChildren().addAll(new VBox(4, title, sub), selBox, stats, fundCard, makeDon);
        return wrapScroll(page);
    }

    // ═══════════ DONATIONS PAGE ═══════════
    private ScrollPane buildDonationsPage() {
        VBox page = new VBox(20);
        page.setPadding(new Insets(24));

        Label title = new Label("Donation History");
        title.setFont(Font.font("Segoe UI", FontWeight.MEDIUM, 20));
        Label sub = new Label("View all your donations and transactions");
        sub.setFont(Font.font("Segoe UI", 13));
        sub.setTextFill(Color.web(MUTED_FG()));

        HBox stats = new HBox(16);
        stats.getChildren().addAll(
                statCard("Total Donated", "$4,250", "All time", SECONDARY),
                statCard("This Month", "$350", "+15% from last month", SECONDARY),
                statCard("Transactions", "28", "Total donations", MUTED_FG()),
                statCard("Children Helped", "3", "Active sponsorships", PRIMARY));

        // Transaction table
        VBox tableCard = new VBox(0);
        tableCard.setStyle("-fx-background-color: " + CARD() + "; -fx-border-color: " + BORDER()
                + "; -fx-border-width: 1; -fx-background-radius: 8; -fx-border-radius: 8;");
        HBox hdr = new HBox();
        hdr.setPadding(new Insets(16));
        hdr.setAlignment(Pos.CENTER_LEFT);
        hdr.setStyle("-fx-border-color: " + BORDER() + "; -fx-border-width: 0 0 1 0;");
        Label tl = new Label("All Transactions");
        tl.setFont(Font.font("Segoe UI", FontWeight.MEDIUM, 17));
        Region sp1 = new Region();
        HBox.setHgrow(sp1, Priority.ALWAYS);
        Button exportBtn = new Button("Export to CSV");
        exportBtn.setStyle("-fx-background-color: " + SECONDARY
                + "; -fx-text-fill: white; -fx-background-radius: 4; -fx-padding: 6 16; -fx-font-size: 12px; -fx-cursor: hand;");
        hdr.getChildren().addAll(tl, sp1, exportBtn);

        GridPane grid = new GridPane();
        String[] cols = { "Date", "Child", "Amount", "Purpose", "Status" };
        for (int i = 0; i < cols.length; i++) {
            Label h = new Label(cols[i]);
            h.setFont(Font.font("Segoe UI", FontWeight.SEMI_BOLD, 11));
            h.setPadding(new Insets(8, 16, 8, 16));
            h.setMaxWidth(Double.MAX_VALUE);
            h.setStyle("-fx-background-color: " + MUTED() + ";");
            grid.add(h, i, 0);
        }
        String[][] rows = {
                { "Jan 20, 2026", "Tahmid Rahman", "$150", "Education", "Completed" },
                { "Jan 20, 2026", "Mugdho Hossain", "$200", "Medical", "Completed" },
                { "Jan 15, 2026", "Sokina Akter", "$180", "General Welfare", "Completed" },
                { "Jan 5, 2026", "Tahmid Rahman", "$150", "Food & Nutrition", "Completed" },
                { "Dec 20, 2025", "Mugdho Hossain", "$150", "Education", "Completed" },
                { "Dec 5, 2025", "Sokina Akter", "$150", "Clothing", "Completed" },
                { "Nov 20, 2025", "Tahmid Rahman", "$150", "Education", "Completed" },
                { "Nov 5, 2025", "Mugdho Hossain", "$150", "Medical", "Completed" },
        };
        for (int r = 0; r < rows.length; r++) {
            for (int c = 0; c < rows[r].length; c++) {
                if (c == 4) {
                    Label badge = new Label(rows[r][c]);
                    badge.setFont(Font.font("Segoe UI", 11));
                    badge.setStyle("-fx-background-color: " + SECONDARY + "1A; -fx-text-fill: " + SECONDARY
                            + "; -fx-background-radius: 4; -fx-padding: 2 8;");
                    HBox w = new HBox(badge);
                    w.setPadding(new Insets(10, 16, 10, 16));
                    w.setStyle("-fx-border-color: " + BORDER() + "; -fx-border-width: 0 0 1 0;");
                    grid.add(w, c, r + 1);
                    continue;
                }
                Label cell = new Label(rows[r][c]);
                cell.setFont(Font.font("Segoe UI", 13));
                if (c == 0) {
                    cell.setFont(Font.font("Segoe UI", 12));
                    cell.setTextFill(Color.web(MUTED_FG()));
                }
                if (c == 2) {
                    cell.setFont(Font.font("Segoe UI", FontWeight.SEMI_BOLD, 13));
                    cell.setTextFill(Color.web(SECONDARY));
                }
                cell.setPadding(new Insets(10, 16, 10, 16));
                cell.setStyle("-fx-border-color: " + BORDER() + "; -fx-border-width: 0 0 1 0;");
                grid.add(cell, c, r + 1);
            }
        }
        tableCard.getChildren().addAll(hdr, grid);

        // Make Donation
        Button makeDon = new Button("\uD83D\uDCB2  Make a New Donation");
        makeDon.setStyle("-fx-background-color: " + SECONDARY
                + "; -fx-text-fill: white; -fx-background-radius: 4; -fx-padding: 10 24; -fx-font-size: 14px; -fx-cursor: hand;");

        page.getChildren().addAll(new VBox(4, title, sub), stats, tableCard, makeDon);
        return wrapScroll(page);
    }

    // ═══════════ REPORTS PAGE ═══════════
    private ScrollPane buildReportsPage() {
        VBox page = new VBox(20);
        page.setPadding(new Insets(24));

        Label title = new Label("Reports");
        title.setFont(Font.font("Segoe UI", FontWeight.MEDIUM, 20));
        Label sub = new Label("View donation reports and impact analysis");
        sub.setFont(Font.font("Segoe UI", 13));
        sub.setTextFill(Color.web(MUTED_FG()));

        VBox genCard = new VBox(16);
        genCard.setPadding(new Insets(16));
        genCard.setStyle("-fx-background-color: " + CARD() + "; -fx-border-color: " + BORDER()
                + "; -fx-border-width: 1; -fx-background-radius: 8; -fx-border-radius: 8;");
        Label genTitle = new Label("Report Generator");
        genTitle.setFont(Font.font("Segoe UI", FontWeight.MEDIUM, 17));

        HBox row1 = new HBox(24);
        VBox col1 = new VBox(8);
        Label rtLabel = new Label("Report Type");
        rtLabel.setFont(Font.font("Segoe UI", FontWeight.MEDIUM, 13));
        ComboBox<String> rtCombo = new ComboBox<>();
        rtCombo.getItems().addAll("Donation Summary", "Impact Report", "Tax Receipt");
        rtCombo.setValue("Donation Summary");
        rtCombo.setMaxWidth(Double.MAX_VALUE);
        col1.getChildren().addAll(rtLabel, rtCombo);
        HBox.setHgrow(col1, Priority.ALWAYS);

        VBox col2 = new VBox(8);
        Label drLabel = new Label("Date Range");
        drLabel.setFont(Font.font("Segoe UI", FontWeight.MEDIUM, 13));
        ComboBox<String> drCombo = new ComboBox<>();
        drCombo.getItems().addAll("Last 30 Days", "Last 3 Months", "Last Year", "All Time");
        drCombo.setValue("Last 30 Days");
        drCombo.setMaxWidth(Double.MAX_VALUE);
        col2.getChildren().addAll(drLabel, drCombo);
        HBox.setHgrow(col2, Priority.ALWAYS);
        row1.getChildren().addAll(col1, col2);

        HBox buttons = new HBox(12);
        Button genBtn = new Button("Generate Report");
        genBtn.setStyle("-fx-background-color: " + PRIMARY
                + "; -fx-text-fill: white; -fx-background-radius: 4; -fx-padding: 8 16; -fx-font-size: 13px; -fx-cursor: hand;");
        Button pdfBtn = new Button("Export to PDF");
        pdfBtn.setStyle("-fx-background-color: " + MUTED()
                + "; -fx-text-fill: #1a1a1a; -fx-background-radius: 4; -fx-padding: 8 16; -fx-font-size: 13px; -fx-cursor: hand; -fx-border-color: "
                + BORDER() + "; -fx-border-width: 1; -fx-border-radius: 4;");
        buttons.getChildren().addAll(genBtn, pdfBtn);

        genCard.getChildren().addAll(genTitle, row1, buttons);

        HBox qStats = new HBox(16);
        qStats.getChildren().addAll(
                statCard("Reports Generated", "12", "All time", MUTED_FG()),
                statCard("Last Generated", "Jan 20", "Donation Summary", MUTED_FG()),
                statCard("Tax Receipts", "3", "Available for download", PRIMARY),
                statCard("Impact Score", "92", "Excellent", SECONDARY));

        page.getChildren().addAll(new VBox(4, title, sub), genCard, qStats);
        return wrapScroll(page);
    }
}
