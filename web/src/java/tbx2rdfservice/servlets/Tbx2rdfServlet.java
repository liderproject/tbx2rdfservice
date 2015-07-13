package tbx2rdfservice.servlets;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.ServletException;
import javax.servlet.http.*;

import tbx2rdf.Mappings;
import tbx2rdf.TBX2RDF_Converter;

/**
 * @api {post} /tbx2rdf tbx2rdf
 * @apiName /tbx2rdf
 * @apiGroup TBX2RDF
 * @apiVersion 1.0.0
 * @apiDescription Translates a TBX document into a RDF version
 * 
 * @apiParam {String} namespace URI of the namespace to be added to the locally generated entities.
 * @apiParam {String} action Action to be made over the input TBX document. Valid values are:
 * <ul>
 * <li>translate</li> Merely makes the translation
 * <li>enrich</li> Makes the translation and enriches the document with links to other terminologies (TEST)
 * <li>reverse</li> Makes the reverse transformation from RDF to TBX (TEST)
 * </ul>
 * @apiParam {String} lenient Determines whether strict or lax parsing is performed. Valid values: true, false (TESTING)
 * @apiParam {String} mappings Mappings to be used, according to the documenation <a href="#">here</a>
 * @apiParam {String} body The HTTP message contains the TBX document
 *
 * @apiSuccess {String} RDF Version of the input data <br/>
 * 
 * @author OEG, Univ. Bielefeld, LIDER
  */
public class Tbx2rdfServlet extends HttpServlet {

    
    protected void processRequest(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String action = req.getParameter("action"); 
        String tbx = req.getParameter("tbx");
        String namespace = req.getParameter("namespace");
        String lenient = req.getParameter("lenient");
        if (tbx==null || tbx.isEmpty())
        {
            serveError(req, resp);
            return;
        }
        
        
        tbx = java.net.URLDecoder.decode(tbx, "UTF-8");
        String uri = req.getRequestURI();
        
        PrintWriter archivo = new PrintWriter("/tmp/tbx.txt");
        archivo.println(uri);
        archivo.println("current: "+req.getParameter("current")+"\n");
        archivo.println("action: "+req.getParameter("action")+"\n");
        archivo.println("tbx: "+req.getParameter("tbx")+"\n");
        archivo.println("lenient: "+req.getParameter("lenient")+"\n");
        archivo.close();
        
        
        String rdf="Ooops";
        if (action.contains("enrich"))
            rdf = "Feature coming soon!";
        else
        {
            try{
                InputStream in = getClass().getResourceAsStream("/tbx2rdfservice/servlets/mappings.default"); 
                BufferedReader reader = new BufferedReader(new InputStreamReader(in));                
                final Mappings mappings = Mappings.readInMappings(reader);
                rdf = new TBX2RDF_Converter().convert(tbx, mappings, namespace);
            }catch(Exception e)
            {
                rdf = "An ERROR has happened. We are sorry.\n\n\n";
                rdf += e.getMessage();
                
            }
        }
        resp.getWriter().println(rdf);
        resp.setContentType("text/plain");
    }    
    
    /**
     * Handles the HTTP <code>GET</code> method.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
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
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
    }
    
    public static void serveError(HttpServletRequest req, HttpServletResponse resp)
    {
        try {
            resp.getWriter().println("Not found or parameters missing");
        } catch (IOException ex) {
            Logger.getLogger(Tbx2rdfServlet.class.getName()).log(Level.SEVERE, null, ex);
        }
        resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
    }
    
}
