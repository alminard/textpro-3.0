package eu.fbk.textpro.modules.entitypro;

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
import java.util.LinkedList;
import java.util.List;

import javafx.stage.DirectoryChooser;

import javax.xml.bind.JAXBException;


public class ProcessFeatures implements TextProModuleInterface {
    
	static HashMap<String,HashMap<String,HashMap<String,HashMap<String,Integer>>>> dichs = new HashMap<>();
    
    
    public ProcessFeatures(String lang, MYProperties prop){
    	if (dichs.size() == 0) {
    		init(lang,prop.getProperty("TEXTPROHOME"));
    	}
    }
    
    public ProcessFeatures(String lang){
    	if (dichs.size() == 0) {
    		init(lang,"");
    	}
    }
    
    public OBJECTDATA run_extractFeatures (String language, OBJECTDATA filein) throws Exception{
    	
    	return extractFeatures(language, filein);
    }
    
    private  void init(String lang, String pathTextPro) {
     //   String pathResources = "/home/anne-lyse//workspace_mars/textpro-java-objectdata/src/main/resources/modules/EntityPro/";
    	String pathResources =  pathTextPro+"Modules/resources/EntityPro/";
    	
    	HashMap<String, HashMap<String, HashMap<String, Integer>>> value;
		
    	List<String> langs = new ArrayList<String> ();
    	
    	if(!lang.matches("ENG|ITA|FRE")) {
    		langs.add("ENG");
    		langs.add("ITA");
    	}
    	else {
    		if(lang.equals("FRE")) {
    			langs.add("ITA");
    		}
    		else {
    			langs.add(lang);
    		}
    	}
    	
    	for(String l:langs){
    		System.out.println("Loading dichionaries of "+l);
        	if(dichs.containsKey(l)){
        		value = dichs.get(l);
        	}else{
        	    value = new HashMap<>();
        	    dichs.put(l, value);
        	}
        	value.put("GPE", readDictionary(pathResources+l+"/dictionary.GPE.1.3-1.tsv"));
        	value.put("ORG", readDictionary(pathResources+l+"/dictionary.ORG.1.3-1.tsv"));
        	value.put("LOC ", readDictionary(pathResources+l+"/dictionary.LOC.1.3-1.tsv"));
        	value.put("PER", readDictionary(pathResources+l+"/dictionary.PER.1.3-1.tsv"));
    	}
	    	
		dichs.entrySet().stream().forEach(e->System.out.println(e.getKey()+" "+print(e.getValue())));
    }
    
        
    private String print(
			HashMap<String, HashMap<String, HashMap<String, Integer>>> value) {
    	value.entrySet().stream().forEach(w->System.out.println("\t"+w.getKey()+" "+w.getValue().size()));
    	return "";
	}

	public static OBJECTDATA extractFeatures(String language, OBJECTDATA filein)
			throws Exception {
		//filein.getFileLineByLine().forEach(System.out::println);
		//filein.saveInFile(filein.input_file.getAbsolutePath()+".nerIn", TEXTPROCONSTANT.encoding, false);

		LinkedList<String> columValues = new LinkedList<String>();
		
		Hashtable<String,Integer> copyThisTokens = new Hashtable<String,Integer> ();
		copyThisTokens.put("token",1);
		copyThisTokens.put("tokennorm",2);		
		copyThisTokens.put("tokentype",3);
		copyThisTokens.put("pos",4);
		copyThisTokens.put("lemma",5);
		//copyThisTokens.put("entity",2);

		if (language.toLowerCase().startsWith ("eng")){
			filein.addColumn("coarseGrainedPos", getCoarseGrainPosENG(filein.getFileLineByLine(copyThisTokens, true)));
			filein.addColumn("tokennormbis", getTokenLowerCase(filein.getFileLineByLine(copyThisTokens, true)));
		}
		else{
			filein.addColumn("coarseGrainedPos", getCoarseGrainPosITA(filein.getFileLineByLine(copyThisTokens, true)));
			filein.addColumn("tokennormbis", getTokenLowerCase(filein.getFileLineByLine(copyThisTokens, true)));
		}
		
		copyThisTokens.clear();
		copyThisTokens.put("token",1);
		
		HashMap<String, HashMap<String, HashMap<String, Integer>>> df = new HashMap<>();
		HashMap<String, HashMap<String, HashMap<String, Integer>>> ll = new HashMap<>();
		if(language.startsWith("FRE")) {
			ll = dichs.getOrDefault("ITA",df );			
		}
		else {
			ll = dichs.getOrDefault(language,df );
		}
		HashMap<String, HashMap<String, Integer>> defaultValue = new HashMap<>();
		
		filein.addColumn("GPE", assignTagDictionary(filein.getFileLineByLine(copyThisTokens,true),ll.getOrDefault("GPE",defaultValue)));
		filein.addColumn("ORG", assignTagDictionary(filein.getFileLineByLine(copyThisTokens,true), ll.getOrDefault("ORG",defaultValue)));
		filein.addColumn("LOC", assignTagDictionary(filein.getFileLineByLine(copyThisTokens,true), ll.getOrDefault("LOC",defaultValue)));
		filein.addColumn("PER", assignTagDictionary(filein.getFileLineByLine(copyThisTokens,true), ll.getOrDefault("PER", defaultValue )));
		
		
		List<LinkedList<String>> additionalFeat = getFeatures(filein.getFileLineByLine(copyThisTokens,true));
		filein.addColumn("orth",additionalFeat.get(0));
		filein.addColumn("sufBig",additionalFeat.get(2));
		filein.addColumn("prefBig",additionalFeat.get(1));
		
		return filein;
	}
    
