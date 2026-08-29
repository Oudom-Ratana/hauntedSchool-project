package com.khmerspirit.admin.view;

import com.khmerspirit.admin.model.QuestionModel;
import com.khmerspirit.admin.model.RewardModel;
import com.khmerspirit.admin.model.RoomModel;
import com.khmerspirit.admin.service.QuestionFileService;
import com.khmerspirit.admin.service.RewardFileService;
import com.khmerspirit.admin.service.RoomFileService;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.*;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Horror Dashboard View featuring old rock stone stat cards, crimson blood glow, and room analytics.
 */
public class DashboardView extends VBox {

    private final QuestionFileService questionFileService = new QuestionFileService();
    private final RoomFileService roomFileService = new RoomFileService();
    private final RewardFileService rewardFileService = new RewardFileService();

    public DashboardView() {
        setSpacing(20);
        setPadding(new Insets(24));
        setStyle("-fx-background-color: transparent;");

        refreshDashboard();
    }

    public void refreshDashboard() {
        getChildren().clear();

        // 1. Header Banner
        VBox headerBox = new VBox(4);
        Label headerTitle = new Label("ADMINISTRATIVE DASHBOARD");
        headerTitle.setStyle("-fx-font-family: 'Georgia', serif; -fx-font-size: 26px; -fx-font-weight: 900; -fx-text-fill: #f8fafc; -fx-letter-spacing: 1px; -fx-effect: dropshadow(gaussian, rgba(225, 29, 72, 0.5), 12, 0.3, 0, 0);");
        Label headerSubtitle = new Label("Haunted School Real-Time Overview & Educational Logic Controller");
        headerSubtitle.setStyle("-fx-font-size: 13px; -fx-text-fill: #94a3b8; -fx-font-weight: bold;");
        headerBox.getChildren().addAll(headerTitle, headerSubtitle);
        getChildren().add(headerBox);

        // Fetch data
        List<QuestionModel> questions = questionFileService.loadQuestions();
        List<RoomModel> rooms = roomFileService.loadRooms();
        List<RewardModel> rewards = rewardFileService.loadRewards();

        long totalQs = questions.size();
        long easyQs = questions.stream().filter(q -> "Easy".equalsIgnoreCase(q.getDifficulty())).count();
        long mediumQs = questions.stream().filter(q -> "Medium".equalsIgnoreCase(q.getDifficulty())).count();
        long hardQs = questions.stream().filter(q -> "Hard".equalsIgnoreCase(q.getDifficulty())).count();
        long totalRooms = rooms.size();
        long totalRewards = rewards.size();

        // 2. Stat Cards Grid (2 rows x 3 columns)
        GridPane statGrid = new GridPane();
        statGrid.setHgap(16);
        statGrid.setVgap(16);

        VBox totalCard = createStatCard("TOTAL QUESTIONS", String.valueOf(totalQs), "All active quiz pool items", "#f43f5e");
        VBox easyCard = createStatCard("EASY QUESTIONS", String.valueOf(easyQs), "Novice student level", "#10b981");
        VBox mediumCard = createStatCard("MEDIUM QUESTIONS", String.valueOf(mediumQs), "Intermediate challenges", "#f59e0b");
        VBox hardCard = createStatCard("HARD QUESTIONS", String.valueOf(hardQs), "Deadly difficulty level", "#ef4444");
        VBox roomCard = createStatCard("HAUNTED ROOMS", String.valueOf(totalRooms), "Map location chambers", "#38bdf8");
        VBox rewardCard = createStatCard("TOTAL REWARDS", String.valueOf(totalRewards), "Keys, charms & medical items", "#a855f7");

        statGrid.add(totalCard, 0, 0);
        statGrid.add(easyCard, 1, 0);
        statGrid.add(mediumCard, 2, 0);
        statGrid.add(hardCard, 0, 1);
        statGrid.add(roomCard, 1, 1);
        statGrid.add(rewardCard, 2, 1);

        ColumnConstraints cc = new ColumnConstraints();
        cc.setPercentWidth(33.33);
        cc.setHgrow(Priority.ALWAYS);
        statGrid.getColumnConstraints().addAll(cc, cc, cc);

        getChildren().add(statGrid);

        // 3. Lower Section: Room Distribution & Horror Game Engine Rules
        HBox lowerSection = new HBox(20);
        HBox.setHgrow(lowerSection, Priority.ALWAYS);

        // Left Panel: Questions per Room breakdown
        VBox roomBreakdown = new VBox(14);
        roomBreakdown.setPadding(new Insets(20));
        roomBreakdown.getStyleClass().add("admin-stat-card");
        roomBreakdown.setStyle("-fx-background-color: rgba(12, 16, 24, 0.94); -fx-border-color: rgba(255, 255, 255, 0.08); -fx-border-width: 1px; -fx-background-radius: 10px;");
        HBox.setHgrow(roomBreakdown, Priority.ALWAYS);

        Label breakdownTitle = new Label("QUESTIONS PER ROOM BREAKDOWN");
        breakdownTitle.setStyle("-fx-font-family: 'Georgia', serif; -fx-font-size: 15px; -fx-font-weight: bold; -fx-text-fill: #f1f5f9;");
        roomBreakdown.getChildren().add(breakdownTitle);

        Map<String, Long> qsByRoom = questions.stream()
                .collect(Collectors.groupingBy(q -> q.getRoom() != null ? q.getRoom() : "Unassigned", Collectors.counting()));

        if (rooms.isEmpty()) {
            Label emptyLbl = new Label("No rooms registered.");
            emptyLbl.setStyle("-fx-text-fill: #64748b;");
            roomBreakdown.getChildren().add(emptyLbl);
        } else {
            GridPane roomListGrid = new GridPane();
            roomListGrid.setVgap(10);
            roomListGrid.setHgap(16);
            int row = 0;
            for (RoomModel r : rooms) {
                long count = qsByRoom.getOrDefault(r.getId(), 0L);
                Label rName = new Label(r.getName() + " [" + r.getId() + "]");
                rName.setStyle("-fx-text-fill: #e2e8f0; -fx-font-weight: bold; -fx-font-size: 13px;");

                Label rCount = new Label(count + " / " + r.getRequiredQs() + " required");
                String countStyle = count >= r.getRequiredQs() ? "-fx-text-fill: #10b981; -fx-font-weight: bold; -fx-font-size: 13px;" : "-fx-text-fill: #f43f5e; -fx-font-weight: bold; -fx-font-size: 13px;";
                rCount.setStyle(countStyle);

                Label statusTag = new Label(r.isActive() ? "ACTIVE" : "INACTIVE");
                statusTag.setStyle(r.isActive() ? "-fx-background-color: rgba(16, 185, 129, 0.15); -fx-text-fill: #34d399; -fx-font-size: 11px; -fx-font-weight: bold; -fx-padding: 3 8 3 8; -fx-background-radius: 6px; -fx-border-color: rgba(52, 211, 153, 0.3); -fx-border-width: 1px; -fx-border-radius: 6px;" : "-fx-background-color: rgba(100, 116, 139, 0.15); -fx-text-fill: #94a3b8; -fx-font-size: 11px; -fx-padding: 3 8 3 8; -fx-background-radius: 6px;");

                roomListGrid.add(rName, 0, row);
                roomListGrid.add(rCount, 1, row);
                roomListGrid.add(statusTag, 2, row);
                row++;
            }
            roomBreakdown.getChildren().add(roomListGrid);
        }

        // Right Panel: Business Logic Integration Status
        VBox rulePanel = new VBox(14);
        rulePanel.setPadding(new Insets(20));
        rulePanel.getStyleClass().add("admin-stat-card");
        rulePanel.setStyle("-fx-background-color: rgba(12, 16, 24, 0.94); -fx-border-color: rgba(225, 29, 72, 0.25); -fx-border-width: 1px; -fx-background-radius: 10px;");
        rulePanel.setPrefWidth(380);

        Label ruleTitle = new Label("GAME HORROR LOGIC RULES");
        ruleTitle.setStyle("-fx-font-family: 'Georgia', serif; -fx-font-size: 15px; -fx-font-weight: bold; -fx-text-fill: #f43f5e;");

        Label rule1 = new Label("• 5 Random Active Questions chosen per room challenge.");
        rule1.setWrapText(true);
        rule1.setStyle("-fx-text-fill: #cbd5e1; -fx-font-size: 13px;");

        Label rule2 = new Label("• 5 Correct Answers = Unlock Next Door + Drop Room Reward.");
        rule2.setWrapText(true);
        rule2.setStyle("-fx-text-fill: #34d399; -fx-font-size: 13px; -fx-font-weight: bold;");

        Label rule3 = new Label("• 1 WRONG ANSWER = GHOST SPAWNS AND CHASES PLAYER!");
        rule3.setWrapText(true);
        rule3.setStyle("-fx-text-fill: #f43f5e; -fx-font-size: 13px; -fx-font-weight: bold;");

        Label statusBox = new Label("Storage Mode: Pure JSON File Persistence\nAutomated Backups Enabled\nZero External Database Required");
        statusBox.setStyle("-fx-background-color: rgba(159, 18, 57, 0.15); -fx-border-color: rgba(225, 29, 72, 0.3); -fx-border-width: 1px; -fx-border-radius: 8px; -fx-background-radius: 8px; -fx-padding: 12; -fx-text-fill: #fda4af; -fx-font-size: 12px;");

        rulePanel.getChildren().addAll(ruleTitle, rule1, rule2, rule3, statusBox);

        lowerSection.getChildren().addAll(roomBreakdown, rulePanel);
        getChildren().add(lowerSection);
    }

