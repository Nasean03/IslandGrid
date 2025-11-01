package com.islandgrid;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;

public class Grid {
    private int rows;
    private int cols;
    private Color[][] cells;
    private EnergyManager energyManager;
    private boolean showGameOverMessage = false; // shows game over message when true
    private GameView gameView;
    
    private static final int TILE_SIZE = 40;

     public void setEnergyManager(EnergyManager energyManager) {
        this.energyManager = energyManager;
    }
    
    public Grid(int rows, int cols, EnergyManager energyManager, GameView gameView) {
        this.rows = rows;
        this.cols = cols;
        this.energyManager = energyManager;
        this.cells = new Color[rows][cols];
        this.gameView = gameView;
    }

    public void reset() {
        for (int i = 0; i < rows; i++)
            for (int j = 0; j < cols; j++)
                cells[i][j] = null;
        
        showGameOverMessage = false;
    }

       // Optional: collision check stub
    public boolean canMove(Piece piece, int newX, int newY) {
        int[][] shape = piece.getShape();
        for(int i = 0; i < shape.length; i++) {
            for(int j  = 0; j < shape[i].length; j++) {
                if(shape[i][j] == 1)
                {
                    int gridX = newX + j;
                    int gridY = newY + i;

                    // Check boundaries
                    if(gridX < 0 || gridX >= cols || gridY < 0 || gridY >= rows) {
                        return false;
                    }

                    // Check collision with existing pieces
                    if(cells[gridY][gridX] != null) {
                        return false;
                    }
                }
            }
        }
        return true;
    }

    public void lockPiece(Piece piece) {
        int[][] shape = piece.getShape();
        int x = piece.getX();
        int y = piece.getY();

        for(int i = 0; i < shape.length; i++) {
            for(int j  = 0; j < shape[0].length; j++) {
                if(shape[i][j] == 1) {
                    cells[y + i][x + j] = piece.getColor(); // Mark cell as occupied + store original color
                }
            }
        }
    }

    public void draw(GraphicsContext gc) {
        int pollutionStage = energyManager.getPollutionStage();
        double darknessLevel = (pollutionStage >= 6) ? pollutionStage / 10.0 : 0.0;

        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                 if (cells[i][j] != null) {
                    gc.setFill(cells[i][j]);
                } else {
                    gc.setFill(Color.BEIGE);
                }
                 gc.fillRect(j * TILE_SIZE, i * TILE_SIZE, TILE_SIZE, TILE_SIZE);
                gc.setStroke(Color.BLACK);
                gc.strokeRect(j * TILE_SIZE, i * TILE_SIZE, TILE_SIZE, TILE_SIZE);
            }
        }//enfor

        gc.setFill(new Color(0.35, 0.28, 0.1, darknessLevel));
        gc.fillRect(0, 0, cols * TILE_SIZE, rows * TILE_SIZE);

        if (showGameOverMessage) {
             gc.setFill(Color.rgb(255, 200, 200));
            gc.setFont(new Font("Verdana", 30));
            gc.fillText("GAME OVER - BLACKOUT", TILE_SIZE * 5-1, TILE_SIZE * 7-1);
            gc.setFill(Color.RED);
            gc.setFont(new Font("Verdana", 30));
            gc.fillText("GAME OVER - BLACKOUT", TILE_SIZE * 5, TILE_SIZE * 7);
         }

    }   

    public void triggerGameOverMessage() {
    showGameOverMessage = true;
    }

        
        public int getRows() { return rows; }
        public int getCols() { return cols; }
    }
 

    