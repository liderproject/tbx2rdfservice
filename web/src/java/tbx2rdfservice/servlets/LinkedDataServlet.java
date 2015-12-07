package tbx2rdfservice.servlets;

import com.hp.hpl.jena.rdf.model.Literal;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.NodeIterator;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.vocabulary.RDF;
import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.StringTokenizer;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import lemon.LexicalEntry;
import lemon.LexicalSense;
import org.apache.commons.httpclient.util.URIUtil;
import org.apache.commons.io.IOUtils;
import static org.apache.commons.lang.StringEscapeUtils.escapeHtml;
import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFDataMgr;
import tbx.IATEUtils;
import tbx2rdfservice.TBX2RDFServiceConfig;
import tbx2rdfservice.store.RDFPrefixes;
import tbx2rdfservice.store.RDFStoreFuseki;

/**
 * Servlet que sirve LinkedData
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
        String url = request.getRequestURL().toString();
        ServletLogger.global.log("Request: "+ url);
        try {
            if (url.equals("http://lider2.dia.fi.upm.es:8080/tbx2rdf/resource/")) //Por si viene de la lectura del paper
            {
                response.sendRedirect("http://copyrighttermbank.linkeddata.es/");
//                response.sendRedirect("http://tbx2rdf.lider-project.eu/converter/resource/");
                return;
            }
        } catch (Exception ex) {
        }
/*
        StringTokenizer tokens = new StringTokenizer(peticion, "/");
        List<String> partes = new ArrayList();
        while (tokens.hasMoreTokens()) {
            partes.add(tokens.nextToken());
        }
        if (partes.size() < 2) {
            Tbx2rdfServlet.serveError(request, response);
            return;
        }
        */
//        String last = partes.get(partes.size() - 1);
//        String prelast = partes.get(partes.size() - 2);
        
        String recurso = url;
        recurso = recurso.replace("http://localhost:8080/copyrighttermbank/resource", "http://copyrighttermbank.linkeddata.es/resource");
        ServletLogger.global.log("RECURSO: " + recurso);
        try {
            recurso = URIUtil.encodeQuery(recurso);
        } catch (Exception e) {
            e.getMessage();
        }          
        
        if (recurso.equals("http://copyrighttermbank.linkeddata.es/resource/") || recurso.equals("http://copyrighttermbank.linkeddata.es/resource")) {
            listResources(request, response);
            return;
        }

