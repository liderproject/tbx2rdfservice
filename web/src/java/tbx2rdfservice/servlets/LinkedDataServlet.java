package tbx2rdfservice.servlets;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.NodeIterator;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.vocabulary.RDF;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Enumeration;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFDataMgr;
import tbx2rdfservice.TBX2RDFServiceConfig;
import tbx2rdfservice.store.RDFPrefixes;
import tbx2rdfservice.store.RDFStoreFuseki;

/**
 *
 * @author admin
 */
public class LinkedDataServlet extends HttpServlet {

    /**
     * Processes requests for both HTTP <code>GET</code> and <code>POST</code>
     * methods.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

    }

    /**
     * Handles the HTTP <code>GET</code> method. // input:
     * http://tbx2rdf.lider-project.eu/converter/resource/iate/IATE-84 //
     * peticion: --> /tbx2rdf/resource/iate/IATE-84
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String peticion = request.getRequestURI();
        if (peticion.endsWith("/resource/")) {                               //SERVING THE LIST OF resources
            System.out.println("Serving HTML for resources");
            response.setContentType("text/html;charset=UTF-8");
            InputStream is1 = LinkedDataServlet.class.getResourceAsStream("../../../../ld.html");
            BufferedReader reader = new BufferedReader(new InputStreamReader(is1));
            StringBuilder outx = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                outx.append(line);
            }
            String body = outx.toString();
            body = body.replace("<!--TEMPLATE_TITLE-->", "\n" + "List of terms");
            String tabla ="<table id=\"grid-data\" class=\"table table-condensed table-hover table-striped\">\n" +
"        <thead>\n" +
"                <tr>\n" +
"                        <th data-column-id=\"resource\" data-formatter=\"link\" data-order=\"desc\">Terms</th>\n" +
"                </tr>\n" +
"        </thead>\n" +
"</table>	\n" +
"";
            body = body.replace("<!--TEMPLATE_PGN-->", "<br>" + tabla);
            response.getWriter().println(body);
            response.setStatus(HttpServletResponse.SC_OK);
            return;            
        }        
        
        
        String id = request.getRequestURI().replace("/tbx2rdf/resource/", "");
        System.out.println(peticion + " " + id);
        PrintWriter archivo = new PrintWriter(TBX2RDFServiceConfig.get("logsfolder", ".") + "/get.txt");
        archivo.println("requestURI: " + peticion);
        String base = TBX2RDFServiceConfig.get("datauri", "http://tbx2rdf.lider-project.eu/converter/resource/iate/");
        String xid = peticion.replace("/tbx2rdf/resource/iate/", "");
        String recurso = base + xid;
        System.out.println("I have been getted for this resource: " + recurso);
        archivo.println("\nrecurso: " + recurso);
        String nt = RDFStoreFuseki.getEntity(recurso);
        if (nt.isEmpty())
        {
            Tbx2rdfServlet.serveError(request, response);
            return;
        }
        archivo.println("\ntriples: " + nt);
        archivo.close();
        if (isRDFTTL(request)) {
            System.out.println("Serving TTL for " + recurso);
            Model model = ModelFactory.createDefaultModel();
            InputStream is = new ByteArrayInputStream(nt.getBytes(StandardCharsets.UTF_8));
            RDFDataMgr.read(model, is, Lang.NT);
            StringWriter sw = new StringWriter();
            RDFDataMgr.write(sw, model, Lang.TTL);
            response.getWriter().println(sw);
            response.setContentType("text/turtle;charset=UTF-8");
        } else {
            response.setContentType("text/html;charset=UTF-8");
            
            InputStream is1 = LinkedDataServlet.class.getResourceAsStream("../../../../ld.html");
            BufferedReader reader = new BufferedReader(new InputStreamReader(is1, "UTF-8"));
            StringBuilder outx = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                outx.append(line);
            }
            String body = outx.toString();            
            
            Model model = ModelFactory.createDefaultModel();
            InputStream is = new ByteArrayInputStream(nt.getBytes(StandardCharsets.UTF_8));
            RDFDataMgr.read(model, is, Lang.NT);
            model = RDFPrefixes.addPrefixesIfNeeded(model);
            
            Resource entidad = ModelFactory.createDefaultModel().createResource(recurso);
            String titulo = entidad.getLocalName();
            titulo = URLDecoder.decode(titulo, "UTF-8");
            StringWriter sw = new StringWriter();
            RDFDataMgr.write(sw, model, Lang.TTL);
            response.setCharacterEncoding("UTF-8");
            System.out.println("Serving HTML for " + recurso);
            String ttl2 = StringEscapeUtils.escapeHtml4(sw.toString());
            
            try (PrintWriter out = response.getWriter()) {
                body = body.replace("<!--TEMPLATE_TITLE-->", "\n" + titulo);
                body = body.replace("<!--TEMPLATE_TTL-->", "<br>" + ttl2);
//                response.getWriter().println(body);                
                out.println(body);
            } catch (Exception e) {

            }

        }
        response.setStatus(HttpServletResponse.SC_OK);
        

    }
    
    @Override
    protected void doDelete(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String peticion = request.getRequestURI();
        String base = TBX2RDFServiceConfig.get("datauri", "http://tbx2rdf.lider-project.eu/converter/resource/iate/");
        String xid = peticion.replace("/tbx2rdf/resource/iate/", "");
        String recurso = base + xid;                
        RDFStoreFuseki.deleteGraph(recurso);
        response.setStatus(HttpServletResponse.SC_OK);
//        response.sendRedirect("/");
    }
    
    

    /**
     * Handles the HTTP <code>POST</code> method.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String peticion = request.getRequestURI();
        BufferedReader br = request.getReader();
        String s = "";
        String tot = "";
        while ((s = br.readLine()) != null) {
            tot = tot + s + "";
        }
        tot = java.net.URLDecoder.decode(tot, "UTF-8");
        try {
            PrintWriter archivo = new PrintWriter(TBX2RDFServiceConfig.get("logsfolder", ".") + "/post.txt");
            archivo.println("requestURI: " + peticion);archivo.flush();
            archivo.println("content: " + tot);archivo.flush();
            boolean ok = RDFStoreFuseki.postEntity(peticion, tot, Lang.NT);
            archivo.println("postedentity: " + ok);archivo.flush();
            archivo.close();
        } catch (Exception e) {
            System.err.println("Mal al postear en fuseki");
        }
        response.setContentType("text/html;charset=UTF-8");
        try (PrintWriter out = response.getWriter()) {
            out.println(tot);
        }catch(Exception e){
            System.err.println("Mal al dar respuesta.");
        }
        response.setStatus(HttpServletResponse.SC_OK);

    }

    /**
     * Returns a short description of the servlet.
     *
     * @return a String containing servlet description
     */
    @Override
    public String getServletInfo() {
        return "Short description";
    }// </editor-fold>

