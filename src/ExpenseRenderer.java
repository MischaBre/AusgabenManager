import javax.swing.*;
import java.awt.*;

public class ExpenseRenderer extends JLabel implements ListCellRenderer<Expense> {

    private final JPanel panel;
    private final JLabel nameLabel;
    private final JLabel idLabel;

    public ExpenseRenderer() {
        panel = new JPanel(new GridLayout(0,5));
        nameLabel = new JLabel();
        idLabel = new JLabel();
        panel.add(idLabel);
        panel.add(nameLabel);
    }

    @Override
    public Component getListCellRendererComponent(JList<? extends Expense> list, Expense expense, int index,
                                                  boolean isSelected, boolean cellHasFocus) {
        idLabel.setText(expense.getConsignor());
        nameLabel.setText(expense.getCategory());
        if (isSelected) {
            idLabel.setBackground(Color.PINK);
        } else {
            idLabel.setBackground(Color.WHITE);
        }
        return panel;
    }

}
