package com.islandgrid;

import java.sql.*;
import java.io.*;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class DatabaseManager {

    // 🔧 CONFIG: Toggle database mode automatically or manually
    private static final String URL = "jdbc:mysql://localhost:3306/islandgrid";
    private static final String USER = "root";  // Change if needed
    private static final String PASSWORD = "";  // Your MySQL password
    private static final String USER_FILE = "users.txt"; // Fallback file for deployment

    // Automatically detect if MySQL is available
    private static final boolean USE_MYSQL = isMySQLAvailable();

    // --- CONNECTION ---
    public static Connection connect() {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            return DriverManager.getConnection(URL, USER, PASSWORD);
        } catch (ClassNotFoundException e) {
            System.err.println("❌ MySQL JDBC Driver not found!");
            return null;
        } catch (SQLException e) {
            System.err.println("❌ Database connection failed: " + e.getMessage());
            return null;
        }
    }

    // --- PASSWORD HASHING ---
    private static String hashPassword(String password) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hashed = md.digest(password.getBytes());
            StringBuilder sb = new StringBuilder();
            for (byte b : hashed) sb.append(String.format("%02x", b));
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 algorithm not found");
        }
    }

    // --- VALIDATE USER ---
    public static boolean validateUser(String username, String password) {
        if (!USE_MYSQL) return validateFromFile(username, password);

        Connection conn = connect();
        if (conn == null) return false;

        String sql = "SELECT * FROM users WHERE username=? AND password=?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, username);
            stmt.setString(2, hashPassword(password)); // ✅ use hashed passwords
            ResultSet rs = stmt.executeQuery();
            return rs.next();
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // --- REGISTER USER ---
    public static boolean registerUser(String username, String password) {
        if (!USE_MYSQL) return registerToFile(username, password);

        Connection conn = connect();
        if (conn == null) return false;

        String sql = "INSERT INTO users (username, password) VALUES (?, ?)";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, username);
            stmt.setString(2, hashPassword(password)); // ✅ hash before saving
            stmt.executeUpdate();
            return true;
        } catch (SQLException e) {
            System.out.println("⚠️ Username already exists or DB error.");
            return false;
        }
    }

    // --- FILE FALLBACK VALIDATION ---
    private static boolean validateFromFile(String username, String password) {
        try (BufferedReader br = new BufferedReader(new FileReader(USER_FILE))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts.length == 2 && parts[0].equals(username) && parts[1].equals(password))
                    return true;
            }
        } catch (IOException e) {
            System.out.println("⚠️ Could not read user file: " + e.getMessage());
        }
        return false;
    }

    // --- FILE FALLBACK REGISTRATION ---
    private static boolean registerToFile(String username, String password) {
        // Prevent duplicate usernames
        if (validateFromFile(username, password)) return false;

        try (BufferedWriter bw = new BufferedWriter(new FileWriter(USER_FILE, true))) {
            bw.write(username + "," + password);
            bw.newLine();
            return true;
        } catch (IOException e) {
            System.out.println("⚠️ Could not write to user file: " + e.getMessage());
            return false;
        }
    }

    // --- CHECK MYSQL AVAILABILITY ---
    private static boolean isMySQLAvailable() {
        try (Connection test = DriverManager.getConnection(URL, USER, PASSWORD)) {
            System.out.println("✅ MySQL detected — using database mode.");
            return true;
        } catch (Exception e) {
            System.out.println("⚠️ MySQL not available — using file mode.");
            return false;
        }
    }
}
