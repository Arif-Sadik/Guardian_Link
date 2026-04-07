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
import model.entity.Donation;
import model.entity.SystemLog;
import model.user.User;
import service.ChildService;
import service.DonationService;
import service.SystemLogService;
import service.MedicalRecordService;
import service.EducationRecordService;
import model.entity.MedicalRecord;
import model.entity.EducationRecord;
import util.ThemeManager;
import javafx.stage.FileChooser;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import util.FAQs;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import service.UserService;

/**
 * Donor dashboard — Figma-matched.
 * Sidebar pages: Dashboard, Sponsorships, Donations, Reports.
 */
public class DonorController {

    private final Stage stage;
    private final User user;
    private BorderPane root;
    private Scene scene;
    private VBox sidebar;
    private String activePage = "dashboard";

    private final ChildService childService = new ChildService();
    private final DonationService donationService = new DonationService();
    private final SystemLogService systemLogService = new SystemLogService();
    private final MedicalRecordService medicalRecordService = new MedicalRecordService();
    private final EducationRecordService educationRecordService = new EducationRecordService();

    private static final String PRIMARY = ThemeManager.PRIMARY;
    private static final String SECONDARY = ThemeManager.SECONDARY;
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
        scene = new Scene(root, 1280, 800);
        
        // Apply dark mode styles for DatePicker visibility
        applyDarkModeStylesheet(scene);
        
