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
    private TreeMap<String, Double> categories;
    private final String cfgFile;

    private LocalDate beginDate;
    private LocalDate endDate;

    private final FileReader fileReader;
    private String savedFilePath;
    private Double sumExpenses;
    private Double sumCatExpenses;
    private final DefaultPieDataset pieDataset;
    private final DefaultCategoryDataset categoryDataset;

    public ExpenseManager(String filename) {
        System.out.println("EM " + Thread.currentThread().getName());
        //ExpenseList and JList initialization
        expenses = new ArrayList<>();
        categories = new TreeMap<>();

        //FileReader initialization
        fileReader = new FileReader();
        cfgFile = filename;
        categories = fileReader.LoadFromCfg(cfgFile);
        savedFilePath = "";

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

    public TreeMap<String, Double> GetCategories() {
        return categories;
    }

    public Set<String> GetCategoryStringSet() {
        return categories.keySet();
    }

    public TreeSet<Banksetting> GetBanksettingSet() {
        return fileReader.getBanks();
    }

    public String GetSavedFilePath() {
        return savedFilePath;
    }

    public Double GetSumExpenses() {
        return sumExpenses;
    }

    public Double GetSumCatExpenses() {
        return sumCatExpenses;
    }

    public LocalDate GetBeginDate() { return beginDate; }

    public LocalDate GetEndDate() { return endDate; }

    public int GetMonthDifference() {
        return (int) ChronoUnit.MONTHS.between(beginDate.withDayOfMonth(1), endDate.withDayOfMonth(1)) + 1;
    }

    public TreeSet<LocalDate> GetMonths() {
        TreeSet<LocalDate> months = new TreeSet<>();
        int monthDifference = GetMonthDifference();
        for (int i = 0; i < monthDifference; i++) {
            months.add(beginDate.withDayOfMonth(1).plusMonths(i));
        }
        return months;
    }

    public void ClearExpenses() {
        expenses.clear();
        beginDate = LocalDate.of(1900,1,1);
        endDate = LocalDate.of(1900,1,2);
        sumExpenses = 0.0;
        sumCatExpenses = 0.0;
        categories.replaceAll((k,v) -> v = 0.0);
        pieDataset.clear();
        categoryDataset.clear();
        savedFilePath = "";
    }

    public boolean isNewCategory(String input) {
        return !categories.containsKey(input);
    }

    public void AddCategory(String input) {
        categories.put(input, 0.0);
    }

    public void DeleteCategory(String input) {
        categories.remove(input);
    }

    public String[] GetTopExpenses(int limit, LocalDate beginDate, LocalDate endDate, List<String> categories, boolean onlyPositive) {
        String[] topFive;
        Comparator<Expense> compareByAmount = Comparator.comparing(Expense::getAmount).reversed();
        topFive = expenses.stream()
                .filter(e -> categories.contains(e.getCategory()))
                .filter(e -> e.getDate().isAfter(beginDate.minusDays(1)) && endDate.plusMonths(1).isAfter(e.getDate()))
                .filter(e -> e.getAmount() > 0.0 || !onlyPositive)
                .sorted(compareByAmount)
                .limit(limit)
                .map(Expense::GetShortString)
                .toArray(String[]::new);
        return topFive;
    }

    public void CalculateCategoryDataset(LocalDate beginDate, LocalDate endDate, List<String> categories, boolean onlyPositive) {
        System.out.println("CCD " + Thread.currentThread().getName());
        categoryDataset.clear();
        int startMonth = beginDate.getMonthValue();
        int monthDifference = Math.min(GetMonthDifference(), (int) ChronoUnit.MONTHS.between(beginDate.withDayOfMonth(1), endDate.withDayOfMonth(1)) + 1);
        double[][] valuesMonths = new double[categories.size()][monthDifference];
        String[] months = new String[monthDifference];
        String[] categoryStrings = new String[categories.size()];
        for (int i = 0; i < monthDifference; i++) {
            months[i] = beginDate.plusMonths(i).format(DateTimeFormatter.ofPattern("MM.yyyy"));
            int finalI = i;
            int finalC = 0;
            for (String c : categories) {
                int finalC1 = finalC;
                if (i == 0) {
                    categoryStrings[finalC1] = c;
                }
                valuesMonths[finalC1][finalI] = 0.0;
                expenses.stream()
                        .filter(e -> e.getDate().format(DateTimeFormatter.ofPattern("MM.yyyy")).equals(months[finalI]))
                        .filter(e -> e.getCategory().equals(c))
                        .filter(e -> e.getAmount() > 0.0 || !onlyPositive)
                        .forEach(e -> {
                            valuesMonths[finalC1][finalI] += e.getAmount();
                        });
                categoryDataset.setValue(valuesMonths[finalC1][finalI], categoryStrings[finalC1], months[finalI]);
                finalC++;
            }
        }
    }

    public void CalculatePieDataset(LocalDate beginDate, LocalDate endDate, List<String> categories, boolean onlyPositive) {
        pieDataset.clear();
        TreeMap<String, Double> dataMap = new TreeMap<String, Double>();
        categories.forEach(k -> dataMap.put(k, 0.0));
        expenses.stream()
                .filter(e -> !e.getCategory().equals(""))
                .filter(e -> e.getDate().isAfter(beginDate.minusDays(1)) && endDate.plusMonths(1).isAfter(e.getDate()))
                .filter(e -> e.getAmount() > 0.0 || !onlyPositive)
                .forEach(e -> {
                    if (dataMap.containsKey(e.getCategory())) {
                        dataMap.replace(e.getCategory(), dataMap.get(e.getCategory())+e.getAmount());
                    }
                });
        dataMap.forEach((k,v) -> {
            if (v > 0.0) {
                pieDataset.setValue(k, v);
            }
        });
    }

    public DefaultCategoryDataset GetCategoryDataset() {
        return categoryDataset;
    }
    public DefaultPieDataset GetPieDataset() {
        return pieDataset;
    }

    public void CalculateCategoryAmounts(boolean onlyPositive) {
        System.out.println("CCA " + Thread.currentThread().getName());
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
        System.out.println("GFE " + Thread.currentThread().getName());
        return expenses.stream()
                .sorted(Expense::compareTo)
                .filter(e -> e.getAmount() > 0.0 || !onlyPositive)
                .filter(e -> e.getCategory().equals("") || !onlyUncategorized)
                .filter(e -> e.getConsignor().toLowerCase().contains(filterField.toLowerCase()) || e.getCategory().toLowerCase().contains((filterField.toLowerCase())));
    }

    public boolean OpenFile(boolean isImport, Banksetting selectedBanksetting) {
        List<Expense> expenseList;
        JFileChooser chooser = new JFileChooser();
        int choice = chooser.showOpenDialog(null);
        if (choice == JFileChooser.APPROVE_OPTION) {
            if (isImport) {
                expenseList = fileReader.ImportExpensesFromCSV(chooser.getSelectedFile().getAbsolutePath(), selectedBanksetting);
            } else {
                expenseList = fileReader.LoadExpensesFromFileIO(chooser.getSelectedFile().getAbsolutePath());
            }
            if (expenseList.size() > 0) {
                savedFilePath = chooser.getSelectedFile().getAbsolutePath();
                expenses = expenseList;
                beginDate = expenses.stream()
                        .min(Expense::compareTo)
                        .get().getDate();
                endDate = expenses.stream()
                        .max(Expense::compareTo)
                        .get().getDate();
                return true;
            }
        }
        return false;
    }

    public void SaveFile() {
        try {
            fileReader.SaveExpensesToEMFIO(savedFilePath, expenses);
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
                fileReader.SaveExpensesToEMFIO(chooser.getSelectedFile().getAbsolutePath(), expenses);
                savedFilePath = chooser.getSelectedFile().getAbsolutePath();

            } catch (FileNotFoundException ex) {
                System.out.println("Error");
                ex.printStackTrace();
            }
        }
    }

    public void SaveCfgFile() {
        JFileChooser chooser = new JFileChooser();
        int choice = chooser.showSaveDialog(null);

        if (choice == JFileChooser.APPROVE_OPTION) {
            fileReader.SaveCfg(chooser.getSelectedFile().getAbsolutePath(), categories);
            savedFilePath = chooser.getSelectedFile().getAbsolutePath();

        }
    }
}
