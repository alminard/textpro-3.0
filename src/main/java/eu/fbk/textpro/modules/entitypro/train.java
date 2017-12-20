package eu.fbk.textpro.modules.entitypro;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Collection;
import java.util.Hashtable;
import java.util.LinkedList;

import javax.swing.plaf.synth.SynthSpinnerUI;

import eu.fbk.textpro.evaluation_metrics.ConfusionMatrix;
import eu.fbk.textpro.ml.MLMallet;
import eu.fbk.textpro.ml.learner;
import eu.fbk.textpro.wrapper.OBJECTDATA;

public class train {

	public static void main(String[] args) throws Exception {
		String lang = "ITA";
		String feature ="F:-2..1:3..13 T:-3..-1"; //ENG
		String fn = "Modules/TextPro-DataSet-Script/NE/ENG/Dataset-3cl/training.txt";
		String fntest = "Modules/TextPro-DataSet-Script/NE/ENG/Dataset-3cl/test.txt";
		String algo = "maxent";
		
		if(lang.equals("ITA")) {
			//feature ="F:-2..1:0,2,3,4 F:0:5..13 T:-3..-1"; 
			feature ="F:-1..1:0,6,3,12,13,11,7,8,9,10 T:-3..-1";
			fn = "Modules/TextPro-DataSet-Script/NE/ITA/Dataset-3cl/training.txt";
			fntest = "Modules/TextPro-DataSet-Script/NE/ITA/Dataset-3cl/test.txt";
			algo = "svm";
		}
		
		ProcessFeatures pf = new ProcessFeatures(lang);
		System.out.println("Process");
		
		Hashtable<String,Integer> copyThisTokens = new Hashtable<String,Integer> ();
		copyThisTokens.put("token",1);
		copyThisTokens.put("tokennorm",2);
		copyThisTokens.put("tokentype",3);
		copyThisTokens.put("pos",4);
		copyThisTokens.put("lemma",5);
		copyThisTokens.put("coarseGrainedPos",6);
		copyThisTokens.put("tokennormbis",7);
		copyThisTokens.put("GPE",8);
		copyThisTokens.put("ORG",9);
		copyThisTokens.put("LOC",10);
		copyThisTokens.put("PER",11);
		copyThisTokens.put("orth",12);
		copyThisTokens.put("sufBig",13);
		copyThisTokens.put("prefBig",14);
		copyThisTokens.put("entity",15);
		
		
		OBJECTDATA f = new OBJECTDATA();
		f.readData(new File(fn), "utf8");
		
		System.out.println("Extract features....");
		pf.run_extractFeatures(lang,f);
		f.saveInFile(fn+".processed","utf8",copyThisTokens, false);

		
		String trainset=fn+".processed";
		String model=fn+".model";
		String index=fn+".processed.key";
		
		System.out.println("Feature representation....");
		learner lr= new learner();
		lr.train(feature, trainset);
		
		System.out.println("Train....");
		MLMallet ml = new MLMallet();
		ml.train (trainset+".out", model, algo);
		//ml.trainCRF (trainset+".out", model, algo);
		
		System.out.println("Test....");
		OBJECTDATA ftest = new OBJECTDATA();
		ftest.readData(new File(fntest), "utf8");
		pf.run_extractFeatures(lang,ftest);
		
		LinkedList<String> columValues = new LinkedList<String>();
		
		
		ml.classify (model,lr.test_evaluation( index,ftest.getFileLineByLineAsList(copyThisTokens,false)),false,lr,algo)		
		.map(e-> e.trim().length()>0? lr.getTag(index, Integer.parseInt(e)):e)
		.forEach(columValues::addLast);
		/*ml.classifyCRF (model,lr.test( index,ftest.getFileLineByLineAsList(copyThisTokens,false)),false,lr,algo)		
		.map(e-> e.trim().length()>0? lr.getTag(index, Integer.parseInt(e)):e)
		.forEach(columValues::addLast);*/
		
		ftest.addColumn("entity_predicted", columValues);
		
		System.out.println("Evaluation...");
		Hashtable<String,Integer> copyThisTokenss = new Hashtable<String,Integer> ();
		copyThisTokenss.put("token",1);
		copyThisTokenss.put("entity",2);
		copyThisTokenss.put("entity_predicted",3);
		
		ftest.saveInFile(fntest+".final","utf8",copyThisTokenss, true);

		//String[] tags={"PER","LOC","MISC","ORG"};
		String[] tags={"PER","LOC","ORG"};
		ConfusionMatrix confusionMatrix = new ConfusionMatrix(tags);

		confusionMatrix.parseBIO(fntest+".final");
		System.out.println(confusionMatrix.printLabelPrecRecFmBIO());
		System.out.println(confusionMatrix.getFMeasureForLabelsBIO());
		System.out.println(confusionMatrix.getFMeasureAvgBIO());
	}

}
