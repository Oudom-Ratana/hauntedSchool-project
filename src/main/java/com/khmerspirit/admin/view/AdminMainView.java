package com.khmerspirit.admin.view;

import com.khmerspirit.core.SceneManager;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.shape.Circle;

import java.io.File;
import java.io.InputStream;
import java.util.Optional;

/**
 * Main Layout Container for the Admin Panel matching draftDesign.png,
 * featuring Top Header Bar with haunted school silhouettes, modern left navigation sidebar,
 * and responsive content switching.
 */
public class AdminMainView extends BorderPane {

    private final StackPane contentArea = new StackPane();

    private final QuestionManagementView questionManagementView = new QuestionManagementView();
    private final RoomManagementView roomManagementView = new RoomManagementView();
    private final MapItemManagementView mapItemManagementView = new MapItemManagementView();
    private final RewardManagementView rewardManagementView = new RewardManagementView();
    private final DashboardView dashboardView = new DashboardView();

    private Button btnQuestions;
    private Button btnRooms;
    private Button btnGameSettings;
    private Button btnSaveData;
    private Button btnOverview;

    private final Runnable onLogout;

    public AdminMainView(Runnable onLogout) {
        this.onLogout = onLogout;
        setPrefSize(1280, 720);

        getStyleClass().add("admin-modern-bg");
        setStyle("-fx-background-color: #070b14;");

        // Set Top Header Bar
        setTop(buildHeaderBar());

        // Set Left Navigation Sidebar
        setLeft(buildSidebar());

        // Center Content Area
        contentArea.setStyle("-fx-background-color: #070b14;");
        setCenter(contentArea);

        // Default view: Questions panel (as featured in draftDesign.png)
        showView(questionManagementView, btnQuestions);
    }

    /**
     * Top Header Bar matching draftDesign.png
     */
    private HBox buildHeaderBar() {
        HBox header = new HBox(14);
        header.setAlignment(Pos.CENTER_LEFT);
        header.setPrefHeight(64);
        header.getStyleClass().add("admin-modern-header");

        // 1. Ghost Icon + Game Brand
        ImageView ghostIcon = new ImageView();
        Image ghostImg = loadImage("/images/ghost/transparent_ghost_student_sprite.png", "images/ghost/transparent_ghost_student_sprite.png");
        if (ghostImg == null) {
            ghostImg = loadImage("/images/ghost/card_student_ghost.png", "images/ghost/card_student_ghost.png");
        }
        if (ghostImg != null) {
            ghostIcon.setImage(ghostImg);
            ghostIcon.setFitWidth(32);
            ghostIcon.setFitHeight(32);
            ghostIcon.setPreserveRatio(true);
        }

        VBox brandBox = new VBox(1);
        brandBox.setAlignment(Pos.CENTER_LEFT);
        Label brandTitle = new Label("Khmer Spirit");
        brandTitle.getStyleClass().add("admin-header-logo-title");
        Label brandSub = new Label("The Haunted School");
        brandSub.getStyleClass().add("admin-header-logo-sub");
        brandBox.getChildren().addAll(brandTitle, brandSub);

        // 2. Vertical Divider
        Region divider = new Region();
        divider.getStyleClass().add("admin-header-divider");
        divider.setPrefHeight(28);

        // 3. Admin Panel Title & Subtitle
        HBox panelTitleRow = new HBox(6);
        panelTitleRow.setAlignment(Pos.CENTER_LEFT);
        Label cogIcon = new Label("⚙");
        cogIcon.setStyle("-fx-text-fill: #38bdf8; -fx-font-size: 15px;");
        Label panelTitle = new Label("Admin Panel");
        panelTitle.getStyleClass().add("admin-header-panel-title");
        panelTitleRow.getChildren().addAll(cogIcon, panelTitle);

        Label panelSubtitle = new Label("Manage Questions & Tasks");
        panelSubtitle.getStyleClass().add("admin-header-panel-sub");

        VBox panelInfoBox = new VBox(2);
        panelInfoBox.setAlignment(Pos.CENTER_LEFT);
        panelInfoBox.getChildren().addAll(panelTitleRow, panelSubtitle);

        // 4. Spacer pushing profile to right
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        // 5. Admin Profile Pill
        HBox profilePill = new HBox(8);
        profilePill.setAlignment(Pos.CENTER);
        profilePill.getStyleClass().add("admin-profile-pill");

        ImageView avatarImg = new ImageView();
        Image avImg = loadImage("/images/Charactors/card_rithy.png", "images/Charactors/card_rithy.png");
        if (avImg != null) {
            avatarImg.setImage(avImg);
            avatarImg.setFitWidth(24);
            avatarImg.setFitHeight(24);
            Circle clip = new Circle(12, 12, 12);
            avatarImg.setClip(clip);
        }
        Label adminName = new Label("Admin");
        adminName.setStyle("-fx-text-fill: #f1f5f9; -fx-font-family: 'Segoe UI', sans-serif; -fx-font-size: 12.5px; -fx-font-weight: bold;");
        Label dropdownArrow = new Label("▾");
        dropdownArrow.setStyle("-fx-text-fill: #94a3b8; -fx-font-size: 11px;");
        profilePill.getChildren().addAll(avatarImg, adminName, dropdownArrow);

        // 6. Settings Gear Button
        Button btnSettings = new Button("⚙");
        btnSettings.getStyleClass().add("admin-settings-btn");
        btnSettings.setOnAction(e -> {
            dashboardView.refreshDashboard();
            showView(dashboardView, btnOverview);
        });

        header.getChildren().addAll(ghostIcon, brandBox, divider, panelInfoBox, spacer, profilePill, btnSettings);
        return header;
    }

