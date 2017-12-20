package eu.fbk.textpro.modules.morphoprojava;

import java.io.File;
import java.util.Hashtable;

import eu.fbk.textpro.ml.MLMallet;
import eu.fbk.textpro.ml.learner;
import eu.fbk.textpro.modules.entitypro.ProcessFeatures;
import eu.fbk.textpro.wrapper.OBJECTDATA;

public class train {

	public static void main(String[] args) throws Exception {

		String lang = "ITA";
		String fn = "Modules/TextPro-DataSet-Script/MorphoPro/ITA/DataSet/training.txt";
		//MorphoAnalysis ma = new MorphoAnalysis(lang);

		System.out.println("Process");
		
		Hashtable<String,Integer> copyThisTokens = new Hashtable<String,Integer> ();
		copyThisTokens.put("token",1);
		copyThisTokens.put("pos",2);
		copyThisTokens.put("full_morpho",3);
		
		OBJECTDATA f = new OBJECTDATA();
		f.readData(new File(fn), "utf8");
		//ma.run_searchMorpho(lang,f);
		
		//f.getFileLineByLine(copyThisTokens)
		f.saveInFile(fn+".processed","utf8",copyThisTokens, true);
	}

}
