package CLI;

import asg.cliche.Command;
import asg.cliche.Param;
import asg.cliche.Shell;
import asg.cliche.ShellDependent;
import helperClasses.db;
import wikiAPI.wikiFileDumpParser;

public class fileDumpCLI implements ShellDependent {
    private Shell theShell;

    private boolean stats = false;
    private boolean verbose = true;
    private boolean export = true;
    private int max = Integer.MAX_VALUE;;
    private int threads = 4;
    private String filepath;
    private String dbpath;
    private db exportDB;

    fileDumpCLI(config config) {
        this.filepath = config.getFilepath();
        if (config.getDbpath() != null) {
            this.exportDB = config.getDatabase();
            this.dbpath = config.getDbpath();
        }
        showconfig();
    }

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


    public void cliSetShell(Shell theShell) {
        this.theShell = theShell;
    }

    @Command(name = "switchverbose",
            abbrev = "sv",
            description = "changes verbose boolean")
    public void switchVerbose() {
        this.verbose = !verbose;
        showconfig();
    }

    @Command(name = "switchexport",
            abbrev = "se",
            description = "changes export boolean")
    public void switchExport() {
        this.export = !export;
        showconfig();
    }

    @Command(name = "switchstats",
            abbrev = "ss",
            description = "changes statistics boolean")
    public void switchStats() {
        this.stats = !stats;
        showconfig();
    }

    @Command(name = "settmax",
            abbrev = "sm",
            description = "sets max definition to generate")
    public void setMax(@Param(name = "maximum", description = "max definitions generated") int max) {
        this.max = max;
        showconfig();
    }
    @Command(name = "setpath",
            abbrev = "sp",
            description = "stets filepath")
    public void setPath(@Param(name = "path", description = "filepath") String filepath) {
        this.filepath = filepath;
        showconfig();
    }

    @Command(name = "setthreads",
            abbrev = "st",
            description = "stets number of threads to use")
    public void setThreads(@Param(name = "threads", description = "amount of threads spawned") int threads) {
        this.threads = threads;
        showconfig();
    }

}