package eu.fbk.textpro.modules.tagpro;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import eu.fbk.textpro.wrapper.OBJECTDATA;


public class ProcessFeatures  {
    
	
    
    public ProcessFeatures(){

    }
    public OBJECTDATA run_extractFeatures (String language, OBJECTDATA filein) throws Exception{
    	System.out.println("run extract features");
    	return extractFeatures(language, filein);
    }
    

    
  
	public static OBJECTDATA extractFeatures(String language, OBJECTDATA filein)
			throws Exception {
		//filein.getFileLineByLine().forEach(System.out::println);
		//filein.saveInFile(filein.input_file.getAbsolutePath()+".nerIn", TEXTPROCONSTANT.encoding, false);

		LinkedList<String> columValues = new LinkedList<String>();
		
		Hashtable<String,Integer> copyThisTokens = new Hashtable<String,Integer> ();
		
		if (language.equals("ENG") || language.equals("ITA")){
			copyThisTokens.put("token",1);
		}
		else if (language.equals("FRE")) {
			//copyThisTokens.put("tokennorm",1);
			copyThisTokens.put("token",1);
		}
			
		if (language.equals("FRE") || language.equals("ITA")){
			copyThisTokens.put("full_morpho",2);
		}
		
		
		List<LinkedList<String>> additionalFeat = getFeatures(filein.getFileLineByLine(copyThisTokens,true));
		filein.addColumn("tokennormaccent",additionalFeat.get(0));
		filein.addColumn("tokentype",additionalFeat.get(1));
		filein.addColumn("pref2",additionalFeat.get(2));
		filein.addColumn("pref3",additionalFeat.get(3));
		filein.addColumn("pref4",additionalFeat.get(4));
		filein.addColumn("suf2",additionalFeat.get(5));
		filein.addColumn("suf3",additionalFeat.get(6));
		filein.addColumn("suf4",additionalFeat.get(7));
		
		
		
		if (language.equals("FRE") || language.equals("ITA")){
			List<LinkedList<String>> morphoPosFeat = getMorphoPosFeat(filein.getFileLineByLine(copyThisTokens,true));
			filein.addColumn("v", morphoPosFeat.get(0));
			filein.addColumn("nc", morphoPosFeat.get(1));
			filein.addColumn("adj", morphoPosFeat.get(2));
			filein.addColumn("adv", morphoPosFeat.get(3));
			filein.addColumn("prep", morphoPosFeat.get(4));
			filein.addColumn("det", morphoPosFeat.get(5));
			filein.addColumn("cl", morphoPosFeat.get(6));
			filein.addColumn("coo", morphoPosFeat.get(7));
			filein.addColumn("pres", morphoPosFeat.get(8));
			filein.addColumn("pron", morphoPosFeat.get(9));
			filein.addColumn("np", morphoPosFeat.get(10));
			//filein.addColumn("np2", morphoPosFeat.get(11));
			//filein.addColumn("det2", morphoPosFeat.get(12));
		}
		
		return filein;
	}
	
