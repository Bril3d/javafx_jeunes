package com.coach.ui.view.components;

import org.commonmark.node.*;
import org.commonmark.parser.Parser;
import javafx.scene.layout.VBox;
import org.commonmark.node.Text;
import javafx.scene.text.TextFlow;
import javafx.scene.layout.StackPane;
import javafx.scene.control.Label;
import javafx.geometry.Insets;
import java.util.Stack;

public class MarkdownView extends VBox {

    public MarkdownView(String markdown) {
        this.setSpacing(10);
        Parser parser = Parser.builder().build();
        Node document = parser.parse(markdown);
        MarkdownVisitor visitor = new MarkdownVisitor(this);
        document.accept(visitor);
    }

    private static class MarkdownVisitor extends AbstractVisitor {
        private final Stack<VBox> containerStack = new Stack<>();
        private TextFlow currentTextFlow;

        public MarkdownVisitor(VBox root) {
            containerStack.push(root);
        }

        private VBox currentContainer() {
            return containerStack.peek();
        }

        @Override
        public void visit(Paragraph paragraph) {
            TextFlow oldFlow = currentTextFlow;
            currentTextFlow = new TextFlow();
            visitChildren(paragraph);
            currentContainer().getChildren().add(currentTextFlow);
            currentTextFlow = oldFlow;
        }

        @Override
        public void visit(Text text) {
            if (currentTextFlow != null) {
                javafx.scene.text.Text fxText = new javafx.scene.text.Text(text.getLiteral());
                currentTextFlow.getChildren().add(fxText);
            } else {
                visitChildren(text);
            }
        }

        @Override
        public void visit(StrongEmphasis strongEmphasis) {
            if (currentTextFlow != null) {
                int startSize = currentTextFlow.getChildren().size();
                visitChildren(strongEmphasis);
                for (int i = startSize; i < currentTextFlow.getChildren().size(); i++) {
                    javafx.scene.Node node = currentTextFlow.getChildren().get(i);
                    if (node instanceof javafx.scene.text.Text) {
                        node.setStyle("-fx-font-weight: bold;");
                    }
                }
            }
        }

        @Override
        public void visit(Emphasis emphasis) {
            if (currentTextFlow != null) {
                int startSize = currentTextFlow.getChildren().size();
                visitChildren(emphasis);
                for (int i = startSize; i < currentTextFlow.getChildren().size(); i++) {
                    javafx.scene.Node node = currentTextFlow.getChildren().get(i);
                    if (node instanceof javafx.scene.text.Text) {
                        node.setStyle("-fx-font-style: italic;");
                    }
                }
            }
        }

        @Override
        public void visit(FencedCodeBlock fencedCodeBlock) {
            Label codeLabel = new Label(fencedCodeBlock.getLiteral());
            codeLabel.setWrapText(true);
            codeLabel.setStyle("-fx-font-family: monospace;");
            
            StackPane codeContainer = new StackPane(codeLabel);
            codeContainer.setStyle("-fx-background-color: -color-bg-subtle; -fx-background-radius: 4;");
            codeContainer.setPadding(new Insets(10));
            
            currentContainer().getChildren().add(codeContainer);
        }
        
        @Override
        public void visit(Code code) {
            if (currentTextFlow != null) {
                javafx.scene.text.Text fxText = new javafx.scene.text.Text(code.getLiteral());
                fxText.setStyle("-fx-font-family: monospace; -fx-fill: -color-accent-emphasis;");
                currentTextFlow.getChildren().add(fxText);
            }
        }

        @Override
        public void visit(BulletList bulletList) {
            VBox listContainer = new VBox(5);
            listContainer.setPadding(new Insets(0, 0, 0, 20));
            currentContainer().getChildren().add(listContainer);
            
            containerStack.push(listContainer);
            visitChildren(bulletList);
            containerStack.pop();
        }

        @Override
        public void visit(OrderedList orderedList) {
            VBox listContainer = new VBox(5);
            listContainer.setPadding(new Insets(0, 0, 0, 20));
            currentContainer().getChildren().add(listContainer);
            
            containerStack.push(listContainer);
            // We should ideally track the order number, but bullets are fine for simplicity
            visitChildren(orderedList);
            containerStack.pop();
        }

        @Override
        public void visit(ListItem listItem) {
            javafx.scene.layout.HBox hbox = new javafx.scene.layout.HBox(5);
            javafx.scene.text.Text bullet = new javafx.scene.text.Text("• ");
            
            VBox itemContent = new VBox();
            hbox.getChildren().addAll(bullet, itemContent);
            currentContainer().getChildren().add(hbox);
            
            containerStack.push(itemContent);
            visitChildren(listItem);
            containerStack.pop();
        }
    }
}
