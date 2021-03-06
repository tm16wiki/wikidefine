package helperClasses;

import javax.net.ssl.HttpsURLConnection;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.cert.Certificate;
import java.util.Date;


/**
 * helperclass to manage https connections
 */
public class https {

    private String content;
    private String certInfo;
    private Certificate[] certificates;
    private String URL;
    private Date timestamp;

    /**
     * Creates connection to given URL
     *
     * @param https_url URL to connect
     * @return true if successful or false if not
     */
    public boolean loadURL(String https_url) {
        this.URL = https_url;
        this.timestamp = new Date();
        URL url;
        try {
            url = new URL(https_url);
        } catch (MalformedURLException e) {
            System.out.println("malformed url");
            return false;
        }
        try {
            HttpsURLConnection con = (HttpsURLConnection) url.openConnection();
            loadCertInfo(con);
            loadContent(con);
        } catch (IOException e) {
            System.out.println("can't load " + https_url);
            return false;
        }
        return true;
    }


    /**
     * Loads certificates from given connection
     *
     * @param con connection to load from
     */
    private void loadCertInfo(HttpsURLConnection con) throws IOException {
        if (con != null) {
            certInfo += "Response Code : " + con.getResponseCode();
            certInfo += "Cipher Suite : " + con.getCipherSuite();
            certInfo += "\n";
            certificates = con.getServerCertificates();
            int i = 1;
            for (Certificate cert : certificates) {
                certInfo += "Certificate " + i++;
                certInfo += "Cert Type : " + cert.getType();
                certInfo += "Cert Hash Code : " + cert.hashCode();
                certInfo += "Cert Public Key Algorithm : " + cert.getPublicKey().getAlgorithm();
                certInfo += "Cert Public Key Format : " + cert.getPublicKey().getFormat();
            }
        }
    }

    /**
     * Loads content from given https connection
     *
     * @param con httpsURLConnection
     */
    private void loadContent(HttpsURLConnection con) throws IOException {
        String lines = "";
        if (con != null) {
            BufferedReader br =
                    new BufferedReader(
                            new InputStreamReader(con.getInputStream()));
            String input;
            while ((input = br.readLine()) != null) {
                lines += input + "\n";
            }
            br.close();
        }
        content = new String(lines.getBytes(), "UTF-8");
    }

    /**
     * Getter method for the content
     *
     * @return returns content as string
     */
    public String getContent() {
        return content;
    }

    /**
     * Getter method for the certificate information
     *
     * @return returns certificate string
     */
    public String getCertInfo() {
        return certInfo;
    }

    /**
     * Getter method for the URL
     *
     * @return URL
     */
    public String getURL() {
        return URL;
    }

    /**
     * Getter method for the timestamp
     *
     * @return timestamp
     */
    public Date getTimestamp() {
        return timestamp;
    }

    /**
     * Getter method for the https certificates
     *
     * @return array of certificates
     */
    public Certificate[] getCertificates() {
        return certificates;
    }
}