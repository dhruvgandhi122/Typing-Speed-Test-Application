package com.typemaster;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.util.Duration;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import javafx.stage.Stage;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Random;

public class Main {
    public static void main(String[] args) {
        Application.launch(TypeMasterApp.class, args);
    }

    public static class TypeMasterApp extends Application {
        @Override
        public void start(Stage primaryStage) throws Exception {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/main.fxml"));
            Parent root = loader.load();
            
            Scene scene = new Scene(root, 820, 620);
            scene.getStylesheets().add(getClass().getResource("/css/style.css").toExternalForm());
            
            primaryStage.setTitle("TypeMaster - Typing Speed Test");
            primaryStage.setScene(scene);
            primaryStage.show();
        }
    }

    public static class TypeMasterController {
        // Navigation Panels
        @FXML private VBox homePane;
        @FXML private VBox testPane;
        @FXML private VBox resultPane;

        // Home Controls
        @FXML private ToggleGroup difficultyGroup;
        @FXML private ToggleButton easyBtn;
        @FXML private ToggleButton mediumBtn;
        @FXML private ToggleButton hardBtn;

        // Test Controls & Labels
        @FXML private Text difficultyLabel;
        @FXML private Text wpmText;
        @FXML private Text accuracyText;
        @FXML private Text timeText;
        @FXML private ScrollPane scrollPane;
        @FXML private TextFlow textFlow;
        @FXML private TextField hiddenInput;

        // Result Controls & Labels
        @FXML private Text feedbackText;
        @FXML private Text finalWpmText;
        @FXML private Text finalAccuracyText;
        @FXML private Text summaryText;

        private enum CharacterStatus {
            UNTYPED,
            CORRECT,
            INCORRECT
        }

        private String targetText;
        private CharacterStatus[] typedStatus;
        private int currentIndex = 0;
        private int totalKeystrokes = 0;
        private int timeLeft = 60;
        private boolean testActive = false;
        private boolean testFinished = false;
        private Timeline timeline;
        private String currentDifficultyFile = "easy.txt";
        private String currentDifficultyName = "EASY MODE";

        @FXML
        public void initialize() {
            // Transfer focus to hidden input whenever user clicks on typing area
            scrollPane.setOnMouseClicked(event -> hiddenInput.requestFocus());
            textFlow.setOnMouseClicked(event -> hiddenInput.requestFocus());
            
            // Input key typed listener for normal characters
            hiddenInput.setOnKeyTyped(event -> {
                String character = event.getCharacter();
                if (character.length() == 1 && character.charAt(0) >= 32 && character.charAt(0) != 127) {
                    handleCharacterTyped(character.charAt(0));
                }
            });
            
            // Input key pressed listener for backspace
            hiddenInput.setOnKeyPressed(event -> {
                if (event.getCode() == KeyCode.BACK_SPACE) {
                    handleBackspace();
                }
            });
        }

        @FXML
        private void handleStartTest() {
            // Read selected difficulty level
            if (difficultyGroup.getSelectedToggle() == easyBtn) {
                currentDifficultyFile = "easy.txt";
                currentDifficultyName = "EASY MODE";
            } else if (difficultyGroup.getSelectedToggle() == mediumBtn) {
                currentDifficultyFile = "medium.txt";
                currentDifficultyName = "MEDIUM MODE";
            } else if (difficultyGroup.getSelectedToggle() == hardBtn) {
                currentDifficultyFile = "hard.txt";
                currentDifficultyName = "HARD MODE";
            }

            difficultyLabel.setText(currentDifficultyName);
            startNewTestSession();
        }

        @FXML
        private void handleRestart() {
            startNewTestSession();
        }

        @FXML
        private void handleGoHome() {
            if (timeline != null) {
                timeline.stop();
            }
            testActive = false;
            testFinished = false;
            
            // Toggle view panels
            homePane.setVisible(true);
            testPane.setVisible(false);
            resultPane.setVisible(false);
        }

        private void startNewTestSession() {
            if (timeline != null) {
                timeline.stop();
            }
            testActive = false;
            testFinished = false;
            currentIndex = 0;
            totalKeystrokes = 0;
            timeLeft = 60;

            hiddenInput.setDisable(false);
            hiddenInput.clear();

            loadParagraphForDifficulty();

            wpmText.setText("0");
            accuracyText.setText("100%");
            timeText.setText(String.valueOf(timeLeft));

            // Toggle view panels
            homePane.setVisible(false);
            testPane.setVisible(true);
            resultPane.setVisible(false);

            Platform.runLater(() -> hiddenInput.requestFocus());
        }

        private void loadParagraphForDifficulty() {
            try (InputStream is = getClass().getResourceAsStream("/paragraphs/" + currentDifficultyFile)) {
                if (is == null) {
                    targetText = "The quick brown fox jumps over the lazy dog. Backup text loaded.";
                } else {
                    BufferedReader reader = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8));
                    StringBuilder sb = new StringBuilder();
                    String line;
                    java.util.List<String> paragraphs = new java.util.ArrayList<>();
                    while ((line = reader.readLine()) != null) {
                        if (line.trim().equals("---")) {
                            if (sb.length() > 0) {
                                paragraphs.add(sb.toString().trim());
                                sb.setLength(0);
                            }
                        } else {
                            sb.append(line).append(" ");
                        }
                    }
                    if (sb.length() > 0) {
                        paragraphs.add(sb.toString().trim());
                    }
                    
                    if (paragraphs.isEmpty()) {
                        targetText = "The quick brown fox jumps over the lazy dog.";
                    } else {
                        Random rand = new Random();
                        targetText = paragraphs.get(rand.nextInt(paragraphs.size()));
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
                targetText = "The quick brown fox jumps over the lazy dog. Error reading resource.";
            }

            // Standardize spaces
            targetText = targetText.replaceAll("\\s+", " ").trim();

            // Set up characters in TextFlow
            textFlow.getChildren().clear();
            typedStatus = new CharacterStatus[targetText.length()];
            for (int i = 0; i < targetText.length(); i++) {
                String charStr = String.valueOf(targetText.charAt(i));
                Text t = new Text(charStr);
                t.getStyleClass().add("char-untyped");
                textFlow.getChildren().add(t);
                typedStatus[i] = CharacterStatus.UNTYPED;
            }

            // Set blue highlight for the first next character
            if (!textFlow.getChildren().isEmpty()) {
                textFlow.getChildren().get(0).getStyleClass().clear();
                textFlow.getChildren().get(0).getStyleClass().add("char-cursor");
            }
        }

