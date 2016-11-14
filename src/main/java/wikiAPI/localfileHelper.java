package wikiAPI;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import java.io.*;
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
        String article = "",
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
                //threadprocess
                //System.out.println(threadnr+" : " + pos+ " " + chunksize*threadnr);
                if ((line = file.readLine()) == (null) || pos >= stop)  break;
                if (line.contains("<page id=")) {
                    pages++;
                    if (pages == 1) start = file.getChannel().position();
                    if (pages > max) {
                        stoppingreason = "max sites parsed!";
                        break;
                    }
                    line = file.readLine();
                    while (!line.contains("</page>")) {
                        article += line + "\n";
                        line = file.readLine();
                    }
                    //TODO: create definition and store to db?
                    getDefinition(article);
                    //xmlHelper xml= new xmlHelper();
                    //System.out.println(xml.getTagValue(article, "title") + " : " + getDefinition(article));
                    article = "";
                }
            }
            file.close();
            //Threadstats
            printStats(stoppingreason, System.currentTimeMillis() - startTime, start, pos);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private String getDefinition(String article) throws Exception {
        //TODO: absatz besser extrahieren, encoding...
        String definition;
        definition = new String(article.getBytes("UTF-8"));
        //cut definition
        if (article.indexOf("\n\n") > 0) {
            definition = definition.substring(0, article.indexOf("\n\n"));
        }
        Document doc = Jsoup.parse(definition);
        definition = doc.select("text").text().trim()
                //        .replaceAll("(\\[http://)([^\\[\\]]*)(\\])", "")
                .replaceAll("\\[ \\]", "")
                .replaceAll("\\( \\)", "")
                .replaceAll(" ; ", "")
                //wtf?! wozu benutz ich denn einen parser...
                .replaceAll("Ã¼", "ü")
                .replaceAll("Ã¤", "ä")
                .replaceAll("Ã¶", "ö")
                .replaceAll("Ã\u009F", "ß")
        ;
        //blacklist
        if (definition.length() < 50 || definition.length() > 2000) {
            errors++;
            return "";
        }
        definitions++;
        return new String(definition.getBytes("UTF-8"));
    }

    private void printStats(String reason, long time, long start, long stop) {
        System.out.println(
                        "\nThread" + id + " stopped:\t" + reason +
                        "\n" + round((stop - start)/1000.) + "kB from:\t\t" + start + " to " + stop +
                        "\nTime (in millis):\t" + time + " (" + round(time/1000.) + " s)"+
                        "\nProcessed pages:\t" + (definitions + errors) + " (" + pages + "found)"+
                        "\nDefGen coverage:\t" + round((definitions / (pages * 1.) * 100.)) +
                        "% (+" + definitions + "/ -" + errors + ")"
                        );
    }
}


class filetest {
    public static void main(String[] args) throws IOException {
        int threadnr = 4;
        int maxpages = 100;
        if(threadnr >Runtime.getRuntime().availableProcessors()){
            threadnr=Runtime.getRuntime().availableProcessors();
        }

        String path = "C:\\Users\\rene2\\Desktop\\dewiki.xml";
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