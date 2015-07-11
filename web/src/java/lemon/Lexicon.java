package lemon;

/**
 *
 * @author admin
 */
public class Lexicon {
 
    String uri="";
    String lan="";
    
    public String getNT()
    {
        String nt="";
        nt+="<"+uri+"> <http://www.w3.org/ns/lemon/ontolex#language> \""+ lan +"\" .\n";
        nt+="<"+uri+"> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://www.w3.org/ns/lemon/ontolex#Lexicon> .\n";
        return nt;
    }
    
}