        stage.setScene(scene);
        stage.setTitle("GuardianLink \u2014 Donor Dashboard");
        stage.show();
    }

    private void refreshTheme() {
        root.setStyle("-fx-background-color: " + BG() + ";");
        
        // Manage stylesheets based on theme
        if (scene != null) {
            applyDarkModeStylesheet(scene);
        }
        
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

        // GuarduanLink Shield Logo
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
        Label uRole = new Label("Donor");
        uRole.setFont(Font.font("Segoe UI", FontWeight.BOLD, 11));
        uRole.setTextFill(Color.web(PRIMARY));
        userInfo.getChildren().addAll(uName, uRole);

        userTypeBox.getChildren().addAll(userIcon, userInfo);
        
        // Make user box clickable to show profile
        userTypeBox.setStyle(userTypeBox.getStyle() + "; -fx-cursor: hand;");
        userTypeBox.setOnMouseClicked(e -> root.setCenter(buildProfilePage()));

        header.getChildren().addAll(logoIcon, titleBox, spacer, userTypeBox);
        return header;
    }
    
    // ═══════════ PROFILE PAGE ═══════════
    private ScrollPane buildProfilePage() {
        VBox page = new VBox(20);
        page.setPadding(new Insets(32));
        page.setMaxWidth(600);
        
        Button backBtn = new Button("\u2190 Back");
        backBtn.setStyle("-fx-background-color: transparent; -fx-text-fill: " + PRIMARY + "; -fx-cursor: hand; -fx-font-size: 13px;");
        backBtn.setOnAction(e -> root.setCenter(buildDashboardPage()));
        
        VBox card = new VBox(0);
        card.setStyle("-fx-background-color: " + CARD() + "; -fx-border-color: " + BORDER() + "; -fx-border-width: 1; -fx-background-radius: 8; -fx-border-radius: 8;");
        
        // Profile header
        HBox profileHeader = new HBox(16);
        profileHeader.setPadding(new Insets(24));
        profileHeader.setStyle("-fx-border-color: " + BORDER() + "; -fx-border-width: 0 0 1 0;");
        profileHeader.setAlignment(Pos.CENTER_LEFT);
        
        Node profileIcon;
        if (user.getProfilePhoto() != null && !user.getProfilePhoto().isEmpty()) {
            javafx.scene.shape.Circle clip = new javafx.scene.shape.Circle(30);
            javafx.scene.image.ImageView photoView = new javafx.scene.image.ImageView();
            photoView.setFitWidth(60);
            photoView.setFitHeight(60);
            photoView.setClip(clip);
            clip.setCenterX(30);
            clip.setCenterY(30);
            try {
                photoView.setImage(new javafx.scene.image.Image(new java.io.File(user.getProfilePhoto()).toURI().toString()));
            } catch (Exception e) {}
            profileIcon = photoView;
        } else {
            Label icon = new Label("👤");
            icon.setFont(Font.font("Segoe UI Emoji", 48));
            profileIcon = icon;
        }
        
        VBox profileInfo = new VBox(8);
        Label profileName = new Label(user.getUsername());
        profileName.setFont(Font.font("Segoe UI", FontWeight.BOLD, 24));
        profileName.setTextFill(Color.web(TEXT()));
        
        Label profileRole = new Label(user.getRole().name().replace("_", " "));
        profileRole.setFont(Font.font("Segoe UI", FontWeight.SEMI_BOLD, 14));
        profileRole.setTextFill(Color.web(PRIMARY));
        
        Label profileEmail = new Label(user.getEmail() != null ? user.getEmail() : "Not set");
        profileEmail.setFont(Font.font("Segoe UI", 12));
        profileEmail.setTextFill(Color.web(MUTED_FG()));
        
        profileInfo.getChildren().addAll(profileName, profileRole, profileEmail);
        profileHeader.getChildren().addAll(profileIcon, profileInfo);
        
        // Profile details
        VBox details = new VBox(12);
        details.setPadding(new Insets(24));
        
        // Account Information section
        Label accountTitle = new Label("Account Information");
        accountTitle.setFont(Font.font("Segoe UI", FontWeight.BOLD, 14));
        accountTitle.setTextFill(Color.web(TEXT()));
        details.getChildren().add(accountTitle);
        
        GridPane detailsGrid = new GridPane();
        detailsGrid.setHgap(16);
        detailsGrid.setVgap(12);
        
        String[] labels = {"Username", "Email", "Phone", "Organization", "Role", "Status", "User ID"};
        String[] values = {
            user.getUsername(),
            user.getEmail() != null ? user.getEmail() : "Not provided",
            user.getPhoneNumber() != null ? user.getPhoneNumber() : "Not provided",
            user.getOrganization() != null ? user.getOrganization() : "Not provided",
            user.getRole().name().replace("_", " "),
            user.isApproved() ? "Active" : "Pending",
            "USR-" + String.format("%03d", user.getId())
        };
        
        for (int i = 0; i < labels.length; i++) {
            Label label = new Label(labels[i] + ":");
            label.setFont(Font.font("Segoe UI", FontWeight.MEDIUM, 12));
            label.setTextFill(Color.web(MUTED_FG()));
            
            Label value = new Label(values[i]);
            value.setFont(Font.font("Segoe UI", 12));
            value.setTextFill(Color.web(TEXT()));
            
            detailsGrid.add(label, 0, i);
            detailsGrid.add(value, 1, i);
        }
        details.getChildren().add(detailsGrid);
        
        // Settings section
        VBox settingsSection = new VBox(12);
        Label settingsTitle = new Label("Settings");
        settingsTitle.setFont(Font.font("Segoe UI", FontWeight.BOLD, 14));
        settingsTitle.setTextFill(Color.web(TEXT()));
        settingsSection.getChildren().add(settingsTitle);
        
        HBox btnBox = new HBox(12);
        
        Button editProfileBtn = new Button("Edit Profile");
        editProfileBtn.setStyle("-fx-background-color: " + PRIMARY + "; -fx-text-fill: white; -fx-padding: 8 16; -fx-background-radius: 4; -fx-cursor: hand;");
        editProfileBtn.setOnAction(e -> showEditProfileDialog());
        
        Button changePassBtn = new Button("Change Password");
        changePassBtn.setStyle("-fx-background-color: transparent; -fx-border-color: " + PRIMARY + "; -fx-text-fill: " + PRIMARY + "; -fx-padding: 8 16; -fx-background-radius: 4; -fx-border-radius: 4; -fx-cursor: hand;");
        changePassBtn.setOnAction(e -> showChangePasswordDialog());
        
        btnBox.getChildren().addAll(editProfileBtn, changePassBtn);
        settingsSection.getChildren().add(btnBox);
        
        details.getChildren().add(new Separator());
        details.getChildren().add(settingsSection);
        
        card.getChildren().addAll(profileHeader, details);
        page.getChildren().addAll(backBtn, card);
        
        ScrollPane sp = new ScrollPane(page);
        sp.setFitToWidth(true);
        sp.setStyle("-fx-background: " + BG() + "; -fx-background-color: " + BG() + ";");
        return sp;
    }
    
    private void showEditProfileDialog() {
        Dialog<Boolean> dialog = new Dialog<>();
        dialog.setTitle("Edit Profile");
        dialog.setHeaderText("Update your profile information");
        
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20));
        
        TextField emailField = new TextField(user.getEmail() != null ? user.getEmail() : "");
        TextField phoneField = new TextField(user.getPhoneNumber() != null ? user.getPhoneNumber() : "");
        TextField orgField = new TextField(user.getOrganization() != null ? user.getOrganization() : "");
        
        Label photoLabel = new Label("Photo:");
        Button changePhotoBtn = new Button("Select Image");
        final String[] newPhotoPath = {user.getProfilePhoto()};
        changePhotoBtn.setOnAction(e -> {
            FileChooser chooser = new FileChooser();
            chooser.setTitle("Select Profile Photo");
            chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg"));
            java.io.File file = chooser.showOpenDialog(stage);
            if (file != null) {
                newPhotoPath[0] = file.getAbsolutePath();
                changePhotoBtn.setText("Selected: " + file.getName());
            }
        });
        
        int row = 0;
        grid.add(new Label("Email:"), 0, row); grid.add(emailField, 1, row++);
        grid.add(new Label("Phone:"), 0, row); grid.add(phoneField, 1, row++);
        grid.add(new Label("Organization:"), 0, row); grid.add(orgField, 1, row++);
        grid.add(photoLabel, 0, row); grid.add(changePhotoBtn, 1, row++);
        
        dialog.getDialogPane().setContent(grid);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
        
        dialog.setResultConverter(buttonType -> {
            if (buttonType == ButtonType.OK) {
                user.setEmail(emailField.getText());
                user.setPhoneNumber(phoneField.getText());
                user.setOrganization(orgField.getText());
                user.setProfilePhoto(newPhotoPath[0]);
                
                UserService userService = new UserService();
                if (userService.updateProfile(user)) {
                    Alert a = new Alert(Alert.AlertType.INFORMATION, "Profile updated successfully!");
                    a.show();
                    root.setCenter(buildProfilePage());
                    root.setTop(buildHeader());
                    return true;
                } else {
                    Alert a = new Alert(Alert.AlertType.ERROR, "Failed to update profile.");
                    a.show();
                    return false;
                }
            }
            return null;
        });
        dialog.showAndWait();
    }
    
    private void showChangePasswordDialog() {
        javafx.scene.control.Dialog<String> dialog = new javafx.scene.control.Dialog<>();
        dialog.setTitle("Change Password");
        dialog.setHeaderText("Update your password");
        
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20));
        
        PasswordField currentPass = new PasswordField();
        currentPass.setPromptText("Current password");
        PasswordField newPass = new PasswordField();
        newPass.setPromptText("New password");
        PasswordField confirmPass = new PasswordField();
        confirmPass.setPromptText("Confirm password");
        
        grid.add(new Label("Current:"), 0, 0);
        grid.add(currentPass, 1, 0);
        grid.add(new Label("New:"), 0, 1);
        grid.add(newPass, 1, 1);
        grid.add(new Label("Confirm:"), 0, 2);
        grid.add(confirmPass, 1, 2);
        
        dialog.getDialogPane().setContent(grid);
        dialog.getDialogPane().getButtonTypes().addAll(
            javafx.scene.control.ButtonType.OK,
            javafx.scene.control.ButtonType.CANCEL
        );
        
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == javafx.scene.control.ButtonType.OK) {
                if (!newPass.getText().equals(confirmPass.getText())) {
                    Alert a = new Alert(Alert.AlertType.ERROR, "Passwords do not match.");
                    a.show();
                    return null;
                }
                if (!util.PasswordUtil.verify(currentPass.getText(), user.getPassword())) {
                    Alert a = new Alert(Alert.AlertType.ERROR, "Current password is incorrect.");
                    a.show();
                    return null;
                }
                user.setPassword(util.PasswordUtil.hash(newPass.getText()));
                UserService userService = new UserService();
                if (userService.updateProfile(user)) {
                    Alert a = new Alert(Alert.AlertType.INFORMATION, "Password updated successfully!");
                    a.show();
                } else {
                    Alert a = new Alert(Alert.AlertType.ERROR, "Failed to update password.");
                    a.show();
                }
            }
            return null;
        });

        dialog.showAndWait();
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
                sidebarBtn("Inquiries", "inquiries"),
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
                case "inquiries" -> root.setCenter(buildInquiriesPage());
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
        v.setTextFill(Color.web(TEXT()));
        Label d = new Label(detail);
        d.setFont(Font.font("Segoe UI", 11));
        d.setTextFill(Color.web(detailColor));
        card.getChildren().addAll(l, v, d);
        return card;
    }

    private void showAlert(String t, String msg) {
        Alert a = new Alert(Alert.AlertType.INFORMATION);
        a.setTitle(t);
        a.setContentText(msg);
        a.showAndWait();
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

        // Fetch fresh data every time (real-time)
        List<Child> allChildren = childService.getChildrenBySponsor(user.getId());
        List<Donation> myDonations = donationService.getByDonorId(user.getId());
        double totalDonated = myDonations.stream().mapToDouble(Donation::getAmount).sum();
        HBox stats = new HBox(16);
        stats.getChildren().addAll(
                statCard("Sponsored Children", String.valueOf(allChildren.size()), "Active sponsorships", PRIMARY),
                statCard("Total Donated", String.format("\u09F3%,.0f", totalDonated), "All time", SECONDARY),
                statCard("Donations", String.valueOf(myDonations.size()), "Total records", SECONDARY),
                statCard("Impact Score", "92", "Excellent rating", SECONDARY));

        // Sponsored Children cards
        Label scTitle = new Label("Your Sponsored Children");
        scTitle.setFont(Font.font("Segoe UI", FontWeight.MEDIUM, 17));
        scTitle.setTextFill(Color.web(TEXT()));
        HBox childCards = new HBox(16);
        for (Child ch : allChildren) {
            VBox card = new VBox(12);
            card.setPadding(new Insets(20));
            card.setStyle("-fx-background-color: " + CARD() + "; -fx-border-color: " + BORDER()
                    + "; -fx-border-width: 1; -fx-background-radius: 8; -fx-border-radius: 8; -fx-cursor: hand;");
            HBox.setHgrow(card, Priority.ALWAYS);

            StackPane avatar = new StackPane();
            avatar.setPrefSize(56, 56);
            avatar.setStyle("-fx-background-color: " + PRIMARY + "1A; -fx-background-radius: 28;");
            if (ch.getPhotoPath() != null && !ch.getPhotoPath().isEmpty()) {
                try {
                    javafx.scene.image.Image img = new javafx.scene.image.Image("file:" + ch.getPhotoPath(), 56, 56, true, true);
                    javafx.scene.image.ImageView iv = new javafx.scene.image.ImageView(img);
                    iv.setFitWidth(56);
                    iv.setFitHeight(56);
                    javafx.scene.shape.Circle clip = new javafx.scene.shape.Circle(28, 28, 28);
                    iv.setClip(clip);
                    avatar.getChildren().add(iv);
                } catch (Exception ex) {
                    String initStr = ch.getName().length() >= 2 ? ch.getName().substring(0, 2).toUpperCase() : ch.getName().toUpperCase();
                    Label initials = new Label(initStr);
                    initials.setFont(Font.font("Segoe UI", FontWeight.SEMI_BOLD, 18));
                    initials.setTextFill(Color.web(PRIMARY));
                    avatar.getChildren().add(initials);
                }
            } else {
                String initStr = ch.getName().length() >= 2 ? ch.getName().substring(0, 2).toUpperCase() : ch.getName().toUpperCase();
                Label initials = new Label(initStr);
                initials.setFont(Font.font("Segoe UI", FontWeight.SEMI_BOLD, 18));
                initials.setTextFill(Color.web(PRIMARY));
                avatar.getChildren().add(initials);
            }

            Label name = new Label(ch.getName());
            name.setFont(Font.font("Segoe UI", FontWeight.MEDIUM, 14));
            name.setTextFill(Color.web(TEXT()));
            Label age = new Label(
                    ch.getAge() + " years \u2022 " + (ch.getOrganization() != null ? ch.getOrganization() : "N/A"));
            age.setFont(Font.font("Segoe UI", 11));
            age.setTextFill(Color.web(MUTED_FG()));

            double childTotal = donationService.getTotalByChildId(ch.getId());
            HBox walletRow = new HBox();
            Label wl = new Label("Total Received:");
            wl.setFont(Font.font("Segoe UI", 11));
            wl.setTextFill(Color.web(MUTED_FG()));
            Region sp2 = new Region();
            HBox.setHgrow(sp2, Priority.ALWAYS);
            Label wv = new Label(String.format("\u09F3%,.0f", childTotal));
            wv.setFont(Font.font("Segoe UI", FontWeight.SEMI_BOLD, 14));
            wv.setTextFill(Color.web(SECONDARY));
            walletRow.getChildren().addAll(wl, sp2, wv);

            Button viewBtn = new Button("View Details \u2192");
            viewBtn.setMaxWidth(Double.MAX_VALUE);
            viewBtn.setStyle("-fx-background-color: " + PRIMARY
                    + "; -fx-text-fill: white; -fx-background-radius: 4; -fx-padding: 8 0; -fx-font-size: 12px; -fx-cursor: hand;");
            final String[] finalC = { String.valueOf(ch.getId()), ch.getName(), String.valueOf(ch.getAge()),
                    ch.getGender() != null ? ch.getGender() : "N/A",
                    ch.getOrganization() != null ? ch.getOrganization() : "N/A" };
            viewBtn.setOnAction(e -> root.setCenter(buildChildDetailView(finalC)));

            card.getChildren().addAll(avatar, name, age, walletRow, viewBtn);
            childCards.getChildren().add(card);
        }

        // Recent Donations table (only this donor's donations)
        VBox donTable = new VBox(0);
        donTable.setStyle("-fx-background-color: " + CARD() + "; -fx-border-color: " + BORDER()
                + "; -fx-border-width: 1; -fx-background-radius: 8; -fx-border-radius: 8;");
        HBox donHdr = new HBox();
        donHdr.setPadding(new Insets(16));
        donHdr.setStyle("-fx-border-color: " + BORDER() + "; -fx-border-width: 0 0 1 0;");
        Label donTitle = new Label("Your Recent Donations");
        donTitle.setFont(Font.font("Segoe UI", FontWeight.MEDIUM, 17));
        donTitle.setTextFill(Color.web(TEXT()));
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
        List<Donation> recentDons = myDonations.size() > 5 ? myDonations.subList(0, 5) : myDonations;
        for (int r = 0; r < recentDons.size(); r++) {
            Donation don = recentDons.get(r);
            Child donChild = childService.getChildById(don.getChildId());
            String childName = donChild != null ? donChild.getName() : "Child #" + don.getChildId();
            String[] rowData = { don.getDate() != null ? don.getDate() : "", childName,
                    String.format("\u09F3%,.0f", don.getAmount()),
                    don.getPurpose() != null ? don.getPurpose() : "",
                    don.getStatus() != null ? don.getStatus() : "Completed" };
            for (int c = 0; c < rowData.length; c++) {
                if (c == 4) {
                    Label badge = new Label(rowData[c]);
                    badge.setFont(Font.font("Segoe UI", 11));
                    badge.setStyle("-fx-background-color: " + SECONDARY + "1A; -fx-text-fill: " + SECONDARY
                            + "; -fx-background-radius: 4; -fx-padding: 2 8;");
                    HBox w = new HBox(badge);
                    w.setPadding(new Insets(10, 16, 10, 16));
                    w.setStyle("-fx-border-color: " + BORDER() + "; -fx-border-width: 0 0 1 0;");
                    grid.add(w, c, r + 1);
                    continue;
                }
                Label cell = new Label(rowData[c]);
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

        HBox titlebar = new HBox(16);
        titlebar.setAlignment(Pos.CENTER_LEFT);
        Label title = new Label("Sponsorship & Digital Wallet Management");
        title.setFont(Font.font("Segoe UI", FontWeight.MEDIUM, 20));
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        Button refreshBtn = new Button("🔄 Refresh");
        refreshBtn.setStyle("-fx-background-color: transparent; -fx-border-color: " + PRIMARY + "; -fx-text-fill: " + PRIMARY + "; -fx-border-radius: 4; -fx-cursor: hand; -fx-padding: 6 12;");
        refreshBtn.setOnAction(e -> root.setCenter(buildSponsorshipPage())); // Rebuild with fresh data
        titlebar.getChildren().addAll(title, spacer, refreshBtn);
        
        Label sub = new Label("Track donations and fund utilization");
        sub.setFont(Font.font("Segoe UI", 13));
        sub.setTextFill(Color.web(MUTED_FG()));

        List<Donation> spDonations = donationService.getAll();
        double spTotal = spDonations.stream().mapToDouble(Donation::getAmount).sum();
        HBox stats = new HBox(16);
        
        List<Child> sponsoredChildren = childService.getChildrenBySponsor(user.getId());
        stats.getChildren().addAll(
                statCard("Total Received (Org)", String.format("\u09F3%,.0f", spTotal), "All time", SECONDARY),
                statCard("Total Donations (Org)", String.valueOf(spDonations.size()), "Records", MUTED_FG()),
                statCard("Your Sponsored", String.valueOf(sponsoredChildren.size()), "Actively Supported", PRIMARY),
                statCard("Active Sponsor", "You", "Since joining", MUTED_FG()));

        // Fund Utilization
        VBox fundCard = new VBox(16);
        fundCard.setPadding(new Insets(16));
        fundCard.setStyle("-fx-background-color: " + CARD() + "; -fx-border-color: " + BORDER()
                + "; -fx-border-width: 1; -fx-background-radius: 8; -fx-border-radius: 8;");
        Label fundTitle = new Label("Fund Utilization by Category");
        fundTitle.setFont(Font.font("Segoe UI", FontWeight.MEDIUM, 17));
        fundCard.getChildren().add(fundTitle);

        String[][] funds = {
                { "Education", String.format("\u09F3%,.0f", spTotal * 0.50), "50", "#2563eb" },
                { "Medical Care", String.format("\u09F3%,.0f", spTotal * 0.25), "25", "#16a34a" },
                { "Food & Nutrition", String.format("\u09F3%,.0f", spTotal * 0.15), "15", "#f59e0b" },
                { "Clothing", String.format("\u09F3%,.0f", spTotal * 0.075), "7.5", "#8b5cf6" },
                { "Other", String.format("\u09F3%,.0f", spTotal * 0.025), "2.5", "#ec4899" },
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
        makeDon.setOnAction(e -> root.setCenter(buildDonationForm(null)));

        // --- DEDICATED SPONSORSHIP SECTION ---
        VBox sponsorshipCardsContainer = new VBox(20);
        
        // 1. Sponsored Children
        VBox sponsoredSection = new VBox(12);
        Label mySpTitle = new Label("Your Actively Sponsored Children");
        mySpTitle.setFont(Font.font("Segoe UI", FontWeight.BOLD, 16));
        mySpTitle.setTextFill(Color.web(TEXT()));
        javafx.scene.layout.FlowPane mySpCards = new javafx.scene.layout.FlowPane(16, 16);
        if (sponsoredChildren.isEmpty()) {
            Label noSp = new Label("You are not currently sponsoring any children exclusively.");
            noSp.setTextFill(Color.web(MUTED_FG()));
            mySpCards.getChildren().add(noSp);
        } else {
            for (Child ch : sponsoredChildren) {
                VBox scard = new VBox(12);
                scard.setPadding(new Insets(16));
                scard.setStyle("-fx-background-color: " + CARD() + "; -fx-border-color: " + PRIMARY
                        + "; -fx-border-width: 1; -fx-background-radius: 8; -fx-border-radius: 8;");
                Label n = new Label("CH-" + ch.getId() + " | " + ch.getName());
                n.setFont(Font.font("Segoe UI", FontWeight.SEMI_BOLD, 14));
                Label o = new Label((ch.getOrganization() != null ? ch.getOrganization() : "Unknown Org"));
                o.setFont(Font.font("Segoe UI", 12));
                o.setTextFill(Color.web(MUTED_FG()));
                
                Button unSponsor = new Button("Un-sponsor");
                unSponsor.setStyle("-fx-background-color: transparent; -fx-border-color: " + DESTRUCTIVE + "; -fx-text-fill: " + DESTRUCTIVE + "; -fx-border-radius: 4; -fx-cursor: hand;");
                unSponsor.setOnAction(e -> {
                    childService.removeSponsorFromChild(ch.getId());
                    systemLogService.save(new model.entity.SystemLog("Sponsorship Removal", "Donor " + user.getUsername() + " unsponsored child CH-" + ch.getId(), user.getUsername(), java.time.LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))));
                    root.setCenter(buildSponsorshipPage()); // Refresh
                });
                scard.getChildren().addAll(n, o, unSponsor);
                mySpCards.getChildren().add(scard);
            }
        }
        sponsoredSection.getChildren().addAll(mySpTitle, mySpCards);

        // 2. Unsponsored Children
        VBox unsponsoredSection = new VBox(12);
        Label unSpTitle = new Label("Children Waiting For A Sponsor");
        unSpTitle.setFont(Font.font("Segoe UI", FontWeight.BOLD, 16));
        unSpTitle.setTextFill(Color.web(TEXT()));
        javafx.scene.layout.FlowPane unSpCards = new javafx.scene.layout.FlowPane(16, 16);
        List<Child> unsponsoredChildren = childService.getUnsponsoredChildren();
        if (unsponsoredChildren.isEmpty()) {
            Label allSp = new Label("All children are currently sponsored! Thank you donors!");
            allSp.setTextFill(Color.web(SECONDARY));
            unSpCards.getChildren().add(allSp);
        } else {
            for (Child ch : unsponsoredChildren) {
                VBox ucard = new VBox(12);
                ucard.setPadding(new Insets(16));
                ucard.setStyle("-fx-background-color: " + CARD() + "; -fx-border-color: " + BORDER()
                        + "; -fx-border-width: 1; -fx-background-radius: 8; -fx-border-radius: 8;");
                Label n = new Label("CH-" + ch.getId() + " | " + ch.getName() + " (" + ch.getAge() + "y)");
                n.setFont(Font.font("Segoe UI", FontWeight.SEMI_BOLD, 14));
                Label o = new Label((ch.getOrganization() != null ? ch.getOrganization() : "Unknown Org"));
                o.setFont(Font.font("Segoe UI", 12));
                o.setTextFill(Color.web(MUTED_FG()));
                
                Button adoptBtn = new Button("\u2764\uFE0F Sponsor Child");
                adoptBtn.setStyle("-fx-background-color: " + PRIMARY + "; -fx-text-fill: white; -fx-background-radius: 4; -fx-cursor: hand;");
                adoptBtn.setOnAction(e -> {
                    ch.setSponsorId(user.getId());
                    childService.updateChild(ch);
                    systemLogService.save(new model.entity.SystemLog("Sponsorship Added", "Donor " + user.getUsername() + " sponsored child CH-" + ch.getId(), user.getUsername(), java.time.LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))));
                    root.setCenter(buildSponsorshipPage()); // Refresh
                });
                ucard.getChildren().addAll(n, o, adoptBtn);
                unSpCards.getChildren().add(ucard);
            }
        }
        unsponsoredSection.getChildren().addAll(unSpTitle, unSpCards);
        
        sponsorshipCardsContainer.getChildren().addAll(new Separator(), sponsoredSection, new Separator(), unsponsoredSection);

        page.getChildren().addAll(new VBox(4, titlebar, sub), stats, fundCard, makeDon, sponsorshipCardsContainer);
        return wrapScroll(page);
    }

    // ═══════════ DONATIONS PAGE ═══════════
    private ScrollPane buildDonationsPage() {
        VBox page = new VBox(20);
        page.setPadding(new Insets(24));

        Label title = new Label("Donation History");
        title.setFont(Font.font("Segoe UI", FontWeight.MEDIUM, 20));
        title.setTextFill(Color.web(TEXT()));
        Label sub = new Label("View all your donations and transactions");
        sub.setFont(Font.font("Segoe UI", 13));
        sub.setTextFill(Color.web(MUTED_FG()));

        List<Donation> donDonations = donationService.getByDonorId(user.getId());
        double donTotal = donDonations.stream().mapToDouble(Donation::getAmount).sum();
        HBox stats = new HBox(16);
        stats.getChildren().addAll(
                statCard("Total Donated", String.format("\u09F3%,.0f", donTotal), "By you", SECONDARY),
                statCard("Donations", String.valueOf(donDonations.size()), "Your records", SECONDARY),
                statCard("Children Helped", String.valueOf(childService.getChildrenBySponsor(user.getId()).size()), "Actively sponsoring",
                        PRIMARY),
                statCard("Impact", "100%", "Of donations given", INFO));

        // Transaction table
        VBox tableCard = new VBox(0);
        tableCard.setStyle("-fx-background-color: " + CARD() + "; -fx-border-color: " + BORDER()
                + "; -fx-border-width: 1; -fx-background-radius: 8; -fx-border-radius: 8;");
        HBox hdr = new HBox();
        hdr.setPadding(new Insets(16));
        hdr.setAlignment(Pos.CENTER_LEFT);
        hdr.setStyle("-fx-border-color: " + BORDER() + "; -fx-border-width: 0 0 1 0;");
        Label tl = new Label("Your Recent Donations");
        tl.setFont(Font.font("Segoe UI", FontWeight.MEDIUM, 17));
        Region sp1 = new Region();
        HBox.setHgrow(sp1, Priority.ALWAYS);
        Button exportBtn = new Button("Export to CSV");
        exportBtn.setStyle("-fx-background-color: " + SECONDARY
                + "; -fx-text-fill: white; -fx-background-radius: 4; -fx-padding: 6 16; -fx-font-size: 12px; -fx-cursor: hand;");
        exportBtn.setOnAction(e -> {
            FileChooser fc = new FileChooser();
            fc.setTitle("Export Donations to CSV");
            fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("CSV Files", "*.csv"));
            fc.setInitialFileName("donations.csv");
            java.io.File file = fc.showSaveDialog(stage);
            if (file != null) {
                try (java.io.PrintWriter pw = new java.io.PrintWriter(file)) {
                    pw.println("Date,Child,Amount,Time,Status");
                    for (Donation don : donDonations) {
                        Child c = childService.getChildById(don.getChildId());
                        String cName = c != null ? c.getName() : "Child #" + don.getChildId();
                        String time = "";
                        if (don.getDate() != null && don.getDate().length() >= 16) {
                            time = don.getDate().substring(11, 16);
                        }
                        pw.printf("%s,%s,%.0f,%s,%s%n",
                                don.getDate() != null ? don.getDate().substring(0, 10) : "",
                                cName,
                                don.getAmount(),
                                time,
                                don.getStatus() != null ? don.getStatus() : "Completed");
                    }
                    showAlert("Success", "Exported " + donDonations.size() + " records to " + file.getName());
                } catch (Exception ex) {
                    showAlert("Error", "Failed to export: " + ex.getMessage());
                }
            }
        });
        hdr.getChildren().addAll(tl, sp1, exportBtn);

        GridPane grid = new GridPane();
        String[] cols = { "Date", "Child", "Amount", "Time", "Status" };
        for (int i = 0; i < cols.length; i++) {
            Label h = new Label(cols[i]);
            h.setFont(Font.font("Segoe UI", FontWeight.SEMI_BOLD, 11));
            h.setPadding(new Insets(8, 16, 8, 16));
            h.setMaxWidth(Double.MAX_VALUE);
            h.setStyle("-fx-background-color: " + MUTED() + ";");
            h.setTextFill(Color.web(TEXT()));
            grid.add(h, i, 0);
        }
        String[][] rows = donDonations.stream().map(don -> {
            Child donChild = childService.getChildById(don.getChildId());
            String cName = donChild != null ? donChild.getName() : "Child #" + don.getChildId();
            String time = "";
            if (don.getDate() != null && don.getDate().length() >= 16) {
                time = don.getDate().substring(11, 16); // Extract HH:mm from date
            }
            return new String[] { don.getDate() != null ? don.getDate().substring(0, 10) : "", cName,
                    String.format("\u09F3%,.0f", don.getAmount()),
                    time,
                    don.getStatus() != null ? don.getStatus() : "Completed" };
        }).toArray(String[][]::new);
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
                cell.setTextFill(Color.web(TEXT()));
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
        makeDon.setOnAction(e -> root.setCenter(buildDonationForm(null)));

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
        genBtn.setOnAction(e -> {
            String reportType = rtCombo.getValue();
            String dateRange = drCombo.getValue();
            List<Donation> donations = filterDonationsByDateRange(donationService.getByDonorId(user.getId()), dateRange);
            String reportContent = generateReportContent(reportType, donations, dateRange);
            exportReportToTXT(reportContent, reportType);
            systemLogService.save(new SystemLog("Report", "Generated " + reportType + " (" + dateRange + ")", user.getUsername(),
                    LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))));
        });
        Button pdfBtn = new Button("📄 Export to PDF");
        pdfBtn.setStyle("-fx-background-color: " + SECONDARY
                + "; -fx-text-fill: white; -fx-background-radius: 4; -fx-padding: 8 16; -fx-font-size: 13px; -fx-cursor: hand;");
        pdfBtn.setOnAction(e -> {
            String reportType = rtCombo.getValue();
            String dateRange = drCombo.getValue();
            List<Donation> donations = filterDonationsByDateRange(donationService.getByDonorId(user.getId()), dateRange);
            String reportContent = generateReportContent(reportType, donations, dateRange);
            exportReportToPDF(reportContent, reportType);
            systemLogService.save(new SystemLog("Export", "Exported " + reportType + " to PDF", user.getUsername(),
                    LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))));
        });
        buttons.getChildren().addAll(genBtn, pdfBtn);

        genCard.getChildren().addAll(genTitle, row1, buttons);

        Label qStats_title = new Label("Report Statistics");
        qStats_title.setFont(Font.font("Segoe UI", FontWeight.MEDIUM, 17));
        qStats_title.setTextFill(Color.web(TEXT()));
        List<Donation> rpDonations = donationService.getAll();
        double rpTotal = rpDonations.stream().mapToDouble(Donation::getAmount).sum();
        HBox qStats = new HBox(16);
        qStats.getChildren().addAll(
                statCard("Total Donated", String.format("\u09F3%,.0f", rpTotal), "All time", SECONDARY),
                statCard("Donations", String.valueOf(rpDonations.size()), "Records", MUTED_FG()),
                statCard("Children Helped", String.valueOf(childService.getAllChildren().size()), "Active", PRIMARY),
                statCard("Impact Score", "92", "Excellent", SECONDARY));

        page.getChildren().addAll(new VBox(4, title, sub), genCard, qStats_title, qStats);
        return wrapScroll(page);
    }

    private ScrollPane buildDonationForm(String childName) {
        VBox page = new VBox(24);
        page.setPadding(new Insets(32));
        page.setMaxWidth(600);

        Button backBtn = new Button("\u2190 Back to Sponsorship");
        backBtn.setStyle("-fx-background-color: transparent; -fx-text-fill: " + PRIMARY + "; -fx-cursor: hand;");
        backBtn.setOnAction(e -> root.setCenter(buildSponsorshipPage()));

        Label title = new Label("Make a Donation");
        title.setFont(Font.font("Segoe UI", FontWeight.BOLD, 24));

        VBox form = new VBox(20);
        form.setPadding(new Insets(24));
        form.setStyle("-fx-background-color: " + CARD() + "; -fx-border-color: " + BORDER()
                + "; -fx-border-width: 1; -fx-background-radius: 8; -fx-border-radius: 8;");

        // Load only children sponsored by the donor
        java.util.List<Child> children = childService.getChildrenBySponsor(user.getId());
        
        // Child selection
        Label childLabel = new Label("Select Your Sponsored Child");
        childLabel.setFont(Font.font("Segoe UI", FontWeight.MEDIUM, 14));
        childLabel.setTextFill(Color.web(TEXT()));
        
        ComboBox<String> childCombo = new ComboBox<>();
        for (Child c : children) {
            childCombo.getItems().add(c.getId() + " - " + c.getName());
        }
        childCombo.setPromptText("Choose your sponsored child...");
        childCombo.setStyle("-fx-background-color: " + CARD() + "; -fx-border-color: " + BORDER()
                + "; -fx-text-fill: " + TEXT() + "; -fx-border-radius: 4; -fx-padding: 10; -fx-font-size: 13px;");
        
        // Theme the ComboBox dropdown and button cell
        childCombo.setButtonCell(new javafx.scene.control.ListCell<String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty ? "" : item);
                setStyle("-fx-text-fill: " + TEXT() + "; -fx-padding: 6; -fx-background-color: " + CARD() + ";");
            }
        });
        
        childCombo.setCellFactory(lv -> new javafx.scene.control.ListCell<String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty ? "" : item);
                if (empty || isSelected()) {
                    setStyle("-fx-text-fill: white; -fx-padding: 6; -fx-background-color: " + PRIMARY + ";");
                } else {
                    setStyle("-fx-text-fill: " + TEXT() + "; -fx-padding: 6; -fx-background-color: " + CARD() + ";");
                }
            }
        });

        Label amtLabel = new Label("Donation Amount (\u09F3)");
        amtLabel.setFont(Font.font("Segoe UI", FontWeight.MEDIUM, 14));
        amtLabel.setTextFill(Color.web(TEXT()));
        TextField amtField = new TextField();
        amtField.setPromptText("e.g. 100");
        amtField.setStyle("-fx-background-color: " + CARD() + "; -fx-border-color: " + BORDER()
                + "; -fx-text-fill: " + TEXT() + "; -fx-prompt-text-fill: " + MUTED_FG() + "; -fx-border-radius: 4; -fx-padding: 10; -fx-font-size: 13px;");

        // Purpose dropdown
        Label purposeLabel = new Label("Purpose of Donation");
        purposeLabel.setFont(Font.font("Segoe UI", FontWeight.MEDIUM, 14));
        purposeLabel.setTextFill(Color.web(TEXT()));
        ComboBox<String> purposeCombo = new ComboBox<>();
        purposeCombo.getItems().addAll(
            "General Welfare",
            "Education",
            "Medical Care",
            "Food & Nutrition",
            "Clothing",
            "Housing & Shelter",
            "Emergency Support",
            "Skill Development"
        );
        purposeCombo.setValue("General Welfare");
        purposeCombo.setStyle("-fx-background-color: " + CARD() + "; -fx-border-color: " + BORDER()
                + "; -fx-text-fill: " + TEXT() + "; -fx-border-radius: 4; -fx-padding: 10; -fx-font-size: 13px;");
        
        // Update purpose when child is selected
        childCombo.setOnAction(e -> {
            String selected = childCombo.getValue();
            if (selected != null && !selected.isEmpty()) {
                int childId = Integer.parseInt(selected.split(" - ")[0]);
                Child selectedChild = childService.getChildById(childId);
                // Set purpose based on child needs (could be expanded with more logic)
                if (selectedChild != null) {
                    purposeCombo.setValue("General Welfare");
                }
            }
        });

        Label msgLabel = new Label("Personal Message (Optional)");
        msgLabel.setFont(Font.font("Segoe UI", FontWeight.MEDIUM, 14));
        msgLabel.setTextFill(Color.web(TEXT()));
        TextArea msgArea = new TextArea();
        msgArea.setPromptText("Words of encouragement...");
        msgArea.setPrefHeight(100);
        msgArea.setStyle("-fx-background-color: " + CARD() + "; -fx-border-color: " + BORDER()
                + "; -fx-text-fill: " + TEXT() + "; -fx-font-size: 13px; -fx-border-radius: 4; -fx-padding: 10; -fx-control-inner-background: " + CARD() + ";");
        msgArea.setWrapText(true);
        
        // Make purpose hidden — auto-use "General Welfare" for simplicity
        purposeCombo.setVisible(false);
        purposeLabel.setVisible(false);

        Button submit = new Button("Submit Donation");
        submit.setMaxWidth(Double.MAX_VALUE);
        submit.setStyle("-fx-background-color: " + SECONDARY
                + "; -fx-text-fill: white; -fx-background-radius: 4; -fx-padding: 12; -fx-font-weight: bold; -fx-cursor: hand;");
        
        // Validation error label
        Label validationError = new Label();
        validationError.setStyle("-fx-text-fill: #d32f2f; -fx-font-size: 12px;");
        validationError.setVisible(false);
        validationError.setWrapText(true);
        
        submit.setOnAction(e -> {
            // Clear previous error
            validationError.setVisible(false);
            validationError.setText("");
            
            // Validate child selection
            String selectedChild = childCombo.getValue();
            if (selectedChild == null || selectedChild.trim().isEmpty()) {
                validationError.setText("⚠ Please select a child to donate to.");
                validationError.setVisible(true);
                return;
            }
            
            String amtText = amtField.getText().trim();
            if (amtText.isEmpty()) {
                validationError.setText("⚠ Amount is required.");
                validationError.setVisible(true);
                return;
            }
            double amount;
            try {
                amount = Double.parseDouble(amtText);
            } catch (NumberFormatException ex) {
                validationError.setText("⚠ Amount must be a valid number.");
                validationError.setVisible(true);
                return;
            }
            
            // Extract child ID from selected text (format: "ID - Name")
            int childId = Integer.parseInt(selectedChild.split(" - ")[0]);
            
            String purpose = purposeCombo.getValue() != null ? purposeCombo.getValue() : "General Welfare";
            Donation d = new Donation(user.getId(), childId, amount, purpose, LocalDate.now().toString());
            donationService.save(d);
            systemLogService
                    .save(new SystemLog("Donation", "Submitted donation of \u09F3" + amtText + " to child ID " + childId + " for " + purpose, user.getUsername(),
                            LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))));
            Alert success = new Alert(Alert.AlertType.INFORMATION, "Donation submitted successfully!");
            success.showAndWait();
            root.setCenter(buildSponsorshipPage()); // Return to sponsorship page with fresh data
        });

        // ═══ PAYMENT METHOD SECTION ═══
        Label payLabel = new Label("Payment Method");
        payLabel.setFont(Font.font("Segoe UI", FontWeight.MEDIUM, 14));
        payLabel.setTextFill(Color.web(TEXT()));

        // Payment method data: [name, brand color, emoji/symbol, subtitle]
        String[][] methods = {
            { "bKash",      "#E2136E", "৳",  "Mobile Banking"    },
            { "Nagad",      "#F05829", "N",  "Digital Financial" },
            { "Rocket",     "#8B008B", "🚀", "DBBL Mobile"       },
            { "Visa",       "#1A1F71", "V",  "Credit / Debit"    },
            { "Mastercard", "#EB001B", "MC", "Credit / Debit"    },
        };

        // Track which card is selected (index -1 = none)
        final int[] selectedPayIdx = {-1};
        javafx.scene.layout.FlowPane payCards = new javafx.scene.layout.FlowPane(12, 12);

        // Build each card as a StackPane overlay with a VBox label inside
        java.util.List<VBox> cardNodes = new java.util.ArrayList<>();
        for (int i = 0; i < methods.length; i++) {
            final int idx = i;
            String[] m = methods[i];

            VBox payCard = new VBox(6);
            payCard.setAlignment(javafx.geometry.Pos.CENTER);
            payCard.setPrefWidth(100);
            payCard.setPrefHeight(72);
            payCard.setPadding(new Insets(10, 12, 10, 12));
            payCard.setStyle(
                "-fx-background-color: " + CARD() + ";" +
                "-fx-border-color: " + BORDER() + ";" +
                "-fx-border-width: 2;" +
                "-fx-border-radius: 10;" +
                "-fx-background-radius: 10;" +
                "-fx-cursor: hand;"
            );

            // Brand circle / logo stand-in
            StackPane logoCircle = new StackPane();
            logoCircle.setPrefSize(32, 32);
            logoCircle.setStyle(
                "-fx-background-color: " + m[1] + ";" +
                "-fx-background-radius: 8;"
            );
            Label logoLbl = new Label(m[2]);
            logoLbl.setFont(Font.font("Segoe UI", FontWeight.BOLD, 11));
            logoLbl.setTextFill(Color.WHITE);
            logoCircle.getChildren().add(logoLbl);

            Label nameLbl = new Label(m[0]);
            nameLbl.setFont(Font.font("Segoe UI", FontWeight.SEMI_BOLD, 11));
            nameLbl.setTextFill(Color.web(TEXT()));

            Label subLbl = new Label(m[3]);
            subLbl.setFont(Font.font("Segoe UI", 9));
            subLbl.setTextFill(Color.web(MUTED_FG()));

            payCard.getChildren().addAll(logoCircle, nameLbl, subLbl);
            cardNodes.add(payCard);

            payCard.setOnMouseClicked(ev -> {
                // Deselect all
                for (VBox c : cardNodes) {
                    c.setStyle(
                        "-fx-background-color: " + CARD() + ";" +
                        "-fx-border-color: " + BORDER() + ";" +
                        "-fx-border-width: 2;" +
                        "-fx-border-radius: 10;" +
                        "-fx-background-radius: 10;" +
                        "-fx-cursor: hand;"
                    );
                }
                // Highlight selected
                selectedPayIdx[0] = idx;
                payCard.setStyle(
                    "-fx-background-color: " + methods[idx][1] + "1A;" +
                    "-fx-border-color: " + methods[idx][1] + ";" +
                    "-fx-border-width: 2;" +
                    "-fx-border-radius: 10;" +
                    "-fx-background-radius: 10;" +
                    "-fx-cursor: hand;"
                );
                nameLbl.setTextFill(Color.web(methods[idx][1]));
            });

            payCards.getChildren().add(payCard);
        }

        Label payNote = new Label("⚠ Demo mode — payment is simulated.");
        payNote.setFont(Font.font("Segoe UI", 10));
        payNote.setTextFill(Color.web(MUTED_FG()));

        // Check if donor has any sponsored children
        if (children.isEmpty()) {
            // Show message if no sponsored children
            Label noChildMsg = new Label("You haven't sponsored any children yet.");
            noChildMsg.setFont(Font.font("Segoe UI", 14));
            noChildMsg.setTextFill(Color.web(MUTED_FG()));
            noChildMsg.setWrapText(true);
            
            Label guideMsg = new Label("Visit the Sponsorship page to choose a child to support first.");
            guideMsg.setFont(Font.font("Segoe UI", 13));
            guideMsg.setTextFill(Color.web(MUTED_FG()));
            guideMsg.setWrapText(true);
            
            Button sponsorBtn = new Button("Go to Sponsorship");
            sponsorBtn.setStyle("-fx-background-color: " + PRIMARY
                    + "; -fx-text-fill: white; -fx-background-radius: 4; -fx-padding: 10 24; -fx-font-size: 13px; -fx-cursor: hand;");
            sponsorBtn.setOnAction(e -> root.setCenter(buildSponsorshipPage()));
            
            VBox noChildCard = new VBox(16);
            noChildCard.setPadding(new Insets(32));
            noChildCard.setAlignment(Pos.CENTER);
            noChildCard.setStyle("-fx-background-color: " + CARD() + "; -fx-border-color: " + BORDER()
                    + "; -fx-border-width: 1; -fx-background-radius: 8; -fx-border-radius: 8;");
            noChildCard.getChildren().addAll(noChildMsg, guideMsg, sponsorBtn);
            
            page.getChildren().addAll(backBtn, title, noChildCard);
            return wrapScroll(page);
        }

        form.getChildren().addAll(validationError, childLabel, childCombo, amtLabel, amtField,
                payLabel, payCards, payNote, msgLabel, msgArea, submit);

        page.getChildren().addAll(backBtn, title, form);
        return wrapScroll(page);
    }

    private ScrollPane buildChildDetailView(String[] child) {
        VBox page = new VBox(20);
        page.setPadding(new Insets(24));

        Button backBtn = new Button("\u2190 Back to Dashboard");
        backBtn.setStyle("-fx-background-color: transparent; -fx-text-fill: " + PRIMARY + "; -fx-cursor: hand;");
        backBtn.setOnAction(e -> root.setCenter(buildDashboardPage()));

        Label title = new Label("Child Detail: " + child[1]);
        title.setFont(Font.font("Segoe UI", FontWeight.BOLD, 22));
        title.setTextFill(Color.web(TEXT()));

        VBox card = new VBox(20);
        card.setPadding(new Insets(24));
        card.setStyle("-fx-background-color: " + CARD() + "; -fx-border-color: " + BORDER()
                + "; -fx-border-width: 1; -fx-background-radius: 8; -fx-border-radius: 8;");

        HBox hdr = new HBox(20);
        hdr.setAlignment(Pos.CENTER_LEFT);
        StackPane avatar = new StackPane(new Label(child[1].substring(0, 1)));
        avatar.setPrefSize(64, 64);
        avatar.setStyle("-fx-background-color: " + PRIMARY + "1A; -fx-background-radius: 32;");
        Label avatarLabel = (Label) avatar.getChildren().get(0);
        avatarLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 24));
        avatarLabel.setTextFill(Color.web(PRIMARY));

        VBox info = new VBox(4);
        Label n = new Label(child[1]);
        n.setFont(Font.font("Segoe UI", FontWeight.BOLD, 18));
        n.setTextFill(Color.web(TEXT()));
        Label i = new Label("ID: " + child[0]);
        i.setTextFill(Color.web(MUTED_FG()));
        info.getChildren().addAll(n, i);
        hdr.getChildren().addAll(avatar, info);

        // Pull real data from DB
        int childId;
        try {
            childId = Integer.parseInt(child[0].replace("CH-", ""));
        } catch (NumberFormatException ex) {
            childId = 0;
        }
        Child dbChild = childService.getChildById(childId);
        String org = dbChild != null && dbChild.getOrganization() != null ? dbChild.getOrganization() : "N/A";

        java.util.List<MedicalRecord> meds = medicalRecordService.getRecordsByChildId(childId);
        String healthStatus = meds.isEmpty() ? "N/A"
                : meds.get(0).getMedicalCondition() != null ? meds.get(0).getMedicalCondition() : "N/A";

        java.util.List<EducationRecord> edus = educationRecordService.getRecordsByChildId(childId);
        String education = edus.isEmpty() ? "N/A" : (edus.get(0).getGrade() != null ? edus.get(0).getGrade() : "N/A");

        GridPane details = new GridPane();
        details.setHgap(40);
        details.setVgap(15);
        String[][] data = {
                { "Age", child[2] },
                { "Gender", child[3] },
                { "Organization", org },
                { "Health Status", healthStatus },
                { "Education", education }
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

        Button donate = new Button("Make a Donation");
        donate.setStyle("-fx-background-color: " + SECONDARY
                + "; -fx-text-fill: white; -fx-background-radius: 4; -fx-padding: 10 20; -fx-font-weight: bold; -fx-cursor: hand;");
        donate.setOnAction(e -> root.setCenter(buildDonationForm(child[1])));

        // ═══════════ MEDICAL RECORD SECTION (READ-ONLY) ═══════════
        VBox medSection = new VBox(12);
        Label medicalTitle = new Label("Medical Information");
        medicalTitle.setFont(Font.font("Segoe UI", FontWeight.BOLD, 15));
        medicalTitle.setTextFill(Color.web(TEXT()));
        
        if (!meds.isEmpty()) {
            MedicalRecord med = meds.get(0);
            GridPane medGrid = new GridPane();
            medGrid.setHgap(40);
            medGrid.setVgap(10);
            
            String[][] medData = {
                    { "Blood Group", med.getBloodGroup() != null ? med.getBloodGroup() : "N/A" },
                    { "Medical Condition", med.getMedicalCondition() != null ? med.getMedicalCondition() : "N/A" },
                    { "Last Checkup", med.getLastCheckup() != null ? med.getLastCheckup() : "N/A" }
            };
            for (int r = 0; r < medData.length; r++) {
                Label l = new Label(medData[r][0]);
                l.setTextFill(Color.web(MUTED_FG()));
                Label v = new Label(medData[r][1]);
                v.setFont(Font.font("Segoe UI", FontWeight.MEDIUM, 13));
                v.setTextFill(Color.web(TEXT()));
                medGrid.add(l, 0, r);
                medGrid.add(v, 1, r);
            }
            medSection.getChildren().addAll(medicalTitle, medGrid);
        } else {
            Label noMed = new Label("No medical records available");
            noMed.setTextFill(Color.web(MUTED_FG()));
            medSection.getChildren().addAll(medicalTitle, noMed);
        }
        
        // ═══════════ EDUCATION RECORD SECTION (READ-ONLY) ═══════════
        VBox eduSection = new VBox(12);
        Label educationTitle = new Label("Education Information");
        educationTitle.setFont(Font.font("Segoe UI", FontWeight.BOLD, 15));
        educationTitle.setTextFill(Color.web(TEXT()));
        
        if (!edus.isEmpty()) {
            EducationRecord edu = edus.get(0);
            GridPane eduGrid = new GridPane();
            eduGrid.setHgap(40);
            eduGrid.setVgap(10);
            
            String[][] eduData = {
                    { "School Name", edu.getSchoolName() != null ? edu.getSchoolName() : "N/A" },
                    { "Grade/Class", edu.getGrade() != null ? edu.getGrade() : "N/A" },
                    { "Attendance %", String.valueOf(edu.getAttendancePercentage()) }
            };
            for (int r = 0; r < eduData.length; r++) {
                Label l = new Label(eduData[r][0]);
                l.setTextFill(Color.web(MUTED_FG()));
                Label v = new Label(eduData[r][1]);
                v.setFont(Font.font("Segoe UI", FontWeight.MEDIUM, 13));
                v.setTextFill(Color.web(TEXT()));
                eduGrid.add(l, 0, r);
                eduGrid.add(v, 1, r);
            }
            eduSection.getChildren().addAll(educationTitle, eduGrid);
        } else {
            Label noEdu = new Label("No education records available");
            noEdu.setTextFill(Color.web(MUTED_FG()));
            eduSection.getChildren().addAll(educationTitle, noEdu);
        }

        card.getChildren().addAll(hdr, new Separator(), details, new Separator(), 
                medSection, new Separator(), eduSection, new Separator(), donate);
        page.getChildren().addAll(backBtn, title, card);
        return wrapScroll(page);
    }

    // ═══════════ INQUIRIES PAGE ═══════════
    private ScrollPane buildInquiriesPage() {
        VBox page = new VBox(20);
        page.setPadding(new Insets(24));

        Label title = new Label("Ask Us Anything");
        title.setFont(Font.font("Segoe UI", FontWeight.MEDIUM, 20));
        title.setTextFill(Color.web(TEXT()));
        Label sub = new Label("Submit your inquiries and get support from our team");
        sub.setFont(Font.font("Segoe UI", 13));
        sub.setTextFill(Color.web(MUTED_FG()));

        // Inquiry form
        VBox formCard = new VBox(16);
        formCard.setPadding(new Insets(20));
        formCard.setStyle("-fx-background-color: " + CARD() + "; -fx-border-color: " + BORDER()
                + "; -fx-border-width: 1; -fx-background-radius: 8; -fx-border-radius: 8;");

        Label formTitle = new Label("Submit Your Inquiry");
        formTitle.setFont(Font.font("Segoe UI", FontWeight.BOLD, 16));
        formTitle.setTextFill(Color.web(TEXT()));

        TextField subjectField = new TextField();
        subjectField.setPromptText("Subject of your inquiry...");
        subjectField.setStyle("-fx-control-inner-background: " + BG() + "; -fx-text-fill: " + TEXT()
                + "; -fx-padding: 10; -fx-border-color: " + BORDER() + "; -fx-border-radius: 4;");

        TextArea messageArea = new TextArea();
        messageArea.setPromptText("Please describe your inquiry in detail...");
        messageArea.setWrapText(true);
        messageArea.setPrefRowCount(8);
        messageArea.setStyle("-fx-control-inner-background: " + BG() + "; -fx-text-fill: " + TEXT()
                + "; -fx-padding: 10; -fx-border-color: " + BORDER() + "; -fx-border-radius: 4;");

        HBox tagBox = new HBox(8);
        tagBox.setAlignment(Pos.CENTER_LEFT);
        Label tagLabel = new Label("Category: ");
        tagLabel.setFont(Font.font("Segoe UI", 12));
        tagLabel.setTextFill(Color.web(TEXT()));

        ComboBox<String> categoryBox = new ComboBox<>();
        categoryBox.getItems().addAll("General", "Donations", "Sponsorship", "Account", "Technical", "Other");
        categoryBox.setValue("General");
        categoryBox.setStyle("-fx-background-color: " + CARD() + "; -fx-border-color: " + BORDER()
                + "; -fx-border-width: 1; -fx-border-radius: 4; -fx-padding: 8 12;");

        tagBox.getChildren().addAll(tagLabel, categoryBox);

        Button submitBtn = new Button("Submit Inquiry");
        submitBtn.setStyle("-fx-background-color: " + PRIMARY
                + "; -fx-text-fill: white; -fx-background-radius: 4; -fx-padding: 10 24; -fx-font-size: 13px; -fx-cursor: hand; -fx-font-weight: bold;");

        Label successMsg = new Label();
        successMsg.setFont(Font.font("Segoe UI", 12));
        successMsg.setVisible(false);

        submitBtn.setOnAction(e -> {
            String subject = subjectField.getText().trim();
            String message = messageArea.getText().trim();
            String category = categoryBox.getValue();

            if (subject.isEmpty() || message.isEmpty()) {
                successMsg.setText("❌ Please fill in all fields");
                successMsg.setTextFill(Color.web(DESTRUCTIVE));
                successMsg.setVisible(true);
                return;
            }

            // Log the inquiry
            systemLogService.save(new SystemLog("Donor Inquiry",
                    "[" + category + "] " + subject + " - " + message.substring(0, Math.min(50, message.length())),
                    user.getUsername(),
                    LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))));

            successMsg.setText("✓ Your inquiry has been submitted successfully! Our support team will respond within 24 hours.");
            successMsg.setTextFill(Color.web(SECONDARY));
            successMsg.setVisible(true);

            // Clear form
            subjectField.clear();
            messageArea.clear();
            categoryBox.setValue("General");

            // Auto-hide success message after 4 seconds
            javafx.animation.Timeline timeline = new javafx.animation.Timeline(
                    new javafx.animation.KeyFrame(javafx.util.Duration.seconds(4), evt -> successMsg.setVisible(false))
            );
            timeline.play();
        });

        formCard.getChildren().addAll(formTitle, subjectField, messageArea, tagBox, submitBtn, successMsg);

        // FAQ Section
        Label faqTitle = new Label("Frequently Asked Questions");
        faqTitle.setFont(Font.font("Segoe UI", FontWeight.MEDIUM, 16));
        faqTitle.setTextFill(Color.web(TEXT()));

        VBox faqContainer = new VBox(12);
        for (FAQs.FAQ faq : FAQs.DONOR_FAQS) {
            VBox faqCard = new VBox(8);
            faqCard.setPadding(new Insets(12));
            faqCard.setStyle("-fx-background-color: " + MUTED() + "; -fx-border-color: " + BORDER()
                    + "; -fx-border-width: 1; -fx-border-radius: 6; -fx-background-radius: 6;");

            Label faqQ = new Label("❓ " + faq.title);
            faqQ.setFont(Font.font("Segoe UI", FontWeight.BOLD, 12));
            faqQ.setTextFill(Color.web(PRIMARY));
            faqQ.setWrapText(true);

            Label faqA = new Label(faq.description);
            faqA.setFont(Font.font("Segoe UI", 11));
            faqA.setTextFill(Color.web(TEXT()));
            faqA.setWrapText(true);

            Label faqCat = new Label("📁 " + faq.category);
            faqCat.setFont(Font.font("Segoe UI", 10));
            faqCat.setTextFill(Color.web(MUTED_FG()));

            Button sendFAQBtn = new Button("Send This as Question");
            sendFAQBtn.setStyle("-fx-background-color: transparent; -fx-text-fill: " + SECONDARY
                    + "; -fx-border-color: " + SECONDARY + "; -fx-border-radius: 3; -fx-padding: 4 8; -fx-cursor: hand; -fx-font-size: 10px;");
            sendFAQBtn.setOnAction(ev -> {
                subjectField.setText(faq.title);
                messageArea.setText(faq.message);
                categoryBox.setValue(faq.category);
            });

            faqCard.getChildren().addAll(faqQ, faqA, faqCat, sendFAQBtn);
            faqContainer.getChildren().add(faqCard);
        }

        ScrollPane faqScroll = new ScrollPane(faqContainer);
        faqScroll.setFitToWidth(true);
        faqScroll.setPrefHeight(300);
        faqScroll.setStyle("-fx-background-color: " + BG() + ";");

        page.getChildren().addAll(new VBox(4, title, sub), formCard, new Separator(), faqTitle, faqScroll);
        return wrapScroll(page);
    }

    private void applyDarkModeStylesheet(Scene scene) {
        // Remove all existing stylesheets to prevent theme mixing
        scene.getStylesheets().clear();
        
        // Only apply dark mode stylesheet if dark mode is enabled
        if (!ThemeManager.isDarkMode()) {
            return; // Light mode uses default JavaFX styling
        }
        
        // Create comprehensive dark mode CSS for all controls
        String darkModeCSS = ".text-input { -fx-text-fill: #e2e8f0; -fx-control-inner-background: #16213e; }"
                + ".combo-box { -fx-text-fill: #e2e8f0; }"
                + ".combo-box .list-cell { -fx-text-fill: #e2e8f0; }"
                + ".combo-box-popup .list-view { -fx-background-color: #16213e; }"
                + ".combo-box-popup .list-view .list-cell { -fx-text-fill: #e2e8f0; -fx-background-color: #16213e; }"
                + ".combo-box-popup .list-view .list-cell:hover { -fx-background-color: #0f3460; }"
                + ".date-picker { -fx-text-fill: #e2e8f0; }"
                + ".date-picker-popup { -fx-background-color: #16213e; }"
                + ".date-picker-popup .button { -fx-text-fill: #e2e8f0; -fx-background-color: #0f3460; }"
                + ".date-picker-popup .button:hover { -fx-background-color: #1a3a52; }"
                + ".date-picker-popup .label { -fx-text-fill: #e2e8f0; }"
                + ".date-picker-popup .spinner { -fx-text-fill: #e2e8f0; -fx-background-color: #0f3460; }"
                + ".date-picker-popup .spinner .text-field { -fx-control-inner-background: #0f3460; -fx-text-fill: #e2e8f0; }"
                + ".date-picker-popup .spinner .text { -fx-fill: #e2e8f0; }"
                + ".date-picker-popup .spinner .button { -fx-text-fill: #e2e8f0; }"
                + ".date-cell { -fx-text-fill: #e2e8f0; -fx-background-color: #16213e; -fx-padding: 4px; }"
                + ".date-cell:hover { -fx-background-color: #0f3460; }"
                + ".date-cell:focused { -fx-background-color: #2563eb; -fx-text-fill: white; }"
                + ".date-cell:today { -fx-border-color: #2563eb; -fx-border-width: 1; }"
                + ".date-cell:selected { -fx-background-color: #2563eb; -fx-text-fill: white; }";
        
        // Add stylesheet to scene
        try {
            java.io.File tempFile = java.io.File.createTempFile("darkmode", ".css");
            tempFile.deleteOnExit();
            try (java.io.FileWriter writer = new java.io.FileWriter(tempFile)) {
                writer.write(darkModeCSS);
            }
            scene.getStylesheets().add("file:///" + tempFile.getAbsolutePath().replace("\\", "/"));
        } catch (java.io.IOException e) {
            System.err.println("Failed to apply dark mode stylesheet: " + e.getMessage());
        }
    }

    // ═══════════ REPORT GENERATION HELPERS ═══════════
    private List<Donation> filterDonationsByDateRange(List<Donation> donations, String dateRange) {
        LocalDate now = LocalDate.now();
        LocalDate startDate = switch (dateRange) {
            case "Last 30 Days" -> now.minusDays(30);
            case "Last 3 Months" -> now.minusMonths(3);
            case "Last Year" -> now.minusYears(1);
            default -> LocalDate.ofEpochDay(0); // All time
        };
        
        return donations.stream()
            .filter(d -> {
                LocalDate donationDate = LocalDate.parse(d.getDate().substring(0, 10));
                return donationDate.isAfter(startDate) || donationDate.isEqual(startDate);
            })
            .toList();
    }

    private String generateReportContent(String reportType, List<Donation> donations, String dateRange) {
        StringBuilder sb = new StringBuilder();
        sb.append("GUARDIANLINK DONOR REPORT\n\n");
        
        sb.append("Report Type: ").append(reportType).append("\n");
        sb.append("Date Range: ").append(dateRange).append("\n");
        sb.append("Donor: ").append(user.getUsername()).append("\n");
        sb.append("Generated: ").append(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))).append("\n\n");
        
        switch (reportType) {
            case "Donation Summary":
                sb.append(generateDonationSummary(donations));
                break;
            case "Impact Report":
                sb.append(generateImpactReport(donations));
                break;
            case "Tax Receipt":
                sb.append(generateTaxReceipt(donations));
                break;
        }
        
        return sb.toString();
    }

    private String generateDonationSummary(List<Donation> donations) {
        StringBuilder sb = new StringBuilder();
        sb.append("DONATION SUMMARY\n\n");
        
        double totalAmount = donations.stream().mapToDouble(Donation::getAmount).sum();
        double avgAmount = donations.isEmpty() ? 0 : totalAmount / donations.size();
        
        sb.append("Total Donations: ").append(String.format("৳%,.2f", totalAmount)).append("\n");
        sb.append("Number of Donations: ").append(donations.size()).append("\n");
        sb.append("Average Donation: ").append(String.format("৳%,.2f", avgAmount)).append("\n\n");
        
        sb.append("DONATION DETAILS\n");
        sb.append(String.format("%-12s %-20s %-15s\n", "Date", "Child", "Amount"));
        
        for (Donation d : donations) {
            String childName = "General";
            if (d.getChildId() > 0) {
                Child child = childService.getChildById(d.getChildId());
                if (child != null) {
                    childName = child.getName();
                }
            }
            sb.append(String.format("%-12s %-20s %-15s\n", 
                d.getDate().substring(0, 10),
                childName.length() > 19 ? childName.substring(0, 19) : childName,
                String.format("৳%,.2f", d.getAmount())));
        }
        
        return sb.toString();
    }

    private String generateImpactReport(List<Donation> donations) {
        StringBuilder sb = new StringBuilder();
        sb.append("IMPACT REPORT\n\n");
        
        double totalDonated = donations.stream().mapToDouble(Donation::getAmount).sum();
        List<Child> sponsoredChildren = childService.getChildrenBySponsor(user.getId());
        int medicalRecords = sponsoredChildren.isEmpty() ? 0 : medicalRecordService.getRecordsByChildId(sponsoredChildren.get(0).getId()).size();
        int educationRecords = sponsoredChildren.isEmpty() ? 0 : educationRecordService.getRecordsByChildId(sponsoredChildren.get(0).getId()).size();
        
        sb.append("IMPACT METRICS\n");
        sb.append("Total Amount Donated: ").append(String.format("৳%,.2f", totalDonated)).append("\n");
        sb.append("Number of Donations: ").append(donations.size()).append("\n");
        sb.append("Children Sponsored: ").append(sponsoredChildren.size()).append("\n");
        sb.append("Medical Records Supported: ").append(medicalRecords).append("\n");
        sb.append("Education Records Supported: ").append(educationRecords).append("\n\n");
        
        sb.append("\nSPONSORED CHILDREN\n");
        if (sponsoredChildren.isEmpty()) {
            sb.append("No children currently sponsored.\n");
        } else {
            for (Child child : sponsoredChildren) {
                sb.append("• ").append(child.getName()).append(" (Age: ").append(child.getAge()).append(")\n");
            }
        }
        
        return sb.toString();
    }

    private String generateTaxReceipt(List<Donation> donations) {
        StringBuilder sb = new StringBuilder();
        sb.append("OFFICIAL TAX RECEIPT\n\n");
        
        sb.append("Donor Name: ").append(user.getUsername()).append("\n");
        sb.append("Tax ID: GDL-").append(user.getId()).append("\n");
        sb.append("Receipt Date: ").append(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))).append("\n\n");
        
        double totalAmount = donations.stream().mapToDouble(Donation::getAmount).sum();
        
        sb.append("DONATION RECEIPT\n");
        sb.append("Total Charitable Donations: ").append(String.format("৳%,.2f", totalAmount)).append("\n");
        sb.append("Number of Donations: ").append(donations.size()).append("\n\n");
        
        sb.append("\nTAX INFORMATION\n");
        sb.append("Organization: GuardianLink NGO\n");
        sb.append("Registration: Approved for tax deductions\n");
        sb.append("Tax Deductible: Yes\n\n");
        
        sb.append("TRANSACTION HISTORY\n");
        sb.append(String.format("%-12s %-20s %-15s\n", "Date", "Recipient", "Amount"));
        
        for (Donation d : donations) {
            String childName = "General Fund";
            if (d.getChildId() > 0) {
                Child child = childService.getChildById(d.getChildId());
                if (child != null) {
                    childName = child.getName();
                }
            }
            sb.append(String.format("%-12s %-20s %-15s\n", 
                d.getDate().substring(0, 10),
                childName.length() > 19 ? childName.substring(0, 19) : childName,
                String.format("৳%,.2f", d.getAmount())));
        }
        
        sb.append("\nThis is an official receipt for tax purposes.\n");
        sb.append("════════════════════════════════════════════════════════\n");
        
        return sb.toString();
    }

    private void showReportDialog(String reportContent, String reportTitle) {
        Stage reportStage = new Stage();
        reportStage.setTitle(reportTitle);
        reportStage.setWidth(800);
        reportStage.setHeight(600);
        
        VBox dialogBox = new VBox(12);
        dialogBox.setPadding(new Insets(16));
        
        Label title = new Label(reportTitle);
        title.setFont(Font.font("Segoe UI", FontWeight.BOLD, 16));
        
        TextArea reportArea = new TextArea();
        reportArea.setText(reportContent);
        reportArea.setWrapText(true);
        reportArea.setEditable(false);
        reportArea.setStyle("-fx-control-inner-background: " + CARD() + "; -fx-text-fill: " + TEXT() + ";");
        reportArea.setFont(Font.font("Courier New", 11));
        
        HBox buttonBox = new HBox(12);
        buttonBox.setAlignment(Pos.CENTER_RIGHT);
        
        Button copyBtn = new Button("Copy to Clipboard");
        copyBtn.setStyle("-fx-background-color: " + PRIMARY + "; -fx-text-fill: white; -fx-padding: 8 16;");
        copyBtn.setOnAction(e -> {
            Clipboard clipboard = Clipboard.getSystemClipboard();
            ClipboardContent content = new ClipboardContent();
            content.putString(reportContent);
            clipboard.setContent(content);
            showAlert("Copied", "Report copied to clipboard!");
        });
        
        Button closeBtn = new Button("Close");
        closeBtn.setStyle("-fx-background-color: " + MUTED() + "; -fx-padding: 8 16;");
        closeBtn.setOnAction(e -> reportStage.close());
        
        buttonBox.getChildren().addAll(copyBtn, closeBtn);
        
        dialogBox.getChildren().addAll(title, reportArea, buttonBox);
        
        Scene scene = new Scene(new ScrollPane(dialogBox));
        reportStage.setScene(scene);
        reportStage.show();
    }

    private void exportReportToTXT(String reportContent, String reportType) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Export Report as Text");
        fileChooser.setInitialFileName(reportType.replace(" ", "_") + "_" + LocalDate.now() + ".txt");
        fileChooser.getExtensionFilters().addAll(
            new FileChooser.ExtensionFilter("Text Files", "*.txt"),
            new FileChooser.ExtensionFilter("All Files", "*.*")
        );
        
        java.io.File file = fileChooser.showSaveDialog(stage);
        if (file != null) {
            try (java.io.FileWriter writer = new java.io.FileWriter(file)) {
                writer.write(reportContent);
                showAlert("Success", "Report exported to:\n" + file.getAbsolutePath());
            } catch (java.io.IOException e) {
                showAlert("Error", "Failed to export report: " + e.getMessage());
            }
        }
    }

    private void exportReportToPDF(String reportContent, String reportType) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Export Report as PDF");
        fileChooser.setInitialFileName(reportType.replace(" ", "_") + "_" + LocalDate.now() + ".pdf");
        fileChooser.getExtensionFilters().addAll(
            new FileChooser.ExtensionFilter("PDF Files", "*.pdf"),
            new FileChooser.ExtensionFilter("All Files", "*.*")
        );
        
        java.io.File file = fileChooser.showSaveDialog(stage);
        if (file != null) {
            try (java.io.FileOutputStream fos = new java.io.FileOutputStream(file)) {
                StringBuilder pdf = new StringBuilder();
                
                // PDF Header
                pdf.append("%PDF-1.4\n");
                
                // Content stream with proper text formatting
                String[] lines = reportContent.split("\n");
                StringBuilder content = new StringBuilder();
                content.append("BT\n");
                content.append("/F1 10 Tf\n");
                content.append("50 750 Td\n");
                
                for (String line : lines) {
                    String escaped = line.replace("\\", "\\\\").replace("(", "\\(").replace(")", "\\)");
                    content.append("(").append(escaped).append(") Tj\n");
                    content.append("0 -12 Td\n");
                }
                content.append("ET\n");
                
                String contentStr = content.toString();
                
                // Object 1: Catalog
                pdf.append("1 0 obj\n");
                pdf.append("<< /Type /Catalog /Pages 2 0 R >>\n");
                pdf.append("endobj\n");
                
                // Object 2: Pages
                pdf.append("2 0 obj\n");
                pdf.append("<< /Type /Pages /Kids [3 0 R] /Count 1 >>\n");
                pdf.append("endobj\n");
                
                // Object 3: Page
                pdf.append("3 0 obj\n");
                pdf.append("<< /Type /Page /Parent 2 0 R /MediaBox [0 0 612 792] /Contents 4 0 R /Resources 5 0 R >>\n");
                pdf.append("endobj\n");
                
                // Object 4: Content Stream
                pdf.append("4 0 obj\n");
                pdf.append("<< /Length ").append(contentStr.length()).append(" >>\n");
                pdf.append("stream\n");
                pdf.append(contentStr);
                pdf.append("endstream\n");
                pdf.append("endobj\n");
                
                // Object 5: Resources
                pdf.append("5 0 obj\n");
                pdf.append("<< /Font << /F1 << /Type /Font /Subtype /Type1 /BaseFont /Helvetica >> >> >>\n");
                pdf.append("endobj\n");
                
                // Calculate xref offsets
                String[] pdfObjects = pdf.toString().split("endobj\n");
                int[] offsets = new int[6];
                int currentOffset = 0;
                for (int i = 0; i < 5; i++) {
                    offsets[i] = currentOffset;
                    currentOffset += pdfObjects[i].length() + 7; // +7 for "endobj\n"
                }
                
                // xref table
                pdf.append("xref\n");
                pdf.append("0 6\n");
                pdf.append("0000000000 65535 f \n");
                for (int i = 1; i < 6; i++) {
                    pdf.append(String.format("%010d 00000 n \n", offsets[i]));
                }
                
                // Trailer
                pdf.append("trailer\n");
                pdf.append("<< /Size 6 /Root 1 0 R >>\n");
                pdf.append("startxref\n");
                pdf.append(currentOffset).append("\n");
                pdf.append("%%EOF\n");
                
                // Write to file
                byte[] pdfBytes = pdf.toString().getBytes(java.nio.charset.StandardCharsets.UTF_8);
                fos.write(pdfBytes);
                fos.flush();
                
                showAlert("Success", "PDF Report exported to:\n" + file.getAbsolutePath());
            } catch (java.io.IOException e) {
                showAlert("Error", "Failed to export PDF: " + e.getMessage());
            }
        }
    }
}