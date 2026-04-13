package com.coach.ui.view;

import atlantafx.base.theme.Styles;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;

public class AuthView {

    private final ViewManager viewManager;
    private final VBox root;

    public AuthView(ViewManager viewManager) {
        this.viewManager = viewManager;
        this.root = new VBox(20);
        initView();
    }

    private void initView() {
        root.setAlignment(Pos.CENTER);
        root.setPadding(new Insets(40));
        root.setSpacing(25);
        root.setMaxWidth(420);
        root.getStyleClass().add("glass-panel");

        Label title = new Label("SIGN IN");
        title.setStyle("-fx-font-size: 20px; -fx-font-weight: 900; -fx-text-fill: white; -fx-letter-spacing: 2px;");

        TextField usernameField = new TextField();
        usernameField.setPromptText("Username or Email");
        usernameField.setPrefHeight(45);

        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("Password");
        passwordField.setPrefHeight(45);

        Button loginBtn = new Button("Login to TaskFlow");
        loginBtn.getStyleClass().add("button-gradient");
        loginBtn.setMaxWidth(Double.MAX_VALUE);
        loginBtn.setPrefHeight(45);

        Label errorLabel = new Label();
        errorLabel.getStyleClass().add(Styles.DANGER);
        errorLabel.setVisible(false);

        loginBtn.setOnAction(e -> {
            boolean success = viewManager.getUserService().login(usernameField.getText(), passwordField.getText());
            if (success) {
                viewManager.navigateToDashboard();
            } else {
                errorLabel.setText("Invalid credentials!");
                errorLabel.setVisible(true);
            }
        });

        Button registerLink = new Button("Don't have an account? Sign Up");
        registerLink.getStyleClass().addAll(Styles.FLAT, Styles.SMALL);
        registerLink.setStyle("-fx-text-fill: -color-accent-emphasis;");
        registerLink.setOnAction(e -> viewManager.navigateToRegister());

        Button backBtn = new Button("← Back to Home");
        backBtn.getStyleClass().addAll(Styles.FLAT, Styles.SMALL);
        backBtn.setStyle("-fx-text-fill: white; -fx-opacity: 0.7;");
        backBtn.setOnAction(e -> viewManager.navigateToLanding());

        root.getChildren().addAll(title, usernameField, passwordField, errorLabel, loginBtn, registerLink, backBtn);
    }

    public Node getView() {
        // Return centered in a StackPane so it acts like a modal
        VBox container = new VBox(root);
        container.setAlignment(Pos.CENTER);
        container.setStyle("-fx-background-color: -color-bg-subtle;");
        return container;
    }
}
