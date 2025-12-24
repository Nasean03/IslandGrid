package com.islandgrid;

import javafx.application.Application;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class WelcomeMenu extends Application {

    public static String loggedInUser = null; // stores the active user

    @Override
    public void start(Stage stage) {
        Label title = new Label("🌴 Welcome to Island Grid");
        title.setStyle("-fx-font-size: 28px; -fx-font-weight: bold; -fx-text-fill: darkgreen;");

        Label userLabel = new Label();
        if (loggedInUser != null)
            userLabel.setText("👋 Welcome back, " + loggedInUser + "!");
        else
            userLabel.setText("Guest mode");

        Button playGuest = new Button("▶ Play");
        Button login = new Button("🔐 Login / Register");
        Button instructions = new Button("📘 View Instructions");
        Button exit = new Button("❌ Exit");

        playGuest.setStyle("-fx-font-size: 16px; -fx-min-width: 200px;");
        login.setStyle("-fx-font-size: 16px; -fx-min-width: 200px;");
        instructions.setStyle("-fx-font-size: 16px; -fx-min-width: 200px;");
        exit.setStyle("-fx-font-size: 16px; -fx-min-width: 200px;");

        // --- Button actions ---
        playGuest.setOnAction(e -> {
            GameView game = new GameView();
            try {
                if (loggedInUser != null) {
                    System.out.println("Logged in as: " + loggedInUser);
                    game.setCurrentUser(loggedInUser);
                } else {
                    loggedInUser = "Guest";
                    System.out.println("Playing as Guest");
                }

                game.start(new Stage());
                stage.close();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });

        login.setOnAction(e -> {
            LoginView loginView = new LoginView();
            try {
                loginView.start(new Stage());
                stage.close();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });

        instructions.setOnAction(e -> {
            InstructionsView info = new InstructionsView();
            try {
                info.start(new Stage());
                stage.close();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });

        exit.setOnAction(e -> stage.close());

        VBox layout = new VBox(20, title, userLabel, playGuest, login, instructions, exit);
        layout.setAlignment(Pos.CENTER);
        layout.setStyle("-fx-background-color: wheat;");

        Scene scene = new Scene(layout, 500, 400);
        stage.setTitle("Island Grid - Main Menu");
        stage.setScene(scene);
        stage.show();
    }
}
