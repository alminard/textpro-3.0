package eu.fbk.textpro.ml;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Stream;

import ca.uwo.csd.ai.nlp.kernel.LinearKernel;
import ca.uwo.csd.ai.nlp.kernel.RBFKernel;
import ca.uwo.csd.ai.nlp.libsvm.svm_parameter;
import ca.uwo.csd.ai.nlp.mallet.libsvm.SVMClassifierTrainer;
import cc.mallet.classify.Classification;
import cc.mallet.classify.Classifier;
import cc.mallet.classify.ClassifierTrainer;
import cc.mallet.classify.DecisionTreeTrainer;
import cc.mallet.classify.MaxEntTrainer;
import cc.mallet.classify.NaiveBayesTrainer;
import cc.mallet.classify.Trial;
import cc.mallet.fst.CRF;
import cc.mallet.fst.CRFTrainerByL1LabelLikelihood;
import cc.mallet.fst.CRFTrainerByLabelLikelihood;
import cc.mallet.fst.CRFTrainerByStochasticGradient;
import cc.mallet.pipe.Csv2FeatureVector;
import cc.mallet.pipe.Pipe;
import cc.mallet.pipe.SerialPipes;
import cc.mallet.pipe.Target2Label;
import cc.mallet.types.FeatureVector;
import cc.mallet.types.FeatureVectorSequence;
import cc.mallet.types.Instance;
import cc.mallet.types.InstanceList;
import cc.mallet.types.Label;
import cc.mallet.types.LabelAlphabet;
import cc.mallet.types.LabelSequence;
import cc.mallet.types.Labeling;
import cc.mallet.types.Sequence;
import cc.mallet.util.FileUtils;

public class MLMallet{
	public Classifier classifier;

	public static void main(String[] args) throws IOException, ClassNotFoundException{
		
		// third argument: likelihood, L1, gradient
		//trainCRF(args[0], args[2], "likelihood");
	
		// third argument: evaluate or not
		//String [] predicOutput = classifyCRF(args[2], args[1], true);

		// third argument: naivebayes, svm, decisiontree or maxent
		MLMallet ml = new MLMallet();
		train (args[0], args[2], "maxent");
		ml.loadClassifier(new File(args[2]));
		learner ln=null; //here load the model and the parameters from learner.java
		Stream<String> predicOutput = ml.classify(args[2], args[1], true,ln,"maxent");
		
	}
	
	/*public Stream<String> train_classify_evaluate(String trainset,String testset, String model,learner ln){
		try {
			MLMallet ml = new MLMallet();
			train (trainset, model, "maxent");
			ml.loadClassifier(new File(model));
			Stream<String> predicOutput = ml.classify(model, testset, true,ln);
			return predicOutput;
		} catch (ClassNotFoundException e) {
			System.err.println("MLMALLET error: "+e.getMessage());
			e.printStackTrace();
		} catch (IOException e) {
			System.err.println("MLMALLET error, file not found: "+e.getMessage());
		}
		return null;
	}*/
	
	
	public Stream<String> classifyCRF (String fileModel, Collection<String> fileTest, boolean eval,learner ln, String algo) throws ClassNotFoundException, IOException{
		CRF crf = (CRF) FileUtils.readObject(new File(fileModel));
	      
        Pipe pipe= crf.getInputPipe();
				
		LinkedList<String> ret = new LinkedList<String>();
		for(String as:fileTest){
			if(as.trim().length()>0){
			//Add the FT features here
				boolean startEmpty=false;
				for (feature_node fe : ln.pr.tags_predicted_features_list) {
					for(Integer tp:fe.tokenpos){
						int index = ret.size()+(tp.intValue()) ;
						String fek="-1";
						//if(index<0||startEmpty||(index<ret.size()&&index>=0&&ret.get(index).trim().length()==0)){
						if(index<0){
							 fek = "FT:" + tp +":"+tp+ "__BOS__";
							 //startEmpty=true;
						}
						else{
							 fek = "FT:" + tp +":"+tp+ "_"+ret.get(index);
						}
						int key = -1;
						if (ln.pr.tags_predicted.containsKey(fek)) {
							key = ln.pr.tags_predicted.get(fek);
							as = as.trim() + (" "+key + ":1 ");
						}
						
						as=as.trim();
					}

					//System.out.println(as);
				}
				
				//InstanceList testInstances = new InstanceList(pipe);
				Instance inst =  prepareTestDataLabelSequenceString (pipe, as);
				//testInstances.addThruPipe((cc.mallet.types.Instance)getObjClassify(as));
				//Instance inst = testInstances.get(0);
		        Sequence input = (Sequence) inst.getData();
		        //System.out.println(input);
		        Sequence output = crf.transduce(input);
		        String predic = output.toString().substring(1);       
		        ret.add(predic);
				
			}else{
				ret.add("");
			}
		}
		return ret.stream();
	}
	
