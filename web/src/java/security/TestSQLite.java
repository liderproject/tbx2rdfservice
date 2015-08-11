package security;

import java.sql.*;
import tbx2rdfservice.TBX2RDFServiceConfig;

/**
 * Class to test SQLITE access
 *
 * @author vroddon
 */
public class TestSQLite {

//    static String cadena = "jdbc:sqlite:test.db";

    public static void main(String args[]) {

        boolean test = test();
        try{
            createTables();
            addUser("victor","1234");
            addUser("a","b");
        }catch(Exception e){}
        boolean b = authenticate("a","c");
        boolean c = authenticate("victor", "1234");
        System.out.println("Resultado " + b+" "+c);
    }
    
    public static String getCadena()
    {
        String d=TBX2RDFServiceConfig.get("datafolder", ".");
        String cadena = "jdbc:sqlite:"+d+"/db";
        System.out.println(cadena);
        return cadena;
    }
    

    public static boolean test() {
        Connection c = null;
        try {
            Class.forName("org.sqlite.JDBC");
            c = DriverManager.getConnection(getCadena());
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public static boolean authenticate(String u, String p) {
        Connection c = null;
        Statement stmt = null;
        try {
            Class.forName("org.sqlite.JDBC");
            c = DriverManager.getConnection(getCadena());
            c.setAutoCommit(false);
            stmt = c.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT * FROM USERS;");
            while (rs.next()) {
                String us = rs.getString("USER");
                String pa = rs.getString("PASSWORD");
                if (us.equals(u) && pa.equals(p)) {
                    rs.close();
                    stmt.close();
                    c.close();
                    return true;
                }
            }
            rs.close();
            stmt.close();
            c.close();
        } catch (Exception e) {
            System.err.println(e.getClass().getName() + ": " + e.getMessage());
        }
        System.out.println("Operation done successfully");
        return false;
    }

    public static void addUser(String u, String p) {
        Connection c = null;
        Statement stmt = null;
        try {
            Class.forName("org.sqlite.JDBC");
            c = DriverManager.getConnection(getCadena());
            stmt = c.createStatement();
            String sql = "INSERT INTO USERS (USER,PASSWORD) "
                    + "VALUES ('"+u+"', '"+p+"');";
            stmt.executeUpdate(sql);
            stmt.close();
            c.close();
        } catch (Exception e) {
            System.err.println(e.getClass().getName() + ": " + e.getMessage());
        }
        System.out.println("Table created successfully");
    }

    public static void createTables() {
        Connection c = null;
        Statement stmt = null;
        try {
            Class.forName("org.sqlite.JDBC");
            c = DriverManager.getConnection(getCadena());
            stmt = c.createStatement();
            String sql = "CREATE TABLE USERS "
                    + "(USER             INT PRIMARY KEY     NOT NULL,"
                    + " PASSWORD           TEXT    NOT NULL)";
            stmt.executeUpdate(sql);
            stmt.close();
            c.close();
        } catch (Exception e) {
            System.err.println(e.getClass().getName() + ": " + e.getMessage());
        }
        System.out.println("Table created successfully");
    }

}
