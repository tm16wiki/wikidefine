import CLI.ConfigCLI;
import GUI.GUIMain;
import asg.cliche.Shell;
import asg.cliche.ShellFactory;
import wikiAPI.WikiFileDumpParser;

/**
 * Manages application launch - shell or GUI
 */
public class App {

    /**
     * Starting method
     *
     * @param args
     */
    public static void main(String[] args) {

        if (args.length > 1 && args[0].contains("csv")) {
            new Thread(new WikiFileDumpParser(8, Integer.MAX_VALUE, false, true, args[1], null)).start();
            return;
        }

        if (args.length > 0 && args[0].contains("gui")) {
            javafx.application.Application.launch(GUIMain.class);
        } else { // launch shell
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
}
