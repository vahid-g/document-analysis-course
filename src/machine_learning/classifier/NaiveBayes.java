package machine_learning.classifier;

import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;

import machine_learning.classifier.ArffData.DataEntry;
import machine_learning.classifier.ArffData.FoldData;

/*
 * Naive Bayes implementation
 */

public class NaiveBayes extends Predictor {

	public static DecimalFormat _df = new DecimalFormat("0.######");

	public int CLASS_INDEX = -1;
	public double DIRICHLET_PRIOR = 1d;
	public double _threshold;
	public ArrayList<ClassCondProb> _condProb = null; 

	public class ClassCondProb {
		int _attr_index;
		public double[][] _logprob; // For each class and attribute,
		// a probability that sums to 1

		public ClassCondProb(int index) {
			_attr_index = index;
		}

		public String toString() {
			StringBuffer sb = new StringBuffer();
			ArffData.Attribute  a = _trainData._attr.get(_attr_index);
			ArffData.Attribute ca = _trainData._attr.get(CLASS_INDEX);
			if (_attr_index == CLASS_INDEX) {
				for (int cv = 0; cv < ca.class_vals.size(); cv++) {
					sb.append("P( " + ca.name + " = " + ca.getClassName(cv) + " ) = " + 
							_df.format(Math.exp(_logprob[cv][0])) + "\n");
				}
			} else { 
				for (int cv = 0; cv < ca.class_vals.size(); cv++) {
					for (int av = 0; av < a.class_vals.size(); av++) {
						sb.append("P( " + a.name + " = " + a.getClassName(av) + " | " + ca.name + " = " + ca.getClassName(cv) + 
								" ) = " + _df.format(Math.exp(_logprob[av][cv])) + "\n");
					}
				}
			}
			return sb.toString();
		}
	}

	public NaiveBayes(double dirichlet_prior) {
		
		_threshold = 0.5d;
		
		DIRICHLET_PRIOR = dirichlet_prior;
	}

	@Override
	public void train() {
		if (_trainData == null) { System.out.println("No data!"); }

		_condProb = new ArrayList<ClassCondProb>(_trainData._attr.size());
		CLASS_INDEX = _trainData._attr.size() - 1;
		
		//System.out.println("Training for " + _condProb.size() + " attributes.");

		// Build conditional probability tables
		ArffData.Attribute ca = _trainData._attr.get(CLASS_INDEX);		

		if (ca.type != ArffData.TYPE_CLASS) {
			System.out.println("Cannot classify non-class attribute index " + 
					CLASS_INDEX + ":\n" + ca);
			System.exit(1);
		}

		// For each class, record count with positive and record 
		// count with negative
		for (int i = 0; i < _trainData._attr.size(); i++) {

			if (_trainData._attr.get(i).type != ArffData.TYPE_CLASS){ // dont want to do anything with the real cols
				//System.out.println("Skipping - " + _trainData._attr.get(i));
				continue;
			}

			// TODO: Inefficient to constantly recompute
			int[] overall_count = new int[ca.class_vals.size()];

			//System.out.println("Processing " + i);
			ClassCondProb ccp = new ClassCondProb(i);
			_condProb.add(ccp);

			// Put the prior in this class
			if (i == CLASS_INDEX) {
				ccp._logprob = new double[ca.class_vals.size()][];
				for (int j = 0; j < ca.class_vals.size(); j++) {
					ccp._logprob[j] = new double[1];
				}
				for (int j = 0; j < _trainData._data.size(); j++) {
					ArffData.DataEntry de = _trainData._data.get(j);					
					int class_value = ((Integer)de.getData(CLASS_INDEX)).intValue();
					ccp._logprob[class_value][0] = ccp._logprob[class_value][0] + 1d; 
				}
				// Normalize and take log
				for (int j = 0; j < ca.class_vals.size(); j++) {
					if (DIRICHLET_PRIOR + ccp._logprob[j][0] > 0d)
						ccp._logprob[j][0] = Math.log((DIRICHLET_PRIOR + ccp._logprob[j][0]) / 
								(_trainData._data.size() + ca.class_vals.size() * DIRICHLET_PRIOR));
				}
				continue;
			}

			// Otherwise compute the conditional probabilities for this attribute
			ArffData.Attribute a  = _trainData._attr.get(i);
			if (a.type != ArffData.TYPE_CLASS) {
				//System.out.println("Skipping - " + a);
				//System.out.println("Cannot classify non-class attribute index " + 
				//		i + ":\n" + a);
				//System.exit(1);
				continue;
			}

			ccp._logprob = new double[a.class_vals.size()][];
			for (int j = 0; j < a.class_vals.size(); j++) {
				ccp._logprob[j] = new double[ca.class_vals.size()];
			}

			// Sort data entries into subnodes
			for (int j = 0; j < _trainData._data.size(); j++) {				
				ArffData.DataEntry de = _trainData._data.get(j);
				int attr_value  = ((Integer)de.getData(i)).intValue();
				int class_value = ((Integer)de.getData(CLASS_INDEX)).intValue();
				ccp._logprob[attr_value][class_value] = ccp._logprob[attr_value][class_value] + 1d;
				overall_count[class_value]++;
			}

			// Normalize and take log
			for (int av = 0; av < a.class_vals.size(); av++) {
				for (int cv = 0; cv < ca.class_vals.size(); cv++) {
					if (DIRICHLET_PRIOR + ccp._logprob[av][cv] != 0d)
						ccp._logprob[av][cv] = Math.log((DIRICHLET_PRIOR + ccp._logprob[av][cv]) 
								/ (overall_count[cv] + DIRICHLET_PRIOR * ca.class_vals.size()));
				}
			}
		}
		//System.out.println("Constructed " + _condProb.size() + " CPTs.");
		//System.out.println(this);
	}

