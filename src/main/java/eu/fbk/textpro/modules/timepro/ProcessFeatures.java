package eu.fbk.textpro.modules.timepro;

import eu.fbk.textpro.TextProModuleInterface;
import eu.fbk.textpro.ml.MLMallet;
import eu.fbk.textpro.ml.learner;
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
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Pattern;

import javafx.stage.DirectoryChooser;

import javax.xml.bind.JAXBException;


public class ProcessFeatures implements TextProModuleInterface {
    
	static HashMap<String,HashMap<String,String>> rules = new HashMap<>();
    
	
    public ProcessFeatures(String lang, MYProperties prop){
    	if(rules.size() == 0) {
    		init(lang,prop.getProperty("TEXTPROHOME"));
    	}
    }
    
    public ProcessFeatures(String lang){
    	if(rules.size() == 0) {
    		init(lang,"");
    	}
    }
    
    public OBJECTDATA run_extractFeatures (String language, OBJECTDATA filein) throws Exception{
    	
    	return extractFeatures(language, filein);
    }
    
    private  void init(String lang, String pathTextPro) {
    	String pathResources =  pathTextPro+"Modules/resources/TimePro/";
    	
    	List<String> langs = new ArrayList<String> ();
    	
    	if(!lang.matches("ENG|ITA|FRE")) {
    		langs.add("FRE");
    		langs.add("ENG");
    		langs.add("ITA");
    	}
    	else {
    		langs.add(lang);
    	}
    		
		HashMap<String, String> value;
		for(String l:langs){
    		System.out.println("Loading dictionaries of "+l);
        	if(rules.containsKey(l)){
        		value = rules.get(l);
        	}else{
        	    value = new HashMap<>();
        	    rules.put(l, value);
        	}
        	value.putAll(readRules(pathResources+l+"/rules_awk"));
    	}
    	
		//rules.entrySet().stream().forEach(e->System.out.println(e.getKey()+" "+print(e.getValue())));

    }
    
    private String print(
			HashMap<String,String> value) {
    	value.entrySet().stream().forEach(w->System.out.println("\t"+w.getKey()));
    	return "";
	}
    
	public static OBJECTDATA extractFeatures(String language, OBJECTDATA filein)
			throws Exception {
		LinkedList<String> columValues = new LinkedList<String>();
		
		Hashtable<String,Integer> copyThisTokens = new Hashtable<String,Integer> ();
		if(language.equalsIgnoreCase("ita") || language.equalsIgnoreCase("eng") ) {
			copyThisTokens.put("token",1);
		}
		else if(language.equalsIgnoreCase("fre")) {
			copyThisTokens.put("tokennorm",1);
		}
		
		copyThisTokens.put("pos",2);		
		/*copyThisTokens.put("lemma",3);
		copyThisTokens.put("chunk",4);
		copyThisTokens.put("entity",5);*/

		
		List<LinkedList<String>> additionalFeat = getFeatures(language, filein.getFileLineByLine(copyThisTokens,true));
		filein.addColumn("Rule1",additionalFeat.get(0));
		filein.addColumn("Rule2",additionalFeat.get(1));
		
		return filein;
	}
    
   
    	
