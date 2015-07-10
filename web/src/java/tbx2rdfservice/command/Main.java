package tbx2rdfservice.command;

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
import org.apache.log4j.Logger;
import org.apache.log4j.varia.NullAppender;
import tbx2rdfservice.store.RDFStore;

/**
 * Main entry point to manage the web application
 * @author admin
 */
public class Main {
    public static void main(String[] args) throws IOException {
        BasicConfigurator.configure();
        Logger.getRootLogger().removeAllAppenders();
        Logger.getRootLogger().addAppender(new NullAppender());

        StringBuilder sb = new StringBuilder();
        CommandLineParser clparser = null;
        CommandLine cl = null;
        try {
            Options options = new Options();
            options.addOption("count", false, "Counts the number of terms");
            clparser = new BasicParser();
            cl = clparser.parse(options, args);
            if (cl.hasOption("help")) {
                System.out.println("RDFChess command line tool");
                new HelpFormatter().printHelp(Main.class.getCanonicalName(), options);
            }
            if (cl.hasOption("count")) {
                int total =RDFStore.countGames();
                System.out.println(total);
            }
            if (cl.hasOption("add")) {
                String sfile = cl.getOptionValue("add");
            }
        } catch (Exception e) {
            System.err.println("error");
        }

    }    
}
