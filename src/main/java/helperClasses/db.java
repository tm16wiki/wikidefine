package helperClasses;

import java.sql.*;


public class db {
    private Connection c;
    private String path;

    //sqlite
    public db(String db) {
        path = db;
        try {
            System.out.print("connecting to sqliteDB ... ");
            Class.forName("org.sqlite.JDBC");
            c = DriverManager.getConnection("jdbc:sqlite:" + path);
            System.out.printf("connected!\n");
        } catch (SQLException e) {
            System.out.printf("Can't connect to " + path + "\n");
        } catch (ClassNotFoundException e) {
            System.out.println("Error loading driver");
        }
    }

    public db(String db, String user, String pass) {
        path = db;
        //PostgreSQL
        if (path.contains("postgresql://")) {
            try {
                System.out.print("connecting to postgresqlDB ... ");
                c = DriverManager.getConnection("jdbc:" + path, user, pass);
                Class.forName("org.postgresql.Driver");
                System.out.printf("connected!\n");
            } catch (SQLException e) {
                System.out.printf("Can't connect to " + path + " as " + user + "\n");
            } catch (ClassNotFoundException e) {
                System.out.println("Error loading driver");
            }
        }
        //MySQL
        if (path.contains("mysql://")) {
            try {
                System.out.print("connecting to MySQLDB ... ");
                c = DriverManager.getConnection("jdbc:" + path, user, pass);
                Class.forName("com.mysql.jdbc.Driver\"");
                System.out.printf("connected!\n");
            } catch (SQLException e) {
                System.out.printf("Can't connect to " + path + " as " + user + "\n");
            } catch (ClassNotFoundException e) {
                System.out.println("Error loading driver");
            }
        }
    }

    public ResultSet execQuery(String query) {
        Statement stmt;
        try {
            stmt = c.createStatement();

            ResultSet rs = stmt.executeQuery(query);
            if (rs != null) {
                return rs;
            }
        } catch (SQLException e) {
            if (!e.getLocalizedMessage().equals("query does not return ResultSet")) {
                e.printStackTrace();
            }
        }
        return null;
    }

    public boolean insertDefinition(String title, String definition) {
        Statement stmt;
        try {
            stmt = c.createStatement();
            stmt.executeQuery("insert into definition values('" + title + "', '" + definition + "');");
            return true;
        } catch (SQLException e) {
            //e.printStackTrace();
            return false;
        }
    }
}
