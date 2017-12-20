package eu.fbk.textpro.modules.tagpro;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.Hashtable;
import java.util.LinkedList;

import eu.fbk.textpro.evaluation_metrics.ConfusionMatrix;
import eu.fbk.textpro.ml.MLMallet;
import eu.fbk.textpro.ml.learner;
import eu.fbk.textpro.modules.bin.M1Para;
import eu.fbk.textpro.toolbox.TEXTPROCONSTANT;
import eu.fbk.textpro.wrapper.OBJECTDATA;

public class train {

	public static void main(String[] args) throws Exception {
		String lang = "FRE";
		String feature ="F:0:1..8 F:-1:2,6,7,8 F:1:2,6,7,8 T:-2..-1"; //ENG
		String fn = "Modules/TextPro-DataSet-Script/Pos-Tagging/ENG/DataSet-ALM/BNC_train.txt";
		String fntest = "Modules/TextPro-DataSet-Script/Pos-Tagging/ENG/DataSet-ALM/BNC_test.txt";
		String algo = "svm";
		
		if (lang.equals("ITA")) {
			//feature ="F:-1..-1:6,7 F:0..0:1,2,4,7 F:1..1:6,7 F:0:9..19 T:-2..-1";
			feature ="F:0:1..8 F:-1:2,6,7,8 F:1:2,6,7,8 F:0:9..19 T:-2..-1";
			//feature ="F:-2..2:1,2,3,5,6 F:0:9..19 T:-2..-1"; //FRE
			fn = "Modules/TextPro-DataSet-Script/Pos-Tagging/ITA/DataSet-morpho/training.txt";
			fntest = "Modules/TextPro-DataSet-Script/Pos-Tagging/ITA/DataSet-morpho/test.txt";
		}
		else if (lang.equals("FRE")) {
			feature ="F:-2..2:1,2,3,5,6 F:0:9..19 T:-2..-1";
			fn = "Modules/TextPro-DataSet-Script/Pos-Tagging/FRA/DataSet-frm2/training-test.txt";
			fntest = "Modules/TextPro-DataSet-Script/Pos-Tagging/FRA/DataSet-frm2/test.txt";
		}
		
		if(args.length>1){
			fn=args[0];
			fntest=args[1];
		}
		
		ProcessFeatures pf = new ProcessFeatures();
		System.out.println("Process");
		Hashtable<String,Integer> copyThisTokens = new Hashtable<String,Integer> ();
		
		if (lang.equals("FRE") || lang.equals("ITA") ) {
			copyThisTokens.put("token",1);
			//copyThisTokens.put("full_morpho",2);
			copyThisTokens.put("tokennormaccent",2);
			copyThisTokens.put("tokentype",3);	
			copyThisTokens.put("pref2",4);
			copyThisTokens.put("pref3",5);
			copyThisTokens.put("pref4",6);
			copyThisTokens.put("suf2",7);
			copyThisTokens.put("suf3",8);
			copyThisTokens.put("suf4",9);
			copyThisTokens.put("v",10);
			copyThisTokens.put("nc",11);
			copyThisTokens.put("adj",12);
			copyThisTokens.put("adv",13);
			copyThisTokens.put("prep",14);
			copyThisTokens.put("det",15);
			copyThisTokens.put("cl",16);
			copyThisTokens.put("coo",17);
			copyThisTokens.put("pres",18);
			copyThisTokens.put("pron",19);
			copyThisTokens.put("np",20);
			//copyThisTokens.put("np2",22);
			//copyThisTokens.put("det2",23);
			copyThisTokens.put("pos",21);
		}
		else {
			copyThisTokens.put("token",1);
			copyThisTokens.put("tokennormaccent",2);
			copyThisTokens.put("tokentype",3);
			copyThisTokens.put("pref2",4);
			copyThisTokens.put("pref3",5);
			copyThisTokens.put("pref4",6);
			copyThisTokens.put("suf2",7);
			copyThisTokens.put("suf3",8);
			copyThisTokens.put("suf4",9);
			copyThisTokens.put("pos",10);
		}

		OBJECTDATA f = new OBJECTDATA();
		f.readData(new File(fn), "utf8");
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
		//ml.train (trainset+".out", model, "maxent");
		ml.train (trainset+".out", model, algo);
		//ml.trainCRF(trainset, model, algo);
		
		System.out.println("Testing....");

		
		OBJECTDATA ftest = new OBJECTDATA();
		ftest.readData(new File(fntest), "utf8");
		pf.run_extractFeatures(lang,ftest);
		ftest.saveInFile(fntest+".processed","utf8",copyThisTokens, false);
		
		
		
		LinkedList<String> columValues = new LinkedList<String>();
		ml.classify (model,lr.test_evaluation( index,ftest.getFileLineByLineAsList(copyThisTokens,false)),false,lr,algo)		
			.map(e-> e.trim().length()>0? lr.getTag(index, Integer.parseInt(e)):e)
			.forEach(columValues::addLast);
			//System.out.println("columValues: "+columValues.size()+"   lines: "+ftest.linesCount);
			//System.out.println(columValues);
			ftest.addColumn("pos_predicted", columValues);
			
			
			
			Hashtable<String,Integer> copyThisTokenss = new Hashtable<String,Integer> ();
			copyThisTokenss.put("token",1);
			copyThisTokenss.put("pos",2);
			copyThisTokenss.put("pos_predicted",3);
			//ftest.getFileLineByLineAsList(copyThisTokenss, true).stream().forEach(System.out::println);
			ftest.saveInFile(fntest+".final","utf8",copyThisTokenss, true);

			ConfusionMatrix confusionMatrix = new ConfusionMatrix();
			confusionMatrix = 	ConfusionMatrix.parseGoldPredictColumns(fntest+".final");
			System.out.println(confusionMatrix.printLabelPrecRecFm());
			System.out.println(confusionMatrix.getFMeasureForLabels());
			System.out.println(confusionMatrix.getFMeasureAvg());
			

		
//		try {
//			preProcess(fn);
//			preProcess(fntest);
//		} catch (Exception e) {
//			e.printStackTrace();
//		}
//		learner lr= new learner();
//		lr.train(feature, fn+".tmp");
//		lr.test(fn+".tmp"+".key", fntest+".tmp");		
//		
//		MLMallet ml = new MLMallet();
//		String[] arg = {fn+".tmp"+".out",fntest+".tmp"+".out",fn+".tmp"+".model"};
//		ml.main(arg);
	}

	private static void preProcess(String fn) throws UnsupportedEncodingException, FileNotFoundException, Exception {
		M1Para m1 = new M1Para();
		String content = m1.readFileString(new InputStreamReader(new FileInputStream(fn), M1Para.ENCODING), null);
		OBJECTDATA file = new OBJECTDATA();
		file.readData( content+System.lineSeparator());
		file.saveInFile(fn+".tmp", TEXTPROCONSTANT.encoding, false);
	}

}
