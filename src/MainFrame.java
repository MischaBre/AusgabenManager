import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileNotFoundException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

public class MainFrame extends JFrame{
    private JPanel mainPanel;
    private JButton ladenButton;
    private JButton unsortiertButton;
    private JButton sortiertButton;
    private JButton löschenButton;
    private JList expenseJList;
    private DefaultListModel expenseJListModel;
    private List<Expense> expenses;

    public MainFrame(String title) {
                                                                //JFrame-stuff
        super(title);

        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setContentPane(mainPanel);
        this.pack();

                                                                //ExpenseList and JList initialization
        expenses = new ArrayList<>();
        expenseJListModel = new DefaultListModel<Expense>();
        expenseJList.setModel(expenseJListModel);

        ladenButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                expenses = LoadExpensesFromCSVDKB("1059958338.csv", "Lastschrift");
            }
        });

        unsortiertButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                ReloadJList(expenses);
            }
        });
        sortiertButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                ReloadJList(SortExpenseListByDate(expenses));
            }
        });
        löschenButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                expenseJListModel.clear();
                expenses.clear();
            }
        });
    }

    public static void main(String[] args) {

        JFrame frame = new MainFrame("Ausgabenmanager");
        frame.setVisible(true);

        System.out.println("Program start");

    }

                                                                //Expense-related functions

    private void ReloadJList(List<Expense> expenses) {
        expenseJListModel.clear();
        for (Expense e : expenses) {
            expenseJListModel.addElement(e.GetNiceExpenseString());
        }
    }

    private List<Expense> SortExpenseListByDate(List<Expense> expenses) {
        return expenses.stream()
                .sorted(Expense::compareTo)
                .collect(Collectors.toList());
    }

    private void PrintExpenseList(List<Expense> expenses) {
        expenses.forEach(v -> v.PrintExpense());
    }

    private String ExpenseListToString(List<Expense> expenses) {
        StringBuilder string = new StringBuilder();
        expenses.forEach(v -> string.append(v.GetNiceExpenseString() + System.lineSeparator()));
        return string.toString();
    }

    private static List<Expense> LoadExpensesFromCSVDKB(String fileName, String keyword) {
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
                if (data[2].equals(keyword)) {
                    expenses.add(new Expense(
                            LocalDate.parse(data[1].trim(), DateTimeFormatter.ofPattern("dd.MM.yyyy")),
                            data[3].trim(),
                            data[5].trim(),
                            data[4].trim().replace("/\s{2,}/",""),
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


