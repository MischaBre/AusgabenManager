import javax.swing.*;
import java.awt.*;

import java.util.List;

public class DetailFrame extends JFrame{
    private JPanel mainPanel;
    private List<Expense> expenses;

    public DetailFrame(String title, List<Expense> expenses) {
        super(title);

        this.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
        this.setContentPane(mainPanel);
        this.pack();

        this.expenses = expenses;
        
        Initialization();
    }

    private void Initialization() {



    }
}
