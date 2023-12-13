package com.example.triventure;

import javafx.animation.*;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.paint.Color;
import javafx.scene.shape.*;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.stage.Stage;
import javafx.scene.input.KeyCode;
import javafx.util.Duration;

import java.io.*;
import java.util.ArrayList;
import java.util.Scanner;

public class Triventure extends Application {
    private Stage primaryStage;
    private boolean isUserLoggedIn = false;
    public static boolean soundEnabled = true;
    private String currentUser;
    private static final double SCENE_WIDTH = 800;
    private static final double SCENE_HEIGHT = 600;
    private static final double SCROLL_THRESHOLD = SCENE_WIDTH/2;
    private GameLevel currentLevel;
    private Player player;
    private int totalCollectiblesCount;
    private int collectiblesCount = 0;
    private Text collectiblesBar;
    private Rectangle sky;
    private Timeline gameLoop;
    private boolean level2Locked = true;
    private boolean level3Locked = true;

    @Override
    public void start(Stage primaryStage) {
        this.primaryStage = primaryStage;
        setupMainMenu();
    }

    private void setupMainMenu() {
        BorderPane mainMenuPane = new BorderPane();
        mainMenuPane.setId("menu-screen");

        Text title = new Text("TRIVENTURE");
        title.setFont(Font.loadFont(getClass().getResourceAsStream("/fonts/pixelated.ttf"), 60));
        title.setId("title-text");

        BorderPane.setAlignment(title, Pos.CENTER);
        mainMenuPane.setTop(title);

        VBox menuOptions = new VBox(10);
        menuOptions.setAlignment(Pos.CENTER);

        Button levelsButton = new Button("Levels");
        levelsButton.setMinSize(200, 100);
        levelsButton.setOnAction(e -> showLevelsMenu());
        levelsButton.setId("button-text");

        Button profileButton = new Button("Profile"); // Replace with your actual option
        profileButton.setMinSize(200, 100);
        profileButton.setOnAction(e -> {
            if (!isUserLoggedIn) {
                showLoginRegisterForm();
            } else {
                showUserProfile();
            }
        });

        profileButton.setId("button-text");
        profileButton.setMinSize(200, 100);

        Button settingsButton = new Button("Settings");
        settingsButton.setMinSize(200, 100);
        settingsButton.setOnAction(e -> showSettingsMenu());
        settingsButton.setId("button-text");

        Button quitButton = new Button("Quit Game");
        quitButton.setOnAction(e -> {
            System.exit(0);
        });
        quitButton.setMinSize(200, 100);
        quitButton.setId("button-text");

        menuOptions.getChildren().addAll(levelsButton, profileButton, settingsButton, quitButton);

        mainMenuPane.setCenter(menuOptions);

        Scene mainMenuScene = new Scene(mainMenuPane, SCENE_WIDTH, SCENE_HEIGHT);
        mainMenuScene.getStylesheets().add(getClass().getResource("/style.css").toExternalForm());
        primaryStage.setResizable(false);
        primaryStage.setTitle("Triventure");
        primaryStage.setScene(mainMenuScene);
        primaryStage.getIcons().add(new Image(getClass().getResource("/images/triangle.png").toExternalForm()));
        primaryStage.show();

    }

