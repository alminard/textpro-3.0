package eu.fbk.textpro.ml;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map.Entry;


public class prepare_features {
	private LinkedList<feature_node> features_list = new LinkedList<feature_node>();
	private LinkedList<Integer> tokenposBi = new LinkedList<Integer>();
	private LinkedList<Integer> colposBi = new LinkedList<Integer>();
	private LinkedList<feature_node> T_features_list = new LinkedList<feature_node>();

	public prepare_features(String features) {
		// F:[beginning pos. of token]..[end pos. of token]:[beginning pos. of
		// column]..[end pos. of column]
		String[] featuress = features.split(" ");
		for (String feature : featuress) {
			if (feature.startsWith("F:")) {
				feature = feature.replaceFirst("F:", "").trim();
				String[] sp = feature.split(":");
				feature_node f_node = new feature_node();
				fillList(f_node.tokenpos, sp[0], f_node);
				fillList(f_node.colpos, sp[1], f_node);
				features_list.add(f_node);
				
			} else if (feature.startsWith("FB:")) {
				feature = feature.replaceFirst("FB:", "").trim();
				String[] sp = feature.split(":");
				fillList(tokenposBi, sp[0], null);
				fillList(colposBi, sp[1], null);
			}else if (feature.startsWith("T:")) {
				feature = feature.replaceFirst("T:", "").trim();
				feature_node f_node = new feature_node();
				fillList(f_node.tokenpos, feature, f_node);
				T_features_list.add(f_node);
			}
		}
	}

	private void fillList(LinkedList<Integer> list, String sp, feature_node f_node) {
		if (sp.contains("..")) {
			String[] indxs = sp.trim().split("\\.\\.");
			if (indxs.length == 2) {
				for (int i = Integer.parseInt(indxs[0]); i <= Integer.parseInt(indxs[1]); i++) {
					list.add(i);
				}
			} else if (indxs.length == 1) {
				list.add(Integer.parseInt(indxs[0]));
				if (f_node != null)
					f_node.last_feature_not_known = true;
			}
		}else if(sp.contains(",")){
			String[] indxs = sp.trim().split(",");
			for(String ss:indxs){
				list.add(Integer.parseInt(ss));
			}
		}else{
			try{
				list.add(Integer.parseInt(sp));
			}catch(Exception e){
				System.err.println("Problem with feature extraction: "+sp+"\n"+e.getMessage());
				System.exit(0);
			}
		}
	}

