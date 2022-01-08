import javax.swing.*;
import java.awt.*;
import java.awt.event.ItemEvent;
import java.io.FileNotFoundException;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;

public class MainFrame extends JFrame{
    private JPanel mainPanel;
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
    private JCheckBox onlyPositiveCheckBox;
    private JCheckBox onlyOneCBox;

    private final JMenuBar menuBar = new JMenuBar();
    private final JMenu menu = new JMenu("Datei");
    private final JMenuItem openFileMenu = new JMenuItem("Datei öffnen");
    private final JMenuItem saveFileMenu = new JMenuItem("Datei speichern");
    private final JMenuItem saveNewFileMenu = new JMenuItem("Datei speichern unter...");
    private final JMenuItem closeFileMenu = new JMenuItem("Datei schließen");
    private final JMenuItem importFileMenu = new JMenuItem("CSV importieren");
    private final JMenuItem exitMenu = new JMenuItem("Programm schließen");


    private DefaultComboBoxModel<Banksetting> banksettingsJCBoxModel;
    private DefaultListModel<Expense> expenseJListModel;
    private DefaultComboBoxModel<String> categoriesJCBoxModel;

    private List<Expense> expenses;
    private Expense selectedExpense;
    private TreeMap<String, Double> categories;
    private FileReader fileReader;
    private String savedFilePath;
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

        expenseJList.addListSelectionListener(e -> {
            //getValueIsAdjusting verhindert doppeltes Aufrufen des Listeners.
            // selectedExpense wird aktualisiert und Details angezeigt.
            if (!e.getValueIsAdjusting()) {
                selectedExpense = GetSelectedExpense();
                ShowExpenseDetails(selectedExpense);
            }
        });

        categoryBox.addItemListener(e -> {
            if (e.getStateChange() == ItemEvent.SELECTED && selectedExpense != null) {
                Expense currentExpense = selectedExpense;
                String selectedCategory = GetSelectedCategory();
                if (!selectedExpense.getCategory().equals(selectedCategory)) {
                    String setToCategory = categoryBox.getSelectedIndex() != 0 ? selectedCategory : "";
                    selectedExpense.setCategory(setToCategory);
                    if (!onlyOneCBox.isSelected()) {
                        int setAllExpenses = JOptionPane.showConfirmDialog(null, "Sollen die Ausgaben mit dem gleichen Absender der gleichen Kategorie hinzugefügt werden?");
                        if (setAllExpenses == JOptionPane.YES_OPTION) {
                            expenses.stream().filter(exp -> exp.getConsignor().equals(selectedExpense.getConsignor())).forEach(exp -> exp.setCategory(setToCategory));
                        }
                    }
                    ReloadJList(expenses);
                    selectedExpense = currentExpense;
                    expenseJList.setSelectedIndex(expenseJListModel.indexOf(selectedExpense));
                    CalculateCategoryAmounts();
                }
            }
        });

        uncategorizedCheckBox.addActionListener(e -> {
            //Sollen nur unkategorisierte Ausgaben angezeigt werden, wird die Liste
            //entsprechend aktualisiert. Die Abfrage der CheckBox ist in ReloadJList()
            ReloadJList(expenses);
        });

        onlyPositiveCheckBox.addActionListener(e -> {
            //sollen nur positive Ausgaben berücksichtigt werden, wird die Liste
            //entsprechend aktualisiert und die Auswertung neuberechnet. Die Abfrage der CheckBox
            //ist in CalculateCategoryAmounts()
            ReloadJList(expenses);
            CalculateCategoryAmounts();
        });

        filterField.getDocument().addDocumentListener((SimpleDocumentListener) e -> {
            //Die Liste wird entsprechend des Filters in filterField gefiltert (passiert alles in ReloadJList())
            ReloadJList(expenses);
        });

        openFileMenu.addActionListener(e -> {
            expenses = OpenFile(expenses, false);
            UISettingsAfterOpen();
        });

        saveFileMenu.addActionListener(e -> {
            SaveFile(expenses);
        });

        saveNewFileMenu.addActionListener(e -> {
            SaveNewFile(expenses);
        });

        closeFileMenu.addActionListener(e -> {
            expenseJListModel.removeAllElements();
            expenses.clear();
            UISettingsAfterClose();
        });

        importFileMenu.addActionListener(e -> {
            expenses = OpenFile(expenses, true);
            UISettingsAfterOpen();
        });

