module ua.masalytin.subtitleplayer {
    requires javafx.controls;
    requires javafx.fxml;


    opens ua.masalytin.subtitleplayer to javafx.fxml;
    exports ua.masalytin.subtitleplayer;
}