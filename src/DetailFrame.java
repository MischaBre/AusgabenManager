import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PiePlot;
import org.jfree.chart.plot.PlotOrientation;

import javax.swing.*;

import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.ItemEvent;
import java.time.LocalDate;

public class DetailFrame extends JFrame{
    private JPanel mainPanel;
    private JPanel piePanel;
    private JPanel stackedBarPanel;
    private JPanel linePanel;
    private JPanel controlPanel;
    private JComboBox<LocalDate> fromBox;
    private JComboBox<LocalDate> toBox;
    private JList<String> categoryList;
    private JLabel top5Label;
    private JCheckBox onlyPositive;

    private JFreeChart pieChart;
    private JFreeChart stackedBarChart;
    private JFreeChart lineChart;
    private ChartPanel pieChartPanel;
    private ChartPanel stackedBarChartPanel;
    private ChartPanel lineChartPanel;

    private final ExpenseManager expenseManager;
    private DefaultComboBoxModel<LocalDate> fromBoxModel;
    private DefaultComboBoxModel<LocalDate> toBoxModel;
    private DefaultListModel<String> categoryListModel;

    public DetailFrame(String title, ExpenseManager expenseManager) {
        super(title);

        this.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
        this.setContentPane(mainPanel);
        this.pack();
        this.setMinimumSize(new Dimension(1100,680));

        this.expenseManager = expenseManager;
        InitializationUI();

        this.addComponentListener(new ComponentAdapter() {
            public void componentHidden(ComponentEvent e) {
                /* code run when component hidden*/
            }
            public void componentShown(ComponentEvent e) {
                InitializationData();
            }
        });

        categoryList.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                InitializationData();
            }
        });

        fromBox.addItemListener(e -> {
            if (e.getStateChange() == ItemEvent.SELECTED) {
                if (fromBox.getSelectedIndex() > toBox.getSelectedIndex()) {
                    toBox.setSelectedIndex(fromBox.getSelectedIndex());
                } else {
                    InitializationData();
                }
            }
        });

        toBox.addItemListener(e -> {
            if (e.getStateChange() == ItemEvent.SELECTED) {
                if (toBox.getSelectedIndex() < fromBox.getSelectedIndex()) {
                    fromBox.setSelectedIndex(toBox.getSelectedIndex());
                } else {
                    InitializationData();
                }
            }
        });

        onlyPositive.addActionListener(e -> {
            InitializationData();
        });
    }

    private void InitializationData() {
            if (!expenseManager.IsEmpty() && fromBox.getSelectedItem() != null && toBox.getSelectedItem() != null) {
                expenseManager.CalculatePieDataset((LocalDate)fromBox.getSelectedItem(),
                        (LocalDate)toBox.getSelectedItem(),
                        categoryList.getSelectedValuesList(),
                        onlyPositive.isSelected());
                expenseManager.CalculateCategoryDataset((LocalDate)fromBox.getSelectedItem(),
                        (LocalDate)toBox.getSelectedItem(),
                        categoryList.getSelectedValuesList(),
                        onlyPositive.isSelected());
                DrawTop(expenseManager.GetTopExpenses(20,
                        (LocalDate)fromBox.getSelectedItem(),
                        (LocalDate)toBox.getSelectedItem(),
                        categoryList.getSelectedValuesList(),
                        onlyPositive.isSelected()));
            }

    }

    private void InitializationUI() {
        if (!expenseManager.IsEmpty()) {
            InitDateBoxes();
            InitCategoryList();
            CreatePieChart();
            CreateStackedBarChart();
            CreateLineChart();
        }
    }

    private void InitCategoryList() {
        categoryList.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        categoryListModel = new DefaultListModel<>();
        for (String c : expenseManager.GetCategories().keySet()) {
            categoryListModel.addElement(c);
        }
        categoryList.setModel(categoryListModel);
        int[] indices = new int[categoryListModel.getSize()];
        for (int i = 0; i < indices.length; i++) {
            indices[i] = i;
        }
        categoryList.setSelectedIndices(indices);
    }

    private void InitDateBoxes() {
        fromBoxModel = new DefaultComboBoxModel<>();
        toBoxModel = new DefaultComboBoxModel<>();
        fromBox.setModel(fromBoxModel);
        toBox.setModel(toBoxModel);

        fromBoxModel.addAll(expenseManager.GetMonths());
        toBoxModel.addAll(expenseManager.GetMonths());
        fromBox.setSelectedIndex(0);
        toBox.setSelectedIndex(toBoxModel.getSize()-1);
    }

    private void DrawTop(String[] input) {
        if (input != null && input.length > 0) {
            StringBuilder string = new StringBuilder();
            string.append("<html>");
            for (String s : input) {
                string.append(s);
                string.append("<br>");
            }
            string.append("</html>");
            top5Label.setText(string.toString());
        } else {
            top5Label.setText("");
        }
    }

    private void CreateStackedBarChart() {
        stackedBarPanel.setLayout(new java.awt.BorderLayout());
        stackedBarChart = ChartFactory.createStackedBarChart(
                "Nach Monaten",
                "Monate",
                "€",
                expenseManager.GetCategoryDataset(),
                PlotOrientation.VERTICAL,
                true,
                false,
                false);
        stackedBarChartPanel = new ChartPanel(stackedBarChart);
        stackedBarPanel.add(stackedBarChartPanel);
    }

    private void CreateLineChart() {
        linePanel.setLayout(new java.awt.BorderLayout());
        lineChart = ChartFactory.createLineChart(
                "Nach Monaten",
                "Monate",
                "€",
                expenseManager.GetCategoryDataset(),
                PlotOrientation.VERTICAL,
                true,
                false,
                false);
        lineChartPanel = new ChartPanel(lineChart);
        linePanel.add(lineChartPanel);
    }

    private void CreatePieChart() {
        piePanel.setLayout(new java.awt.BorderLayout());
        pieChart = ChartFactory.createPieChart(
                "Nach Kategorie",
                expenseManager.GetPieDataset(),
                false,
                false,
                false);
        PiePlot plot = (PiePlot) pieChart.getPlot();
        plot.setSectionPaint(0, new Color(0,0,0));
        pieChartPanel = new ChartPanel(pieChart);
        piePanel.add(pieChartPanel);
    }

}
