package CLI;

import asg.cliche.Command;
import asg.cliche.Param;
import asg.cliche.Shell;
import asg.cliche.ShellDependent;
import helperClasses.db;
import wikiAPI.WikiFileDumpParser;


/**
 * Class to control the fildump parser by CLI
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
     * Constructor for the file dump operation
     *
     * @param config CLI configuration to load
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
     * Prints current configuration
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
     * Starts filedump processing
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
        while (task.isAlive()) {
        }
    }


    /**
     * Setter for the shell
     *
     * @param theShell shell to set
     */
    public void cliSetShell(Shell theShell) {
        this.theShell = theShell;
    }

    /**
     * Changes the value of verbose
     */
    @Command(name = "switchverbose",
            abbrev = "sv",
            description = "changes verbose boolean")
    public void switchVerbose() {
        this.verbose = !verbose;
        showconfig();
    }

    /**
     * Changes the value of export
     */
    @Command(name = "switchexport",
            abbrev = "se",
            description = "changes export boolean")
    public void switchExport() {
        this.export = !export;
        showconfig();
    }

    /**
     * Changes the value of stats
     */
    @Command(name = "switchstats",
            abbrev = "ss",
            description = "changes statistics boolean")
    public void switchStats() {
        this.stats = !stats;
        showconfig();
    }

    /**
     * Sets the value for the maximum entries to create definitions for
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
     * Changes the filepath to the Wikipedia XML dump
     *
     * @param filepath path to Wikipedia XML dump
     */
    @Command(name = "setpath",
            abbrev = "sp",
            description = "stets filepath")
    public void setPath(@Param(name = "path", description = "path to wikipedia XML dump") String filepath) {
        this.filepath = filepath;
        showconfig();
    }

    /**
     * Changes the amount of threads to open
     *
     * @param threads amount of threads to open
     */
    @Command(name = "setthreads",
            abbrev = "st",
            description = "stets number of threads to use")
    public void setThreads(@Param(name = "threads", description = "amount of threads spawned") int threads) {
        this.threads = threads;
        showconfig();
    }
}