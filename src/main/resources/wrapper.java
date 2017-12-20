package eu.fbk.textpro.wrapper;

import java.io.*;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import eu.fbk.textpro.api.TextProGate;
import eu.fbk.textpro.modules.tokenpro.LexparsConfig;
import eu.fbk.textpro.modules.tokenpro.TokenPro;
import eu.fbk.textpro.wrapper.Textpro.Modules.Module;
import eu.fbk.textpro.wrapper.utility.CommandLineParser;
import eu.fbk.textpro.wrapper.utility.Txp2XML;
import it.unimi.dsi.fastutil.bytes.ByteArrayList;
import net.olivo.lc4j.LanguageCategorization;
import org.apache.commons.io.FileUtils;


public class wrapper {
    static private Textpro myFile;
    static private CommandLineParser commandLine;
    static private LinkedHashMap<Object, String> activeModules = new LinkedHashMap<Object, String>();
    static private LinkedHashMap<Object, Integer> dependentModules = new LinkedHashMap<Object, Integer>();
    static private LinkedHashMap<Object, String> processedModules = new LinkedHashMap<Object, String>();
    static private LinkedHashMap<String, Integer> tokensIndex = new LinkedHashMap<String, Integer>();

    static private String tmpDir = "/tmp";
    static private boolean detectLanguage = false;
    static private String language = null; //"english";
    static private String kxparams = "";
    static private String date = null;
    static private boolean htmlcleaner = false;
    static private String disable = "";
    static public boolean DEBUG = false;
    static public boolean VERBOSE = false;
    static private Txp2XML txpxml;

    static LanguageCategorization languageCat = null;

    static final private String headerFields="# FIELDS:";
    static final private String headerFile="# FILE:";
    static final public String NULL = "__NULL__";
    static final private String encoding = "UTF8";

    static public String TEXTPROPATH = "./";
    static public String VERSION = "v2.0.1, Apr 2015";
    final public static String UTF8_BOM = "\uFEFF";
    private static DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ",Locale.ENGLISH);


