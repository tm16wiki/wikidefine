package CLI;

import asg.cliche.Command;
import asg.cliche.Param;
import asg.cliche.Shell;
import asg.cliche.ShellDependent;
import helperClasses.db;
import wikiAPI.WikiFileDumpParser;


/**
 * class to controll the fildumpparser by cli
 */
public class FileDumpCLI implements ShellDependent {
    private Shell theShell;
    private boolean stats = false;
    private boolean verbose = true;
    private boolean export = true;
    private int max = Integer.MAX_VALUE;
    private int threads = 4;
    private String filepath;
    private String dbpath;
    private db exportDB;


    /**
     * constructor
     *
     * @param config configuration to load
     */
    FileDumpCLI(Config config) {
        this.filepath = CLI.Config.getFilepath();
        if (CLI.Config.getDbpath() != null) {
            this.exportDB = CLI.Config.getDatabase();
            this.dbpath = CLI.Config.getDbpath();
        }
        showconfig();
    }


    /**
     * prints current configuration
     */
    @Command(name = "config",
            abbrev = "c",
            description = "prints current configuration")
    public void showconfig() {
        System.out.println("====    CONFIG    ====");
        System.out.println("threads:\t" + threads);
        System.out.println("maximum:\t" + max);
        System.out.println("filepath:\t" + filepath);
        System.out.println("db path:\t" + dbpath);
        System.out.println("db export:\t" + export);
        System.out.println("verbose:\t" + verbose);
        System.out.println("show stats:\t" + stats);
    }

    /**
     * starts filedump processing
     */
    @Command(name = "run",
            abbrev = "r",
            description = "creates definitions out of file")
    public void run() {
        Thread task;
        if (!export) {
            task = new Thread(new WikiFileDumpParser(threads, max, stats, verbose, filepath, null));
        } else {
            task = new Thread(new WikiFileDumpParser(threads, max, stats, verbose, filepath, exportDB));
        }
        task.start();
        while (task.isAlive()){
        }
    }


    /**
     * setter for the shell
     *
     * @param theShell shell to set
     */
    public void cliSetShell(Shell theShell) {
        this.theShell = theShell;
    }

    /**
     * changes the value of verbose
     */
    @Command(name = "switchverbose",
            abbrev = "sv",
            description = "changes verbose boolean")
    public void switchVerbose() {
        this.verbose = !verbose;
        showconfig();
    }

    /**
     * changes the value of export
     */
    @Command(name = "switchexport",
            abbrev = "se",
            description = "changes export boolean")
    public void switchExport() {
        this.export = !export;
        showconfig();
    }

    /**
     * changes the value of stats
     */
    @Command(name = "switchstats",
            abbrev = "ss",
            description = "changes statistics boolean")
    public void switchStats() {
        this.stats = !stats;
        showconfig();
    }

    /**
     * sets the vaulue for the maximum
     *
     * @param max maximmum to set
     */
    @Command(name = "settmax",
            abbrev = "sm",
            description = "sets max definition to generate")
    public void setMax(@Param(name = "maximum", description = "max definitions generated") int max) {
        this.max = max;
        showconfig();
    }

    /**
     * changes the filepath of the xml dump
     *
     * @param filepath path to file
     */
    @Command(name = "setpath",
            abbrev = "sp",
            description = "stets filepath")
    public void setPath(@Param(name = "path", description = "filepath") String filepath) {
        this.filepath = filepath;
        showconfig();
    }

    /**
     * changes the amount of threads to spawn
     *
     * @param threads amount of threads to spawn
     */
    @Command(name = "setthreads",
            abbrev = "st",
            description = "stets number of threads to use")
    public void setThreads(@Param(name = "threads", description = "amount of threads spawned") int threads) {
        this.threads = threads;
        showconfig();
    }

}