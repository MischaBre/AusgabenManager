import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileNotFoundException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class MainFrame extends JFrame{
    private JPanel mainPanel;
    private JButton button1;
    private JButton button2;
    private JButton button3;
    private JButton button4;
    private JList expenseJList;
    private static HashMap<String, Expense> expenses;

    public MainFrame(String title) {
        super(title);

        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setContentPane(mainPanel);
        this.pack();
        expenses = new HashMap<>();
    }

    public static void main(String[] args) {

        JFrame frame = new MainFrame("Ausgabenmanager");
        frame.setVisible(true);

        System.out.println("Program start");

        List<Expense> list = loadExpensesFromCSV("1059958338.csv");
        for (Expense e : list) {
            expenses.put(e.getHash(),e);
        }
        expenses.entrySet().stream()
                .sorted(Map.Entry.comparingByValue(Expense::compareTo))
                .forEach(e->e.getValue().PrintExpense());
    }

    private static List<Expense> loadExpensesFromCSV(String fileName) {
        List<Expense> expenses = new ArrayList<>();
        try {
            File file = new File(fileName);
            String line = new String();
            String[] data = new String[11];
            Scanner myScanner = new Scanner(file);
            myScanner.useDelimiter(";");
            myScanner.nextLine();
            while (myScanner.hasNextLine()) {
                line = myScanner.nextLine();
                byte[] bytes = line.getBytes(StandardCharsets.UTF_8);
                String utf8EncodedString = new String(bytes, StandardCharsets.UTF_8).replaceAll("\"", "");
                //do something with data
                data = utf8EncodedString.split(";");
                if (data[2].equals("Lastschrift")) {
                    expenses.add(new Expense(
                            LocalDate.parse(data[1].trim(), DateTimeFormatter.ofPattern("dd.MM.yyyy")),
                            "Michi",
                            123456789,
                            data[3].trim(),
                            data[5].trim(),
                            -1 * Double.parseDouble(data[7].trim().replace(".","").replace(',','.'))
                    ));
                };
            }
            myScanner.close();
        } catch (FileNotFoundException e) {
            System.out.println("Error");
            e.printStackTrace();
        }
        return expenses;
    }

}


