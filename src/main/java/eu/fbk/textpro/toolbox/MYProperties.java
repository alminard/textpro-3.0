package eu.fbk.textpro.toolbox;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class MYProperties extends Properties{
	private static Properties prop = new Properties();
	public static String conf_path="";
	public String modelPath=null;
	public MYProperties(String configFilePath) throws IOException {
		super(prop);
		if(configFilePath==null){
		InputStream input = null;
		String filename = "config.properties";
		input = MYProperties.class.getClassLoader().getResourceAsStream(filename);
		// load a properties file from class path, inside static method
		prop.load(input);
		conf_path=filename;
		}else{
			prop.load(new FileInputStream(configFilePath));
			conf_path=configFilePath;
		}
		
	}
}
