package lemon;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.NodeIterator;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import tbx2rdfservice.TBX2RDFServiceConfig;
import tbx2rdfservice.store.RDFPrefixes;
import tbx2rdfservice.store.RDFUtil;

/**
 *
 * @author admin
 */
public class LexicalSense {

    String id = "";
    String name = "";
    public String subjectField = "";
    public String reference ="";
    public String parent="";
    public List<String> definitions=new ArrayList();
    public List<String> definitionlans=new ArrayList();
    public List<String> links = new ArrayList();
    public String jurisdiction ="";
    public List<LexicalEntry> entries = new ArrayList();
    public String base = TBX2RDFServiceConfig.get("datauri","http://localhost:8080/");
    
    public LexicalSense()
    {
        
    }
    public LexicalSense(String _name)
    {
        name=_name;
    }

    public LexicalSense(Model model, Resource res)
    { 
        String uri = res.getURI();
        name = RDFPrefixes.getLastPart(uri);
        NodeIterator ni = model.listObjectsOfProperty(res, model.createProperty("http://creativecommons.org/ns#jurisdiction"));
        if (ni.hasNext())
            jurisdiction = ni.next().asResource().toString();
        ni = model.listObjectsOfProperty(res, model.createProperty("http://www.w3.org/2004/02/skos/core#definition"));
        while (ni.hasNext())
        {
            RDFNode nodo = ni.next();
            definitionlans.add(nodo.asLiteral().getLanguage());
            definitions.add(nodo.asLiteral().getLexicalForm());
        }
        parent = RDFUtil.getFirst(model, uri, "http://www.w3.org/2004/02/skos/core#narrower");
        /*
        ni = model.listObjectsOfProperty(res, model.createProperty("http://www.w3.org/2004/02/skos/core#narrower"));
        if (ni.hasNext())
            parent = ni.next().asResource().toString();*/
        ni = model.listObjectsOfProperty(res, model.createProperty("http://lemon-model.net/lemon#reference"));
        if (ni.hasNext())
            reference = ni.next().asResource().toString();
        ni = model.listObjectsOfProperty(res, model.createProperty("http://www.w3.org/ns/lemon/ontolex#isSenseOf"));
        while (ni.hasNext())
        {
            RDFNode nodo = ni.next();
            LexicalEntry le = new LexicalEntry(model, nodo.asResource());
            entries.add(le);
        }
        
    }
            
    
    public String getNT()
    {
        try {
            String nt="";
            String sres=RDFPrefixes.encode(base,name);
            
            nt += "<"+sres+"> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://www.w3.org/2004/02/skos/core#Concept> .\n"; 
            nt += "<"+sres+"> <http://www.w3.org/2000/01/rdf-schema#label> \""+name+ "\" .\n"; 
            if (!subjectField.isEmpty())
                nt += "<"+sres+"> <http://tbx2rdf.lider-project.eu/tbx#subjectField> \""+ subjectField +"\" .\n"; 
            if (!jurisdiction.isEmpty())
                nt += "<"+sres+"> <http://creativecommons.org/ns#jurisdiction> <"+ jurisdiction +"> .\n"; 
            if (!reference.isEmpty())
                nt += "<"+sres+"> <http://lemon-model.net/lemon#reference> <"+ reference +"> .\n"; 
            if (!parent.isEmpty())
                nt += "<"+sres+"> <http://www.w3.org/2004/02/skos/core#narrower> <"+ parent +"> .\n"; 
            int n = definitions.size();
            for(int i=0;i<n;i++)
            {
                String def = definitions.get(i);
                def=def.replace("\"", "");
                String deflan= definitionlans.get(i);
                String lit="\""+def+"\"";
                if (!deflan.isEmpty())
                    lit+="@"+deflan;
                nt += "<"+sres+"> <http://www.w3.org/2004/02/skos/core#definition> "+ lit +" .\n"; 
            }
            for(String link : links)
            {
                nt += "<"+sres+"> <http://www.w3.org/2004/02/skos/core#closeMatch> <"+ link +"> .\n"; 
            }
            for(LexicalEntry entry : entries)
            {
                nt += "<"+sres+"> <http://www.w3.org/ns/lemon/ontolex#isSenseOf> <"+ entry.getURI() +"> .\n"; 
                
                nt += entry.getNT();
                
            }
            return nt;
        }catch(Exception e)
        {
            return "";
        }
    }
    
    public String getXML() {
        try {
            String codificado = URLEncoder.encode(name, "UTF-8");
            codificado=codificado.replace("+", "%20");

            String xml = "<termEntry id=\"" + codificado + ">\n";
            if (!subjectField.isEmpty()) {
                xml += " <descripGrp>\n";
                xml += " <descrip type=\"subjectField\">" + subjectField + "</descrip>\n";
                xml += " </descripGrp>\n";
            }
            for(LexicalEntry entry : entries)
            {
                xml+=entry.getXML();
            }
            xml += " </termEntry>\n";
            return xml;
        } catch (UnsupportedEncodingException ex) {
            Logger.getLogger(LexicalSense.class.getName()).log(Level.SEVERE, null, ex);
            return "";
        }
    }

    public void addEntry(LexicalEntry le) {
        le.urisense=this.getURI();
        entries.add(le);
    }
    public String getURI() {
        return RDFPrefixes.encode(base,name);
    }

    public void addDefinition(String def, String es) {
        def=def.replace("\n","");
        definitions.add(def);
        definitionlans.add(es);
    }
}
