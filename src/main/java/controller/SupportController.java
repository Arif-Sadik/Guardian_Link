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
import service.DonationService;
import service.ChildService;
import service.UserService;
import service.MedicalRecordService;
import service.EducationRecordService;
import service.NotificationService;
import model.entity.Donation;
import model.entity.Child;
import model.entity.Notification;
import util.ThemeManager;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import javafx.stage.FileChooser;

/**
 * Support staff dashboard.
 * Sidebar pages: Dashboard, Alerts, Incident Reports.
 */
public class SupportController {

    private final Stage stage;
    private final User user;
    private BorderPane root;
    private Scene scene;
    private VBox sidebar;
    private String activePage = "dashboard";

    private final SystemLogService systemLogService = new SystemLogService();
    private final DonationService donationService = new DonationService();
    private final ChildService childService = new ChildService();
    private final UserService userService = new UserService();
    private final MedicalRecordService medicalRecordService = new MedicalRecordService();
    private final EducationRecordService educationRecordService = new EducationRecordService();
    private final NotificationService notificationService = new NotificationService();

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
        scene = new Scene(root, 1280, 800);
        
        // Apply dark mode styles for DatePicker visibility
        applyDarkModeStylesheet(scene);
        
        stage.setScene(scene);
        stage.setTitle("GuardianLink \u2014 Support Dashboard");
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
        switch (activePage) {
            case "dashboard" -> root.setCenter(buildDashboardPage());
            case "alerts" -> root.setCenter(buildAlertsPage());
            case "donors" -> root.setCenter(buildDonorInquiriesPage());
            case "caregivers" -> root.setCenter(buildCaregiverSupportPage());
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
        
        // Make user box clickable to show profile
        userBox.setStyle(userBox.getStyle() + "; -fx-cursor: hand;");
        userBox.setOnMouseClicked(e -> root.setCenter(buildProfilePage()));

        header.getChildren().addAll(logoIcon, titleBox, spacer, userBox);
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
                sidebarBtn("System Alerts", "alerts"),
                sidebarBtn("Donor Inquiries", "donors"),
                sidebarBtn("Caregiver Support", "caregivers"),
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
                case "donors" -> root.setCenter(buildDonorInquiriesPage());
                case "caregivers" -> root.setCenter(buildCaregiverSupportPage());
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
        if (text.contains("Alert"))
            return "alerts";
        if (text.contains("Donor"))
            return "donors";
        if (text.contains("Caregiver"))
            return "caregivers";
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

    // ═══════════ SYSTEM ALERTS PAGE (FEATURE 5 - ENHANCED) ═══════════
    private ScrollPane buildAlertsPage() {
        VBox page = new VBox(20);
        page.setPadding(new Insets(24));

        Label title = new Label("System Monitoring & Alerts");
        title.setFont(Font.font("Segoe UI", FontWeight.MEDIUM, 20));
        title.setTextFill(Color.web(TEXT()));
        Label sub = new Label("Monitor system health, errors, and suspicious activities");
        sub.setFont(Font.font("Segoe UI", 13));
        sub.setTextFill(Color.web(MUTED_FG()));

        List<SystemLog> allLogs = systemLogService.getRecent(50);

        // Alert categories
        long errorCount = allLogs.stream()
                .filter(l -> l.getEventType() != null && l.getEventType().contains("Error"))
                .count();
        long warningCount = allLogs.stream()
                .filter(l -> l.getEventType() != null && l.getEventType().contains("Warning"))
                .count();
        long deleteCount = allLogs.stream()
                .filter(l -> l.getEventType() != null && l.getEventType().contains("Delete"))
                .count();

        HBox stats = new HBox(16);
        stats.getChildren().addAll(
                statCard("Errors", String.valueOf(errorCount), "Critical issues", DESTRUCTIVE),
                statCard("Warnings", String.valueOf(warningCount), "Potential issues", WARNING),
                statCard("Deletions", String.valueOf(deleteCount), "Delete operations", DESTRUCTIVE));

        // Alert feed
        VBox alertsFeed = new VBox(12);
        if (allLogs.isEmpty()) {
            Label noAlerts = new Label("No system alerts at this time. Everything is working smoothly!");
            noAlerts.setFont(Font.font("Segoe UI", 13));
            noAlerts.setTextFill(Color.web(SECONDARY));
            noAlerts.setPadding(new Insets(24));
            alertsFeed.getChildren().add(noAlerts);
        } else {
            for (SystemLog log : allLogs) {
                String eventType = log.getEventType() != null ? log.getEventType() : "Info";
                boolean isError = eventType.contains("Error") || eventType.contains("Delete");
                boolean isWarning = eventType.contains("Warning");
                String borderC = isError ? DESTRUCTIVE : isWarning ? WARNING : BORDER();
                String badgeColor = isError ? DESTRUCTIVE : isWarning ? WARNING : INFO;

                VBox card = new VBox(8);
                card.setPadding(new Insets(12));
                card.setStyle("-fx-background-color: " + CARD() + "; -fx-border-color: " + borderC
                        + "; -fx-border-width: 1; -fx-border-radius: 8; -fx-background-radius: 8;");

                HBox top = new HBox(12);
                top.setAlignment(Pos.CENTER_LEFT);

                Label typeBadge = new Label(isError ? "🚨 " + eventType : isWarning ? "⚠️ " + eventType : "ℹ️ " + eventType);
                typeBadge.setFont(Font.font("Segoe UI", FontWeight.BOLD, 11));
                typeBadge.setStyle("-fx-background-color: " + badgeColor + "1A; -fx-text-fill: " + badgeColor
                        + "; -fx-background-radius: 4; -fx-padding: 2 8;");

                Label desc = new Label(log.getDescription() != null ? log.getDescription() : "No description");
                desc.setFont(Font.font("Segoe UI", 12));
                desc.setTextFill(Color.web(TEXT()));
                desc.setWrapText(true);
                HBox.setHgrow(desc, Priority.ALWAYS);

                top.getChildren().addAll(typeBadge, desc);

                HBox bottom = new HBox(12);
                Label actor = new Label("🔑 " + (log.getActor() != null ? log.getActor() : "System"));
                actor.setFont(Font.font("Segoe UI", 11));
                actor.setTextFill(Color.web(MUTED_FG()));

                Label time = new Label(log.getTimestamp() != null ? log.getTimestamp() : "");
                time.setFont(Font.font("Segoe UI", 11));
                time.setTextFill(Color.web(MUTED_FG()));

                bottom.getChildren().addAll(actor, new Region(), time);
                HBox.setHgrow(bottom.getChildren().get(1), Priority.ALWAYS);

                card.getChildren().addAll(top, bottom);
                alertsFeed.getChildren().add(card);
            }
        }

        page.getChildren().addAll(new VBox(4, title, sub), stats, alertsFeed);
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
        
        // Validation error label
        Label validationError = new Label();
        validationError.setStyle("-fx-text-fill: #d32f2f; -fx-font-size: 12px;");
        validationError.setVisible(false);
        validationError.setWrapText(true);

        Button submit = new Button("Submit Incident Report");
        submit.setStyle("-fx-background-color: " + PRIMARY
                + "; -fx-text-fill: white; -fx-background-radius: 4; -fx-padding: 10 24; -fx-font-weight: bold; -fx-cursor: hand;");
        submit.setOnAction(e -> {
            // Clear previous error
            validationError.setVisible(false);
            validationError.setText("");
            
            String descText = descArea.getText().trim();
            if (descText.isEmpty()) {
                validationError.setText("⚠ Please enter a description.");
                validationError.setVisible(true);
                return;
            }
            String logDesc = "[" + severity.getValue() + "] " + category.getValue() + ": " + descText;
            systemLogService.save(new SystemLog("Incident", logDesc, user.getUsername(),
                    LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))));
            Alert success = new Alert(Alert.AlertType.INFORMATION, "Incident report submitted successfully!");
            success.showAndWait();
            root.setCenter(buildReportsPage());
        });

