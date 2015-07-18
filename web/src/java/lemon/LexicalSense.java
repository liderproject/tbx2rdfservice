package lemon;

import com.hp.hpl.jena.rdf.model.Literal;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.NodeIterator;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.ResourceFactory;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
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
    public List<String> definitionsources=new ArrayList();
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
        NodeIterator ni = model.listObjectsOfProperty(res, model.createProperty("http://www.w3.org/2004/02/skos/core#definition"));
        while (ni.hasNext())
        {
            RDFNode nodo = ni.next();
            definitionlans.add(nodo.asLiteral().getLanguage());
            definitions.add(nodo.asLiteral().getLexicalForm());
            definitionsources.add("");
        }
        subjectField = RDFUtil.getFirstLiteral(model, uri, "http://tbx2rdf.lider-project.eu/tbx#subjectField");
        jurisdiction = RDFUtil.getFirstResource(model, uri, "http://creativecommons.org/ns#jurisdiction");
        parent = RDFUtil.getFirstResource(model, uri, "http://www.w3.org/2004/02/skos/core#narrower");
        reference = RDFUtil.getFirstResource(model, uri, "http://lemon-model.net/lemon#reference");
        ni = model.listObjectsOfProperty(res, model.createProperty("http://www.w3.org/ns/lemon/ontolex#isSenseOf"));
        while (ni.hasNext())
        {
            RDFNode nodo = ni.next();
            LexicalEntry le = new LexicalEntry(model, nodo.asResource());
            entries.add(le);
        }
        ni = model.listObjectsOfProperty(res, model.createProperty("http://www.w3.org/2004/02/skos/core#closeMatch"));
        while (ni.hasNext())
        {
            RDFNode nodo = ni.next();
            links.add(nodo.asResource().toString());
        }
        ni = model.listObjectsOfProperty(res, model.createProperty("http://lemon-model.net/lemon#definition"));
        while (ni.hasNext())
        {
            String label="";
            String lan="";
            Resource r = ni.next().asResource();
            NodeIterator ny = model.listObjectsOfProperty(r, model.createProperty("http://www.w3.org/2000/01/rdf-schema#label"));
            if (ny.hasNext())
            {
                Literal lit=ny.next().asLiteral();
                label = lit.getLexicalForm();
                lan= lit.getLanguage();
            }            
            String source=RDFUtil.getFirstLiteral(model, r.getURI(), "http://purl.org/dc/terms/source");
            definitions.add(label);
            definitionsources.add(source);
            definitionlans.add(lan);
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
                String deflan= definitionlans.get(i);
                String defsource= definitionsources.get(i);
                
                String defuri = base+UUID.randomUUID().toString();
                
                Literal li = ResourceFactory.createLangLiteral(def, deflan);
                nt += "<"+sres+"> <http://lemon-model.net/lemon#definition> <"+ defuri +"> .\n";
                if (deflan.isEmpty())
                    nt += "<"+defuri+"> <http://www.w3.org/2000/01/rdf-schema#label> \""+ def +"\" .\n";
                else
                     nt += "<"+defuri+"> <http://www.w3.org/2000/01/rdf-schema#label> \""+ def +"\"@"+ deflan+ " .\n";
                   
                nt += "<"+defuri+"> <http://purl.org/dc/terms/source> \""+ defsource +"\" .\n";
                
                
//                def=def.replace("\"", "");
//               String lit="\""+def+"\"";
//                if (!deflan.isEmpty())
//                    lit+="@"+deflan;
//                nt += "<"+sres+"> <http://www.w3.org/2004/02/skos/core#definition> "+ lit +" .\n"; 
            }
            for(String link : links)
            {
                nt += "<"+sres+"> <http://www.w3.org/2004/02/skos/core#closeMatch> <"+ link +"> .\n"; 
            }
            for(LexicalEntry entry : entries)
            {
                System.out.println(entry.getURI());
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
        addDefinition(def,es,"");
    }

    public void addDefinition(String def, String es, String source) {
        def=def.replace("\n","");
        definitions.add(def);
        definitionlans.add(es);
        definitionsources.add(source);
    }
}
