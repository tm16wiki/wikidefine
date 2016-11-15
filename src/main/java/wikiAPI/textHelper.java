package wikiAPI;


import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

class textHelper {

    // -{{Begriffsklärungshinweis}} -> flag mehrdeutig
    // *{{Siehe-auch|Liste-von-Straßen-und-Plätzen-in-Leipzig}} tags
    // *{{Hauptartikel|Geschichte-der-Stadt-Leipzig}}
    // *{{Infobox-...
    // *{{Nachbargemeinden...
    // *{{Panorama|
    // *{|-class=&quot;wikitable&quot;...
    // *{{Wahldiagramm... {{Sitzverteilung
    // -Links &lt;ref&gt;[URL text]datum&lt;/ref&gt;
    // *
    // *{{Internetquelle |url...
    // *&amp;nbsp;
    // *{{Zitat|...


    ArrayList<String> findFiles(String text) {
        ArrayList<String> files = new ArrayList<>();
        String pattern = "(\\[\\[)Datei:(.*)(\\]\\])";
        Pattern r = Pattern.compile(pattern);
        Matcher m = r.matcher(text);
        while (m.find()) {
            String file = m.group();
            //TODO: filetags finden?
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
        ArrayList<String> articles = new ArrayList<>();
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
        ArrayList<String> categories = new ArrayList<>();
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
        ArrayList<String> weblinks = new ArrayList<>();
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
        ArrayList<String> objects = new ArrayList<>();
        //TODO infoboxen und {| class... |} richtig entfernen
        //2 stufig um verschachtelung von objekten aufzuloesen
        for(int i=0; i<2; i++){
            String pattern = "\\{\\{([^{}]*)\\}\\}";
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


    private String extractText(String article) {

        //remove html comments
        String extract = article.replaceAll("<!--(.*)-->", "")
                //remove category tag
                .replaceAll("(\\[\\[)Kategorie:([^\\[\\]]*)(\\]\\])", "")
                //remove files
                .replaceAll("(\\[\\[)Datei:(.*)(\\]\\])", "")
                //remove wikipediaClasses
                .replaceAll("\\{\\| class=&quot;(\\w*)&quot;", "");
        //remove articles keep text
        Pattern r = Pattern.compile("(\\[\\[)([^:\\[\\]]*)(\\]\\])");
        Matcher m = r.matcher(extract);
        while (m.find()) {
            String atext = m.group(2);
            String text;
            //removes text link from article tag
            if (atext.contains("|")) {
                text = atext.substring(atext.indexOf("|") + 1, atext.length());
                extract = extract.replace(m.group(), text);
            } else {
                extract = extract.replace(m.group(), m.group(2));
            }
        }

        //remove wikipediaobjects
        for (int i = 0; i < 5; i++) {
            String pattern = "(\\{\\{)([^\\{\\}]*)(\\}\\})";
            r = Pattern.compile(pattern);
            m = r.matcher(extract);
            while (m.find()) {
                extract = extract.replace(m.group(), "");
            }
        }
        return extract;
    }


    String getDefinition(String article) {
        String xml = extractText(article); // clear xml tags
        //reduce to first paragraph
        try {
            while (xml.indexOf("\n") == 0) {
                xml = xml.substring(1, xml.length());
            }
            xml = xml.substring(0, xml.indexOf("\n"));
        } catch (Exception e) {
            e.printStackTrace();
        }
        //TODO besser machen als nochmal durch jsoup jagen..?
        Document doc = Jsoup.parse(xml);
        //System.out.println(doc.text().trim());
        xml = doc.text().trim();
        //remove html artifacts
        xml = xml.replaceAll("(<ref>)([^<>]*  )(</ref>)", "")
                .replaceAll("(<ref([^<>]*)>)([^<>]*)(</ref>)", "")
                .replaceAll("<!--(.*)-->", "")
                .replaceAll("'''", "\"")
                .replaceAll("''", "\"")
                .replaceAll("\\[\\]", "")
                .replaceAll("\\(\\)", "")
                .replaceAll(" ; ", "")
        ;
        return xml;
    }
}