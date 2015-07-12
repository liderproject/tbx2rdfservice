package tbx;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import static javax.ws.rs.core.HttpHeaders.USER_AGENT;
import lemon.LexicalEntry;
import lemon.LexicalSense;
import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Logger;
import org.apache.log4j.varia.NullAppender;
import tbx2rdfservice.store.RDFStoreFuseki;

/**
 *
 * @author admin
 */
public class TBX {

    static List<LexicalSense> senses = new ArrayList();

    public static void main(String[] args) {
        BasicConfigurator.configure();
        Logger.getRootLogger().removeAllAppenders();
        Logger.getRootLogger().addAppender(new NullAppender());
        RDFStoreFuseki.init();
        //String res=RDFStoreFuseki.getEntity("http://tbx2rdf.lider-project.eu/converter/resource/iate/IATE-84");
        //System.out.println(res);

        List<LexicalSense> senses = getSampleSenses();

        try {
            String url = senses.get(0).getURI();
            String nt = senses.get(0).getNT();
            System.out.println(url);
            System.out.println(nt);
            nt=URLEncoder.encode(nt,"UTF-8");
            
            //String url = "http://tbx2rdf.lider-project.eu/converter/resource/iate/IATE-84";
            URL obj = new URL(url);
            HttpURLConnection con = (HttpURLConnection) obj.openConnection();

            //add reuqest header
            con.setRequestMethod("POST");
            con.setRequestProperty("User-Agent", USER_AGENT);
		//String urlParameters = "sn=C02G8416DRJM&cn=&locale=&caller=&num=12345";
            // Send post request
            con.setDoOutput(true);
            DataOutputStream wr = new DataOutputStream(con.getOutputStream());
            wr.writeBytes(nt);
            wr.flush();
            wr.close();
            int responseCode = con.getResponseCode();
            System.out.println("\nSending 'POST' request to URL : " + url);
            System.out.println("Response Code : " + responseCode);
            BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
            String inputLine;
            StringBuffer response = new StringBuffer();

            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
            in.close();

            //print result
            System.out.println(response.toString());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void uploadSenses() {
        List<LexicalSense> senses = getSampleSenses();
        String nt = getNT(senses);
//        System.out.println(nt);
        for (LexicalSense sense : senses) {
            String uri = sense.getURI();
            System.out.println(uri);
//            RDFStoreFuseki.postEntity(uri, sense.getNT(), Lang.NT);
        }

    }

    public static List<LexicalSense> getSampleSenses() {
        //http://tbx2rdf.lider-project.eu/converter/resource/iate/lexicalsense/IATE-84
        LexicalSense ls = new LexicalSense("IATE-84");
        ls.subjectField = "1011";
        LexicalEntry le1 = new LexicalEntry("Zuständigkeit der Mitgliedstaaten", "de");
        LexicalEntry le2 = new LexicalEntry("competence of the Member States", "en");
        le2.source = "Eurovoc V4.2";
        ls.addEntry(le1);
        ls.addEntry(le2);
        LexicalSense ls2 = new LexicalSense("IATE-74645");
        LexicalEntry le21 = new LexicalEntry("derivative work", "en");
        le21.comentario = "propriété intellectuelle Note: (contexte américain) A \"derivative work\" is a work based upon one or more preexisting works, such as a translation, musical arrangement, dramatization, fictionalization, motion picture version, sound recording, art reproduction, abridgment, condensation, or any other form in which a work may be recast, transformed, or adapted.; 1976 Copyright Act (U.S.A.), art. 101.; (contexte canadien) The plaintiff says that Abacus Systems Inc. wrongfully continued to market the roofing software and to make derivative works from it.; Gudaitis c. Abacus Systems Inc., [1995] A.C.-B. no 91 (QL), p. 15.";
        le21.source = "1976 Copyright Act (U.S.A.), Title 17 U.S.C., art. 101;";
        ls2.addEntry(le21);
        LexicalEntry le22 = new LexicalEntry("oeuvre dérivée", "fr");
        le22.comentario = "propriété intellectuelle Note: oeuvre composite. - Oeuvre dérivée ou de seconde main qui procède de la juxtaposition d'une oeuvre nouvelle à une oeuvre préexistante (mise en musique d'un sonnet) et, dans une conception extensive, de l'incorporation à une oeuvre nouvelle des éléments originaux d'une oeuvre préexistante (adaptation cinématographique d'un roman, traduction, anthologie). [Cornu, Vocabulaire juridique, 3e éd., p. 552.]; composite. - 1. Qui participe de plusieurs styles d'architecture. ((...)) 2. Par ext. Formé d'éléments très différents, souvent disparates. ((Ex.)) Un mobilier composite. [Petit Robert, 1994, p. 424.];";
        le22.source = "Juriterm - Banque Terminologique de la Common Law. Université de Moncton 1999;";
        ls2.addEntry(le22);
        senses.add(ls);
        senses.add(ls2);
        return senses;
    }

    public static String getNT(List<LexicalSense> senses) {
        String nt = "";
        for (LexicalSense sense : senses) {
            nt += sense.getNT();
        }
        return nt;
    }

    public static String getXML(List<LexicalSense> senses) {
        String xml = "";
        xml += getXMLHeader();

        for (LexicalSense ls : senses) {
            xml += ls.getXML();
        }

        xml += getXMLTail();
        return xml;
    }

    public static String getXMLHeader() {
        String header = "<martif type=\"TBX-Default\" xml:lang=\"en\">\n"
                + "  <martifHeader>\n"
                + "    <fileDesc>\n"
                + "      <sourceDesc>\n"
                + "        <p>This file has been generated by the LIDER project team.</p>\n"
                + "      </sourceDesc>\n"
                + "    </fileDesc>\n"
                + "    <encodingDesc>\n"
                + "      <p type=\"XCSURI\">TBXXCS.xcs</p>\n"
                + "    </encodingDesc>\n"
                + "  </martifHeader><text>\n<body>\n";
        return header;
    }

    public static String getXMLTail() {
        String tail = "</body>\n"
                + "</text>\n"
                + "</martif>";
        return tail;
    }

    //http://bpmlod.github.io/report/multilingual-terminologies/index.html
    public static String getNTHeader() {
        String tail = "";
        return tail;
    }

}
