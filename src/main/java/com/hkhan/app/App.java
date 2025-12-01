package com.hkhan.app;

import java.sql.SQLException;
import java.util.Scanner;
import java.util.logging.*;
import java.io.IOException;

import com.hkhan.app.dao.ItemDAO;
import com.hkhan.app.model.Item;

public class App {
    // Logger for this class
    private static final Logger logger = AppLogger.setup(App.class);

    public static void main(String[] args) {
        logger.info("Inventory Management System started");

        // Scanner helps us read what the user types
        Scanner scanner = new Scanner(System.in);

        // Show the menu options to the user
        System.out.println("Welcome to the Inventory Management System");
        System.out.println("------------------------------------------");
        System.out.println("Please select an operation:");
        System.out.println(
                "1. Add Item\n2. View Items\n3. Update Item\n4. Delete Item\n5. Search Item\n6. Generate Report\n7. Exit");

        // Read user's menu choice
        String input = scanner.nextLine();

        // Conditionals for each menu option
        if (input.equals("1")) {
            logger.info("User selected: Add Item");
            addItemToDatabase(scanner);
        } else if (input.equals("2")) {
            logger.info("User selected: View Items");
            viewItemsFromDatabase();
        } else if (input.equals("3")) {
            logger.info("User selected: Update Item");
            // Placeholder for update item
        } else if (input.equals("4")) {
            logger.info("User selected: Delete Item");
            // Placeholder for delete item
        } else if (input.equals("5")) {
            logger.info("User selected: Search Item");
            searchItemInDatabase(scanner);
        } else if (input.equals("6")) {
            logger.info("User selected: Generate Report");
            // Placeholder for generate report
        } else {
            logger.info("User exited the application");
            return;
        }
    }

    // Functions to handle each menu option

    // Adds a new item to the database
    public static void addItemToDatabase(Scanner scanner) {
        logger.fine("Starting addItemToDatabase method");

        System.out.println("Enter item name:");
        String name = scanner.nextLine();

        // Convert string to number or decimal as needed
        System.out.println("Enter item quantity:");
        int quantity = Integer.parseInt(scanner.nextLine());

        System.out.println("Enter item price:");
        double price = Double.parseDouble(scanner.nextLine());

        // Create an item with the inputted information
        // The 0 is a placeholder for now, the database will assign a real ID number
        Item item = new Item(0, name, quantity, price);
        logger.info("Attempting to add item: " + name + ", Quantity: " + quantity + ", Price: $" + price);

        // ItemDAO.java handles all the backend database operations
        // This keeps the database code separate and organized
        try {
            // Create a new ItemDAO object to interact with the database
            ItemDAO dao = new ItemDAO();
            // The insertItem() function is performed in ItemDAO.java, so we call it here
            // and pass the item as a parameter
            dao.insertItem(item);
            // Confirm the item was saved successfully
            logger.info("Item successfully saved to database with ID: " + item.getId());
            System.out.println("Item saved to database!");
            System.out.println("  ID: " + item.getId());
            System.out.println("  Name: " + item.getName());
            System.out.println("  Quantity: " + item.getQuantity());
            System.out.println("  Price: $" + item.getPrice());
            // If something goes wrong while saving the item
            // SQL error for database issues
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Database error while saving item: " + name, e);
            System.err.println("✗ Error saving item to database: " + e.getMessage());
            System.err.println("  Make sure MySQL is running and environment variables are set:");
            System.err.println("  MYSQL_URL, MYSQL_USER, MYSQL_PASSWORD");
            // IllegalStateException for missing configuration
            // Note: This is the issue when MYSQL_URL, MYSQL_USER, or MYSQL_PASSWORD is not
            // set, which is what I was having trouble with
        } catch (IllegalStateException e) {
            logger.log(Level.SEVERE, "Configuration error: Missing environment variables", e);
            System.err.println("✗ " + e.getMessage());
            System.err.println("  Set environment variables first:");
            System.err.println(
                    "  $env:MYSQL_URL = 'jdbc:mysql://127.0.0.1:3306/Inventory?useSSL=false&serverTimezone=UTC'");
            System.err.println("  $env:MYSQL_USER = 'root'");
            System.err.println("  $env:MYSQL_PASSWORD = ''");
        }
    }

    public static void viewItemsFromDatabase() {
        logger.fine("Starting viewItemsFromDatabase method");

        // Create a new ItemDAO object to interact with the database
        ItemDAO dao = new ItemDAO();
        try {
            // Initialize count variable to display total number of items
            int count = 0;
            // An item object is initialized to store each item retrieved from the database
            // "dao.getAllItems()" gets a list of all items from the database
            // The for loop goes through each item in that list and prints its info
            for (Item item : dao.getAllItems()) {
                System.out.println("ID: " + item.getId() + ", Name: " + item.getName() + ", Quantity: "
                        + item.getQuantity() + ", Price: $" + item.getPrice());
                // Increment count for each item displayed, eventually matches total number of
                // items
                count++;
            }
            logger.info("Successfully retrieved and displayed " + count + " items from database");
            // If there's an error getting the items from the database
            // or if database connection fails
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Error retrieving items from database", e);
            System.err.println("✗ Error retrieving items from database: " + e.getMessage());
        }
    }

