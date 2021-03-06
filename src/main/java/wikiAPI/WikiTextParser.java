package wikiAPI;

import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * WikiTextParser parses and converts the text to a well formed definition
 */
public class WikiTextParser {

    /**
     * Takes a long definition string and converts to a string with a given max-sentences number
     *
     * @param str Input definition string
     * @return shortened definition string
     */
    private static String shortenDefinition(String str) {
        str = str.replaceAll("\\s+", " "); // remove multiple whitespaces
        String finalstr = "";
        String newstr = "";
        int sentences = 0;
        int brackets = 0;
        int maxsentences = 2; // number of output sentences
        String[] segs = str.split(Pattern.quote("."));
        String consonants = "[B,C,D,F,G,H,J,K,L,M,N,P,Q,R,S,ß,T,V,W,X,Z,b,c,d,f,g,h,j,k,l,m,n,p,q,r,s,t,v,w,x,z]";

        for (int i = 0; i < segs.length; i++) {
            brackets += StringUtils.countMatches(segs[i], "(");
            brackets -= StringUtils.countMatches(segs[i], ")");
            if (i >= 1 && segs[i - 1].length() > 2 && segs[i].length() > 3 && segs[i - 1].contains(" ") && brackets == 0) { // Segmente gross genug zum Untersuchen und nicht in Klammern
                if (segs[i].trim().substring(0, 1).equals("=") || segs[i].substring(1, 2).matches("<")) { // new headline - cut
                    break;
                }
                // pruefe ob aktueller chunk neuer satz ist, Satzende: i-1
                if (segs[i].substring(0, 1).equals(" ") // jeder neue satz beginnt mit leerzeichen
                        && segs[i].substring(1, 2).matches("[A-Z]") // jeder neue Satz beginnt mit großem Buchstaben
                        && !segs[i - 1].substring(0, segs[i - 1].lastIndexOf(" ")).equals("")
                        && !segs[i - 1].substring(segs[i - 1].lastIndexOf(" ")).matches(" \\d+") // wenn vor Punkt Zahl kein Satzende
                        && !segs[i - 1].substring(segs[i - 1].length() - 2).matches("^ \\w") // direkt vor Punkt steht nicht nur ein Zeichen
                        && segs[i].length() > 2 // neues Segment ist laenger als 2 Zeichen
                        && !segs[i - 1].substring(segs[i - 1].lastIndexOf(" ")).matches(" " + consonants + "+") // letztes Wort besteht nicht ausschliesslich aus Konsonanten
                        && !segs[i - 1].substring(segs[i - 1].lastIndexOf(" ")).matches(" (^[a-z])(.*)([h|l|z]$)") // letztes Wort ist nicht kleingeschrieben und endet mit h oder l oder z
                        ) {
                    sentences++; // neuen Satzanfang gefunden
                    if (sentences < maxsentences) { // noch nicht max sentences erreicht - fuege anfang naechsten satz hinzu
                        newstr += segs[i] + ".";
                        finalstr += newstr;
                        newstr = "";
                    } else {
                        break; // Gewuenschte Anzahl Saetze gefunden
                    }
                } else { // noch kein Satzende erreicht - weiter
                    newstr += segs[i] + ".";
                }
            } else {
                newstr += segs[i] + "."; // skippe aktuelles segment
            }
        }
        finalstr += newstr; // max sentences erreicht - fuege letzten satz zu final string hinzu
        if (finalstr.length() > 2 && finalstr.substring(finalstr.length() - 2, finalstr.length()).equals(" .")) {
            finalstr = finalstr.substring(0, finalstr.length() - 2);
        }
        return finalstr.trim();
    }

    /**
     * Unescapes html and wikipedia escapes in article
     *
     * @param text article text to unescape
     * @return cleaned text as string
     */
    private String unescapeArticle(String text) {
        String[] replacements;
        String[] escapes;
        escapes = new String[]{
                //html escape sequences
                "&quot;", "&apos;", "&amp;", "&lt;", "&gt;", "&nbsp;", "&shy;"
        };
        replacements = new String[]{
                //html escape sequence replacements
                "\"", "'", "&", "<", ">", " ", " "
        };

        text = StringUtils.replace(text, "'''", "");
        text = StringUtils.replace(text, "''", "");

        try {
            return StringUtils.replaceEachRepeatedly(text, escapes, replacements);
        } catch (IllegalStateException e) {
            return text;
        }
    }

