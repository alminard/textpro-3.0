package eu.fbk.textpro.toolbox;

import java.io.File;

public class TEXTPROVARIABLES {
	private String tmpDir = "/tmp";
	private boolean detectLanguage = false;
	private String language = null; // "english";
	private String kxparams = "";
	private String dct = "";
	private boolean htmlcleaner = false;
	private String disable = "";
	private boolean DEBUG = false;
	private boolean VERBOSE = false;
	private boolean isBigFile = false;
	private boolean activeRecursiveDir = false;
	private boolean standardinput = false;
	private boolean colloquialLanguage = false;
	private static String TEXTPROPATH = "./";
	private File infile=null;
	private String outputDir;
	private String outputFileName;
	private boolean xml;
	private boolean y;
	private String intermediateFile;
	private String intermediateFileOutput;
	private String userModelsToRun= "token+pos+lemma";
	private String configPath;
	private static MYProperties prop;
	public static MYProperties getProp() {
		return prop;
	}
	public static void setProp(MYProperties propt) {
		prop = propt;
	}
	public String getConfigPath() {
		return configPath;
	}
	public void setConfigPath(String configPath) {
		this.configPath = configPath;
	}
	public boolean isStandardInput () {
		return standardinput;
	}
	public void setStandardInput(boolean stinput) {
		this.standardinput = stinput;
	}
	public boolean isActiveRecursiveDir() {
		return activeRecursiveDir;
	}
	public void setActiveRecursiveDir(boolean activeRecursiveDir) {
		this.activeRecursiveDir = activeRecursiveDir;
	}
	public boolean isBigFile() {
		return isBigFile;
	}
	public void setBigFile(boolean isBigFile) {
		this.isBigFile = isBigFile;
	}
	public String getUserModelsToRun() {
		return userModelsToRun;
	}
	public void setUserModelsToRun(String userModelsToRun) {
		this.userModelsToRun = userModelsToRun;
	}
	public String getIntermediateFile() {
		return intermediateFile;
	}
	public void setIntermediateFile(String intermediateFile) {
		this.intermediateFile = intermediateFile;
	}
	public String getIntermediateFileOutput() {
		return intermediateFileOutput;
	}
	public void setIntermediateFileOutput(String intermediateFileOutput) {
		this.intermediateFileOutput = intermediateFileOutput;
	}
	public boolean isXml() {
		return xml;
	}
	public void setXml(boolean xml) {
		this.xml = xml;
	}
	public boolean isY() {
		return y;
	}
	public void setY(boolean y) {
		this.y = y;
	}
	public String getOutputDir() {
		return outputDir;
	}
	public void setOutputDir(String outputDir) {
		this.outputDir = outputDir;
	}
	public String getOutputFileName() {
		return outputFileName;
	}
	public void setOutputFileName(String outputFileName) {
		this.outputFileName = outputFileName;
	}
	public File getInfile() {
		return infile;
	}
	public void setInfile(File infile) {
		this.infile = infile;
	}
	public String getTmpDir() {
		return tmpDir;
	}
	public void setTmpDir(String tmpDir) {
		this.tmpDir = tmpDir;
	}
	public boolean isDetectLanguage() {
		return detectLanguage;
	}
	public void setDetectLanguage(boolean detectLanguage) {
		this.detectLanguage = detectLanguage;
	}
	public String getLanguage() {
		return language;
	}
	public void setLanguage(String language) {
		this.language = language;
	}
	
	public String getDCT() {
		return dct;
	}
	public void setDCT(String dct) {
		this.dct = dct;
	}
	
	public boolean isColloquialLanguage () {
		return this.colloquialLanguage;
	}
	public void setColloquialLanguage (boolean typeLang) {
		this.colloquialLanguage = true;
	}
	
	public String getKxparams() {
		return kxparams;
	}
	public void setKxparams(String kxparams) {
		this.kxparams = kxparams;
	}
	public boolean isHtmlcleaner() {
		return htmlcleaner;
	}
	public void setHtmlcleaner(boolean htmlcleaner) {
		this.htmlcleaner = htmlcleaner;
	}
	public String getDisable() {
		return disable;
	}
	public void setDisable(String disable) {
		this.disable = disable;
	}
	public boolean isDEBUG() {
		return DEBUG;
	}
	public void setDEBUG(boolean dEBUG) {
		DEBUG = dEBUG;
	}
	public boolean isVERBOSE() {
		return VERBOSE;
	}
	public void setVERBOSE(boolean vERBOSE) {
		VERBOSE = vERBOSE;
	}
	public static String getTEXTPROPATH() {
		return TEXTPROPATH;
	}
	public static void setTEXTPROPATH(String tEXTPROPATH) {
		TEXTPROPATH = tEXTPROPATH;
	}
	
	
}
