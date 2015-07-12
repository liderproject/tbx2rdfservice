package lemon;

import java.net.URLEncoder;
import tbx2rdfservice.TBX2RDFServiceConfig;

/**
 * This class represents a lexical entry
 *
 * @author admin
 */
public class LexicalEntry {

    String id = "";
    String name = "";
    String uritype = "";
    String urisense = "";
    String uricanonicalform = "";
    String reliabilitycode = "3";
    String lan = "en";
    public String comentario="";
    public String source = "";
    //

    public LexicalEntry() {

    }

    public LexicalEntry(String _name, String _lan) {
        name = _name;
        lan = _lan;
    }

    public String getURI() {
        try {
            String codificado = URLEncoder.encode(name, "UTF-8");
            String base = TBX2RDFServiceConfig.get("datauri", "http://localhost:8080/");
            String sres = base + codificado;
            return sres;
        } catch (Exception e) {
            return ""; 
        }
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
            String codificado = URLEncoder.encode(name, "UTF-8");
            String base = TBX2RDFServiceConfig.get("datauri", "http://localhost:8080/");
            String sres = base + codificado;
            nt += "<" + sres + "> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://www.w3.org/ns/lemon/ontolex#LexicalEntry> .\n";
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
                nt += "<" + sres + "> <http://tbx2rdf.lider-project.eu/tbx#reliabilityCode> <" + reliabilitycode + "> .\n";
            }
            if (!source.isEmpty()) {
                nt += "<" + sres + "> <http://purl.org/dc/terms/source> \"" + source + "\" .\n";
            }
            if (!comentario.isEmpty()) {
                nt += "<" + sres + "> <http://www.w3.org/2000/01/rdf-schema#comment> \"" + comentario + "\" .\n";
            }
            return nt;
        } catch (Exception e) {
            return "";
        }
    }

}
