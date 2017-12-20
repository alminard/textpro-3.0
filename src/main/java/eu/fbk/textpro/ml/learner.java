package eu.fbk.textpro.ml;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.sql.Timestamp;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;
import java.util.stream.Stream;


public class learner {
	
	parameters pr;
	public static void main(String[] args) throws IOException {
		//String feature = "F:-1..1:0..2 FB:0..0:1..2";
		String feature ="F:-1..-1:6,7 F:0..0:1,2,4,7 F:1..1:6,7";
		String fn = "pos-annelyse/data/Elsnet-Training-tok-feat-ref.txp";
		//String fn = "pos-annelyse/data/Elsnet-Test-tok-feat-ref.txp";

		learner lr= new learner();
			
		lr.train(feature, fn);
		//lr.test("pos-annelyse/Elsnet-Training-tok-feat-ref.txp.key", fn);
		// System.out.println(indexm);
	}
	
	public Collection<String> test (String index_file, Collection<String> fileinput) throws IOException {
		
		if(pr == null){
			getParameterObjSerialized(new File(index_file));
		}
		
		//process file sentence by sentence
		prepare_features pf = new prepare_features(pr.feature);
		
		LinkedList<Sentence> para = new LinkedList<Sentence>();

		Collection<String> ret = new ArrayList<String>();
		
		for(String str: fileinput) {
			str = str.trim();
			if (str.trim().length() > 0) {
				Sentence e = new Sentence();
				String[] toks = str.split("\t| ");
				//System.out.println("=="+str+"==size="+toks.length);

				if(toks.length>1){
					for (int i = 0; i < toks.length ; i++) {
						e.features.put(i,toks[i].trim());
					}
				}else{
					e.features.put(0,str.trim());
				}
				if (pr.column_size == Integer.MIN_VALUE) {
					pr.column_size = toks.length;
				}
				e.token = toks[0];
				para.add(e);
			} else {
				pf.processTest(para,pr);
				for (Sentence pa : para) {
					ret.add(pa.features_transformed.size()>0?(pa.tag_transformed+" "+getString(pa.features_transformed).toString()):"-3:1");
				}
				
				ret.add("");
				para = new LinkedList<Sentence>();
			}
		}
		if (para.size() > 0) {
			pf.processTest(para,pr);
			for (Sentence pa : para) {
				ret.add(pa.features_transformed.size()>0?(pa.tag_transformed+" "+getString(pa.features_transformed)).toString():"-3:1");
			}
			para = new LinkedList<Sentence>();
		}
		return ret;
		
	}
	
	
	//method to be used when evaluating a model in train classes. It does not consider the last column. 
	public Collection<String> test_evaluation (String index_file, Collection<String> fileinput) throws IOException {
		
		if(pr==null){
			//getParameterObj(new File(index_file));
			getParameterObjSerialized(new File(index_file));
		}
		//process file sentence by sentence
		prepare_features pf = new prepare_features(pr.feature);
		
		LinkedList<Sentence> para = new LinkedList<Sentence>();

		Collection<String> ret=new ArrayList<String>();
		
		for(String str: fileinput) {
			str = str.trim();
			if (str.trim().length() > 0) {
				Sentence e = new Sentence();
				String[] toks = str.split("\t| ");
				//System.out.println("=="+str+"==size="+toks.length);

				if(toks.length>1){
					for (int i = 0; i < toks.length - 1; i++) {
						e.features.put(i,toks[i].trim());
					}
				}else{
					e.features.put(0,str.trim());
				}
				if (pr.column_size == Integer.MIN_VALUE) {
					pr.column_size = toks.length - 1;
				}
				e.tag = toks[toks.length - 1];
				e.token = toks[0];
				para.add(e);
			} else {
				
				
				//System.out.println("para.size()="+para.size());
				pf.processTest(para,pr);
				for (Sentence pa : para) {
					//System.err.println(pa.token+"=>"+pa.features_transformed.size()+"=>"+pa.features_transformed);
					ret.add(pa.features_transformed.size()>0?(pa.tag_transformed+" "+getString(pa.features_transformed).toString()):"-3:1");
				}
				//writeToFile(para, fn, out);
				
				ret.add("");
				para = new LinkedList<Sentence>();
				//System.gc();
			}
		//	System.out.println("learner.test.LINE: "+str);
		}
		if (para.size() > 0) {
			pf.processTest(para,pr);
			for (Sentence pa : para) {
				ret.add(pa.features_transformed.size()>0?(pa.tag_transformed+" "+getString(pa.features_transformed)).toString():"-3:1");
			}
		//	ret.add("");
			//writeToFile(para, fn, out);
			para = new LinkedList<Sentence>();
			//System.gc();
		}
		//System.out.println("Learner.test.returnValue.size: "+ret.size());
		return ret;	
	}
	
	
	/*public void test(String index_file, String fn) throws IOException {
		if(pr==null){
			//getParameterObj(new File(index_file));
			getParameterObjSerialized(new File(index_file));
		}
		
		
		//process file sentence by sentence
		prepare_features pf = new prepare_features(pr.feature);

		LinkedList<Sentence> para = new LinkedList<Sentence>();
		BufferedReader in = new BufferedReader(new InputStreamReader(new FileInputStream(fn), "UTF8"));
		File fileDir = new File(fn + ".out");


		Writer out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(fileDir), "UTF8"));

		String str="";
		while ((str = in.readLine()) != null) {
			str = str.trim();
			if (str.trim().length() > 0) {
					Sentence e = new Sentence();
					String[] toks = str.split("\t| ");
					if(toks.length>1){
				for (int i = 0; i < toks.length - 1; i++) {
					e.features.put(i,toks[i].trim());
				}
					}else{
						e.features.put(0,str.trim());
					}
				if (pr.column_size == Integer.MIN_VALUE) {
					pr.column_size = toks.length - 1;
				}
				e.tag = toks[toks.length - 1];
				e.token = toks[0];
				para.add(e);
			} else {
				pf.processTest(para,pr);
				writeToFile(para, fn, out);
				out.append("\n");
				para = new LinkedList<Sentence>();
				//System.gc();
			}
		}
		in.close();
		if (para.size() > 0) {
			pf.processTest(para,pr);
			writeToFile(para, fn, out);
			para = new LinkedList<Sentence>();
			//System.gc();
		}
		out.flush();
		out.close();
				
	}*/

