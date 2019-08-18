package ner.nlp;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

import opennlp.tools.tokenize.TokenizerME;
import opennlp.tools.tokenize.TokenizerModel;
import opennlp.tools.util.InvalidFormatException;

public class Tokenizer {
	public static ArrayList<String[]> tokens=new ArrayList<String[]>();
	
	public static ArrayList<String[]> take(String[] sentences) throws InvalidFormatException, IOException {
		InputStream is = new FileInputStream("models/en-token.bin");	 
		TokenizerModel model = new TokenizerModel(is);	 
		TokenizerME tokenizer = new TokenizerME(model);	
		for(int i=0;i<sentences.length;i++){
			String sentencetokens[] = tokenizer.tokenize(sentences[i]);	 
			tokens.add(sentencetokens);
//			for (String a : sentencetokens)
//				System.out.println(a);	 
			is.close();
		}
		return tokens;
	}
	
	public static void main(String[] args) {
		try {
			String[] sentences={"I like surface book!","But, Mac system is better."};
			ArrayList<String[]> tokens=Tokenizer.take(sentences);
			for(int i=0;i<tokens.size();i++){
				for(int j=0;j<tokens.get(i).length;j++){
					System.out.println(tokens.get(i)[j]);
				}
			}
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}

}