	public void process(LinkedList<Sentence> lns, parameters pr) {
		boolean done = false;
		for (int i = 0; i < lns.size(); i++) {
			// System.out.println("Current Sentence: "+i +" word:
			// "+lns.get(i).token);
			Sentence lnt = lns.get(i);
			int keyt = -1;
			if (!pr.tags.containsKey(lnt.tag)) {
				keyt = pr.tags.size() + 1;
				pr.tags.put(lnt.tag, keyt);
			} else {
				keyt = pr.tags.get(lnt.tag);
			}
			//lnt.features_transformed.addFirst(String.valueOf(keyt));
			//lnt.features_transformed.put(String.valueOf(keyt),"");
			lnt.tag_transformed = String.valueOf(keyt);
			// adjust the featuren LAST_FEATURE_INDEX on colpos
			if (!done) {
				for (feature_node fe : features_list) {
					if (fe.last_feature_not_known) {
						for (int ilo = fe.colpos.getFirst(); ilo <= lnt.features.size() - 1; ilo++) {
							fe.colpos.addLast(ilo);
						}
						fe.last_feature_not_known=false;
					}
				}
				

				// loop on tags and T_features_list to insert the indexs:
					for (feature_node fe : T_features_list) {
						if (fe.last_feature_not_known) {
							for (int ilo = fe.tokenpos.getFirst(); ilo <= -1; ilo++) {
								fe.tokenpos.addLast(ilo);
							}
							fe.last_feature_not_known=false;
						}
					}
					pr.tags_predicted_features_list = T_features_list;
				done = true;
			}
			
			
			for (feature_node fe : T_features_list) {
				for(Integer tp:fe.tokenpos){
					int index = i+(tp.intValue()) ;
					
					//System.out.println("lns: "+lns.size()+" i: "+i+" tp: "+tp+" index: "+index);

					String fek="-1";
					if(index<0){
						 fek = "FT:" + tp +":"+tp+ "__BOS__";
					}else{
						 Sentence ln = lns.get(index);
						 fek = "FT:" + tp +":"+tp+ "_"+pr.tags.get(ln.tag);
					}
					int key = -1;
					if (!pr.indexm.containsKey(fek)) {
						key = pr.indexm.size() + 1;
						pr.indexm.put(fek, key);
						pr.tags_predicted.put(fek, key);
					} else {
						key = pr.indexm.get(fek);
					}
					lnt.features_transformed.put(key + ":1","");
					//System.out.println("T: "+fek);
				}
			}
			
			
			for (feature_node fecu : features_list) {

				for (Integer tp : fecu.tokenpos) {
					// System.out.println("i="+i+" tp="+tp+" tp+i="+(tp+i)+"
					// if="+((tp+i)>=0));
					if ((i + tp) >= 0 && (i + tp) < lns.size()) {
						Sentence ln = lns.get((i + tp));

						for (Integer cp : fecu.colpos) {
							//System.out.println(tp+" : "+cp+" : "+ln.features.get(cp));
							String fe = "F:" + tp + ":" + cp + ":" + ln.features.get(cp);
							int key = -1;
							if (!pr.indexm.containsKey(fe)) {
								key = pr.indexm.size() + 1;
								pr.indexm.put(fe, key);
							} else {
								key = pr.indexm.get(fe);
							}
							lnt.features_transformed.put(key + ":1","");
							//System.out.println("F:"+tp+":"+cp+":"+ln.features.get(cp));
						}
					} else if ((i + tp) < 0) {
						//Sentence ln = lns.get(i);
						for (Integer cp : fecu.colpos) {
							String fe = "F:" + tp + ":" + cp +":"+tp+ "__BOS__";
							int key = -1;
							if (!pr.indexm.containsKey(fe)) {
								key = pr.indexm.size() + 1;
								pr.indexm.put(fe, key);
							} else {
								key = pr.indexm.get(fe);
							}
							lnt.features_transformed.put(key + ":1","");
							//System.out.println("F:"+tp+":"+cp+":"+tp+"__BOS__");
						}
					} else if ((i + tp) >= lns.size()) {
						//Sentence ln = lns.get(i);
						for (Integer cp : fecu.colpos) {
							String fe = "F:" + tp + ":" + cp +":"+tp+ "__EOS__";
							int key = -1;
							if (!pr.indexm.containsKey(fe)) {
								key = pr.indexm.size() + 1;
								pr.indexm.put(fe, key);
							} else {
								key = pr.indexm.get(fe);
							}
							lnt.features_transformed.put(key + ":1","");
							//System.out.println("F:"+tp+":"+cp+":"+tp+"__EOS__");
						}
					}
				}
			}
			
			
			// System.out.println("=======");

			// Bigrams
			/*for (Integer tp : tokenposBi) {
				// System.out.println("i="+i+" tp="+tp+" tp+i="+(tp+i)+"
				// if="+((tp+i)>=0));
				if ((i + tp) >= 0 && (i + tp) < lns.size()) {
					Sentence ln = lns.get((i + tp));
					String fe = 
							//"FB:" 
							"F:" 
					+ tp + ":" + colposBi.getFirst() + "-" + colposBi.getLast() + ":"
							+ ln.features.get(colposBi.getFirst()) + ln.features.get(colposBi.getLast());
					int key = -1;
					if (!pr.indexm.containsKey(fe)) {
						key = pr.indexm.size() + 1;
						pr.indexm.put(fe, key);
					} else {
						key = pr.indexm.get(fe);
					}
					ln.features_transformed.put(key + ":1","");
					// System.out.println("F:"+tp+":"+cp+":"+ln.features.get(cp));
				}
			}*/

		}

	}

