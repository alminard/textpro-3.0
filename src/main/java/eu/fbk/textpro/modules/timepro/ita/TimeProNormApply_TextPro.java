package eu.fbk.textpro.modules.timepro.ita;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import eu.fbk.textpro.modules.timepro.ita.*;

import eu.fbk.textpro.wrapper.OBJECTDATA;

import java.io.*;

import java.util.concurrent.TimeUnit;

public class TimeProNormApply_TextPro {


	public static int colToken = 0;
	public static int colPOS = 1;
	public static int colLemma = 2;
	public static int colTimex = 3;
	public static int colTimexID = 4;
	public static int colRules = 5;
	public static int colSent = 7;
	public static int colValTimex = 8;
	public static int colAnchor = 9;
	public static int colBeginPoint = 10;
	public static int colEndPoint = 11;
	public static int nbCol = 12;

	public static HashMap<String,String> numberLetter = fill_hash_numbers();

	public static HashMap<String,String> day = fill_hash_days();
	
	public static HashMap<String,String> fill_hash_numbers(){
		HashMap<String,String> hash = new HashMap<String,String> ();
		hash.put("un'","1");
		hash.put("un","1");
		hash.put("uno","1");
		hash.put("due","2");
		hash.put("tre","3");
		hash.put("quattro","4");
		hash.put("cinque","5");
		hash.put("sei","6");
		hash.put("sette","7");
		hash.put("otto","8");
		hash.put("nove","9");
		hash.put("dieci","10");
		hash.put("undici","11");
		hash.put("dodici","12");
		return hash;
	}
	
	
	public static HashMap<String,String> fill_hash_days(){
		HashMap<String,String> hash = new HashMap<String,String> ();
		hash.put("lun","1");
		hash.put("mar","2");
		hash.put("mer","3");
		hash.put("gio","4");
		hash.put("ven","5");
		hash.put("sab","6");
		hash.put("dom","7");
		return hash;
	}
	
