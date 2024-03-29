package edu.stanford.nlp.parser.ensemble.maltparser.core.symbol;

import edu.stanford.nlp.parser.ensemble.maltparser.core.exception.MaltChainedException;

public interface Table {
	public int addSymbol(String symbol) throws MaltChainedException;
	public String getSymbolCodeToString(int code) throws MaltChainedException;
	public int getSymbolStringToCode(String symbol) throws MaltChainedException;
	public String getName();
	public int size();
}
