package tbx2rdfservice.servlets;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import tbx2rdfservice.TBX2RDFServiceConfig;

/**
 * Makes logging in a servlet environment
 *
 * @author vrodriguez
 */
public class ServletLogger {

    PrintWriter archivo = null;

    public static ServletLogger global = new ServletLogger();

    public ServletLogger() {
        try {
            String filename = getFilename();
            archivo = new PrintWriter(filename);
            Date date = new Date();
            java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            log("TBX2RDF SERVER started on " + sdf.format(date));
        } catch (Exception e) {
            archivo = null;
        }
    }

    public String getFilename() {
        String lf = TBX2RDFServiceConfig.get("logsfolder", ".");
        File f = new File(lf);
        if (!(f.exists())) {
            lf = ".";
        }
        String filename = lf + "/get.txt";
        return filename;
    }

    public void log(String s) {
        if (archivo == null) {
            return;
        }
        try {
            String txt = "";
            Date date = new Date();
            java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            txt = sdf.format(date) + " " + s;
            archivo.println(txt);
            archivo.flush();
        } catch (Exception e) {
        }
    }

    /**
     * Gets the last n files of the text document.
     */
    public String tail(int n) {
        String filename = getFilename();
        try {
            FileInputStream in = new FileInputStream(filename);
            BufferedReader br = new BufferedReader(new InputStreamReader(in));
            List<String> lines = new LinkedList<String>();
            for (String tmp; (tmp = br.readLine()) != null;) {
                if (lines.add(tmp) && lines.size() > n) {
                    lines.remove(0);
                }
            }
            String o ="";
            for(String s :lines)
                o+=s+"\n";
            return o;
        } catch (Exception e) {

        }
        return "";
    }

}