	private static LinkedList<String> getTokenLowerCase (Iterable<String> lines){
		LinkedList<String> columnValues = new LinkedList<String> ();
		Iterator it = lines.iterator();
		
		while(it.hasNext()){
			String ltmp=(String) it.next();
			String [] col = ltmp.split("\t");
			if(ltmp.trim().startsWith("# FIELDS: ")){
				continue;
			}
			if (!ltmp.equals("")){
				columnValues.add(col[1].toLowerCase());
			}
			else{
				columnValues.add("");
			}
		}
		return columnValues;
	}

   
	private static LinkedList<String> getCoarseGrainPosITA (Iterable<String> lines){ // In TextPro2.0, processing done with awk and the ita_rules_v1.0.0 
		LinkedList<String> columnValues = new LinkedList<String> ();
		Iterator it = lines.iterator();
		while(it.hasNext()){
			String ltmp=(String) it.next();
			if(ltmp.trim().startsWith("# FIELDS: ")){
				continue;
			}
			
			if(!ltmp.equals("")){
				String [] line = ltmp.split("\t");
				//LinkedList<String> line = (LinkedList<String>) it.next();
	
				String token = line[0];
				String normalisedToken = line[1];
				String tokenType = line[2];
				String pos = line[3];
				String lemma = line[4];
				String coarseGrainPos = "";	
				
				if (pos.equals("SPN") || (( pos.equals("SS") || pos.equals("SP") || pos.equals("SN")) && lemma.equals("__NULL__")) ){
					lemma = normalisedToken;
					coarseGrainPos = pos;
				}
				else{
					coarseGrainPos = pos.substring(0,1); // TODO: control that we want to get only one char
				}
				
				if (coarseGrainPos.equals("R") || coarseGrainPos.equals("E")){
					coarseGrainPos += "_"+normalisedToken;
				}
					
				columnValues.add(coarseGrainPos);
			}
			else{
				columnValues.add("");
			}
			//System.out.println(normalisedToken+"\t"+pos+"\t"+lemma+"\t"+tokenType);
		}
		return columnValues;
	}