    private void showLoginRegisterForm() {
        // Layout
        VBox layout = new VBox(10);
        layout.setId("menu-screen");
        layout.setAlignment(Pos.CENTER);

        // Username and Password Fields
        TextField usernameField = new TextField();
        usernameField.setMaxSize(100, 50);
        usernameField.setPromptText("Username");
        PasswordField passwordField = new PasswordField();
        passwordField.setMaxSize(100, 50);
        passwordField.setPromptText("Password");

        // Buttons
        Button loginButton = new Button("Login");
        loginButton.setId("button-text");
        Button registerButton = new Button("Register");
        registerButton.setId("button-text");

        // Set button actions
        loginButton.setOnAction(e -> loginUser(usernameField.getText(), passwordField.getText()));
        registerButton.setOnAction(e -> registerUser(usernameField.getText(), passwordField.getText()));

        layout.getChildren().addAll(usernameField, passwordField, loginButton, registerButton);

        Scene scene = new Scene(layout, SCENE_WIDTH, SCENE_HEIGHT);
        scene.getStylesheets().add(getClass().getResource("/style.css").toExternalForm());
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private void loginUser(String username, String password) {
        String[] userData = readUserData(username);

        if (userData != null && userData[1].equals(password)) {
            // Login successful
            currentUser = userData[0];
            if (userData[2] != null)
                totalCollectiblesCount = collectiblesCount + Integer.parseInt(userData[2]);
            isUserLoggedIn = true;
            System.out.println("Login Successful");
            setupMainMenu();
        } else {
            // Login failed
            System.out.println("Login Failed");
        }
    }

    private void registerUser(String username, String password) {
        writeUserData(username, password, totalCollectiblesCount);
        System.out.println("Registration Successful");
    }

    private void writeUserData(String username, String password, int collectiblesCount) {
        try {
            PrintWriter writer = new PrintWriter(new FileWriter(username + ".txt"));
            writer.println(username);
            writer.println(password);
            writer.println(collectiblesCount);
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private String[] readUserData(String username) {
        String[] userDetails = new String[3]; // username, password, collectiblesCount
        File userFile = new File(username + ".txt");

        try (Scanner scanner = new Scanner(userFile)) {
            if (scanner.hasNextLine()) {
                userDetails[0] = scanner.nextLine(); // username
            }
            if (scanner.hasNextLine()) {
                userDetails[1] = scanner.nextLine(); // password
            }
            if (scanner.hasNextLine()) {
                userDetails[2] = scanner.nextLine(); // collectiblesCount

            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        return userDetails;

    }

    private void showUserProfile() {
        VBox profileLayout = new VBox(10);
        profileLayout.setId("menu-screen");
        profileLayout.setAlignment(Pos.CENTER);

        Text usernameText = new Text("Username: " + currentUser);
        usernameText.setFont(Font.loadFont(getClass().getResourceAsStream("/fonts/pixelated.ttf"), 30));

        String[] ud = readUserData(currentUser);
        Text scoreText;
        if (ud[2] != null) {
            int score = Integer.parseInt(ud[2]) * 100;
            scoreText = new Text("Score: " + score);
            scoreText.setFont(Font.loadFont(getClass().getResourceAsStream("/fonts/pixelated.ttf"), 30));
        } else {
            scoreText = new Text("Score: 0");
            scoreText.setFont(Font.loadFont(getClass().getResourceAsStream("/fonts/pixelated.ttf"), 30));
        }
        Button backButton = new Button("Back");
        backButton.setOnAction(e -> setupMainMenu());

        profileLayout.getChildren().addAll(usernameText, scoreText, backButton);

        Scene profileScene = new Scene(profileLayout, SCENE_WIDTH, SCENE_HEIGHT);
        profileScene.getStylesheets().add(getClass().getResource("/style.css").toExternalForm());
        primaryStage.setScene(profileScene);
    }

    private void showSettingsMenu() {
        BorderPane settingsPane = new BorderPane();
        settingsPane.setId("menu-screen");
        VBox settingsOptions = new VBox(10);
        settingsOptions.setAlignment(Pos.CENTER);

        // Sound Settings
        Label soundLabel = new Label("Sound:");
        soundLabel.setFont(Font.loadFont(getClass().getResourceAsStream("/fonts/pixelated.ttf"), 14));
        final ToggleButton soundToggle = new ToggleButton("Sound: " + (Triventure.soundEnabled ? "On" : "Off"));
        soundToggle.setMinSize(100, 50);
        soundToggle.setId("button-text");
        soundToggle.setSelected(Triventure.soundEnabled);

        soundToggle.setOnAction(e -> {
            Triventure.soundEnabled = soundToggle.isSelected();
            soundToggle.setText("Sound: " + (Triventure.soundEnabled ? "On" : "Off"));
        });

        // Back Button
        Button backButton = new Button("Back");
        backButton.setOnAction(e -> setupMainMenu());
        backButton.setMinSize(100, 50);
        backButton.setId("button-text");

        BorderPane.setAlignment(backButton, Pos.CENTER_LEFT);
        settingsPane.setBottom(backButton);

        settingsOptions.getChildren().add(new HBox(10, soundLabel, soundToggle));

        if (isUserLoggedIn) {
            Label passwordChangeLabel = new Label("Change Password:");
            passwordChangeLabel.setId("button-text");
            passwordChangeLabel.setFont(Font.loadFont(getClass().getResourceAsStream("/fonts/pixelated.ttf"), 14));
            PasswordField oldPasswordField = new PasswordField();
            oldPasswordField.setMaxSize(200, 100);
            oldPasswordField.setPromptText("Current Password");
            PasswordField newPasswordField = new PasswordField();
            newPasswordField.setMaxSize(200, 100);
            newPasswordField.setPromptText("New Password");
            PasswordField confirmPasswordField = new PasswordField();
            confirmPasswordField.setMaxSize(200, 100);
            confirmPasswordField.setPromptText("Confirm New Password");

            Button changePasswordButton = new Button("Change Password");
            changePasswordButton.setMinSize(200, 100);
            changePasswordButton.setId("button-text");
            changePasswordButton.setOnAction(e ->
                    changePassword(oldPasswordField.getText(), newPasswordField.getText(), confirmPasswordField.getText()));

            settingsOptions.getChildren().addAll(passwordChangeLabel, oldPasswordField, newPasswordField, confirmPasswordField, changePasswordButton);
        }


        settingsPane.setCenter(settingsOptions);

        Scene settingsScene = new Scene(settingsPane, SCENE_WIDTH, SCENE_HEIGHT);
        settingsScene.getStylesheets().add(getClass().getResource("/style.css").toExternalForm());
        primaryStage.setScene(settingsScene);
    }

    private void changePassword(String oldPassword, String newPassword, String confirmPassword) {
        if (!newPassword.equals(confirmPassword)) {
            System.out.println("New passwords do not match.");
            return;
        }

        String[] userData = readUserData(currentUser);
        if (userData[1].equals(oldPassword)) {
            writeUserData(currentUser, newPassword, totalCollectiblesCount);
            System.out.println("Password successfully changed.");
        } else {
            System.out.println("Current password is incorrect.");
        }
    }

    private void updateUserDataFile() {
        String[] ud = readUserData(currentUser);
        if (isUserLoggedIn) {
            if (ud[2] != null) {
                int c = Integer.parseInt(ud[2]);
                if (c < totalCollectiblesCount)
                    writeUserData(currentUser, ud[1], totalCollectiblesCount);
                else
                    writeUserData(currentUser, ud[1], c);
            }
        }
    }

    private void showLevelsMenu() {
        BorderPane levelsMenuPane = new BorderPane();
        levelsMenuPane.setId("menu-screen");

        Text title = new Text("Select Level");
        title.setFont(Font.loadFont(getClass().getResourceAsStream("/fonts/pixelated.ttf"), 40));
        title.setId("title-text");
        BorderPane.setAlignment(title, Pos.CENTER);
        levelsMenuPane.setTop(title);

        VBox levelsOptions = new VBox(20);
        levelsOptions.setAlignment(Pos.CENTER);

        if (isUserLoggedIn) {
            String[] userData = readUserData(currentUser);
            if (userData != null && userData[2] != null) {
                totalCollectiblesCount = Integer.parseInt(userData[2]);
            }
        }

        if (totalCollectiblesCount >= 6) {
            level2Locked = false;
            level3Locked = false;
        } else if (totalCollectiblesCount >= 3)
            level2Locked = false;

        // Level 1 Button
        Button level1Button = new Button("Level 1");
        level1Button.setMinSize(200, 100);
        level1Button.setOnAction(e -> loadLevel1());
        level1Button.setId("button-text");

        // Level 2 Button
        Button level2Button = new Button("Level 2");
        level2Button.setMinSize(200, 100);
        level2Button.setOnAction(e -> loadLevel2());
        level2Button.setDisable(level2Locked);
        level2Button.setId("button-text");

        // Level 3 Button (Locked)
        Button level3Button = new Button("Level 3");
        level3Button.setMinSize(200, 100);
        level3Button.setOnAction(e -> loadLevel3());
        level3Button.setDisable(level3Locked);
        level3Button.setId("button-text");

        // Back Button
        Button backButton = new Button("Back");
        backButton.setOnAction(e -> setupMainMenu());
        backButton.setMinSize(100, 50);
        backButton.setId("button-text");

        BorderPane.setAlignment(backButton, Pos.CENTER_LEFT);
        levelsMenuPane.setBottom(backButton);

        levelsOptions.getChildren().addAll(level1Button, level2Button, level3Button);

        levelsMenuPane.setCenter(levelsOptions);

        Scene levelsMenuScene = new Scene(levelsMenuPane, SCENE_WIDTH, SCENE_HEIGHT);
        levelsMenuScene.getStylesheets().add(getClass().getResource("/style.css").toExternalForm());
        primaryStage.setScene(levelsMenuScene);
    }

    public void playSound(String soundFile) {
        if (soundEnabled) {
            MediaPlayer mediaPlayer = new MediaPlayer(new Media(soundFile));
            mediaPlayer.play();
        }
    }


    private void loadLevel1() {
        currentLevel = new GameLevel1();
        player.resetState();

        Scene scene = new Scene(currentLevel, SCENE_WIDTH, SCENE_HEIGHT);
        scene.setOnKeyPressed(e -> player.handleKeyPress(e.getCode()));
        scene.setOnKeyReleased(e -> player.handleKeyRelease(e.getCode()));
        primaryStage.setTitle("Triventure");
        primaryStage.setScene(scene);
        primaryStage.show();
        startGameLoop();
    }

    private void loadLevel2() {
        currentLevel = new GameLevel2();
        player.resetState();

        Scene scene = new Scene(currentLevel, SCENE_WIDTH, SCENE_HEIGHT);
        scene.setOnKeyPressed(e -> player.handleKeyPress(e.getCode()));
        scene.setOnKeyReleased(e -> player.handleKeyRelease(e.getCode()));
        primaryStage.setTitle("Triventure");
        primaryStage.setScene(scene);
        primaryStage.show();
        startGameLoop();
    }

    private void loadLevel3() {
        currentLevel = new GameLevel3();
        player.resetState();

        Scene scene = new Scene(currentLevel, SCENE_WIDTH, SCENE_HEIGHT);
        scene.setOnKeyPressed(e -> player.handleKeyPress(e.getCode()));
        scene.setOnKeyReleased(e -> player.handleKeyRelease(e.getCode()));
        primaryStage.setTitle("Triventure");
        primaryStage.setScene(scene);
        primaryStage.show();
        startGameLoop();
    }

    private void startGameLoop() {
        gameLoop = new Timeline(new KeyFrame(Duration.millis(20), e -> {
            player.move();
            scrollScene();
            checkCollisions();
        }));
        gameLoop.setCycleCount(Timeline.INDEFINITE);
        gameLoop.play();
    }

    private void scrollScene() {
        double playerX = player.getView().getTranslateX();

        // Check if the player has reached the scroll threshold
        if (playerX >= SCROLL_THRESHOLD) {
            double offsetX = player.velocityX;

            for (Node node : currentLevel.getChildren()) {
                if (node != player.getView() && node != collectiblesBar) // Do not move the player and collectiblesBar
                    node.setTranslateX(node.getTranslateX() - offsetX);
            }
            if(sky.getTranslateX() == 0 && offsetX < 0){
                player.getView().setTranslateX(playerX-1);
            }
        }
    }

    private void checkCollisions() {
        ArrayList<Node> toRemove = new ArrayList<>();

        for (Node child : currentLevel.getChildren()) {
            Object userdata = child.getUserData();
            if (userdata instanceof Enemy) {
                Enemy enemy = (Enemy) userdata;
                if (player.isColliding(enemy)) {
                    playSound(getClass().getResource("/sounds/death.mp3").toExternalForm());
                    handlePlayerDeath();
                    break;
                }
            } else if (userdata instanceof Collectible) {
                if (player.isColliding((Collectible) userdata)) {
                    playSound(getClass().getResource("/sounds/coin.mp3").toExternalForm());
                    toRemove.add(child);
                    collectiblesCount++;
                    String[] userData = readUserData(currentUser);
                    if (userData != null) {
                        writeUserData(currentUser, userData[1], collectiblesCount); // userData[1] is the password
                    }
                    updateCollectiblesBar();
                }
            } else if (child.getUserData() instanceof Platform) {
                Platform platform = (Platform) child.getUserData();
                if (player.isColliding(platform)) {
                    handlePlatformCollision(player, platform);
                }
            } else if (child.getUserData() instanceof Gate) {
                Gate gate = (Gate) child.getUserData();
                if (player.isColliding(gate)) {
                    handleGateCollision(gate);
                    break;
                }
            }
        }
        if(!toRemove.isEmpty()) {
            currentLevel.getChildren().removeAll(toRemove);
        }
    }

    private void handleGateCollision(Gate gate) {
        if (totalCollectiblesCount == gate.REQUIRED_COINS) {
            totalCollectiblesCount += collectiblesCount;
            gate.showLevelCompletionScreen();
            updateUserDataFile();
        } else {
            gate.showLevelCompletionScreen();
        }
    }

    private void handlePlatformCollision(Player player, Platform platform) {
        // AABB Implementation

        // Player bounds
        double playerLeft = player.getView().getTranslateX();
        double playerRight = playerLeft + player.getView().getBoundsInLocal().getWidth();
        double playerTop = player.getView().getTranslateY();
        double playerBottom = playerTop + player.getView().getBoundsInLocal().getHeight();

        // Platform bounds
        double platformLeft = platform.getView().getTranslateX();
        double platformRight = platformLeft + platform.getView().getBoundsInLocal().getWidth();
        double platformTop = platform.getView().getTranslateY();
        double platformBottom = platformTop + platform.getView().getBoundsInLocal().getHeight();

        // Check for AABB collision
        boolean collisionX = playerRight > platformLeft && playerLeft < platformRight;
        boolean collisionY = playerBottom > platformTop && playerTop < platformBottom;


        if (collisionX && collisionY) {
            // Determine collision type
            boolean fromTop = playerBottom <= platformTop + player.velocityY;
            boolean fromBottom = playerTop >= platformBottom + player.velocityY;
            boolean fromLeft = playerRight <= platformLeft + player.velocityX;
            boolean fromRight = playerLeft >= platformRight + player.velocityX;

            if (fromTop && player.velocityY >= 0) {
                // Landed on top of the platform
                player.land(platformTop - player.getView().getBoundsInLocal().getHeight());
            } else if (fromBottom) {
                // Hit the bottom of the platform
                player.getView().setTranslateY(platformBottom);
                player.velocityY = 0;
            } else if (fromLeft || fromRight) {
                // Hit the side of the platform
                player.stopHorizontalMovement();
            }
        }
    }

    private void handlePlayerDeath() {
        // Create the death message
        Text deathMessage = new Text("YOU DIED");
        deathMessage.setFont(Font.loadFont(getClass().getResourceAsStream("/fonts/pixelated.ttf"), 70));
        deathMessage.setFill(Color.RED);
        deathMessage.setStroke(Color.BLACK);
        deathMessage.setStrokeWidth(2);
        deathMessage.setX((SCENE_WIDTH - deathMessage.getLayoutBounds().getWidth()) / 2);
        deathMessage.setY(SCENE_HEIGHT / 2);

        Rectangle tint = new Rectangle(SCENE_WIDTH, SCENE_HEIGHT, new Color(0, 0, 0, 0.3));

        Button goBackButton = new Button("Back To Levels Menu");
        goBackButton.setLayoutX((SCENE_WIDTH / 2) - 60);
        goBackButton.setLayoutY(SCENE_HEIGHT / 2 + 50);
        goBackButton.setMinSize(100, 70);
        goBackButton.setOpacity(0);
        goBackButton.setId("button-text");

        FadeTransition fadeTransition = new FadeTransition(Duration.millis(1000), goBackButton);
        fadeTransition.setFromValue(0);
        fadeTransition.setToValue(1);
        fadeTransition.setDelay(Duration.millis(1000));

        goBackButton.setOnAction(e -> {
            goBackButton.setOnAction(null);
            fadeTransition.stop();
            player.resetState();
            showLevelsMenu();
        });

        currentLevel.getChildren().addAll(tint, deathMessage, goBackButton);

        fadeTransition.play();
        gameLoop.stop();
    }

    private void updateCollectiblesBar() {
        collectiblesBar.setText("Collectibles: " + collectiblesCount);
    }

    public static void main(String[] args) {
        launch(args);
    }

    // GameLevel class
    public abstract class GameLevel extends Pane {
        public GameLevel() {
            createLevel();
        }

        protected abstract void createLevel();
    }

    // GameObject class
    public abstract class GameObject {
        protected Node view;
        protected double velocityX = 0;
        protected double velocityY = 0;

        public GameObject(Node view) {
            this.view = view;
            this.view.setUserData(this);
        }

        public boolean isColliding(GameObject other) {
            return view.getBoundsInParent().intersects(other.view.getBoundsInParent());
        }

        public Node getView() {
            return view;
        }
    }

    // White House
    public class Gate extends GameObject {
        private int REQUIRED_COINS;
        private int level;
        public Gate(String imagepath) {
            super(new ImageView(new Image(imagepath)));
            ImageView imageView = (ImageView) view;
            imageView.setFitWidth(163);
            imageView.setFitHeight(272);
            view.setTranslateX(4600);
            view.setTranslateY(220);
        }
        public void setREQUIRED_COINS(int c) {
            this.REQUIRED_COINS = c;
        }

        public void setLevel(int level) {
            this.level = level;
        }

        public void showLevelCompletionScreen() {
            Text completionMessage = new Text("LEVEL "+level+" COMPLETED");
            completionMessage.setFont(Font.loadFont(getClass().getResourceAsStream("/fonts/pixelated.ttf"), 50));
            completionMessage.setFill(Color.BLUE);
            completionMessage.setX((SCENE_WIDTH - completionMessage.getLayoutBounds().getWidth()) / 2);
            completionMessage.setY(SCENE_HEIGHT / 2);

            Text collectibleCount = new Text("Collectibles: " + collectiblesCount + "/" + REQUIRED_COINS);
            collectibleCount.setFont(Font.loadFont(getClass().getResourceAsStream("/fonts/pixelated.ttf"), 30));
            collectibleCount.setX((SCENE_WIDTH - collectibleCount.getLayoutBounds().getWidth()) / 2);
            collectibleCount.setY(SCENE_HEIGHT / 2 + 60);

            Button goBackButton = new Button("Back To Levels Menu");
            goBackButton.setLayoutX((SCENE_WIDTH / 2) - 60);
            goBackButton.setLayoutY(SCENE_HEIGHT / 2 + 90);
            goBackButton.setMinSize(100, 70);
            goBackButton.setOpacity(0);
            goBackButton.setId("button-text");

            FadeTransition fadeTransition = new FadeTransition(Duration.millis(1000), goBackButton);
            fadeTransition.setFromValue(0);
            fadeTransition.setToValue(1);
            fadeTransition.setDelay(Duration.millis(1000));

            goBackButton.setOnAction(e -> {
                goBackButton.setOnAction(null);
                fadeTransition.stop();
                player.resetState();
                showLevelsMenu();
            });

            Rectangle tint = new Rectangle(SCENE_WIDTH, SCENE_HEIGHT, new Color(0, 0, 0, 0.5));
            currentLevel.getChildren().addAll(tint, completionMessage, collectibleCount, goBackButton);

            fadeTransition.play();
            gameLoop.stop();
        }
    }

    // Player class
    public class Player extends GameObject {
        private final double speed = 5;
        private final double gravity = 0.5;
        private final double jumpSpeed = -10;
        private boolean canJump = true;
        private boolean onGround = false;

        public Player(String imagepath) {
            super(new ImageView(new Image(imagepath)));
            ImageView imageView = (ImageView) view;
            imageView.setFitWidth(50);
            imageView.setFitHeight(50);
            view.setTranslateX(100);
            view.setTranslateY(450);
        }

        public void handleKeyPress(KeyCode key) {
            switch (key) {
                case LEFT:
                    velocityX = -speed;
                    break;
                case RIGHT:
                    velocityX = speed;
                    break;
                case SPACE:
                    if (canJump) {
                        playSound(getClass().getResource("/sounds/jump.mp3").toExternalForm());
                        velocityY = jumpSpeed;
                        canJump = false;
                    }
                    break;
            }
        }

        public void handleKeyRelease(KeyCode key) {
            if (key == KeyCode.LEFT || key == KeyCode.RIGHT) {
                velocityX = 0;
            }
        }

        public void move() {
            if (view.getTranslateX() < SCROLL_THRESHOLD)
                view.setTranslateX(view.getTranslateX() + velocityX);

            if (view.getTranslateX() < sky.getTranslateX())
                view.setTranslateX(sky.getTranslateX());

            // Apply gravity if not on ground
            if (!onGround) {
                velocityY += gravity;
            }

            // Update vertical position
            view.setTranslateY(view.getTranslateY() + velocityY);

            // Reset onGround for next frame
            onGround = false;
        }

        public void land(double y) {
            onGround = true;
            canJump = true;
            velocityY = 0;
            view.setTranslateY(y);
        }


        public void stopVerticalMovement() {
            velocityY = 0;
        }

        public void stopHorizontalMovement() {
            velocityX = 0;
        }

        public void resetState() {
            view.setTranslateX(100);
            view.setTranslateY(450);
            velocityX = 0;
            velocityY = 0;
            canJump = true;
            collectiblesCount = 0;
        }
    }

    // Enemy class
    public class Enemy extends GameObject {
        public Enemy(double x, double y, double length) {
            super(new Rectangle(length, 40, Color.RED));
            view.setTranslateX(x);
            view.setTranslateY(y);
        }
        public Enemy(double x, double y, double length, double height) {
            super(new Rectangle(length, height, Color.RED));
            view.setTranslateX(x);
            view.setTranslateY(y);
        }
    }

    // Collectible class
    public class Collectible extends GameObject {
        private static final double MOVEMENT_RANGE = 3; // The range of the up and down movement
        private static final double ANIMATION_DURATION = 500; // Duration in milliseconds for one up-and-down cycle

        public Collectible(double x, double y) {
            super(new ImageView(new Image("C:\\Users\\abdul\\IdeaProjects\\Triventure\\src\\main\\resources\\images\\coin.png")));
            ImageView imageView = (ImageView) view;
            imageView.setX(x);
            imageView.setY(y);
            imageView.setFitWidth(50);
            imageView.setFitHeight(50);

            startAnimation();
        }

        private void startAnimation() {
            Timeline timeline = new Timeline(
                    new KeyFrame(Duration.ZERO, new KeyValue(view.translateYProperty(), view.getTranslateY())),
                    new KeyFrame(new Duration(ANIMATION_DURATION / 2), new KeyValue(view.translateYProperty(), view.getTranslateY() - MOVEMENT_RANGE)),
                    new KeyFrame(new Duration(ANIMATION_DURATION), new KeyValue(view.translateYProperty(), view.getTranslateY()))
            );
            timeline.setCycleCount(Timeline.INDEFINITE);
            timeline.setAutoReverse(true);
            timeline.play();
        }
    }

    // Platform class
    public class Platform extends GameObject {
        public Platform(double x, double y, double length) {
            super(new Rectangle(length, 30, Color.GREY));  // Assuming a fixed height of 30 for all platforms
            ((Rectangle) view).setStroke(Color.BLACK);
            ((Rectangle) view).setStrokeWidth(5);
            view.setTranslateX(x);
            view.setTranslateY(y);
        }
        public Platform(double x, double y, double length, double height) {
            super(new Rectangle(length, height, Color.GREY));  // Assuming a fixed height of 30 for all platforms
            ((Rectangle) view).setStroke(Color.BLACK);
            ((Rectangle) view).setStrokeWidth(5);
            view.setTranslateX(x);
            view.setTranslateY(y);
        }
    }

    // GameLevel1 class
    public class GameLevel1 extends GameLevel {
        @Override
        protected void createLevel() {
            // Helps with Scene Scrolling
            sky = new Rectangle(6000, SCENE_HEIGHT);
            sky.setX(0);
            sky.setVisible(false);

            // Intro Text
            Text intro = new Text("Welcome to Triventure\nUse arrow keys to move,\nand spacebar to jump");
            intro.setTextAlignment(TextAlignment.CENTER);
            intro.setY(200);
            intro.setX(80);
            intro.setFont(Font.loadFont(getClass().getResourceAsStream("/fonts/pixelated.ttf"), 50));

            // Enemy Tutorial Text
            Text enTut = new Text("Never touch\nthe red!\nTry jumping over it");
            enTut.setTextAlignment(TextAlignment.CENTER);
            enTut.setY(200);
            enTut.setX(1000);
            enTut.setFont(Font.loadFont(getClass().getResourceAsStream("/fonts/pixelated.ttf"), 50));

            // Collectible Tutorial Text
            Text colTut = new Text("These coins are important!\nCollect every coin\nin the level to progress");
            colTut.setTextAlignment(TextAlignment.CENTER);
            colTut.setY(100);
            colTut.setX(2500);
            colTut.setFont(Font.loadFont(getClass().getResourceAsStream("/fonts/pixelated.ttf"), 50));

            // Ending Text
            Text end = new Text("Now that you've got\nthe basics, it's time to\nget into the real adventure!");
            end.setTextAlignment(TextAlignment.CENTER);
            end.setY(200);
            end.setX(3700);
            end.setFont(Font.loadFont(getClass().getResourceAsStream("/fonts/pixelated.ttf"), 50));

            // Enemies
            Enemy e1 = new Enemy(1250, 450, 40);

            // Collectibles
            Collectible c1 = new Collectible(2525, 350);
            Collectible c2 = new Collectible(2775, 268);
            Collectible c3 = new Collectible(3050, 348);

            // Platforms
            Platform p1 = new Platform(2475, 400, 150);
            Platform p2 = new Platform(2725, 320, 150);
            Platform p3 = new Platform(3000, 400, 150);
            Platform floor = new Platform(0, 500, 6000);
            floor.getView().setVisible(false);

            // Gate
            Gate gate = new Gate(getClass().getResource("/images/gate.png").toExternalForm());
            gate.setLevel(1);
            gate.setREQUIRED_COINS(3);

            // Collectibles bar
            collectiblesBar = new Text("Collectibles: 0");
            collectiblesBar.setY(60);
            collectiblesBar.setX(30);
            collectiblesBar.setFont(Font.loadFont(getClass().getResourceAsStream("/fonts/pixelated.ttf"), 30));

            // Player Node
            player = new Player(getClass().getResource("/images/triangle.png").toExternalForm());

            // Background
            Image backgroundImage = new Image(getClass().getResource("/images/backrgound.png").toExternalForm());
            double scale = 700 / backgroundImage.getHeight();
            int numTilesHorizontal = (int) Math.ceil(5000 / backgroundImage.getWidth());
            double scaledWidth = backgroundImage.getWidth() * scale;
            for (int x = 0; x < numTilesHorizontal; x++) {
                ImageView imageView = new ImageView(backgroundImage);
                imageView.setFitHeight(700);
                imageView.setPreserveRatio(true);
                imageView.setX(x * scaledWidth);
                getChildren().add(imageView);
            }
            getChildren().addAll(sky, e1.getView(), intro, enTut, colTut, end, floor.getView(), c1.getView(), c2.getView(), c3.getView(), p1.getView(), p2.getView(), p3.getView(), gate.getView());
            getChildren().addAll(player.getView(), collectiblesBar);
        }
    }

    // GameLevel2 class
    public class GameLevel2 extends GameLevel {
        @Override
        protected void createLevel() {
            sky = new Rectangle(5000, SCENE_HEIGHT, Color.LIGHTGRAY);
            getChildren().add(sky);
            sky.setVisible(false);

            Image backgroundImage = new Image(getClass().getResource("/images/volcano.png").toExternalForm());
            double scale = SCENE_HEIGHT / backgroundImage.getHeight();
            int numTilesHorizontal = (int) Math.ceil(7200 / backgroundImage.getWidth());
            double scaledWidth = backgroundImage.getWidth() * scale;
            for (int x = 0; x < numTilesHorizontal; x++) {
                ImageView imageView = new ImageView(backgroundImage);
                imageView.setFitHeight(SCENE_HEIGHT);
                imageView.setPreserveRatio(true);
                imageView.setX(x * scaledWidth);
                getChildren().add(imageView);
            }

            // Floor Setup (as an enemy)
            Enemy floorAsEnemy = new Enemy(0, 570,7000); // Floor is 20px high
            getChildren().add(floorAsEnemy.getView());
            floorAsEnemy.getView().setVisible(false);

            // Intro text
            Text introTxt = new Text("The floor is lava!\nTry not to fall");
            introTxt.setStroke(Color.RED);
            introTxt.setTextAlignment(TextAlignment.CENTER);
            introTxt.setY(200);
            introTxt.setX(80);
            introTxt.setFont(Font.loadFont(getClass().getResourceAsStream("/fonts/pixelated.ttf"), 50));
            getChildren().add(introTxt);

            // Challenge Text
            Text chlgTxt = new Text("Easy right?\nYou could do better");
            chlgTxt.setStroke(Color.RED);
            chlgTxt.setTextAlignment(TextAlignment.CENTER);
            chlgTxt.setY(200);
            chlgTxt.setX(1650);
            chlgTxt.setFont(Font.loadFont(getClass().getResourceAsStream("/fonts/pixelated.ttf"), 50));
            getChildren().add(chlgTxt);

            // Platforms
            Platform p0 = new Platform(100, 500, 420);
            Platform p1 = new Platform(600, 500, 150);
            Platform p2 = new Platform(800, 420, 150);
            Platform p3 = new Platform(1000, 430, 150);
            Platform p4 = new Platform(800, 330, 150);
            Platform p5 = new Platform(1200, 400, 150);
            Platform p6 = new Platform(1400, 420, 1200);
            Platform p7 = new Platform(2800, 420, 5);
            Platform p8 = new Platform(2950, 400, 5);
            Platform p9 = new Platform(2800, 320, 5);
            Platform p10 = new Platform(3000, 320, 200);
            Platform p11 = new Platform(3450, 315, 150);
            Platform p12 = new Platform(3700, 540, 1000);

            getChildren().addAll(p0.getView(), p1.getView(), p2.getView(), p3.getView(), p4.getView(),
                    p5.getView(), p6.getView(), p7.getView(), p8.getView(), p9.getView(),
                    p10.getView(), p11.getView(), p12.getView());

            // Coins
            Collectible c1 = new Collectible(850, 280); // First coin
            Collectible c2 = new Collectible(2770, 230); // Second coin
            Collectible c3 = new Collectible(3500, 265); // Third coin
            getChildren().addAll(c1.getView(), c2.getView(), c3.getView());


            // Gate
            Gate gate = new Gate(getClass().getResource("/images/gate.png").toExternalForm());
            gate.getView().setTranslateX(4500);
            gate.getView().setTranslateY(271);
            gate.setLevel(2);
            gate.setREQUIRED_COINS(3);
            getChildren().add(gate.getView());

            // Player Setup
            player = new Player(getClass().getResource("/images/triangle.png").toExternalForm());
            player.getView().setTranslateX(100); // Starting position on the first platform
            player.getView().setTranslateY(440); // Adjust Y position as per the first platform's height
            getChildren().add(player.getView());

            // Collectibles Bar
            collectiblesBar = new Text("Collectibles: 0");
            collectiblesBar.setY(60);
            collectiblesBar.setX(30);
            collectiblesBar.setFont(Font.loadFont(getClass().getResourceAsStream("/fonts/pixelated.ttf"), 30));
            getChildren().add(collectiblesBar);
        }
    }

    // GameLevel3 class
    public class GameLevel3 extends GameLevel {
        @Override
        protected void createLevel() {
            sky = new Rectangle(6000, SCENE_HEIGHT);
            sky.setX(0);
            sky.setVisible(false);

            // Background
            Image backgroundImage = new Image(getClass().getResource("/images/backrgound.png").toExternalForm());
            double scale = SCENE_HEIGHT+100 / backgroundImage.getHeight();
            int numTilesHorizontal = (int) Math.ceil(5000 / backgroundImage.getWidth());
            double scaledWidth = backgroundImage.getWidth() * scale;
            for (int x = 0; x < numTilesHorizontal; x++) {
                ImageView imageView = new ImageView(backgroundImage);
                imageView.setFitHeight(SCENE_HEIGHT+100);
                imageView.setPreserveRatio(true);
                imageView.setX(x * scaledWidth);
                getChildren().add(imageView);
            }

            // Platforms
            Platform p1 = new Platform(300, 400, 150);
            Platform p2 = new Platform(500, 320, 150);
            Platform p3 = new Platform(700, 240, 150);
            Platform p4 = new Platform(900, 210, 5, 390);
            Platform p5 = new Platform(900, 0, 5, 140);
            Platform floor = new Platform(0, 500, 6000);
            floor.getView().setVisible(false);
            getChildren().addAll(p1.getView(), p2.getView(), p3.getView(), p4.getView(), p5.getView(), floor.getView());

            // Enemies
            Enemy e1 = new Enemy(900, 210, 5, 40);
            Enemy e2 = new Enemy(900, 100, 5, 40);
            getChildren().addAll(e1.getView(), e2.getView());

            // Gate
            Gate gate = new Gate(getClass().getResource("/images/gate.png").toExternalForm());
            gate.getView().setTranslateX(1500);
            gate.getView().setTranslateY(271);
            gate.setLevel(2);
            gate.setREQUIRED_COINS(3);
            getChildren().add(gate.getView());

            // Collectibles
            Collectible c1 = new Collectible(350, 350);
            Collectible c2 = new Collectible(550, 270);
            Collectible c3 = new Collectible(750, 190);
            getChildren().addAll(c1.getView(), c2.getView(), c3.getView());

            // Collectibles bar
            collectiblesBar = new Text("Collectibles: 0");
            collectiblesBar.setY(60);
            collectiblesBar.setX(30);
            collectiblesBar.setFont(Font.loadFont(getClass().getResourceAsStream("/fonts/pixelated.ttf"), 30));
            getChildren().add(collectiblesBar);

            // Player Node
            player = new Player(getClass().getResource("/images/triangle.png").toExternalForm());
            getChildren().add(player.getView());
        }
    }
}