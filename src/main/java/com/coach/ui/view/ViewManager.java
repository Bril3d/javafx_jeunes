package com.coach.ui.view;

import atlantafx.base.theme.Styles;
import com.coach.service.UserService;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.layout.*;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;

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
        sidebar.setStyle("-fx-background-color: -color-bg-subtle; -fx-border-color: -color-border-default; -fx-border-width: 0 1 0 0;");
        refreshSidebar();
    }

    private void refreshSidebar() {
        if (sidebar == null) return;
        sidebar.getChildren().clear();
        
        Label logoLabel = new Label("TaskFlow");
        logoLabel.getStyleClass().addAll(Styles.TITLE_3);
        logoLabel.setStyle("-fx-text-fill: -color-accent-emphasis;");

        VBox navItems = new VBox(10);
        
        Button dashBtn = createNavBtn("📊 Dashboard", e -> navigateToDashboard(), Styles.ACCENT);
        Button tasksBtn = createNavBtn("✅ Tasks", e -> navigateToTasks(), Styles.ACCENT);
        Button calBtn = createNavBtn("📅 Calendar", e -> navigateToCalendar(), Styles.ACCENT);
        Button profileBtn = createNavBtn("👤 My Profile", e -> navigateToProfile(), Styles.ACCENT);
        Button aiBtn = createNavBtn("🤖 AI Assistant", e -> navigateToAi(), Styles.SUCCESS);
        
        navItems.getChildren().addAll(dashBtn, tasksBtn, calBtn, profileBtn, aiBtn);

        if (userService.isAdmin()) {
            Button adminBtn = createNavBtn("🔐 Admin Console", e -> navigateToAdminDashboard(), Styles.WARNING);
            navItems.getChildren().add(new Separator());
            navItems.getChildren().add(adminBtn);
        }

        Button logoutBtn = createNavBtn("🚪 Logout", e -> {
            userService.logout();
            navigateToAuth();
        }, Styles.DANGER);

        Region spacer = new Region();
        VBox.setVgrow(spacer, Priority.ALWAYS);

        sidebar.getChildren().addAll(logoLabel, new Region(), navItems, spacer, logoutBtn);
    }

    private Button createNavBtn(String text, EventHandler<ActionEvent> handler, String style) {
        Button btn = new Button(text);
        btn.getStyleClass().addAll(Styles.BUTTON_OUTLINED, style);
        btn.setMaxWidth(Double.MAX_VALUE);
        btn.setOnAction(handler);
        return btn;
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

    public void navigateToCalendar() {
        showSidebar();
        rootPane.setCenter(new CalendarView(this).getView());
    }

    public void navigateToAi() {
        showSidebar();
        rootPane.setCenter(new AiCoachView(this).getView());
    }

    public void navigateToProfile() {
        showSidebar();
        rootPane.setCenter(new ProfileView(this).getView());
    }

    public void navigateToAdminDashboard() {
        showSidebar();
        rootPane.setCenter(new AdminDashboardView(userService).getView());
    }

    private void showSidebar() {
        refreshSidebar();
        if (!sidebar.isVisible()) {
            sidebar.setVisible(true);
            sidebar.setManaged(true);
            rootPane.setLeft(sidebar);
        }
    }
}
