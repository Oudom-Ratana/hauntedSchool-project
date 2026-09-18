package com.khmerspirit.admin.view;

import com.khmerspirit.admin.service.AdminFileService;
import com.khmerspirit.core.SceneManager;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.geometry.Pos;
import javafx.scene.Group;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.util.Duration;

import java.io.File;
import java.io.InputStream;

/**
 * Admin Panel Login View styled with authentic ancient Angkorian gothic aesthetics,
 * utilizing the cleaned admin_login_panel stone tablet design with interactive custom inputs,
 * buttons, and responsive scaling.
 */
public class AdminLoginView extends StackPane {

    private static final double BASE_WIDTH = 1200.0;
    private static final double BASE_HEIGHT = 896.0;

    private final AdminFileService adminFileService = new AdminFileService();
    private final Runnable onLoginSuccess;

    private Pane panelPane;
    private TextField userField;
    private PasswordField passField;
    private Label statusMsg;

    public AdminLoginView(Runnable onLoginSuccess) {
        this.onLoginSuccess = onLoginSuccess;
        setPrefSize(1280, 720);
        getStyleClass().add("admin-root-bg");

        // Dark atmospheric background overlay
        Region bgOverlay = new Region();
        bgOverlay.setStyle("-fx-background-color: radial-gradient(center 50% 50%, radius 70%, rgba(18, 14, 24, 0.75), rgba(5, 7, 12, 0.98));");
        bgOverlay.prefWidthProperty().bind(widthProperty());
        bgOverlay.prefHeightProperty().bind(heightProperty());

        // Build the scalable admin login tablet
        panelPane = buildTabletPane();
        Group scaledGroup = new Group(panelPane);
        StackPane.setAlignment(scaledGroup, Pos.CENTER);

        getChildren().addAll(bgOverlay, scaledGroup);

        // Responsive scaling listener
        widthProperty().addListener((obs, oldV, newV) -> updateScale());
        heightProperty().addListener((obs, oldV, newV) -> updateScale());
        updateScale();

        // Keyboard navigation
        setOnKeyPressed(e -> {
            if (e.getCode() == KeyCode.ESCAPE) {
                SceneManager.showMainMenu();
            }
        });
    }

    private void updateScale() {
        double availW = getWidth() > 0 ? getWidth() : 1280.0;
        double availH = getHeight() > 0 ? getHeight() : 720.0;

        // Leave comfortable padding around edges
        double scaleX = (availW - 32.0) / BASE_WIDTH;
        double scaleY = (availH - 32.0) / BASE_HEIGHT;
        double scale = Math.min(scaleX, scaleY);

        if (scale > 1.25) scale = 1.25;
        if (scale < 0.40) scale = 0.40;

        panelPane.setScaleX(scale);
        panelPane.setScaleY(scale);
    }

