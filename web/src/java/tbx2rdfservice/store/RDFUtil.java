package tbx2rdfservice.store;

import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.rdf.model.Literal;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.NodeIterator;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.Charset;
import java.util.UUID;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFDataMgr;
import tbx2rdfservice.servlets.ServletLogger;

/**
 *
 * @author admin
 */
public class RDFUtil {

    
    public static String getFirstLiteral(Model model, String s, String p) {
        NodeIterator ni = model.listObjectsOfProperty(model.createResource(s), model.createProperty(p));
        if (ni.hasNext()) {
            RDFNode nodo = ni.next();
            if (nodo.isLiteral())
                return nodo.asLiteral().getLexicalForm();
        }
        return "";
    }

    public static String getFirstResource(Model model, String s, String p) {
        NodeIterator ni = model.listObjectsOfProperty(model.createResource(s), model.createProperty(p));
        if (ni.hasNext()) {
            RDFNode r = ni.next();
            if (r.isLiteral())
            {
                Literal l = r.asLiteral(); 
                String cadena = l.getLexicalForm();
                cadena = RDFUtil.escapeNTriples(cadena);
                return cadena;
            }
            else if (r.isResource()){
                return r.asResource().toString();
            }
            else
                return "";
        }
        return "";
    }

    public static void main(String[] args) {
        StringWriter sw = new StringWriter();
        Model model = browseRDF("http://tbx2rdf.lider-project.eu/data/iate/IATE-1178464");
        RDFDataMgr.write(sw, model, Lang.TTL);
        System.out.println(sw);

    }