	public Stream<String> classifyCRF (String fileModel, Collection<String> fileTest, boolean eval) throws ClassNotFoundException, IOException{
		CRF crf = (CRF) FileUtils.readObject(new File(fileModel));
	      
        Pipe pipe= crf.getInputPipe(); 
       
        InstanceList testInstances = prepareTestDataLabelSequence (pipe, fileTest);
       
        Collection<String> ret = new ArrayList<String>();
       
        String [] predicOutput = new String [testInstances.size()];
        //System.out.println(fileTest.iterator().next());
        List<String> it = new ArrayList(fileTest);
       
        for (int i = 0; i < testInstances.size(); i++) {
            String as = it.get(i);
            //System.out.println(">>"+as);
            if (!as.equals("")){
	            Instance inst = testInstances.get(i);   
	            Sequence input = (Sequence) inst.getData();
	            //System.out.println(input);
	            Sequence output = crf.transduce(input);
	            String predic = output.toString().substring(1);       
	            predicOutput[i] = predic;
	            ret.add(predic);
	            //System.out.println(predic);
            }
            else{
                ret.add("");
            }
        }
           
        return ret.stream();
       
	}

	
	public String [] classifyCRF (String fileModel, String fileTest, boolean eval) throws IOException{
		CRF crf = (CRF) FileUtils.readObject(new File(fileModel));
		Pipe pipe = crf.getInputPipe();
		
		InstanceList testInstances = prepareTestDataLabelSequence (pipe, fileTest);
		
		int tp = 0;
	  	
	  	HashMap<String,Integer> tpByClass = new HashMap<String,Integer>();
	  	HashMap<String,Integer> fnByClass = new HashMap<String,Integer>();
	  	HashMap<String,Integer> fpByClass = new HashMap<String,Integer>();
	  	List<String> listClass = new ArrayList<String> ();
	  	
		
		String [] predicOutput = new String [testInstances.size()];
		for (int i = 0; i < testInstances.size(); i++) {
			Instance inst = testInstances.get(i);
			Sequence input = (Sequence) inst.getData();
			Sequence output = crf.transduce(input);
			String predic = output.toString().substring(1);		
			predicOutput[i] = predic;
		
			if(eval){
				Label labelref = ((LabelSequence)inst.getTarget()).getLabelAtPosition(0);
				if (labelref.toString().equals(predic)){
					tp ++;
					if (!tpByClass.containsKey(labelref.toString())){ tpByClass.put(labelref.toString(), 0); }
					tpByClass.put(labelref.toString(), tpByClass.get(labelref.toString())+1);
				}
				else{
					if (!fpByClass.containsKey(predic)){ fpByClass.put(predic, 0); }
					if (!fnByClass.containsKey(labelref.toString())){ fnByClass.put(labelref.toString(), 0); }
					fpByClass.put(predic, fpByClass.get(predic)+1);
					fnByClass.put(labelref.toString(), fnByClass.get(labelref.toString())+1);
				}

				if (!listClass.contains(labelref.toString())){
					listClass.add(labelref.toString());
				}
			}
			
		}
	
		if(eval){
			System.out.println(tp + "/" + testInstances.size());
			double accuracy = (double) tp / (double) testInstances.size();
			System.out.println(accuracy);
			
			for (String key : listClass){
				if(tpByClass.containsKey(key)){
					if(!fnByClass.containsKey(key)){
						fnByClass.put(key,0);
					}
					if(!fpByClass.containsKey(key)){
						fpByClass.put(key,0);
					}
	
					double recall = (double) tpByClass.get(key) / ((double) tpByClass.get(key) + (double) fnByClass.get(key));
					double precision = (double) tpByClass.get(key) / ((double) tpByClass.get(key) + (double) fpByClass.get(key));
					double f1 = (2 * precision * recall) / (recall + precision) ;
					System.out.println(key+ " : "+"recall="+recall+" precision="+precision+" f1="+f1);
				}
				else{
					System.out.println(key+ " : "+"recall=0"+" precision=0"+" f1=0");
				}
			}
		}
			
		return predicOutput;
	}
	