        exitMenu.addActionListener(e -> {
            System.exit(0);
        });

    }

    public static void main(String[] args) {

        if (System.getProperty("os.name").toLowerCase().contains("mac")) {
            System.setProperty("apple.laf.useScreenMenuBar", "true");
        }

        JFrame frame = new MainFrame("Ausgabenmanager");
        frame.setMinimumSize(new Dimension(1150,528));



        frame.setVisible(true);


        System.out.println("Program start");

    }

    private JMenuBar AddMenuBar() {
        menuBar.add(menu);

        menu.add(openFileMenu);
        menu.add(saveFileMenu);
        menu.add(saveNewFileMenu);
        menu.add(closeFileMenu);
        menu.addSeparator();

        menu.add(importFileMenu);
        menu.addSeparator();

        menu.add(exitMenu);

        return menuBar;
    }

    private void Initialization() {
                                                                //ExpenseList and JList initialization
        expenses = new ArrayList<>();
        expenseJListModel = new DefaultListModel<>();
        expenseJList.setModel(expenseJListModel);

                                                                //FileReader initialization
        fileReader = new FileReader();
        fileReader.LoadFromCfg("settings.ini");
        savedFilePath = "";

        banksettingsJCBoxModel = new DefaultComboBoxModel<>();
        banksettingsBox.setModel(banksettingsJCBoxModel);
        banksettingsJCBoxModel.addAll(fileReader.getBanks());
        banksettingsBox.setSelectedIndex(0);

                                                                //Load categories
        categories = fileReader.getCategories();
        categoriesJCBoxModel = new DefaultComboBoxModel<>();
        categoryBox.setModel(categoriesJCBoxModel);
        categoriesJCBoxModel.addElement("");
        categoriesJCBoxModel.addAll(categories.keySet());
        categoryBox.setEnabled(false);

                                                                //Zeige Kategorienauswertung
        ShowCategoryAmounts();

                                                                //sonstiges
        this.setJMenuBar(AddMenuBar());
        saveFileMenu.setEnabled(false);
        saveNewFileMenu.setEnabled(false);
        closeFileMenu.setEnabled(false);
        dateLabel.setText(" ");
        consignorLabel.setText(" ");
    }
                                                                //Open File

    private List<Expense> OpenFile(List<Expense> oldExpenseList, boolean isImport) {
        List<Expense> expenseList = new ArrayList<>();

        if (oldExpenseList != null) {
            expenseList = oldExpenseList;
        }
        JFileChooser chooser = new JFileChooser();
        int choice = chooser.showOpenDialog(null);

        if (choice == JFileChooser.APPROVE_OPTION) {
            if (isImport) {
                expenseList = fileReader.ImportExpensesFromCSV(chooser.getSelectedFile().getAbsolutePath(), GetSelectedBanksettings());
            } else {
                expenseList = fileReader.LoadExpensesFromFile(chooser.getSelectedFile().getAbsolutePath());
            }

            if (expenseList.size() > 0) {
                savedFilePath = chooser.getSelectedFile().getAbsolutePath();
            }
        }

        return expenseList;
    }

    private void SaveFile(List<Expense> expenses) {
        try {
            fileReader.SaveExpensesToEMF(savedFilePath, expenses);
        } catch (FileNotFoundException ex) {
            System.out.println("Error");
            ex.printStackTrace();
        }
    }

    private void SaveNewFile(List<Expense> expenses) {
        JFileChooser chooser = new JFileChooser();
        int choice = chooser.showSaveDialog(null);

        if (choice == JFileChooser.APPROVE_OPTION) {
            try {
                fileReader.SaveExpensesToEMF(chooser.getSelectedFile().getAbsolutePath(), expenses);
                savedFilePath = chooser.getSelectedFile().getAbsolutePath();
                this.setTitle("Ausgabenmanager - " + savedFilePath);
            } catch (FileNotFoundException ex) {
                System.out.println("Error");
                ex.printStackTrace();
            }
        }
    }

    private void UISettingsAfterOpen() {
        if (expenses.size() > 0) {
            this.setTitle("Ausgabenmanager - " + savedFilePath);
            if (savedFilePath.endsWith("emf")) {
                saveFileMenu.setEnabled(true);
            }
            saveNewFileMenu.setEnabled(true);
            closeFileMenu.setEnabled(true);
            ReloadJList(expenses);
            banksettingsBox.setEnabled(false);
            filterField.setEnabled(true);
            uncategorizedCheckBox.setEnabled(true);
            onlyPositiveCheckBox.setEnabled(true);
            onlyOneCBox.setEnabled(true);
            CalculateCategoryAmounts();
        }
    }

    private void UISettingsAfterClose() {
        this.setTitle("Ausgabenmanager");
        saveFileMenu.setEnabled(false);
        saveNewFileMenu.setEnabled(false);
        closeFileMenu.setEnabled(false);
        banksettingsBox.setEnabled(true);
        categoryBox.setEnabled(false);
        filterField.setEnabled(false);
        uncategorizedCheckBox.setEnabled(false);
        onlyPositiveCheckBox.setEnabled(false);
        onlyOneCBox.setEnabled(true);
        CalculateCategoryAmounts();
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
            if (!e.getCategory().equals("") && (!onlyPositiveCheckBox.isSelected() || e.getAmount() > 0.0)) {
                categories.replace(e.getCategory(),categories.get(e.getCategory())+e.getAmount());
            }
        }

        sumExpenses = 0.0;
        expenses.stream()
                .filter(e -> !onlyPositiveCheckBox.isSelected() || e.getAmount() > 0.0)
                .forEach(e -> sumExpenses += e.getAmount());

        sumCatExpenses = 0.0;
        expenses.stream()
                .filter(e -> !onlyPositiveCheckBox.isSelected() || e.getAmount() > 0.0)
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
            categoryBox.setEnabled(true);
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
            categoryBox.setEnabled(false);
            categoryBox.setSelectedIndex(0);
        }
    }

    private void ReloadJList(List<Expense> expenses) {
        expenseJListModel.clear();
        expenses.stream()
                .sorted(Expense::compareTo)
                .filter(e -> e.getAmount() > 0.0 || !onlyPositiveCheckBox.isSelected())
                .filter(e -> e.getCategory().equals("") || !uncategorizedCheckBox.isSelected())
                .filter(e -> e.getConsignor().toLowerCase().contains(filterField.getText().toLowerCase()))
                .forEach(e -> expenseJListModel.addElement(e));
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


