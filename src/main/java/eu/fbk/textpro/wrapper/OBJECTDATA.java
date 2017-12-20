package eu.fbk.textpro.wrapper;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;
import java.util.TreeMap;
import java.util.stream.Stream;


public class OBJECTDATA {
	public boolean isRawDataFile = true;
	public LinkedHashMap<String, Integer> tokensIndex = new LinkedHashMap<String, Integer>();
	public LinkedHashMap<String, String> headerList = new LinkedHashMap<String, String>();
	public LinkedHashMap<Integer, String> lineList = new LinkedHashMap<Integer, String>();
	public int linesCount = 0;
	String headerStartPrefix = "# FILE:";
	String headerEndPrefix = "# FIELDS:";
	String NULL_VALUE = "__NULL__";
	public static File input_file;
	public void resetFileData() {

		tokensIndex.clear();
		headerList.clear();
		lineList.clear();
		linesCount = 0;
		isRawDataFile = true;

	}

	public void printStandardOutput (String encoding, Hashtable<String, Integer> tokens,boolean createHeader) {
	
		if(createHeader) {
			Iterator<String> ti = this.headerList.keySet().iterator();
			while (ti.hasNext()) {
				String titmp = ti.next();
				if (!titmp.contains("FIELDS")) { //FIELDS line is added later
					System.out.println(headerList.get(titmp));
				}
			}
		}
		
		for (String tok : getFileLineByLine(tokens,createHeader)) {
			System.out.println(tok);
		}
	
	}
	
