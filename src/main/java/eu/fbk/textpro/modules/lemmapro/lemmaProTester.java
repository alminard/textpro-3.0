/**
 *
 */
package eu.fbk.textpro.modules.lemmapro;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.ListIterator;
import java.util.regex.Pattern;

import javax.xml.bind.JAXBException;

/**
 * @author qwaider
 *
 */
public class lemmaProTester {

    public static Hashtable main(String[] args) throws IOException, JAXBException {
        InputStream in = null;
        Hashtable<String, Integer> returnValue = new  Hashtable<String, Integer>();
        if (args[0].equalsIgnoreCase("-f")) {
            String file = args[1];
            in = new FileInputStream(file);
        }else if (args[0].equalsIgnoreCase("-s")) {
            String myString = args[1];
            in = new ByteArrayInputStream( myString.getBytes( "UTF8" ) );
        }else if (args[0].equalsIgnoreCase("-stdin")) {
            in = (System.in) ;
        }else{
            System.out.println("Error of calling lemmapro! "+args[0]);
            System.exit(-1);
        }
        String language = "english";
        if (args.length>2&&args[2].equalsIgnoreCase("-l"))
            language = args[3];

        Reader reader = new InputStreamReader(in, "UTF8");
        BufferedReader br = new BufferedReader(reader);
        File fileDir = new File("lemmaResult.txt");

        Writer out = new BufferedWriter(new OutputStreamWriter(
                new FileOutputStream(fileDir), "UTF8"));
        String line;
        int i=0,total=0,emptyReply=0;

        lemmaConfig test = new  lemmaConfig();
        test.readConfigFile(language);
        while((line = br.readLine()) != null) {
            Hashtable<String, String> ansSet = new Hashtable<String, String>();
            String[] lineTokens = Pattern.compile("\t").split(line);
            total++;
            if (lineTokens.length>3) {
                //System.out.println(i+"line="+lineTokens[3]);
                i++;
                String[] chek = Pattern.compile(" ").split(lineTokens[3]);
                //confirm+v+indic+past confirm+v+part+past confirmed+adj+zero i should check one by one.
                for(int u =0;u<chek.length;u++) {
                    if (test.morphoMatcher(lineTokens[1],chek[u])) {
                        ansSet.put(chek[u], "");
                    }
                }
                // end of checking all the patterns
                // adding the ansSet to the outputfile!
                String an3rd="";
                Iterator ansSetl = ansSet.keySet().iterator();
                while(ansSetl.hasNext()) {
                    Object anst = ansSetl.next();
                    an3rd+=" "+anst.toString();
                    /*if (!lineTokens[2].contains(anst.toString()))
                          System.err.println(i+"error ========="+anst.toString());
                          */
                }
                an3rd = "\""+an3rd;
                an3rd = an3rd.replaceAll("\" ", "");
                an3rd = an3rd.replaceAll("\"", "");
                if (an3rd.length()<1) {
                    emptyReply++;
                }
                out.append(lineTokens[0]+"	"+lineTokens[1]+"	"+an3rd+"	"+lineTokens[3]).append("\n");
                ansSet.clear();
                //System.out.println(i+"==========");
            }else{
                out.append(line).append("\n");
                //System.err.println("error Column 4 is empty="+line);
            }
        }
        System.out.println("System finish executing," +
                " we have processed "+i+" lines/ "+total+" lines." +
                "\n Un-catched lines copied to their original place!\n"+
                "There are "+(total-i)+" lines copied without processing.");
        System.out.println("The rational percentage of the processed line is "+(float)(i*100/total)+"%");
        System.out.println(emptyReply+" lines are empty reply, and "+(i-emptyReply)+" line are processed correctly, from "+i+" checked lines." );
        System.out.println("The rational percentage of the correctly processed line is "+(float)((i-emptyReply)*100/total)+"%");

        /*    Runtime runtime = Runtime.getRuntime();

        int mb = 1024*1024;
        //Get the jvm heap size.
        long heapSize = runtime.totalMemory();

        //Print the jvm heap size.
        System.out.println("Heap Size: " + heapSize / mb);

        System.out.println("Used Memory:" + (heapSize - runtime.freeMemory()) / mb);
        */
        out.flush();
        out.close();
        returnValue.put("processedLines", i);
        returnValue.put("totalLines", total);
        returnValue.put("copiedLines", (total-i));
        returnValue.put("emptyLines",emptyReply );
        returnValue.put("RprocessedLines", (i*100/total));
        returnValue.put("processedCorrectlyLines", (i-emptyReply));
        returnValue.put("RprocessedCorrectlyLines", ((i-emptyReply)*100/total));
        return returnValue;
    }

}
