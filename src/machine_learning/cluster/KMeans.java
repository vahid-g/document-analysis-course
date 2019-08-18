package machine_learning.cluster;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import machine_learning.util.DocUtils;
import machine_learning.util.FileFinder;

public class KMeans {

	//public final static String DATA_SRC = "data/kmeans_test/";
	public final static String DATA_SRC = "data/blog_data_test/";
	public final static int    NUM_CLUSTERS = 3;
	public final static int    SHOW_DOCS_PER_CLUSTER = 5;
	public final static int    MAX_ITER = 100;
	
	public int _nClusters;
	public String _sDirSource;
	public Random _rgen;
	public Map<String,Map<Object,Double>> _hmFile2TF;
	public Map<String,Integer> _hmFile2Label;
	public Map<Integer,Map<Object,Double>> _hmLabel2Centroid;
	
	public KMeans(int num_clusters, String dir_source) {
		_nClusters = num_clusters;
		_sDirSource = dir_source;
		_hmFile2TF = new HashMap<String,Map<Object,Double>>();
		_hmFile2Label = new HashMap<String,Integer>();
		_hmLabel2Centroid = new HashMap<Integer,Map<Object,Double>>();
		_rgen = new Random();
	}
	
	// Main k-means loop
	public void run() {
				
		loadTFVectors();
		initCentroids(); // Choose initial centroids as random data points
		
		// Do 10 iterations of K-means
		double last_iter_error = Double.MAX_VALUE;
		double cur_iter_error  = Double.MAX_VALUE;
		int iter = 1;
		
		// Terminate on convergence or max iterations 
		do {
			
			// Main loop
			//displayClusters();
			relabelData(); // Relabel data according to nearest centroids
			computeCentroids(); // Recompute centroids according to new labels 
			
			// Compute error and display
			last_iter_error = cur_iter_error;
			cur_iter_error = computeSumOfSqError(); // Compute error of data from current centroids
			System.out.println("End of iter #" + iter + ", sq error before = " + 
							   last_iter_error + ", after = " + cur_iter_error);
			
		} while (cur_iter_error != last_iter_error && ++iter <= MAX_ITER);
		
		// Diagnostic output regarding loop termination
		if (iter <= MAX_ITER)
			System.out.println("\nConverged!\n");
		else 
			System.out.println("\nReached iteration limit.\n");
		
		// Display top 5 documents in each cluster
		displayClusters();
	}
		
	// Display top 5 documents in each cluster
	private void displayClusters() {

		// Build a sorted list of documents for each cluster label
		HashMap<Integer,ArrayList<DocDistPair>> label2docs = new HashMap<Integer,ArrayList<DocDistPair>>();
		for (int c = 1; c <= _nClusters; c++) {

			// Get cluster centroid
			Map<Object,Double> centroid_vector = _hmLabel2Centroid.get(c);
			
			// Initialize an empty document list for this centroid 
			ArrayList<DocDistPair> doclist = new ArrayList<DocDistPair>();
			label2docs.put(c, doclist); // Initialize to empty list
			
			// Add all documents with this cluster label to doclist
			for (Map.Entry<String, Integer> entry : _hmFile2Label.entrySet()) {
				if (c == entry.getValue()) {
					
					String filename = entry.getKey();
					Map<Object,Double> tf_vector = _hmFile2TF.get(filename);

					// Compute distance of this document from its centroid
					Map<Object,Double> diff_vector = VecUtils.Sum(tf_vector, VecUtils.ScalarMultiply(centroid_vector, -1.0));
					double dist = VecUtils.ComputeSqLen(diff_vector);
					
					doclist.add(new DocDistPair(filename, dist));
				}
				
				// Sort doclist
				Collections.sort(doclist);
			}
		}

		// Print top SHOW_DOCS_PER_CLUSTER documents for each cluster
		for (int c = 1; c <= _nClusters; c++) {
			
			ArrayList<DocDistPair> doclist = label2docs.get(c);
			System.out.println("Cluster #" + c + " (" + doclist.size() + " files total)");
			for (int n = 0; n < SHOW_DOCS_PER_CLUSTER && n < doclist.size(); n++)
				System.out.println(" - " + doclist.get(n));
			System.out.println();
		}
	}
	
