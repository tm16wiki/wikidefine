package wikiAPI;

import helperClasses.LangFilter;
import helperClasses.xml;
import javafx.concurrent.Task;
import javafx.embed.swing.JFXPanel;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.channels.FileChannel;
import java.util.Objects;

import static java.lang.StrictMath.round;

/**
 * Class for processing wikipedia xml dump as file
 */
public class WikiFileDumpParser extends Task implements Runnable {
    public Thread[] threads;
    private int pages = 0;
    private int prefiltered = 0;
    private int definitions = 0;
    private int errors = 0;
    private long chunksize;
    private long filesize;
    private long progress;
    private String lang;
    private int max;
    private boolean printstats = false;
    private boolean verbose = false;
    private String path;
    private helperClasses.db db;

    /**
     * Constructor for setting up parameter, spawn and run process threads and wait for them
     *
     * @param threadcount number of threads to spawn
     * @param maximum     maximum of pages to process
     * @param printstats  boolean print statistics
     * @param verbose     boolean verbose
     * @param path        path to file
     * @param db          database to store
     */

    public WikiFileDumpParser(int threadcount, int maximum, boolean printstats, boolean verbose, String path, helperClasses.db db) {
        if (threadcount > Runtime.getRuntime().availableProcessors()) {
            this.threads = new Thread[Runtime.getRuntime().availableProcessors()];
        } else
            this.threads = new Thread[threadcount];
        this.max = maximum;
        this.printstats = printstats;
        this.verbose = verbose;
        this.path = path;
        this.db = db;
        new JFXPanel(); // create dummy JFXPanel to avoid "Toolkit not initialized" message

        //autodetect lang
        try {
            RandomAccessFile file = new RandomAccessFile(path, "r");
            filesize = file.getChannel().size();
            this.chunksize = filesize / threads.length;
            //get first line
            String line = file.readLine();
            //cut xml:lang ..
            this.lang = line.substring(line.indexOf("xml:lang=\""), line.lastIndexOf("\">"));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Prints results and time needed
     *
     * @param starttime starting time
     */
    private void generateStatistics(long starttime) {
        //calculate time needed
        long time = System.currentTimeMillis() - starttime - 100;
        //print information
        System.out.println(
                "finished after " + round(time / 1000.) + " s  (" + time + " millis).\n" +
                        round((definitions / ((definitions + errors) * 1.) * 100.)) + "% ( +" + definitions + " / -" + errors + " )" +
                        " prefiltered: " + prefiltered
        );
    }

    /**
     * Calls the threads to read the XML file
     *
     * @return Chunks
     * @throws Exception File not found exception
     */
    @Override
    protected Object call() throws Exception {
        try {
            long startTime = System.currentTimeMillis();

            for (int i = 0; i < threads.length; i++) {
                threads[i] = new fileThread(new RandomAccessFile(path, "r"), i);
                threads[i].start();
            }

            synchronized (Objects.requireNonNull(threads)) {
                if (max == Integer.MAX_VALUE) {
                    while (progress < filesize) {
                        Thread.sleep(1000);
                        updateProgress(progress, filesize);
                    }
                } else {
                    while (pages < max) {
                        Thread.sleep(1000);
                        updateProgress(pages, max);
                    }
                }
                Thread.sleep(5000);
                updateProgress(0, 100);
            }

            if (printstats) {
                generateStatistics(startTime);
            }
        } catch (FileNotFoundException | NullPointerException e) {
            System.out.println("file not found!");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Starts to call the threads
     */
    @Override
    public void run() {
        try {
            call();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Threads for XML processing
     */
    private class fileThread extends Thread {
        private int id;
        private RandomAccessFile file;

        /**
         * Constructor for processing thread
         *
         * @param file     file to process
         * @param threadid id of the thread
         */
        fileThread(RandomAccessFile file, int threadid) {
            this.id = threadid;
            this.file = file;
        }

        /**
         * Processes filechunk generates definition and stores it to db
         */
        @Override
        public synchronized void run() {
            xml xml = new xml();
            long oldprogress = chunksize * (id);

            WikiTextParser text = new WikiTextParser();
            String page = "", line;
            try {
                //seek to chunkstart
                file.seek(chunksize * (id));
                long pos = file.getChannel().position();
                while ((line = file.readLine()) != (null) && pos < chunksize * (id + 1) && pages - prefiltered <= max) {
                    pos = file.getChannel().position();
                    if (line.contains("<page>")) { //line.contains("<page id=") { //for preprocessed pages
                        pages++;
                        progress += pos - oldprogress;
                        oldprogress = pos;
                        line = readLineUTF();
                        boolean reject = false;
                        while (!Objects.requireNonNull(line).contains("</page>")) {
                            if (LangFilter.check(lang, line) || LangFilter.check("EN", line)) {
                                prefiltered++;
                                reject = true;
                                break;
                                //TODO: better condition

                            } else if (line.contains("==")) {
                                break;
                            }
                            page += line + "\n";
                            //line = file.readLine();
                            line = readLineUTF();
                        }
                        if (!reject) {
                            String article = xml.getTagValue(page, "text");
                            String title = xml.getTagValue(page, "title");
                            int id = Integer.parseInt(xml.getTagValue(page, "id"));
                            String definition = text.getDefinition(article);
                            //postevaluate
                            if (evaluateDefinition(definition)) {
                                if (verbose) {
                                    //System.out.println("+ " + title + " : " + definition);
                                    System.out.println(title + " : " + definition);
                                }
                                if (db != null) {
                                    db.insertDefinition(id, title, definition);
                                }
                            } else {
                                if (verbose) {
                                    //System.out.println("- " + title + " : " + definition);
                                }
                            }
                        }
                        page = "";
                    }
                }
                file.close();
            } catch (NullPointerException | IOException e) {
                e.printStackTrace();
            }
        }


        /**
         * Reads next line from cursor and converts it to UTF-8
         *
         * @return next line as string
         */
        private String readLineUTF() {
            //set buffersize
            int buffersize = 256;
            try {
                String line = "";
                //remember position
                long pos = file.getChannel().position();
                do {
                    byte[] charbuffer = new byte[buffersize];
                    for (int i = 0; i < buffersize; i++) {
                        //fill charbuffer
                        charbuffer[i] = (byte) file.read();
                    }
                    //add buffer to line
                    line += new String(charbuffer, "UTF-8");
                    //repeat until linebreak reched
                } while (!line.contains("\n"));
                //shorten line until lineend
                line = line.substring(0, line.indexOf("\n"));
                //set cursor back to lineend
                file.getChannel().position(pos + line.getBytes().length + 1); //schneller als file.seek ..
                return line;
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        }

        /**
         * Checks if definition has the right length
         *
         * @param definition definition
         * @return returns true if the definition is valid and false if it isn't
         */
        private boolean evaluateDefinition(String definition) {
            if (definition.length() < 30 || definition.length() > 1000) {
                errors++;
                return false;
            } else {
                definitions++;
                return true;
            }
        }
    }
}