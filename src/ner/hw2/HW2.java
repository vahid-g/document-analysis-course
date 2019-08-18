package ner.hw2;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import ner.nlp.CapDetector;
import ner.nlp.POStagger;
import ner.nlp.Tokenizer;
import ner.web.reader.WebPageReader;

public class HW2 {
	public static void main(String[] args) throws Exception {
		 String url =
		 "https://www.washingtonpost.com/politics/on-ex-im-bank-gop-leadership-allies-launch-rebellion-of-their-own/2015/10/26/c6c7b0fe-7bff-11e5-afce-2afd1d3eb896_story.html";
		 WebPageReader web_xml = new WebPageReader(url);
		 String xpath =
		 "//*/H1[string-length(text()) > 25] | //HTML//BODY//ARTICLE/P[string-length(text()) > 50]";
		 ArrayList<String> result = web_xml.getXPathQueryResults(xpath);
		
		 List<String> sentences = new ArrayList<String>();
		 for (String r : result) {
		 sentences.add(r.trim());
		 // System.out.println(r.trim());
		 }
		 ArrayList<String[]> tokens = Tokenizer.take(sentences
		 .toArray(new String[sentences.size()]));
		 ArrayList<String[]> tags = POStagger.tag(tokens);
		 ArrayList<String[]> caps = CapDetector.check(tokens);
		 BufferedWriter bw = new BufferedWriter(new
		 FileWriter("Tools/CRF/test"));
		 for (int i = 0; i < tokens.size(); i++) {
		 for (int j = 0; j < tokens.get(i).length; j++) {
		 bw.write(tokens.get(i)[j] + " " + caps.get(i)[j] + " "
		 + tags.get(i)[j] + " " + "O\n");
		 }
		 }
		 bw.close();
		Process proc = Runtime.getRuntime().exec(
				"Tools/CRF/crf_test.exe -m model_ff test", null,
				new File("Tools/CRF/"));
		BufferedReader bri = new BufferedReader(new InputStreamReader(
				proc.getInputStream()));
		// BufferedReader bre = new BufferedReader(new InputStreamReader(
		// proc.getErrorStream()));
		String currentLine = "";
		HashSet<String> entities = new HashSet<String>();
		String entity = "";
		String label = "";
		boolean flag = false;
		while ((currentLine = bri.readLine()) != null) {
			String line[] = currentLine.split("\\s");
			if (line.length > 1)
				if (!line[4].equals("O")) {
					if (flag && line[4].equals(label)) {
						entity = entity + " " + line[0];
					} else if (flag && !line[4].equals(label)) {
						entities.add(entity);
						entity = line[0];
					} else {
						flag = true;
						entity = line[0];
						label = line[4];
					}
				} else {
					if (flag) {
						entities.add(entity);
					}
					flag = false;
					entity = "";
					label = "";
				}
		}
		bri.close();
		// bre.close();
		proc.waitFor();
		System.out.println(entities);
	}

	public static void addPosCap(String filename) {
		BufferedReader br = null;
		BufferedWriter bw = null;
		try {
			br = new BufferedReader(new FileReader(filename));
			bw = new BufferedWriter(new FileWriter(filename + "_feat"));
			String line;
			ArrayList<String[]> tokens = new ArrayList<String[]>();
			ArrayList<String> labels = new ArrayList<String>();
			while ((line = br.readLine()) != null) {
				if (line.equals(""))
					continue;
				String[] tokenArray = new String[1];
				tokenArray[0] = line.split(" ")[0];
				labels.add(line.split(" ")[1]);
				tokens.add(tokenArray);
			}

			ArrayList<String[]> tags = POStagger.tag(tokens);
			ArrayList<String[]> caps = CapDetector.check(tokens);
			for (int i = 0; i < tags.size(); i++) {
				for (int j = 0; j < tags.get(i).length; j++) {
					bw.write(tokens.get(i)[j] + " " + caps.get(i)[j] + " "
							+ tags.get(i)[j] + " " + labels.get(i) + "\n");
				}
			}

			// ArrayList<String[]> low = CapDetector.toLowerCase(tokens);
			// for (int i = 0; i < caps.size(); i++) {
			// for (int j = 0; j < caps.get(i).length; j++) {
			// System.out.println(tokens.get(i)[j] + " " + caps.get(i)[j]
			// + " " + low.get(i)[j]);
			// }
			// }
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			try {
				br.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
			try {
				bw.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
}