	public static LinkedList<String> getCoarseGrainPosENG (Iterable<String> lines){ // In TextPro2.0, processing done with awk and the eng_rules_v1.0.0 
		LinkedList<String> columnValues = new LinkedList<String> ();
		Iterator it = lines.iterator();
		while(it.hasNext()){
			String ltmp=(String) it.next();
			if(ltmp.trim().startsWith("# FIELDS: ")){
				continue;
			}
			
			if(!ltmp.equals("")){
			String [] line = ltmp.split("\t");
			//LinkedList<String> line = (LinkedList<String>) it.next();
			

			String token = line[0];
			String normalisedToken = line[1];
			String tokenType = line[2];
			String pos = line[3];
			String lemma = line[4];
				
			if (pos.equals("NP0") || pos.equals("NN0") || pos.equals("NN1") || pos.equals("NN2")){
				if (lemma.equals("__NULL__")){ lemma = normalisedToken;}
				
				if (token.matches("^[A-Z]+$")){ pos += "_U"; } //Uppercase tokens
				else if (token.matches("^[A-Z][a-z]+$")){ pos += "_C"; } //Capitalized tokens
				else if (token.matches("^[a-z]+$")){ pos += "_L"; } //Lower cased tokens
				else if (token.matches("^[A-Za-z]+`[A-Za-z]*$")){ pos += "_accent"; } //Tokens that contain accents
				else { pos += "_M"; } //Other
			}
			else if (pos.equals("CRD") || pos.equals("ORD") ){
				lemma = pos;
				if (normalisedToken.matches("^[A-Za-z][A-Za-z]*")){ pos += "_letter"; } // numbers as letters
				else if (normalisedToken.matches("[0-9]\\.[0-9]")){ pos += "_dec"; } // decimal numbers
				else if (normalisedToken.matches("^[0-9]+\\-[0-9]+") || normalisedToken.matches("^[12][0-9][0-9][0-9]")){ pos += "_date"; } // possibly a date
				else if (normalisedToken.matches("[0-9]/[0-9]")){ pos += "_/"; } // numbers that contain /
				else if (normalisedToken.matches("[0-9]+\\.")){ pos += "_."; } // numbers that contain .
			}
			else if (pos.equals("PRP")){
				pos += "_"+normalisedToken;
			}
			columnValues.add(pos);
			}
			else{
				columnValues.add("");
			}
			//System.out.println(normalisedToken+"\t"+pos+"\t"+lemma+"\t"+tokenType);
		}
		return columnValues;
	}
	
	public static List<LinkedList<String>> getFeatures (Iterable<String> lines){
		List<LinkedList<String>> columnValues = new ArrayList<LinkedList<String>> ();
		
		columnValues.add(new LinkedList<String> ());
		columnValues.add(new LinkedList<String> ());
		columnValues.add(new LinkedList<String> ());
		
		Iterator it = lines.iterator();
		int cpt = 0;
		while(it.hasNext()){
			String ltmp=(String) it.next();
			if(ltmp.trim().startsWith("# FIELDS: ")){
				continue;
			}
			//LinkedList<String> line = (LinkedList<String>) it.next();
			List<String> line = new ArrayList<String> ();
			line.add(ltmp);
			String tok = line.get(0);
			cpt ++;
			if (!tok.equals("")){
				String orth = "O";
				String pref_big = "";
				String suf_big = "";
					
				if (tok.matches("^[A-Z][a-z]+")){ orth = "CAP"; }
				else if (tok.matches("^[0-9]+$")){ orth = "DIG"; }
				else if (tok.matches("^[A-Z][A-Z\\-]+$")){ orth = "UC"; }
				else if (tok.matches("^[a-zéèà][a-zéèà\\-]*$")){ orth = "LC"; }
					
				if(tok.length() > 1){
					pref_big = tok.substring(0,2);
				}
				else{
					pref_big = tok;
				}
					
				if(tok.length() > 1){
					suf_big = tok.substring(tok.length()-2,tok.length());
				}
				else{
					suf_big = tok;
				}
					
				columnValues.get(0).add(orth);
				columnValues.get(1).add(pref_big);
				columnValues.get(2).add(suf_big);
				//System.out.println(lines[i][0]+"\t"+orth+"\t"+pref_big+"\t"+suf_big);
			}
			else{
				columnValues.get(0).add("");
				columnValues.get(1).add("");
				columnValues.get(2).add("");
			}
		}
//		columnValues.get(0).removeFirst();
//		columnValues.get(1).removeFirst();
//		columnValues.get(2).removeFirst();

		/*columnValues.get(0).remove(columnValues.get(0).size()-1);
		columnValues.get(1).remove(columnValues.get(1).size()-1);
		columnValues.get(2).remove(columnValues.get(2).size()-1);*/
		
		/*if (columnValues.get(0).size() != cpt){
			System.out.println(columnValues.get(0).size()+ " : "+cpt);
			System.out.println("different size");
		}*/
		
		return columnValues;
	}

    
	
	
	
