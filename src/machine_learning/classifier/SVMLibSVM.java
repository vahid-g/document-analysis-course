package machine_learning.classifier;

import java.io.IOException;

import libsvm.svm;
import libsvm.svm_model;
import libsvm.svm_node;
import libsvm.svm_parameter;
import libsvm.svm_problem;
import machine_learning.classifier.ArffData.DataEntry;

public class SVMLibSVM extends Predictor {

	/* Note: SPS -- libsvm has an odd rule than the +1 class is assigned for the class 
	 *       label of the *first* training example.  So we need to track what that 
	 *       label is in case we need to invert it.
	 *       
	 *       See footnote, page 4: http://www.csie.ntu.edu.tw/~cjlin/papers/libsvm.pdf
	 *       
	 *       SPS -- also since SVMs are not probabilistic and can predict values from
	 *       -infinity to infinity, it makes more sense to make the false class -1 and
	 *       and the true class 1 so that the natural threshold is 0.
	 */
	private boolean _firstLabelIsTrue;
	
	private svm_model _model = null;
	private svm_parameter _param = null;
	
	public SVMLibSVM(svm_parameter param) {
		_param = param;
		
		svm.svm_set_print_string_function(new libsvm.svm_print_interface(){
		    public void print(String s) {} // Disables svm output
		});
	}
	
	public void train() {
		_model = svmTrain();	
	}

	/*
	 * train the svm model
	 */
	private svm_model svmTrain() {
		svm_problem prob = new svm_problem();
		int dataCount = _trainData._data.size();
		prob.y = new double[dataCount];
		prob.l = dataCount;
		prob.x = new svm_node[dataCount][];		
		
		for (int i = 0; i < dataCount; i++){			
			double[] features = getFeatures(_trainData._data.get(i));
			prob.x[i] = new svm_node[features.length-1];
			// first 'feature' is class value
			for (int j = 1; j < features.length; j++){ // 0th feature is class label
				svm_node node = new svm_node();
				node.index = j;
				node.value = features[j];
				prob.x[i][j-1] = node;
			}			
			prob.y[i] = (features[0] == 0) ? -1 : 1;
			if (i == 0) // Check first training example
				_firstLabelIsTrue = (prob.y[i] == 1);
		}				
		
		//param.probability = 1;
		//param.gamma = 0.45;	// 1/num_features
		//param.C = _C;
		//param.eps = _EPS;		
		_param.svm_type = svm_parameter.C_SVC;
		//param.kernel_type = _kernel; // svm_parameter.LINEAR;		
		_param.cache_size = 200000;
				
		svm_model model = svm.svm_train(prob, _param);
		
		return model;
	}

	@Override
	public int evaluate(DataEntry de) {
		
		double[] features = getFeatures((DataEntry)de);
		svm_node[] nodes = new svm_node[features.length-1];
		for (int i = 1; i < features.length; i++){
			nodes[i-1] = new svm_node();
			nodes[i-1].index = i;
			nodes[i-1].value = features[i];
		}
		
		double[] dbl = new double[1]; 
		svm.svm_predict_values(_model, nodes, dbl);
		// +1 will be assigned for the label of the first training datum
		double prediction = _firstLabelIsTrue ? dbl[0] : -dbl[0];
		return (prediction > 0d ? 1 : 0);		
	}

	@Override
	public void clear() {
		_model = null;
	}

	@Override
	public String getName() {
		return "SVMLibSVM(" + getKernelName(_param) +  ")";
	}

	public String getKernelName(svm_parameter param) {
		switch (param.kernel_type) {
		case svm_parameter.LINEAR: return "Linear, C=" + param.C + ", eps=" + param.eps;
		case svm_parameter.POLY:   return "Poly, C=" + param.C + ", eps=" + param.eps + ", gamma=" + param.gamma + ", coef0=" + param.coef0 + ", degree=" + param.degree;
		case svm_parameter.RBF:    return "RBF, C=" + param.C + ", eps=" + param.eps + ", gamma=" + param.gamma;
		default: return "Unknown kernel";
		}
	}
	
	public static void main(String[] args) throws IOException{
		svm_parameter linear = new svm_parameter();
		linear.C = 1.0d;
		linear.eps = 0.001d;
		linear.kernel_type = svm_parameter.LINEAR;
		SVMLibSVM svm = new SVMLibSVM(linear);
		svm.runTests("active.arff", 10);
	}
	
}
