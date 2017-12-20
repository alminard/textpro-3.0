package eu.fbk.textpro.toplevel;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.Hashtable;
import java.util.ListIterator;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import eu.fbk.textpro.toolbox.TEXTPROVARIABLES;
import eu.fbk.textpro.wrapper.Textpro;
import eu.fbk.textpro.wrapper.Textpro.Modules.Module;
import eu.fbk.textpro.wrapper.Textpro.Modules.Module.Output;
import eu.fbk.textpro.wrapper.TextProPipeLine;

public class textpro {

	/**
	 * @param args
	 */
	Textpro myFile;
	Hashtable<String, textproModule> textproOptions = new Hashtable<String, textproModule>();

	public void textpro() throws JAXBException, IOException {
		readConfigFile();
		ListIterator<Module> modules = myFile.getModules().getModule()
				.listIterator();
		while (modules.hasNext()) {
			Module modtmp = modules.next();
			
			Output asd = modtmp.getOutput();
			textproModule tmp = new textproModule();
			tmp.moduleName = modtmp.getName();
			tmp.optionValue=asd;
			
			textproOptions.put(modtmp.getName(), tmp);
		}
		

	}

	void readConfigFile() throws JAXBException, IOException {
		JAXBContext jc = JAXBContext.newInstance("eu.fbk.textpro.wrapper");
		Unmarshaller unmarshaller = jc.createUnmarshaller();

		URL url = getClass().getResource("/conf/modules.xml");
		File overwrittenFile = new File(TEXTPROVARIABLES.getTEXTPROPATH()
				+ "/conf/modules.xml");

		if (overwrittenFile.exists() && overwrittenFile.isFile()) {
			// System.out.println("Found1:"+wrapper.TEXTPROPATH +
			// "/conf/modules.xml");
			myFile = (Textpro) unmarshaller.unmarshal(new InputStreamReader(
					new FileInputStream(TEXTPROVARIABLES.getTEXTPROPATH()
							+ "/conf/modules.xml"), "UTF-8"));
		} else if (url != null) {
			// System.out.println("Found2:"+getClass().getResource("/conf/modules.xml"));
			myFile = (Textpro) unmarshaller.unmarshal(new InputStreamReader(url
					.openStream(), "UTF-8"));
		} else {
			// System.out.println("F:"+wrapper.TEXTPROPATH +
			// "/conf/modules.xml");
			// System.out.println("URL:"+getClass().getResource("/conf/modules.xml"));
			System.out.println("Error3: modules.xml file not found!");

		}

	}
}
