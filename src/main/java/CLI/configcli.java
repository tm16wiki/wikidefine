package CLI;

import asg.cliche.Command;
import asg.cliche.Param;
import asg.cliche.Shell;
import helperClasses.db;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Scanner;


public class configcli {

    private static int configurations = 0;
    private static String lang;
    private static String filepath;
    private static String dbpath;
    private static Shell shell;
    private db postgresDB;
    private db localdb;

    public configcli(db localdb) {
        this.localdb = localdb;
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
                System.out.println("loading " + rs.getString("name"));
                lang = rs.getString("language");
                filepath = rs.getString("file");
                dbpath = rs.getString("exportdb");
                if (dbpath != null) {
                    postgresDB = new db(dbpath, "postgres", "postgres");
                }
            }
            if (results == 0) {
                System.out.println("error loading config");
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
                System.out.println("loading " + rs.getString("name"));
                lang = rs.getString("language");
                filepath = rs.getString("file");
                dbpath = rs.getString("exportdb");
                if (dbpath != null) {
                    postgresDB = new db(dbpath, "postgres", "postgres");
                }
            }
            if (results == 0) {
                System.out.println("error loading config");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }


    @Command(name = "delconfig",
            abbrev = "dc",
            description = "deletes configuration")
    public void delconfig(@Param(name = "name", description = "config name to delete") String name) {
        localdb.execQuery("delete from config where name ='" + name + "';");
    }


    @Command(name = "delconfig",
            abbrev = "dc",
            description = "deletes configuration")
    public void delconfig(@Param(name = "id", description = "config id to delete") int id) {
        localdb.execQuery("delete from config where id ='" + id + "';");
    }

    @Command(name = "newconfig",
            abbrev = "nc",
            description = "new configuration")
    public void newconfig() {
        try {
            Scanner scan = new Scanner(System.in);
            System.out.print("name: ");
            String name = scan.nextLine();
            System.out.print("language: ");
            lang = scan.nextLine();
            System.out.print("file: ");
            filepath = scan.nextLine();
            System.out.print("database: ");
            dbpath = scan.nextLine();
            System.out.print("id (0 for default): ");
            int id = scan.nextInt();
            if (dbpath.equals("")) {
                dbpath = null;
            }
            localdb.execQuery("insert into config values('" +
                    id + "', '" +
                    name + "' , '" +
                    lang + "', '" +
                    filepath + "', '" +
                    dbpath + "');");
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
            configurations = 0;
            while (rs.next()) {
                if (++configurations == 1) {
                    System.out.println("id\tname\tlang\tpath");
                }
                System.out.print(rs.getInt("id") + "\t");
                System.out.print(rs.getString("name") + "\t");
                System.out.print(rs.getString("language") + "\t\t");
                System.out.print(rs.getString("file") + "\t");
                System.out.print(rs.getString("exportdb") + "\n");
            }
            if (configurations == 0) {
                System.out.println("no configurations found");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }


}
