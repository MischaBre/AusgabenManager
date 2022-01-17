import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PiePlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.category.CategoryDataset;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.data.general.DefaultPieDataset;

import javax.swing.*;


import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.TreeMap;
import java.util.TreeSet;

public class DetailFrame extends JFrame{
    private JPanel mainPanel;
    private JPanel piePanel;
    private JPanel stackedBarPanel;
    private JPanel linePanel;
    private JPanel controlPanel;

    private JFreeChart pieChart;
    private ChartPanel pieChartPanel;
    private DefaultPieDataset pieDataset;

    private JFreeChart stackedBarChart;
    private ChartPanel stackedBarChartPanel;
    private DefaultCategoryDataset stackedDCBarDataset;

    private List<Expense> expenses;
    private TreeSet<String> categories;

    public DetailFrame(String title, List<Expense> expenses, TreeMap<String, Double> categories) {
        super(title);

        this.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
        this.setContentPane(mainPanel);
        this.pack();

        this.expenses = expenses;
        this.categories = new TreeSet<>();
        categories.forEach((k,v) -> this.categories.add(k));

        Initialization();

        this.addComponentListener(new ComponentAdapter() {
            public void componentHidden(ComponentEvent e) {
                /* code run when component hidden*/
            }
            public void componentShown(ComponentEvent e) {
                if (!expenses.isEmpty()) {
                    RedrawPieChart();
                    RedrawStackedBarDataset();
                }
            }
        });
    }

    private void Initialization() {
        if (!expenses.isEmpty()) {
            CreatePieChart();
            CreateStackedBarChart();
        }
    }

    private void CreateStackedBarChart() {
        stackedBarPanel.setLayout(new java.awt.BorderLayout());
        stackedDCBarDataset = new DefaultCategoryDataset();
        RedrawStackedBarDataset();
        stackedBarChart = ChartFactory.createStackedBarChart(
                "Nach Monaten",
                "Monate",
                "€",
                stackedDCBarDataset,
                PlotOrientation.VERTICAL,
                true,
                false,
                false);
        stackedBarChartPanel = new ChartPanel(stackedBarChart);
        stackedBarPanel.add(stackedBarChartPanel);
    }

    private void RedrawStackedBarDataset() {
        stackedDCBarDataset.clear();
        //stackedBarDataset = null;
        LocalDate begin = expenses.stream()
                .min(Expense::compareTo)
                .get().getDate();
        LocalDate end = expenses.stream()
                .max(Expense::compareTo)
                .get().getDate();
        int startMonth = begin.getMonthValue();
        int monthDifference = (int)ChronoUnit.MONTHS.between(begin.withDayOfMonth(1), end.withDayOfMonth(1));

        double[][] values = new double[categories.size()][monthDifference];
        String[] months = new String[monthDifference];
        String[] categoryStrings = new String[categories.size()];

        for (int i = 0; i < monthDifference; i++) {
            months[i] = String.valueOf((startMonth + i - 1) % 12 + 1) ;
            int finalI = i;
            int finalC = 0;
            for (String c : categories) {
                if (i == 0) {
                    categoryStrings[finalC] = c;
                }
                values[finalC][finalI] = 0.0;
                int finalC1 = finalC;
                expenses.stream()
                        .filter(e -> e.getDate().getMonthValue() == Integer.parseInt(months[finalI]))
                        .filter(e -> e.getCategory().equals(c))
                        .forEach(e -> {
                            values[finalC1][finalI] += e.getAmount();
                        });
                finalC++;
            }
        }
        int i = 0;
        for (double[] a : values) {
            int j = 0;
            for (double b : a) {
                stackedDCBarDataset.setValue(b, categoryStrings[i],months[j]);
                j++;
            }
            i++;
        }
        //stackedBarDataset = DatasetUtils.createCategoryDataset(categoryStrings, months, values);
    }

    private void CreatePieChart() {
        piePanel.setLayout(new java.awt.BorderLayout());
        pieDataset = new DefaultPieDataset();
        RedrawPieChart();
        pieChart = ChartFactory.createPieChart(
                "Nach Kategorie",
                pieDataset,
                false,
                false,
                false);
        PiePlot plot = (PiePlot) pieChart.getPlot();
        plot.setSectionPaint(0, new Color(0,0,0));
        pieChartPanel = new ChartPanel(pieChart);
        piePanel.add(pieChartPanel);
    }

    private void RedrawPieChart() {
        pieDataset.clear();
        calculateCategorySummary().forEach(pieDataset::setValue);
    }

    private TreeMap<String, Double> calculateCategorySummary() {
        TreeMap<String, Double> dataMap = new TreeMap<String, Double>();
        categories.forEach(k -> dataMap.put(k, 0.0));
        expenses.stream()
                .filter(e -> e.getCategory() != "")
                .forEach(e -> dataMap.replace(e.getCategory(), dataMap.get(e.getCategory())+e.getAmount()));
        return dataMap;
    }


}
