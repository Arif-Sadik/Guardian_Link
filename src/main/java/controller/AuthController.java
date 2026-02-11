package controller;

import exception.UserNotApprovedException;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;
import model.user.*;
import service.AuthService;
import service.UserService;
import util.PasswordUtil;

/**
 * Login screen — clean split-panel layout.
 * Left: blue branding. Right: login form with signup options.
 */
public class AuthController {

    private final Stage stage;
    private final AuthService authService = new AuthService();
    private final UserService userService = new UserService();

    private static final String PRIMARY = "#2563eb";
    private static final String SECONDARY = "#22c55e";
    // Remove static color constants, use ThemeManager
    private static final String BORDER = "#e2e8f0";
    private static final String BG = "#f8f9fa";
    private static final String CARD = "#ffffff";
    private static final String RING = "#3b82f6";
    private static final String MUTED_FG = util.ThemeManager.getMutedFg();

    public AuthController(Stage stage) {
        this.stage = stage;
    }

    public void show() {

        // ═══════════ LEFT PANEL — blue branding ═══════════
        VBox leftPanel = new VBox(0);
        leftPanel.setAlignment(Pos.CENTER);
        leftPanel.setStyle("-fx-background-color: linear-gradient(to bottom right, #2563eb, #1e40af);");
        leftPanel.setPrefWidth(640);

        VBox leftContent = new VBox(16);
        leftContent.setAlignment(Pos.CENTER);
        leftContent.setMaxWidth(400);
        leftContent.setPadding(new Insets(48));

        StackPane shieldCircle = new StackPane();
        shieldCircle.setPrefSize(96, 96);
        shieldCircle.setMaxSize(96, 96);
        shieldCircle.setStyle("-fx-background-color: rgba(255,255,255,0.1); -fx-background-radius: 48;");
        Label shieldIcon = new Label("\uD83D\uDEE1");
        shieldIcon.setFont(Font.font("Segoe UI Emoji", 40));
        shieldIcon.setTextFill(Color.WHITE);
        shieldCircle.getChildren().add(shieldIcon);

        Label brandTitle = new Label("GuardianLink");
        brandTitle.setFont(Font.font("Segoe UI", FontWeight.BOLD, 36));
        brandTitle.setTextFill(Color.WHITE);
        javafx.scene.effect.DropShadow ds = new javafx.scene.effect.DropShadow();
        ds.setOffsetY(2.0f);
        ds.setColor(Color.color(0, 0, 0, 0.3));
        brandTitle.setEffect(ds);

        Label brandSub = new Label("NGO Welfare Management System");
        brandSub.setFont(Font.font("Segoe UI", FontWeight.BOLD, 16));
        brandSub.setTextFill(Color.WHITE);
        brandSub.setEffect(ds);

        leftContent.getChildren().addAll(shieldCircle, brandTitle, brandSub);
        leftPanel.getChildren().add(leftContent);

        // ═══════════ RIGHT PANEL — login form ═══════════
        VBox rightPanel = new VBox(0);
        rightPanel.setAlignment(Pos.CENTER);
        rightPanel.setPrefWidth(640);
        rightPanel.setStyle("-fx-background-color: " + CARD + ";");
        rightPanel.setPadding(new Insets(48));

        VBox formBox = new VBox(0);
        formBox.setMaxWidth(400);
        formBox.setAlignment(Pos.CENTER_LEFT);

        Label welcomeTitle = new Label("Welcome Back");
        welcomeTitle.setFont(Font.font("Segoe UI", FontWeight.SEMI_BOLD, 24));

        Label welcomeSub = new Label("Enter your credentials to continue");
        welcomeSub.setFont(Font.font("Segoe UI", 14));
        welcomeSub.setTextFill(Color.web(util.ThemeManager.getMutedFg()));
        VBox.setMargin(welcomeSub, new Insets(4, 0, 32, 0));

        Label userLabel = new Label("Username or Email");
        userLabel.setFont(Font.font("Segoe UI", FontWeight.MEDIUM, 13));
        TextField userField = new TextField();
        userField.setPromptText("Enter your username");
        styleInput(userField);
        VBox.setMargin(userField, new Insets(8, 0, 20, 0));

        Label passLabel = new Label("Password");
        passLabel.setFont(Font.font("Segoe UI", FontWeight.MEDIUM, 13));
        PasswordField passField = new PasswordField();
        passField.setPromptText("Enter your password");
        styleInput(passField);
        VBox.setMargin(passField, new Insets(8, 0, 24, 0));

        Label errorLabel = new Label();
        errorLabel.setTextFill(Color.web("#dc2626"));
        errorLabel.setFont(Font.font("Segoe UI", 13));
        errorLabel.setWrapText(true);
        VBox.setMargin(errorLabel, new Insets(0, 0, 8, 0));

        Button signInBtn = new Button("Sign In");
        signInBtn.setMaxWidth(Double.MAX_VALUE);
        signInBtn.setStyle(String.format(
                "-fx-background-color: %s; -fx-text-fill: white; -fx-font-size: 14px; -fx-font-weight: bold; -fx-padding: 10 0; -fx-background-radius: 6; -fx-cursor: hand;",
                PRIMARY));
        signInBtn.setOnMouseEntered(e -> signInBtn.setStyle(signInBtn.getStyle().replace(PRIMARY, "#1d4ed8")));
        signInBtn.setOnMouseExited(e -> signInBtn.setStyle(signInBtn.getStyle().replace("#1d4ed8", PRIMARY)));
        signInBtn.setOnAction(e -> handleLogin(userField.getText(), passField.getText(), errorLabel));
        passField.setOnAction(e -> signInBtn.fire());

        // Divider
        HBox divider = new HBox();
        divider.setAlignment(Pos.CENTER);
        Label orLabel = new Label("or");
        orLabel.setFont(Font.font("Segoe UI", 12));
        orLabel.setTextFill(Color.web(util.ThemeManager.getMutedFg()));
        orLabel.setPadding(new Insets(0, 12, 0, 12));
        Region leftLine = new Region();
        leftLine.setStyle("-fx-background-color: " + BORDER + ";");
        leftLine.setPrefHeight(1);
        HBox.setHgrow(leftLine, Priority.ALWAYS);
        Region rightLine = new Region();
        rightLine.setStyle("-fx-background-color: " + BORDER + ";");
        rightLine.setPrefHeight(1);
        HBox.setHgrow(rightLine, Priority.ALWAYS);
        divider.getChildren().addAll(leftLine, orLabel, rightLine);
        VBox.setMargin(divider, new Insets(20, 0, 20, 0));

        // Sign Up button
        Button signUpBtn = new Button("Create an Account");
        signUpBtn.setMaxWidth(Double.MAX_VALUE);
        signUpBtn.setStyle(String.format(
                "-fx-background-color: transparent; -fx-text-fill: %s; -fx-font-size: 14px; -fx-font-weight: bold; -fx-padding: 10 0; -fx-background-radius: 6; -fx-border-color: %s; -fx-border-width: 1; -fx-border-radius: 6; -fx-cursor: hand;",
                PRIMARY, BORDER));
        signUpBtn.setOnMouseEntered(e -> signUpBtn.setStyle(String.format(
                "-fx-background-color: %s10; -fx-text-fill: %s; -fx-font-size: 14px; -fx-font-weight: bold; -fx-padding: 10 0; -fx-background-radius: 6; -fx-border-color: %s; -fx-border-width: 1; -fx-border-radius: 6; -fx-cursor: hand;",
                PRIMARY, PRIMARY, PRIMARY)));
        signUpBtn.setOnMouseExited(e -> signUpBtn.setStyle(String.format(
                "-fx-background-color: transparent; -fx-text-fill: %s; -fx-font-size: 14px; -fx-font-weight: bold; -fx-padding: 10 0; -fx-background-radius: 6; -fx-border-color: %s; -fx-border-width: 1; -fx-border-radius: 6; -fx-cursor: hand;",
                PRIMARY, BORDER)));
        signUpBtn.setOnAction(e -> showSignUpChoice());

        formBox.getChildren().addAll(
                welcomeTitle, welcomeSub,
                userLabel, userField,
                passLabel, passField,
                errorLabel, signInBtn, divider, signUpBtn);
        rightPanel.getChildren().add(formBox);

        HBox root = new HBox(leftPanel, rightPanel);
        HBox.setHgrow(leftPanel, Priority.ALWAYS);
        HBox.setHgrow(rightPanel, Priority.ALWAYS);

        StackPane wrapper = new StackPane(root);
        wrapper.setStyle("-fx-background-color: " + BG + ";");

        Scene scene = new Scene(wrapper, 1280, 800);
        stage.setScene(scene);
        stage.setTitle("GuardianLink \u2014 Login");
        stage.setMinWidth(1280);
        stage.setMinHeight(800);
        stage.show();
    }

