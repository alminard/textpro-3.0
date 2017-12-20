package eu.fbk.textpro.modules.chunkpro;

import java.io.File;
import java.io.IOException;
import java.util.Hashtable;
import java.util.LinkedList;

import eu.fbk.textpro.evaluation_metrics.ConfusionMatrix;
import eu.fbk.textpro.ml.MLMallet;
import eu.fbk.textpro.ml.learner;
import eu.fbk.textpro.wrapper.OBJECTDATA;

public class train {

	public static void main(String[] args) throws ClassNotFoundException, IOException {
		String lang = "FRE";
		String feature ="F:-2..2:1 F:0:0 T:-2..-1";
		String fn = "Modules/TextPro-DataSet-Script/Chunking/ENG/DataSet-ALM/training.txt";
		String fntest = "Modules/TextPro-DataSet-Script/Chunking/ENG/DataSet-ALM/test.txt";
		String algo = "svm";
		
		if (lang.equals("ITA")) {
			feature ="F:-2..2:1 F:0:0 T:-2..-1";
			fn = "Modules/TextPro-DataSet-Script/Chunking/ITA/DataSet-newCorpus/training.txp";
			fntest = "Modules/TextPro-DataSet-Script/Chunking/ITA/DataSet-newCorpus/test.txp";
		}
		else if (lang.equals("FRE")) {
			algo = "svm";
			feature ="F:-2..2:0,1 T:-2..-1";
			fn = "Modules/TextPro-DataSet-Script/Chunking/FRA/DataSet/training-test.txt";
			fntest = "Modules/TextPro-DataSet-Script/Chunking/FRA/DataSet/test.txt";
		}
			
		if(args.length>1){
			fn=args[0];
			fntest=args[1];
		}

		String trainset=fn+".out";
		String model=fn+".model";
		String index=fn+".key";

		learner lr= new learner();
		System.out.println("generate training....");
		lr.train(feature, fn);   //Temporary to not take much time....
		
		MLMallet ml = new MLMallet();
		System.out.println("Training....");
		ml.train (trainset, model, algo);
		
		System.out.println("Testing....");

		
		OBJECTDATA ftest = new OBJECTDATA();
		ftest.readData(new File(fntest), "utf8");
		Hashtable<String,Integer> copyThisTokens = new Hashtable<String,Integer> ();
		copyThisTokens.put("token",1);
		copyThisTokens.put("pos",2);
		copyThisTokens.put("chunk",3);
		
		
		LinkedList<String> columValues = new LinkedList<String>();
		ml.classify (model,lr.test_evaluation( index,ftest.getFileLineByLineAsList(copyThisTokens,false)),false,lr,algo)		
			.map(e-> e.trim().length()>0? lr.getTag(index, Integer.parseInt(e)):e)
			.forEach(columValues::addLast);
			//System.out.println("columValues: "+columValues.size()+"   lines: "+ftest.linesCount);
			//System.out.println(columValues);
			ftest.addColumn("chunk_predicted", columValues);
			
			
		System.out.println("Evaluation....");	
			Hashtable<String,Integer> copyThisTokenss = new Hashtable<String,Integer> ();
			copyThisTokenss.put("token",1);
			copyThisTokenss.put("chunk",2);
			copyThisTokenss.put("chunk_predicted",3);
			//ftest.getFileLineByLineAsList(copyThisTokenss, true).stream().forEach(System.out::println);
			ftest.saveInFile(fntest+".final","utf8",copyThisTokenss, true);
			
			
			String[] tags={"ADJP", "ADVP", "CONJP", "INTJ", "LST", "NP", "PP", "PRT", "SBAR", "VP"}; //UCP
			if (lang.equals("ITA")) {
				tags = new String [] {"NP", "VP"};
			}
			else if (lang.equals("FRE")) {
				tags = new String [] {"ADJP", "NP", "ADVP", "PP", "VP"};
			}
			
			
			
			ConfusionMatrix confusionMatrix = new ConfusionMatrix(tags);

			confusionMatrix.parseBIO(fntest+".final");
			System.out.println(confusionMatrix.printLabelPrecRecFmBIO());
			System.out.println(confusionMatrix.getFMeasureForLabelsBIO());
			System.out.println(confusionMatrix.getFMeasureAvgBIO());
	}

}
