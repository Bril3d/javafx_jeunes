package com.coach.ui.view;

import atlantafx.base.theme.Styles;
import com.coach.service.UserService;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.*;

public class ViewManager {
    private static ViewManager instance;
    private final Scene mainScene;
    private final BorderPane rootPane;
    private final UserService userService;

    // Sidebar references to toggle visibility
    private VBox sidebar;

    private ViewManager(Scene scene) {
        this.mainScene = scene;
        this.userService = new UserService();

        this.rootPane = new BorderPane();
        mainScene.setRoot(rootPane);

        initSidebar();
    }

    public static void init(Scene scene) {
        if (instance == null) {
            instance = new ViewManager(scene);
            // Default view based on auth text
            // Default view starts at the Landing Page
            instance.navigateToLanding();
        }
    }

    public static ViewManager getInstance() {
        return instance;
    }

    public UserService getUserService() {
        return userService;
    }

    private void initSidebar() {
        sidebar = new VBox(20);
        sidebar.setPadding(new Insets(20));
        sidebar.setPrefWidth(250);
        sidebar.setStyle(
                "-fx-background-color: -color-bg-subtle; -fx-border-color: -color-border-default; -fx-border-width: 0 1 0 0;");

        Label logoLabel = new Label("TaskFlow");
        logoLabel.getStyleClass().addAll(Styles.TITLE_3);
        logoLabel.setStyle("-fx-text-fill: -color-accent-emphasis;");

        Button dashBtn = new Button("📊 Dashboard");
        dashBtn.getStyleClass().addAll(Styles.BUTTON_OUTLINED, Styles.ACCENT);
        dashBtn.setMaxWidth(Double.MAX_VALUE);
        dashBtn.setOnAction(e -> navigateToDashboard());

        Button tasksBtn = new Button("✅ Tasks");
        tasksBtn.getStyleClass().addAll(Styles.BUTTON_OUTLINED, Styles.ACCENT);
        tasksBtn.setMaxWidth(Double.MAX_VALUE);
        tasksBtn.setOnAction(e -> navigateToTasks());

        Button aiBtn = new Button("🤖 AI Assistant");
        aiBtn.getStyleClass().addAll(Styles.BUTTON_OUTLINED, Styles.SUCCESS);
        aiBtn.setMaxWidth(Double.MAX_VALUE);
        aiBtn.setOnAction(e -> navigateToAi());

        Button logoutBtn = new Button("🚪 Logout");
        logoutBtn.getStyleClass().addAll(Styles.BUTTON_OUTLINED, Styles.DANGER);
        logoutBtn.setMaxWidth(Double.MAX_VALUE);
        logoutBtn.setOnAction(e -> {
            userService.logout();
            navigateToAuth();
        });

        Region spacer = new Region();
        VBox.setVgrow(spacer, Priority.ALWAYS);

        sidebar.getChildren().addAll(logoLabel, new Region() /* spacer */, dashBtn, tasksBtn, aiBtn, spacer, logoutBtn);
    }

    public void navigateToLanding() {
        sidebar.setVisible(false);
        sidebar.setManaged(false);
        rootPane.setLeft(null);

        rootPane.setCenter(new LandingView(this).getView());
    }

    public void navigateToAuth() {
        sidebar.setVisible(false);
        sidebar.setManaged(false);
        rootPane.setLeft(null);

        AuthView authView = new AuthView(this);
        rootPane.setCenter(authView.getView());
    }

    public void navigateToRegister() {
        sidebar.setVisible(false);
        sidebar.setManaged(false);
        rootPane.setLeft(null);

        RegisterView registerView = new RegisterView(this);
        rootPane.setCenter(registerView.getView());
    }

    public void navigateToDashboard() {
        showSidebar();
        rootPane.setCenter(new DashboardView(this).getView());
    }

    public void navigateToTasks() {
        showSidebar();
        rootPane.setCenter(new TasksView(this).getView());
    }

    public void navigateToAi() {
        showSidebar();
        rootPane.setCenter(new AiCoachView(this).getView());
    }

    private void showSidebar() {
        if (!sidebar.isVisible()) {
            sidebar.setVisible(true);
            sidebar.setManaged(true);
            rootPane.setLeft(sidebar);
        }
    }
}