	// SPS -- TODO: Threshold should be determined on train data and automatically set
	//              to value that maximizes accuracy (?). 
	@Override
	public int evaluate(DataEntry de) {
		// Get class attribute
		ArffData.Attribute ca = _testData._attr.get(CLASS_INDEX);
		if (ca.type != ArffData.TYPE_CLASS) {
			System.out.println("Cannot classify non-class attribute index " + 
					CLASS_INDEX + ":\n" + ca);
			System.exit(1);
		}

		// For each class, record count with positive and record 
		// count with negative
		int best_class = -1;
		double best_class_value = Double.NEGATIVE_INFINITY;
		for (int i = 0; i < ca.class_vals.size(); i++) { 
			
			double class_value = 0d;
			for (int j = 0; j < _condProb.size(); j++) {
			
				ClassCondProb ccp = _condProb.get(j);
				if (j == CLASS_INDEX) {
					class_value += ccp._logprob[i][0];
				} else {
					//System.out.print(((Integer)de.getData(j)).intValue() + " ");
					class_value += ccp._logprob[((Integer)de.getData(j)).intValue()][i];
				}
			}
			
			//System.out.println("[" + i + "] " + class_value);
			if (class_value > best_class_value) {
				best_class = i;
				best_class_value = class_value;
			}
		}
		
		//System.out.println("Best [" + best_class + "] " + best_class_value + " :: " + de);
		return best_class;	
	}

	@Override
	public void clear() {
		_condProb = null;
	}

	@Override
	public String getName() {
		return "NaiveBayes(" + DIRICHLET_PRIOR + ")";
	}

	@Override
	public String toString() {
		StringBuffer sb = new StringBuffer("\nNaive Bayes CPTs [" + _condProb.size() + "]:\n\n");
		for (int i = 0; i < _condProb.size(); i++) {
			ClassCondProb ccp = _condProb.get(i);
			sb.append("Attribute: " + _trainData._attr.get(i).name + "\n");
			sb.append(ccp.toString() + "\n");
		}
		return sb.toString();
	}

	public static String DATA_FILE = "data/arff/vote.arff";
	//public static String DATA_FILE = "data/arff/newsgroups.arff";
	public static int NUM_FOLDS = 10;

	public static void main(String[] args) throws IOException {
				
		// Generate reusable train/test splits so that all learners are evaluated the same way
		System.out.print("Splitting '" + DATA_FILE + "' into " + NUM_FOLDS + " folds...");
		ArffData arff = new ArffData(DATA_FILE);
		//System.out.println("Training fold contents: " + arff);
		FoldData f = arff.foldData(NUM_FOLDS);
		f.writeData();
		System.out.println(" done.\n");

		// Create a Naive Bayes classifier, evaluate it, and display it
		NaiveBayes nb = new NaiveBayes(1.0d);
		nb.runTests(DATA_FILE, NUM_FOLDS);
		System.out.println(nb); // Comment this out to suppress Naive Bayes conditional probability display
	}

}