	public void processTest(LinkedList<Sentence> lns, parameters pr) {
		boolean done = false;
		for (int i = 0; i < lns.size(); i++) {
			Sentence lnt = lns.get((i));
			
			// System.out.println("Current Sentence: "+i +" word:
			// "+lns.get(i).token);
			// adjust the featuren LAST_FEATURE_INDEX on colpos
			/*if (!done) {
				
				for (feature_node fe : features_list) {
					if (fe.last_feature_not_known) {
						for (int ilo = fe.colpos.getFirst(); ilo <= lnt.features.size() - 1; ilo++) {
							fe.colpos.addLast(ilo);
						}
						fe.last_feature_not_known=false;
					}
				}
				
				// loop on tags and T_features_list to insert the indexs:
				for (feature_node fe : T_features_list) {
					if (fe.last_feature_not_known) {
						for (int ilo = fe.tokenpos.getFirst(); ilo <= -1; ilo++) {
							fe.tokenpos.addLast(ilo);
						}
						fe.last_feature_not_known=false;
					}
				}
				pr.tags_predicted_features_list = T_features_list;

				done = true;
			}*/
//			System.out.println("tokenpos="+features_list.getFirst().tokenpos);
//			System.out.println("colpos="+features_list.getFirst().colpos);

			int keyt = -1;
			if (!pr.tags.containsKey(lnt.tag)) {
				keyt = pr.tags.size() + 1;
				pr.tags.put(lnt.tag, keyt);
			} else {
				keyt = pr.tags.get(lnt.tag);
			}
			//lnt.features_transformed.addFirst(String.valueOf(keyt));
			//lnt.features_transformed.put(String.valueOf(keyt),"");
			lnt.tag_transformed = String.valueOf(keyt);
			
			for (feature_node fecu : features_list) {

				for (Integer tp : fecu.tokenpos) {
					// System.out.println("i="+i+" tp="+tp+" tp+i="+(tp+i)+"
					// if="+((tp+i)>=0));
					if ((i + tp) >= 0 && (i + tp) < lns.size()) {
						Sentence ln = lns.get((i + tp));

						for (Integer cp : fecu.colpos) {
							//System.out.println(tp+" : "+cp+" : "+ln.features.get(cp));
							String fe = "F:" + tp + ":" + cp + ":" + ln.features.get(cp);
							int key = -1;
							if (pr.indexm.containsKey(fe)) {
								key = pr.indexm.get(fe);
								lnt.features_transformed.put(key + ":1","");
							}
							//System.out.println("F:"+tp+":"+cp+":"+ln.features.get(cp));
						}
					} else if ((i + tp) < 0) {
						//Sentence ln = lns.get(i);
						for (Integer cp : fecu.colpos) {
							String fe = "F:" + tp + ":" + cp +":"+tp+ "__BOS__";
							int key = -1;
							if (pr.indexm.containsKey(fe)) {
								key = pr.indexm.get(fe);
								lnt.features_transformed.put(key + ":1","");
							}
							//System.out.println("F:"+tp+":"+cp+":"+tp+"__BOS__");
						}
					} else if ((i + tp) >= lns.size()) {
						//Sentence ln = lns.get(i);
						for (Integer cp : fecu.colpos) {
							String fe = "F:" + tp + ":" + cp +":"+tp+ "__EOS__";
							int key = -1;
							if (pr.indexm.containsKey(fe)) {
								key = pr.indexm.get(fe);
								lnt.features_transformed.put(key + ":1","");
							}
							//System.out.println("F:"+tp+":"+cp+":"+tp+"__EOS__");
						}
					}
				}
			}
			
			
			
			/*for (feature_node fecu : features_list) {
				for (Integer tp : fecu.tokenpos) {
					// System.out.println("i="+i+" tp="+tp+" tp+i="+(tp+i)+"
					// if="+((tp+i)>=0));
					if ((i + tp) >= 0 && (i + tp) < lns.size()) {
						Sentence ln = lns.get((i + tp));
						//System.out.println("tp: "+tp+" i: "+i+"  features=>"+ln.features.size()+" = "+ln.features);
						for (Integer cp : fecu.colpos) {
							
							String fe = "F:" + tp + ":" + cp + ":" + ln.features.get(cp);
							//System.out.println("F:"+tp+":"+cp+":"+ln.features.get(cp)+" ==> "+pr.indexm.containsKey(fe));
							if (pr.indexm.containsKey(fe)) {
								int key = pr.indexm.get(fe);
								lnt.features_transformed.put(key + ":1","");
								// System.out.println("F:"+tp+":"+cp+":"+ln.features.get(cp));
							}
						}
					} else if ((i + tp) < 0) {
						//Sentence ln = lns.get(i);
						for (Integer cp : fecu.colpos) {
							String fe = "F:" + tp + ":" + cp +":"+tp+ "__BOS__";
							//System.out.println("F:"+tp+":"+cp+":"+ln.features.get(cp)+"__BOS__"+" ==> "+pr.indexm.containsKey(fe));
							if (pr.indexm.containsKey(fe)) {
								int key = pr.indexm.get(fe);
								lnt.features_transformed.put(key + ":1","");
								// System.out.println("F:"+tp+":"+cp+":"+ln.features.get(cp)+"__BOS__");
							}
						}
					} else if ((i + tp) >= lns.size()) {
						//Sentence ln = lns.get(i);
						for (Integer cp : fecu.colpos) {
							String fe = "F:" + tp + ":" + cp +":"+tp+ "__EOS__";
							if (pr.indexm.containsKey(fe)) {
								int key = pr.indexm.get(fe);
								lnt.features_transformed.put(key + ":1","");
								// System.out.println("F:"+tp+":"+cp+":"+ln.features.get(cp)+"__EOS__");
							}
						}
					}
				}
			}
			// System.out.println("=======");

			// Bigrams
			for (Integer tp : tokenposBi) {
				// System.out.println("i="+i+" tp="+tp+" tp+i="+(tp+i)+"
				// if="+((tp+i)>=0));
				if ((i + tp) >= 0 && (i + tp) < lns.size()) {
					Sentence ln = lns.get((i + tp));
					String fe = "FB:" + tp + ":" + colposBi.getFirst() + "-" + colposBi.getLast() + ":"
							+ ln.features.get(colposBi.getFirst()) + ln.features.get(colposBi.getLast());
					if (pr.indexm.containsKey(fe)) {
						int key = pr.indexm.get(fe);
						ln.features_transformed.put(key + ":1","");
						// System.out.println("F:"+tp+":"+cp+":"+ln.features.get(cp));
					}
				}
			}*/

		}

	}
	
	
}
