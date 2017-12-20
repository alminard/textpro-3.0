package eu.fbk.textpro.tester;

import static org.junit.Assert.*;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;

import org.junit.Test;

import eu.fbk.textpro.TextProModuleInterface;

public class lineCounter implements TesterInterface {

    @Test
    public void run(Object method, Object input, Object output, Object moduleName)
            throws IOException {
        System.out.println("@Test \""+method+"\" source file: "+input+", test file: "+output);
                    

        File fileDir = new File(input.toString());

        BufferedReader in = new BufferedReader(
                new InputStreamReader(
                        new FileInputStream(fileDir), "UTF8"));

        File fileDir1 = new File(output.toString());

        BufferedReader in1 = new BufferedReader(
                new InputStreamReader(
                        new FileInputStream(fileDir1), "UTF8"));

        int f1 =0,f2 =0;
        while ((in.readLine()) != null) {
            f1++;
        }


        while((in1.readLine()) != null) {
            f2++;
        }
        if (f1 != f2) {
            System.out.println("< "+f1+" lines\n> "+f2+" lines");
            System.out.println("= TEST FAILED!");
            assertFalse(true);
        }


        in.close();
        in1.close();

        assertTrue(true);
        System.out.println("= TEST PASSED!");
    }
}
