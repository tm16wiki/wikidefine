package CLI;

import asg.cliche.Command;
import asg.cliche.Param;
import asg.cliche.Shell;
import asg.cliche.ShellDependent;
import helperClasses.db;
import wikiAPI.wikiFileDumpParser;

public class fileDumpCLI implements ShellDependent {
    private Shell theShell;

    private boolean stats;
    private boolean verbose;
    private boolean export;
    private int max;
    private int threads;
    private String filepath;
    private String dbpath;
    private db exportDB;

    fileDumpCLI(config config) {
        this.stats = false;
        this.verbose = true;
        this.export = false;
        this.threads = 4;
        this.max = Integer.MAX_VALUE;
        this.filepath = filepath;
        if (config.getDbpath() != null) {
            this.exportDB = config.getDatabase();
            this.dbpath = config.getDbpath();
        }
        showconfig();
    }


    public void cliSetShell(Shell theShell) {
        this.theShell = theShell;
    }

    @Command(name = "setverbose",
            abbrev = "sv",
            description = "sets verbose boolean")
    public void setVerbose(@Param(name = "printverbose", description = "printverboseboolean") boolean verbose) {
        this.verbose = verbose;
        showconfig();
    }

    @Command(name = "setexport",
            abbrev = "se",
            description = "sets db export boolean")
    public void setExport(@Param(name = "dbexport", description = "dbexportboolean") boolean export) {
        this.export = export;
        showconfig();
    }

    @Command(name = "setstats",
            abbrev = "ss",
            description = "stets statistics boolean")
    public void setStats(@Param(name = "printstats", description = "printstatsboolean") boolean stats) {
        this.stats = stats;
        showconfig();
    }

    @Command(name = "setmax",
            abbrev = "sm",
            description = "sets max definition to generate")
    public void setMax(@Param(name = "maximum", description = "max definitions generated") int max) {
        this.max = max;
        showconfig();
    }

    @Command(name = "setthreads",
            abbrev = "st",
            description = "stets number of threads to use")
    public void setThreads(@Param(name = "threads", description = "amount of threads spawned") int threads) {
        this.threads = threads;
        showconfig();
    }

    @Command(name = "setpath",
            abbrev = "sp",
            description = "stets filepath")
    public void setPath(@Param(name = "path", description = "filepath") String filepath) {
        this.filepath = filepath;
        showconfig();
    }

    @Command(name = "showconfig",
            abbrev = "sc",
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
        System.out.println();
    }

    @Command(name = "run",
            abbrev = "r",
            description = "creates definitions out of file")
    public void run() {
        if (!export) {
            new wikiFileDumpParser(threads, max, stats, verbose, filepath, null);
        } else {
            new wikiFileDumpParser(threads, max, stats, verbose, filepath, exportDB);
        }
    }

    @Command(name = "runfast",
            abbrev = "rf",
            description = "creates all definitions out of file with max speed")
    public void fastrun() {
        new wikiFileDumpParser(Integer.MAX_VALUE, Integer.MAX_VALUE, stats, verbose, filepath, null);
    }


}