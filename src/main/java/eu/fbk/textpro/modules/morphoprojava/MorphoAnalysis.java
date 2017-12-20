package eu.fbk.textpro.modules.morphoprojava;

import eu.fbk.textpro.TextProModuleInterface;
import eu.fbk.textpro.ml.MLMallet;
import eu.fbk.textpro.ml.learner;
import eu.fbk.textpro.modules.bin.M1Para;
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
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.TreeMap;

import javafx.stage.DirectoryChooser;
import net.openhft.chronicle.map.ChronicleMap;
import net.openhft.chronicle.map.ChronicleMapBuilder;

import javax.xml.bind.JAXBException;

import gnu.trove.THashMap;
//import gnu.trove.map.hash.*;

public class MorphoAnalysis implements TextProModuleInterface {
    
	//static HashMap<String,HashMap<String,List<String>>> dict = new HashMap<>();
	//static LinkedHashMap<String,String> dict = new LinkedHashMap<String,String>();
	static HashMap<String,ChronicleMap> dictAllLang = new HashMap<>();
	
	static HashMap<String,String> pathAllResources = new HashMap<>(); 
    
    public MorphoAnalysis(String lang, MYProperties prop, toolbox tools){
    	init(lang, prop, tools);
    }
    
    public OBJECTDATA run_searchMorpho (OBJECTDATA filein, MYProperties prop, toolbox tools) throws Exception{
    	//if (! dict.containsKey(language) || dict.get(language).size() == 0) {
    	String language = tools.variables.getLanguage().substring(0,3).toUpperCase();
    	if(! dictAllLang.containsKey(language)) { 	
    		init(language,prop, tools);
    	}
    	
    	return searchMorpho(language, filein, tools.variables.isColloquialLanguage());
    }
    
    private  void init(String lang, MYProperties prop, toolbox tools) {
    	//String pathResources =  prop.getProperty("ENTITYPRO_IT_MODEL");
    	
    	List<String> langs = new ArrayList<String> ();
    	
    	if(!lang.toLowerCase().matches("eng|ita|fre")) {
    		langs.add("FRE");
    		langs.add("ENG");
    		langs.add("ITA");
    	}
    	else {
    		langs.add(lang.toUpperCase());
    	}
    	
    	for (String l:langs){
    		if (l.equals("ENG")){
    			pathAllResources.put(l, prop.getProperty("TEXTPROHOME")+prop.getProperty("MORPHOPRO_EN_RESOURCE"));
//    			pathAllResources.put(l, pathResources+l+"/english-frm-onelinebywform.frm");
    		}
    		else if(l.equals("ITA")){
    			pathAllResources.put(l, prop.getProperty("TEXTPROHOME")+prop.getProperty("MORPHOPRO_IT_RESOURCE"));
    			//pathAllResources.put(lang, pathResources+lang+"/italian-utf8.frm");
    			//pathAllResources.put(l, pathResources+l+"/italian-frm-onelinebywform.frm");
    		}
    		else if(l.equals("FRE")) {
    			pathAllResources.put(l, prop.getProperty("TEXTPROHOME")+prop.getProperty("MORPHOPRO_FR_RESOURCE"));
    			//pathAllResources.put(l, pathResources+l+"/french-lefff-onelinebywform.frm");    			
    		}
    	}
    	
    	if(! dictAllLang.containsKey("ITA") && pathAllResources.containsKey("ITA")) {
    		dictAllLang.put("ITA", readDictionary(pathAllResources.get("ITA"), "ITA", 1480930));
    	}
    	if(! dictAllLang.containsKey("FRE") && pathAllResources.containsKey("FRE")) {
    		dictAllLang.put("FRE", readDictionary(pathAllResources.get("FRE"), "FRE", 453380));
    		if (tools.variables.isColloquialLanguage()) {
    			dictAllLang.put("FRE_norm",  buildDictionaryNorm (dictAllLang.get("FRE"), 453380));
    		}
    	}
    	if(! dictAllLang.containsKey("ENG") && pathAllResources.containsKey("ENG")) {
    		dictAllLang.put("ENG", readDictionary(pathAllResources.get("ENG"), "ENG", 186459));
    	}
    }
    
        
    private String print(
			HashMap<String, HashMap<String, HashMap<String, Integer>>> value) {
    	value.entrySet().stream().forEach(w->System.out.println("\t"+w.getKey()+" "+w.getValue().size()));
    	return "";
	}

	public static OBJECTDATA searchMorpho (String language, OBJECTDATA filein, boolean isColloquialLanguage)
			throws Exception {

		Hashtable<String,Integer> copyThisTokens = new Hashtable<String,Integer> ();
		if (language.equalsIgnoreCase("ita") || language.equalsIgnoreCase("eng")){
		    copyThisTokens.put("token",1);
	        }
		else{//FRE
		    copyThisTokens.put("tokennorm",1);
		}

		filein.addColumn("full_morpho", searchDictionary(filein.getFileLineByLine(copyThisTokens,true), language, isColloquialLanguage));
		
		return filein;
	}
    
   
   
