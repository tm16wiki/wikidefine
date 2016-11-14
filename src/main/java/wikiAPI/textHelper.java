package wikiAPI;


import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

class textHelper {

    //TODO: -{{Begriffsklärungshinweis}} -> flag mehrdeutig
    //TODO: *{{Siehe-auch|Liste-von-Straßen-und-Plätzen-in-Leipzig}} tags
    //TODO: *{{Hauptartikel|Geschichte-der-Stadt-Leipzig}}
    //TODO: *{{Infobox-...
    //TODO: *{{Nachbargemeinden...
    //TODO: *{{Panorama|
    //TODO: *{|-class=&quot;wikitable&quot;...
    //TODO: *{{Wahldiagramm... {{Sitzverteilung
    //TODO: -Links &lt;ref&gt;[URL text]datum&lt;/ref&gt;
    //TODO: *
    //TODO: *{{Internetquelle |url...
    //TODO: *&amp;nbsp;
    //TODO: *{{Zitat|...


    ArrayList<String> findFiles(String text) {
        ArrayList<String> files = new ArrayList<String>();
        String pattern = "(\\[\\[)Datei:(.*)(\\]\\])";
        Pattern r = Pattern.compile(pattern);
        Matcher m = r.matcher(text);
        while (m.find()) {
            String file = m.group();
            //TODO: filetags?
            //removes picturetags and text from file tag
            if (file.contains("|")) {
                file = file.substring(0, file.indexOf("|"));
            }
            files.add(
                    file
                            .replace("[[", "")
                            .replace("]]", "")
                            .replace("Datei:", "")
            );
        }
        return files;
    }


    ArrayList<String> findArticles(String text) {
        ArrayList<String> articles = new ArrayList<String>();
        //Regex für artikel [[ ... ]] ('[' und ']’ ausgeschlossen falls verschachtelt)
        String pattern = "(\\[\\[)([^:\\[\\]]*)(\\]\\])";
        Pattern r = Pattern.compile(pattern);
        Matcher m = r.matcher(text);
        for(int i=0; i<2; i++) {
            while (m.find()) {
                String article = m.group(2);
                String articletext;
                //removes text link from article tag
                if (article.contains("|")) {
                    articletext = article.substring(article.indexOf("|")+1, article.length());
                    article = article.substring(0, article.lastIndexOf("|"));
                    articles.add(article);
                    text = text.replace(m.group(), articletext);
                    //System.out.println(article + " text: " + articletext);
                } else {
                    articles.add(m.group(2));
                    text = text.replace(m.group(), m.group(2));
                    //System.out.println(m.group(2) + " text: " + m.group(2));
                }
            }
        }
        return articles;
    }


    ArrayList<String> findCategories(String text) {
        ArrayList<String> categories = new ArrayList<String>();
        String pattern = "(\\[\\[)Kategorie:([^\\[\\]]*)(\\]\\])";
        Pattern r = Pattern.compile(pattern);
        Matcher m = r.matcher(text);
        while (m.find()) {
            categories.add(
                    m.group()
                            .replace("[[", "")
                            .replace("]]", "")
                            .replace("Kategorie:", "")
                            //TODO: Überkategorie?
                            .replace("|", "")
            );
        }
        return categories;
    }


    ArrayList<String> findWeblinks(String text) {
        ArrayList<String> weblinks = new ArrayList<String>();
        //"&lt;ref([^\\[]*)&gt;\\[http://([\\S]*) ([^\\]]*)\\]([^/]*)&lt;/ref&gt;" link ohne http://

        String pattern = "&lt;ref([^\\[]*)&gt;\\[([\\S]*) ([^\\]]*)\\]([^/]*)&lt;/ref&gt;";
        Pattern r = Pattern.compile(pattern);
        Matcher m = r.matcher(text);
        while (m.find()) {
            String weblink = m.group(2);
            weblinks.add(weblink);
        }
        return weblinks;
    }


    ArrayList<String> findWikiObject(String text) {
        ArrayList<String> objects = new ArrayList<String>();
        //TODO artikel in objekten
        //TODO {| class... |} richtig entfernen
        //2 stufig um verschachtelung von objekten aufzuloesen
        for(int i=0; i<2; i++){
            String pattern = "\\{\\{([^\\{\\}]*)\\}\\}";
            Pattern r = Pattern.compile(pattern);
            Matcher m = r.matcher(text);
            while (m.find()) {
                objects.add(m.group());
                text = text.replace(m.group(), m.group(1));
            }
        }
        return objects;
    }


    boolean isPolysemous(String text) {
        return text.contains("{{Begriffsklärungshinweis}}");
    }


    String clearXML(String xml) {

        //remove category tag
        xml = xml.replaceAll("(\\[\\[)Kategorie:([^\\[\\]]*)(\\]\\])","");
        //remove files
        xml = xml.replaceAll("(\\[\\[)Datei:(.*)(\\]\\])","");

        //TODO Linktexte erhalten, klassen und objekte unterscheiden
        //TODO {| class... |} richtig entfernen
        //remove wikipediaClasses
        xml = xml.replaceAll("\\{\\| class=&quot;(\\w*)&quot;" , "");
        //remove weblinks
        //xml = xml.replaceAll("&lt;ref([^\\[]*)&gt;\\[http://([\\S]*) ([^\\]]*)\\]([^/]*)&lt;/ref&gt;","");
        //TODO
        //xml = xml.replaceAll("&lt;/ref&gt;","");
        //xml = xml.replaceAll("&lt;ref&gt;","");


        //remove wikipediaobjects
        for(int i=0; i<2; i++){
            String pattern = "\\{\\{([^\\{\\}]*)\\}\\}";
            Pattern r = Pattern.compile(pattern);
            Matcher m = r.matcher(xml);
            while (m.find()) {
                xml = xml.replace(m.group(), "");
            }
        }

        //remove articles keep text
        Pattern r;
        r = Pattern.compile("(\\[\\[)([^:\\[\\]]*)(\\]\\])");
        Matcher m = r.matcher(xml);
        while (m.find()) {
            String article = m.group(2);
            String articletext;
            //removes text link from article tag
            if (article.contains("|")) {
                articletext = article.substring(article.indexOf("|")+1, article.length());
                article = article.substring(0, article.lastIndexOf("|"));
                xml = xml.replace(m.group(), articletext);
                //System.out.println(article + " text: " + articletext);
            } else {
                xml = xml.replace(m.group(), m.group(2));
                //System.out.println(m.group(2) + " text: " + m.group(2));
            }
        }



        Document doc = Jsoup.parse(xml);
        //System.out.println(doc.html());
        //xml = doc.text();


        //remove html comments
        xml = xml.replaceAll("&lt;!--(.*)--&gt;","");
        //remove empty <ref></ref> tags
        xml = xml.replaceAll("&lt;ref&gt;&lt;/ref&gt;","");
        xml = xml.replaceAll("<ref></ref>","");

        return xml;
    }


    String getDefinition(String xml){
        String definition = clearXML(xml); // clear xml tags
        while (definition.indexOf("\n")==0){ // only first paragraph
            definition = definition.substring(1, definition.length());
        }
        definition = definition.substring(0, definition.indexOf("\n"));
        definition = definition.replaceAll("'''", "\"");
        definition = definition.replaceAll("''", "\"");
        definition = definition.replaceAll("\\s*\\([^)]*\\)", ""); // remove brackets
        definition = definition.replaceAll("&lt;ref&gt;\\[[^)]*\\]&lt;\\/ref&gt;", ""); // remove ref links
        return definition;
    }
}