	// Select initial centroids as random documents -- need to ensure all different				
	private void initCentroids() {
		
		// First select centroid indices
		HashMap<Integer,Integer> index2label = new HashMap<Integer,Integer>();
		for (int c = 1; c <= _nClusters; c++) {
			
			// Only accept index if not already assigned to other centroid, otherwise reselect
			int centroid_index = _rgen.nextInt(_hmFile2TF.size());
			while (index2label.containsKey(centroid_index))
				centroid_index = _rgen.nextInt(_hmFile2TF.size());
			
			index2label.put(centroid_index, c);
		}
		System.out.println("Initialization - data index -> centroid label: " + index2label);
		
		// Next go through data and pick out any assigned indices and update _hmLabel2Centoid
		int index = 0;
		for (Map.Entry<String,Map<Object,Double>> entry : _hmFile2TF.entrySet()) {
			
			// Was this data index assigned to a centroid?  If so, get the TF vector and update centroid.
			Integer label = null;
			if ((label = index2label.get(index)) != null)
				_hmLabel2Centroid.put(label, entry.getValue() /* TF vector */);
			
			++index; // Move onto next datum (if it exists)
		}
	}

	// Given current centroids, relabels all data with nearest centroid label
	private void relabelData() {
		
		// Remap each file to its nearest centroid
		for (Map.Entry<String,Map<Object,Double>> entry : _hmFile2TF.entrySet()) {
			String filename = entry.getKey();
			Map tf_vector   = entry.getValue();
			
			// Find nearest centroid
			int nearest_centroid = -1;
			double nearest_dist  = Double.MAX_VALUE;
			for (int c = 1; c <= _nClusters; c++) {
				// Cosine is not a distance -- 0 is "most different", so take 1 - cossim
				double dist_to_c = 1d - VecUtils.CosSim(tf_vector, _hmLabel2Centroid.get(c));
				//System.out.println(filename + " <-> " + c + " = " + dist_to_c);
				if (dist_to_c < nearest_dist) {
					nearest_dist = dist_to_c;
					nearest_centroid = c;
				}
			}
			//System.out.println(filename + " -> " + nearest_centroid + "\n");
			
			// Relabel document/filename with nearest centroid
			_hmFile2Label.put(filename, nearest_centroid);
		}		
	}

	// Given current labels, compute centroids
	private void computeCentroids() {
		
		// Collect data for each cluster, average to find centroid
		for (int c = 1; c <= _nClusters; c++) {
			
			// Collect all doc vectors with label c
			ArrayList<Map<Object,Double>> vecs_with_c = new ArrayList<Map<Object,Double>>();
			for (Map.Entry<String, Integer> entry : _hmFile2Label.entrySet()) {
				String filename = entry.getKey();
				if (entry.getValue() == c)
					vecs_with_c.add(_hmFile2TF.get(filename));			
			}
			
			// Compute mean vector for c and store it
			Map<Object,Double> mean = VecUtils.Mean(vecs_with_c);
			mean = VecUtils.Normalize(mean);
			_hmLabel2Centroid.put(c, mean);
		}
	}

	// Compute current sum of squared errors of each data point from its centroid
	private double computeSumOfSqError() {
	
		// Go through each datum, accumulating squared error
		double accum_sq_error = 0d;
		
		for (Map.Entry<String,Map<Object,Double>> entry : _hmFile2TF.entrySet()) {
			
			String filename = entry.getKey();
			Map<Object,Double> tf_vector   = entry.getValue();
			
			int label = _hmFile2Label.get(filename);
			Map<Object,Double> centroid_vector = _hmLabel2Centroid.get(label);
			
			Map<Object,Double> diff_vector = VecUtils.Sum(tf_vector, VecUtils.ScalarMultiply(centroid_vector, -1.0));
			
			accum_sq_error += VecUtils.ComputeSqLen(diff_vector);
		}
		
		return accum_sq_error;
	}
	
