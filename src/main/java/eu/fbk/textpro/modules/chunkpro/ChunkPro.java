package eu.fbk.textpro.modules.chunkpro;

import eu.fbk.textpro.TextProModuleInterface;
import eu.fbk.textpro.ml.MLMallet;
import eu.fbk.textpro.ml.learner;
import eu.fbk.textpro.toolbox.MYProperties;
import eu.fbk.textpro.toolbox.TEXTPROVARIABLES;
import eu.fbk.textpro.toolbox.toolbox;
import eu.fbk.textpro.wrapper.OBJECTDATA;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Collection;
import java.util.Hashtable;
import java.util.LinkedList;

/**
 * Created by IntelliJ IDEA.
 * User: cgirardi
 * Date: 13-giu-2013
 * Time: 16.16.33
 */
public class ChunkPro implements TextProModuleInterface {
    private String language = null;
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
    
    static String algo = "svm";
    
    public void init(String[] params,MYProperties prop) {
    	 model=prop.getProperty("TEXTPROHOME")+prop.getProperty("CHUNKPRO_IT_MODEL");
         index=prop.getProperty("TEXTPROHOME")+prop.getProperty("CHUNKPRO_IT_INDEX");
         modelEN=prop.getProperty("TEXTPROHOME")+prop.getProperty("CHUNKPRO_EN_MODEL");
         indexEN=prop.getProperty("TEXTPROHOME")+prop.getProperty("CHUNKPRO_EN_INDEX");
         modelFR=prop.getProperty("TEXTPROHOME")+prop.getProperty("CHUNKPRO_FR_MODEL");
         indexFR=prop.getProperty("TEXTPROHOME")+prop.getProperty("CHUNKPRO_FR_INDEX");
    	
        language = params[0];
        
        if(language.equals("ita")) {
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
        
        if(language.equals("eng")) {
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
        
        if(language.equals("fre")) {
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
    public void analyze(String filein, String fileout,toolbox tools) throws IOException {
        String modelname = "pke_eng_model_v1.0";
        if (tools.variables.getLanguage().substring(0,3).equalsIgnoreCase("ita"))
            modelname = "pke_ita_model_v1.0";
        String[] arrcmd = {TEXTPROVARIABLES.getTEXTPROPATH()+ "modules/tools/yamcha-0.33/usr/local/bin/yamcha",
                "-m", TEXTPROVARIABLES.getTEXTPROPATH() + "modules/ChunkPro/models/" + modelname,"--output",fileout,filein};
        try {
            Process p = toolbox.runCommand(arrcmd);
            p.waitFor();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
    public void analyze(String filein, String fileout) throws IOException {
        String modelname = "pke_eng_model_v1.0";
        if (language.substring(0,3).equalsIgnoreCase("ita"))
            modelname = "pke_ita_model_v1.0";
        String[] arrcmd = {TEXTPROVARIABLES.getTEXTPROPATH()+ "modules/tools/yamcha-0.33/usr/local/bin/yamcha",
                "-m", TEXTPROVARIABLES.getTEXTPROPATH() + "modules/ChunkPro/models/" + modelname,"--output",fileout,filein};
        try {
            Process p = toolbox.runCommand(arrcmd);
            p.waitFor();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
    
    public OBJECTDATA analyze(OBJECTDATA filein, toolbox tools)
			throws Exception {
    	
    	String [] params = {tools.variables.getLanguage().substring(0,3).toUpperCase()};
    	if (ml.classifier == null || mlEN.classifier == null || mlFR.classifier == null) {
    		init(params, tools.variables.getProp());
    	}
    	
    	if (tools.variables.getLanguage().substring(0,3).equalsIgnoreCase("ita")){
    		return analyzeIT( filein,  tools);
    	}
    	else if (tools.variables.getLanguage().substring(0,3).equalsIgnoreCase("fre")) {
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
    	Hashtable<String,Integer> copyThisTokens = new Hashtable<String,Integer> ();
		copyThisTokens.put("token",1);
		copyThisTokens.put("pos",2);

		LinkedList<String> columValues = new LinkedList<String>();
		Collection<String> cc = lnEN.test(indexEN, filein.getFileLineByLineAsList(copyThisTokens,false));
		mlEN.classify(modelEN, cc, false, lnEN, algo)
		.map(e-> e.trim().length()>0? lnEN.getTag(indexEN, Integer.parseInt(e)):e)
		.forEach(columValues::addLast);	
		
		filein.addColumn("chunk", columValues );
		//filein.saveInFile(filein.input_file.getAbsolutePath()+".ner", TEXTPROCONSTANT.encoding, false);
		return filein;
	}
    
    
    public OBJECTDATA analyzeIT(OBJECTDATA filein, toolbox tools)
			throws Exception {
		LinkedList<String> columValues = new LinkedList<String>();
		Hashtable<String,Integer> copyThisTokens = new Hashtable<String,Integer> ();
		copyThisTokens.put("token",1);
		copyThisTokens.put("pos",2);

		ml.classify (model,ln.test( index,filein.getFileLineByLineAsList(copyThisTokens,false)),false,ln,algo)		
			.map(e-> e.trim().length()>0? ln.getTag(index, Integer.parseInt(e)):e)
			.forEach(columValues::addLast);
			
		filein.addColumn("chunk", columValues );
		
		return filein;
	}
    
    public OBJECTDATA analyzeFR(OBJECTDATA filein, toolbox tools)
			throws Exception {

		LinkedList<String> columValues = new LinkedList<String>();
		
		Hashtable<String,Integer> copyThisTokens = new Hashtable<String,Integer> ();
		copyThisTokens.put("tokennorm",1);
		copyThisTokens.put("pos",2);
		
		Collection<String> cc = lnFR.test(indexFR, filein.getFileLineByLineAsList(copyThisTokens,false));

		mlFR
		.classify(modelFR, 
				cc
				, false,lnFR,algo)
		.map(e-> e.trim().length()>0? lnFR.getTag(indexFR, Integer.parseInt(e)):e)
		.forEach(columValues::addLast);	
		
		filein.addColumn("chunk", columValues );
		
		return filein;
	}

}
