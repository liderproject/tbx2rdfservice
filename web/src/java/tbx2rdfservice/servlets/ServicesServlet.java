package tbx2rdfservice.servlets;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.URLDecoder;
import java.util.List;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.lang3.StringEscapeUtils;
import security.TestSQLite;
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
        //http://tbx2rdf.lider-project.eu/converter/service/getResources?current=1&rowCount=10&sort[resource]=desc&searchPhrase=
        if (uri.endsWith("/service/getResources")) {
            String offset = request.getParameter("current");
            String limit = request.getParameter("rowCount");
            String searchFrase =request.getParameter("searchPhrase");
            int current = Integer.parseInt(offset);
            int ilimit = Integer.parseInt(limit);
            
            
            
           String s = Services.countEntities(current, ilimit, searchFrase);

            try (PrintWriter out = response.getWriter()) {
                out.print(s);
                response.setStatus(HttpServletResponse.SC_OK);
                response.setContentType("application/json");
            } catch (Exception e) {

            }
        }
        if (uri.endsWith("/service/joker")) {
            List<String> ls = RDFStoreFuseki.listGraphs();
            try (PrintWriter out = response.getWriter()) {
                for (String s : ls) {
                    out.print(s + "<br>");
                }
                response.setStatus(HttpServletResponse.SC_OK);
                response.setContentType("text/html");
            } catch (Exception e) {

            }
        }
            if (uri.endsWith("/service/dump")) {
                String ttl = RDFStoreFuseki.dump();
                try (PrintWriter out = response.getWriter()) {
                    String html="<html><head><script src=\"https://google-code-prettify.googlecode.com/svn/loader/run_prettify.js\"></script></head>";
                    html+="<body><pre class=\"prettyprint\">";
                    ttl = StringEscapeUtils.escapeHtml4(ttl);
                    ttl = ttl.replace("\n", "<br>");
                    html+=ttl;
                    html+="</pre></body></html>";
                    out.print(html);
                    response.setStatus(HttpServletResponse.SC_OK);
                    response.setContentType("text/html");
                } catch (Exception e) {

                }
            }
        if (uri.endsWith("/service/getUser")) {
                try (PrintWriter out = response.getWriter()) {
                    String google = (String) request.getSession().getAttribute("google");
                    out.print(google);
                    response.setStatus(HttpServletResponse.SC_OK);
                    response.setContentType("text/html");
                    return;
                }catch(Exception e)
                {
                    
                }
        }
        if (uri.endsWith("/service/logout")) {
            request.getSession().setAttribute("google","");
            response.sendRedirect("account");
            return;
        }
        
        if (uri.endsWith("/service/login")) {
                try (PrintWriter out = response.getWriter()) {
                    String u = request.getParameter("user");
                    String p = request.getParameter("password");
                    
                    boolean ok = TestSQLite.authenticate(u, p);
                    if (!ok)
                    {
                        out.print("403");
                        response.setStatus(HttpServletResponse.SC_OK);
                        response.setContentType("text/html");
                        return;
                    }
                    else
                    {
                        out.print("200");
                        request.getSession().setAttribute("google",u);
                        response.setStatus(HttpServletResponse.SC_OK);
                        response.setContentType("text/html");
                        return;
                    }
                }catch(Exception e)
                {
                    
                }
        }
            
        
        if (uri.endsWith("/service/clear")) {
            RDFStoreFuseki.deleteAll();
            response.setStatus(HttpServletResponse.SC_OK);
        } else {
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
