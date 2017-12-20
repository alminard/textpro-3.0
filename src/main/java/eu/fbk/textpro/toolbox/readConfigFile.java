package eu.fbk.textpro.toolbox;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.Hashtable;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import eu.fbk.textpro.wrapper.Textpro;

public class readConfigFile {
	static Hashtable<String, Object> filesAlreadyRead = new Hashtable<String, Object>();
	public Object read(MYProperties prop,String fileName, String classPackageName) {
		try {
			if (!filesAlreadyRead.containsKey(fileName)) {
				Object myFile = null;
				JAXBContext jc = JAXBContext.newInstance(classPackageName);
				Unmarshaller unmarshaller = jc.createUnmarshaller();

				File overwrittenFile = new File(prop.getProperty("TEXTPROHOME")+prop.getProperty("textpro_config"));

				if (overwrittenFile.exists() && overwrittenFile.isFile()) {

					myFile = (Textpro) unmarshaller
							.unmarshal(new InputStreamReader(
									new FileInputStream(overwrittenFile),
									"UTF-8"));
				} else {
					System.err.println("Error3: " + fileName
							+ " file not found!");
				}
				if (myFile != null) {
					filesAlreadyRead.put(fileName, myFile);
					return filesAlreadyRead.get(fileName);
				} else {
					return null;
				}
			} else {
				return filesAlreadyRead.get(fileName);
			}

		} catch (JAXBException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;

	}
	public Object read(String fileName, String classPackageName) {
		try {
			if (!filesAlreadyRead.containsKey(fileName)) {
				Object myFile = null;
				JAXBContext jc = JAXBContext.newInstance(classPackageName);
				Unmarshaller unmarshaller = jc.createUnmarshaller();

				URL url = getClass().getResource("/conf/" + fileName);
				File overwrittenFile = new File(TEXTPROVARIABLES.getTEXTPROPATH()
						+ "/conf/" + fileName);

				if (overwrittenFile.exists() && overwrittenFile.isFile()) {

					myFile = (Textpro) unmarshaller
							.unmarshal(new InputStreamReader(
									new FileInputStream(
											TEXTPROVARIABLES.getTEXTPROPATH()
													+ "/conf/" + fileName),
									"UTF-8"));
				} else if (url != null) {
					myFile = (Textpro) unmarshaller
							.unmarshal(new InputStreamReader(url.openStream(),
									"UTF-8"));
				} else {
					System.err.println("Error3: " + fileName
							+ " file not found!");
				}
				if (myFile != null) {
					filesAlreadyRead.put(fileName, myFile);
					return filesAlreadyRead.get(fileName);
				} else {
					return null;
				}
			} else {
				return filesAlreadyRead.get(fileName);
			}

		} catch (JAXBException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;

	}
}
