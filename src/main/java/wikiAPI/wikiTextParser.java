package wikiAPI;


import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class wikiTextParser {

    public ArrayList<String> findFiles(String text) {
        ArrayList<String> files = new ArrayList<>();
        String pattern = "(\\[\\[)Datei:(.*)(\\]\\])";
        Pattern r = Pattern.compile(pattern);
        Matcher m = r.matcher(text);
        while (m.find()) {
            String file = m.group();
            //todo: filetags finden?
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


    public ArrayList<String> findArticles(String text) {
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


    public ArrayList<String> findCategories(String text) {
        ArrayList<String> categories = new ArrayList<>();
        String pattern = "(\\[\\[)Kategorie:([^\\[\\]]*)(\\]\\])";
        Pattern r = Pattern.compile(pattern);
        Matcher m = r.matcher(text);
        while (m.find()) {
            categories.add( m.group()
                            .replace("[[", "")
                            .replace("]]", "")
                            .replace("Kategorie:", "")
                            .replace("|", "")
            );
        }
        return categories;
    }


    public ArrayList<String> findWeblinks(String text) {
        ArrayList<String> weblinks = new ArrayList<>();
        String pattern = "&lt;ref([^\\[]*)&gt;\\[([\\S]*) ([^\\]]*)\\]([^/]*)&lt;/ref&gt;";
        Pattern r = Pattern.compile(pattern);
        Matcher m = r.matcher(text);
        while (m.find()) {
            String weblink = m.group(2);
            weblinks.add(weblink);
        }
        return weblinks;
    }


    public ArrayList<String> findWikiObject(String text) {
        ArrayList<String> objects = new ArrayList<>();
        //todo infoboxen und {| class... |} zu csv?
        //4 stufig um verschachtelung von objekten aufzuloesen
        for(int i=0; i<4; i++){
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


    public boolean isPolysemous(String text) {
        return text.contains("{{Begriffsklärungshinweis}}");
    }


    public String extractText(String article) {
        if(article==null){
            return null;
        }
        //TODO: Listen (z.b. Apollo, Tenor (Begriffsklärung), DPA, GBI)


        //remove all < > </ > tags
        String extract = article.replaceAll("!--(.*)--", "");
        extract = extract.replaceAll("ref(.*)/ref", "");
        extract = extract.replaceAll("math(.*)/math", "");


        //remove all [[ ]] tags
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
        //remove files categories
        extract = extract.replaceAll("(\\[\\[)(\\w)*:(.*)(\\]\\])", "")
                         .replaceAll("&lt;!--(.*)--&gt;", "");


        //remove all {{ }} tags
        //remove wikipediaobjects
        //4 level deep e.g. infoboxes
        //todo: z.b. Talk (Mineral)
        for (int i = 0; i < 5; i++) {
            r = Pattern.compile("(\\{\\{)([^\\{\\}]*)(\\}\\})");
            m = r.matcher(extract);
            while (m.find()) {
                extract = extract.replace(m.group(), "");
            }
        }


        //wikiclasses
        //todo: z.B. Region Stuttgart, 1256
        //bessere regex finden
        extract = extract.replaceAll("\n\\|(?:.*)", "");
        extract = extract.replaceAll("\\{\\| class=(?:.*)", "");


        //remove headlines
        extract = extract.replaceAll("== (.*) ==", "");
        extract = extract.replaceAll("==", " ");


        //remove artifacts
        // remove brackets and content in brackets
        extract = extract.replaceAll("'''", "\"");
        extract = extract.replaceAll("''", "\"");
        extract = extract.replaceAll("\"\"", "");
        extract = extract.replaceAll("&nbsp;", "");
        extract = extract.replaceAll("&shy;", "");
        extract = extract.replaceAll("&amp;", "");
        extract = extract.replaceAll(" ; ", "");
        extract = extract.replaceAll("�", "");
        extract = extract.replaceAll("<(.*)>", "");
        extract = extract.replaceAll("\\s*\\([^)]*\\)", "");
        extract = extract.replaceAll("\"\" ", "");
        extract = extract.replaceAll("\\[\\]", "");
        extract = extract.replaceAll("\\(\\)", "");
        extract = extract.replaceAll("&lt;(.*)&gt;", "");
        extract = extract.replaceAll("__NOTOC__", "");

        extract = extract.replaceAll("\n", "");
        return extract;
    }


    public String getDefinition(String article) {
        article = extractText(article);

        //TODO: links werden abgeschnitten [http://www. in Rotaria (Titularbistum)
        // Satzerkennung: Abschnitt generell erst nach 300 Zeichen,
        // dann nach Punkt aber nicht wenn unmittelar vor Punkt nur
        // ein Zeichen oder eine beliebige Zahl steht (z.B. "Er ist der 1. Mensch")
        String[] segs = article.split( Pattern.quote( "." ) );
        String finalstr = "";
        for (String seg : segs) {
            finalstr += seg + ".";
            if (finalstr.length() >= 300) {
                break;
            }
        }
        return finalstr;
    }
}
