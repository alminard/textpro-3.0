package eu.fbk.textpro.modules.tagpro;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.Scanner;

import eu.fbk.textpro.TextProModuleInterface;
import eu.fbk.textpro.ml.MLMallet;
import eu.fbk.textpro.ml.learner;
import eu.fbk.textpro.modules.bin.M1Para;
import eu.fbk.textpro.modules.tagpro.ProcessFeatures;
import eu.fbk.textpro.toolbox.MYProperties;
import eu.fbk.textpro.toolbox.TEXTPROCONSTANT;
import eu.fbk.textpro.toolbox.TEXTPROVARIABLES;
import eu.fbk.textpro.toolbox.toolbox;
import eu.fbk.textpro.wrapper.OBJECTDATA;

/**
 * Created by IntelliJ IDEA.
 * User: cgirardi
 * Date: 3-apr-2013
 * Time: 20.20.44
 */
public class TagPro implements TextProModuleInterface {
    private static String language = null;
	M1Para m1 = new M1Para();
	static learner ln = new learner();
    static String model="",
    		index="";
    static MLMallet ml = new MLMallet();
    static learner lnEN = new learner();
    static String modelEN="",
    		indexEN="";
    static MLMallet mlEN = new MLMallet();
    static learner lnFR = new learner();
    static String modelFR="",
    		indexFR="";
    static MLMallet mlFR = new MLMallet();
    String algo = "maxent";
    static ProcessFeatures pf = new ProcessFeatures();
    
    public void init(String[] params, MYProperties prop) {
        model=prop.getProperty("TEXTPROHOME")+prop.getProperty("TAGPRO_IT_MODEL");
        index=prop.getProperty("TEXTPROHOME")+prop.getProperty("TAGPRO_IT_INDEX");
        modelEN=prop.getProperty("TEXTPROHOME")+prop.getProperty("TAGPRO_EN_MODEL");
        indexEN=prop.getProperty("TEXTPROHOME")+prop.getProperty("TAGPRO_EN_INDEX");
        modelFR=prop.getProperty("TEXTPROHOME")+prop.getProperty("TAGPRO_FR_MODEL");
        indexFR=prop.getProperty("TEXTPROHOME")+prop.getProperty("TAGPRO_FR_INDEX");
        
        algo="maxent";
        language = params[0];
        
        if (pf == null) {
        	pf = new ProcessFeatures();
        }
        
        if (language.contentEquals("ita")) {
	        if(ml.classifier == null)
				try {
					ml.classifier = ml.loadClassifier(new File(model));
				} catch (FileNotFoundException e) {
					e.printStackTrace();
				} catch (ClassNotFoundException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				}
	        ln.init(new File(index));
        }
        
        if (language.contentEquals("eng")) {
	        if(mlEN.classifier == null)
				try {
					mlEN.classifier = mlEN.loadClassifier(new File(modelEN));
				} catch (FileNotFoundException e) {
					e.printStackTrace();
				} catch (ClassNotFoundException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				}
	        lnEN.init(new File(indexEN));
        }
        
        if (language.contentEquals("fre")) {
	        if(mlFR.classifier == null)
				try {
					mlFR.classifier = mlFR.loadClassifier(new File(modelFR));
				} catch (FileNotFoundException e) {
					e.printStackTrace();
				} catch (ClassNotFoundException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				}
	        lnFR.init(new File(indexFR));
        }
       
    }

