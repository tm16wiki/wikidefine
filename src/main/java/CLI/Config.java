package CLI;

import helperClasses.db;

import java.io.File;

/**
 * Manages configurations for the CLI
 */
public class Config {
    private static String lang;
    private static String filepath;
    private static String dbpath;
    private static String dbuser;
    private static String dbpassword;
    private static db database;

    /**
     * Creates a new CLI configuration
     *
     * @param lang Language to use for webdefinition
     * @param filepath Path to Wikipedia XML dump for filedump
     * @param dbpath Path to database postgre/mysql/sqlite
     * @param dbuser Database user
     * @param dbpasswort Database password
     */
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

    /**
     * Returns the configuration language
     * @return configuration language
     */
    public static String getLang() {
        return lang;
    }

    /**
     * Sets the configuration language
     * @param lang configuration language
     */
    static void setLang(String lang) {
        Config.lang = lang;
    }

    /**
     * Gets the file path to Wikipedia dump XML
     * @return file path to Wikipedia dump XML
     */
    public static String getFilepath() {
        return filepath;
    }

    /**
     * Sets the file path to Wikipedia dump XML
     * @param filepath file path to Wikipedia dump XML
     */
    static void setFilepath(String filepath) {
        Config.filepath = filepath;
    }

    /**
     * Gets the database path to save the short definitions
     * @return database path
     */
    static String getDbpath() {
        return dbpath;
    }

    /**
     * Sets the database path to save the short definitions
     * @param dbpath database path
     */
    static void setDbpath(String dbpath) {
        Config.dbpath = dbpath;
    }

    /**
     * Gets the database to save the short definitions
     * @return database object
     */
    static db getDatabase() {
        return database;
    }

    /**
     * Sets the database to save the short definitions
     * @param database database object
     */
    public static void setDatabase(db database) {
        Config.database = database;
    }

    /**
     * Gets the database user
     * @return database user
     */
    public static String getDbuser() {
        return dbuser;
    }

    /**
     * Sets the database user
     * @param dbuser database user
     */
    public static void setDbuser(String dbuser) {
        Config.dbuser = dbuser;
    }

    /**
     * Gets the database password
     * @return database password
     */
    public static String getDbpassword() {
        return dbpassword;
    }

    /**
     * Sets the database password
     * @param dbpassword database password
     */
    public static void setDbpassword(String dbpassword) {
        Config.dbpassword = dbpassword;
    }
}
