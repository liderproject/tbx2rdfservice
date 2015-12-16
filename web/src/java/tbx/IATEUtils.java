package tbx;

import com.hp.hpl.jena.rdf.model.Literal;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.NodeIterator;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.ResIterator;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.ResourceFactory;

//java
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFDataMgr;
import tbx2rdfservice.servlets.ServletLogger;

import tbx2rdfservice.store.RDFPrefixes;
import tbx2rdfservice.store.RDFUtil;

/**
 *
 * @author admin
 */
public class IATEUtils {

    public static void main(String[] args) {
        StringWriter sw = new StringWriter();
        List<Literal> lista = getIATETerms("http://tbx2rdf.lider-project.eu/data/iate/IATE-858430");
        for(Literal l :lista)
        {
            System.out.println(l.getLexicalForm()+" " + l.getLanguage());
        }
    }

    /**
     * Retrieves the terms for a concept.
     * @param urisense URI of a IATE as RDF. For example
     * http://tbx2rdf.lider-project.eu/data/iate/IATE-858430
     * @return List of terms 
     */
    public static List<Literal> getIATETerms(String urisense) {
        List<Literal> lista = new ArrayList();
        try{
        Model model = RDFUtil.browseNT(urisense);
        ResIterator ri = model.listSubjectsWithProperty(model.createProperty("http://www.w3.org/ns/lemon/ontolex#reference"), model.createResource(urisense));
        while (ri.hasNext()) {
            Resource r = ri.next();
            String last = RDFPrefixes.getLastPart(r.getURI());
            int i = last.lastIndexOf("-");
            if (i != -1) {
                String text = last.substring(0, i);
                String lan = last.substring(i + 1, last.length());
                lan = lan.replace("#Sense", "");
                Literal l = ResourceFactory.createLangLiteral(text, lan);
                lista.add(l);
            }
        }}catch(Exception e){e.printStackTrace();}
        return lista;
    }
    
    public static List<Literal> getEurovocTerms(String uri) {
        List<Literal> lista = new ArrayList();
        try{
            
            String uri1= uri+"&language=en";
            ServletLogger.global.log("Browsing... " + uri1 );
            Model model = RDFUtil.browseTTL(uri1);
            
            StringWriter sw = new StringWriter();
            RDFDataMgr.write(sw, model, Lang.TTL);
            ServletLogger.global.log("Browsed " + sw.toString() );
            ServletLogger.global.log("Scanning now " + uri );
            
            NodeIterator ni = model.listObjectsOfProperty(model.createResource(uri), model.createProperty("http://www.w3.org/2008/05/skos-xl#literalForm"));
            while (ni.hasNext()) {
                RDFNode n = ni.next();
                if (n.isLiteral())
                    lista.add(n.asLiteral());
                }
        
            uri1= uri+"?language=es";
            model = RDFUtil.browseTTL(uri1);
            ni = model.listObjectsOfProperty(model.createResource(uri), model.createProperty("http://www.w3.org/2008/05/skos-xl#literalForm"));
            while (ni.hasNext()) {
                RDFNode n = ni.next();
                if (n.isLiteral())
                    lista.add(n.asLiteral());
                }
            
            uri1= uri+"?language=fr";
            model = RDFUtil.browseTTL(uri1);
            ni = model.listObjectsOfProperty(model.createResource(uri), model.createProperty("http://www.w3.org/2008/05/skos-xl#literalForm"));
            while (ni.hasNext()) {
                RDFNode n = ni.next();
                if (n.isLiteral())
                    lista.add(n.asLiteral());
                }
            
            uri1= uri+"?language=pt";
            model = RDFUtil.browseTTL(uri1);
            ni = model.listObjectsOfProperty(model.createResource(uri), model.createProperty("http://www.w3.org/2008/05/skos-xl#literalForm"));
            while (ni.hasNext()) {
                RDFNode n = ni.next();
                if (n.isLiteral())
                    lista.add(n.asLiteral());
                }
            
            uri1= uri+"?language=de";
            model = RDFUtil.browseTTL(uri1);
            ni = model.listObjectsOfProperty(model.createResource(uri), model.createProperty("http://www.w3.org/2008/05/skos-xl#literalForm"));
            while (ni.hasNext()) {
                RDFNode n = ni.next();
                if (n.isLiteral())
                    lista.add(n.asLiteral());
                }
            
            uri1= uri+"?language=el";
            model = RDFUtil.browseTTL(uri1);
            ni = model.listObjectsOfProperty(model.createResource(uri), model.createProperty("http://www.w3.org/2008/05/skos-xl#literalForm"));
            while (ni.hasNext()) {
                RDFNode n = ni.next();
                if (n.isLiteral())
                    lista.add(n.asLiteral());
                }            
            
            }catch(Exception e){
                ServletLogger.global.log("Error Browsing Eurovoc... " + e.getMessage()  );
                e.printStackTrace();
            }

        
        ServletLogger.global.log("Found " + lista.size() );

        return lista;
        
    }
    
    
    
}
