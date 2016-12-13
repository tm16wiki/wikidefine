package CLI;

import asg.cliche.Command;
import asg.cliche.Param;
import asg.cliche.Shell;
import asg.cliche.ShellDependent;
import helperClasses.https;
import helperClasses.xml;
import wikiAPI.wikiTextParser;

public class webDefCLI implements ShellDependent {
    private static config config;
    private Shell theShell;

    private wikiTextParser text = new wikiTextParser();
    private helperClasses.xml xml = new xml();
    private helperClasses.https https = new https();


    webDefCLI(config config) {
        this.config = config;
        System.out.println("====    CONFIG    ====");
        System.out.println("language is set to: " + config.getLang());

        System.out.println();
    }


    public void cliSetShell(Shell theShell) {
        this.theShell = theShell;
    }


    @Command(name = "define",
            abbrev = "d",
            description = "creates definitions out of web")
    public void webdef(@Param(name = "term", description = "define this term") String term) {
        term = term.replaceAll(" ", "_");

        String url;
        switch (config.getLang()) {
            case "de":
                url = "https://de.wikipedia.org/wiki/Spezial:Exportieren/";
                break;
            case "en":
                url = "https://en.wikipedia.org/wiki/Special:Export/";
                break;
            default:
                url = "https://de.wikipedia.org/wiki/Spezial:Exportieren/";
                break;
        }
        if (!https.loadURL(url + term)) return;
        String content = xml.getTagValue(https.getContent(), "text");

        //Test Definition
        System.out.println(text.getDefinition(content));
    }

    @Command(name = "info",
            abbrev = "i",
            description = "prints webdump info of the article")
    public void webinfo(@Param(name = "article", description = "get info about this article") String term) {
        term = term.replaceAll(" ", "_");
        String url;
        switch (config.getLang()) {
            case "de":
                url = "https://de.wikipedia.org/wiki/Spezial:Exportieren/";
                break;
            case "en":
                url = "https://en.wikipedia.org/wiki/Special:Export/";
                break;
            default:
                url = "https://de.wikipedia.org/wiki/Spezial:Exportieren/";
                break;
        }
        if (!https.loadURL(url + term)) return;
        String content = xml.getTagValue(https.getContent(), "text");

        //https Info
        System.out.println("articleurl:\t\t\t" + https.getURL());
        System.out.println("timestamp:\t\t\t" + https.getTimestamp().toString());
        //System.out.println("certinfo:\t\t\t" + https.getCertInfo());
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
        //System.out.println("cleaned content:\t" + text.extractText(content));
    }

    @Command(name = "setlanguage",
            abbrev = "sl",
            description = "sets language")
    public void setLang(@Param(name = "language", description = "new lang value") String lang) {
        this.config.setLang( lang);
    }

}