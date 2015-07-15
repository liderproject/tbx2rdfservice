package tbx2rdfservice.store;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.NodeIterator;
import com.hp.hpl.jena.rdf.model.Resource;

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
    
}
