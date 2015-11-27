package tbx2rdfservice.store;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import static org.apache.http.HttpHeaders.USER_AGENT;
import org.apache.commons.httpclient.util.URIUtil;
/**
 *
 * @author admin
 */
public class RDFStoreClient {

    public static boolean delete(String url)
    {
        try{
            URL obj = new URL(url);
            HttpURLConnection con = (HttpURLConnection) obj.openConnection();
            con.setRequestMethod("DELETE");
            con.setRequestProperty("User-Agent", USER_AGENT);
            con.setDoOutput(true);
            DataOutputStream wr = new DataOutputStream(con.getOutputStream());
            wr.writeBytes("");
            wr.flush();
            wr.close();
            int responseCode = con.getResponseCode();
            return true;
        }catch(Exception e){
            e.printStackTrace();
        }
        return false;
    }
                
    /**
     * Posts a fragment of NTRIPLES to a given URL
     */
    public static boolean post(String url, String nt) {
        if (url.equals("http://tbx2rdf.lider-project.eu/converter/resource/cc/"))
            return false;
        try {

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
            System.out.println(responseCode + " " + url);
            BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
            String inputLine;
            StringBuffer response = new StringBuffer();
            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
            in.close();

            //print result
//            System.out.println(response.toString());
            return true;
        } catch (Exception e) {
            System.err.println("mal");
//            e.printStackTrace();
            return false;
        }   
    }
    
}
