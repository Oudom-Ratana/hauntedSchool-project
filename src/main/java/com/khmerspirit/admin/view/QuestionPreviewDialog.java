package com.khmerspirit.admin.view;

import com.khmerspirit.admin.model.QuestionModel;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.*;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

/**
 * Modal dialog displaying an exact horror-themed preview of how players experience questions in-game.
 */
public class QuestionPreviewDialog {

    public static void showPreview(QuestionModel question) {
        if (question == null) return;

        Stage stage = new Stage();
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.initStyle(StageStyle.TRANSPARENT);
        stage.setTitle("Player View Preview");

        BorderPane container = new BorderPane();
        container.setPadding(new Insets(24));
        container.setPrefSize(740, 540);
        container.setStyle("-fx-background-color: rgba(10, 14, 22, 0.96); -fx-border-color: rgba(225, 29, 72, 0.4); -fx-border-width: 1px; -fx-background-radius: 12px; -fx-border-radius: 12px; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.9), 30, 0.6, 0, 0);");

        // Header with badges
        HBox header = new HBox(12);
        header.setAlignment(Pos.CENTER_LEFT);
        header.setPadding(new Insets(0, 0, 16, 0));

        Label previewTitle = new Label("IN-GAME PLAYER QUIZ PREVIEW");
        previewTitle.setStyle("-fx-font-family: 'Georgia', serif; -fx-font-size: 18px; -fx-font-weight: 900; -fx-text-fill: #f8fafc;");
        HBox.setHgrow(previewTitle, Priority.ALWAYS);

        Label roomBadge = createBadge("Room: " + (question.getRoom() != null ? question.getRoom() : "General"), "rgba(30, 41, 59, 0.8)", "#94a3b8");
        Label categoryBadge = createBadge("Category: " + (question.getCategory() != null ? question.getCategory() : "General"), "rgba(30, 41, 59, 0.8)", "#cbd5e1");
        Label difficultyBadge = createBadge("Difficulty: " + (question.getDifficulty() != null ? question.getDifficulty() : "Medium"), 
                "Hard".equalsIgnoreCase(question.getDifficulty()) ? "rgba(159, 18, 57, 0.4)" : ("Easy".equalsIgnoreCase(question.getDifficulty()) ? "rgba(16, 185, 129, 0.2)" : "rgba(245, 158, 11, 0.2)"),
                "Hard".equalsIgnoreCase(question.getDifficulty()) ? "#f43f5e" : ("Easy".equalsIgnoreCase(question.getDifficulty()) ? "#34d399" : "#fbbf24"));

        header.getChildren().addAll(previewTitle, roomBadge, categoryBadge, difficultyBadge);
        container.setTop(header);

        // Center content area: Question box & Options
        VBox centerContent = new VBox(16);
        centerContent.setAlignment(Pos.TOP_LEFT);

        VBox questionBox = new VBox(8);
        questionBox.setPadding(new Insets(16));
        questionBox.setStyle("-fx-background-color: rgba(15, 23, 42, 0.92); -fx-border-color: rgba(225, 29, 72, 0.3); -fx-border-width: 1px; -fx-border-radius: 8px; -fx-background-radius: 8px;");

        Label questionPromptLabel = new Label("Q: " + question.getText());
        questionPromptLabel.setWrapText(true);
        questionPromptLabel.setStyle("-fx-font-family: 'Georgia', serif; -fx-font-size: 17px; -fx-font-weight: bold; -fx-text-fill: #f1f5f9;");
        questionBox.getChildren().add(questionPromptLabel);

        VBox optionsList = new VBox(10);
        optionsList.getChildren().addAll(
                createOptionCard("1 (A)", question.getOptionA(), "A".equalsIgnoreCase(question.getCorrectAnswer())),
                createOptionCard("2 (B)", question.getOptionB(), "B".equalsIgnoreCase(question.getCorrectAnswer())),
                createOptionCard("3 (C)", question.getOptionC(), "C".equalsIgnoreCase(question.getCorrectAnswer())),
                createOptionCard("4 (D)", question.getOptionD(), "D".equalsIgnoreCase(question.getCorrectAnswer()))
        );

        // Explanation / Clue panel
        VBox explanationBox = new VBox(4);
        explanationBox.setPadding(new Insets(12));
        explanationBox.setStyle("-fx-background-color: rgba(16, 185, 129, 0.1); -fx-border-color: rgba(52, 211, 153, 0.3); -fx-border-width: 1px; -fx-background-radius: 6px;");

        Label expTitle = new Label("Educational Explanation / Clue:");
        expTitle.setStyle("-fx-font-size: 12px; -fx-font-weight: bold; -fx-text-fill: #34d399;");
        Label expText = new Label(question.getExplanation() != null && !question.getExplanation().isBlank() ? question.getExplanation() : "No explanation provided.");
        expText.setWrapText(true);
        expText.setStyle("-fx-font-size: 13px; -fx-text-fill: #cbd5e1;");
        explanationBox.getChildren().addAll(expTitle, expText);

        centerContent.getChildren().addAll(questionBox, optionsList, explanationBox);
        container.setCenter(centerContent);

        // Bottom rule banner & Close button
        HBox footer = new HBox(16);
        footer.setAlignment(Pos.CENTER_RIGHT);
        footer.setPadding(new Insets(16, 0, 0, 0));

        Label ruleBanner = new Label("Rule: 5 correct = unlock room & reward | 1 wrong = ghost chases player!");
        ruleBanner.setStyle("-fx-font-size: 12px; -fx-text-fill: #f43f5e; -fx-font-weight: bold;");
        HBox.setHgrow(ruleBanner, Priority.ALWAYS);

        Button closeBtn = new Button("CLOSE PREVIEW");
        closeBtn.setStyle("-fx-background-color: linear-gradient(to bottom, #9f1239, #4c0519); -fx-text-fill: #f8fafc; -fx-font-weight: 900; -fx-padding: 8 20 8 20; -fx-cursor: hand; -fx-border-color: #e11d48; -fx-border-width: 1px; -fx-background-radius: 6px;");
        closeBtn.setOnAction(e -> stage.close());
        closeBtn.setOnAction(e -> stage.close());

        footer.getChildren().addAll(ruleBanner, closeBtn);
        container.setBottom(footer);

        Scene scene = new Scene(container);
        scene.setFill(null);
        stage.setScene(scene);
        stage.showAndWait();
    }