	public static List<LinkedList<String>> getMorphoPosFeat (Iterable<String> lines){
		List<LinkedList<String>> columnValues = new ArrayList<LinkedList<String>> ();
		
		for (int i=0; i<=10; i++) {
			columnValues.add(new LinkedList<String> ());
		}
		
		Iterator it = lines.iterator();
		int cpt = 0;
		while(it.hasNext()){
			String ltmp=(String) it.next();
			if(ltmp.trim().startsWith("# FIELDS: ")){
				continue;
			}
			
			if (ltmp.contains("\t")) {
			
				String [] line = ltmp.split("\t");
				
				String token = line[0];
				String fullMorpho = line[1];
				
				if (!fullMorpho.equals("_")){
					if (! fullMorpho.contains(" ")){
						fullMorpho += " ";
					}
					String [] morphoAnal = fullMorpho.split(" ");
					for (int j=0; j<morphoAnal.length; j++){
						if(morphoAnal[j].contains("+")){
						String tmpPos = morphoAnal[j].split("\\+")[1];
						
						if(tmpPos.equals("v") && columnValues.get(0).size() == cpt){
							columnValues.get(0).add("1");
						}
						else if(tmpPos.equals("nc") && columnValues.get(1).size() == cpt){
							columnValues.get(1).add("1");
						}
						else if(tmpPos.equals("adj") && columnValues.get(2).size() == cpt){
							columnValues.get(2).add("1");
						}
						else if(tmpPos.startsWith("adv") && columnValues.get(3).size() == cpt){
							columnValues.get(3).add("1");
						}
						else if(tmpPos.equals("prep") && columnValues.get(4).size() == cpt){
							columnValues.get(4).add("1");
						}
						else if(tmpPos.startsWith("det") && columnValues.get(5).size() == cpt){
							columnValues.get(5).add("1");
						}
						else if(tmpPos.equals("cl") && columnValues.get(6).size() == cpt){
							columnValues.get(6).add("1");
						}
						else if(tmpPos.startsWith("coo") && columnValues.get(7).size() == cpt){
							columnValues.get(7).add("1");
						}
						else if(tmpPos.startsWith("pres") && columnValues.get(8).size() == cpt){
							columnValues.get(8).add("1");
						}
						else if(tmpPos.startsWith("pron") && columnValues.get(9).size() == cpt){
							columnValues.get(9).add("1");
						}
						else if(tmpPos.equals("np") && columnValues.get(10).size() == cpt){
							columnValues.get(10).add("1");
						}
						
						//morphological analysis nel formato di Lefff
						/*	if(tmpPos.equals("v") || tmpPos.startsWith("aux")){
								columnValues.get(0).add("1");
							}
							else if(tmpPos.equals("nc")){
								columnValues.get(1).add("1");
							}
							else if(tmpPos.equals("adj")){
								columnValues.get(2).add("1");
							}
							else if(tmpPos.startsWith("adv")){
								columnValues.get(3).add("1");
							}
							else if(tmpPos.equals("prep")){
								columnValues.get(4).add("1");
							}
							else if(tmpPos.startsWith("cl")){
								columnValues.get(5).add("1");
							}
							else if(tmpPos.equals("coo")){
								columnValues.get(6).add("1");
							}
							else if(tmpPos.startsWith("ponct") || tmpPos.startsWith("parent") || tmpPos.startsWith("epsilon")){
								columnValues.get(7).add("1");
							}
							else if(tmpPos.startsWith("pr") || tmpPos.equals("ilimp") || tmpPos.equals("caimp")){
								columnValues.get(8).add("1");
							}
							else if(tmpPos.startsWith("cs")){
								columnValues.get(9).add("1");
							}
							else if(tmpPos.equals("np")){
								columnValues.get(10).add("1");
							}
							else if(tmpPos.equals("det")){
								columnValues.get(11).add("1");
							}
							else if(tmpPos.equals("pres")){
								columnValues.get(12).add("1");
							}
							*/
						}
					}
				}
				for (int j=0; j<=10; j++) {
					if (columnValues.get(j).size() == cpt) {
						columnValues.get(j).add("0");
					}
				}
			}
			else {
				for (int j=0; j<=10; j++) {
					if (columnValues.get(j).size() == cpt ) {
						columnValues.get(j).add("");
					}
				}
			}
			
			
			
			cpt++;
		}	
		
		return columnValues;
	}
	
    
   