    private void showSignUpChoice() {
        // ═══════════ LEFT PANEL — blue branding ═══════════
        VBox leftPanel = buildBrandingPanel();

        // ═══════════ RIGHT PANEL — account type choice ═══════════
        VBox rightPanel = new VBox(0);
        rightPanel.setAlignment(Pos.CENTER);
        rightPanel.setPrefWidth(640);
        rightPanel.setStyle("-fx-background-color: " + CARD + ";");
        rightPanel.setPadding(new Insets(48));

        VBox content = new VBox(0);
        content.setMaxWidth(450);
        content.setAlignment(Pos.CENTER_LEFT);

        Label title = new Label("Create an Account");
        title.setFont(Font.font("Segoe UI", FontWeight.SEMI_BOLD, 24));

        Label subtitle = new Label("Choose your account type to get started");
        subtitle.setFont(Font.font("Segoe UI", 14));
        subtitle.setTextFill(Color.web(MUTED_FG));
        VBox.setMargin(subtitle, new Insets(4, 0, 32, 0));

        // Donor card
        VBox donorCard = new VBox(8);
        donorCard.setPadding(new Insets(24));
        donorCard.setStyle("-fx-background-color: " + BG + "; -fx-background-radius: 8; -fx-border-color: " + BORDER
                + "; -fx-border-radius: 8; -fx-cursor: hand;");
        Label donorTitle = new Label("Donor");
        donorTitle.setFont(Font.font("Segoe UI", FontWeight.SEMI_BOLD, 18));
        Label donorDesc = new Label(
                "Support children through donations and sponsorships. Track your contributions and see the impact you make.");
        donorDesc.setFont(Font.font("Segoe UI", 13));
        donorDesc.setTextFill(Color.web(util.ThemeManager.getMutedFg()));
        donorDesc.setWrapText(true);
        donorCard.getChildren().addAll(donorTitle, donorDesc);
        donorCard.setOnMouseEntered(e -> donorCard
                .setStyle("-fx-background-color: " + CARD + "; -fx-background-radius: 8; -fx-border-color: " + PRIMARY
                        + "; -fx-border-radius: 8; -fx-border-width: 2; -fx-cursor: hand;"));
        donorCard.setOnMouseExited(e -> donorCard
                .setStyle("-fx-background-color: " + BG + "; -fx-background-radius: 8; -fx-border-color: " + BORDER
                        + "; -fx-border-radius: 8; -fx-cursor: hand;"));
        donorCard.setOnMouseClicked(e -> showDonorSignUp());
        VBox.setMargin(donorCard, new Insets(0, 0, 16, 0));

        // Caregiver card
        VBox caregiverCard = new VBox(8);
        caregiverCard.setPadding(new Insets(24));
        caregiverCard.setStyle("-fx-background-color: " + BG + "; -fx-background-radius: 8; -fx-border-color: " + BORDER
                + "; -fx-border-radius: 8; -fx-cursor: hand;");
        Label caregiverTitle = new Label("Caregiver");
        caregiverTitle.setFont(Font.font("Segoe UI", FontWeight.SEMI_BOLD, 18));
        Label caregiverDesc = new Label(
                "Manage child profiles, track welfare, and coordinate care activities within your organization.");
        caregiverDesc.setFont(Font.font("Segoe UI", 13));
        caregiverDesc.setTextFill(Color.web(util.ThemeManager.getMutedFg()));
        caregiverDesc.setWrapText(true);
        caregiverCard.getChildren().addAll(caregiverTitle, caregiverDesc);
        caregiverCard.setOnMouseEntered(e -> caregiverCard
                .setStyle("-fx-background-color: " + CARD + "; -fx-background-radius: 8; -fx-border-color: " + PRIMARY
                        + "; -fx-border-radius: 8; -fx-border-width: 2; -fx-cursor: hand;"));
        caregiverCard.setOnMouseExited(e -> caregiverCard
                .setStyle("-fx-background-color: " + BG + "; -fx-background-radius: 8; -fx-border-color: " + BORDER
                        + "; -fx-border-radius: 8; -fx-cursor: hand;"));
        caregiverCard.setOnMouseClicked(e -> showCaregiverSignUp());
        VBox.setMargin(caregiverCard, new Insets(0, 0, 24, 0));

        // Back to login link
        Hyperlink backLink = new Hyperlink("Already have an account? Sign in");
        backLink.setFont(Font.font("Segoe UI", 13));
        backLink.setTextFill(Color.web(util.ThemeManager.PRIMARY));
        // Theme toggle button
        ToggleButton themeToggle = new ToggleButton(util.ThemeManager.isDarkMode() ? "🌙" : "☀️");
        themeToggle.setSelected(util.ThemeManager.isDarkMode());
        themeToggle.setStyle(
                "-fx-background-radius: 16; -fx-background-color: transparent; -fx-font-size: 18px; -fx-cursor: hand;");
        themeToggle.setOnAction(e -> {
            util.ThemeManager.toggleTheme();
            themeToggle.setText(util.ThemeManager.isDarkMode() ? "🌙" : "☀️");
            show();
        });
        VBox.setMargin(themeToggle, new Insets(0, 0, 24, 0));
        rightPanel.getChildren().add(0, themeToggle);
        backLink.setOnAction(e -> show());

        content.getChildren().addAll(title, subtitle, donorCard, caregiverCard, backLink);
        rightPanel.getChildren().add(content);

        HBox root = new HBox(leftPanel, rightPanel);
        HBox.setHgrow(leftPanel, Priority.ALWAYS);
        HBox.setHgrow(rightPanel, Priority.ALWAYS);

        StackPane wrapper = new StackPane(root);
        wrapper.setStyle("-fx-background-color: " + BG + ";");

        Scene scene = new Scene(wrapper, 1280, 800);
        stage.setScene(scene);
        stage.setTitle("GuardianLink \u2014 Create Account");
    }

