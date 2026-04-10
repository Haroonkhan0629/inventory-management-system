package com.hkhan.app;

import java.awt.GraphicsEnvironment;
import java.sql.SQLException;
import java.util.Scanner;
import java.util.logging.*;
import java.io.IOException;

import javax.swing.JOptionPane;

import com.hkhan.app.dao.ItemDAO;
import com.hkhan.app.model.Item;

public class App {
    // Logger for this class.
    private static final Logger logger = AppLogger.setup(App.class);

    public static void main(String[] args) {
        logger.info("Inventory Management System started");

        // If someone runs the app with a start option, follow that option.
        // --gui means "open the window version"
        // --cli means "open the text menu version"
        if (args != null && args.length > 0) {
            String firstArg = args[0].trim().toLowerCase();
            if (firstArg.equals("--gui")) {
                // Some environments (like certain servers) cannot show windows.
                if (GraphicsEnvironment.isHeadless()) {
                    System.out.println("GUI mode is not available in this environment.");
                    return;
                }
                InventoryGUI.launch();
                return;
            }
            if (firstArg.equals("--cli")) {
                runCliMode();
                return;
            }
        }

        // If this machine cannot show windows, start the text menu automatically.
        if (GraphicsEnvironment.isHeadless()) {
            runCliMode();
            return;
        }

        // If windows are available, show a simple "GUI or CLI" choice box.
        // This appears when you press Play and gives both options.
        Object[] options = { "Start GUI", "Start CLI", "Cancel" };
        int startupChoice = JOptionPane.showOptionDialog(
                null,
                "How would you like to start the Inventory Management System?",
                "Choose Startup Mode",
                JOptionPane.DEFAULT_OPTION,
                JOptionPane.QUESTION_MESSAGE,
                null,
                options,
                options[0]);

        // Button 1: open the window interface.
        if (startupChoice == 0) {
            InventoryGUI.launch();
            return;
        }

        // Button 2: open the text menu interface.
        if (startupChoice == 1) {
            runCliMode();
            return;
        }

        // If Cancel is pressed, simply stop here.
        logger.info("Application launch canceled by user");
    }

    // Runs the text menu version of the app.
    private static void runCliMode() {
        Boolean continueRunning = true;
        // Show the menu repeatedly until the user exits.
        while (continueRunning) {

            // Read text typed by the user.
            Scanner scanner = new Scanner(System.in);

            // Display the menu.
            System.out.println("Welcome to the Inventory Management System");
            System.out.println("------------------------------------------");
            System.out.println("Please select an operation:");
            System.out.println(
                    "1. Add Item\n2. View Items\n3. Update Item\n4. Delete Item\n5. Search Item\n6. Generate Report\n7. Exit");

            // Read the user's choice.
            String input = scanner.nextLine();

            // Run the selected menu option.
            if (input.equals("1")) {
                logger.info("User selected: Add Item");
                addItemToDatabase(scanner);
            } else if (input.equals("2")) {
                logger.info("User selected: View Items");
                viewItemsFromDatabase();
            } else if (input.equals("3")) {
                logger.info("User selected: Update Item");
                updateItemInDatabase(scanner);
            } else if (input.equals("4")) {
                logger.info("User selected: Delete Item");
                deleteItemFromDatabase(scanner);
            } else if (input.equals("5")) {
                logger.info("User selected: Search Item");
                searchItemInDatabase(scanner);
            } else if (input.equals("6")) {
                logger.info("User selected: Generate Report");
                generateInventoryReport();
            } else {
                logger.info("User exited the application");
                return;
            }
            // Ask if the user wants to continue.
            System.out.println("\nContinue? (y or n)");
            String answer = scanner.nextLine();
            if (answer.equals("y")) {
                continueRunning = true;
            } else {
                continueRunning = false;
                logger.info("Inventory Management System exited by user");
                System.out.println("Goodbye!");
            }
        }
    }

    // Functions to handle each menu option

