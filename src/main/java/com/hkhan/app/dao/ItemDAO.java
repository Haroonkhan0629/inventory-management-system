package com.hkhan.app.dao;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import com.hkhan.app.model.Item;

/**
 * Handles all database operations for Items.
 * This class connects to a MySQL database using connection info from environment variables.
 */
public class ItemDAO {
    private final String url;
    private final String user;
    private final String password;

    // Reads connection details from environment variables.
    public ItemDAO() {
        this.url = System.getenv("MYSQL_URL");
        this.user = System.getenv("MYSQL_USER");
        this.password = System.getenv("MYSQL_PASSWORD");
        
        // Stop if the database URL is missing.
        if (url == null || url.isEmpty()) {
            throw new IllegalStateException("MYSQL_URL environment variable not set");
        }
    }

    // Opens a connection to the database.
    private Connection getConnection() throws SQLException {
        // Use an empty password if none was provided.
        return DriverManager.getConnection(url, user, password != null ? password : "");
    }

    public void insertItem(Item item) throws SQLException {
        // SQL command to add a new item.
        String sql = "INSERT INTO items (name, quantity, price) VALUES (?, ?, ?)";
        
        // Connect to the database and run the insert.
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            
            // Fill in name, quantity, and price.
            stmt.setString(1, item.getName());
            stmt.setInt(2, item.getQuantity());
            stmt.setDouble(3, item.getPrice());
            
            // Run the command.
            int rowsAffected = stmt.executeUpdate();
            
            // Save the database-assigned ID back to the item.
            if (rowsAffected > 0) {
                try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        item.setId(generatedKeys.getInt(1));
                    }
                }
            }
        }
    }

    public List<Item> getAllItems() throws SQLException {
        // List to hold all items from the database.
        List<Item> items = new ArrayList<>();
        // SQL command to get all items.
        String sql = "SELECT id, name, quantity, price FROM items";
        
        // Connect to the database and run the query.
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            
            // Loop through each row and build an Item object.
            while (rs.next()) {
                Item item = new Item(
                    rs.getInt("id"),
                    rs.getString("name"),
                    rs.getInt("quantity"),
                    rs.getDouble("price")
                );
                // Add each item to the list.
                items.add(item);
            }
        }
        
        // Return the complete list.
        return items;
    }

    public Item getItemByName(String name) throws SQLException {
        // Start with null in case no match is found.
        Item item = null;
        // SQL command to find an item by name.
        String sql = "SELECT id, name, quantity, price FROM items WHERE name = ?";
        
        // Connect to the database and search.
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            // Fill in the name to search for.
            stmt.setString(1, name);

            try (ResultSet rs = stmt.executeQuery()) {
                // Build an Item if a match was found.
                if (rs.next()) {
                    item = new Item(
                        rs.getInt("id"),
                        rs.getString("name"),
                        rs.getInt("quantity"),
                        rs.getDouble("price")
                    );
                }
            }
        }
        
        // Return the found item, or null if not found.
        return item;
    }

    public void updateItem(Item item) throws SQLException {
        // SQL command to update an existing item.
        String sql = "UPDATE items SET name = ?, quantity = ?, price = ? WHERE id = ?";
        
        // Connect to the database and run the update.
        try (Connection conn = getConnection();
            PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            // Fill in the new values and the target item ID.
            stmt.setString(1, item.getName());
            stmt.setInt(2, item.getQuantity());
            stmt.setDouble(3, item.getPrice());
            stmt.setInt(4, item.getId());
            
            // Run the command.
            stmt.executeUpdate();
        }
    }

    public void deleteItem(int id) throws SQLException {
        // SQL command to delete an item by ID.
        String sql = "DELETE FROM items WHERE id = ?";
        
        // Connect to the database and run the delete.
        try (Connection conn = getConnection();
            PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            // Fill in the ID to delete.
            stmt.setInt(1, id);
            
            // Run the command.
            stmt.executeUpdate();
        }
    }

    public void generateReport() throws SQLException {
        // SQL command to count items and total their quantities and values.
        String sql = "SELECT COUNT(*) as total_items, SUM(quantity) as total_quantity, SUM(price * quantity) as total_value FROM items";
        
        // Connect to the database and run the query.
        try (Connection conn = getConnection();
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(sql)) {
            
            // Print the report totals if the query returned a result.
            if (rs.next()) {
                int totalItems = rs.getInt("total_items");
                int totalQuantity = rs.getInt("total_quantity");
                double totalValue = rs.getDouble("total_value");
                // Print totals to the console.
                System.out.println("\n========== INVENTORY REPORT ==========");
                System.out.println("Total Items in Inventory: " + totalItems);
                System.out.println("Total Quantity: " + totalQuantity);
                System.out.println("Total Inventory Value: $" + String.format("%.2f", totalValue));
                System.out.println("======================================\n");
            }
        }
    }
}