	/**
	 * get the last timex id
	 */
	private static int getCptTimexId (Iterable<String> lines){
		Iterator it = lines.iterator();
		int lastTimexId = 0;
		while(it.hasNext()){
			String ltmp=(String) it.next();
			if(ltmp.trim().startsWith("# FIELDS: ")){
				continue;
			}
			if(ltmp.contains("\t") && ltmp.split("\t")[2].startsWith("tmx")){
				lastTimexId = Integer.parseInt(ltmp.split("\t")[2].replace("tmx",""));
			}
		}
		return lastTimexId;
	}
	
	
	/**
	 * get the number of lines of the file
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
	
	/**
	 * Main function
	 * For each timex:
	 * - identify if it has an anchorTime and/or a beginPoint and/or an endPoint and their values
	 * - send the timex and the anchorTime to TimeNorm, which return the normalized value of the timex
	 * - if TimeNorm failed, compute a value thanks to simple rules
	 * @param lines: array containing one array by token
	 * @param dct: document creation time
	 * @return lines with new columns (value, anchorTime, beginPoint, endPoint)
	 * @throws ParseException
	 */
	public static List<LinkedList<String>> addTimexValue (Iterable<String> linesIt, String dct) throws ParseException{
		String idTimex = "tmx0";
		

		
		int cptTimexId = getCptTimexId(linesIt);
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
		
		
		// add num sentence
		int cptSent = 0;
		for (int i=0; i<lines.length; i++) {
			if (lines[i] == null || lines[i][0].equals("")) {
				cptSent ++;
			}
			else {
				lines[i][colSent] = Integer.toString(cptSent);
			}
		}
		
		
		for (int i=0; i<lines.length; i++){
			// If first token of a timex
			if(lines[i] != null && lines[i][colTimex] != null && lines[i][colTimex].startsWith("B-")){
				boolean hasAnchorTime = true;
				boolean hasBeginPoint = false;
				boolean hasEndPoint = false;
				String pointID [] = new String [2];
				
				idTimex = lines[i][colTimexID];
				
				String anchorTime = dct;
				if(anchorTime.contains("T") && anchorTime.length()>10){
					anchorTime = anchorTime.substring(0,10);
				}
				
				String anchorTimeID = "tmx0";
				
				String timex = lines[i][colToken];
				int j = i+1;
				int numTok = 1;
				while (lines[j] != null && lines[j][colTimex] != null && lines[j][colTimex].startsWith("I-")){
					timex += " "+lines[j][colToken];
					j++;
					numTok++;
				}
				
			

				/*
				 * TimeNorm
				 */
				
				if(!anchorTimeID.equals("tmx0") && !anchorTimeID.equals("")){
					anchorTime = getValTimexID(anchorTimeID, lines, anchorTime);
				}
				anchorTime = checkDate(anchorTime);
				if(anchorTime.equals("")){
					anchorTime = dct;
				}
				
				//"ore 17 - 23" --> two timexes "ore 17" and "ore 23"
				if(timex.matches("([012])?[0-9]") && i > 3 && lines[i-3][colToken].equals("ore")){
					timex = "ore "+timex;
				}
				else if(timex.matches("([012])?[0-9]")){
					boolean modify = false;
					int li=0;
					if(i>6){
						li = i-6;
					}
					for(int l=li; l<i; l++){
						if (lines[l] != null) {
							if(lines[l][colToken].equals("ore")){
								//timex = "ore "+timex;
								modify = true;
								break;
							}
							else if(l==i-1 && lines[l][colToken].matches("d?alle?'?")){
								//timex = "ore "+timex;
								modify = true;
								break;
							}
							if(lines[l][colRules] == null){
								break;
							}
						}
						else {
							break;
						}
					}
					
					if(modify){
						if(lines[i][colTimex].endsWith("TIME")){
							timex = "ore "+timex;
						}
						else{
							for(int l=i+numTok+1; l<lines.length; l++){
								if(lines[l] != null && lines[l][colTimexID].startsWith("tmx")){
									if(lines[l][colRules].matches("_UNIT_") || lines[l][colRules].matches("_MONTH_")){
										timex = timex + " "+ lines[l][colToken];
										modify = true;
										break;
									}
								}
								else if(lines[l] == null || lines[l][colTimexID] == null || !lines[l][colTimexID].startsWith("tmx")){
									break;
								}
							}
						}
					}
					
					if(!modify){
						if(lines[i][colTimex].equals("B-TIME")){
							timex = "ore "+timex;
							modify = true;
						}
					}
					
					if(!modify){
						if(lines[i+1][colToken].equals("e") || (lines[i+1][colToken].equals("o"))){
							for(int l=i+numTok+1; l<lines.length; l++){
								if(lines[l] != null && lines[l].length >= colTimexID && lines[l][colTimexID] != null && lines[l][colTimexID].startsWith("tmx")){
									if(lines[l][colRules].matches("_UNIT_") || lines[l][colRules].matches("_MONTH_")){
										timex = timex + " "+ lines[l][colToken];
										modify = true;
										break;
									}
								}
								else if(lines[l] == null || lines[l][colTimexID] == null || (l>i+numTok+2 && !lines[l][colTimexID].startsWith("tmx"))){
									break;
								}
							}
						}
					}
				}
				else if (lines[i][colRules].equals("_CN_") && ! timex.matches("[0-9]+") && !timex.contains(" ")){
					int li=0;
					if(i>3){
						li = i-3;
					}
					int ls = lines.length;
					if(i+numTok<lines.length-3){
						ls = i+numTok+3;
					}
					for(int l=li; l<ls; l++){
						if (lines[l] != null && lines[l][colRules] != null && (lines[l][colRules].equals("_UNIT_") || lines[l][colRules].equals("_MONTH_"))){
							if(l<i){
								timex = lines[l][colToken]+" "+timex;
							}
							else{
								timex += " "+lines[l][colToken];
							}
							break;
						}
					}
				}
				else if(timex.matches(".* [012]?[0-9]") && lines[i+numTok] != null && lines[i+numTok][colToken].equals("e")){
					int ls = lines.length;
					if(i+numTok<lines.length-5){
						ls = i+numTok+5;
					}
					for(int l=i+numTok-1; l<ls; l++){
						if (lines[l] != null && lines[l][colRules] != null && (lines[l][colRules].equals("_UNIT_") || lines[l][colRules].equals("_MONTH_"))){
							timex += " "+lines[l][colToken];
							break;
						}
					}
				}
				else if(timex.contains(" e ") && timex.matches("^([012])?[0-9] .*")){
					timex = timex.replace(" e ", ":");
				}
				else if(timex.matches(".*\'[1-9][0-9].*")){
					timex = timex.replace("'", "");
				}
				else if(timex.startsWith("verso") || timex.startsWith("circa")){
					timex = timex.replace("verso","");
					timex = timex.replace("circa","");
				}
				else if(timex.contains("e mezzo")){
					timex = timex.replace("e ","");
				}
				else if(lines[i][colRules].equals("_DAY_") && timex.matches(".* [123][0-9] .*") && lines[i+numTok-1][colRules].equals("_MONTH_")){
					timex = timex.replace(lines[i][colToken]+" ", "");
				}
				else if(timex.contains("di ritardo")){
					timex = timex.replace(" di ritardo","");
				}
				else if(timex.contains("un massimo") || timex.contains("un minimo")){
					timex = timex.replace("un massimo di ","");
					timex = timex.replace("un minimo di ","");
				}
				
				String value = "";
				//System.out.println(timex.toLowerCase()+" : "+anchorTime);
				value = TestTimeNorm_parseAll.getTimeNorm(timex.toLowerCase(), anchorTime);
				
				if(value.contains(";")){
					String [] listVal = value.split(";");
					value = listVal[0];
				
					if(value.matches("[0-9]+-.*")){
						int nbCar = value.length();
						for(int l=0; l<listVal.length; l++){
							if(listVal[l].length() > nbCar){
								value = listVal[l];
								break;
							}
						}
					}
					
					if(timex.contains("prossimo")){
						if(listVal.length>1){
							value = listVal[1];
						}
					}
					
					else if(lines[i][colTimex].equals("B-DATE") && listVal.length>1 
							&& value.matches("[0-9]+-[0-9]+-[0-9]+") && listVal[1].matches("[0-9]+-[0-9]+-[0-9]+")){
						SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
						Date d1 = format.parse(anchorTime);
						Date d2 = format.parse(value);
						Date d3 = format.parse(listVal[1]);
						if(d3.getTime() - d1.getTime() < d1.getTime() - d2.getTime()){
							value = listVal[1];
						}
					}
					else{
						for (int l=i-1; l>0; l--){
							if(lines[l] != null && lines[l][colTimex] != null && lines[l][colToken].equals("fino") && listVal.length>1){
								if(!value.equals(anchorTime)){
									value = listVal[1];
								}
								break;
							}
							if( lines[l] == null || lines[l][colTimex] == null){
								break;
							}
						}
					}
					
					//DURATION --> P1D
					if(lines[i][colTimex].endsWith("DURATION")){
						for (int l=0; l<listVal.length; l++){
							if(listVal[l].startsWith("P")){
								value = listVal[l];
							}
						}
						if(value.matches("P.(MO|MI|AF|EV|NI|DT)")){
							value = value.replace("P","PT");
						}
					}
					//SET --> P1D
					else if(lines[i][colTimex].endsWith("SET")){
						for (int l=0; l<listVal.length; l++){
							if(listVal[l].startsWith("P") || listVal[l].startsWith("X")){
								value = listVal[l];
							}
						}
					}
					//TIME --> 1994-05-25T04:00
					else if(lines[i][colTimex].endsWith("TIME")){
						for(int l=i-1; l>0; l--){
							if(lines[l] != null && lines[l][colTimex] != null && lines[l][colTimex].endsWith("DATE") && l>i-10
									&& lines[l][colValTimex].matches("[0-9]+-[0-9]+-[0-9]+") && value.contains("T")){
								value = lines[l][colValTimex]+"T"+value.split("T")[1];
								break;
							}
							if(lines[l] == null || lines[l][colTimex] == null){
								break;
							}
						}
					}
				}
				
				if(value.equals("FAIL") || timex.contains("mestr")){
					value = getValueSpecialCases (timex, anchorTime);
					
					if(value.equals("FAIL")){
						value = getValueFAIL(timex, anchorTime);
					}
				}
				
				//System.out.println(i+" : "+numTok+" : "+value);
				for (int k = i; k < i+numTok; k++){
					lines[k][colValTimex] = value;
				}
				
				String beginPointID = "-";
				String endPointID = "-";
				if(anchorTimeID.equals("")){
					anchorTimeID = "-";
				}
				if(hasBeginPoint && pointID[0] == null){
					beginPointID = "tmx0";
				}
				else if(pointID[0] != null){
					beginPointID = pointID[0];
				}
				if(hasEndPoint && pointID[1] == null){
					endPointID = "tmx0";
				}
				else if(pointID[1] != null){
					endPointID = pointID[1];
				}
				for (int k = i; k < i+numTok; k++){
					lines[k][colAnchor] = anchorTimeID;
					lines[k][colBeginPoint] = beginPointID;
					lines[k][colEndPoint] = endPointID;
				}
				
			}
		}
		
		//int cpt = Integer.parseInt(idTimex.substring(3));
		//lines = getEmptyTimex(lines, dct, cpt);
		
		List<LinkedList<String>> columnValues = new ArrayList<LinkedList<String>> ();
		
		columnValues.add(new LinkedList<String> ());
		columnValues.add(new LinkedList<String> ());
		columnValues.add(new LinkedList<String> ());
		columnValues.add(new LinkedList<String> ());
		
		
		for (int j=0; j<lines.length; j++) {
			if (lines[j] != null && (lines[j][colTimex].startsWith("B-") || lines[j][colTimex].startsWith("I-"))){
				columnValues.get(0).add(lines[j][colValTimex]);
				columnValues.get(1).add(lines[j][colAnchor]);
				columnValues.get(2).add(lines[j][colBeginPoint]);
				columnValues.get(3).add(lines[j][colEndPoint]);
			}
			else if (lines[j] != null) {
				columnValues.get(0).add("O");
				columnValues.get(1).add("O");
				columnValues.get(2).add("O");
				columnValues.get(3).add("O");
			}
			else {
				columnValues.get(0).add("");
				columnValues.get(1).add("");
				columnValues.get(2).add("");
				columnValues.get(3).add("");
			}
		
		}
		
		return columnValues;
	}
	
	
	private static String getValueFAIL (String timex, String anchorTime){
		String value = "";
		
		String year = anchorTime.substring(0,4);
		
		if(timex.matches("per il momento")){value = "PRESENT_REF";}
		else if(timex.matches("quel momento")){value = "PAST_REF";} //ref date
		else if(timex.matches(".*([0-9]+) ennale")){
			Pattern p = Pattern.compile(".*([0-9]+) ennale");
			Matcher m = p.matcher(timex);
			if(m.find()){
				value = "P"+m.group(1)+"Y";
			}
		}
		else if(timex.matches("16 ora italiana di ieri")){value = "";}//precise TIME
		else if(timex.matches("a questo punto")){value = "PRESENT_REF";}
		else if(timex.matches("stesso periodo del [0-9]+")){value = "";}//precise DATE
		else if(timex.matches("il prossimo biennio")){value = "P2Y";}
		else if(timex.matches("la prima met(a|à) di quest' anno")){value = year+"-H1";}
		else if(timex.matches("data")){value = "";}//precise DATE
		else if(timex.matches("primi mesi del ([0-9]+)")){
			Pattern p = Pattern.compile(".*del ([0-9]+)");
			Matcher m = p.matcher(timex);
			if(m.find()){
				value = m.group(1)+"-XX";
			}
		}
		else if(timex.matches("qualche mese a questa parte")){value = "PXM";}
		else if(timex.matches("principio dell' anno")){value = year;}
		else if(timex.matches("il ([0-9]+) anniversario")){
			Pattern p = Pattern.compile("il ([0-9]+) .*");
			Matcher m = p.matcher(timex);
			if(m.find()){
				value = "P"+m.group(1)+"Y";
			}
		}
		else if(timex.matches("tutti i giorni")){value = "P1D";}
		else if(timex.matches("all' anno")){value = "P1Y";}
		else if(timex.matches(".*tutti gli anni")){value = "P1Y";}
		else if(timex.matches(".*altro giorno")){value = "";}//precise DATE
		else if(timex.matches("un giorno all' altro")){value = "XXXX-XX-XX";}
		else if(timex.matches(".*anno scolastico")){value = "XXXX-XX-XX";}
		else if(timex.matches("pomeriggio del giorno [0-9]+")){
			Pattern p = Pattern.compile("^pomeriggio del giorno ([0-9]+)$");
			Matcher m = p.matcher(timex);
			if(m.find()){
				value = year+"-XX-"+m.group(1)+"TAF";
			}
		}
		else if(timex.matches("prima giornata")){value = "XXXX-XX-XX";}
		else if(timex.matches("queste annate")){value = "XXXX-XX-XX";}
		else if(timex.matches("era de.*")){value = "XXXX-XX-XX";}
		else if(timex.matches("a suo tempo")){value = "PAST_REF";}
		else if(timex.matches("quella data")){value = "";}//precise DATE
		else if(timex.matches("medioevo")){value = "XXXX-XX-XX";}
		else if(timex.matches(".*epoca.*")){value = "PAST_REF";}
		else if(timex.matches("due week(-| )?end")){value = "P2WE";}
		else if(timex.matches("[1-9].?[0-9]+ anni")){
			Pattern p = Pattern.compile("([1-9]).?([0-9]+) anni");
			Matcher m = p.matcher(timex);
			if(m.find()){
				value = "P"+m.group(1)+m.group(2)+"Y";
			}
		}
		else if(timex.matches("prim(o|i) giorn(o|i) di scuola")){value = "XXXX-XX-XX";}
		else if(timex.matches("prim(o|i) mes(e|i) dell' anno prossimo")){value = Integer.toString(Integer.parseInt(year)+1)+"-XX";}
		else if(timex.matches(".*tempo")){value = "XXXX-XX-XX";}
		else if(timex.matches(".*n(u|o)ttata")){value = "XXXX-XX-XXTNI";}
		else if(timex.matches("(.*) ore e mezzo")){
			Pattern p = Pattern.compile("(.*) ore.*");
			Matcher m = p.matcher(timex);
			if(m.find()){
				String ore = m.group(1);
				if(numberLetter.containsKey(ore)){
					ore = numberLetter.get(ore);
				}
				value = "T"+ore+"H30M";
			}
		}
		else if(timex.matches(".* mese e mezzo")){
			Pattern p = Pattern.compile("(.*) mese.*");
			Matcher m = p.matcher(timex);
			if(m.find()){
				String num = m.group(1);
				if(numberLetter.containsKey(num)){
					num = numberLetter.get(num);
				}
				value = "P"+num+".5M";
			}
		}
		else if(timex.matches("[1-9] anni e [1-9] mesi")){
			Pattern p = Pattern.compile("(.*) anni e (.*) mesi");
			Matcher m = p.matcher(timex);
			if(m.find()){
				value = "P"+m.group(1)+"Y"+m.group(2)+"M";
			}
		}
		else if(timex.matches("tutti i (lun|mar|mer|gio|ven|sab|dom).*")){
			Pattern p = Pattern.compile(".* (lun|mar|mer|gio|ven|sab|dom).*");
			Matcher m = p.matcher(timex);
			if(m.find()){
				String num = m.group(1);
				if(day.containsKey(num)){
					num = day.get(num);
				}
				value = "XXXX-WXX-"+num;
			}
		}
		
		if(value.equals("") || value.equals("FAIL")){
			value = "XXXX-XX-XX";
		}
		
		return value;
	}
	/*
	private static String[][] getEmptyTimex (String [][] lines, String dct, int cptTimex) throws ParseException{
		List<String[]> emptyTx = new ArrayList<String[]> ();
		
		for(int i=0; i<lines.length; i++){
	
			// if current token is B-TIMEX
			if (lines[i] != null && lines[i][colTimex] != null && lines[i][colTimex].startsWith("B-")
					&& !lines[i][colValTimex].equals("FAIL") && lines[i][colValTimex] != null){
				String timex = lines[i][colToken];
				
				//get the last index of the TIMEX token (in case of multi-tokens timex)
				for (int j=i+1; j<lines.length; j++){
					if(lines[j][colTimex] != null && lines[j][colTimex].startsWith("I-")){
						timex += " "+lines[j][colToken];
					}
					else{
						break;
					}
				}
				
				if(lines[i][colTimex].endsWith("TIME") || lines[i][colTimex].endsWith("DATE")){
					boolean dalle = false;
					boolean alle = false;
					boolean dash = false;
					boolean endFirstTimex = false;
					int secondTimex = -1;
					
					
					// token-1 == dal ("dalle 7 alle 10")
					if(i>1 && lines[i-1][colToken].toLowerCase().startsWith("da")){
						dalle = true;
					}
					for(int j=i+1; j<lines.length; j++){
						// if between two timexes there are "al" or "-" or "/" ("dalle 7 alle 10")
						if(lines[j][colTimex] != null && !lines[j][colTimex].equals("I-TIME") && !lines[j][colTimex].equals("I-DATE") 
								&& !endFirstTimex){
							endFirstTimex = true;
							if(lines[j][colToken].toLowerCase().startsWith("a")){
								alle = true;
							}
							if(lines[j][colToken].equals("-") || lines[j][colToken].equals("/")){
								dash = true;
							}
						}
						else if(endFirstTimex && lines[j][colTimex] != null 
								&& (lines[j][colTimex].endsWith("TIME") || lines[j][colTimex].endsWith("DATE"))
								&& !lines[j][colValTimex].equals("FAIL")){
							secondTimex = j;
							break;
						}
						else if(lines[j][colTimex] == null || endFirstTimex){
							break;
						}
						
					}
					// if there are two timex separated by "al" or "-"
					if((dalle && alle & secondTimex > -1 || dash && secondTimex > -1) 
							&& lines[i][colValTimex].matches("[0-9]+.*") && lines[secondTimex][colValTimex].matches("[0-9]+.*")){
						
						// build an empty timex of type DURATION
						String [] eTx = new String [nbCol];
						eTx[colTimex] = "B-DURATION";
						eTx[colTimexID] = Integer.toString(cptTimex++);
						
						
						SimpleDateFormat format = null;
						String formatString = getFormat(lines[i][colValTimex]);
						if(!formatString.equals("")){
							format = new SimpleDateFormat(formatString);
						}
						
						
						if(format != null){
							
							Date d1 = format.parse(lines[i][colValTimex]);
							Date d2 = format.parse(lines[secondTimex][colValTimex]);
							
							long diff = d2.getTime() - d1.getTime();
	
							//long seconds = TimeUnit.MILLISECONDS.toSeconds(diff);
							long minutes = TimeUnit.MILLISECONDS.toMinutes(diff);
							long hours = TimeUnit.MILLISECONDS.toHours(diff);
							long day = TimeUnit.MILLISECONDS.toDays(diff);
							String value = "";
							
							if(day > 0){
								value = "P";
								if(day > 30 && day < 365 && day%30 < 2){
									value += Long.toString(day/30)+"M";
								}
								else if(day > 30 && day < 365){
									value += Long.toString(day)+"D";
								}
								else if(day >= 365){
									value += Long.toString(day/365)+"Y";
								}
								else{
									value += Long.toString(day)+"D";
								}
							}
							else{
								value = "PT";
								if(hours > 0){
									value += Long.toString(hours)+"H";
									if(minutes > hours*60){
										value += Long.toString(minutes - hours*60)+"M";
									}
								}
								else if(minutes > 0){
									value += Long.toString(minutes)+"M";
								}
							}
							
							eTx[colValTimex] = value;
							eTx[0] = eTx[colValTimex];
							eTx[colBeginPoint] = lines[i][colTimexID];
							eTx[colEndPoint] = lines[secondTimex][colTimexID];
							emptyTx.add(eTx);
						}
					}
				}
				// "due settimane fa" or "da 3 mesi" or "dopo 2 mesi"
				else if((timex.contains(" fa") || timex.contains(" fà") || (i>1 && lines[i-1][colToken].toLowerCase().equals("da"))
						|| (i>1 && lines[i-1][colToken].toLowerCase().startsWith("ne") && !timex.contains("prim") && timex.contains(" "))
						|| (i>1 && lines[i-1][colToken].toLowerCase().startsWith("dopo"))
						|| timex.contains(" dopo")) 
						&& lines[i][colTimex].endsWith("DURATION")){
					
					String [] eTx = new String [nbCol];
					if(lines[i][colValTimex].startsWith("P")){
						if(!timex.contains(" fa") && !timex.contains(" fà")){
							timex = timex+" fa";
						}
						
						String value = TestTimeNorm_parseAll.getTimeNorm(timex.toLowerCase(), dct);
						String [] listVal = value.split(";");
						for(int l=0; l<listVal.length; l++){
							if(!listVal[l].startsWith("P")){
								value = listVal[l];
								break;
							}
						}
						
						// if timeNorm failed, some simple rules to get the value
						if(value.startsWith("P") || value.equals("FAIL")){
							if (timex.contains("scors")){
								value = "PAST_REF";
							}
							else if(timex.contains("prossim")){
								value = "FUTURE_REF";
							}
							else if(timex.contains("prece")){
								value = "PAST_REF";
							}
							else if(i>1 && lines[i-1][colToken].toLowerCase().equals("dopo")){
								value = "FUTURE_REF";
							}
							else if(timex.contains("dopo")){
								value = "FUTURE_REF";
							}
							else{
								value = "PAST_REF";
							}
						}
						else{
							if(timex.contains("ann") && value.matches("[0-9]+-[0-9]+-[0-9].*")){
								value = value.substring(0,4);
							}
						}
						eTx[0] = value;
						eTx[colValTimex] = value;
						eTx[colTimex] = "B-DATE";
						eTx[colTimexID] = Integer.toString((cptTimex++));
					}
					else if(lines[i][colValTimex].matches("[0-9]+.*")){
						eTx[0] = lines[i][colValTimex];
						eTx[colValTimex] = lines[i][colValTimex];
						eTx[colTimex] = "B-DATE";
						eTx[colTimexID] = Integer.toString((cptTimex++));
						timex = timex.replace(" fa","");
						timex = "per "+timex;
						
						String value = TestTimeNorm.getTimeNorm(timex.toLowerCase(), dct);
						for (int j=i; j<lines.length; j++){
							if(lines[j][colTimex].equals("I-DURATION")){
								lines[j][colValTimex] = value;
							}
							else{
								break;
							}
						}
					}
					eTx[colAnchor] = lines[i][colTimexID];
					emptyTx.add(eTx);
				}
			}
		}
		
		for(int e = 0; e<emptyTx.size(); e++){
			String eTx = "\n";
			for (int l=0; l<emptyTx.get(e).length; l++){
				eTx += emptyTx.get(e)[l]+"\t";
			}
			lines[lines.length-1][0] += eTx+"\n";
		}
		
		return lines;
	}
*/
	
	
	
