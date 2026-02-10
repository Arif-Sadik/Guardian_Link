package controller;

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

/**
 * Login screen — Figma-matched split-panel layout.
 * Left: blue branding. Right: login form with role selector.
 * Any credentials accepted; role dropdown determines dashboard.
 */
public class AuthController {

    private final Stage stage;

    private static final String PRIMARY = "#2563eb";
    private static final String MUTED_FG = "#64748b";
    private static final String BORDER = "#e2e8f0";
    private static final String BG = "#f8f9fa";
    private static final String CARD = "#ffffff";
    private static final String ACCENT_BG = "#e0e7ff";
    private static final String ACCENT_FG = "#1e40af";
    private static final String RING = "#3b82f6";

    public AuthController(Stage stage) {
        this.stage = stage;
    }

    public void show() {

        // ═══════════ LEFT PANEL — blue branding ═══════════
        VBox leftPanel = new VBox(0);
        leftPanel.setAlignment(Pos.CENTER);
        leftPanel.setStyle("-fx-background-color: " + PRIMARY + ";");
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
        shieldCircle.getChildren().add(shieldIcon);

        Label brandTitle = new Label("GuardianLink");
        brandTitle.setFont(Font.font("Segoe UI", FontWeight.SEMI_BOLD, 36));
        brandTitle.setTextFill(Color.WHITE);

        Label brandSub = new Label("NGO Welfare Management System");
        brandSub.setFont(Font.font("Segoe UI", 16));
        brandSub.setTextFill(Color.web("#ffffffE6"));

        VBox featureBox = new VBox(8);
        featureBox.setPadding(new Insets(24));
        featureBox.setStyle("-fx-background-color: rgba(255,255,255,0.1); -fx-background-radius: 8;");
        Label featTitle = new Label("System Features");
        featTitle.setFont(Font.font("Segoe UI", FontWeight.SEMI_BOLD, 16));
        featTitle.setTextFill(Color.WHITE);
        featureBox.getChildren().add(featTitle);
        for (String f : new String[] {
                "\u2022 Role-based access control",
                "\u2022 Child welfare tracking",
                "\u2022 Donation management",
                "\u2022 Secure data handling",
                "\u2022 Comprehensive reporting" }) {
            Label fl = new Label(f);
            fl.setFont(Font.font("Segoe UI", 13));
            fl.setTextFill(Color.web("#ffffffCC"));
            featureBox.getChildren().add(fl);
        }
        VBox.setMargin(featureBox, new Insets(16, 0, 0, 0));
        leftContent.getChildren().addAll(shieldCircle, brandTitle, brandSub, featureBox);
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

        Label welcomeSub = new Label("Sign in to access the GuardianLink system");
        welcomeSub.setFont(Font.font("Segoe UI", 14));
        welcomeSub.setTextFill(Color.web(MUTED_FG));
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
        VBox.setMargin(passField, new Insets(8, 0, 20, 0));

        Label roleLabel = new Label("Select Role (Demo)");
        roleLabel.setFont(Font.font("Segoe UI", FontWeight.MEDIUM, 13));
        ComboBox<String> roleCombo = new ComboBox<>();
        roleCombo.getItems().addAll(
                "Caregiver / Organization Manager",
                "Donor",
                "System Administrator");
        roleCombo.setValue("Caregiver / Organization Manager");
        roleCombo.setMaxWidth(Double.MAX_VALUE);
        roleCombo.setStyle(String.format(
                "-fx-background-color: %s; -fx-border-color: %s; -fx-border-width: 1; -fx-border-radius: 4; -fx-background-radius: 4; -fx-padding: 4 8; -fx-font-size: 14px;",
                CARD, BORDER));
        VBox.setMargin(roleCombo, new Insets(8, 0, 24, 0));

        Label errorLabel = new Label();
        errorLabel.setTextFill(Color.web("#dc2626"));
        errorLabel.setFont(Font.font("Segoe UI", 13));
        errorLabel.setWrapText(true);

        Button signInBtn = new Button("\uD83D\uDD11  Sign In");
        signInBtn.setMaxWidth(Double.MAX_VALUE);
        signInBtn.setStyle(String.format(
                "-fx-background-color: %s; -fx-text-fill: white; -fx-font-size: 14px; -fx-font-weight: bold; -fx-padding: 10 0; -fx-background-radius: 6; -fx-cursor: hand;",
                PRIMARY));
        signInBtn.setOnMouseEntered(e -> signInBtn.setStyle(signInBtn.getStyle().replace(PRIMARY, "#1d4ed8")));
        signInBtn.setOnMouseExited(e -> signInBtn.setStyle(signInBtn.getStyle().replace("#1d4ed8", PRIMARY)));
        signInBtn.setOnAction(e -> handleLogin(roleCombo.getValue(), errorLabel));
        passField.setOnAction(e -> signInBtn.fire());

        Label footer = new Label("Academic Project Demo \u2022 JavaFX UI Design Reference");
        footer.setFont(Font.font("Segoe UI", 11));
        footer.setTextFill(Color.web(MUTED_FG));
        footer.setAlignment(Pos.CENTER);
        footer.setMaxWidth(Double.MAX_VALUE);
        VBox.setMargin(footer, new Insets(16, 0, 0, 0));
        footer.setStyle("-fx-border-color: " + BORDER + "; -fx-border-width: 1 0 0 0; -fx-padding: 16 0 0 0;");

        VBox demoBox = new VBox(8);
        demoBox.setPadding(new Insets(16));
        demoBox.setStyle(String.format(
                "-fx-background-color: %s80; -fx-border-color: %s; -fx-border-width: 1; -fx-background-radius: 6; -fx-border-radius: 6;",
                ACCENT_BG, ACCENT_BG));
        Label demoTitle = new Label("Demo Instructions:");
        demoTitle.setFont(Font.font("Segoe UI", FontWeight.SEMI_BOLD, 12));
        demoTitle.setTextFill(Color.web(ACCENT_FG));
        Label demoText = new Label(
                "Select any role from the dropdown and click \"Sign In\" to explore the corresponding user interface. No actual authentication is performed in this demo.");
        demoText.setFont(Font.font("Segoe UI", 11));
        demoText.setTextFill(Color.web(MUTED_FG));
        demoText.setWrapText(true);
        demoBox.getChildren().addAll(demoTitle, demoText);
        VBox.setMargin(demoBox, new Insets(24, 0, 0, 0));

        formBox.getChildren().addAll(
                welcomeTitle, welcomeSub,
                userLabel, userField,
                passLabel, passField,
                roleLabel, roleCombo,
                errorLabel, signInBtn, footer, demoBox);
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

    private void handleLogin(String roleChoice, Label errorLabel) {
        errorLabel.setText("");
        if (roleChoice == null) {
            errorLabel.setText("Please select a role.");
            return;
        }

        String r = roleChoice.toLowerCase();
        if (r.contains("caregiver") || r.contains("organization")) {
            OrganizationAdmin user = new OrganizationAdmin("orgadmin", "pass");
            user.setId(2);
            user.setApproved(true);
            new OrgAdminController(stage, user).show();
        } else if (r.contains("donor")) {
            Donor user = new Donor("donor", "pass");
            user.setId(3);
            user.setApproved(true);
            new DonorController(stage, user).show();
        } else if (r.contains("admin")) {
            SystemAdmin user = new SystemAdmin("admin", "pass");
            user.setId(1);
            user.setApproved(true);
            new AdminController(stage, user).show();
        } else {
            errorLabel.setText("Unknown role selected.");
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
