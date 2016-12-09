package helperClasses;

import javax.net.ssl.HttpsURLConnection;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.cert.Certificate;
import java.util.Date;

public class https {

    private String content;
    private String certInfo;
    private Certificate[] certificates;
    private String URL;
    private Date timestamp;


    public https() {

    }

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
            System.out.println("can't load "+ https_url);
            return false;
        }
        return true;
    }


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

    private void loadContent(HttpsURLConnection con) throws IOException {
        String lines = "";
        if (con != null) {
            BufferedReader br =
                    new BufferedReader(
                            new InputStreamReader(con.getInputStream()));
            String input;
            while ((input = br.readLine()) != null) {
                lines+=input+"\n";
            }
            br.close();
        }
        content = new String(lines.getBytes(), "UTF-8");
    }

    public String getContent() {
        return content;
    }

    public String getCertInfo() {
        return certInfo;
    }

    public String getURL() {
        return URL;
    }

    public Date getTimestamp() {
        return timestamp;
    }

    public Certificate[] getCertificates() {
        return certificates;
    }
}