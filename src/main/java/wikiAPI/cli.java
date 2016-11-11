package wikiAPI;

import java.io.IOException;
import java.util.Scanner;

public class cli {

    public static void main(String[] args) throws IOException {
        Scanner scan = new Scanner(System.in);
        System.out.print("Define: ");
        String term = scan.nextLine().replaceAll(" ", "_"); //"Leipzig"; //

        //dbHelper postgresDB = new dbHelper("postgresql://127.0.0.1:5432/textmining", "postgres", "postgres");
        //dbHelper sqliteDB = new dbHelper("db.db");
        //scan.next();
        textHelper text = new textHelper();
        xmlHelper xml = new xmlHelper();
        httpsHelper https = new httpsHelper();
        if(!https.loadURL("https://de.wikipedia.org/wiki/Spezial:Exportieren/"+term)) return;
        String content = xml.getTagValue(https.getContent(), "text");

        //https Info
        System.out.println("articleurl:\t\t\t" + https.getURL());
        System.out.println("timestamp:\t\t\t" + https.getTimestamp().toString());

        //xml info
        System.out.println("articleid:\t\t\t" + xml.getId(https.getContent()));
        System.out.println("creator:\t\t\t" + xml.getUser(https.getContent()));
        System.out.println("sha1sum:\t\t\t" + xml.getChecksum(https.getContent()));

        //Mining Info
        System.out.println("mehrdeutig:\t\t\t" + text.isPolysemous(content));
        System.out.println("weblinks:\t\t\t" + text.findWeblinks(content).size());
        //text.findWeblinks(content).forEach(System.out::println);
        System.out.println("linked articles:\t" + text.findArticles(content).size());
        //text.findArticles(content).forEach(System.out::println);
        System.out.println("linked files:\t\t" + text.findFiles(content).size());
        //text.findFiles(content).forEach(System.out::println);
        System.out.println("categories:\t\t\t" + text.findCategories(content).size());
        //text.findCategories(content).forEach(System.out::println);
        System.out.println("WikiObjects:\t\t" + text.findWikiObject(content).size());
        //text.findWikiObject(content).forEach(System.out::println);

        //whole content
        //System.out.println(text.clearXML(content));


        //Test Definition
        String definition = text.clearXML(content);
        while (definition.indexOf("\n")==0){
            definition = definition.substring(1, definition.length());
        }
        definition = definition.substring(0, definition.indexOf("\n"));
        definition = definition.replaceAll("'''", "\"");
        definition = definition.replaceAll("''", "\"");
        //zeilenumbruch

        System.out.println("\n\n"+definition);

    }

}
