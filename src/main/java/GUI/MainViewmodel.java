package GUI;

import javafx.application.Platform;
import javafx.beans.property.*;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;
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
    AnchorPane mainAnchorPane;
    @FXML
    Button runButton;
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
    @FXML
    CheckBox maxToggleCheckBox;
    @FXML
    CheckBox verboseToggleCheckBox;
    @FXML
    CheckBox statisticToggleCheckBox;
    @FXML
    CheckBox dbToggleCheckBox;
    @FXML
    VBox dataBaseVBox;

    PrintStream ps;

    private BooleanProperty dbToggle = new SimpleBooleanProperty(false);
    private BooleanProperty verboseToggle = new SimpleBooleanProperty(false);
    private BooleanProperty statisticToggle = new SimpleBooleanProperty(true);
    private BooleanProperty maxToggle = new SimpleBooleanProperty(true);
    private StringProperty path = new SimpleStringProperty();
    private IntegerProperty max = new SimpleIntegerProperty(Integer.MAX_VALUE);

    public void initialize(){
        pathTextBox.textProperty().bindBidirectional(path);
        maxTextBox.textProperty().bindBidirectional(max, new NumberStringConverter());
        maxToggleCheckBox.selectedProperty().bindBidirectional(maxToggle);
        maxTextBox.disableProperty().bindBidirectional(maxToggle);
        statisticToggleCheckBox.selectedProperty().bindBidirectional(statisticToggle);
        verboseToggleCheckBox.selectedProperty().bindBidirectional(verboseToggle);
        dbToggleCheckBox.selectedProperty().bindBidirectional(dbToggle);

        dataBaseVBox.visibleProperty().bindBidirectional(dbToggle);
        dataBaseVBox.managedProperty().bindBidirectional(dbToggle);

        runButton.setGraphic( new ImageView( new Image(this.getClass().getResourceAsStream("/Theme/RunButton.png") )));




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

        Task copyWorker = new WikiFileDumpParser(threadcount, max.getValue(), statisticToggle.getValue(), verboseToggle.getValue(), path.get(), null);
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
