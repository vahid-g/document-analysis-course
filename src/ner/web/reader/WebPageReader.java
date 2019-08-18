/* Reads an html file from the web into XHTML and displays */

package ner.web.reader;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathFactory;



/** Note: robust HTML parser -- parses HTML into XHTML **/
import org.cyberneko.html.parsers.DOMParser;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

import ner.util.XMLUtils;

public class WebPageReader {

	Document _rssDoc = null;
	XPath _xpath = XPathFactory.newInstance().newXPath();

	public WebPageReader(String url) throws Exception {
		/**
		 * Parse the input url
		 */
		DOMParser parser = new DOMParser();
		InputStream byteStream = ner.util.InputStreamLoader.OpenStream(url);
		parser.parse(new org.xml.sax.InputSource(new InputStreamReader(byteStream, StandardCharsets.UTF_8)));
		_rssDoc = parser.getDocument();
	}

	public void displayXMLContent() {
		XMLUtils.PrintNode(_rssDoc);
	}

	public ArrayList<String> getXPathQueryResults(String query) {

		ArrayList<String> results = new ArrayList<String>();

		NodeList nodes = (NodeList) XMLUtils.XPathQuery(_xpath, _rssDoc,
				query /* TODO: XPath query */, false /*
													 * don't return just a
													 * single item
													 */);
		for (int i = 0; i < nodes.getLength(); i++) {
			// String node_value = nodes.item(i).getNodeValue();
			// String text_content = nodes.item(i).getTextContent();
			// System.out.println(node_value + ": " + text_content);
			results.add(nodes.item(i).getTextContent());
		}

		return results;
	}

	/**
	 * @param args
	 */
	/**
	 * @param args
	 * @throws Exception
	 */
	public static void main(String[] args) throws Exception {

		// String url =
		// "http://www.smh.com.au/lifestyle/lifematters/agedcare-revolution-20110808-1ijbu.html";
		String url = "https://www.washingtonpost.com/news/capital-weather-gang/wp/2015/10/22/extremely-dangerous-category-4-hurricane-patricia-to-slam-west-coast-of-mexico-friday/";
		if (args.length >= 1) {
			url = args[0];
		}

		// Read stream from http using robust html -> XHTML parser and display
		WebPageReader web_xml = new WebPageReader(url);
		// web_xml.displayXMLContent();
		String xPath1 = "//*/h1[string-length(text()) > 25] | //*/p[string-length(text()) > 50] | //*/div[string-length(text()) > 100]";
		String xPath2 = "//HTML//BODY//DIV[@class='article-body']";
		String xPath3 = "//*/H1[string-length(text()) > 25] | //HTML//BODY//DIV[@class='article-body']//*//P[string-length(text()) > 50]";
		String xPath4 = "//*/H1[string-length(text()) > 25] | //HTML//BODY//ARTICLE/P[string-length(text()) > 50]";
		ArrayList<String> result = web_xml.getXPathQueryResults(xPath4);
		for (String r : result) {
			//System.out.println("--");
			System.out.println(r.trim());
		}
	}

}