	private static String getValueSpecialCases (String timex, String anchorTime){
		String value = "";
		
		String num = "";
		
		String year = anchorTime.substring(0,4);
		String month = anchorTime.substring(5,7);
		String day = anchorTime.substring(8,9);
		
		// stagionale
		if(timex.matches(".*stagion.*")){
			value = "XXXX-XX-XX";
		}
		
		//semestre
		else if(timex.contains("mestr")){
			String [] elt = timex.split(" ");
			for(int l=0; l<elt.length; l++){
				if(elt[l].matches("[0-9]+")){
					if(elt[l].length() == 4){
						value += elt[l]+"-";
					}
					else if(elt[l].length() == 2){
						value += "19"+elt[l]+"-";
					}
					break;
				}
			}
			if(value.equals("")){
				if(timex.contains("anno") && (timex.contains("precedente") || timex.contains("scors"))){
					year = Integer.toString(Integer.parseInt(year)-1);
				}
				else if(timex.contains("anno") && timex.contains("prossim")){
					year = Integer.toString(Integer.parseInt(year)+1);
				}
				value += year+"-";
			}
			if (timex.contains("primo")){
				num = "1";
			}
			else if(timex.contains("secondo")){
				num = "2";
			}
			else if(timex.contains("ultim")){
				num = "-1";
			}
			else{
				num = "1";
			}
			
			if (timex.contains("semestr")){
				value += "H";
			}
			else if(timex.contains("trimestr")){
				value += "Q";
			}
			else if(timex.contains("quadrimestr")){
				value += "QU";
			}
			else if(timex.contains("bimestr")){
				value += "B";
			}
			
			if(num.equals("-1")){
				if(value.endsWith("H")){ num = "2";}
				else if(value.endsWith("Q")){ num = "4";}
				else if(value.endsWith("Qu")){ num = "3"; }
				else if(value.endsWith("B")){ num = "6"; }
			}
			
			value += num;
		}
		
		else if(timex.contains("passato")){
			value = "PAST_REF";
		}
		
		else if(timex.contains("cento") || timex.matches("(\' )?[1-9]00")){
			String numCento = "";

			value = "1";
			
			if(timex.contains("cento")){
				numCento = timex.substring(0,timex.length()-5);

				if(numCento.equals("sei")){ value += "6";}
				else if(numCento.equals("sette")){ value += "7";}
				else if(numCento.equals("otto")){ value += "8";}
				else if(numCento.equals("nove")){ value += "9";}
				else if(numCento.equals("cinque")){ value += "5";}
				else if(numCento.equals("quattro")){ value += "4";}
				else if(numCento.equals("tre")){ value += "3";}
				else if(numCento.equals("due")){ value += "2";}
			}
			else{
				value += timex.substring(timex.length()-3,timex.length()-2);
			}
		}
	
		if(value.equals("")){
			value = "FAIL";
		}
		
		return value;
	}
	
	
	/**
	 * Check if a date is valid (for example if date=2012-02-31 modify in date=2012-03-03
	 * @param date
	 * @return
	 */
	public static String checkDate (String date){
		if(date.length() == 10){
			String year = date.substring(0,4);
			String month = date.substring(5,7);
			String day = date.substring(8,10);
			if(month.equals("02") && (day.equals("30") || day.equals("31") || day.equals("29"))){
				month = "03";
				day = "0"+Integer.toString(Integer.parseInt(day) - 28);
			}
			if((month.equals("04") || month.equals("06") || month.equals("09") || month.equals("11")) && day.equals("31")){
				day = "01";
				month = Integer.toString(Integer.parseInt(month)+1);
				if(month.length() == 1){
					month = "0"+month;
				}
				if(month.equals("13")){
					month = "01";
					year = Integer.toString(Integer.parseInt(year)+1);
				}
			}
			date = year+"-"+month+"-"+day;
		}
		return date;
	}
	
