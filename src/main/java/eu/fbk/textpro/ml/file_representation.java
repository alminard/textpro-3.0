package eu.fbk.textpro.ml;

import java.util.LinkedHashMap;


public class file_representation {
	public LinkedHashMap<Integer,Sentence> file = new LinkedHashMap<Integer,Sentence>();

	file_representation addSentence(Sentence sent){
		file.put(file.size(), sent);
		return this;
	}
	
	
}