	public static List<LinkedList<String>> getFeatures (String language, Iterable<String> lines){
		List<LinkedList<String>> columnValues = new ArrayList<LinkedList<String>> ();
		
		columnValues.add(new LinkedList<String> ());
		columnValues.add(new LinkedList<String> ());
		
		HashMap<String,String> rulesLang = rules.get(language);
		
		Iterator it = lines.iterator();
		int cpt = 0;
		while(it.hasNext()){
			String ltmp=(String) it.next();
			if(ltmp.trim().startsWith("# FIELDS: ")){
				continue;
			}
			
			if (ltmp.contains("\t")) {
			String tok = ltmp.split("\t")[0]; // [2] per il lemma
			String pos = ltmp.split("\t")[1];
			cpt ++;
			
			String signal = "_";
			String timex = "_";
			if (!tok.equals("")){
			    tok = tok.toLowerCase();
				
				if (rulesLang.containsKey("signal_words") && tok.matches(rulesLang.get("signal_words"))){
					signal = "_SIGNAL_";
				}
				if (tok.matches(".*[0-9].*")){
					if (rulesLang.containsKey("yy") && tok.matches(rulesLang.get("yy"))){ timex = "_YY_"; }
					else if (rulesLang.containsKey("time") && tok.matches(rulesLang.get("time"))){ timex = "_TIME_"; }
					else if (rulesLang.containsKey("duration") && tok.matches(rulesLang.get("duration"))){ timex = "_DURATION_"; }
					else if (rulesLang.containsKey("date") && tok.matches(rulesLang.get("date"))){ timex = "_DATE_"; }
					else if (rulesLang.containsKey("number") && tok.matches(rulesLang.get("number"))){ timex = "_NUMBER_"; }
					else { timex = "_OTHER_"; }
				}
				else if (rulesLang.containsKey("unit") && tok.matches(rulesLang.get("unit"))){ timex = "_UNIT_"; }
				else if (rulesLang.containsKey("day") && tok.matches(rulesLang.get("day"))){ timex = "_DAY_"; }
				else if (rulesLang.containsKey("month") && tok.matches(rulesLang.get("month"))){ timex = "_MONTH_"; }
				else if (rulesLang.containsKey("season") && tok.matches(rulesLang.get("season"))){ timex = "_SEASON_"; }
				else if (rulesLang.containsKey("ordinal_number") && tok.matches(rulesLang.get("ordinal_number"))){ timex = "_ON_"; }
				else if (rulesLang.containsKey("parts_of_the_day") && tok.matches(rulesLang.get("parts_of_the_day"))){ timex = "_PD_"; }
				else if (rulesLang.containsKey("cardinal_number") && tok.matches(rulesLang.get("cardinal_number"))){ timex = "_CN_"; }
				else if (rulesLang.containsKey("mods") && tok.matches(rulesLang.get("mods"))){ timex = "_MOD_"; }
				else if ((! language.startsWith("ENG") || pos.equals("AV0")) 
						&& rulesLang.containsKey("adverbs") && tok.matches(rulesLang.get("adverbs"))){ timex = "_AVT_"; }
				else if (rulesLang.containsKey("adverbs_date") && tok.matches(rulesLang.get("adverbs_date"))){ timex = "_AVTDATE_"; }
				else if (rulesLang.containsKey("plurals") && tok.matches(rulesLang.get("plurals"))){ timex = "_PLURAL_"; }
				else if (rulesLang.containsKey("names") && tok.matches(rulesLang.get("names"))){ timex = "_NAMES_"; }
				else if (rulesLang.containsKey("set") && tok.matches(rulesLang.get("set"))){ timex = "_SET_"; }
				else { timex = "_"; }
			

				columnValues.get(0).add(signal);
				columnValues.get(1).add(timex);
			}
			else{
				columnValues.get(0).add("");
				columnValues.get(1).add("");
			}
			}
			else{
				columnValues.get(0).add("");
				columnValues.get(1).add("");
			}

		}

		return columnValues;
	}
	
	
	
	private static HashMap<String,String> readRules (String fileName){
		HashMap<String,String> hashTmp = new HashMap<String,String> ();
		try{
			BufferedReader br = new BufferedReader(new FileReader(fileName));
	
			String sCurrentLine;
			while ((sCurrentLine = br.readLine()) != null) {
				if (sCurrentLine.contains("=")){
					String [] line = sCurrentLine.split("=");
					String tag = line[0].replaceAll(" ","");
					String rule = line[1].replaceAll(" ","");
					/*rule.replaceAll("\"", "");
					rule.replace("^","");
					rule.replace("$","");*/
					rule = rule.substring(1, rule.length()-2);
					rule = "(?i:"+rule+")";
					//System.out.println(rule);
					hashTmp.put(tag, rule);
				}
					
			}
		} catch (NumberFormatException e) {
			System.err.println("Error reading dictionary: "+fileName+" "+e.getMessage());
		} catch (IOException e) {
			System.err.println("Dictionary doesn't exist: "+fileName+" "+e.getMessage());
		}
		
		return hashTmp;
	}
		
   

	@Override
	public void init(String[] params,MYProperties prop)
			throws FileNotFoundException, UnsupportedEncodingException, MalformedURLException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void analyze(String filein, String fileout) throws IOException, JAXBException {
		// TODO Auto-generated method stub
		
	}



	
}
