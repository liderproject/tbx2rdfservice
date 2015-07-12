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
    List<LexicalEntry> entries = new ArrayList();
    
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
            String base = TBX2RDFServiceConfig.get("datauri","http://localhost:8080/");
            String sres = base + codificado;
            nt += "<"+sres+"> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://www.w3.org/2004/02/skos/core#Concept> .\n"; 
            if (!subjectField.isEmpty())
                nt += "<"+sres+"> <http://tbx2rdf.lider-project.eu/tbx#subjectField> \""+ subjectField +"\" .\n"; 
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

}