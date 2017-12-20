package eu.fbk.textpro.toolbox;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;
import java.util.TimeZone;
import net.olivo.lc4j.LanguageCategorization;
import eu.fbk.textpro.wrapper.TextProPipeLine;
import eu.fbk.textpro.wrapper.Textpro;
import eu.fbk.textpro.wrapper.checkModulesXml;

public class toolbox {
	static readConfigFile readConfigFile;
	static initiateAllModules initiateAllModules;
	static FileChecker FileChecker;
	public TEXTPROVARIABLES variables;
	checkModulesXml tryt = null;
	CommandLineChecker cmdparser;
	public static LanguageCategorization languageCat = null;

	public toolbox(MYProperties prop) {
		TEXTPROVARIABLES.setProp(prop);
		TEXTPROVARIABLES.setTEXTPROPATH(prop.getProperty("TEXTPROHOME"));
		loadLanguageModels(prop);
		cmdparser = new CommandLineChecker(this);
	}

	public void invokeTextPro(String[] args,MYProperties prop) {
		Textpro myFile = (Textpro) getConfigFileReader().read("modules.xml",
				"eu.fbk.textpro.wrapper");
		//modulesInitiator().initiate(myFile.getModules().getModule(),prop);
		int parsingResponse = inputLine(args);
		if(parsingResponse == TEXTPROCONSTANT.process_fatal_error_stop){
			System.err.println("Fatal Error: Calling system discarded!");
		}else if(parsingResponse ==TEXTPROCONSTANT.process_ok||parsingResponse ==TEXTPROCONSTANT.process_error_continue){
			decisionUnit();
		}

	}

	public void invokeTextProFromServer(String[] args) {
		int parsingResponse = inputLine(args);
		if(parsingResponse == TEXTPROCONSTANT.process_fatal_error_stop){
			System.err.println("Fatal Error: Calling system discarded!");
		}else if(parsingResponse ==TEXTPROCONSTANT.process_ok){
			decisionUnit();
		}
		else if (parsingResponse ==TEXTPROCONSTANT.process_error_continue) {
			System.err.println("Fatal Error: Calling system discarded!");
		}
	}

	void decisionUnit() {
		TextProPipeLine textpro = new TextProPipeLine();

		// initiate TextProPipeline , check multi-threading , reporting, logging
		if (variables.getInfile().isDirectory()) {
			manageDirectories(textpro);
		} else {

			manageFile(textpro);

		}
	}

	void manageDirectories(TextProPipeLine textpro) {
		File[] faFiles = variables.getInfile().listFiles();
		if (faFiles != null) {
			for (File file : faFiles) {
				variables.setInfile(file);

				
				variables.setOutputDir(variables.getInfile().getParent());
			
				variables.setOutputFileName(variables.getInfile().getName()
						+ ".txp");

				if (!file.isFile() && new File(file.getAbsolutePath()+"/").isDirectory()) {
					if (variables.isActiveRecursiveDir()) {
						manageDirectories(textpro);
					}
				} else {
					if (!file.getName().startsWith(".")
							&& !file.getName().endsWith(".txp")
							&& file.getName().matches("^(.*?)")
							&& file.isFile()) {
						manageFile(textpro);
					}
				}
			}
		}

	}

	private void manageFile(TextProPipeLine textpro) {

		if (!variables.getInfile().getName().startsWith(".")
				&& !variables.getInfile().getName().endsWith(".txp")
				&& variables.getInfile().getName().matches("^(.*?)")
				&& variables.getInfile().isFile()) {
			if (variables.isBigFile()) {
				// textpro.TextProPipeLine(tools);
				manageBigFile(textpro);
			} else {
				textpro.TextProPipeLine(this);
			}
		}
	}

