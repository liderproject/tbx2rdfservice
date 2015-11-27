
package tbx;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.varia.NullAppender;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

/**
 *
 * @author vrodriguez
 */
public class HTMLImport {
    public static void main(String[] args) {
        BasicConfigurator.configure();
        org.apache.log4j.Logger.getRootLogger().removeAllAppenders();
        org.apache.log4j.Logger.getRootLogger().addAppender(new NullAppender());
        importar();
    }

    public static void importar()
    {
        String url="http://cosasbuenas.es/s/wipo1.html";
        Document doc = null;
        try {
            doc = Jsoup.connect(url).get();
  //          doc = Jsoup.parse(input, "UTF-8", "http://example.com/");      
            System.out.println("ok");
        } catch (Exception e) {
            System.out.println("mal");
            e.printStackTrace();
            return;
        }
        String title = doc.title();
        Elements els = doc.getAllElements();
        boolean b = false;
        String termino ="";
        String def ="";
        for(Element el : els)
        {
            String estilo = el.attr("style");
            if (estilo.contains("#0290C7") )
            {
                termino = el.text();
                if (termino.equals("Published edition"))
                {
                    System.out.println("edicion publicada");
                }
                
  //              System.out.println("TITULO: " + el.text());
                b=true;
                continue;
            }
            if (!estilo.contains("#0290C7"))
            {
                if (b==false)
                    continue;
                def = el.text();
                b=false;
                if (!def.startsWith("See "))
                {
                    System.out.println(termino + "\t" + def);
                }
                
            }
        }
        System.out.println(title);
        
    }
    
}
