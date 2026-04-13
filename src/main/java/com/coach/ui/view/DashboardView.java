package com.coach.ui.view;

import atlantafx.base.theme.Styles;
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
        statsRow.getChildren().addAll(
            createStatCard("TOTAL TASKS", String.valueOf(tasks.size()), "fth-list", "-color-accent-emphasis"),
            createStatCard("COMPLETED", String.valueOf(completed), "fth-check-circle", "-color-success-emphasis"),
            createStatCard("PRODUCTIVITY", String.format("%.0f%%", rate * 100), "fth-trending-up", "#facc15")
        );

        // Charts Row
        HBox chartsRow = new HBox(30);
        chartsRow.setPrefHeight(400);
        
        VBox pieContainer = new VBox(20, new Label("TASK DISTRIBUTION"), createStatusChart(completed, todo));
        pieContainer.getStyleClass().add("glass-panel");
        pieContainer.setPadding(new Insets(20));
        HBox.setHgrow(pieContainer, Priority.ALWAYS);

        VBox barContainer = new VBox(20, new Label("TASKS BY CATEGORY"), createCategoryChart(tasks));
        barContainer.getStyleClass().add("glass-panel");
        barContainer.setPadding(new Insets(20));
        HBox.setHgrow(barContainer, Priority.ALWAYS);

        chartsRow.getChildren().addAll(pieContainer, barContainer);

        root.getChildren().addAll(header, statsRow, chartsRow);
    }

    private Chart createStatusChart(long completed, long todo) {
        PieChart chart = new PieChart();
        chart.getData().add(new PieChart.Data("Done", completed));
        chart.getData().add(new PieChart.Data("Todo", todo));
        chart.setLabelsVisible(true);
        chart.setLegendSide(javafx.geometry.Side.BOTTOM);
        chart.getStyleClass().add("modern-chart");
        return chart;
    }

    private Chart createCategoryChart(List<Task> tasks) {
        Map<String, Long> counts = tasks.stream()
            .collect(Collectors.groupingBy(t -> t.getCategory() == null || t.getCategory().isEmpty() ? "Uncategorized" : t.getCategory(), Collectors.counting()));

        CategoryAxis xAxis = new CategoryAxis();
        NumberAxis yAxis = new NumberAxis();
        BarChart<String, Number> chart = new BarChart<>(xAxis, yAxis);
        
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        counts.forEach((cat, count) -> series.getData().add(new XYChart.Data<>(cat, count)));
        
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