    private Pane buildTabletPane() {
        Pane pane = new Pane();
        pane.setPrefSize(BASE_WIDTH, BASE_HEIGHT);
        pane.setMaxSize(BASE_WIDTH, BASE_HEIGHT);
        pane.setMinSize(BASE_WIDTH, BASE_HEIGHT);

        // 1. Background Tablet Image
        ImageView imageView = new ImageView();
        Image img = loadAdminImage();
        if (img != null) {
            imageView.setImage(img);
            imageView.setFitWidth(BASE_WIDTH);
            imageView.setFitHeight(BASE_HEIGHT);
            imageView.setPreserveRatio(false);
            imageView.setSmooth(true);
        }
        imageView.setLayoutX(0);
        imageView.setLayoutY(0);

        // 2. Custom Typography: Header Title & Subtitle
        Label titleLbl = new Label("❖  KHMER SPIRIT SANCTUM  ❖");
        titleLbl.setLayoutX(300);
        titleLbl.setLayoutY(246);
        titleLbl.setPrefWidth(600);
        titleLbl.setAlignment(Pos.CENTER);
        titleLbl.setStyle("-fx-font-family: 'Georgia', serif; -fx-font-size: 24px; -fx-font-weight: bold; "
                + "-fx-text-fill: #ffd591; -fx-letter-spacing: 2px; "
                + "-fx-effect: dropshadow(gaussian, rgba(0, 0, 0, 0.95), 10, 0.7, 0, 2);");

        Label subTitleLbl = new Label("TEACHER & ACADEMY ADMINISTRATION PORTAL");
        subTitleLbl.setLayoutX(300);
        subTitleLbl.setLayoutY(292);
        subTitleLbl.setPrefWidth(600);
        subTitleLbl.setAlignment(Pos.CENTER);
        subTitleLbl.setStyle("-fx-font-family: 'Segoe UI', sans-serif; -fx-font-size: 12px; -fx-font-weight: bold; "
                + "-fx-text-fill: #c5a059; -fx-letter-spacing: 2px; "
                + "-fx-effect: dropshadow(gaussian, rgba(0, 0, 0, 0.9), 8, 0.5, 0, 1);");

        // 3. Username Field Overlay
        Label userLbl = new Label("ADMINISTRATOR USERNAME");
        userLbl.setLayoutX(358);
        userLbl.setLayoutY(355);
        userLbl.setStyle("-fx-font-family: 'Segoe UI', sans-serif; -fx-font-size: 11px; -fx-font-weight: bold; "
                + "-fx-text-fill: #e8dcc4; -fx-letter-spacing: 1.5px; "
                + "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.9), 4, 0.5, 0, 1);");

        userField = new TextField();
        userField.setLayoutX(354);
        userField.setLayoutY(380);
        userField.setPrefWidth(491);
        userField.setPrefHeight(78);
        userField.setPromptText("Enter admin username (default: admin)");
        String baseInputStyle = "-fx-background-color: rgba(10, 14, 22, 0.65); "
                + "-fx-text-fill: #ffffff; -fx-prompt-text-fill: #78716c; "
                + "-fx-font-family: 'Segoe UI', sans-serif; -fx-font-size: 15px; -fx-font-weight: bold; "
                + "-fx-border-color: rgba(212, 175, 55, 0.40); -fx-border-width: 1.5px; "
                + "-fx-border-radius: 8px; -fx-background-radius: 8px; -fx-padding: 0 20 0 20;";
        String focusedInputStyle = "-fx-background-color: rgba(15, 20, 32, 0.90); "
                + "-fx-text-fill: #ffffff; -fx-prompt-text-fill: #a8a29e; "
                + "-fx-font-family: 'Segoe UI', sans-serif; -fx-font-size: 15px; -fx-font-weight: bold; "
                + "-fx-border-color: #d4af37; -fx-border-width: 2px; "
                + "-fx-border-radius: 8px; -fx-background-radius: 8px; -fx-padding: 0 20 0 20; "
                + "-fx-effect: dropshadow(gaussian, rgba(212, 175, 55, 0.5), 14, 0.45, 0, 0);";
        userField.setStyle(baseInputStyle);
        userField.focusedProperty().addListener((obs, oldV, isFocused) -> {
            userField.setStyle(isFocused ? focusedInputStyle : baseInputStyle);
        });

        // 4. Password Field Overlay
        Label passLbl = new Label("SECURITY PASSWORD");
        passLbl.setLayoutX(358);
        passLbl.setLayoutY(468);
        passLbl.setStyle("-fx-font-family: 'Segoe UI', sans-serif; -fx-font-size: 11px; -fx-font-weight: bold; "
                + "-fx-text-fill: #e8dcc4; -fx-letter-spacing: 1.5px; "
                + "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.9), 4, 0.5, 0, 1);");

        passField = new PasswordField();
        passField.setLayoutX(354);
        passField.setLayoutY(491);
        passField.setPrefWidth(491);
        passField.setPrefHeight(78);
        passField.setPromptText("Enter admin password (default: admin123)");
        passField.setStyle(baseInputStyle);
        passField.focusedProperty().addListener((obs, oldV, isFocused) -> {
            passField.setStyle(isFocused ? focusedInputStyle : baseInputStyle);
        });

        // 5. Error & Status Message Label
        statusMsg = new Label();
        statusMsg.setLayoutX(320);
        statusMsg.setLayoutY(582);
        statusMsg.setPrefWidth(560);
        statusMsg.setAlignment(Pos.CENTER);
        statusMsg.setStyle("-fx-text-fill: #f43f5e; -fx-font-family: 'Segoe UI', sans-serif; -fx-font-size: 13px; -fx-font-weight: bold; "
                + "-fx-effect: dropshadow(gaussian, rgba(0, 0, 0, 0.95), 8, 0.8, 0, 1);");
        statusMsg.setVisible(false);

        // 6. Interactive Button: ENTER SYSTEM (Left slot)
        Button enterBtn = new Button("⚔  Enter System");
        enterBtn.setLayoutX(302);
        enterBtn.setLayoutY(631);
        enterBtn.setPrefWidth(288);
        enterBtn.setPrefHeight(80);
        enterBtn.getStyleClass().add("btn-khmer-gold");
        enterBtn.setStyle("-fx-font-size: 15px; -fx-font-weight: bold; -fx-letter-spacing: 1px;");

        // 7. Interactive Button: RETURN TO MENU (Right slot)
        Button returnBtn = new Button("✦  Return to Menu");
        returnBtn.setLayoutX(612);
        returnBtn.setLayoutY(631);
        returnBtn.setPrefWidth(288);
        returnBtn.setPrefHeight(80);
        returnBtn.getStyleClass().add("btn-khmer-stone");
        returnBtn.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-letter-spacing: 1px;");

        // 8. Footer Credential Hint
        Label hintLbl = new Label("Default Credentials: admin / admin123  •  Press [ESC] to return to main menu");
        hintLbl.setLayoutX(200);
        hintLbl.setLayoutY(732);
        hintLbl.setPrefWidth(800);
        hintLbl.setAlignment(Pos.CENTER);
        hintLbl.setStyle("-fx-text-fill: #a89f91; -fx-font-family: 'Segoe UI', sans-serif; -fx-font-size: 12px; -fx-font-weight: normal; "
                + "-fx-effect: dropshadow(gaussian, rgba(0, 0, 0, 0.9), 6, 0.5, 0, 1);");

        // Action Handlers
        Runnable loginAction = this::handleLogin;
        enterBtn.setOnAction(e -> loginAction.run());
        userField.setOnAction(e -> loginAction.run());
        passField.setOnAction(e -> loginAction.run());
        returnBtn.setOnAction(e -> SceneManager.showMainMenu());

        pane.getChildren().addAll(
                imageView,
                titleLbl, subTitleLbl,
                userLbl, userField,
                passLbl, passField,
                statusMsg,
                enterBtn, returnBtn,
                hintLbl
        );

        return pane;
    }

