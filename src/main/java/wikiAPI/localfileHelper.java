package wikiAPI;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import java.io.*;
import java.util.ArrayList;

import static java.lang.StrictMath.round;


class localfileHelper extends Thread {

    private int max;
    private RandomAccessFile file;
    private int id,
            chunksize,
            pages = 0,
            definitions = 0,
            errors = 0;

    localfileHelper(String path, int threadid, int threadcount, int max) throws IOException {
        this.id = threadid;
        this.max = max;
        try {
            //gehts sicherlich besser
            FileInputStream inputStream = new FileInputStream(path);
            this.chunksize = (inputStream.available() / threadcount);
            inputStream.close();

            file = new RandomAccessFile(path, "r");
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
                if ((line = file.readLine()) == (null) || pos >= stop) {
                    System.out.println();
                    break;
                }
                if (line.contains("<page id=")) {
                    pages++;
                    if (pages == 1) {
                        start = file.getChannel().position();
                    }
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
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private String getDefinition(String article) {
        //TODO: absatz besser extrahieren
        String definition = "";
        try {
            definition = article.substring(0, article.indexOf("\n\n"));
        } catch (StringIndexOutOfBoundsException e) {
            errors++;
        }
        Document doc = Jsoup.parse(definition);
        definition = doc.select("text").text()
                .replaceAll("(\\[http://)([^\\[\\]]*)(\\])", "")
                .replaceAll("\\[ \\]", "")
                .replaceAll("\\( \\)", "");
        //blacklist
        if (definition.length() < 50 || definition.length() > 2000) {
            errors++;
            return "";
        }
        definitions++;
        return definition;
    }

    private void printStats(String reason, long time, long start, long stop) {
        System.out.println(
                "\nThread" + id + " stopped:\t"+reason+
                        "\nChunk (in bytes):\t" + start + " - " + stop +
                        "\nTime (in millis):\t" + time +
                        "\nProcessed pages:\t" + (definitions + errors) + " / " + pages +
                        "\nsucced / failed:\t" + definitions + " / " + errors +
                        "\t(" + round((definitions/(pages*1.)*100.)) +"%)"
            );
    }
}


class filetest {
    public static void main(String[] args) throws IOException {
        int threadnr = 1;
        int maxpages = 500;

        ArrayList<localfileHelper> threads = new ArrayList<>();
        for (int i = 0; i < threadnr; i++) {
            threads.add(new localfileHelper("C:\\Users\\rene2\\Desktop\\dewiki.xml", i, threadnr, maxpages / threadnr));
        }
        for (localfileHelper thread : threads) {
            thread.start();
        }
    }
}
