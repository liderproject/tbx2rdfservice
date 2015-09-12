package tbx2rdfservice.servlets;

import com.hp.hpl.jena.query.DatasetAccessor;
import com.hp.hpl.jena.query.DatasetAccessorFactory;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFDataMgr;
import tbx2rdfservice.TBX2RDFServiceConfig;
import tbx2rdfservice.store.RDFStoreFuseki;

/**
 *
 * @author vroddon
 */
public class Services {
    
    
    public static String countEntities(int current, int ilimit, String searchFrase)
    {
            int total = RDFStoreFuseki.countEntities("http://www.w3.org/2004/02/skos/core#Concept");
            if (total==-1)
            {
                //WE HAVE A PROBLEM, WE PROBABLY LACK CONNECTION TO FUSEKI OR ANY OTHER STORE
                return "";
            }
            int init = (current - 1) * ilimit;
            List<String> ls = RDFStoreFuseki.listConcepts(init, ilimit, searchFrase);
            String s = "{\n"
                    + "  \"current\": " + current + ",\n"
                    + "  \"rowCount\": " + ilimit + ",\n"
                    + "  \"rows\": [\n";
            int conta = 0;
            for (String cp : ls) {

                int lasti = cp.lastIndexOf("/");
                String nombre = cp.substring(lasti + 1, cp.length());
                try {
                    nombre = URLDecoder.decode(nombre, "UTF-8");
                } catch (UnsupportedEncodingException ex) {
                    Logger.getLogger(Services.class.getName()).log(Level.SEVERE, null, ex);
                }
                cp = cp.replace(" ", "+");
                if (conta != 0) {
                    s += ",\n";
                }
                s += "    {\n"
                        + "      \"resource\": \"" + nombre + "\",\n"
                        + "      \"resourceurl\": \"" + cp + "\"\n"
                        + "    } ";
                conta++;
            }

            s += "  ],\n"
                    + "  \"total\": " + total + "\n"
                    + "}    ";        
            return s;
    }
    
        public static String writeGame(String gameid, String game) {
        try {
            String serviceURI = TBX2RDFServiceConfig.get("fuseki", "http://localhost:3030/RDFChess/data");
            DatasetAccessor dataAccessor = DatasetAccessorFactory.createHTTP(serviceURI);
            Model model = ModelFactory.createDefaultModel();
            InputStream stream = new ByteArrayInputStream(game.getBytes("UTF-8"));
            RDFDataMgr.read(model, stream, Lang.TTL);
            dataAccessor.putModel(gameid, model); //gameid
            return gameid;
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
    }
}
