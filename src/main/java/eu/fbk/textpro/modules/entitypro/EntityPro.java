package eu.fbk.textpro.modules.entitypro;

import eu.fbk.textpro.TextProModuleInterface;
import eu.fbk.textpro.ml.MLMallet;
import eu.fbk.textpro.ml.learner;
import eu.fbk.textpro.modules.entitypro.ProcessFeatures;
import eu.fbk.textpro.toolbox.MYProperties;
import eu.fbk.textpro.toolbox.TEXTPROCONSTANT;
import eu.fbk.textpro.toolbox.TEXTPROVARIABLES;
import eu.fbk.textpro.toolbox.toolbox;
import eu.fbk.textpro.wrapper.OBJECTDATA;
import eu.fbk.textpro.wrapper.TextProPipeLine;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: cgirardi
 * Date: 3-apr-2013
 * Time: 20.20.44
 */
public class EntityPro implements TextProModuleInterface {
	private static String language = null;
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
    static ProcessFeatures pf;
	
    
    @Override
    public void init(String[] params,MYProperties prop) {
    	model=prop.getProperty("TEXTPROHOME")+prop.getProperty("ENTITYPRO_IT_MODEL");
        index=prop.getProperty("TEXTPROHOME")+prop.getProperty("ENTITYPRO_IT_INDEX");
        modelEN=prop.getProperty("TEXTPROHOME")+prop.getProperty("ENTITYPRO_EN_MODEL");
        indexEN=prop.getProperty("TEXTPROHOME")+prop.getProperty("ENTITYPRO_EN_INDEX");
        modelFR=prop.getProperty("TEXTPROHOME")+prop.getProperty("ENTITYPRO_FR_MODEL");
        indexFR=prop.getProperty("TEXTPROHOME")+prop.getProperty("ENTITYPRO_FR_INDEX");
    	
        language = params[0];
        
        if(language.toLowerCase().startsWith("ita")){
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
	        if(ln == null) {
	        	ln.init(new File(index));
	        }
        }

        if(language.toLowerCase().startsWith("eng")){
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
			if (lnEN == null) {
				lnEN.init(new File(indexEN));
			}
        }
        
        if(language.toLowerCase().startsWith("fre")){
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
	        if(lnFR == null) {
	        	lnFR.init(new File(indexFR));
	        }
        }
        
        if (pf == null) {
        	pf = new ProcessFeatures(language, prop);
        }
      
    }
    
