package eu.fbk.textpro.modules.timepro;

import eu.fbk.textpro.TextProModuleInterface;
import eu.fbk.textpro.ml.MLMallet;
import eu.fbk.textpro.ml.learner;
import eu.fbk.textpro.modules.timepro.eng.TimeProNormApplyEN;
import eu.fbk.textpro.modules.timepro.fre.TimeProNormApplyFR;
import eu.fbk.textpro.modules.timepro.ita.TimeProNormApply_TextPro;
import eu.fbk.textpro.toolbox.MYProperties;
import eu.fbk.textpro.toolbox.TEXTPROVARIABLES;
import eu.fbk.textpro.toolbox.toolbox;
import eu.fbk.textpro.wrapper.OBJECTDATA;
import eu.fbk.textpro.wrapper.TextProPipeLine;

import javax.xml.bind.JAXBException;
import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;
import java.util.Hashtable;
import java.util.LinkedList;



public class TimePro implements TextProModuleInterface {
	
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
    static ProcessFeatures pf;
    static String algo = "svm";
    
    public void init(String[] params,MYProperties prop) {
        model=prop.getProperty("TEXTPROHOME")+prop.getProperty("TIMEPRO_IT_MODEL");
        index=prop.getProperty("TEXTPROHOME")+prop.getProperty("TIMEPRO_IT_INDEX");
        modelEN=prop.getProperty("TEXTPROHOME")+prop.getProperty("TIMEPRO_EN_MODEL");
        indexEN=prop.getProperty("TEXTPROHOME")+prop.getProperty("TIMEPRO_EN_INDEX");
        modelFR=prop.getProperty("TEXTPROHOME")+prop.getProperty("TIMEPRO_FR_MODEL");
        indexFR=prop.getProperty("TEXTPROHOME")+prop.getProperty("TIMEPRO_FR_INDEX");
    	
        language = params[0];
        
        if(language.toLowerCase().startsWith("ita")) {
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

        if(language.toLowerCase().startsWith("eng")) {
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
	        if(lnEN == null) {
	        	lnEN.init(new File(indexEN));
	        }
        }
        
        if(language.toLowerCase().startsWith("fre")) {
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
        
        if(pf == null) {
        	pf = new ProcessFeatures(language,prop);
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
    
    /**
	 * Get the current date
	 * @return
	 */
	public static String getTodayDate (){
		DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
		Date date = new Date();
		String dateString = dateFormat.format(date).toString();
		return dateString;
	}
    
    private String getDocumentCreationTime (toolbox tools) {
    	String dct = tools.variables.getDCT();
		
    	if (dct.equals("")) {
			dct = getTodayDate();
		}
    	else {
    		if (dct.matches("^\\d\\d\\d\\d.\\d\\d.\\d\\d.*$")) {
    			dct = dct.substring(0,4)+"-"+dct.substring(5,7)+"-"+dct.substring(8,10);
    		} else if (dct.matches("^\\d\\d\\d\\d\\d\\d\\d\\d.*$")) {
    			dct = dct.substring(0,4)+"-"+dct.substring(4,6)+"-"+dct.substring(6,8);
    		} else if (dct.matches("^\\d\\d\\d\\d-\\d\\d-\\d\\d.*$")) {
    			dct = dct.substring(0,4)+"-"+dct.substring(4,6)+"-"+dct.substring(6,8);
    		} else {
    			dct = getTodayDate();
    		}
		}
    	
    	return dct;
    }
    
    public OBJECTDATA analyzeEN(OBJECTDATA filein, toolbox tools)
			throws Exception {
		
    	LinkedList<String> columValues = new LinkedList<String>();
		
		pf.run_extractFeatures("ENG",filein);
		
		Hashtable<String,Integer> copyThisTokens = new Hashtable<String,Integer> ();
		copyThisTokens.put("token",1);
		copyThisTokens.put("pos",2);
		copyThisTokens.put("lemma",3);
		copyThisTokens.put("chunk",4);
		copyThisTokens.put("entity",5);
		copyThisTokens.put("Rule1",6);
		copyThisTokens.put("Rule2",7);
		
		algo = "maxent";		
		
		mlEN
		.classify(modelEN, lnEN.test(indexEN, filein.getFileLineByLineAsList(copyThisTokens,false)), false,lnEN,algo)
		.map(e-> e.trim().length()>0? lnEN.getTag(indexEN, Integer.parseInt(e)):e)
		.forEach(columValues::addLast);	
		
		filein.addColumn("tmx", columValues );
		
		String dct = getDocumentCreationTime(tools);
		
		
		filein = TimeProNormApplyEN.normalized_timex(filein, dct);
		
		filein = ModAttribute.analyze_modifiers(filein, tools.variables.getProp().getProperty("TEXTPROHOME")+tools.variables.getProp().getProperty("TIMEPRO_EN_MOD"));
		
		return filein;
	}
    
    public OBJECTDATA analyzeIT(OBJECTDATA filein, toolbox tools)
			throws Exception {
    	LinkedList<String> columValues = new LinkedList<String>();
		
		pf.run_extractFeatures("ITA",filein);
		
		Hashtable<String,Integer> copyThisTokens = new Hashtable<String,Integer> ();
		copyThisTokens.put("token",1);
		copyThisTokens.put("pos",2);
		copyThisTokens.put("lemma",3);
		copyThisTokens.put("chunk",4);
		copyThisTokens.put("entity",5);
		copyThisTokens.put("Rule1",6);
		copyThisTokens.put("Rule2",7);
		
		algo = "svm";		
		
		ml
		.classify(model, ln.test(index, filein.getFileLineByLineAsList(copyThisTokens,false)), false,ln,algo)
		.map(e-> e.trim().length()>0? ln.getTag(index, Integer.parseInt(e)):e)
		.forEach(columValues::addLast);	
	
		filein.addColumn("tmx", columValues );
		
		String dct = getDocumentCreationTime(tools);
		
		filein = TimeProNormApply_TextPro.normalized_timex(filein, dct);
		
		
		return filein;
	}
    
    public OBJECTDATA analyzeFR(OBJECTDATA filein, toolbox tools)
			throws Exception {
    	LinkedList<String> columValues = new LinkedList<String>();
		
		pf.run_extractFeatures("FRE",filein);
		
		Hashtable<String,Integer> copyThisTokens = new Hashtable<String,Integer> ();
		copyThisTokens.put("token",1);
		copyThisTokens.put("pos",2);
		copyThisTokens.put("lemma",3);
		copyThisTokens.put("chunk",4);
		copyThisTokens.put("Rule1",5);
		copyThisTokens.put("Rule2",6);
		
		algo = "svm";		
		
		mlFR
		.classify(modelFR, lnFR.test(indexFR, filein.getFileLineByLineAsList(copyThisTokens,false)), false,lnFR,algo)
		.map(e-> e.trim().length()>0? lnFR.getTag(indexFR, Integer.parseInt(e)):e)
		.forEach(columValues::addLast);	
		
		filein.addColumn("tmx", columValues );
		
		String dct = getDocumentCreationTime(tools);
		
		filein = TimeProNormApplyFR.normalized_timex(filein, dct);
		
		filein = ModAttribute.analyze_modifiers(filein, tools.variables.getProp().getProperty("TEXTPROHOME")+tools.variables.getProp().getProperty("TIMEPRO_FR_MOD"));
		
		return filein;
	}

	@Override
	public void analyze(String filein, String fileout) throws IOException, JAXBException {
		// TODO Auto-generated method stub
		
	}
}
