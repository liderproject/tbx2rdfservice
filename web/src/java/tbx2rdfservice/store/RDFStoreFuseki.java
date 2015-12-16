package tbx2rdfservice.store;

import com.hp.hpl.jena.query.DatasetAccessor;
import com.hp.hpl.jena.query.DatasetAccessorFactory;
import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.rdf.model.Literal;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.sparql.core.DatasetGraph;
import com.hp.hpl.jena.update.UpdateExecutionFactory;
import com.hp.hpl.jena.update.UpdateFactory;
import com.hp.hpl.jena.update.UpdateProcessor;
import com.hp.hpl.jena.update.UpdateRequest;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import static org.apache.commons.lang.StringEscapeUtils.escapeHtml;
import org.apache.jena.atlas.lib.FileOps;
import org.apache.jena.fuseki.EmbeddedFusekiServer;
import org.apache.jena.fuseki.server.FusekiConfig;
import org.apache.jena.fuseki.server.ServerConfig;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFDataMgr;
import tbx2rdfservice.TBX2RDFServiceConfig;
import tbx2rdfservice.servlets.ServletLogger;

/**
 * Interface for different RDFStores
 *
 * @author admin
 */
public class RDFStoreFuseki {

//    private static EmbeddedFusekiServer fuseki = null;
    public static void init() {
        /*
         if (fuseki == null) {
         try {
         System.out.println("Launching fuseki server");
         DatasetGraph dsg = TDBFactory.createDatasetGraph("data");
         fuseki = RDFStoreFuseki.create(3030, dsg, "tbx");
         fuseki.start();
         } catch (Exception e) {
         System.err.println("Could not start fuseki " + e.getMessage());
         }
         }*/
    }

    public static boolean test() {
        return false;
    }

    /**
     * Returns an entity given the ID. All the triples whosse subject is the one given.
     */
    public static String getEntity(String resource) {
        init();
        String sresults = "";

        try {
            String sparql = "SELECT DISTINCT *\n"
                    + "WHERE {\n"
                    + "  GRAPH ?g {\n"
                    + "    <" + resource + "> ?p ?o\n"
                    + "  }\n"
                    + "} LIMIT 1000";
            Query query = QueryFactory.create(sparql);
            String endpoint = "http://localhost:3031/tbx/query";
            QueryExecution qexec = QueryExecutionFactory.sparqlService(endpoint, query);
            ResultSet results = qexec.execSelect();
            for (; results.hasNext();) {
                QuerySolution soln = results.nextSolution();
                Resource p = soln.getResource("p");       // Get a result variable by name.
                RDFNode o = soln.get("o"); // Get a result variable - must be a resource
                String so = "";
                if (o.isLiteral()) {
                    Literal l = o.asLiteral();
                    String cadena = l.getLexicalForm();
                    cadena = RDFUtil.escapeNTriples(cadena);
                    so = "\"" + cadena + "\"";
                    String lan = l.getLanguage();
                    if (!lan.isEmpty()) {
                        so += "@" + lan;
                    }
                } else {
                    so = "<" + o.toString() + ">";
                }
                sresults += "<" + resource + "> <" + p.toString() + "> " + so + " . \n";
            }
        } catch (Exception e) {
            try {
                PrintWriter archivo = new PrintWriter(TBX2RDFServiceConfig.get("logsfolder", ".") + "/error.txt");
                archivo.println(e.getMessage());
                archivo.close();
            } catch (Exception ex) {
            }

        }
        return sresults;
    }

    public static boolean deleteGraph(String graph) {
        init();
        String endpoint = "http://localhost:3031/tbx/update";
        UpdateRequest request = UpdateFactory.create();
        request.add("DROP GRAPH <" + graph + ">");
        UpdateProcessor qexec = UpdateExecutionFactory.createRemoteForm(request, endpoint);
        qexec.execute();
        return true;
    }

    public static boolean postEntity(String id, String rdf) {
        return postEntity(id, rdf, Lang.TTL);
    }

