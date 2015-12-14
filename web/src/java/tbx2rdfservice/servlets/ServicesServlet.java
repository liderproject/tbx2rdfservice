package tbx2rdfservice.servlets;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.URLDecoder;
import java.util.List;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import static org.apache.commons.lang.StringEscapeUtils.escapeHtml;
import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.jena.riot.Lang;
import security.ManagerSQLite;
import tbx2rdfservice.TBX2RDFServiceConfig;
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
    protected void processRequest(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("text/html;charset=UTF-8");
        String uri = request.getRequestURI();
        ServletLogger.global.log("Requested: " + uri);
        ServletLogger.global.log("IP: " +request.getHeader("X-Forwarded-For") );

        String txt = "";
        
        //Internal management service
        if (uri.endsWith("/service/vroddon")) {
            try (PrintWriter out = response.getWriter()) {
                out.println("<html><body>");
                out.println("<h2>Configuration data</h2>");
                out.println("datauri:"+TBX2RDFServiceConfig.get("datauri","")+"<br>");
                out.println("context:"+TBX2RDFServiceConfig.get("context","")+"<br>");
                out.println("logsfolder:"+TBX2RDFServiceConfig.get("logsfolder","")+"<br>");
                out.println("datafolder:"+TBX2RDFServiceConfig.get("datafolder","")+"<br>");
                
                out.println("<h2>Stats</h2>");
//                String s = Services.countEntities(0, 1000000, "");
//                out.println("entitites:"+s+"<br>");
                out.println("Triples in graphs of the store: "+ RDFStoreFuseki.countTriples() +"<br>");
                List<String> ls = RDFStoreFuseki.listGraphs();
                out.println("<a href=\""+ "./service/joker" +"\">graphs</a>:"+ls.size()+"<br>");
                
                String google = (String) request.getSession().getAttribute("google");
                out.println("user:"+google+"<br>");
                
                out.println("<a href=\""+ "./service/dump" +"\">total entities</a>:"+RDFStoreFuseki.countEntities("")+"<br>");

                out.println("<h2>Logs</h2>");
                String s= ServletLogger.global.tail(100);
                s=s.replace("\n", "<br>");
                out.println(s);

                out.println("<h2>Upload data</h2>");
                String html = ServiceCommons.getHTMLPostForm("http://tbx2rdf.lider-project.eu/converter/service/service/upload");
                out.println(html);
                
                out.println("</body></html>");
            }
            catch(Exception e){}
            response.setStatus(HttpServletResponse.SC_OK);
            response.setContentType("text/html;charset=utf-8");
            return;
        }
        
        //Shows the table of resources
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
        
        //Joder que lista los graphs
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
                String format = request.getParameter("format");
                if (format==null || format.isEmpty())
                    format = "ttl";
                String ttl = RDFStoreFuseki.dump(format);
                String ttl2 = RDFStoreFuseki.dumpdefaultgraph(format);
                ttl+=ttl2;
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
                    
                    boolean ok = ManagerSQLite.authenticate(u, p);
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

        if (uri.endsWith("/service/delete")) {
            String res = request.getParameter("uri");
            String body = ServiceCommons.getBody(request);
//            RDFStoreFuseki.deleteTriples(body);
        }

        //el parámetro que se espera es una lista de ntriples
        if (uri.endsWith("/service/upload")) {
 //            String res = request.getParameter("uri");
            String body = ServiceCommons.getBody(request);
            ServletLogger.global.log(body);
            try{
                RDFStoreFuseki.postEntity(null, body, Lang.NTRIPLES);
                ServletLogger.global.log("Data has been uploaded");
            }catch(Exception e){
                ServletLogger.global.log("Data has not been uploaded " + e.getMessage());
            };
            response.setContentType("text/html");
            response.addHeader("Access-Control-Allow-Origin", "*");
            try (PrintWriter out = response.getWriter()) {            
                out.write(body);
            }catch(Exception e){}
            response.setStatus(HttpServletResponse.SC_OK);
            return;
        }
        if (uri.endsWith("/service/updateSPARQL")) {
            String body = ServiceCommons.getBody(request);
            ServletLogger.global.log(body);
            try{
                String s = RDFStoreFuseki.updateSPARQL(body);
                ServletLogger.global.log("Query has been made. Query:<br> "+ escapeHtml(body));
                ServletLogger.global.log("Query has been made. Results:<br> "+ escapeHtml(s));
            }catch(Exception e){
                ServletLogger.global.log("Query has not been made" + e.getMessage());
            };
        }
        if (uri.endsWith("/service/selectSPARQL")) {
            String body = ServiceCommons.getBody(request);
            ServletLogger.global.log(body);
            try{
                String s = RDFStoreFuseki.selectSPARQL(body);
                ServletLogger.global.log("Query has been made. Query:<br> "+ escapeHtml(body));
                ServletLogger.global.log("Query has been made. Results:<br> "+ escapeHtml(s));
            }catch(Exception e){
                ServletLogger.global.log("Query has not been made" + e.getMessage());
            };
        }
        if (uri.endsWith("/service/clear")) {
//            RDFStoreFuseki.deleteAll();
            ServletLogger.global.log("Borrado termporalmente deshabilitado");
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