    private VBox buildBrandingPanel() {
        VBox leftPanel = new VBox(0);
        leftPanel.setAlignment(Pos.CENTER);
        // Modern gradient background
        leftPanel.setStyle("-fx-background-color: linear-gradient(to bottom right, #2563eb, #1e40af);");
        leftPanel.setPrefWidth(640);

        VBox leftContent = new VBox(16);
        leftContent.setAlignment(Pos.CENTER);
        leftContent.setMaxWidth(400);
        leftContent.setPadding(new Insets(48));

        StackPane shieldCircle = new StackPane();
        shieldCircle.setPrefSize(96, 96);
        shieldCircle.setMaxSize(96, 96);
        // Lighter, semi-transparent white circle
        shieldCircle.setStyle("-fx-background-color: rgba(255,255,255,0.25); -fx-background-radius: 48;");
        Label shieldIcon = new Label("\uD83D\uDEE1");
        shieldIcon.setFont(Font.font("Segoe UI Emoji", 40));
        shieldIcon.setTextFill(Color.WHITE);
        shieldCircle.getChildren().add(shieldIcon);

        Label brandTitle = new Label("GuardianLink");
        brandTitle.setFont(Font.font("Segoe UI", FontWeight.BOLD, 36));
        // White for strong contrast
        brandTitle.setTextFill(Color.WHITE);
        javafx.scene.effect.DropShadow ds = new javafx.scene.effect.DropShadow();
        ds.setOffsetY(2.0f);
        ds.setColor(Color.color(0, 0, 0, 0.3));
        brandTitle.setEffect(ds);

        Label brandSub = new Label("NGO Welfare Management System");
        brandSub.setFont(Font.font("Segoe UI", FontWeight.BOLD, 16));
        // Softer, lighter subtitle
        brandSub.setTextFill(Color.WHITE);
        brandSub.setEffect(ds);

        leftContent.getChildren().addAll(shieldCircle, brandTitle, brandSub);
        leftPanel.getChildren().add(leftContent);
        return leftPanel;
    }

