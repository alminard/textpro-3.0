package eu.fbk.textpro.modules.timepro;

import java.io.File;
import java.io.IOException;
import java.util.Hashtable;
import java.util.LinkedList;

import eu.fbk.textpro.evaluation_metrics.ConfusionMatrix;
import eu.fbk.textpro.ml.MLMallet;
import eu.fbk.textpro.ml.learner;
import eu.fbk.textpro.wrapper.OBJECTDATA;

public class train {

	public static void main(String[] args) throws Exception {
		String lang = "FRE";
		String feature ="F:-4..4:0..6 T:-4..-1"; //ENG
		String fn = "Modules/TextPro-DataSet-Script/TimePro/ENG/DataSet/tempeval3-train-309741-ref.txp";
		String fntest = "Modules/TextPro-DataSet-Script/TimePro/ENG/DataSet/test-feat-ref.txp";
		String algo = "svm";
		
		if (lang.equals("ITA")) {
			algo = "svm";
			//feature="F:-2..2:0..6 T:-3..-1";
			//feature ="F:-1..-1:6,7 F:0..0:1,2,4,7 F:1..1:6,7 F:0:9..19 T:-2..-1";
			feature = "F:-4..4:0..6 T:-4..-1";
			//feature ="F:-2..2:1,2,3,5,6 F:0:9..19 T:-2..-1"; //FRE
			fn = "Modules/TextPro-DataSet-Script/TimePro/ITA/DataSet/training_timepro_ita_eventi_ref.txp";
			fntest = "Modules/TextPro-DataSet-Script/TimePro/ITA/DataSet/test_timepro_ita_eventi_ref.txp";
		}
		else if (lang.equals("FRE")) {
			feature ="F:-2..1:0..5 T:-3..-1";
			fn = "Modules/TextPro-DataSet-Script/TimePro/FRE/DataSet/training.txt";
			fntest = "Modules/TextPro-DataSet-Script/TimePro/FRE/DataSet/test.txt";
		}
		
		if(args.length>1){
			fn=args[0];
			fntest=args[1];
		}
		
		ProcessFeatures pf = new ProcessFeatures(lang);
		System.out.println("Process");
		Hashtable<String,Integer> copyThisTokens = new Hashtable<String,Integer> ();
		
		copyThisTokens.put("tokennorm",1);
		copyThisTokens.put("pos",2);
		copyThisTokens.put("lemma",3);
		copyThisTokens.put("chunk",4);
		
		if (lang.equals("ENG") || lang.equals("ITA")) {
			copyThisTokens.put("entity",5);
			copyThisTokens.put("Rule1",6);
			copyThisTokens.put("Rule2",7);
			copyThisTokens.put("tmx",8);
		}
		else {
			copyThisTokens.put("Rule1",5);
			copyThisTokens.put("Rule2",6);
			copyThisTokens.put("tmx",7);
		}
		
		
		OBJECTDATA f = new OBJECTDATA();
		f.readData(new File(fn), "utf8");
		
		System.out.println("Extract features");
		pf.run_extractFeatures(lang,f);
		f.saveInFile(fn+".processed","utf8",copyThisTokens, false);

		String trainset=fn+".processed";
		String model=fn+".model";
		String index=fn+".processed.key";
		learner lr= new learner();
		
		System.out.println("generate training....");
		lr.train(feature, trainset);   //Temporary to not take much time....
		
		System.out.println("Training....");
		
		MLMallet ml = new MLMallet();
		ml.train (trainset+".out", model, algo);
		//ml.trainCRF(trainset+".out", model, algo);
		
		System.out.println("Testing....");

		
		OBJECTDATA ftest = new OBJECTDATA();
		ftest.readData(new File(fntest), "utf8");
		pf.run_extractFeatures(lang,ftest);
		ftest.saveInFile(fntest+".processed","utf8",copyThisTokens, false);
		
		
		LinkedList<String> columValues = new LinkedList<String>();
		ml.classify (model,lr.test_evaluation( index,ftest.getFileLineByLineAsList(copyThisTokens,false)),false,lr,algo)		
			.map(e-> e.trim().length()>0? lr.getTag(index, Integer.parseInt(e)):e)
			.forEach(columValues::addLast);
		/*ml.classifyCRF (model,lr.test_evaluation( index,ftest.getFileLineByLineAsList(copyThisTokens,false)),false,lr,algo)		
		.map(e-> e.trim().length()>0? lr.getTag(index, Integer.parseInt(e)):e)
		.forEach(columValues::addLast);*/
		//System.out.println("columValues: "+columValues.size()+"   lines: "+ftest.linesCount);
		//System.out.println(columValues);
		ftest.addColumn("tmx_predicted", columValues);
		
		
		
		Hashtable<String,Integer> copyThisTokenss = new Hashtable<String,Integer> ();
		copyThisTokenss.put("tokennorm",1);
		copyThisTokenss.put("tmx",2);
		copyThisTokenss.put("tmx_predicted",3);
		//ftest.getFileLineByLineAsList(copyThisTokenss, true).stream().forEach(System.out::println);
		ftest.saveInFile(fntest+".final","utf8",copyThisTokenss, true);

		ConfusionMatrix confusionMatrix = new ConfusionMatrix();
		confusionMatrix = 	ConfusionMatrix.parseGoldPredictColumns(fntest+".final");
		System.out.println(confusionMatrix.printLabelPrecRecFm());
		System.out.println(confusionMatrix.getFMeasureForLabels());
		System.out.println(confusionMatrix.getFMeasureAvg());
	
	}

}
