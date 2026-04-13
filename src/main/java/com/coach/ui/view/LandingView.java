package com.coach.ui.view;

import atlantafx.base.theme.Styles;
import javafx.animation.FadeTransition;
import javafx.animation.ParallelTransition;
import javafx.animation.TranslateTransition;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.*;
import javafx.util.Duration;

public class LandingView {

    private final ViewManager viewManager;
    private final ScrollPane root;

    public LandingView(ViewManager viewManager) {
        this.viewManager = viewManager;
        
        VBox content = new VBox(0);
        content.setStyle("-fx-background-color: -color-bg-default;");
        content.setAlignment(Pos.TOP_CENTER);
        
        Node hero = createHeroSection();
        Node features = createFeaturesSection();
        
        content.getChildren().addAll(createNavbar(), hero, features);

        this.root = new ScrollPane(content);
        root.setFitToWidth(true);
        root.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        root.setStyle("-fx-background-color: transparent; -fx-background: -color-bg-default;");

        // Entrance Animation
        playEntranceAnimation(hero, features);
    }

    private void playEntranceAnimation(Node hero, Node features) {
        // Hero animation
        hero.setOpacity(0);
        FadeTransition fadeInHero = new FadeTransition(Duration.millis(1200), hero);
        fadeInHero.setFromValue(0);
        fadeInHero.setToValue(1);

        TranslateTransition moveUpHero = new TranslateTransition(Duration.millis(1200), hero);
        moveUpHero.setFromY(40);
        moveUpHero.setToY(0);

        // Features animation
        features.setOpacity(0);
        FadeTransition fadeInFeatures = new FadeTransition(Duration.millis(1200), features);
        fadeInFeatures.setFromValue(0);
        fadeInFeatures.setToValue(1);
        fadeInFeatures.setDelay(Duration.millis(400));

        ParallelTransition pt = new ParallelTransition(fadeInHero, moveUpHero, fadeInFeatures);
        pt.play();
    }

    private Node createNavbar() {
        HBox navbar = new HBox(30);
        navbar.setPadding(new Insets(20, 60, 20, 60));
        navbar.setAlignment(Pos.CENTER_LEFT);
        navbar.setPrefHeight(80);
        navbar.getStyleClass().add("glass-panel");

        Label logo = new Label("TASKFLOW");
        logo.setStyle("-fx-font-size: 24px; -fx-font-weight: 900; -fx-text-fill: -color-accent-emphasis;");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);


        Button loginBtn = new Button("Login");
        loginBtn.getStyleClass().addAll(Styles.FLAT, "nav-link");
        loginBtn.setOnAction(e -> viewManager.navigateToAuth());

        Button getStartedBtn = new Button("Register");
        getStartedBtn.getStyleClass().add("button-gradient");
        getStartedBtn.setOnAction(e -> viewManager.navigateToRegister());

        navbar.getChildren().addAll(logo, spacer, loginBtn, getStartedBtn);
        return navbar;
    }

    private Node createHeroSection() {
        VBox hero = new VBox(25);
        hero.setPadding(new Insets(100, 60, 100, 60));
        hero.setAlignment(Pos.CENTER);
        hero.setMaxWidth(900);

        Label title = new Label("Master Your Day,\nOne Task at a Time.");
        title.getStyleClass().add("hero-title");
        title.setAlignment(Pos.CENTER);
        title.setTextAlignment(javafx.scene.text.TextAlignment.CENTER);

        Label subtitle = new Label("Experience the power of the most advanced JavaFX productivity suite.\nFast, secure, and cross-platform.");
        subtitle.getStyleClass().add("hero-subtitle");
        subtitle.setAlignment(Pos.CENTER);
        subtitle.setTextAlignment(javafx.scene.text.TextAlignment.CENTER);

        HBox ctas = new HBox(20);
        ctas.setAlignment(Pos.CENTER);
        
        Button mainCta = new Button("Get Started for Free");
        mainCta.getStyleClass().add("button-gradient");
        mainCta.setPrefHeight(50);
        mainCta.setPrefWidth(220);
        mainCta.setOnAction(e -> viewManager.navigateToRegister());

        ctas.getChildren().addAll(mainCta);

        hero.getChildren().addAll(title, subtitle, ctas);
        return hero;
    }

    private Node createFeaturesSection() {
        HBox features = new HBox(30);
        features.setPadding(new Insets(60));
        features.setAlignment(Pos.CENTER);

        features.getChildren().addAll(
            createFeatureCard("Smart Scheduling", "AI-driven priority management."),
            createFeatureCard("Real-time Sync", "Keep your tasks in sync across all devices."),
            createFeatureCard("JavaFX Power", "Native performance with a modern desktop UI.")
        );

        return features;
    }

    private Node createFeatureCard(String title, String desc) {
        VBox card = new VBox(15);
        card.setPadding(new Insets(30));
        card.setPrefSize(280, 200);
        card.getStyleClass().add("glass-panel");

        Label t = new Label(title);
        t.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: white;");
        
        Label d = new Label(desc);
        d.setWrapText(true);
        d.setStyle("-fx-text-fill: #adaaaa;");

        card.getChildren().addAll(t, d);
        return card;
    }

    public Node getView() {
        return root;
    }
}
