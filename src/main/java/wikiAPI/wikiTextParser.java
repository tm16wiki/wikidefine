package wikiAPI;


import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.Map;
import java.util.HashMap;

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


    private String extractText(String article) {
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

        String[][] replacements = {
                //wikiclasses
                //todo: z.B. Region Stuttgart, 1256
                //bessere regex finden
                {"\n\\|(?:.*)", ""},
                {"\\{\\| class=(?:.*)", ""},

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
                {"<(.*)>", ""},
                {"^\\[", ""},
                {"\\s*\\([^)]*\\)", ""},
                {"\"\" ", ""},
                {"\\[\\]", ""},
                {"\\(\\)", ""},
                {"&lt;(.*)&gt;", ""},
                {"__NOTOC__", ""},
                {"\n", ""},
        };

        return cleanString(extract, replacements).trim();
    }

    private static String cleanString(String str, String[][] replacements) {
        for (int i = 0; i < replacements.length; i++) {
            // Todo: Pattern compile
            str = str.replaceAll(replacements[i][0], replacements[i][1]);
        }
        return str;
    }


    public String getDefinition(String article) {
        article = extractText(article);



        //TODO: links werden abgeschnitten [http://www. in Rotaria (Titularbistum)
        // Satzerkennung: Abschnitt generell erst nach 300 Zeichen,
        // dann nach Punkt aber nicht wenn unmittelar vor Punkt nur
        // ein Zeichen oder eine beliebige Zahl steht (z.B. "Er ist der 1. Mensch")
        String finalstr = "";
        String[] segs = article.split( Pattern.quote( "." ) );
        String consonants = "[B,C,D,F,G,H,J,K,L,M,N,P,Q,R,S,ß,T,V,W,X,Z,b,c,d,f,g,h,j,k,l,m,n,p,q,r,s,t,v,w,x,z]";

        for (int i = 0; i < segs.length; i++) {

            // TODO: Catch IndexOutOfBounds Exception
            if (finalstr.length() >= 300) { // pruefe ob satzende
                if (i < segs.length && i > 0 && segs[i-1].length() > 5 && segs[i].length() > 2 && segs[i-1].contains(" ")) {
                    // pruefe ob aktueller chunk neuer satz ist
                    if (segs[i].substring(0, 1).equals(" ") // jeder neue satz beginnt mit leerzeichen
                            && segs[i].substring(1, 2).matches("[A-Z]") // jeder neue Satz beginnt mit großem Buchstaben
                            && !segs[i-1].substring(segs[i-1].lastIndexOf(" ")).matches("\\d*") // direkt vor Punkt steht keine Zahl
                            && !segs[i-1].substring(segs[i-1].length()-2).matches("^ \\w") // direkt vor Punkt steht nicht nur ein Zeichen
                            && !segs[i-1].substring(segs[i-1].lastIndexOf(" ")).matches(consonants+"*") // letztes Wort besteht nicht ausschliesslich aus Konsonanten
                            && !segs[i-1].substring(segs[i-1].lastIndexOf(" ")).matches("(^[a-z])(.*)([h|l|z]$)") // letztes Wort ist nicht kleingeschrieben und endet mit h oder l oder z
                            ) {
                        break; // neuer satz soll nicht reingenommen werden
                    } else { // noch kein satzende erreicht - weiter
                        finalstr += segs[i] + ".";
                    }
                }
            } else {
                finalstr += segs[i] + "."; // noch keine 300 Zeichen erreicht
            }
        }

        return finalstr;
    }
}
