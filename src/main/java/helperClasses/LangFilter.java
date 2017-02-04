package helperClasses;

import org.apache.commons.lang3.StringUtils;

/**
 * Created by Rene on 21.01.2017.
 */
public class LangFilter {

    public static boolean check(String lang, String str) {
        switch (lang) {
            case "EN":
                return checkEN(str);
            case "DE":
                return checkDE(str);
        }
        return false;
    }

    public static boolean checkEN(String str) {
        if (StringUtils.containsAny(str, "#REDIRECT", "#Redirect", "#redirect")) {
            return true;
        } else return false;
    }

    //TODO: implement other lang
    public static boolean checkRU(String str) {

        return StringUtils.containsAny(str, "#..");
    }

    public static boolean checkDE(String str) {
        if (StringUtils.containsAny(str,
                "#WEITERLEITUNG",
                "#Weiterleitung",
                "#weiterleitung",
                "<title>List",
                "<title>Wikipedia:",
                "<title>Vorlage:",
                "<title>Kategorie:",
                "<title>Datei:"
        )) {
            System.out.println("redirect");
            return true;
        }

        return false;
    }
}
