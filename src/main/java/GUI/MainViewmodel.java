package GUI;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.TextField;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import wikiAPI.WikiFileDumpParser;

import java.io.File;

public class MainViewmodel {

    private StringProperty path = new SimpleStringProperty(" ");

    @FXML
    TextField pathTextBox;
    @FXML
    ProgressBar progressBar;



    public void initialize(){
        pathTextBox.textProperty().bindBidirectional(path);
    }



    @FXML
    private void openFile(ActionEvent event) {
        FileChooser fileChooser = new FileChooser();
        File file = fileChooser.showOpenDialog(new Stage());
        if (file != null) {
            System.out.print(file.getAbsolutePath());
        }
        path.setValue( file.getAbsolutePath());
    }

    @FXML
    private void runParser(ActionEvent e) {
        Task copyWorker = new WikiFileDumpParser(8, 2000, true, true, path.get(), null);
        progressBar.progressProperty().unbind();
        progressBar.progressProperty().bind(copyWorker.progressProperty());
        copyWorker.messageProperty().addListener(new ChangeListener<String>() {
            public void changed(ObservableValue<? extends String> observable,
                                String oldValue, String newValue) {
                System.out.println(newValue);
            }
        });
        new Thread(copyWorker).start();
    }

}
