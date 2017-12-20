package eu.fbk.textpro.ml;



public interface TextProModuleInterface {
    public void init (String[] params);
    static file_representation FeatureRepresentation(file_representation file, String feature_templates){
    	return file;
   }
    
//    		Input: testset + fileModel + flags [“evaluation”, “probability”]
//    	    Columns  = Call preprocess (testset)
//    	    Feature vectors = Call featurerepresentation (columns, feature templates)
//    	    Predictions  = Call classifier (feature vectors, path to model)
//    	    Output (predictions)
    
    public static file_representation classify(file_representation file,MLModel model,boolean... flag){
    	FeatureRepresentation(file,model.feature_templates);
    	file.file.values().parallelStream().filter(o->o.features_transformed.size()>0).forEach(o->classify(o,model));
    	return file;
    }
	public static Sentence classify(Sentence o, MLModel model){
		return o;
	}
    
//	 Train (public)
//	 Input: trainset + feature templates + algorithm(CRF,SVM) + path to file
//	 output model
//	 Columns = Call preprocess (trainset)
//	 Feature vectors = Call featurerepresentation (columns, feature templates)
//	 Call trainer (feature vectors, algorithm, path to model)
//	 Evaluation (call classify(testset + path to model + “evaluation”))
	 public static file_representation train(file_representation file,MLModel model,boolean... big_file){
	    	FeatureRepresentation(file,model.feature_templates);
	    	file.file.values().parallelStream().filter(o->o.features_transformed.size()>0).forEach(o->classify(o,model));
	    	return file;
	    }

    
    
    
}