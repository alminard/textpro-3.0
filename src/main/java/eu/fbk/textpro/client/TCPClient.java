package eu.fbk.textpro.client;

import java.io.*;
import java.net.*;

import eu.fbk.textpro.toolbox.MYProperties;
import eu.fbk.textpro.toolbox.toolbox;
import eu.fbk.textpro.wrapper.Textpro;

public class TCPClient {
	public static void main(String[] argv) throws Exception {
		String sentence;
		String modifiedSentence;
		//BufferedReader inFromUser = new BufferedReader(new InputStreamReader(
		//		System.in));
		
		 Socket clientSocket =null;
		if (argv.length > 0) {
			String conf = argv[0];
			System.err.println("Conf file: " + conf);
			MYProperties prop = new MYProperties(conf);

			toolbox tools = new toolbox(prop);
			 Textpro myFile;
			
			myFile = (Textpro) tools.getConfigFileReader().read(prop, "target/textpro/conf/modules.xml", "eu.fbk.textpro.wrapper");
			clientSocket = new Socket(myFile.getServer().get(0).getName(), Integer.parseInt(myFile.getServer().get(0).getPort()));
		} else {
			clientSocket = new Socket("localhost", 6789);
		}
		
		DataOutputStream outToServer = new DataOutputStream(
				clientSocket.getOutputStream());
		BufferedReader inFromServer = new BufferedReader(new InputStreamReader(
				clientSocket.getInputStream()));
		//sentence = inFromUser.readLine();
		//sentence = "-y -v -c token+tokenid+pos+full_morpho+wnpos+lemma -i Facebook-onepost.txt";
		//sentence = "-y -v -c token -i Facebook-testset.txt";
		//sentence = "-y -v -c token+sentiment -i Facebook-onepost.txt";
		if(argv.length>0){
		 String output ="";
		    for(int i=1;i< argv.length;i++)
		        output+=" "+argv[i];
		sentence =output;
		}else{
			sentence = "-y -v -c token+full_morpho+pos+comp_morpho+lemma+wnpos+chunk+entity -i test/trento_wiki_en.txt";
		}
		outToServer.writeBytes(sentence + '\n');
		modifiedSentence = inFromServer.readLine();
		System.out.println("FROM SERVER: " + modifiedSentence);
		clientSocket.close();
	}
}