package wikiAPI;

import helperClasses.xml;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;

import static java.lang.StrictMath.round;
import static java.lang.Thread.sleep;


public class wikiFileDumpParser {
    private helperClasses.db db;
    private int pages = 0,
            prefiltered = 0,
            definitions = 0,
            errors = 0,
            max,
            threadcount;
    private long chunksize,
            startTime;
    private boolean verbose = false;


    public wikiFileDumpParser(int threadnr, int maxpages, boolean printstats, boolean verbose, String path, helperClasses.db db) {
        if (threadnr > Runtime.getRuntime().availableProcessors()) {
            threadnr = Runtime.getRuntime().availableProcessors();
        }
        this.verbose = verbose;
        this.db = db;
        this.max = maxpages;
        this.threadcount = threadnr;
        try {
            this.startTime = System.currentTimeMillis();
            this.chunksize = new RandomAccessFile(path, "r").getChannel().size() / threadnr;

            fileThread thread = null;
            for (int i = 0; i < threadnr; i++) {
                thread = new fileThread(new RandomAccessFile(path, "r"), i);
                thread.start();
            }
            synchronized (thread) {
                try {
                    thread.wait();
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

    public void stats(long starttime) {
        long time = System.currentTimeMillis() - starttime - 100;
        System.out.println("finished after " + round(time / 1000.) + " s." +
                "\n" + round((definitions / ((definitions + errors) * 1.) * 100.)) + "% ( +" + definitions + " / -" + errors + " )" +
                " prefiltered: " + prefiltered);
    }


    private class fileThread extends Thread {
        private int id;
        private RandomAccessFile file;

        fileThread(RandomAccessFile file, int threadid) {
            this.id = threadid;
            this.file = file;
        }

        @Override
        public synchronized void run() {
            xml xml = new xml();
            wikiTextParser text = new wikiTextParser();
            String page = "",
                    line;

            try {
                //seek to chunkstart
                file.seek(chunksize * (id));

                long pos;
                while (true) {
                    pos = file.getChannel().position();
                    if ((line = file.readLine()) == (null) || pos >= chunksize * (id + 1))
                        break;
                    //if (line.contains("<page id=")) { //for preprocessed pages
                    if (line.contains("<page>")) {
                        pages++;
                        if (pages - prefiltered > max) break;
                        //own implementation...
                        line = readLineUTF();
                        boolean reject = false;
                        while (!line.contains("</page>") & !(line == null)) {

                            //TODO: prefilter for lists or redirects
                            if (line.contains("#REDIRECT") ||
                                    line.contains("#WEITERLEITUNG") ||
                                    line.contains("<title>List") ||
                                    line.contains("<title>Wikipedia:") ||
                                    line.contains("#redirect") ||
                                    line.contains("#Redirect")
                                    ) {
                                page += "FILTERED</text>";
                                prefiltered++;
                                reject = true;
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
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        private String readLineUTF() {
            int buffersize = 128;
            try {
                String line = "";
                long pos = file.getChannel().position();
                do {
                    byte[] charbuffer = new byte[buffersize];
                    for (int i = 0; i < buffersize; i++) {
                        charbuffer[i] = (byte) file.read();
                    }
                    line += new String(charbuffer, "UTF-8");
                } while (!line.contains("\n"));
                line = line.substring(0, line.indexOf("\n"));
                file.getChannel().position(pos + line.getBytes().length + 1); //schneller als file.seek ..
                return line;
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        }

        private boolean evaluateDefinition(String definition) {
            if (definition.length() < 50 || definition.length() > 3000) {
                errors++;
                return false;
            } else {
                definitions++;
                return true;
            }
        }

    }
}