    /**
     * Called by FileDumpParser and webdefinition
     *
     * @param article Content of text-tag in XML
     * @return clean compact definition string
     */
    public String getDefinition(String article) {
        //long starttime = System.currentTimeMillis();

        article = unescapeArticle(article);
        article = extractText(article);
        article = shortenDefinition(article);
        article = StringUtils.replace(article, "\n", " ");

        //System.out.print(System.currentTimeMillis() - starttime);
        return article;
    }

    /**
     * Extract definition part by removing additional info
     *
     * @param article Content of text-tag in XML
     * @return whole dirty definition string
     */
    private String extractText(String article) {
        String extract;
        Matcher m;

        //remove all < > notated tags
        extract = StringUtils.replaceAll(article, "(?:<!--)(?:[^<]*)(?:-->)", "");
        extract = StringUtils.replaceAll(extract, "(!-->)", "");
        extract = StringUtils.replaceAll(extract, "(?:<)(?:\\w*)(?:[^>]*)(?:>)(?:[^<]*)(?:<\\/)(?:\\w*)(?:>)", "");
        extract = StringUtils.replaceAll(extract, "(?:<ref )(?:[^<]*)(?:\\/>)", "");
        extract = StringUtils.replaceAll(extract, "(?:<div )(?:[^<]*)(?:>)(?:[^<]*)(?:<\\/div>)", "");
        extract = StringUtils.replace(extract, "<br />", "");
        extract = StringUtils.replace(extract, "<nowiki />", "");
        extract = StringUtils.replace(extract, "<references />", "");
        extract = StringUtils.replace(extract, "<onlyinclude>", "");

        try {
            //remove all [[ ]] tags
            //remove articles keep text
            Pattern r = Pattern.compile("(?:\\[\\[)([^:\\[\\]]*)(?:\\]\\])");
            m = r.matcher(extract);
            while (m.find()) {
                String atext = m.group(1);
                String text;
                //removes text link from article tag
                if (atext.contains("|")) {
                    text = atext.substring(atext.indexOf("|") + 1, atext.length());
                    extract = StringUtils.replace(extract, m.group(), text);
                } else {
                    extract = StringUtils.replace(extract, m.group(), m.group(1));
                }
            }
            //remove files, picture, categories and other
            for (int i = 0; i < 2; i++) {
                r = Pattern.compile("(?:\\[\\[)(?:\\w*):(?:[^\\[\\]]*)(?:\\]\\])");
                m = r.matcher(extract);
                while (m.find()) {
                    extract = StringUtils.replace(extract, m.group(), "");
                }
            }

            //remove all {{ }} tags
            //remove wikipediaobjects
            //4 level deep e.g. infoboxes
            //TODO: z.b. Talk (Mineral)
            for (int i = 0; i < 3; i++) {
                r = Pattern.compile("(?:\\{\\{)(?:[^\\{\\}]*)(?:\\}\\})");
                m = r.matcher(extract);
                while (m.find()) {
                    if (m.group().contains("lang") || m.group().contains("IPA")) { // keep info in lang- and IPA-tags
                        extract = StringUtils.replace(extract, m.group(), m.group().substring(m.group().lastIndexOf("|") + 1, m.group().length() - 2));
                    } else if (m.group().contains("enS")) { // "englisch", bsp: Anime
                        extract = StringUtils.replace(extract, m.group(), "engl. " + m.group().substring(m.group().lastIndexOf("|") + 1, m.group().length() - 2));
                    } else {
                        extract = StringUtils.replace(extract, m.group(), "");
                    }
                }
            }
        } catch (NullPointerException e) {
            return "..";
        }

        //remove all {| |} tags e.g. wikiclasses
        extract = StringUtils.replaceAll(extract, "(?:\\{\\|)(?:[^{]*)(?:\\|\\})", "");

        //exclude links
        extract = StringUtils.replaceAll(extract, "(?:\\[htttp)(?:.*)(?:\\])", "");

        //remove ==  == tags e.g. headlines
        extract = StringUtils.replaceAll(extract, "(?:== )(?:.*)(?: ==)", "");

        extract = StringUtils.replace(extract, "(; ", "(");
        extract = StringUtils.replace(extract, "(, ", "(");
        extract = StringUtils.replace(extract, "[]", "");
        extract = StringUtils.replace(extract, "()", "");
        extract = StringUtils.replace(extract, "�", "");
        extract = StringUtils.replace(extract, " : ", "");
        extract = StringUtils.replace(extract, " ; ", "");
        extract = StringUtils.replace(extract, "\" \"", "");
        extract = StringUtils.replace(extract, "__NOTOC__", "");

        return extract;
    }

