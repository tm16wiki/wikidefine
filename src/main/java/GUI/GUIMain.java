package GUI;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

/**
 * Created by Christian on 01.03.2017.
 */
public class GUIMain extends Application {
    /**
     * Starts the GUI
     *
     * @param primaryStage Window
     * @throws Exception FXML Exception
     */
    @Override
    public void start(Stage primaryStage) throws Exception {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/Views/parserGUI.fxml"));
            primaryStage.setTitle("WikiDefine");
            primaryStage.setScene(new Scene(root));
            primaryStage.setResizable(false);
            primaryStage.sizeToScene();
            primaryStage.getIcons().add(
                    new javafx.scene.image.Image(
                            GUIMain.class.getResourceAsStream("/Theme/WikiDefineIcon.png"))
            );

            GUIMain.class.getResource("/Theme/WikiDefineIcon.png");
            primaryStage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