/*        String base = TBX2RDFServiceConfig.get("datauri", "http://coyprighttermbank.linkeddata.es/");
        String lastid = peticion.substring(peticion.lastIndexOf("/") + 1, peticion.length());
        String dataset = peticion.substring(peticion.lastIndexOf("resource/") + 19, peticion.lastIndexOf("/"));
        String domain = base.substring(0, base.indexOf("resource/"));
        String recurso = domain + "resource/" + dataset + "/" + lastid;
        */

        
        
        
        ServletLogger.global.log("Resource requested: " + recurso);
        ServletLogger.global.log("IP: " + request.getHeader("X-Forwarded-For") );
        
        String nt = RDFStoreFuseki.getEntity(recurso);
        
        String l = "Obtained RDF: <pre><code>" + escapeHtml(nt) + "</code></pre>";
        l = l.replace("\n", "<br>");
        ServletLogger.global.log(l);
        
        if (nt.isEmpty()) {
            Tbx2rdfServlet.serveError(request, response);
            return;
        }
        if (isRDFTTL(request)) {
            Model model = ModelFactory.createDefaultModel();
            InputStream is = new ByteArrayInputStream(nt.getBytes(StandardCharsets.UTF_8));
            RDFDataMgr.read(model, is, Lang.NT);
            StringWriter sw = new StringWriter();
            RDFDataMgr.write(sw, model, Lang.TTL);
            response.getWriter().println(sw);
            ServletLogger.global.log("Served TTL: " + sw.toString().length() + " bytes");
            response.setContentType("text/turtle;charset=UTF-8");
        } else if (isRDFXML(request)) {
            System.out.println("Serving RDF/XML for " + recurso);
            Model model = ModelFactory.createDefaultModel();
            InputStream is = new ByteArrayInputStream(nt.getBytes(StandardCharsets.UTF_8));
            RDFDataMgr.read(model, is, Lang.NT);
            StringWriter sw = new StringWriter();
            RDFDataMgr.write(sw, model, Lang.RDFXML);
            ServletLogger.global.log("Served RDF/XML: " + sw.toString().length() + " bytes");
            response.getWriter().println(sw);
            response.setContentType("application/rdf+xml;charset=UTF-8");
        } else {
            response.setContentType("text/html;charset=UTF-8");
            InputStream input = getClass().getResourceAsStream("ld.html");
            BufferedInputStream bis = new BufferedInputStream(input);
            String body = IOUtils.toString(bis, "UTF-8");
            String context = TBX2RDFServiceConfig.get("context", "/tbx2rdf");
            context = "http://copyrighttermbank.linkeddata.es";
            body = body.replace("TEMPLATE_CONTEXTO", context);

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
            ServletLogger.global.log("Serving HTML for " + recurso);
            String ttl2 = StringEscapeUtils.escapeHtml4(sw.toString());

            String xax = sw.toString();
            int nlines = StringUtils.countMatches(xax, "\n");
            ServletLogger.global.log("Served HTML: " + xax.length() + " bytes. lines: "+ nlines );

            String tipo = "";
            NodeIterator tipos = model.listObjectsOfProperty(entidad, RDF.type);
            if (tipos.hasNext()) {
                tipo = tipos.next().asResource().getLocalName();
            }
 
            String html = "<h2>" + tipo + "</h2>";
            if (tipo.equals("Concept")) {
                //inicio experimetanl
                nt = RDFStoreFuseki.loadGraph(entidad.toString()); 
                ServletLogger.global.log("Informacion NT de la entidad  "+ entidad.toString() + ": <pre><code>"+ escapeHtml(nt) +"</pre></code>");
                InputStream iy = new ByteArrayInputStream(nt.getBytes(StandardCharsets.UTF_8));
                Model model3 = ModelFactory.createDefaultModel();
                RDFDataMgr.read(model3, iy, Lang.NT);
                model3=RDFPrefixes.addPrefixesIfNeeded(model3);
                StringWriter sw7 = new StringWriter();
                RDFDataMgr.write(sw7, model3, Lang.TTL);
                ServletLogger.global.log("Informacion de la entidad en doget TTL: <pre><code>"+ escapeHtml(sw7.toString()) +"</pre></code>");
                ttl2 = StringEscapeUtils.escapeHtml4(sw7.toString());
                //fin experimental
                html += getTable(model3, entidad);

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

    /**
     * Obtains the table of properties to be shown  for a certain resource
     */
    public static String getTable(Model model, Resource res) {
        String tabla = "";
        
        model = RDFPrefixes.addPrefixesIfNeeded(model);
        
        tabla += "<table class=\"table table-condensed table-bordered\">"; //table-striped 
        tabla += "<thead><tr><td width=\"25%\"><strong>Property</strong></td><td width=\"75%\"><strong>Value</strong></td></tr></thead>\n";

        LexicalSense sense = new LexicalSense(model, res);
        
        
        for (int i = 0; i < sense.definitions.size(); i++) {
           
            String sdefinition = sense.definitions.get(i);
            sdefinition = sdefinition.replace("\n", "<br>");
            tabla += "<tr><td>" + "Definition" + "</td><td>" + sdefinition;
            tabla += " <kbd>" + sense.definitionlans.get(i) +  "</kbd>";
            
            String source = sense.definitionsources.get(i);
            if (source.startsWith("http"))
                source = "<a href=\"" + source + "\">" + source + "</a>"; 
            
            tabla += "<br/>Source: " + source + "\n";
            tabla += "</td></tr>\n";
        }        
        
        
        // Multiple links to iate are ok
        for (int i = 0; i < sense.links.size(); i++) {
            String link = sense.links.get(i);
            String add = "";
            if (link.contains("iate/")) {
                add += "<br/>";
                List<Literal> lista = IATEUtils.getIATETerms(link);
                int n = 0;
                for (Literal l : lista) {
                    if (n != 0) {
                        add += ",";
                    }
                    add += l.getLexicalForm() + " <kbd>" + l.getLanguage() + "</kbd>\n";
                    n++;
                }
            }
            tabla += "<tr><td>" + "Matches" + "</td><td><a href=\"" + link + "\">" + RDFPrefixes.getLastPart(link) + "</a> <span class=\"glyphicon glyphicon-share-alt\"></span>" + add + "</td></tr>\n";
        }        
        
        if (!sense.jurisdiction.isEmpty()) {
            tabla += "<tr><td>" + "Jurisdiction" + "</td><td><a href=\"" + sense.jurisdiction + "\">" + RDFPrefixes.getLastPart(sense.jurisdiction) + "</a><span class=\"glyphicon glyphicon-share-alt\"></span></td></tr>\n";
        }
        if (!sense.parent.isEmpty()) {
            tabla += "<tr><td>" + "General concept" + "</td><td><a href=\"" + sense.parent + "\">" + RDFPrefixes.getLastPart(sense.parent) + "</a></td></tr>\n";
        }
        if (!sense.reference.isEmpty()) {
            tabla += "<tr><td>" + "Reference" + "</td><td><a href=\"" + sense.reference + "\">" + RDFPrefixes.getLastPart(sense.reference) + "</a><span class=\"glyphicon glyphicon-share-alt\"></span></td></tr>\n";
        }
        if (!sense.related.isEmpty()) {
            tabla += "<tr><td>" + "Related" + "</td><td><a href=\"" + sense.related + "\">" + RDFPrefixes.getLastPart(sense.related) + "</a><span class=\"glyphicon glyphicon-share-alt\"></span></td></tr>\n";
        }
        if (!sense.comment.isEmpty()) {
            tabla += "<tr><td>" + "Comment" + "</td><td>" + sense.comment + "</td></tr>\n";
        }


        

  //      if (!sense.reference.isEmpty())
  //          tabla += "<tr><td>" + "Related" + "</td><td><a href=\"" + sense.reference + "\">" + RDFPrefixes.getLastPart(sense.reference) + "</a> <span class=\"glyphicon glyphicon-share-alt\"></span>" + " " + "</td></tr>\n";
        
        
        //
        for (int i = 0; i < sense.entries.size(); i++) {
            String les = sense.entries.get(i).getURI();
            Model ms = ModelFactory.createDefaultModel();
            String rdf = RDFStoreFuseki.getEntity(les);

            InputStream ix = new ByteArrayInputStream(rdf.getBytes(StandardCharsets.UTF_8));
            RDFDataMgr.read(ms, ix, Lang.NT);
            LexicalEntry le = new LexicalEntry(ms, ms.createResource(les));
            tabla += "<tr><td>" + "is sense of" + "</td><td>";

            tabla += "<table class=\"table table-condensed table-bordered\">";

            tabla += "<tr><td width=\"30%\"><b>";
            tabla += le.getBeautifulname();
            tabla += " <kbd>" + le.lan + "</kbd>";
            tabla += "</b></td><td width=\"70%\">";
            tabla += "</td></tr>\n";
            if (!le.definition.isEmpty()) {
                tabla += "<tr><td width=\"30%\">";
                tabla += "Definition";
                tabla += "</td><td width=\"70%\">";
                
                String sdefinition = le.definition;
                sdefinition = sdefinition.replace("\n", "<br>");
                 
                tabla += sdefinition + "<kbd>" + le.lan + "</kbd>";
                tabla += "</td></tr>\n";
            }

            if (!le.comentario.isEmpty()) {
                tabla += "<tr><td width=\"30%\">";
                tabla += "Comment";
                tabla += "</td><td width=\"70%\">";
                tabla += le.comentario + "<kbd>" + le.lan + "</kbd>";
                tabla += "</td></tr>\n";
            }
            if (!le.source.isEmpty()) {
                tabla += "<tr><td width=\"30%\">";
                tabla += "Source";
                tabla += "</td><td width=\"70%\">";
                if (le.source.startsWith("http:"))
                    tabla += "<a href=\""+le.source + "\">" +le.source + "</a>";
                else
                    tabla += le.source;
                tabla += "</td></tr>\n";
            }
            if (!le.uricanonicalform.isEmpty()) {
                tabla += "<tr><td width=\"30%\">";
                tabla += "Canonicalform";
                tabla += "</td><td width=\"70%\">";
                tabla += le.uricanonicalform;
                tabla += "</td></tr>\n";
            }
            if (!le.reliabilitycode.isEmpty()) {
                tabla += "<tr><td width=\"30%\">";
                tabla += "Reliability";
                tabla += "</td><td width=\"70%\">";
                tabla += le.reliabilitycode;
                tabla += "</td></tr>\n";
            }

            tabla += "</table>";

            tabla += "</td></tr>\n";
        }
        List<String> narrows = RDFStoreFuseki.getNarrower(res.getURI());
        if (!narrows.isEmpty()) {
            tabla += "<tr><td width=\"30%\">";
            tabla += "Specialized by";
            tabla += "</td><td width=\"70%\">";
            for (String narrow : narrows) {
                String bonito = RDFPrefixes.getLastPart(narrow);
                try {
                    bonito = URLDecoder.decode(bonito, "UTF-8");
                } catch (UnsupportedEncodingException ex) {
                }
                tabla += "<a href=\"" + narrow + "\">" + bonito + "</a> ";
            }
            tabla += "</td></tr>\n";
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
        String url = request.getRequestURL().toString();
        BufferedReader br = request.getReader();
        String s = "";
        String tot = "";
        while ((s = br.readLine()) != null) {
            tot = tot + s + "\n";
        }
        //tot = java.net.URLDecoder.decode(tot, "UTF-8");
        try {

            //TODO HAY QUE CAMBIAR ESTO PARA QUE NO SEA IATE SOLO!!!!!!!!
            ServletLogger.global.log("Peticion: " + peticion);
            String recurso = url;
            recurso = recurso.replace("http://localhost:8080/copyrighttermbank/", "http://copyrighttermbank.linkeddata.es/");
            
/*            String base = TBX2RDFServiceConfig.get("datauri", "http://tbx2rdf.lider-project.eu/converter/resource/iate/");
            String lastid = peticion.substring(peticion.lastIndexOf("/") + 1, peticion.length());
            String dataset = peticion.substring(peticion.lastIndexOf("resource/") + 9, peticion.lastIndexOf("/"));
            String domain = base.substring(0, base.indexOf("resource/"));
            String recurso = domain + "resource/" + dataset + "/" + lastid; */
            
            ServletLogger.global.log("SE QUIERE POSTEAR EL RECURSO: " + recurso);

            
            boolean ok = RDFStoreFuseki.postEntity(recurso, tot, Lang.NT);
            ServletLogger.global.log("We have just posted into Fuseki. Exito: " + ok + " <pre><code>" + escapeHtml(tot)+"</code></pre>");
            
            if (!ok)
            {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                return;
            }
        } catch (Exception e) {
            ServletLogger.global.log("ERROR posting to Fuseki. Message: " +e.getMessage());
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }
        response.setContentType("text/html;charset=UTF-8");
        try (PrintWriter out = response.getWriter()) {
            out.println(tot);
        } catch (Exception e) {
            ServletLogger.global.log("ERROR posting to Fuseki");
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

        String email = "";
        //ESTO ES DE STORMPATH

        //LO SIGUIENTE ES DE APACHE SHIRO
        /*Subject currentUser = SecurityUtils.getSubject();
         if ( !currentUser.isAuthenticated() ) {
         SecurityManager securityManager = SecurityUtils.getSecurityManager();
         System.out.println("not authenticated");
         UsernamePasswordToken token = new UsernamePasswordToken("lonestarr", "vespa");
         token.setRememberMe(true);
         currentUser.login(token);
         }*/
        try {
            response.setContentType("text/html;charset=UTF-8");
            InputStream input = getClass().getResourceAsStream("ld.html");
            BufferedInputStream bis = new BufferedInputStream(input);
            String body = IOUtils.toString(bis, "UTF-8");
            String context = TBX2RDFServiceConfig.get("context", "/tbx2rdf");
            body = body.replace("TEMPLATE_CONTEXTO", context);

            body = body.replace("<!--TEMPLATE_TITLE-->", "\n" + "List of concepts");
            String tabla = "<table id=\"grid-data\" class=\"table table-condensed table-hover table-striped\">\n"
                    + "        <thead>\n"
                    + "                <tr>\n"
                    + "                        <th data-column-id=\"resource\" data-formatter=\"link\" data-order=\"desc\">Concepts</th>\n"
                    + "                </tr>\n"
                    + "        </thead>\n"
                    + "</table>	\n"
                    + "";

            tabla += email;

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
            InputStream input = getClass().getResourceAsStream("ld.html");
            BufferedInputStream bis = new BufferedInputStream(input);
            String body = IOUtils.toString(bis, "UTF-8");
            String context = TBX2RDFServiceConfig.get("context", "/tbx2rdf");
            body = body.replace("TEMPLATE_CONTEXTO", context);
            body = body.replace("<!--TEMPLATE_TITLE-->", "\n" + "List of concepts of " + dataset);
            String tabla = "<table id=\"grid-data\" class=\"table table-condensed table-hover table-striped\">\n"
                    + "        <thead>\n"
                    + "                <tr>\n"
                    + "                        <th data-column-id=\"resource\" data-formatter=\"link\" data-order=\"desc\">Concepts</th>\n"
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
