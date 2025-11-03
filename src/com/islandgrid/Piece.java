package com.islandgrid;

import javafx.scene.paint.Color;

public class Piece {

    // Piece type (Solar, Wind, Hydro, Battery, Fossil)
    private String type;

    // Shape of the piece: 1 = filled, 0 = empty
    private int[][] shape;

    // Position on the grid
    private int x; // column
    private int y; // row

    // Color for rendering
    private Color color;

    // Constructor
    public Piece(String type, int[][] shape, Color color) {
        this.type = type;
        this.shape = shape;
        this.color = color;
        this.x = 0; // default start column
        this.y = 0; // default start row
    }

    // Getters
    public String getType() { return type; }
    public int[][] getShape() { return shape; }
    public int getX() { return x; }
    public int getY() { return y; }
    public Color getColor() { return color; }

    // Setters
    public void setX(int x) { this.x = x; }
    public void setY(int y) { this.y = y; }

    // Rotate piece (stub, fill logic later)
    public void rotateClockWise() {
        int rows = shape.length; // number of rows
        int cols = shape[0].length; // number of columns
        int[][] rotated = new int[cols][rows]; //temporary array

        for(int i = 0; i < rows; i++) {
            for(int j  = 0; j < cols; j++) {
                rotated [j][rows - i - 1] = shape[i][j]; //transposes the array and then reverses the rows
            }
        }
        shape = rotated;
    }

        // Rotate piece (stub, fill logic later)
    public void rotateAntiClockwise() {
        int rows = shape.length; // number of rows
        int cols = shape[0].length; // number of columns
        int[][] rotated = new int[cols][rows]; //temporary array

        for(int i = 0; i < rows; i++) {
            for(int j  = 0; j < cols; j++) {
                rotated [cols - j - 1][i] = shape[i][j]; //transposes the array and then reverses the columns
            }
        }
        shape = rotated;
    }

    // Move piece
    public void moveLeft() { x--; }
    public void moveRight() { x++; }
    public void moveDown() { y++; }

  
    public static Piece createPiece(String type) {
        switch (type) {
            case "solar":
                 return new Piece("Solar", new int[][]{
                {1, 1, 1},
                {0, 1, 0}
            }, Color.YELLOW);

            case "wind":
                 return new Piece("Wind", new int[][]{
                {1, 1}
            }, Color.WHITE);

            case "hydro":
                 return new Piece("Hydro", new int[][]{
                {1, 0},
                {1, 0},
                {1, 1}
            }, Color.BLUE);

            case "battery":
                 return new Piece("Battery", new int[][]{
                {1, 1},
                {1, 1}
            }, Color.GREEN);

            case "fossil":
                 return new Piece("Fossil", new int[][]{
                {1, 1, 1}
            }, Color.BLACK);

            default:
                return null;
        }
    }
}