        formCard.getChildren().addAll(catLabel, category, sevLabel, severity, descLabel, descArea, validationError, new Separator(),
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

    // ═══════════ DONOR INQUIRIES PAGE (FEATURE 3) ═══════════
    private ScrollPane buildDonorInquiriesPage() {
        VBox page = new VBox(20);
        page.setPadding(new Insets(24));

        Label title = new Label("Donor Inquiries");
        title.setFont(Font.font("Segoe UI", FontWeight.MEDIUM, 20));
        title.setTextFill(Color.web(TEXT()));
        Label sub = new Label("View all donors and assist with their inquiries");
        sub.setFont(Font.font("Segoe UI", 13));
        sub.setTextFill(Color.web(MUTED_FG()));

        // Donor search
        HBox searchBox = new HBox(8);
        searchBox.setAlignment(Pos.CENTER_LEFT);
        TextField searchField = new TextField();
        searchField.setPromptText("Search donor by username or ID (optional)...");
        searchField.setPrefWidth(300);

        Button clearBtn = new Button("Clear");
        clearBtn.setStyle("-fx-background-color: transparent; -fx-text-fill: " + SECONDARY
                + "; -fx-border-color: " + SECONDARY
                + "; -fx-border-radius: 4; -fx-padding: 8 16; -fx-cursor: hand;");

        searchBox.getChildren().addAll(searchField, clearBtn);

        // Results area
        ScrollPane donorDetails = new ScrollPane();
        donorDetails.setPrefHeight(400);
        donorDetails.setFitToWidth(true);
        donorDetails.setStyle("-fx-background-color: " + BG() + ";");
        
        // Helper method to display donors
        Runnable displayDonors = () -> {
            String search = searchField.getText().trim().toLowerCase();
            
            List<model.user.Donor> donors = userService.getAllUsers().stream()
                    .filter(u -> u instanceof model.user.Donor)
                    .map(u -> (model.user.Donor) u)
                    .filter(d -> search.isEmpty() || d.getUsername().toLowerCase().contains(search)
                            || String.valueOf(d.getId()).contains(search))
                    .toList();

            if (!search.isEmpty() && donors.isEmpty()) {
                VBox noResults = new VBox(12);
                noResults.setPadding(new Insets(24));
                noResults.setAlignment(Pos.CENTER);
                Label noMsg = new Label("❌ No donors found matching: \"" + searchField.getText() + "\"");
                noMsg.setFont(Font.font("Segoe UI", 13));
                noMsg.setTextFill(Color.web(MUTED_FG()));
                noResults.getChildren().add(noMsg);
                donorDetails.setContent(noResults);
                return;
            }

            // Use TilePane for 3-column layout
            javafx.scene.layout.TilePane tilePane = new javafx.scene.layout.TilePane();
            tilePane.setPrefColumns(3);
            tilePane.setHgap(16);
            tilePane.setVgap(16);
            tilePane.setPadding(new Insets(8));
            tilePane.setStyle("-fx-background-color: " + BG() + ";");
            
            for (model.user.Donor donor : donors) {
                VBox donorCard = new VBox(8);
                donorCard.setPrefWidth(320);
                donorCard.setMinHeight(240);
                donorCard.setPadding(new Insets(12));
                donorCard.setStyle("-fx-background-color: " + CARD() + "; -fx-border-color: " + BORDER()
                        + "; -fx-border-width: 1; -fx-border-radius: 8; -fx-background-radius: 8;");

                Label dName = new Label("👤 " + donor.getUsername());
                dName.setFont(Font.font("Segoe UI", FontWeight.BOLD, 14));
                dName.setTextFill(Color.web(TEXT()));

                Label dEmail = new Label("Email: " + (donor.getEmail() != null ? donor.getEmail() : "Not set"));
                dEmail.setFont(Font.font("Segoe UI", 12));
                dEmail.setTextFill(Color.web(MUTED_FG()));

                Label dPhone = new Label("Phone: " + (donor.getPhoneNumber() != null ? donor.getPhoneNumber() : "Not set"));
                dPhone.setFont(Font.font("Segoe UI", 12));
                dPhone.setTextFill(Color.web(MUTED_FG()));

                // Donation stats
                List<Donation> donations = donationService.getByDonorId(donor.getId());
                double totalDonated = donationService.getTotalByDonorId(donor.getId());
                int childrenSupported = donationService.countChildrenByDonorId(donor.getId());

                HBox stats = new HBox(16);
                stats.setPadding(new Insets(8, 0, 0, 0));
                Label stat1 = new Label("Donations: " + donations.size());
                stat1.setFont(Font.font("Segoe UI", 12));
                stat1.setTextFill(Color.web(PRIMARY));
                Label stat2 = new Label("Total: $" + String.format("%.2f", totalDonated));
                stat2.setFont(Font.font("Segoe UI", FontWeight.BOLD, 12));
                stat2.setTextFill(Color.web(SECONDARY));
                Label stat3 = new Label("Children: " + childrenSupported);
                stat3.setFont(Font.font("Segoe UI", 12));
                stat3.setTextFill(Color.web(PRIMARY));
                stats.getChildren().addAll(stat1, stat2, stat3);

                // Buttons
                HBox buttonBox = new HBox(8);
                buttonBox.setPadding(new Insets(4, 0, 0, 0));
                
                Button viewDonationsBtn = new Button("💰 Donations");
                viewDonationsBtn.setStyle("-fx-background-color: " + INFO
                        + "; -fx-text-fill: white; -fx-padding: 4 10; -fx-background-radius: 4; -fx-cursor: hand; -fx-font-size: 10px;");
                viewDonationsBtn.setMaxWidth(Double.MAX_VALUE);
                viewDonationsBtn.setOnAction(ev -> showDonationHistory(donor));
                
                Button viewInquiriesBtn = new Button("❓ Inquiries");
                viewInquiriesBtn.setStyle("-fx-background-color: " + SECONDARY
                        + "; -fx-text-fill: white; -fx-padding: 4 10; -fx-background-radius: 4; -fx-cursor: hand; -fx-font-size: 10px;");
                viewInquiriesBtn.setMaxWidth(Double.MAX_VALUE);
                viewInquiriesBtn.setOnAction(ev -> showDonorInquiries(donor));
                
                HBox.setHgrow(viewDonationsBtn, Priority.ALWAYS);
                HBox.setHgrow(viewInquiriesBtn, Priority.ALWAYS);
                buttonBox.getChildren().addAll(viewDonationsBtn, viewInquiriesBtn);

                donorCard.getChildren().addAll(dName, dEmail, dPhone, stats, buttonBox);
                tilePane.getChildren().add(donorCard);
            }
            donorDetails.setContent(tilePane);
        };
        
        // Search with text change listener
        searchField.textProperty().addListener((obs, oldVal, newVal) -> displayDonors.run());
        
        // Clear button  
        clearBtn.setOnAction(e -> {
            searchField.clear();
            displayDonors.run();
        });
        
        // Display all donors on load
        displayDonors.run();

        page.getChildren().addAll(new VBox(4, title, sub), searchBox, donorDetails);
        return wrapScroll(page);
    }

    private void showDonationHistory(model.user.Donor donor) {
        Dialog<String> dialog = new Dialog<>();
        dialog.setTitle("Donation History - " + donor.getUsername());
        dialog.setHeaderText("All donations from this donor");

        VBox content = new VBox(12);
        content.setPadding(new Insets(16));

        List<Donation> donations = donationService.getByDonorId(donor.getId());

        if (donations.isEmpty()) {
            content.getChildren().add(new Label("No donations from this donor yet."));
        } else {
            double totalAmount = 0;
            for (Donation d : donations) {
                Child child = childService.getChildById(d.getChildId());
                String childName = child != null ? child.getName() : "Unknown (ID: " + d.getChildId() + ")";

                VBox donationItem = new VBox(4);
                donationItem.setPadding(new Insets(8));
                donationItem.setStyle("-fx-background-color: " + MUTED() + "; -fx-border-radius: 4; -fx-background-radius: 4;");

                Label cName = new Label("Child: " + childName);
                cName.setFont(Font.font("Segoe UI", FontWeight.MEDIUM, 12));
                cName.setTextFill(Color.web(TEXT()));

                Label amount = new Label("Amount: $" + String.format("%.2f", d.getAmount()));
                amount.setFont(Font.font("Segoe UI", FontWeight.BOLD, 12));
                amount.setTextFill(Color.web(SECONDARY));

                Label purpose = new Label("Purpose: " + (d.getPurpose() != null ? d.getPurpose() : "General"));
                purpose.setFont(Font.font("Segoe UI", 11));
                purpose.setTextFill(Color.web(MUTED_FG()));

                Label date = new Label("Date: " + (d.getDate() != null ? d.getDate() : "N/A"));
                date.setFont(Font.font("Segoe UI", 11));
                date.setTextFill(Color.web(MUTED_FG()));

                donationItem.getChildren().addAll(cName, amount, purpose, date);
                content.getChildren().add(donationItem);

                totalAmount += d.getAmount();
            }

            Separator sep = new Separator();
            Label totalLabel = new Label("Total Donated: $" + String.format("%.2f", totalAmount));
            totalLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 13));
            totalLabel.setTextFill(Color.web(PRIMARY));

            content.getChildren().addAll(sep, totalLabel);
        }

