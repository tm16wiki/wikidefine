package wikiAPI;


import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

public class wikiTextParser {

    // replacements for german language - to be commented
    static String[][] replacementsDE = {
            //remove headlines
            {"== (.*) ==", ""},
            {"==", " "},
            //remove links
            {"\\[([^\\[]*)\\]", ""},

            //remove artifacts
            // remove brackets and content in brackets
            {"'''", "\""},
            {"''", "\""},
            {"\"\"", ""},
            {"&nbsp;", " "},
            {"nbsp;", " "},
            {"&shy;", ""},
            {"&amp;", ""},
            {" ; ", ""},
            {"�", ""},
            //{"<(.*)>", ""}, // BAD - demo: Adzukibohne
            {"^\\[", ""},
            {"\\s*\\([^)]*\\)", ""},
            {"\"\" ", ""},
            {"\\[\\]", ""},
            {"\\(\\)", ""},
            {"&lt;&gt;", ""},
            {"__NOTOC__", ""},
            {"\n", ""},
    };

    /**
     * Called by FileDumpParser and webdefinition
     * @param article Content of text-tag in XML
     * @return clean compact definition string
     */
    public String getDefinition(String article) {

        article = extractText(article); // remove additional info

        article = cleanString(article, "de"); // remove and replace special characters not necessary for the definition

        article = removeTags(article); // remove ref-tags
        article = shortenDefinition(article); // trim length
        article = removeWhiteSpaces(article);

        return article;
    }

    /**
     * Extract definition part by removing additional info
     * @param article Content of text-tag in XML
     * @return whole dirty definition string
     */
    private String extractText(String article) {
        if(article==null){
            return null;
        }
        //TODO: Listen (z.b. Apollo, Tenor (Begriffsklärung), DPA, GBI)
        //remove all < > </ > tags
        //String extract = article;

        String[] extracts = article.split("\\r\\n|\\n|\\r");
        ArrayList<String> resExtract = new ArrayList<String>();

        for (String i : extracts ) {
            if (i.length() > 0) {
                if (!i.substring(0, 1).matches("[^a-zA-Z0-9'\\*]")) { // word beginning
                    resExtract.add(i);
                }
            }
        }
        String extract = "";
        for (String j : resExtract) {
                extract += " " + j;
        }


        /*if (extract.contains("ref")) {
            System.out.println("ref found");
            String res = "";
            String[] norefs = extract.split("&lt;ref[^&lt;]*&lt;");
            for (String k : norefs) {
                if (k.contains("&lt;/ref&gt;")) {
                    res += " " + k.substring(k.indexOf("&lt;/ref&gt;")+1);
                } else {
                    res += " " + k;
                }
            }
            extract = res;
        }*/

        extract = extract.replaceAll("\n", ""); // remove html comment
        extract = extract.replaceAll("!--(.*)--", ""); // remove html comments
        extract = extract.replaceAll("!--(.*)--", ""); // remove html comments
        //extract = extract.replaceAll("(&lt;ref&gt;)([^&]*)(&lt;\\/ref&gt;)", ""); // remove ref tags
        extract = extract.replaceAll("math(.*)/math", ""); // remove mathematical tags

        //remove all [[ ]] tags
        //remove pictures
        extract = extract.replaceAll("(\\[\\[Datei:)([^\\[\\[]*)(\\]\\])", "");
        extract = extract.replaceAll("(\\[\\[File:)([^\\[\\[]*)(\\]\\])", "");
        extract = extract.replaceAll("(\\[\\[Kategorie:)([^\\[\\[]*)(\\]\\])", "");
        extract = extract.replaceAll("(\\[\\[bat-smg:)([^\\[\\[]*)(\\]\\])", "");
        extract = extract.replaceAll("(\\[\\[eo:)([^\\[\\[]*)(\\]\\])", "");

        extract = Jsoup.parse(extract).text();

        //remove articles keep text
        Pattern r = Pattern.compile("(\\[\\[)([^\\[\\]]*)(\\]\\])");

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
        extract = extract.replaceAll("(\\[\\[)(\\w)*:([^\\]]*)(\\]\\])", "")
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

        return extract;
    }

    private static String cleanString(String str, String lang) {
        if (lang.equals("de")) {
            // use replacementsDE
        }
        for (int i = 0; i < replacementsDE.length; i++) {
            // Todo: Pattern compile
            str = str.replaceAll(replacementsDE[i][0], replacementsDE[i][1]);
        }
        return str;
    }

    private static String removeTags(String str) {
        str = str.replaceAll("^\\W*", ""); // replace all non-word chars at beginning of string
        str = str.replaceAll("(<)([^\\/]*)(\\/>)", ""); // replace all <ref />-tags
        return str;
    }

    private static String shortenDefinition(String str) {
        String finalstr = "";
        String[] segs = str.split( Pattern.quote( "." ) );
        String consonants = "[B,C,D,F,G,H,J,K,L,M,N,P,Q,R,S,ß,T,V,W,X,Z,b,c,d,f,g,h,j,k,l,m,n,p,q,r,s,t,v,w,x,z]";

        for (int i = 0; i < segs.length; i++) {

            // TODO: Catch IndexOutOfBounds Exception
            if (finalstr.length() >= 200) { // pruefe ob satzende
                if (i < segs.length && i > 0 && segs[i-1].length() > 5 && segs[i].length() > 2 && segs[i-1].contains(" ")) { // Segmente gross genug zum Untersuchen
                    // pruefe ob aktueller chunk neuer satz ist
                    if (segs[i].substring(0, 1).equals(" ") // jeder neue satz beginnt mit leerzeichen
                            && segs[i].substring(1, 2).matches("[A-Z]") // jeder neue Satz beginnt mit großem Buchstaben
                            && !segs[i-1].substring(segs[i-1].lastIndexOf(" ")).matches("\\d*") // direkt vor Punkt steht keine Zahl
                            && !segs[i-1].substring(segs[i-1].length()-2).matches("^ \\w") // direkt vor Punkt steht nicht nur ein Zeichen
                            && segs[i].length() > 2 // neues Segment ist laenger als 2 Zeichen
                            && !segs[i-1].substring(segs[i-1].lastIndexOf(" ")).matches(consonants+"*") // letztes Wort besteht nicht ausschliesslich aus Konsonanten
                            && !segs[i-1].substring(segs[i-1].lastIndexOf(" ")).matches("(^[a-z])(.*)([h|l|z]$)") // letztes Wort ist nicht kleingeschrieben und endet mit h oder l oder z
                            ) {
                        break; // neuer satz soll nicht reingenommen werden
                    } else { // noch kein satzende erreicht - weiter
                        finalstr += segs[i] + ".";
                    }
                } else { // Segmente zu klein -> reinnehmen
                    finalstr += segs[i] + ".";
                }
            } else {
                finalstr += segs[i] + "."; // noch keine 300 Zeichen erreicht
            }
        }
        return finalstr;
    }

    private static String removeWhiteSpaces(String str) {
        str = str.replaceAll("\\.(\\s|\\*)*\\.", ".");
        return str.trim();
    }

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
                } else {
                    articles.add(m.group(2));
                    text = text.replace(m.group(), m.group(2));
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

}