    /**
     * @param id Nombre del namedgraph donde se ponen los datos.
     * @param rdf String with valid RDF as segun lo que aparece en lan
     * (Lang.TTL)
     * @param lan Lang.TTL, Lang.NTRIPLES
     */
    public static boolean postEntity(String id, String rdf, org.apache.jena.riot.Lang lan) {
        try {
            init();
            ServletLogger.global.log("RDFStoreFuseki: We have been posted for the named graph: <pre><code>" + id + " </code></pre>the follwoing rdf:<br><pre><code>" + escapeHtml(rdf) + "</code></pre>");
            String endpoint = "http://localhost:3031/tbx/data";
            DatasetAccessor dataAccessor = DatasetAccessorFactory.createHTTP(endpoint);
            Model model = ModelFactory.createDefaultModel();
            InputStream stream = new ByteArrayInputStream(rdf.getBytes("UTF-8"));
            RDFDataMgr.read(model, stream, lan);
            if (id != null) {
                dataAccessor.putModel(id, model); //gameid

                Model model2 = dataAccessor.getModel(id);

                StringWriter sw = new StringWriter();
                RDFDataMgr.write(sw, model2, Lang.NT);
                ServletLogger.global.log("SE HA RECIBIDO: <pre><code>" + escapeHtml(sw.toString()) + "</code></pre>");

            } else {
                dataAccessor.putModel(model);
            }
            return true;
        } catch (Exception e) {
            ServletLogger.global.log("RDFStoreFuseki: error " + e.getMessage());
            return false;
        }
    }

    public static boolean deleteEntity(String id, String rdf) {
        return false;
    }

    public static void deleteAll() {
        init();
        String endpoint = "http://localhost:3031/tbx/update";
        UpdateRequest request = UpdateFactory.create();
        request.add("DROP ALL");
        UpdateProcessor qexec = UpdateExecutionFactory.createRemoteForm(request, endpoint);
        qexec.execute();
    }

    public static int countTriples() {
        init();
        List<String> uris = new ArrayList();
        String aux = "?o";
        String sparql = "SELECT (COUNT(*) AS ?count) "
                + "WHERE {\n"
                + "  GRAPH ?g {\n"
                + "    ?s ?p ?o \n"
                + "  }\n"
                + "} ";
        Query query = QueryFactory.create(sparql);
        String endpoint = "http://localhost:3031/tbx/query";
        QueryExecution qexec = QueryExecutionFactory.sparqlService(endpoint, query);
        try {
            ResultSet results = qexec.execSelect();
            for (; results.hasNext();) {
                QuerySolution soln = results.nextSolution();
                Iterator<String> it = soln.varNames();
                while (it.hasNext()) {
                    String col = it.next();
                    Literal literal = soln.getLiteral(col);
                    return Integer.parseInt(literal.getLexicalForm());
                }
            }
        } catch (Exception e) {
            System.err.println("NO se pudo contar conceptos en fuseki");
        }
        return -1;
    }

    /**
     *
     * @param type Type of entity to be counted or null if all the entities are
     * to be returned. Empty for all kind of entities.
     * @return -1 if error
     */
    public static int countEntities(String type) {
        init();
        List<String> uris = new ArrayList();
        String aux = "?o";
        if (!type.isEmpty()) {
            aux = "<" + type + ">";
        }
        String sparql = "SELECT (COUNT(DISTINCT ?g) AS ?count) "
                + "WHERE {\n"
                + "  GRAPH ?g {\n"
                + "    ?s <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> " + aux + "\n"
                + "  }\n"
                + "} ";
        Query query = QueryFactory.create(sparql);
        String endpoint = "http://localhost:3031/tbx/query";
        QueryExecution qexec = QueryExecutionFactory.sparqlService(endpoint, query);
        try {
            ResultSet results = qexec.execSelect();
            for (; results.hasNext();) {
                QuerySolution soln = results.nextSolution();
                Iterator<String> it = soln.varNames();
                while (it.hasNext()) {
                    String col = it.next();
                    Literal literal = soln.getLiteral(col);
                    return Integer.parseInt(literal.getLexicalForm());
                }
            }
        } catch (Exception e) {
            System.err.println("NO se pudo contar conceptos en fuseki");
        }
        return -1;
    }

    public static EmbeddedFusekiServer create(int port, DatasetGraph dsg, String datasetPath) {
        ServerConfig conf = FusekiConfig.defaultConfiguration(datasetPath, dsg, true, true);
        conf.port = port;
        conf.pagesPort = port;
        if (!FileOps.exists(conf.pages)) {
            conf.pages = null;
        }
        return new EmbeddedFusekiServer(conf);
    }

