package wikiAPI;

import javax.net.ssl.HttpsURLConnection;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.cert.Certificate;
import java.util.Date;

class httpsHelper {

    private String content;
    private String certInfo;
    private Certificate[] certs;
    private String URL;
    private Date timestamp;


    httpsHelper() {

    }

    boolean loadURL(String https_url){
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
            certs = con.getServerCertificates();
            int i = 1;
            for (Certificate cert : certs) {
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

    String getContent() {
        return content;
    }

    String getCertInfo() {
        return certInfo;
    }

    String getURL() {
        return URL;
    }

    Date getTimestamp() {
        return timestamp;
    }

    Certificate[] getCerts() {
        return certs;
    }
}