	/**
	 * Get the value of a timex from its id
	 * If no value has been yet computed, send the timex to TimeNorm
	 * @param id
	 * @param lines
	 * @param anchorTime
	 * @return
	 */
	public static String getValTimexID(String id, String [][] lines, String anchorTime){
		String val = "";
		int k = 0;
		while (val.equals("") && k<lines.length){
			if(lines[k]!= null && lines[k][colTimexID] != null && lines[k][colTimexID].equals(id)){
				if(lines[k][colValTimex] != null){
					val = lines[k][colValTimex];
				}
				else{
					String timex = lines[k][colToken];
					int j = k+1;
					while(lines[j][colTimex].startsWith("I-")){
						timex += " "+lines[j][colToken];
						j++;
					}
					val = TestTimeNorm.getTimeNorm (timex, anchorTime);
				}
			}
			k++;
		}
		
		if(val.length() == 4){
			val += "-01-01";
		}
		else if(val.length() == 7){
			val += "-01";
		}
		if(val.contains("-Q")){
			if(val.contains("Q1")){
				val = val.replace("Q1", "01");
			}
			else if(val.contains("Q2")){
				val = val.replace("Q2", "04");
			}
			else if(val.contains("Q3")){
				val = val.replace("Q3", "07");
			}
			else if(val.contains("Q4")){
				val = val.replace("Q4", "10");
			}
		}
		
		if(!val.matches("[0-9][0-9][0-9][0-9]-[0-9][0-9]-[0-9][0-9]")){
			val = anchorTime;
		}
		
		return val;
	}
	
	
	
