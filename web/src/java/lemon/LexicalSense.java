package lemon;

import com.hp.hpl.jena.rdf.model.Literal;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.NodeIterator;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.ResourceFactory;
import com.hp.hpl.jena.rdf.model.Selector;
import com.hp.hpl.jena.rdf.model.SimpleSelector;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;
import static com.hp.hpl.jena.vocabulary.RDF.Statement;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.httpclient.URIException;
import org.apache.commons.httpclient.util.URIUtil;
import tbx2rdfservice.TBX2RDFServiceConfig;
import tbx2rdfservice.servlets.ServletLogger;
import tbx2rdfservice.store.RDFPrefixes;
import tbx2rdfservice.store.RDFStoreFuseki;
import tbx2rdfservice.store.RDFUtil;

/**
 * Clase que representa un concepto
 *
 * @author admin
 */
public class LexicalSense {

    public String name = "";
    public String subjectField = "";
    public String reference = "";
    public String parent = "";
    public String comment = "";
    public String related = "";
    public List<String> definitions = new ArrayList();
    public List<String> definitionsources = new ArrayList();
    public List<String> definitionlans = new ArrayList();
    public List<String> links = new ArrayList();
    public String jurisdiction = "";
    public List<LexicalEntry> entries = new ArrayList();
    public String base = TBX2RDFServiceConfig.get("datauri", "http://localhost:8080/");

    public LexicalSense() {

    }

    public LexicalSense(String _name) {
        name = _name;
    }

    /**
     *
     */
    public LexicalSense(Model model, Resource res) {
        String uri = res.getURI();
        name = RDFPrefixes.getLastPart(uri);

        //BUSCAMOS LAS DEFINICIONES
        NodeIterator ni = null;
        /*ni = model.listObjectsOfProperty(res, model.createProperty("http://lemon-model.net/lemon#definition")); //http://www.w3.org/2004/02/skos/core#definition
         while (ni.hasNext())
         {
         RDFNode nodo = ni.next();
         if (nodo.isLiteral())
         {
         definitionlans.add(nodo.asLiteral().getLanguage());
         definitions.add(nodo.asLiteral().getLexicalForm());
         definitionsources.add("");
         }
         if (nodo.isResource())
         {
         Resource r = nodo.asResource();
         ServletLogger.global.log("WWWWWWWWWWWWWWWWWWWWWWW");
         }
         }*/
        subjectField = RDFUtil.getFirstLiteral(model, uri, "http://tbx2rdf.lider-project.eu/tbx#subjectField");
        jurisdiction = RDFUtil.getFirstResource(model, uri, "http://creativecommons.org/ns#jurisdiction");
        parent = RDFUtil.getFirstResource(model, uri, "http://www.w3.org/2004/02/skos/core#narrower");
        reference = RDFUtil.getFirstResource(model, uri, "http://lemon-model.net/lemon#reference");
        comment = RDFUtil.getFirstLiteral(model, uri, "http://www.w3.org/2000/01/rdf-schema#comment");
        related = RDFUtil.getFirstResource(model, uri, "http://www.w3.org/2004/02/skos/core#related");

        ServletLogger.global.log("COMMENT: " + comment);

        //SENSE OF
        ni = model.listObjectsOfProperty(res, model.createProperty("http://www.w3.org/ns/lemon/ontolex#isSenseOf"));
        while (ni.hasNext()) {
            RDFNode nodo = ni.next();
            LexicalEntry le = new LexicalEntry(model, nodo.asResource());
            entries.add(le);
        }

        //CLOSE MATCH
        ni = model.listObjectsOfProperty(res, model.createProperty("http://www.w3.org/2004/02/skos/core#closeMatch"));
        while (ni.hasNext()) {
            RDFNode nodo = ni.next();
            links.add(nodo.asResource().toString());
        }
//        ni = model.listObjectsOfProperty(res, ModelFactory.createDefaultModel().createProperty("http://lemon-model.net/lemon#definition"));
        Selector selector = new SimpleSelector(res, ModelFactory.createDefaultModel().createProperty("http://www.w3.org/2004/02/skos/core#definition"), (RDFNode) null);
        StmtIterator li = model.listStatements(selector);
        while (li.hasNext()) {
            Statement st = li.next();
            String so = "";
            RDFNode o = st.getObject();
            if (!o.isResource()) {
                continue;
            }
            Resource r = o.asResource();
            if (r.isAnon()) {
                so = "_:" + r.getId().getLabelString();
            } else {
                so = r.getURI();
            }

            String label = "";
            String lan = "";
            String source = "";
            NodeIterator ny = model.listObjectsOfProperty(r, model.createProperty("http://www.w3.org/1999/02/22-rdf-syntax-ns#value"));
            if (ny.hasNext()) {
                Literal lit = ny.next().asLiteral();
                label = lit.getLexicalForm();
                lan = lit.getLanguage();
            }
            NodeIterator n2 = model.listObjectsOfProperty(o.asResource(), model.createProperty("http://purl.org/dc/terms/source"));
            if (n2.hasNext()) {
                RDFNode n = n2.next();
                if (n.isLiteral()) {
                    source = n.asLiteral().getLexicalForm();
                } else if (n.isResource()) {
                    source = n.toString();
                }
            }
            definitions.add(label);
            definitionsources.add(source);
            definitionlans.add(lan);
        }
        ServletLogger.global.log("Definiciones: " + definitions.size());

        while (ni.hasNext()) {
            Resource r = ni.next().asResource();
            if (!r.isAnon()) {
                continue;
            }
            ServletLogger.global.log("Parseando nodo de definition " + r.getURI() + " " + r.toString() + " " + r.isAnon() + " TRIPLES: " + model.size());
            ServletLogger.global.log("ANONIMO: " + r.getId().getLabelString() + " ");
            String label = "";
            String lan = "";
            String source = "";
            NodeIterator ny = model.listObjectsOfProperty(r, model.createProperty("http://www.w3.org/1999/02/22-rdf-syntax-ns#value"));
            if (ny.hasNext()) {
                Literal lit = ny.next().asLiteral();
                label = lit.getLexicalForm();
                lan = lit.getLanguage();
                ServletLogger.global.log("LOOP: " + label + " " + lan);
                source = RDFUtil.getFirstLiteral(model, r.getURI(), "http://purl.org/dc/terms/source");
            }
            if (!label.isEmpty()) {
                definitions.add(label);
                definitionsources.add(source);
                definitionlans.add(lan);
            }
        }
    }
    
