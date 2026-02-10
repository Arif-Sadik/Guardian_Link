package app;

import controller.AuthController;
import javafx.application.Application;
import javafx.stage.Stage;
import util.DBUtil;

/**
 * Application entry point — initializes the database and shows the login
 * screen.
 */
public class Main extends Application {

    @Override
    public void start(Stage primaryStage) {
        // Initialize database (creates tables + seeds admin on first run)
        DBUtil.initialize();

        // Set initial window size
        primaryStage.setWidth(1200);
        primaryStage.setHeight(800);
        primaryStage.setMinWidth(800);
        primaryStage.setMinHeight(600);

        // Show login screen
        new AuthController(primaryStage).show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