    public static void updateItemInDatabase(Item item) {
        // Placeholder for updating item in database
    }

    public static void deleteItemFromDatabase(int itemId) {
        // Placeholder for deleting item from database
    }

    public static void searchItemInDatabase(Scanner scanner) {
        logger.fine("Starting searchItemInDatabase method");

        // Ask user for the item name to search
        System.out.println("Enter item name to search:");
        // Set itemName to what the user types
        String itemName = scanner.nextLine();
        logger.info("Searching for item: " + itemName);

        // Create a new ItemDAO object to interact with the database
        ItemDAO dao = new ItemDAO();
        try {
            // An item object is initialized to store the search result
            // "dao.getItemByName(itemName)" searches for the user-inputted name in the
            // database
            Item item = dao.getItemByName(itemName);
            // If the item is not null (found in database), print its info
            if (item != null) {
                logger.info("Item found: " + itemName + " (ID: " + item.getId() + ")");
                System.out.println("Item found: ID: " + item.getId() + ", Name: " + item.getName() + ", Quantity: "
                        + item.getQuantity() + ", Price: $" + item.getPrice());
                // If the item is null (not found), tell the user it wasn't found
            } else {
                logger.warning("Item not found: " + itemName);
                System.out.println("Item not found with name: " + itemName);
            }
            // If there's an error searching for the item in the database
            // or if database connection fails
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Error searching for item: " + itemName, e);
            System.err.println("✗ Error searching for item in database: " + e.getMessage());
        }
    }

    public static void generateInventoryReport() {
        logger.fine("Starting generateInventoryReport method");
        // Placeholder for generating inventory report
    }
}

// Logging class with formatter, console handler, file handler, and manager
class AppLogger {
    // Define log file name
    private static final String LOG_FILE = "inventory-app.log";
    // Make sure logging is only initialized once
    private static boolean initialized = false;

    // Setup logging and return logger for the class
    // This returns a Logger object for the specified class
    public static Logger setup(Class<?> clazz) {

        // If not initialized yet, set up logging handlers
        if (!initialized) {
            try {
                // Set up root logger
                Logger root = Logger.getLogger("");

                // For each existing handler, remove it
                // This removes the default handlers
                for (Handler h : root.getHandlers())
                    root.removeHandler(h);

                // Console handler - INFO and above
                // Handles logging output to the console
                // Set console variable as a ConsoleHandler object to handle console output
                ConsoleHandler console = new ConsoleHandler();
                // Set console handler level to INFO
                console.setLevel(Level.INFO);
                // Set custom formatter for console output
                console.setFormatter(createFormatter());
                // Add console handler to root logger
                root.addHandler(console);

                // File handler - ALL levels
                // Logs everything to a file
                // Set file variable as a FileHandler object to handle file output
                FileHandler file = new FileHandler(LOG_FILE, true);
                // Set file handler level to ALL
                file.setLevel(Level.ALL);
                // Set custom formatter for file output
                file.setFormatter(createFormatter());
                // Add file handler to root logger
                root.addHandler(file);

                // Set root logger level to ALL to capture all log messages
                root.setLevel(Level.ALL);
                // Set initialized to true so we don't set up logging again
                initialized = true;
            // Catch any IO exceptions during setup
            } catch (IOException e) {
                System.err.println("Failed to set up logging: " + e.getMessage());
            }
        }
        return Logger.getLogger(clazz.getName());
    }

    // Create formatter: [LEVEL] [Class] message
    // Used by both console and file handlers
    private static Formatter createFormatter() {
        // Return a new Formatter instance
        return new Formatter() {
            // Format each log record
            // r is the log record object
            public String format(LogRecord r) {
                // Set class name to just the simple class name of the log record
                String className = r.getSourceClassName();
                // If the class name is not null and contains a dot, then extract the simple name
                // the dot means it's a full package name
                // So if the dot is there, we only want the part after the last dot
                if (className != null && className.contains(".")) {
                    // Class name is set to substring after last dot
                    // because after the last dot is the actual class name without the package
                    className = className.substring(className.lastIndexOf('.') + 1);
                }
                // Result string with level, class name, and message
                String result = "[" + r.getLevel() + "] [" + className + "] " + formatMessage(r) + "\n";
                // If there's an exception, include its info
                if (r.getThrown() != null) {
                    // The exception information is added to the result string
                    result += "Exception: " + r.getThrown() + "\n";
                }
                return result;
            }
        };
    }
}
