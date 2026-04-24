package com.coach.ui.view;

import atlantafx.base.theme.Styles;
import com.coach.model.User;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.scene.layout.HBox;

public class ProfileView {

    private final ViewManager viewManager;
    private final VBox root;

    public ProfileView(ViewManager viewManager) {
        this.viewManager = viewManager;
        this.root = new VBox(20);
        initView();
    }

    private void initView() {
        root.setAlignment(Pos.TOP_CENTER);
        root.setPadding(new Insets(40));
        root.setSpacing(25);
        root.setMaxWidth(600);
        root.getStyleClass().add("glass-panel");

        User currentUser = viewManager.getUserService().getCurrentUser();

        Label title = new Label("MY PROFILE & GOALS");
        title.getStyleClass().add(Styles.TITLE_2);
        title.setStyle("-fx-text-fill: -color-accent-emphasis;");

        VBox goalsSection = new VBox(10);
        Label goalsLabel = new Label("My Main Goals");
        goalsLabel.getStyleClass().add(Styles.TEXT_BOLD);
        TextArea goalsArea = new TextArea(currentUser != null ? currentUser.getGoals() : "");
        goalsArea.setPromptText("What are you trying to achieve? (e.g., Learn Java, Run a marathon...)");
        goalsArea.setPrefHeight(100);
        goalsSection.getChildren().addAll(goalsLabel, goalsArea);

        VBox rhythmSection = new VBox(10);
        Label rhythmLabel = new Label("Ideal Work Rhythm");
        rhythmLabel.getStyleClass().add(Styles.TEXT_BOLD);
        ComboBox<String> rhythmCombo = new ComboBox<>();
        rhythmCombo.getItems().addAll("Morning Person", "Evening Person", "Night Owl", "Flexible", "Deep Work Specialist");
        if (currentUser != null && currentUser.getWorkRhythm() != null) {
            rhythmCombo.setValue(currentUser.getWorkRhythm());
        } else {
            rhythmCombo.setValue("Flexible");
        }
        rhythmCombo.setMaxWidth(Double.MAX_VALUE);
        rhythmSection.getChildren().addAll(rhythmLabel, rhythmCombo);

        VBox preferencesSection = new VBox(10);
        Label prefsLabel = new Label("Coach Preferences");
        prefsLabel.getStyleClass().add(Styles.TEXT_BOLD);
        TextArea prefsArea = new TextArea(currentUser != null ? currentUser.getPreferences() : "");
        prefsArea.setPromptText("How do you like to be coached? (e.g., strict, encouraging, direct...)");
        prefsArea.setPrefHeight(100);
        preferencesSection.getChildren().addAll(prefsLabel, prefsArea);

        Button saveBtn = new Button("Update Profile");
        saveBtn.getStyleClass().addAll(Styles.LARGE, Styles.ACCENT);
        saveBtn.setMaxWidth(Double.MAX_VALUE);
        saveBtn.setPrefHeight(45);

        Label feedbackLabel = new Label();
        feedbackLabel.setVisible(false);

        saveBtn.setOnAction(e -> {
            boolean success = viewManager.getUserService().updateProfile(
                    goalsArea.getText(),
                    rhythmCombo.getValue(),
                    prefsArea.getText()
            );
            if (success) {
                feedbackLabel.setText("Profile updated successfully!");
                feedbackLabel.getStyleClass().setAll(Styles.SUCCESS);
                feedbackLabel.setVisible(true);
            } else {
                feedbackLabel.setText("Failed to update profile.");
                feedbackLabel.getStyleClass().setAll(Styles.DANGER);
                feedbackLabel.setVisible(true);
            }
        });

        root.getChildren().addAll(title, goalsSection, rhythmSection, preferencesSection, feedbackLabel, saveBtn);
    }

    public Node getView() {
        VBox container = new VBox(root);
        container.setAlignment(Pos.CENTER);
        container.setPadding(new Insets(20));
        return container;
    }
}
