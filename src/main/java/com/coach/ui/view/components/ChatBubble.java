package com.coach.ui.view.components;

import atlantafx.base.theme.Styles;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import org.kordamp.ikonli.feather.Feather;
import org.kordamp.ikonli.javafx.FontIcon;
import javafx.scene.Node;

public class ChatBubble extends HBox {

    public ChatBubble(boolean isUser, String text) {
        this.setSpacing(15);
        this.setPadding(new Insets(10, 15, 10, 15));
        
        FontIcon icon = new FontIcon(isUser ? Feather.USER : Feather.CPU);
        icon.setIconSize(24);
        icon.getStyleClass().add(isUser ? Styles.ACCENT : Styles.SUCCESS);
        
        VBox iconContainer = new VBox(icon);
        iconContainer.setAlignment(Pos.TOP_CENTER);
        iconContainer.setPadding(new Insets(5, 0, 0, 0));

        VBox messageContainer = new VBox(5);
        Label nameLabel = new Label(isUser ? "You" : "Coach");
        nameLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: -color-fg-muted;");
        
        Node contentNode;
        if (isUser) {
            Label textLabel = new Label(text);
            textLabel.setWrapText(true);
            contentNode = textLabel;
        } else {
            contentNode = new MarkdownView(text);
        }
        
        messageContainer.getChildren().addAll(nameLabel, contentNode);
        
        // Let message container grow
        HBox.setHgrow(messageContainer, Priority.ALWAYS);
        
        if (isUser) {
            this.setAlignment(Pos.TOP_RIGHT);
            this.getChildren().addAll(messageContainer, iconContainer);
            this.getStyleClass().add("chat-bubble-user");
        } else {
            this.setAlignment(Pos.TOP_LEFT);
            this.getChildren().addAll(iconContainer, messageContainer);
            this.getStyleClass().add("chat-bubble-ai");
        }
    }
}
