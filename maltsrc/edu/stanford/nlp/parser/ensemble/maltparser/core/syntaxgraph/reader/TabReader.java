package edu.stanford.nlp.parser.ensemble.maltparser.core.syntaxgraph.reader;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.util.Iterator;
import java.util.zip.GZIPInputStream;


import edu.stanford.nlp.parser.ensemble.maltparser.core.exception.MaltChainedException;
import edu.stanford.nlp.parser.ensemble.maltparser.core.io.dataformat.ColumnDescription;
import edu.stanford.nlp.parser.ensemble.maltparser.core.io.dataformat.DataFormatException;
import edu.stanford.nlp.parser.ensemble.maltparser.core.io.dataformat.DataFormatInstance;
import edu.stanford.nlp.parser.ensemble.maltparser.core.syntaxgraph.DependencyStructure;
import edu.stanford.nlp.parser.ensemble.maltparser.core.syntaxgraph.Element;
import edu.stanford.nlp.parser.ensemble.maltparser.core.syntaxgraph.TokenStructure;
import edu.stanford.nlp.parser.ensemble.maltparser.core.syntaxgraph.edge.Edge;
/**
*
*
* @author Johan Hall
*/
public class TabReader implements SyntaxGraphReader {
	private BufferedReader reader;
	private int sentenceCount;
	private final StringBuilder input;
	private DataFormatInstance dataFormatInstance;
	private static final String IGNORE_COLUMN_SIGN = "-"; // mihai: changed from _ to -
	private static final char TAB = '\t';
	private static final char NEWLINE = '\n';
	private static final char CARRIAGE_RETURN = '\r';
	private String fileName = null;
	private URL url = null;
	private String charsetName;
	private int nIterations;
	private int cIterations;
	
	
	public TabReader() { 
		input = new StringBuilder();
		nIterations = 1;
		cIterations = 1;
	}
	
	private void reopen() throws MaltChainedException {
		close();
		if (fileName != null) {
			open(fileName, charsetName);
		} else if (url != null) {
			open(url, charsetName);
		} else {
			throw new DataFormatException("The input stream cannot be reopen. ");
		}
	}
	
	public void open(String fileName, String charsetName) throws MaltChainedException {
		setFileName(fileName);
		setCharsetName(charsetName);
		try {
			// mihai: detect .gz files on the fly
			InputStream is = new FileInputStream(fileName);
			if(fileName.endsWith(".gz")) is = new GZIPInputStream(is);
			open(is, charsetName);
		} catch (FileNotFoundException e) {
			throw new DataFormatException("The input file '"+fileName+"' cannot be found. ", e);
		} catch(IOException e) {
			throw new DataFormatException("The input file '"+fileName+"' cannot be gzipped. ", e);
		}
	}
	
	public void open(URL url, String charsetName) throws MaltChainedException {
		setUrl(url);
		setCharsetName(charsetName);
		if (url == null) {
			throw new DataFormatException("The input file cannot be found. ");
		}
		try {
			open(url.openStream(), charsetName);
		} catch (IOException e) {
			throw new DataFormatException("The URL '"+url.toString()+"' cannot be opened. ", e);
		}
	}
	
	public void open(InputStream is, String charsetName) throws MaltChainedException {
		try {
			open(new InputStreamReader(is, charsetName));
		} catch (UnsupportedEncodingException e) {
			throw new DataFormatException("The character encoding set '"+charsetName+"' isn't supported. ", e);
		}
	}
	
	public void open(InputStreamReader isr) throws MaltChainedException {
		setReader(new BufferedReader(isr));
		setSentenceCount(0);
	}
	
	public void readProlog() throws MaltChainedException {
		
	}
	