    private VBox createSignUpCard(String title, String description) {
        VBox card = new VBox(8);
        card.setPadding(new Insets(20));
        card.setPrefWidth(250);
        card.setStyle("-fx-background-color: " + BG + "; -fx-background-radius: 8; -fx-border-color: " + BORDER
                + "; -fx-border-radius: 8;");

        Label titleLabel = new Label(title);
        titleLabel.setFont(Font.font("Segoe UI", FontWeight.SEMI_BOLD, 16));

        Label descLabel = new Label(description);
        descLabel.setFont(Font.font("Segoe UI", 12));
        descLabel.setTextFill(Color.web(MUTED_FG));
        descLabel.setWrapText(true);

        card.getChildren().addAll(titleLabel, descLabel);
        return card;
    }

    private void showDonorSignUp() {
        VBox leftPanel = buildBrandingPanel();

        VBox rightPanel = new VBox(0);
        rightPanel.setAlignment(Pos.CENTER);
        rightPanel.setPrefWidth(640);
        rightPanel.setStyle("-fx-background-color: " + CARD + ";");
        rightPanel.setPadding(new Insets(48));

        VBox formBox = new VBox(0);
        formBox.setMaxWidth(400);
        formBox.setAlignment(Pos.CENTER_LEFT);

        Label title = new Label("Donor Registration");
        title.setFont(Font.font("Segoe UI", FontWeight.SEMI_BOLD, 24));

        Label subtitle = new Label("Create your donor account");
        subtitle.setFont(Font.font("Segoe UI", 14));
        subtitle.setTextFill(Color.web(MUTED_FG));
        VBox.setMargin(subtitle, new Insets(4, 0, 24, 0));

        Label userLabel = new Label("Username");
        userLabel.setFont(Font.font("Segoe UI", FontWeight.MEDIUM, 13));
        TextField usernameField = new TextField();
        usernameField.setPromptText("Enter username");
        styleInput(usernameField);
        VBox.setMargin(usernameField, new Insets(6, 0, 16, 0));

        Label passLabel = new Label("Password");
        passLabel.setFont(Font.font("Segoe UI", FontWeight.MEDIUM, 13));
        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("Enter password");
        styleInput(passwordField);
        VBox.setMargin(passwordField, new Insets(6, 0, 16, 0));

        Label confirmLabel = new Label("Confirm Password");
        confirmLabel.setFont(Font.font("Segoe UI", FontWeight.MEDIUM, 13));
        PasswordField confirmField = new PasswordField();
        confirmField.setPromptText("Confirm password");
        styleInput(confirmField);
        VBox.setMargin(confirmField, new Insets(6, 0, 16, 0));

        Label emailLabel = new Label("Email Address");
        emailLabel.setFont(Font.font("Segoe UI", FontWeight.MEDIUM, 13));
        TextField emailField = new TextField();
        emailField.setPromptText("Enter email");
        styleInput(emailField);
        VBox.setMargin(emailField, new Insets(6, 0, 16, 0));

        Label phoneLabel = new Label("Phone Number");
        phoneLabel.setFont(Font.font("Segoe UI", FontWeight.MEDIUM, 13));
        TextField phoneField = new TextField();
        phoneField.setPromptText("Enter phone number");
        styleInput(phoneField);
        VBox.setMargin(phoneField, new Insets(6, 0, 16, 0));

        Label paymentLabel = new Label("Preferred Payment Method");
        paymentLabel.setFont(Font.font("Segoe UI", FontWeight.MEDIUM, 13));
        ComboBox<String> paymentCombo = new ComboBox<>();
        paymentCombo.getItems().addAll("Credit Card", "Debit Card", "Bank Transfer", "PayPal", "Mobile Payment");
        paymentCombo.setPromptText("Select Payment Method");
        paymentCombo.setMaxWidth(Double.MAX_VALUE);
        paymentCombo.setStyle("-fx-background-color: " + CARD + "; -fx-border-color: " + BORDER
                + "; -fx-border-radius: 4; -fx-background-radius: 4;");
        VBox.setMargin(paymentCombo, new Insets(6, 0, 20, 0));

        Label errorLabel = new Label();
        errorLabel.setTextFill(Color.web("#dc2626"));
        errorLabel.setFont(Font.font("Segoe UI", 13));
        errorLabel.setWrapText(true);

        Button registerBtn = new Button("Create Account");
        registerBtn.setMaxWidth(Double.MAX_VALUE);
        registerBtn.setStyle(String.format(
                "-fx-background-color: %s; -fx-text-fill: white; -fx-font-size: 14px; -fx-font-weight: bold; -fx-padding: 10 0; -fx-background-radius: 6; -fx-cursor: hand;",
                PRIMARY));
        registerBtn.setOnMouseEntered(e -> registerBtn.setStyle(registerBtn.getStyle().replace(PRIMARY, "#1d4ed8")));
        registerBtn.setOnMouseExited(e -> registerBtn.setStyle(registerBtn.getStyle().replace("#1d4ed8", PRIMARY)));
        registerBtn.setOnAction(e -> {
            errorLabel.setText("");
            String username = usernameField.getText().trim();
            String password = passwordField.getText();
            String confirm = confirmField.getText();

            if (username.isEmpty() || password.isEmpty()) {
                errorLabel.setText("Username and password are required.");
                return;
            }
            if (!password.equals(confirm)) {
                errorLabel.setText("Passwords do not match.");
                return;
            }
            if (paymentCombo.getValue() == null) {
                errorLabel.setText("Please select a payment method.");
                return;
            }

            Donor donor = new Donor(username, PasswordUtil.hash(password));
            donor.setApproved(true); // Auto-approve for easy login
            if (userService.createUser(donor)) {
                showSuccessPage("Donor");
            } else {
                errorLabel.setText("Username already exists. Please choose a different one.");
            }
        });
        VBox.setMargin(registerBtn, new Insets(8, 0, 16, 0));

        Hyperlink backLink = new Hyperlink("Back to account type selection");
        backLink.setFont(Font.font("Segoe UI", 13));
        backLink.setTextFill(Color.web(PRIMARY));
        backLink.setOnAction(e -> showSignUpChoice());

        formBox.getChildren().addAll(title, subtitle,
                userLabel, usernameField,
                passLabel, passwordField,
                confirmLabel, confirmField,
                emailLabel, emailField,
                phoneLabel, phoneField,
                paymentLabel, paymentCombo,
                errorLabel, registerBtn, backLink);
        rightPanel.getChildren().add(formBox);

        HBox root = new HBox(leftPanel, rightPanel);
        HBox.setHgrow(leftPanel, Priority.ALWAYS);
        HBox.setHgrow(rightPanel, Priority.ALWAYS);

        StackPane wrapper = new StackPane(root);
        wrapper.setStyle("-fx-background-color: " + BG + ";");

        Scene scene = new Scene(wrapper, 1280, 800);
        stage.setScene(scene);
        stage.setTitle("GuardianLink \u2014 Donor Registration");
    }

