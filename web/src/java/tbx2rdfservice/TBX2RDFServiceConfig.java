package tbx2rdfservice;

import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Properties;
import java.util.TreeSet;
import org.apache.log4j.Logger;

/**
 * De donde se lee la configuración?
 * @author admin
 */
public class TBX2RDFServiceConfig {

    private final static String CONFIGFILE = "tbx2rdfservice.config";
    static boolean loaded = false;

    //Propiedades
    static Properties prop = new Properties();

    public static void main(String args[]) {
        String d=TBX2RDFServiceConfig.get("datafolder", ".");
        System.out.println(d);
    }
    
    
    /**
     * Obtiene el valor de una propiedad, y si no lo tiene da un valor por
     * defecto
     *
     * @param p Propiedad
     * @param valor Valor por defecto
     * @return El valor leido o el de por defecto
     */
    public static String get(String p, String valor) {
        if (!loaded) {
            LoadEmbedded();
        }
        return prop.getProperty(p, valor);
    }

    /**
     * Estable el valor de una propiedad
     *
     * @param p Propiedad
     * @param valor Valor
     */
    public static void set(String p, String defvalue) {
        prop.setProperty(p, defvalue);
        Store();
    }

    /**
     * Carga los parametros de configuracion del archivo contenidos.config
     *
     * @return True si todo fue bien
     */
    public static boolean LoadEmbedded() {
        try {
            InputStream is = new FileInputStream(CONFIGFILE);
            prop.load(is);
 //           System.out.println("Successfuly read " + CONFIGFILE + " from the folder");
            return true;
        } catch (Exception ex) {
            try {
                InputStream is_local = TBX2RDFServiceConfig.class.getResourceAsStream(CONFIGFILE);
                prop.load(is_local);
 //               System.out.println("Successfuly read the embedded " + CONFIGFILE);
                return true;

            } catch (Exception ex2) {
                System.out.println("NO CONFIG FILE AT ALL " + CONFIGFILE + " at " + System.getProperty("user.dir"));
                ex.printStackTrace(); //todavía no está el logger
                return false;
            }
        }
    }

    public static boolean LoadEmbeddedOLD() {
        try {
            InputStream is_local = TBX2RDFServiceConfig.class.getResourceAsStream(CONFIGFILE);
            prop.load(is_local);
            Logger.getLogger("rdfchess").info("Config file read from " + CONFIGFILE);
            return true;
        } catch (Exception ex) {
//            ex.printStackTrace(); //todavía no está el logger
            System.out.println("There was no internal file " + CONFIGFILE + " at " + System.getProperty("user.dir"));
            InputStream is;
            try {
                is = new FileInputStream(CONFIGFILE);
                prop.load(is);
                System.out.println("Successfuly read " + CONFIGFILE + " from the folder");
                Logger.getLogger("rdfchess").info("Config file read from " + CONFIGFILE);
                return true;
            } catch (Exception ex2) {
                System.out.println("NO CONFIG FILE AT ALL " + CONFIGFILE + " at " + System.getProperty("user.dir"));
                ex.printStackTrace(); //todavía no está el logger
                return false;
            }
        }
    }

    /**
     * Almacena los parámetros de configuración en el archivo Este archivo no es
     * configurable El orden de almacenamiento en archivo es alfabético
     */
    public static void Store() {
        OutputStream os;
        try {
            Properties tmp = new Properties() {
                @Override
                public synchronized Enumeration<Object> keys() {
                    return Collections.enumeration(new TreeSet<Object>(super.keySet()));
                }
            };
            tmp.putAll(prop);
            tmp.store(new FileWriter(CONFIGFILE), null);
        } catch (Exception ex) {
            Logger.getLogger("rdfchess").error("Error opening config file" + ex.toString());
            System.out.println("Error trying to write config file");
        }
    }
}
