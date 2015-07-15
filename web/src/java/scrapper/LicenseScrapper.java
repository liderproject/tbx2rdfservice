
package scrapper;

import java.io.File;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.ArrayList;
import java.util.List;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.parser.Tag;
import org.jsoup.select.Elements;
import java.io.File;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
/**
 *
 * @author admin
 */
public class LicenseScrapper {
    
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        
        test1();
    }

    //
    public static void test1()
    {
      String file="D:\\Usuarios\\admin\\Desktop\\ca.html";
      
        File input = new File(file);
//        String url = "https://creativecommons.org/licenses/by/4.0/legalcode";
        String url="http://creativecommons.org/licenses/by/3.0/es/legalcode.ca";
        Document doc = null;
        try {
            doc = Jsoup.connect(url).get();
  //          doc = Jsoup.parse(input, "UTF-8", "http://example.com/");      
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }
        String title = doc.title();
//        System.out.println(title);

        Elements elements = doc.getElementsByTag("body");
        elements = elements.get(0).getAllElements();
        boolean init=false;
        String definicion="";
        String definido="";
//        List<String> terms = new ArrayList();
        Map<String, String> terms = new HashMap();
        for(Element element:elements)
        {
            Tag tag=element.tag();
            String stag=tag.toString();
            if (stag.contains("body") || stag.contains("ol"))
                continue;
            String text = element.text();
            if (text.contains("1. Definiciones")||(text.contains("1. Definições") || (text.contains("Section 1 – Definitions")) || (text.contains("1. Definicions.")) || text.contains("1. Definitions")) && stag.equals("strong"))
                init=true;
            if ((text.contains("2. Límites")||text.contains("2. Limits des drets") ||text.contains("2. Limitações") || text.contains("Section 2 – Scope") || text.contains("License Terms")) && stag.equals("strong"))
                init=false;
                   
            if (init)
            {
//                System.out.println(init+" "+tag+" "+element.text());
                if (stag.equals("li"))
                    definicion=text;
                if (stag.equals("strong"))
                {
                    definido=text;
                    if (!definido.equals("1. Definiciones"))
                        terms.put(definido, definicion);
                }
            }
        } //FIN DEL PARSING
        
        Iterator it = terms.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry e = (Map.Entry)it.next();
            String term = (String)e.getKey();
            String def = (String)e.getValue();
            System.out.println(term + "\t" + def);
        }
        
    }
    
}