    private void showCaregiverSignUp() {
        VBox leftPanel = buildBrandingPanel();

        VBox rightPanel = new VBox(0);
        rightPanel.setAlignment(Pos.CENTER);
        rightPanel.setPrefWidth(640);
        rightPanel.setStyle("-fx-background-color: " + CARD + ";");
        rightPanel.setPadding(new Insets(48));

        VBox formBox = new VBox(0);
        formBox.setMaxWidth(400);
        formBox.setAlignment(Pos.CENTER_LEFT);

        Label title = new Label("Caregiver Registration");
        title.setFont(Font.font("Segoe UI", FontWeight.SEMI_BOLD, 24));

        Label subtitle = new Label("Create your caregiver account");
        subtitle.setFont(Font.font("Segoe UI", 14));
        subtitle.setTextFill(Color.web(MUTED_FG));
        VBox.setMargin(subtitle, new Insets(4, 0, 24, 0));

        Label userLabel = new Label("Username");
        userLabel.setFont(Font.font("Segoe UI", FontWeight.MEDIUM, 13));
        TextField usernameField = new TextField();
        usernameField.setPromptText("Enter username");
        styleInput(usernameField);
        VBox.setMargin(usernameField, new Insets(6, 0, 16, 0));

        Label passLabel = new Label("Password");
        passLabel.setFont(Font.font("Segoe UI", FontWeight.MEDIUM, 13));
        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("Enter password");
        styleInput(passwordField);
        VBox.setMargin(passwordField, new Insets(6, 0, 16, 0));

        Label confirmLabel = new Label("Confirm Password");
        confirmLabel.setFont(Font.font("Segoe UI", FontWeight.MEDIUM, 13));
        PasswordField confirmField = new PasswordField();
        confirmField.setPromptText("Confirm password");
        styleInput(confirmField);
        VBox.setMargin(confirmField, new Insets(6, 0, 16, 0));

        Label emailLabel = new Label("Email Address");
        emailLabel.setFont(Font.font("Segoe UI", FontWeight.MEDIUM, 13));
        TextField emailField = new TextField();
        emailField.setPromptText("Enter email");
        styleInput(emailField);
        VBox.setMargin(emailField, new Insets(6, 0, 16, 0));

        Label phoneLabel = new Label("Phone Number");
        phoneLabel.setFont(Font.font("Segoe UI", FontWeight.MEDIUM, 13));
        TextField phoneField = new TextField();
        phoneField.setPromptText("Enter phone number");
        styleInput(phoneField);
        VBox.setMargin(phoneField, new Insets(6, 0, 16, 0));

        Label orgLabel = new Label("Organization Name");
        orgLabel.setFont(Font.font("Segoe UI", FontWeight.MEDIUM, 13));
        TextField orgField = new TextField();
        orgField.setPromptText("Enter organization name");
        styleInput(orgField);
        VBox.setMargin(orgField, new Insets(6, 0, 16, 0));

        Label expertiseLabel = new Label("Work Expertise");
        expertiseLabel.setFont(Font.font("Segoe UI", FontWeight.MEDIUM, 13));
        ComboBox<String> expertiseCombo = new ComboBox<>();
        expertiseCombo.getItems().addAll(
                "Child Psychology", "Social Work", "Healthcare", "Education",
                "Nutrition", "Counseling", "Administration", "Other");
        expertiseCombo.setPromptText("Select Work Expertise");
        expertiseCombo.setMaxWidth(Double.MAX_VALUE);
        expertiseCombo.setStyle("-fx-background-color: " + CARD + "; -fx-border-color: " + BORDER
                + "; -fx-border-radius: 4; -fx-background-radius: 4;");
        VBox.setMargin(expertiseCombo, new Insets(6, 0, 20, 0));

        Label errorLabel = new Label();
        errorLabel.setTextFill(Color.web("#dc2626"));
        errorLabel.setFont(Font.font("Segoe UI", 13));
        errorLabel.setWrapText(true);

        Button registerBtn = new Button("Create Account");
        registerBtn.setMaxWidth(Double.MAX_VALUE);
        registerBtn.setStyle(String.format(
                "-fx-background-color: %s; -fx-text-fill: white; -fx-font-size: 14px; -fx-font-weight: bold; -fx-padding: 10 0; -fx-background-radius: 6; -fx-cursor: hand;",
                PRIMARY));
        registerBtn.setOnMouseEntered(e -> registerBtn.setStyle(registerBtn.getStyle().replace(PRIMARY, "#1d4ed8")));
        registerBtn.setOnMouseExited(e -> registerBtn.setStyle(registerBtn.getStyle().replace("#1d4ed8", PRIMARY)));
        registerBtn.setOnAction(e -> {
            errorLabel.setText("");
            String username = usernameField.getText().trim();
            String password = passwordField.getText();
            String confirm = confirmField.getText();

            if (username.isEmpty() || password.isEmpty()) {
                errorLabel.setText("Username and password are required.");
                return;
            }
            if (!password.equals(confirm)) {
                errorLabel.setText("Passwords do not match.");
                return;
            }
            if (expertiseCombo.getValue() == null) {
                errorLabel.setText("Please select your work expertise.");
                return;
            }

            OrganizationAdmin caregiver = new OrganizationAdmin(username, PasswordUtil.hash(password));
            caregiver.setApproved(true); // Auto-approve for easy login
            if (userService.createUser(caregiver)) {
                showSuccessPage("Caregiver");
            } else {
                errorLabel.setText("Username already exists. Please choose a different one.");
            }
        });
        VBox.setMargin(registerBtn, new Insets(8, 0, 16, 0));

        Hyperlink backLink = new Hyperlink("Back to account type selection");
        backLink.setFont(Font.font("Segoe UI", 13));
        backLink.setTextFill(Color.web(PRIMARY));
        backLink.setOnAction(e -> showSignUpChoice());

        formBox.getChildren().addAll(title, subtitle,
                userLabel, usernameField,
                passLabel, passwordField,
                confirmLabel, confirmField,
                emailLabel, emailField,
                phoneLabel, phoneField,
                orgLabel, orgField,
                expertiseLabel, expertiseCombo,
                errorLabel, registerBtn, backLink);
        rightPanel.getChildren().add(formBox);

        HBox root = new HBox(leftPanel, rightPanel);
        HBox.setHgrow(leftPanel, Priority.ALWAYS);
        HBox.setHgrow(rightPanel, Priority.ALWAYS);

        StackPane wrapper = new StackPane(root);
        wrapper.setStyle("-fx-background-color: " + BG + ";");

        Scene scene = new Scene(wrapper, 1280, 800);
        stage.setScene(scene);
        stage.setTitle("GuardianLink \u2014 Caregiver Registration");
    }

