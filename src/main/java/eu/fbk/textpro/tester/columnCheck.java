package eu.fbk.textpro.tester;

import static org.junit.Assert.*;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.regex.Pattern;

import org.junit.Test;


public class columnCheck implements TesterInterface {

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
        boolean reachHeader1 = false,reachHeader2=false;
        int indexCol1 = 0, indexCol2=0;
        while ((str1 = in1.readLine()) != null&&(str = in.readLine()) != null) {
            System.out.println(str+"\n"+str1);
            if (!reachHeader1&&!reachHeader1) {
                while(!reachHeader1&&!reachHeader1) {
                    if (!reachHeader1) {
                        if (str.startsWith("# FIELDS: ")) {
                            str = str.replaceAll("# FIELDS: ", "");
                            String[] cols = Pattern.compile("\t").split(str);
                            for(int i=0;i<cols.length;i++) {
                                if (cols[i].equals(moduleName.toString())) {
                                    indexCol1 = i;
                                }
                            }
                            reachHeader1=true;
                        }
                        str = in.readLine();
                    }
                    if (!reachHeader2) {
                        if (str1.startsWith("# FIELDS: ")) {
                            str1 = str1.replaceAll("# FIELDS: ", "");
                            String[] cols1 = Pattern.compile("\t").split(str1);
                            for(int iq=0;iq<cols1.length;iq++) {
                                if (cols1[iq].equals(moduleName.toString())) {
                                    indexCol2 = iq;
                                }
                            }
                            reachHeader2=true;
                        }
                        str1 = in1.readLine();
                    }

                }
            }else{
                // after know the indexs and read the headers
                String[] row1 = Pattern.compile("\t").split(str);
                String[] row2 = Pattern.compile("\t").split(str1);
                if (!row1[indexCol1].equals(row2[indexCol2])) {
                    System.out.println("< " + str + "\n> "+ str1);
                    System.out.println("= TEST FAILED!");

                    assertFalse(true);
                }
            }


        }

        in.close();
        in1.close();

        assertTrue(true);
        //assertFalse(true);
        System.out.println("= TEST PASSED!");
    }
}