    /**
     * @param args
     */
    public static void main(String[] args) throws JAXBException, IOException, InstantiationException, IllegalAccessException, ClassNotFoundException, NoSuchMethodException, SecurityException, IllegalArgumentException, InvocationTargetException {
        TEXTPROPATH	= getTextProPath();
        wrapper wrapConf = new wrapper();
        String timestamp = wrapConf.getISODate();

        wrapConf.readConfigFile();


        checkModulesXml tryt = new checkModulesXml();


        /// manage the files and all the procedure related to files by passing the required file name locally
        String outputDir="";
        String outputFileName=null;
        boolean activeRecursiveDir = false;
        // 	inputFile,outputDir,outputFile,inputFileMerged,intermediateFile,intermediateFileOutput
        // Use the command line parser to evaluate the command line.
        String fields = "";
        for (String field : checkModulesXml.outputColumns.keySet()) {
            fields += "+"+field;
        }

        for (String header : checkModulesXml.outputHeaders.keySet()) {
            fields += "+"+header;
        }

        final String[][] OptionsConfig = new String[][] {
                { "version", "show the version details and exit;", null, "false"},
                { "html", "clean html input file; the relevant text is kept as input text;", null, "false"},
                { "h", "show the help and exit;", null, "false"},
                { "debug", "debug mode, do not delete tmp-files and to get more verbose output;", null, "false"},
                { "report", "check the input text and print a report on the unknown things;", null, "false"},
                { "v", "verbose mode;", null, "false"},
                { "l", "the language: 'eng' or 'ita' are possible; 'eng' is the default;", "<LANGUAGE>","false"},
                { "c", "the sequence of the output values: "+fields.replaceFirst("\\+","")+";", "<COLUMN or HEADER fields>","false"},
                { "o", "the output directory path;", "<DIRNAME>", "false"},
                { "n", "the output filename. If this value is specified the output is redirected to the file named as FILENAME. By default the file named as INPUTFILE plus '.txp' suffix;", "<FILENAME>", "false"},
                { "xml", "provides XML output;", null, "false"},
                { "y", "force rewriting all existing output files;", null, "false"},
                { "d", "disable the tokenization or/and sentence splitting;", "tokenization+sentence", "false"},
                { "r", "process the input directory recursively;", null, "false" },
                { "tmp", "set a temporary directory (by default TextPro uses /tmp/);", "<TMPDIR>", "false"},
                { "kxparams", "set the options of the keywords extraction module: PARAMS is a list of pair PARAMNAME[=VALUE] separated by comma. The list of all PARAMNAME and their VALUE are available in docs/KX_Reference.pdf.", "<PARAMS>", "false"},
                { "date", "set the anchor date", "<DATE>","false"},
                { "update", "update TextPro model using manual adding;", null, "false"},
                //{ "test", "run the testing;", null, "false"},
                //{ "learn", "run TextPro using learning mode;", null, "false"},
                //{ "1", "input raw text, html text or directory.", "<INPUT FILE or DIR>", "false" },
        };

        // Capture the command line arguments or display errors and correct usage and then exit.

        try {
            commandLine = new CommandLineParser(args, OptionsConfig);

            if (commandLine.hasOption("version")) {
                for (Textpro.Property prop : myFile.getProperty()) {
                    if (prop.getName().equals("version")) {
                        VERSION = prop.getValue();
                    }
                }
                System.out.println("TextPro version: " + VERSION);
                wrapper.getModulesVersionDetails();
                System.exit(0);
            }
            if (commandLine.hasOption("test")) {
                tryt.main(args);
                System.exit(0);
            }
            if (commandLine.hasOption("r")) {
                activeRecursiveDir=true;
            }
            if (commandLine.hasOption("v")) {
                VERBOSE = true;
            }
            if (commandLine.hasOption("html")) {
                htmlcleaner=true;
            }
            if (commandLine.hasOption("debug")) {
                DEBUG = true;
            }
            if (commandLine.hasOption("date") && commandLine.hasArgument("date")) {
                date = (String) commandLine.optionValue("date");
            } else {
                date = timestamp;
            }
            //System.err.println("TextPro path:"+TEXTPROPATH + " ("+date+")");

            if (commandLine.hasOption("kxparams") && commandLine.hasArgument("kxparams")) {
                kxparams = (String) commandLine.optionValue("kxparams");
            }

            if (commandLine.hasOption("tmp") && commandLine.hasArgument("tmp")) {
                tmpDir = (String) commandLine.optionValue("tmp");
            }

            if (commandLine.hasOption("l") && commandLine.hasArgument("l")) {
                language = ((String) commandLine.optionValue("l")).toLowerCase().substring(0,3);
            }
            if (language == null) {
                detectLanguage=true;
                loadLanguageModels();
            } else {
                checkIsAvailableLanguage();
            }

            String userModelsToRun = "token+pos+lemma";
            if (commandLine.hasOption("c") && commandLine.hasArgument("c"))
                userModelsToRun = (String) commandLine.optionValue("c");

            if (commandLine.hasOption("update")) {
                if (language == null) {
                    System.err.println("WARNING! Updating needs to set the option -l with a valid language code (example: -l ita)");
                } else {
                    try {
                        String[] arrcmd = {TEXTPROPATH + "modules/bin/update_resources.sh",language, "|&","cat"};
                        Process process = runCommand(arrcmd);

                        InputStream stdout = process.getInputStream();
                        String line;
                        // change the first space of each line into tabular
                        BufferedReader fstmergeout = new BufferedReader (new InputStreamReader (stdout));
                        while ((line = fstmergeout.readLine()) != null) {
                            System.err.println(line);
                        }

                        fstmergeout.close();
                        process.waitFor();

                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                    //update italian entities
                    String entaddfile = TEXTPROPATH + "conf/resources/"+language+"/entitydictionary.csv";
                    File entfile = new File(entaddfile);
                    StringBuilder newentities = new StringBuilder();
                    if (entfile.exists() && entfile.length() > 0) {
                        System.err.println("\nupdating the named entities white/black list ("+entaddfile+"...");
                        BufferedReader entaddon = new BufferedReader (new InputStreamReader ( new FileInputStream (entfile), encoding));
                        String line;
                        int counter = 0;
                        int error = 0;
                        TokenPro tp = new TokenPro();
                        String[] params = new String[4];
                        params[0] = "-l";
                        params[1] = language;
                        params[2] = "-d";
                        params[3] = "sentence";
                        tp.init(params);

                        while ((line = entaddon.readLine()) != null) {
                            String[] items = line.split("\\t");
                            if (items.length == 2) {
                                counter++;
                                LinkedList<String> tokens = tp.tokenize(items[0], 0, null);
                                int i = 0;
                                for (String token : tokens) {
                                    //System.err.println("> (" +token+")");
                                    if (token.trim().length() > 0) {
                                        newentities.append(token.toLowerCase()).append("\t");
                                        if (i == 0) {
                                            newentities.append("B-");
                                        } else {
                                            newentities.append("I-");
                                        }
                                        i++;
                                        newentities.append(items[1].toUpperCase()).append("\n");
                                    }
                                }

                            } else {
                                error++;
                                System.err.println("WARNING! The line " +(counter+error+1)+ " of the file " +entaddfile + " is not a valid.");
                                //System.exit(0);
                            }

                        }
                        entaddon.close();

                        //write out file modules/EntityPro/resource/ITA/bw_list_v1.0.0
                        OutputStreamWriter out = new OutputStreamWriter(new FileOutputStream(new File(TEXTPROPATH+"modules/EntityPro/resource/ita/bw_list_v1.0.0")), encoding);
                        out.write(newentities.toString());
                        out.close();
                        System.err.print("added "+ counter +" custom entities");
                        if (error > 0) {
                            System.err.println(" ("+error+ " warnings)");
                        }
                        System.err.println("\nDONE!\n");
                    }
                }
                System.exit(1);
            }

            if (commandLine.hasOption("h")) {
                wrapper.Usage();
                System.exit(0);
            }


            Random rn = new Random(System.nanoTime());
            File inFile = null;
            if (commandLine.nonOptionArguments().size() > 0) {
                inFile = new File((String) commandLine.nonOptionArguments().get(0));
            }
            if (inFile != null) {
                if (!inFile.exists()) {
                    System.err.println("WARNING! The input directory or file is not valid.");
                    System.exit(0);
                }
                if (inFile.isDirectory()) {
                    outputDir = inFile.getCanonicalPath();
                } else {
                    if (commandLine.hasOption("n") && commandLine.hasArgument("n"))
                        outputFileName = (String) commandLine.optionValue("n");
                    else
                        outputFileName = inFile.getName() +".txp";

                    //report some info about the input file
                    if (commandLine.hasOption("report")) {
                        System.err.println("Checking... " +inFile);
                        BufferedReader in = new BufferedReader (new InputStreamReader (new FileInputStream(inFile)));
                        Hashtable<Character, Integer> chars = new Hashtable<Character, Integer>();
                        String line;
                        while ((line = in.readLine()) != null) {
                            for(int chp=0; chp<line.length(); chp++) {
                                if (chars.containsKey(line.charAt(chp)))
                                    chars.put(line.charAt(chp), chars.get(line.charAt(chp))+1);
                                else
                                    chars.put(line.charAt(chp), 1);
                            }
                        }
                        in.close();static String readFileContent(InputStream instream) throws IOException {
                    		StringBuilder builder = new StringBuilder();
                    		String aux = "";

                    		BufferedReader reader = new BufferedReader(new InputStreamReader(
                    				instream));
                    		while ((aux = reader.readLine()) != null) {
                    			builder.append(aux);
                    		}

                    		return builder.toString();
                    	}

                        List<Character> chardec = Collections.list(chars.keys());
                        Collections.sort(chardec);
                        Iterator<Character> it = chardec.iterator();

                        LexparsConfig lexpars = new LexparsConfig(language);
                        int counter = 0;
                        while (it.hasNext()) {
                            Character ch = it.next();
                            System.err.printf("Char %s (id=%d, hexcode=%s, freq=%d)", ch, (int) ch, String.format("%04x", (int) ch), chars.get(ch));
                            if (!lexpars.charSplitter(ch) && !lexpars.generalSplittingRules(ch) ) {
                                // Remove entry if key is null or equals 0.
                                //if (ch > 128)
                                counter++;
                                System.err.print(" is not used as splitting character");
                            }
                            System.err.println();
                        }
                        if (counter > 0) {
                            System.err.println("There are some characters that TextPro doesn't consider. \n" +
                                    "If you think it useful, you can add in the section charSplitter to the file conf/tokenization.xml some of them.");
                        }

                        //check the missed morphological analysis
                        //TokenPro tokenpro = new TokenPro();
                        //String[] options = {"-l","ita","-c","token"};
                        //tokenpro.init(tokpara);
                        //tokenpro.analyze(inFile.getCanonicalPath(), "/tmp/tokens.txt");
                        //get probable abbreviation

                        //MorphoPro morphpro = new MorphoPro();
                        //options = {"-l","ita"};
                        //get tokens without their lemma
                        System.err.flush();
                        System.err.print("\nChecking which tokens don't have the lemma...\n");
                        TextProGate textpro = null;
                        try {
                            textpro = new TextProGate();
                            // textpro.getChunkPro().deactivateAll(ChunkProType.class);
                            // textpro.getChunkPro().deactive(ChunkProType.chunk.name());
                            textpro.setLanguage(language);

                            textpro.overwriteOutput();
                            //textpro.activeVerboseMood();
                            //textpro.activeHtmlCleaner();
                            textpro.setOutputFolder("/tmp/");
                            textpro.setOutputFileName("report.txp");
                            textpro.setInputFile(inFile.getAbsolutePath());
                            textpro.getTokenPro().active(TextProGate.TokenProType.token.name());
                            textpro.getLemmaPro().active(TextProGate.LemmaProType.comp_morpho.name());
                            textpro.getTagPro().active(TextProGate.TagProType.pos.name());
                            textpro.runTextPro();

                            //read the result
                            HashMap<String,Integer> nolemma = new HashMap<String,Integer>();
                            File reportFile = new File("/tmp/report.txp");
                            in = new BufferedReader (new InputStreamReader (new FileInputStream(reportFile)));
                            while ((line = in.readLine()) != null) {
                                //System.err.println(line);
                                if (line.contains(wrapper.NULL)) {
                                    String[] items =  line.split("\t");
                                    if (!nolemma.containsKey(items[0]+" "+items[1]))
                                        nolemma.put(items[0]+" "+items[1], 1);
                                    else {
                                        nolemma.put(items[0]+" "+items[1], nolemma.get(items[0]+" "+items[1]) +1);

                                    }
                                }
                            }
                            in.close();
                            reportFile.delete();
                            if (nolemma.size() > 0) {
                                HashMap<String, Integer> nolemma2 = sortByValue( nolemma,true );

                                for (String tokenPos : nolemma2.keySet()) {
                                    System.out.println(nolemma2.get(tokenPos) + "\t" +tokenPos);
                                }
                                System.out.flush();
                                System.err.flush();
                                System.err.print("The lemma of the entries above hasn't been recognized, if you think it useful, you can add it in the custom morphological file conf/resources/"+language+"/morphodictionary.csv\n");
                            }
                        } catch (CloneNotSupportedException e) {
                            e.printStackTrace();
                        }
                        System.exit(1);
                    }

                    if (inFile.getParent() != null)
                        outputDir = inFile.getParent();
                    else {
                        outputDir = "./";
                    }

                }

                if (commandLine.hasOption("o") && commandLine.hasArgument("o"))
                    outputDir = (String) commandLine.optionValue("o");
            }


            if (commandLine.hasOption("d") && commandLine.hasArgument("d")) {
                disable = (String) commandLine.optionValue("d");
                if (!disable.contains("tokenization") && !disable.contains("sentence")) {
                    disable = "";
                    System.err.println("WARNING! The value of the -d option is not valid.");
                    System.exit(0);
                }
            }

            String intermediateFile= tmpDir + File.separator + "txp"+rn.nextLong()+".tmp";
            String inputFileMerged = intermediateFile+".merged";
            String intermediateFileOutput = intermediateFile+".output";

            ///check if the asked module from the user are accomplishable or not!
            tryt.checkUserTaskAccomplishable(userModelsToRun);

            manageInput(inFile,
                    outputDir,
                    outputFileName,
                    activeRecursiveDir,
                    inputFileMerged,
                    intermediateFile,
                    intermediateFileOutput,
                    userModelsToRun,
                    commandLine.hasOption("xml"),
                    commandLine.hasOption("y"));
        } catch (Exception e) {
            //System.err.println(commandLine.getErrors());
            wrapper.Usage();
            e.printStackTrace();
            System.exit(-1);
        }

    }


    private static void manageInput(File inFile, String outputDir, String outputFileName, boolean activeRecursiveDir,
                                    String inputFileMerged, String intermediateFile, String intermediateFileOutput,
                                    String userModelsToRun, boolean outputXML, boolean forceOutput) throws Exception {
        if (inFile != null && inFile.isDirectory()) {
            manageDirectories(inFile, outputDir, outputFileName, activeRecursiveDir, userModelsToRun, intermediateFile,  intermediateFileOutput,  inputFileMerged, outputXML, forceOutput);
        } else {
            manageFile(inFile,  outputDir, outputFileName, userModelsToRun, intermediateFile, intermediateFileOutput, inputFileMerged, outputXML, forceOutput);
        }

    }

    private static void manageFile(File inFile, String outputDir, String outputFileName, String userModelsToRun,
                                   String intermediateFile, String intermediateFileOutput,
                                   String inputFileMerged, boolean outputXML, boolean forceOutput) throws Exception {

        long nowtime = 0;
        if (VERBOSE) {
            nowtime = new Date().getTime();
        }

        File outFile = null;
        Writer outChannel;
        if (inFile == null) {
            if (VERBOSE)
                System.err.println("# TextPro is running on STDIN");
            outChannel = new BufferedWriter(new OutputStreamWriter(System.out));
            String input = readFileContent(System.in);
            inFile = new File(intermediateFile +".in");
            writeFileContent(inFile, input);

            /*if (detectLanguage) {
                List langs = languageCat.findLanguage(new ByteArrayList(input.getBytes()));
                if (langs.size() > 0) {
                    if (VERBOSE)
                        System.err.println("# Detected language: " +((String) langs.get(0)).replaceAll("\\..+",""));
                    language = ((String) langs.get(0)).substring(0,3);
                }
                if (!checkIsAvailableLanguage()) {
                    System.err.println("ERROR! Language "+ language+ " is not valid");
                    return false;
                }
            } */
        } else {
            if (VERBOSE)
                System.err.println("# TextPro is running on " + inFile);
            if (outputFileName==null)
                outFile = new File(inFile.getCanonicalPath() + ".txp");
            else {
                outFile = new File(outputDir, outputFileName);
            }
            if (outputXML) {
                outFile = new File(outFile.getPath().replaceFirst("\\.txp$",".xml"));
                txpxml = new Txp2XML();
            }
            if (outFile.exists() && !forceOutput) {
                System.err.println("WARNING! The output file " + outFile + " already exists. Run TextPro again using rewrite mode to on (see the option -y)");
                System.exit(0);
            }
            outChannel = new BufferedWriter(new OutputStreamWriter(
                    new FileOutputStream(outFile), encoding));
            if (outputXML) {
                txpxml.start(outChannel);
            }
        }

        if (isBigFile(inFile)) {
            manageBigFile(inFile, outChannel, userModelsToRun, intermediateFile, intermediateFileOutput, inputFileMerged, outputXML);
        } else {
            runFile(inFile, outChannel, userModelsToRun, intermediateFile, intermediateFileOutput, inputFileMerged, outputXML, false);
        }

        if (outFile != null && outFile.exists()) {
            if (VERBOSE)
                System.err.println("\n# Saved the file " + outFile);
            //System.err.println("outBig="+outputFile)
            outChannel.flush();
            if (outputXML) {
                txpxml.end(outChannel);
            } else {
                outChannel.close();
            }
        } else {
            outChannel.flush();
            outChannel.close();
        }
        if (VERBOSE) {
            System.err.println("# Execution time: "+(new Date().getTime() - nowtime) + "ms");
        }
        if (DEBUG) {
            System.err.println("# ... check the left tmp-files as " + intermediateFile + "*");
        }

    }


    private static void manageDirectories(File inFile, String outputDir, String outputFileName,
                                          boolean activeRecursiveDir,
                                          String userModelsToRun,
                                          String intermediateFile,
                                          String intermediateFileOutput,
                                          String inputFileMerged,
                                          boolean outputXML, boolean forceOutput) throws Exception {
        File[] faFiles = inFile.listFiles();
        if (faFiles != null) {
            for(File file: faFiles) {

                if (file.isDirectory()) {
                    if (activeRecursiveDir) {
                        manageDirectories(file,
                                outputDir,
                                outputFileName,
                                activeRecursiveDir,
                                userModelsToRun,
                                intermediateFile,
                                intermediateFileOutput,
                                inputFileMerged,
                                outputXML,
                                forceOutput);
                    }
                } else {
                    if (!file.getName().startsWith(".")
                            && !file.getName().endsWith(".txp")
                            && file.getName().matches("^(.*?)")
                            && file.isFile()) {
                        manageFile(file,
                                outputDir,
                                outputFileName,
                                userModelsToRun,
                                intermediateFile,
                                intermediateFileOutput,
                                inputFileMerged,
                                outputXML,
                                forceOutput);
                    }  else {
                        System.err.println("Skipping... " +file + " " +file.getName().startsWith(".") + " " +file.getName().endsWith(".txp"));
                    }
                }
            }
        }
    }


    private static void manageBigFile(File inputFile,
                                      Writer output,
                                      String userModelsToRun,
                                      String intermediateFile,
                                      String intermediateFileOutput,
                                      String inputFileMerged, boolean outputXML) throws InstantiationException, IllegalAccessException, ClassNotFoundException, NoSuchMethodException, SecurityException, IllegalArgumentException, InvocationTargetException, IOException, JAXBException {

        ////if a file with out # FIELDS: then it will be taken as header and ignored from processing
        BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(inputFile), encoding));
        String line;
        StringBuilder previousHeader = new StringBuilder();
        while((line = br.readLine()) != null) {
            if (!line.startsWith("# ")) {
                //System.err.println("TMP file: "+inputFile+".partical ("+line+")");
                File outputtmp = new File(inputFile+".part.output");

                File inputtmp = new File(inputFile+".part");
                Writer intmp = new BufferedWriter(new OutputStreamWriter(
                        new FileOutputStream(inputtmp), encoding));
                intmp.write(line+"\n");
                while((line = br.readLine()) != null && !line.startsWith("# ")) {
                    intmp.write(line+"\n");
                }
                intmp.flush();
                intmp.close();

                //add the header lines
                output.append(previousHeader.toString());

                Writer outtmp = new BufferedWriter(new OutputStreamWriter(
                        new FileOutputStream(outputtmp), encoding));

                if (runFile(inputtmp,
                        outtmp,
                        userModelsToRun,
                        intermediateFile,
                        intermediateFileOutput,
                        inputFileMerged,
                        outputXML,
                        false)) {
                    outtmp.close();

                    if (outputtmp.exists() && outputtmp.length() > 0) {
                        BufferedReader brtmp = new BufferedReader(new InputStreamReader(new FileInputStream(outputtmp), encoding));
                        String linetmp;
                        while ((linetmp = brtmp.readLine()) != null) {
                            if (linetmp.startsWith("# ") && previousHeader.toString().contains(linetmp.replaceFirst("\\s*:.*","")))
                                continue;
                            output.append(linetmp).append("\n");
                        }
                        brtmp.close();
                    }
                } else {
                    outtmp.close();
                    output.append(headerFields).append("\n");

                    System.err.println("WARNING! Something doesn't work in the pipeline.");
                }
                inputtmp.delete();
                outputtmp.delete();

                previousHeader.setLength(0);
                if (line != null && line.startsWith(headerFile)) {
                    if (VERBOSE)
                        System.err.println("\n< " +line);
                    previousHeader.append(line).append("\n");
                }

            } else {// other case of having "# FIELDS:" or having previous header
                if (line.startsWith(headerFile)) {
                    if (VERBOSE)
                        System.err.println("\n< " +line);
                }
                if (!line.startsWith(headerFields))
                    previousHeader.append(line).append("\n");
            }
        }
    }


    private static boolean runFile(File inFile,
                                   Writer output,
                                   String userModelsToRun,
                                   String intermediateFile,
                                   String intermediateFileOutput,
                                   String inputFileMerged,
                                   boolean outputXML, boolean bigFile) throws InstantiationException, IllegalAccessException, ClassNotFoundException, NoSuchMethodException, SecurityException, IllegalArgumentException, InvocationTargetException, IOException, JAXBException {
        ///first make a copy of the input file to save, then all the process will be on the input name file
        //FileUtils.moveFile(new File(inputFile).toPath(), new File(inputFile+".pipeline").toPath(),REPLACE_EXISTING);
        String tmpinput = intermediateFile+".input";
        //writeFileContent(new File(tmpinput), readFileContent(new FileInputStream(inFile)));
        //copyFile(inFile, tmpinput);
        if (detectLanguage) {
            String input = readFileContent(new FileInputStream(inFile));
            writeFileContent(new File(tmpinput), input);
            List langs = languageCat.findLanguage(new ByteArrayList(input.getBytes()));
            if (langs.size() > 0) {
                if (VERBOSE)
                    System.err.println("# Detected language: " +((String) langs.get(0)).replaceAll("\\..+",""));
                language = ((String) langs.get(0)).substring(0,3);
            }
            if (!checkIsAvailableLanguage()) {
                System.err.println("ERROR! Language is not valid for "+inFile);
                return false;
            }
        } else {
            copyFile(inFile, tmpinput);
        }

        //get the file tokens and the index of each token
        getTokensIndex(tmpinput);
        ////////finish checking the input of the user
        getActiveModules(userModelsToRun);
        if (!getDependentModules(activeModules)) {
            return false;
        }
        //System.out.println("activeModules="+activeModules.size()+"=dependentModules="+dependentModules.size());
        // System.out.println("===activeModules===");
        // PrintHashtableKeySet(activeModules);
        //System.out.println("===dependentModules===");
        //PrintHashtableKeySet(dependentModules);
        // now process the dependent modules one by one
        prepareOrdering(dependentModules);
        //System.out.println("===dependentModulesAfter Prepare Order===");
        //PrintHashtableKeySet(dependentModules);

        // check if the input file has the column which we get as output from a module, add the moduleName to the proceeded modules
        checkProcessedModules();// from the active and the dependent modules

        /*
        System.out.println("===activeModules===");
        PrintHashtableKeySet(activeModules);

        System.out.println("===dependentModules After Prepare Order===");
        PrintHashtableKeySet(dependentModules);

        System.out.println("===processed Modules===");
        PrintHashtableKeySet(processedModules);
        */

        //System.out.println("===tokens & indexings===");
        //PrintHashtableKeySet(tokensIndex);

        // sort and run the dependent modules
        /// now the dependent list show be ignored
        sortandrunDependentModulesValue( intermediateFile, intermediateFileOutput,
                inputFileMerged, tmpinput, dependentModules);
        dependentModules.clear();
        //check if we have

        //System.out.println("===activeModules&After finish the dependences===");
        // here the dependences modules has been processed and also the activated modules required by the user
        // has been updated so the activated modules which has the value 1 is processed and still the value 0
        // now we need to process the modules which have the value 0 in the active modules hashtable
        //PrintHashtableKeySet(activeModules);
        // all the rest modules in the active module hash table has the same priorty so we could run them one by one randomly
        runActiveModuleList(intermediateFile, intermediateFileOutput, inputFileMerged, tmpinput);
        //PrintHashtableKeySet(activeModules);
        // prepare final output file
        prepareFinalOutput(inFile.getName(), userModelsToRun, tmpinput, output, outputXML, bigFile);
        // when everything finishes
        // make the final output file  from inputFile to inputFile+".output"

        File tmpIn = new File(tmpinput);
        if (tmpIn.exists())
            tmpIn.delete();

        activeModules.clear();
        dependentModules.clear();
        processedModules.clear();
        return true;
    }


    static boolean isBigFile (File file) {
        // the big file is a file contains at least two files with two # FILE: or two # FIELDS:
        boolean foundHeader = false;
        try{
            String line;
            Reader reader = new InputStreamReader(new FileInputStream(file), encoding);
            BufferedReader br = new BufferedReader(reader);
            boolean foundFirst=false;
            while((line = br.readLine()) != null) {
                if (line.startsWith(UTF8_BOM)) {
                    line = line.substring(1);
                }
                if (line.length() > 0 && (line.startsWith(headerFields) || line.startsWith(headerFile))) {
                    //System.err.println(file.getName() + " -- " +line);
                    if (foundFirst) {
                        foundHeader = true;
                        return foundHeader;
                    } else {
                        foundFirst=true;
                    }
                }
            }
            reader.close();
        }catch(IOException e) {
            e.printStackTrace();
        }
        return foundHeader;
    }

    private static void getTokensIndex(String inputFile) throws IOException {
        tokensIndex.clear();
        BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(inputFile), encoding));
        String line;
        while((line = br.readLine()) != null) {
            if (line.startsWith(UTF8_BOM)) {
                line = line.substring(1);
            }
            if (line.startsWith(headerFields)) {
                line = line.replaceFirst(headerFields + "\\s*", "");
                String[] cols = line.split("\t");
                for(int i =0;i<cols.length;i++) {
                    //  System.err.println(cols[i]+","+ i);
                    //if(!tokensIndex.containsKey(cols[i]))
                    tokensIndex.put(cols[i], i);
                    //System.err.println(cols[i]+","+ i);
                    //TODO check here we have duplicate the key
                }
                break;		 //break do not want to continue
            }
        }
        // end getting the index of the column from the input file
        //System.out.println("We found "+tokensIndex.size()+" columns");
        br.close();

        //System.err.println("Columns in output ="+tokensIndex.size());
    }

    private static void checkProcessedModules() {
        ////check the active modules list, if we found it, add it to the processed Modules
        for (Object acttmp : activeModules.keySet()) {
            Module modtm = getModule(acttmp.toString());
            for (Module.Output.Field modOuttmp : modtm.getOutput().getField()) {
                if (tokensIndex.containsKey(modOuttmp.getName())) {
                    processedModules.put(acttmp.toString(), "FoundInFile");
                    ///actl.remove(); donot delete it, as we need it to filter on the final output file.
                }
            }

        }
    }

    private static void runActiveModuleList(String intermediateFile,String intermediateFileOutput,String inputFileMerged,String inputFile) throws IOException, InstantiationException, IllegalAccessException, ClassNotFoundException, NoSuchMethodException, SecurityException, IllegalArgumentException, InvocationTargetException {
        //System.out.println("running the rest of active modules");
        for (Object mt : activeModules.keySet()) {
            //System.err.println("######\n ModuleName="+mt.toString()+",val="+activeModules.get(mt)+"=Found int processed="+processedModules.containsKey(mt.toString()));
            //PrintHashtableKeySet(processedModules);
            if (activeModules.get(mt).equalsIgnoreCase("0") && !processedModules.containsKey(mt)) {
                //System.err.println("runActiveModuleList runModule(" + mt.toString() + " ...");
                runModule(mt.toString(), intermediateFile, intermediateFileOutput, inputFileMerged, inputFile);
                activeModules.put(mt, "1");

                //System.err.println(mt +", processedActiveModule");
                processedModules.put(mt, "processedActiveModule");
                //} else {
                // this message is just to say that this message has been run before and we have its output
                // System.out.println("The Module " + mt.toString() + " is required as active Module, and we found that it had been processed");
            }
        }
    }

    public static void sortandrunDependentModulesValue(String intermediateFile, String intermediateFileOutput, String inputFileMerged, String inputFile,LinkedHashMap<?, Integer> dependentmodules) throws IOException, InstantiationException, IllegalAccessException, ClassNotFoundException, NoSuchMethodException, SecurityException, IllegalArgumentException, InvocationTargetException{
        //Transfer as List and sort it
        //System.err.println("dependentmodules.size()"+ dependentmodules.size() + " " + dependentmodules.keySet());
        ArrayList<Map.Entry<?, Integer>> l = new ArrayList<Map.Entry<?, Integer>>(dependentmodules.entrySet());
        Collections.sort(l, new Comparator<Map.Entry<?, Integer>>() {

            public int compare(Map.Entry<?, Integer> o1, Map.Entry<?, Integer> o2) {
                return o1.getValue().compareTo(o2.getValue());
            }});
        for (Map.Entry<?, Integer> asd : l) {
            // dependentModules.put( asd.getKey(), asd.getValue());

            dependentmodules.remove(asd.getKey());
            //System.err.println(" sortandrunDependentModulesValue runModule(" + asd.getKey().toString() + " "+ asd.getValue() + "...");
            runModule(asd.getKey().toString(), intermediateFile, intermediateFileOutput, inputFileMerged, inputFile);
            processedModules.put(asd.getKey(), "processedDependent");
            if (activeModules.containsKey(asd.getKey())) {
                //activeModules.put(asd.getKey(), "1");
                // or simply i could remove the runned module from the active Module both of them are correct
                activeModules.remove(asd.getKey());
            }
        }

    }

    private static void runModule(String module,
                                  String intermediateFile,
                                  String intermediateFileOutput,
                                  String inputFileMerged,
                                  String inputFile) throws IOException, InstantiationException, IllegalAccessException, ClassNotFoundException, NoSuchMethodException, SecurityException, IllegalArgumentException, InvocationTargetException {
        //	run the warraper of the model
        //// here we have the correct module to run, so we need to run the module depending on its run information!!!
        Hashtable<String, Integer> copyThisTokens = new Hashtable<String, Integer>();
        LinkedHashMap<String,String> columnNameIntermediateOutput = new LinkedHashMap<String,String>();
        LinkedHashMap<String,String> headerNameIntermediateOutput = new LinkedHashMap<String,String>();
        Textpro.Modules.Module runM= getModule(module);
        boolean createHeader = false,readHeaderfromOutput=false;
        boolean noHeader=true;

        //if (DEBUG)
        //    System.err.println(">> " + module + ":" + " [readHeaderfromOutput: "+readHeaderfromOutput+", noInputFileHeader:" + noHeader+"]");
        if (VERBOSE) {
            System.err.print("# "+module +"... \t");
        }
        if (tokensIndex.size()>0) {
            noHeader = false;
            // this is to check if the header of the files contains the required header of the module , if not system exit!
            // because we couldnot prepare the column for this module for some reson
            // that the module which produce this column is not found in our system!
            // and here we are sure that what our system could produce as dependences should be served before reaching this point
            //System.err.println("Columns before running the module"+tokensIndex.size()+" eoken exist?="+tokensIndex.containsKey("token"));
            for (Textpro.Modules.Module.Input.Field field : runM.getInput().getField()) {
                if (!tokensIndex.containsKey(field.getName())) {
                    System.err.println("SYSTEM BREAK!");
                    System.err.println("The " + module + " module requires " + field.getName() + " column to be present in the input file."
                            + "\n Meanwhile, our system doesn't have the module to produce this column.");
                    System.exit(-1);
                } else {
                    copyThisTokens.put(field.getName(), tokensIndex.get(field.getName()));
                }
            }
        }
        // if no header = no tokensIndex and the module requires columns as input system exit error
        if (noHeader&&runM.getInput().getField().size()>0) {
            // print error and exist the system!
            System.err.println("The input file doesn't contain any columns, and the "+module+" module requires "+runM.getInput().getField().size()+" columns as Input!");
            System.exit(-1);
        }

        // System.out.println("Running the module: "+module);

        //String cmd = runM.getCmd().getValue()+" ";
        Textpro.Modules.Module.Params params = runM.getParams();
        String[] parms = null;
        if (params != null) {
            parms = new String[params.getParam().size()*2+2];
            Iterator<Textpro.Modules.Module.Params.Param> parml = params.getParam().iterator();
            int q=0;
            while(parml.hasNext()) {
                Textpro.Modules.Module.Params.Param parmtmp = parml.next();

                String ff="";
                String tt="";
                if (parmtmp.getValue().contains("$inputFile")) {
                    // cut the string from the start till reaching {$inputFile}
                    String gg = parmtmp.getValue().replaceAll("\\$inputFile", "");
                    gg = gg.replaceAll(" ", "");
                    tt = gg;
                    ff=intermediateFile;
                } else if (parmtmp.getValue().contains("$outputFile")) {
                    // cut the string from the start till reaching {$outputFile}
                    String gg = parmtmp.getValue().replaceAll("\\$outputFile", "");
                    gg = gg.replaceAll(" ", "");
                    tt= gg;
                    ff=intermediateFileOutput;
                } else if (parmtmp.getValue().contains("$language")) {
                    // cut the string from the start till reaching {$language}
                    String gg = parmtmp.getValue().replaceAll("\\$language", "");
                    gg = gg.replaceAll(" ", "");
                    tt= gg;
                    ff=language;
                } else if (parmtmp.getValue().contains("$kxparams")) {
                    // cut the string from the start till reaching {$language}
                    String gg = parmtmp.getValue().replaceAll("\\$kxparams", "");
                    gg = gg.replaceAll(" ", "");
                    tt= gg;
                    ff=kxparams;
                } else if (parmtmp.getValue().contains("$date")) {
                    // cut the string from the start till reaching {$date}
                    String gg = parmtmp.getValue().replaceAll("\\$date", "");
                    gg = gg.replaceAll(" ", "");
                    tt= gg;
                    ff=date;
                } else if (parmtmp.getValue().contains("$disable")) {
                    // cut the string from the start till reaching {$language}
                    String gg = parmtmp.getValue().replaceAll("\\$disable", "");
                    gg = gg.replaceAll(" ", "");
                    tt= gg;
                    ff=disable;
                } else if (parmtmp.getValue().contains("-html")) {
                    tt="-html";
                    ff="no";
                    if(htmlcleaner) {
                        ff = "yes";
                    }
                } else if (parmtmp.getValue().equalsIgnoreCase("-h i")) {
                    createHeader = true;
                    continue;/// check it is jump while reaching this point or not
                } else if (parmtmp.getValue().equalsIgnoreCase("-h o")) {
                    readHeaderfromOutput = true;
                    continue;/// check it is jump while reaching this point or not
                } else {
                    tt=parmtmp.getValue();
                    /*if (tt.contains(" ")) {
                       tt = tt.replaceAll("\\s+", " ");
                       ff = tt.substring(tt.indexOf(" ")+1);
                       tt = tt.substring(0,tt.indexOf(" "));
                   } */
                }
                //cmd+=tt+" ";
                if (tt.length()>0) {
                    parms[q]=tt;
                    q++;
                }

                if (ff != null && ff.length()>0) {
                    //if (ff.length()>0) {
                    parms[q]=ff;
                    q++;
                    //Arrays.fill(parms, ff);
                    //cmd+=ff+" ";
                }
            }
            if (disable.length() > 0) {
                parms[q] = "-d";
                parms[q+1] = disable;
            }
        }

        String className = runM.getCmd().getValue();

        prepareTmpFile(inputFile,intermediateFile,copyThisTokens,noHeader,createHeader);

        // Load the Class. Must use fully qualified name here!
        Class clazz = Class.forName(className);

        // I need an array as follows to describe the signature
        Class[] parameters = new Class[] {String[].class};
        //String[] parms = {"-f","itaA.txp","-l","italian"};
        // Now I can get a reference to the right constructor
        //Constructor constructor = clazz.getConstructor(parameters);
        /*	Method[] asd = clazz.getDeclaredMethods();
        for(int i=0;i<asd.length;i++)
            System.err.println("m="+asd[i]);
        */
        //Method method = clazz.getDeclaredMethod("init", parameters);
        //System.out.println(Arrays.asList(parms));
        Method method = clazz.getMethod("init", parameters);
        Object obj = clazz.newInstance();
        method.invoke(obj, new Object[]{parms});

        Class[] analyzeParameters = new Class[] {String.class, String.class};

        Method anameth = clazz.getMethod("analyze", analyzeParameters);
        long nowtime = 0;
        if (VERBOSE) {
            nowtime = new Date().getTime();
        }
        anameth.invoke(obj, intermediateFile, intermediateFileOutput);
        if (VERBOSE) {
            System.err.println((new Date().getTime() - nowtime) + "ms");
        }
        //System.err.println("size"+parms.length);
        // And I can use that Constructor to instantiate the class
        //Object o = constructor.newInstance(new Object[] {parms});

        //cmd += "-f "+intermediateFile+" -o "+intermediateFileOutput;
        // here we call the cmd to run the other system
        // finish executing the module
        // start merging process with the input original file
        // if (!noHeader) {
        // if there is a header, so filter the output of the execution of the module, and merge it with the original file
        String outFieldOrHeader;
        int iw=0;
        for (Module.Output.Field field : runM.getOutput().getField()) {
            outFieldOrHeader = field.getName();
            columnNameIntermediateOutput.put(outFieldOrHeader, String.valueOf(iw));
            if (DEBUG)
                System.err.println("colum: " + outFieldOrHeader);
            iw++;
        }
        for (Module.Output.Header header : runM.getOutput().getHeader()) {
            outFieldOrHeader = header.getName();
            if (DEBUG)
                System.err.println("header: " + outFieldOrHeader);
            headerNameIntermediateOutput.put(outFieldOrHeader, "1");
        }

        //System.err.println("MERGE " + module + " " +inputFileMerged);
        /// if there will be a header from the intermediate output, then restructure the
        mergeTheModuleOutWithOriginalFile(module, inputFileMerged, intermediateFileOutput,
                inputFile, columnNameIntermediateOutput, headerNameIntermediateOutput, readHeaderfromOutput, noHeader);
        //System.exit(0);
        ///delete the original file , create new file with the same as the original file, rename the final output of the module
        //delete the intermediate files in and output
        File file = new File(intermediateFile);
        if (DEBUG) {
            copyFile(file, intermediateFile + ".input."+module);
        }
        file.delete();

        File file2 = new File(intermediateFileOutput);
        if (DEBUG) {
            copyFile(file2, intermediateFileOutput+"."+module);
        } else
            file2.delete();

        // now replace the inputFileMerged file with the inputFile
        File file3 = new File(inputFileMerged);

        File file4 = new File(inputFile);
        file4.delete();
        //is a check point Files.copy(new File(inputFileMerged).toPath(), new File("xxx.input").toPath(),REPLACE_EXISTING);

        file3.renameTo(new File(inputFile));
        // finish the merging process

        // after finish everything, we need to update the tokensIndex list from the original file
        getTokensIndex(inputFile);
    }


    /*private static void mergeTheModuleOutWithOriginalFile(String moduleName,
			String inputFileMerged, String intermediateFileOutput,
			String inputFile,
			LinkedHashMap<String, String> columnNameIntermediateOutput,
			LinkedHashMap<String, String> headerNameIntermediateOutput,
			boolean readHeaderfromOutput, boolean noInputFileHeader)
			throws NumberFormatException, IOException {
		merge as = new merge(moduleName, inputFileMerged, intermediateFileOutput, inputFile, columnNameIntermediateOutput, headerNameIntermediateOutput, readHeaderfromOutput, noInputFileHeader);
	}
	*/
    private static void mergeTheModuleOutWithOriginalFile(String moduleName,
                                                          String inputFileMerged,
                                                          String intermediateFileOutput,
                                                          String inputFile,
                                                          LinkedHashMap<String, String> columnNameIntermediateOutput,
                                                          LinkedHashMap<String, String> headerNameIntermediateOutput,
                                                          boolean readHeaderfromOutput,
                                                          boolean noInputFileHeader) throws NumberFormatException, IOException {

        // i need to merge the output with the original output file but saving the original as it is, as
        // save the merge in new file called "original filename+.merged"
        FileInputStream in1 = new FileInputStream(inputFile);
        Reader reader1 = new InputStreamReader(in1, encoding);
        BufferedReader br1 = new BufferedReader(reader1);
        String line1;
        // just for check purposes , delete intermediateFileOutput="xxx.output";
        //System.err.println(moduleName+">> " + readHeaderfromOutput + " " +noInputFileHeader + " -- " +inputFile + " " + intermediateFileOutput);
        FileInputStream in2 = new FileInputStream(intermediateFileOutput);
        Reader reader2 = new InputStreamReader(in2, encoding);
        BufferedReader br2 = new BufferedReader(reader2);
        String line2;


        boolean startSynchronizing = false;
        File fileDir = new File(inputFileMerged);

        Writer out = new BufferedWriter(new OutputStreamWriter(
                new FileOutputStream(fileDir), encoding));
        ///here i need to check if the inputFile does not have header so it is a raw text,
        // and the output intermediateFile produce a header, then i copy it as it is as an outputof the merge.
        // otherwise, if the output intermediateFile doesnot has a header, i add a header in the output file, and copy the intermediateoutputfile as it is under that header!!!
        if (noInputFileHeader) {
            if (readHeaderfromOutput) {
                // rename the file of the intermediateFileOutput to be ".merged" file
                // and this one will be the output of the merge
                //because the inputFile has no header so it is a raw file, and i should read the header from the output of the module./
                // this conclude that the output of the module will be the output of the merge as it is!!!
                File interm = new File(intermediateFileOutput);
                interm.renameTo(fileDir);

            } else {
                // no inputFile header, and the output of the module will have noheader to consider
                // so i need to build the header from the output fields of the modules.xml related to that module,
                // then copy the output of the module as it is to the merged file,
                // no lines will be taken to consideration from the inputFile as it is a raw text
                String neHeader="";
                for (String s : columnNameIntermediateOutput.keySet()) {
                    if (!neHeader.equals(""))
                        neHeader += "\t";
                    neHeader += s;
                }
                //neHeader = neHeader.replaceAll("\t$","");
                out.append(headerFields).append(" ").append(neHeader).append("\n");
                String line3;
                while((line3 = br2.readLine()) != null) {
                    //System.err.println("line3: " + line3);
                    out.append(line3).append("\n");
                }
            }
        } else{
            //System.err.println("PASSO " + moduleName + " " +inputFileMerged);
            // otherwise
            //// start merging between the inputFile and the intermediateOutputFile
            String[] line2Cols;
            while((line1 = br1.readLine()) != null) {
                //System.err.println("line1: " + line1);
                if (line1.trim().equals("")) {
                    out.append("\n");
                    continue;
                }
                String outline;
                //System.err.println("PASSO "+ moduleName + " " + headerNameIntermediateOutput.size());

                if (startSynchronizing) {
                    // we need to check for temporary purpose if the two lines are synchronized correctly or not
                    String tmp="";
                    while((line2 = br2.readLine()) != null) {
                        if (!line2.trim().equals(""))
                            break;
                    }
                    if (line2 != null && !line2.trim().equals("")) {
                        line2Cols = line2.split("\t");
                        for (String tty : columnNameIntermediateOutput.keySet()) {
                            int pos = Integer.parseInt(columnNameIntermediateOutput.get(tty));
                            if (!tmp.equals(""))
                                tmp += "\t";
                            tmp += line2Cols[line2Cols.length - columnNameIntermediateOutput.size() + pos];
                        }
                        //tmp=tmp.replaceAll("\t$","");
                    }
                    outline = line1+"\t"+tmp;
                    //System.err.println(moduleName + " " +outline);

                } else if (line1.startsWith(headerFields)) {
                    startSynchronizing = true;
                    // add to the line the new header of columns
                    String tmp="";
                    for (String tty : columnNameIntermediateOutput.keySet()) {
                        if (!tmp.equals(""))
                            tmp += "\t";
                        tmp += tty;
                    }
                    outline=line1 +"\t"+tmp;


                    if (headerNameIntermediateOutput.size() > 0 || readHeaderfromOutput)  {
                        //start reading from the second file
                        while((line2 = br2.readLine()) != null) {
                            if (headerNameIntermediateOutput.containsKey(line2.replaceFirst("# ","").replaceFirst(":\\s*.+","").toLowerCase()))  {
                                out.append(line2).append("\n");
                            }
                            //////start restructuring the position depending on the intermediate output!
                            // this option will be activated if there will be in the parms fields a value of "-h o" which means:
                            // the module output will have a header
                            if (line2.startsWith(headerFields)) {
                                if (readHeaderfromOutput) {
                                    line2 = line2.replaceFirst(headerFields + "\\s*", "");
                                    String[] tokens = line2.split("\t");
                                    //columnNameIntermediateOutput.clear();
                                    for(int i=0;i<tokens.length;i++) {
                                        //System.err.println("("+tokens[i]+"), "+ String.valueOf(i));
                                        columnNameIntermediateOutput.put(tokens[i], String.valueOf(i));
                                    }
                                }
                                break;
                            }
                            //// end of restructure the position of the tokens depending on the intermediate output, overriding the pipeline position which given from the modules.xml
                        }
                    }

                } else {
                    outline = line1;
                }
                out.append(outline.trim()).append("\n");
            }// end getting the index of the column from the input file
        }// end merging between the inputFile and the intermediateOutputFile
        columnNameIntermediateOutput.clear();
        headerNameIntermediateOutput.clear();
        // copyThisTokens.clear();
        in1.close();
        in2.close();
        out.flush();
        out.close();

    }

    private static void prepareTmpFile(String inputFile,
                                       String intermediateFile, Hashtable copyThisTokens,boolean noHeader, boolean createHeader) throws IOException {
        BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(inputFile), encoding));
        Writer out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(intermediateFile), encoding));
        String line;
        if (noHeader) {
            // if the input is a raw text so for the tokenization, so keep it as it is!

            while((line = br.readLine()) != null) {
                out.append(line).append("\n");
            }
        } else {
            //here we should consider that the input file will have the header, so we need to delete that header from our output
            //and start reading the columns which we need for this module, getting their indexs from tokensindex list and give it as
            //output, here we are sure that there is a header and that header contains the required columns ,
            //so do not need to check that again!
            //there are some columns which we need to filter the file and save it!

            boolean finishHeader = false;

            if (createHeader) {
                String head ="";
                for(int y=0;y< tokensIndex.size();y++) {
                    for (Object coltmp : copyThisTokens.keySet()) {
                        //System.err.println(y+" "+copyThisTokens.get(coltmp)+" "+coltmp+" "+copyThisTokens.get(coltmp).toString().equals(y+""));
                        if (copyThisTokens.get(coltmp).toString().equals(String.valueOf(y))) {
                            if (!head.equals(""))
                                head += "\t";
                            head += coltmp;
                            break;
                            //}else{
                            //nothing just ignore this token as it's n't required to be copied for this module call.
                            //System.err.println("Here2!!!");
                        }
                    }
                }
                //out.append(head.replaceAll("\t$","")).append("\n");
                out.append(headerFields).append(" ").append(head).append("\n");

            }

            /*
          Iterator ks = copyThisTokens.keySet().iterator();
          while(ks.hasNext()){
        	 Object ktmp = ks.next();
        	  System.out.println(ktmp+"="+copyThisTokens.get(ktmp));
          }
          */

            StringBuilder outline= new StringBuilder();

            while((line = br.readLine()) != null) {
                if (finishHeader) {
                    //keep the empty lines
                    if (line.equals("")) {
                        out.append("\n");
                        continue;
                    }
                    ///here we have the bug of the new empty line
                    // if the original file has more than we need take what we need, otherwise print the line as it is.
                    String[] cols = line.split("\t");
                    if (cols.length >= copyThisTokens.size()) {
                        outline.setLength(0);
                        int copiedCols =0;
                        //TODO Fixed a bug that many line could be skipped while processing or giving a null value
                        int maxValue = getMaxValue(copyThisTokens);
                        for(int er=0;er<=maxValue&&line.length()>0;er++){
                            if(copyThisTokens.containsValue(er)){
                                //System.out.println(er+" of "+maxValue);
                                if (outline.length() != 0)
                                    outline.append("\t");
                                //System.err.println(line+"=="+outline.toString() + " SIZE:"+cols.length + " " +er);
                                if (er < cols.length) {
                                    outline.append(cols[er]);
                                    copiedCols +=1;
                                } else {
                                    System.err.println("DISALIGNMENT: "+line+" ["+outline.toString() + "] SIZE:"+cols.length + " " +er);
                                    outline.append("__ERR__");
                                }
                            }
                        }
                        //System.out.println("=======");
                       /* Try to fix new bug which is the unordered columns
                        * Iterator tobecopiedVals = copyThisTokens.values().iterator();
                        while(tobecopiedVals.hasNext()&&line.length()>0){
                        	Object valtmp = tobecopiedVals.next();
                                if (outline.length() != 0)
                                    outline.append("\t");
                        	//System.out.println(line+"=="+valtmp.toString());
                        	outline.append(cols[Integer.parseInt(valtmp.toString())]);
                        	copiedCols +=1;
                        }*/
                        if (copiedCols == copyThisTokens.size()) {
                            //TODO System.err.println(outline.toString());
                            //out.append(outline.replaceAll("\t$","")).append("\n");
                            out.append(outline.toString()).append("\n");
                        }


                        /*
                         * Very Old
                         *
                         * for(int i =0;i<cols.length;i++) {

                            if (copyThisTokens.containsValue(String.valueOf(i))) {
                        		copiedCols +=1;
                                if (outline.length() != 0)
                                    outline.append("\t");
                                outline.append(cols[i]);
                            }else{
                            	System.err.println("drop here!!!");
                            }

                            if (i == cols.length-1) {
                                //out.append(outline.replaceAll("\t$","")).append("\n");
                                out.append(outline.toString()).append("\n");
                            }
                        }*/
                        if (line.length() > 0 && copiedCols != copyThisTokens.size()) {
                            //System.err.println("Copied cols="+copiedCols + " != tobecopied=" +copyThisTokens.size() + " -- " +line);
                            System.err.println("Problem, tmp file prepared wrongly! as we do not have the same number of required cols in the input file!");
                        }
                    } else {
                        out.append(line.trim()).append("\n");
                    }
                } else if (line.startsWith(headerFields)) {
                    finishHeader = true;
                }
            }

        }
        //add a new line for CoNLL format, it needs an empty line at the end
        //out.append("\n");
        out.flush();
        out.close();

        br.close();

    }

    private static int getMaxValue(Hashtable<String,Integer> copyThisTokens) {
        int tmp=-1;
        for (Integer va : copyThisTokens.values()) {
            if (va > tmp)
                tmp = va;
        }
        return tmp;
    }


    public static Textpro.Modules.Module getModule(String modulename) {
        for (Module modt : myFile.getModules().getModule()) {
            if (modt.name.equalsIgnoreCase(modulename))
                return modt;
        }
        return null;
    }

    private static void prepareOrdering(LinkedHashMap dependentmodules) {
        for (Object modtmp : dependentmodules.keySet()) {
            int siz = getModuleDep(modtmp.toString());
            Object vv = dependentmodules.get(modtmp);
            //System.err.println("ORDER " + vv.toString());
            int val = Integer.parseInt(vv.toString()) + siz;
            dependentmodules.put(modtmp, val);
        }
    }


    private static int getModuleDep(String string) {
        ListIterator<Textpro.Modules.Module> modl = myFile.getModules().getModule().listIterator();
        int i=0;
        while(modl.hasNext()) {
            Textpro.Modules.Module modltmp = modl.next();
            if (modltmp.getName().equalsIgnoreCase(string)) {
                for (Module.Input.Field inte : modltmp.getInput().getField()) {
                    Module module = getModuleByOutputField(inte.getName());

                    //System.err.println("% " + module.name);
                    if (dependentModules.containsKey(module.name)) {
                        Object aa = dependentModules.get(module.name);
                        int val = Integer.parseInt(aa.toString()) - 1;
                        if (val < 0)
                            val = 0;
                        dependentModules.put(module.name, val);
                        //System.err.println("% " + module.name+" val= "+val);
                        i += 1;
                    }// if i added it to filter just the needed modules, not sure about it!!!
                }
            }
        }
        return i;
    }

    private static void PrintHashtableKeySet (Map inp) {
        for (Object tt : inp.keySet()) {
            System.err.println(tt + "= val =" + inp.get(tt));
        }
    }


    private static boolean getDependentModules(Map modules) {
        Iterator activeMl = modules.keySet().iterator();
        Textpro.Modules.Module module;
        while(activeMl.hasNext()) {
            String activMtmp = (String) activeMl.next();
            module = getModule(activMtmp);
            //System.err.println("# " + activMtmp + " ("+module.name+")");
            if (language != null && !module.getLanguages().contains(language)) {
                System.err.println("ERROR! The module " + module.name + " doesn't work for the language '" +
                        language +"'. Check the file conf/modules.xml and try again.");
                return false;
                //System.exit(0);
            }
            Textpro.Modules.Module.Input inpl = getInputFields(activMtmp);

            for (Module.Input.Field fieldin : inpl.getField()) {
                module = getModuleByOutputField(fieldin.name);


                if (module.name == null) {
                    System.err.println("ERROR! The field '" + fieldin.name + "' is not valid. Check the file conf/modules.xml and try again.");
                    System.exit(0);

                } else if (!dependentModules.containsKey(module.name) && !tokensIndex.containsKey(fieldin.getName())) {
                    ///2/10-2013 here Mohammed Fix the problem of getting dependincy without checking the input fields if there are !tokensIndex.containsKey(fieldin.getName())
                    //System.err.println("+ " +fieldin.name +" ("+module.name+") " + module.getLanguages());
                    dependentModules.put(module.name, 0);
                    getDependentModules((Map) dependentModules.clone());
                }
            }
        }
        return true;
    }



    private static Textpro.Modules.Module getModuleByOutputField (String outputfieldname) {
        for (Module moduletmp : myFile.getModules().module) {
            for (Module.Output.Field fieldtmp : moduletmp.output.field) {
                if (outputfieldname.equalsIgnoreCase(fieldtmp.getName())) {
                    return moduletmp;
                }
            }
        }
        return null;
    }


    private static Textpro.Modules.Module.Input getInputFields(String modName) {
        Textpro.Modules.Module.Input temp = new Textpro.Modules.Module.Input();
        for (Module modltmp : myFile.modules.module) {
            if (modltmp.name.equalsIgnoreCase(modName)) {
                temp = modltmp.input;
                return temp;
            }
        }
        return temp;
    }


    private static void getActiveModules(String input) {
        input = "+"+input+"+";
        for (Module moduletmp : myFile.getModules().module) {
            String moduleName = moduletmp.name;
            for (Module.Output.Field fieldtmp : moduletmp.getOutput().getField()) {
                if (input.contains("+" + fieldtmp.getName() + "+") && !tokensIndex.containsKey(fieldtmp.getName())) {
                    // System.err.println("-- " + moduleName);
                    activeModules.put(moduleName, "0");
                    break;
                }
            }
            if (!activeModules.containsKey(moduleName)) {
                for (Module.Output.Header headertmp : moduletmp.getOutput().getHeader()) {
                    if (input.contains("+" + headertmp.getName() + "+") && !tokensIndex.containsKey(headertmp.getName())) {
                        activeModules.put(moduleName, "0");
                    }
                }
            }

        }
    }


    public void readConfigFile() throws JAXBException, IOException{
        JAXBContext jc = JAXBContext.newInstance("eu.fbk.textpro.wrapper");
        Unmarshaller unmarshaller = jc.createUnmarshaller();

        URL url = getClass().getResource("/conf/modules.xml");
        File overwrittenFile = new File(wrapper.TEXTPROPATH + "/conf/modules.xml");

        if (overwrittenFile.exists() && overwrittenFile.isFile()){
            //System.out.println("Found1:"+wrapper.TEXTPROPATH + "/conf/modules.xml");
            myFile = (Textpro) unmarshaller.unmarshal(new InputStreamReader(new FileInputStream(wrapper.TEXTPROPATH + "/conf/modules.xml"), "UTF-8"));
        } else if (url != null){
            //System.out.println("Found2:"+getClass().getResource("/conf/modules.xml"));
            myFile = (Textpro) unmarshaller.unmarshal(new InputStreamReader(url.openStream(), "UTF-8"));
        } else{
            //System.out.println("F:"+wrapper.TEXTPROPATH + "/conf/modules.xml");
            //System.out.println("URL:"+getClass().getResource("/conf/modules.xml"));
            System.err.println("Error3: "+wrapper.TEXTPROPATH+"modules.xml file not found!");
        }
    }

    private static void prepareFinalOutput(String filename, String userModelsToRun, String inputTmp, Writer output, boolean outputXML, boolean bigFile) throws IOException {
        if (userModelsToRun != null) {
            userModelsToRun = "+"+userModelsToRun+"+";
        }
        File inputFile = new File(inputTmp);
        if (outputXML) {
            try {
                if (txpxml.writeXML(inputFile, output)) {
                    System.err.println("ERROR! An error occured to write XML output");
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            output.write(headerFile);
            if (filename.length() > 0)
                output.write(" " + filename);
            output.write("\n");

            output.write("# LANGUAGE: "+language+"\n");
            output.write("# TIMESTAMP: "+wrapper.getISODate()+"\n");

            Hashtable<String,String> copyTokenscol = new Hashtable<String,String>();

            String line;
            boolean startSynchronizing = false;
            BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(inputFile), encoding));
            while((line = br.readLine()) != null) {
                if (line.startsWith(headerFields) && !startSynchronizing) {
                    startSynchronizing = true;
                    line = line.replaceFirst(headerFields + "\\s+", "");
                    String[] cols = line.split("\\t");
                    String newHeader = headerFields + " ";
                    for(int i=0;i<cols.length;i++) {
                        if (userModelsToRun != null) {
                            if (userModelsToRun.contains("+"+cols[i]+"+")) {
                                //System.err.println(userModelsToRun + " -- " +cols[i]);
                                copyTokenscol.put(cols[i], String.valueOf(i));
                                newHeader +=cols[i]+"\t";
                            }
                        }
                    }
                    newHeader=newHeader.trim();
                    output.append(newHeader).append("\n");
                    //no colomns were set as output
                    if (headerFields.equals(newHeader))
                        break;
                } else if (startSynchronizing) {
                    // here we check if the line have the column or empty line IMP.
                    if (line.split("\\t").length>=copyTokenscol.size()) {
                        String[] tokcols = line.split("\\t");
                        String newLine="";
                        for(int i=0;i<tokcols.length;i++) {
                            if (copyTokenscol.contains(String.valueOf(i))) {
                                newLine += tokcols[i]+"\t";
                            }
                        }
                        newLine = newLine.trim();
                        output.append(newLine).append("\n");
                    } else {
                        output.append(line).append("\n");
                    }
                } else {
                    // other wise copy the above the header as it is
                    output.append(line).append("\n");
                }
            }
        }
    }

    static String readFileContent (InputStream instream) throws IOException {
        StringBuilder builder = new StringBuilder();
        String aux = "";

        BufferedReader reader = new BufferedReader (new InputStreamReader(instream));
        while ((aux = reader.readLine()) != null) {
            builder.append(aux+"\n");
        }

        return builder.toString();
    }

    static void writeFileContent (File output, String content) throws IOException {
        OutputStream outStream = new FileOutputStream(output);
        outStream.write(content.getBytes());
        outStream.close();
    }

    static void copyFile(File source, String target) {
        if (source == null || !source.exists())
            return;
        try{

            InputStream inStream = new FileInputStream(source);
            OutputStream outStream = new FileOutputStream(new File(target));

            byte[] buffer = new byte[1024];

            int length;
            while ((length = inStream.read(buffer)) > 0) {
                outStream.write(buffer, 0, length);
            }

            inStream.close();
            outStream.close();

            //System.out.println("File copied into " + target);
        }catch(IOException e) {
            e.printStackTrace();
        }
    }

    /** Runs executable command
     * @param command
     * @exception java.io.IOException
     */
    public static Process runCommand(String[] command) throws IOException {
        try {
            ProcessBuilder pb = new ProcessBuilder();
            Map<String, String> env = pb.environment();
            //set environment variable
            env.put("TEXTPRO", TEXTPROPATH);
            env.put("PATH", "/usr/bin:/bin:/usr/sbin:/sbin:/usr/local/bin:.:"+TEXTPROPATH);
            //env.put("LANG","it_IT.UTF-8");
            //System.err.println("! " +Arrays.asList(command));
            pb.command(command);
            return pb.start();

        } catch(Exception e) {
            throw new IOException("TextPro error: " + e.getMessage());
        }

    }

    private static void Usage() {
        System.err.println("Usage:\n   textpro.sh [OPTIONS] <STDIN or INPUT FILE or DIR>\n");
        System.err.println(commandLine.getUsage());
    }

    private static void getModulesVersionDetails() {
        Iterator<Module> ml = myFile.getModules().getModule().iterator();
        System.err.println("\nVersion of TextPro's modules:\n==========================");
        while(ml.hasNext()){
            Module mtmp = ml.next();
            System.err.println("- " +mtmp.getName()+": "+mtmp.getModulesVersionDetails());
        }
    }

    public static String getISODate() {
        return wrapper.df.format(new Date());
    }

    public static String getTextProPath () throws IOException {
        String temp = wrapper.class.getProtectionDomain().getCodeSource().getLocation().getPath();
        //System.err.println(">> "+temp);
        File textprotmp = new File(temp);
        if (textprotmp.isDirectory()) {
            textprotmp = textprotmp.getParentFile();
        } else {
            textprotmp = textprotmp.getParentFile().getParentFile();
        }

        if (!textprotmp.exists() || !textprotmp.isDirectory()) {
            System.err.println("Couldn't initialize TextPro path!\nMake sure that textproX.X.jar is inside lib directory!");
            System.exit(0);
        }
        return textprotmp.getCanonicalPath()+File.separator;
    }


    private static void loadLanguageModels () {
        //nl_nl tr_tr ar_me iw_il pt-PT_pt fr es de cn ru_ru uk us de_at it
        /*String[] langAbbr = {"ar_ae","ar_me","ar","ara","fa","fas","per",
                "pol","nl_nl","nl","nl_be","flemish","vl","tr_tr","turkiye",
                "iw_il","pt-pt_pt","pt","fr","fre",
                "es","spa","de","ger","cn","chi","zh",
                "ru_ru","ru","uk","us","en","eng","de_at","it","ita"};
        String[] langName = {"arabic","arabic","arabic","arabic","persian","persian","persian",
                "polish","dutch","dutch","dutch","dutch","dutch","turkish","turkish",
                "hebrew","portuguese","portuguese","french","french",
                "spanish","spanish","german","german","chinese","chinese","chinese",
                "russian","russian","english","english","english","english","german","italian","italian"};
        */
        String[] langAbbr = {"ita","eng"};
        String[] langName = {"italian","english"};

        if (langAbbr.length != langName.length) {
            System.err.println("Language detector error!");
            System.exit(0);
        }


        languageCat = new LanguageCategorization();
        try {
            languageCat.loadLanguages(getTextProPath()+File.separator+"language_models");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static boolean checkIsAvailableLanguage () {
        if (language != null && !language.startsWith("eng") &&
                !language.startsWith("ita")) {
            System.err.println("WARNING! The language is "+language+". The valid language values are 'eng' or 'ita'.");
            return false;
        }
        return true;
    }


    private static HashMap<String, Integer> sortByValue(Map<String, Integer> unsortMap, final boolean order)
    {

        List<Map.Entry<String, Integer>> list = new LinkedList<Map.Entry<String, Integer>>(unsortMap.entrySet());

        // Sorting the list based on values
        Collections.sort(list, new Comparator<Map.Entry<String, Integer>>()
        {
            public int compare(Map.Entry<String, Integer> o1,
                               Map.Entry<String, Integer> o2)
            {
                if (order)
                {
                    return o1.getValue().compareTo(o2.getValue());
                }
                else
                {
                    return o2.getValue().compareTo(o1.getValue());

                }
            }
        });

        // Maintaining insertion order with the help of LinkedList
        HashMap<String, Integer> sortedMap = new LinkedHashMap<String, Integer>();
        for (Map.Entry<String, Integer> entry : list)
        {
            sortedMap.put(entry.getKey(), entry.getValue());
        }

        return sortedMap;
    }

}
