import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class FileReader {

    private final TreeMap<String, Double> categories;
    private final TreeSet<Banksetting> banksettings;

    public FileReader() {

        categories = new TreeMap<>();
        banksettings = new TreeSet<>();

    }

    public void LoadFromCfg(String fileName) {

        int type = 0;                                   //int type for switching between add to categories/types
        try {
            File file = new File(fileName);
            Scanner myScanner = new Scanner(file);
            while (myScanner.hasNextLine()) {
                String line = myScanner.nextLine().trim();
                //do something with data
                switch (line) {
                    case "[categories]":
                        type = 1;
                        break;

                    case "[banksettings]":
                        type = 2;
                        break;

                    case "":
                        type = 0;
                        break;

                    default:
                        switch (type) {
                            case 1 -> categories.put(line, 0.0);
                            case 2 -> banksettings.add(ParseBanksettings(line));
                            default -> {
                            }
                        }
                        break;
                }
            }
            myScanner.close();
        } catch (FileNotFoundException e) {
            System.out.println("Error");
            e.printStackTrace();
        }

    }

    private Banksetting ParseBanksettings(String input) {
        String[] i = input.split("=");
        return new Banksetting(i[0],i[1].split(","));
    }

    public List<Expense> ImportExpensesFromCSV(String fileName, Banksetting banksetting) {
        List<Expense> expenses = new ArrayList<>();
        int[] banksettingInputLines = banksetting.getInputLines();
        try {
            File file = new File(fileName);
            Scanner myScanner = new Scanner(file, banksetting.getCharSet());
            myScanner.useDelimiter(";");

            for (int i = 0; i < banksetting.getSkipLines(); i++) {
                myScanner.nextLine();
            }
            String[] data;
            while (myScanner.hasNextLine()) {
                String line = myScanner.nextLine();
                if (banksetting.isReplaceQuotes()) {
                    line = line.replaceAll("\"", "");
                }
                data = line.split(banksetting.getDelimiter());
                expenses.add(new Expense(
                        LocalDate.parse(data[banksettingInputLines[0]].trim(), DateTimeFormatter.ofPattern(banksetting.getDateFormat())),
                        banksettingInputLines[1] > 0 ? data[banksettingInputLines[1]].trim() : "",
                        banksettingInputLines[2] > 0 ? data[banksettingInputLines[2]].trim() : "",
                        banksettingInputLines[3] > 0 ? data[banksettingInputLines[3]].trim().replace("/\s{2,}/", "") : "",
                        -1 * Double.parseDouble(data[banksettingInputLines[4]].trim().replace(".", "").replace(',', '.')),
                        ""
                ));
            }
            myScanner.close();
        } catch (FileNotFoundException e) {
            System.out.println("Error: File not found.");
            e.printStackTrace();
        }
        return expenses;
    }

    public List<Expense> LoadExpensesFromFile(String fileName) {
        List<Expense> expenses = new ArrayList<>();
        try {
            File file = new File(fileName);
            Scanner myScanner = new Scanner(file, StandardCharsets.UTF_8);
            myScanner.useDelimiter(";");
            if (!myScanner.nextLine().equals("emf_file")) {
                throw new IOException("Falsches Dateiformat");
            }
            String[] data;
            while (myScanner.hasNextLine()) {
                String line = myScanner.nextLine();
                data = line.split(";");
                expenses.add(new Expense(
                        LocalDate.parse(data[0].trim(), DateTimeFormatter.ofPattern("dd.MM.yyyy")),
                        data[1].trim(),
                        data[2].trim(),
                        data[3].trim(),
                        Double.parseDouble(data[4].trim()),
                        data.length > 5 ? data[5].trim() : ""
                ));
            }
            myScanner.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return expenses;
    }

    public void SaveExpensesToEMF(String filename, List<Expense> expenses) throws FileNotFoundException {
        File file = new File(filename);
        try (PrintWriter pw = new PrintWriter(file)) {
            pw.println("emf_file");
            expenses.stream()
                    .map(e -> e.GetCSVExpenseString(";"))
                    .forEach(pw::println);
        }
    }

    public void SaveExpenseSummaryToTXT(String filename, String description, TreeMap<String, Double> categories) throws FileNotFoundException {
        File file = new File(filename);
        try (PrintWriter pw = new PrintWriter(file)) {
            pw.println("Ausgabenmanager - " + description);
            categories.forEach((k,v) -> pw.println(String.format("%s\t%,.02f",k, v)));
        }
    }

    public TreeMap<String, Double> getCategories() {
        return categories;
    }

    public TreeSet<Banksetting> getBanks() {
        return banksettings;
    }
}