        ScrollPane sp = new ScrollPane(content);
        sp.setStyle("-fx-background-color: " + BG() + ";");
        dialog.getDialogPane().setContent(sp);
        dialog.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);
        dialog.showAndWait();
    }

    private void showDonorInquiries(model.user.Donor donor) {
        Dialog<String> dialog = new Dialog<>();
        dialog.setTitle("Inquiry History - " + donor.getUsername());
        dialog.setHeaderText("All inquiries submitted by this donor");

        VBox content = new VBox(12);
        content.setPadding(new Insets(16));

        // Get all donor inquiries from SystemLog
        List<SystemLog> allLogs = systemLogService.getRecent(200);
        List<SystemLog> donorInquiries = allLogs.stream()
                .filter(log -> log.getEventType() != null && log.getEventType().equals("Donor Inquiry"))
                .filter(log -> log.getActor() != null && log.getActor().equals(donor.getUsername()))
                .toList();

        if (donorInquiries.isEmpty()) {
            Label noInquiries = new Label("No inquiries submitted by this donor yet.");
            noInquiries.setFont(Font.font("Segoe UI", 12));
            noInquiries.setTextFill(Color.web(MUTED_FG()));
            content.getChildren().add(noInquiries);
        } else {
            // Reverse to show newest first
            for (SystemLog log : donorInquiries.reversed()) {
                VBox inquiryItem = new VBox(6);
                inquiryItem.setPadding(new Insets(12));
                inquiryItem.setStyle("-fx-background-color: " + MUTED() + "; -fx-border-radius: 4; -fx-background-radius: 4;");

                // Timestamp
                Label timestamp = new Label("📅 " + (log.getTimestamp() != null ? log.getTimestamp() : "N/A"));
                timestamp.setFont(Font.font("Segoe UI", 10));
                timestamp.setTextFill(Color.web(MUTED_FG()));

                // Description contains "[Category] Subject - Message"
                String desc = log.getDescription() != null ? log.getDescription() : "";
                
                // Parse the category and message
                String category = "General";
                String subjectAndMsg = desc;
                
                if (desc.startsWith("[")) {
                    int endBracket = desc.indexOf("]");
                    if (endBracket > 0) {
                        category = desc.substring(1, endBracket);
                        subjectAndMsg = desc.substring(endBracket + 2); // Skip "] "
                    }
                }
                
                // Extract subject and message preview
                String[] parts = subjectAndMsg.split(" - ", 2);
                String subject = parts.length > 0 ? parts[0] : "No subject";
                String messagePreview = parts.length > 1 ? parts[1] : "";

                // Category badge
                Label categoryLabel = new Label("📁 " + category);
                categoryLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 11));
                categoryLabel.setTextFill(Color.web(PRIMARY));

                // Subject
                Label subjectLabel = new Label(subject);
                subjectLabel.setFont(Font.font("Segoe UI", FontWeight.SEMI_BOLD, 12));
                subjectLabel.setTextFill(Color.web(TEXT()));
                subjectLabel.setWrapText(true);

                // Message preview
                Label messageLabel = new Label(messagePreview);
                messageLabel.setFont(Font.font("Segoe UI", 11));
                messageLabel.setTextFill(Color.web(TEXT()));
                messageLabel.setWrapText(true);

                // Reply button
                Button replyBtn = new Button("Reply to Donor");
                replyBtn.setStyle("-fx-background-color: " + PRIMARY + "; -fx-text-fill: white; "
                        + "-fx-padding: 6 12; -fx-background-radius: 4; -fx-cursor: hand; -fx-font-size: 11px;");
                
                final String donorUsername = donor.getUsername();
                final SystemLog logEntry = log;
                replyBtn.setOnAction(e -> {
                    TextInputDialog replyDialog = new TextInputDialog();
                    replyDialog.setTitle("Reply to " + donorUsername);
                    replyDialog.setHeaderText("Send a response to the donor's inquiry");
                    replyDialog.setContentText("Your reply:");
                    
                    java.util.Optional<String> result = replyDialog.showAndWait();
                    if (result.isPresent() && !result.get().trim().isEmpty()) {
                        String reply = result.get().trim();
                        
                        // Create notification for donor
                        Notification notification = new Notification();
                        notification.setCaregiverId(donor.getId());
                        notification.setMessage("Support Response: " + reply);
                        notification.setNotificationType("SUPPORT_REPLY");
                        notification.setChildName("Inquiry Response");
                        notification.setChildId(0);
                        notification.setRead(false);
                        notification.setTimestamp(java.time.LocalDateTime.now().toString());
                        
                        notificationService.createNotification(notification);
                        
                        // Show confirmation
                        Alert successAlert = new Alert(Alert.AlertType.INFORMATION);
                        successAlert.setTitle("Reply Sent");
                        successAlert.setHeaderText(null);
                        successAlert.setContentText("Your reply has been sent to " + donorUsername);
                        successAlert.showAndWait();
                    }
                });

                inquiryItem.getChildren().addAll(timestamp, categoryLabel, subjectLabel, messageLabel, replyBtn);
                content.getChildren().add(inquiryItem);
            }
        }

        ScrollPane sp = new ScrollPane(content);
        sp.setStyle("-fx-background-color: " + BG() + ";");
        dialog.getDialogPane().setContent(sp);
        dialog.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);
        dialog.showAndWait();
    }

    // ═══════════ CAREGIVER SUPPORT PAGE (FEATURE 4) ═══════════
    private ScrollPane buildCaregiverSupportPage() {
        VBox page = new VBox(20);
        page.setPadding(new Insets(24));

        Label title = new Label("Caregiver Inquiries");
        title.setFont(Font.font("Segoe UI", FontWeight.MEDIUM, 20));
        title.setTextFill(Color.web(TEXT()));
        Label sub = new Label("View all caregivers and manage their inquiries");
        sub.setFont(Font.font("Segoe UI", 13));
        sub.setTextFill(Color.web(MUTED_FG()));

        // Caregiver search
        HBox searchBox = new HBox(8);
        searchBox.setAlignment(Pos.CENTER_LEFT);
        TextField searchField = new TextField();
        searchField.setPromptText("Search caregiver by username (optional)...");
        searchField.setPrefWidth(300);

        Button clearBtn = new Button("Clear");
        clearBtn.setStyle("-fx-background-color: transparent; -fx-text-fill: " + SECONDARY
                + "; -fx-border-color: " + SECONDARY
                + "; -fx-border-radius: 4; -fx-padding: 8 16; -fx-cursor: hand;");

        searchBox.getChildren().addAll(searchField, clearBtn);

        // Results area
        ScrollPane resultsScroll = new ScrollPane();
        resultsScroll.setPrefHeight(400);
        resultsScroll.setFitToWidth(true);
        resultsScroll.setStyle("-fx-background-color: " + BG() + ";");
        
        // Helper method to display caregivers
        Runnable displayCaregivers = () -> {
            String search = searchField.getText().trim().toLowerCase();
            
            List<model.user.Caregiver> caregivers = userService.getAllUsers().stream()
                    .filter(u -> u instanceof model.user.Caregiver)
                    .map(u -> (model.user.Caregiver) u)
                    .filter(c -> search.isEmpty() || c.getUsername().toLowerCase().contains(search))
                    .toList();

            if (!search.isEmpty() && caregivers.isEmpty()) {
                VBox noResults = new VBox(12);
                noResults.setPadding(new Insets(24));
                noResults.setAlignment(Pos.CENTER);
                Label noMsg = new Label("❌ No caregivers found matching: \"" + searchField.getText() + "\"");
                noMsg.setFont(Font.font("Segoe UI", 13));
                noMsg.setTextFill(Color.web(MUTED_FG()));
                noResults.getChildren().add(noMsg);
                resultsScroll.setContent(noResults);
                return;
            }

            // Use TilePane for 3-column layout
            javafx.scene.layout.TilePane tilePane = new javafx.scene.layout.TilePane();
            tilePane.setPrefColumns(3);
            tilePane.setHgap(16);
            tilePane.setVgap(16);
            tilePane.setPadding(new Insets(8));
            tilePane.setStyle("-fx-background-color: " + BG() + ";");
            
            for (model.user.Caregiver caregiver : caregivers) {
                VBox caregiverCard = new VBox(12);
                caregiverCard.setPrefWidth(320);
                caregiverCard.setMinHeight(280);
                caregiverCard.setPadding(new Insets(16));
                caregiverCard.setStyle("-fx-background-color: " + CARD() + "; -fx-border-color: " + BORDER()
                        + "; -fx-border-width: 1; -fx-border-radius: 8; -fx-background-radius: 8;");

                Label cName = new Label("👤 " + caregiver.getUsername());
                cName.setFont(Font.font("Segoe UI", FontWeight.BOLD, 15));
                cName.setTextFill(Color.web(TEXT()));

                // Get assigned children
                List<Child> assignedChildren = childService.getAllChildren().stream()
                        .filter(c -> c.getAssignedCaregiverId() != null && c.getAssignedCaregiverId().equals(caregiver.getId()))
                        .toList();

                Label childCount = new Label("👶 Assigned Children: " + assignedChildren.size());
                childCount.setFont(Font.font("Segoe UI", FontWeight.MEDIUM, 13));
                childCount.setTextFill(Color.web(PRIMARY));

                // Child list
                VBox childrenList = new VBox(8);
                childrenList.setStyle("-fx-fill-height: true;");
                if (assignedChildren.isEmpty()) {
                    Label noChildren = new Label("No children assigned to this caregiver.");
                    noChildren.setFont(Font.font("Segoe UI", 11));
                    noChildren.setTextFill(Color.web(MUTED_FG()));
                    childrenList.getChildren().add(noChildren);
                } else {
                    for (Child child : assignedChildren) {
                        VBox childItem = new VBox(4);
                        childItem.setPadding(new Insets(8));
                        childItem.setStyle("-fx-background-color: " + MUTED() + "; -fx-border-radius: 4; -fx-background-radius: 4;");

                        Label childName = new Label("🏠 " + child.getName() + " (Age: " + child.getAge() + ")");
                        childName.setFont(Font.font("Segoe UI", FontWeight.MEDIUM, 12));
                        childName.setTextFill(Color.web(TEXT()));

                        // Check record status
                        boolean hasMedical = !medicalRecordService.getRecordsByChildId(child.getId()).isEmpty();
                        boolean hasEducation = !educationRecordService.getRecordsByChildId(child.getId()).isEmpty();

                        HBox recordStatus = new HBox(8);
                        Label medicalStatus = new Label(hasMedical ? "✓ Medical" : "✗ Medical");
                        medicalStatus.setFont(Font.font("Segoe UI", 11));
                        medicalStatus.setTextFill(Color.web(hasMedical ? SECONDARY : DESTRUCTIVE));

                        Label educationStatus = new Label(hasEducation ? "✓ Education" : "✗ Education");
                        educationStatus.setFont(Font.font("Segoe UI", 11));
                        educationStatus.setTextFill(Color.web(hasEducation ? SECONDARY : DESTRUCTIVE));

                        recordStatus.getChildren().addAll(medicalStatus, educationStatus);

                        childItem.getChildren().addAll(childName, recordStatus);
                        childrenList.getChildren().add(childItem);
                    }
                }

                caregiverCard.getChildren().addAll(cName, childCount, childrenList);
                tilePane.getChildren().add(caregiverCard);
            }
            resultsScroll.setContent(tilePane);
        };
        
        // Search with text change listener
        searchField.textProperty().addListener((obs, oldVal, newVal) -> displayCaregivers.run());
        
        // Clear button
        clearBtn.setOnAction(e -> {
            searchField.clear();
            displayCaregivers.run();
        });
        
        // Display all caregivers on load
        displayCaregivers.run();

        page.getChildren().addAll(new VBox(4, title, sub), searchBox, resultsScroll);
        return wrapScroll(page);
    }

    // ═══════════ SYSTEM ALERTS PAGE (FEATURE 5 - ENHANCED) ═══════════
    private ScrollPane buildAlertsPageEnhanced() {
        VBox page = new VBox(20);
        page.setPadding(new Insets(24));

        Label title = new Label("System Monitoring & Alerts");
        title.setFont(Font.font("Segoe UI", FontWeight.MEDIUM, 20));
        title.setTextFill(Color.web(TEXT()));
        Label sub = new Label("Monitor system health, errors, and suspicious activities");
        sub.setFont(Font.font("Segoe UI", 13));
        sub.setTextFill(Color.web(MUTED_FG()));

        List<SystemLog> allLogs = systemLogService.getRecent(50);

        // Alert categories
        long errorCount = allLogs.stream()
                .filter(l -> l.getEventType() != null && l.getEventType().contains("Error"))
                .count();
        long warningCount = allLogs.stream()
                .filter(l -> l.getEventType() != null && l.getEventType().contains("Warning"))
                .count();
        long deleteCount = allLogs.stream()
                .filter(l -> l.getEventType() != null && l.getEventType().contains("Delete"))
                .count();

        HBox stats = new HBox(16);
        stats.getChildren().addAll(
                statCard("Errors", String.valueOf(errorCount), "Critical issues", DESTRUCTIVE),
                statCard("Warnings", String.valueOf(warningCount), "Potential issues", WARNING),
                statCard("Deletions", String.valueOf(deleteCount), "Delete operations", DESTRUCTIVE));

        // Alert feed
        VBox alertsFeed = new VBox(12);
        if (allLogs.isEmpty()) {
            Label noAlerts = new Label("No system alerts at this time. Everything is working smoothly!");
            noAlerts.setFont(Font.font("Segoe UI", 13));
            noAlerts.setTextFill(Color.web(SECONDARY));
            noAlerts.setPadding(new Insets(24));
            alertsFeed.getChildren().add(noAlerts);
        } else {
            for (SystemLog log : allLogs) {
                String eventType = log.getEventType() != null ? log.getEventType() : "Info";
                boolean isError = eventType.contains("Error") || eventType.contains("Delete");
                boolean isWarning = eventType.contains("Warning");
                String borderC = isError ? DESTRUCTIVE : isWarning ? WARNING : BORDER();
                String badgeColor = isError ? DESTRUCTIVE : isWarning ? WARNING : INFO;

                VBox card = new VBox(8);
                card.setPadding(new Insets(12));
                card.setStyle("-fx-background-color: " + CARD() + "; -fx-border-color: " + borderC
                        + "; -fx-border-width: 1; -fx-border-radius: 8; -fx-background-radius: 8;");

                HBox top = new HBox(12);
                top.setAlignment(Pos.CENTER_LEFT);

                Label typeBadge = new Label(isError ? "🚨 " + eventType : isWarning ? "⚠️ " + eventType : "ℹ️ " + eventType);
                typeBadge.setFont(Font.font("Segoe UI", FontWeight.BOLD, 11));
                typeBadge.setStyle("-fx-background-color: " + badgeColor + "1A; -fx-text-fill: " + badgeColor
                        + "; -fx-background-radius: 4; -fx-padding: 2 8;");

                Label desc = new Label(log.getDescription() != null ? log.getDescription() : "No description");
                desc.setFont(Font.font("Segoe UI", 12));
                desc.setTextFill(Color.web(TEXT()));
                desc.setWrapText(true);
                HBox.setHgrow(desc, Priority.ALWAYS);

                top.getChildren().addAll(typeBadge, desc);

                HBox bottom = new HBox(12);
                Label actor = new Label("🔑 " + (log.getActor() != null ? log.getActor() : "System"));
                actor.setFont(Font.font("Segoe UI", 11));
                actor.setTextFill(Color.web(MUTED_FG()));

                Label time = new Label(log.getTimestamp() != null ? log.getTimestamp() : "");
                time.setFont(Font.font("Segoe UI", 11));
                time.setTextFill(Color.web(MUTED_FG()));

                bottom.getChildren().addAll(actor, new Region(), time);
                HBox.setHgrow(bottom.getChildren().get(1), Priority.ALWAYS);

                card.getChildren().addAll(top, bottom);
                alertsFeed.getChildren().add(card);
            }
        }

        page.getChildren().addAll(new VBox(4, title, sub), stats, alertsFeed);
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
}