	public static LinkedList<String> searchDictionary (Iterable<String> lines, String lang, boolean isColloquialLang){
		LinkedList<String> columnValues = new LinkedList<String> ();

		//HashMap<String,List<String>> dicttmp = dict.get(lang);
		
		Iterator it = lines.iterator();
		int cpt = 0;
		while(it.hasNext()){
			String ltmp=(String) it.next();
			if(ltmp.trim().startsWith("# FIELDS: ")){
				continue;
			}
			//LinkedList<String> line = (LinkedList<String>) it.next();
			List<String> line = new ArrayList<String> ();
			//line.add(ltmp);
			//String tok = line.get(0);
			String tok = ltmp.split("\t")[0];
			cpt ++;
			//normal form
			if (dictAllLang.get(lang).containsKey(tok)){
				String full_morpho = (String) dictAllLang.get(lang).get(tok);
				if(full_morpho.length() >2) {
					full_morpho.substring(0, full_morpho.length()-2);
				}
				columnValues.add(full_morpho);
			}
			// capitalized word
			else if(tok.length() > 1 && dictAllLang.get(lang).containsKey(tok.toLowerCase().substring(0,1).toUpperCase()+tok.toLowerCase().substring(1,tok.length()))) {
				String tokCap = tok.toLowerCase().substring(0,1).toUpperCase()+tok.toLowerCase().substring(1,tok.length());
				String full_morpho = (String) dictAllLang.get(lang).get(tokCap);
				if(full_morpho.length() >2) {
					full_morpho.substring(0, full_morpho.length()-2);
				}
				columnValues.add(full_morpho);
			}
			// lower case
			else if (dictAllLang.get(lang).containsKey(tok.toLowerCase())){
				String full_morpho = (String) dictAllLang.get(lang).get(tok.toLowerCase());
				if(full_morpho.length() >2) {
					full_morpho.substring(0, full_morpho.length()-2);
				}
				
				columnValues.add(full_morpho);
			}
			else if (isColloquialLang && dictAllLang.containsKey(lang+"_norm")) {
				if(dictAllLang.get(lang+"_norm").containsKey(tok.toLowerCase())) {
					String full_morpho = (String) dictAllLang.get(lang+"_norm").get(tok.toLowerCase());
					if(full_morpho.length() >2) {
						full_morpho.substring(0, full_morpho.length()-2);
					}
					columnValues.add(full_morpho);
				}
				else {
					tok = tok.toLowerCase().replaceAll("é", "e").replaceAll("è","e").replaceAll("à","a").replaceAll("ê","e")
							.replaceAll("â","a").replaceAll("ô","o").replaceAll("ù","u").replaceAll("î","i")
							.replaceAll("ë","e").replaceAll("ü","u").replaceAll("ç","c").replaceAll("û","u");
					if(dictAllLang.get(lang+"_norm").containsKey(tok)) {
						String full_morpho = (String) dictAllLang.get(lang+"_norm").get(tok);
						if(full_morpho.length() >2) {
							full_morpho.substring(0, full_morpho.length()-2);
						}
						columnValues.add(full_morpho);
					}
					else {
						if(tok.equals("")){
							columnValues.add("");
						}
						else{	
							columnValues.add("_");
						}
					}
				}
			}
			else{
				if(tok.equals("")){
					columnValues.add("");
				}
				else{	
					columnValues.add("_");
				}
			}
		}
		
		return columnValues;
	}
	
	
	private static ChronicleMap<String,String> readDictionary (String fileName, String lang, int size){
		ChronicleMap<String,String> dict = ChronicleMap.of(String.class, String.class)
    			.entries(size)
    			.averageKey("ritrescagli")
    			.averageValue("ritresca~ritrescare+v+imp+pres+nil+2+sing/gli~pro+pron+dat+_+3+")
    			.create();
		
		try{
			BufferedReader br = new BufferedReader(new FileReader(fileName));
			
			String sCurrentLine;
			while ((sCurrentLine = br.readLine()) != null) {
				if(sCurrentLine.contains("\t")) {
					String [] line = sCurrentLine.split("\t");
					dict.put(line[0], line[1]);
				}
			}
		} catch (NumberFormatException e) {
			System.err.println("Error reading dictionary: "+fileName+" "+e.getMessage());
		} catch (IOException e) {
			System.err.println("Dictionary doesn't exist: "+fileName+" "+e.getMessage());
		}
		return dict;
	}
	
	private static ChronicleMap<String,String> buildDictionaryNorm (ChronicleMap<String,String> dict, int size){
		ChronicleMap<String,String> dictNorm = ChronicleMap.of(String.class, String.class)
    			.entries(size)
    			.averageKey("ritrescagli")
    			.averageValue("ritresca~ritrescare+v+imp+pres+nil+2+sing/gli~pro+pron+dat+_+3+")
    			.create();
		
		for (String entry : dict.keySet()) {
			if (entry.toLowerCase().matches(".*[éèàêâôùîëüçû].*")) {
				String wfNorm = entry.toLowerCase();
				wfNorm = wfNorm.replaceAll("é", "e").replaceAll("è","e").replaceAll("à","a").replaceAll("ê","e")
						.replaceAll("â","a").replaceAll("ô","o").replaceAll("ù","u").replaceAll("î","i")
						.replaceAll("ë","e").replaceAll("ü","u").replaceAll("ç","c").replaceAll("û","u");
				
				if (! dictNorm.containsKey(wfNorm)){
					dictNorm.put(wfNorm, dict.get(entry));
				}
				//if the entry already exists, add new morphologic information
				else if (!dictNorm.get(wfNorm).contains(dict.get(entry))) {
					dictNorm.put(wfNorm, dictNorm.get(wfNorm)+" "+dict.get(entry));
				}
			}
		}
		return dictNorm;
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
