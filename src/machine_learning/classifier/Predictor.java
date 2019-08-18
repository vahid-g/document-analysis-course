package machine_learning.classifier;

import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;

import machine_learning.classifier.ArffData.DataEntry;
import machine_learning.classifier.ArffData.FoldData;
import machine_learning.util.Statistics;

public abstract class Predictor {

	DecimalFormat df3 = new DecimalFormat("#.###");

	public ArffData _trainData;
	public ArffData _testData;
	
	public abstract void train();									// train the model
	public abstract int evaluate(DataEntry de);	// evaluate a new data entry based on trained model
	public abstract void clear();									// clear the model
	public abstract String getName();								// name of the classifier

	/*
	 * Convert from arff to features array format.
	 * 
	 * The features array assumes the class index is first.
	 * 
	 * The arff format assumes the class index is last, so need to swaps.
	 */
	public double[] getFeatures(DataEntry dataEntry){	
		double[] features = new double[dataEntry._entries.size()];

		// Set features taking special care to make the last ARFF entry (class) the first feature entry
		for(int i = 0; i < dataEntry._entries.size(); i++){
			
			int index = (i < dataEntry._entries.size() - 1) ? i + 1 : 0;
			if(dataEntry._entries.get(i) instanceof Double){
				features[ index ] = (Double) dataEntry._entries.get(i);
			}
			else{
				Integer value = (Integer) dataEntry._entries.get(i);
				features[ index ] = value.doubleValue();
			}
		}
		return features;
	}	

	/*
	 * Calculate measures for accuracy, precision, recall and f measure
	 */
	public int[] measures(ArrayList<DataEntry> data) {
		int[] measures = new int[4];
		int truePositive = 0;
		int falsePositive = 0;
		int falseNegative = 0;
		int correct = 0;
		int CLASS_INDEX = data.get(0)._entries.size() - 1;
		for (DataEntry de : data) {
			int pred = evaluate(de); 										// predicted class
			int actual = ((Integer)((ArffData.DataEntry)de).getData(CLASS_INDEX)).intValue();	// actual class
			//System.out.println(evaluate(de) + " / " + ((Integer)((ArffData.DataEntry)de).getData(CLASS_INDEX)).intValue() + ": correct - " + (pred == actual));
			//System.out.println(evaluate(de) + " / " + ((Integer)((ArffData.DataEntry)de).getData(CLASS_INDEX)).intValue() + ": correct - " + (pred == actual) + " :: " + de);
			if (pred == actual) correct++;
			if (pred == actual && actual == 1) truePositive++;
			if (pred == 1 && actual == 0) falsePositive++;
			if (pred == 0 && actual == 1) falseNegative++;
		}
		measures[0] = correct;					 						// accuracy
		measures[1] = truePositive;										// true positive
		measures[2] = falsePositive;									// false positive
		measures[3] = falseNegative;									// false negative
		return measures;
	}

	/*
	 * Run tests on data
	 */
	public void runTests(String source_file, int num_folds) throws IOException {

		int correct = 0;									// correct classification
		int truePositive = 0;								// true positives
		int falsePositive = 0;								// false positives
		int falseNegative = 0;								// false negatives

		ArrayList<Double> train_accuracies = new ArrayList<Double>();
		ArrayList<Double> accuracies = new ArrayList<Double>();
		ArrayList<Double> precisions = new ArrayList<Double>();
		ArrayList<Double> recalls    = new ArrayList<Double>();
		ArrayList<Double> fscores    = new ArrayList<Double>();
		
		for (int i = 0; i < num_folds; i++){
			
			String trainName = source_file + ".train." + (i+1);
			String testName  = source_file + ".test."  + (i+1);
			_trainData = new ArffData(trainName);
			_testData  = new ArffData(testName);
			
			//System.out.println("Training fold contents '" + trainName + "': " + _trainData);
			//System.out.println("Testing fold contents '"  + testName  + "':  " + _testData);
			
			clear();
			train();										// build a classifier and train

			int[] trainMeasures = measures(_trainData._data); // train data
			train_accuracies.add( trainMeasures[0] / (double)_trainData._data.size() );
			
			int[] testMeasures = measures(_testData._data);	// test data
			correct = testMeasures[0];
			truePositive = testMeasures[1];
			falsePositive = testMeasures[2];
			falseNegative = testMeasures[3];
			double precision = (truePositive + falsePositive > 0) ? truePositive/(double)(truePositive + falsePositive) : 1d; // precision (1 if nothing predicted true)
			double recall    = truePositive/(double)(truePositive + falseNegative);	// recall 
		
			accuracies.add( correct / (double)_testData._data.size() );
			precisions.add( precision );
			recalls.add( recall );
			fscores.add(  2d * ((precision * recall)/(double)(precision + recall)) );
			
			//System.out.println("- Finished fold " + (i+1) + ", accuracy: " + df3.format( correct / (double)_testData._data.size() ));
		}

		System.out.println("Test Evaluation of " + getName());
		System.out.print("  Accuracy:  " + df3.format(Statistics.Avg(accuracies)) + "  +/-  " + df3.format(Statistics.StdError95(accuracies)));
		System.out.println("    vs.    Train Accuracy:  " + df3.format(Statistics.Avg(train_accuracies)) + "  +/-  " + df3.format(Statistics.StdError95(train_accuracies)));
		
		ArffData.Attribute ca = _trainData._attr.get(_trainData._attr.size() - 1);
		if (ca.class_vals.size() <= 3) { // 3rd is usually '?'		
			System.out.println("  Precision: " + df3.format(Statistics.Avg(precisions)) + "  +/-  " + df3.format(Statistics.StdError95(precisions)));
			System.out.println("  Recall:    " + df3.format(Statistics.Avg(recalls))    + "  +/-  " + df3.format(Statistics.StdError95(recalls)));
			System.out.println("  F-Score:   " + df3.format(Statistics.Avg(fscores))    + "  +/-  " + df3.format(Statistics.StdError95(fscores)));
		} else
			System.out.println("  (Multiclass classification -- not displaying other metrics)");
		System.out.println();
	}

}