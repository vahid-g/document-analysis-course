package machine_learning.classifier;

import java.io.IOException;

import libsvm.svm_parameter;
import machine_learning.classifier.ArffData.FoldData;
import de.bwaldvogel.liblinear.SolverType;

/*
 * Set up and launch classifiers
 * 
 * Assuming last ARFF entry is the class label.
 */

public class Launcher {

	public final static String DATA_FILE = "data/arff/vote.arff";
	//public final static String DATA_FILE = "data/arff/newsgroups.arff";
	public final static int    NUM_FOLDS = 10;
	
	public static void main(String[] args) throws IOException {
		
		// Baselines: always compare to these... if you can't beat them, something is wrong!
		Predictor constPredTrue  = new ConstantPredictor(true);
		Predictor constPredFalse = new ConstantPredictor(false);

		// Naive Bayes: Scott's implementation for teaching purposes, not robust or scalable
		Predictor naiveBayes = new NaiveBayes(1.0d); 

		// LibLinear
		Predictor liblinear1 = new SVMLibLinear(SolverType.L2R_L2LOSS_SVC, /*C*/1.0d,  /*eps*/0.001d); // Liblinear SVM: L2R_L2LOSS_SVC, L1R_L2LOSS_SVC, L2R_L1LOSS_SVC_DUAL
		Predictor liblinear2 = new SVMLibLinear(SolverType.L2R_LR,         /*C*/1.0d,  /*eps*/0.001d); // Liblinear Logistic Regression: L1R_LR, L2R_LR 

		// LibSVM with linear kernel
		svm_parameter linear = new svm_parameter();
		linear.C = 1.0d;
		linear.eps = 0.001d;
		linear.kernel_type = svm_parameter.LINEAR;
		Predictor libsvm_linear_kernel = new SVMLibSVM(linear);

		// LibSVM with polynomial kernel
		svm_parameter poly = new svm_parameter();
		poly.C = 1.0d;
		poly.eps = 0.001d;
	    poly.kernel_type = svm_parameter.POLY; // (gamma*u'*v + coef0)^degree
	    poly.coef0  = 1; 
	    poly.gamma  = 1;
	    poly.degree = 2; // quadratic kernel
		Predictor libsvm_poly_kernel = new SVMLibSVM(poly); // LibSVM allows nonlinear kernels
		
		// LibSVM with RBF kernel
		svm_parameter rbf = new svm_parameter();
		rbf.C = 1.0d;
		rbf.eps = 0.001d;
	    rbf.kernel_type = svm_parameter.RBF; // exp(-gamma*|u-v|^2)
	    rbf.gamma  = 1;
		Predictor libsvm_rbf_kernel = new SVMLibSVM(rbf); // LibSVM allows nonlinear kernels

		// LingPipe Logistic Regression
		Predictor logisticRegression_l1 = new LogisticRegression(LogisticRegression.PRIOR_TYPE.L1, 1d); // LingPipe's logistic regression implementation -- slower than Liblinear
		Predictor logisticRegression_l2 = new LogisticRegression(LogisticRegression.PRIOR_TYPE.L2, 1d);

		///////////////////////////////////////////////////////////////////////////////////////////

		// Generate reusable train/test splits so that all learners are evaluated the same way
		System.out.print("Splitting '" + DATA_FILE + "' into " + NUM_FOLDS + " folds...");
		ArffData arff = new ArffData(DATA_FILE);
		//System.out.println("Training fold contents: " + arff);
		FoldData f = arff.foldData(NUM_FOLDS);
		f.writeData();
		System.out.println(" done.\n");

		///////////////////////////////////////////////////////////////////////////////////////////

		// Build an array of all predictors
		Predictor[] predictors = new Predictor[] { 
				constPredTrue,
				constPredFalse,
				naiveBayes, 
				liblinear1,
				liblinear2,
				libsvm_linear_kernel, 
				libsvm_poly_kernel,
				libsvm_rbf_kernel
				//, 
				//logisticRegression_l1, // LingPipe Logistic Regression -- slower than Liblinear but comparable accuracy
				//logisticRegression_l2,
		};
		
		// Run each predictor in turn and evaluate
		for (Predictor p : predictors)
			p.runTests(DATA_FILE, NUM_FOLDS);
	}
	
}
