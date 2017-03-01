package helperClasses;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.*;

/**
 * Class to manage db connection independent of the type of the db
 */
public class db {
    PreparedStatement prepStmt = null;
    int count = 0;
    private Connection c;
    private String path;

    /**
     * Constructor for sqlite
     *
     * @param db path to sqlite db
     */
    public db(String db) {
        path = db;
        if (db.contains("null")) {
            System.out.println("filepath is set to null");
            return;
        }
        try {
            System.out.print("connecting to " + path + " ... ");
            Class.forName("org.sqlite.JDBC");
            c = DriverManager.getConnection("jdbc:sqlite:" + path);
            System.out.printf("connected!\n");
            //setup
            executeDBScript("Skripts/config.sql");
            executeDBScript("Skripts/definition.sqlite.sql");
        } catch (SQLException e) {
            System.out.printf("Error!\n");
        } catch (ClassNotFoundException e) {
            System.out.println("Error loading driver");
        }
    }


    /**
     * Constructor for postgresql and mysql
     *
     * @param db   path to db
     * @param user username for db
     * @param pass password for db
     */
    public db(String db, String user, String pass) {
        path = db;
        //PostgreSQL
        if (path.contains("postgresql://")) {
            try {
                System.out.print("connecting to postgresqlDB ... ");
                c = DriverManager.getConnection("jdbc:" + path, user, pass);
                c.setAutoCommit(false);
                Class.forName("org.postgresql.Driver");
                System.out.printf("connected!\n");
                //setup table
                executeDBScript("Skripts/definition.postgre.sql");

            } catch (SQLException e) {
                System.out.printf("Error!\n");
                e.printStackTrace();
            } catch (ClassNotFoundException e) {
                System.out.println("Error loading driver");
            }
        }
        //MySQL
        if (path.contains("mysql://")) {
            try {
                System.out.print("connecting to MySQLDB " + path + " " + user + " " + pass + " " +
                        "... ");
                c = DriverManager.getConnection("jdbc:" + path, user, pass);
                Class.forName("com.mysql.jdbc.Driver\"");
                System.out.printf("connected!\n");
                //setup table
                executeDBScript("definition.mysql.sql");
            } catch (SQLException e) {
                System.out.printf("Error!\n");
            } catch (ClassNotFoundException e) {
                System.out.println("Error loading driver");
            }
        }
    }

    /**
     * Runs query on db
     *
     * @param query querystring to execute
     * @return result of the query as resultset
     */
    public ResultSet execQuery(String query) {
        Statement stmt;
        try {
            stmt = c.createStatement();
            ResultSet rs = stmt.executeQuery(query);
            if (rs != null) {
                return rs;
            }
        } catch (Exception e) {
            if (!e.getLocalizedMessage().equals("does not return ResultSet")) {
                return null;
            }
            if (!e.getLocalizedMessage().equals("A UNIQUE constraint failed")) {
                return null;
            }
        }
        return null;
    }

    /**
     * Inserts definition into db
     *
     * @param id         database column ID of the entry
     * @param title      title of the article
     * @param definition generated definition
     * @return returns boolean
     */

    //TODO: batch inserts for performance!!!
    public void insertDefinition(int id, String title, String definition) {
        try {
            execQuery("insert into definition( id, title, text) values('" + id + "', '" + title + "', '" + definition + "');");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void insertDefinition2(int id, String title, String definition, String wikititle) {
        try {
            if (prepStmt == null) {
                prepStmt = c.prepareStatement("insert into definition( id, title, text, wikititle) values( ?,?,?,?);");
            }
            prepStmt.setLong(1, id);
            prepStmt.setString(2, title);
            prepStmt.setString(3, definition);
            prepStmt.setString(4, wikititle);

            prepStmt.addBatch();

            if (count++ > 1000) {
                commitBatch();
                count = 0;
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void commitBatch() {
        try {
            int[] test = prepStmt.executeBatch();
        } catch (SQLException e) {
            //e.printStackTrace();
        } catch (NullPointerException e) {
            //e.printStrackTrace();
        }
    }

    /**
     * Inserts configuration into db
     *
     * @param name       name of the configuration
     * @param lang       language
     * @param filepath   path of the file to process
     * @param dbpath     location of the export db
     * @param dbuser     db user
     * @param dbpassword db password
     * @return true if succeed
     */
    public boolean insertConfiguration(String name, String lang, String filepath, String dbpath, String dbuser, String dbpassword) {
        try {
            Statement stmt = c.createStatement();
            stmt.executeQuery("insert into config(name, language, file, exportdb, dbuser, dbpassword) values('" +
                    name + "' , '" +
                    lang + "', '" +
                    filepath + "', '" +
                    dbpath + "', '" +
                    dbuser + "', '" +
                    dbpassword + "');");
            return true;
        } catch (SQLException e) {
            //e.printStackTrace();
            return false;
        }
    }

    /**
     * Runs SQL script
     *
     * @param filepath location of the sql script
     * @return true if succeed
     */
    private boolean executeDBScript(String filepath) {
        boolean isScriptExecuted = false;
        try {
            Statement stmt = c.createStatement();
            InputStream i = this.getClass().getClassLoader().getResourceAsStream(filepath);
            BufferedReader in = new BufferedReader(new InputStreamReader(i));
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
