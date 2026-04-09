package com.hkhan.app;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.sql.SQLException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.UIManager;
import javax.swing.table.DefaultTableModel;

import com.hkhan.app.dao.ItemDAO;
import com.hkhan.app.model.Item;

/**
 * This class builds the main inventory window.
 * It talks to the same database layer used by the console app.
 */
public class InventoryGUI extends JFrame {
    // Logger helps record useful messages while the app is running.
    private static final Logger logger = AppLogger.setup(InventoryGUI.class);

    // These text fields hold user input from the form.
    private final JTextField nameField = new JTextField(18);
    private final JTextField quantityField = new JTextField(10);
    private final JTextField priceField = new JTextField(10);
    private final JTextField searchField = new JTextField(16);

    // This label shows which item is currently selected in the table.
    private final JLabel selectedIdLabel = new JLabel("Selected Item ID: none");

    // Table model stores rows shown in the on-screen table.
    // The 4 column names match the item properties.
    private final DefaultTableModel tableModel = new DefaultTableModel(
            new Object[] { "ID", "Name", "Quantity", "Price" }, 0) {
        @Override
        public boolean isCellEditable(int row, int column) {
            // Return false so users cannot type directly into table cells.
            return false;
        }
    };

    // JTable shows data from tableModel.
    private final JTable itemTable = new JTable(tableModel);

    // Constructor runs when a new window is created.
    public InventoryGUI() {
        // Set the text shown in the top bar of the window.
        setTitle("Inventory Management System");
        // Close the app when this window is closed.
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        // Use BorderLayout to place top/middle/bottom sections.
        setLayout(new BorderLayout(10, 10));

        // Build and add the top form section.
        add(buildFormPanel(), BorderLayout.NORTH);
        // Build and add the center table section.
        add(buildTablePanel(), BorderLayout.CENTER);
        // Build and add the bottom button section.
        add(buildActionPanel(), BorderLayout.SOUTH);

        // Prevent window from getting too small to use.
        setMinimumSize(new Dimension(900, 520));
        // Open window in the center of the screen.
        setLocationRelativeTo(null);

        // Load all current items as soon as the GUI opens.
        refreshTable();
    }

    // Builds the top panel with text fields and labels.
    private JPanel buildFormPanel() {
        // Create a panel that supports a grid-style layout.
        JPanel panel = new JPanel(new GridBagLayout());
        // Add a visible border title.
        panel.setBorder(BorderFactory.createTitledBorder("Item Details"));

        // Constraint object controls where each field appears.
        GridBagConstraints gbc = new GridBagConstraints();
        // Add spacing around each component.
        gbc.insets = new Insets(6, 6, 6, 6);
        // Keep content aligned to the left.
        gbc.anchor = GridBagConstraints.WEST;

        // First row: item name and search fields.
        gbc.gridx = 0;
        gbc.gridy = 0;
        panel.add(new JLabel("Name:"), gbc);

        gbc.gridx = 1;
        panel.add(nameField, gbc);

        gbc.gridx = 2;
        panel.add(new JLabel("Search by Name:"), gbc);

        gbc.gridx = 3;
        panel.add(searchField, gbc);

        // Second row: quantity and price fields.
        gbc.gridx = 0;
        gbc.gridy = 1;
        panel.add(new JLabel("Quantity:"), gbc);

        gbc.gridx = 1;
        panel.add(quantityField, gbc);

        gbc.gridx = 2;
        panel.add(new JLabel("Price:"), gbc);

        gbc.gridx = 3;
        panel.add(priceField, gbc);

        // Third row: selected item status label.
        gbc.gridx = 0;
        gbc.gridy = 2;
        // Let this label span all 4 columns.
        gbc.gridwidth = 4;
        // Keep status label aligned left.
        selectedIdLabel.setHorizontalAlignment(SwingConstants.LEFT);
        panel.add(selectedIdLabel, gbc);

        // Return completed top panel.
        return panel;
    }

