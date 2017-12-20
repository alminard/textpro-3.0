package eu.fbk.textpro.ml;

public class MLModel {
	String feature_templates;
	Object model;
	String model_path;
	enum algorithm { CRF,SVM}; //algorithm(CRF,SVM)
}
