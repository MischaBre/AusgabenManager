import jdk.jfr.Category;

import java.io.File;
import java.io.FileNotFoundException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class FileReader {



    public FileReader() {

    }

    public TreeMap<String, Double> LoadCategoriesFromCfg(String fileName) {
        TreeMap<String, Double> categories = new TreeMap<>();
        boolean catStart = false;
        try {
            File file = new File(fileName);
            Scanner myScanner = new Scanner(file);
            myScanner.useDelimiter(";");
            while (myScanner.hasNextLine()) {
                String line = myScanner.nextLine().trim();
                //do something with data
                if (catStart) {
                    categories.put(line,0.0);
                }
                if (line.equals("[categories]")) {
                    catStart = true;
                } else if(line.equals("")) {
                    catStart = false;
                }
            }
            myScanner.close();
        } catch (FileNotFoundException e) {
            System.out.println("Error");
            e.printStackTrace();
        }
        return categories;
    }

    public List<Expense> LoadExpensesFromCSVDKB(String fileName, String keyword) {
        List<Expense> expenses = new ArrayList<>();
        try {
            File file = new File(fileName);
            Scanner myScanner = new Scanner(file);
            myScanner.useDelimiter(";");
            myScanner.nextLine();
            String[] data;
            while (myScanner.hasNextLine()) {
                String line = myScanner.nextLine();
                byte[] bytes = line.getBytes(StandardCharsets.UTF_8);
                String utf8EncodedString = new String(bytes, StandardCharsets.UTF_8).replaceAll("\"", "");
                //do something with data
                data = utf8EncodedString.split(";");
                if (data[2].equals(keyword)) {
                    expenses.add(new Expense(
                            LocalDate.parse(data[1].trim(), DateTimeFormatter.ofPattern("dd.MM.yyyy")),
                            data[3].trim(),
                            data[5].trim(),
                            data[4].trim().replace("/\s{2,}/",""),
                            -1 * Double.parseDouble(data[7].trim().replace(".","").replace(',','.'))
                    ));
                }
            }
            myScanner.close();
        } catch (FileNotFoundException e) {
            System.out.println("Error");
            e.printStackTrace();
        }
        return expenses;
    }
}