    private VBox createStatCard(String title, String value, String subtitle, String accentHex) {
        VBox card = new VBox(6);
        card.setPadding(new Insets(18));
        card.getStyleClass().add("admin-stat-card");
        card.setStyle(String.format("-fx-background-color: rgba(12, 16, 24, 0.94); -fx-border-color: %s; -fx-border-width: 2px 0 0 0; -fx-background-radius: 10px; -fx-border-radius: 10px; -fx-effect: dropshadow(gaussian, rgba(0, 0, 0, 0.75), 18, 0.4, 0, 4);", accentHex));

        Label titleLbl = new Label(title);
        titleLbl.setStyle(String.format("-fx-font-family: 'Georgia', serif; -fx-font-size: 12px; -fx-font-weight: bold; -fx-text-fill: %s;", accentHex));

        Label valLbl = new Label(value);
        valLbl.setStyle(String.format("-fx-font-size: 32px; -fx-font-weight: 900; -fx-text-fill: #f8fafc; -fx-effect: dropshadow(gaussian, %s, 10, 0.3, 0, 0);", accentHex));

        Label subLbl = new Label(subtitle);
        subLbl.setStyle("-fx-font-size: 11px; -fx-text-fill: #64748b;");

        card.getChildren().addAll(titleLbl, valLbl, subLbl);
        return card;
    }
}
