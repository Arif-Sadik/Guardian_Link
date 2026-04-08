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
        
        // Run database migrations (creates notifications table, adds new columns, etc.)
        try {
            DatabaseMigration.runMigrations();
        } catch (Exception e) {
            System.err.println("Warning: Database migration failed: " + e.getMessage());
            e.printStackTrace();
        }

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
