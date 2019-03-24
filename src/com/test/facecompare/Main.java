package com.test.facecompare;

import javafx.animation.FadeTransition;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.layout.BorderPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;

public class Main extends Application {

    private static double WIDTH = 400;
    private static double HEIGHT = 300;

    private Scene firstImageScene;
    private Scene secondImageScene;
    private Scene resultScene;
    private Scene transitionScene;

    private final FileChooser firstImageChooser = new FileChooser();
    private final FileChooser secondImageChooser = new FileChooser();

    private File firstImage = null;
    private File secondImage = null;

    private final TextArea resultTextArea = new TextArea("This is result");

    {
        resultTextArea.setEditable(false);
        resultTextArea.setMaxWidth(200);
        resultTextArea.setMaxHeight(200);
    }

    private final MicrosoftAzureFaceClient apiClient = new MicrosoftAzureFaceClient();

    @Override
    public void start(Stage primaryStage) throws Exception {

        primaryStage.setTitle("Face Compare v0.1");
        primaryStage.setResizable(false);

        initFirstImageScene(primaryStage);
        initSecondImageScene(primaryStage);
        initResultScene(primaryStage);
        initTransitionScene();

        primaryStage.setScene(firstImageScene);
        primaryStage.show();

    }


    /**
     * Init first scene
     * @param primaryStage Primary stage
     */
    private void initFirstImageScene(Stage primaryStage) {
        Button uploadButton = new Button("Please upload first image");
        uploadButton.setOnAction(e -> {
            firstImage = firstImageChooser.showOpenDialog(primaryStage);
        });
        Button nextButton = new Button("Next");
        nextButton.setOnAction(e -> primaryStage.setScene(secondImageScene));
        BorderPane layout = new BorderPane(uploadButton);
        layout.setRight(nextButton);
        firstImageScene = new Scene(layout, WIDTH, HEIGHT);
    }


    /**
     * Init second scene
     * @param primaryStage Primary stage
     */
    private void initSecondImageScene(Stage primaryStage) {
        Button uploadButton = new Button("Please upload second image");
        uploadButton.setOnAction(e -> {
            secondImage = secondImageChooser.showOpenDialog(primaryStage);
        });
        Button prevButton = new Button("Back");
        prevButton.setOnAction(e -> primaryStage.setScene(firstImageScene));
        Button nextButton = new Button("Next");
        nextButton.setOnAction(e -> {
            primaryStage.setScene(transitionScene);
            actionResult(primaryStage, e);
        });
        BorderPane layout = new BorderPane(uploadButton);
        layout.setLeft(prevButton);
        layout.setRight(nextButton);
        secondImageScene = new Scene(layout, WIDTH, HEIGHT);
    }


    /**
     * Init result scene
     * @param primaryStage Primary stage
     */
    private void initResultScene(Stage primaryStage) {
        Button prevButton = new Button("Back");
        prevButton.setOnAction(e -> primaryStage.setScene(secondImageScene));
        BorderPane layout = new BorderPane(resultTextArea);
        layout.setLeft(prevButton);
        resultScene = new Scene(layout, WIDTH, HEIGHT, Color.WHEAT);
    }

    private void initTransitionScene() {

        Circle cir = new Circle(200,150,20);
        cir.setFill(Color.GRAY);
        cir.setStroke(Color.GRAY);

        FadeTransition fade = new FadeTransition();
        fade.setDuration(Duration.millis(500));
        fade.setFromValue(10);
        fade.setToValue(0);
        fade.setCycleCount(1000);
        fade.setAutoReverse(true);
        fade.setNode(cir);
        fade.play();

        Group root = new Group();
        root.getChildren().addAll(cir);
        transitionScene = new Scene(root, WIDTH, HEIGHT);

    }


    /**
     * Execute result action
     * @param primaryStage Primary stage
     */
    private void actionResult(Stage primaryStage, ActionEvent event) {

        Runnable action = () -> {
            try {
                String result = apiClient.getFacesSimilarity(firstImage, secondImage);
                resultTextArea.setText(result);
                Platform.runLater(() -> primaryStage.setScene(resultScene));
            } catch (IOException | URISyntaxException e) {
                e.printStackTrace();
            }
        };

        Thread actionThread = new Thread(action);
        actionThread.setDaemon(true);
        actionThread.start();

    }


    public static void main(String[] args) {
        launch(args);
    }

}