	public static InstanceList prepareTrainingDataLabelSequence (String fileTrain) throws IOException {
		//CsvIterator reader = new CsvIterator (new FileReader(fileTrain), "(\\w+)\\s+(\\d+)\\s+(.*)",3,2,1);
		
		Pipe pipe = new SerialPipes(new Pipe[] { new Target2Label(), new Csv2FeatureVector() });
		InstanceList trainInstances = new InstanceList(pipe);
		//trainInstances.addThruPipe(reader);
		trainInstances.addThruPipe( Files.lines(new File(fileTrain).toPath())
				.filter(e->e.trim().length()>0)
				.map((e)->
				(cc.mallet.types.Instance)getObj(e)
				).iterator()
				);
			
		LabelAlphabet dict = new LabelAlphabet ();
		Object[] labels = pipe.getTargetAlphabet().toArray();
		for (int i=0; i<labels.length; i++){
			dict.lookupIndex(labels[i],true);
		}
		
		for (int i=0; i<trainInstances.size(); i++){
			Instance inst = trainInstances.get(i);
	
			
			FeatureVector[] featvectab = new FeatureVector [1];
			featvectab[0] = (FeatureVector) inst.getData();
			FeatureVectorSequence featvec = new FeatureVectorSequence(featvectab);
			inst.unLock();
			inst.setData(featvec);
			
			Label [] labelTab = new Label [1];
			labelTab[0] = dict.lookupLabel(inst.getTarget().toString());
				
			LabelSequence tags = new LabelSequence(labelTab);
			inst.setTarget(tags);
			
		}
		
		return trainInstances;
	}
	
	public static InstanceList prepareTrainingData (String fileTrain) throws IOException {
		//CsvIterator reader = new CsvIterator (new FileReader(fileTrain), "(\\w+)\\s+(\\d+)\\s+(.*)",3,2,1);
		
		Pipe pipe = new SerialPipes(new Pipe[] { new Target2Label(), new Csv2FeatureVector() });
		InstanceList trainInstances = new InstanceList(pipe);
		
		trainInstances.addThruPipe( Files.lines(new File(fileTrain).toPath())
				.filter(e->e.trim().length()>0)
				.map((e)->
				(cc.mallet.types.Instance)getObj(e)
				).iterator()
				);
		
		return trainInstances;
	}
	
	private static cc.mallet.types.Instance getObj(String e) {
		
		return new Instance( e.substring(e.indexOf(" ")).trim(),e.substring(0,e.indexOf(" ")).trim(),null,null);
	}
	private static cc.mallet.types.Instance getObjClassify(String e) {
		return new Instance( e.trim(),"1","","");
	}
	public static void train (String fileTrain, String fileOut, String algo) throws IOException{
		ClassifierTrainer trainer = null; 
		
		if(algo.equals("naivebayes")){
			trainer = new NaiveBayesTrainer();
		}
		else if(algo.equals("decisiontree")){
			trainer = new DecisionTreeTrainer();
		}
		else if(algo.equals("svm")){
			trainer = new SVMClassifierTrainer(new LinearKernel());
		}
		else{
			trainer = new MaxEntTrainer(); 
		}
		
		InstanceList trainInstances = prepareTrainingData (fileTrain);
		Pipe pipe = trainInstances.getPipe();
		
		Classifier classifier = trainer.train(trainInstances);
		saveClassifier(classifier, new File(fileOut));
	        
	}
	
	public static void saveClassifier(Classifier classifier, File serializedFile)
        throws IOException {

        // The standard method for saving classifiers in                                                   
        //  Mallet is through Java serialization. Here we                                                  
        //  write the classifier object to the specified file.                                             

        ObjectOutputStream oos =
            new ObjectOutputStream(new FileOutputStream (serializedFile));
        oos.writeObject (classifier);
        oos.close();
	}
	
	 public Classifier loadClassifier(File serializedFile)
        throws FileNotFoundException, IOException, ClassNotFoundException {

        // The standard way to save classifiers and Mallet data                                            
        //  for repeated use is through Java serialization.                                                
        // Here we load a serialized classifier from a file.                                               

        Classifier classifier;

        ObjectInputStream ois =
            new ObjectInputStream (new FileInputStream (serializedFile));
        classifier = (Classifier) ois.readObject();
        ois.close();

        return classifier;
	 }
	
