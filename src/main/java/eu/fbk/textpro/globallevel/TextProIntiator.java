package eu.fbk.textpro.globallevel;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;

import eu.fbk.textpro.toolbox.MYProperties;
import eu.fbk.textpro.toolbox.toolbox;
import eu.fbk.textpro.wrapper.TextProPipeLine;
import eu.fbk.textpro.wrapper.Textpro;

public class TextProIntiator {

	public static void main(String[] args) throws ClassNotFoundException,
			IllegalAccessException, IllegalArgumentException,
			InvocationTargetException, InstantiationException,
			NoSuchMethodException, SecurityException, IOException {

		String conf = args[0];
		System.err.println("Conf file: " + conf);
		MYProperties prop = new MYProperties(conf);
		toolbox tools = new toolbox(prop);
		String sentence = "-y -v -c token+sentiment -i Facebook-testset.txt";
		tools.invokeTextPro(sentence.split(" "),prop);
		
		
		
	}

}