	/**
	 * From a dateString, get its format
	 * @param date
	 * @return
	 */
	public static String getFormat (String date){
		String format = "";
		if(date.contains("T")){
			format = "yyyy-MM-dd'T'HH:mm";
		}
		else if(date.matches("[0-9]+-[0-9]+-[0-9]+")){
			format = "yyyy-MM-dd";
		}
		else if(date.matches("[0-9]+-[0-9]+")){
			format = "yyyy-MM";
		}
		else if(date.matches("[0-9]+")){
			format = "yyyy";
		}
		return format;
	}
	
	/**
	 * Add a timex id to all the timexes
	 * @param lines
	 * @return
	 */
	public static String[][] addTimexID(String [][] lines){
		int cpt = 0;
		for (int i=0; i<lines.length; i++){
			if(lines[i] != null && lines[i][colTimex] != null && lines[i][colTimex].matches("(B|I)-(DATE|SET|TIME|DURATION)")){
				if(lines[i][colTimex].startsWith("B-")){
					cpt++;
				}
				lines[i][colTimexID] = "tmx"+cpt;
			}
			else{
				lines[i][colTimexID] = "O";
			}
		}
		return lines;
	}

	/**
	 * Concatenation of two arrays at two dimensions
	 * @param s
	 * @param e
	 * @param nbCol
	 * @return
	 */
	public static String [][] concatArray (String [][] s, String [][] e, int nbCol){
		String [][] newArray = new String [s.length+e.length][nbCol];
		int j = 0;
		for (int k=0; k<s.length; k++){
			newArray[j] = s[k];
			j++;
		}
		for (int k=0; k<e.length; k++){
			newArray[j] = e[k];
			j++;
		}
		
		return newArray;
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
	
	public static String addNameCol (String nameCol){
		nameCol += "\ttag1Rules\ttag2Rules" + "\ttimex" + "\ttimexId" + "\ttimexValue";
	//+ "\tanchorTimeId" + "\tbeginPointId" + "\tendPointId";
		return nameCol;
	}
	
	/**
	 * Add a timex id to all the timexes
	 * @param lines
	 * @return
	 */
	public static LinkedList<String> addTimexID (Iterable<String> lines){
		LinkedList<String> columnValues = new LinkedList<String> ();
		
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
			
			if(ltmp.contains("\t")) {
			
			String [] elts = ltmp.split("\t");
			
			String tok = elts[0];
			//String pos = line.get(1);
			String timex = elts[3];
			
			if(timex.matches("(B|I)-(DATE|SET|TIME|DURATION)")){
				if(timex.startsWith("B-")){
					cpt++;
				}
				columnValues.add("tmx"+cpt);
			}
			else{
				columnValues.add("O");
			}
			}
			else {
				columnValues.add("");
			}
		}
		return columnValues;
	}

	
	public static void init() {
		 fill_hash_numbers();
		 fill_hash_days();
	}
	
