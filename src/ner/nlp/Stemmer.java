package ner.nlp;

import java.util.ArrayList;

import opennlp.tools.stemmer.PorterStemmer;

public class Stemmer {
	
	public static ArrayList<String[]> stem(ArrayList<String[]> tokens){
		PorterStemmer stemmer=new PorterStemmer();
		ArrayList<String[]> stem_tokens = new ArrayList<String[]>();
		for(int i=0;i<tokens.size();i++){
			String[] sentence_tokens=tokens.get(i);		
			for(int j=0;j<sentence_tokens.length;j++){
				sentence_tokens[j]=stemmer.stem(sentence_tokens[j]);
			}
			stem_tokens.add(sentence_tokens);
		}
		return stem_tokens;
	}

	public static void main(String[] args) {

		try {
			ArrayList<String[]> tokens=new ArrayList<String[]>();
			String[] sentence={"he","likes","surface","book","!"};
			tokens.add(sentence);
			ArrayList<String[]> stemmed_token= stem(tokens);
			for(int i=0;i< stemmed_token.size();i++){
				for(int j=0;j<stemmed_token.get(i).length;j++){
					System.out.println(stemmed_token.get(i)[j]);
				}
			}
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