	public boolean readSentence(TokenStructure syntaxGraph) throws MaltChainedException  {
		if (syntaxGraph == null || dataFormatInstance == null) {
			return false;
		}
		
		Element node = null;
		Edge edge = null;
		input.setLength(0);
		int i = 0;
		int terminalCounter = 0;
		int nNewLines = 0;
		syntaxGraph.clear();
		Iterator<ColumnDescription> columns = dataFormatInstance.iterator();
		while (true) {
			int c;

			try {
				c = reader.read();
			} catch (IOException e) {
				close();
				throw new DataFormatException("Error when reading from the input file. ", e);
			}
			if (c == TAB || c == NEWLINE || c == CARRIAGE_RETURN || c == -1) {
				if (input.length() != 0) {					
					if (i == 0) {
						terminalCounter++;
						node = syntaxGraph.addTokenNode(terminalCounter);
					}
					ColumnDescription column = null;
					if (columns.hasNext()) {
						column = columns.next();
						if (column.getCategory() == ColumnDescription.INPUT && node != null) {
							syntaxGraph.addLabel(node, column.getName(), input.toString());
						} else if (column.getCategory() == ColumnDescription.HEAD) {
							if (syntaxGraph instanceof DependencyStructure) {
								if (column.getType() != ColumnDescription.IGNORE && !input.toString().equals(IGNORE_COLUMN_SIGN)) { // bugfix
								//if (!input.toString().equals(IGNORE_COLUMN_SIGN)) {
									edge = ((DependencyStructure)syntaxGraph).addDependencyEdge(Integer.parseInt(input.toString()), terminalCounter);
								}
							} 
							else {
								close();
								throw new DataFormatException("The input graph is not a dependency graph and therefore it is not possible to add dependncy edges. ");
							}
						} else if (column.getCategory() == ColumnDescription.DEPENDENCY_EDGE_LABEL && edge != null) {
							if (column.getType() != ColumnDescription.IGNORE && !input.toString().equals(IGNORE_COLUMN_SIGN)) { // bugfix
								//System.err.println("LABEL = " + input.toString());
								syntaxGraph.addLabel(edge, column.getName(), input.toString().toLowerCase());
							} // bugfix
						}
					}
					input.setLength(0);
					nNewLines = 0;
					i++;
				}
				if (c == NEWLINE) {
					nNewLines++;
					i = 0;
					columns = dataFormatInstance.iterator();
				}
			} else {
				input.append((char)c);
			}
			
			if (nNewLines == 2 && c == NEWLINE) {
				if (syntaxGraph.hasTokens()) {
					sentenceCount++;
				}
				return true;
			} else if (c == -1) {
				if (syntaxGraph.hasTokens()) {
					sentenceCount++;
				}
				if (cIterations < nIterations) {
					cIterations++;
					reopen();
					return true;
				}
				
				return false;					
			}
		}
	}
	
	public void readEpilog() throws MaltChainedException {
		
	}
	
	public BufferedReader getReader() {
		return reader;
	}

	public void setReader(BufferedReader reader) throws MaltChainedException {
		close();
		this.reader = reader;
	}
	
	public DataFormatInstance getDataFormatInstance() {
		return dataFormatInstance;
	}

	public void setDataFormatInstance(DataFormatInstance dataFormatInstance) {
		this.dataFormatInstance = dataFormatInstance;
	}

	public int getSentenceCount() throws MaltChainedException {
		return sentenceCount;
	}
	
	public void setSentenceCount(int sentenceCount) {
		this.sentenceCount = sentenceCount;
	}
	
	public String getOptions() {
		return null;
	}
	
	public void setOptions(String optionString) throws MaltChainedException {
		
	}
	
	public String getFileName() {
		return fileName;
	}

	public void setFileName(String fileName) {
		this.fileName = fileName;
	}

	public URL getUrl() {
		return url;
	}

	public void setUrl(URL url) {
		this.url = url;
	}

	public String getCharsetName() {
		return charsetName;
	}

	public void setCharsetName(String charsetName) {
		this.charsetName = charsetName;
	}

	public int getNIterations() {
		return nIterations;
	}

	public void setNIterations(int iterations) {
		nIterations = iterations;
	}

	public int getIterationCounter() {
		return cIterations;
	}

	public void close() throws MaltChainedException {
		try {
			if (reader != null) {
				reader.close();
				reader = null;
			}
		} catch (IOException e) {
			throw new DataFormatException("Error when closing the input file. ", e);
		} 
	}
	
	public void clear() throws MaltChainedException {
		close();
		input.setLength(0);
		dataFormatInstance = null;
		sentenceCount = 0;
	}
}
