import javax.swing.*;
import java.io.FileNotFoundException;
import java.util.*;
import java.util.stream.Stream;

public class ExpenseManager {

    private List<Expense> expenses;
    private final TreeMap<String, Double> categories;
    private final FileReader fileReader;
    private String savedFilePath;
    private Double sumExpenses;
    private Double sumCatExpenses;

    public ExpenseManager() {
        //ExpenseList and JList initialization
        expenses = new ArrayList<>();

        //FileReader initialization
        fileReader = new FileReader();
        fileReader.LoadFromCfg("settings.ini");
        savedFilePath = "";
        categories = fileReader.getCategories();

        sumExpenses = 0.0;
        sumCatExpenses = 0.0;
    }

    public List<Expense> GetExpenseList() {
        return expenses;
    }

    public int GetExpenseListSize() {
        return expenses.size();
    }

    public TreeMap<String, Double> getCategories() {
        return categories;
    }

    public Set<String> GetCategoryStringSet() {
        return categories.keySet();
    }

    public TreeSet<Banksetting> GetBanksettingSet() {
        return fileReader.getBanks();
    }

    public String getSavedFilePath() {
        return savedFilePath;
    }

    public Double GetSumExpenses() {
        return sumExpenses;
    }

    public Double GetSumCatExpenses() {
        return sumCatExpenses;
    }

    public void ClearExpenses() {
        expenses.clear();
    }

    public void CalculateCategoryAmounts(boolean onlyPositive) {
        categories.replaceAll((k,v) -> v = 0.0);

        sumExpenses = 0.0;
        sumCatExpenses = 0.0;
        for (Expense e : expenses) {
            if ((!onlyPositive || e.getAmount() > 0.0)) {
                sumExpenses += e.getAmount();
                if (!e.getCategory().equals("")) {
                    categories.replace(e.getCategory(),categories.get(e.getCategory())+e.getAmount());
                    sumCatExpenses += e.getAmount();
                }
            }
        }
    }

    public Stream<Expense> GetFilteredExpenses(boolean onlyPositive, boolean onlyUncategorized, String filterField) {
        return expenses.stream()
                .sorted(Expense::compareTo)
                .filter(e -> e.getAmount() > 0.0 || !onlyPositive)
                .filter(e -> e.getCategory().equals("") || !onlyUncategorized)
                .filter(e -> e.getConsignor().toLowerCase().contains(filterField.toLowerCase()) || e.getCategory().toLowerCase().contains((filterField.toLowerCase())));
    }

    public void OpenFile(boolean isImport, Banksetting selectedBanksetting) {
        List<Expense> expenseList = new ArrayList<>();

        if (expenses != null) {
            expenseList = expenses;
        }
        JFileChooser chooser = new JFileChooser();
        int choice = chooser.showOpenDialog(null);

        if (choice == JFileChooser.APPROVE_OPTION) {
            if (isImport) {
                expenseList = fileReader.ImportExpensesFromCSV(chooser.getSelectedFile().getAbsolutePath(), selectedBanksetting);
            } else {
                expenseList = fileReader.LoadExpensesFromFile(chooser.getSelectedFile().getAbsolutePath());
            }

            if (expenseList.size() > 0) {
                savedFilePath = chooser.getSelectedFile().getAbsolutePath();
            }
        }

        expenses = expenseList;
    }

    public void SaveFile() {
        try {
            fileReader.SaveExpensesToEMF(savedFilePath, expenses);
        } catch (FileNotFoundException ex) {
            System.out.println("Error");
            ex.printStackTrace();
        }
    }

    public void SaveNewFile() {
        JFileChooser chooser = new JFileChooser();
        int choice = chooser.showSaveDialog(null);

        if (choice == JFileChooser.APPROVE_OPTION) {
            try {
                fileReader.SaveExpensesToEMF(chooser.getSelectedFile().getAbsolutePath(), expenses);
                savedFilePath = chooser.getSelectedFile().getAbsolutePath();

            } catch (FileNotFoundException ex) {
                System.out.println("Error");
                ex.printStackTrace();
            }
        }
    }
}
