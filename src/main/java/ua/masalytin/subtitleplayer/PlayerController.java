package ua.masalytin.subtitleplayer;

import javafx.application.Platform;
import javafx.beans.Observable;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.scene.text.TextFlow;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class PlayerController {

    @FXML
    private Button chooseFileButton;

    @FXML
    private Button exitButton;

    @FXML
    private Button playAndStopButton;

    @FXML
    private volatile Slider slider;

    @FXML
    private volatile TextFlow textFlow;

    @FXML
    private volatile Label timeLabel;

    private static final Image PLAY_BUTTON_IMAGE = new Image(PlayerController.class.getResourceAsStream("/icons/play.png"));
    private static final Image PAUSE_BUTTON_IMAGE = new Image(PlayerController.class.getResourceAsStream("/icons/pause.png"));
    private static final Image EXIT_BUTTON_IMAGE = new Image(PlayerController.class.getResourceAsStream("/icons/exit.png"));
    private static final Image CHOOSE_FILE_BUTTON_IMAGE = new Image(PlayerController.class.getResourceAsStream("/icons/folder.png"));
    private Text subtitleText = new Text();
    private volatile boolean isPlaying;
    private double xOffset = 0;
    private double yOffset = 0;
    private LocalTime currentTime = LocalTime.MIN;
    private String filePath;
    private List<SubtitleFragment> fragments = new ArrayList<>();

    @FXML
    private void initialize() {
        playAndStopButton.setGraphic(new ImageView(PLAY_BUTTON_IMAGE));
        chooseFileButton.setGraphic(new ImageView(CHOOSE_FILE_BUTTON_IMAGE));
        exitButton.setGraphic(new ImageView(EXIT_BUTTON_IMAGE));
        subtitleText.setFont(Font.font("Arial", 24));
        textFlow.setTextAlignment(TextAlignment.CENTER);
        textFlow.getChildren().add(subtitleText);
        textFlow.setOnMousePressed(this::onLabelMousePressed);
        textFlow.setOnMouseDragged(this::onLabelMouseDragged);
        exitButton.setOnAction(this::exit);
        playAndStopButton.setOnAction(this::playAndStopHandler);
        chooseFileButton.setOnAction(this::chooseFile);
        slider.valueProperty().addListener(this::sliderChangeValueHandler);
    }

    private void chooseFile(ActionEvent actionEvent) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Choose subtitle file (*.vtt)");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Subtitle file (*vtt)", "*.vtt"));
        java.io.File selectedFile = fileChooser.showOpenDialog(chooseFileButton.getScene().getWindow());
        if (selectedFile != null) {
            filePath = selectedFile.getPath();
            readFragments();
            slider.setValue(0);
        }
    }

    private void playAndStopHandler(ActionEvent actionEvent) {
        if (filePath == null || filePath.isEmpty()) {
            subtitleText.setText("Choose file");
        } else {
            if (isPlaying) {
                stop();
            } else {
                new Thread(this::play).start();
            }
        }
    }

    private void exit(ActionEvent actionEvent) {
        System.exit(0);
    }

    private void sliderChangeValueHandler(Observable observable, Number oldValue, Number newValue) {
        if (filePath == null || filePath.isEmpty()) {
            return;
        }
        LocalTime newTime = LocalTime.MIN;
        currentTime = newTime;
        updateUI();
    }

    private void onLabelMouseDragged(MouseEvent mouseEvent) {
        Stage primaryStage = (Stage) exitButton.getScene().getWindow();
        textFlow.setOnMouseDragged((MouseEvent event) -> {
            primaryStage.setX(event.getScreenX() - xOffset);
            primaryStage.setY(event.getScreenY() - yOffset);
        });
    }

    private void onLabelMousePressed(MouseEvent mouseEvent) {
        textFlow.setOnMousePressed((MouseEvent event) -> {
            xOffset = event.getSceneX();
            yOffset = event.getSceneY();
        });
    }

    private void readFragments() {
        try {
            Pattern pattern = Pattern.compile("([\\d:]+)[.\\d]+\\s-->\\s([\\d:]+)[.\\d]+\\s+([\\d\\D]+?)(?=\\n\\n)");
            String text = Files.readAllLines(Path.of(filePath)).stream().collect(Collectors.joining("\n"));
            Matcher matcher = pattern.matcher(text);
            while (matcher.find()) {
                LocalTime start = LocalTime.parse(matcher.group(1), DateTimeFormatter.ISO_LOCAL_TIME);
                LocalTime finish = LocalTime.parse(matcher.group(2),
                        DateTimeFormatter.ISO_LOCAL_TIME);
                fragments.add(new SubtitleFragment(start, finish, matcher.group(3)));
            }
            subtitleText.setText(fragments.get(0).getText());
            LocalTime finisTime = fragments.get(fragments.size() - 1).getFinish();
            slider.setMax((finisTime.getMinute() * 60) + finisTime.getSecond());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void play() {
        Platform.runLater(() -> playAndStopButton.setGraphic(new ImageView(PAUSE_BUTTON_IMAGE)));
        isPlaying = true;
        while (currentTime.isBefore(fragments.get(fragments.size() - 1).getFinish()) && isPlaying) {
            try {
                Thread.sleep(1000);
                Platform.runLater(() -> slider.setValue((currentTime.getMinute() * 60) + currentTime.getSecond() + 1));
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
        Platform.runLater(() -> slider.setValue(0));
        play();
    }

    private void stop() {
        playAndStopButton.setGraphic(new ImageView(PLAY_BUTTON_IMAGE));
        isPlaying = false;
    }

    private SubtitleFragment findFragmentByTime(LocalTime time) {
        if (time.equals(LocalTime.MIN))
            return fragments.get(0);
        return fragmentsBinarySearch(time, 0, fragments.size() - 1);
    }

    private SubtitleFragment fragmentsBinarySearch(LocalTime time, int first, int last) {
        if (first > last) {
            return null;
        }
        int mid = first + (last - first) / 2;
        LocalTime middleFragmentStartTime = fragments.get(mid).getStart();
        if (middleFragmentStartTime.equals(time)) {
            return fragments.get(mid);
        }
        if (middleFragmentStartTime.isBefore(time) && fragments.get(mid).getFinish().isAfter(time)) {
            return fragments.get(mid);
        }
        if (middleFragmentStartTime.isAfter(time)) {
            return fragmentsBinarySearch(time, first, mid - 1);
        }
        if (middleFragmentStartTime.isBefore(time)) {
            return fragmentsBinarySearch(time, mid + 1, last);
        }
        return null;
    }

    private void updateUI() {
        SubtitleFragment fragment = findFragmentByTime(currentTime);
        timeLabel.setText(currentTime.toString());
        if (fragment != null)
            subtitleText.setText(fragment.getText());
    }

    public class SubtitleFragment {
        private LocalTime start;
        private LocalTime finish;
        private String text;

        public SubtitleFragment(LocalTime start, LocalTime finish, String text) {
            this.start = start;
            this.finish = finish;
            this.text = text;
        }

        public LocalTime getStart() {
            return start;
        }

        public void setStart(LocalTime start) {
            this.start = start;
        }

        public LocalTime getFinish() {
            return finish;
        }

        public void setFinish(LocalTime finish) {
            this.finish = finish;
        }

        public String getText() {
            return text;
        }

        public void setText(String text) {
            this.text = text;
        }
    }

}
