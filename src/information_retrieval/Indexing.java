package information_retrieval;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Date;

import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.IndexWriterConfig.OpenMode;
import org.apache.lucene.search.similarities.BM25Similarity;
import org.apache.lucene.store.FSDirectory;

/**
 * This class builds an index over the set of text documents.
 * @author Vahid Gh 
 */

public class Indexing {

	public static String indexPath = "data/index";
	public static String datasetPath = "../lucene-demo/data/government/documents";
	
	public static void main(String[] args) {
		try {
			System.out.println("Indexing documents..");
			Date start = new Date();
			FSDirectory directory = FSDirectory.open(Paths.get(indexPath));
			IndexWriter writer = new IndexWriter(directory,
					buildIndexWriterConfig());
			indexDocs(writer, datasetPath);
			writer.close();
			Date end = new Date();
			System.out.println((end.getTime() - start.getTime()) / 1000.0
					+ " total seconds");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Builds an IndexWrierConfig file. This parameters are used
	 * while making and index. 
	 * @return The built config file
	 */
	static IndexWriterConfig buildIndexWriterConfig() {
		IndexWriterConfig config;
		// The standard analyzer does three things: 
		// 1- Normalizing, 2- Lower-casing, 3- Removing stopwords 
		config = new IndexWriterConfig(new StandardAnalyzer());
		config.setOpenMode(OpenMode.CREATE);
		// Optional: can be increased for better indexing performance
		config.setRAMBufferSizeMB(512.00);
		// Optional: can be used to change the similarity metric. The same metric should 
		// be used in the IndexSearcher during the search.
		config.setSimilarity(new BM25Similarity());
		return config;
	}

	/** 
	 * Builds an index over the set of documents.
	 * @param writer The writer object used for adding the documents to the index
	 * @param path The path of the files/folders to be indexed
	 */
	static void indexDocs(IndexWriter writer, String path) {
		File file = new File(path);
		if (file.isDirectory()) {
			System.out.println(" indexing dir " + file.getPath());
			for (File f : file.listFiles()) {
				indexDocs(writer, f.getAbsolutePath());
			}
		} else { // file is not a directory
			indexFile(writer, file);
		}
	}

	/**
	 * Adds a single file to the index.
	 */
	static void indexFile(IndexWriter writer, File file) {
		try (InputStream fis = Files.newInputStream(file.toPath())) {
			Document doc = new Document();
			doc.add(new StringField("name", file.getName(), Field.Store.YES));
			doc.add(new StringField("path", file.getPath(), Field.Store.YES));
			doc.add(new TextField("content", new BufferedReader(
					new InputStreamReader(fis, StandardCharsets.UTF_8))));
			writer.addDocument(doc);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
