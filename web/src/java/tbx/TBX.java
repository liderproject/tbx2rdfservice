package tbx;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

import lemon.LexicalEntry;
import lemon.LexicalSense;
import org.apache.jena.riot.Lang;
import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Logger;
import org.apache.log4j.varia.NullAppender;
import tbx2rdfservice.store.RDFStoreClient;
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
        List<LexicalSense> senses = getSampleSenses();
        for(int i=0;i<senses.size();i++)
        {
            LexicalSense sense = senses.get(i);
        String url = senses.get(i).getURI();
//        String nt = senses.get(i).getNT();
//        System.out.println(url);
//        System.out.println(nt);
   //  boolean ok=RDFStoreClient.post(url, nt);
        
        System.out.println(sense.getXML());
        
        }
//        RDFStoreClient.delete(url);
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
        ls2.addDefinition("1. teos või tarkvara, mida litsentsisaajal on olnud võimalik luua tuginedes originaalteosele või selle muudetud versioonile 2. teose tõlge, algse teose kohandus (adaptsioon), töötlus (arranžeering) ja teose muu töötlus ", "et");
        
        LexicalEntry le21 = new LexicalEntry("derivative work", "en");
        le21.comentario = "propriété intellectuelle Note: (contexte américain) A \"derivative work\" is a work based upon one or more preexisting works, such as a translation, musical arrangement, dramatization, fictionalization, motion picture version, sound recording, art reproduction, abridgment, condensation, or any other form in which a work may be recast, transformed, or adapted.; 1976 Copyright Act (U.S.A.), art. 101.; (contexte canadien) The plaintiff says that Abacus Systems Inc. wrongfully continued to market the roofing software and to make derivative works from it.; Gudaitis c. Abacus Systems Inc., [1995] A.C.-B. no 91 (QL), p. 15.";
        le21.source = "1976 Copyright Act (U.S.A.), Title 17 U.S.C., art. 101;";
        ls2.addEntry(le21);
        LexicalEntry le22 = new LexicalEntry("oeuvre dérivée", "fr");
        le22.comentario = "propriété intellectuelle Note: oeuvre composite. - Oeuvre dérivée ou de seconde main qui procède de la juxtaposition d'une oeuvre nouvelle à une oeuvre préexistante (mise en musique d'un sonnet) et, dans une conception extensive, de l'incorporation à une oeuvre nouvelle des éléments originaux d'une oeuvre préexistante (adaptation cinématographique d'un roman, traduction, anthologie). [Cornu, Vocabulaire juridique, 3e éd., p. 552.]; composite. - 1. Qui participe de plusieurs styles d'architecture. ((...)) 2. Par ext. Formé d'éléments très différents, souvent disparates. ((Ex.)) Un mobilier composite. [Petit Robert, 1994, p. 424.];";
        le22.source = "Juriterm - Banque Terminologique de la Common Law. Université de Moncton 1999;";
        ls2.addEntry(le22);
        LexicalEntry le23 = new LexicalEntry("tuletatud teos", "et");
        le23.source = "Euroopa Liidu tarkvara vaba kasutuse litsents v.1.0 EUPL © Euroopa Ühendus 2007";
        ls2.addEntry(le23);

        
        
        LexicalSense ls3= new LexicalSense("Derivative work (ES)");
        ls3.base="http://tbx2rdf.lider-project.eu/converter/resource/cc/";
        ls3.links.add("http://tbx2rdf.lider-project.eu/data/iate/IATE-74645");
        ls3.jurisdiction="http://dbpedia.org/resource/Spain";
        ls3.reference="http://dbpedia.org/resource/Derivative_work";
        ls3.addDefinition("Se considerará obra derivada aquella que se encuentre basada en una obra o en una obra y otras preexistentes, tales como: las traducciones y adaptaciones; las revisiones, actualizaciones y anotaciones; los compendios, resúmenes y extractos; los arreglos musicales y; en general, cualesquiera transformaciones de una obra literaria, artística o científica, salvo que la obra resultante tenga el carácter de obra conjunta en cuyo caso no será considerada como una obra derivada a los efectos de esta licencia. Para evitar la duda, si la obra consiste en una composición musical o grabación de sonidos, la sincronización temporal de la obra con una imagen en movimiento (\"synching\") será considerada como una obra derivada a los efectos de esta licencia.", "es");
        ls3.addDefinition("Sedrán obres derivaes les feches a partir de la llicenciada, como por exemplu: les tornes y adautaciones; les revisiones, actualizaciones y anotaciones; los compendios, resúmenes y estractos; los arreglos musicales y, en xeneral, les tresformaciones d’una obra lliteraria, artística o científica. Pa que nun se plantegue dulda nenguna, si la obra ye una composición musical o grabación de soníos, la sincronización temporal de la obra con una imaxe en movimientu (synching) consideraráse como una obra derivada a efeutos d’esta llicencia", "ast");
        ls3.addDefinition("Es consideraran obres derivades aquelles obres creades a partir de l'objecte d'aquesta llicència com, per exemple, les traduccions i adaptacions; les revisions, actualitzacions i anotacions; els compendis, resums i extractes; els arranjaments musicals, i en general qualsevol transformació d'una obra literària, artística o científica. Per aclarir dubtes, si l'obra consisteix en una composició musical o en un enregistrament de sons, la sincronització temporal de l'obra amb una imatge en moviment (synching) serà considerada com una obra derivada als efectes d'aquesta llicència.", "ca");
        ls3.addDefinition("Lan eratorritzat joko dira lan baimendunetik abiatuta sortutakoak, besteak beste: itzulpenak eta egokitzapenak; berrikuspenak, eguneratzeak eta oharpenak; bildumak, laburpenak eta aterakinak; musika moldaketak eta, orobat, literatur, arte edo zientzia lan baten eraldaketa oro. Zalantzarik egon ez dadin, lana baldin bada musika konposizioa eta soinu grabaketa, lan hori mugimenduzko irudiekin denboraz sinkronizatzea lan eratorritzat joko da baimen honen ondorioetarako.", "eu");
        ls3.addDefinition("Consideraranse obras derivadas aquelas obras creadas a partir da licenciada, como por exemplo: as traducións e adaptacións; as revisións, actualizacións e anotacións; os compendios, resumos e extractos; os arranxos musicais e, en xeral, calquera transformación dunha obra literaria, artística ou científica. Para evitar a dúbida, se a obra consiste nunha composición musical ou gravación de sons, a sincronización temporal da obra cunha imaxe en movemento (synching) será considerada como unha obra derivada para os efectos desta licenza.", "gl");
        ls3.addDefinition("Se consideraràn òbres derivades aqueres òbres creades a compdar der objècte d'aguesta licéncia com, per exemple, es traduccions e adaptacions; es revisions, actualizacions e anotacions; es compendis, resums e extractes; es arranjaments musicaus, e en generau quaussevolh transformacion d'ua òbra literària, artistica o scientifica. Entà aclarir dobtes, s'era òbra consistís en ua composicion musicau o en un enregistrament de sons, era sincronizacion temporau dera òbra damb ua imatge en moviment (synching) serà considerada com ua òbra derivada as efèctes d'aguesta licéncia.", "oci");
        
        LexicalEntry le31 = new LexicalEntry("obra derivada", "es");
        le31.base="http://tbx2rdf.lider-project.eu/converter/resource/cc/";
        le31.source="http://creativecommons.org/licenses/by/2.0/es/legalcode.es";
        ls3.addEntry(le31);
        LexicalEntry le32 = new LexicalEntry("obra derivada", "ca");
        le32.base="http://tbx2rdf.lider-project.eu/converter/resource/cc/";
        le32.source="http://creativecommons.org/licenses/by/3.0/es/legalcode.ca";
        ls3.addEntry(le32);
        LexicalEntry le33 = new LexicalEntry("obra derivada", "gl");
        le33.base="http://tbx2rdf.lider-project.eu/converter/resource/cc/";
        le33.source="http://creativecommons.org/licenses/by/3.0/es/legalcode.gl";
        ls3.addEntry(le33);
        LexicalEntry le34 = new LexicalEntry("òbra derivada", "oci");
        le34.base="http://tbx2rdf.lider-project.eu/converter/resource/cc/";
        le34.source="http://creativecommons.org/licenses/by/3.0/es/legalcode.oci";
        ls3.addEntry(le34);
        LexicalEntry le35 = new LexicalEntry("lan eratorririk", "eu");
        le35.base="http://tbx2rdf.lider-project.eu/converter/resource/cc/";
        le35.source="http://creativecommons.org/licenses/by/3.0/es/legalcode.eu";
        ls3.addEntry(le35);
        

        LexicalSense ls4= new LexicalSense("Derivative work (UK)");
        ls4.addDefinition("\"Derivative Work\" means any work created by the editing, modification, adaptation or translation of the Work in any media (however a work that constitutes a Collective Work will not be considered a Derivative Work for the purpose of this Licence). For the avoidance of doubt, where the Work is a musical composition or sound recording, the synchronization of the Work in timed-relation with a moving image (\"synching\") will be considered a Derivative Work for the purpose of this Licence.", "en");
        ls4.base="http://tbx2rdf.lider-project.eu/converter/resource/cc/";
        ls4.links.add("http://tbx2rdf.lider-project.eu/data/iate/IATE-74645");
        ls4.jurisdiction="http://dbpedia.org/page/United_Kingdom";
        LexicalEntry le41 = new LexicalEntry("derivative work", "en");
        le41.base="http://tbx2rdf.lider-project.eu/converter/resource/cc/";
        le41.source="http://creativecommons.org/licenses/by-nc-nd/2.0/uk/legalcode";
        ls4.addEntry(le41);
       
        
                
        
        senses.add(ls);
        senses.add(ls2);
        senses.add(ls3);
        senses.add(ls4);
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