    /**
     * Left Sidebar navigation matching draftDesign.png
     */
    private VBox buildSidebar() {
        VBox sidebar = new VBox(8);
        sidebar.setPrefWidth(210);
        sidebar.getStyleClass().add("admin-modern-sidebar");

        btnQuestions = createNavButton("📋  Questions");
        btnRooms = createNavButton("🏠  Rooms & Tasks");
        btnGameSettings = createNavButton("⚙️  Game Settings");
        btnSaveData = createNavButton("💾  Save Data");
        btnOverview = createNavButton("🏛️  Overview");

        btnQuestions.setOnAction(e -> {
            questionManagementView.loadData();
            showView(questionManagementView, btnQuestions);
        });

        btnRooms.setOnAction(e -> {
            roomManagementView.loadData();
            showView(roomManagementView, btnRooms);
        });

        btnGameSettings.setOnAction(e -> {
            mapItemManagementView.loadData();
            showView(mapItemManagementView, btnGameSettings);
        });

        btnSaveData.setOnAction(e -> {
            rewardManagementView.loadData();
            showView(rewardManagementView, btnSaveData);
        });

        btnOverview.setOnAction(e -> {
            dashboardView.refreshDashboard();
            showView(dashboardView, btnOverview);
        });

        sidebar.getChildren().addAll(btnQuestions, btnRooms, btnGameSettings, btnSaveData, btnOverview);

        // Spacer pushing logout to bottom
        Region spacer = new Region();
        VBox.setVgrow(spacer, Priority.ALWAYS);
        sidebar.getChildren().add(spacer);

        // Logout Button
        Button btnLogout = new Button("🚪  Logout");
        btnLogout.setMaxWidth(Double.MAX_VALUE);
        btnLogout.getStyleClass().add("admin-draft-nav-btn");
        btnLogout.setStyle("-fx-text-fill: #f87171; -fx-alignment: center-left; -fx-padding: 9 14;");
        btnLogout.setOnAction(e -> handleLogout());
        sidebar.getChildren().add(btnLogout);

        // Bottom Khmer Lore Watermark (as shown in draftDesign.png)
        VBox loreBox = new VBox(4);
        loreBox.setAlignment(Pos.CENTER);
        loreBox.setPadding(new Insets(14, 6, 8, 6));
        loreBox.setStyle("-fx-border-color: rgba(30, 41, 59, 0.6) transparent transparent transparent; -fx-border-width: 1px;");

        Label khmerLogo = new Label("វិញ្ញាណសាលា");
        khmerLogo.setStyle("-fx-font-family: 'Khmer OS', 'Georgia', serif; -fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #60a5fa; -fx-effect: dropshadow(gaussian, rgba(37, 99, 235, 0.5), 8, 0.3, 0, 0);");

        Label quote = new Label("Knowledge opens the door,\nbut not all doors should be opened...");
        quote.setStyle("-fx-font-family: 'Segoe UI', sans-serif; -fx-font-size: 9.5px; -fx-font-style: italic; -fx-text-fill: #64748b; -fx-text-alignment: center;");

        loreBox.getChildren().addAll(khmerLogo, quote);
        sidebar.getChildren().add(loreBox);

        return sidebar;
    }

    private Button createNavButton(String text) {
        Button btn = new Button(text);
        btn.setMaxWidth(Double.MAX_VALUE);
        btn.getStyleClass().add("admin-draft-nav-btn");
        return btn;
    }

    private void showView(Node view, Button activeBtn) {
        contentArea.getChildren().clear();

        ScrollPane scrollPane = new ScrollPane(view);
        scrollPane.setFitToWidth(true);
        scrollPane.setFitToHeight(true);
        scrollPane.setStyle("-fx-background: #070b14; -fx-background-color: #070b14; -fx-padding: 0;");
        contentArea.getChildren().add(scrollPane);

        // Reset all navigation button styles
        if (btnQuestions != null) btnQuestions.getStyleClass().removeAll("admin-draft-nav-btn-active");
        if (btnRooms != null) btnRooms.getStyleClass().removeAll("admin-draft-nav-btn-active");
        if (btnGameSettings != null) btnGameSettings.getStyleClass().removeAll("admin-draft-nav-btn-active");
        if (btnSaveData != null) btnSaveData.getStyleClass().removeAll("admin-draft-nav-btn-active");
        if (btnOverview != null) btnOverview.getStyleClass().removeAll("admin-draft-nav-btn-active");

        if (activeBtn != null) {
            activeBtn.getStyleClass().add("admin-draft-nav-btn-active");
        }
    }

    private void handleLogout() {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirm Logout");
        confirm.setHeaderText("Exit Admin Panel?");
        confirm.setContentText("Are you sure you want to return to the main menu?");

        Optional<ButtonType> result = confirm.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            if (onLogout != null) {
                onLogout.run();
            } else {
                SceneManager.showMainMenu();
            }
        }
    }

    private Image loadImage(String resourcePath, String filePath) {
        try {
            InputStream stream = getClass().getResourceAsStream(resourcePath);
            if (stream != null) {
                return new Image(stream);
            }
            File file = new File(filePath);
            if (file.exists()) {
                return new Image(file.toURI().toString());
            }
            File resFile = new File("src/main/resources" + resourcePath);
            if (resFile.exists()) {
                return new Image(resFile.toURI().toString());
            }
        } catch (Exception ignored) {
        }
        return null;
    }
}
