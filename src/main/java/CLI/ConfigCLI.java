package CLI;

import asg.cliche.Command;
import asg.cliche.Param;
import asg.cliche.Shell;
import asg.cliche.ShellFactory;
import helperClasses.db;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.sql.ResultSet;
import java.util.Scanner;

public class ConfigCLI {
    private static Shell shell;

    private db localdb;
    private Config config;


    private ConfigCLI() {
        String sqlitepath = Paths.get(".").toAbsolutePath().normalize().toString() + "/config.db";
        localdb = new db(sqlitepath);
        loadconfig("default");
    }

    public static void main(String[] args) {


        shell = ShellFactory.createConsoleShell("wikiDefine", "'?list' or '?list-all' to show commands", new ConfigCLI());
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


    @Command(name = "deleteconfig",
            abbrev = "dc",
            description = "delete configuration")
    public void delConfig(@Param(name = "name", description = "config name to load") String name) {
        localdb.execQuery("delete from config where name='" + name + "';");
    }


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


class Config {
    private static String lang;
    private static String filepath;
    private static String dbpath;
    private static String dbuser;
    private static String dbpassword;
    private static db database;


    Config(String lang, String filepath, String dbpath, String dbuser, String dbpasswort) {
        this.lang = lang;
        File f = new File(filepath);
        this.filepath = filepath;
        this.dbpath = dbpath;
        this.dbuser = dbuser;
        this.dbpassword = dbpasswort;
        if (dbpath.contains("postgresql://") || dbpath.contains("postgresql://")) {
            database = new db(dbpath, dbuser, dbpasswort);
        } else if (dbpath.equals("null")) {
            database = null;
        } else {
            database = new db(dbpath);
        }
    }

    static String getLang() {
        return lang;
    }

    static void setLang(String lang) {
        Config.lang = lang;
    }

    public static String getFilepath() {
        return filepath;
    }

    static void setFilepath(String filepath) {
        Config.filepath = filepath;
    }

    static String getDbpath() {
        return dbpath;
    }

    static void setDbpath(String dbpath) {
        Config.dbpath = dbpath;
    }

    static db getDatabase() {
        return database;
    }

    public static void setDatabase(db database) {
        Config.database = database;
    }


    public static String getDbuser() {
        return dbuser;
    }

    public static void setDbuser(String dbuser) {
        Config.dbuser = dbuser;
    }

    public static String getDbpassword() {
        return dbpassword;
    }

    public static void setDbpassword(String dbpassword) {
        Config.dbpassword = dbpassword;
    }
}
