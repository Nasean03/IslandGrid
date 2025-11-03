package com.islandgrid;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;

public class Login extends Application {

    @Override
    public void start(Stage stage) {
        Label titleLabel = new Label("🔋 Island Grid Login");
        titleLabel.setStyle("-fx-font-size: 22px; -fx-font-weight: bold;");

        TextField usernameField = new TextField();
        usernameField.setPromptText("Username");

        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("Password");

        Button loginButton = new Button("Login");
        Button registerButton = new Button("Register");
        Label messageLabel = new Label();

        HBox buttonBox = new HBox(10, loginButton, registerButton);
        VBox layout = new VBox(15, titleLabel, usernameField, passwordField, buttonBox, messageLabel);
        layout.setPadding(new Insets(25));
        layout.setStyle("-fx-background-color: wheat; -fx-alignment: center;");

        Scene scene = new Scene(layout, 350, 300);
        stage.setScene(scene);
        stage.setTitle("Island Grid Login");
        stage.show();

        // --- Login button logic ---
        loginButton.setOnAction(e -> {
            String username = usernameField.getText().trim();
            String password = passwordField.getText().trim();

            if (DatabaseManager.validateUser(username, password)) {
                messageLabel.setText("✅ Login successful!");
                messageLabel.setStyle("-fx-text-fill: green;");

                // Launch GameView
                GameView game = new GameView();
                try {
                    game.start(new Stage());
                    stage.close();
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            } else {
                messageLabel.setText("❌ Invalid username or password");
                messageLabel.setStyle("-fx-text-fill: red;");
            }
        });

        // --- Register button logic ---
        registerButton.setOnAction(e -> {
            String username = usernameField.getText().trim();
            String password = passwordField.getText().trim();

            if (DatabaseManager.registerUser(username, password)) {
                messageLabel.setText("✅ Account created! You can log in now.");
                messageLabel.setStyle("-fx-text-fill: green;");
            } else {
                messageLabel.setText("⚠️ Username already exists or DB error.");
                messageLabel.setStyle("-fx-text-fill: red;");
            }
        });
    }

    public static void main(String[] args) {
        launch(args);
    }
}
