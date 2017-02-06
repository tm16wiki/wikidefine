import CLI.ConfigCLI;
import asg.cliche.Shell;
import asg.cliche.ShellFactory;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class App extends Application {

    public static void main(String[] args) {

        if(args.length>0 && args[0].contains("gui")){
            launch();
        }else {
            Shell shell = ShellFactory.createConsoleShell("wikiDefine", "'?list' or '?list-all' to show commands", new ConfigCLI());
            try {
                System.out.println("\n====   CONFIGURATIONS   ====");
                shell.processLine("ls");
                if (CLI.Config.getLang() == null) {
                    System.out.println("\ncreating new configuration. please name it default.");
                    shell.processLine("nc");
                }
                shell.commandLoop();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        Parent root = FXMLLoader.load(getClass().getResource("/Views/parserGUI.fxml"));
        primaryStage.setTitle("WikiDefine");
        primaryStage.setScene(new Scene(root));

        primaryStage.setResizable(false);
        primaryStage.sizeToScene();


        primaryStage.getIcons().add(
                new javafx.scene.image.Image(
                        App.class.getResourceAsStream( "/Theme/WikiDefineIcon.png" ))
        );


        App.class.getResource( "/Theme/WikiDefineIcon.png" );


        primaryStage.show();


    }
}
