package ua.masalytin.subtitleplayer;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.io.IOException;

public class Program extends Application {
    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(Program.class.getResource("player.fxml"));
        Scene scene = new Scene(fxmlLoader.load());
        stage.initStyle(StageStyle.UNDECORATED);
        stage.setTitle("Subtitle Player");
        stage.setScene(scene);
        stage.setAlwaysOnTop(true);
        stage.setOpacity(0.7);
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}