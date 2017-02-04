package GUI;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Main extends Application {

    public static void main(String[] args) {
        launch();
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        Parent root = FXMLLoader.load(getClass().getResource("/Views/parserGUI.fxml"));
        primaryStage.setTitle("WikiDefine");
        primaryStage.setScene(new Scene(root, 300, 275));
        primaryStage.show();

    }
}