	// Load data from disk
	private void loadTFVectors() {

		// Get list of data files
		ArrayList<File> files = FileFinder.GetAllFiles(_sDirSource, "", true);
		System.out.println("Found " + files.size() + " files.\n");

		// Build term frequency vectors
		for (File f : files) {
			
			// Get word frequencies in this file
			String file_content = DocUtils.ReadFile(f);
			Map<Object,Double> word_counts = DocUtils.ConvertToFeatureMap(file_content);
			word_counts = VecUtils.Normalize(word_counts);
			_hmFile2TF.put(f.toString(), word_counts);
			
			// Diagnostics
			//System.out.println(f.toString() + ": " + word_counts);
		}
	}
	
	// An inner comparable (i.e., sortable) class that stores a pair of <filename,distance>
	public class DocDistPair implements Comparable<DocDistPair> {

		public String _sFilename;
		public double _dDist;
		
		public DocDistPair(String filename, double dist) {
			_sFilename = filename;
			_dDist = dist;
		}

		public String toString() {
			return _sFilename + " -> " + _dDist;
		}

		public int compareTo(DocDistPair d) {
			if (d._dDist > this._dDist)
				return -1;
			else if (d._dDist < this._dDist)
				return 1;
			else
				return 0;
		}
	}
	
	// A utility class for sparse vector functions on HashMaps... should be a separate
	// class but will make an inner class so this file is self-contained.
	public static class VecUtils {

		// Compute squared vector length
		public static double ComputeSqLen(Map<Object,Double> v) {

			// Compute sum of squared vector elements
			double sum_of_sq = 0d;
			for (Map.Entry<Object,Double> entry : v.entrySet()) {
				double value = entry.getValue();
				sum_of_sq += value*value;
			}
			
			return sum_of_sq;
		}

		// Scalar multiply (elementwise multiply with a double) - returns vector
		public static Map<Object,Double> ScalarMultiply(Map<Object,Double> v, double c) {
			
			Map<Object,Double> result = new HashMap<Object,Double>();
			
			// Multiply all elements by c
			for (Object key : v.keySet()) {
				double cur_val = v.get(key);
				double norm_val = cur_val * c;
				result.put(key, norm_val);
			}			
			
			return result;
		}
		
		// Normalize a vector to unit length (in place)
		public static Map<Object,Double> Normalize(Map<Object,Double> v) {
			
			double len = Math.sqrt(ComputeSqLen(v));
			return ScalarMultiply(v, 1.0/len);
		}
		
		// Computes the inner product of two vectors - assume v1 and v2 are already normalized
		public static double CosSim(Map<Object,Double> v1, Map<Object,Double> v2) {
		
			// v1 should be smaller Map
			if (v2.size() < v1.size()) {
				Map<Object,Double> swap = v1;
				v1 = v2;
				v2 = swap;
			}
			
			double running_sum = 0d;
			for (Object key : v1.keySet()) {
				Double value = v2.get(key);
				if (value != null)
					running_sum += v1.get(key)*value;
			}
			
			return running_sum;
		}

		// Compute the sum of two vectors - returns a vector
		public static Map<Object,Double> Sum(Map<Object,Double> v1, Map<Object,Double> v2) {

			Map<Object,Double> sum = new HashMap<Object,Double>();
			
			// Go through all keys in v1
			for (Object key : v1.keySet()) {
				double val1 = v1.get(key);
				Double val2 = v2.get(key);
				if (val2 == null)
					val2 = 0d;
				sum.put(key, val1 + val2);
			}
			
			// Go through all remaining non-zero keys from v2 not processed above
			for (Object key : v2.keySet()) {
				if (v1.containsKey(key))
					continue;

				sum.put(key, v2.get(key));
			}

			return sum;
		}
		
		// Compute the mean of a list of vectors - returns a vector
		public static Map<Object,Double> Mean(ArrayList<Map<Object,Double>> vectors) {
			
			Map<Object,Double> sum = new HashMap<Object,Double>();
			
			for (Map<Object,Double> v : vectors) {
				sum = Sum(v,sum);
			}
			
			int num_vecs = vectors.size();
			for (Object key : sum.keySet()) {
				Double val = sum.get(key);
				sum.put(key, val / (double)num_vecs);
			}
			
			return sum;
		}
	}
		
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		KMeans my_kmeans = new KMeans(NUM_CLUSTERS, DATA_SRC);
		my_kmeans.run();
	}

}
