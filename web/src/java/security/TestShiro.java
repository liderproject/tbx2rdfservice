package security;

import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.*;
import org.apache.shiro.config.IniSecurityManagerFactory;
import org.apache.shiro.mgt.SecurityManager;
import org.apache.shiro.session.Session;
import org.apache.shiro.subject.Subject;
import org.apache.shiro.util.Factory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author vroddon
 */
public class TestShiro {

    private static final transient Logger log = LoggerFactory.getLogger(TestShiro.class);

    public static void main(String[] args) {

        //1.
        Factory<SecurityManager> factory = new IniSecurityManagerFactory("classpath:security/shiro.ini");

        //2.
        SecurityManager securityManager = factory.getInstance();

        //3.
        SecurityUtils.setSecurityManager(securityManager);
        Subject currentUser = SecurityUtils.getSubject();
        Session session = currentUser.getSession();
        session.setAttribute("someKey", "aValue");
        if (!currentUser.isAuthenticated()) {
            //collect user principals and credentials in a gui specific manner 
            //such as username/password html form, X509 certificate, OpenID, etc.
            //We'll use the username/password example here since it is the most common.
            UsernamePasswordToken token = new UsernamePasswordToken("lonestarr", "vespa");

            //this is all you have to do to support 'remember me' (no config - built in!):
            token.setRememberMe(true);

            try {
                currentUser.login(token);
            } catch (UnknownAccountException uae) {
                System.out.println("uae");
            } catch (IncorrectCredentialsException ice) {
                System.out.println("ice");
            } catch (LockedAccountException lae) {
                System.out.println("lae");
            } catch (AuthenticationException ae) {
                System.out.println("ae");

            }
            System.out.println( "User [" + currentUser.getPrincipal() + "] logged in successfully." );      
            if ( currentUser.isPermitted( "lightsaber:weild" ) ) {
                System.out.println("You may use a lightsaber ring.  Use it wisely.");
            } else {
                System.out.println("Sorry, lightsaber rings are for schwartz masters only.");
            }        
            if ( currentUser.isPermitted( "winnebago:drive:eagle5" ) ) {
                System.out.println("You are permitted to 'drive' the 'winnebago' with license plate (id) 'eagle5'.  " +
                            "Here are the keys - have fun!");
            } else {
                System.out.println("Sorry, you aren't allowed to drive the 'eagle5' winnebago!");
            }            
            currentUser.logout();
        }
        

        System.exit(0);
    }
}