    /**
     * Returns title in right case, extracted from definition text
     * @param title Title from title-tag
     * @param article Definition text from text-tag
     * @return title in right case
     */
    public String getRightTitle(String title, String article) {
        Pattern p = Pattern.compile("'''(.*?)'''");
        if (article == null || article == "") {
            return title;
        } else {
            Matcher m = p.matcher(article);
            while (m.find()) {
                if (m.group().equalsIgnoreCase("'''" + title + "'''")) { // match - uebernehme neuen title
                    title = StringUtils.replace(m.group(), "'''", "");
                }
            }
            return title;
        }
    }

    /**
     * Extracts filenames from wikipedia tags
     *
     * @param text Input wikipedia string with wikipedia file tags
     * @return all file URIs found
     */
    public ArrayList<String> findFiles(String text) {
        ArrayList<String> files = new ArrayList<>();
        String pattern = "(?:\\[\\[)Datei:(.*)(?:\\]\\])";
        Pattern r = Pattern.compile(pattern);
        Matcher m = r.matcher(text);
        while (m.find()) {
            String file = m.group();
            //todo: filetags
            //removes picturetags and text from file tag
            if (file.contains("|")) {
                files.add(file.substring(0, file.indexOf("|")));
            }
            files.add(m.group(1));
        }
        return files;
    }

    /**
     * Returns links to articles found in wikipedia definition string
     *
     * @param text Input wikipedia definition string
     * @return all article URIs found
     */
    public ArrayList<String> findArticles(String text) {
        ArrayList<String> articles = new ArrayList<>();
        //Regex für artikel [[ ... ]] ('[' und ']’ ausgeschlossen falls verschachtelt)
        String pattern = "(?:\\[\\[)([^:\\[\\]]*)(?:\\]\\])";
        Pattern r = Pattern.compile(pattern);
        Matcher m = r.matcher(text);
        for (int i = 0; i < 2; i++) {
            while (m.find()) {
                String article = m.group(1);
                String articletext;
                //removes text link from article tag
                if (article.contains("|")) {
                    articletext = article.substring(article.indexOf("|") + 1, article.length());
                    article = article.substring(0, article.lastIndexOf("|"));
                    articles.add(article);
                    text = text.replace(m.group(), articletext);
                } else {
                    articles.add(m.group(1));
                    text = text.replace(m.group(), m.group(1));
                }
            }
        }
        return articles;
    }

    /**
     * Returns links to categories found in wikipedia definitions
     *
     * @param text Input wikipedia definition string
     * @return all category URIs found
     */
    public ArrayList<String> findCategories(String text) {
        ArrayList<String> categories = new ArrayList<>();
        String pattern = "(?:\\[\\[)(?:Kategorie:)([^\\[\\]]*)(?:\\]\\])";
        Pattern r = Pattern.compile(pattern);
        Matcher m = r.matcher(text);
        while (m.find()) {
            categories.add(m.group(1).replace("|", "")
            );
        }
        return categories;
    }

    /**
     * Returns links to weblinks found in wikipedia definitions
     *
     * @param text Input wikipedia definition string
     * @return all weblinks URIs found
     */
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

    /**
     * Returns links to wikiobjects found in wikipedia definitions
     *
     * @param text Input wikipedia definition string
     * @return all wikiobject URIs found
     */
    public ArrayList<String> findWikiObject(String text) {
        ArrayList<String> objects = new ArrayList<>();
        //todo infoboxen und {| class... |} zu csv?
        //4 stufig um verschachtelung von objekten aufzuloesen
        for (int i = 0; i < 4; i++) {
            String pattern = "(?:\\{\\{)([^{}]*)(?:\\}\\})";
            Pattern r = Pattern.compile(pattern);
            Matcher m = r.matcher(text);
            while (m.find()) {
                objects.add(m.group());
                text = text.replace(m.group(), m.group(1));
            }
        }
        return objects;
    }

    /**
     * Checks if the definition string contains a polysemous
     *
     * @param text Input wikipedia definition string
     * @return true if polysemous was found
     */
    public boolean isPolysemous(String text) {
        return text.contains("{{Begriffsklärungshinweis}}");
    }

}
