package edu.stanford.nlp.parser.ensemble.maltparser.core.symbol;

import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.Set;

import org.apache.log4j.Logger;

import edu.stanford.nlp.parser.ensemble.maltparser.core.exception.MaltChainedException;

public interface SymbolTableHandler extends TableHandler {
	public SymbolTable addSymbolTable(String tableName) throws MaltChainedException;
	public SymbolTable addSymbolTable(String tableName, SymbolTable parentTable) throws MaltChainedException;
	public SymbolTable addSymbolTable(String tableName, int columnCategory, String nullValueStrategy) throws MaltChainedException;
	public SymbolTable addSymbolTable(String tableName, int columnCategory, String nullValueStrategy, String rootLabel) throws MaltChainedException;
	public SymbolTable getSymbolTable(String tableName) throws MaltChainedException;
	public Set<String> getSymbolTableNames();
	public void save(OutputStreamWriter osw) throws MaltChainedException;
	public void save(String fileName, String charSet) throws MaltChainedException;
	public void load(InputStreamReader isr) throws MaltChainedException;
	public void load(String fileName, String charSet) throws MaltChainedException;
	public void printSymbolTables(Logger logger) throws MaltChainedException;
	public SymbolTable loadTagset(String fileName, String tableName, String charSet, int columnCategory, String nullValueStrategy) throws MaltChainedException;
	public SymbolTable loadTagset(String fileName, String tableName, String charSet, int columnCategory, String nullValueStrategy, String rootLabel) throws MaltChainedException;
}