    public String getURI() {
        String url = base + name;
        try {
            url = URIUtil.encodeQuery(url);
        } catch (Exception e) {
            e.getMessage();
        }
        return url;
    }

    /**
     * Obtiene la versión NT de la lexical sense
     */
    public String getNT() {
        try {
            String nt = "";
            String sres = RDFPrefixes.encode(base, name);

            nt += "<" + sres + "> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://www.w3.org/2004/02/skos/core#Concept> .\n";
            nt += "<" + sres + "> <http://www.w3.org/2000/01/rdf-schema#label> \"" + name + "\" .\n";
            if (!subjectField.isEmpty()) {
                nt += "<" + sres + "> <http://tbx2rdf.lider-project.eu/tbx#subjectField> \"" + subjectField + "\" .\n";
            }
            if (!jurisdiction.isEmpty()) {
                nt += "<" + sres + "> <http://creativecommons.org/ns#jurisdiction> <" + jurisdiction + "> .\n";
            }
            if (!reference.isEmpty()) {
                nt += "<" + sres + "> <http://lemon-model.net/lemon#reference> <" + reference + "> .\n";
            }
            if (!related.isEmpty()) {
                nt += "<" + sres + "> <http://www.w3.org/2004/02/skos/core#related> <" + related + "> .\n";
            }
            if (!parent.isEmpty()) {
                nt += "<" + sres + "> <http://www.w3.org/2004/02/skos/core#narrower> <" + parent + "> .\n";
            }
            if (!comment.isEmpty()) {
                nt += "<" + sres + "> <http://www.w3.org/2000/01/rdf-schema#comment> \" " + comment + " \"@en .\n";
                boolean b2 = comment.contains("\n");
                ServletLogger.global.log("asdf " + comment);
            }

            int n = definitions.size();
            for (int i = 0; i < n; i++) {
                String def = definitions.get(i);
                String deflan = definitionlans.get(i);
                String defsource = definitionsources.get(i);

//                String defuri = base + UUID.randomUUID().toString();

                Resource blank = ModelFactory.createDefaultModel().createResource(RDFUtil.getBlankNodeName());
                String defuri = blank.getURI();
                ServletLogger.global.log("AL HACER GETNT TENEMOS " + definitions.size() + " " + definitionlans.size());
//                Literal li = ResourceFactory.createLangLiteral(def, deflan);

                
                
                nt += "<" + sres + "> <http://www.w3.org/2004/02/skos/core#definition> " + defuri + " .\n";
                if (deflan.isEmpty()) {
                    nt +=  defuri + " <http://www.w3.org/1999/02/22-rdf-syntax-ns#value> \"" + def + "\" .\n";
                } else {
                    nt +=  defuri + " <http://www.w3.org/1999/02/22-rdf-syntax-ns#value> \"" + def + "\"@" + deflan + " .\n";
                }
                nt +=  defuri + " <http://purl.org/dc/terms/source> \"" + defsource + "\" .\n";
                nt +=  defuri + " <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> " + "<http://lemon-model.net/lemon#SenseDefinition> .\n";
            }
            for (String link : links) {
                nt += "<" + sres + "> <http://www.w3.org/2004/02/skos/core#closeMatch> <" + link + "> .\n";
            }
            for (LexicalEntry entry : entries) {
                nt += "<" + sres + "> <http://www.w3.org/ns/lemon/ontolex#isSenseOf> <" + entry.getURI() + "> .\n";

                nt += entry.getNT();

            }
            return nt;
        } catch (Exception e) {
            return "";
        }
    }

    public String getXML() {
        try {
            String codificado = URLEncoder.encode(name, "UTF-8");
            codificado = codificado.replace("+", "%20");

            String xml = "<termEntry id=\"" + codificado + ">\n";
            if (!subjectField.isEmpty()) {
                xml += " <descripGrp>\n";
                xml += " <descrip type=\"subjectField\">" + subjectField + "</descrip>\n";
                xml += " </descripGrp>\n";
            }
            for (LexicalEntry entry : entries) {
                xml += entry.getXML();
            }
            xml += " </termEntry>\n";
            return xml;
        } catch (UnsupportedEncodingException ex) {
            Logger.getLogger(LexicalSense.class.getName()).log(Level.SEVERE, null, ex);
            return "";
        }
    }

    public void addEntry(LexicalEntry le) {
        le.urisense = this.getURI();
        entries.add(le);
    }


    public void addDefinition(String def, String es) {
        addDefinition(def, es, "");
    }

    public void addDefinition(String def, String es, String source) {
        definitions.add(def);
        definitionlans.add(es);
        definitionsources.add(source);
    }
}
