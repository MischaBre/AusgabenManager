import javax.swing.*;
import java.awt.*;
import java.awt.event.ItemEvent;
import java.time.format.DateTimeFormatter;
import java.util.*;

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
    private JButton detailFrameButton;

    private DetailFrame detailFrame;

    private final JMenuBar menuBar = new JMenuBar();
    private final JMenu menu = new JMenu("Datei");
    private final JMenuItem openFileMenu = new JMenuItem("Datei öffnen");
    private final JMenuItem saveFileMenu = new JMenuItem("Datei speichern");
    private final JMenuItem saveNewFileMenu = new JMenuItem("Datei speichern unter...");
    private final JMenuItem closeFileMenu = new JMenuItem("Datei schließen");
    private final JMenuItem importFileMenu = new JMenuItem("CSV importieren");
    private final JMenuItem exitMenu = new JMenuItem("Programm schließen");

    private final ExpenseManager expenseManager;
    private Expense selectedExpense;

    private DefaultComboBoxModel<Banksetting> banksettingsJCBoxModel;
    private DefaultListModel<Expense> expenseJListModel;
    private DefaultComboBoxModel<String> categoriesJCBoxModel;

    public MainFrame(String title) {
                                                                //JFrame-stuff
        super(title);

        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setContentPane(mainPanel);
        this.pack();

                                                                //StartUp and Initialization of program
        expenseManager = new ExpenseManager();
        InitializationUI();

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
                            expenseManager.GetExpenseList().stream()
                                    .filter(exp -> exp.getConsignor().equals(selectedExpense.getConsignor()))
                                    .forEach(exp -> exp.setCategory(setToCategory));
                        }
                    }
                    ReloadJList();
                    selectedExpense = currentExpense;
                    expenseJList.setSelectedIndex(expenseJListModel.indexOf(selectedExpense));
                    expenseManager.CalculateCategoryAmounts(onlyPositiveCheckBox.isSelected());
                    ShowCategoryAmounts();
                }
            }
        });

        uncategorizedCheckBox.addActionListener(e -> {
            //Sollen nur unkategorisierte Ausgaben angezeigt werden, wird die Liste
            //entsprechend aktualisiert. Die Abfrage der CheckBox ist in ReloadJList()
            ReloadJList();
        });

        onlyPositiveCheckBox.addActionListener(e -> {
            //sollen nur positive Ausgaben berücksichtigt werden, wird die Liste
            //entsprechend aktualisiert und die Auswertung neuberechnet. Die Abfrage der CheckBox
            //ist in CalculateCategoryAmounts()
            ReloadJList();
            expenseManager.CalculateCategoryAmounts(onlyPositiveCheckBox.isSelected());
            ShowCategoryAmounts();
        });

        filterField.getDocument().addDocumentListener((SimpleDocumentListener) e -> {
            //Die Liste wird entsprechend des Filters in filterField gefiltert (passiert alles in ReloadJList())
            ReloadJList();
        });

        openFileMenu.addActionListener(e -> {
            expenseManager.OpenFile(false, GetSelectedBanksetting());
            UISettingsAfterOpen();
        });

        saveFileMenu.addActionListener(e -> {
            expenseManager.SaveFile();
        });

        saveNewFileMenu.addActionListener(e -> {
            expenseManager.SaveNewFile();
            this.setTitle("Ausgabenmanager - " + expenseManager.getSavedFilePath());
        });

        closeFileMenu.addActionListener(e -> {
            expenseJListModel.removeAllElements();
            expenseManager.ClearExpenses();
            UISettingsAfterClose();
        });

        importFileMenu.addActionListener(e -> {
            expenseManager.OpenFile(true, GetSelectedBanksetting());
            UISettingsAfterOpen();
        });

        exitMenu.addActionListener(e -> {
            System.exit(0);
        });

        detailFrameButton.addActionListener(e -> {
            if (detailFrame == null) {
                detailFrame = new DetailFrame("Ausgaben-Analyse", expenseManager.GetExpenseList(), expenseManager.getCategories());
                detailFrame.setMinimumSize(new Dimension(900,550));
            }
            detailFrame.setVisible(true);
        });
    }

    public static void main(String[] args) {

        if (System.getProperty("os.name").toLowerCase().contains("mac")) {
            System.setProperty("apple.laf.useScreenMenuBar", "true");
        }

        JFrame frame = new MainFrame("Ausgabenmanager");
        frame.setMinimumSize(new Dimension(1200,580));
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

    private void InitializationUI() {

        expenseJListModel = new DefaultListModel<>();
        banksettingsJCBoxModel = new DefaultComboBoxModel<>();
        categoriesJCBoxModel = new DefaultComboBoxModel<>();

        categoryBox.setModel(categoriesJCBoxModel);
        categoriesJCBoxModel.addElement("");
        categoriesJCBoxModel.addAll(expenseManager.GetCategoryStringSet());
        categoryBox.setEnabled(false);

        banksettingsBox.setModel(banksettingsJCBoxModel);
        banksettingsJCBoxModel.addAll(expenseManager.GetBanksettingSet());
        banksettingsBox.setSelectedIndex(0);
        expenseJList.setModel(expenseJListModel);

                                                                //Zeige Kategorienauswertung
        ShowCategoryAmounts();

                                                                //Label initialisieren
        dateLabel.setText(" ");
        consignorLabel.setText(" ");

                                                                //sonstiges
        this.setJMenuBar(AddMenuBar());
        saveFileMenu.setEnabled(false);
        saveNewFileMenu.setEnabled(false);
        closeFileMenu.setEnabled(false);
    }

    private void UISettingsAfterOpen() {
        if (expenseManager.GetExpenseListSize() > 0) {
            this.setTitle("Ausgabenmanager - " + expenseManager.getSavedFilePath());
            if (expenseManager.getSavedFilePath().endsWith("emf")) {
                saveFileMenu.setEnabled(true);
            }
            saveNewFileMenu.setEnabled(true);
            closeFileMenu.setEnabled(true);
            ReloadJList();
            banksettingsBox.setEnabled(false);
            filterField.setEnabled(true);
            uncategorizedCheckBox.setEnabled(true);
            onlyPositiveCheckBox.setEnabled(true);
            onlyOneCBox.setEnabled(true);
            filterField.setText("");
            detailFrame = null;

            expenseManager.CalculateCategoryAmounts(onlyPositiveCheckBox.isSelected());
            ShowCategoryAmounts();
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
        filterField.setText("");
        detailFrame = null;

        expenseManager.CalculateCategoryAmounts(onlyPositiveCheckBox.isSelected());
        ShowCategoryAmounts();
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

    private void ShowCategoryAmounts() {
        sumLabel.setText(String.format("%,.02f €", expenseManager.GetSumExpenses()));
        catLabel.setText(String.format("%,.02f €", expenseManager.GetSumCatExpenses()));
        catInfoLabel.setText(TreeMapToString(expenseManager.getCategories(), false));
        catAmountLabel.setText(TreeMapToString(expenseManager.getCategories(), true));
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

    private void ReloadJList() {
        expenseJListModel.clear();
        expenseManager.GetFilteredExpenses(
                onlyPositiveCheckBox.isSelected(),
                uncategorizedCheckBox.isSelected(),
                filterField.getText())
                .forEach(e -> expenseJListModel.addElement(e));
    }

    private Banksetting GetSelectedBanksetting() {
        return (Banksetting) banksettingsBox.getSelectedItem();
    }

    private String TreeMapToString(TreeMap<String, Double> data, boolean isValue) {
        StringBuilder string = new StringBuilder();
        String alignment = (isValue ? "right" : "left");
        string.append("<html>");
        data.forEach((k,v) -> string.append("<p align=\"").append(alignment).append("\">").append(isValue ? String.format("%,.2f €", v) : k).append("</p>"));
        string.append("</html>");
        return string.toString();
    }
}


