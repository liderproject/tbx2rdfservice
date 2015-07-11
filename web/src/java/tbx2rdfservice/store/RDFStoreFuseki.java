package tbx2rdfservice.store;

import com.hp.hpl.jena.query.DatasetAccessor;
import com.hp.hpl.jena.query.DatasetAccessorFactory;
import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.sparql.core.DatasetGraph;
import com.hp.hpl.jena.tdb.TDBFactory;
import com.hp.hpl.jena.update.UpdateExecutionFactory;
import com.hp.hpl.jena.update.UpdateFactory;
import com.hp.hpl.jena.update.UpdateProcessor;
import com.hp.hpl.jena.update.UpdateRequest;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import org.apache.jena.atlas.lib.FileOps;
import org.apache.jena.fuseki.EmbeddedFusekiServer;
import org.apache.jena.fuseki.server.FusekiConfig;
import org.apache.jena.fuseki.server.ServerConfig;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFDataMgr;
import tbx2rdfservice.command.Main;

/**
 * Interface for different RDFStores
 * @author admin
 */
public class RDFStoreFuseki {

    private static EmbeddedFusekiServer fuseki = null;
    
    public static boolean test()
    {
        if (fuseki==null)
        {
            DatasetGraph dsg = TDBFactory.createDatasetGraph("data") ;
            fuseki = RDFStoreFuseki.create(3030, dsg, "tbx");
            System.out.println("Created fuseki");
            fuseki.start();
            System.out.println("Started fuseki");
            
        }
        return false;
    }
    
    /**
     * Returns an entity given the ID
     */
    public static String getEntity(String id)
    {
        return "";
    }
    
    public static boolean deleteGraph(String graph)
    { 
        String endpoint = "http://localhost:3030/tbx/update";
        UpdateRequest request = UpdateFactory.create() ;
        request.add("DROP GRAPH <"+graph+">");      
        UpdateProcessor qexec=UpdateExecutionFactory.createRemoteForm(request,endpoint);
        qexec.execute();
        return true;
    }
    
    
    /**
     * @param rdf String with valid RDF as turtle
     */
    public static boolean postEntity(String id, String rdf)
    {
        try {
            String endpoint = "http://localhost:3030/tbx/data";
            DatasetAccessor dataAccessor = DatasetAccessorFactory.createHTTP(endpoint);
            Model model = ModelFactory.createDefaultModel();
            InputStream stream = new ByteArrayInputStream(rdf.getBytes("UTF-8"));
            RDFDataMgr.read(model, stream, Lang.TTL);
            dataAccessor.putModel("", model); //gameid
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }        
    }

    public static boolean deleteEntity(String id, String rdf)
    {
        return false;
    }
    
    /**
     * @param type Type of entity to be counted or null if all the entities are to be returned.
     */
    public static int countEntities(String type) {
        int offset=0;
        int limit=10;
        List<String> uris = new ArrayList();
        String sparql = "SELECT DISTINCT ?s\n"
                + "WHERE {\n"
                + "  GRAPH ?g {\n"
                + "    ?s ?p ?o\n"
                + "  }\n"
                + "} ";
        sparql += " OFFSET " + offset +"\n";
        sparql += " LIMIT " + limit +"\n";
        Query query = QueryFactory.create(sparql);
        String endpoint = "http://localhost:3030/tbx/query";
        QueryExecution qexec = QueryExecutionFactory.sparqlService(endpoint, query);
        ResultSet results = qexec.execSelect();
        int conta=0;
        for (; results.hasNext();) {
            QuerySolution soln = results.nextSolution();
            Resource p = soln.getResource("s");       // Get a result variable by name.
            uris.add(p.toString());
//            System.out.println(p.toString());
            conta++;
        }                
        return conta;
    }
    
    public static EmbeddedFusekiServer create(int port, DatasetGraph dsg, String datasetPath) {
        ServerConfig conf = FusekiConfig.defaultConfiguration(datasetPath, dsg, true, true) ;
        conf.port = port ;
        conf.pagesPort = port ;
        if ( ! FileOps.exists(conf.pages) )
            conf.pages = null ;
        return new EmbeddedFusekiServer(conf) ;
    }    
        
    
}
