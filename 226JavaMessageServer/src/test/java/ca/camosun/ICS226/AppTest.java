package ca.camosun.ICS226;

import static org.junit.Assert.assertTrue;

import org.junit.Test;
import java.io.FileNotFoundException;
//import org.junit.jupiter.api.Assertions;
//import org.junit.jupiter.api.Test;
import java.security.GeneralSecurityException;

/**
 * Unit test for simple App.
 */
public class AppTest 
{
    /**
     * Rigorous Test :-)
     */
    @Test
    public void shouldAnswerWithTrue()
    {
        assertTrue( true );
    }

    @Test
    public void testServer() throws Exception
    {
        System.out.println("[TEST 1]");
        App s = new App(12345);
        System.out.println("[TEST 2]");
        //s.serve();
        System.out.println("[TEST 3]");




        Client c = new Client("10.21.75.71", 12345, "12345678Test");
        System.out.println("[TEST 4]");
        //c.connect();

        
        System.out.println("[START Test Server]");
        //System.out.println(c.getReply());
        System.out.println("[END Test Server]");
        
        //assertEquals("12345678Test", c);
    }

}