	public void saveInFile(String path, String encoding, Hashtable<String, Integer> tokens,boolean createHeader) {
		try {
			File fileDir = new File(path);

			Writer out = new BufferedWriter(new OutputStreamWriter(
					new FileOutputStream(fileDir), encoding));
			
			if(createHeader) {
				Iterator<String> ti = this.headerList.keySet().iterator();
				while (ti.hasNext()) {
					String titmp = ti.next();
					if (!titmp.contains("FIELDS")) { //FIELDS line is added later
						out.append(headerList.get(titmp)+"\n");
					}
				}
			}
			
			for (String tok : getFileLineByLine(tokens,createHeader)) {
				out.append(tok).append("\n");
			}
			out.flush();
			out.close();

		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public OBJECTDATA getFileData(String encoding,
			final Hashtable<String, Integer> copyThisTokens, final boolean createHeader) {
		Iterable<String> reture = new Iterable<String>() {

			@Override
			public Iterator<String> iterator() {
				LinkedList<String> tmp = new LinkedList<String>();
				if (createHeader) {
					for (String tok : headerList.values()) {
						if (!tok.contains("# FIELDS:"))
							tmp.add(tok);
					}
				}
				if (copyThisTokens.size() > 0) {
					for (String tok : getFileLineByLine(copyThisTokens,createHeader)) {
						tmp.add(tok);
					}
				} else {
					for (String tok : lineList.values()) {
						tmp.add(tok);
					}
				}
				return tmp.iterator();
			}
		};
		OBJECTDATA fileDir = new OBJECTDATA();
		fileDir.readData(reture, encoding);
		return fileDir;
	}

	public void saveInFile(String path, String encoding,boolean createHeader) {
		try {
			File fileDir = new File(path);

			Writer out = new BufferedWriter(new OutputStreamWriter(
					new FileOutputStream(fileDir), encoding));
			if(createHeader){
			for (String tok : headerList.values()) {
				out.append(tok).append("\n");
			}
			}
			for (String lc : lineList.values()) {

				out.append(lc.trim()).append("\n");

			}
			out.flush();
			out.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	public Collection<String> getFileLineByLineAsList(final Hashtable<String, Integer> copyThisTokens,boolean header) {
		if (copyThisTokens == null) {
			throw new IllegalArgumentException(
					"Input value is null at method getFileLineByLine(LinkedList).");
		}

		  Map<String, Integer> asd = sortValue(copyThisTokens);
		 
		
		LinkedList<String> tmp = new LinkedList<String>();
		if(header){
			String head = "# FIELDS: ";
			for (String tok : asd.keySet()){
				head += tok + "\t";
			}
			tmp.add(head.trim());
		}
		for (String lc : lineList.values()) {
			String tmpLine = "";
			for (String tok : asd.keySet()){
				tmpLine += getColumnValue(lc, tok) + "\t";
			}
			tmpLine = tmpLine.trim();
			tmp.add(tmpLine);
		}
		return tmp;
	
	}
	public Iterable<String> getFileLineByLine(final Hashtable<String, Integer> copyThisTokens,boolean header) {
		if (copyThisTokens == null) {
			throw new IllegalArgumentException(
					"Input value is null at method getFileLineByLine(LinkedList).");
		}
		Iterable<String> ret = new Iterable<String>() {

			@Override
			public Iterator<String> iterator() {
				  Map<String, Integer> asd = sortValue(copyThisTokens);
				 
				
				LinkedList<String> tmp = new LinkedList<String>();
				if(header){
					
					String head = "# FIELDS: ";
					for (String tok : asd.keySet()){
						head += tok + "\t";
					}
					
					tmp.add(head.trim());
				}
				
			
				for (String lc : lineList.values()) {
					String tmpLine = "";
					for (String tok : asd.keySet()){
						tmpLine += getColumnValue(lc, tok) + "\t";
					}
					tmpLine = tmpLine.trim();
					tmp.add(tmpLine);
				}
				return tmp.iterator();
			}

			
		};
		return ret;
	}
	  public static ArrayList<Entry<?, Integer>> sortValue1(Hashtable<?, Integer> t){

	       //Transfer as List and sort it
	       ArrayList<Map.Entry<?, Integer>> l = new ArrayList(t.entrySet());
	       Collections.sort(l, new Comparator<Map.Entry<?, Integer>>(){

	         public int compare(Map.Entry<?, Integer> o1, Map.Entry<?, Integer> o2) {
	            return o1.getValue().compareTo(o2.getValue());
	        }});
	       return l;
	       //System.out.println(l);
	    }
	  public static <K, V extends Comparable<V>> Map<K, V> sortValue(final Map<K, V> map) {
		  Comparator<K> valueComparator =  new Comparator<K>() {
		      public int compare(K k1, K k2) {
		        //  int compare = map.get(k2).compareTo(map.get(k1));
		    	  int compare = map.get(k1).compareTo(map.get(k2));
		          if (compare == 0) return 1;
		          else return compare;
		      }
		  };
		  Map<K, V> sortedByValues = new TreeMap<K, V>(valueComparator);
		  sortedByValues.putAll(map);
		  return sortedByValues;
	  }
	public Iterable<String> getFileLineByLine() {
		Iterable<String> ret = new Iterable<String>() {

			@Override
			public Iterator<String> iterator() {
				LinkedList<String> tmp = new LinkedList<String>();
				tmp.addAll(headerList.values());
				tmp.addAll(lineList.values());
				return tmp.iterator();
			}
		};
		return ret;
	}
	public Stream<String> getFileLineByLineNoHeader() {
		return lineList.values().stream();
	}
	public Collection<String> getFileAsList() {
		return lineList.values();
	}
	public boolean addColumn(String columnName, LinkedList<String> columValues) {
		if (columnName == null || columnName.length() == 0
				|| columnName.trim().equals("")) {
			System.err.println("columnName is key, couldn't be empty or null.");
			return false;
			// System.exit(-1);
		}
		if (tokensIndex.containsKey(columnName)) {
			System.err.println("(" + columnName
					+ ")columnName is key, No duplications.");
			return false;
			// System.exit(-1);
		}
		if (tokensIndex.containsKey(columnName)) {
			System.err.println("columnName is key, couldn't be defined twice.");
			return false;
			// System.exit(-1);
		}
		if (linesCount > 0 && linesCount != columValues.size()) {
			System.err
					.println("error OBJECTDATA.addColumn - size isn't compatible: column values and lines count should be the same. In case of no value you could add the null value("
							+ NULL_VALUE + ").");
			return false;
			// System.exit(-1);
		}
		tokensIndex.put(columnName, tokensIndex.size());
		if (headerList.containsKey(headerEndPrefix)) {
			headerList.put(headerEndPrefix, (headerList.get(headerEndPrefix)
					+ "\t" + columnName));
		} else {
			headerList.put(headerEndPrefix,
					(headerEndPrefix + " " + columnName));
		}
		if (lineList.size() > 0) {
			int i = 0;
			for (String itmps : columValues) {
				lineList.put(i, lineList.get(i) + "\t" + itmps);
				i++;
			}

		} else {
			int i = 0;
			for (String n : columValues) {
				lineList.put(i, n);
				i++;
			}
			linesCount = lineList.size();
		}
		// printTokenIndexs();
		// System.out.println("lineList: "+lineList.size()+" - columValues:"+columValues.size());
		isRawDataFile = false;
		return true;
	}

	public boolean deleteColumn(String columnName) {
		if (tokensIndex.containsKey(columnName)) {
			if (columnName.equals("token")) {
				System.err
						.println("token is a protected column from deletion.");
				return false;
			} else {
				int pos = tokensIndex.get(columnName);
				for (Entry<Integer, String> ent : lineList.entrySet()) {
					String l = ent.getValue();
					if (l.length() > 0) {
						String[] toks = l.split("\t");
						String tmp = "";
						for (int p = 0; p < toks.length; p++) {
							if (p != pos) {
								tmp += toks[p] + "\t";
							}
						}
						tmp = tmp.trim();
						ent.setValue(tmp);
					}
				}
				String head = headerList.get(headerEndPrefix);
				head = head.replaceFirst(columnName, "");
				head = head.replaceFirst("\t\t", "");
				setTokensIndex(head);
				return true;
			}
		} else {
			System.err.println(columnName + " column name not found!");
			return false;
		}
	}

	public boolean deleteHeader(String headerKey) { // headerKey "# HEADERNAME:"
		if (!headerList.containsKey(headerKey)) {
			System.err.println("Deletion Error: (" + headerKey
					+ ") header key not found.");
			return false;
		}
		if (headerKey.equals(headerEndPrefix)
				|| headerKey.equals(headerStartPrefix)) {
			System.err.println(headerKey
					+ " is a protected header from deletion.");
			return false;
		} else {
			headerList.remove(headerKey);
			return true;
		}

	}

	public void readData(Iterable<String> lines, String encoding) {

		@SuppressWarnings("unused")
		boolean headerStartFound = false, headerEndFound = false;
		int lineN = 0;

		for (String line : lines) {
			if (line.startsWith(headerStartPrefix)) {
				updateHeaderList(line, false);
				headerStartFound = true;
			} else if (line.startsWith(headerEndPrefix)) {
				if (line.length() > headerEndPrefix.length() + 1) {
					isRawDataFile = false;
					setTokensIndex(line);
				}
				updateHeaderList(line, false);
				headerEndFound = true;
			} else if (line.startsWith("# ") && line.length() > 5) {
				updateHeaderList(line, false);
			} else {

				lineList.put(lineN, line);
				lineN++;
				// break;
			}
		}
		linesCount = lineN;
	}

	public void readData(String content) {
		String[] lns = content.split("\n");
		int lineN=0;
		for(String ln:lns){
			lineList.put(lineN, ln);
			lineN++;
		}
		linesCount = lineN;
	}
	public void readData(File file, String encoding,boolean haveHeader,LinkedList<String> headerFromModulesXML) {
		try {
			String line;
			input_file = file;
			Reader reader = new InputStreamReader(new FileInputStream(
					file.getPath()), encoding);
			BufferedReader br = new BufferedReader(reader);
			@SuppressWarnings("unused")
			boolean headerStartFound = false, headerEndFound = false;
			int lineN = 0;
			while ((line = br.readLine()) != null) {
				if (line.startsWith(headerStartPrefix)) {
					updateHeaderList(line, false);
					headerStartFound = true;
				} else if (line.startsWith(headerEndPrefix)) {
					if (line.length() > headerEndPrefix.length() + 1) {
						isRawDataFile = false;
						setTokensIndex(line);
					}
					updateHeaderList(line, false);
					headerEndFound = true;
				} else if (line.startsWith("# ") && line.length() > 5) {
					updateHeaderList(line, false);
				} else {

					lineList.put(lineN, line);
					lineN++;
				}
				linesCount = lineN;
			}
			
			if(!headerEndFound&!haveHeader&&headerFromModulesXML.size()>0){
				line = "# FIELDS: ";
				for(String ll : headerFromModulesXML){
					line+=ll+"\t";
				}
				line = line.trim();
				if (line.length() > headerEndPrefix.length() + 1) {
					isRawDataFile = false;
					setTokensIndex(line);
				}
				updateHeaderList(line, false);
				headerEndFound = true;
			}
			if((!headerEndFound&haveHeader)&&headerFromModulesXML.size()==0){
				System.err.println("FATAL ERROR: A module output file doesn't have any header file("+file.getPath()+")");
			}
			reader.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void readData(File file, String encoding) {
		try {
			String line;
			input_file = file;
			Reader reader = new InputStreamReader(new FileInputStream(
					file.getPath()), encoding);
			BufferedReader br = new BufferedReader(reader);
			@SuppressWarnings("unused")
			boolean headerStartFound = false, headerEndFound = false;
			int lineN = 0;
			while ((line = br.readLine()) != null) {
				if (line.startsWith(headerStartPrefix)) {
					updateHeaderList(line, false);
					headerStartFound = true;
				} else if (line.startsWith(headerEndPrefix)) {
					if (line.length() > headerEndPrefix.length() + 1) {
						isRawDataFile = false;
						setTokensIndex(line);
					}
					updateHeaderList(line, false);
					headerEndFound = true;
				} else if (line.startsWith("# ") && line.length() > 5) {
					updateHeaderList(line, false);
				} else {

					lineList.put(lineN, line);
					lineN++;
				}
				linesCount = lineN;
			}
			reader.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public String getColumnValue(String line, String fieldname) {
		Integer index = -1;
		if (tokensIndex.containsKey(fieldname))
			index = tokensIndex.get(fieldname);
		else {
			System.err.println("Column name(" + fieldname + ") not found!");
			//System.exit(-1);
		}
		if (line.trim().length() == 0) {
			return "";
		}
		String[] lineToks = line.trim().split("\t");
		if (lineToks.length > index && index > -1) {
			return lineToks[index];
		} else {
			System.err.println(line);
			System.err
					.println("The input line values is not aligned with the number of columns which we have!.\nWe couldn't provide a value of "
							+ fieldname + " column");
			//System.exit(0);
			return null;
		}
	}

	public String getHeaderValue(String header) {
		if (headerList.containsKey(header)) {
			return headerList.get(header);
		} else {
			System.err.println("We couldn't find a value to the " + header
					+ " header");
		}
		return "";
	}

	private void setTokensIndex(String line) {
		tokensIndex.clear();
		updateHeaderList(line, true);
		if (line.startsWith(headerEndPrefix)
				&& line.length() > headerEndPrefix.length()) {
			line = line.replaceFirst(headerEndPrefix + "\\s*", "");
			String[] cols = line.split("\t");
			for (int i = 0; i < cols.length; i++) {
				// System.err.println(cols[i]+","+ i);
				if (!tokensIndex.containsKey(cols[i]))
					tokensIndex.put(cols[i], i);
			}
		}
	}

	public void printTokenIndexs() {
		Iterator<String> ti = tokensIndex.keySet().iterator();
		while (ti.hasNext()) {
			String titmp = ti.next();
			System.out.println(titmp + "=>" + tokensIndex.get(titmp));
		}
	}

	public void printHeaderList() {
		Iterator<String> ti = headerList.keySet().iterator();
		while (ti.hasNext()) {
			String titmp = ti.next();
			System.out.println(titmp + "=>" + headerList.get(titmp));
		}
	}

	public boolean updateHeaderList(String headerLine,
			boolean callfromSetTokensFunc) {
		// we are sure here that the headerLine is a from the header we don't
		// need to check that again.
		String key = "";
		String minimumHeader = "# : ";
		if (headerLine.length() > minimumHeader.length() + 2) {
			key = headerLine.substring(0, headerLine.indexOf(":") + 1);
			if (!callfromSetTokensFunc
					&& key.equals(headerEndPrefix)
					&& headerLine.replace(headerEndPrefix, "").trim().length() > 0) {
				/*
				 * System.err .println("you can't update " + key +
				 * " header, please check \"setTokenIndex(String line)\" function for more information.\nThis function should influance changes on the file columns."
				 * );
				 */
				setTokensIndex(headerLine);
				return true;
			} else {
				headerList.put(key, headerLine);
				return true;
			}
		} else {
			System.err.println("We couldn't update the header of this line:\n"
					+ headerLine);
			return false;
		}
	}

	/*
	 * public PipedOutputStream getFileAsPipeStreaming() {
	 * 
	 * PipedOutputStream output = new PipedOutputStream(); PipedInputStream snk
	 * = new PipedInputStream();
	 * 
	 * try { output.connect(snk); for(String tmp:headerList.values()){
	 * output.write(tmp.getBytes()); output.flush();
	 * System.err.println("s:"+tmp); } for(String tmp:lineList.values()){
	 * output.write(tmp.getBytes()); output.flush();
	 * System.err.println("s:"+tmp); } output.close(); } catch (IOException e) {
	 * e.printStackTrace(); } return output; }
	 * 
	 * public void readDataFromPipeStream(PipedOutputStream input2, String
	 * encoding) { try { String line; PipedInputStream input =new
	 * PipedInputStream(); input.connect(input2); boolean headerStartFound =
	 * false, headerEndFound = false; int lineN = 0; int data = input.read();
	 * while (data != -1) { line = String.valueOf(data);
	 * System.err.println("R:"+line); if (line.startsWith(headerStartPrefix)) {
	 * updateHeaderList(line); headerStartFound = true; } else if
	 * (line.startsWith(headerEndPrefix)) { if (line.length() >
	 * headerEndPrefix.length() + 1) { isRawDataFile = false;
	 * setTokensIndex(line); } updateHeaderList(line); headerEndFound = true; }
	 * else if (line.startsWith("# ") && line.length() > 5) {
	 * updateHeaderList(line); } else { lineList.put(lineN, line); lineN++; }
	 * linesCount = lineN; data = input.read(); } input.close(); } catch
	 * (IOException e) { e.printStackTrace(); } }
	 */

	//  getFileLineByLineAsPIPEStreaming two functions: general, for
	// specific tokens
	//  readData from PIPEStreamIn
}

class ObjectDataUtil {
	/*
	 * if the is mis-matching on the line counter between the two files,
	 * exception will be thrown and exit. Any common column between the primary
	 * and the seconday files, the primary column will be the choosed one. No
	 * merge between raw files. The column headers field name are considered as
	 * keys, so if the two files have the same header the original file data
	 * will be returned as result of merging, this is also true for the header.
	 */
	public OBJECTDATA merge(OBJECTDATA primary, OBJECTDATA secondary) {
		//System.err.println("primary raw:"+primary.isRawDataFile+" - secondary raw:"+secondary.isRawDataFile);
		if (secondary.isRawDataFile&&checkIdentical(primary.headerList.keySet(),
				secondary.headerList.keySet())) {
			// need to build a system to merge two files raw and structured
			throw new IllegalArgumentException("Can't merge raw data file.");
		}

		if (!primary.isRawDataFile && !secondary.isRawDataFile
				&& 
				((primary.linesCount - secondary.linesCount) >1||-1>(primary.linesCount - secondary.linesCount) )
				//TODO important when fixed the bug of morphopro to not give more one line in output substitute the above line with 
				
				
				) {
			try {
				throw new Exception("Can't merge two mismatched lines files.");
			} catch (Exception e) {
				e.printStackTrace();
			}
			System.exit(-1);
		}

		OBJECTDATA merged = new OBJECTDATA();
		merged.isRawDataFile = false;
		if (checkIdentical(primary.headerList.keySet(),
				secondary.headerList.keySet())) {
			// the two headers are identical so take the original one
			// System.err.println("the two headers are identical so take the original one");
			merged.headerList.putAll(primary.headerList);
		} else {
			// merge the header, primary + the difference.
			// System.err.println("merge the header");
			merged.headerList.putAll(primary.headerList);
			for (Entry<String, String> head : secondary.headerList.entrySet()) {
				if (!merged.headerList.containsKey(head.getKey())) {
					merged.headerList.put(head.getKey(), head.getValue());
				}
			}
		}
		if (primary.isRawDataFile && !secondary.isRawDataFile) {
			merged.tokensIndex.putAll(secondary.tokensIndex);
			merged.isRawDataFile = secondary.isRawDataFile;
			merged.lineList.putAll(secondary.lineList);
			merged.linesCount = secondary.linesCount;
		}else if(!primary.isRawDataFile && secondary.isRawDataFile&&!checkIdentical(primary.headerList.keySet(),
				secondary.headerList.keySet())){ 
			// here means that the secondary file has a new header
			// so merge the header
			merged.tokensIndex.putAll(primary.tokensIndex);
			merged.lineList.putAll(primary.lineList);
			merged.linesCount = primary.linesCount;
		}else if(!primary.isRawDataFile && !secondary.isRawDataFile) { 
			if (checkIdentical(primary.tokensIndex.keySet(),
					secondary.tokensIndex.keySet())) {
				// the two tokens are identical so take the original one
				// System.err.println("the two tokens are identical so take the original one");
				merged.tokensIndex.putAll(primary.tokensIndex);
				merged.isRawDataFile = secondary.isRawDataFile;
				merged.lineList.putAll(primary.lineList);
				merged.linesCount = primary.linesCount;
			} else {
				
				merged.tokensIndex.putAll(primary.tokensIndex);
				
				LinkedHashMap<String, Integer> toBeCopied = new LinkedHashMap<String, Integer>();
				for (Entry<String, Integer> head : secondary.tokensIndex
						.entrySet()) {
					if (!merged.tokensIndex.containsKey(head.getKey())) {
						toBeCopied.put(head.getKey(), head.getValue());
						merged.tokensIndex.put(head.getKey(),
								merged.tokensIndex.size());
					
					}
				}
				String tmpex="# FIELDS: ";
				for(String hh: merged.tokensIndex.keySet()){
					tmpex+=hh+"\t";
				}
				tmpex=tmpex.trim();
				merged.headerList.put("# FIELDS:", tmpex);
					
					
				for (Entry<Integer, String> ent : primary.lineList.entrySet()) {
					String line = ent.getValue();
					String sL = secondary.lineList.get(ent.getKey());
					String[] secL = sL.split("\t");
					String tmp = "";
					if (sL.length() > 0 && secL.length >= toBeCopied.size()) {
						for (Entry<String, Integer> ii : toBeCopied.entrySet()) {
//							System.out.println(ii.getKey()+"="+ii.getValue()+"=>"+secL.length);
//							System.out.println(secL);
							tmp += secL[ii.getValue()] + "\t";
						}
						tmp = tmp.trim();
					} else {
						// nothing
					}
					line += "\t"+ tmp;
					merged.lineList.put(merged.linesCount, line);
					merged.linesCount++;
				}

			}
		}
		/*System.out.println("Primary:");
		primary.printTokenIndexs();
		System.out.println("Second:");
		secondary.printTokenIndexs();
		System.out.println("Merged:");
		merged.printTokenIndexs();*/
		// System.err.println("Fine!");
		return merged;
	}

	private boolean checkIdentical(Set<String> primary, Set<String> secondary) {
		if (primary.size() != secondary.size()) {
			return false;
		} else {
			for (String txt : primary) {
				if (!secondary.contains(txt)) {
					return false;
				}
			}
			return true;
		}
	}
}
