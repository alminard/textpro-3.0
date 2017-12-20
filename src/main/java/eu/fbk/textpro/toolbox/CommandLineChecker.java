package eu.fbk.textpro.toolbox;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.Collections;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import javax.xml.bind.JAXBException;
import org.apache.qpid.junit.extensions.util.CommandLineParser;
import org.apache.qpid.junit.extensions.util.ParsedProperties;
import org.omg.CORBA.ExceptionList;

import eu.fbk.textpro.modules.tokenpro.LexparsConfig;
import eu.fbk.textpro.modules.tokenpro.NormalizeText;
import eu.fbk.textpro.wrapper.Textpro;
import eu.fbk.textpro.wrapper.Textpro.Modules;
import eu.fbk.textpro.wrapper.checkModulesXml;
import eu.fbk.textpro.wrapper.Textpro.Modules.Module;

public class CommandLineChecker {
	CommandLineParser commandLine;
	checkModulesXml tryt;

	public CommandLineChecker(toolbox toolbox) {
		if (commandLine == null) {
			String fields = "";
			tryt = toolbox.checkModulesXML();
			for (String field : checkModulesXml.outputColumns.keySet()) {
				fields += "+" + field;
			}

			for (String header : checkModulesXml.outputHeaders.keySet()) {
				fields += "+" + header;
			}
			commandLine = new CommandLineParser(
					new String[][] {
							{ "version", "show the version details and exit;",
									null, "false" },
							{
									"html",
									"clean html input file; the relevant text is kept as input text;",
									null, "false" },
							{ "h", "show the help and exit;", null, "false" },
							{
									"debug",
									"debug mode, do not delete tmp-files and to get more verbose output;",
									null, "false" },
							{
									"report",
									"check the input text and print a report on the unknown things;",
									null, "false" },
							{ "v", "verbose mode;", null, "false" },
							{
									"l",
									"the language: 'eng' or 'ita' are possible; 'eng' is the default;",
									"<LANGUAGE>", "false" },
							{
									"c",
									"the sequence of the output values: "
											+ fields.replaceFirst("\\+", "")
											+ ";", "<COLUMN or HEADER fields>",
									"false" },
							{ "o", "the output directory path;", "<DIRNAME>",
									"false" },
							{
									"n",
									"the output filename. If this value is specified the output is redirected to the file named as FILENAME. By default the file named as INPUTFILE plus '.txp' suffix;",
									"<FILENAME>", "false" },
							{ "xml", "provides XML output;", null, "false" },
							{
									"y",
									"force rewriting all existing output files;",
									null, "false" },
							{
									"dis",
									"disable the tokenization or/and sentence splitting;",
									"tokenization+sentence", "false" },
							{
									"tmp",
									"set a temporary directory (by default TextPro uses /tmp/);",
									"<TMPDIR>", "false" },
							{
										"config",
										"path to config.properties file",
										"config.properties", "false" },
							{
									"kxparams",
									"set the options of the keywords extraction module: PARAMS is a list of pair PARAMNAME[=VALUE] separated by comma. The list of all PARAMNAME and their VALUE are available in docs/KX_Reference.pdf.",
									"<PARAMS>", "false" },
							{
										"dct",
										"set the document creation time.",
										"<DCT>", "false" },
							{
									"update",
									"update TextPro model using manual adding;",
									null, "false" },
									{ "i", "input raw text, html text or directory.", "<INPUT FILE or DIR>", "false" },
									{ "rec", "read recursively all the files of a folder and his sub-folders.", null, "false" },
									{ "type", "type of the texts. For the moment the only value is 'colloquial', by default it will be news.", "<TYPE>", "false"}

					});
		}
	}

	private void Usage() {
		System.err
				.println("Usage:\n   textpro.sh [OPTIONS] <STDIN or INPUT FILE or DIR>\n");
		System.err.println(commandLine.getUsage());
	}

	private void getModulesVersionDetails(Modules modules) {
		Iterator<Module> ml = modules.getModule().iterator();
		System.err
				.println("\nVersion of TextPro's modules:\n==========================");
		while (ml.hasNext()) {
			Module mtmp = ml.next();
			System.err.println("- " + mtmp.getName() + ": "
					+ mtmp.getModulesVersionDetails());
		}
	}

