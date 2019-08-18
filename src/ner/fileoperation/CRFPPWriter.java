package ner.fileoperation;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

public class CRFPPWriter {

	public static void writefile(ArrayList<String[]> tokens, ArrayList<String[]> caps, ArrayList<String[]> tags, ArrayList<String[]> ners, String file) throws IOException {
		FileWriter fw = new FileWriter(file);
		for (int i = 0; i < tokens.size(); i++) {
			String[] sentence_tokens=tokens.get(i);
			String[] sentence_caps=caps.get(i);
			String[] sentence_tags=tags.get(i);
			String[] sentence_ners=ners.get(i);
			for(int j=0; j<sentence_tokens.length;j++)
			fw.write(sentence_tokens[j]+" "+sentence_caps[j]+" "+sentence_tags[j]+" "+sentence_ners[j]+"\n");
			fw.write("\n");
		}
	 
		fw.close();
		System.out.println("File writting successed..");
	}
	
	public static void writefile(ArrayList<String[]> tokens, ArrayList<String[]> caps, ArrayList<String[]> tags, String file) throws IOException {
		FileWriter fw = new FileWriter(file);
		for (int i = 0; i < tokens.size(); i++) {
			String[] sentence_tokens=tokens.get(i);
			String[] sentence_caps=caps.get(i);
			String[] sentence_tag=tags.get(i);
			for(int j=0; j<sentence_tokens.length;j++)
			fw.write(sentence_tokens[j]+" "+sentence_caps[j]+" "+sentence_tag[j]+"\n");
			fw.write("\n");
		}
	 
		fw.close();
		System.out.println("File writting successed..");
	}
	
	public static void writefile(ArrayList<String[]> tokens, ArrayList<String[]> caps, String file) throws IOException {
		FileWriter fw = new FileWriter(file);
		for (int i = 0; i < tokens.size(); i++) {
			String[] sentence_tokens=tokens.get(i);
			String[] sentence_caps=caps.get(i);
			for(int j=0; j<sentence_tokens.length;j++)
			fw.write(sentence_tokens[j]+" "+sentence_caps[j]+"\n");
			fw.write("\n");
		}
	 
		fw.close();
		System.out.println("File writting successed..");
	}
	
	public static void writefile(ArrayList<String[]> tokens, String file) throws IOException {
		FileWriter fw = new FileWriter(file);
		for (int i = 0; i < tokens.size(); i++) {
			String[] sentence_tokens=tokens.get(i);
			for(int j=0; j<sentence_tokens.length;j++)
			fw.write(sentence_tokens[j]+"\n");
			fw.write("\n");
		}
	 
		fw.close();
		System.out.println("File writting successed..");
	}
	
	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

}
