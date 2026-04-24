package com.coach.ui.view;

import atlantafx.base.theme.Styles;
import com.coach.model.Task;
import com.coach.service.TaskService;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.*;
import javafx.scene.text.Text;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class CalendarView {

    private final VBox root;
    private final TaskService taskService;

    public CalendarView(ViewManager viewManager) {
        this.taskService = new TaskService(viewManager.getUserService());
        this.root = new VBox(20);
        initView();
    }

    private void initView() {
        root.setPadding(new Insets(30));
        root.getStyleClass().add("view-container");

        Label header = new Label("CALENDAR VIEW");
        header.setStyle("-fx-font-size: 24px; -fx-font-weight: 900; -fx-text-fill: white; -fx-letter-spacing: 2px;");

        ScrollPane scroll = new ScrollPane();
        scroll.setFitToWidth(true);
        scroll.getStyleClass().add("modern-scroll");

        VBox content = new VBox(25);
        content.setPadding(new Insets(10));

        // Group tasks by deadline
        List<Task> tasks = taskService.getMyTasks();
        Map<LocalDate, List<Task>> groupedTasks = tasks.stream()
                .filter(t -> t.getDeadline() != null)
                .collect(Collectors.groupingBy(Task::getDeadline));

        // Sort dates
        List<LocalDate> sortedDates = groupedTasks.keySet().stream()
                .sorted()
                .collect(Collectors.toList());

        if (sortedDates.isEmpty()) {
            content.getChildren().add(new Label("No tasks with deadlines found."));
        } else {
            for (LocalDate date : sortedDates) {
                content.getChildren().add(createDateSection(date, groupedTasks.get(date)));
            }
        }

        scroll.setContent(content);
        VBox.setVgrow(scroll, Priority.ALWAYS);

        root.getChildren().addAll(header, scroll);
    }

    private Node createDateSection(LocalDate date, List<Task> tasks) {
        VBox section = new VBox(15);
        
        Label dateLbl = new Label(date.format(DateTimeFormatter.ofPattern("EEEE, MMMM d, yyyy")));
        dateLbl.getStyleClass().add(Styles.TITLE_3);
        dateLbl.setStyle("-fx-text-fill: -color-accent-fg;");

        VBox cards = new VBox(10);
        for (Task t : tasks) {
            cards.getChildren().add(createMiniTaskCard(t));
        }

        section.getChildren().addAll(dateLbl, cards);
        return section;
    }

    private Node createMiniTaskCard(Task task) {
        HBox card = new HBox(15);
        card.setPadding(new Insets(12));
        card.setAlignment(Pos.CENTER_LEFT);
        card.getStyleClass().add("glass-panel");
        card.setStyle("-fx-background-radius: 8px; -fx-border-color: rgba(255,255,255,0.05); -fx-border-width: 1px;");

        VBox info = new VBox(2);
        Text title = new Text(task.getTitle());
        title.getStyleClass().add(Styles.TEXT_BOLD);
        title.setFill(javafx.scene.paint.Color.WHITE);

        Label meta = new Label(task.getCategory() + " | " + task.getStatus());
        meta.getStyleClass().add(Styles.TEXT_SMALL);
        meta.setOpacity(0.6);

        info.getChildren().addAll(title, meta);
        card.getChildren().add(info);
        
        return card;
    }

    public Node getView() {
        return root;
    }
}
