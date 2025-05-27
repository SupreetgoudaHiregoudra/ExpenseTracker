import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class ExpenseTracker extends JFrame {

    private JTextField amountField, dateField, descriptionField;
    private JComboBox<String> categoryComboBox;
    private JLabel feedbackLabel;
    private JTable expenseTable;
    private DefaultTableModel tableModel;

    private List<Expense> expenses;
    private static final String FILE_NAME = "expenses.csv";

    public ExpenseTracker() {
        expenses = new ArrayList<>();
        loadExpenses();

        setTitle("Expense Tracker");
        setSize(750, 500);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(EXIT_ON_CLOSE);

        initComponents();
    }

    private void initComponents() {
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Input form panel on the left
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBorder(BorderFactory.createTitledBorder("Add New Expense"));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 6, 6, 6);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Amount
        gbc.gridx = 0; gbc.gridy = 0;
        formPanel.add(new JLabel("Amount (₹):"), gbc);
        amountField = new JTextField();
        gbc.gridx = 1; gbc.gridy = 0;
        formPanel.add(amountField, gbc);

        // Category (dropdown)
        gbc.gridx = 0; gbc.gridy = 1;
        formPanel.add(new JLabel("Category:"), gbc);
        String[] categories = {"Food", "Transport", "Bills", "Shopping", "Entertainment", "Other"};
        categoryComboBox = new JComboBox<>(categories);
        gbc.gridx = 1; gbc.gridy = 1;
        formPanel.add(categoryComboBox, gbc);

        // Date
        gbc.gridx = 0; gbc.gridy = 2;
        formPanel.add(new JLabel("Date (YYYY-MM-DD):"), gbc);
        dateField = new JTextField();
        gbc.gridx = 1; gbc.gridy = 2;
        formPanel.add(dateField, gbc);

        // Description
        gbc.gridx = 0; gbc.gridy = 3;
        formPanel.add(new JLabel("Description:"), gbc);
        descriptionField = new JTextField();
        gbc.gridx = 1; gbc.gridy = 3;
        formPanel.add(descriptionField, gbc);

        // Buttons panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 0));
        JButton addButton = new JButton("Add Expense");
        JButton deleteButton = new JButton("Delete Selected");
        JButton clearButton = new JButton("Clear Form");
        JButton reportButton = new JButton("Monthly Report");
        buttonPanel.add(addButton);
        buttonPanel.add(deleteButton);
        buttonPanel.add(clearButton);
        buttonPanel.add(reportButton);

        gbc.gridx = 0; gbc.gridy = 4; gbc.gridwidth = 2;
        formPanel.add(buttonPanel, gbc);

        // Feedback label
        feedbackLabel = new JLabel(" ");
        feedbackLabel.setForeground(new Color(0, 128, 0)); // green
        gbc.gridy = 5;
        formPanel.add(feedbackLabel, gbc);

        // Table panel on the right
        String[] columnNames = {"Amount (₹)", "Category", "Date", "Description"};
        tableModel = new DefaultTableModel(columnNames, 0) {
            public boolean isCellEditable(int row, int column) { return false; }
        };
        expenseTable = new JTable(tableModel);
        expenseTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        JScrollPane scrollPane = new JScrollPane(expenseTable);

        // Fill table with existing expenses
        refreshTable();

        mainPanel.add(formPanel, BorderLayout.WEST);
        mainPanel.add(scrollPane, BorderLayout.CENTER);

        add(mainPanel);

        // Button actions
        addButton.addActionListener(e -> addExpense());
        deleteButton.addActionListener(e -> deleteSelectedExpense());
        clearButton.addActionListener(e -> clearForm());
        reportButton.addActionListener(e -> showMonthlyReport());
    }

    private void addExpense() {
        try {
            String amountText = amountField.getText().trim();
            if (amountText.isEmpty()) {
                showFeedback("Please enter amount.", Color.RED);
                return;
            }
            double amount = Double.parseDouble(amountText);
            if (amount <= 0) {
                showFeedback("Amount must be positive.", Color.RED);
                return;
            }

            String category = (String) categoryComboBox.getSelectedItem();
            String date = dateField.getText().trim();
            if (!date.matches("\\d{4}-\\d{2}-\\d{2}")) {
                showFeedback("Date must be in YYYY-MM-DD format.", Color.RED);
                return;
            }
            String description = descriptionField.getText().trim();

            Expense expense = new Expense(amount, category, date, description);
            expenses.add(expense);
            saveExpenses();
            refreshTable();

            showFeedback("Expense added successfully!", new Color(0, 128, 0));
            clearForm();

        } catch (NumberFormatException e) {
            showFeedback("Invalid amount entered.", Color.RED);
        } catch (Exception e) {
            showFeedback("Error adding expense: " + e.getMessage(), Color.RED);
        }
    }

    private void deleteSelectedExpense() {
        int selectedRow = expenseTable.getSelectedRow();
        if (selectedRow == -1) {
            showFeedback("No expense selected to delete.", Color.RED);
            return;
        }
        expenses.remove(selectedRow);
        saveExpenses();
        refreshTable();
        showFeedback("Expense deleted successfully.", new Color(0, 128, 0));
    }

    private void clearForm() {
        amountField.setText("");
        categoryComboBox.setSelectedIndex(0);
        dateField.setText("");
        descriptionField.setText("");
        feedbackLabel.setText(" ");
    }

    private void showMonthlyReport() {
        if (expenses.isEmpty()) {
            showFeedback("No expenses to show report.", Color.RED);
            return;
        }

        // Aggregate expenses by month (YYYY-MM)
        java.util.Map<String, Double> monthlyTotals = new java.util.HashMap<>();

        for (Expense e : expenses) {
            String month = "";
            if (e.getDate().length() >= 7) {
                month = e.getDate().substring(0, 7); // YYYY-MM
            }
            monthlyTotals.put(month, monthlyTotals.getOrDefault(month, 0.0) + e.getAmount());
        }

        // Build report string
        StringBuilder report = new StringBuilder("Monthly Expense Report:\n\n");
        for (String month : monthlyTotals.keySet()) {
            report.append(month).append(" : ₹").append(String.format("%.2f", monthlyTotals.get(month))).append("\n");
        }

        JOptionPane.showMessageDialog(this, report.toString(), "Monthly Report", JOptionPane.INFORMATION_MESSAGE);
    }

    private void refreshTable() {
        tableModel.setRowCount(0); // clear table
        for (Expense e : expenses) {
            Object[] row = {e.getAmount(), e.getCategory(), e.getDate(), e.getDescription()};
            tableModel.addRow(row);
        }
    }

    private void loadExpenses() {
        File file = new File(FILE_NAME);
        if (!file.exists()) return;

        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            expenses.clear();
            while ((line = br.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts.length >= 4) {
                    double amount = Double.parseDouble(parts[0]);
                    String category = parts[1];
                    String date = parts[2];
                    String description = parts[3];
                    expenses.add(new Expense(amount, category, date, description));
                }
            }
        } catch (IOException | NumberFormatException e) {
            // If error loading, just start fresh
            expenses.clear();
        }
    }

    private void saveExpenses() {
        try (PrintWriter pw = new PrintWriter(new FileWriter(FILE_NAME))) {
            for (Expense e : expenses) {
                String line = String.format("%.2f,%s,%s,%s",
                        e.getAmount(), e.getCategory(), e.getDate(), e.getDescription());
                pw.println(line);
            }
        } catch (IOException e) {
            showFeedback("Error saving expenses: " + e.getMessage(), Color.RED);
        }
    }

    private void showFeedback(String message, Color color) {
        feedbackLabel.setForeground(color);
        feedbackLabel.setText(message);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            ExpenseTracker tracker = new ExpenseTracker();
            tracker.setVisible(true);
        });
    }
}
