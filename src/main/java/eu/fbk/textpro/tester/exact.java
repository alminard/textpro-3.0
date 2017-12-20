package eu.fbk.textpro.tester;

import static org.junit.Assert.*;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;

import org.junit.Test;


public class exact implements TesterInterface {

    @Test
    public void run(Object method, Object input, Object output, Object moduleName)
            throws IOException {
        System.out.println("@Test \""+method+"\" source file: "+input+", test file: "+output);


        File fileDir = new File(input.toString());

        BufferedReader in = new BufferedReader(
                new InputStreamReader(
                        new FileInputStream(fileDir), "UTF8"));

        String str;
        File fileDir1 = new File(output.toString());

        BufferedReader in1 = new BufferedReader(
                new InputStreamReader(
                        new FileInputStream(fileDir1), "UTF8"));

        String str1;
        String headerIN = "", headerIN2 ="";
        while ((str = in.readLine()) != null) {
            if (!str.startsWith("# "))
                break;
            else {
                if (!str.startsWith("# FILE:") && !str.startsWith("# TIMESTAMP:"))
                    headerIN += str;
            }
        }
        while ((str1 = in1.readLine()) != null) {
            if (str1.startsWith("# ")) {
                if (!str1.startsWith("# FILE:") && !str1.startsWith("# TIMESTAMP:")) {
                    headerIN2 += str1;
                    if (str1.startsWith("# FIELDS:")) {
                        if (!headerIN2.equals(headerIN)) {
                            System.out.println("< " + str + "\n> "+ str1 +"\n");
                            System.out.println("= TEST FAILED!\n");

                            assertFalse(true);
                        }
                    }
                }
                continue;
            }
            //System.out.println(str+"\n"+str1);
            assert str != null;
            if (!str.equals(str1)) {

                System.out.println("< " + str + "\n> "+ str1 +"\n");
                System.out.println("= TEST FAILED!\n");

                assertFalse(true);
            }
            str = in.readLine();
        }

        in.close();
        in1.close();

        assertTrue(true);
        System.out.println("= TEST PASSED!");
    }
}