    // Builds the middle panel with the data table.
    private JPanel buildTablePanel() {
        // Create panel using BorderLayout.
        JPanel panel = new JPanel(new BorderLayout());
        // Add visible title around the table section.
        panel.setBorder(BorderFactory.createTitledBorder("Inventory Items"));

        // Allow user to select only one row at a time.
        itemTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        // Listen for row selection changes.
        itemTable.getSelectionModel().addListSelectionListener(event -> {
            // Run only once after selection settles.
            if (!event.getValueIsAdjusting()) {
                // Copy selected row values into the form fields.
                populateFieldsFromSelectedRow();
            }
        });

        // Add table inside a scroll pane so long lists can scroll.
        panel.add(new JScrollPane(itemTable), BorderLayout.CENTER);
        // Return completed table panel.
        return panel;
    }

    // Builds the bottom panel with all action buttons.
    private JPanel buildActionPanel() {
        // FlowLayout places buttons in a row.
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.CENTER, 8, 8));

        // Create buttons for every user action.
        JButton addButton = new JButton("Add Item");
        JButton updateButton = new JButton("Update Item");
        JButton deleteButton = new JButton("Delete Item");
        JButton searchButton = new JButton("Search");
        JButton clearSearchButton = new JButton("Show All");
        JButton reportButton = new JButton("Generate Report");
        JButton clearFormButton = new JButton("Clear Form");

        // Connect each button to its method.
        addButton.addActionListener(e -> addItem());
        updateButton.addActionListener(e -> updateItem());
        deleteButton.addActionListener(e -> deleteItem());
        searchButton.addActionListener(e -> searchItem());
        clearSearchButton.addActionListener(e -> refreshTable());
        reportButton.addActionListener(e -> generateReport());
        clearFormButton.addActionListener(e -> clearForm());

        // Add buttons to the panel in display order.
        panel.add(addButton);
        panel.add(updateButton);
        panel.add(deleteButton);
        panel.add(searchButton);
        panel.add(clearSearchButton);
        panel.add(reportButton);
        panel.add(clearFormButton);

        // Return completed action panel.
        return panel;
    }

    // Loads every item from the database and displays them in the table.
    private void refreshTable() {
        try {
            // Create DAO object to talk to the database.
            ItemDAO dao = new ItemDAO();
            // Get all rows from the items table.
            List<Item> items = dao.getAllItems();
            // Replace table rows with latest database rows.
            updateTable(items);
            // Clear selected label text after refresh.
            clearSelectionDetails();
            // Write info message to log file/console.
            logger.info("Loaded " + items.size() + " items into GUI table");
        } catch (SQLException e) {
            // Log detailed database error.
            logger.log(Level.SEVERE, "Error loading items for GUI", e);
            // Show simple error to user.
            showError("Could not load items from the database.\n" + e.getMessage());
        } catch (IllegalStateException e) {
            // Log environment/config errors.
            logger.log(Level.SEVERE, "Configuration error while loading GUI", e);
            // Show missing config message to user.
            showError(e.getMessage());
        }
    }

    // Adds a new item using values typed in the form.
    private void addItem() {
        try {
            // Build item object from form values.
            Item item = buildItemFromForm(0);
            // Create DAO for database insert.
            ItemDAO dao = new ItemDAO();
            // Insert item into database.
            dao.insertItem(item);
            // Reload table so new row appears.
            refreshTable();
            // Clear fields after successful insert.
            clearForm();
            // Show success popup with new ID.
            showInfo("Item added successfully. New ID: " + item.getId());
        } catch (NumberFormatException e) {
            // Show message when quantity or price is not numeric.
            showError("Please enter numbers only for Quantity and Price.");
        } catch (IllegalArgumentException e) {
            // Show validation message from buildItemFromForm.
            showError(e.getMessage());
        } catch (SQLException e) {
            // Log and show database insert error.
            logger.log(Level.SEVERE, "Error adding item", e);
            showError("Could not add item.\n" + e.getMessage());
        } catch (IllegalStateException e) {
            // Log and show missing environment configuration.
            logger.log(Level.SEVERE, "Configuration error while adding item", e);
            showError(e.getMessage());
        }
    }

    // Updates selected item using current form values.
    private void updateItem() {
        // Read selected ID from table.
        int selectedId = getSelectedItemId();
        // If no row is selected, stop here.
        if (selectedId < 0) {
            showError("Please select an item in the table before updating.");
            return;
        }

        try {
            // Build item object using selected ID + edited values.
            Item updatedItem = buildItemFromForm(selectedId);
            // Create DAO for database update.
            ItemDAO dao = new ItemDAO();
            // Save updated values in database.
            dao.updateItem(updatedItem);
            // Reload table after update.
            refreshTable();
            // Tell user update worked.
            showInfo("Item updated successfully.");
        } catch (NumberFormatException e) {
            // Show message when quantity or price is not numeric.
            showError("Please enter numbers only for Quantity and Price.");
        } catch (IllegalArgumentException e) {
            // Show validation message from buildItemFromForm.
            showError(e.getMessage());
        } catch (SQLException e) {
            // Log and show database update error.
            logger.log(Level.SEVERE, "Error updating item", e);
            showError("Could not update item.\n" + e.getMessage());
        } catch (IllegalStateException e) {
            // Log and show missing environment configuration.
            logger.log(Level.SEVERE, "Configuration error while updating item", e);
            showError(e.getMessage());
        }
    }

    // Deletes the currently selected item.
    private void deleteItem() {
        // Read selected ID from table.
        int selectedId = getSelectedItemId();
        // If no row is selected, stop here.
        if (selectedId < 0) {
            showError("Please select an item in the table before deleting.");
            return;
        }

        // Ask user to confirm deletion.
        int answer = JOptionPane.showConfirmDialog(
                this,
                "Delete item ID " + selectedId + "?",
                "Confirm Delete",
                JOptionPane.YES_NO_OPTION);

        // If user does not click Yes, cancel delete.
        if (answer != JOptionPane.YES_OPTION) {
            return;
        }

        try {
            // Create DAO for database delete.
            ItemDAO dao = new ItemDAO();
            // Delete selected row in database.
            dao.deleteItem(selectedId);
            // Reload table so deleted row disappears.
            refreshTable();
            // Clear form fields and selection.
            clearForm();
            // Show success popup.
            showInfo("Item deleted successfully.");
        } catch (SQLException e) {
            // Log and show database delete error.
            logger.log(Level.SEVERE, "Error deleting item", e);
            showError("Could not delete item.\n" + e.getMessage());
        } catch (IllegalStateException e) {
            // Log and show missing environment configuration.
            logger.log(Level.SEVERE, "Configuration error while deleting item", e);
            showError(e.getMessage());
        }
    }

    // Searches for one item by name and shows only that result in the table.
    private void searchItem() {
        // Read text from search box and remove extra spaces.
        String nameToSearch = searchField.getText().trim();
        // If search box is empty, show message and stop.
        if (nameToSearch.isEmpty()) {
            showError("Type a name in 'Search by Name' first.");
            return;
        }

        try {
            // Create DAO for database search.
            ItemDAO dao = new ItemDAO();
            // Find item by name.
            Item foundItem = dao.getItemByName(nameToSearch);
            // Clear old rows from table before showing result.
            tableModel.setRowCount(0);

            // If nothing was found, inform user and stop.
            if (foundItem == null) {
                showInfo("No item found with name: " + nameToSearch);
                clearSelectionDetails();
                return;
            }

            // Add the found item as one table row.
            tableModel.addRow(new Object[] {
                    foundItem.getId(),
                    foundItem.getName(),
                    foundItem.getQuantity(),
                    foundItem.getPrice()
            });
            // Auto-select the found row.
            itemTable.setRowSelectionInterval(0, 0);
            // Show success message.
            showInfo("Found 1 item named: " + nameToSearch);
        } catch (SQLException e) {
            // Log and show database search error.
            logger.log(Level.SEVERE, "Error searching item", e);
            showError("Could not search for item.\n" + e.getMessage());
        } catch (IllegalStateException e) {
            // Log and show missing environment configuration.
            logger.log(Level.SEVERE, "Configuration error while searching item", e);
            showError(e.getMessage());
        }
    }

    // Generates inventory report data using DAO method.
    private void generateReport() {
        try {
            // Create DAO object.
            ItemDAO dao = new ItemDAO();
            // Generate report in console output (same as old structure).
            dao.generateReport();
            // Let user know report was created.
            showInfo("Report generated. Check the console output for full details.");
        } catch (SQLException e) {
            // Log and show database report error.
            logger.log(Level.SEVERE, "Error generating report", e);
            showError("Could not generate report.\n" + e.getMessage());
        } catch (IllegalStateException e) {
            // Log and show missing environment configuration.
            logger.log(Level.SEVERE, "Configuration error while generating report", e);
            showError(e.getMessage());
        }
    }

    // Replaces all table rows with rows from the provided list.
    private void updateTable(List<Item> items) {
        // Remove all existing rows first.
        tableModel.setRowCount(0);
        // Add one new row for each item in the list.
        for (Item item : items) {
            tableModel.addRow(new Object[] {
                    item.getId(),
                    item.getName(),
                    item.getQuantity(),
                    item.getPrice()
            });
        }
    }

    // Reads form fields, validates values, and builds an Item object.
    private Item buildItemFromForm(int id) {
        // Read and trim name field.
        String name = nameField.getText().trim();
        // Name is required.
        if (name.isEmpty()) {
            throw new IllegalArgumentException("Name is required.");
        }

        // Parse number fields from text input.
        int quantity = Integer.parseInt(quantityField.getText().trim());
        double price = Double.parseDouble(priceField.getText().trim());

        // Do not allow negative quantity.
        if (quantity < 0) {
            throw new IllegalArgumentException("Quantity cannot be negative.");
        }
        // Do not allow negative price.
        if (price < 0) {
            throw new IllegalArgumentException("Price cannot be negative.");
        }

        // Return new item object with provided ID and form values.
        return new Item(id, name, quantity, price);
    }

    // Gets selected table row ID, or -1 when no row is selected.
    private int getSelectedItemId() {
        // Read selected row index from JTable.
        int selectedRow = itemTable.getSelectedRow();
        // If no row is selected, return -1.
        if (selectedRow < 0) {
            return -1;
        }
        // Return the ID value from first column.
        return (int) tableModel.getValueAt(selectedRow, 0);
    }

    // Copies selected table row values into form fields.
    private void populateFieldsFromSelectedRow() {
        // Read selected row index.
        int selectedRow = itemTable.getSelectedRow();
        // If no row is selected, clear label and stop.
        if (selectedRow < 0) {
            clearSelectionDetails();
            return;
        }

        // Read values from selected row columns.
        int id = (int) tableModel.getValueAt(selectedRow, 0);
        String name = tableModel.getValueAt(selectedRow, 1).toString();
        String quantity = tableModel.getValueAt(selectedRow, 2).toString();
        String price = tableModel.getValueAt(selectedRow, 3).toString();

        // Put values into text fields.
        nameField.setText(name);
        quantityField.setText(quantity);
        priceField.setText(price);
        // Update selected ID label.
        selectedIdLabel.setText("Selected Item ID: " + id);
    }

    // Clears all form inputs and table selection.
    private void clearForm() {
        // Clear text fields.
        nameField.setText("");
        quantityField.setText("");
        priceField.setText("");
        searchField.setText("");
        // Remove selected row in table.
        itemTable.clearSelection();
        // Reset selected ID label.
        clearSelectionDetails();
    }

    // Resets selected ID label to default text.
    private void clearSelectionDetails() {
        selectedIdLabel.setText("Selected Item ID: none");
    }

    // Shows an informational popup message.
    private void showInfo(String message) {
        JOptionPane.showMessageDialog(this, message, "Inventory", JOptionPane.INFORMATION_MESSAGE);
    }

    // Shows an error popup message.
    private void showError(String message) {
        JOptionPane.showMessageDialog(this, message, "Inventory Error", JOptionPane.ERROR_MESSAGE);
    }

    // Starts this GUI with system look-and-feel.
    public static void launch() {
        try {
            // Try to use the same look as native Windows controls.
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {
            // If this fails, Swing default look still works.
        }

        // Start GUI creation on Swing event thread.
        java.awt.EventQueue.invokeLater(() -> {
            try {
                // Create and show the inventory window.
                new InventoryGUI().setVisible(true);
            } catch (Exception ex) {
                // Show startup error instead of silent close.
                JOptionPane.showMessageDialog(
                        null,
                        "The window could not be opened.\n" + ex.getMessage(),
                        "Inventory Error",
                        JOptionPane.ERROR_MESSAGE);
            }
        });
    }
}
