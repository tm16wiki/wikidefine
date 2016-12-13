package helperClasses;

import java.io.BufferedReader;
import java.io.FileReader;
import java.sql.*;


public class db {
    private Connection c;
    private String path;

    //sqlite
    public db(String db) {
        path = db;
        if(db.contains("null")){
            System.out.println("filepath is set to null");
            return;
        }
        try {
            System.out.print("connecting to "+ path +" ... ");
            Class.forName("org.sqlite.JDBC");
            c = DriverManager.getConnection("jdbc:sqlite:" + path);
            System.out.printf("connected!\n");
            executeDBScript("./src/main/resources/definition.sqlite.sql");
        } catch (SQLException e) {
            System.out.printf("Error!\n");
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
                //setup table
                executeDBScript("./src/main/resources/definition.postgresql.sql");
            } catch (SQLException e) {
                System.out.printf("Error!\n");
            } catch (ClassNotFoundException e) {
                System.out.println("Error loading driver");
            }
        }
        //MySQL
        if (path.contains("mysql://")) {
            try {
                System.out.print("connecting to MySQLDB "+ path +" " + user +" " + pass+" " +
                        "... ");
                c = DriverManager.getConnection("jdbc:" + path, user, pass);
                Class.forName("com.mysql.jdbc.Driver\"");
                System.out.printf("connected!\n");
                //setup table
                executeDBScript("./src/main/resources/definition.mysql.sql");
            } catch (SQLException e) {
                System.out.printf("Error!\n");
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
            if (!e.getLocalizedMessage().equals("does not return ResultSet")) {
                return null;
            }
            if (!e.getLocalizedMessage().equals("A UNIQUE constraint failed")) {
                return null;
            }
        }
        return null;
    }

    public boolean insertDefinition(String title, String definition) {
        if( execQuery("insert into definition(title, text) values('" + title + "', '" + definition + "');") != null){
            return true;
        }else {
            return false;
        }
    }

    public boolean insertConfiguration(String title, String definition) {
        try {
            Statement stmt = c.createStatement();
            //stmt.executeQuery("create table if not exists definitions( title");
            stmt.executeQuery("insert into definition values('" + title + "', '" + definition + "');");
            return true;
        } catch (SQLException e) {
            //e.printStackTrace();
            return false;
        }
    }

    public boolean executeDBScript(String filepath) {
        boolean isScriptExecuted = false;
        try {
            Statement stmt = c.createStatement();
            BufferedReader in = new BufferedReader(new FileReader(filepath));
            String line;
            StringBuilder stringBuffer = new StringBuilder();
            while ((line = in.readLine()) != null) {
                stringBuffer.append(line).append("\n ");
            }
            in.close();
            stmt.executeUpdate(stringBuffer.toString());
            isScriptExecuted = true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return isScriptExecuted;
    }

}
