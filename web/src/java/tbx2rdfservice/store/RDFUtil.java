package tbx2rdfservice.store;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.NodeIterator;
import com.hp.hpl.jena.rdf.model.Resource;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.net.URL;
import java.net.URLConnection;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFDataMgr;

/**
 *
 * @author admin
 */
public class RDFUtil {
    
    public static String getFirstLiteral(Model model, String s, String p)
    {
        NodeIterator ni = model.listObjectsOfProperty(model.createResource(s), model.createProperty(p));
        if (ni.hasNext())
            return ni.next().asLiteral().getLexicalForm();
        return "";
    }
    public static String getFirstResource(Model model, String s, String p)
    {
        NodeIterator ni = model.listObjectsOfProperty(model.createResource(s), model.createProperty(p));
        if (ni.hasNext())
            return ni.next().asResource().toString();
        return "";
    }
        public static void main(String[] args) {
            StringWriter sw = new StringWriter();
            Model model = browseRDF("http://tbx2rdf.lider-project.eu/data/iate/IATE-1178464");
            RDFDataMgr.write(sw, model, Lang.TTL);
            System.out.println(sw);
            
        }
    
    public static Model browseRDF(String url)
    {
        try{
            URLConnection connection = new URL(url).openConnection();
            connection.setRequestProperty("Accept", "application/rdf+xml");
            Model model = ModelFactory.createDefaultModel();
            RDFDataMgr.read(model, connection.getInputStream(), Lang.RDFXML);
            return model;
/*            BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream(), "UTF-8"));
            StringBuilder xml = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                xml.append(line);
            }
            */
            
       }catch(Exception e){}
        return null;
    }
    
}