        private void handleCharacterTyped(char typedChar) {
            if (testFinished || currentIndex >= targetText.length()) {
                return;
            }

            if (!testActive) {
                startTest();
            }

            char expectedChar = targetText.charAt(currentIndex);
            totalKeystrokes++;

            if (typedChar == expectedChar) {
                typedStatus[currentIndex] = CharacterStatus.CORRECT;
            } else {
                typedStatus[currentIndex] = CharacterStatus.INCORRECT;
            }

            currentIndex++;
            updateTextFlow();
            updateStats();

            // Auto-finish if paragraph is completed
            if (currentIndex >= targetText.length()) {
                finishTest();
            }
        }

        private void handleBackspace() {
            if (testFinished || currentIndex <= 0) {
                return;
            }

            currentIndex--;
            typedStatus[currentIndex] = CharacterStatus.UNTYPED;

            updateTextFlow();
            updateStats();
        }

        private void startTest() {
            testActive = true;
            timeline = new Timeline(new KeyFrame(Duration.seconds(1), event -> {
                timeLeft--;
                if (timeLeft <= 0) {
                    timeLeft = 0;
                    finishTest();
                }
                updateStats();
            }));
            timeline.setCycleCount(Animation.INDEFINITE);
            timeline.play();
        }

        private void finishTest() {
            testActive = false;
            testFinished = true;
            if (timeline != null) {
                timeline.stop();
            }
            hiddenInput.setDisable(true);

            // Compute final statistics
            int correctCount = 0;
            int errorCount = 0;
            for (int i = 0; i < currentIndex; i++) {
                if (typedStatus[i] == CharacterStatus.CORRECT) {
                    correctCount++;
                } else if (typedStatus[i] == CharacterStatus.INCORRECT) {
                    errorCount++;
                }
            }

            double minutesElapsed = (60.0 - timeLeft) / 60.0;
            if (minutesElapsed <= 0) {
                minutesElapsed = 0.0001;
            }
            int finalWpm = (int) Math.round((correctCount / 5.0) / minutesElapsed);

            double accuracy = 100.0;
            if (totalKeystrokes > 0) {
                accuracy = ((double) correctCount / totalKeystrokes) * 100.0;
            }

            // Display results on Results panel
            finalWpmText.setText(String.valueOf(finalWpm));
            finalAccuracyText.setText(String.format(java.util.Locale.US, "%.0f%%", accuracy));
            summaryText.setText(String.format(java.util.Locale.US, "Raw Keystrokes: %d | Correct: %d | Mistakes: %d", 
                    totalKeystrokes, correctCount, errorCount));

            // Feedback generation
            if (finalWpm < 30) {
                feedbackText.setText("Keep practicing! Consistency leads to muscle memory.");
            } else if (finalWpm < 50) {
                feedbackText.setText("Good speed! Work on precision to break 50 WPM.");
            } else if (finalWpm < 80) {
                feedbackText.setText("Excellent typing speed! You type faster than average!");
            } else {
                feedbackText.setText("Phenomenal work! You are a master of the keys!");
            }

            // Transition to Result Pane
            homePane.setVisible(false);
            testPane.setVisible(false);
            resultPane.setVisible(true);
        }

        private void updateTextFlow() {
            for (int i = 0; i < targetText.length(); i++) {
                Text t = (Text) textFlow.getChildren().get(i);
                t.getStyleClass().clear();
                if (i < currentIndex) {
                    if (typedStatus[i] == CharacterStatus.CORRECT) {
                        t.getStyleClass().add("char-correct");
                    } else {
                        t.getStyleClass().add("char-incorrect");
                    }
                } else if (i == currentIndex) {
                    t.getStyleClass().add("char-cursor");
                } else {
                    t.getStyleClass().add("char-untyped");
                }
            }
        }

        private void updateStats() {
            int correctCount = 0;
            for (int i = 0; i < currentIndex; i++) {
                if (typedStatus[i] == CharacterStatus.CORRECT) {
                    correctCount++;
                }
            }

            // WPM formula (5 characters = 1 word)
            double minutesElapsed = (60.0 - timeLeft) / 60.0;
            if (minutesElapsed <= 0) {
                minutesElapsed = 0.0001;
            }
            int wpm = (int) Math.round((correctCount / 5.0) / minutesElapsed);
            wpmText.setText(String.valueOf(wpm));

            // Accuracy
            double accuracy = 100.0;
            if (totalKeystrokes > 0) {
                accuracy = ((double) correctCount / totalKeystrokes) * 100.0;
            }
            accuracyText.setText(String.format(java.util.Locale.US, "%.0f%%", accuracy));

            // Timer
            timeText.setText(String.valueOf(timeLeft));
        }
    }
}