	public void train(String feat, String fn) throws IOException {
		
		pr = new parameters();
		pr.feature=feat;
		prepare_features pf = new prepare_features(pr.feature);

		LinkedList<Sentence> para = new LinkedList<Sentence>();
		BufferedReader in = new BufferedReader(new InputStreamReader(new FileInputStream(fn), "UTF8"));
		File fileDir = new File(fn + ".out");
		File fileDirKey = new File(fn + ".key");


		Writer out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(fileDir), "UTF8"));
		Writer out2 = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(new File(fn+ ".key.tmp")), "UTF8"));
		
		int iq=0;
		String str;
		
		while ((str = in.readLine()) != null) {
		//	iq++;
			str = str.trim();
			if (str.length() > 0) {
				Sentence e = new Sentence();
				String[] toks = str.split("\t| ");
				
				//System.out.println(iq+"="+toks.length);
				for (int i = 0; i < toks.length - 1; i++) {
					e.features.put(i,toks[i].trim());
				}
				if (pr.column_size == Integer.MIN_VALUE) {
					pr.column_size = toks.length - 1;
				}
				e.tag = toks[toks.length - 1];
				e.token = toks[0];
				para.add(e);
			} else {
				iq++;
				//Timestamp timestamp = new Timestamp(System.currentTimeMillis());
		        //System.out.println(timestamp);
		        
			//	System.out.println("Process "+iq);
				pf.process(para,pr);
			//	System.out.println("Finish Process "+iq);
				writeToFile(para, fn, out);
			//	System.out.println("Finish write "+iq);
				out.append("\n");
				out.flush();
				para = new LinkedList<Sentence>();
				//System.gc();
				//Timestamp timestamp2 = new Timestamp(System.currentTimeMillis());
		        //System.out.println("2: "+timestamp2);
			}
			//out.flush();
		}
		in.close();
		if (para.size() > 0) {
			pf.process(para,pr);
			writeToFile(para, fn, out);
			para = new LinkedList<Sentence>();
			//out.flush();
			//System.gc();
		}
		//out.flush();
		out.close();
		
		
		writeToFileKeys(pr,out2);
		out2.close();
		
		//setParameterObj( fileDirKey,  feature);
		setParameterObjSerialized( fileDirKey);
	}
	
	private static void writeToFileKeys (parameters pr2, Writer out2) throws IOException {

		for (String k : pr2.indexm.keySet()) {
			out2.append(k + "\t" + pr2.indexm.get(k)).append("\n").flush();
		}
		
		out2.append(pr2.feature);
		
		out2.flush();
	}
	
	private void setParameterObjSerialized(File fileDirKey) throws IOException {
		ObjectOutputStream oos = null;
		FileOutputStream fout = null;
		try{
		     fout = new FileOutputStream(fileDirKey, false);
		     oos = new ObjectOutputStream(fout);
		   //  oos.defaultWriteObject();

		    oos.writeObject(pr);
		    oos.flush();
		} catch (Exception ex) {
		    ex.printStackTrace();
		} finally {
			if (fout != null) {
				try {
					fout.flush();
					fout.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		    if(oos  != null){
		    	oos.flush();
		        oos.close();
		    } 
		}
		
	}
	private void getParameterObj(File file) throws NumberFormatException, IOException {
		pr= new parameters();
		//read index_file
		BufferedReader in = new BufferedReader(new InputStreamReader(new FileInputStream(file), "UTF8"));
		String str;
		while ((str = in.readLine()) != null) {
			str = str.trim();
			if (str.length() > 0) {
				if(str.startsWith("Feature_Parameter:")){
					str=str.replace("Feature_Parameter:", "").trim();
					if(!(str.length()>0)){
						System.err.println("Problem: no Features in index_file");
						System.exit(0);
					}
					pr.feature=str;
				}else if(str.startsWith("Class_List:")){
					str=str.replace("Class_List:", "").trim();
					if(!(str.length()>0)){
						System.err.println("Problem: no Classes in index_file");
						System.exit(0);
					}
					
					String[] ind_tag = str.split(" ");
					for(int i=0;i<ind_tag.length;i+=2){
						pr.tags.put( ind_tag[i+1].replace(">", "").trim(),Integer.parseInt(ind_tag[i].replace("<", "").trim()));
					}
				}else if(str.startsWith("Column_Size:")){
					str=str.replace("Column_Size:", "").trim();
					if(!(str.length()>0)){
						System.err.println("Problem: no Column_Size in index_file");
						System.exit(0);
					}
					pr.column_size=Integer.parseInt(str);
				}else{
					String[] ind_fe = str.split(" ");
					if(ind_fe.length==2)
						pr.indexm.put(ind_fe[1].trim(),Integer.parseInt(ind_fe[0].trim()));
				}
			}
		}
		in.close();
		
	}

	void getParameterObjSerialized(File fileDirKey) throws IOException{
		ObjectInputStream objectinputstream = null;
		try {
			System.out.println("getPar: "+fileDirKey);
			FileInputStream streamIn = new FileInputStream(fileDirKey);
		     objectinputstream = new ObjectInputStream(streamIn);
		    // objectinputstream.defaultReadObject();

		    this.pr = (parameters) objectinputstream.readObject();
		} catch (Exception e) {
		    e.printStackTrace();
		} finally {
		    if(objectinputstream != null){
		        objectinputstream .close();
		    } 
		}
	}
	void setParameterObj(File fileDirKey, String feature) throws IOException{
		Writer out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(fileDirKey), "UTF8"));
		 out.append("Feature_Parameter: "+pr.feature+"\n");
		 out.append("Column_Size: "+pr.column_size+"\n");
		 out.append("Class_List: ");
		 
		 for (Entry<String, Integer> pa : pr.tags.entrySet()) {
				out.append("<"+pa.getValue() + " " + pa.getKey()+">").append(" ").flush();
			}
		 out.append("\n");
		 
		for (Entry<String, Integer> pa : pr.indexm.entrySet()) {
			out.append(pa.getValue() + " " + pa.getKey()).append("\n").flush();
		}
		out.flush();out.close();
	}
	private static void writeToFile(LinkedList<Sentence> para, String fn, Writer out) throws IOException {

		for (Sentence pa : para) {
			out.append(pa.features_transformed.size()>0?(pa.tag_transformed+" "+getString(pa.features_transformed).toString()):"-1:1").append("\n").flush();
		}
		out.flush();
	}

	private static CharSequence getString(HashMap<String, String> features_transformed) {
		String ret = "";
		for (Entry<String, String> ft : features_transformed.entrySet()) {
			ret += ft.getKey() + " ";
		}
		return ret.trim();
	}
	public parameters getParameters() {
		return pr;
	}
	public String getTag(String index_file, int index) {
		if(pr==null){
			//getParameterObj(new File(index_file));
			try {
				getParameterObjSerialized(new File(index_file));
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		}
//		System.out.println(pr.feature);
//		System.out.println(pr.tags.size());
//		System.out.println(pr.tags);
//		System.out.println(index);
		//System.out.println("learner.getTag.index: "+index);

		Map.Entry<String,Integer> entry =
			    new AbstractMap.SimpleEntry<String, Integer>("__NULL__", -1);
		return pr.tags.entrySet().stream()
				.filter(e->e.getValue()==index)
				.findFirst()
				.orElse(entry)
				.getKey();
	}
	public void init(File fileDirKey) {
		try {
			getParameterObjSerialized(fileDirKey);
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}

}
