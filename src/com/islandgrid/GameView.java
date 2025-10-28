package com.islandgrid;

import javafx.scene.*;
import javafx.scene.canvas.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import javafx.animation.AnimationTimer;
//import javafx.scene.input.KeyCode;


public class GameView {
    private Canvas canvas;
    private GraphicsContext gc;
    private Piece currentPiece;
    private Piece nextPiece;
    private Grid grid;
    private boolean gameOver = false;
    private EnergyManager energyManager;
    private boolean paused = false;

    private static final int TILE_SIZE = 40; // size of one grid cell
    private static final int GRID_WIDTH = 20; 
    private static final int GRID_HEIGHT = 15;

    private long lastFallTime = 0;
    private long fallInterval = 500_000_000; // nanoseconds = 0.5s


    public void start(Stage stage) {
        canvas = new Canvas(GRID_WIDTH * TILE_SIZE, GRID_HEIGHT * TILE_SIZE);
        gc = canvas.getGraphicsContext2D();

        energyManager = new EnergyManager();
        //System.out.println("Created EnergyManager instance in GameView: " + System.identityHashCode(energyManager));
        grid = new Grid(GRID_HEIGHT, GRID_WIDTH, energyManager, this);
        energyManager.setGrid(grid);
        energyManager.setGameView(this);

        spawnPiece();
        currentPiece.setX(4);
        currentPiece.setY(2);

      
        Group root = new Group();
        root.getChildren().add(canvas);
        Scene scene = new Scene(root, GRID_WIDTH * TILE_SIZE, GRID_HEIGHT * TILE_SIZE, Color.DARKSLATEGRAY);
        stage.setScene(scene);
        stage.setTitle("Island Grid - Renewable Puzzle");
        stage.show();

      new AnimationTimer() {
            @Override
            public void handle(long now) {
                // Auto fall
                if (now - lastFallTime > fallInterval) {
                    update();
                    lastFallTime = now;
                }
                draw();
            }
        }.start();
    

        //input handling
        scene.setOnKeyPressed(event -> {
            if (currentPiece == null) return;

            switch (event.getCode()) {
                case LEFT:
                    if (grid.canMove(currentPiece, currentPiece.getX() - 1, currentPiece.getY())) {
                        currentPiece.moveLeft();
                    }
                    break;

                case RIGHT:
                    if (grid.canMove(currentPiece, currentPiece.getX() + 1, currentPiece.getY())) {
                        currentPiece.moveRight();
                    }
                    break;

                case DOWN:
                    if (grid.canMove(currentPiece, currentPiece.getX(), currentPiece.getY() + 1)) {
                        currentPiece.moveDown();
                    }
                    break;

                case UP:
                    currentPiece.rotateClockWise();
                    if (!grid.canMove(currentPiece, currentPiece.getX(), currentPiece.getY())) {
                        currentPiece.rotateAntiClockwise(); // undo if invalid
                    }
                    break;
                
                case Z:  // press Z for anticlockwise rotation
                    currentPiece.rotateAntiClockwise(); // first, rotate 90° anticlockwise
                    if (!grid.canMove(currentPiece, currentPiece.getX(), currentPiece.getY())) {
                        currentPiece.rotateClockWise(); // undo if invalid
                    }
                    break;

                case P:   // <-- Pause the game when 'P' is pressed
                    paused = !paused;
                    break;

                case R:   // <-- Reset the game when 'R' is pressed
                    resetGame();
                    break;

                default:
                    // Ignore other keys
                    break;
            }
        });
    }

    private void resetGame() {
        grid.reset();      // clear all locked pieces
        spawnPiece();      // spawn a new piece at the top
        gameOver = false; // reset game over state
        energyManager = new EnergyManager(); // reset energy manager
        grid.setEnergyManager(energyManager);
        energyManager.setGrid(grid);
        energyManager.setGameView(this);
        energyManager.resetPollution();
    }

    private void spawnPiece() {
        if (gameOver) {
            System.out.println("Game over — no new pieces will spawn."); //debugging purposes
            return;
        }

        // If there's no next piece yet, generate one
        if (nextPiece == null) {
            nextPiece = Piece.createPiece(randomType());
        }

        // Promote nextPiece to currentPiece
        currentPiece = nextPiece;
        currentPiece.setX(GRID_WIDTH / 2 - 1);
        currentPiece.setY(0);

        // Generate a new nextPiece
        nextPiece = Piece.createPiece(randomType());
        
        return;
    }

