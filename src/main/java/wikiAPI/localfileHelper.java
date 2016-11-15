package wikiAPI;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;

import static java.lang.StrictMath.round;

class localfileHelper extends Thread {
    private int id,
                max,
                pages = 0,
                definitions = 0,
                errors = 0;
    private long chunksize;
    private RandomAccessFile file;


    localfileHelper(String path, int threadid, int threadcount, int max) throws IOException {
        this.id = threadid;
        this.max = max;
        try {
            file = new RandomAccessFile(path, "r");
            this.chunksize =  (file.getChannel().size() / threadcount);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }


    @Override
    public void run() {
        //umorganisieren?
        xmlHelper xml = new xmlHelper();
        textHelper text = new textHelper();
        String page = "",
                line,
                stoppingreason = "chunkend reached!";
        long start = 0,
                pos,
                stop = chunksize * (id + 1);
        long startTime = System.currentTimeMillis();

        try {
            file.seek(chunksize * (id));
            while (true) {
                pos = file.getChannel().position();
                if ((line = file.readLine()) == (null) || pos >= stop)  break;
                //if (line.contains("<page id=")) { //fuer uni daten
                if (line.contains("<page>")) {
                    pages++;
                    if (pages == 1) start = file.getChannel().position();
                    if (pages > max) {
                        stoppingreason = "max sites parsed!";
                        break;
                    }
                    line = file.readLine();
                    while (!line.contains("</page>")) {
                        page += line + "\n";
                        line = file.readLine();
                    }
                    String article = xml.getTagValue(page, "text");
                    String title = xml.getTagValue(page, "title");
                    String definition = text.getDefinition(article);
                    if (evaluateDefinition(definition)) {
                        //TODO to db?
                        System.out.println(title + " : " + definition);
                    }
                    page = "";
                }
            }
            file.close();
            //Threadstats
            printStats(stoppingreason, System.currentTimeMillis() - startTime, start, pos);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    private boolean evaluateDefinition(String definition) {
        if (definition.length() < 50 || definition.length() > 2000) {
            errors++;
            return false;
        } else {
            definitions++;
            return true;
        }
    }


    private void printStats(String reason, long time, long start, long stop) {
        System.out.println(
                        "\nThread" + id + " stopped:\t" + reason +
                        "\n" + round((stop - start)/1000.) + "kB from:\t\t" + start + " to " + stop +
                        "\nTime (in millis):\t" + time + " (" + round(time/1000.) + " s)"+
                        "\nProcessed pages:\t" + (definitions + errors) + " (" + pages + "found)"+
                                "\nDefGen coverage:\t" + round((definitions / ((definitions + errors) * 1.) * 100.)) +
                        "% (+" + definitions + "/ -" + errors + ")"
                        );
    }
}


class filetest {
    public static void main(String[] args) throws IOException {
        int threadnr = 1;
        int maxpages = 10;
        if(threadnr >Runtime.getRuntime().availableProcessors()){
            threadnr=Runtime.getRuntime().availableProcessors();
        }

        String path = "C:\\Users\\rene2\\Desktop\\dewikidump.xml";
        //spawns and starts threads
        ArrayList<localfileHelper> threads = new ArrayList<>();
        for (int i = 0; i < threadnr; i++) {
            threads.add(new localfileHelper(path, i, threadnr, maxpages / threadnr));
        }
        for (localfileHelper thread : threads) {
            thread.start();
        }
    }
}