package information_retrieval;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Paths;
import java.text.DecimalFormat;
import java.util.Date;
import java.util.List;

import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.search.similarities.BM25Similarity;
import org.apache.lucene.store.FSDirectory;

/**
 * This class runs a set of queries on an inverted index.
 * @author vahid
 *
 */

public class Searching {

	public static void main(String[] args) {
		String indexPath = Indexing.indexPath;
		List<QueryDAO> queries = QueryDAO
				.loadQueries("data/government/topics/gov.topics");
		File result = new File("data/result_bm.txt");
		if (result.exists()) {
			result.delete();
		}
		try (IndexReader reader = DirectoryReader.open(FSDirectory.open(Paths
				.get(indexPath)))) {
			System.out.println("Searching for queries...");
			Date start = new Date();
			IndexSearcher searcher = new IndexSearcher(reader);
			searcher.setSimilarity(new BM25Similarity());
			DecimalFormat df = new DecimalFormat("#.####");
			QueryParser parser = new QueryParser("content",
					new StandardAnalyzer());
			for (QueryDAO myQuery : queries) {
				Query query = null;
				query = parser.parse(QueryParser.escape(myQuery.getText()));
				PrintStream ps = new PrintStream(new FileOutputStream(result,
						true));
				TopDocs topDocs = searcher.search(query, 1000);
				ScoreDoc[] hits = topDocs.scoreDocs;
				for (int i = 0; i < hits.length; i++) {
					int docId = hits[i].doc;
					Document d = searcher.doc(docId);
					ps.append(myQuery.getId() + " Q0 " + d.get("name") + " " + i
							+ " " + df.format(hits[i].score) + " "
							+ "your_name\r\n");
				}
				ps.close();
			}
			Date end = new Date();
			System.out.println((end.getTime() - start.getTime()) / 1000.0
					+ " total seconds");
		} catch (IOException e) {
			e.printStackTrace();
		} catch (ParseException e) {
			e.printStackTrace();
		}
	}

}