    private String randomType() {
        String[] RenewableTypes = {"solar", "wind","hydro","battery","fossil"};
        //String[] RenewableTypes = {"fossil"};

        int index = (int) (Math.random() * RenewableTypes.length);
        String type = RenewableTypes[index];
        return type;
    }

    private void update() {
        if(gameOver || paused) return;

        if (grid.canMove(currentPiece, currentPiece.getX(), currentPiece.getY() + 1)) {
            currentPiece.moveDown();
        } else {
            grid.lockPiece(currentPiece);
            energyManager.addSupply(currentPiece.getType());
            energyManager.DemandChange();
            energyManager.pollutionAlert();

            
        // Check if any part of the piece is above the top row
        if (currentPiece.getY() <= 0) {
            if (!gameOver) {
                System.out.println("Game Over!");
            }
            gameOver = true;
            return; // stop further updates
        }

            spawnPiece();
        }
    }

    private void drawHUD(GraphicsContext gc) {
        gc.setFill(Color.BLACK);
        gc.setFont(new Font("Verdana", 12));
        gc.fillText("Energy Supply: " + energyManager.getEnergySupply(), 10, 20);
        gc.fillText("Energy Demand: " + energyManager.getEnergyDemand(), 10, 40);
        gc.fillText("Battery Level: " + energyManager.getBatteryLevel(), 10, 60);
        gc.fillText("Battery Capacity: " + energyManager.getBatteryCapacity(), 10, 80);
        gc.fillText("Pollution Level: " + energyManager.getPollutionLevel(), 10, 100);

        if (energyManager.isPowerCrisis()) {
            gc.setFill(Color.RED);
            gc.fillText("Power Crisis!", 10, 120);
        }
        if (energyManager.isOvercharged()) {
            gc.setFill(Color.RED);
            gc.fillText("Overcharged!", 10, 140);
        }
    }

    private void draw() {
        gc.setFill(Color.rgb(245, 218, 191)); 
        gc.fillRect(0, 0, GRID_WIDTH * TILE_SIZE, GRID_HEIGHT * TILE_SIZE);

        grid.draw(gc);

        // Draw the current piece
        if (!gameOver && currentPiece != null) {
            int[][] shape = currentPiece.getShape();
            gc.setFill(currentPiece.getColor());

            for (int i = 0; i < shape.length; i++) {
                for (int j = 0; j < shape[i].length; j++) {
                    if (shape[i][j] == 1) {
                        int drawX = (currentPiece.getX() + j) * TILE_SIZE;
                        int drawY = (currentPiece.getY() + i) * TILE_SIZE;
                        gc.fillRect(drawX, drawY, TILE_SIZE, TILE_SIZE);
                        gc.setStroke(Color.BLACK);
                          gc.strokeRect(drawX, drawY, TILE_SIZE, TILE_SIZE);
                    }
                }
            }
        }
        drawHUD(gc);
        drawNextPiece(gc);
    }

    private void drawNextPiece(GraphicsContext gc) {
        if (nextPiece == null) return;

        gc.setFill(Color.BLACK);
        gc.setFont(new Font("Verdana", 20));
        gc.fillText("Next Piece:", GRID_WIDTH * TILE_SIZE - 160, 40);

        int[][] shape = nextPiece.getShape();
        gc.setFill(nextPiece.getColor());

        for (int i = 0; i < shape.length; i++) {
            for (int j = 0; j < shape[i].length; j++) {
                if (shape[i][j] == 1) {
                    int drawX = GRID_WIDTH * TILE_SIZE - 160 + j * TILE_SIZE;
                    int drawY = 60 + i * TILE_SIZE;
                    gc.fillRect(drawX, drawY, TILE_SIZE, TILE_SIZE);
                    gc.setStroke(Color.BLACK);
                    gc.strokeRect(drawX, drawY, TILE_SIZE, TILE_SIZE);
                }
            }
        }
    }


    public void setGameOver(boolean gameOver) {
        this.gameOver = true;
        grid.triggerGameOverMessage();
    }
}