	public static OBJECTDATA normalized_timex (OBJECTDATA filein, String dct) throws ParseException, InterruptedException {
		init();
	
		Hashtable<String,Integer> copyThisTokens = new Hashtable<String,Integer> ();
		copyThisTokens.put("token",1);
		copyThisTokens.put("pos",2);
		copyThisTokens.put("lemma",3);
		copyThisTokens.put("tmx", 4);
		copyThisTokens.put("Rule1", 5);
		copyThisTokens.put("Rule2", 6);
		
		filein.addColumn("tmxid",addTimexID(filein.getFileLineByLine(copyThisTokens,true)));
		
		copyThisTokens.put("tmxid", 7);
		
		List<LinkedList<String>> columnValues = addTimexValue(filein.getFileLineByLine(copyThisTokens,true), dct);
		filein.addColumn("tmxvalue", columnValues.get(0));
		filein.addColumn("tmxanchor", columnValues.get(1));
		filein.addColumn("beginpoint", columnValues.get(2));
		filein.addColumn("endpoint", columnValues.get(3));
		
		
		return filein;
	}
	
	
	
	/**
	 * @param args
	 * @throws ParseException 
	 * @throws FileNotFoundException 
	 */
	public static void main(String[] args) throws ParseException, FileNotFoundException {
		
	}

}