    public void analyze(String filein, String fileout) throws IOException {
        String langprefix = language.substring(0,3).toUpperCase();
        String[] arrcmd = {TEXTPROVARIABLES.getTEXTPROPATH()+"modules/TagPro/bin/TagPro.sh", "-l",langprefix, "-o", fileout, filein};
        //String[] arrcmd = {wrapper.TEXTPROPATH+"modules/TagPro/bin/TagPro.sh", "-l",langprefix, filein};
        Process process = toolbox.runCommand(arrcmd);
        inheritIO(process.getInputStream(), System.out);
        inheritIO(process.getErrorStream(), System.err);
        try {
            process.waitFor();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
    
    public void analyze(String filein, String fileout,toolbox tools) throws IOException {
        String langprefix = tools.variables.getLanguage().substring(0,3).toUpperCase();
        String[] arrcmd = {TEXTPROVARIABLES.getTEXTPROPATH()+"modules/TagPro/bin/TagPro.sh", "-l",langprefix, "-o", fileout, filein};
        //String[] arrcmd = {wrapper.TEXTPROPATH+"modules/TagPro/bin/TagPro.sh", "-l",langprefix, filein};
        Process process = toolbox.runCommand(arrcmd);
        inheritIO(process.getInputStream(), System.out);
        inheritIO(process.getErrorStream(), System.err);
        try {
            process.waitFor();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private static void inheritIO(final InputStream src, final PrintStream dest) {
        new Thread(new Runnable() {
            public void run() {
                Scanner sc = new Scanner(src);
                while (sc.hasNextLine()) {
                    dest.println(sc.nextLine());
                }
            }
        }).start();
    }

    public OBJECTDATA analyze(OBJECTDATA filein, toolbox tools)
			throws Exception {
    	String [] params = {tools.variables.getLanguage().substring(0,3).toUpperCase()};
    	
    	if(ml.classifier == null || mlEN.classifier == null || mlFR.classifier == null) {
    		init(params,tools.variables.getProp());
    	}

    	if (tools.variables.getLanguage().substring(0,3).equalsIgnoreCase("ita")){
    		return analyzeIT( filein,  tools);
    	}
    	else if (tools.variables.getLanguage().substring(0,3).equalsIgnoreCase("fre")){
    		return analyzeFR (filein, tools);
    	}
    	else{
    		return analyzeEN( filein,  tools);
    	}
    }
    
    
	public OBJECTDATA analyzeEN(OBJECTDATA filein, toolbox tools)
			throws Exception {
		//filein.getFileLineByLine().forEach(System.out::println);
		//filein.saveInFile(filein.input_file.getAbsolutePath()+".tmp", TEXTPROCONSTANT.encoding, false);
	

		//String[] myStringArray = {filein.input_file.getAbsolutePath()+".tmp"};
		//String content = m1.readFileString(new InputStreamReader(new FileInputStream(filein.input_file.getAbsolutePath()+".tmp"), M1Para.ENCODING), null);
		//OBJECTDATA file = new OBJECTDATA();
		//file.readData( content+System.lineSeparator());
		//file.saveInFile(filein.input_file.getAbsolutePath()+".tmp.out", TEXTPROCONSTANT.encoding, false);
		
		//ln.test("pos-annelyse/Elsnet-Training-tok-feat-ref.txp.key", file.getFileAsList()).forEach(System.out::println);
		
		//pf = new ProcessFeatures();
		
		LinkedList<String> columValues = new LinkedList<String>();
		
		pf.run_extractFeatures("ENG",filein);
		
		Hashtable<String,Integer> copyThisTokens = new Hashtable<String,Integer> ();
		copyThisTokens.put("token",1);
		copyThisTokens.put("tokennormaccent",2);
		copyThisTokens.put("tokentype",3);
		copyThisTokens.put("pref2",4);
		copyThisTokens.put("pref3",5);
		copyThisTokens.put("pref4",6);
		copyThisTokens.put("suf2",7);
		copyThisTokens.put("suf3",8);
		copyThisTokens.put("suf4",9);
		
		algo = "maxent";
		
		
		mlEN
		.classify(modelEN, lnEN.test(indexEN, filein.getFileLineByLineAsList(copyThisTokens,false)), false,lnEN,algo)
		.map(e-> e.trim().length()>0? lnEN.getTag(indexEN, Integer.parseInt(e)):e)
		.forEach(columValues::addLast);	
		//.forEach(System.out::println);
		//System.out.println(columValues.size()+"="+filein.linesCount+"="+file.linesCount);
		//System.out.println(columValues);
		filein.addColumn("pos", columValues );
		return filein;
	}
	
	
	public OBJECTDATA analyzeIT(OBJECTDATA filein, toolbox tools)
			throws Exception {
		
		LinkedList<String> columValues = new LinkedList<String>();
		
		pf.run_extractFeatures("ITA",filein);
		
		Hashtable<String,Integer> copyThisTokens = new Hashtable<String,Integer> ();
		copyThisTokens.put("token",1);
		copyThisTokens.put("tokennormaccent",2);
		copyThisTokens.put("tokentype",3);	
		copyThisTokens.put("pref2",4);
		copyThisTokens.put("pref3",5);
		copyThisTokens.put("pref4",6);
		copyThisTokens.put("suf2",7);
		copyThisTokens.put("suf3",8);
		copyThisTokens.put("suf4",9);
		copyThisTokens.put("v",10);
		copyThisTokens.put("nc",11);
		copyThisTokens.put("adj",12);
		copyThisTokens.put("adv",13);
		copyThisTokens.put("prep",14);
		copyThisTokens.put("det",15);
		copyThisTokens.put("cl",16);
		copyThisTokens.put("coo",17);
		copyThisTokens.put("pres",18);
		copyThisTokens.put("pron",19);
		copyThisTokens.put("np",20);
		algo = "svm";
		
		ml
		.classify(model, ln.test(index, filein.getFileLineByLineAsList(copyThisTokens,false)), false,ln,algo)
		.map(e-> e.trim().length()>0? ln.getTag(index, Integer.parseInt(e)):e)
		.forEach(columValues::addLast);	
		
		filein.addColumn("pos", columValues );
		return filein;
	}
	
	public OBJECTDATA analyzeFR (OBJECTDATA filein, toolbox tools)
			throws Exception {
		
		LinkedList<String> columValues = new LinkedList<String>();
		
		pf.run_extractFeatures("FRE",filein);
		
		Hashtable<String,Integer> copyThisTokens = new Hashtable<String,Integer> ();
		copyThisTokens.put("tokennorm",1);
		copyThisTokens.put("tokennormaccent",2);
		copyThisTokens.put("tokentype",3);	
		copyThisTokens.put("pref2",4);
		copyThisTokens.put("pref3",5);
		copyThisTokens.put("pref4",6);
		copyThisTokens.put("suf2",7);
		copyThisTokens.put("suf3",8);
		copyThisTokens.put("suf4",9);
		copyThisTokens.put("v",10);
		copyThisTokens.put("nc",11);
		copyThisTokens.put("adj",12);
		copyThisTokens.put("adv",13);
		copyThisTokens.put("prep",14);
		copyThisTokens.put("det",15);
		copyThisTokens.put("cl",16);
		copyThisTokens.put("coo",17);
		copyThisTokens.put("pres",18);
		copyThisTokens.put("pron",19);
		copyThisTokens.put("np",20);
		//copyThisTokens.put("np2",22);
		//copyThisTokens.put("det2",23);
		algo = "svm";
		
		mlFR
		.classify(modelFR, lnFR.test(indexFR, filein.getFileLineByLineAsList(copyThisTokens,false)), false,lnFR,algo)
		.map(e-> e.trim().length()>0? lnFR.getTag(indexFR, Integer.parseInt(e)):e)
		.forEach(columValues::addLast);	
		//.forEach(System.out::println);
		//System.out.println(columValues.size()+"="+filein.linesCount+"="+file.linesCount);
		//System.out.println(columValues);
		filein.addColumn("pos", columValues );
		return filein;
	}
	

}