	 public static InstanceList prepareTestData (Pipe pipe, String fileTest) throws IOException{
			//CsvIterator readerTest = new CsvIterator (new FileReader(fileTest), "(\\w+)\\s+(\\d+)\\s+(.*)", 3,2,1);
			
			InstanceList testInstances = new InstanceList(pipe);
			//testInstances.addThruPipe(readerTest);
			testInstances.addThruPipe( Files.lines(new File(fileTest).toPath())
					//.filter(e->e.trim().length()>0)
					.map((e)->
					(cc.mallet.types.Instance)getObjClassify(e)
					).iterator()
					);
			return testInstances;
	 }
	 
	 
	 public static Instance prepareTestDataLabelSequenceString (Pipe pipe, String vector) throws IOException{
			//CsvIterator readerTest = new CsvIterator (new FileReader(fileTest), "(\\w+)\\s+(\\d+)\\s+(.*)", 3,2,1);
			
			InstanceList testInstances = new InstanceList(pipe);
			//testInstances.addThruPipe(readerTest);
			testInstances.addThruPipe((cc.mallet.types.Instance)getObjClassify(vector));
			LabelAlphabet dict = new LabelAlphabet();
			
			//for (int i=0; i<testInstances.size(); i++){
			Instance inst = testInstances.get(0);

			FeatureVector[] featvectab = new FeatureVector [1];
			featvectab[0] = (FeatureVector) inst.getData();
			FeatureVectorSequence featvec = new FeatureVectorSequence(featvectab);
			inst.unLock();
			inst.setData(featvec);
			
			Label [] labelTab = new Label [1];
			labelTab[0] = dict.lookupLabel(inst.getTarget().toString());
				
			LabelSequence tags = new LabelSequence(labelTab);
			inst.setTarget(tags);
			
			return inst;
	 }
	 
	 public static InstanceList prepareTestDataLabelSequence (Pipe pipe, String fileTest) throws IOException{
			//CsvIterator readerTest = new CsvIterator (new FileReader(fileTest), "(\\w+)\\s+(\\d+)\\s+(.*)", 3,2,1);
			
			InstanceList testInstances = new InstanceList(pipe);
			//testInstances.addThruPipe(readerTest);
			testInstances.addThruPipe( Files.lines(new File(fileTest).toPath())
				//	.filter(e->e.trim().length()>0)
					.map((e)->
					(cc.mallet.types.Instance)getObjClassify(e)
					).iterator()
					);
			LabelAlphabet dict = new LabelAlphabet();
			
			for (int i=0; i<testInstances.size(); i++){
				Instance inst = testInstances.get(i);

				FeatureVector[] featvectab = new FeatureVector [1];
				featvectab[0] = (FeatureVector) inst.getData();
				FeatureVectorSequence featvec = new FeatureVectorSequence(featvectab);
				inst.unLock();
				inst.setData(featvec);
				
				Label [] labelTab = new Label [1];
				labelTab[0] = dict.lookupLabel(inst.getTarget().toString());
					
				LabelSequence tags = new LabelSequence(labelTab);
				inst.setTarget(tags);
			}
			
			return testInstances;
	 }
	 
	 public static InstanceList prepareTestDataLabelSequence (Pipe pipe, Collection<String> fileTest) throws IOException{
         //CsvIterator readerTest = new CsvIterator (new FileReader(fileTest), "(\\w+)\\s+(\\d+)\\s+(.*)", 3,2,1);
        
         InstanceList testInstances = new InstanceList(pipe);
         //testInstances.addThruPipe(readerTest);
         testInstances.addThruPipe(fileTest.stream()
             //    .filter(e->e.trim().length()>0)
                 .map((e)->
                 (cc.mallet.types.Instance)getObjClassify(e)
                 ).iterator()
                 );
         LabelAlphabet dict = new LabelAlphabet();
        
         for (int i=0; i<testInstances.size(); i++){
             Instance inst = testInstances.get(i);

             FeatureVector[] featvectab = new FeatureVector [1];
             featvectab[0] = (FeatureVector) inst.getData();
             FeatureVectorSequence featvec = new FeatureVectorSequence(featvectab);
             inst.unLock();
             inst.setData(featvec);
            
             Label [] labelTab = new Label [1];
             labelTab[0] = dict.lookupLabel(inst.getTarget().toString());
                
             LabelSequence tags = new LabelSequence(labelTab);
             inst.setTarget(tags);
         }
        
         return testInstances;
  }
	 

	 
	public Stream<String> classify (String fileModel, String fileTest, boolean eval,learner ln, String algo) throws ClassNotFoundException, IOException{
		 classifier = loadClassifier(new File(fileModel));
		Pipe pipe = classifier.getInstancePipe();
		
		InstanceList testInstances = prepareTestData(pipe, fileTest);
		
		return classify ( fileModel,  Files.readAllLines(new File(fileTest).toPath()),  eval,ln, algo);
	}
	
