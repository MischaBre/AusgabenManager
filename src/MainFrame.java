import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;

public class MainFrame extends JFrame{
    private JPanel mainPanel;
    private JButton ladenButton;
    private JButton clearButton;
    private JList<Expense> expenseJList;
    private JTextArea detailTextArea;
    private JComboBox<String> categoryBox;
    private JLabel amountLabel;
    private JLabel consignorLabel;
    private JLabel dateLabel;
    private JLabel catInfoLabel;
    private JLabel catAmountLabel;
    private JComboBox<String> banksettingsBox;
    private DefaultComboBoxModel<String> banksettingsJCBoxModel;
    private DefaultListModel<Expense> expenseJListModel;
    private DefaultComboBoxModel<String> categoriesJCBoxModel;
    private List<Expense> expenses;
    private Expense selectedExpense;
    private TreeMap<String, Double> categories;
    private FileReader fileReader;

    public MainFrame(String title) {
                                                                //JFrame-stuff
        super(title);

        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setContentPane(mainPanel);
        this.pack();

                                                                //StartUp and Initialization of program
        Initialization();

                                                                //ActionListeners
        ladenButton.addActionListener(e -> {

            expenses = OpenFile(expenses);

            ReloadJList(expenses);
            banksettingsBox.setEnabled(false);
            CalculateCategoryAmounts();

        });
        clearButton.addActionListener(e -> {

            expenseJListModel.removeAllElements();
            expenses.clear();
            banksettingsBox.setEnabled(true);
            CalculateCategoryAmounts();

        });
        expenseJList.addListSelectionListener(e -> {

            if (!e.getValueIsAdjusting()) {
                selectedExpense = GetSelectedExpense();
                ShowExpenseDetails(selectedExpense);
            }

        });
        categoryBox.addItemListener(e -> {

            if (e.getStateChange() == ItemEvent.SELECTED && selectedExpense != null) {
                String selectedCategory = GetSelectedCategory();
                if (!selectedExpense.getCategory().equals(selectedCategory)) {
                    if (categoryBox.getSelectedIndex() != 0) {
                        selectedExpense.setCategory(selectedCategory);
                        int setAllExpenses = JOptionPane.showConfirmDialog(null, "Sollen die Ausgaben mit dem gleichen Absender der gleichen Kategorie hinzugefügt werden?");
                        if (setAllExpenses == JOptionPane.YES_OPTION) {
                            expenses.stream().filter(exp -> exp.getConsignorNumber().equals(selectedExpense.getConsignorNumber())).forEach(exp -> exp.setCategory(selectedCategory));
                        }

                    } else {
                        selectedExpense.setCategory("");
                    }
                    CalculateCategoryAmounts();
                }
            }

        });
    }

    public static void main(String[] args) {

        JFrame frame = new MainFrame("Ausgabenmanager");
        frame.setMinimumSize(new Dimension(1050,528));
        frame.setVisible(true);

        System.out.println("Program start");

    }

    private void Initialization() {
                                                                //ExpenseList and JList initialization
        expenses = new ArrayList<>();
        expenseJListModel = new DefaultListModel<>();
        expenseJList.setModel(expenseJListModel);

                                                                //FileReader initialization
        fileReader = new FileReader();
        fileReader.LoadFromCfg("settings.cfg");

        banksettingsJCBoxModel = new DefaultComboBoxModel<>();
        banksettingsBox.setModel(banksettingsJCBoxModel);
        banksettingsJCBoxModel.addAll(fileReader.getBanks());
        banksettingsBox.setSelectedIndex(0);

                                                                //Load categories
        categories = fileReader.getCategories();
        categoriesJCBoxModel = new DefaultComboBoxModel<>();
        categoryBox.setModel(categoriesJCBoxModel);
        categoriesJCBoxModel.addElement("<keine>");
        categoriesJCBoxModel.addAll(categories.keySet());

                                                                //Zeige Kategorienauswertung
        ShowCategoryAmounts();
    }
                                                                //Open File

    private List<Expense> OpenFile(List<Expense> oldExpenseList) {
        List<Expense> expenseList = new ArrayList<>();

        if (oldExpenseList != null) {
            expenseList = oldExpenseList;
        }
        JFileChooser chooser = new JFileChooser();
        int choice = chooser.showOpenDialog(null);

        if (choice == JFileChooser.APPROVE_OPTION) {
            expenseList = fileReader.LoadExpensesFromCSVDKB(chooser.getSelectedFile().getName(), "Lastschrift");
        }
        return expenseList;
    }

                                                                //List operations

    private Expense GetSelectedExpense() {
        if (!expenseJList.isSelectionEmpty()) {
            return expenseJList.getSelectedValue();
        } else {
            return null;
        }
    }

    private String GetSelectedCategory() {
        return (String) categoryBox.getSelectedItem();
    }

    private void CalculateCategoryAmounts() {
        categories.replaceAll((k,v) -> v = 0.0);

        for (Expense e : expenses) {
            if (!e.getCategory().equals("")) {
                categories.replace(e.getCategory(),categories.get(e.getCategory())+e.getAmount());
            }
        }
        catAmountLabel.setText(TreeMapToString(categories, 1));
    }

    private void ShowCategoryAmounts() {
        catInfoLabel.setText(TreeMapToString(categories, 0));
        catAmountLabel.setText(TreeMapToString(categories, 1));
    }
                                                                //Expense-related functions

    private void ShowExpenseDetails(Expense expense) {
        if (expense != null) {
            System.out.println("Showing " + expense);
            dateLabel.setText(expense.getDate().format(DateTimeFormatter.ofPattern("dd.MM.yyyy")));
            consignorLabel.setText(expense.getConsignor());
            amountLabel.setText(String.format("%,.2f €", expense.getAmount()));
            detailTextArea.setText(expense.getDetail());
            if (!expense.getCategory().equals("")) {
                categoryBox.setSelectedItem(expense.getCategory());
            } else {
                categoryBox.setSelectedIndex(0);
            }
        } else {
            dateLabel.setText(" ");
            consignorLabel.setText(" ");
            amountLabel.setText(" €");
            detailTextArea.setText(" ");
            categoryBox.setSelectedIndex(0);
        }
    }

    private void ReloadJList(List<Expense> expenses) {
        expenseJListModel.clear();
        for (Expense e : expenses) {
            expenseJListModel.addElement(e);
        }
    }

    private List<Expense> SortExpenseListByDate(List<Expense> expenses) {
        return expenses.stream()
                .sorted(Expense::compareTo)
                .collect(Collectors.toList());
    }

    private void PrintExpenseList(List<Expense> expenses) {
        expenses.forEach(Expense::PrintExpense);
    }

    private String TreeMapToString(TreeMap<String, Double> data, int value) {
        StringBuilder string = new StringBuilder();
        String alignment = (value == 0 ? "left" : "right");
        string.append("<html>");
        data.forEach((k,v) -> string.append("<p align=\"").append(alignment).append("\">").append(value == 0 ? k : String.format("%,.2f €", v)).append("</p>"));
        string.append("</html>");
        return string.toString();
    }
}


