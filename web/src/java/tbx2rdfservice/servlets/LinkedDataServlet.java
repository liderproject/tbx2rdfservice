package tbx2rdfservice.servlets;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import tbx2rdfservice.TBX2RDFServiceConfig;
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

    // <editor-fold defaultstate="collapsed" desc="HttpServlet methods. Click on the + sign on the left to edit the code.">
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
        String peticion = request.getRequestURI();
        String id = request.getRequestURI().replace("/tbx2rdf/resource/", "");
        System.out.println(peticion+" "+id);
        PrintWriter archivo = new PrintWriter("/tmp/tbx2.txt");
        archivo.println("requestURI: " + peticion);  
        archivo.close();
        // input: http://tbx2rdf.lider-project.eu/converter/resource/iate/IATE-84
        // peticion: -->  /tbx2rdf/resource/iate/IATE-84
        String base=TBX2RDFServiceConfig.get("datauri","http://tbx2rdf.lider-project.eu/converter/resource/iate/");
        String xid = peticion.replace("/tbx2rdf/resource/iate/", "");
        String recurso = base+xid;
        String nt=RDFStoreFuseki.getEntity(recurso);
        
        response.setContentType("text/html;charset=UTF-8");
        try (PrintWriter out = response.getWriter()) {
            /* TODO output your page here. You may use following sample code. */
            out.println("<!DOCTYPE html>");
            out.println("<html>");
            out.println("<head>");
            out.println("<title>Servlet LinkedDataServlet</title>");      
            out.println(nt);
            out.println("</head>");
            out.println("<body>");
            out.println("<h1>Servlet LinkedDataServlet at " + request.getContextPath() + "</h1>");
            out.println("</body>");
            out.println("</html>");
        }
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
        BufferedReader br=request.getReader();
        String s="";
        String tot="";
        while ((s=br.readLine())!=null)
        {
            tot=tot+s+"\n";
        }
        tot = java.net.URLDecoder.decode(tot, "UTF-8");

        PrintWriter archivo = new PrintWriter("/tmp/post.txt");
        archivo.println("requestURI: " + peticion);  
        archivo.println("content: " + tot);  
        archivo.close();
        
        response.setContentType("text/html;charset=UTF-8");
        try (PrintWriter out = response.getWriter()) {
            out.println(tot);
        }
        
       // AHORA ESTOY INTENTANDO DAR SOPORTE AL METODO POST
        
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

}
