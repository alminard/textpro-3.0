package eu.fbk.textpro.ml;

import java.util.HashMap;
import java.util.LinkedList;

public class Sentence {
	public HashMap<Integer,String> features = new HashMap<>();
	public HashMap<String,String> features_transformed = new HashMap<String,String>();
	public LinkedList<String> predictions = new LinkedList<String>();
	public String tag_transformed;

	public String tag;
	public String token;
}
