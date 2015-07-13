package tbx2rdfservice.command;

import com.hp.hpl.jena.sparql.core.DatasetGraph;
import com.hp.hpl.jena.tdb.TDBFactory;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import org.apache.commons.io.FileUtils;
import org.apache.jena.riot.Lang;
import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Logger;
import org.apache.log4j.varia.NullAppender;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import javax.xml.parsers.DocumentBuilderFactory;
import org.apache.jena.riot.Lang;
import org.apache.log4j.BasicConfigurator;
import org.apache.commons.cli.BasicParser;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.Option;
import org.apache.commons.io.FileUtils;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.HttpClients;
import org.apache.jena.atlas.lib.FileOps;
import org.apache.jena.fuseki.EmbeddedFusekiServer;
import org.apache.jena.fuseki.server.FusekiConfig;
import org.apache.jena.fuseki.server.ServerConfig;
import org.apache.log4j.Logger;
import org.apache.log4j.varia.NullAppender;
import tbx2rdfservice.store.RDFPrefixes;
import tbx2rdfservice.store.RDFStoreFuseki;


/**
 * Main entry point to manage the web application. 
 * The war file is expected to be deployed as tbx2rdf.war AND NOT AS TBX2RDFService. After compile please rename
 * The service is expected to be given under http://tbx2rdf.lider-project.eu/converter/tbx2rdf in lider2
 * @author admin
 */
public class Main {
    
    public static void main(String[] argx) throws IOException {

//        String[] args = {"-count","foaf:Agent"};
        String[] args = {"-dump"};
        
        BasicConfigurator.configure();
        Logger.getRootLogger().removeAllAppenders();
        Logger.getRootLogger().addAppender(new NullAppender());
        StringBuilder sb = new StringBuilder();
        CommandLineParser clparser = null;
        CommandLine cl = null;
        try {
            Options options = new Options();
            options.addOption("countall", false, "Counts the number of class individuals");
            options.addOption("count", true, "Counts the number of class individuals of a given class");
            options.addOption("dump", false, "Dumps all the RDF in the server");
            options.addOption("clear", false, "Deletes everything");
            options.addOption("help", false, "shows this help (Help)");
            
            
            clparser = new BasicParser();
            cl = clparser.parse(options, args);
            if (cl.hasOption("help")) {
                System.out.println("tbx2rdfservice command line tool");
                new HelpFormatter().printHelp(Main.class.getCanonicalName(), options);
            }
            if (cl.hasOption("clear")) {
                RDFStoreFuseki.deleteAll();
                System.out.println("ok");
            }
            if (cl.hasOption("dump")) {
                String ttl = RDFStoreFuseki.dump();
                System.out.println(ttl);
                System.out.println("ok");
            }
            if (cl.hasOption("count")) {
                String param = cl.getOptionValue("count");
                param=RDFPrefixes.extend(param);
                int total =RDFStoreFuseki.countEntities(param);
                System.out.println(total);
            }
            if (cl.hasOption("countall")) {
                int total =RDFStoreFuseki.countEntities("");
                System.out.println(total);
            }

        } catch (Exception e) {
            System.err.println("error " + e.getMessage());
        }

    }    
}
