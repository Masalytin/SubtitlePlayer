package ua.masalytin.subtitleplayer;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Queue;
import java.util.concurrent.LinkedBlockingQueue;
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
    private volatile Label startLabel;

    @FXML
    private volatile Label textLabel;
    private static final Image PLAY_BUTTON_IMAGE = new Image(PlayerController.class.getResourceAsStream("/icons/play.png"));
    private static final Image STOP_BUTTON_IMAGE = new Image(PlayerController.class.getResourceAsStream("/icons/stop.png"));
    private static final Image EXIT_BUTTON_IMAGE = new Image(PlayerController.class.getResourceAsStream("/icons/exit.png"));
    private static final Image CHOOSE_FILE_BUTTON_IMAGE = new Image(PlayerController.class.getResourceAsStream("/icons/folder.png"));

    private volatile boolean isPlaying;
    private double xOffset = 0;
    private double yOffset = 0;
    private Queue<SubtitleFragment> fragments = new LinkedBlockingQueue<>();
    private String filePath;
    private LocalTime currentTime = LocalTime.of(0, 0,0);
    private LocalTime finisTime;

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    @FXML
    private void initialize() {
        currentTime.format(DateTimeFormatter.ISO_LOCAL_TIME);
        playAndStopButton.setGraphic(new ImageView(PLAY_BUTTON_IMAGE));
        playAndStopButton.setMaxSize(16,16);
        chooseFileButton.setGraphic(new ImageView(CHOOSE_FILE_BUTTON_IMAGE));
        chooseFileButton.setMaxSize(16,16);
        exitButton.setGraphic(new ImageView(EXIT_BUTTON_IMAGE));
        exitButton.setMaxSize(16,16);
        textLabel.setOnMousePressed(this::onLabelMousePressed);
        textLabel.setOnMouseDragged(this::onLabelMouseDragged);
        exitButton.setOnAction(this::exit);
        playAndStopButton.setOnAction(this::playAndStopHandler);
        chooseFileButton.setOnAction(this::chooseFile);
        if (validate()) {
            currentTime = LocalTime.of(0,0,0);
        }
    }

    private void onLabelMouseDragged(MouseEvent mouseEvent) {
        Stage primaryStage = (Stage) exitButton.getScene().getWindow();
        textLabel.setOnMouseDragged((MouseEvent event) -> {
            primaryStage.setX(event.getScreenX() - xOffset);
            primaryStage.setY(event.getScreenY() - yOffset);
        });
    }

    private void onLabelMousePressed(MouseEvent mouseEvent) {
        textLabel.setOnMousePressed((MouseEvent event) -> {
            xOffset = event.getSceneX();
            yOffset = event.getSceneY();
        });
    }

    private void chooseFile(ActionEvent actionEvent) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Выберите файл");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Subtitle file (*vtt)", "*.vtt"));
        java.io.File selectedFile = fileChooser.showOpenDialog(chooseFileButton.getScene().getWindow());
        if (selectedFile != null) {
            filePath = selectedFile.getPath();
            readFragments();
        }
    }

    private void playAndStopHandler(ActionEvent actionEvent) {
        if (filePath == null || filePath.isEmpty()) {

        } else {
            if (isPlaying) {
                stop();
            } else {
                new Thread(this::startPlaying).start();
            }
        }
    }

    private void exit(ActionEvent actionEvent) {
        Stage currentStage = (Stage) exitButton.getScene().getWindow();
        currentStage.close();
    }

    private void readFragments() {
        try {
            Pattern pattern = Pattern.compile("(\\d{2}\\:\\d{2}\\:\\d{2})\\.\\d{3} \\-\\-\\> (\\d{2}\\:\\d{2}\\:\\d{2})\\.\\d{3}([\\-\\D\\n]*)\\n");
            String text = Files.readAllLines(Path.of(filePath)).stream().collect(Collectors.joining("\n"));
            Matcher matcher = pattern.matcher(text);
            while (matcher.find()) {
                LocalTime start = LocalTime.parse(matcher.group(1), DateTimeFormatter.ISO_LOCAL_TIME);
                LocalTime finish = LocalTime.parse(matcher.group(2),
                        DateTimeFormatter.ISO_LOCAL_TIME);
                fragments.add(new SubtitleFragment(start, finish, matcher.group(3)));
                finisTime = finish;
            }
            textLabel.setText(fragments.peek().text);
            slider.setMax((finisTime.getMinute() * 60) + finisTime.getSecond());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private boolean validate() {
        return true;
    }

    private void startPlaying() {
        if (currentTime.equals(finisTime)) {
            currentTime = LocalTime.of(0,0,0);
            isPlaying = false;
            //change button image
        }
        while (!fragments.isEmpty()) {
            SubtitleFragment currentFragment = fragments.poll();
            Platform.runLater(() -> textLabel.setText(currentFragment.text));
            while (currentTime.isBefore(currentFragment.finish)) {
                try {
                    Thread.sleep(1000);
                    currentTime = currentTime.plusSeconds(1);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }

    private void stop() {
    }
    private static class SubtitleFragment implements Comparable<SubtitleFragment> {

        LocalTime start;
        LocalTime finish;
        String text;

        public SubtitleFragment(LocalTime start, LocalTime finish, String text) {
            this.start = start;
            this.finish = finish;
            this.text = text;
        }

        @Override
        public int compareTo(SubtitleFragment o) {
            return finish.compareTo(o.finish);
        }
    }
}
