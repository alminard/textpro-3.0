package eu.fbk.textpro.modules.lemmapro;

import java.io.IOException;

import javax.xml.bind.JAXBException;

public class client {

	/**
	 * @param args
	 * @throws JAXBException 
	 * @throws IOException 
	 */
	public static void main(String[] args) throws IOException, JAXBException {
		// TODO Auto-generated method stub
		//this is take the input from file
    	String[] parms = {"-l","english"};
		LemmaPro test = new  LemmaPro();
		test.init(parms,null);
		test.analyze(args[0], args[1]);
		//this is take the input from string
		//String send = "entrambi	DS	entrambi+adj+m+sing+pst+ind	entrambi+adj+m+sing+pst+ind entrambi+pron+_+m+3+sing+ind";
    	//String[] parms1 = {"-s",send,"-l","italian"};
    	//test.main(parms1);
    	
    	
    	// the comming run is to put the input from console and to finish entering that ctrl+D
    	//String[] parms2 = {"-stdin","","-l","italian"};
    	//test.main(parms2);
    	
	}

}
