package wikiAPI;

import helperClasses.xml;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;

import static java.lang.StrictMath.round;


public class wikiFileDumpParser {
    private boolean printstats = false;
    private boolean verbose = false;
    private helperClasses.db db;

    public wikiFileDumpParser(int threadnr, int maxpages, boolean printstats, boolean verbose, String path, helperClasses.db db) {
        this.printstats = printstats;
        this.verbose = verbose;
        this.db = db;
        if (threadnr > Runtime.getRuntime().availableProcessors()) {
            threadnr = Runtime.getRuntime().availableProcessors();
        }
        ArrayList<fileThread> threads = new ArrayList<>();
        for (int i = 0; i < threadnr; i++) {
            threads.add(new fileThread(path, i, threadnr, 0, (maxpages / threadnr)));
        }
        for (fileThread thread : threads) {
            thread.start();
        }
    }


    private class fileThread extends Thread {
        private int id,
                max,
                pages,
                filtered = 0,
                definitions = 0,
                errors = 0;
        private long chunksize;
        private RandomAccessFile file;

        fileThread(String path, int threadid, int threadcount, int pages, int max) {
            this.id = threadid;
            this.pages = pages;
            this.max = max;
            try {
                file = new RandomAccessFile(path, "r");
                this.chunksize = (file.getChannel().size() / threadcount);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        @Override
        public synchronized void run() {
            xml xml = new xml();
            wikiTextParser text = new wikiTextParser();
            String page = "",
                    line;
            long start = 0,
                    pos;
            long startTime = System.currentTimeMillis();

            try {
                //seek to chunkstart
                file.seek(chunksize * (id));
                while (true) {
                    pos = file.getChannel().position();
                    if ((line = file.readLine()) == (null) || pos >= chunksize * (id + 1))
                        break; //readline is faster;
                    //if (line.contains("<page id=")) { //for preprocessed pages
                    if (line.contains("<page>")) {
                        pages++;
                        if (pages == 1) start = file.getChannel().position();
                        if (pages - filtered > max) break;
                        //own implementation...
                        line = readLineUTF();
                        boolean reject = false;
                        while (!line.contains("</page>") & !(line == null)) {

                            //TODO: prefilter for lists or redirects
                            if (line.contains("#REDIRECT") ||
                                    line.contains("#WEITERLEITUNG") ||
                                    line.contains("<title>List") ||
                                    line.contains("#redirect") ||
                                    line.contains("#Redirect")
                                    ) {
                                page += "FILTERED</text>";
                                filtered++;
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
                                    System.out.println(title + " : " + definition);
                                }
                                if (db != null) {
                                    db.insertDefinition(title, definition);
                                }
                            }
                        }
                        page = "";
                    }
                }
                file.close();
                if (printstats) {
                    printStats(System.currentTimeMillis() - startTime, start, pos);
                }
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

        private void printStats(long time, long start, long stop) {
            System.out.println("\n==================   \tThread" + id + "   \t==================" +
                    "\n Chunk: \t" + round((stop - start) / 1000000.) / 1000. + " GB\tfrom: " + start + ", to: " + stop + "" +
                    "\n Time:  \t" + round(time / 1000.) + " s    \tmilis: " + time + "" +
                    "\n Pages: \t" + (definitions + errors) + "    \t\tfound: " + pages + ", filtered: " + filtered + "" +
                    "\n DefGen:\t" + round((definitions / ((definitions + errors) * 1.) * 100.)) + "%    \t\t+ " + definitions + "/ - " + errors + "");
        }
    }
}