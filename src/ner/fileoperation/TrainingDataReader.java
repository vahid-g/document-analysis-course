package ner.fileoperation;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;

/**
 * @author Wuga
 * This class is used to read Conll2002 training data format file,
 * and separate it into token, tags and ners column.
 * The out put format is in ArrayList<String[]> type, 
 * ArrayList is for sentence,
 * String[] is for words.
 * Note output is implicit as parameters.
 */
public class TrainingDataReader {

	
	public static void read(String file, ArrayList<String[]> tokens, ArrayList<String[]> tags,ArrayList<String[]> unknown, ArrayList<String[]> ners) throws IOException{
		FileInputStream fstream = new FileInputStream(file);
		BufferedReader br = new BufferedReader(new InputStreamReader(fstream));
		String strLine;
		try {
			ArrayList<String> sentence_tokens=new ArrayList<String>();
			ArrayList<String> sentence_tags=new ArrayList<String>();
			ArrayList<String> sentence_unknown=new ArrayList<String>();
			ArrayList<String> sentence_ners=new ArrayList<String>();
			while ((strLine = br.readLine()) != null)   {
				  if(strLine.isEmpty()){
					  tokens.add(sentence_tokens.toArray(new String[sentence_tokens.size()]));
					  tags.add(sentence_tags.toArray(new String[sentence_tags.size()]));
					  unknown.add(sentence_unknown.toArray(new String[sentence_unknown.size()]));
					  ners.add(sentence_ners.toArray(new String[sentence_ners.size()]));
					  sentence_tokens=new ArrayList<String>();
					  sentence_tags=new ArrayList<String>();
					  sentence_unknown=new ArrayList<String>();
					  sentence_ners=new ArrayList<String>();
				  }else{
					  String[] row=strLine.split("\\s+");
					  sentence_tokens.add(row[0]);
					  sentence_tags.add(row[1]);
					  sentence_unknown.add(row[2]);
					  sentence_ners.add(row[3]);
				  }
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		br.close();
	}
//	public static void main(String[] args) {
//		// TODO Auto-generated method stub
//		ArrayList<String[]> tokens=new ArrayList<String[]>();
//		ArrayList<String[]> tags=new ArrayList<String[]>();
//		ArrayList<String[]> ners=new ArrayList<String[]>();
//		try {
//			TrainingDataReader.read(FileLocation, tokens, tags, ners);
//		} catch (IOException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//	}

}