	private void manageBigFile(TextProPipeLine textpro){
		try {
		// //if a file with out # FIELDS: then it will be taken as header and
		// ignored from processing
		FileInputStream in = new FileInputStream(variables.getInfile());
		Reader reader = new InputStreamReader(in, TEXTPROCONSTANT.encoding);
		BufferedReader br = new BufferedReader(reader);
		Writer out = new BufferedWriter(new OutputStreamWriter(
				new FileOutputStream(new File(variables.getOutputDir(), variables.getOutputFileName())),
				TEXTPROCONSTANT.encoding));
		String line;
		boolean headerHoldfromPreviousStep = false;
		String previousHeader = "";
		String org = variables.getInfile().getPath();
		while ((line = br.readLine()) != null) {
			if (line.startsWith(TEXTPROCONSTANT.headerFile)) {
				if (variables.isVERBOSE())
					System.err.println(line);
			}
			if ((line.startsWith(TEXTPROCONSTANT.headerFields) || headerHoldfromPreviousStep)) {
				File inputtmp = new File(org + ".partical");
				File outputtmp = new File(org + ".partical.output");
				variables.setInfile(inputtmp);
				variables.setOutputDir(inputtmp.getParent());
				variables.setOutputFileName(outputtmp.getName());
				
				Writer outtmp = new BufferedWriter(new OutputStreamWriter(
						new FileOutputStream(inputtmp), TEXTPROCONSTANT.encoding));
				if (headerHoldfromPreviousStep) {
					headerHoldfromPreviousStep = false;
					outtmp.append(previousHeader).append("\n");
					previousHeader = "";
				} else if (line.startsWith(TEXTPROCONSTANT.headerFields)
						&& line.length() > TEXTPROCONSTANT.headerFields.length()) {
					// include it to the tmp because the input is column
					// structure
					outtmp.append(line).append("\n");
				} // else{
					// otherwise the input is raw text so just ignore that line
					// as the pipeline would generate the header for us
				// out.append(line).append("\n");
				// }
				while ((line = br.readLine()) != null && !line.startsWith("# ")) {
					outtmp.append(line).append("\n");
				}
				outtmp.flush();
				outtmp.close();
				textpro.TextProPipeLine(this);
				//TODO
					/* if (variables.getOutputDir().length() > 0)
						outputtmp = new File(variables.getOutputDir() + File.separator
								+ outputtmp.getName());*/

					FileInputStream intmp = new FileInputStream(outputtmp);
					Reader readertmpout = new InputStreamReader(intmp, TEXTPROCONSTANT.encoding);
					BufferedReader brtmp = new BufferedReader(readertmpout);
					String linetmp;
					while ((linetmp = brtmp.readLine()) != null) {
						out.append(linetmp).append("\n");
					}
					intmp.close();
					inputtmp.delete();
					outputtmp.delete();

					if (line != null) {
						if (line.startsWith(TEXTPROCONSTANT.headerFields)) {
							headerHoldfromPreviousStep = true;
							previousHeader = line;
						} else {
							// it is another header but start with "# " so copy
							// it to the output file
							out.append(line).append("\n");

						}
					}
				
			} else {// other case of having "# FIELDS:" or having previous
					// header
				out.append(line).append("\n");
			}
		}

		// System.err.println("outBig="+outputFile)
		out.flush();
		
			out.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	boolean checkIsBigFile(File file) {
		// the big file is a file contains at least two files with two # FILE:
		// or two # FIELDS:
		boolean foundHeader = false;
		try {
			String line;
			Reader reader = new InputStreamReader(new FileInputStream(file),
					TEXTPROCONSTANT.encoding);
			@SuppressWarnings("resource")
			BufferedReader br = new BufferedReader(reader);
			boolean foundFirst = false;
			while ((line = br.readLine()) != null) {
				if (line.length() > 0
						&& (
								//line.startsWith(TEXTPROCONSTANT.headerFields) 
								//|| 
								line.startsWith(TEXTPROCONSTANT.headerFile)
								)) {
					// System.err.println(file.getName() + " -- " +line);
					if (foundFirst) {
						foundHeader = true;
						return foundHeader;
					} else {
						foundFirst = true;
					}
				}
			}
			reader.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return foundHeader;
	}

	public int inputLine(String[] args) {
		variables = new TEXTPROVARIABLES();
		return cmdparser.check(args, this);
	}

	public checkModulesXml checkModulesXML() {
		if (tryt == null) {
			try {
				tryt = new checkModulesXml(this);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return tryt;
	}

	public static String getISODate() {
		TimeZone tz = TimeZone.getTimeZone("CET");
		DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'CET'");
		df.setTimeZone(tz);
		return df.format(new Date());
	}

	public String getTextProPath() {
		String temp = toolbox.class.getProtectionDomain().getCodeSource()
				.getLocation().getPath();
		int end;
		if (temp.contains("classes/"))
			end = temp.indexOf("classes/");
		else if (temp.contains("bin/"))
			end = temp.indexOf("bin/");
		else if (temp.contains("lib/textpro"))
			end = temp.indexOf("lib/textpro");
		else {
			end = -1;
			System.err
					.println("Couldn't initialize TextPro path!\nMake sure that textproX.X.jar is inside lib directory!");
			System.exit(0);
		}
		return temp.substring(0, end);
	}

	public boolean checkIsAvailableLanguage() {
		if (variables == null || variables.getLanguage() == null) {
			return false;
		}
		else if (variables.getLanguage() != null
				&& !variables.getLanguage().startsWith("eng")
				&& !variables.getLanguage().startsWith("ita")
				&& !variables.getLanguage().startsWith("fre")) {
			System.err.println("WARNING! The language is "
					+ variables.getLanguage()
					+ ". The valid language values are 'eng', 'ita' or 'fre'.");
			return false;
		}
		return true;
	}

	public void loadLanguageModels(MYProperties prop) {
		// nl_nl tr_tr ar_me iw_il pt-PT_pt fr es de cn ru_ru uk us de_at it
		/*
		 * String[] langAbbr = {"ar_ae","ar_me","ar","ara","fa","fas","per",
		 * "pol","nl_nl","nl","nl_be","flemish","vl","tr_tr","turkiye",
		 * "iw_il","pt-pt_pt","pt","fr","fre",
		 * "es","spa","de","ger","cn","chi","zh",
		 * "ru_ru","ru","uk","us","en","eng","de_at","it","ita"}; String[]
		 * langName =
		 * {"arabic","arabic","arabic","arabic","persian","persian","persian",
		 * "polish","dutch","dutch","dutch","dutch","dutch","turkish","turkish",
		 * "hebrew","portuguese","portuguese","french","french",
		 * "spanish","spanish","german","german","chinese","chinese","chinese",
		 * "russian"
		 * ,"russian","english","english","english","english","german","italian"
		 * ,"italian"};
		 */
		if (languageCat == null) {
			String[] langAbbr = { "ita", "eng", "fre" };
			String[] langName = { "italian", "english", "french" };

			if (langAbbr.length != langName.length) {
				System.err.println("Language detector error!");
				System.exit(0);
			}

			languageCat = new LanguageCategorization();
			//System.out.println("var: "+TEXTPROVARIABLES.getTEXTPROPATH()
			//			+ File.separator + prop.getProperty("language_models"));
			try {
				languageCat.loadLanguages(TEXTPROVARIABLES.getTEXTPROPATH()
						+ File.separator + prop.getProperty("language_models"));
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	public readConfigFile getConfigFileReader() {
		if (readConfigFile == null) {
			readConfigFile = new readConfigFile();
		}
		return readConfigFile;
	}

	public initiateAllModules modulesInitiator() {
		if (initiateAllModules == null) {
			initiateAllModules = new initiateAllModules();
		}
		return initiateAllModules;
	}

	public FileChecker fileChecker() {
		if (FileChecker == null) {
			FileChecker = new FileChecker();
		}
		return FileChecker;
	}

	public static Process runCommand(String[] command) throws IOException {
		try {
			ProcessBuilder pb = new ProcessBuilder();
			Map<String, String> env = pb.environment();
			// set environment variable
			env.put("TEXTPRO", TEXTPROVARIABLES.getTEXTPROPATH() + "modules/");
			env.put("PATH", "/usr/bin:/bin:/usr/sbin:/sbin:/usr/local/bin:.:"
					+ TEXTPROVARIABLES.getTEXTPROPATH() + ":"
					+ TEXTPROVARIABLES.getTEXTPROPATH() + "../:");
			env.put("TEXTPROHOME", TEXTPROVARIABLES.getTEXTPROPATH() + "/");
			env.put("JAVA_HOME", "/usr");

			// env.put("LANG","it_IT.UTF-8");
			// System.err.println("! " +Arrays.asList(command));
			pb.command(command);
			return pb.start();

		} catch (Exception e) {
			throw new IOException("TextPro error: " + e.getMessage());
		}

	}
	
	
}
