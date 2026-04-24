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

        HBox actions = new HBox(10);
        actions.setAlignment(Pos.CENTER);

        Button manageTasksBtn = new Button("Tasks", new FontIcon("fth-list"));
        manageTasksBtn.getStyleClass().add(Styles.BUTTON_OUTLINED);
        manageTasksBtn.setOnAction(e -> showManageTasksDialog(user));

        Button editBtn = new Button("", new FontIcon("fth-edit"));
        editBtn.getStyleClass().addAll(Styles.BUTTON_ICON, Styles.FLAT);
        editBtn.setOnAction(e -> showEditUserDialog(user));

        Button deleteBtn = new Button("", new FontIcon("fth-trash-2"));
        deleteBtn.getStyleClass().addAll(Styles.BUTTON_ICON, Styles.DANGER, Styles.FLAT);
        deleteBtn.setOnAction(e -> {
            Alert confirm = new Alert(Alert.AlertType.CONFIRMATION, "Delete user " + user.getUsername() + "? This will also delete all their tasks.", ButtonType.YES, ButtonType.NO);
            confirm.showAndWait().ifPresent(response -> {
                if (response == ButtonType.YES) {
                    userService.deleteUser(user.getId());
                    refreshData();
                }
            });
        });

        actions.getChildren().addAll(manageTasksBtn, editBtn, deleteBtn);

        row.getChildren().addAll(userInfo, spacer, stats, new Separator(javafx.geometry.Orientation.VERTICAL), actions);
        return row;
    }

    private void showEditUserDialog(User user) {
        Dialog<User> dialog = new Dialog<>();
        dialog.setTitle("Edit User");
        dialog.setHeaderText("Modify user details for " + user.getUsername());

        ButtonType saveButtonType = new ButtonType("Update", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(15);
        grid.setPadding(new Insets(20));

        TextField username = new TextField(user.getUsername());
        TextField email = new TextField(user.getEmail());
        ComboBox<String> role = new ComboBox<>();
        role.getItems().addAll("USER", "ADMIN");
        role.setValue(user.getRole());

        grid.add(new Label("Username:"), 0, 0);
        grid.add(username, 1, 0);
        grid.add(new Label("Email:"), 0, 1);
        grid.add(email, 1, 1);
        grid.add(new Label("Role:"), 0, 2);
        grid.add(role, 1, 2);

        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == saveButtonType) {
                user.setUsername(username.getText());
                user.setEmail(email.getText());
                user.setRole(role.getValue());
                return user;
            }
            return null;
        });

        dialog.showAndWait().ifPresent(updatedUser -> {
            userService.updateUser(updatedUser);
            refreshData();
        });
    }

    private void showManageTasksDialog(User user) {
        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle("Manage Tasks - " + user.getUsername());
        
        AdminTaskManagementView taskView = new AdminTaskManagementView(userService, user);
        dialog.getDialogPane().setContent(taskView.getView());
        dialog.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);
        
        dialog.showAndWait();
        refreshData(); // Refresh metrics in case tasks were deleted
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
