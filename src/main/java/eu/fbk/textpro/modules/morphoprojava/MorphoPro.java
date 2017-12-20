package eu.fbk.textpro.modules.morphoprojava;

import eu.fbk.textpro.TextProModuleInterface;
import eu.fbk.textpro.ml.MLMallet;
import eu.fbk.textpro.ml.learner;
import eu.fbk.textpro.modules.bin.M1Para;
import eu.fbk.textpro.toolbox.MYProperties;
import eu.fbk.textpro.toolbox.TEXTPROCONSTANT;
import eu.fbk.textpro.toolbox.TEXTPROVARIABLES;
import eu.fbk.textpro.toolbox.toolbox;
import eu.fbk.textpro.wrapper.OBJECTDATA;
import eu.fbk.textpro.wrapper.TextProPipeLine;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import javax.xml.bind.JAXBException;

/**
 * Created by IntelliJ IDEA.
 * User: cgirardi
 * Date: 3-apr-2013
 * Time: 20.20.44
 */
public class MorphoPro implements TextProModuleInterface {
    private String language = null;
    
    MorphoAnalysis ma;
    
    public void init(String[] params, MYProperties prop) {
       
        language = params[0];
        
        //ma = new MorphoAnalysis(language,prop);
        
    }
    
    public OBJECTDATA analyze(OBJECTDATA filein, toolbox tools)
			throws Exception {
    	
    	if (ma == null) {
    		ma = new MorphoAnalysis(tools.variables.getLanguage().substring(0,3),tools.variables.getProp(), tools);
    	}
    	
    	if (tools.variables.getLanguage().substring(0,3).equalsIgnoreCase("ita")){
    		return analyzeIT( filein,  tools);
    	}
    	else if (tools.variables.getLanguage().substring(0,3).equalsIgnoreCase("fre")) {
    		return analyzeFR(filein, tools);
    	}
    	else{
    		return analyzeEN( filein,  tools);
    	}
    }
    
    public OBJECTDATA analyzeEN(OBJECTDATA filein, toolbox tools)
			throws Exception {
    	
		ma.run_searchMorpho(filein, tools.variables.getProp(), tools);
				
		return filein;
	}
    
    public OBJECTDATA analyzeIT(OBJECTDATA filein, toolbox tools)
			throws Exception {

    	ma.run_searchMorpho(filein, tools.variables.getProp(), tools);		
		
		return filein;
	}
    
    public OBJECTDATA analyzeFR(OBJECTDATA filein, toolbox tools)
			throws Exception {

    	ma.run_searchMorpho(filein, tools.variables.getProp(), tools);		
		
		return filein;
	}

	@Override
	public void analyze(String filein, String fileout) throws IOException, JAXBException {
		// TODO Auto-generated method stub
		
	}
	
}
