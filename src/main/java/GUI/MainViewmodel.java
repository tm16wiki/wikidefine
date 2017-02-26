package GUI;

import helperClasses.db;
import javafx.application.Platform;
import javafx.beans.property.*;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
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
import java.nio.file.Paths;
import java.sql.ResultSet;

/**
 * GUI for wikidefine
 */
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
    @FXML
    CheckBox dbAuthCheckBox;
    @FXML
    TextField dbPathTextField;
    @FXML
    TextField dbUserTextField;
    @FXML
    PasswordField dbPasswordPasswordField;
    PrintStream ps;
    db configDB;

    private BooleanProperty dbToggle = new SimpleBooleanProperty(false);
    private BooleanProperty verboseToggle = new SimpleBooleanProperty(false);
    private BooleanProperty statisticToggle = new SimpleBooleanProperty(true);
    private BooleanProperty maxToggle = new SimpleBooleanProperty(false);
    private StringProperty filePath = new SimpleStringProperty();
    private IntegerProperty max = new SimpleIntegerProperty(Integer.MAX_VALUE);
    private BooleanProperty dbAuthToggle = new SimpleBooleanProperty();
    private StringProperty dbPath = new SimpleStringProperty();
    private StringProperty dbUser = new SimpleStringProperty("");
    private StringProperty dbPassword = new SimpleStringProperty("");

    /**
     * Initializes the wikidefine GUI
     */
    public void initialize() {
        pathTextBox.textProperty().bindBidirectional(filePath);
        maxTextBox.textProperty().bindBidirectional(max, new NumberStringConverter());
        maxToggleCheckBox.selectedProperty().bindBidirectional(maxToggle);
        maxTextBox.disableProperty().bind(maxToggle.not());
        statisticToggleCheckBox.selectedProperty().bindBidirectional(statisticToggle);
        verboseToggleCheckBox.selectedProperty().bindBidirectional(verboseToggle);

        dbToggleCheckBox.selectedProperty().bindBidirectional(dbToggle);
        dataBaseVBox.disableProperty().bind(dbToggle.not());
        dbPathTextField.textProperty().bindBidirectional(dbPath);
        dbAuthCheckBox.selectedProperty().bindBidirectional(dbAuthToggle);
        dbUserTextField.textProperty().bindBidirectional(dbUser);
        dbUserTextField.disableProperty().bind(dbAuthToggle.not());
        dbPasswordPasswordField.textProperty().bindBidirectional(dbPassword);
        dbPasswordPasswordField.disableProperty().bind(dbAuthToggle.not());

        runButton.setGraphic(new ImageView(new Image(this.getClass().getResourceAsStream("/Theme/RunButton.png"))));

        threadNumberComboBox.getItems().addAll(
                "1", "2", "3", "4", "5", "6", "7", "8"
        );


        Console console = new Console(consoleTextArea);
        ps = new PrintStream(console, true);
        System.setOut(ps);
        System.setErr(ps);


        //loading default config
        String sqlitepath = Paths.get(".").toAbsolutePath().normalize().toString() + "/config.db";
        ResultSet rs = new db(sqlitepath).execQuery("select * from config where name = 'default';");
        try {
            while (rs.next()) {
                //        rs.getString("language");
                filePath.setValue(rs.getString("file"));
                dbPath.setValue(rs.getString("exportdb"));
                dbUser.setValue(rs.getString("dbuser"));
                dbPassword.setValue(rs.getString("dbpassword"));
            }
            if (!dbUser.getValue().equals("null") && !dbPassword.getValue().equals("null")) {
                dbAuthToggle.setValue(true);
            } else {
                dbUser.setValue(null);
                dbPassword.setValue(null);
            }
        } catch (Exception e) {
            //e.printStackTrace();
        }


    }


    @FXML
    private void openDB(ActionEvent event) {
        FileChooser fileChooser = new FileChooser();
        File file = fileChooser.showOpenDialog(new Stage());
        if (file != null) {
            System.out.println("Open db:\t" + file.getAbsolutePath());

        }
        dbPath.setValue(file.getAbsolutePath());
    }


    /**
     * Shows file dialog to choose the Wikipedia XML file
     *
     * @param event not used
     */
    @FXML
    private void openFile(ActionEvent event) {
        FileChooser fileChooser = new FileChooser();
        File file = fileChooser.showOpenDialog(new Stage());
        if (file != null) {
            System.out.println("Open file:\t" + file.getAbsolutePath());

        }
        filePath.setValue(file.getAbsolutePath());
    }

    /**
     * Runs the text parser
     *
     * @param e not used
     */
    @FXML
    private void runParser(ActionEvent e) {
        int threadcount = threadNumberComboBox.getSelectionModel().getSelectedIndex() + 1;

        db db = null;
        if (dbToggle.getValue()) {
            if (dbUser.getValue() != null && dbPassword != null) {
                db = new db(dbPath.getValue(), dbUser.getValue(), dbPassword.getValue());
            } else {
                db = new db(dbPath.getValue());
            }
        }


        Task copyWorker = new WikiFileDumpParser(threadcount, max.getValue(), statisticToggle.getValue(), verboseToggle.getValue(), filePath.get(), db);
        progressBar.progressProperty().unbind();
        progressBar.progressProperty().bind(copyWorker.progressProperty());
        new Thread(copyWorker).start();
    }

    /**
     * Shows parser stats
     */
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