	public static List<LinkedList<String>> getFeatures (Iterable<String> lines){
		List<LinkedList<String>> columnValues = new ArrayList<LinkedList<String>> ();
		
		for (int i=0; i<=7; i++) {
			columnValues.add(new LinkedList<String> ());
		}
		
		Iterator it = lines.iterator();
		int cpt = 0;
		while(it.hasNext()){
			String ltmp=(String) it.next();
			if(ltmp.trim().startsWith("# FIELDS: ")){
				continue;
			}	
			List<String> line = new ArrayList<String> ();
			line.add(ltmp);
			String tok = line.get(0);
			
			if (!tok.equals("")){
				String token = tok.split("\t")[0];
				String tokenType = getTokenType(token);
				String tokenNorm = "_";
				
				if (tokenType.equals ("DIG")){
					tokenNorm = "_NUM_";
				}
				else {
					tokenNorm = getTokenNorm(token);
				}
				
				if(tokenType.equals("LOW")||tokenType.equals("CAP")||tokenType.equals("UPP")){
					//tokenNorm and tokenType should be produced by TokenPro??
					columnValues.get(0).add(tokenNorm);
					columnValues.get(1).add(tokenType);
					columnValues.get(2).add(getPrefix(2,token));
					columnValues.get(3).add(getPrefix(3,token));
					columnValues.get(4).add(getPrefix(4,token));
					columnValues.get(5).add(getSufix(2,token));
					columnValues.get(6).add(getSufix(3,token));
					columnValues.get(7).add(getSufix(4,token));
					/*content += w+"\t"+tokenNorm+"\t"+tokenType+"\t"
							+getPrefix(2,w)+"\t"+getPrefix(3,w)+"\t"+getPrefix(4,w)+"\t"
							+getSufix(2,w)+"\t"+getSufix(3,w)+"\t"+getSufix(4,w)+"\t"+pos+"\n";*/
				}
				else{
					columnValues.get(0).add(tokenNorm);
					columnValues.get(1).add(tokenType);
					for (int j=2; j<8; j++){
						columnValues.get(j).add("_");
					}
					/*content += w+"\t"+tokenNorm+"\t"+tokenType+"\t"
							+"\t_"+"\t_"+"\t_"+"\t_"+"\t_"+"\t_"+"\t"+pos+"\n";*/
				}
			}
			else{
				for (int j=0; j<8; j++){
					columnValues.get(j).add("");
				}
			}
		}
		
		return columnValues;
	}
	
	public static String getTokenNorm (String tok){
		tok = tok.toLowerCase();
		tok = tok.replaceAll("é","e").replaceAll("è","e").replaceAll("ê","e");
		tok = tok.replaceAll("à","a").replaceAll("ù","u").replaceAll("û","u");
		tok = tok.replaceAll("ô","o").replaceAll("î","i").replaceAll("â","a");
		return tok;
	}

	public static String getPrefix (int length, String tok){
		tok = tok.toLowerCase();
		if (tok.length() < length){
			return "_";
		}
		else if (tok.length() == length){
			return tok;
		}
		else {
			String pref = "";
			String [] tokC = tok.split("");
			for (int i=0; i<length; i++){
				pref += tokC[i];
			}
			return pref;
		}
	}

	public static String getSufix (int length, String tok){
		if (tok.length() < length){
			return "_";
		}
		else if (tok.length() == length){
			return tok;
		}
		else {
			String suf = "";
			String [] tokC = tok.split("");
			for (int i=length; i>0; i--){
				suf += tokC[tokC.length-i];
			}
			return suf;
		}
	}

	public static String getTokenType (String tok){
		if (tok.matches("^[A-ZÀÉÙÈÊÇÔ']+$")){
			return "UPP";
		}
		else if (tok.matches("^[A-ZÀÉÊÇÔ].*")){
			return "CAP";
		}
		else if (tok.matches("[a-zàéùèêçô']+")){
			return "LOW";
		}
		else if (tok.matches("^[\\.,]?[0-9][0-9\\.,]*")){
			return "DIG";
		}
		else if(tok.matches("[\\.,;:\\?\\!]")){
			return "PUN";
		}
		else if(tok.contains("[a-zàéùèêôçA-ZÀÉÙÈÊÇÔ]") && tok.contains("[0-9',-_;:^]")){
			return "JLD";
		}
		else if(tok.matches("[a-zàéùèêçôA-ZÀÉÙÈÊÇÔ]+\\.([a-zàéùèêçôA-ZÀÉÙÈÊÇÔ]+\\.?)?")){
			return "ABB";
		}
		else if(tok.matches("[%-_/+=*]")){
			return "SYM";
		}
		
		
		return "OTH";
	}
	

}
