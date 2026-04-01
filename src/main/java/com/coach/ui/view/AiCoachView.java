package com.coach.ui.view;

import atlantafx.base.theme.Styles;
import com.coach.service.AiService;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import com.coach.ui.view.components.ChatBubble;

public class AiCoachView {

    private final ViewManager viewManager;
    private final AiService aiService;
    private final VBox root;

    public AiCoachView(ViewManager viewManager) {
        this.viewManager = viewManager;
        this.aiService = new AiService();
        this.root = new VBox(20);
        initView();
    }

    private void initView() {
        root.setPadding(new Insets(30));

        Label header = new Label("🤖 AI Coach");
        header.getStyleClass().add(Styles.TITLE_1);

        Label subHeader = new Label("Ask the coach to break down tasks, reformulate objectives, or provide productivity advice.");
        subHeader.getStyleClass().add(Styles.TEXT_MUTED);

        VBox chatContainer = new VBox(15);
        chatContainer.setPadding(new Insets(10));
        
        ScrollPane chatScroll = new ScrollPane(chatContainer);
        chatScroll.setFitToWidth(true);
        chatScroll.setStyle("-fx-background-color: transparent;");
        VBox.setVgrow(chatScroll, Priority.ALWAYS);

        TextField inputField = new TextField();
        inputField.setPromptText("E.g., How can I stop procrastinating on my math homework?");
        HBox.setHgrow(inputField, Priority.ALWAYS);

        Button sendBtn = new Button("Ask");
        sendBtn.getStyleClass().addAll(Styles.BUTTON_OUTLINED, Styles.ACCENT);
        
        ProgressIndicator loading = new ProgressIndicator();
        loading.setMaxSize(24, 24);
        loading.setVisible(false);

        HBox inputParent = new HBox(10, inputField, sendBtn, loading);
        inputParent.setAlignment(Pos.CENTER_LEFT);

        sendBtn.setOnAction(e -> {
            String question = inputField.getText().trim();
            if (question.isEmpty()) return;

            chatContainer.getChildren().add(new ChatBubble(true, question));
            Platform.runLater(() -> chatScroll.setVvalue(1.0));
            inputField.clear();
            
            loading.setVisible(true);
            sendBtn.setDisable(true);

            // Execute AI request in a background thread
            new Thread(() -> {
                int userId = viewManager.getUserService().getCurrentUser().getId();
                String response = aiService.getAdvice(userId, question);
                Platform.runLater(() -> {
                    chatContainer.getChildren().add(new ChatBubble(false, response));
                    chatScroll.setVvalue(1.0);
                    loading.setVisible(false);
                    sendBtn.setDisable(false);
                });
            }).start();
        });

        root.getChildren().addAll(header, subHeader, chatScroll, inputParent);
    }

    public Node getView() {
        return root;
    }
}
