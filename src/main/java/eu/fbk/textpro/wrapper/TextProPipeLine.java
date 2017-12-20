package eu.fbk.textpro.wrapper;

import it.unimi.dsi.fastutil.bytes.ByteArrayList;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import javax.xml.bind.JAXBException;
import eu.fbk.textpro.toolbox.TEXTPROCONSTANT;
import eu.fbk.textpro.toolbox.toolbox;
import eu.fbk.textpro.wrapper.Textpro.Modules.Module;
import eu.fbk.textpro.wrapper.Textpro.Modules.Module.Input.Field;
import eu.fbk.textpro.wrapper.utility.Txp2XML;

public class TextProPipeLine {
	 private LinkedHashMap<Object, String> activeModules = new LinkedHashMap<Object, String>();
	 private LinkedHashMap<Object, Integer> dependentModules = new LinkedHashMap<Object, Integer>();
	 private LinkedHashMap<Object, String> processedModules = new LinkedHashMap<Object, String>();
	OBJECTDATA wrapperFile = new OBJECTDATA();
	ObjectDataUtil wrapperFileUtil = new ObjectDataUtil();
	Textpro myFile;
	toolbox tools;


	

	
	public void TextProPipeLine(toolbox tool){
		myFile=(Textpro) tool.getConfigFileReader().read("modules.xml","eu.fbk.textpro.wrapper");
		tools = tool;

		try {
			manageInput(tools.variables.getInfile(), tools.variables.getOutputDir(), tools.variables.getOutputFileName(),
					tools.variables.getIntermediateFile(), tools.variables.getIntermediateFileOutput(), tools.variables.getUserModelsToRun(),
					tools.variables.isXml(),
					tools.variables.isY());
		} catch (InvocationTargetException|IOException|JAXBException |InstantiationException |ClassNotFoundException|NoSuchMethodException|IllegalAccessException e) {
			e.printStackTrace();
		}
		
		
	}