	public Stream<String> classify_old (String fileModel, Collection<String> fileTest, boolean eval) throws ClassNotFoundException, IOException{
		if(classifier == null)
		 classifier = loadClassifier(new File(fileModel));
		Pipe pipe = classifier.getInstancePipe();
		
		//testInstances.addThruPipe(readerTest);
	//	testInstances.addThruPipe( 
//		fileTest.forEach(e-> System.out.println("="+e+"="+(e.length()==0)));
//				fileTest
//			//	.filter(e->e.trim().length()>0)
//				.map((e)->
//				(cc.mallet.types.Instance)getObjClassify(e)
//				)
//				.forEach(testInstances::addThruPipe);
//				
		InstanceList testInstances = new InstanceList(pipe);

		Collection<String> ret = new ArrayList<String>();
			for(String as:fileTest){
				//System.out.println("="+as+"=");
				if(as.trim().length()>0){
					testInstances.addThruPipe(getObjClassify(as));
				}else{
					if(testInstances.size()>0){
					Trial testTrial = new Trial(classifier,testInstances);
					 testTrial.stream()
			 		.map(e-> e.getLabeling().getLabelAtRank(0).toString())
					.forEach(ret::add);
					 testInstances = new InstanceList(pipe);
					 if(eval){
							printResultsClassification (testTrial, classifier.getLabelAlphabet());
						}
					}
					ret.add("");
					
				}
			}
			if(testInstances.size()>0){
				Trial testTrial = new Trial(classifier,testInstances);
				testTrial.stream()
		 		.map(e-> e.getLabeling().getLabelAtRank(0).toString())
				.forEach(ret::add);
				}
	//	Trial testTrial = new Trial(classifier,testInstances);
		 
//		
//		String [] predic = new String [testTrial.size()];
//		for (int i = 0; i < testTrial.size(); i++) {
//			Labeling labeling = testTrial.get(i).getLabeling();
//			predic[i] = labeling.getLabelAtRank(0).toString();
//		}
		
//		if(eval){
//			printResultsClassification (testTrial, classifier.getLabelAlphabet());
//		}
//		return testTrial.stream()
//		 		.map(e-> e.getLabeling().getLabelAtRank(0).toString())
//				.collect(Collectors.toList()).stream();
			return ret.stream();
	}
	
	public Stream<String> classify (String fileModel, Collection<String> fileTest, boolean eval,learner ln, String algo) throws ClassNotFoundException, IOException{
		if(classifier == null) {
			classifier = loadClassifier(new File(fileModel));	
		}
		int i = 0;
		LinkedList<String> ret = new LinkedList<String>();
		InstanceList testInstances = new InstanceList(classifier.getInstancePipe());
		
		Classification classified = null;
		
		for(String as:fileTest){
			if(as.trim().length()>0){
			//Add the FT features here
				boolean startEmpty=false;
				for (feature_node fe : ln.pr.tags_predicted_features_list) {
					for(Integer tp:fe.tokenpos){
						int index = ret.size()+(tp.intValue()) ;
						String fek="-1";
						//if(index<0||startEmpty||(index<ret.size()&&index>=0&&ret.get(index).trim().length()==0)){
						if(index<0){
							 fek = "FT:" + tp +":"+tp+ "__BOS__";
							 //startEmpty=true;
						}
						else{
							 fek = "FT:" + tp +":"+tp+ "_"+ret.get(index);
						}
						int key = -1;
						if (ln.pr.tags_predicted.containsKey(fek)) {
							key = ln.pr.tags_predicted.get(fek);
							as = as.trim() + (" "+key + ":1 ");
						}
						
						as=as.trim();
					}

					//System.out.println(as);
				}
				
				//Instance readyToClassify = getObj(as);

				//Instance readyToClassify = getObjClassify(as);
				
				//System.out.println(as);
				
				if(algo.equals("svm")) {
					testInstances.addThruPipe(getObjClassify(as));
					
					classified = classifier.classify(testInstances.get(i));
					//System.out.println(classified.getLabeling().getBestIndex());
					ret.add(classified.getLabeling().getBestLabel().toString());

					i++;
				}
				else {
					classified = classifier.classify(as);
					//System.out.println(classified.getLabeling().getBestIndex());
					ret.add(classified.getLabeling().getBestLabel().toString());
				}
				
			}else{
				ret.add("");
			}
		}
			return ret.stream();
	}
	
	
	public static void printResultsClassification(Trial trial, LabelAlphabet labelalph){
		System.out.println("Accuracy: " + trial.getAccuracy());
   	 	 
		for(int i=0; i<labelalph.size(); i++){
			System.out.println("F1 ("+labelalph.lookupLabel(i)+"): "+ trial.getF1(i));
   	 	}
	}
	
