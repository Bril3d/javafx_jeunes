package com.coach.ui.view;

import com.coach.model.Task;
import com.coach.service.TaskService;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.chart.*;
import javafx.scene.control.Label;
import javafx.scene.layout.*;
import org.kordamp.ikonli.javafx.FontIcon;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class DashboardView {

    private final ViewManager viewManager;
    private final VBox root;

    public DashboardView(ViewManager viewManager) {
        this.viewManager = viewManager;
        this.root = new VBox(20);
        initView();
    }

    private void initView() {
        root.setPadding(new Insets(30));
        root.setSpacing(30);
        
        Label header = new Label("OVERVIEW");
        header.setStyle("-fx-font-size: 24px; -fx-font-weight: 900; -fx-text-fill: white; -fx-letter-spacing: 2px;");

        TaskService ts = new TaskService(viewManager.getUserService());
        List<Task> tasks = ts.getMyTasks();
        long completed = tasks.stream().filter(t -> "DONE".equalsIgnoreCase(t.getStatus())).count();
        long todo = tasks.size() - completed;
        double rate = ts.getCompletionRate();

        // Stats Row
        HBox statsRow = new HBox(20);
        long totalMinutes = tasks.stream().mapToLong(Task::getTimeSpentMinutes).sum();
        String totalHours = String.format("%.1fh", totalMinutes / 60.0);
        
        statsRow.getChildren().addAll(
            createStatCard("TOTAL TASKS", String.valueOf(tasks.size()), "fth-list", "-color-accent-emphasis"),
            createStatCard("TIME TRACKED", totalHours, "fth-clock", "#facc15"),
            createStatCard("PRODUCTIVITY", String.format("%.0f%%", rate * 100), "fth-trending-up", "-color-success-emphasis")
        );

        // Charts Row 1
        HBox chartsRow = new HBox(30);
        chartsRow.setPrefHeight(350);
        
        VBox pieContainer = new VBox(20, new Label("TASK DISTRIBUTION"), createStatusChart(completed, todo));
        pieContainer.getStyleClass().add("glass-panel");
        pieContainer.setPadding(new Insets(20));
        HBox.setHgrow(pieContainer, Priority.ALWAYS);

        VBox barContainer = new VBox(20, new Label("TASKS BY CATEGORY"), createCategoryChart(tasks));
        barContainer.getStyleClass().add("glass-panel");
        barContainer.setPadding(new Insets(20));
        HBox.setHgrow(barContainer, Priority.ALWAYS);

        chartsRow.getChildren().addAll(pieContainer, barContainer);

        // Charts Row 2: Time Comparison
        VBox timeContainer = new VBox(20, new Label("TIME ANALYSIS (ESTIMATED VS ACTUAL)"), createTimeComparisonChart(tasks));
        timeContainer.getStyleClass().add("glass-panel");
        timeContainer.setPadding(new Insets(20));
        timeContainer.setPrefHeight(350);

        root.getChildren().addAll(header, statsRow, chartsRow, timeContainer);
    }

    private Chart createTimeComparisonChart(List<Task> tasks) {
        CategoryAxis xAxis = new CategoryAxis();
        NumberAxis yAxis = new NumberAxis();
        yAxis.setLabel("Minutes");
        BarChart<String, Number> chart = new BarChart<>(xAxis, yAxis);

        XYChart.Series<String, Number> estSeries = new XYChart.Series<>();
        estSeries.setName("Estimated");
        XYChart.Series<String, Number> spentSeries = new XYChart.Series<>();
        spentSeries.setName("Spent");

        // Take top 5 recent tasks or some subset to avoid clutter
        List<Task> recentTasks = tasks.stream()
            .sorted((t1, t2) -> Long.compare(t2.getId(), t1.getId()))
            .limit(7)
            .collect(Collectors.toList());

        for (Task t : recentTasks) {
            String shortTitle = t.getTitle().length() > 10 ? t.getTitle().substring(0, 10) + "..." : t.getTitle();
            XYChart.Data<String, Number> estData = new XYChart.Data<>(shortTitle, t.getEstimatedTimeMinutes());
            XYChart.Data<String, Number> spentData = new XYChart.Data<>(shortTitle, t.getTimeSpentMinutes());
            
            estSeries.getData().add(estData);
            spentSeries.getData().add(spentData);
        }

        chart.getData().addAll(estSeries, spentSeries);
        chart.getStyleClass().add("modern-chart");
        
        // Custom colors for bars
        chart.getData().forEach(s -> {
            String color = s.getName().equals("Estimated") ? "#6366F1" : "#10b981";
            for (XYChart.Data<String, Number> data : s.getData()) {
                data.nodeProperty().addListener((obs, oldNode, newNode) -> {
                    if (newNode != null) newNode.setStyle("-fx-background-color: " + color + ";");
                });
            }
        });

        return chart;
    }

    private Chart createStatusChart(long completed, long todo) {
        PieChart chart = new PieChart();
        PieChart.Data doneData = new PieChart.Data("Done", completed);
        PieChart.Data todoData = new PieChart.Data("Todo", todo);
        chart.getData().addAll(doneData, todoData);

        // Programmatic coloring to override theme defaults
        applyPieDataStyle(doneData, "#6366F1"); // Indigo
        applyPieDataStyle(todoData, "rgba(99, 102, 241, 0.4)"); // Muted Indigo

        chart.setLabelsVisible(true);
        chart.setLegendSide(javafx.geometry.Side.BOTTOM);
        chart.getStyleClass().add("modern-chart");
        return chart;
    }

    private void applyPieDataStyle(PieChart.Data data, String color) {
        data.nodeProperty().addListener((obs, oldNode, newNode) -> {
            if (newNode != null) {
                newNode.setStyle("-fx-background-color: " + color + ";");
            }
        });
        // Also try immediate applying if already exists
        if (data.getNode() != null) {
            data.getNode().setStyle("-fx-background-color: " + color + ";");
        }
    }

    private Chart createCategoryChart(List<Task> tasks) {
        Map<String, Long> counts = tasks.stream()
            .collect(Collectors.groupingBy(t -> t.getCategory() == null || t.getCategory().isEmpty() ? "Uncategorized" : t.getCategory(), Collectors.counting()));

        CategoryAxis xAxis = new CategoryAxis();
        NumberAxis yAxis = new NumberAxis();
        BarChart<String, Number> chart = new BarChart<>(xAxis, yAxis);
        
        @SuppressWarnings("unchecked")
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        String[] palette = {"#6366F1", "#10b981", "#f59e0b", "#ec4899", "#8b5cf6"};
        int i = 0;
        
        for (Map.Entry<String, Long> entry : counts.entrySet()) {
            XYChart.Data<String, Number> data = new XYChart.Data<>(entry.getKey(), entry.getValue());
            String color = palette[i % palette.length];
            data.nodeProperty().addListener((obs, oldNode, newNode) -> {
                if (newNode != null) {
                    newNode.setStyle("-fx-background-color: " + color + ";");
                }
            });
            series.getData().add(data);
            i++;
        }
        
        chart.getData().add(series);
        chart.setLegendVisible(false);
        chart.getStyleClass().add("modern-chart");
        return chart;
    }

    private Node createStatCard(String title, String value, String iconString, String accentColor) {
        VBox card = new VBox(10);
        card.setPadding(new Insets(20));
        card.getStyleClass().add("glass-panel");
        card.setPrefWidth(220);

        FontIcon icon = new FontIcon(iconString);
        icon.setIconSize(24);
        icon.setStyle("-fx-icon-color: " + accentColor + ";");

        Label titleLbl = new Label(title);
        titleLbl.setStyle("-fx-font-size: 12px; -fx-font-weight: bold; -fx-text-fill: -color-fg-muted;");

        Label valueLbl = new Label(value);
        valueLbl.setStyle("-fx-font-size: 28px; -fx-font-weight: 800; -fx-text-fill: white;");

        HBox top = new HBox(icon);
        top.setAlignment(Pos.CENTER_LEFT);

        card.getChildren().addAll(top, titleLbl, valueLbl);
        return card;
    }

    public Node getView() {
        return root;
    }
}
