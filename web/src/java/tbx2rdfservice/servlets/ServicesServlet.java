package tbx2rdfservice.servlets;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.URLDecoder;
import java.util.List;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import tbx2rdfservice.store.RDFStoreFuseki;

/**
 *
 * @author admin
 */
public class ServicesServlet extends HttpServlet {

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
        response.setContentType("text/html;charset=UTF-8");
        String uri = request.getRequestURI();
        String txt = "";
        if (uri.endsWith("/service/getResources")) {
            String offset = request.getParameter("current");
            String limit = request.getParameter("rowCount");
            int current = Integer.parseInt(offset);
            int total = RDFStoreFuseki.countEntities("http://www.w3.org/2004/02/skos/core#Concept");
            int ilimit = Integer.parseInt(limit);
            int init = (current - 1) * ilimit;
            List<String> ls = RDFStoreFuseki.listResources(init, ilimit);
            System.out.println(offset + " " + limit);
            String s = "{\n"
                    + "  \"current\": " + current + ",\n"
                    + "  \"rowCount\": " + ilimit + ",\n"
                    + "  \"rows\": [\n";
            int conta = 0;
            for (String cp : ls) {

                int lasti = cp.lastIndexOf("/");
                String nombre = cp.substring(lasti + 1, cp.length());
                nombre = URLDecoder.decode(nombre, "UTF-8");
                cp = cp.replace(" ", "+");
                if (conta != 0) {
                    s += ",\n";
                }
                s += "    {\n"
                        + "      \"resource\": \"" + nombre + "\",\n"
                        + "      \"resourceurl\": \"" + cp + "\"\n"
                        + "    } ";
                conta++;
            }

            s += "  ],\n"
                    + "  \"total\": " + total + "\n"
                    + "}    ";
            try (PrintWriter out = response.getWriter()) {
                out.print(s);
                response.setStatus(HttpServletResponse.SC_OK);
                response.setContentType("application/json");
            } catch (Exception e) {

            }
        }else
        {
            Tbx2rdfServlet.serveError(request, response);
        }

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
