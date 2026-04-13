package com.coach;

import atlantafx.base.theme.PrimerDark;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

public class CoachApplication extends Application {

    @Override
    public void start(Stage stage) {
        // Set modern theme using AtlantaFX
        Application.setUserAgentStylesheet(new PrimerDark().getUserAgentStylesheet());

        Scene scene = new Scene(new StackPane(), 1024, 768);
        scene.getStylesheets().add(getClass().getResource("/com/coach/css/obsidian-kinetic.css").toExternalForm());
        
        // Initialize the Router/ViewManager
        com.coach.ui.view.ViewManager.init(scene);

        stage.setTitle("Coach Intelligent de Productivité");
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}