	public static void trainCRF (String fileTrain, String fileOut, String algo) throws IOException {
      // setup:
      //    CRF (model) and the state machine
      //    CRFOptimizableBy* objects (terms in the objective function)
      //    CRF trainer
      //    evaluator and writer

		InstanceList trainInstances = prepareTrainingDataLabelSequence (fileTrain);
		Pipe pipe = trainInstances.getPipe();
	
		// model
		CRF crf = new CRF(pipe,pipe);
/*		CRF crf = new CRF(trainingData.getDataAlphabet(),
                        trainingData.getTargetAlphabet());
 */     

		crf.addStatesForThreeQuarterLabelsConnectedAsIn(trainInstances);
		crf.addStartState();
      
		if(algo.equals("L1")){
			//L1-regularization
			CRFTrainerByL1LabelLikelihood trainer = 
				new CRFTrainerByL1LabelLikelihood(crf);
			trainer.setGaussianPriorVariance(10.0);
			
			trainer.train(trainInstances,100);
		}
		else if(algo.equals("gradient")){
			//stochastic gradient
			CRFTrainerByStochasticGradient trainer = new CRFTrainerByStochasticGradient(crf, 0.000001);
			
			trainer.train(trainInstances,100);
		}
		else{
			CRFTrainerByLabelLikelihood trainer = 
				new CRFTrainerByLabelLikelihood(crf);
			trainer.setGaussianPriorVariance(10.0);
			
			trainer.train(trainInstances,100);
		}

		// save the trained model 
		FileOutputStream fos = new FileOutputStream(fileOut);
		ObjectOutputStream oos = new ObjectOutputStream(fos);
		oos.writeObject(crf);
	
	}
	
    public static void runNB (InstanceList trainingData, InstanceList testingData) throws IOException {
      
      ClassifierTrainer trainer = new NaiveBayesTrainer();
      Classifier classifier = trainer.train(trainingData);
      Trial trainTrial = new Trial(classifier,trainingData);
      Trial testTrial = new Trial(classifier,testingData);
      classifier.print();
      printTrialClassification(testTrial);

    }

    public static void printClassification (Trial trial){
		System.out.println("Predictions:");
		for (int i = 0; i < trial.size(); i++) {
			Labeling labeling = trial.get(i).getLabeling();
			System.out.println(labeling.getLabelAtRank(0));
		}
	}
    
    public static void runME (InstanceList trainingData, InstanceList testingData) throws IOException {
        
        ClassifierTrainer trainer = new MaxEntTrainer();
        Classifier classifier = trainer.train(trainingData);
        Trial trainTrial = new Trial(classifier,trainingData);
        Trial testTrial = new Trial(classifier,testingData);
        //classifier.print();
        printTrialClassification(testTrial);
        printClassification(testTrial);
      }

    /*public static void runMaxEnt (Pipe pipe, InstanceList testInstances){
		double [] parameters=new double[pipe.getTargetAlphabet().size() * (pipe.getAlphabet().size()+1)];
		RankMaxEnt maxentclass = new RankMaxEnt (pipe, parameters);
		List<Classification> listClass = maxentclass.classify(testInstances);
		
		for (int i=0; i<listClass.size(); i++){
			System.out.println(listClass.get(i).getLabeling());
		}
		
	}*/
    
    public static void runCRF (Pipe pipe, InstanceList trainingData, InstanceList testingData){
    	CRF crf = new CRF(pipe, null);
    	crf.addStatesForThreeQuarterLabelsConnectedAsIn(trainingData);
    	crf.addStartState();
    	

		System.out.println("Test results: ");
    	for (int i = 0; i < testingData.size(); i++) {
    		Sequence input = (Sequence) testingData.get(i).getData();
    		Sequence output = crf.transduce(input);
    		System.out.println(output.toString());
    	}
    }
    
    public static void printTrialClassification(Trial testT){
		//System.out.println(testT.getAverageRank());
		System.out.println("result: "+testT.getF1("1"));
	}

}
  