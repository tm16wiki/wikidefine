package CLI;

import asg.cliche.CLIException;
import asg.cliche.Command;
import asg.cliche.Shell;
import asg.cliche.ShellFactory;
import helperClasses.db;

import java.io.IOException;

public class cli {
    private static Shell shell;

    private static String sqlitepath = "./src/main/resources/config.db";
    private static String lang;
    private static String filepath;
    private db postgresDB;
    private db localdb;


    public cli() {
        localdb = new db(sqlitepath);
        return;
    }

    public static void main(String[] args) {
        cli cli = new cli();

        shell = ShellFactory.createConsoleShell("wikiDefine", "", cli);
        try {
            System.out.println("\n====   COMMANDS   ====");
            shell.processLine("?la");
            shell.commandLoop();
        } catch (CLIException | IOException e) {
            e.printStackTrace();
        }
    }


    @Command(name = "webdef",
            abbrev = "wd",
            description = "creates definitions out of web")
    public void webdef() {
        try {
            Shell webcli = ShellFactory.createSubshell("webdef", shell, "", new webcli(lang));
            System.out.println("\n====   COMMANDS   ====");
            webcli.processLine("?l");
            webcli.commandLoop();
        } catch (CLIException | IOException e) {
            e.printStackTrace();
        }
    }


    @Command(name = "filedump",
            abbrev = "fd",
            description = "creates all definitions out of file with max speed")
    public void filedump() {
        try {
            Shell filecli = ShellFactory.createSubshell("filedump", shell, "", new filecli(filepath, postgresDB));
            System.out.println("\n====   COMMANDS   ====");
            filecli.processLine("?l");
            filecli.commandLoop();
        } catch (CLIException | IOException e) {
            e.printStackTrace();
        }
    }

    @Command(name = "configure",
            abbrev = "c",
            description = "manage configurations")
    public void configure() {
        try {
            Shell filecli = ShellFactory.createSubshell("configure", shell, "", new configcli(localdb));
            System.out.println("\n====   COMMANDS   ====");
            filecli.processLine("?l");
            filecli.commandLoop();
        } catch (CLIException | IOException e) {
            e.printStackTrace();
        }
    }

}