	public TextProPipeLine() {
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

	private void manageInput(File inFile, String outputDir,
			String outputFileName, String intermediateFile,
			String intermediateFileOutput, String userModelsToRun,
			boolean outputXML, boolean forceOutput) throws JAXBException,
			IOException, InvocationTargetException, ClassNotFoundException,
			NoSuchMethodException, IllegalAccessException,
			InstantiationException {
		if (inFile != null && inFile.isDirectory()) {
		} else {
			manageFile(inFile, outputDir, outputFileName, userModelsToRun,
					intermediateFile, intermediateFileOutput, outputXML,
					forceOutput);
		}
	}

	private void manageFile(File inFile, String outputDir,
			String outputFileName, String userModelsToRun,
			String intermediateFile, String intermediateFileOutput,
			boolean outputXML, boolean forceOutput)
			throws InstantiationException, IllegalAccessException,
			ClassNotFoundException, NoSuchMethodException, SecurityException,
			IllegalArgumentException, InvocationTargetException, IOException,
			JAXBException {
		if (tools.variables.isVERBOSE()) {
			if (inFile == null)
				System.err.println("# TextPro is running on STDIN");
			else
				System.err.println("# TextPro is running on " + inFile);
		}
		runFile(inFile, outputDir, outputFileName, userModelsToRun,
				intermediateFile, intermediateFileOutput, outputXML,
				forceOutput);
	}

	@SuppressWarnings("unchecked")
	private boolean runFile(File inFile, String outputDir,
			String outputFileName, String userModelsToRun,
			String intermediateFile, String intermediateFileOutput,
			boolean outputXML, boolean forceOutCreation)
			throws InstantiationException, IllegalAccessException,
			ClassNotFoundException, NoSuchMethodException, SecurityException,
			IllegalArgumentException, InvocationTargetException, IOException,
			JAXBException {
		
		File outFile = null;
		if (inFile == null) {
			String input = readFileContent(System.in);
			ByteArrayList hht = new ByteArrayList(input.getBytes());
			if (tools.variables.isDetectLanguage()) {
			
			
				List<?> langs = toolbox.languageCat.findLanguage(hht);
				if (langs.size() > 0) {
					if (tools.variables.isVERBOSE())
						System.err.println("# Detected language: "
								+ ((String) langs.get(0)).replaceAll("\\..+",
										""));
					tools.variables.setLanguage(((String) langs.get(0)).substring(0, 3));
				}
				if (!tools.checkIsAvailableLanguage()) {
					System.err.println("ERROR! Language " + tools.variables.getLanguage()
							+ " is not valid");
					return false;
				}
			}
			wrapperFile.readData((Iterable<String>) hht.iterator(), TEXTPROCONSTANT.encoding);//TODO couldbe a bug here with the byte iterator to String iterator check it
		} else {
			
			if (tools.variables.isDetectLanguage()) {
				String input = readFileContent(new FileInputStream(inFile));
				List langs = tools.languageCat.findLanguage(new ByteArrayList(input
						.getBytes()));
				if (langs.size() > 0) {
					if (tools.variables.isVERBOSE())
						System.err.println("# Detected language: "
								+ ((String) langs.get(0)).replaceAll("\\..+",
										""));
					tools.variables.setLanguage(((String) langs.get(0)).substring(0, 3));
				}
				if (!tools.checkIsAvailableLanguage()) {
					System.err.println("ERROR! Language is not valid for "
							+ inFile);
					return false;
				}
				wrapperFile.readData(inFile, TEXTPROCONSTANT.encoding);

			} else {
				wrapperFile.readData(inFile, TEXTPROCONSTANT.encoding);
			}

			if (outputFileName == null)
				outFile = new File(outputDir, inFile.getName() + ".txp");
			else {
				outFile = new File(outputDir, outputFileName);
			}
		}

		
		if (outFile != null && outFile.exists() && !forceOutCreation) {
			System.err
					.println("WARNING! The output file "
							+ outFile
							+ " already exists. Run TextPro again using rewrite mode to on (see the option -y)");
			
			//System.exit(0);
		}

		long nowtime = 0;
		if (tools.variables.isVERBOSE()) {
			nowtime = new Date().getTime();
		}

		getActiveModules(userModelsToRun);
		if (!getDependentModules(activeModules)) {
			return false;
		}
		
		prepareOrdering(dependentModules);
		
		checkProcessedModules();
		
		sortandrunDependentModulesValue(intermediateFile,
				intermediateFileOutput, dependentModules);
		
		dependentModules.clear();
		

		// System.out.println("===activeModules&After finish the dependences===");
		// here the dependences modules has been processed and also the
		// activated modules required by the user
		// has been updated so the activated modules which has the value 1 is
		// processed and still the value 0
		// now we need to process the modules which have the value 0 in the
		// active modules hashtable
		// PrintHashtableKeySet(activeModules);
		// all the rest modules in the active module hash table has the same
		// priorty so we could run them one by one randomly
		runActiveModuleList(intermediateFile, intermediateFileOutput);
		
		// PrintHashtableKeySet(activeModules);
		if (tools.variables.isVERBOSE())
			System.err.println("# Execution time: "
					+ (new Date().getTime() - nowtime) + "ms");

		String[] usrToks = userModelsToRun.split("\\+");

		Hashtable<String, Integer> usr = new Hashtable<String, Integer>();
		int i=0;
		for (String tmp : usrToks){
			usr.put(tmp,i);
			i++;
		}
		wrapperFile.updateHeaderList("# FILE: " + tools.variables.getOutputFileName().replace(".txp", ""), false);
		wrapperFile.updateHeaderList("# LANGUAGE: " + tools.variables.getLanguage(),false);
		wrapperFile.updateHeaderList("# TIMESTAMP: " + toolbox.getISODate(),false);
		
		if (outputXML) {
			Txp2XML txpxml = new Txp2XML();
			try {
				File outxml = null;
				if (outFile != null) {
					outxml = new File(outFile.getPath().replaceFirst(
							"\\.txp$", ".xml"));
					if (outxml.exists()) {
						outxml.delete();
					}
				}
				txpxml.getXML(inFile, outxml);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}else{
			boolean createHeader =true;
			
			if (tools.variables.isStandardInput()) {
				wrapperFile.printStandardOutput(TEXTPROCONSTANT.encoding, usr,createHeader);
			}
			else {
				wrapperFile.saveInFile(outFile.getPath(), TEXTPROCONSTANT.encoding, usr,createHeader);
			}
		}
		
	
		

		if (tools.variables.isVERBOSE())
			System.err.println("\n# Saved the file " + outFile);
		activeModules.clear();
		dependentModules.clear();
		processedModules.clear();
		wrapperFile.resetFileData();
		return true;
	}

	private void runActiveModuleList(String intermediateFile,
			String intermediateFileOutput) throws IOException,
			InstantiationException, IllegalAccessException,
			ClassNotFoundException, NoSuchMethodException, SecurityException,
			IllegalArgumentException, InvocationTargetException {
		// System.out.println("running the rest of active modules");
		for (Object mt : activeModules.keySet()) {
			// System.err.println("######\n ModuleName="+mt.toString()+",val="+activeModules.get(mt)+"=Found int processed="+processedModules.containsKey(mt.toString()));
			// PrintHashtableKeySet(processedModules);
			if (activeModules.get(mt).equalsIgnoreCase("0")
					&& !processedModules.containsKey(mt)) {
				// System.err.println("runActiveModuleList runModule(" +
				// mt.toString() + " ...");
				runModule(mt.toString(), intermediateFile,
						intermediateFileOutput);
				activeModules.put(mt, "1");
				// System.err.println(mt +", processedActiveModule");
				processedModules.put(mt, "processedActiveModule");
				// } else {
				// this message is just to say that this message has been run
				// before and we have its output
				// System.out.println("The Module " + mt.toString() +
				// " is required as active Module, and we found that it had been processed");
			}
		}
	}

	private void checkProcessedModules() {
		// //check the active modules list, if we found it, add it to the
		// processed Modules
		for (Object acttmp : activeModules.keySet()) {
			Module modtm = getModule(acttmp.toString());
			for (Module.Output.Field modOuttmp : modtm.getOutput().getField()) {
				if (wrapperFile.tokensIndex.containsKey(modOuttmp.getName())) {
					processedModules.put(acttmp.toString(), "FoundInFile");
					// /actl.remove(); donot delete it, as we need it to filter
					// on the final output file.
				}
			}

		}
	}

	private void sortandrunDependentModulesValue(String intermediateFile,
			String intermediateFileOutput,
			LinkedHashMap<?, Integer> dependentmodules) throws IOException,
			InstantiationException, IllegalAccessException,
			ClassNotFoundException, NoSuchMethodException, SecurityException,
			IllegalArgumentException, InvocationTargetException {
		// Transfer as List and sort it
		// System.err.println("dependentmodules.size()"+
		// dependentmodules.size());
		ArrayList<Map.Entry<?, Integer>> l = new ArrayList<Map.Entry<?, Integer>>(
				dependentmodules.entrySet());
		Collections.sort(l, new Comparator<Map.Entry<?, Integer>>() {

			public int compare(Map.Entry<?, Integer> o1,
					Map.Entry<?, Integer> o2) {
				return o1.getValue().compareTo(o2.getValue());
			}
		});
		for (Map.Entry<?, Integer> asd : l) {
			// dependentModules.put( asd.getKey(), asd.getValue());

			dependentmodules.remove(asd.getKey());
			// System.err.println(" sortandrunDependentModulesValue runModule("
			// + asd.getKey().toString() + " "+ asd.getValue() + "...");
			runModule(asd.getKey().toString(), intermediateFile,
					intermediateFileOutput);
			processedModules.put(asd.getKey(), "processedDependent");
			if (activeModules.containsKey(asd.getKey())) {
				// activeModules.put(asd.getKey(), "1");
				// or simply i could remove the runned module from the active
				// Module both of them are correct
				activeModules.remove(asd.getKey());
			}
		}

	}

	private int getModuleDep(String string) {
		ListIterator<Textpro.Modules.Module> modl = myFile.getModules()
				.getModule().listIterator();
		int i = 0;
		while (modl.hasNext()) {
			Textpro.Modules.Module modltmp = modl.next();

			//System.out.println("list module : "+modltmp.getName());
			
			List<String> tmpDepModules = new ArrayList<String> ();
			
			if (modltmp.getName().equalsIgnoreCase(string)) {
				for (Module.Input.Field inte : modltmp.getInput().getField()) {
					Module module = getModuleByOutputField(inte.getName());

					//System.err.println("% " + module.name);
					if (!tmpDepModules.contains(module.name)) {
					//if (dependentModules.containsKey(module.name) && !tmpDepModules.contains(module.name)) {
						Object aa = dependentModules.get(module.name);
						int val = Integer.parseInt(aa.toString());
						//int val = Integer.parseInt(aa.toString()) - 1;
						if (val < 0)
							val = 0;
						
						dependentModules.put(module.name, val);
						
						//System.err.println("% " + module.name+" val= "+val);
						i += 1+val;
						tmpDepModules.add(module.name); //2017-12-18 added AL Minard. Used to avoid to add twice a module in case more than one column of it is neede.
					}// if i added it to filter just the needed modules, not
						// sure about it!!!
				}
			}
		}
		return i;
	}
	
	
	private void runModule(String module, String intermediateFile,
			String intermediateFileOutput) throws IOException,
			InstantiationException, IllegalAccessException,
			ClassNotFoundException, NoSuchMethodException, SecurityException,
			IllegalArgumentException, InvocationTargetException {
		long nowtime = 0;
		if (tools.variables.isVERBOSE()) {
			nowtime = new Date().getTime();
		}

		Hashtable<String, Integer> copyThisTokens = new Hashtable<String, Integer>();
		Textpro.Modules.Module runM = getModule(module);
		boolean createHeader = false, readHeaderfromOutput = false;
		boolean noHeader = true;

		// if (DEBUG)
		// System.err.println(">> " + module + ":" +
		// " [readHeaderfromOutput: "+readHeaderfromOutput+", noInputFileHeader:"
		// + noHeader+"]");
		if (tools.variables.isVERBOSE()) {
			System.err.print("# " + module + "... \n");
		}
		
		if (wrapperFile.tokensIndex.size() > 0) {
			noHeader = false;
			// this is to check if the header of the files contains the required
			// header of the module , if not system exit!
			// because we couldn't prepare the column for this module for some
			// reason
			// that the module which produce this column is not found in our
			// system!
			// and here we are sure that what our system could produce as
			// dependences should be served before reaching this point
			//System.err.println("Columns before running the module"+wrapperFile.tokensIndex.size()+" eoken exist?="+wrapperFile.tokensIndex.containsKey("token"));
			for (Textpro.Modules.Module.Input.Field field : runM.getInput()
					.getField()) {
				if (!wrapperFile.tokensIndex.containsKey(field.getName())) {
					System.err.println("SYSTEM BREAK!");
					System.err
							.println("The "
									+ module
									+ " module requires "
									+ field.getName()
									+ " column to be present in the input file."
									+ "\n Meanwhile, our system doesn't have the module to produce this column.");
					System.exit(-1);
				} else {
					copyThisTokens.put(field.getName(),
							wrapperFile.tokensIndex.get(field.getName()));
				}
			}
		}
		// if no header = no tokensIndex and the module requires columns as
		// input system exit error
		if (noHeader && runM.getInput().getField().size() > 0) {
			// print error and exist the system!
			System.err
					.println("The input file doesn't contain any columns, and the "
							+ module
							+ " module requires "
							+ runM.getInput().getField().size()
							+ " columns as Input!");
			System.exit(-1);
		}
		// System.out.println("Running the module: "+module);

		// String cmd = runM.getCmd().getValue()+" ";
		Textpro.Modules.Module.Params params = runM.getParams();
		String[] parms = null;
		if (params != null) {
			parms = new String[params.getParam().size() * 2 + 2];
			Iterator<Textpro.Modules.Module.Params.Param> parml = params
					.getParam().iterator();
			int q = 0;
			while (parml.hasNext()) {
				Textpro.Modules.Module.Params.Param parmtmp = parml.next();
				String ff = "";
				String tt = "";
				if (parmtmp.getValue().contains("$inputFile")) {
					// cut the string from the start till reaching {$inputFile}
					String gg = parmtmp.getValue().replaceAll("\\$inputFile",
							"");
					gg = gg.replaceAll(" ", "");
					tt = gg;
					ff = intermediateFile;
				} else if (parmtmp.getValue().contains("$outputFile")) {
					// cut the string from the start till reaching {$outputFile}
					String gg = parmtmp.getValue().replaceAll("\\$outputFile",
							"");
					gg = gg.replaceAll(" ", "");
					tt = gg;
					ff = intermediateFileOutput;
				} else if (parmtmp.getValue().contains("$language")) {
					// cut the string from the start till reaching {$language}
					String gg = parmtmp.getValue()
							.replaceAll("\\$language", "");
					gg = gg.replaceAll(" ", "");
					tt = gg;
					ff = tools.variables.getLanguage();
				} else if (parmtmp.getValue().contains("$kxparams")) {
					// cut the string from the start till reaching {$language}
					String gg = parmtmp.getValue()
							.replaceAll("\\$kxparams", "");
					gg = gg.replaceAll(" ", "");
					tt = gg;
					ff = tools.variables.getKxparams();
				} else if (parmtmp.getValue().contains("$dct")) {
					// cut the string from the start till reaching {$language}
					String gg = parmtmp.getValue()
							.replaceAll("\\$dct", "");
					gg = gg.replaceAll(" ", "");
					tt = gg;
					ff = tools.variables.getDCT();
				} else if (parmtmp.getValue().contains("$disable")) {
					// cut the string from the start till reaching {$language}
					String gg = parmtmp.getValue().replaceAll("\\$disable", "");
					gg = gg.replaceAll(" ", "");
					tt = gg;
					ff = tools.variables.getDisable();
				} else if (parmtmp.getValue().contains("-html")) {
					tt = "-html";
					ff = "no";
					if (tools.variables.isHtmlcleaner()) {
						ff = "yes";
					}
				} else if (parmtmp.getValue().equalsIgnoreCase("-h i")) {
					createHeader = true;
					continue;// / check it is jump while reaching this point or
								// not
				} else if (parmtmp.getValue().equalsIgnoreCase("-h o")) {
					readHeaderfromOutput = true;
					continue;// / check it is jump while reaching this point or
								// not
				} else {
					tt = parmtmp.getValue();
					/*
					 * if (tt.contains(" ")) { tt = tt.replaceAll("\\s+", " ");
					 * ff = tt.substring(tt.indexOf(" ")+1); tt =
					 * tt.substring(0,tt.indexOf(" ")); }
					 */
				}
				// cmd+=tt+" ";
				if (tt.length() > 0) {
					parms[q] = tt;
					q++;
				}

				if (ff != null && ff.length() > 0) {
					// if (ff.length()>0) {
					parms[q] = ff;
					q++;
					// Arrays.fill(parms, ff);
					// cmd+=ff+" ";
				}
			}
			if (tools.variables.getDisable().length() > 0) {
				parms[q] = "-dis";
				parms[q + 1] = tools.variables.getDisable();
			}
		}
		if (tools.variables.isVERBOSE()) {
			System.err.println("# time for preparing input process: "+(new Date().getTime() - nowtime) + "ms");
		}
		long nowtime2 = 0;
		if (tools.variables.isVERBOSE()) {
			nowtime2 = new Date().getTime();
		}
		
		String className = runM.getCmd().getValue();
		
		
		// Load the Class. Must use fully qualified name here!
		Class clazz = Class.forName(className);
		// I need an array as follows to describe the signature
		//Class[] parameters = new Class[] { String[].class };
		//Method method = clazz.getMethod("init", parameters);
		Object obj = clazz.newInstance();
		//method.invoke(obj, new Object[] { parms });
		
		
		
		if (tools.variables.isVERBOSE()) {
			System.err.println("# time for intial process: "+(new Date().getTime() - nowtime2) + "ms");
		}

		//LinkedList<String> tmp = new LinkedList<String>();
		//tmp.addAll(copyThisTokens.keySet());
		
		
		OBJECTDATA tmpInput = wrapperFile.getFileData(TEXTPROCONSTANT.encoding, copyThisTokens,createHeader);
		Object returned = null;
		

		if (runM.getInput().getChannel().equals("OBJECTDATA")) {
			returned = sendRequestAsFileData(parms,clazz, obj, tmpInput);
		} else if (runM.getInput().getChannel().equals("FILE")) {
			//System.err.println("intermediateFile:"+intermediateFile);	
			tmpInput.saveInFile(intermediateFile, TEXTPROCONSTANT.encoding,createHeader); 
			sendRequestAsFile(parms,clazz, obj, intermediateFile,intermediateFileOutput);
			returned = intermediateFileOutput;
		}// we could add stream data here.

		long nowtime3 = 0;
		if (tools.variables.isVERBOSE()) {
			nowtime3 = new Date().getTime();
		}
		
		
		if (runM.getOutput().getChannel().equals("OBJECTDATA")) {
			wrapperFile = wrapperFileUtil.merge(wrapperFile,
					(OBJECTDATA) returned);
		} else if (runM.getOutput().getChannel().equals("FILE")) {
			OBJECTDATA tt = new OBJECTDATA();
			LinkedList<String> headerFromModulesXML = new LinkedList<String>();
			headerFromModulesXML.addAll(copyThisTokens.keySet());
			
			for (Module.Output.Field field : runM.getOutput().getField()) {
				headerFromModulesXML.addLast(field.getName());
	        }
			/*boolean haveHeader=false;
			if(runM.getOutput().getHeader().size()>0){
				haveHeader=true;
			}*/
			//readHeaderfromOutput
			tt.readData(new File((String) returned), TEXTPROCONSTANT.encoding,readHeaderfromOutput,headerFromModulesXML); 
			wrapperFile = wrapperFileUtil.merge(wrapperFile, tt);
			File tmp3 = new File(intermediateFile);
			tmp3.delete();
			File tmp4 = new File(intermediateFileOutput);
			tmp4.delete();
		}
		
		if (tools.variables.isVERBOSE()) {
			System.err.println("# time for merging process: "+(new Date().getTime() - nowtime3) + "ms");
			//wrapperFile.printTokenIndexs();
		}

	}

	private Object sendRequestAsFile(String[] parms, Class<? extends TextProPipeLine> clazz,
			Object obj, String path, String intermediateFileOutput2) throws IllegalAccessException,
			IllegalArgumentException, InvocationTargetException,
			NoSuchMethodException, SecurityException {
		Class[] analyzeParameters = new Class[] { String.class,String.class,toolbox.class };

		Method anameth = clazz.getMethod("analyze", analyzeParameters);
		long nowtime = 0;
		if (tools.variables.isVERBOSE()) {
			nowtime = new Date().getTime();
		}
		Object intermediateFileOutput = (Object) anameth.invoke(obj, path,intermediateFileOutput2,tools);
		if (tools.variables.isVERBOSE()) {
			System.err.println("# processing time: "+(new Date().getTime() - nowtime) + "ms");
		}
		return intermediateFileOutput;
	}

	private Object sendRequestAsFileData(String[] parms, Class<? extends TextProPipeLine> clazz,
			Object obj, OBJECTDATA tmpInput) throws IllegalAccessException,
			IllegalArgumentException, InvocationTargetException,
			NoSuchMethodException, SecurityException {
		Class[] analyzeParameters = new Class[] {OBJECTDATA.class,toolbox.class };

		Method anameth = clazz.getMethod("analyze", analyzeParameters);
		long nowtime = 0;
		if (tools.variables.isVERBOSE()) {
			nowtime = new Date().getTime();
		}
		Object intermediateFileOutput = (Object) anameth.invoke(obj ,tmpInput,tools);
		/*((OBJECTDATA)intermediateFileOutput).printTokenIndexs();
		for(String l : ((OBJECTDATA)intermediateFileOutput).getFileLineByLine()){
			System.out.println(l);
		}*/
		if (tools.variables.isVERBOSE()) {
			System.err.println("# processing time: "+(new Date().getTime() - nowtime) + "ms");
		}
		return intermediateFileOutput;
	}

	private void prepareOrdering(LinkedHashMap dependentmodules) {
		for (Object modtmp : dependentmodules.keySet()) {
			int siz = getModuleDep(modtmp.toString());
			Object vv = dependentmodules.get(modtmp);
			//System.err.println("ORDER " + vv.toString());
			int val = Integer.parseInt(vv.toString()) + siz;
			dependentmodules.put(modtmp, val);
		}
	}

	public Textpro.Modules.Module getModule(String modulename) {
		for (Module modt : myFile.getModules().getModule()) {
			if (modt.name.equalsIgnoreCase(modulename))
				return modt;
		}
		return null;
	}

	private Textpro.Modules.Module.Input getInputFields(String modName) {
		Textpro.Modules.Module.Input temp = new Textpro.Modules.Module.Input();
		for (Module modltmp : myFile.modules.module) {
			if (modltmp.name.equalsIgnoreCase(modName)) {
				temp = modltmp.input;
				return temp;
			}
		}
		return temp;
	}

	private Textpro.Modules.Module getModuleByOutputField(String outputfieldname) {
		for (Module moduletmp : myFile.getModules().module) {
			for (Module.Output.Field fieldtmp : moduletmp.output.field) {
				if (outputfieldname.equalsIgnoreCase(fieldtmp.getName())) {
					return moduletmp;
				}
			}
		}
		return null;
	}

	private boolean getDependentModules(Map modules) {
		Iterator activeMl = modules.keySet().iterator();
		Textpro.Modules.Module module;
		while (activeMl.hasNext()) {
			String activMtmp = (String) activeMl.next();
			module = getModule(activMtmp);
			// System.err.println("# " + activMtmp + " ("+module.name+")");
			if (tools.variables.getLanguage() != null && !module.getLanguages().contains(tools.variables.getLanguage())) {
				System.err.println("ERROR! The module " + module.name
						+ " doesn't work for the language '" + tools.variables.getLanguage()
						+ "'. Check the file conf/modules.xml and try again.");
				return false;
				// System.exit(0);
			}
			Textpro.Modules.Module.Input inpl = getInputFields(activMtmp);

			for (Module.Input.Field fieldin : inpl.getField()) {
				module = getModuleByOutputField(fieldin.name);

				if (module.name == null) {
					System.err
							.println("ERROR! The field '"
									+ fieldin.name
									+ "' is not valid. Check the file conf/modules.xml and try again.");
					System.exit(0);

				} else if (!dependentModules.containsKey(module.name)
						&& !wrapperFile.tokensIndex.containsKey(fieldin
								.getName())) {
					// /2/10-2013 here Mohammed Fix the problem of getting
					// dependincy without checking the input fields if there are
					// !tokensIndex.containsKey(fieldin.getName())
					 //System.err.println("+ " +fieldin.name
					 //+" ("+module.name+") " + module.getLanguages());
					dependentModules.put(module.name, 0);
					getDependentModules((Map) dependentModules.clone());
				}
			}
		}
		return true;
	}

	private void getActiveModules(String input) {
		input = "+" + input + "+";
		for (Module moduletmp : myFile.getModules().module) {
			String moduleName = moduletmp.name;
			for (Module.Output.Field fieldtmp : moduletmp.getOutput()
					.getField()) {
				if (input.contains("+" + fieldtmp.getName() + "+")
						&& !wrapperFile.tokensIndex.containsKey(fieldtmp
								.getName())) {
					// System.err.println("-- " + moduleName);
					activeModules.put(moduleName, "0");
					break;
				}
			}
			if (!activeModules.containsKey(moduleName)) {
				for (Module.Output.Header headertmp : moduletmp.getOutput()
						.getHeader()) {
					if (input.contains("+" + headertmp.getName() + "+")
							&& !wrapperFile.tokensIndex.containsKey(headertmp
									.getName())) {
						activeModules.put(moduleName, "0");
					}
				}
			}

		}
	}

}