	public int check(String[] args, toolbox tools) {
		ParsedProperties options = null;

		try {
			options = new ParsedProperties(commandLine.parseCommandLine(args));
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
			System.err.println(commandLine.getErrors());
			Usage();
			//return TEXTPROCONSTANT.process_fatal_error_stop;
			return TEXTPROCONSTANT.process_error_continue;
		}
		try {
			if (options.getPropertyAsBoolean("version")) {
				System.out.println("TextPro version: "
						+ TEXTPROCONSTANT.VERSION);

				getModulesVersionDetails(((Textpro) tools.getConfigFileReader()
						.read("modules.xml", "eu.fbk.textpro.wrapper"))
						.getModules());
				return TEXTPROCONSTANT.process_ok_exit;
			}
			if (options.getPropertyAsBoolean("rec")) {
				tools.variables.setActiveRecursiveDir(true);
	        }
			if (options.getPropertyAsBoolean("test")) {
				tryt.main(args);
				return TEXTPROCONSTANT.process_ok_exit;
			}
			if (options.getProperty("v") != null) {
				tools.variables.setVERBOSE(options.getPropertyAsBoolean("v"));
			}
			if (options.getProperty("html") != null) {
				tools.variables.setHtmlcleaner(options
						.getPropertyAsBoolean("html"));
			}
			if (options.getPropertyAsBoolean("debug")) {
				tools.variables.setDEBUG(true);
			}
			if (options.getProperty("kxparams") != null) {
				tools.variables.setKxparams(options.getProperty("kxparams"));
			}
			if (options.getProperty("tmp") != null) {
				tools.variables.setTmpDir(options.getProperty("tmp"));
			}
			if (options.getProperty("config") != null) {
				tools.variables.setConfigPath(options.getProperty("config"));
			}

			if (options.getProperty("l") != null) {
				tools.variables.setLanguage(options.getProperty("l"));
			}
			
			if (options.getProperty("dct") != null) {
				tools.variables.setDCT(options.getProperty("dct"));
			}
			if (options.getProperty("type") != null) {
				if (options.getProperty("type").equalsIgnoreCase("colloquial")) {
					tools.variables.setColloquialLanguage(true);
				}
			}

			if (tools.variables.getLanguage() == null) {
				tools.variables.setDetectLanguage(true);
				tools.loadLanguageModels(tools.variables.getProp());
			} else {
				tools.checkIsAvailableLanguage();
			}

			if (options.getProperty("c") != null)
				tools.variables.setUserModelsToRun(options.getProperty("c"));

			if (options.getPropertyAsBoolean("update")) {
				NormalizeText nt = new NormalizeText();

				try {
					String[] arrcmd = {
							TEXTPROVARIABLES.getTEXTPROPATH()
									+ "modules/bin/update_resources.sh", "ITA",
							"|&", "cat" };
					Process process = toolbox.runCommand(arrcmd);

					InputStream stdout = process.getInputStream();
					String line;
					// change the first space of each line into tabular
					BufferedReader fstmergeout = new BufferedReader(
							new InputStreamReader(stdout));
					while ((line = fstmergeout.readLine()) != null) {
						System.err.println(line);
					}

					fstmergeout.close();
					process.waitFor();

				} catch (InterruptedException e) {
					e.printStackTrace();
				}

				// update italian entities
				String entaddfile = TEXTPROVARIABLES.getTEXTPROPATH()
						+ "conf/resources/ita/entitydictionary.csv";
				File entfile = new File(entaddfile);
				StringBuilder newentities = new StringBuilder();
				if (entfile.exists() && entfile.length() > 0) {
					System.err
							.println("\nupdating the named entities white/black list for Italian...");
					BufferedReader entaddon = new BufferedReader(
							new InputStreamReader(new FileInputStream(entfile),
									TEXTPROCONSTANT.encoding));
					String line;
					int counter = 0;
					int error = 0;
					while ((line = entaddon.readLine()) != null) {
						String[] items = line.split("\\t");
						if (items.length == 2) {
							counter++;
							String tokennorm = nt.normalize(items[0],tools.variables.getLanguage());
							String[] tokens = tokennorm.split("\\s+");
							int i = 0;
							for (String token : tokens) {
								// System.err.println("> " +token);
								newentities.append(token.toLowerCase()).append(
										"\t");
								if (i == 0) {
									newentities.append("B-");
								} else {
									newentities.append("I-");
								}
								i++;
								newentities.append(items[1].toUpperCase())
										.append("\n");
							}

						} else {
							error++;
							System.err.println("WARNING! The line "
									+ (counter + error + 1) + " of the file "
									+ entaddfile + " is not a valid.");
							// System.exit(0);
						}

					}
					entaddon.close();

					// write out file
					// modules/EntityPro/resource/ITA/bw_list_v1.0.0
					OutputStreamWriter out = new OutputStreamWriter(
							new FileOutputStream(
									new File(
											"modules/EntityPro/resource/ITA/bw_list_v1.0.0")),
							TEXTPROCONSTANT.encoding);
					out.write(newentities.toString());
					out.close();
					System.err.print("added " + counter + " custom entities");
					if (error > 0) {
						System.err.println(" (" + error + " warnings)");
					}
					System.err.println("\nDONE!\n");

				}

				return TEXTPROCONSTANT.process_ok_exit;
			}

			if (options.getPropertyAsBoolean("h")) {
				Usage();
				return TEXTPROCONSTANT.process_ok_exit;
			}
			Random rn = new Random(System.nanoTime());
			if (options.getProperty("i") != null) {
				File inf = new File(options.getProperty("i"));
			//	System.err.println("111:"+options.getProperty("1")+"-:"+inf.getPath());
				tools.variables.setInfile(inf);
			}else{
				String intermediateFile= tools.variables.getTmpDir() + File.separator + "txp"+rn.nextLong()+".tmp";
				String input = readFileContent(System.in);
				File inf = new File(intermediateFile +".in");
	            writeFileContent(inf, input);
				
	            tools.variables.setInfile(inf);
	            
	            options.setProperty("i", intermediateFile +".in");
	            tools.variables.setStandardInput(true);
				//System.err.println("No Input file! call break!");
				//return TEXTPROCONSTANT.process_fatal_error_stop;
				//return TEXTPROCONSTANT.process_error_continue;
			}
			
			
			if (tools.variables.getInfile() != null) {
				if (!tools.variables.getInfile().exists()) {
					System.err.println("WARNING! The input file doesn't exists.("+tools.variables.getInfile()+")");
					//return TEXTPROCONSTANT.process_fatal_error_stop;
					return TEXTPROCONSTANT.process_error_continue;
				}
				if (tools.variables.getInfile().isDirectory()) {
					tools.variables.setOutputDir(tools.variables.getInfile()
							.getCanonicalPath());
				} else {
					if (options.getProperty("n") != null) {
						tools.variables.setOutputFileName(options
								.getProperty("n"));
					} else {
						tools.variables.setOutputFileName(tools.variables
								.getInfile().getName() + ".txp");
					}
					// report some info about the input file
					if (options.getPropertyAsBoolean("report")) {
						System.err.println("Checking... "
								+ tools.variables.getInfile());
						BufferedReader in = new BufferedReader(
								new InputStreamReader(new FileInputStream(
										tools.variables.getInfile())));
						Hashtable<Character, Integer> chars = new Hashtable<Character, Integer>();
						String line;
						while ((line = in.readLine()) != null) {
							for (int chp = 0; chp < line.length(); chp++) {
								if (chars.containsKey(line.charAt(chp)))
									chars.put(line.charAt(chp),
											chars.get(line.charAt(chp)) + 1);
								else
									chars.put(line.charAt(chp), 1);
							}
						}
						in.close();

						List<Character> chardec = Collections
								.list(chars.keys());
						Collections.sort(chardec);
						Iterator<Character> it = chardec.iterator();

						LexparsConfig lexpars = new LexparsConfig(
								tools.variables.getLanguage());
						int counter = 0;
						while (it.hasNext()) {
							Character ch = it.next();
							System.err.printf(
									"Char %s (id=%d, hexcode=%s, freq=%d)", ch,
									(int) ch, String.format("%04x", (int) ch),
									chars.get(ch));
							if (!lexpars.charSplitter(ch)
									&& !lexpars.generalSplittingRules(ch)) {
								// Remove entry if key is null or equals 0.
								// if (ch > 128)
								counter++;
								System.err
										.print(" is not used as splitting character");
							}
							System.err.println();
						}
						if (counter > 0) {
							System.err
									.println("There are some characters that TextPro doesn't consider. \n"
											+ "If you think it useful, you can add in the section charSplitter to the file conf/tokenization.xml some of them.");
						}

						return TEXTPROCONSTANT.process_ok_exit;
					}

					if (tools.variables.getInfile().getParent() != null) {
						tools.variables.setOutputDir(tools.variables
								.getInfile().getParent());
					} else {
						tools.variables.setOutputDir("./");
					}
				}

				if (options.getProperty("o") != null) {
					tools.variables.setOutputDir(options.getProperty("o"));
				}
			}

			if (options.getProperty("dis") != null) {
				if (options.getProperty("dis").contains("tokenization")
						|| options.getProperty("dis").contains("sentence")) {
					tools.variables.setDisable(options.getProperty("dis"));
				} else {
					System.err
							.println("WARNING! The value of the -dis option is not valid.");
					return TEXTPROCONSTANT.process_error_continue;
				}
			}

			tools.variables.setXml(options.getPropertyAsBoolean("xml"));
			tools.variables.setY(options.getPropertyAsBoolean("y"));
			tools.variables.setIntermediateFile(tools.variables.getTmpDir()
					+ File.separator + "txp" + rn.nextLong() + ".tmp");
			tools.variables.setIntermediateFileOutput(tools.variables
					.getIntermediateFile() + ".output");

			if(! tools.variables.getInfile().isDirectory()) {
				tools.variables.setBigFile(tools.checkIsBigFile(tools.variables
					.getInfile()));
			}
			
			return tryt.checkUserTaskAccomplishable(tools.variables.getUserModelsToRun());
		} catch (JAXBException | IOException e) {
			e.printStackTrace();
		}
		return TEXTPROCONSTANT.process_ok;

	}
	
	static String readFileContent(InputStream instream) throws IOException {
		StringBuilder builder = new StringBuilder();
		String aux = "";

		BufferedReader reader = new BufferedReader(new InputStreamReader(
				instream));
		while ((aux = reader.readLine()) != null) {
			builder.append(aux);
		}

		return builder.toString();
	}
	
	static void writeFileContent (File output, String content) throws IOException {
        OutputStream outStream = new FileOutputStream(output);
        content += "\n";
        outStream.write(content.getBytes());
        outStream.close();
    }

}
