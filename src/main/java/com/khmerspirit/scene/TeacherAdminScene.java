package com.khmerspirit.scene;

import com.khmerspirit.config.Constants;
import com.khmerspirit.core.SceneManager;
import com.khmerspirit.map.MapLoader;
import com.khmerspirit.map.Room;
import com.khmerspirit.education.Question;
import com.khmerspirit.education.QuestionLoader;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class TeacherAdminScene {

    private boolean loggedIn = false;
    private ListView<Room> roomList;
    private ListView<String> questionList;
    private TextArea questionText;
    private TextField optA, optB, optC, optD, rewardField;
    private ToggleGroup answerGroup;
    private ComboBox<String> levelBox;
    private final QuestionLoader loader = new QuestionLoader();

    public Scene createScene() {
        BorderPane root = new BorderPane();
        root.getStyleClass().add("screen-root");

        if (!loggedIn) {
            return createLoginScene(root);
        } else {
            return createAdminDashboardScene(root);
        }
    }

    private Scene createLoginScene(BorderPane root) {
        root.getChildren().clear();

        VBox loginCard = new VBox(16);
        loginCard.setAlignment(Pos.CENTER);
        loginCard.setPadding(new Insets(32));
        loginCard.setMaxWidth(420);
        loginCard.getStyleClass().add("character-card");

        Label title = new Label("TEACHER ADMIN LOGIN");
        title.getStyleClass().add("card-title");

        Label subtitle = new Label("Authorized Personnel Access Only");
        subtitle.getStyleClass().add("character-description");

        TextField usernameField = new TextField();
        usernameField.setPromptText("Username (e.g. admin)");
        usernameField.setPrefHeight(40);

        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("Password (e.g. admin123)");
        passwordField.setPrefHeight(40);

        Label errorLabel = new Label();
        errorLabel.setStyle("-fx-text-fill: #ff6b6b; -fx-font-weight: bold;");
        errorLabel.setVisible(false);

        Button loginBtn = new Button("LOGIN");
        loginBtn.getStyleClass().add("primary-button");
        loginBtn.setMaxWidth(Double.MAX_VALUE);

        Button backBtn = new Button("BACK TO MENU");
        backBtn.getStyleClass().add("secondary-button");
        backBtn.setMaxWidth(Double.MAX_VALUE);
        backBtn.setOnAction(e -> SceneManager.showMainMenu());

        Runnable performLogin = () -> {
            String user = usernameField.getText().trim();
            String pass = passwordField.getText().trim();

            if (isValidCredentials(user, pass)) {
                loggedIn = true;
                createAdminDashboardScene(root);
            } else {
                errorLabel.setText("Invalid credentials! Default: admin / admin123");
                errorLabel.setVisible(true);
            }
        };

        loginBtn.setOnAction(e -> performLogin.run());
        passwordField.setOnAction(e -> performLogin.run());
        usernameField.setOnAction(e -> performLogin.run());

        Label hintLabel = new Label("Default Admin Credentials: admin / admin123");
        hintLabel.setStyle("-fx-text-fill: #8a9ba8; -fx-font-size: 13px;");

        loginCard.getChildren().addAll(
                title, subtitle,
                new VBox(4, new Label("Username:"), usernameField),
                new VBox(4, new Label("Password:"), passwordField),
                errorLabel,
                loginBtn, backBtn, hintLabel
        );

        StackPane centerContainer = new StackPane(loginCard);
        centerContainer.setAlignment(Pos.CENTER);
        root.setCenter(centerContainer);

        return new Scene(root, Constants.WINDOW_WIDTH, Constants.WINDOW_HEIGHT);
    }

    private boolean isValidCredentials(String user, String pass) {
        if (user.equalsIgnoreCase("admin") && (pass.equals("admin") || pass.equals("admin123") || pass.equals("123456"))) {
            return true;
        }
        if (user.equalsIgnoreCase("teacher") && (pass.equals("teacher") || pass.equals("teacher123"))) {
            return true;
        }
        return false;
    }

    private Scene createAdminDashboardScene(BorderPane root) {
        root.getChildren().clear();

        HBox top = new HBox();
        top.setAlignment(Pos.CENTER_LEFT);
        top.setPadding(new Insets(14, 24, 14, 24));
        top.setSpacing(20);

        Label title = new Label("TEACHER ADMIN PANEL");
        title.getStyleClass().add("section-heading");
        HBox.setHgrow(title, Priority.ALWAYS);

        Button backBtn = new Button("BACK TO MENU");
        backBtn.getStyleClass().add("secondary-button");
        backBtn.setOnAction(e -> SceneManager.showMainMenu());

        top.getChildren().addAll(title, backBtn);
        root.setTop(top);

        GridPane grid = new GridPane();
        grid.setPadding(new Insets(10));
        grid.setHgap(12);
        grid.setVgap(10);

        roomList = new ListView<>();
        List<Room> rooms = MapLoader.class.getResourceAsStream("/unused") == null ? MapLoader.class.getClassLoader().getResourceAsStream("/unused") == null ? new MapLoader().loadAbandonedSchool().getRooms() : new MapLoader().loadAbandonedSchool().getRooms() : new MapLoader().loadAbandonedSchool().getRooms();
        ObservableList<Room> roomItems = FXCollections.observableArrayList(rooms);
        roomList.setItems(roomItems);
        roomList.setPrefWidth(220);
        roomList.getSelectionModel().selectedItemProperty().addListener((obs, o, n) -> onRoomSelected(n));

        questionList = new ListView<>();
        questionList.setPrefWidth(320);
        questionList.getSelectionModel().selectedIndexProperty().addListener((obs, o, n) -> onQuestionSelected(n.intValue()));

        VBox left = new VBox(8, new Label("Rooms"), roomList, new Label("Questions"), questionList);
        left.setPrefWidth(260);
        left.setPadding(new Insets(6));

        questionText = new TextArea();
        questionText.setWrapText(true);
        questionText.setPrefRowCount(3);

        optA = new TextField(); optB = new TextField(); optC = new TextField(); optD = new TextField();
        rewardField = new TextField();
        answerGroup = new ToggleGroup();
        RadioButton ra = new RadioButton("A"); ra.setToggleGroup(answerGroup);
        RadioButton rb = new RadioButton("B"); rb.setToggleGroup(answerGroup);
        RadioButton rc = new RadioButton("C"); rc.setToggleGroup(answerGroup);
        RadioButton rd = new RadioButton("D"); rd.setToggleGroup(answerGroup);

        levelBox = new ComboBox<>(FXCollections.observableArrayList("1","2","3","4","5"));
        levelBox.getSelectionModel().selectFirst();

        GridPane form = new GridPane();
        form.setHgap(8); form.setVgap(8);
        form.add(new Label("Question:"), 0, 0);
        form.add(questionText, 1, 0, 3, 1);
        form.add(new Label("Option A:"), 0, 1); form.add(optA, 1, 1);
        form.add(new Label("Option B:"), 2, 1); form.add(optB, 3, 1);
        form.add(new Label("Option C:"), 0, 2); form.add(optC, 1, 2);
        form.add(new Label("Option D:"), 2, 2); form.add(optD, 3, 2);
        form.add(new Label("Correct:"), 0, 3); HBox answers = new HBox(6, ra, rb, rc, rd); form.add(answers, 1, 3, 3, 1);
        form.add(new Label("Reward:"), 0, 4); form.add(rewardField, 1, 4);
        form.add(new Label("Level:"), 2, 4); form.add(levelBox, 3, 4);

        Button addBtn = new Button("Add Question");
        Button editBtn = new Button("Save Edit");
        Button deleteBtn = new Button("Delete");
        Button saveFilesBtn = new Button("Save to Files");
        HBox actions = new HBox(8, addBtn, editBtn, deleteBtn, saveFilesBtn);
        actions.setAlignment(Pos.CENTER_RIGHT);

        addBtn.setOnAction(e -> onAddQuestion());
        editBtn.setOnAction(e -> onSaveEdit());
        deleteBtn.setOnAction(e -> onDeleteQuestion());
        saveFilesBtn.setOnAction(e -> onSaveFiles());

        VBox center = new VBox(8, form, actions);
        center.setPadding(new Insets(6));

        root.setLeft(left);
        root.setCenter(center);
        BorderPane.setMargin(left, new Insets(8));
        BorderPane.setMargin(center, new Insets(8));

        return new Scene(root, Constants.WINDOW_WIDTH, Constants.WINDOW_HEIGHT);
    }

    private void onRoomSelected(Room room) {
        if (room == null) return;
        List<Question> questions = loader.loadQuestionsForRoom(room.getId());
        List<String> titles = questions.stream().map(q -> q.getText()).collect(Collectors.toList());
        questionList.setItems(FXCollections.observableArrayList(titles));
        try {
            Files.createDirectories(Path.of("questions"));
            Path json = Path.of("questions", room.getId() + ".json");
            if (!Files.exists(json)) {
                Files.writeString(json, "{ \"questions\": [] }", StandardCharsets.UTF_8);
            }
        } catch (IOException ignored) {}
    }

    private void onQuestionSelected(int idx) {
        Room room = roomList.getSelectionModel().getSelectedItem();
        if (room == null) return;
        List<Question> questions = loader.loadQuestionsForRoom(room.getId());
        if (idx < 0 || idx >= questions.size()) return;
        Question q = questions.get(idx);
        questionText.setText(q.getText());
        List<String> opts = q.getOptions();
        optA.setText(opts.size() > 0 ? opts.get(0) : "");
        optB.setText(opts.size() > 1 ? opts.get(1) : "");
        optC.setText(opts.size() > 2 ? opts.get(2) : "");
        optD.setText(opts.size() > 3 ? opts.get(3) : "");
        int ans = q.getAnswerIndex();
        if (ans == 0) answerGroup.selectToggle(answerGroup.getToggles().get(0));
        if (ans == 1) answerGroup.selectToggle(answerGroup.getToggles().get(1));
        if (ans == 2) answerGroup.selectToggle(answerGroup.getToggles().get(2));
        if (ans == 3) answerGroup.selectToggle(answerGroup.getToggles().get(3));
    }

    private void onAddQuestion() {
        Room room = roomList.getSelectionModel().getSelectedItem();
        if (room == null) return;
        String q = questionText.getText().trim();
        if (q.isBlank()) return;
        List<String> opts = new ArrayList<>();
        if (!optA.getText().isBlank()) opts.add(optA.getText().trim());
        if (!optB.getText().isBlank()) opts.add(optB.getText().trim());
        if (!optC.getText().isBlank()) opts.add(optC.getText().trim());
        if (!optD.getText().isBlank()) opts.add(optD.getText().trim());
        int answer = 0;
        Toggle t = answerGroup.getSelectedToggle();
        if (t != null) answer = answerGroup.getToggles().indexOf(t);
        com.khmerspirit.education.Question newQ = new com.khmerspirit.education.Question(q, opts, answer);
        try {
            Path json = Path.of("questions", room.getId() + ".json");
            String content = Files.exists(json) ? Files.readString(json, StandardCharsets.UTF_8) : "{ \"questions\": [] }";
            String item = buildJsonQuestion(newQ);
            int insertAt = content.lastIndexOf(']');
            String out;
            if (insertAt > 0) {
                String before = content.substring(0, insertAt).trim();
                if (before.endsWith("[")) out = before + item + "]" + content.substring(insertAt+1);
                else out = before + ",\n" + item + "]" + content.substring(insertAt+1);
            } else {
                out = "{ \"questions\": [" + item + "] }";
            }
            Files.writeString(json, out, StandardCharsets.UTF_8);
            appendTextQuestion(room.getId(), newQ);
            onRoomSelected(room);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    private void onSaveEdit() {
        Room room = roomList.getSelectionModel().getSelectedItem();
        if (room == null) return;
        List<Question> questions = loader.loadQuestionsForRoom(room.getId());
        int idx = questionList.getSelectionModel().getSelectedIndex();
        if (idx < 0 || idx >= questions.size()) return;
        String qTxt = questionText.getText().trim();
        List<String> opts = new ArrayList<>();
        if (!optA.getText().isBlank()) opts.add(optA.getText().trim());
        if (!optB.getText().isBlank()) opts.add(optB.getText().trim());
        if (!optC.getText().isBlank()) opts.add(optC.getText().trim());
        if (!optD.getText().isBlank()) opts.add(optD.getText().trim());
        int answer = 0;
        Toggle t = answerGroup.getSelectedToggle();
        if (t != null) answer = answerGroup.getToggles().indexOf(t);
        Question q = new Question(qTxt, opts, answer);
        questions.set(idx, q);
        saveQuestionsToFiles(room.getId(), questions);
        onRoomSelected(room);
    }

    private void onDeleteQuestion() {
        Room room = roomList.getSelectionModel().getSelectedItem();
        if (room == null) return;
        List<Question> questions = loader.loadQuestionsForRoom(room.getId());
        int idx = questionList.getSelectionModel().getSelectedIndex();
        if (idx < 0 || idx >= questions.size()) return;
        questions.remove(idx);
        saveQuestionsToFiles(room.getId(), questions);
        onRoomSelected(room);
    }

    private void onSaveFiles() {
        Room room = roomList.getSelectionModel().getSelectedItem();
        if (room == null) return;
        List<Question> questions = loader.loadQuestionsForRoom(room.getId());
        saveQuestionsToFiles(room.getId(), questions);
    }

    private void saveQuestionsToFiles(String roomId, List<Question> questions) {
        try {
            Files.createDirectories(Path.of("questions"));
            StringBuilder sb = new StringBuilder();
            sb.append("{\n  \"questions\": [\n");
            for (int i = 0; i < questions.size(); i++) {
                sb.append("    ").append(buildJsonQuestion(questions.get(i)));
                if (i < questions.size() - 1) sb.append(",\n");
                else sb.append("\n");
            }
            sb.append("  ]\n}");
            Files.writeString(Path.of("questions", roomId + ".json"), sb.toString(), StandardCharsets.UTF_8);

            StringBuilder txt = new StringBuilder();
            for (Question q : questions) {
                txt.append("Q: ").append(q.getText()).append("\n");
                List<String> opts = q.getOptions();
                if (opts.size() > 0) txt.append("A: ").append(opts.get(0)).append("\n");
                if (opts.size() > 1) txt.append("B: ").append(opts.get(1)).append("\n");
                if (opts.size() > 2) txt.append("C: ").append(opts.get(2)).append("\n");
                if (opts.size() > 3) txt.append("D: ").append(opts.get(3)).append("\n");
                txt.append("Answer: ").append(q.getAnswerIndex() + 1).append("\n\n");
            }
            Files.writeString(Path.of("questions", roomId + ".txt"), txt.toString(), StandardCharsets.UTF_8);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    private String buildJsonQuestion(Question q) {
        StringBuilder sb = new StringBuilder();
        sb.append("{\n");
        sb.append("      \"text\": \"").append(escape(q.getText())).append("\",\n");
        sb.append("      \"options\": [");
        List<String> opts = q.getOptions();
        for (int i = 0; i < opts.size(); i++) {
            sb.append("\"").append(escape(opts.get(i))).append("\"");
            if (i < opts.size() - 1) sb.append(", ");
        }
        sb.append("],\n");
        sb.append("      \"answer\": \"").append(Integer.toString(q.getAnswerIndex())).append("\"\n");
        sb.append("    }");
        return sb.toString();
    }

    private String escape(String s) {
        return s.replace("\\", "\\\\").replace("\"", "\\\"");
    }

    private void appendTextQuestion(String roomId, Question q) throws IOException {
        StringBuilder txt = new StringBuilder();
        txt.append("Q: ").append(q.getText()).append("\n");
        List<String> opts = q.getOptions();
        if (opts.size() > 0) txt.append("A: ").append(opts.get(0)).append("\n");
        if (opts.size() > 1) txt.append("B: ").append(opts.get(1)).append("\n");
        if (opts.size() > 2) txt.append("C: ").append(opts.get(2)).append("\n");
        if (opts.size() > 3) txt.append("D: ").append(opts.get(3)).append("\n");
        txt.append("Answer: ").append(q.getAnswerIndex() + 1).append("\n\n");
        Files.writeString(Path.of("questions", roomId + ".txt"), txt.toString(), StandardCharsets.UTF_8, java.nio.file.StandardOpenOption.CREATE, java.nio.file.StandardOpenOption.APPEND);
    }
}
