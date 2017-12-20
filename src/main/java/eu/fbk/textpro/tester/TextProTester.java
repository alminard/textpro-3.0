/**
 *
 */
package eu.fbk.textpro.tester;

import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Random;

import javax.xml.bind.JAXBException;

import org.junit.Test;

import eu.fbk.textpro.toolbox.MYProperties;
import eu.fbk.textpro.toolbox.toolbox;
import eu.fbk.textpro.wrapper.TextProPipeLine;

/**
 * @author qwaider
 *
 */

public class TextProTester {

    static String moduleName ="";
    static String method ="";
    static String language ="";
    static String file ="";
    static String testfile ="";
    static boolean runTextpro = true;



    public static void main(String[] as) {
    	//as[as.length-1]; config.properties
        init(as);

        /// run our pipeline on the input file
        File output = null;
        if (runTextpro) {
            Random rn = new Random(System.nanoTime());
            String arf[]= new String[9];
            arf[0] = "-l";
            arf[1] = language;
            arf[2] = "-c";
            arf[3] = moduleName;
            arf[4] = "-o";
            arf[5] = "/tmp/";
            arf[6] = "-n";
            arf[7] = "tmp" + rn.nextLong() + ".txp";
            arf[8] = file;
            //arf[2] = "test/"+moduleName+"/tmp/"+file;

            try {
            	TextProPipeLine run = new TextProPipeLine();
            	String conf = as[as.length-1];
        		System.err.println("Conf file: " + conf);
        		MYProperties prop = new MYProperties(conf);
        		toolbox tools = new toolbox(prop);
            	tools.inputLine(arf);
            	run.TextProPipeLine(tools);
            	
                output = new File(arf[5], arf[7]);
                if(output.exists())
                    file = output.getCanonicalPath();
                else
                    assertFalse(true);
            }  catch (IOException e) {
                e.printStackTrace();
            } 
        }

        try {
            Class clazz = Class.forName(method);
            if (clazz == null) {
                System.err.println("TextProTester: ERROR! The class " + method + " doesn't exists.");
                System.exit(0);
            }
            Class[] parameters = new Class[] {Object.class, Object.class, Object.class, Object.class};
            Method method1 = clazz.getDeclaredMethod("run", parameters);
            Object obj = clazz.newInstance();

            Object initob = method1.invoke(obj, method, file, testfile, moduleName);
            if (runTextpro) {
                if (output != null) {
                    output.delete();
                }
            }
        } catch (InvocationTargetException e) {

        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }

    }



    private static void init(String[] args) {

        for(int i=0;i < args.length;i++) {
            if ( args[i].equals("-c")) {
                moduleName = args[++i];
            }else if ( args[i].equals("-e")) {
                method = args[++i];
            }else if ( args[i].equals("-l")) {
                language = args[++i];
            }else if ( args[i].equals("-t")) {
                testfile = args[++i];
                File testFile = new File(testfile);
                if (!testFile.exists()) {
                    System.err.println("TextProTester: ERROR! The test file doesn't exists.");
                    System.exit(0);
                }
            }else if ( args[i].equals("-f")) {
                file = args[++i];
                File inputFile = new File(file);
                if (!inputFile.exists()) {
                    System.err.println("TextProTester: ERROR! The input file doesn't exists.");
                    System.exit(0);
                }
            }else if ( args[i].equals("-runTextpro")) {
                if (args[++i].equals("false"))
                    runTextpro = false;
            }else if ( args[i].equals("-h")) {
                System.out.println("Tester for TextPro options:\n -c <column or header name>\t Example:\"-c full_morpho\" \n " +
                        "-e <CLASS>\t test CLASS. Ex:\"-e eu.fbk.textpro.tester.exact\" \n " +
                        "-l <LANG>\t LANG could be 'eng' or 'ita'. Ex:\"-l eng\" \n " +
                        "-runTextpro [true | false]\t to activate/disactivate running textpro pipeline. The default is true. Ex:\"-runTextpro false\". " +
                        "-t <FILE>\t file to check as gold standard. Ex:\"-t example_eng.txp\" \n " +
                        "-f <FILE>\t input file name. If -runTextpro is set to false it is compared to the gold standard, otherwise it is taken as input of the TextPro. Ex:\"-t example_eng.txt\" \n ");
                System.exit(-1);
            }

        }

    }

}
