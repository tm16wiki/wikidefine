package CLI;

import helperClasses.db;

import java.io.File;

/**
 * Created by rene2 on 06.02.2017.
 */
public class Config {
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

    public static String getLang() {
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
