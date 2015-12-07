package lemon;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.Resource;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import tbx2rdfservice.TBX2RDFServiceConfig;
import tbx2rdfservice.store.RDFPrefixes;
import tbx2rdfservice.store.RDFUtil;

/**
 * This class represents a lexical entry
 *
 * @author admin
 */
public class LexicalEntry {

    String id = "";
    String name = "";
    String uritype = "";
    public String urisense = "";
    public String uricanonicalform = "";
    public String reliabilitycode = "";
    public String lan = "en";
    public String sense="";
    public String comentario="";
    public String source = "";
    public String base = TBX2RDFServiceConfig.get("datauri", "http://localhost:8080/tbx/");
    public String definition="";


    public LexicalEntry() {

    }

    public LexicalEntry(String _name, String _lan) {
        name = _name+"_"+_lan;
        lan = _lan;
    }
    public LexicalEntry(Model model, Resource res)
    {
        name = RDFPrefixes.getLastPart(res.getURI().toString());
        base=res.getURI().substring(0,res.getURI().lastIndexOf("/")+1);
        String uri = res.getURI();
        
        try{
            lan = RDFUtil.getFirstLiteral(model, uri, "http://lemon-model.net/lemon#language");
        }catch(Exception e)
        {
            lan = RDFUtil.getFirstResource(model, uri, "http://lemon-model.net/lemon#language");
        }
        
        
        source = RDFUtil.getFirstLiteral(model, uri, "http://purl.org/dc/terms/source");
        comentario = RDFUtil.getFirstLiteral(model, uri, "http://www.w3.org/2000/01/rdf-schema#comment");
        reliabilitycode = RDFUtil.getFirstLiteral(model, uri, "http://tbx2rdf.lider-project.eu/tbx#reliabilityCode");
        uricanonicalform = RDFUtil.getFirstResource(model, uri, "http://www.w3.org/ns/lemon/ontolex#canonicalForm");
        definition = RDFUtil.getFirstLiteral(model, uri, "http://www.w3.org/2004/02/skos/core#definition");
    }
    public String getURI() {
         return RDFPrefixes.encode(base,name);
    }

    public String getXML() {
        String xml = "<langSet xml:lang=\"" + lan + "\">\n";
        xml += "  <tig>\n"
                + "    <term>" + name + "</term>\n"
                + "    <termNote type=\"termType\">fullForm</termNote>\n"
                + "    <descrip type=\"reliabilityCode\">" + reliabilitycode + "</descrip>\n"
                + "  </tig>";
        xml += "</langSet>\n";
        return xml;
    }

    /**
     * Crea una LexicalEntry y un LexicalConcept asociado
     */
    public String getNT() {
        try {
            String nt = "";
            String sres=RDFPrefixes.encode(base,name);
            
            //Creamos el LexicalSense
            String sreslc = sres+"-lexicalsense";
            nt += "<" + sreslc + "> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://www.w3.org/ns/lemon/ontolex#LexicalSense> .\n";
            nt += "<" + sres + "> <http://www.w3.org/ns/lemon/ontolex#sense> <"+ sreslc +"> .\n";
            
             
            
            nt += "<" + sres + "> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://www.w3.org/ns/lemon/ontolex#LexicalEntry> .\n";
            nt += "<"+sres+"> <http://www.w3.org/2000/01/rdf-schema#label> \""+name+ "\" .\n"; 
            if (!uritype.isEmpty()) {
                nt += "<" + sres + "> <http://tbx2rdf.lider-project.eu/tbx#termType> <" + uritype + "> .\n";
            }
            if (!urisense.isEmpty()) {
                nt += "<" + sreslc + "> <http://www.w3.org/ns/lemon/ontolex#reference> <" + urisense + "> .\n";
                nt += "<" + sres + "> <http://www.w3.org/ns/lemon/ontolex#denotes> <" + urisense + "> .\n";
            }
            if (!uricanonicalform.isEmpty()) {
                nt += "<" + sres + "> <http://www.w3.org/ns/lemon/ontolex#canonicalForm> <" + uricanonicalform + "> .\n";
            }
            if (!reliabilitycode.isEmpty()) {
                nt += "<" + sres + "> <http://tbx2rdf.lider-project.eu/tbx#reliabilityCode> \"" + reliabilitycode + "\" .\n";
            }
            if (!lan.isEmpty()) {
                lan=lan.replace("\"", "");

                String fullstring = "http://id.loc.gov/vocabulary/iso639-1/";
                fullstring+=lan;
 //               nt += "<" + sres + "> <http://lemon-model.net/lemon#language> <" + fullstring + "> .\n";
                        
                nt += "<" + sres + "> <http://lemon-model.net/lemon#language> \"" + lan + "\" .\n";
                
            }
            if (!source.isEmpty()) {
                source=source.replace("\"", "");
                nt += "<" + sres + "> <http://purl.org/dc/terms/source> \"" + source + "\" .\n";
            }
            if (!comentario.isEmpty()) {
                comentario=comentario.replace("\"", "");
                nt += "<" + sres + "> <http://www.w3.org/2000/01/rdf-schema#comment> \"" + comentario + "\" .\n";
            }
            if (!definition.isEmpty()) {
                definition=definition.replace("\"", "");
                nt += "<" + sres + "> <http://www.w3.org/2004/02/skos/core#definition> \"" + definition + "\" .\n";
            }
            return nt;
        } catch (Exception e) {
            return "";
        }
    }

    /**
     * Retains the last part of an URI.
     */
    public String getBeautifulname() {
        String s = RDFPrefixes.getLastPart(getURI());
        try {
            s=URLDecoder.decode(s, "UTF-8");
        } catch (UnsupportedEncodingException ex) {
        }
        s=s.substring(0, s.lastIndexOf("_"));
        return s;
    }

    public String getLanguageFromName() {
        if (name.length()<4)
            return "";
        int ind = name.lastIndexOf("_");
        if (ind==-1)
            return "";
        String st = name.substring(name.length()-2, name.length());
        if (st.charAt(0)<'a' || st.charAt(1)>'z')
            return "";
        return st;
            
    }

}
