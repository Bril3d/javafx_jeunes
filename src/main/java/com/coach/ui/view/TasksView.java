package com.coach.ui.view;

import atlantafx.base.theme.Styles;
import com.coach.model.Task;
import com.coach.service.TaskService;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.text.Text;
import org.kordamp.ikonli.javafx.FontIcon;
import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.util.Duration;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

public class TasksView {

    private final TaskService taskService;
    private final VBox root;
    private final VBox tasksListContainer;

    public TasksView(ViewManager viewManager) {
        this.taskService = new TaskService(viewManager.getUserService());
        this.root = new VBox(20);
        this.tasksListContainer = new VBox(10);
        initView();
    }

    private void initView() {
        root.setPadding(new Insets(30));

        HBox headerBox = new HBox();
        headerBox.setAlignment(Pos.CENTER_LEFT);
        
        Label header = new Label("My Tasks");
        header.getStyleClass().add(Styles.TITLE_1);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button addTaskBtn = new Button("Add Task", new FontIcon("fth-plus"));
        addTaskBtn.getStyleClass().add("button-gradient");
        addTaskBtn.setOnAction(e -> showTaskDialog(null));

        headerBox.getChildren().addAll(header, spacer, addTaskBtn);

        ScrollPane scrollPane = new ScrollPane(tasksListContainer);
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background-color: transparent; -fx-control-inner-background: transparent;");

        refreshTasks();

        root.getChildren().addAll(headerBox, scrollPane);
    }

    private final Map<Integer, Timeline> activeTimelines = new HashMap<>();
    private final Map<Integer, Integer> sessionMinutes = new HashMap<>();

    private void refreshTasks() {
        // Stop all timelines before clearing
        activeTimelines.values().forEach(Timeline::stop);
        activeTimelines.clear();
        sessionMinutes.clear();

        tasksListContainer.getChildren().clear();
        List<Task> tasks = taskService.getAutoPrioritizedTasks();
        if (tasks.isEmpty()) {
            Label emptyLbl = new Label("No tasks yet. Create one!");
            emptyLbl.getStyleClass().add(Styles.TEXT_MUTED);
            tasksListContainer.getChildren().add(emptyLbl);
        } else {
            for (Task t : tasks) {
                tasksListContainer.getChildren().add(createTaskCard(t));
            }
        }
    }

    private Node createTaskCard(Task task) {
        HBox card = new HBox(15);
        card.setPadding(new Insets(15));
        card.setAlignment(Pos.CENTER_LEFT);
        card.getStyleClass().add("glass-panel");
        card.setStyle("-fx-background-radius: 12px;");

        CheckBox doneCheck = new CheckBox();
        doneCheck.setSelected("DONE".equalsIgnoreCase(task.getStatus()));
        doneCheck.setOnAction(e -> {
            task.setStatus(doneCheck.isSelected() ? "DONE" : "TODO");
            taskService.updateTask(task);
            refreshTasks();
        });

        VBox info = new VBox(2);
        Text title = new Text(task.getTitle());
        title.getStyleClass().add(Styles.TITLE_4);
        title.setFill(javafx.scene.paint.Color.WHITE);
        
        if (doneCheck.isSelected()) {
            title.setStrikethrough(true);
            title.setOpacity(0.5);
        }

        Label timeLbl = new Label(task.getTimeSpentMinutes() + "/" + task.getEstimatedTimeMinutes() + "m");
        timeLbl.getStyleClass().add(Styles.TEXT_SUBTLE);

        Label meta = new Label((task.getCategory() != null ? task.getCategory() : "No Category") + 
                               " | Due: " + (task.getDeadline() != null ? task.getDeadline() : "None"));
        meta.getStyleClass().add(Styles.TEXT_SUBTLE);
        
        HBox metaBox = new HBox(10, meta, timeLbl);
        info.getChildren().addAll(title, metaBox);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button timerBtn = new Button("", new FontIcon("fth-play"));
        timerBtn.getStyleClass().addAll(Styles.BUTTON_ICON, Styles.FLAT);
        
        timerBtn.setOnAction(e -> {
            if (activeTimelines.containsKey(task.getId())) {
                // Stop timer
                activeTimelines.get(task.getId()).stop();
                activeTimelines.remove(task.getId());
                timerBtn.setGraphic(new FontIcon("fth-play"));
                taskService.updateTask(task); // Save accumulated time
            } else {
                // Start timer
                timerBtn.setGraphic(new FontIcon("fth-square"));
                Timeline timeline = new Timeline(new KeyFrame(Duration.minutes(1), ev -> {
                    int current = sessionMinutes.getOrDefault(task.getId(), 0);
                    sessionMinutes.put(task.getId(), current + 1);
                    
                    // Save to DB every minute
                    task.setTimeSpentMinutes(task.getTimeSpentMinutes() + 1);
                    taskService.updateTask(task);
                    
                    timeLbl.setText(task.getTimeSpentMinutes() + "/" + task.getEstimatedTimeMinutes() + "m");
                }));
                timeline.setCycleCount(Animation.INDEFINITE);
                timeline.play();
                activeTimelines.put(task.getId(), timeline);
            }
        });

        Button logTimeBtn = new Button("", new FontIcon("fth-plus-circle"));
        logTimeBtn.getStyleClass().addAll(Styles.BUTTON_ICON, Styles.FLAT);
        logTimeBtn.setOnAction(e -> showLogTimeDialog(task));

        Button editBtn = new Button("", new FontIcon("fth-edit"));
        editBtn.getStyleClass().addAll(Styles.BUTTON_ICON, Styles.FLAT);
        editBtn.setOnAction(e -> showTaskDialog(task));

        Button deleteBtn = new Button("", new FontIcon("fth-trash-2"));
        deleteBtn.getStyleClass().addAll(Styles.BUTTON_ICON, Styles.DANGER, Styles.FLAT);
        deleteBtn.setOnAction(e -> {
            taskService.deleteTask(task.getId());
            refreshTasks();
        });

        card.getChildren().addAll(doneCheck, info, spacer, timerBtn, logTimeBtn, editBtn, deleteBtn);
        return card;
    }

