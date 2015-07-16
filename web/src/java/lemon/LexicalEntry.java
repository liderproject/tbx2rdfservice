package lemon;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.NodeIterator;
import com.hp.hpl.jena.rdf.model.Resource;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.logging.Level;
import java.util.logging.Logger;
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
    public String reliabilitycode = "3";
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
        lan = RDFUtil.getFirstLiteral(model, uri, "http://lemon-model.net/lemon#language");
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

    public String getNT() {
        try {
            String nt = "";
            String sres=RDFPrefixes.encode(base,name);
            nt += "<" + sres + "> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://www.w3.org/ns/lemon/ontolex#LexicalEntry> .\n";
            nt += "<"+sres+"> <http://www.w3.org/2000/01/rdf-schema#label> \""+name+ "\" .\n"; 
            if (!uritype.isEmpty()) {
                nt += "<" + sres + "> <http://tbx2rdf.lider-project.eu/tbx#termType> <" + uritype + "> .\n";
            }
            if (!urisense.isEmpty()) {
                nt += "<" + sres + "> <http://www.w3.org/ns/lemon/ontolex#sense> <" + urisense + "> .\n";
            }
            if (!uricanonicalform.isEmpty()) {
                nt += "<" + sres + "> <http://www.w3.org/ns/lemon/ontolex#canonicalForm> <" + uricanonicalform + "> .\n";
            }
            if (!reliabilitycode.isEmpty()) {
                nt += "<" + sres + "> <http://tbx2rdf.lider-project.eu/tbx#reliabilityCode> \"" + reliabilitycode + "\" .\n";
            }
            if (!lan.isEmpty()) {
                lan=lan.replace("\"", "");
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

    public String getBeautifulname() {
        String s = RDFPrefixes.getLastPart(getURI());
        try {
            s=URLDecoder.decode(s, "UTF-8");
        } catch (UnsupportedEncodingException ex) {
        }
        s=s.substring(0, s.lastIndexOf("_"));
        return s;
    }

}