    public static boolean isRDFTTL(HttpServletRequest request) {
        String uri = request.getRequestURI();
        boolean human = true;
        Enumeration enume = request.getHeaderNames();
        while (enume.hasMoreElements()) {
            String hname = (String) enume.nextElement();
            Enumeration<String> enum2 = request.getHeaders(hname);
            //      System.out.print(hname + "\t");
            while (enum2.hasMoreElements()) {
                String valor = enum2.nextElement();
                if (hname.equalsIgnoreCase("Accept")) {
                    if (valor.equals("text/turtle")) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    /**
     * Determina si la petición ha de ser servida a un humano o directamente el
     * RDF
     *
     * @param request HTTP request
     */
    public static boolean isRDFXML(HttpServletRequest request) {
        String uri = request.getRequestURI();
        boolean human = true;
        Enumeration enume = request.getHeaderNames();
        while (enume.hasMoreElements()) {
            String hname = (String) enume.nextElement();
            Enumeration<String> enum2 = request.getHeaders(hname);
            //      System.out.print(hname + "\t");
            while (enum2.hasMoreElements()) {
                String valor = enum2.nextElement();
                if (valor.contains("application/rdf+xml")) {
                    return true;
                }
                if (hname.equalsIgnoreCase("Accept")) {
                    if (valor.contains("application/rdf+xml")) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

}
