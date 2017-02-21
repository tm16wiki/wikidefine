package CLI;

import asg.cliche.Command;
import asg.cliche.Param;
import asg.cliche.Shell;
import asg.cliche.ShellFactory;
import helperClasses.db;

import java.io.IOException;
import java.nio.file.Paths;
import java.sql.ResultSet;
import java.util.Scanner;

public class ConfigCLI {
    private static Shell shell;
    private db localdb;
    private Config config;

    /**
     * Loads CLI configuration and shell
     */
    public ConfigCLI() {
        String sqlitepath = Paths.get(".").toAbsolutePath().normalize().toString() + "/config.db";
        localdb = new db(sqlitepath);
        loadconfig("default");
        this.shell = ShellFactory.createConsoleShell("cli", "cli", "cli"); // dummy shell
    }

    /**
     * Enters web definition subshell
     */
    @Command(name = "webdef",
            abbrev = "wd",
            description = "creates definitions out of web")
    public void webdef() {
        try {
            Shell webcli = ShellFactory.createSubshell("webdef", shell, "", new WebDefCLI(this.config));
            webcli.commandLoop();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Enters file dump subshell
     */
    @Command(name = "filedump",
            abbrev = "fd",
            description = "creates all definitions out of file with max speed")
    public void filedump() {
        try {
            Shell filecli = ShellFactory.createSubshell(config.getFilepath(), shell, "", new FileDumpCLI(this.config));
            filecli.commandLoop();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Loads CLI configuration
     *
     * @param name CLI configuration to load
     */
    @Command(name = "loadconfig",
            abbrev = "lc",
            description = "load configuration")
    public void loadconfig(@Param(name = "name", description = "config name to load") String name) {
        ResultSet rs;
        rs = localdb.execQuery("select * from config where name = '" + name + "';");
        try {
            int results = 0;
            while (rs.next()) {
                results++;
                System.out.println("loading configuration: " + rs.getString("name"));
                this.config = new Config(
                        rs.getString("language"),
                        rs.getString("file"),
                        rs.getString("exportdb"),
                        rs.getString("dbuser"),
                        rs.getString("dbpassword")
                );
            }
            if (results == 0) {
                System.out.println("error loading configuration " + name);
            }
        } catch (Exception e) {
            //e.printStackTrace();
        }
    }

    /**
     * Deletes CLI configuration
     *
     * @param name CLI configuration to delete
     */
    @Command(name = "deleteconfig",
            abbrev = "dc",
            description = "delete configuration")
    public void delConfig(@Param(name = "name", description = "config name to load") String name) {
        localdb.execQuery("delete from config where name='" + name + "';");
    }

    /**
     * Creates a new CLI configuration
     */
    @Command(name = "newconfig",
            abbrev = "nc",
            description = "new configuration")
    public void newconfigDialog() {
        try {
            Scanner scan = new Scanner(System.in);
            System.out.print("name: ");
            String name = scan.nextLine();
            System.out.print("language: ");
            String lang = scan.nextLine();
            System.out.print("file: ");
            String filepath = scan.nextLine();
            System.out.print("database: ");
            String dbpath = scan.nextLine();
            String dbuser = null;
            String dbpassword = null;
            if (dbpath.equals("")) {
                dbpath = null;
                dbuser = null;
                dbpassword = null;
            } else if (dbpath.contains("postgresql://") || dbpath.contains("mysql://")) {
                System.out.print("databaseuser: ");
                dbuser = scan.nextLine();
                System.out.print("databasepassword: ");
                dbpassword = scan.nextLine();
            }
            localdb.insertConfiguration(name, lang, filepath, dbpath, dbuser, dbpassword);
            loadconfig(name);
        } catch (Exception e) {
            System.out.println("can't create config");
            e.printStackTrace();
        }
    }

    /**
     * Lists all available CLI configurations
     */
    @Command(name = "listconfig",
            abbrev = "ls",
            description = "list configuration")
    public void listconfig() {
        try {
            ResultSet rs = localdb.execQuery("select * from config;");
            System.out.println("id name:\t\tlang\tfile\t\t\tdb");
            while (rs.next()) {
                System.out.print(rs.getInt("id") + " ");
                System.out.print(rs.getString("name") + ":\t\t");
                if (rs.getString("name").length() % 4 < 3) {
                    System.out.print("\t");
                }
                System.out.print(rs.getString("language") + "\t\t");
                System.out.print(rs.getString("file") + "\t");
                System.out.print(rs.getString("exportdb") + "\t");
                System.out.print(rs.getString("dbuser") + "\t");
                System.out.print(rs.getString("dbpassword") + "\n");
            }
        } catch (Exception e) {
            //e.printStackTrace();
        }
    }
}


