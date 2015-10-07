package tbx2rdfservice.command;

import java.io.File;
import java.io.IOException;

import org.apache.log4j.BasicConfigurator;
import org.apache.commons.cli.BasicParser;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.log4j.Logger;
import org.apache.log4j.varia.NullAppender;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.webapp.WebAppContext;

import tbx2rdfservice.TBX2RDFServiceConfig;
import tbx2rdfservice.store.RDFPrefixes;
import tbx2rdfservice.store.RDFStoreFuseki;


/**
 * Main entry point to manage the web application. 
 * The war file is expected to be deployed as tbx2rdf.war AND NOT AS TBX2RDFService. After compile please rename
 * The service is expected to be given under http://tbx2rdf.lider-project.eu/converter/tbx2rdf in lider2
 * 
 * TO DEPLOY THE WEB SERVICE IN LIDER2
 * - Compile this project in local
 * - Go to http://lider2.dia.fi.upm.es:8080/manager (admin, I-DONT-REMEMBER-PASSWORD)
 * - Redeploy the old version and deploy the war file generated before
 * 
 * TO LAUNCH FUSEKI:
 * - go to lider2, opt/fuseki
 * - execute sudo nohup java -jar ./fuseki-server.jar --update --loc=data --port 3031 /tbx&
 * 
 * 
 * Para buscar el proceso que ocupa el fuseki:
 * lsof -i :3031
 * kill -9 LOQUESALGANANTES
 * cd /opt/fuseki/apache-jena-fuseki-2.0.0
 * 
 * 
 * @author admin
 */
public class Main {
    
    
    
    public static void main(String[] argx) throws IOException {

//        String[] args = {"-count","foaf:Agent"};
//        String[] args = {"-dump"};
        String[] args = {"-jetty"};
        
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
            options.addOption("jetty", false, "Starts the application in a Jetty server");
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
            if (cl.hasOption("jetty")) {
                startJetty();
            }

        } catch (Exception e) {
            System.err.println("error " + e.getMessage());
        }

    }    
    
    public static void startJetty() throws Exception
    {
        String jetty_home = System.getProperty("jetty.home",".");
        String war = jetty_home+"/dist/tbx2rdf.war";
        File f = new File(war);
        if (!f.exists())
            System.out.println("error");
        Server server = new Server(8080);
        WebAppContext webapp = new WebAppContext();
        String context = TBX2RDFServiceConfig.get("context", "/tbx2rdf");
        webapp.setContextPath(context);
        webapp.setWar(war);
        server.setHandler(webapp);
        server.start();
        server.join();
    }
    
}