    private void showLogTimeDialog(Task task) {
        TextInputDialog dialog = new TextInputDialog("15");
        dialog.setTitle("Log Time");
        dialog.setHeaderText("Log time spent on: " + task.getTitle());
        dialog.setContentText("Minutes spent:");
        dialog.showAndWait().ifPresent(minutes -> {
            try {
                int mins = Integer.parseInt(minutes);
                task.setTimeSpentMinutes(task.getTimeSpentMinutes() + mins);
                taskService.updateTask(task);
                refreshTasks();
            } catch (NumberFormatException e) {
                // Ignore invalid input
            }
        });
    }

    private void showTaskDialog(Task taskToEdit) {
        boolean isEdit = taskToEdit != null;
        Dialog<Task> dialog = new Dialog<>();
        dialog.setTitle(isEdit ? "Edit Task" : "New Task");
        dialog.setHeaderText(isEdit ? "Update your task details" : "Create a new productivity task");

        ButtonType saveButtonType = new ButtonType(isEdit ? "Update" : "Save", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(15);
        grid.setPadding(new Insets(20));

        TextField title = new TextField(isEdit ? taskToEdit.getTitle() : "");
        title.setPromptText("Task Title");
        
        ComboBox<com.coach.model.TaskCategory> category = new ComboBox<>();
        category.getItems().addAll(com.coach.model.TaskCategory.values());
        if (isEdit && taskToEdit.getCategory() != null) {
            category.setValue(com.coach.model.TaskCategory.fromString(taskToEdit.getCategory()));
        } else {
            category.setValue(com.coach.model.TaskCategory.OTHER);
        }

        ComboBox<String> priority = new ComboBox<>();
        priority.getItems().addAll("High", "Medium", "Low");
        if (isEdit) {
            priority.getSelectionModel().select(taskToEdit.getPriority() - 1);
        } else {
            priority.getSelectionModel().select(1); // Default to Medium
        }

        DatePicker deadline = new DatePicker(isEdit ? taskToEdit.getDeadline() : null);

        TextField estTime = new TextField(isEdit ? String.valueOf(taskToEdit.getEstimatedTimeMinutes()) : "30");
        estTime.setPromptText("Est. Minutes");

        grid.add(new Label("Title:"), 0, 0);
        grid.add(title, 1, 0);
        grid.add(new Label("Category:"), 0, 1);
        grid.add(category, 1, 1);
        grid.add(new Label("Priority:"), 0, 2);
        grid.add(priority, 1, 2);
        grid.add(new Label("Deadline:"), 0, 3);
        grid.add(deadline, 1, 3);
        grid.add(new Label("Est. Time (min):"), 0, 4);
        grid.add(estTime, 1, 4);

        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == saveButtonType) {
                Task t = isEdit ? taskToEdit : new Task();
                t.setTitle(title.getText());
                t.setCategory(category.getValue().getDisplayName());
                t.setPriority(priority.getSelectionModel().getSelectedIndex() + 1);
                t.setDeadline(deadline.getValue());
                try {
                    t.setEstimatedTimeMinutes(Integer.parseInt(estTime.getText()));
                } catch (NumberFormatException e) {
                    t.setEstimatedTimeMinutes(0);
                }
                if (!isEdit) {
                    t.setStatus("TODO");
                    t.setTimeSpentMinutes(0);
                }
                return t;
            }
            return null;
        });

        dialog.showAndWait().ifPresent(task -> {
            if (isEdit) {
                taskService.updateTask(task);
            } else {
                taskService.addTask(task);
            }
            refreshTasks();
        });
    }

    public Node getView() {
        return root;
    }
}
