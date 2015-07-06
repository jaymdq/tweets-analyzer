package tweetsAnalyzer;

import java.util.Enumeration;
import java.util.Vector;

import weka.classifiers.*;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.converters.ConverterUtils.DataSource;

public class TweetClassifier {

	// Variables
	private String arguments;
	private Instances data;
	private Instances testData;
	private Classifier classifier;


	// Constructors
	public TweetClassifier (String arguments){
		this.arguments = arguments;
		this.classifier = loadClassifier(arguments);
		
		//bottom
		configureClasiffier();
	}

	// Getters and Setters

	// Methods
	private Classifier loadClassifier(String arguments){
		Classifier out = null;
		String classifierName = arguments.split(" ")[0].trim();
		
		try {
			out = (Classifier) Class.forName(classifierName).newInstance();
		} catch (InstantiationException | IllegalAccessException | ClassNotFoundException e) {
			e.printStackTrace();
		}
		
		return out;
	}
	
	private void configureClasiffier(){
		try {
			String[] options = weka.core.Utils.splitOptions(arguments);
			classifier.setOptions(options);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void loadArffData(String path){
		try {
			DataSource source = new DataSource(path);
			data = source.getDataSet();
			if (data.classIndex() == -1)
				data.setClassIndex(data.numAttributes() - 1);
		} catch (Exception e){
			e.printStackTrace();
		}
	}

	private void loadArffTestData(String path){
		try {
			DataSource source = new DataSource(path);
			testData = source.getDataSet();
			if (testData.classIndex() == -1)
				testData.setClassIndex(testData.numAttributes() - 1);
		} catch (Exception e){
			e.printStackTrace();
		}
	}

	public void trainClassifier(String trainPath){
		loadArffData(trainPath);
		
		try {
			classifier.buildClassifier(data);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public String classify(String evaluationPath){
		String out = "";
		loadArffTestData(evaluationPath);
		
		try {
			Evaluation eval = new Evaluation(data);
			eval.evaluateModel(classifier, testData);
			out = eval.toSummaryString() + "\n\n" + eval.toMatrixString() + "\n\n" + eval.toClassDetailsString();
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return out;
	}

	public String classifyTweets(Vector<String> tweets, String evaluationPath) {
		String out = "";
		loadArffTestData(evaluationPath);
		
		try {
			Enumeration<?> enumerateInstances = data.enumerateInstances();
			int i = 0;
			while (enumerateInstances.hasMoreElements()){
				Instance inst = (Instance) enumerateInstances.nextElement();
				double result = classifier.classifyInstance(inst);
				out+= "[" + (i+1) + "]\n";
				out+= "Tweet: [" + tweets.elementAt(i) + "]\n";
				out+= "Data-Arff: [" + inst.toString() + "]\n";
				out+= "Classified as: [" + data.classAttribute().value((int) result) + "]\n";
				out+= "Actually is: [" + data.classAttribute().value((int) inst.classValue()) + "]\n\n";
				i++;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return out;
	}

}
