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
 * Imports data from an excel
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
            String fname = "F:\\svn\\tbx2rdfservice\\import.xls";
            WorkbookSettings ws = new WorkbookSettings();
            ws.setEncoding("cp1252");
            Workbook workbook = Workbook.getWorkbook(new File(fname), ws);
            Sheet sheet = workbook.getSheet(0);
            int nrows = sheet.getRows();
            for (int i = 2; i < nrows; i++) {
                String sense0 = sheet.getCell(0, i).getContents();
                LexicalSense ls3 = new LexicalSense(sense0);
                ls3.base = "http://tbx2rdf.lider-project.eu/converter/resource/cc/";
                
                if (!ls3.getURI().equals("http://tbx2rdf.lider-project.eu/converter/resource/cc/author"))
                {
                 //   continue;
                }
                String juris1 = sheet.getCell(1, i).getContents();
                if (!juris1.isEmpty()) {
                    ls3.jurisdiction = "http://dbpedia.org/resource/" + juris1;
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
                    parent4 = parent4.replace(" ", "%20");
                    parent4 = parent4.replace("(", "%28");
                    parent4 = parent4.replace(")", "%29");
                    ls3.parent = "http://tbx2rdf.lider-project.eu/converter/resource/cc/" + parent4;
                }

                String def5 = sheet.getCell(5, i).getContents();
                if (!def5.isEmpty()) {
                    String lan6 = sheet.getCell(6, i).getContents();
                    String source7 = sheet.getCell(7, i).getContents();
                    ls3.addDefinition(def5, lan6, source7);
                }
                String def8 = sheet.getCell(8, i).getContents();
                if (!def8.isEmpty()) {
                    String lan9 = sheet.getCell(9, i).getContents();
                    ls3.addDefinition(def8, lan9);
                }

                int ind = 10;
                while (true) {
                    String term10 = sheet.getCell(ind, i).getContents();
                    if (term10.isEmpty()) {
                        break;
                    }
                    String lan11 = sheet.getCell(ind+1, i).getContents();
                    String def12 = sheet.getCell(ind+2, i).getContents();
                    String source13 = sheet.getCell(ind+3, i).getContents();
                    String comentario14 = sheet.getCell(ind+4, i).getContents();
                    String fiabilidad15 = sheet.getCell(ind+5, i).getContents();
                    
                    LexicalEntry le31 = new LexicalEntry(term10, lan11);
                   // System.out.println(term10+" "+le31.getURI());
                    
                    le31.base = "http://tbx2rdf.lider-project.eu/converter/resource/cc/";
                    if (!def12.isEmpty()) {
                        le31.definition = def12;
                    }
                    if (!comentario14.isEmpty()) {
                        le31.comentario = comentario14;
                    }
                    if (!source13.isEmpty()) {
                        le31.source = source13;
                    }
                    if (!fiabilidad15.isEmpty()) {
                        le31.reliabilitycode = fiabilidad15;
                    }
                    ls3.addEntry(le31);
                    ind+=6;
                }
                senses.add(ls3);
            }
            
            for (LexicalSense sense : senses) {
                String nt = sense.getNT();
                String uri = sense.getURI();
                boolean ok = RDFStoreClient.post(uri, nt);

            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }

    }

}
