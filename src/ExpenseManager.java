import javax.swing.*;
import java.io.FileNotFoundException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Stream;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.data.general.DefaultPieDataset;

public class ExpenseManager {

    private List<Expense> expenses;
    private final TreeMap<String, Double> categories;

    private LocalDate beginDate;
    private LocalDate endDate;

    private final FileReader fileReader;
    private String savedFilePath;
    private Double sumExpenses;
    private Double sumCatExpenses;
    private final DefaultPieDataset pieDataset;
    private final DefaultCategoryDataset categoryDataset;

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

        pieDataset = new DefaultPieDataset();
        categoryDataset = new DefaultCategoryDataset();
    }

    public List<Expense> GetExpenseList() {
        return expenses;
    }

    public int GetExpenseListSize() {
        return expenses.size();
    }

    public boolean IsEmpty() {
        return expenses.isEmpty();
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

    public void CalculateCategoryDataset() {
        categoryDataset.clear();
        int startMonth = beginDate.getMonthValue();
        int monthDifference = (int) ChronoUnit.MONTHS.between(beginDate.withDayOfMonth(1), endDate.withDayOfMonth(1));
        double[][] valuesMonths = new double[categories.size()][monthDifference];
        String[] months = new String[monthDifference];
        String[] categoryStrings = new String[categories.size()];

        for (int i = 0; i < monthDifference; i++) {
            months[i] = beginDate.plusMonths(i).format(DateTimeFormatter.ofPattern("MM.yyyy"));
            int finalI = i;
            int finalC = 0;
            for (String c : categories.keySet()) {
                int finalC1 = finalC;
                if (i == 0) {
                    categoryStrings[finalC1] = c;
                }
                valuesMonths[finalC1][finalI] = 0.0;
                expenses.stream()
                        .filter(e -> e.getDate().format(DateTimeFormatter.ofPattern("MM.yyyy")).equals(months[finalI]))
                        .filter(e -> e.getCategory().equals(c))
                        .forEach(e -> {
                            valuesMonths[finalC1][finalI] += e.getAmount();
                        });
                categoryDataset.setValue(valuesMonths[finalC1][finalI], categoryStrings[finalC1], months[finalI]);
                finalC++;
            }
        }
    }

    public void CalculatePieDataset() {
        pieDataset.clear();
        TreeMap<String, Double> dataMap = new TreeMap<String, Double>();
        categories.keySet().forEach(k -> dataMap.put(k, 0.0));
        expenses.stream()
                .filter(e -> !e.getCategory().equals(""))
                .forEach(e -> dataMap.replace(e.getCategory(), dataMap.get(e.getCategory())+e.getAmount()));
        dataMap.forEach(pieDataset::setValue);
    }

    public DefaultCategoryDataset GetCategoryDataset() {
        return categoryDataset;
    }
    public DefaultPieDataset GetPieDataset() {
        return pieDataset;
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
        if (!expenses.isEmpty()) {
            beginDate = expenses.stream()
                    .min(Expense::compareTo)
                    .get().getDate();
            endDate = expenses.stream()
                    .max(Expense::compareTo)
                    .get().getDate();
        }
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
