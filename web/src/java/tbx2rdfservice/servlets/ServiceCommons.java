package tbx2rdfservice.servlets;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import javax.servlet.http.HttpServletRequest;

/**
 *
 * @author vrodriguez
 */
public class ServiceCommons {

    /**
     * *
     * Obtains the body from a HTTP request. Period. No more, no less.
     */
    public static String getBody(HttpServletRequest request) throws IOException {

        String body = null;
        StringBuilder stringBuilder = new StringBuilder();
        BufferedReader bufferedReader = null;
        try {
            InputStream inputStream = request.getInputStream();
            if (inputStream != null) {
                bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
                char[] charBuffer = new char[128];
                int bytesRead = -1;
                while ((bytesRead = bufferedReader.read(charBuffer)) > 0) {
                    stringBuilder.append(charBuffer, 0, bytesRead);
                }
            } else {
                stringBuilder.append("");
            }
        } catch (IOException ex) {
            throw ex;
        } finally {
            if (bufferedReader != null) {
                try {
                    bufferedReader.close();
                } catch (IOException ex) {
                    throw ex;
                }
            }
        }
        body = stringBuilder.toString();
        return body;
    }

    /**
     * Ejemplo de uri: http://tbx2rdf.lider-project.eu/converter/service/service/upload o http://lider2.dia.fi.upm.es:8080/tbx2rdf/service/upload
     */
    public static String getHTMLPostForm(String uri) {
        String uri2= "http://tbx2rdf.lider-project.eu/converter/service/service/service/selectSPARQL";
        String uri3= "http://tbx2rdf.lider-project.eu/converter/service/service/service/updateSPARQL";
        String html = "<center>\n"
                + "<textarea rows=\"10\" cols=\"120\" id=\"tatriples\">\n"
                + "<http://one.example/subject1> <http://one.example/predicate1> <http://one.example/object1> .\n"
                + "</textarea>\n"
                + "<button onclick=\"dopost('"+uri+"')\">Upload ntriples</button>\n"
                + "<button onclick=\"dopost('"+uri2+"')\">SPARQL select</button>\n"
                + "<button onclick=\"dopost('"+uri3+"')\">SPARQL update</button>\n"
                + "</center>\n"
                + "<script>\n"
                + "\n"
                + "function dopost(url) {\n"
                + " var method = \"POST\";\n"
                + " var postData = document.getElementById(\"tatriples\").value;\n"
                + " var async = true;\n"
                + " var request = new XMLHttpRequest();\n"
                + " request.onload = function () {\n"
                + "    console.log(postData);\n"
                + "    var status = 200; \n"
                + " }\n"
                + " request.open(method, url, async);\n"
                + " request.setRequestHeader(\"Content-Type\", \"text/plain;charset=UTF-8\");\n"
                + " request.send(postData);\n" 
                + "}\n"
                + "</script>";

        return html;
    }

}
