package tbx;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import jxl.*;
import lemon.LexicalEntry;
import lemon.LexicalSense;
import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.varia.NullAppender;
import tbx2rdfservice.store.RDFStoreClient;

/**
 *
 * @author admin
 */
public class ExcelImport {

    public static void main(String[] args) {
        BasicConfigurator.configure();
        org.apache.log4j.Logger.getRootLogger().removeAllAppenders();
        org.apache.log4j.Logger.getRootLogger().addAppender(new NullAppender());
        importar();
    }

    public static void importar() {
        List<LexicalSense> senses = new ArrayList();
        try {
            String fname = "D:\\Dropbox\\_COMPARTIDAS\\CRISTIANAVICTOR\\output\\2015 Terminology of legal terms in common licenses RANLP 2015\\import.xls";
            WorkbookSettings ws = new WorkbookSettings();
            ws.setEncoding("cp1252");
            Workbook workbook = Workbook.getWorkbook(new File(fname),ws);
            Sheet sheet = workbook.getSheet(0);
            int nrows = sheet.getRows();
            for (int i = 2; i < nrows; i++) {
                String sense0 = sheet.getCell(0, i).getContents();
                LexicalSense ls3 = new LexicalSense(sense0);
                ls3.base = "http://tbx2rdf.lider-project.eu/converter/resource/cc/";

                String juris1 = sheet.getCell(1, i).getContents();
                if (!juris1.isEmpty()) {
                    ls3.jurisdiction = "http://dbpedia.org/resource/"+juris1;
                }

                String iate2 = sheet.getCell(2, i).getContents();
                if (!iate2.isEmpty()) {
                    ls3.links.add("http://tbx2rdf.lider-project.eu/data/iate/IATE-" + iate2);
                }

                String reference3 = sheet.getCell(3, i).getContents();
                if (!reference3.isEmpty()) {
                    ls3.reference = reference3;
                }

                String parent4 = sheet.getCell(4, i).getContents();
                if (!parent4.isEmpty()) {
                    parent4=parent4.replace(" ", "%20");
                    parent4=parent4.replace("(", "%28");
                    parent4=parent4.replace(")", "%29");
                    ls3.parent = "http://tbx2rdf.lider-project.eu/converter/resource/cc/"+parent4;
                }

                String def5 = sheet.getCell(5, i).getContents();
                if (!def5.isEmpty()) {
                    String lan6 = sheet.getCell(6, i).getContents();
                    ls3.addDefinition(def5, lan6);
                }
                String def7 = sheet.getCell(7, i).getContents();
                if (!def7.isEmpty()) {
                    String lan8 = sheet.getCell(8, i).getContents();
                    ls3.addDefinition(def7, lan8);
                }
                
                String term10 = sheet.getCell(10, i).getContents();
                if (!term10.isEmpty())
                {
                    String lan11 = sheet.getCell(11, i).getContents();
                    String source13 = sheet.getCell(13, i).getContents();
                    String comentario14 = sheet.getCell(14, i).getContents();
                    LexicalEntry le31 = new LexicalEntry(term10, lan11);
                    le31.base="http://tbx2rdf.lider-project.eu/converter/resource/cc/";
                    if (!comentario14.isEmpty())
                        le31.comentario=comentario14;
                    if (!source13.isEmpty())
                        le31.source=source13;
                    ls3.addEntry(le31);
                }
                String term15 = sheet.getCell(15, i).getContents();
                if (!term15.isEmpty())
                {
                    String lan16 = sheet.getCell(16, i).getContents();
                    String source18 = sheet.getCell(18, i).getContents();
                    String comentario19 = sheet.getCell(19, i).getContents();
                    LexicalEntry le31 = new LexicalEntry(term15, lan16);
                    le31.base="http://tbx2rdf.lider-project.eu/converter/resource/cc/";
                    if (!comentario19.isEmpty())
                        le31.comentario=comentario19;
                    if (!source18.isEmpty())
                        le31.source=source18;
                    ls3.addEntry(le31);
                }
                senses.add(ls3);
            }
            for(LexicalSense sense : senses)
            {
                System.out.println(sense.getURI());
                boolean ok=RDFStoreClient.post(sense.getURI(), sense.getNT());

            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }

    }

}
