package javatools.webapi;

/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.NumericField;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig.OpenMode;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryParser.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Version;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javatools.administrative.D;
import javatools.filehandlers.DelimitedReader;
import javatools.filehandlers.DelimitedWriter;

/** Index all text files under a directory. See http://lucene.apache.org/java/3_1/demo.html. */
public class LuceneIndexFiles {

	public static void indexDelimitedFile(String file, int indexColumn, int pathColumn, String dirIndex) {
		Date start = new Date();

		try {
			if ((new File(dirIndex)).exists()) {
				(new File(dirIndex)).delete();
			}
			Directory dir = FSDirectory.open(new File(dirIndex));
			Analyzer analyzer = new StandardAnalyzer(Version.LUCENE_31);
			IndexWriterConfig iwc = new IndexWriterConfig(Version.LUCENE_31, analyzer);
			iwc.setOpenMode(OpenMode.CREATE);

			// Optional: for better indexing performance, if you
			// are indexing many documents, increase the RAM
			// buffer.  But if you do this, increase the max heap
			// size to the JVM (eg add -Xmx512m or -Xmx1g):
			//
			// iwc.setRAMBufferSizeMB(256.0);

			IndexWriter writer = new IndexWriter(dir, iwc);
			indexDocs(writer, file, indexColumn, pathColumn);

			// NOTE: if you want to maximize search performance,
			// you can optionally call optimize here.  This can be
			// a costly operation, so generally it's only worth
			// it when your index is relatively static (ie you're
			// done adding documents to it):
			//
			// writer.optimize();

			writer.close();

			Date end = new Date();
			System.out.println(end.getTime() - start.getTime() + " total milliseconds");

		} catch (IOException e) {
			System.out.println(" caught a " + e.getClass() + "\n with message: " + e.getMessage());
		}
	}

	public static void indexDelimitedFile(String file, int indexColumn, int[] pathColumn, String dirIndex) {
		Date start = new Date();

		try {
			if ((new File(dirIndex)).exists()) {
				(new File(dirIndex)).delete();
			}
			Directory dir = FSDirectory.open(new File(dirIndex));
			Analyzer analyzer = new StandardAnalyzer(Version.LUCENE_31);
			IndexWriterConfig iwc = new IndexWriterConfig(Version.LUCENE_31, analyzer);
			iwc.setOpenMode(OpenMode.CREATE);

			// Optional: for better indexing performance, if you
			// are indexing many documents, increase the RAM
			// buffer.  But if you do this, increase the max heap
			// size to the JVM (eg add -Xmx512m or -Xmx1g):
			//
			// iwc.setRAMBufferSizeMB(256.0);

			IndexWriter writer = new IndexWriter(dir, iwc);
			indexDocs(writer, file, indexColumn, pathColumn);

			// NOTE: if you want to maximize search performance,
			// you can optionally call optimize here.  This can be
			// a costly operation, so generally it's only worth
			// it when your index is relatively static (ie you're
			// done adding documents to it):
			//
			// writer.optimize();

			writer.close();

			Date end = new Date();
			System.out.println(end.getTime() - start.getTime() + " total milliseconds");

		} catch (IOException e) {
			System.out.println(" caught a " + e.getClass() + "\n with message: " + e.getMessage());
		}
	}

	public static void main(String[] args) {
		//	indexDelimitedFile
	}

