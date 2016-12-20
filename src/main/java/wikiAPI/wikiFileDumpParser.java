package wikiAPI;

import helperClasses.xml;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.Objects;

import static java.lang.StrictMath.round;
import static java.lang.Thread.sleep;

/**
 * class for processing wikipedia xml dump as file
 */
public class wikiFileDumpParser {
    private helperClasses.db db;
    private int pages = 0;
    private int prefiltered = 0;
    private int definitions = 0;
    private int errors = 0;
    private int max;
    private long chunksize;
    private boolean verbose = false;

    /**
     * constructor for setting up parameter, spawn and run processthreads and wait for them
     * @param threads number of threads to spawn
     * @param maximum maximum of pages to process
     * @param printstats boolean print statistics
     * @param verbose boolean verbose
     * @param path path to file
     * @param db database to store
     */
    public wikiFileDumpParser(int threads, int maximum, boolean printstats, boolean verbose, String path, helperClasses.db db) {
        if (threads > Runtime.getRuntime().availableProcessors()) {
            threads = Runtime.getRuntime().availableProcessors();
        }
        this.verbose = verbose;
        this.db = db;
        this.max = maximum;
        try {
            long startTime = System.currentTimeMillis();
            this.chunksize = new RandomAccessFile(path, "r").getChannel().size() / threads;

            fileThread thread = null;
            for (int i = 0; i < threads; i++) {
                thread = new fileThread(new RandomAccessFile(path, "r"), i);
                thread.start();
            }
            //synchronize over last thread
            synchronized (Objects.requireNonNull(thread)) {
                try {
                    //wait for last thread to finish
                    thread.wait();
                    //wait some time to finish the printprocess
                    sleep(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            if (printstats) {
                stats(startTime);
            }
        } catch (FileNotFoundException | NullPointerException e) {
            System.out.println("file not found!");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * prints results and time needed
     * @param starttime starttime
     */
    private void stats(long starttime) {
        //calculate time needed
        long time = System.currentTimeMillis() - starttime - 100;
        //print information
        System.out.println(
                "finished after " + round(time / 1000.) + " s  (" + time + " millis).\n" +
                round((definitions / ((definitions + errors) * 1.) * 100.)) + "% ( +" + definitions + " / -" + errors + " )" +
                " prefiltered: " + prefiltered
        );
    }


    private class fileThread extends Thread {
        private int id;
        private RandomAccessFile file;

        /**
         * constructor for processing thread
         * @param file file to process
         * @param threadid id of the thread
         */
        fileThread(RandomAccessFile file, int threadid) {
            this.id = threadid;
            this.file = file;
        }


        /**
         * processes filechunk generates definition and stores it to db
         */
        @Override
        public synchronized void run() {
            xml xml = new xml();
            //wikiTextParser text = new wikiTextParser();
            newTextParser text = new newTextParser();
            String page = "", line;
            try {
                //seek to chunkstart
                file.seek(chunksize * (id));
                long pos = file.getChannel().position();
                while ((line = file.readLine()) != (null) && pos < chunksize * (id + 1) && pages-prefiltered<=max) {
                    pos = file.getChannel().position();
                    if (line.contains("<page>")) { //line.contains("<page id=") { //for preprocessed pages
                        pages++;
                        line = readLineUTF();
                        boolean reject = false;
                        while (!Objects.requireNonNull(line).contains("</page>") ) {
                            //TODO: prefilter for lists or redirects
                            if (line.contains("#REDIRECT") ||
                                    line.contains("#WEITERLEITUNG") ||
                                    line.contains("<title>List") ||
                                    line.contains("<title>Wikipedia:") ||
                                    line.contains("#redirect") ||
                                    line.contains("#Redirect")
                                    ) {
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
                            String definition = text.getDefinition(article);
                            //TODO: postevaluate
                            if (evaluateDefinition(definition)) {
                                if (verbose) {
                                    System.out.println("+ " + title + " : " + definition);
                                }
                                if (db != null) {
                                    db.insertDefinition(title, definition);
                                }
                            } else {
                                if (verbose) {
                                    System.out.println("- " + title + " : " + definition);
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
         * reads next line from cursor and converts it to UTF-8
         * @return next line as string
         */
        private String readLineUTF() {
            //set buffersize
            int buffersize = 128;
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
         * cheacks if definition has the right length
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