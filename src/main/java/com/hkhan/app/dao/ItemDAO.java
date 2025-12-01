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

    // Set up database connection information
    // Gets the database location, username, and password from secure settings.
    public ItemDAO() {
        // Get database connection info from system settings (not from code)
        this.url = System.getenv("MYSQL_URL");
        this.user = System.getenv("MYSQL_USER");
        this.password = System.getenv("MYSQL_PASSWORD");
        
        // Check that we have the required information
        if (url == null || url.isEmpty()) {
            // Can't connect without a database URL
            throw new IllegalStateException("MYSQL_URL environment variable not set");
        }
    }

    // Opens a connection to the database
    private Connection getConnection() throws SQLException {
        // Connect to the database using the saved information
        // If there's no password, use an empty one
        return DriverManager.getConnection(url, user, password != null ? password : "");
    }

    public void insertItem(Item item) throws SQLException {
        // This command tells the database to add a new item
        // The ? marks are placeholders that are filled in later
        String sql = "INSERT INTO items (name, quantity, price) VALUES (?, ?, ?)";
        
        // Connect to database and prepare the command
        // The connection closes automatically when done 
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            
            // Fill in the ? placeholders with actual item data
            stmt.setString(1, item.getName());      // First ? = name
            stmt.setInt(2, item.getQuantity());     // Second ? = quantity
            stmt.setDouble(3, item.getPrice());     // Third ? = price
            
            // Send the command to the database
            int rowsAffected = stmt.executeUpdate();
            
            // If the item was saved successfully 
            if (rowsAffected > 0) {
                // Get the ID the database created for this item
                try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        // Set the ID to the item
                        item.setId(generatedKeys.getInt(1));
                    }
                }
            }
        }
    }

    public List<Item> getAllItems() throws SQLException {
        // List to hold all retrieved items
        List<Item> items = new ArrayList<>();
        // SQL command to get all items
        // We are storing id, name, quantity, and price fields in the sql variable
        String sql = "SELECT id, name, quantity, price FROM items";
        
        // Connect to database and prepare the command
        // The connection closes automatically when done
        try (Connection conn = getConnection();
            // Prepare the SQL statement
            // "sql" variable is passed to the prepareStatement function
             PreparedStatement stmt = conn.prepareStatement(sql);
             // "rs" variable stores the results of the executed query
             ResultSet rs = stmt.executeQuery()) {
            
            // rs.next() moves to the next row and returns false if there are no more rows
            // While the executed query returns more rows, keep processing them
            while (rs.next()) {
                // Create a new Item object using data from the current row
                // "rs" variable is used to get each field's value
                // "rs" has the results of the executed query
                Item item = new Item(
                    rs.getInt("id"),
                    rs.getString("name"),
                    rs.getInt("quantity"),
                    rs.getDouble("price")
                );
                // Add the created Item to the list
                // The loop continues until all rows are processed
                items.add(item);
            }
        }
        
        // return the list of items
        return items;
    }

    public Item getItemByName(String name) throws SQLException {
        // Set item to null initially
        Item item = null;
        // Prepare SQL command to get item by name
        String sql = "SELECT id, name, quantity, price FROM items WHERE name = ?";
        
        // Connect to database and prepare the command
        // The connection closes automatically when done
        try (Connection conn = getConnection();
            // "sql" variable is passed to the prepareStatement function
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            // Fill in the ? placeholder with the actual name
            // stmt.setString(1, name) sets the first placeholder (?) in the SQL query 
            // to the value of the 'name' parameter.
            stmt.setString(1, name);

            // "rs" variable stores the results of the executed query
            try (ResultSet rs = stmt.executeQuery()) {
                // "rs.next()" moves to the next row and returns false if there are no more rows
                // "rs.next()" executes the query, which will return one row with the matching name (if it exists)
                // If the executed query found a matching row, then create the Item object using data from that row
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
        
        // return the found item (or null if not found)
        return item;
    }

    public void updateItem(Item item) throws SQLException {
        // Prepare SQL command to update an existing item
        // Updates an existing item in the database with new values (set to ? placeholders)
        String sql = "UPDATE items SET name = ?, quantity = ?, price = ? WHERE id = ?";
        
        // Connect to database and prepare the command
        // The connection closes automatically when done
        try (Connection conn = getConnection();
            // "sql" variable is passed to the prepareStatement function
            PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            // Fill in the ? placeholders with actual item data 
            // id is used in the WHERE clause to identify which item to update
            // first parameter finds which ? to fill (1 = first ?, 2 = second ?, etc)
            // second parameter is the value to set it to (which comes from the item object)
            stmt.setString(1, item.getName());   
            stmt.setInt(2, item.getQuantity());    
            stmt.setDouble(3, item.getPrice());   
            stmt.setInt(4, item.getId());           
            
            // Send the command to the database to update the item
            stmt.executeUpdate();
        }
    }

    public void deleteItem(int id) throws SQLException {
        // Prepare SQL command to delete an item by id
        // Deletes an item from the database based on its id
        String sql = "DELETE FROM items WHERE id = ?";
        
        // Connect to database and prepare the command
        // The connection closes automatically when done
        try (Connection conn = getConnection();
            // "sql" variable is passed to the prepareStatement function
            PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            // Fill in the first ? placeholder with the actual id (there is only one ? here)
            stmt.setInt(1, id);
            
            // Send the command to the database to delete the item
            stmt.executeUpdate();
        }
    }
}

