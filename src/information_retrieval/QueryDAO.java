package information_retrieval;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * This is a DAO class used for handling queries. 
 * @author vahid
 *
 */

public class QueryDAO {

	private String id;

	private String text;

	/**
	 * Loads the queries from a text file into a list.
	 */
	public static List<QueryDAO> loadQueries(String filename) {
		List<QueryDAO> queries = new ArrayList<QueryDAO>();
		BufferedReader fr = null;
		try {
			fr = new BufferedReader(new FileReader(filename));
			String line = fr.readLine();
			while (line != null) {
				Pattern pr = Pattern.compile("(\\d+) (.*)");
				Matcher mr = pr.matcher(line);
				mr.find();
				String qid = mr.group(1);
				String text = mr.group(2);
				queries.add(new QueryDAO(qid, text));
				line = fr.readLine();
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (fr != null)
				try {
					fr.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
		}
		return queries;
	}

	public QueryDAO(String id, String text) {
		this.text = text;
		this.id = id;
	}

	public String getId() {
		return id;
	}

	public String getText() {
		return text;
	}

	public void setText(String text) {
		this.text = text;
	}

}
