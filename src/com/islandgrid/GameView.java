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
        canvas = new Canvas(GRID_WIDTH * TILE_SIZE + 300, GRID_HEIGHT * TILE_SIZE);
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
        Scene scene = new Scene(root, GRID_WIDTH * TILE_SIZE + 300, GRID_HEIGHT * TILE_SIZE, Color.WHEAT);
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
        // Clear HUD background area
        gc.setFill(Color.WHEAT); // or whatever your main background color is
        gc.fillRect(GRID_WIDTH * TILE_SIZE, 0, 300, GRID_HEIGHT * TILE_SIZE);
        double barWidth = 200;
        double barX = GRID_WIDTH * TILE_SIZE + 50; // places it beside grid
        double baseY = 300;

        gc.setFill(Color.BLACK);
        gc.setFont(new Font("Verdana", 20));
        gc.fillText("ISLAND GRID STATUS:", barX, baseY-30);

        gc.setFont(new Font("Verdana", 12));

        // === Energy Supply Bar ===
        gc.setFill(Color.BLACK);
        gc.fillText("Energy Supply", barX, baseY - 5);
        gc.setFill(Color.GRAY);
        gc.fillRect(barX, baseY, barWidth, 15);

        double supplyRatio = energyManager.getEnergySupply() / 150.0;
        if (energyManager.getEnergySupply() > energyManager.getEnergyDemand() + 20) {
            gc.setFill(Color.RED);
        } else {
            gc.setFill(Color.ORANGE);
        }
        gc.fillRect(barX, baseY, Math.min(supplyRatio * barWidth, barWidth), 15);

        gc.setFill(Color.BLACK);
        gc.fillText(String.format("%d", energyManager.getEnergySupply()), barX + barWidth + 10, baseY + 12);


        // === Energy Demand Bar ===
        gc.setFill(Color.BLACK);
        gc.fillText("Energy Demand", barX, baseY + 40);
        gc.setFill(Color.GRAY);
        gc.fillRect(barX, baseY + 45, barWidth, 15);

       // double supply = energyManager.getEnergySupply();
        double demandRatio = energyManager.getEnergyDemand() / 300.0;
        gc.setFill(Color.LIGHTBLUE);
        gc.fillRect(barX, baseY + 45, Math.min(demandRatio * barWidth, barWidth), 15);

        gc.setFill(Color.BLACK);
        gc.fillText(String.format("%d", energyManager.getEnergyDemand()), barX + barWidth + 10, baseY + 57);


        // === Battery Bar ===
        gc.setFill(Color.BLACK);
        gc.fillText("Battery Level", barX, baseY + 80);
        gc.setFill(Color.GRAY);
        gc.fillRect(barX, baseY + 85, barWidth, 15);

        double batteryRatio = energyManager.getBatteryLevel() / (double) energyManager.getBatteryCapacity();
        if (batteryRatio > 0.85) {
            gc.setFill(Color.RED);   // overcharged
        } else if (batteryRatio < 0.15) {
            gc.setFill(Color.YELLOW); // low
        } else {
            gc.setFill(Color.GREEN);  // healthy
        }
        gc.fillRect(barX, baseY + 85, Math.min(batteryRatio * barWidth, barWidth), 15);

        gc.setFill(Color.WHITE);
        gc.fillText(String.format("%d / %d", energyManager.getBatteryLevel(),
        energyManager.getBatteryCapacity()), barX + barWidth - 60, baseY + 97);


        // === Pollution Bar ===
        gc.setFill(Color.BLACK);
        gc.fillText("Pollution Level", barX, baseY + 120);
        gc.setFill(Color.GRAY);
        gc.fillRect(barX, baseY + 125, barWidth, 15);

        double pollutionRatio = energyManager.getPollutionLevel() / 100.0;
        gc.setFill(Color.BLACK);
        gc.fillRect(barX, baseY + 125, Math.min(pollutionRatio * barWidth, barWidth), 15);

        gc.setFill(Color.BLACK);
        gc.fillText(String.format("%.0f%%", pollutionRatio * 100), barX + barWidth + 10, baseY + 137);


        // === Alerts ===
        if (energyManager.isPowerCrisis()) {
            gc.setFill(Color.RED);
            gc.fillText("⚠️ Power Crisis!", barX, baseY + 170);
        }
        if (energyManager.isOvercharged()) {
            gc.setFill(Color.RED);
            gc.fillText("⚡ Overcharged!", barX, baseY + 190);
        }
    }

    private void draw() {
        gc.setFill(Color.WHEAT); 
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

        double panelX = GRID_WIDTH * TILE_SIZE + 50;
        double panelY = 40; // top of HUD area

        gc.setFill(Color.BLACK);
        gc.setFont(new Font("Verdana", 20));
        gc.fillText("Next Piece:", panelX, panelY);

        int[][] shape = nextPiece.getShape();
        gc.setFill(nextPiece.getColor());

        for (int i = 0; i < shape.length; i++) {
            for (int j = 0; j < shape[i].length; j++) {
                if (shape[i][j] == 1) {
                    int drawX = (int) (panelX + j * TILE_SIZE);
                    int drawY = (int) (panelY + 20 + i * TILE_SIZE);
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
