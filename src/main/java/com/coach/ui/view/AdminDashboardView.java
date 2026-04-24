package com.coach.ui.view;

import atlantafx.base.theme.Styles;
import com.coach.model.Task;
import com.coach.model.User;
import com.coach.repository.TaskRepository;
import com.coach.service.UserService;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.text.Text;
import org.kordamp.ikonli.javafx.FontIcon;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class AdminDashboardView {

    private final UserService userService;
    private final TaskRepository taskRepository;
    private final VBox root;
    private final VBox usersList;

    public AdminDashboardView(UserService userService) {
        this.userService = userService;
        this.taskRepository = new TaskRepository();
        
        this.root = new VBox(20);
        this.root.setPadding(new Insets(20));
        this.root.getStyleClass().add("admin-dashboard");

        Label header = new Label("Admin Management Console");
        header.getStyleClass().add(Styles.TITLE_1);

        Label subHeader = new Label("Overview of all users and productivity metrics");
        subHeader.getStyleClass().add(Styles.TEXT_MUTED);

        this.usersList = new VBox(15);
        
        ScrollPane scrollPane = new ScrollPane(usersList);
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background-color: transparent; -fx-control-inner-background: transparent;");

        refreshData();

        root.getChildren().addAll(header, subHeader, new Separator(), scrollPane);
    }

    private void refreshData() {
        usersList.getChildren().clear();
        
        List<User> users = userService.getAllUsers();
        List<Task> allTasks = taskRepository.findAll();
        Map<Integer, List<Task>> tasksByUser = allTasks.stream().collect(Collectors.groupingBy(Task::getUserId));

        for (User user : users) {
            usersList.getChildren().add(createUserRow(user, tasksByUser.getOrDefault(user.getId(), List.of())));
        }
    }

    private Node createUserRow(User user, List<Task> userTasks) {
        HBox row = new HBox(20);
        row.setPadding(new Insets(15));
        row.setAlignment(Pos.CENTER_LEFT);
        row.getStyleClass().addAll(Styles.BG_DEFAULT, "glass-panel");
        row.setStyle("-fx-background-radius: 8; -fx-border-color: #333; -fx-border-radius: 8;");

        VBox userInfo = new VBox(5);
        Label name = new Label(user.getUsername() + " (" + user.getRole() + ")");
        name.getStyleClass().add(Styles.TEXT_BOLD);
        Label email = new Label(user.getEmail());
        email.getStyleClass().add(Styles.TEXT_SMALL);
        userInfo.getChildren().addAll(name, email);

        HBox stats = new HBox(30);
        stats.setAlignment(Pos.CENTER);
        
        long completed = userTasks.stream().filter(t -> "DONE".equals(t.getStatus())).count();
        int totalMinutes = userTasks.stream().mapToInt(Task::getTimeSpentMinutes).sum();

        stats.getChildren().addAll(
            createStat("Tasks", String.valueOf(userTasks.size()), "fth-list"),
            createStat("Done", String.valueOf(completed), "fth-check-circle"),
            createStat("Time", totalMinutes + "m", "fth-clock")
        );

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        row.getChildren().addAll(userInfo, spacer, stats);
        return row;
    }

    private VBox createStat(String label, String value, String iconCode) {
        VBox vbox = new VBox(2);
        vbox.setAlignment(Pos.CENTER);
        
        Label val = new Label(value);
        val.getStyleClass().addAll(Styles.TEXT_BOLD, Styles.TITLE_4);
        FontIcon icon = new FontIcon(iconCode);
        icon.setIconSize(18);
        val.setGraphic(icon);
        
        Label lbl = new Label(label);
        lbl.getStyleClass().add(Styles.TEXT_SMALL);
        
        vbox.getChildren().addAll(val, lbl);
        return vbox;
    }

    public VBox getView() {
        return root;
    }
}