	private static HashMap<String,HashMap<String,Integer>> readDictionary (String fileName){
		HashMap<String,HashMap<String,Integer>> hashTmp = new HashMap<String,HashMap<String,Integer>> ();
	try{
		BufferedReader br = new BufferedReader(new FileReader(fileName));

		String sCurrentLine;
			while ((sCurrentLine = br.readLine()) != null) {
				String [] line = sCurrentLine.split("\t");
				HashMap<String,Integer> classes = new HashMap<String,Integer> ();
				for (int i=1; i<line.length; i++){
					String [] elts = line[i].split(":");
					
					classes.put(elts[0], Integer.parseInt(elts[1]));
				}
				hashTmp.put(line[0], classes);
			}
		} catch (NumberFormatException e) {
			System.err.println("Error reading dichinary: "+fileName+" "+e.getMessage());
		} catch (IOException e) {
			System.err.println("Dichinary doesn't exist: "+fileName+" "+e.getMessage());
		}
		
		return hashTmp;
	}
		
    
	private static LinkedList<String> assignTagDictionary (Iterable<String> lines, HashMap<String,HashMap<String,Integer>> hash) throws IOException{
		LinkedList<String> columnValues = new LinkedList<String> ();
		Iterator it = lines.iterator();
		List<String> lineSent = new ArrayList<String> ();
		int cpt = 0;
		while(it.hasNext()){
			cpt ++;
			//LinkedList<String> line = (LinkedList<String>) it.next();
			String line = (String) it.next();
			lineSent.add(line);
			if (line.trim().equals("") && cpt > 1){																																																																																																																																																																																																																																																										
				columnValues.addAll(assign_tag(lineSent, hash));	
				columnValues.add("");
				lineSent.clear();
				//System.out.println("add "+cpt+" "+columnValues.size());
			}
		}
		//System.out.println("add "+cpt+" "+columnValues.size());
		if(lineSent.size() > 0){
			columnValues.addAll(assign_tag(lineSent, hash));	
			columnValues.addLast("");
			lineSent.clear();
		//	System.out.println("add2 "+cpt);
		}
		/*else if(cpt > columnValues.size()){
			for (int l=columnValues.size()-1; l<cpt; l++){
				columnValues.add("");
			}
		}*/
		columnValues.removeFirst();
		
		/*if (columnValues.size() > cpt){
			System.out.println(columnValues.size()+ " : "+cpt);
			System.out.println("assignTag different size");
		}
		else{
			System.out.println("same size");
			//columnValues.removeLast();
		}*/
		
		return columnValues;
	}
	
	private static String [] removeLastElt(String [] a) {
		
		String [] tmp = new String [a.length-1];
	    for (int i=0; i<a.length-1; i++){
	    	tmp[i]=a[i];
	    }
	    return tmp;
	}
	
	private static String joinArray (String [] a, String del){
		String join = "";
		for (int i=0; i<a.length; i++){
			if (i==a.length-1){
				join += a[i];
			}
			else{
				join += a[i]+del;
			}
		}
		return join;
	}
	