    // Adds a new item to the database.
    public static void addItemToDatabase(Scanner scanner) {
        logger.fine("Starting addItemToDatabase method");

        System.out.println("Enter item name:");
        String name = scanner.nextLine();

        System.out.println("Enter item quantity:");
        int quantity = Integer.parseInt(scanner.nextLine());

        System.out.println("Enter item price:");
        double price = Double.parseDouble(scanner.nextLine());

        // Create an item with the entered values.
        // The ID is set to 0 for now; the database will assign a real one.
        Item item = new Item(0, name, quantity, price);
        logger.info("Attempting to add item: " + name + ", Quantity: " + quantity + ", Price: $" + price);

        try {
            // Save the item to the database.
            ItemDAO dao = new ItemDAO();
            dao.insertItem(item);
            logger.info("Item successfully saved to database with ID: " + item.getId());
            System.out.println("Item saved to database!");
            System.out.println("  ID: " + item.getId());
            System.out.println("  Name: " + item.getName());
            System.out.println("  Quantity: " + item.getQuantity());
            System.out.println("  Price: $" + item.getPrice());
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Database error while saving item: " + name, e);
            System.err.println("✗ Error saving item to database: " + e.getMessage());
            System.err.println("  Make sure MySQL is running and environment variables are set:");
            System.err.println("  MYSQL_URL, MYSQL_USER, MYSQL_PASSWORD");
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

        // Create a DAO object and fetch all items.
        ItemDAO dao = new ItemDAO();
        try {
            int count = 0;
            // Print each item and count them.
            for (Item item : dao.getAllItems()) {
                System.out.println("ID: " + item.getId() + ", Name: " + item.getName() + ", Quantity: "
                        + item.getQuantity() + ", Price: $" + item.getPrice());
                count++;
            }
            logger.info("Successfully retrieved and displayed " + count + " items from database");
            System.out.println("Total items: " + count);
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Error retrieving items from database", e);
            System.err.println("✗ Error retrieving items from database: " + e.getMessage());
        }
    }

    public static void updateItemInDatabase(Scanner scanner) {
        logger.fine("Starting updateItemInDatabase method");

        ItemDAO dao = new ItemDAO();
        try {
            // Show all items so the user can pick one.
            for (Item item : dao.getAllItems()) {
                System.out.println("ID: " + item.getId() + ", Name: " + item.getName() + ", Quantity: "
                        + item.getQuantity() + ", Price: $" + item.getPrice());
            }
            logger.info("Successfully retrieved and displayed items from database for update");

            // Ask for the ID and new values.
            System.out.println("Select an item by its ID to update");
            int itemId = Integer.parseInt(scanner.nextLine());
            System.out.println("Enter new name:");
            String newName = scanner.nextLine();
            System.out.println("Enter new quantity:");
            int newQuantity = Integer.parseInt(scanner.nextLine());
            System.out.println("Enter new price:");
            double newPrice = Double.parseDouble(scanner.nextLine());

            // Save the updated item.
            Item updatedItem = new Item(itemId, newName, newQuantity, newPrice);
            dao.updateItem(updatedItem);
            logger.info("Item successfully updated in database with ID: " + updatedItem.getId());
            System.out.println("Item updated successfully!");

            // Show the updated list.
            for (Item item : dao.getAllItems()) {
                System.out.println("ID: " + item.getId() + ", Name: " + item.getName() + ", Quantity: "
                        + item.getQuantity() + ", Price: $" + item.getPrice());
            }
            logger.info("Successfully retrieved and displayed items from database after update");
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Error retrieving items from database for update", e);
            System.err.println("✗ Error retrieving items from database: " + e.getMessage());
        }
    }

    public static void deleteItemFromDatabase(Scanner scanner) {
        logger.fine("Starting deleteItemFromDatabase method");

        ItemDAO dao = new ItemDAO();
        // Tracks the ID entered by the user.
        int itemId = 0;
        try {
            // Show all items so the user can pick one.
            for (Item item : dao.getAllItems()) {
                System.out.println("ID: " + item.getId() + ", Name: " + item.getName() + ", Quantity: "
                        + item.getQuantity() + ", Price: $" + item.getPrice());
            }
            logger.info("Successfully retrieved and displayed items from database for deletion");
            // Ask which item to delete.
            System.out.println("Select an item by its ID to delete:");
            itemId = Integer.parseInt(scanner.nextLine());
            // Delete the item.
            dao.deleteItem(itemId);
            logger.info("Item successfully deleted from database with ID: " + itemId);
            System.out.println("Item deleted successfully!");

            // Show the updated list.
            for (Item item : dao.getAllItems()) {
                System.out.println("ID: " + item.getId() + ", Name: " + item.getName() + ", Quantity: "
                        + item.getQuantity() + ", Price: $" + item.getPrice());
            }
            logger.info("Successfully retrieved and displayed items from database after deletion");
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Error deleting item from database with ID: " + itemId, e);
            System.err.println("✗ Error deleting item from database: " + e.getMessage());
        }
    }

    public static void searchItemInDatabase(Scanner scanner) {
        logger.fine("Starting searchItemInDatabase method");

        // Ask for the item name to search for.
        System.out.println("Enter item name to search:");
        String itemName = scanner.nextLine();
        logger.info("Searching for item: " + itemName);

        // Search for the item by name.
        ItemDAO dao = new ItemDAO();
        try {
            Item item = dao.getItemByName(itemName);
            // Print the item if found, or a not-found message if not.
            if (item != null) {
                logger.info("Item found: " + itemName + " (ID: " + item.getId() + ")");
                System.out.println("Item found: ID: " + item.getId() + ", Name: " + item.getName() + ", Quantity: "
                        + item.getQuantity() + ", Price: $" + item.getPrice());
                // If the item is null (not found), tell the user it wasn't found.
            } else {
                logger.warning("Item not found: " + itemName);
                System.out.println("Item not found with name: " + itemName);
            }
            logger.info("Completed search for item: " + itemName);
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Error searching for item: " + itemName, e);
            System.err.println("✗ Error searching for item in database: " + e.getMessage());
        }
    }

    public static void generateInventoryReport() {
        logger.fine("Starting generateInventoryReport method");

        // Call the report method in ItemDAO.
        ItemDAO dao = new ItemDAO();
        try {
            dao.generateReport();
            logger.info("Inventory report generated successfully");
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Error generating inventory report", e);
            System.err.println("✗ Error generating report: " + e.getMessage());
        }
    }
}

// Handles logging setup for the app.
class AppLogger {
    // Name of the log file.
    private static final String LOG_FILE = "inventory-app.log";
    // Ensures logging is only set up once.
    private static boolean initialized = false;

