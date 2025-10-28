package com.islandgrid;

import javafx.application.Application;
import javafx.stage.Stage;

public class Main extends Application {
    @Override
    public void start(Stage primaryStage) {
        GameView game = new GameView();
        game.start(primaryStage);
    }

    public static void main(String[] args) {
        launch(args);
    }
}
