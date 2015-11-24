package tbx2rdfservice.servlets;

import java.io.File;
import java.io.PrintWriter;
import tbx2rdfservice.TBX2RDFServiceConfig;

/**
 * Makes logging in a servlet environment
 * @author vrodriguez
 */
public class ServletLogger {

    PrintWriter archivo = null;

    public ServletLogger() {
        try {
            String lf = TBX2RDFServiceConfig.get("logsfolder", ".");
            File f = new File(lf);
            if (!(f.exists())) {
                lf = ".";
            }
            archivo = new PrintWriter(lf + "/get.txt");
        } catch (Exception e) {
            archivo = null;
        }
    }

    public void log(String s) {
        if (archivo == null) {
            return;
        }
        try {
            archivo.println(s);
            archivo.flush();
        } catch (Exception e) {

        }
    }

}
