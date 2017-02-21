package helperClasses;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;


/**
 * helperclass for managing xml
 */
public class xml {


    /**
     * Creates sha1 checksum of a given string
     *
     * @param input input
     * @return hashsum as string
     */
    private static String sha1(String input) throws NoSuchAlgorithmException {
        MessageDigest mDigest = MessageDigest.getInstance("SHA1");
        byte[] result = mDigest.digest(input.getBytes());
        StringBuilder sb = new StringBuilder();
        for (byte aResult : result) {
            sb.append(Integer.toString((aResult & 0xff) + 0x100, 16).substring(1));
        }
        return sb.toString();
    }

    /**
     * Extracts tag out of an xml
     *
     * @param xml     the xml as string
     * @param tagName the name of the tag to extract
     * @return tag content as string
     */
    public String getTagValue(String xml, String tagName) {
        try {
            String s = xml.split("<" + tagName)[1].split("</" + tagName + ">")[0];
            s = s.split(">")[1];
            return s;
        } catch (ArrayIndexOutOfBoundsException e) {
            return null;
        }
    }


    /**
     * Getter method for the user of an wikipedia article
     *
     * @return username as string
     */
    public String getUser(String xml) {
        return getTagValue(xml, "username");
    }

    /**
     * Getter method for the id of an wikipedia article
     *
     * @return id as string
     */
    public String getId(String xml) {
        return getTagValue(xml, "id");
    }

    /**
     * Getter method for the checksum of an wikipedia article
     *
     * @return checksum as string
     */
    public String getChecksum(String xml) {
        try {
            //todo sha1 from _...?
            //System.out.println(sha1(xml));
            sha1(xml);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return getTagValue(xml, "sha1");
    }

}
