package eu.fbk.textpro.wrapper;


import java.io.*;
import java.net.*;

import eu.fbk.textpro.toolbox.MYProperties;
import eu.fbk.textpro.toolbox.toolbox;
import eu.fbk.textpro.wrapper.Textpro;

public class wrapper {

	public static void main(String[] argv) throws Exception {
		String sentence;
		String modifiedSentence;
		//BufferedReader inFromUser = new BufferedReader(new InputStreamReader(
		//		System.in));
	
		if (argv.length > 0) {
			String conf = argv[0];
			System.err.println("Conf file: " + conf);
			MYProperties prop = new MYProperties(conf);

			toolbox tools = new toolbox(prop);
			//Textpro myFile;
			
			//myFile = (Textpro) tools.getConfigFileReader().read(prop, prop.getProperty("TEXTPROHOME")+"conf/modules.xml", "eu.fbk.textpro.wrapper");
			
			
			//tools.modulesInitiator().initiate(myFile.getModules().getModule(),prop);

		
			/*BufferedReader in = new BufferedReader(new InputStreamReader(connectionSocket.getInputStream()));
			DataOutputStream out = new DataOutputStream(connectionSocket.getOutputStream());
			*/
			
		
		
			if(argv.length>0){
			 String output ="";
			    for(int i=1;i< argv.length;i++)
			        output+=" "+argv[i];
			sentence =output;
			}else{
				sentence = "-y -v -c token+full_morpho+pos+comp_morpho+lemma+wnpos+chunk+entity -i test/trento_wiki_en.txt";
			}
			
			tools.invokeTextPro(sentence.split(" "), prop);
			
		
			
			//out.writeBytes(sentence + '\n');
			//modifiedSentence = in.readLine();
			//System.out.println("FROM SERVER: " + modifiedSentence);
		
		}
		
	}
}