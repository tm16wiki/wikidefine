package CLI;

import asg.cliche.*;
import helperClasses.db;

import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Scanner;

public class configCLI {
    private static Shell shell;

    private static db localdb;
    private config config;


    private configCLI() {
        String sqlitepath = "./src/main/resources/config.db";
        localdb = new db(sqlitepath);
        loadconfig("default");
    }

    public static void main(String[] args) {
        shell = ShellFactory.createConsoleShell("wikiDefine", "", new configCLI());
        try {
            System.out.println("\n====   CONFIGURATIONS   ====");
            shell.processLine("ls");
            if(CLI.config.getLang() == null){
                System.out.println("\ncreating new configuration. please name it default.");
                shell.processLine("nc");
            }
            System.out.println();
            System.out.println("\n====   COMMANDS   ====");
            shell.processLine("?l");
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
            Shell webcli = ShellFactory.createSubshell("webdef", shell, "", new webDefCLI(this.config));
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
            Shell filecli = ShellFactory.createSubshell("filedump", shell, "", new fileDumpCLI(this.config));
            System.out.println("\n====   COMMANDS   ====");
            filecli.processLine("?l");
            filecli.commandLoop();
        } catch (CLIException | IOException e) {
            e.printStackTrace();
        }
    }


    @Command(name = "deleteconfig",
            abbrev = "dc",
            description = "delete configuration")
    public void delConfig(@Param(name = "id", description = "config id to load") int id) {
        localdb.execQuery("delete from config where id='"+id+"';");
    }

    @Command(name = "loadconfig",
            abbrev = "lc",
            description = "load configuration")
    public void loadconfig(@Param(name = "id", description = "config id to load") int id) {
        ResultSet rs;
        rs = localdb.execQuery("select * from config where id = '" + id + "';");
        try {
            int results = 0;
            while (rs.next()) {
                results++;
                System.out.println("loading configuration: " + rs.getString("name"));
                config.setLang( rs.getString("language"));
                config.setFilepath(rs.getString("file"));
                config.setDbpath(rs.getString("exportdb"));
                config.setDbuser(rs.getString("dbuser"));
                config.setDbpassword(rs.getString("dbpassword"));
            }
            if (results == 0) {
                System.out.println("error loading configuration by id " + id);
            }
        } catch (SQLException e) {
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
                config.setLang( rs.getString("language"));
                config.setFilepath(rs.getString("file"));
                config.setDbpath(rs.getString("exportdb"));
                config.setDbuser(rs.getString("dbuser"));
                config.setDbpassword(rs.getString("dbpassword"));
            }
            if (results == 0) {
                System.out.println("error loading configuration "+ name);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
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
            String dbuser;
            String dbpassword;
            if (dbpath.equals("")) {
                dbpath = null;
                dbuser = null;
                dbpassword = null;
            }else{
                System.out.print("databaseuser: ");
                dbuser = scan.nextLine();
                System.out.print("databasepassword: ");
                dbpassword = scan.nextLine();
            }
            localdb.execQuery("insert into config(name, language, file, exportdb, dbuser, dbpassword) values('" +
                    name + "' , '" +
                    lang + "', '" +
                    filepath + "', '" +
                    dbpath + "', '" +
                    dbuser + "', '" +
                    dbpassword + "');");
        } catch (Exception e) {
            System.out.println("can't create config");
            e.printStackTrace();
        }
    }

    @Command(name = "listconfig",
            abbrev = "ls",
            description = "list configuration")
    public void listconfig() {
        ResultSet rs = localdb.execQuery("select * from config;");
        try {
            while (rs.next()) {
                System.out.print(rs.getInt("id") + "\t");
                System.out.print(rs.getString("name") + "\t");
                System.out.print(rs.getString("language") + "\t\t");
                System.out.print(rs.getString("file") + "\t");
                System.out.print(rs.getString("exportdb") + "\t");
                System.out.print(rs.getString("dbuser") + "\t");
                System.out.print(rs.getString("dbpassword") + "\n");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }


}


class config {
    private static String lang;
    private static String filepath;
    private static String dbpath;
    private static String dbuser;
    private static String dbpassword;
    private static db database;


    config(String lang, String filepath, String dbpath, String dbuser, String dbpasswort){
        config.lang = lang;
        config.filepath = filepath;
        config.dbpath = dbpath;
        config.dbuser = dbuser;
        config.dbpassword = dbpasswort;
        database = new db(dbpath);
    }

    static String getLang() {
        return lang;
    }

    static void setLang(String lang) {
        config.lang = lang;
    }

    public static String getFilepath() {
        return filepath;
    }

    static void setFilepath(String filepath) {
        config.filepath = filepath;
    }

    static String getDbpath() {
        return dbpath;
    }

    static void setDbpath(String dbpath) {
        config.dbpath = dbpath;
    }

    static db getDatabase() {
        return database;
    }

    public static void setDatabase(db database) {
        config.database = database;
    }


    public static String getDbuser() {
        return dbuser;
    }

    public static void setDbuser(String dbuser) {
        config.dbuser = dbuser;
    }

    public static String getDbpassword() {
        return dbpassword;
    }

    public static void setDbpassword(String dbpassword) {
        config.dbpassword = dbpassword;
    }
}
