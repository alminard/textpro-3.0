package eu.fbk.textpro.modules.lemmapro;

import java.io.IOException;

import javax.xml.bind.JAXBException;
import org.junit.*;

import static org.junit.Assert.*;
import java.util.*;
public class junitTestLemmapro {

	/**
	 * @param args
	 * @throws JAXBException 
	 * @throws IOException 
	 */

	lemmaProTester test = new lemmaProTester();
	
	/*Default return value to be tested
	 * 
	 returnValue.put("processedLines", i);
	 returnValue.put("totalLines", total);
	 returnValue.put("copiedLines", (total-i));
	 returnValue.put("emptyLines",emptyReply );
	 returnValue.put("RprocessedLines", (i*100/total));
	 returnValue.put("processedCorrectlyLines", (i-emptyReply));
	 returnValue.put("RprocessedCorrectlyLines", ((i-emptyReply)*100/total));
	 * 
	 * 
	 */
	
	
    @Test
    public void testReadFromString() throws IOException, JAXBException {
		String send = "entrambi	DS	entrambi+adj+m+sing+pst+ind	entrambi+adj+m+sing+pst+ind entrambi+pron+_+m+3+sing+ind";
    	String[] parms = {"-s",send,"-l","italian"};
    	 Hashtable op = test.main(parms);
    	boolean ans = false;
    	if (Integer.parseInt(op.get("RprocessedCorrectlyLines").toString()) > 70)
    		ans = true;
    	assertTrue(ans);
        System.out.println("@Test - testReadFromString");
    }
 
    @Test
    public void testReadFromFile() throws IOException, JAXBException {
    	String[] parms = {"-f","itaA.txp","-l","italian"};
    	Hashtable op = test.main(parms);
    	boolean ans = false;
    	if (Integer.parseInt(op.get("RprocessedCorrectlyLines").toString()) > 70)
    		ans = true;
    	assertTrue(ans);
        System.out.println("@Test - testReadFromFile");
    }
}
