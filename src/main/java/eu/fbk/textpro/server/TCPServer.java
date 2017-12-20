package eu.fbk.textpro.server;

import java.io.*;
import java.net.*;

import eu.fbk.textpro.toolbox.MYProperties;
import eu.fbk.textpro.toolbox.toolbox;
import eu.fbk.textpro.wrapper.Textpro;
import eu.fbk.textpro.wrapper.Textpro.Server;
import eu.fbk.textpro.wrapper.TextProPipeLine;

public class TCPServer {
	static Textpro myFile;

	public static void main(String argv[]) throws Exception {

			//String conf = "resources/conf/config.properties";
			String conf = "target/textpro/conf/config.properties";
			//String modules="modules.xml";
			String modules="target/textpro/conf/modules.xml";

			if(argv.length>0)
				conf= argv[0];
			MYProperties prop = new MYProperties(conf);
			System.err.println("Conf file: " + prop.conf_path);

			toolbox tools = new toolbox(prop);
			
			myFile = (Textpro) tools.getConfigFileReader().read(prop, modules, "eu.fbk.textpro.wrapper");
		
		tools.modulesInitiator().initiate(myFile.getModules().getModule(),prop);

		String clientSentence;
		String capitalizedSentence;
		Server server = myFile.getServer().get(0);
		InetAddress addr = InetAddress.getByName(server.getName());
		@SuppressWarnings("resource")
		ServerSocket welcomeSocket = new ServerSocket(Integer.parseInt(server.getPort()), 5, addr);
		System.out.println("Start listening to server name: " + server.getName() + " - port: " + server.getPort());
		while (true) {
			Socket connectionSocket = welcomeSocket.accept();
			BufferedReader inFromClient = new BufferedReader(new InputStreamReader(connectionSocket.getInputStream()));
			DataOutputStream outToClient = new DataOutputStream(connectionSocket.getOutputStream());
			clientSentence = inFromClient.readLine();
			System.out.println("Received: " + clientSentence);
			tools.invokeTextProFromServer(clientSentence.split(" "));

			capitalizedSentence = clientSentence.toUpperCase() + '\n';
			outToClient.writeBytes(capitalizedSentence);
		}
	}

}