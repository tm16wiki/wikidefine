package helperClasses;

import org.apache.commons.lang3.StringUtils;

/**
 * Language filter implements translated Wikipedia tags for given language
 */
public class LangFilter {

    /**
     * Checks if input string contains Wikipedia tags in given language
     * @param lang Language to check
     * @param str Input wikipedia string
     * @return true if input string contains tags in given language
     */
    public static boolean check(String lang, String str) {
        switch (lang) {
            case "EN":
                return checkEN(str);
            case "DE":
                return checkDE(str);
        }
        return false;
    }

    /**
     * Checks if input string contains Wikipedia tags in english language
     * @param str Input wikipedia string
     * @return true if input string contains tags in english language
     */
    public static boolean checkEN(String str) {
        if (StringUtils.containsAny(str, "#REDIRECT", "#Redirect", "#redirect")) {
            return true;
        } else return false;
    }

    //TODO: implement other lang
    /**
     * Checks if input string contains Wikipedia tags in russian language
     * @param str Input wikipedia string
     * @return true if input string contains tags in russian language
     */
    public static boolean checkRU(String str) {
        return StringUtils.containsAny(str, "#..");
    }

    /**
     * Checks if input string contains Wikipedia tags in german language
     * @param str Input wikipedia string
     * @return true if input string contains tags in german language
     */
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
            return true;
        }
        return false;
    }
}
