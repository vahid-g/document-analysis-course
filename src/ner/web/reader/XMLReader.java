package ner.web.reader;

import java.io.InputStream;
import java.io.ByteArrayInputStream;
import java.util.ArrayList;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathFactory;

/** Note: standard Xerces XML DOM parser **/
import org.apache.xerces.parsers.DOMParser;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

import ner.util.XMLUtils;

public class XMLReader {

	Document _xmlDoc = null;
	XPath    _xpath = XPathFactory.newInstance().newXPath();
	
	public XMLReader(String str, String type) throws Exception{
		if(type=="FILE"){
			XMLFileReader(str);
		}
		if(type=="MEMORY"){
			XMLMemReader(str);
		}
	}
	
	public void XMLFileReader(String url) throws Exception {
        /**
         * Parse the input url
         */
        DOMParser parser = new DOMParser();
        InputStream byteStream = ner.util.InputStreamLoader.OpenStream(url);
        parser.parse(new org.xml.sax.InputSource(byteStream));
        _xmlDoc = parser.getDocument();
	}
	
	public void XMLMemReader(String xmlfile) throws Exception {
		DOMParser parser = new DOMParser();
		InputStream byteStream = new ByteArrayInputStream(xmlfile.getBytes());
		parser.parse(new org.xml.sax.InputSource(byteStream));
        _xmlDoc = parser.getDocument();		
	}
	
	public void displayXMLContent() {
		XMLUtils.PrintNode(_xmlDoc);
	}

	private ArrayList<String> getXPathQueryResults(String query) {
		
		ArrayList<String> results = new ArrayList<String>();
		
		NodeList nodes = (NodeList)XMLUtils.XPathQuery(_xpath, _xmlDoc, 
				query /* TODO: XPath query */, 
				false /* don't return just a single item */);
		for (int i = 0; i < nodes.getLength(); i++) {
			//String node_value = nodes.item(i).getNodeValue();
			//String text_content = nodes.item(i).getTextContent();
		    //System.out.println(node_value + ": " + text_content);
			results.add(nodes.item(i).getTextContent());
		}
		
		return results;
	}
	
	/**
	 * @param args
	 */
	public static void main(String[] args) throws Exception {

		String url = "http://feeds.smh.com.au/rssheadlines/top.xml";
		if(args.length >= 1) {
			url = args[0];
		} 

		// Read XML content from http stream and display
		XMLReader xml = new XMLReader(url,"FILE");
		xml.displayXMLContent();

		// Extract information from XML via XPath query
		ArrayList<String> links = xml.getXPathQueryResults("//item/link");

		// Display content
		System.out.println("\n============ Links =============");
		for (String link : links)
			System.out.println("Link: " + link);
		System.out.println("================================");		
	}

}