    public static Model browseNT(String url) {
        try {
            URLConnection connection = new URL(url).openConnection();
            connection.setRequestProperty("Accept", "application/n-triples");
            Model model = ModelFactory.createDefaultModel();
            RDFDataMgr.read(model, connection.getInputStream(), Lang.NT);
            return model;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
    
    public static Model browseTTL(String url) {
        try {
            URLConnection connection = new URL(url).openConnection();
            connection.setRequestProperty("Accept", "text/turtle");
            Model model = ModelFactory.createDefaultModel();
            RDFDataMgr.read(model, connection.getInputStream(), Lang.TTL);
            return model;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }    

    public static Model browseRDF(String url) {
        try {
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
        } catch (Exception e) {
        }
        return null;
    }

    public static String getBlankNodeName() {
        String s = "_:";
        s += UUID.randomUUID().toString();;
        return s;
    }

    public static byte[] unescapePython(String escapedString) throws Exception {
		// simple state machine iterates over the bytes
        // in the escapedString and converts
        byte[] escaped = escapedString.getBytes();
        byte[] unescaped = new byte[escaped.length];
        int j = 0;
        for (int i = 0; i < escaped.length; i++) {
            // if its not special then just move on
            if (escaped[i] != '\\') {
                unescaped[j] = escaped[i];
                j++;
                continue;
            }
            // if there is no next byte, throw incorrect encoding error
            if (i + 1 >= escaped.length) {
                throw new Exception(
                        "String incorrectly escaped, ends with escape character.");
            }
            // deal with hex first
            if (escaped[i + 1] == 'x') {
                // if there's no next byte, throw incorrect encoding error
                if (i + 3 >= escaped.length) {
                    throw new Exception(
                            "String incorrectly escaped, ends early with incorrect hex encoding.");
                }
                unescaped[j] = (byte) ((Character.digit(escaped[i + 2], 16) << 4) + Character
                        .digit(escaped[i + 3], 16));
                j++;
                i += 3;
            } // deal with n, then t, then r
            else if (escaped[i + 1] == 'n') {
                unescaped[j] = '\n';
                j++;
                i++;
            } else if (escaped[i + 1] == 't') {
                unescaped[j] = '\t';
                j++;
                i++;
            } else if (escaped[i + 1] == 'r') {
                unescaped[j] = '\r';
                j++;
                i++;
            } else if (escaped[i + 1] == '\\') {
                unescaped[j] = escaped[i + 1];
                j++;
                i++;
            } else if (escaped[i + 1] == '\'') {
                unescaped[j] = escaped[i + 1];
                j++;
                i++;
            } else {
                // invalid character
                throw new Exception(
                        "String incorrectly escaped, invalid escaped character");
            }
        }
        byte[] unescapedTrim = new byte[j];
        for (int k = 0; k < j; k++) {
            unescapedTrim[k] = unescaped[k];
        }
        // return byte array, not string. Callers can convert to string.
        return unescapedTrim;
    }

    /*
     * Converts a byte array into an escaped character string that could be used
     * as a python string literal.
     */
    public static String escapeNTriples(String cadena)  {
        
        String s = cadena;

        s = s.replace("“", "\"");
        s = s.replace("”", "\"");
        s = s.replace("’", "\"");
        s = s.replace("‘", "\"");
        s = s.replace("–", "-");
        s = s.replace("„", "\"");
        
        s = s.replace("\"", "\\\"");
        s = s.replace("'", "\\'");
        s = s.replace("\n", "\\n");
        s = s.replace("\t", "\\t");
        s = s.replace("\b", "\\b");
        s = s.replace("\r", "\\r");
        
        
        if (true)
            return s;
        
        byte []raw = cadena.getBytes(Charset.forName("UTF-8"));
        StringBuilder escaped = new StringBuilder();
        byte c;
        for (int i = 0; i < raw.length; i++) {
            c = raw[i];
            if (c == '\'') {
                escaped.append('\\');
                escaped.append('\'');
            } else if (c == '\\') {
                escaped.append('\\');
                escaped.append('\\');
            } else if (c == '\t') {
                escaped.append('\\');
                escaped.append('t');
            } else if (c == '\n') {
                escaped.append('\\');
                escaped.append('n');
            } else if (c == '\r') {
                escaped.append('\\');
                escaped.append('r');
            } else if (c < ' ' || c >= 0x7f) {
                // Outside safe range, so represent as escaped hex
                String hexEscaped;
                hexEscaped = String.format("\\x%02x", c & 0xff);
                escaped.append(hexEscaped);
            } else {
                // Just a normal character, so emit it unchanged.
                escaped.append((char) c);
            }
        }
        return escaped.toString();
    }

    /**
     * Creates a ntriple (ending in newline) from a solution on the form s, p, o
     */
    public static String createNtripleFromSparql(QuerySolution soln) {
            Resource s = soln.getResource("s");       // Get a result variable by name.
            String ss = "<" + s.getURI() +">";
            if (s.isAnon())
                ss="_:"+s.getId().getLabelString();
            
            Resource p = soln.getResource("p");       // Get a result variable by name.
            RDFNode o = soln.get("o"); // Get a result variable - must be a resource
            String so = "";
            if (o.isLiteral()) {
                Literal l = o.asLiteral();
                String cadena = l.getLexicalForm();
                cadena = RDFUtil.escapeNTriples(cadena);
                if (l.getLanguage().isEmpty()) {
                    so = "\"" + cadena + "\"";
                } else {
                    so = "\"" + cadena + "\"@" + l.getLanguage() ;
                }
            } else if (o.isAnon())
            {
                String s1 = o.asResource().getId().getLabelString();
//                String s2 = o.asResource().getId().toString();
//                ServletLogger.global.log("ANONYUMO: " +s1+" "+s2);
                so = "_:"+s1;
            }else if (o.isResource()) {
                Resource r = o.asResource();
                so = "<" + r.getURI() + ">";
            }
            else 
            {
                ServletLogger.global.log("ERROR QUE DEMONIOS ES");
                so="ERROR";
            }
            String newline = ss + " <" + p.toString() + "> " + so + " . \n";    
            return newline;
    }

}
