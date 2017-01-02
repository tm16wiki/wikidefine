package wikiAPI;

import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class newTextParser {


    private static String shortenDefinition(String str) {
        str = str.replaceAll("\\s+", " "); // remove multiple whitespaces
        String finalstr = "";
        int sentences = 0;
        int brackets = 0;
        String[] segs = str.split(Pattern.quote("."));
        String consonants = "[B,C,D,F,G,H,J,K,L,M,N,P,Q,R,S,ß,T,V,W,X,Z,b,c,d,f,g,h,j,k,l,m,n,p,q,r,s,t,v,w,x,z]";

        while (sentences < 2) {
            for (int i = 0; i < segs.length; i++) {
                if (segs[i].contains("(")) {
                    brackets++;
                }
                if (segs[i].contains(")")) {
                    brackets--;
                }
                // TODO: Catch IndexOutOfBounds Exception
                if (i < segs.length && i > 0 && segs[i - 1].length() > 5 && segs[i].length() > 2 && segs[i - 1].contains(" ") && brackets == 0) { // Segmente gross genug zum Untersuchen und nicht in Klammern
                    // pruefe ob aktueller chunk neuer satz ist
                    if (segs[i].substring(0, 1).equals(" ") // jeder neue satz beginnt mit leerzeichen
                            //&& segs[i].substring(1, 2).matches("[A-Z]") // jeder neue Satz beginnt mit großem Buchstaben
                            && !segs[i - 1].substring(segs[i - 1].lastIndexOf(" ")).matches(" \\d+") // direkt vor Punkt steht keine Zahl
                            && !segs[i - 1].substring(segs[i - 1].length() - 2).matches("^ \\w") // direkt vor Punkt steht nicht nur ein Zeichen
                            && segs[i].length() > 2 // neues Segment ist laenger als 2 Zeichen
                            && !segs[i - 1].substring(segs[i - 1].lastIndexOf(" ")).matches(" "+consonants+"+") // letztes Wort besteht nicht ausschliesslich aus Konsonanten
                            && !segs[i - 1].substring(segs[i - 1].lastIndexOf(" ")).matches(" (^[a-z])(.*)([h|l|z]$)") // letztes Wort ist nicht kleingeschrieben und endet mit h oder l oder z
                            ) {
                        sentences++; // neues Satzende gefunden
                        if (segs[i].substring(1, 2).matches("[=]") || segs[i].substring(1, 2).matches("<")) { // new headline - cut
                            break;
                        }
                        if (sentences < 2) {
                            finalstr += segs[i] + ".";
                        } else {
                            break; // Gewuenschte Anzahl Saetze gefunden
                        }
                    } else { // noch kein Satzende erreicht - weiter
                        finalstr += segs[i] + ".";
                    }
                } else {
                    finalstr += segs[i] + "."; // skippe aktuelles segment
                }
            }
            break;
        }
        if (finalstr.length() > 3 && finalstr.substring(finalstr.length() - 3).matches(". .")) {
            finalstr = finalstr.substring(0, finalstr.length() - 2);
        }
        return finalstr;
    }

    /**
     * unescapes html and wikipedia escapes in article
     *
     * @param text article text to unescape
     * @return cleaned text as string
     */
    private String unescapeArticle(String text) {
        String[] replacements;
        String[] escapes;
        escapes = new String[]{
                //html escape sequences
                "&quot;", "&apos;", "&amp;", "&lt;", "&gt;", "&nbsp;", /*"&iexcl;", "&cent;", "&pound;", "&curren;",
                "&yen;", "&brvbar;", "&sect;", "&uml;", "&copy;", "&ordf;", "&laquo;", "&not;", "&shy;", "&reg;",
                "&macr;", "&deg;", "&plusmn;", "&sup2;", "&sup3;", "&acute;", "&micro;", "&para;", "&middot;", "&cedil;",
                "&sup1;", "&ordm;", "&raquo;", "&frac14;", "&frac12;", "&frac34;", "&iquest;", "&times;", "&divide;",
                "&Agrave;", "&Aacute;", "&Acirc;", "&Atilde;", "&Auml;", "&Aring;", "&AElig;", "&Ccedil;", "&Egrave;",
                "&Eacute;", "&Ecirc;", "&Euml;", "&Igrave;", "&Iacute;", "&Icirc;", "&Iuml;", "&ETH;", "&Ntilde;",
                "&Ograve;", "&Oacute;", "&Ocirc;", "&Otilde;", "&Ouml;", "&Oslash;", "&Ugrave;", "&Uacute;", "&Ucirc;",
                "&Uuml;", "&Yacute;", "&THORN;", "&szlig;", "&agrave;", "&aacute;", "&acirc;", "&atilde;", "&auml;",
                "&aring;", "&aelig;", "&ccedil;", "&egrave;", "&eacute;", "&ecirc;", "&euml;", "&igrave;", "&iacute;",
                "&icirc;", "&iuml;", "&eth;", "&ntilde;", "&ograve;", "&oacute;", "&ocirc;", "&otilde;", "&ouml;",
                "&oslash;", "&ugrave;", "&uacute;", "&ucirc;", "&uuml;", "&yacute;", "&thorn;", "&yuml;"
                */
        };
        replacements = new String[]{
                //html escape sequence replacements
                "\"", "'", "&", "<", ">", " ", /*"¡", "¢", "£", "¤", "¥", "¦", "§", "¨", "©", "ª", "«", "¬", " ", "®",
                "¯", "°", "±", "²", "³", "´", "µ", "¶", "·", "¸", "¹", "º", "»", "¼", "½", "¾", "¿", "×", "÷", "À", "Á",
                "Â", "Ã", "Ä", "Å", "Æ", "Ç", "È", "É", "Ê", "Ë", "Ì", "Í", "Î", "Ï", "Ð", "Ñ", "Ò", "Ó", "Ô", "Õ", "Ö",
                "Ø", "Ù", "Ú", "Û", "Ü", "Ý", "Þ", "ß", "à", "á", "â", "ã", "ä", "å", "æ", "ç", "è", "é", "ê", "ë", "ì",
                "í", "î", "ï", "ð", "ñ", "ò", "ó", "ô", "õ", "ö", "ø", "ù", "ú", "û", "ü", "ý", "þ", "ÿ"
                */
        };

        text = StringUtils.replace(text, "'''", "\"");
        text = StringUtils.replace(text, "''", "\"");

        //wegen [http://www.wip...ountry_codes&amp;amp;amp;amp;amp;amp;amp;amp;amp;amp;amp;amp;rdquo; in "African Regional Intellectual Property Organization"
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
        extract = StringUtils.replaceAll(extract, "(?:<)(?:\\w*)(?:[^>]*)(?:>)(?:[^<]*)(?:<\\/)(?:\\w*)(?:>)", "");
        extract = StringUtils.replaceAll(extract, "(?:<ref )(?:[^<]*)(?: \\/>)", "");
        extract = StringUtils.replace(extract, "<br />", "");
        extract = StringUtils.replace(extract, "<nowiki />", "");
        extract = StringUtils.replace(extract, "<references />", "");

        try {
            //remove all [[ ]] tags
            //remove articles keep text
            Pattern r = Pattern.compile("(?:\\[\\[)([^:\\[\\]]*)(?:\\]\\])");
            m = r.matcher(extract);
            while (m.find()) {
                //TODO FEHLER
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
            extract = StringUtils.replaceAll(extract, "(?:\\[\\[)(?:\\w*):(?:.*)(?:\\]\\])", "");

            //remove all {{ }} tags
            //remove wikipediaobjects
            //4 level deep e.g. infoboxes
            //todo: z.b. Talk (Mineral)
            for (int i = 0; i < 4; i++) {
                r = Pattern.compile("(?:\\{\\{)(?:[^\\{\\}]*)(?:\\}\\})");
                m = r.matcher(extract);
                while (m.find()) {
                    extract = StringUtils.replace(extract, m.group(), "");
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
        extract = StringUtils.replace(extract, "__NOTOC__", "");

        return extract;
    }

    public ArrayList<String> findFiles(String text) {
        ArrayList<String> files = new ArrayList<>();
        String pattern = "(?:\\[\\[)Datei:(.*)(?:\\]\\])";
        Pattern r = Pattern.compile(pattern);
        Matcher m = r.matcher(text);
        while (m.find()) {
            String file = m.group();
            //todo: filetags finden?
            //removes picturetags and text from file tag
            if (file.contains("|")) {
                files.add(file.substring(0, file.indexOf("|")));
            }
            files.add(m.group(1));
        }
        return files;
    }


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


    public boolean isPolysemous(String text) {
        return text.contains("{{Begriffsklärungshinweis}}");
    }

}
