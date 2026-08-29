package com.khmerspirit.admin.view;

import com.khmerspirit.core.SceneManager;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.layout.*;

import java.util.Optional;

/**
 * Main Layout Container for the Admin Panel featuring Haunted School Background wallpaper,
 * Old Rock Texture Sidebar, Blood Red Accents, and Swappable Content Views.
 */
public class AdminMainView extends BorderPane {

    private final StackPane contentArea = new StackPane();

    private final DashboardView dashboardView = new DashboardView();
    private final QuestionManagementView questionManagementView = new QuestionManagementView();
    private final RoomManagementView roomManagementView = new RoomManagementView();
    private final RewardManagementView rewardManagementView = new RewardManagementView();

    private Button btnDashboard;
    private Button btnQuestions;
    private Button btnRooms;
    private Button btnRewards;

    private final Runnable onLogout;

    public AdminMainView(Runnable onLogout) {
        this.onLogout = onLogout;
        setPrefSize(1280, 720);

        // Apply Haunted School Background CSS class
        getStyleClass().add("admin-root-bg");

        // Dark fog overlay over right content area
        contentArea.setStyle("-fx-background-color: linear-gradient(to bottom right, rgba(5, 7, 12, 0.90), rgba(12, 16, 26, 0.96));");

        setLeft(buildSidebar());
        setCenter(contentArea);

        // Default view: Dashboard
        showView(dashboardView, btnDashboard);
    }

    private VBox buildSidebar() {
        VBox sidebar = new VBox(12);
        sidebar.setPrefWidth(240);
        sidebar.setPadding(new Insets(20, 16, 20, 16));
        sidebar.getStyleClass().add("admin-sidebar");
        sidebar.setStyle("-fx-background-color: rgba(9, 12, 18, 0.96); -fx-border-color: rgba(225, 29, 72, 0.22); -fx-border-width: 0 1px 0 0;");

        // Header Title Badge
        VBox titleCard = new VBox(4);
        titleCard.setPadding(new Insets(14, 14, 16, 14));
        titleCard.getStyleClass().add("admin-sidebar-title-card");
        titleCard.setStyle("-fx-background-color: rgba(18, 24, 38, 0.85); -fx-border-color: rgba(225, 29, 72, 0.35); -fx-border-width: 1px; -fx-background-radius: 8px; -fx-border-radius: 8px;");

        Label mainTitle = new Label("TEACHER ADMIN");
        mainTitle.setStyle("-fx-font-family: 'Georgia', serif; -fx-font-size: 16px; -fx-font-weight: 900; -fx-text-fill: #f8fafc; -fx-letter-spacing: 1.5px; -fx-effect: dropshadow(gaussian, rgba(225, 29, 72, 0.5), 10, 0.3, 0, 0);");

        Label subTitle = new Label("Khmer Spirit • Haunted School");
        subTitle.setStyle("-fx-font-size: 11px; -fx-text-fill: #94a3b8; -fx-font-weight: bold;");

        titleCard.getChildren().addAll(mainTitle, subTitle);
        sidebar.getChildren().add(titleCard);

        // Navigation Buttons
        btnDashboard = createNavButton("Dashboard");
        btnQuestions = createNavButton("Questions");
        btnRooms = createNavButton("Rooms");
        btnRewards = createNavButton("Rewards");

        btnDashboard.setOnAction(e -> {
            dashboardView.refreshDashboard();
            showView(dashboardView, btnDashboard);
        });

        btnQuestions.setOnAction(e -> {
            questionManagementView.loadData();
            showView(questionManagementView, btnQuestions);
        });

        btnRooms.setOnAction(e -> {
            roomManagementView.loadData();
            showView(roomManagementView, btnRooms);
        });

        btnRewards.setOnAction(e -> {
            rewardManagementView.loadData();
            showView(rewardManagementView, btnRewards);
        });

        sidebar.getChildren().addAll(btnDashboard, btnQuestions, btnRooms, btnRewards);

        // Spacer to push Logout to bottom
        Region spacer = new Region();
        VBox.setVgrow(spacer, Priority.ALWAYS);
        sidebar.getChildren().add(spacer);

        // Logout Button
        Button btnLogout = createNavButton("Logout System");
        btnLogout.setStyle("-fx-background-color: rgba(159, 18, 57, 0.25); -fx-text-fill: #f43f5e; -fx-font-weight: 900; -fx-border-color: rgba(244, 63, 94, 0.4); -fx-border-width: 1px; -fx-border-radius: 6px; -fx-background-radius: 6px; -fx-alignment: center-left; -fx-padding: 12 16 12 16; -fx-cursor: hand;");
        btnLogout.setOnAction(e -> handleLogout());

        Button btnMainMenu = createNavButton("Main Menu");
        btnMainMenu.setStyle("-fx-background-color: rgba(15, 23, 42, 0.6); -fx-text-fill: #94a3b8; -fx-font-weight: bold; -fx-border-color: rgba(255, 255, 255, 0.08); -fx-border-width: 1px; -fx-border-radius: 6px; -fx-background-radius: 6px; -fx-alignment: center-left; -fx-padding: 12 16 12 16; -fx-cursor: hand;");
        btnMainMenu.setOnAction(e -> SceneManager.showMainMenu());

        sidebar.getChildren().addAll(btnMainMenu, btnLogout);

        return sidebar;
    }

    private Button createNavButton(String text) {
        Button btn = new Button(text);
        btn.setMaxWidth(Double.MAX_VALUE);
        btn.getStyleClass().add("admin-nav-button");
        return btn;
    }

    private void showView(Node view, Button activeBtn) {
        contentArea.getChildren().clear();
        contentArea.getChildren().add(view);

        // Reset all navigation button styles
        btnDashboard.getStyleClass().removeAll("admin-nav-button-active");
        btnQuestions.getStyleClass().removeAll("admin-nav-button-active");
        btnRooms.getStyleClass().removeAll("admin-nav-button-active");
        btnRewards.getStyleClass().removeAll("admin-nav-button-active");

        if (activeBtn != null) {
            activeBtn.getStyleClass().add("admin-nav-button-active");
        }
    }

    private void handleLogout() {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirm Logout");
        confirm.setHeaderText("Leave the Haunted Admin Panel?");
        confirm.setContentText("Are you sure you want to end your administrative session?");

        Optional<ButtonType> result = confirm.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            if (onLogout != null) {
                onLogout.run();
            }
        }
    }
}