    private static Label createBadge(String text, String bgColor, String textColor) {
        Label badge = new Label(text);
        badge.setStyle(String.format("-fx-background-color: %s; -fx-text-fill: %s; -fx-font-size: 11px; -fx-font-weight: bold; -fx-padding: 4 8 4 8; -fx-background-radius: 10px; -fx-border-color: %s; -fx-border-width: 1px;", bgColor, textColor, textColor));
        return badge;
    }

    private static HBox createOptionCard(String key, String text, boolean isCorrect) {
        HBox card = new HBox(12);
        card.setAlignment(Pos.CENTER_LEFT);
        card.setPadding(new Insets(10, 14, 10, 14));

        String style = isCorrect ? 
                "-fx-background-color: rgba(20, 60, 30, 0.9); -fx-border-color: #6BCB77; -fx-border-width: 1.5px; -fx-background-radius: 6px; -fx-border-radius: 6px;" :
                "-fx-background-color: rgba(14, 6, 8, 0.85); -fx-border-color: #4A1212; -fx-border-width: 1px; -fx-background-radius: 6px; -fx-border-radius: 6px;";

        card.setStyle(style);

        Label keyBadge = new Label("[" + key + "]");
        keyBadge.setStyle(isCorrect ? "-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #6BCB77;" : "-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #D4AF37;");

        Label optionText = new Label(text != null ? text : "");
        optionText.setStyle("-fx-font-size: 14px; -fx-text-fill: #F0DFB7;");
        HBox.setHgrow(optionText, Priority.ALWAYS);

        card.getChildren().addAll(keyBadge, optionText);

        if (isCorrect) {
            Label correctTag = new Label("✓ CORRECT ANSWER");
            correctTag.setStyle("-fx-font-size: 11px; -fx-font-weight: bold; -fx-text-fill: #6BCB77;");
            card.getChildren().add(correctTag);
        }

        return card;
    }
}
