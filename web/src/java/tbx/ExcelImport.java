package tbx;

import java.io.File;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import jxl.*;
import lemon.LexicalEntry;
import lemon.LexicalSense;
import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.varia.NullAppender;
import tbx2rdfservice.store.RDFStoreClient;
import tbx2rdfservice.store.RDFStoreFuseki;
import tbx2rdfservice.store.RDFUtil;

/**
 * Imports data from an excel
 * @author admin
 */
public class ExcelImport {

    public static void main(String[] args) {
        BasicConfigurator.configure();
        org.apache.log4j.Logger.getRootLogger().removeAllAppenders();
        org.apache.log4j.Logger.getRootLogger().addAppender(new NullAppender());
        String base ="http://copyrighttermbank.linkeddata.es/resource/";
        String filename = "F:\\svn\\tbx2rdfservice\\import.xls";
        importar(filename, base);
    }

    public static void importar(String fname, String base) {
        List<LexicalSense> senses = new ArrayList();
        try {
            WorkbookSettings ws = new WorkbookSettings();
            ws.setEncoding("cp1252");
            Workbook workbook = Workbook.getWorkbook(new File(fname), ws);
            Sheet sheet = workbook.getSheet(0);
            int nrows = sheet.getRows();
            
            /**
             *  CADA FILA ES UN LEXICAL SENSE
             */
            for (int i = 2; i < nrows; i++) {
                System.out.println("File: " + i);
                try{
                String lexicalsensename = sheet.getCell(0, i).getContents();
                
                LexicalSense lexicalsense = new LexicalSense(lexicalsensename);
                lexicalsense.base = base;
                
                if (!lexicalsense.getURI().equals("http://tbx2rdf.lider-project.eu/converter/resource/cc/author"))
                {
                 // hacer algo específico
                 //   continue;
                }
                String jurisdiction = sheet.getCell(1, i).getContents();
                if (!jurisdiction.isEmpty()) {
                    lexicalsense.jurisdiction = "http://dbpedia.org/resource/" + jurisdiction;
                }

                String iateid = sheet.getCell(2, i).getContents();
                if (!iateid.isEmpty()) {
                    lexicalsense.links.add("http://tbx2rdf.lider-project.eu/data/iate/IATE-" + iateid);
                }

                String lemon_reference = sheet.getCell(3, i).getContents();
                if (!lemon_reference.isEmpty()) {
                    lexicalsense.reference = lemon_reference;
                }

                String parentconcept = sheet.getCell(4, i).getContents();
                if (!parentconcept.isEmpty()) {
                    parentconcept = parentconcept.trim();
                    parentconcept = parentconcept.replace(" ", "%20");
                    parentconcept = parentconcept.replace("(", "%28");
                    parentconcept = parentconcept.replace(")", "%29");
                    lexicalsense.parent = "http://tbx2rdf.lider-project.eu/converter/resource/cc/" + parentconcept;
                }

                //Definition, language and source.
                String definition1 = sheet.getCell(5, i).getContents();
                definition1 = RDFUtil.escapeNTriples(definition1);
                if (!definition1.isEmpty()) {
                    String definition1lan = sheet.getCell(6, i).getContents();
                    String definition1source = sheet.getCell(7, i).getContents();
                    lexicalsense.addDefinition(definition1, definition1lan, definition1source);
                }
                
                String comment = sheet.getCell(8, i).getContents();
                if (comment.length()>0)
                {
                    comment = RDFUtil.escapeNTriples(comment);
                    //ByteBuffer encode = Charset.forName("UTF-8").encode(comment);
                    //comment = new String( encode.array(), Charset.forName("UTF-8") );
                    lexicalsense.comment = comment;
                }


                int ind = 10;
                while (true) {
                    String term10 = sheet.getCell(ind, i).getContents();
                    if (term10.isEmpty()) {
                        break;
                    }
                    String lexicalentrylani = sheet.getCell(ind+1, i).getContents();
                    String lexicalentrydefinitioni = sheet.getCell(ind+2, i).getContents();
                    String lexicalentrysourcei = sheet.getCell(ind+3, i).getContents();
                    String lexicalentrycomentarioi = sheet.getCell(ind+4, i).getContents();
                    String lexicalentryfiabilidadi = sheet.getCell(ind+5, i).getContents();
                    
                    
                    lexicalentrydefinitioni = RDFUtil.escapeNTriples(lexicalentrydefinitioni);
                    lexicalentrysourcei = RDFUtil.escapeNTriples(lexicalentrysourcei);
                    lexicalentrycomentarioi = RDFUtil.escapeNTriples(lexicalentrycomentarioi);

                    
                    LexicalEntry lexicalentry = new LexicalEntry(term10, lexicalentrylani);
                   // System.out.println(term10+" "+le31.getURI());
                    
                    lexicalentry.base = "http://tbx2rdf.lider-project.eu/converter/resource/cc/";
                    
                    //THE DEFINITIONS NOW GO TO THE SENSE
                    if (!lexicalentrydefinitioni.isEmpty()) {
//                        lexicalentry.definition = lexicalentrydefinitioni;
                        

                        lexicalsense.addDefinition(lexicalentrydefinitioni, lexicalentrylani, lexicalentrysourcei);
                        /*
                        lexicalsense.definitions.add(lexicalentrydefinitioni);
                        String source ="";
                        if (!lexicalentrysourcei.isEmpty()) {
                            source = lexicalentrysourcei;
                        }
                        String lan ="";
                        if (!lexicalentrylani.isEmpty()) {
                            lan =  lexicalentrylani ;
                        }
                        lexicalsense.definitionsources.add(source);
                        lexicalsense.definitionlans.add(lan);
                        */
                    }
                    
                    if (!lexicalentrycomentarioi.isEmpty()) {
                        lexicalentry.comentario = lexicalentrycomentarioi;
                    }
                    if (!lexicalentryfiabilidadi.isEmpty()) {
                        lexicalentry.reliabilitycode = lexicalentryfiabilidadi;
                    }
                    lexicalsense.addEntry(lexicalentry);
                    ind+=6;
                }
                senses.add(lexicalsense);
                }catch(Exception e)
                {
                    System.err.println("Mal la linea: " + i);
                }
            }
            int oks=0;
            int noks=0;
            for (LexicalSense sense : senses) {
                String nt = sense.getNT();
                String uri = sense.getURI();
                boolean ok = RDFStoreClient.post(uri, nt);
               if (ok)
                   oks++;
                
               else{
                    System.out.println("URI: " +uri);
                    System.out.println("TRIPLES: " + nt);
                   noks++;
               }
            }
            System.out.println("Procesados un total de " + senses.size()+". Bien: " + oks);

        } catch (Exception ex) {
            ex.printStackTrace();
        }

    }

}
