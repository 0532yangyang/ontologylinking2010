package edu.stanford.nlp.parser.ensemble.maltparser.core.syntaxgraph.headrules;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.HashMap;

import org.apache.log4j.Logger;

import edu.stanford.nlp.parser.ensemble.maltparser.core.exception.MaltChainedException;
import edu.stanford.nlp.parser.ensemble.maltparser.core.helper.Util;
import edu.stanford.nlp.parser.ensemble.maltparser.core.io.dataformat.DataFormatInstance;
import edu.stanford.nlp.parser.ensemble.maltparser.core.symbol.SymbolTable;
import edu.stanford.nlp.parser.ensemble.maltparser.core.syntaxgraph.node.NonTerminalNode;
import edu.stanford.nlp.parser.ensemble.maltparser.core.syntaxgraph.node.PhraseStructureNode;

/**
*
*
* @author Johan Hall
*/
public class HeadRules extends HashMap<String,HeadRule> {
	public static final long serialVersionUID = 8045568022124826323L;
	protected Logger logger;
	protected String name;
	protected DataFormatInstance dataFormatInstance;
	protected SymbolTable nonTerminalSymbolTable; // TODO more complex
	protected SymbolTable edgelabelSymbolTable; // TODO more complex
	
	public HeadRules(Logger logger, DataFormatInstance dataFormatInstance) throws MaltChainedException {
		setLogger(logger);
		setDataFormatInstance(dataFormatInstance);
		nonTerminalSymbolTable = dataFormatInstance.getSymbolTables().addSymbolTable("CAT");
		edgelabelSymbolTable = dataFormatInstance.getSymbolTables().addSymbolTable("LABEL");
	}
	
	public void parseHeadRules(String fileName) throws MaltChainedException {
		parseHeadRules(Util.findURL(fileName));
	}
	
	public void parseHeadRules(URL url) throws MaltChainedException {
		BufferedReader br = null;
		try {
			br = new BufferedReader(new InputStreamReader(url.openStream()));
		} catch (IOException e) {
			throw new HeadRuleException("Could not read the head rules from file '"+url.toString()+"'. ", e);
		}
		if (logger.isInfoEnabled()) {
			logger.debug("Loading the head rule specification '"+url.toString()+"' ...\n");
		}
		String fileLine;
		while (true) {
			try {
				fileLine = br.readLine();
			} catch (IOException e) {
				throw new HeadRuleException("Could not read the head rules from file '"+url.toString()+"'. ", e);
			}
			if (fileLine == null) {
				break;
			}
			if (fileLine.length() <= 1 && fileLine.trim().substring(0, 2).trim().equals("--")) {
				continue;
			}
			int index = fileLine.indexOf('\t');
			if (index == -1) {
				throw new HeadRuleException("The specification of the head rule is not correct '"+fileLine+"'. ");
			}
			
			HeadRule rule = new HeadRule(this, fileLine);
			put(fileLine.substring(0,index), rule);
		}
	}
	
	public PhraseStructureNode getHeadChild(NonTerminalNode nt) throws MaltChainedException {
		HeadRule rule = null;
		if (nt.hasLabel(nonTerminalSymbolTable)) {
			rule = this.get(nonTerminalSymbolTable.getName()+":"+nt.getLabelSymbol(nonTerminalSymbolTable));
		}
		if (rule == null && nt.hasParentEdgeLabel(edgelabelSymbolTable)) {
			rule = this.get(edgelabelSymbolTable.getName()+":"+nt.getParentEdgeLabelSymbol(edgelabelSymbolTable));
		}
		
		if (rule != null) {
			return rule.getHeadChild(nt);
		}
		return null;
	}
	
	public Direction getDefaultDirection(NonTerminalNode nt) throws MaltChainedException {
		HeadRule rule = null;
		if (nt.hasLabel(nonTerminalSymbolTable)) {
			rule = this.get(nonTerminalSymbolTable.getName()+":"+nt.getLabelSymbol(nonTerminalSymbolTable));
		}
		if (rule == null && nt.hasParentEdgeLabel(edgelabelSymbolTable)) {
			rule = this.get(edgelabelSymbolTable.getName()+":"+nt.getParentEdgeLabelSymbol(edgelabelSymbolTable));
		}

		if (rule != null) {
			return rule.getDefaultDirection();
		}
		return Direction.LEFT;
	}
	
	public Logger getLogger() {
		return logger;
	}
	
	public void setLogger(Logger logger) {
		this.logger = logger;
	}
	
	public DataFormatInstance getDataFormatInstance() {
		return dataFormatInstance;
	}

	public void setDataFormatInstance(DataFormatInstance dataFormatInstance) {
		this.dataFormatInstance = dataFormatInstance;
	}

	public String toString() {
		final StringBuilder sb = new StringBuilder();
		for (HeadRule rule : this.values()) {
			sb.append(rule);
			sb.append('\n');
		}
		return sb.toString();
	}
}
