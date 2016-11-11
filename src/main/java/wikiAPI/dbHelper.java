package wikiAPI;

import java.sql.Connection;
import java.sql.*;


class dbHelper {
    private Connection c;

    //sqlite
    dbHelper(String db) {
        try {
            System.out.print("Connecting to sqliteDB ... ");
            Class.forName("org.sqlite.JDBC");
            c = DriverManager.getConnection("jdbc:sqlite:" + db);
            System.out.printf("connected!\n");
        } catch (SQLException e) {
            System.out.printf("Can't connect to " + db + "\n");
        } catch (ClassNotFoundException e) {
            System.out.println("Error loading driver");
        }
    }

    dbHelper(String db, String user, String pass) {
        //PostgreSQL
        if (db.contains("postgresql://")) {
            try {
                System.out.print("Connecting to postgresqlDB ... ");
                c = DriverManager.getConnection("jdbc:" + db, user, pass);
                Class.forName("org.postgresql.Driver");
                System.out.printf("connected!\n");
            } catch (SQLException e) {
                System.out.printf("Can't connect to " + db + " as " + user + "\n");
            } catch (ClassNotFoundException e) {
                System.out.println("Error loading driver");
            }
        }
        //MySQL
        if (db.contains("mysql://")) {
            try {
                System.out.print("Connecting to MySQLDB ... ");
                c = DriverManager.getConnection("jdbc:" + db, user, pass);
                Class.forName("com.mysql.jdbc.Driver\"");
                System.out.printf("connected!\n");
            } catch (SQLException e) {
                System.out.printf("Can't connect to " + db + " as " + user + "\n");
            } catch (ClassNotFoundException e) {
                System.out.println("Error loading driver");
            }
        }
    }
}
