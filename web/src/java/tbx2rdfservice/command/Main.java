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

    public static void test()
    {
        RDFStoreFuseki f = new RDFStoreFuseki();
        String ttl=RDFPrefixes.getSampleTTL();
        RDFStoreFuseki.test();
        RDFStoreFuseki.deleteGraph("");
      //  RDFStoreFuseki.postEntity("", ttl);
        int g=RDFStoreFuseki.countEntities("");
        System.out.println(g);
    }
    

    
    public static void main(String[] argX) throws IOException {
        
        test();
        if(true)
        return;
        String[] args = {"-help"};
        
        BasicConfigurator.configure();
        Logger.getRootLogger().removeAllAppenders();
        Logger.getRootLogger().addAppender(new NullAppender());
        StringBuilder sb = new StringBuilder();
        CommandLineParser clparser = null;
        CommandLine cl = null;
        try {
            Options options = new Options();
            options.addOption("count", false, "Counts the number of terms");
            options.addOption("help", false, "shows this help (Help)");
            
            
            clparser = new BasicParser();
            cl = clparser.parse(options, args);
            if (cl.hasOption("help")) {
                System.out.println("RDFChess command line tool");
                new HelpFormatter().printHelp(Main.class.getCanonicalName(), options);
            }
            if (cl.hasOption("count")) {
                int total =RDFStoreFuseki.countEntities("");
                System.out.println(total);
            }
            if (cl.hasOption("add")) {
                String sfile = cl.getOptionValue("add");
            }
        } catch (Exception e) {
            System.err.println("error " + e.getMessage());
        }

    }    
}