    public static String dumpdefaultgraph(String format) {
        init();

        String sparql = "SELECT * \n"
                + "WHERE {\n"
                + "  ?s ?p ?o .\n"
                + "}";
        Query query = QueryFactory.create(sparql);
        String endpoint = "http://localhost:3031/tbx/query";
        QueryExecution qexec = QueryExecutionFactory.sparqlService(endpoint, query);
        ResultSet results = qexec.execSelect();
        String sresults = "";
        for (; results.hasNext();) {
            QuerySolution soln = results.nextSolution();
            Resource s = soln.getResource("s");       // Get a result variable by name.
            Resource p = soln.getResource("p");       // Get a result variable by name.
            RDFNode o = soln.get("o"); // Get a result variable - must be a resource
            String so = "";
            if (o.isLiteral()) {
                Literal l = o.asLiteral();
                String cadena = l.getLexicalForm();
                cadena = RDFUtil.escapeNTriples(cadena);

                so = "\"" + cadena + "\"";
                String lan = l.getLanguage();
                if (!lan.isEmpty()) {
                    so += "@" + lan;
                }
            } else {
                so = "<" + o.toString() + ">";
            }
            sresults += "<" + s + "> <" + p.toString() + "> " + so + " . \n";
        }
        InputStream is = new ByteArrayInputStream(sresults.getBytes(StandardCharsets.UTF_8));
        Model model = ModelFactory.createDefaultModel();
        RDFDataMgr.read(model, is, Lang.NT);
        RDFPrefixes.addPrefixesIfNeeded(model);
        StringWriter sw = new StringWriter();
        if (format.equals("nt")) {
            RDFDataMgr.write(sw, model, Lang.NT);
        } else {
            RDFDataMgr.write(sw, model, Lang.TTL);
        }
        return sw.toString();
    }

    /**
     * hace un dump para todos los grafos.
     */
    public static String dump(String format) {
        init();
        String sparql = "SELECT DISTINCT *\n"
                + "WHERE {\n"
                + "  GRAPH ?g {\n"
                + "    ?s ?p ?o\n"
                + "  }\n"
                + "} LIMIT 100000";
        Query query = QueryFactory.create(sparql);
        String endpoint = "http://localhost:3031/tbx/query";
        QueryExecution qexec = QueryExecutionFactory.sparqlService(endpoint, query);
        ResultSet results = qexec.execSelect();
        String sresults = "";
        for (; results.hasNext();) {
            QuerySolution soln = results.nextSolution();
            String newline = RDFUtil.createNtripleFromSparql(soln);
            sresults += newline;
        }
        ServletLogger.global.log("Dump: <pre><code>" + escapeHtml(sresults) + "</code></pre>");
        InputStream is = new ByteArrayInputStream(sresults.getBytes(StandardCharsets.UTF_8));
        Model model = ModelFactory.createDefaultModel();
        RDFDataMgr.read(model, is, Lang.NT);
        RDFPrefixes.addPrefixesIfNeeded(model);
        StringWriter sw = new StringWriter();

        if (format.equals("ttl") || format.isEmpty()) {
            RDFDataMgr.write(sw, model, Lang.TTL);
        }
        if (format.equals("nt")) {
            RDFDataMgr.write(sw, model, Lang.NT);
        }
        return sw.toString();
    }

    public static List<String> getNarrower(String res) {
        List<String> uris = new ArrayList();
        String sparql = "SELECT DISTINCT ?s\n"
                + "WHERE {\n"
                + "  GRAPH ?g {\n"
                + "    ?s <http://www.w3.org/2004/02/skos/core#narrower> <" + res + ">\n"
                + "  }\n"
                + "} ";

        try {
            PrintWriter archivo = new PrintWriter(TBX2RDFServiceConfig.get("logsfolder", ".") + "/query.txt");
            archivo.println(sparql);
            archivo.close();
        } catch (Exception ex) {
        }
        Query query = QueryFactory.create(sparql);
        String endpoint = "http://localhost:3031/tbx/query";
        QueryExecution qexec = QueryExecutionFactory.sparqlService(endpoint, query);
        ResultSet results = qexec.execSelect();
        for (; results.hasNext();) {
            QuerySolution soln = results.nextSolution();
            Resource p = soln.getResource("s");       // Get a result variable by name.
            uris.add(p.toString());
        }
        return uris;
    }