    // Sets up logging handlers and returns a logger for the given class.
    public static Logger setup(Class<?> clazz) {

        // Only run setup the first time.
        if (!initialized) {
            try {
                // Get the root logger and remove its default handlers.
                LogManager manager = LogManager.getLogManager();
                Logger root = manager.getLogger("");

                for (Handler h : root.getHandlers())
                    root.removeHandler(h);

                // Console handler: shows INFO and above.
                ConsoleHandler console = new ConsoleHandler();
                console.setLevel(Level.INFO);
                console.setFormatter(createFormatter());
                root.addHandler(console);

                // File handler: logs everything.
                FileHandler file = new FileHandler(LOG_FILE, true);
                file.setLevel(Level.ALL);
                file.setFormatter(createFormatter());
                root.addHandler(file);

                // Capture all log levels.
                root.setLevel(Level.ALL);
                initialized = true;
            } catch (IOException e) {
                System.err.println("Failed to set up logging: " + e.getMessage());
            }
        }
        return Logger.getLogger(clazz.getName());
    }

    // Returns a formatter that writes [LEVEL] [ClassName] message.
    private static Formatter createFormatter() {
        return new Formatter() {
            public String format(LogRecord r) {
                // Use only the simple class name, not the full package path.
                String className = r.getSourceClassName();
                if (className != null && className.contains(".")) {
                    className = className.substring(className.lastIndexOf('.') + 1);
                }
                // Build the formatted log line.
                String result = "[" + r.getLevel() + "] [" + className + "] " + formatMessage(r) + "\n";
                // Append exception details if present.
                if (r.getThrown() != null) {
                    result += "Exception: " + r.getThrown() + "\n";
                }
                return result;
            }
        };
    }
}