    private void handleLogin() {
        String u = userField.getText() != null ? userField.getText().trim() : "";
        String p = passField.getText() != null ? passField.getText().trim() : "";

        if (adminFileService.authenticate(u, p)) {
            statusMsg.setVisible(false);
            if (onLoginSuccess != null) {
                onLoginSuccess.run();
            }
        } else {
            statusMsg.setText("Access Denied! Incorrect administrator username or password.");
            statusMsg.setVisible(true);
            triggerShakeAnimation();
        }
    }

    private void triggerShakeAnimation() {
        if (panelPane == null) return;
        double origX = panelPane.getTranslateX();
        Timeline shake = new Timeline(
                new KeyFrame(Duration.millis(0), new KeyValue(panelPane.translateXProperty(), origX)),
                new KeyFrame(Duration.millis(50), new KeyValue(panelPane.translateXProperty(), origX - 10)),
                new KeyFrame(Duration.millis(100), new KeyValue(panelPane.translateXProperty(), origX + 10)),
                new KeyFrame(Duration.millis(150), new KeyValue(panelPane.translateXProperty(), origX - 8)),
                new KeyFrame(Duration.millis(200), new KeyValue(panelPane.translateXProperty(), origX + 8)),
                new KeyFrame(Duration.millis(250), new KeyValue(panelPane.translateXProperty(), origX - 4)),
                new KeyFrame(Duration.millis(300), new KeyValue(panelPane.translateXProperty(), origX))
        );
        shake.play();
    }

    private Image loadAdminImage() {
        try {
            InputStream stream = getClass().getResourceAsStream("/images/ui/admin_login_panel.png");
            if (stream != null) {
                return new Image(stream);
            }
            File f = new File("images/ui/admin_login_panel.png");
            if (f.exists()) {
                return new Image(f.toURI().toString());
            }
            f = new File("src/main/resources/images/ui/admin_login_panel.png");
            if (f.exists()) {
                return new Image(f.toURI().toString());
            }
        } catch (Exception e) {
            System.err.println("[AdminLoginView] Could not load admin_login_panel image: " + e.getMessage());
        }
        return null;
    }
}