    public static List<String> listConcepts(int offset, int limit, String searchConcept) {
        List<String> uris = new ArrayList();
        String sparql = "SELECT DISTINCT ?s\n"
                + "WHERE {\n"
                + "  GRAPH ?g {\n"
                + "    ?s a <http://www.w3.org/2004/02/skos/core#Concept> \n";
                if (searchConcept != null && !searchConcept.isEmpty()) {
                    sparql += "FILTER regex(str(?s),'" + searchConcept + "','i') \n";
                }
        sparql += "  }\n"
                + "} ";
        sparql += " OFFSET " + offset + "\n";
        sparql += " LIMIT " + limit + "\n";

        //INIT LOGGER
        try {
            PrintWriter archivo = new PrintWriter(TBX2RDFServiceConfig.get("logsfolder", ".") + "/query.txt");
            archivo.println(sparql);
            archivo.close();
        } catch (Exception ex) {
        }
        //END LOGGER

        Query query = QueryFactory.create(sparql);
        String endpoint = "http://localhost:3031/tbx/query";
        QueryExecution qexec = QueryExecutionFactory.sparqlService(endpoint, query);
        ResultSet results = qexec.execSelect();
        for (; results.hasNext();) {
            QuerySolution soln = results.nextSolution();
            Resource p = soln.getResource("s");       // Get a result variable by name.
            uris.add(p.toString());
        }
        return uris;
    }

    public static List<String> listGraphs() {
        List<String> uris = new ArrayList();
        String sparql = "SELECT DISTINCT ?g\n"
                + "WHERE {\n"
                + "  GRAPH ?g {\n"
                + "    ?s ?p ?o\n"
                + "  }\n"
                + "} ";
        try {
            PrintWriter archivo = new PrintWriter(TBX2RDFServiceConfig.get("logsfolder", ".") + "/query.txt");
            archivo.println(sparql);
            archivo.close();
        } catch (Exception ex) {
        }
        Query query = QueryFactory.create(sparql);
        String endpoint = "http://localhost:3031/tbx/query";
        QueryExecution qexec = QueryExecutionFactory.sparqlService(endpoint, query);
        ResultSet results = qexec.execSelect();
        for (; results.hasNext();) {
            QuerySolution soln = results.nextSolution();
            Resource p = soln.getResource("g");       // Get a result variable by name.
            uris.add(p.toString());
        }
        return uris;
    }

    public static String loadGraph(String uri) {
        init();
        String sresults = "";
        try {
            String sparql = "SELECT DISTINCT *\n"
                    + "WHERE {\n"
                    + "  GRAPH <" + uri + "> {\n"
                    + "    ?s ?p ?o\n"
                    + "  }\n"
                    + "} LIMIT 1000";
            Query query = QueryFactory.create(sparql);
            String endpoint = "http://localhost:3031/tbx/query";
            QueryExecution qexec = QueryExecutionFactory.sparqlService(endpoint, query);
            ResultSet results = qexec.execSelect();
            for (; results.hasNext();) {
                QuerySolution soln = results.nextSolution();
                String newline = RDFUtil.createNtripleFromSparql(soln);
                sresults += newline;
            }
        } catch (Exception e) {
            try {
                PrintWriter archivo = new PrintWriter(TBX2RDFServiceConfig.get("logsfolder", ".") + "/error.txt");
                archivo.println(e.getMessage());
                archivo.close();
            } catch (Exception ex) {
            }

        }
        return sresults;
    }

    public static String updateSPARQL(String sparql) {
        init();
        String res = "";
        String endpoint = "http://localhost:3031/tbx/update";
        UpdateRequest request = UpdateFactory.create();
        request.add(sparql);
        UpdateProcessor qexec = UpdateExecutionFactory.createRemoteForm(request, endpoint);
        qexec.execute();
        return res;
    }

    public static String selectSPARQL(String sparql) {
        init();
        String res = "";
        Query query = QueryFactory.create(sparql);
        String endpoint = "http://localhost:3031/tbx/query";
        QueryExecution qexec = QueryExecutionFactory.sparqlService(endpoint, query);
        ResultSet results = qexec.execSelect();
        for (; results.hasNext();) {
            QuerySolution soln = results.nextSolution();
            res += soln.toString() + "<br>\n";
        }
        return res;
    }

    public static void main(String[] args) {
        String sparql = "SELECT * WHERE {?s ?p ?o} LIMIT 100.";
        String res = getEntity("http://tbx2rdf.lider-project.eu/converter/resource/cc/collective%20work");
//        String res = selectSPARQL(sparql);
        System.out.println(res);
    }

}
