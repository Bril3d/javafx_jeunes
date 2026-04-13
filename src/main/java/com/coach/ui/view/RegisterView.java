package com.coach.ui.view;

import atlantafx.base.theme.Styles;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;

public class RegisterView {

    private final ViewManager viewManager;
    private final VBox root;

    public RegisterView(ViewManager viewManager) {
        this.viewManager = viewManager;
        this.root = new VBox(20);
        initView();
    }

    private void initView() {
        root.setAlignment(Pos.CENTER);
        root.setPadding(new Insets(40));
        root.setSpacing(20);
        root.setMaxWidth(450);
        root.getStyleClass().add("glass-panel");

        Label title = new Label("CREATE ACCOUNT");
        title.setStyle("-fx-font-size: 20px; -fx-font-weight: 900; -fx-text-fill: white; -fx-letter-spacing: 2px;");

        TextField usernameField = new TextField();
        usernameField.setPromptText("Username");
        usernameField.setPrefHeight(45);

        TextField emailField = new TextField();
        emailField.setPromptText("Email Address");
        emailField.setPrefHeight(45);

        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("Password");
        passwordField.setPrefHeight(45);

        PasswordField confirmPasswordField = new PasswordField();
        confirmPasswordField.setPromptText("Confirm Password");
        confirmPasswordField.setPrefHeight(45);

        Button registerBtn = new Button("Register Account");
        registerBtn.getStyleClass().add("button-gradient");
        registerBtn.setMaxWidth(Double.MAX_VALUE);
        registerBtn.setPrefHeight(45);

        Label feedbackLabel = new Label();
        feedbackLabel.setVisible(false);
        feedbackLabel.setWrapText(true);

        registerBtn.setOnAction(e -> {
            String username = usernameField.getText();
            String email = emailField.getText();
            String pass = passwordField.getText();
            String confirm = confirmPasswordField.getText();

            if (username.isEmpty() || email.isEmpty() || pass.isEmpty()) {
                showFeedback("All fields are required!", true);
                return;
            }

            if (!pass.equals(confirm)) {
                showFeedback("Passwords do not match!", true);
                return;
            }

            boolean success = viewManager.getUserService().register(username, email, pass);
            if (success) {
                showFeedback("Registration successful! Redirecting to login...", false);
                // Simple delay simulation or just let user click back
                registerBtn.setDisable(true);
            } else {
                showFeedback("Username or Email already exists!", true);
            }
        });

        Button loginLink = new Button("Already have an account? Login");
        loginLink.getStyleClass().addAll(Styles.FLAT, Styles.SMALL);
        loginLink.setStyle("-fx-text-fill: -color-accent-emphasis;");
        loginLink.setOnAction(e -> viewManager.navigateToAuth());

        Button backBtn = new Button("← Back to Home");
        backBtn.getStyleClass().addAll(Styles.FLAT, Styles.SMALL);
        backBtn.setStyle("-fx-text-fill: white; -fx-opacity: 0.7;");
        backBtn.setOnAction(e -> viewManager.navigateToLanding());

        root.getChildren().addAll(title, usernameField, emailField, passwordField, confirmPasswordField, feedbackLabel, registerBtn, loginLink, backBtn);
    }

    private void showFeedback(String message, boolean isError) {
        Label feedbackLabel = (Label) root.getChildren().get(5); // feedbackLabel index
        feedbackLabel.setText(message);
        feedbackLabel.getStyleClass().removeAll(Styles.DANGER, Styles.SUCCESS);
        feedbackLabel.getStyleClass().add(isError ? Styles.DANGER : Styles.SUCCESS);
        feedbackLabel.setVisible(true);
    }

    public Node getView() {
        VBox container = new VBox(root);
        container.setAlignment(Pos.CENTER);
        container.setStyle("-fx-background-color: -color-bg-subtle;");
        return container;
    }
}
