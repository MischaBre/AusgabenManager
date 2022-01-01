import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class FileReader {

    private TreeMap<String, Double> categories;
    private TreeSet<String> banksettings;

    public FileReader() {

        categories = new TreeMap<>();
        banksettings = new TreeSet<>();

    }

    public void LoadFromCfg(String fileName) {

        int type = 0;                                   //int type for switching between add to categories/types
        try {
            File file = new File(fileName);
            Scanner myScanner = new Scanner(file);
            myScanner.useDelimiter(";");
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
                            case 2 -> banksettings.add(line);
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

    public List<Expense> LoadExpensesFromCSV(String fileName, String keyword, String banksettings) {
        List<Expense> expenses = new ArrayList<>();
        try {
            File file = new File(fileName);
            Scanner myScanner = new Scanner(file, StandardCharsets.ISO_8859_1);
            myScanner.useDelimiter(";");
            switch (banksettings) {
                case "DKB Giro" -> myScanner.nextLine();
                case "DKB VISA" -> {
                    for (int i = 0; i < 7; i++) {
                        myScanner.nextLine();
                    }
                }
                default -> {
                    //nix
                }
            }
            String[] data;
            while (myScanner.hasNextLine()) {
                String line = myScanner.nextLine();
                data = line.replaceAll("\"", "").split(";");
                switch (banksettings) {
                    case "DKB Giro" -> {
                        if (data[2].equals(keyword)) {
                            expenses.add(new Expense(
                                    LocalDate.parse(data[1].trim(), DateTimeFormatter.ofPattern("dd.MM.yyyy")),
                                    data[3].trim(),
                                    data[5].trim(),
                                    data[4].trim().replace("/\s{2,}/", ""),
                                    -1 * Double.parseDouble(data[7].trim().replace(".", "").replace(',', '.'))
                            ));
                        }
                    }
                    case "DKB VISA" -> expenses.add(new Expense(
                            LocalDate.parse(data[2].trim(), DateTimeFormatter.ofPattern("dd.MM.yyyy")),
                            data[3].trim(),
                            "",
                            "",
                            -1 * Double.parseDouble(data[4].trim().replace(".", "").replace(',', '.'))
                        ));
                    default -> {
                        //nix
                    }
                }
            }
            myScanner.close();
        } catch (FileNotFoundException e) {
            System.out.println("Error: File not found.");
            e.printStackTrace();
        } catch (IOException e) {
            System.out.println("Error: IOException.");
            e.printStackTrace();
        }
        return expenses;
    }

    public TreeMap<String, Double> getCategories() {
        return categories;
    }

    public TreeSet<String> getBanks() {
        return banksettings;
    }
}
