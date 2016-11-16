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

                if ((line = file.readLine()) == (null) || pos >= stop) break;
                //if (line.contains("<page id=")) { //fuer uni daten
                if (line.contains("<page>")) {
                    pages++;
                    if (pages == 1) start = file.getChannel().position();
                    if (pages > max) {
                        stoppingreason = "max sites parsed!";
                        break;
                    }
                    line = readLineUTF();
                    while (!line.contains("</page>") & !(line == null)) {
                        page += line + "\n";
                        //line = file.readLine();
                        line = readLineUTF();
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


    private String readLineUTF() {
        //TODO buffergröße seehr wichtig für performance
        try {
            String l = "";
            long pos = file.getChannel().position();
            do {
                byte[] c = new byte[128];
                for (int i = 0; i < 128; i++) {
                    c[i] = (byte) file.read();
                }
                l += new String(c, "UTF-8");
                if (l.contains("\n")) {
                    break;
                }
            } while (true);
            l = l.substring(0, l.indexOf("\n"));
            file.getChannel().position(pos + l.getBytes().length + 1); //schneller als file.seek ..
            return l;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
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
        int maxpages = 50;
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