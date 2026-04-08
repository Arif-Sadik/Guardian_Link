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
import model.entity.Notification;
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
import service.NotificationService;
import service.SystemLogService;
import service.UserService;
import service.RolePermissionsService;
import service.MedicalRecordService;
import service.EducationRecordService;
import model.entity.MedicalRecord;
import model.entity.EducationRecord;
import util.ThemeManager;
import util.PasswordUtil;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Period;
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
    private final NotificationService notificationService = new NotificationService();
    private final RolePermissionsService rolePermissionsService = new RolePermissionsService();
    private final MedicalRecordService medicalRecordService = new MedicalRecordService();
    private final EducationRecordService educationRecordService = new EducationRecordService();
    private BorderPane root;
    private Scene scene;
    private VBox sidebar;
    private String activePage = "dashboard";
    private boolean isShowingForm = false; // Flag to prevent auto-refresh during form display
    private Timeline refreshTimer; // Store reference to timer for control

    private java.util.List<String[]> activeAlerts = new java.util.ArrayList<>();

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

        // Auto-refresh timer: refresh the active page every 180 seconds (3 minutes) for real-time
        // data (but skip if a form is being shown)
        refreshTimer = new Timeline(new KeyFrame(Duration.seconds(180), ev -> {
            if (!isShowingForm) {
                switch (activePage) {
                    case "dashboard" -> root.setCenter(buildDashboardPage());
                    case "children" -> root.setCenter(buildChildrenPage());
                    case "alerts" -> root.setCenter(buildAlertsPage());
                    case "reports" -> root.setCenter(buildReportsPage());
                    case "admin" -> root.setCenter(buildAdminPage());
                }
            }
        }));
        refreshTimer.setCycleCount(Timeline.INDEFINITE);
        refreshTimer.play();

        Scene scene = new Scene(root, 1280, 800);
        this.scene = scene; // Store scene reference for stylesheet management
        
        // Apply dark mode styles for improved text visibility
        applyDarkModeStylesheet(scene);
        
        stage.setScene(scene);
        stage.setTitle("GuardianLink \u2014 System Administrator");
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
                if (!PasswordUtil.verify(currentPass.getText(), user.getPassword())) {
                    Alert a = new Alert(Alert.AlertType.ERROR, "Current password is incorrect.");
                    a.show();
                    return null;
                }
                user.setPassword(PasswordUtil.hash(newPass.getText()));
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
        
        // Hover effect
        card.setOnMouseEntered(e -> card.setStyle("-fx-background-color: " + MUTED() + "; -fx-border-color: " + PRIMARY
                + "; -fx-border-width: 1; -fx-background-radius: 8; -fx-border-radius: 8; -fx-cursor: hand;"));
        card.setOnMouseExited(e -> card.setStyle("-fx-background-color: " + CARD() + "; -fx-border-color: " + BORDER()
                + "; -fx-border-width: 1; -fx-background-radius: 8; -fx-border-radius: 8; -fx-cursor: hand;"));
        
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
            viewLink.setStyle("-fx-text-decoration: none;");
            viewLink.setOnAction(e -> root.setCenter(buildChildProfileDetailView(childId)));

            Hyperlink editLink = new Hyperlink("Edit");
            editLink.setFont(Font.font("Segoe UI", 13));
            editLink.setTextFill(Color.web(PRIMARY));
            editLink.setStyle("-fx-text-decoration: none;");
            editLink.setOnAction(e -> root.setCenter(buildEditChildForm(childId)));

            Hyperlink deleteLink = new Hyperlink("Delete");
            deleteLink.setFont(Font.font("Segoe UI", 13));
            deleteLink.setTextFill(Color.web(DESTRUCTIVE));
            deleteLink.setStyle("-fx-text-decoration: none;");
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
        page.setStyle("-fx-background-color: " + BG() + ";");

        Label title = new Label("Alerts & Notifications");
        title.setFont(Font.font("Segoe UI", FontWeight.MEDIUM, 20));
        title.setTextFill(Color.web(TEXT()));
        Label sub = new Label("Monitor alerts relevant to your role");
        sub.setFont(Font.font("Segoe UI", 13));
        sub.setTextFill(Color.web(MUTED_FG()));

            // Get role-specific alerts
            java.util.List<String[]> roleAlerts = getAlertsForRole();
            
            long criticalCount = roleAlerts.stream().filter(a -> a != null && a.length > 0 && a[0].equals("critical")).count();
            long warningCount = roleAlerts.stream().filter(a -> a != null && a.length > 0 && a[0].equals("warning")).count();
            long totalActive = roleAlerts.size();

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
            
            if (roleAlerts.isEmpty()) {
                Label noAlerts = new Label("No alerts at this moment.");
                noAlerts.setFont(Font.font("Segoe UI", 13));
                noAlerts.setTextFill(Color.web(MUTED_FG()));
                alertsList.getChildren().add(noAlerts);
            } else {
                for (String[] a : roleAlerts) {
                    if (a == null || a.length < 5) {
                        continue;
                    }
                    
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
                    if (a.length > 5 && a[5] != null && !a[5].isEmpty()) {
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
        title.setTextFill(Color.web(TEXT()));
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

        // Load data from DB
        java.util.List<Donation> allDonations = donationService.getAll();
        java.util.List<Child> allChildren = childService.getAllChildren();
        java.util.List<User> allUsersForReport = userService.getAllUsers();
        java.util.List<SystemLog> allLogs = systemLogService.getAll();
        java.util.Map<Integer, String> childNames = new java.util.HashMap<>();
        for (Child ch : allChildren)
            childNames.put(ch.getId(), ch.getName());
        java.util.Map<Integer, String> userNames = new java.util.HashMap<>();
        for (User u : allUsersForReport)
            userNames.put(u.getId(), u.getUsername());

        double totalDonated = allDonations.stream().mapToDouble(Donation::getAmount).sum();

        // Container for the dynamic report preview
        VBox reportPreviewContainer = new VBox();

        HBox buttons = new HBox(12);
        Button genBtn = new Button("\uD83D\uDCC4  Generate Report");
        genBtn.setStyle("-fx-background-color: " + PRIMARY
                + "; -fx-text-fill: white; -fx-background-radius: 4; -fx-padding: 8 16; -fx-font-size: 13px; -fx-cursor: hand;");
        genBtn.setOnAction(e -> {
            systemLogService.save(new SystemLog("Report", "Generated " + rtCombo.getValue(), user.getUsername(),
                    LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))));
            reportPreviewContainer.getChildren().clear();
            reportPreviewContainer.getChildren().add(
                    buildReportPreview(rtCombo.getValue(), allDonations, allChildren, allUsersForReport,
                            allLogs, childNames, userNames, totalDonated));
            new Alert(Alert.AlertType.INFORMATION, "Report generated successfully: " + rtCombo.getValue()).show();
        });
        Button csvBtn = new Button("\u2B07  Export to CSV");
        csvBtn.setStyle("-fx-background-color: " + SECONDARY
                + "; -fx-text-fill: white; -fx-background-radius: 4; -fx-padding: 8 16; -fx-font-size: 13px; -fx-cursor: hand;");
        csvBtn.setOnAction(e -> {
            String selectedType = rtCombo.getValue();
            FileChooser fc = new FileChooser();
            fc.setTitle("Export CSV");
            fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("CSV Files", "*.csv"));
            String fileName = selectedType.replaceAll("[^a-zA-Z0-9]", "_").toLowerCase() + ".csv";
            fc.setInitialFileName(fileName);
            File file = fc.showSaveDialog(stage);
            if (file != null) {
                try (FileWriter fw = new FileWriter(file)) {
                    switch (selectedType) {
                        case "Donation & Financial Report" -> {
                            fw.write("Donor,Child,Amount,Date\n");
                            for (Donation d : allDonations) {
                                fw.write(String.format("%s,%s,%.0f,%s\n",
                                        userNames.getOrDefault(d.getDonorId(), "Unknown"),
                                        childNames.getOrDefault(d.getChildId(), "Unknown"),
                                        d.getAmount(), d.getDate()));
                            }
                        }
                        case "Child Welfare Summary" -> {
                            fw.write("ID,Name,Age,Gender,Organization,Status\n");
                            for (Child ch : allChildren) {
                                fw.write(String.format("CH-%d,%s,%d,%s,%s,%s\n",
                                        1000 + ch.getId(), ch.getName(), ch.getAge(),
                                        ch.getGender() != null ? ch.getGender() : "",
                                        ch.getOrganization() != null ? ch.getOrganization() : "",
                                        ch.getStatus() != null ? ch.getStatus() : "Active"));
                            }
                        }
                        case "System Audit Log" -> {
                            fw.write("Timestamp,Event Type,Actor,Description\n");
                            for (SystemLog log : allLogs) {
                                fw.write(String.format("%s,%s,%s,%s\n",
                                        log.getTimestamp() != null ? log.getTimestamp() : "",
                                        log.getEventType() != null ? log.getEventType() : "",
                                        log.getActor() != null ? log.getActor() : "",
                                        log.getDescription() != null ? log.getDescription().replace(",", ";") : ""));
                            }
                        }
                        case "Performance Analytics" -> {
                            fw.write("Metric,Value\n");
                            fw.write(String.format("Total Users,%d\n", allUsersForReport.size()));
                            fw.write(String.format("Active Users,%d\n",
                                    allUsersForReport.stream().filter(User::isApproved).count()));
                            fw.write(String.format("Total Children,%d\n", allChildren.size()));
                            fw.write(String.format("Total Donations,%d\n", allDonations.size()));
                            fw.write(String.format("Total Donated,%.0f\n", totalDonated));
                            fw.write(String.format("System Log Entries,%d\n", allLogs.size()));
                        }
                    }
                    systemLogService.save(new SystemLog("Export", "Exported " + selectedType + " CSV",
                            user.getUsername(),
                            LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))));
                    new Alert(Alert.AlertType.INFORMATION, "CSV exported successfully!\nFile: " + file.getName())
                            .show();
                } catch (IOException ex) {
                    new Alert(Alert.AlertType.ERROR, "Export failed: " + ex.getMessage()).show();
                }
            }
        });
        buttons.getChildren().addAll(genBtn, csvBtn);

        genCard.getChildren().addAll(genTitle, row1, buttons);

        // Build the initial report preview (Donation & Financial by default)
        reportPreviewContainer.getChildren().add(
                buildReportPreview("Donation & Financial Report", allDonations, allChildren, allUsersForReport,
                        allLogs, childNames, userNames, totalDonated));

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

        // ── Event Logs Section ──
        VBox eventLogsCard = new VBox(0);
        eventLogsCard.setStyle("-fx-background-color: " + CARD() + "; -fx-border-color: " + BORDER()
                + "; -fx-border-width: 1; -fx-background-radius: 8; -fx-border-radius: 8;");

        HBox elHdr = new HBox();
        elHdr.setPadding(new Insets(16));
        elHdr.setAlignment(Pos.CENTER_LEFT);
        elHdr.setStyle("-fx-border-color: " + BORDER() + "; -fx-border-width: 0 0 1 0;");
        Label elTitle = new Label("Audit Trail — Event Logs");
        elTitle.setFont(Font.font("Segoe UI", FontWeight.MEDIUM, 17));
        elTitle.setTextFill(Color.web(TEXT()));
        Region elSpacer = new Region();
        HBox.setHgrow(elSpacer, Priority.ALWAYS);
        Label elCount = new Label(allLogs.size() + " entries");
        elCount.setFont(Font.font("Segoe UI", 12));
        elCount.setTextFill(Color.web(MUTED_FG()));
        elHdr.getChildren().addAll(elTitle, elSpacer, elCount);

        GridPane elGrid = new GridPane();
        String[] elCols = { "Timestamp", "Event Type", "Actor", "Description", "Actions" };
        for (int i = 0; i < elCols.length; i++) {
            Label h = new Label(elCols[i]);
            h.setFont(Font.font("Segoe UI", FontWeight.SEMI_BOLD, 11));
            h.setPadding(new Insets(8, 16, 8, 16));
            h.setMaxWidth(Double.MAX_VALUE);
            h.setStyle("-fx-background-color: " + MUTED() + ";");
            h.setTextFill(Color.web(TEXT()));
            elGrid.add(h, i, 0);
        }

        int logLimit = Math.min(allLogs.size(), 50);
        for (int r = 0; r < logLimit; r++) {
            SystemLog log = allLogs.get(r);
            String[] rowData = {
                    log.getTimestamp() != null ? log.getTimestamp() : "",
                    log.getEventType() != null ? log.getEventType() : "",
                    log.getActor() != null ? log.getActor() : "",
                    log.getDescription() != null ? log.getDescription() : ""
            };
            for (int c = 0; c < rowData.length; c++) {
                Label cell = new Label(rowData[c]);
                cell.setFont(Font.font(c == 0 ? "Consolas" : "Segoe UI", c == 0 ? 11 : 13));
                cell.setTextFill(Color.web(c == 0 ? MUTED_FG() : TEXT()));
                cell.setPadding(new Insets(10, 16, 10, 16));
                cell.setMaxWidth(Double.MAX_VALUE);
                cell.setStyle("-fx-border-color: " + BORDER() + "; -fx-border-width: 0 0 1 0;");
                elGrid.add(cell, c, r + 1);
            }
            // View Details button
            final SystemLog currentLog = log;
            Button detailBtn = new Button("View Details");
            detailBtn.setFont(Font.font("Segoe UI", 11));
            detailBtn.setStyle("-fx-background-color: " + PRIMARY
                    + "; -fx-text-fill: white; -fx-background-radius: 4; -fx-padding: 4 12; -fx-cursor: hand;");
            detailBtn.setOnAction(e -> {
                Alert detail = new Alert(Alert.AlertType.INFORMATION);
                detail.setTitle("Event Log Details");
                detail.setHeaderText(currentLog.getEventType() != null ? currentLog.getEventType() : "Event");
                detail.setContentText(
                        "Timestamp: " + (currentLog.getTimestamp() != null ? currentLog.getTimestamp() : "N/A")
                                + "\nEvent Type: "
                                + (currentLog.getEventType() != null ? currentLog.getEventType() : "N/A")
                                + "\nActor: " + (currentLog.getActor() != null ? currentLog.getActor() : "N/A")
                                + "\nDescription: "
                                + (currentLog.getDescription() != null ? currentLog.getDescription() : "N/A")
                                + "\nLog ID: " + currentLog.getId());
                detail.getDialogPane().setMinWidth(450);
                detail.show();
            });
            HBox actionBox = new HBox(detailBtn);
            actionBox.setAlignment(Pos.CENTER_LEFT);
            actionBox.setPadding(new Insets(6, 16, 6, 16));
            actionBox.setStyle("-fx-border-color: " + BORDER() + "; -fx-border-width: 0 0 1 0;");
            elGrid.add(actionBox, 4, r + 1);
        }

        eventLogsCard.getChildren().addAll(elHdr, elGrid);

        page.getChildren().addAll(new VBox(4, title, sub), genCard, reportPreviewContainer, qStats, eventLogsCard);
        ScrollPane sp = new ScrollPane(page);
        sp.setFitToWidth(true);
        sp.setStyle("-fx-background: " + BG() + "; -fx-background-color: " + BG() + ";");
        return sp;
    }

    /**
     * Builds the dynamic report preview card based on the selected report type.
     */
    private VBox buildReportPreview(String reportType, java.util.List<Donation> allDonations,
            java.util.List<Child> allChildren, java.util.List<User> allUsers,
            java.util.List<SystemLog> allLogs,
            java.util.Map<Integer, String> childNames, java.util.Map<Integer, String> userNames,
            double totalDonated) {

        VBox reportCard = new VBox(0);
        reportCard.setStyle("-fx-background-color: " + CARD() + "; -fx-border-color: " + BORDER()
                + "; -fx-border-width: 1; -fx-background-radius: 8; -fx-border-radius: 8;");

        HBox rHdr = new HBox();
        rHdr.setPadding(new Insets(16));
        rHdr.setStyle("-fx-border-color: " + BORDER() + "; -fx-border-width: 0 0 1 0;");
        Label rTitle = new Label("Report Preview — " + reportType);
        rTitle.setFont(Font.font("Segoe UI", FontWeight.MEDIUM, 17));
        rTitle.setTextFill(Color.web(TEXT()));
        rHdr.getChildren().add(rTitle);
        reportCard.getChildren().add(rHdr);

        switch (reportType) {
            case "Donation & Financial Report" -> {
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
                reportCard.getChildren().add(summaryStats);

                GridPane donGrid = new GridPane();
                String[] donCols = { "Donor", "Child", "Amount", "Date" };
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
                            don.getDate() != null ? don.getDate() : ""
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
                reportCard.getChildren().add(donGrid);
            }
            case "Child Welfare Summary" -> {
                int activeCount = (int) allChildren.stream()
                        .filter(ch -> ch.getStatus() == null || ch.getStatus().equals("Active")).count();
                HBox summaryStats = new HBox(16);
                summaryStats.setPadding(new Insets(16));
                for (String[] s : new String[][] {
                        { "Total Children", String.valueOf(allChildren.size()), null },
                        { "Active", String.valueOf(activeCount), SECONDARY },
                        { "Organizations", String.valueOf(allChildren.stream()
                                .map(Child::getOrganization).filter(o -> o != null && !o.isEmpty())
                                .distinct().count()), PRIMARY }
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
                reportCard.getChildren().add(summaryStats);

                GridPane childGrid = new GridPane();
                String[] cols = { "Child ID", "Name", "Age", "Gender", "Organization", "Status" };
                for (int i = 0; i < cols.length; i++) {
                    Label h = new Label(cols[i]);
                    h.setFont(Font.font("Segoe UI", FontWeight.SEMI_BOLD, 11));
                    h.setPadding(new Insets(8, 12, 8, 12));
                    h.setStyle("-fx-background-color: " + MUTED() + ";");
                    h.setMaxWidth(Double.MAX_VALUE);
                    h.setTextFill(Color.web(TEXT()));
                    childGrid.add(h, i, 0);
                }
                for (int r = 0; r < allChildren.size(); r++) {
                    Child ch = allChildren.get(r);
                    String[] rowData = {
                            "CH-" + (1000 + ch.getId()), ch.getName(),
                            String.valueOf(ch.getAge()),
                            ch.getGender() != null ? ch.getGender() : "",
                            ch.getOrganization() != null ? ch.getOrganization() : "",
                            ch.getStatus() != null ? ch.getStatus() : "Active"
                    };
                    for (int c = 0; c < rowData.length; c++) {
                        Label cell = new Label(rowData[c]);
                        cell.setFont(Font.font(c == 0 ? "Consolas" : "Segoe UI", c == 0 ? 12 : 13));
                        cell.setTextFill(Color.web(TEXT()));
                        cell.setPadding(new Insets(8, 12, 8, 12));
                        cell.setStyle("-fx-border-color: " + BORDER() + "; -fx-border-width: 0 0 1 0;");
                        if (c == 5) {
                            Label badge = new Label(rowData[c]);
                            badge.setFont(Font.font("Segoe UI", 11));
                            String bgC = rowData[c].equals("Active") ? SECONDARY + "1A" : WARNING + "1A";
                            String fgC = rowData[c].equals("Active") ? SECONDARY : WARNING;
                            badge.setStyle("-fx-background-color: " + bgC + "; -fx-text-fill: " + fgC
                                    + "; -fx-background-radius: 4; -fx-padding: 2 8;");
                            HBox w = new HBox(badge);
                            w.setPadding(new Insets(8, 12, 8, 12));
                            w.setStyle("-fx-border-color: " + BORDER() + "; -fx-border-width: 0 0 1 0;");
                            childGrid.add(w, c, r + 1);
                            continue;
                        }
                        childGrid.add(cell, c, r + 1);
                    }
                }
                reportCard.getChildren().add(childGrid);
            }
            case "System Audit Log" -> {
                HBox summaryStats = new HBox(16);
                summaryStats.setPadding(new Insets(16));
                long reportCount = allLogs.stream()
                        .filter(l -> l.getEventType() != null && l.getEventType().equals("Report")).count();
                long exportCount = allLogs.stream()
                        .filter(l -> l.getEventType() != null && l.getEventType().equals("Export")).count();
                for (String[] s : new String[][] {
                        { "Total Entries", String.valueOf(allLogs.size()), null },
                        { "Reports Generated", String.valueOf(reportCount), PRIMARY },
                        { "Exports", String.valueOf(exportCount), SECONDARY }
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
                reportCard.getChildren().add(summaryStats);

                GridPane logGrid = new GridPane();
                String[] cols = { "Timestamp", "Event Type", "Actor", "Description" };
                for (int i = 0; i < cols.length; i++) {
                    Label h = new Label(cols[i]);
                    h.setFont(Font.font("Segoe UI", FontWeight.SEMI_BOLD, 11));
                    h.setPadding(new Insets(8, 12, 8, 12));
                    h.setStyle("-fx-background-color: " + MUTED() + ";");
                    h.setMaxWidth(Double.MAX_VALUE);
                    h.setTextFill(Color.web(TEXT()));
                    logGrid.add(h, i, 0);
                }
                int limit = Math.min(allLogs.size(), 50);
                for (int r = 0; r < limit; r++) {
                    SystemLog log = allLogs.get(r);
                    String[] rowData = {
                            log.getTimestamp() != null ? log.getTimestamp() : "",
                            log.getEventType() != null ? log.getEventType() : "",
                            log.getActor() != null ? log.getActor() : "",
                            log.getDescription() != null ? log.getDescription() : ""
                    };
                    for (int c = 0; c < rowData.length; c++) {
                        Label cell = new Label(rowData[c]);
                        cell.setFont(Font.font(c == 0 ? "Consolas" : "Segoe UI", c == 0 ? 11 : 13));
                        cell.setTextFill(Color.web(c == 0 ? MUTED_FG() : TEXT()));
                        cell.setPadding(new Insets(8, 12, 8, 12));
                        cell.setStyle("-fx-border-color: " + BORDER() + "; -fx-border-width: 0 0 1 0;");
                        logGrid.add(cell, c, r + 1);
                    }
                }
                reportCard.getChildren().add(logGrid);
            }
            case "Performance Analytics" -> {
                int totalUsers = allUsers.size();
                int activeUsers = (int) allUsers.stream().filter(User::isApproved).count();
                HBox summaryStats = new HBox(16);
                summaryStats.setPadding(new Insets(16));
                for (String[] s : new String[][] {
                        { "Total Users", String.valueOf(totalUsers), null },
                        { "Active Users", String.valueOf(activeUsers), SECONDARY },
                        { "Children in System", String.valueOf(allChildren.size()), PRIMARY },
                        { "Donation Records", String.valueOf(allDonations.size()), WARNING }
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
                reportCard.getChildren().add(summaryStats);

                // Performance metrics table
                GridPane perfGrid = new GridPane();
                String[] cols = { "Metric", "Value", "Status" };
                for (int i = 0; i < cols.length; i++) {
                    Label h = new Label(cols[i]);
                    h.setFont(Font.font("Segoe UI", FontWeight.SEMI_BOLD, 11));
                    h.setPadding(new Insets(8, 12, 8, 12));
                    h.setStyle("-fx-background-color: " + MUTED() + ";");
                    h.setMaxWidth(Double.MAX_VALUE);
                    h.setTextFill(Color.web(TEXT()));
                    perfGrid.add(h, i, 0);
                }
                String[][] metrics = {
                        { "User Approval Rate",
                                totalUsers > 0 ? (activeUsers * 100 / totalUsers) + "%" : "N/A", "Good" },
                        { "Total Donations Received",
                                String.format("\u09F3%,.0f", totalDonated), "Good" },
                        { "Average Donation",
                                allDonations.isEmpty() ? "\u09F30"
                                        : String.format("\u09F3%,.0f", totalDonated / allDonations.size()),
                                "Good" },
                        { "System Log Entries", String.valueOf(allLogs.size()), "Normal" },
                        { "Database Health", "Operational", "Good" }
                };
                for (int r = 0; r < metrics.length; r++) {
                    for (int c = 0; c < metrics[r].length; c++) {
                        Label cell = new Label(metrics[r][c]);
                        cell.setFont(Font.font("Segoe UI", c == 0 ? FontWeight.MEDIUM : FontWeight.NORMAL, 13));
                        cell.setTextFill(Color.web(TEXT()));
                        cell.setPadding(new Insets(10, 12, 10, 12));
                        cell.setStyle("-fx-border-color: " + BORDER() + "; -fx-border-width: 0 0 1 0;");
                        if (c == 2) {
                            Label badge = new Label(metrics[r][c]);
                            badge.setFont(Font.font("Segoe UI", 11));
                            badge.setStyle("-fx-background-color: " + SECONDARY + "1A; -fx-text-fill: " + SECONDARY
                                    + "; -fx-background-radius: 4; -fx-padding: 2 8;");
                            HBox w = new HBox(badge);
                            w.setPadding(new Insets(10, 12, 10, 12));
                            w.setStyle("-fx-border-color: " + BORDER() + "; -fx-border-width: 0 0 1 0;");
                            perfGrid.add(w, c, r + 1);
                            continue;
                        }
                        perfGrid.add(cell, c, r + 1);
                    }
                }
                reportCard.getChildren().add(perfGrid);
            }
        }

        return reportCard;
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
        String[] cols = { "User ID", "Name", "Email", "Role", "Status", "Actions" };
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
            String email = dbUser.getEmail() != null ? dbUser.getEmail() : dbUser.getUsername() + "@guardianlink.org";
            String role = dbUser.getRole().toString().replace("_", " ");
            String status = dbUser.isApproved() ? "Active" : "Pending";

            String[] u = new String[] { userId, name, email, role, status };

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
                if (c == 2)
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

            grid.add(actions, 5, rowCount);
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
        backBtn.setOnAction(e -> {
            isShowingForm = false;
            root.setCenter(buildAdminPage());
        });

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
        
        // Organization selection (shown if Organization Admin)
        Label orgLbl = new Label("Organization");
        orgLbl.setTextFill(Color.web(TEXT()));
        orgLbl.setVisible(false);
        
        ComboBox<String> orgCombo = new ComboBox<>();
        orgCombo.getItems().addAll("Hope Foundation", "Bright Future NGO", "Sunrise Academy", "Community Care Center", "Other");
        orgCombo.setPromptText("Select organization");
        orgCombo.setStyle(fieldStyle + " -fx-background-radius: 4;");
        orgCombo.setVisible(false);

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
            
            roleComboBox.valueProperty().addListener((obs, oldVal, newVal) -> {
                boolean isOrgAdmin = "Organization Admin".equals(newVal);
                orgLbl.setVisible(isOrgAdmin);
                orgCombo.setVisible(isOrgAdmin);
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
            
            if (userToEdit.getRole() == model.user.UserRole.ORGANIZATION_ADMIN) {
                orgLbl.setVisible(true);
                orgCombo.setVisible(true);
                if (userToEdit.getOrganization() != null) {
                    orgCombo.setValue(userToEdit.getOrganization());
                }
            }
        }
        
        form.add(orgLbl, 0, currentRow);
        form.add(orgCombo, 1, currentRow);
        currentRow++;

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
        // Set form flag to true when showing the form
        isShowingForm = true;

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
                    
                    if ("Organization Admin".equals(selectedRole)) {
                        newUser.setOrganization(orgCombo.getValue());
                    }
                    
                    boolean created = userService.createUser(newUser);
                    if (created) {
                        isShowingForm = false;
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
                
                if (userToEdit.getRole() == model.user.UserRole.ORGANIZATION_ADMIN) {
                    userToEdit.setOrganization(orgCombo.getValue());
                }

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
                isShowingForm = false;
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
        StackPane avatar = new StackPane();
        avatar.setPrefSize(64, 64);
        avatar.setStyle("-fx-background-color: " + PRIMARY + "1A; -fx-background-radius: 32;");
        if (child.getPhotoPath() != null && !child.getPhotoPath().isEmpty()) {
            try {
                javafx.scene.image.Image img = new javafx.scene.image.Image("file:" + child.getPhotoPath(), 64, 64, true, true);
                javafx.scene.image.ImageView iv = new javafx.scene.image.ImageView(img);
                iv.setFitWidth(64); iv.setFitHeight(64);
                javafx.scene.shape.Circle clip = new javafx.scene.shape.Circle(32, 32, 32);
                iv.setClip(clip);
                avatar.getChildren().add(iv);
            } catch (Exception ignored) {
                Label avatarLetter = new Label(name.substring(0, 1));
                avatarLetter.setTextFill(Color.web(PRIMARY));
                avatarLetter.setFont(Font.font("Segoe UI", FontWeight.BOLD, 24));
                avatar.getChildren().add(avatarLetter);
            }
        } else {
            Label avatarLetter = new Label(name.substring(0, 1));
            avatarLetter.setTextFill(Color.web(PRIMARY));
            avatarLetter.setFont(Font.font("Segoe UI", FontWeight.BOLD, 24));
            avatar.getChildren().add(avatarLetter);
        }

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

        editBtn.setStyle("-fx-background-color: " + CARD() + "; -fx-border-color: " + BORDER()
                + "; -fx-border-radius: 4; -fx-padding: 8 16; -fx-cursor: hand; -fx-text-fill: " + TEXT() + ";");
        editBtn.setOnAction(e -> root.setCenter(buildEditChildForm(childId)));
        delBtn.setStyle("-fx-background-color: " + CARD() + "; -fx-border-color: " + DESTRUCTIVE
                + "; -fx-border-radius: 4; -fx-padding: 8 16; -fx-cursor: hand; -fx-text-fill: " + DESTRUCTIVE + ";");
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

        HBox buttons = new HBox(12);
        
        Button resolve = new Button("Mark as Resolved");
        resolve.setStyle("-fx-background-color: " + util.ThemeManager.SECONDARY
                + "; -fx-text-fill: white; -fx-background-radius: 4; -fx-padding: 10 20; -fx-font-weight: bold; -fx-cursor: hand;");
        resolve.setOnAction(e -> {
            activeAlerts.remove(alert);
            root.setCenter(buildAlertsPage());
        });
        
        buttons.getChildren().add(resolve);
        
        // Add "Notify Donor" button for subscription alerts
        if (alert[1].contains("Subscription") && alert.length > 7) {
            Button notifyDonor = new Button("Notify Donor to Deposit");
            notifyDonor.setStyle("-fx-background-color: " + util.ThemeManager.PRIMARY
                    + "; -fx-text-fill: white; -fx-background-radius: 4; -fx-padding: 10 20; -fx-font-weight: bold; -fx-cursor: hand;");
            
            final String[] alertData = alert;
            notifyDonor.setOnAction(e -> {
                try {
                    int donorId = Integer.parseInt(alert[7]);
                    int donationId = Integer.parseInt(alert[6]);
                    User donor = userService.findById(donorId);
                    
                    if (donor != null && donor instanceof Donor) {
                        Donor donorUser = (Donor) donor;
                        // Get child info from alert
                        String childCode = alert[5];
                        String childId = childCode.substring(3); // Remove "CH-" prefix
                        Child child = childService.getChildById(Integer.parseInt(childId));
                        
                        // Create notification message
                        String message = "Your subscription for " + child.getName() + 
                            " is ending soon. Please deposit funds to continue supporting this child.";
                        String title_notif = "Subscription Renewal Required";
                        
                        // Create system log for this notification
                        SystemLog log = new SystemLog();
                        log.setEventType("DONOR_NOTIFICATION_SENT");
                        log.setDescription("Admin notified donor " + donorUser.getUsername() + 
                            " about subscription renewal for child " + child.getName());
                        log.setActor(user.getUsername());
                        log.setTimestamp(java.time.LocalDateTime.now().toString());
                        systemLogService.save(log);
                        
                        // Create notification
                        Notification notification = new Notification();
                        notification.setCaregiverId(donorId);
                        notification.setMessage(title_notif + ": " + message);
                        notification.setNotificationType("SUBSCRIPTION_RENEWAL");
                        notification.setChildName(child.getName());
                        notification.setChildId(child.getId());
                        notification.setRead(false);
                        notification.setTimestamp(java.time.LocalDateTime.now().toString());
                        notificationService.createNotification(notification);
                        
                        // Show success message
                        Alert successAlert = new Alert(Alert.AlertType.INFORMATION);
                        successAlert.setTitle("Success");
                        successAlert.setHeaderText(null);
                        successAlert.setContentText("Donor " + donorUser.getUsername() + " has been notified about the subscription renewal.");
                        successAlert.showAndWait();
                        
                        root.setCenter(buildAlertsPage());
                    }
                } catch (Exception ex) {
                    Alert errorAlert = new Alert(Alert.AlertType.ERROR);
                    errorAlert.setTitle("Error");
                    errorAlert.setHeaderText(null);
                    errorAlert.setContentText("Failed to notify donor: " + ex.getMessage());
                    errorAlert.showAndWait();
                }
            });
            
            buttons.getChildren().add(notifyDonor);
        }

        card.getChildren().addAll(typeLabel, title, desc, new Separator(), details, buttons);
        page.getChildren().addAll(backBtn, card);
        return wrapScroll(page);
    }

    // ═══════════ ADD CHILD FORM ═══════════
    private ScrollPane buildAddChildForm() {
        // Set form flag to prevent auto-refresh
        isShowingForm = true;
        
        VBox page = new VBox(20);
        page.setPadding(new Insets(24));

        Button backBtn = new Button("\u2190 Back to Profiles");
        backBtn.setStyle("-fx-background-color: transparent; -fx-text-fill: " + PRIMARY + "; -fx-cursor: hand;");
        backBtn.setOnAction(e -> {
            isShowingForm = false;
            root.setCenter(buildChildrenPage());
        });

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
        ageField.setPromptText("Age (auto-filled from DOB)");
        ageField.setEditable(false);  // Will be auto-filled from DOB
        ageField.setStyle("-fx-opacity: 0.9;");  // Slight opacity to indicate it's auto-filled
        ComboBox<String> genderBox = new ComboBox<>();
        genderBox.getItems().addAll("Male", "Female");
        genderBox.setPromptText("Gender");
        
        // Organization dropdown with predefined values
        ComboBox<String> orgBox = new ComboBox<>();
        orgBox.getItems().addAll("Hope Foundation", "Bright Future NGO", "Sunrise Academy", "Community Care Center", "Other");
        orgBox.setPromptText("Select Organization");
        orgBox.setMaxWidth(Double.MAX_VALUE);
        String comboStyle = "-fx-background-color: " + CARD() + "; -fx-border-color: " + BORDER()
                + "; -fx-border-width: 1; -fx-border-radius: 4; -fx-background-radius: 4; -fx-text-fill: " + TEXT()
                + "; -fx-font-size: 13px;";
        orgBox.setStyle(comboStyle);
        
        // Date picker with calendar for Date of Birth
        javafx.scene.control.DatePicker dobPicker = new javafx.scene.control.DatePicker();
        dobPicker.setPromptText("Select Date of Birth");
        dobPicker.setMaxWidth(Double.MAX_VALUE);
        String datePickerStyle = "-fx-background-color: " + CARD() + "; -fx-border-color: " + BORDER()
                + "; -fx-border-width: 1; -fx-border-radius: 4; -fx-background-radius: 4; -fx-text-fill: " + TEXT()
                + "; -fx-padding: 8 12; -fx-font-size: 13px;";
        dobPicker.setStyle(datePickerStyle);
        
        // Disable future dates in the date picker
        dobPicker.setDayCellFactory(picker -> new javafx.scene.control.DateCell() {
            @Override
            public void updateItem(LocalDate date, boolean empty) {
                super.updateItem(date, empty);
                if (date != null && date.isAfter(LocalDate.now())) {
                    setDisable(true);
                    setStyle("-fx-background-color: #ffcccc;");
                }
            }
        });
        
        // Add listener to auto-calculate age when DOB is selected
        dobPicker.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                int calculatedAge = calculateAge(newVal);
                ageField.setText(String.valueOf(calculatedAge));
            } else {
                ageField.setText("");
            }
        });
        
        // Status dropdown
        ComboBox<String> statusBox = new ComboBox<>();
        statusBox.getItems().addAll("Active", "Graduated", "Inactive");
        statusBox.setValue("Active");
        statusBox.setMaxWidth(Double.MAX_VALUE);
        statusBox.setStyle(comboStyle);
        
        // Caregiver assignment dropdown
        ComboBox<String> caregiverBox = new ComboBox<>();
        caregiverBox.setPromptText("Select Caregiver (Optional)");
        caregiverBox.setMaxWidth(Double.MAX_VALUE);
        
        // Populate with available caregivers
        java.util.List<User> allCaregivers = childService.getAllCaregivers();
        java.util.Map<String, Integer> caregiverMap = new java.util.HashMap<>();
        caregiverBox.getItems().add("-- No Assignment --");
        for (User cg : allCaregivers) {
            String displayName = cg.getUsername() + " (ID: " + cg.getId() + ")";
            caregiverBox.getItems().add(displayName);
            caregiverMap.put(displayName, cg.getId());
        }
        caregiverBox.setValue("-- No Assignment --");
        caregiverBox.setStyle(comboStyle);

        // === Photo Upload ===
        Button photoBtn = new Button("Select Photo");
        photoBtn.setMaxWidth(Double.MAX_VALUE);
        photoBtn.setStyle("-fx-background-color: transparent; -fx-border-color: " + PRIMARY + "; -fx-text-fill: " + PRIMARY
                + "; -fx-border-radius: 4; -fx-padding: 8 12; -fx-cursor: hand;");
        final String[] newPhotoPath = {null};
        photoBtn.setOnAction(ev -> {
            javafx.stage.FileChooser chooser = new javafx.stage.FileChooser();
            chooser.setTitle("Select Child Photo");
            chooser.getExtensionFilters().add(new javafx.stage.FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg"));
            java.io.File file = chooser.showOpenDialog(stage);
            if (file != null) { newPhotoPath[0] = file.getAbsolutePath(); photoBtn.setText("Selected: " + file.getName()); }
        });

        String fieldStyle = "-fx-background-color: " + CARD() + "; -fx-border-color: " + BORDER()
                + "; -fx-border-width: 1; -fx-border-radius: 4; -fx-background-radius: 4; -fx-padding: 8 12; -fx-text-fill: "
                + TEXT() + "; -fx-font-size: 13px; -fx-prompt-text-fill: " + ThemeManager.getMutedFg() + ";";
        nameField.setStyle(fieldStyle);
        ageField.setStyle(fieldStyle);
        genderBox.setStyle(fieldStyle);

        // Validation error label
        Label validationError = new Label();
        validationError.setStyle("-fx-text-fill: #d32f2f; -fx-font-size: 12px;");
        validationError.setVisible(false);
        validationError.setWrapText(true);

        Button saveBtn = new Button("Save Child");
        saveBtn.setStyle("-fx-background-color: " + PRIMARY
                + "; -fx-text-fill: white; -fx-background-radius: 4; -fx-padding: 10 24; -fx-font-size: 13px; -fx-cursor: hand; -fx-font-weight: bold;");
        saveBtn.setOnAction(e -> {
            // Clear previous error
            validationError.setVisible(false);
            validationError.setText("");
            
            String name = nameField.getText().trim();
            String ageText = ageField.getText().trim();
            if (name.isEmpty()) {
                validationError.setText("⚠ Name is required.");
                validationError.setVisible(true);
                return;
            }
            if (dobPicker.getValue() == null) {
                validationError.setText("⚠ Date of Birth is required.");
                validationError.setVisible(true);
                return;
            }
            
            int age;
            try {
                age = Integer.parseInt(ageText);
                if (age < 0 || age > 18) {
                    validationError.setText("⚠ Age must be between 0 and 18.");
                    validationError.setVisible(true);
                    return;
                }
            } catch (NumberFormatException ex) {
                validationError.setText("⚠ Age must be a valid number.");
                validationError.setVisible(true);
                return;
            }
            
            String organization = orgBox.getValue() != null ? orgBox.getValue() : "";
            String dateOfBirth = dobPicker.getValue() != null ? dobPicker.getValue().toString() : "";
            String gender = genderBox.getValue() != null ? genderBox.getValue() : "";
            String status = statusBox.getValue() != null ? statusBox.getValue() : "Active";
            
            try {
                Child newChild = new Child(name, age, organization, gender, dateOfBirth, status);
                newChild.setPhotoPath(newPhotoPath[0]);
                int childId = childService.addChild(newChild);
                
                if (childId > 0) {
                    // Assign caregiver if selected
                    if (!caregiverBox.getValue().equals("-- No Assignment --")) {
                        Integer caregiverId = caregiverMap.get(caregiverBox.getValue());
                        if (caregiverId != null) {
                            childService.assignCaregiverToChild(childId, caregiverId, user.getUsername());
                            systemLogService.save(new SystemLog("Caregiver Assignment",
                                    "Assigned child " + name + " to caregiver ID " + caregiverId, user.getUsername(),
                                    LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))));
                        }
                    }
                    
                    systemLogService.save(new SystemLog("Data Update",
                            "Added new child: " + name, user.getUsername(),
                            LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))));
                    
                    Alert success = new Alert(Alert.AlertType.INFORMATION, "Child added successfully!");
                    success.showAndWait();
                    isShowingForm = false;
                    root.setCenter(buildChildrenPage());
                } else {
                    validationError.setText("⚠ Failed to save child to database. Please try again.");
                    validationError.setVisible(true);
                }
            } catch (Exception ex) {
                validationError.setText("⚠ Error adding child: " + ex.getMessage());
                validationError.setVisible(true);
                ex.printStackTrace();
            }
        });
            
        // Reordered form fields: Name, DOB, Age, Gender, Organization, Status, Caregiver, Photo
        form.getChildren().addAll(
                validationError,
                formRow("Name", nameField), formRow("Date of Birth", dobPicker), 
                formRow("Age", ageField), formRow("Gender", genderBox), 
                formRow("Organization", orgBox), formRow("Status", statusBox),
                formRow("Assigned Caregiver", caregiverBox), formRow("Profile Photo", photoBtn), saveBtn);

        page.getChildren().addAll(backBtn, title, form);
        return wrapScroll(page);
    }

    // ═══════════ EDIT CHILD FORM ═══════════
    private ScrollPane buildEditChildForm(int childId) {
        // Set form flag to prevent auto-refresh
        isShowingForm = true;
        
        Child child = childService.getChildById(childId);
        if (child == null) {
            VBox err = new VBox(new Label("Child not found."));
            err.setPadding(new Insets(24));
            isShowingForm = false;
            return wrapScroll(err);
        }

        VBox page = new VBox(20);
        page.setPadding(new Insets(24));

        Button backBtn = new Button("\u2190 Back to Profile");
        backBtn.setStyle("-fx-background-color: transparent; -fx-text-fill: " + PRIMARY + "; -fx-cursor: hand;");
        backBtn.setOnAction(e -> {
            isShowingForm = false;
            root.setCenter(buildChildProfileDetailView(childId));
        });

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
        
        // Use DatePicker instead of TextField for DOB
        javafx.scene.control.DatePicker dobPicker = new javafx.scene.control.DatePicker();
        if (child.getDateOfBirth() != null && !child.getDateOfBirth().isEmpty()) {
            try {
                dobPicker.setValue(LocalDate.parse(child.getDateOfBirth()));
            } catch (Exception ex) {
                // If parsing fails, leave it empty
            }
        }
        dobPicker.setPromptText("Select Date of Birth");
        dobPicker.setMaxWidth(Double.MAX_VALUE);
        String datePickerStyle = "-fx-background-color: " + CARD() + "; -fx-border-color: " + BORDER()
                + "; -fx-border-width: 1; -fx-border-radius: 4; -fx-background-radius: 4; -fx-text-fill: " + TEXT()
                + "; -fx-padding: 8 12; -fx-font-size: 13px;";
        dobPicker.setStyle(datePickerStyle);
        
        // Disable future dates in the date picker
        dobPicker.setDayCellFactory(picker -> new javafx.scene.control.DateCell() {
            @Override
            public void updateItem(LocalDate date, boolean empty) {
                super.updateItem(date, empty);
                if (date != null && date.isAfter(LocalDate.now())) {
                    setDisable(true);
                    setStyle("-fx-background-color: #ffcccc;");
                }
            }
        });
        
        // Add listener to auto-calculate age when DOB is selected
        dobPicker.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                int calculatedAge = calculateAge(newVal);
                ageField.setText(String.valueOf(calculatedAge));
            }
        });
        
        ComboBox<String> statusBox = new ComboBox<>();
        statusBox.getItems().addAll("Active", "Graduated", "Inactive");
        statusBox.setValue(child.getStatus() != null ? child.getStatus() : "Active");
        
        // Apply combo box styling before populating caregivers
        String comboStyle = "-fx-background-color: " + CARD() + "; -fx-border-color: " + BORDER()
                + "; -fx-border-width: 1; -fx-border-radius: 4; -fx-background-radius: 4; -fx-text-fill: " + TEXT()
                + "; -fx-font-size: 13px;";
        genderBox.setStyle(comboStyle);
        statusBox.setStyle(comboStyle);
        
        // Caregiver assignment dropdown
        ComboBox<String> caregiverBox = new ComboBox<>();
        caregiverBox.setPromptText("Select Caregiver (Optional)");
        caregiverBox.setMaxWidth(Double.MAX_VALUE);
        
        // Populate with available caregivers
        java.util.List<User> allCaregivers = childService.getAllCaregivers();
        java.util.Map<String, Integer> caregiverMap = new java.util.HashMap<>();
        caregiverBox.getItems().add("-- No Assignment --");
        for (User cg : allCaregivers) {
            String displayName = cg.getUsername() + " (ID: " + cg.getId() + ")";
            caregiverBox.getItems().add(displayName);
            caregiverMap.put(displayName, cg.getId());
        }
        
        // Set current caregiver if assigned
        if (child.getAssignedCaregiverId() != null) {
            User currentCaregiver = userService.findById(child.getAssignedCaregiverId());
            if (currentCaregiver != null) {
                String currentDisplay = currentCaregiver.getUsername() + " (ID: " + currentCaregiver.getId() + ")";
                caregiverBox.setValue(currentDisplay);
            } else {
                caregiverBox.setValue("-- No Assignment --");
            }
        } else {
            caregiverBox.setValue("-- No Assignment --");
        }
        
        // Apply combo style to caregiver box
        caregiverBox.setStyle(comboStyle);

        // === Photo Upload ===
        Button photoBtn = new Button(child.getPhotoPath() != null ? "Change Photo" : "Select Photo");
        photoBtn.setMaxWidth(Double.MAX_VALUE);
        photoBtn.setStyle("-fx-background-color: transparent; -fx-border-color: " + PRIMARY + "; -fx-text-fill: " + PRIMARY
                + "; -fx-border-radius: 4; -fx-padding: 8 12; -fx-cursor: hand;");
        final String[] newPhotoPath = {child.getPhotoPath()};
        photoBtn.setOnAction(ev -> {
            javafx.stage.FileChooser chooser = new javafx.stage.FileChooser();
            chooser.setTitle("Select Child Photo");
            chooser.getExtensionFilters().add(new javafx.stage.FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg"));
            java.io.File file = chooser.showOpenDialog(stage);
            if (file != null) { newPhotoPath[0] = file.getAbsolutePath(); photoBtn.setText("Selected: " + file.getName()); }
        });

        String fieldStyle = "-fx-background-color: " + CARD() + "; -fx-border-color: " + BORDER()
                + "; -fx-border-width: 1; -fx-border-radius: 4; -fx-background-radius: 4; -fx-padding: 8 12; -fx-text-fill: "
                + TEXT() + "; -fx-font-size: 13px; -fx-prompt-text-fill: " + ThemeManager.getMutedFg() + ";";
        nameField.setStyle(fieldStyle);
        ageField.setStyle(fieldStyle);
        orgField.setStyle(fieldStyle);
        caregiverBox.setStyle(fieldStyle);

        // ═══════════ MEDICAL RECORD SECTION ═══════════
        Label medicalTitle = new Label("Medical Information");
        medicalTitle.setFont(Font.font("Segoe UI", FontWeight.BOLD, 15));
        medicalTitle.setTextFill(Color.web(TEXT()));
        
        ComboBox<String> medicalBloodGroup = new ComboBox<>();
        medicalBloodGroup.getItems().addAll("O+", "O-", "A+", "A-", "B+", "B-", "AB+", "AB-");
        medicalBloodGroup.setPromptText("Select Blood Type");
        medicalBloodGroup.setMaxWidth(Double.MAX_VALUE);
        medicalBloodGroup.setStyle(comboStyle);
        TextField medicalCondition = new TextField();
        medicalCondition.setPromptText("Medical Condition");
        medicalCondition.setStyle(fieldStyle);
        TextField medicalCheckup = new TextField();
        medicalCheckup.setPromptText("Last Checkup Date");
        medicalCheckup.setStyle(fieldStyle);
        
        // Load existing medical record if available
        java.util.List<MedicalRecord> medRecords = medicalRecordService.getRecordsByChildId(child.getId());
        if (!medRecords.isEmpty()) {
            MedicalRecord medRec = medRecords.get(0);
            if (medRec.getBloodGroup() != null && !medRec.getBloodGroup().isEmpty()) {
                medicalBloodGroup.setValue(medRec.getBloodGroup());
            }
            medicalCondition.setText(medRec.getMedicalCondition() != null ? medRec.getMedicalCondition() : "");
            medicalCheckup.setText(medRec.getLastCheckup() != null ? medRec.getLastCheckup() : "");
        }
        
        // ═══════════ EDUCATION RECORD SECTION ═══════════
        Label educationTitle = new Label("Education Information");
        educationTitle.setFont(Font.font("Segoe UI", FontWeight.BOLD, 15));
        educationTitle.setTextFill(Color.web(TEXT()));
        
        TextField eduSchool = new TextField();
        eduSchool.setPromptText("School Name");
        eduSchool.setStyle(fieldStyle);
        TextField eduGrade = new TextField();
        eduGrade.setPromptText("Grade/Class");
        eduGrade.setStyle(fieldStyle);
        TextField eduAttendance = new TextField();
        eduAttendance.setPromptText("Attendance %");
        eduAttendance.setStyle(fieldStyle);
        
        // Load existing education record if available
        java.util.List<EducationRecord> eduRecords = educationRecordService.getRecordsByChildId(child.getId());
        if (!eduRecords.isEmpty()) {
            EducationRecord eduRec = eduRecords.get(0);
            eduSchool.setText(eduRec.getSchoolName() != null ? eduRec.getSchoolName() : "");
            eduGrade.setText(eduRec.getGrade() != null ? eduRec.getGrade() : "");
            eduAttendance.setText(String.valueOf(eduRec.getAttendancePercentage()));
        }

        Button saveBtn = new Button("Update Child");
        saveBtn.setStyle("-fx-background-color: " + PRIMARY
                + "; -fx-text-fill: white; -fx-background-radius: 4; -fx-padding: 10 24; -fx-font-size: 13px; -fx-cursor: hand; -fx-font-weight: bold;");
        
        // Create a label for validation errors to display within the form instead of popup
        Label validationError = new Label();
        validationError.setStyle("-fx-text-fill: #d32f2f; -fx-font-size: 12px;");
        validationError.setVisible(false);
        validationError.setWrapText(true);
        
        saveBtn.setOnAction(e -> {
            try {
                // Clear previous error
                validationError.setVisible(false);
                validationError.setText("");
                
                String name = nameField.getText().trim();
                String ageText = ageField.getText().trim();
                if (name.isEmpty()) {
                    validationError.setText("⚠ Name is required.");
                    validationError.setVisible(true);
                    return;
                }
                if (dobPicker.getValue() == null) {
                    validationError.setText("⚠ Date of Birth is required.");
                    validationError.setVisible(true);
                    return;
                }
                
                int age;
                try {
                    age = Integer.parseInt(ageText);
                    if (age < 0 || age > 18) {
                        validationError.setText("⚠ Age must be between 0 and 18.");
                        validationError.setVisible(true);
                        return;
                    }
                } catch (NumberFormatException ex) {
                    validationError.setText("⚠ Age must be a valid number.");
                    validationError.setVisible(true);
                    return;
                }
                
                // Track previous caregiver for notifications
                Integer previousCaregiverId = child.getAssignedCaregiverId();
                String selectedCaregiver = caregiverBox.getValue();
                Integer newCaregiverId = null;
                
                if (!selectedCaregiver.equals("-- No Assignment --")) {
                    newCaregiverId = caregiverMap.get(selectedCaregiver);
                }
                
                // Update child details
                child.setName(name);
                child.setAge(age);
                child.setGender(genderBox.getValue());
                child.setOrganization(orgField.getText().trim());
                child.setDateOfBirth(dobPicker.getValue() != null ? dobPicker.getValue().toString() : "");
                child.setStatus(statusBox.getValue());
                child.setPhotoPath(newPhotoPath[0]);
                
                // Handle caregiver changes
                if (previousCaregiverId != null && newCaregiverId == null) {
                    // Caregiver was removed
                    childService.removeCaregiverFromChild(childId, user.getUsername());
                    child.setAssignedCaregiverId(null);
                } else if (newCaregiverId != null && (previousCaregiverId == null || !previousCaregiverId.equals(newCaregiverId))) {
                    // Caregiver was assigned or changed
                    child.setAssignedCaregiverId(newCaregiverId);
                    childService.assignCaregiverToChild(childId, newCaregiverId, user.getUsername());
                    systemLogService.save(new SystemLog("Caregiver Assignment",
                            "Assigned child " + name + " to caregiver ID " + newCaregiverId, user.getUsername(),
                            LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))));
                } else {
                    child.setAssignedCaregiverId(newCaregiverId);
                }
                
                childService.updateChild(child);
                
                // Save medical record
                String bloodGroup = medicalBloodGroup.getValue() != null ? medicalBloodGroup.getValue() : "";
                String medCondition = medicalCondition.getText().trim();
                String lastCheckup = medicalCheckup.getText().trim();
                if (!bloodGroup.isEmpty() || !medCondition.isEmpty() || !lastCheckup.isEmpty()) {
                    java.util.List<MedicalRecord> existingMed = medicalRecordService.getRecordsByChildId(childId);
                    if (existingMed.isEmpty()) {
                        medicalRecordService.addRecord(new MedicalRecord(childId, bloodGroup, medCondition, lastCheckup));
                    } else {
                        MedicalRecord medRec = existingMed.get(0);
                        medRec.setBloodGroup(bloodGroup);
                        medRec.setMedicalCondition(medCondition);
                        medRec.setLastCheckup(lastCheckup);
                        medicalRecordService.updateRecord(medRec);
                    }
                }
                
                // Save education record
                String schoolName = eduSchool.getText().trim();
                String grade = eduGrade.getText().trim();
                String attendanceStr = eduAttendance.getText().trim();
                if (!schoolName.isEmpty() || !grade.isEmpty() || !attendanceStr.isEmpty()) {
                    double attendance = 0;
                    try {
                        attendance = Math.min(100, Math.max(0, Double.parseDouble(attendanceStr))); // Clamp to 0-100
                    } catch (NumberFormatException ex) {
                        /* keep as 0 */ }
                    java.util.List<EducationRecord> existingEdu = educationRecordService.getRecordsByChildId(childId);
                    if (existingEdu.isEmpty()) {
                        educationRecordService.addRecord(new EducationRecord(childId, schoolName, grade, attendance));
                    } else {
                        EducationRecord eduRec = existingEdu.get(0);
                        eduRec.setSchoolName(schoolName);
                        eduRec.setGrade(grade);
                        eduRec.setAttendancePercentage(attendance);
                        educationRecordService.updateRecord(eduRec);
                    }
                }
                systemLogService.save(new SystemLog("Data Update",
                        "Updated child profile: " + name, user.getUsername(),
                        LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))));
                
                // Use showAndWait to ensure completion before navigating
                Alert success = new Alert(Alert.AlertType.INFORMATION, "Child updated successfully!");
                success.showAndWait();
                isShowingForm = false;
                root.setCenter(buildChildProfileDetailView(childId));
            } catch (Exception ex) {
                validationError.setText("⚠ Error updating child: " + ex.getMessage());
                validationError.setVisible(true);
                ex.printStackTrace();
            }
        });

        // Reordered form fields: Name, DOB, Age, Gender, Organization, Status, Caregiver, Photo
        form.getChildren().addAll(
                validationError,
                formRow("Name", nameField), formRow("Date of Birth", dobPicker),
                formRow("Age", ageField), formRow("Gender", genderBox), 
                formRow("Organization", orgField), formRow("Status", statusBox),
                formRow("Assigned Caregiver", caregiverBox), formRow("Profile Photo", photoBtn), new Separator(),
                medicalTitle, formRow("Blood Group", medicalBloodGroup), formRow("Medical Condition", medicalCondition),
                formRow("Last Checkup", medicalCheckup), new Separator(),
                educationTitle, formRow("School Name", eduSchool), formRow("Grade", eduGrade),
                formRow("Attendance %", eduAttendance), new Separator(), saveBtn);


        page.getChildren().addAll(backBtn, title, form);
        return wrapScroll(page);
    }

    /**
     * Calculate age from LocalDate
     */
    private int calculateAge(LocalDate dateOfBirth) {
        if (dateOfBirth == null) return 0;
        return Period.between(dateOfBirth, LocalDate.now()).getYears();
    }

    /**
     * Filter alerts based on the current user's role
     */
    private java.util.List<String[]> getAlertsForRole() {
        java.util.List<String[]> roleSpecificAlerts = new java.util.ArrayList<>();
        UserRole userRole = user.getRole();
        
        // Add dynamic subscription alerts for system admins
        if (userRole == UserRole.SYSTEM_ADMIN) {
            java.util.List<String[]> subAlerts = generateSubscriptionAlerts();
            roleSpecificAlerts.addAll(subAlerts);
        }
        
        for (String[] alert : activeAlerts) {
            String title = alert[1];
            String description = alert[4];
            
            switch (userRole) {
                case SYSTEM_ADMIN:
                    // Admins see all alerts
                    roleSpecificAlerts.add(alert);
                    break;
                    
                case ORGANIZATION_ADMIN:
                    // Organization admins see child-related, data update, and general alerts
                    if (title.contains("Child") || title.contains("Profile") || 
                        title.contains("attendance") || title.contains("Medical") ||
                        title.contains("Expense") || title.contains("Education") ||
                        description.contains("child") || description.contains("profile")) {
                        roleSpecificAlerts.add(alert);
                    }
                    break;
                    
                case CAREGIVER:
                    // Caregivers see alerts about children they might be assigned to
                    if (title.contains("Child") || title.contains("Missed") || 
                        title.contains("Medical") || title.contains("Attendance") ||
                        title.contains("Appointment") || description.contains("child")) {
                        roleSpecificAlerts.add(alert);
                    }
                    break;
                    
                case DONOR:
                    // Donors see donation and sponsorship-related alerts
                    if (title.contains("Donation") || title.contains("Donor") || 
                        title.contains("Sponsorship") || title.contains("Wallet") ||
                        description.contains("donation") || description.contains("sponsorship")) {
                        roleSpecificAlerts.add(alert);
                    }
                    break;
                    
                case SUPPORT:
                    // Support staff see incident and system alerts
                    if (title.contains("Incident") || title.contains("System") || 
                        title.contains("Error") || title.contains("hacker") ||
                        description.contains("error") || description.contains("system")) {
                        roleSpecificAlerts.add(alert);
                    }
                    break;
            }
        }
        
        return roleSpecificAlerts;
    }

    /**
     * Generate real-time alerts for subscriptions ending within 7 days
     */
    private java.util.List<String[]> generateSubscriptionAlerts() {
        java.util.List<String[]> subscriptionAlerts = new java.util.ArrayList<>();
        java.time.LocalDate today = java.time.LocalDate.now();
        java.time.LocalDate sevenDaysFromNow = today.plusDays(7);
        
        java.util.List<Donation> allDonations = donationService.getAll();
        if (allDonations == null || allDonations.isEmpty()) {
            return subscriptionAlerts;
        }
        
        for (Donation donation : allDonations) {
            if (donation.isRecurring() && donation.getEndDate() != null && !donation.getEndDate().isEmpty()) {
                try {
                    java.time.LocalDate endDate = java.time.LocalDate.parse(donation.getEndDate());
                    
                    // If subscription ends within 7 days
                    if (!endDate.isBefore(today) && !endDate.isAfter(sevenDaysFromNow)) {
                        Child child = childService.getChildById(donation.getChildId());
                        Donor donor = (Donor) userService.findById(donation.getDonorId());
                        
                        if (child != null && donor != null) {
                            long daysUntilExpiry = java.time.temporal.ChronoUnit.DAYS.between(today, endDate);
                            String title = "Subscription Ending Soon";
                            String id = "ALT-SUB-" + donation.getId();
                            String timestamp = daysUntilExpiry + " day(s) remaining";
                            String description = "Child " + child.getName() + " (CH-" + child.getId() + 
                                ") subscription ending in " + daysUntilExpiry + " day(s). Donor: " + donor.getUsername();
                            String childId = "CH-" + child.getId();
                            
                            subscriptionAlerts.add(new String[]{
                                "critical",
                                title,
                                id,
                                timestamp,
                                description,
                                childId,
                                String.valueOf(donation.getId()),
                                String.valueOf(donation.getDonorId())
                            });
                        }
                    }
                } catch (Exception e) {
                    // Skip invalid date formats
                }
            }
        }
        
        return subscriptionAlerts;
    }

    private HBox formRow(String label, Node field) {
        HBox row = new HBox(12);
        row.setAlignment(Pos.CENTER_LEFT);
        Label l = new Label(label);
        l.setFont(Font.font("Segoe UI", FontWeight.MEDIUM, 13));
        l.setTextFill(Color.web(TEXT()));
        l.setPrefWidth(120);
        if (field instanceof Region r) {
            HBox.setHgrow(r, Priority.ALWAYS);
        }
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
        } catch (IOException e) {
            System.err.println("Failed to apply dark mode stylesheet: " + e.getMessage());
        }
    }
}