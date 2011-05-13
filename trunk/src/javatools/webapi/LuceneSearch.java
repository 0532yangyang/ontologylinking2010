package javatools.webapi;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javatools.filehandlers.DelimitedWriter;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.queryParser.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Version;

public class LuceneSearch {
	IndexSearcher searcher;
	Analyzer analyzer;
	String field = "contents";
	QueryParser parser;

	public LuceneSearch(String dirIndex) {
		try {
			searcher = new IndexSearcher(FSDirectory.open(new File(dirIndex)));
			analyzer = new StandardAnalyzer(Version.LUCENE_31);
			parser = new QueryParser(Version.LUCENE_31, field, analyzer);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static List<String[]> doPagingSearch2(IndexSearcher searcher, Query query, int hitsPerPage)
			throws IOException {
		List<String[]> searchresult = new ArrayList<String[]>();
		// Collect enough docs to show 5 pages
		TopDocs results = searcher.search(query, hitsPerPage);
		ScoreDoc[] hits = results.scoreDocs;

		int numTotalHits = results.totalHits;
		//System.out.println(numTotalHits + " total matching documents");

		int start = 0;
		int end = Math.min(numTotalHits, hitsPerPage);

		for (int i = 0; i < numTotalHits && i < hits.length; i++) {
			{
				Document doc = searcher.doc(hits[i].doc);
				String path = doc.get("path");
				if (path != null) {
					//System.out.println((i + 1) + ". " + path);
					String contents = doc.get("contents");
					searchresult.add(new String[] { path, contents });
					if (contents != null) {
						//System.out.println("   Contents: " + contents);
					}
				} else {
					//System.out.println((i + 1) + ". " + "No path for this document");
				}
			}
		}
		return searchresult;
	}

	public List<String[]> search(String str_query,int size) {
		String field = "contents";
		String queries = null;
		int repeat = 0;
		boolean raw = false;
		String queryString = null;
		try {
			Query query = parser.parse(str_query);
			//System.out.println("Searching for: " + query.toString(field));
			List<String[]> results = doPagingSearch2(searcher, query, size);
			return results;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	public static void searchDelimitedFile(String dirIndex, List<String> list_query, String output) {
		String index = dirIndex;
		String field = "contents";
		String queries = null;
		int repeat = 0;
		boolean raw = false;
		String queryString = null;
		int hitsPerPage = 10;
		try {
			DelimitedWriter dw = new DelimitedWriter(output);
			IndexSearcher searcher = new IndexSearcher(FSDirectory.open(new File(index)));
			Analyzer analyzer = new StandardAnalyzer(Version.LUCENE_31);
			QueryParser parser = new QueryParser(Version.LUCENE_31, field, analyzer);
			for (String line : list_query) {

				Query query = parser.parse(line);
				System.out.println("Searching for: " + query.toString(field));

				if (repeat > 0) { // repeat & time as benchmark
					Date start = new Date();
					for (int i = 0; i < repeat; i++) {
						searcher.search(query, null, 100);
					}
					Date end = new Date();
					System.out.println("Time: " + (end.getTime() - start.getTime()) + "ms");
				}
				List<String[]> results = doPagingSearch2(searcher, query, hitsPerPage);
				for (String[] r : results) {
					String[] ab = r[0].split("\t");

					dw.write(line, ab[0], ab[1], ab[2], r[1]);
				}
				//doPagingSearch2(in, searcher, query, hitsPerPage, raw, queries == null && queryString == null);
			}
			dw.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
