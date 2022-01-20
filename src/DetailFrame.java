import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PiePlot;
import org.jfree.chart.plot.PlotOrientation;

import javax.swing.*;

import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;

public class DetailFrame extends JFrame{
    private JPanel mainPanel;
    private JPanel piePanel;
    private JPanel stackedBarPanel;
    private JPanel linePanel;
    private JPanel controlPanel;

    private JFreeChart pieChart;
    private JFreeChart stackedBarChart;
    private JFreeChart lineChart;
    private ChartPanel pieChartPanel;
    private ChartPanel stackedBarChartPanel;
    private ChartPanel lineChartPanel;

    private final ExpenseManager expenseManager;

    public DetailFrame(String title, ExpenseManager expenseManager) {
        super(title);

        this.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
        this.setContentPane(mainPanel);
        this.pack();
        this.setMinimumSize(new Dimension(1100,680));

        this.expenseManager = expenseManager;

        if (!expenseManager.IsEmpty()) {
            InitializationData();
            InitializationUI();
        }

        this.addComponentListener(new ComponentAdapter() {
            public void componentHidden(ComponentEvent e) {
                /* code run when component hidden*/
            }
            public void componentShown(ComponentEvent e) {
                if (!expenseManager.IsEmpty()) {
                    expenseManager.CalculatePieDataset();
                    expenseManager.CalculateCategoryDataset();
                }
            }
        });
    }

    private void InitializationData() {
        expenseManager.CalculatePieDataset();
        expenseManager.CalculateCategoryDataset();
    }

    private void InitializationUI() {
        CreatePieChart();
        CreateStackedBarChart();
        CreateLineChart();
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
