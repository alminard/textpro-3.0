package eu.fbk.textpro.api;

import eu.fbk.textpro.toolbox.TEXTPROVARIABLES;
import eu.fbk.textpro.wrapper.TextProPipeLine;

import java.io.File;
import java.io.IOException;
//import java.lang.ProcessBuilder.Redirect;
import java.lang.reflect.InvocationTargetException;
import java.util.Map;
import javax.xml.bind.JAXBException;


public class TextProGate {
    private module TokenPro;
    private module TagPro;
    private module MorphoPro;
    private module LemmaPro;
    private module ChunkPro = null;
    private module EntityPro;
    private module KX;
    private module GeoCoder;
    private module TimePro;
    private module DepParserPro;
    private module SentiPro;
    private String language = "eng";
    private String inputFile = "";
    private String outputFolder = "";
    private String outputFileName = "";
    private boolean overrideOutput = false;
    private boolean activeVerboseMode = false;
    private boolean htmlCleaner = false;
    private boolean disableTokenizer = false;
    private boolean disableSentenceSplitter = false;
    // Textpro myFile;
    static private String textproShellHomepath = "./";


    public void activeVerboseMood(){
        activeVerboseMode=true;
    }

    public void setTextProPath(String path) {
        System.err.println("TextPro path updating... ");

        if (!path.endsWith("textpro.sh")) {

            System.err.println("TextPro path should be complete! For instance: /home/TextPro/textpro.sh");
            System.exit(0);
        } else {
            //System.err.println("TextPro path:" + textproShellHomepath);

            textproShellHomepath = path;
        }
    }

    public void disableTokenizer() {
        disableTokenizer = true;
    }

    public void disableSentenceSplitter() {
        disableSentenceSplitter = true;
    }

    public void activeHtmlCleaner() {
        htmlCleaner = true;
    }

