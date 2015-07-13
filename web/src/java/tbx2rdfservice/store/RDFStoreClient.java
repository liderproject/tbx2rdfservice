package tbx2rdfservice.store;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import static org.apache.http.HttpHeaders.USER_AGENT;

/**
 *
 * @author admin
 */
public class RDFStoreClient {

    public static boolean post(String url, String nt) {
        try {
            nt=URLEncoder.encode(nt,"UTF-8");
            //String url = "http://tbx2rdf.lider-project.eu/converter/resource/iate/IATE-84";
            URL obj = new URL(url);
            HttpURLConnection con = (HttpURLConnection) obj.openConnection();
            con.setRequestMethod("POST");
            con.setRequestProperty("User-Agent", USER_AGENT);
            con.setDoOutput(true);
            DataOutputStream wr = new DataOutputStream(con.getOutputStream());
            wr.writeBytes(nt);
            wr.flush();
            wr.close();
            int responseCode = con.getResponseCode();
            System.out.println("\nSending 'POST' request to URL : " + url);
            System.out.println("Response Code : " + responseCode);
            BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
            String inputLine;
            StringBuffer response = new StringBuffer();

            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
            in.close();

            //print result
            System.out.println(response.toString());
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }   
    }
    
}