    public void analyze(String filein, String fileout,toolbox tools) throws IOException {
        String[] arrcmd = {TEXTPROVARIABLES.getTEXTPROPATH() +
                "modules/EntityPro/bin/EntityPro.sh","-l",tools.variables.getLanguage().substring(0,3).toUpperCase(),"-o",fileout,filein};

        //System.err.println(cmd[2]);
        Process p = toolbox.runCommand(arrcmd);
        try {
            p.waitFor();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void analyze(String filein, String fileout) throws IOException {
        String[] arrcmd = {TEXTPROVARIABLES.getTEXTPROPATH() +
                "modules/EntityPro/bin/EntityPro.sh","-l",language.substring(0,3).toUpperCase(),"-o",fileout,filein};

        //System.err.println(cmd[2]);
        Process p = toolbox.runCommand(arrcmd);
        try {
            p.waitFor();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
    
    public OBJECTDATA analyze(OBJECTDATA filein, toolbox tools)
			throws Exception {
    	
    	String [] params = {tools.variables.getLanguage().substring(0,3).toUpperCase()};
    	
    	if(mlFR.classifier == null || mlEN.classifier == null || ml.classifier == null) {
    		init(params,tools.variables.getProp());
    	}
    	
    	if (tools.variables.getLanguage().substring(0,3).equalsIgnoreCase("ita")){
    		return analyzeIT( filein,  tools);
    	}
    	else if (tools.variables.getLanguage().substring(0,3).equalsIgnoreCase("fre")){
    		return analyzeFR( filein,  tools);
    	}
    	else{
    		return analyzeEN( filein,  tools);
    	}
    }
    
    public OBJECTDATA analyzeEN(OBJECTDATA filein, toolbox tools)
			throws Exception {
		//filein.getFileLineByLine().forEach(System.out::println);
		//filein.saveInFile(filein.input_file.getAbsolutePath()+".nerIn", TEXTPROCONSTANT.encoding, false);

		LinkedList<String> columValues = new LinkedList<String>();
		//Collection<String> cc = lnEN.test(indexEN, filein.getFileAsList());
		
		pf.run_extractFeatures("ENG",filein);
		
		Hashtable<String,Integer> copyThisTokens = new Hashtable<String,Integer> ();
		copyThisTokens.put("token",1);
		copyThisTokens.put("tokennorm",2);
		copyThisTokens.put("tokentype",3);
		copyThisTokens.put("pos",4);
		copyThisTokens.put("lemma",5);
		copyThisTokens.put("coarseGrainedPos",6);
		copyThisTokens.put("tokennormbis",7);
		copyThisTokens.put("GPE",8);
		copyThisTokens.put("ORG",9);
		copyThisTokens.put("LOC",10);
		copyThisTokens.put("PER",11);
		copyThisTokens.put("orth",12);
		copyThisTokens.put("sufBig",13);
		copyThisTokens.put("prefBig",14);

		mlEN.classify (modelEN,lnEN.test( indexEN,filein.getFileLineByLineAsList(copyThisTokens,false)),false,lnEN,algo)		
		.map(e-> e.trim().length()>0? lnEN.getTag(indexEN, Integer.parseInt(e)):e)
		.forEach(columValues::addLast);

		filein.addColumn("entity", columValues );
		//filein.saveInFile(filein.input_file.getAbsolutePath()+".ner", TEXTPROCONSTANT.encoding, false);
		return filein;
	}

    
    public OBJECTDATA analyzeIT(OBJECTDATA filein, toolbox tools)
			throws Exception {
		//filein.getFileLineByLine().forEach(System.out::println);
		//filein.saveInFile(filein.input_file.getAbsolutePath()+".nerIn", TEXTPROCONSTANT.encoding, false);

		LinkedList<String> columValues = new LinkedList<String>();
		//Collection<String> cc = ln.test(index, filein.getFileAsList());
		
		algo = "svm";
		
		pf.run_extractFeatures("ITA",filein);		
		
		//System.out.println(cc);
		Hashtable<String,Integer> copyThisTokens = new Hashtable<String,Integer> ();
		copyThisTokens.put("token",1);
		copyThisTokens.put("tokennorm",2);
		copyThisTokens.put("tokentype",3);
		copyThisTokens.put("pos",4);
		copyThisTokens.put("lemma",5);
		copyThisTokens.put("coarseGrainedPos",6);
		copyThisTokens.put("tokennormbis",7);
		copyThisTokens.put("GPE",8);
		copyThisTokens.put("ORG",9);
		copyThisTokens.put("LOC",10);
		copyThisTokens.put("PER",11);
		copyThisTokens.put("orth",12);
		copyThisTokens.put("sufBig",13);
		copyThisTokens.put("prefBig",14);

		ml.classify (model,ln.test( index,filein.getFileLineByLineAsList(copyThisTokens,false)),false,ln,algo)		
		.map(e-> e.trim().length()>0? ln.getTag(index, Integer.parseInt(e)):e)
		.forEach(columValues::addLast);

//		filein.getFileAsList().stream().map(e-> e.trim().length()>0? ln.getTag(index, Integer.parseInt(e)):e)
//		.forEach(columValues::addLast);
//		filein.getFileLineByLineAsList(copyThisTokens,false).stream().map(e-> e.trim().length()>0? ln.getTag(index, Integer.parseInt(e)):e)
//		.forEach(columValues::addLast);
/*		ml
		.classify(model, 
				cc
				, false)
			//	.forEach(System.out::println);
		.map(e-> e.trim().length()>0? ln.getTag(index, Integer.parseInt(e)):e)
		.forEach(columValues::addLast);	*/
		//.forEach(System.out::println);
		//System.out.println(columValues.size()+"="+filein.linesCount+"="+filein.linesCount);
		//System.out.println(columValues);
		filein.addColumn("entity", columValues );
		//filein.saveInFile(filein.input_file.getAbsolutePath()+".ner", TEXTPROCONSTANT.encoding, false);
		return filein;
	}
	
    public OBJECTDATA analyzeFR(OBJECTDATA filein, toolbox tools)
			throws Exception {
		LinkedList<String> columValues = new LinkedList<String>();
		
		algo = "svm";
		
		pf.run_extractFeatures("FRE",filein);		
		
		Hashtable<String,Integer> copyThisTokens = new Hashtable<String,Integer> ();
		copyThisTokens.put("token",1);
		copyThisTokens.put("tokennorm",2);
		copyThisTokens.put("tokentype",3);
		copyThisTokens.put("pos",4);
		copyThisTokens.put("lemma",5);
		copyThisTokens.put("coarseGrainedPos",6);
		copyThisTokens.put("tokennormbis",7);
		copyThisTokens.put("GPE",8);
		copyThisTokens.put("ORG",9);
		copyThisTokens.put("LOC",10);
		copyThisTokens.put("PER",11);
		copyThisTokens.put("orth",12);
		copyThisTokens.put("sufBig",13);
		copyThisTokens.put("prefBig",14);

		mlFR.classify (modelFR,lnFR.test( indexFR,filein.getFileLineByLineAsList(copyThisTokens,false)),false,lnFR,algo)		
		.map(e-> e.trim().length()>0? lnFR.getTag(indexFR, Integer.parseInt(e)):e)
		.forEach(columValues::addLast);

		filein.addColumn("entity", columValues );

		return filein;
	}
}
