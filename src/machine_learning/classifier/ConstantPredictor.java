package machine_learning.classifier;

import machine_learning.classifier.ArffData.DataEntry;

public class ConstantPredictor extends Predictor {

	public boolean _prediction;
	
	public ConstantPredictor(boolean prediction) {
		_prediction = prediction;
	}

	public void train() { }
	public int evaluate(DataEntry de) { return _prediction ? 1 : 0; }
	public void clear() { }
	public String getName() { return "ConstantPredictor(" + _prediction + ")"; } 
}
