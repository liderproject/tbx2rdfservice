package lemon;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import tbx2rdfservice.TBX2RDFServiceConfig;

/**
 *
 * @author admin
 */
public class LexicalSense {

    String id = "";
    String name = "";
    public String subjectField = "";
    public List<String> definitions=new ArrayList();
    public List<String> definitionlans=new ArrayList();
    List<LexicalEntry> entries = new ArrayList();
    public String base = TBX2RDFServiceConfig.get("datauri","http://localhost:8080/");
    
    public LexicalSense()
    {
        
    }
    public LexicalSense(String _name)
    {
        name=_name;
    }
    
    public String getNT()
    {
        try {
            String nt="";
            String codificado = URLEncoder.encode(name, "UTF-8");
            String sres = base + codificado;
            nt += "<"+sres+"> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://www.w3.org/2004/02/skos/core#Concept> .\n"; 
            if (!subjectField.isEmpty())
                nt += "<"+sres+"> <http://tbx2rdf.lider-project.eu/tbx#subjectField> \""+ subjectField +"\" .\n"; 
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
            for(LexicalEntry entry : entries)
            {
                nt += "<"+sres+"> <http://www.w3.org/ns/lemon/ontolex#reference> <"+ entry.getURI() +"> .\n"; 
                
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
        entries.add(le);
    }
    public String getURI() {
        try {
            String codificado = URLEncoder.encode(name, "UTF-8");
            String sres = base + codificado;
            return sres;
        } catch (Exception e) {
            return ""; 
        }
    }

    public void addDefinition(String def, String es) {
        definitions.add(def);
        definitionlans.add(es);
    }
}
