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
        root.setMaxWidth(400);
        root.setMaxHeight(500);
        root.getStyleClass().add(Styles.ELEVATED_2); // Card effect
        root.setStyle("-fx-background-color: -color-bg-default; -fx-background-radius: 10px;");

        Label title = new Label("Welcome Back");
        title.getStyleClass().add(Styles.TITLE_2);

        TextField usernameField = new TextField();
        usernameField.setPromptText("Username");

        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("Password");

        Button loginBtn = new Button("Login");
        loginBtn.getStyleClass().addAll(Styles.ACCENT);
        loginBtn.setMaxWidth(Double.MAX_VALUE);

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

        // Simplified: Add a register button
        Button registerBtn = new Button("Register New Account");
        registerBtn.getStyleClass().addAll(Styles.FLAT);
        registerBtn.setMaxWidth(Double.MAX_VALUE);
        registerBtn.setOnAction(e -> {
            boolean success = viewManager.getUserService().register(usernameField.getText(), usernameField.getText() + "@coach.com", passwordField.getText());
            if (success) {
                errorLabel.getStyleClass().remove(Styles.DANGER);
                errorLabel.getStyleClass().add(Styles.SUCCESS);
                errorLabel.setText("Registration successful! You can now login.");
                errorLabel.setVisible(true);
            } else {
                errorLabel.getStyleClass().add(Styles.DANGER);
                errorLabel.setText("Username already exists!");
                errorLabel.setVisible(true);
            }
        });

        root.getChildren().addAll(title, usernameField, passwordField, errorLabel, loginBtn, registerBtn);
    }

    public Node getView() {
        // Return centered in a StackPane so it acts like a modal
        VBox container = new VBox(root);
        container.setAlignment(Pos.CENTER);
        container.setStyle("-fx-background-color: -color-bg-subtle;");
        return container;
    }
}