    public void overwriteOutput() {
        overrideOutput = true;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public void setInputFile(String inputFile) {
        this.inputFile = inputFile;
    }
    public void setOutputFileName(String outputFile) {
        this.outputFileName = outputFile;
    }

    public void setOutputFolder(String outputFolder) {
        this.outputFolder = outputFolder;
    }

    public static enum TokenProType {
        token, tokenid, tokenstart, tokenend, tokentype, tokennorm;
    }

    public static enum TagProType {
        pos;
    }

    public static enum MorphoProType {
        full_morpho;
    }

    public static enum LemmaProType {
        comp_morpho, lemma, wnpos;
    }

    public static enum ChunkProType {
        chunk;
    }

    public static enum EntityProType {
        entity;
    }

    public static enum KXType {
        keywords;
    }

    public static enum GeoCoderType {
        geoinfo;
    }

    public static enum TimeProType {
        timex;
    }

    public static enum DepParserProType {
        parserid, feats, head, deprel;
    }

    public static enum SentiProType {
        sentiment;
    }

    public module getTokenPro() {
        return TokenPro;
    }

    public TextProGate() throws JAXBException, IOException,
            NoSuchMethodException, SecurityException, IllegalAccessException,
            IllegalArgumentException, InvocationTargetException,
            CloneNotSupportedException {

        textproShellHomepath = TEXTPROVARIABLES.getTEXTPROPATH();
        //System.err.println("TextPro path:" + textproShellHomepath);

		/*
		 * readConfigFile(); Iterator<Module> modl =
		 * myFile.getModules().getModule().iterator(); while(modl.hasNext()){
		 * Module modtmp = modl.next(); //System.out.println(modtmp.getName());
		 * if(modtmp.getName().equals("TokenPro")){ TokenPro = new
		 * module(TokenProType.class); }else
		 * if(modtmp.getName().equals("TagPro")){ TagPro = new
		 * module(TagProType.class); }else
		 * if(modtmp.getName().equals("MorphoPro")){ MorphoPro = new
		 * module(MorphoProType.class); }else
		 * if(modtmp.getName().equals("LemmaPro")){ LemmaPro = new
		 * module(LemmaProType.class); }else
		 * if(modtmp.getName().equals("ChunkPro")){ ChunkPro = new
		 * module(ChunkProType.class); }else
		 * if(modtmp.getName().equals("EntityPro")){ EntityPro = new
		 * module(EntityProType.class); }else if(modtmp.getName().equals("KX")){
		 * KX = new module(KXType.class); }else
		 * if(modtmp.getName().equals("GeoCoder")){ GeoCoder = new
		 * module(GeoCoderType.class); }else
		 * if(modtmp.getName().equals("TimePro")){ TimePro = new
		 * module(TimeProType.class); }else
		 * if(modtmp.getName().equals("DepParserPro")){ DepParserPro = new
		 * module(DepParserProType.class); } }
		 */
        TokenPro = new module(TokenProType.class);
        TagPro = new module(TagProType.class);
        MorphoPro = new module(MorphoProType.class);
        LemmaPro = new module(LemmaProType.class);
        ChunkPro = new module(ChunkProType.class);
        EntityPro = new module(EntityProType.class);
        KX = new module(KXType.class);
        GeoCoder = new module(GeoCoderType.class);
        TimePro = new module(TimeProType.class);
        DepParserPro = new module(DepParserProType.class);
        SentiPro = new module(SentiProType.class);
    }

    /*
     * public void readConfigFile() throws JAXBException, IOException{
     * JAXBContext jc = JAXBContext.newInstance("eu.fbk.textpro.wrapper");
     * Unmarshaller unmarshaller = jc.createUnmarshaller();
     *
     * URL url = getClass().getResource("/conf/modules.xml"); File
     * overwrittenFile = new File(wrapper.TEXTPROPATH + "/conf/modules.xml");
     *
     * if(overwrittenFile.exists()&&overwrittenFile.isFile()){
     * //System.out.println("Found1:"+wrapper.TEXTPROPATH +
     * "/conf/modules.xml"); myFile = (Textpro) unmarshaller.unmarshal(new
     * InputStreamReader(new FileInputStream(wrapper.TEXTPROPATH +
     * "/conf/modules.xml"), "UTF-8")); }else if (url != null){
     * //System.out.println
     * ("Found2:"+getClass().getResource("/conf/modules.xml")); myFile =
     * (Textpro) unmarshaller.unmarshal(new InputStreamReader(url.openStream(),
     * "UTF-8")); } else{ //System.out.println("F:"+wrapper.TEXTPROPATH +
     * "/conf/modules.xml");
     * //System.out.println("URL:"+getClass().getResource("/conf/modules.xml"));
     * System.out.println("Error3: modules.xml file not found!");
     *
     * }
     *
     * }
     */
    public String run() {
        String run = "";
        String cOptions = "";
        String dOptions = "";
        String otherOptions = "";
        if (disableTokenizer) {
            dOptions += "token";
        }

        if (disableSentenceSplitter) {
            if (dOptions.length() > 0)
                dOptions += "+";
            dOptions += "sentence";
        }
        if (inputFile.length() < 2 || !(new File(inputFile).exists())) {
            System.err.println("Please initiate the input file or check its existance!("
                    + new File(inputFile).getAbsolutePath() + ")");
            System.exit(0);
        }

        if (!overrideOutput
                && ((outputFolder.length() == 0 && new File(inputFile + ".txp")
                .exists()) || (outputFolder.length() > 2 && (new File(
                outputFolder).exists())))) {
            System.err
                    .println("A file with the same output file is exist, please rename it or initiate the overwriting option '-y'!("
                            + ((outputFolder.length() == 0) ? new File(
                            inputFile).getAbsolutePath() + ".txp)" : ""
                            + new File(outputFolder).getAbsolutePath()));
            System.exit(0);
        }
        cOptions += getCOptions(TokenPro, TokenProType.values());
        cOptions += getCOptions(TagPro, TagProType.values());
        cOptions += getCOptions(MorphoPro, MorphoProType.values());
        cOptions += getCOptions(LemmaPro, LemmaProType.values());
        cOptions += getCOptions(ChunkPro, ChunkProType.values());
        cOptions += getCOptions(EntityPro, EntityProType.values());
        cOptions += getCOptions(KX, KXType.values());
        cOptions += getCOptions(GeoCoder, GeoCoderType.values());
        cOptions += getCOptions(TimePro, TimeProType.values());
        cOptions += getCOptions(DepParserPro, DepParserProType.values());
        cOptions += getCOptions(SentiPro, SentiProType.values());

        if (cOptions.length() > 2) {
            cOptions = "-c " + (cOptions + "\"").replace("+\"", "");
        } else {
            System.err.println("Please active some modules to run TextPro!");
            System.exit(0);
        }
        // run = "textpro.sh";
        run = " -l "
                + language.substring(0,3)
                + " "
                + (activeVerboseMode ? "-v " : "")
                + (overrideOutput ? "-y " : "")
                + (htmlCleaner ? "-html " : "")
                +(outputFileName.length()>0 ? "-n "+outputFileName+" ":"")
                + ((dOptions.length() > 0) ? "-d " + dOptions + " " : "")
                + cOptions
                + ((outputFolder.length() > 0) ? " -o "
                + new File(outputFolder).getAbsolutePath()+"/" : "")
                + " -i "
                + (new File(inputFile).getAbsolutePath());

        //System.out.println(">>> PARAM " + run);
        return run;
    }

    private String getCOptions(module mod, Object[] vals) {
        String temp = "";
        for (int i = 0; i < vals.length; i++) {
            if (mod.get(vals[i].toString()).getStatus()) {
                temp += mod.get(vals[i].toString()).getValue() + "+";
            }
        }
        return temp;
    }

    public void runTextPro() throws InstantiationException,
            IllegalAccessException, ClassNotFoundException,
            NoSuchMethodException, SecurityException, IllegalArgumentException,
            InvocationTargetException, JAXBException, IOException {
        String command = run();
        String[] args = command.split(" ");
        //for(int i=0;i<args.length;i++)
        // System.out.println(i+"="+args[i]);
        //eu.fbk.textpro.wrapper.wrapper.main(args);
        System.out.println(command);
        if (!callTextProBuilder(command)) {
            System.err.println("ERROR! TextPro processing failed.");
        }

    }

    public static void main(String[] args) throws InstantiationException,
            IllegalAccessException, ClassNotFoundException,
            NoSuchMethodException, SecurityException, IllegalArgumentException,
            InvocationTargetException, JAXBException, IOException,
            CloneNotSupportedException {
        TextProGate textpro = new TextProGate();
        textpro.overwriteOutput();
        textpro.activeHtmlCleaner();
        // textpro.setOutputFolder("trento_wiki_en.txt.bello");
        textpro.setInputFile("/Users/qwaider/Documents/workspace-textpro-java/textpro-java/textpro-dev/README");
        // textpro.disableTokenizer();
        // textpro.disableSentenceSplitter();
        // textpro.activeHtmlCleaner();
        // textpro.getTagPro().activateAll(TagProType.class);

        textpro.getTokenPro().activateAll(TokenProType.class);
        textpro.getChunkPro().activateAll(ChunkProType.class);
        // textpro.getTokenPro().deactive(TokenProType.token.name());
        // textpro.getTokenizer().active(TokenProType.tokenid.name());
        // textpro.run();
        textpro.runTextPro();

    }

    public static boolean callTextProBuilder(String par) throws IOException {
        //System.err.println("### " +TextProGate.textproShellHomepath + " " + par.toString());
        ProcessBuilder pb = new ProcessBuilder(
                TextProGate.textproShellHomepath, par.toString());
        //Map<String, String> env = pb.environment();
        // env.put("VAR1", "myValue");
        // env.remove("OTHERVAR");
        // env.put("VAR2", env.get("VAR1") + "suffix");
        String[] pars = par.split(" ");
        String path = null;
        for (int o = 0; o < pars.length; o++) {
            if (pars[o].equals("-i")) {
                path = pars[o + 1];
                break;
            }
        }
        //File filein = new File(path);
        //a.getParent();
        //a.exists();
        //a.isDirectory();

        //System.err.println("Processing... " +path+ " " + par.toString());
        //pb.directory(filein.getParentFile());
        //File log = new File(filein.getCanonicalPath() + ".log");
        //pb.redirectErrorStream(true);
        //pb.redirectOutput(Redirect.appendTo(log));
        try {
            Process p = pb.start();
            //assert pb.redirectInput() == Redirect.PIPE;
            //assert pb.redirectOutput().file() == log;
            //assert p.getInputStream().read() == -1;
            p.waitFor();
            return true;
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return false;
    }

    public module getTagPro() {
        return TagPro;
    }

    public module getMorphoPro() {
        return MorphoPro;
    }

    public module getLemmaPro() {
        return LemmaPro;
    }

    public module getChunkPro() {
        return ChunkPro;
    }

    public module getEntityPro() {
        return EntityPro;
    }

    public module getKX() {
        return KX;
    }

    public module getGeoCoder() {
        return GeoCoder;
    }

    public module getTimePro() {
        return TimePro;
    }

    public module getDepParserPro() {
        return DepParserPro;
    }

    public module getSentiPro() {
        return SentiPro;
    }

}