	/** Index all text files under a directory. */
	public static void main2(String[] args) {
		String usage = "java org.apache.lucene.demo.IndexFiles"
				+ " [-index INDEX_PATH] [-docs DOCS_PATH] [-update]\n\n"
				// TODO: Change the link with every release (or: fill in some less error-prone alternative here...)
				+ "See http://lucene.apache.org/java/3_1/demo.html for details.";
		String indexPath = "index";
		String docsPath = null;

		boolean create = true;
		for (int i = 0; i < args.length; i++) {
			if ("-index".equals(args[i])) {
				indexPath = args[i + 1];
				i++;
			} else if ("-docs".equals(args[i])) {
				docsPath = args[i + 1];
				i++;
			} else if ("-update".equals(args[i])) {
				create = false;
			}
		}

		if (docsPath == null) {
			System.err.println("Usage: " + usage);
			System.exit(1);
		}

		final File docDir = new File(docsPath);
		if (!docDir.exists() || !docDir.canRead()) {
			System.out.println("Document directory '" + docDir.getAbsolutePath()
					+ "' does not exist or is not readable, please check the path");
			System.exit(1);
		}

		Date start = new Date();
		try {
			System.out.println("Indexing to directory '" + indexPath + "'...");

			Directory dir = FSDirectory.open(new File(indexPath));
			Analyzer analyzer = new StandardAnalyzer(Version.LUCENE_31);
			IndexWriterConfig iwc = new IndexWriterConfig(Version.LUCENE_31, analyzer);

			if (create) {
				// Create a new index in the directory, removing any
				// previously indexed documents:
				iwc.setOpenMode(OpenMode.CREATE);
			} else {
				// Add new documents to an existing index:
				iwc.setOpenMode(OpenMode.CREATE_OR_APPEND);
			}

			// Optional: for better indexing performance, if you
			// are indexing many documents, increase the RAM
			// buffer.  But if you do this, increase the max heap
			// size to the JVM (eg add -Xmx512m or -Xmx1g):
			//
			// iwc.setRAMBufferSizeMB(256.0);

			IndexWriter writer = new IndexWriter(dir, iwc);
			indexDocs(writer, docDir);

			// NOTE: if you want to maximize search performance,
			// you can optionally call optimize here.  This can be
			// a costly operation, so generally it's only worth
			// it when your index is relatively static (ie you're
			// done adding documents to it):
			//
			// writer.optimize();

			writer.close();

			Date end = new Date();
			System.out.println(end.getTime() - start.getTime() + " total milliseconds");

		} catch (IOException e) {
			System.out.println(" caught a " + e.getClass() + "\n with message: " + e.getMessage());
		}
	}

	/**
	 * Indexes the given file using the given writer, or if a directory is given,
	 * recurses over files and directories found under the given directory.
	 * 
	 * NOTE: This method indexes one document per input file.  This is slow.  For good
	 * throughput, put multiple documents into your input file(s).  An example of this is
	 * in the benchmark module, which can create "line doc" files, one document per line,
	 * using the
	 * <a href="../../../../../contrib-benchmark/org/apache/lucene/benchmark/byTask/tasks/WriteLineDocTask.html"
	 * >WriteLineDocTask</a>.
	 *  
	 * @param writer Writer to the index where the given file/dir info will be stored
	 * @param file The file to index, or the directory to recurse into to find files to index
	 * @throws IOException
	 */
	static void indexDocs(IndexWriter writer, File file) throws IOException {
		// do not try to index files that cannot be read
		if (file.canRead()) {
			if (file.isDirectory()) {
				String[] files = file.list();
				// an IO error could occur
				if (files != null) {
					for (int i = 0; i < files.length; i++) {
						indexDocs(writer, new File(file, files[i]));
					}
				}
			} else {

				FileInputStream fis;
				try {
					fis = new FileInputStream(file);
				} catch (FileNotFoundException fnfe) {
					// at least on windows, some temporary files raise this exception with an "access denied" message
					// checking if the file can be read doesn't help
					return;
				}

				try {

					// make a new, empty document
					Document doc = new Document();

					// Add the path of the file as a field named "path".  Use a
					// field that is indexed (i.e. searchable), but don't tokenize 
					// the field into separate words and don't index term frequency
					// or positional information:
					Field pathField = new Field("path", file.getPath(), Field.Store.YES,
							Field.Index.NOT_ANALYZED_NO_NORMS);
					pathField.setOmitTermFreqAndPositions(true);
					doc.add(pathField);

					// Add the last modified date of the file a field named "modified".
					// Use a NumericField that is indexed (i.e. efficiently filterable with
					// NumericRangeFilter).  This indexes to milli-second resolution, which
					// is often too fine.  You could instead create a number based on
					// year/month/day/hour/minutes/seconds, down the resolution you require.
					// For example the long value 2011021714 would mean
					// February 17, 2011, 2-3 PM.
					NumericField modifiedField = new NumericField("modified");
					modifiedField.setLongValue(file.lastModified());
					doc.add(modifiedField);

					// Add the contents of the file to a field named "contents".  Specify a Reader,
					// so that the text of the file is tokenized and indexed, but not stored.
					// Note that FileReader expects the file to be in UTF-8 encoding.
					// If that's not the case searching for special characters will fail.
					doc.add(new Field("contents", new BufferedReader(new InputStreamReader(fis, "UTF-8"))));

					if (writer.getConfig().getOpenMode() == OpenMode.CREATE) {
						// New index, so we just add the document (no old document can be there):
						System.out.println("adding " + file);
						writer.addDocument(doc);
					} else {
						// Existing index (an old copy of this document may have been indexed) so 
						// we use updateDocument instead to replace the old one matching the exact 
						// path, if present:
						System.out.println("updating " + file);
						writer.updateDocument(new Term("path", file.getPath()), doc);
					}

				} finally {
					fis.close();
				}
			}
		}
	}

