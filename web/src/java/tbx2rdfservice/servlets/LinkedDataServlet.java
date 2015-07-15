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
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.StringTokenizer;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import lemon.LexicalEntry;
import lemon.LexicalSense;
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
        StringTokenizer tokens=new StringTokenizer(peticion, "/");
        List<String> partes=new ArrayList();
        while(tokens.hasMoreTokens())
        {
            partes.add(tokens.nextToken());
        }
        if (partes.size()<2) {
            Tbx2rdfServlet.serveError(request, response);
            return;
        }
        String last = partes.get(partes.size()-1);
        String prelast = partes.get(partes.size()-2);
        if (prelast.equals("resource")) 
        {
                listResources(request, response,last);
                return;
        }
        if (peticion.endsWith("/resource/")) {
            listResources(request, response);
            return;
        }

        PrintWriter archivo = new PrintWriter(TBX2RDFServiceConfig.get("logsfolder", ".") + "/get.txt");
        archivo.println("requestURI: " + peticion);
        archivo.flush();
        String base = TBX2RDFServiceConfig.get("datauri", "http://tbx2rdf.lider-project.eu/converter/resource/iate/");
        String lastid = peticion.substring(peticion.lastIndexOf("/") + 1, peticion.length());
        String dataset = peticion.substring(peticion.lastIndexOf("resource/") + 9, peticion.lastIndexOf("/"));
        String domain = base.substring(0, base.indexOf("resource/"));
        String recurso = domain + "resource/" + dataset + "/" + lastid;
        recurso = recurso.replace("(", "%28");
        recurso = recurso.replace(")", "%29");

        archivo.println("\nrecurso: " + recurso);
        archivo.flush();
        String nt = RDFStoreFuseki.getEntity(recurso);
        if (nt.isEmpty()) {
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
        } else if (isRDFXML(request))
        {
            System.out.println("Serving RDF/XML for " + recurso);
            Model model = ModelFactory.createDefaultModel();
            InputStream is = new ByteArrayInputStream(nt.getBytes(StandardCharsets.UTF_8));
            RDFDataMgr.read(model, is, Lang.NT);
            StringWriter sw = new StringWriter();
            RDFDataMgr.write(sw, model, Lang.RDFXML);
            response.getWriter().println(sw);
            response.setContentType("application/rdf+xml;charset=UTF-8");
        }
        else {
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
            String titulo = entidad.toString().substring(entidad.toString().lastIndexOf("/") + 1, entidad.toString().length());
            titulo = URLDecoder.decode(titulo, "UTF-8");
            StringWriter sw = new StringWriter();
            RDFDataMgr.write(sw, model, Lang.TTL);
            response.setCharacterEncoding("UTF-8");
            System.out.println("Serving HTML for " + recurso);
            String ttl2 = StringEscapeUtils.escapeHtml4(sw.toString());

            String tipo="";
            NodeIterator tipos = model.listObjectsOfProperty(entidad, RDF.type);
            if (tipos.hasNext())
                tipo=tipos.next().asResource().getLocalName();
            
            String html="<h2>"+tipo+"</h2>";
            if (tipo.equals("Concept"))
            {
                html+=getTable(model, entidad);
            }
            body = body.replace("<!--TEMPLATE_PGN-->", html);

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
    
    public static String getTable(Model model, Resource res)
    {
        String tabla="";
        tabla += "<table class=\"table table-condensed\">"; //table-striped 
        tabla += "<thead><tr><td width=\"30%\"><strong>Property</strong></td><td width=\"70%\"><strong>Value</strong></td></tr></thead>\n";

        String tipo="success"; //primary
        LexicalSense sense = new LexicalSense(model, res);
        if (!sense.jurisdiction.isEmpty())
        {
            tabla += "<tr><td>" + "Jurisdiction" + "</td><td><a href=\""+sense.jurisdiction+"\">"+ RDFPrefixes.getLastPart(sense.jurisdiction) + "</a><span class=\"glyphicon glyphicon-share-alt\"></span></td></tr>\n";
        }
        if (!sense.parent.isEmpty())
        {
            tabla += "<tr><td>" + "General concept" + "</td><td><a href=\""+sense.parent+"\">"+RDFPrefixes.getLastPart(sense.parent) + "</a></td></tr>\n";
        }
        
        for(int i=0;i<sense.definitions.size();i++)
        {
            tabla += "<tr><td>" + "Definition" + "</td><td>" + sense.definitions.get(i)+" <kbd>"+sense.definitionlans.get(i) + "</kbd></td></tr>\n";
        }
            //
        for(int i=0;i<sense.entries.size();i++)
        {
            LexicalEntry le = sense.entries.get(i);
            tabla += "<tr><td><b>" + "is sense of" + "</b></td><td>";
            
            tabla+="<table class=\"table table-condensed\">";

            tabla+="<tr class=\"info\"><td width=\"30%\">";
            tabla+="Term";
            tabla+="</td><td width=\"70%\">";
            tabla+=RDFPrefixes.getLastPart(le.getURI());
            tabla+="</td></tr>\n";
            
            if (!le.comentario.isEmpty())
            {
                tabla+="<tr><td width=\"30%\">";
                tabla+="Comment";
                tabla+="</td><td width=\"70%\">";
                tabla+=le.comentario + "<kbd>"+le.lan+"</kbd>";
                tabla+="</td></tr>\n";
            }
            if (!le.source.isEmpty())
            {
                tabla+="<tr><td width=\"30%\">";
                tabla+="Source";
                tabla+="</td><td width=\"70%\">";
                tabla+=le.source;
                tabla+="</td></tr>\n";
            }            
            if (!le.uricanonicalform.isEmpty())
            {
                tabla+="<tr><td width=\"30%\">";
                tabla+="Canonicalform";
                tabla+="</td><td width=\"70%\">";
                tabla+=le.uricanonicalform;
                tabla+="</td></tr>\n";
            }            
            if (!le.reliabilitycode.isEmpty())
            {
                tabla+="<tr><td width=\"30%\">";
                tabla+="Reliability";
                tabla+="</td><td width=\"70%\">";
                tabla+=le.reliabilitycode;
                tabla+="</td></tr>\n";
            }            
            
            tabla+="</table>";
            
            
            tabla+="</td></tr>\n";
        }
        
        
        tabla += "</table>\n";

        return tabla;
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
            archivo.println("requestURI: " + peticion);
            archivo.flush();
            archivo.println("content: " + tot);
            archivo.flush();

            //TODO HAY QUE CAMBIAR ESTO PARA QUE NO SEA IATE SOLO!!!!!!!!
            String base = TBX2RDFServiceConfig.get("datauri", "http://tbx2rdf.lider-project.eu/converter/resource/iate/");
            String lastid = peticion.substring(peticion.lastIndexOf("/") + 1, peticion.length());
            String dataset = peticion.substring(peticion.lastIndexOf("resource/") + 9, peticion.lastIndexOf("/"));
            String domain = base.substring(0, base.indexOf("resource/"));
            archivo.println("domain: " + domain);
            archivo.flush();
            archivo.println("dataset: " + dataset);
            archivo.flush();
            archivo.println("lastid: " + lastid);
            archivo.flush();
            String recurso = domain + "resource/" + dataset + "/" + lastid;
//        String xid = peticion.replace("/tbx2rdf/resource/iate/", "");
//        String recurso = base + xid;

            boolean ok = RDFStoreFuseki.postEntity(recurso, tot, Lang.NT);
            archivo.println("postedentity (name,ok): " + recurso + " " + ok);
            archivo.flush();
            archivo.close();
        } catch (Exception e) {
            System.err.println("Mal al postear en fuseki");
        }
        response.setContentType("text/html;charset=UTF-8");
        try (PrintWriter out = response.getWriter()) {
            out.println(tot);
        } catch (Exception e) {
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

    private void listResources(HttpServletRequest request, HttpServletResponse response) {
        //SERVING THE LIST OF resources
        System.out.println("Serving HTML for resources");
        try {
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
            String tabla = "<table id=\"grid-data\" class=\"table table-condensed table-hover table-striped\">\n"
                    + "        <thead>\n"
                    + "                <tr>\n"
                    + "                        <th data-column-id=\"resource\" data-formatter=\"link\" data-order=\"desc\">Terms</th>\n"
                    + "                </tr>\n"
                    + "        </thead>\n"
                    + "</table>	\n"
                    + "";
            body = body.replace("<!--TEMPLATE_PGN-->", "<br>" + tabla);
            response.getWriter().println(body);
            response.setStatus(HttpServletResponse.SC_OK);
        } catch (Exception e) {
            Tbx2rdfServlet.serveError(request, response);
        }
    }
    private void listResources(HttpServletRequest request, HttpServletResponse response, String dataset) {
        //SERVING THE LIST OF resources
        System.out.println("Serving HTML for resources");
        try {
            response.setContentType("text/html;charset=UTF-8");
            InputStream is1 = LinkedDataServlet.class.getResourceAsStream("../../../../ld.html");
            BufferedReader reader = new BufferedReader(new InputStreamReader(is1));
            StringBuilder outx = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                outx.append(line);
            }
            String body = outx.toString();
            body = body.replace("<!--TEMPLATE_TITLE-->", "\n" + "List of terms of " + dataset);
            String tabla = "<table id=\"grid-data\" class=\"table table-condensed table-hover table-striped\">\n"
                    + "        <thead>\n"
                    + "                <tr>\n"
                    + "                        <th data-column-id=\"resource\" data-formatter=\"link\" data-order=\"desc\">Terms</th>\n"
                    + "                </tr>\n"
                    + "        </thead>\n"
                    + "</table>	\n"
                    + "";
            body = body.replace("<!--TEMPLATE_PGN-->", "<br>" + tabla);
            response.getWriter().println(body);
            response.setStatus(HttpServletResponse.SC_OK);
        } catch (Exception e) {
            Tbx2rdfServlet.serveError(request, response);
        }
    }
}
