package com.islandgrid;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import java.io.*;

public class LoginView extends Application {

    private static final String REMEMBER_FILE = "user.txt";

    @Override
    public void start(Stage stage) {
        Label titleLabel = new Label("🔋 Island Grid Login");
        titleLabel.setStyle("-fx-font-size: 22px; -fx-font-weight: bold;");

        TextField usernameField = new TextField();
        usernameField.setPromptText("Username");

        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("Password");

        CheckBox rememberCheck = new CheckBox("Remember me");
        Button loginButton = new Button("Login");
        Button registerButton = new Button("Register");
        Label messageLabel = new Label();

        HBox buttonBox = new HBox(10, loginButton, registerButton);
        VBox layout = new VBox(15, titleLabel, usernameField, passwordField, rememberCheck, buttonBox, messageLabel);
        layout.setPadding(new Insets(25));
        layout.setStyle("-fx-background-color: wheat; -fx-alignment: center;");

        Scene scene = new Scene(layout, 350, 320);
        stage.setScene(scene);
        stage.setTitle("Island Grid Login");

        // Auto-fill remembered user
        try (BufferedReader br = new BufferedReader(new FileReader(REMEMBER_FILE))) {
            String savedUser = br.readLine();
            if (savedUser != null) usernameField.setText(savedUser);
            rememberCheck.setSelected(true);
        } catch (IOException ignored) {}

        stage.show();

        // LOGIN button logic
        loginButton.setOnAction(e -> {
            String username = usernameField.getText().trim();
            String password = passwordField.getText().trim();

            if (DatabaseManager.validateUser(username, password)) {
                messageLabel.setText("✅ Login successful!");
                messageLabel.setStyle("-fx-text-fill: green;");

                // Save username if 'Remember me' checked
                if (rememberCheck.isSelected()) {
                    try (BufferedWriter bw = new BufferedWriter(new FileWriter(REMEMBER_FILE))) {
                        bw.write(username);
                    } catch (IOException ignored) {}
                } else {
                    new File(REMEMBER_FILE).delete();
                }

                // Launch GameView
                InstructionsView info = new InstructionsView();
                info.setCurrentUser(username);
                try {
                    info.start(new Stage());
                    stage.close();
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            } else {
                messageLabel.setText("❌ Invalid username or password");
                messageLabel.setStyle("-fx-text-fill: red;");
            }
        });

        // REGISTER button logic
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