	static void indexDocs(IndexWriter writer, String file, int indexColumn, int pathColumn) throws IOException {
		DelimitedReader dr = new DelimitedReader(file);
		String[] l;
		while ((l = dr.read()) != null) {
			Document doc = new Document();
			String path = l[pathColumn];
			Field pathField = new Field("path", path, Field.Store.YES, Field.Index.NOT_ANALYZED_NO_NORMS);
			pathField.setOmitTermFreqAndPositions(true);
			doc.add(pathField);
			doc.add(new Field("contents", new StringReader(l[indexColumn])));
			writer.addDocument(doc);
		}
		dr.close();

	}

	static void indexDocs(IndexWriter writer, String file, int indexColumn, int[] pathColumn) throws IOException {
		DelimitedReader dr = new DelimitedReader(file);
		String[] l;
		int count = 0;
		while ((l = dr.read()) != null) {
			//			if (count++ > 10000)
			//				break;
			Document doc = new Document();
			StringBuilder sbp = new StringBuilder();
			for (int a : pathColumn) {
				String path = l[a];
				sbp.append(path).append("\t");
			}
			Field pathField = new Field("path", sbp.toString(), Field.Store.YES, Field.Index.NOT_ANALYZED_NO_NORMS);
			pathField.setOmitTermFreqAndPositions(true);
			doc.add(pathField);
			//doc.add(new Field("contents", new StringReader(l[indexColumn])));
			doc.add(new Field("contents", l[indexColumn], Field.Store.YES, Field.Index.ANALYZED));
			writer.addDocument(doc);
		}
		dr.close();

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

	/** Simple command-line based search demo. */
	public static void main_search(String[] args) throws Exception {
		String usage = "Usage:\tjava org.apache.lucene.demo.SearchFiles [-index dir] [-field f] [-repeat n] [-queries file] [-query string] [-raw] [-paging hitsPerPage]\n\nSee http://lucene.apache.org/java/4_0/demo.html for details.";
		if (args.length > 0 && ("-h".equals(args[0]) || "-help".equals(args[0]))) {
			System.out.println(usage);
			System.exit(0);
		}

		String index = "index";
		String field = "contents";
		String queries = null;
		int repeat = 0;
		boolean raw = false;
		String queryString = null;
		int hitsPerPage = 10;

		for (int i = 0; i < args.length; i++) {
			if ("-index".equals(args[i])) {
				index = args[i + 1];
				i++;
			} else if ("-field".equals(args[i])) {
				field = args[i + 1];
				i++;
			} else if ("-queries".equals(args[i])) {
				queries = args[i + 1];
				i++;
			} else if ("-query".equals(args[i])) {
				queryString = args[i + 1];
				i++;
			} else if ("-repeat".equals(args[i])) {
				repeat = Integer.parseInt(args[i + 1]);
				i++;
			} else if ("-raw".equals(args[i])) {
				raw = true;
			} else if ("-paging".equals(args[i])) {
				hitsPerPage = Integer.parseInt(args[i + 1]);
				if (hitsPerPage <= 0) {
					System.err.println("There must be at least 1 hit per page.");
					System.exit(1);
				}
				i++;
			}
		}

		IndexSearcher searcher = new IndexSearcher(FSDirectory.open(new File(index)));
		Analyzer analyzer = new StandardAnalyzer(Version.LUCENE_31);

		BufferedReader in = null;
		if (queries != null) {
			in = new BufferedReader(new InputStreamReader(new FileInputStream(queries), "UTF-8"));
		} else {
			in = new BufferedReader(new InputStreamReader(System.in, "UTF-8"));
		}
		QueryParser parser = new QueryParser(Version.LUCENE_31, field, analyzer);
		while (true) {
			if (queries == null && queryString == null) { // prompt the user
				System.out.println("Enter query: ");
			}

			String line = queryString != null ? queryString : in.readLine();

			if (line == null || line.length() == -1) {
				break;
			}

			line = line.trim();
			if (line.length() == 0) {
				break;
			}

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

			doPagingSearch(in, searcher, query, hitsPerPage, raw, queries == null && queryString == null);

			if (queryString != null) {
				break;
			}
		}
		searcher.close();
	}

	/**
	 * This demonstrates a typical paging search scenario, where the search engine presents 
	 * pages of size n to the user. The user can then go to the next page if interested in
	 * the next hits.
	 * 
	 * When the query is executed for the first time, then only enough results are collected
	 * to fill 5 result pages. If the user wants to page beyond this limit, then the query
	 * is executed another time and all hits are collected.
	 * 
	 */
	public static void doPagingSearch(BufferedReader in, IndexSearcher searcher, Query query, int hitsPerPage,
			boolean raw, boolean interactive) throws IOException {

		// Collect enough docs to show 5 pages
		TopDocs results = searcher.search(query, 5 * hitsPerPage);
		ScoreDoc[] hits = results.scoreDocs;

		int numTotalHits = results.totalHits;
		System.out.println(numTotalHits + " total matching documents");

		int start = 0;
		int end = Math.min(numTotalHits, hitsPerPage);

		while (true) {
			if (end > hits.length) {
				System.out.println("Only results 1 - " + hits.length + " of " + numTotalHits
						+ " total matching documents collected.");
				System.out.println("Collect more (y/n) ?");
				String line = in.readLine();
				if (line.length() == 0 || line.charAt(0) == 'n') {
					break;
				}

				hits = searcher.search(query, numTotalHits).scoreDocs;
			}

			end = Math.min(hits.length, start + hitsPerPage);

			for (int i = start; i < end; i++) {
				if (raw) { // output raw format
					System.out.println("doc=" + hits[i].doc + " score=" + hits[i].score);
					continue;
				}

				Document doc = searcher.doc(hits[i].doc);
				String path = doc.get("path");
				if (path != null) {
					System.out.println((i + 1) + ". " + path);
					String title = doc.get("title");
					if (title != null) {
						System.out.println("   Title: " + doc.get("title"));
					}
				} else {
					System.out.println((i + 1) + ". " + "No path for this document");
				}

			}

			if (!interactive || end == 0) {
				break;
			}

			if (numTotalHits >= end) {
				boolean quit = false;
				while (true) {
					System.out.print("Press ");
					if (start - hitsPerPage >= 0) {
						System.out.print("(p)revious page, ");
					}
					if (start + hitsPerPage < numTotalHits) {
						System.out.print("(n)ext page, ");
					}
					System.out.println("(q)uit or enter number to jump to a page.");

					String line = in.readLine();
					if (line.length() == 0 || line.charAt(0) == 'q') {
						quit = true;
						break;
					}
					if (line.charAt(0) == 'p') {
						start = Math.max(0, start - hitsPerPage);
						break;
					} else if (line.charAt(0) == 'n') {
						if (start + hitsPerPage < numTotalHits) {
							start += hitsPerPage;
						}
						break;
					} else {
						int page = Integer.parseInt(line);
						if ((page - 1) * hitsPerPage < numTotalHits) {
							start = (page - 1) * hitsPerPage;
							break;
						} else {
							System.out.println("No such page");
						}
					}
				}
				if (quit)
					break;
				end = Math.min(numTotalHits, start + hitsPerPage);
			}
		}
	}

	public static List<String[]> doPagingSearch2(IndexSearcher searcher, Query query, int hitsPerPage)
			throws IOException {
		List<String[]> searchresult = new ArrayList<String[]>();
		// Collect enough docs to show 5 pages
		TopDocs results = searcher.search(query, 10 * hitsPerPage);
		ScoreDoc[] hits = results.scoreDocs;

		int numTotalHits = results.totalHits;
		System.out.println(numTotalHits + " total matching documents");

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
}
