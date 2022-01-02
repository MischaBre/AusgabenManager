import javax.swing.*;
import java.awt.*;
import java.awt.event.ItemEvent;
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
    private JComboBox<Banksetting> banksettingsBox;
    private JCheckBox uncategorizedCheckBox;
    private JTextField filterField;
    private JLabel sumLabel;
    private JLabel catLabel;
    private DefaultComboBoxModel<Banksetting> banksettingsJCBoxModel;
    private DefaultListModel<Expense> expenseJListModel;
    private DefaultComboBoxModel<String> categoriesJCBoxModel;

    private List<Expense> expenses;
    private Expense selectedExpense;
    private TreeMap<String, Double> categories;
    private FileReader fileReader;
    private Double sumExpenses;
    private Double sumCatExpenses;

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
            if (expenses.size() > 0) {
                ReloadJList(expenses);
                banksettingsBox.setEnabled(false);
                categoryBox.setEnabled(true);
                filterField.setEnabled(true);
                uncategorizedCheckBox.setEnabled(true);
                CalculateCategoryAmounts();
            }

        });

        clearButton.addActionListener(e -> {
            expenseJListModel.removeAllElements();
            expenses.clear();
            banksettingsBox.setEnabled(true);
            categoryBox.setEnabled(false);
            filterField.setEnabled(false);
            uncategorizedCheckBox.setEnabled(false);
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
                            expenses.stream().filter(exp -> exp.getConsignor().equals(selectedExpense.getConsignor())).forEach(exp -> exp.setCategory(selectedCategory));
                        }

                    } else {
                        selectedExpense.setCategory("");
                    }
                    ReloadJList(expenses);
                    CalculateCategoryAmounts();
                }
            }
        });

        uncategorizedCheckBox.addActionListener(e -> ReloadJList(expenses));

        filterField.getDocument().addDocumentListener((SimpleDocumentListener) e -> ReloadJList(expenses));
    }

    public static void main(String[] args) {

        JFrame frame = new MainFrame("Ausgabenmanager");
        frame.setMinimumSize(new Dimension(1150,528));
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
            expenseList = fileReader.LoadExpensesFromCSV(chooser.getSelectedFile().getName(), "Lastschrift", GetSelectedBanksettings());
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

    private Banksetting GetSelectedBanksettings() {
        return (Banksetting) banksettingsBox.getSelectedItem();
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

        sumExpenses = 0.0;
        expenses.forEach(e -> sumExpenses += e.getAmount());

        sumCatExpenses = 0.0;
        expenses.stream()
                .filter(e -> !e.getCategory().equals(""))
                .forEach(e -> sumCatExpenses += e.getAmount());

        sumLabel.setText(String.format("%,.02f €", sumExpenses));
        catLabel.setText(String.format("%,.02f €", sumCatExpenses));
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
            amountLabel.setText("0,00 €");
            detailTextArea.setText(" ");
            categoryBox.setSelectedIndex(0);
        }
    }

    private void ReloadJList(List<Expense> expenses) {
        expenseJListModel.clear();
        if (uncategorizedCheckBox.isSelected()) {
            expenses.stream()
                    .filter(e -> e.getConsignor().toLowerCase().contains(filterField.getText().toLowerCase()) && e.getCategory().equals(""))
                    .forEach(e -> expenseJListModel.addElement(e));
        } else {
            expenses.stream()
                    .filter(e -> e.getConsignor().toLowerCase().contains(filterField.getText().toLowerCase()))
                    .forEach(e -> expenseJListModel.addElement(e));
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


