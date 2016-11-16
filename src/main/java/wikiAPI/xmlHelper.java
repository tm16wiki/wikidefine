package wikiAPI;


import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

class xmlHelper {

    private static String sha1(String input) throws NoSuchAlgorithmException {
        MessageDigest mDigest = MessageDigest.getInstance("SHA1");
        byte[] result = mDigest.digest(input.getBytes());
        StringBuilder sb = new StringBuilder();
        for (byte aResult : result) {
            sb.append(Integer.toString((aResult & 0xff) + 0x100, 16).substring(1));
        }
        return sb.toString();
    }

    String getTagValue(String xml, String tagName){
        try {
            String s = xml.split("<"+tagName)[1].split("</" + tagName + ">")[0];
            s = s.split(">")[1];
            return s;
        }catch (ArrayIndexOutOfBoundsException e){
            return null;
        }
    }

    private String getTagValue1(String xml, String tagName){
        Document doc = Jsoup.parse(xml);
        //System.out.println(doc.select( tagName).first().text() + " || " + doc.select( tagName).first().ownText());
        return doc.select(tagName).first().html();
    }

    String getUser(String xml){
        return getTagValue1(xml, "username");
    }

    String getId(String xml){
        return getTagValue1(xml,"id");
    }

    String getChecksum(String xml){
        try {
            //TODO sha1 from _...?
            //System.out.println(sha1(xml));
            sha1(xml);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return getTagValue1(xml, "sha1");
    }

}
