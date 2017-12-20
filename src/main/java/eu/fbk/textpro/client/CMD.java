package eu.fbk.textpro.client;


public class CMD {

	/**
	 * @param args
	 * @throws Exception 
	 */
	public static void main(String[] args) throws Exception {
		
		String sentence="";
		//sentence = "-y -v -c token+sentiment+pf -i Facebook-onepost.txt";
		//sentence = "-y -v -c token+sentiment -i Facebook-testset.txt";
		//sentence = "-y -v -c token+tokenid+pos+full_morpho+wnpos+lemma+sentiment -i Facebook-testset.txt";
		sentence = "-y -v -c token+pos -i test/README";
		TCPClient.main(sentence.split(" "));
	}

}
