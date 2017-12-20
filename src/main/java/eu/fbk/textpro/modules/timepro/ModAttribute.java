package eu.fbk.textpro.modules.timepro;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import eu.fbk.textpro.wrapper.OBJECTDATA;

public class ModAttribute {
	
	public static int colToken = 0;
	public static int colLemma = 1;
	public static int colTimex = 2;
	public static int colMod = 3;
	
	public static int nbCol = 4;
	
	/**
	 * get the number of lines of a file
	 */
	private static int getNbLine (Iterable<String> lines){
		Iterator it = lines.iterator();
		int cpt = 0;
		
		while(it.hasNext()){
			String ltmp=(String) it.next();
			if(ltmp.trim().startsWith("# FIELDS: ")){
				continue;
			}
			cpt ++;
		}
		return cpt;
	}
	
	private static LinkedList<String> addModAttribute (Iterable<String> linesIt, HashMap<String,String> listTriggerMod){
		
		int nbLine = getNbLine(linesIt);
		// convert OBJECTDATA format in String [] []
		String [][] lines = new String [nbLine] [nbCol];
		Iterator it = linesIt.iterator();
		
		int cptL = 0;
		while(it.hasNext()){
			String ltmp=(String) it.next();
			if(ltmp.trim().startsWith("# FIELDS: ")){
				continue;
			}
			
			if (ltmp.contains("\t")) {
				String [] elts = ltmp.split("\t");
				String [] elts2 = new String [nbCol];
				for (int k=0; k<elts.length; k++) {
					elts2[k] = elts[k];
				}
				lines[cptL] = elts2;
			}
			else {
				String [] elts = null;
				lines[cptL] = elts;
			}
			cptL ++;
		}
				
		
		String timex = "";
		String mod = "";
		for (int i=0; i<lines.length; i++){
			if(lines[i] != null && lines[i][colToken] != null && lines[i][colTimex] != null){
				//in timex
				if (lines[i][colTimex].startsWith("B-")){
					timex = lines[i][colLemma];
					if (lines[i+1] != null && lines[i+1][colTimex].startsWith("I-")){
						for (int l=i+1; l<i+15; l++){
							if (l<lines.length && lines[l] != null){
								if (lines[l][colTimex].startsWith("I-")){
									timex += " "+lines[l][colLemma];
								}
								else{
									break;
								}
							}
						}
					}
					
					mod = getTriggerMod(timex,listTriggerMod);
					if(mod.equals("")){
						mod = getTriggerModBef(lines, i, listTriggerMod);
					}
					
					if(!mod.equals("")){
						lines[i][colMod] = mod;
						if (lines[i+1] != null && lines[i+1][colTimex].startsWith("I-")){
							for (int l=i+1; l<i+15; l++){
								if (l<lines.length && lines[l] != null){
									if (lines[l][colTimex].startsWith("I-")){
										lines[l][colMod] = mod;
									}
									else{
										break;
									}
								}
							}
						}
					}					
				}
			}
		}
		
		LinkedList<String> columnValues = new LinkedList<String> ();
		
		
		for (int j=0; j<lines.length; j++) {
			if (lines[j] != null && (lines[j][colTimex].startsWith("B-") || lines[j][colTimex].startsWith("I-")) && lines[j][colMod] != null){
				columnValues.add(lines[j][colMod]);
			}
			else if (lines[j] != null) {
				columnValues.add("O");
			}
			else {
				columnValues.add("");
			}
		
		}
		
		return columnValues;
	}
	
	private static String getTriggerMod(String timex, HashMap<String,String>listTriggerMod){
		String mod = "";
		
		for (String key : listTriggerMod.keySet()){
			if (timex.matches(".* "+key+" .*") || timex.matches("^"+key+" .*") 
					|| timex.matches(".* "+key+"$") || timex.equals(key)
					|| timex.matches(".*-"+key+" .*") || timex.matches(".*-"+key+"$")){
				mod = listTriggerMod.get(key);
				break;
			}
		}
		
		return mod;
	}
	
	private static String getTriggerModBef(String [][] lines, int i, HashMap<String,String>listTriggerMod){
		String mod = "";
		
		String bef = "";
		
		for (int j=i-1; j>0 && j>i-7; j--){
			if(lines[j] != null && lines[j][colTimex] != null && lines[j][colTimex].equals("O")){
				bef = lines[j][colLemma]+" "+bef;
			}
			else{
				break;
			}
		}
		
		
		for (String key : listTriggerMod.keySet()){
			if (bef.matches(".* "+key+" .*") || bef.matches("^"+key+" .*") || bef.matches(".* "+key+"$") || bef.equals(key)){
				mod = listTriggerMod.get(key);
				break;
			}
		}
		
		return mod;
	}
	
	private static HashMap<String,String> readListTriggerMod (String fileName) throws IOException{
		HashMap<String,String> listTriggerMod = new HashMap<String,String> ();
		FileReader fr = new FileReader(fileName);
		BufferedReader br=new BufferedReader(fr);
		String line;
		
		while ((line=br.readLine())!=null){
			if(line.contains("\t")){
				String [] elts = line.split("\t");
				listTriggerMod.put(elts[0], elts[1]);
			}
		}
		
		return listTriggerMod;
	}
	
	public static OBJECTDATA analyze_modifiers (OBJECTDATA filein, String resourceMod) throws ParseException, InterruptedException, IOException {
		
		HashMap<String,String> listTriggerMod = readListTriggerMod(resourceMod);
		//System.out.println(resourceMod+ " : "+listTriggerMod.size());
		
		Hashtable<String,Integer> copyThisTokens = new Hashtable<String,Integer> ();
		copyThisTokens.put("token",1);
		copyThisTokens.put("lemma",2);
		copyThisTokens.put("tmx", 3);
		
		filein.addColumn("mod",addModAttribute(filein.getFileLineByLine(copyThisTokens,true),listTriggerMod));
		
		
		return filein;
	}
	
	
	
	/**
	 * @param args
	 * @throws ParseException 
	 * @throws IOException 
	 */
	public static void main(String[] args) throws ParseException, IOException {
		
	}

}