    private void showSuccessPage(String accountType) {
        VBox leftPanel = buildBrandingPanel();

        VBox rightPanel = new VBox(24);
        rightPanel.setAlignment(Pos.CENTER);
        rightPanel.setPrefWidth(640);
        rightPanel.setStyle("-fx-background-color: " + CARD + ";");
        rightPanel.setPadding(new Insets(48));

        VBox content = new VBox(16);
        content.setMaxWidth(400);
        content.setAlignment(Pos.CENTER);

        Label checkIcon = new Label("\u2713");
        checkIcon.setFont(Font.font("Segoe UI", FontWeight.BOLD, 48));
        checkIcon.setTextFill(Color.web(SECONDARY));

        Label title = new Label("Registration Successful!");
        title.setFont(Font.font("Segoe UI", FontWeight.SEMI_BOLD, 24));

        Label message = new Label(
                "Your " + accountType
                        + " account has been created successfully. You can now sign in with your credentials.");
        message.setFont(Font.font("Segoe UI", 14));
        message.setTextFill(Color.web(MUTED_FG));
        message.setWrapText(true);
        message.setAlignment(Pos.CENTER);

        Button signInBtn = new Button("Sign In Now");
        signInBtn.setStyle(String.format(
                "-fx-background-color: %s; -fx-text-fill: white; -fx-font-size: 14px; -fx-font-weight: bold; -fx-padding: 10 24; -fx-background-radius: 6; -fx-cursor: hand;",
                PRIMARY));
        signInBtn.setOnMouseEntered(e -> signInBtn.setStyle(signInBtn.getStyle().replace(PRIMARY, "#1d4ed8")));
        signInBtn.setOnMouseExited(e -> signInBtn.setStyle(signInBtn.getStyle().replace("#1d4ed8", PRIMARY)));
        signInBtn.setOnAction(e -> show());
        VBox.setMargin(signInBtn, new Insets(8, 0, 0, 0));

        content.getChildren().addAll(checkIcon, title, message, signInBtn);
        rightPanel.getChildren().add(content);

        HBox root = new HBox(leftPanel, rightPanel);
        HBox.setHgrow(leftPanel, Priority.ALWAYS);
        HBox.setHgrow(rightPanel, Priority.ALWAYS);

        StackPane wrapper = new StackPane(root);
        wrapper.setStyle("-fx-background-color: " + BG + ";");

        Scene scene = new Scene(wrapper, 1280, 800);
        stage.setScene(scene);
        stage.setTitle("GuardianLink \u2014 Registration Complete");
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void handleLogin(String username, String password, Label errorLabel) {
        System.out.println("Login attempt for user: " + username);
        errorLabel.setText("");

        if (username == null || username.trim().isEmpty()) {
            errorLabel.setText("Please enter your username.");
            return;
        }
        if (password == null || password.isEmpty()) {
            errorLabel.setText("Please enter your password.");
            return;
        }

        try {
            User user = authService.login(username.trim(), password);

            // Navigate based on user role
            switch (user.getRole()) {
                case SYSTEM_ADMIN -> new AdminController(stage, user).show();
                case ORGANIZATION_ADMIN -> new OrgAdminController(stage, user).show();
                case DONOR -> new DonorController(stage, user).show();
                default -> errorLabel.setText("Unknown user role.");
            }
        } catch (UserNotApprovedException e) {
            errorLabel.setText(e.getMessage());
        } catch (IllegalArgumentException e) {
            errorLabel.setText(e.getMessage());
        }
    }

    private void styleInput(TextField field) {
        String normal = String.format(
                "-fx-background-color: %s; -fx-border-color: %s; -fx-border-width: 1; -fx-border-radius: 4; -fx-background-radius: 4; -fx-padding: 8 12; -fx-font-size: 14px;",
                CARD, BORDER);
        String focused = String.format(
                "-fx-background-color: %s; -fx-border-color: %s; -fx-border-width: 2; -fx-border-radius: 4; -fx-background-radius: 4; -fx-padding: 8 12; -fx-font-size: 14px;",
                CARD, RING);
        field.setStyle(normal);
        field.focusedProperty().addListener((obs, o, n) -> field.setStyle(n ? focused : normal));
    }
}
