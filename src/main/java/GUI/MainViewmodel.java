package GUI;

import javafx.application.Platform;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.converter.NumberStringConverter;
import wikiAPI.WikiFileDumpParser;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;

public class MainViewmodel {

    @FXML
    TextField pathTextBox;
    @FXML
    TextField maxTextBox;
    @FXML
    ProgressBar progressBar;
    @FXML
    ComboBox threadNumberComboBox;
    @FXML
    ComboBox langComboBox;
    @FXML
    TextArea consoleTextArea;
    PrintStream ps;
    private StringProperty path = new SimpleStringProperty();
    private IntegerProperty max = new SimpleIntegerProperty();

    public void initialize(){
        pathTextBox.textProperty().bindBidirectional(path);
        maxTextBox.textProperty().bindBidirectional(max, new NumberStringConverter());

        threadNumberComboBox.getItems().addAll(
                "1", "2", "3", "4", "5", "6", "7", "8"
        );

        langComboBox.getItems().addAll(
                "DE", "EN", "IT", "PL", "RU"
        );

        Console console = new Console(consoleTextArea);
        ps = new PrintStream(console, true);
        System.setOut(ps);
        System.setErr(ps);
    }



    @FXML
    private void openFile(ActionEvent event) {
        FileChooser fileChooser = new FileChooser();
        File file = fileChooser.showOpenDialog(new Stage());
        if (file != null) {
            System.out.println("Open file: " + file.getAbsolutePath());

        }
        path.setValue( file.getAbsolutePath());
    }

    @FXML
    private void runParser(ActionEvent e) {
        int threadcount = threadNumberComboBox.getSelectionModel().getSelectedIndex() + 1;

        Task copyWorker = new WikiFileDumpParser(threadcount, max.getValue(), true, false, path.get(), null);
        progressBar.progressProperty().unbind();
        progressBar.progressProperty().bind(copyWorker.progressProperty());
        new Thread(copyWorker).start();
    }


    public static class Console extends OutputStream {
        private TextArea output;

        public Console(TextArea ta) {
            this.output = ta;
        }

        @Override
        public void write(int i) throws IOException {
            Platform.runLater(() -> output.appendText(String.valueOf((char) i)));
        }
    }


}