	/**
	 * Code from the script dictionary-ser.pl from the EntityPro modules of TextPro 2.0
	 * @param lines
	 * @param hash
	 * @return
	 */
	private static LinkedList<String> assign_tag (List<String> lines, HashMap<String,HashMap<String,Integer>> hash){
		LinkedList<String> columnValues = new LinkedList<String> ();
		int onlyUniq = 0;
		int uniqNbr = 0;
		int i = 0;
		HashMap<Integer,Boolean> alreadyAssigned = new HashMap<Integer,Boolean> ();
		
		firstWhile: while (i<lines.size()) {
			//System.out.println(lines.get(i));
		   //add ($test[$i] ne "0"), 12-2006
		   if ( !lines.get(i).equals("0") && lines.get(i) == null ) {
			   columnValues.add(i,"");
			   alreadyAssigned.put(i, true);
			   //System.out.print("\n"); 
			   i++; 
			   continue firstWhile;            
		   }
		   else if ( lines.get(i).equals("")) { 
			   columnValues.add(i,"");
			   alreadyAssigned.put(i, true);
			   
			   i++; 
			   continue firstWhile;
		   }
		   String [] words = lines.get(i).split("\t");
		   //System.out.println(">>>"+words[0]);
		   //HashMap<String,HashMap<String,Integer>> hash = new HashMap<String,HashMap<String,Integer>> ();
		   /*if (! hash.containsKey(words[0].toLowerCase()) && (columnValues.size() <= i || columnValues.get(i) == null) 
				   && !alreadyAssigned.containsKey(i)){ */
		   if (! hash.containsKey(words[0].toLowerCase())){
		      //System.out.print(lines.get(i)+" O\n"); 
			   columnValues.add(i,"O");
			   alreadyAssigned.put(i, true);
		      i++;
		   } 
		   /*else if (alreadyAssigned.containsKey(i)){
			   i++;
		   }*/
		   else {
		      int j = 0;
		      String buffer = words[0].toLowerCase();
		      
		      //add words to phrase while we are in a phrase prefix and 
		      //the next word exists and is not a line break
		    
		      //System.out.println(hash.get(buffer).size());
		      String prevBuffer = "";
		      while (hash.containsKey(buffer) && hash.get(buffer).containsKey("PREFIX") &&
		    		 hash.get(buffer).size() == 1 && 
		             i+j < lines.size()-1 && lines.get(i+j+1) != null) {
		         j++;
		         words = lines.get(i+j).split("\t");
		         prevBuffer = buffer;
		         buffer += " "+words[0].toLowerCase();
		         
		      }
		      // remove words from entity
		      HashMap<String,Integer> classes = new HashMap<String,Integer> ();
		      if (hash.containsKey(buffer)){
		    	  classes = hash.get(buffer);
		      }
		      /*else{
		    	  buffer = prevBuffer;
		    	  j--;
		    	  classes = hash.get(buffer);
		      }*/
		      
		      // note: classes always contains pairs tag/amount
		      // remove words from phrase while current phrase is nonempty and 
		      // does not contain a phrase or is only a prefix
		      while (!buffer.equals("") &&
		             (classes.size() == 0 ||
		              (classes.size() == 2 && hash.containsKey(buffer) && hash.get(buffer).size() == 1 && hash.get(buffer).containsKey("PREFIX")) ||
		              (onlyUniq == 1 && 
		               (classes.size()-1 > 3 ||
		                (classes.size() -1 > 1 && !hash.get(buffer).containsKey("PREFIX")))))) {
		         j--;
		         words = buffer.split("\t");
		         words = removeLastElt(words);
		         buffer = joinArray(words, "\t").toLowerCase();
		         if (hash.containsKey(buffer)){
		        	 classes = hash.get(buffer);
		         }
		         else{
		        	 classes.clear();
		         }
		      }
		     
		      // if no complete entity was found
		      if (buffer.equals ("")) { 
		    	 columnValues.add(i,"O");
		    	 alreadyAssigned.put(i, true);
		         //System.out.print (lines.get(i)+" O\n"); 
		         i++; 
		         continue firstWhile; 
		      }
		      // get category
		      String bestCat = "UNDEF";
		      int bestCatNbr = 0;
		      if (hash.containsKey(buffer)){
		    	  for (HashMap.Entry<String, ?> entry : hash.get(buffer).entrySet()) {
			      //for (String key: hash.get(buffer).keySet()) {
			         if (! entry.getKey().equals("PREFIX") && hash.get(buffer).get(entry.getKey()) > bestCatNbr) {
			            bestCatNbr = hash.get(buffer).get(entry.getKey());
			            bestCat = entry.getKey();
			         }
			      }
		      }
		      // does the phrase occur frequently enough in the training data?
		      if (bestCatNbr < uniqNbr) { 
		    	 columnValues.add(i,"O");
		    	 alreadyAssigned.put(i, true);
		         //System.out.print(lines.get(i)+" O\n"); 
		         i++; 
		         continue firstWhile; 
		      }
		      for (int k=i;k<=i+j;k++) {
		         if (k == i) { 
		        	 columnValues.add(k, "B-"+bestCat);
		        	 alreadyAssigned.put(k, true);
		        	 //System.out.print (lines.get(k)+" B-"+bestCat+"\n"); 
		         }
		         else {
		        	 //if (!alreadyAssigned.containsKey(k)){
				         columnValues.add(k, "I-"+bestCat);
				         alreadyAssigned.put(k, true);
		        	 //}
		        	 //System.out.print (lines.get(k)+" I-"+bestCat+"\n"); 
		         }
		      }
		      i += j+1;
		      //i++;
		   }
		}
		//columnValues.removeFirst();
		columnValues.remove(columnValues.size()-1);
		return columnValues;
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
