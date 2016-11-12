package wikiAPI;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Scanner;


public class localfileHelper {
    Scanner sc;
    xmlHelper xml = new xmlHelper();
    textHelper text = new textHelper();

    localfileHelper(String path) throws IOException {
        FileInputStream inputStream = null;
        try {
            inputStream = new FileInputStream(path);
            sc = new Scanner(inputStream, "UTF-8");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    String getNextArticle() throws IOException {
        String article = "";
        while (sc.hasNextLine()) {
            String line = sc.nextLine();
            if(line.contains("<page id=")) {
                line = sc.nextLine();
                while (!line.contains("</page>")) {
                    article += line +"\n";
                    line = sc.nextLine();
                }
                System.out.println(xml.getTagValue(article, "title") + " : " + getDefinition(article));
                article = "";
            }
        }
        if (sc.ioException() != null) {
            throw sc.ioException();
        }
        return article;
    }

    String getDefinition(String article){
        String definition = article;
        Document doc = Jsoup.parse(definition);
        definition = doc.select("text").text();
        return definition.replaceAll("\n", "   ");
    }
}


class filetest{
    public static void main(String[] args) throws IOException {
        localfileHelper file = new localfileHelper("C:\\Users\\rene2\\Desktop\\dewiki.xml");
        file.getNextArticle();
    }

}
