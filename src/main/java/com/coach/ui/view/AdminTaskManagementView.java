package com.coach.ui.view;

import atlantafx.base.theme.Styles;
import com.coach.model.Task;
import com.coach.model.User;
import com.coach.service.TaskService;
import com.coach.service.UserService;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.text.Text;
import org.kordamp.ikonli.javafx.FontIcon;

import java.util.List;

public class AdminTaskManagementView {

    private final TaskService taskService;
    private final User user;
    private final VBox root;
    private final VBox tasksList;

    public AdminTaskManagementView(UserService userService, User user) {
        this.taskService = new TaskService(userService);
        this.user = user;
        this.root = new VBox(15);
        this.root.setPadding(new Insets(20));
        this.tasksList = new VBox(10);
        
        initView();
    }

    private void initView() {
        Label header = new Label("Tasks for " + user.getUsername());
        header.getStyleClass().add(Styles.TITLE_3);

        ScrollPane scrollPane = new ScrollPane(tasksList);
        scrollPane.setFitToWidth(true);
        scrollPane.setPrefHeight(400);
        scrollPane.setStyle("-fx-background-color: transparent; -fx-control-inner-background: transparent;");

        refreshTasks();

        root.getChildren().addAll(header, new Separator(), scrollPane);
    }

    private void refreshTasks() {
        tasksList.getChildren().clear();
        List<Task> tasks = taskService.getTasksByUserId(user.getId());
        
        if (tasks.isEmpty()) {
            tasksList.getChildren().add(new Label("No tasks found for this user."));
        } else {
            for (Task t : tasks) {
                tasksList.getChildren().add(createTaskCard(t));
            }
        }
    }

    private Node createTaskCard(Task task) {
        HBox card = new HBox(15);
        card.setPadding(new Insets(10));
        card.setAlignment(Pos.CENTER_LEFT);
        card.getStyleClass().addAll(Styles.BG_DEFAULT, "glass-panel");
        card.setStyle("-fx-background-radius: 8; -fx-border-color: #444; -fx-border-radius: 8;");

        VBox info = new VBox(2);
        Label title = new Label(task.getTitle());
        title.getStyleClass().add(Styles.TEXT_BOLD);
        Label meta = new Label(task.getStatus() + " | Priority: " + task.getPriority() + " | Due: " + (task.getDeadline() != null ? task.getDeadline() : "N/A"));
        meta.getStyleClass().add(Styles.TEXT_SMALL);
        info.getChildren().addAll(title, meta);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button deleteBtn = new Button("", new FontIcon("fth-trash-2"));
        deleteBtn.getStyleClass().addAll(Styles.BUTTON_ICON, Styles.DANGER, Styles.FLAT);
        deleteBtn.setOnAction(e -> {
            Alert confirm = new Alert(Alert.AlertType.CONFIRMATION, "Delete task: " + task.getTitle() + "?", ButtonType.YES, ButtonType.NO);
            confirm.showAndWait().ifPresent(response -> {
                if (response == ButtonType.YES) {
                    taskService.adminDeleteTask(task.getId());
                    refreshTasks();
                }
            });
        });

        card.getChildren().addAll(info, spacer, deleteBtn);
        return card;
    }

    public VBox getView() {
        return root;
    }
}
