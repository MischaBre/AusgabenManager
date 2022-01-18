import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PiePlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.data.general.DefaultPieDataset;

import javax.swing.*;
import javax.swing.text.DateFormatter;


import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
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
    private DefaultCategoryDataset categoryMonthDataset;
    //private DefaultCategoryDataset categoryDayDataset;

    private JFreeChart lineChart;
    private ChartPanel lineChartPanel;


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

        if (!expenses.isEmpty()) {
            InitializationData();
            InitializationUI();
        }

        this.addComponentListener(new ComponentAdapter() {
            public void componentHidden(ComponentEvent e) {
                /* code run when component hidden*/
            }
            public void componentShown(ComponentEvent e) {
                if (!expenses.isEmpty()) {
                    CalculatePieDataset();
                    CalculateCategoryDataset();
                }
            }
        });
    }

    private void InitializationData() {
        categoryMonthDataset = new DefaultCategoryDataset();
        //categoryDayDataset = new DefaultCategoryDataset();
        pieDataset = new DefaultPieDataset();
        CalculatePieDataset();
        CalculateCategoryDataset();
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
                categoryMonthDataset,
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
                categoryMonthDataset,
                PlotOrientation.VERTICAL,
                true,
                false,
                false);
        lineChartPanel = new ChartPanel(lineChart);
        linePanel.add(lineChartPanel);
    }

    private void CalculateCategoryDataset() {
        categoryMonthDataset.clear();
        //categoryDayDataset.clear();
        LocalDate begin = expenses.stream()
                .min(Expense::compareTo)
                .get().getDate();
        LocalDate end = expenses.stream()
                .max(Expense::compareTo)
                .get().getDate();
        int startMonth = begin.getMonthValue();
        //int startDay = begin.getDayOfYear();
        int monthDifference = (int)ChronoUnit.MONTHS.between(begin.withDayOfMonth(1), end.withDayOfMonth(1));
        int dayDifference = (int)ChronoUnit.DAYS.between(begin,end);

        double[][] valuesMonths = new double[categories.size()][monthDifference];
        //double[][] valuesDays = new double[categories.size()][dayDifference];
        String[] months = new String[monthDifference];
        //String[] days = new String[dayDifference];
        String[] categoryStrings = new String[categories.size()];

        for (int i = 0; i < monthDifference; i++) {
            months[i] = begin.plusMonths(i).format(DateTimeFormatter.ofPattern("MM.yyyy"));
            int finalI = i;
            int finalC = 0;
            for (String c : categories) {
                int finalC1 = finalC;
                if (i == 0) {
                    categoryStrings[finalC1] = c;
                }
                valuesMonths[finalC1][finalI] = 0.0;

                expenses.stream()
                        .filter(e -> e.getDate().format(DateTimeFormatter.ofPattern("MM.yyyy")).equals(months[finalI]))
                        .filter(e -> e.getCategory().equals(c))
                        .forEach(e -> {
                            valuesMonths[finalC1][finalI] += e.getAmount();
                        });
                categoryMonthDataset.setValue(valuesMonths[finalC1][finalI], categoryStrings[finalC1], months[finalI]);
                finalC++;
            }
        }

        /*

        @Deprecated
        for(int i = 0; i < dayDifference; i++) {
            days[i] = (begin.plusDays(i)).format(DateTimeFormatter.ofPattern("dd.MM.yyyy"));
            int finalI = i;
            int finalC = 0;
            for(String c : categories) {
                valuesDays[finalC][finalI] = 0.0;
                int finalC1 = finalC;
                expenses.stream()
                        .filter(e -> e.getDate().format(DateTimeFormatter.ofPattern("dd.MM.yyyy")).equals(days[finalI]))
                        .filter(e -> e.getCategory().equals(c))
                        .forEach(e -> {
                            valuesDays[finalC1][finalI] += e.getAmount();
                        });
                categoryDayDataset.setValue(valuesDays[finalC1][finalI], categoryStrings[finalC1], days[finalI]);
                finalC++;
            }
        }
        */
    }

    private void CreatePieChart() {
        piePanel.setLayout(new java.awt.BorderLayout());
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

    private void CalculatePieDataset() {
        pieDataset.clear();
        TreeMap<String, Double> dataMap = new TreeMap<String, Double>();
        categories.forEach(k -> dataMap.put(k, 0.0));
        expenses.stream()
                .filter(e -> !e.getCategory().equals(""))
                .forEach(e -> dataMap.replace(e.getCategory(), dataMap.get(e.getCategory())+e.getAmount()));
        dataMap.forEach(pieDataset::setValue);
    }

}
