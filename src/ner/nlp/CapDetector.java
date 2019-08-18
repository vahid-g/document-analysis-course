package ner.nlp;

import java.util.ArrayList;
import opennlp.tools.util.StringUtil;

/**
 * @author Wuga check function generate a new column to distinguish Capitalized
 *         word and others toLowerCase does exactly as the name
 */
public class CapDetector {

	public static ArrayList<String[]> check(ArrayList<String[]> tokens) {
		ArrayList<String[]> uppercase = new ArrayList<String[]>();
		for (int i = 0; i < tokens.size(); i++) {
			String[] sentence_tokens = tokens.get(i);
			String[] sentence_uppercase = new String[sentence_tokens.length];
			for (int j = 0; j < sentence_tokens.length; j++) {
				if (Character.isUpperCase(sentence_tokens[j].charAt(0))) {
					sentence_uppercase[j] = "CAP";
				} else {
					sentence_uppercase[j] = "LOW";
				}
			}
			uppercase.add(sentence_uppercase);
		}
		return uppercase;
	}

	public static ArrayList<String[]> toLowerCase(ArrayList<String[]> tokens) {
		ArrayList<String[]> new_tokens = new ArrayList<String[]>();
		for (int i = 0; i < tokens.size(); i++) {
			String[] sentence_tokens = tokens.get(i);
			String[] newTokens = new String[sentence_tokens.length];
			for (int j = 0; j < sentence_tokens.length; j++) {
				newTokens[j] = StringUtil.toLowerCase(sentence_tokens[j]);
			}
			new_tokens.add(newTokens);
		}
		return new_tokens;
	}

	public static void main(String[] args) {

		try {
			ArrayList<String[]> tokens = new ArrayList<String[]>();
			String[] sentence = { "He", "likes", "Surface", "Book", "!" };
			tokens.add(sentence);
			ArrayList<String[]> caps = check(tokens);
			ArrayList<String[]> low = toLowerCase(tokens);
			for (int i = 0; i < caps.size(); i++) {
				for (int j = 0; j < caps.get(i).length; j++) {
					System.out.println(tokens.get(i)[j] + " " + caps.get(i)[j]
							+ " " + low.get(i)[j]);
				}
			}

		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
