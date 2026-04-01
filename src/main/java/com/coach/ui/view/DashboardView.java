package com.coach.ui.view;

import atlantafx.base.theme.Styles;
import com.coach.model.Task;
import com.coach.service.TaskService;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import org.kordamp.ikonli.javafx.FontIcon;

import java.util.List;

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
        
        Label header = new Label("Dashboard");
        header.getStyleClass().add(Styles.TITLE_1);

        Label welcome = new Label("Welcome, " + viewManager.getUserService().getCurrentUser().getUsername() + "!");
        welcome.getStyleClass().add(Styles.TITLE_3);
        welcome.setStyle("-fx-text-fill: -color-fg-muted;");

        GridPane statsGrid = new GridPane();
        statsGrid.setHgap(20);
        statsGrid.setVgap(20);

        TaskService ts = new TaskService(viewManager.getUserService());
        List<Task> tasks = ts.getMyTasks();
        long completed = tasks.stream().filter(t -> "DONE".equalsIgnoreCase(t.getStatus())).count();
        double rate = ts.getCompletionRate();

        statsGrid.add(createStatCard("Total Tasks", String.valueOf(tasks.size()), "fth-list"), 0, 0);
        statsGrid.add(createStatCard("Completed", String.valueOf(completed), "fth-check-circle"), 1, 0);
        statsGrid.add(createStatCard("Productivity", String.format("%.0f%%", rate * 100), "fth-trending-up"), 2, 0);

        root.getChildren().addAll(header, welcome, statsGrid);
    }

    private Node createStatCard(String title, String value, String iconString) {
        VBox card = new VBox(10);
        card.setPadding(new Insets(20));
        card.getStyleClass().addAll(Styles.ELEVATED_1);
        card.setStyle("-fx-background-color: -color-bg-default; -fx-background-radius: 8px;");
        card.setPrefWidth(200);

        FontIcon icon = new FontIcon(iconString);
        icon.setIconSize(24);
        icon.getStyleClass().add(Styles.ACCENT);

        Label titleLbl = new Label(title);
        titleLbl.getStyleClass().add(Styles.TEXT_MUTED);

        Label valueLbl = new Label(value);
        valueLbl.getStyleClass().add(Styles.TITLE_2);

        HBox top = new HBox(icon);
        top.setAlignment(Pos.CENTER_LEFT);

        card.getChildren().addAll(top, titleLbl, valueLbl);
        return card;
    }

    public Node getView() {
        return root;
    }
}
