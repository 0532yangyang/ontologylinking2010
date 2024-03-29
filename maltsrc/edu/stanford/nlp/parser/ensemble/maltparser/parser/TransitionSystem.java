package edu.stanford.nlp.parser.ensemble.maltparser.parser;

import java.util.HashMap;


import edu.stanford.nlp.parser.ensemble.maltparser.core.exception.MaltChainedException;
import edu.stanford.nlp.parser.ensemble.maltparser.core.symbol.SymbolTable;
import edu.stanford.nlp.parser.ensemble.maltparser.core.symbol.SymbolTableHandler;
import edu.stanford.nlp.parser.ensemble.maltparser.core.symbol.TableHandler;
import edu.stanford.nlp.parser.ensemble.maltparser.core.syntaxgraph.LabelSet;
import edu.stanford.nlp.parser.ensemble.maltparser.core.syntaxgraph.edge.Edge;
import edu.stanford.nlp.parser.ensemble.maltparser.parser.history.GuideUserHistory;
import edu.stanford.nlp.parser.ensemble.maltparser.parser.history.action.GuideUserAction;
import edu.stanford.nlp.parser.ensemble.maltparser.parser.history.container.ActionContainer;
import edu.stanford.nlp.parser.ensemble.maltparser.parser.transition.TransitionTable;
import edu.stanford.nlp.parser.ensemble.maltparser.parser.transition.TransitionTableHandler;
/**
 * @author Johan Hall
 *
 */
public abstract class TransitionSystem {
	protected HashMap<String, TableHandler> tableHandlers;
	protected TransitionTableHandler transitionTableHandler;
	protected ActionContainer[] actionContainers;
	protected ActionContainer transActionContainer;
	protected ActionContainer[] arcLabelActionContainers;
	
	public TransitionSystem() throws MaltChainedException {	}
	
	public abstract void apply(GuideUserAction currentAction, ParserConfiguration config) throws MaltChainedException;
	public abstract boolean permissible(GuideUserAction currentAction, ParserConfiguration config) throws MaltChainedException;
	public abstract GuideUserAction getDeterministicAction(GuideUserHistory history, ParserConfiguration config) throws MaltChainedException;
	protected abstract void addAvailableTransitionToTable(TransitionTable ttable) throws MaltChainedException;
	protected abstract void initWithDefaultTransitions(GuideUserHistory history) throws MaltChainedException;
	public abstract String getName();
	public abstract GuideUserAction defaultAction(GuideUserHistory history, ParserConfiguration configuration) throws MaltChainedException;
	
	protected GuideUserAction updateActionContainers(GuideUserHistory history, int transition, LabelSet arcLabels) throws MaltChainedException {	
		transActionContainer.setAction(transition);

		if (arcLabels == null) {
			for (int i = 0; i < arcLabelActionContainers.length; i++) {
				arcLabelActionContainers[i].setAction(-1);	
			}
		} else {
			for (int i = 0; i < arcLabelActionContainers.length; i++) {
				arcLabelActionContainers[i].setAction(arcLabels.get(arcLabelActionContainers[i].getTable()).shortValue());
			}		
		}
		GuideUserAction oracleAction = history.getEmptyGuideUserAction();
		oracleAction.addAction(actionContainers);
		return oracleAction;
	}
	
	protected boolean isActionContainersLabeled() {
		for (int i = 0; i < arcLabelActionContainers.length; i++) {
			if (arcLabelActionContainers[i].getActionCode() < 0) {
				return false;
			}
		}
		return true;
	}
	
	protected void addEdgeLabels(Edge e) throws MaltChainedException {
		if (e != null) { 
			for (int i = 0; i < arcLabelActionContainers.length; i++) {
				e.addLabel((SymbolTable)arcLabelActionContainers[i].getTable(), arcLabelActionContainers[i].getActionCode());
			}
		}
	}
	
	public void initTransitionSystem(GuideUserHistory history) throws MaltChainedException {
		this.actionContainers = history.getActionContainerArray();
		if (actionContainers.length < 1) {
			throw new ParsingException("Problem when initialize the history (sequence of actions). There are no action containers. ");
		}
		int nLabels = 0;
		for (int i = 0; i < actionContainers.length; i++) {
			if (actionContainers[i].getTableContainerName().startsWith("A.")) {
				nLabels++;
			}
		}
		int j = 0;
		for (int i = 0; i < actionContainers.length; i++) {
			if (actionContainers[i].getTableContainerName().equals("T.TRANS")) {
				transActionContainer = actionContainers[i];
			} else if (actionContainers[i].getTableContainerName().startsWith("A.")) {
				if (arcLabelActionContainers == null) {
					arcLabelActionContainers = new ActionContainer[nLabels];
				}
				arcLabelActionContainers[j++] = actionContainers[i];
			}
		}
		initWithDefaultTransitions(history);
	}
	
	public void initTableHandlers(String decisionSettings, SymbolTableHandler symbolTableHandler) throws MaltChainedException {
		transitionTableHandler = new TransitionTableHandler();
		tableHandlers = new HashMap<String, TableHandler>();
		
		final String[] decisionElements =  decisionSettings.split(",|#|;|\\+");
		
		int nTrans = 0;
		for (int i = 0; i < decisionElements.length; i++) {
			int index = decisionElements[i].indexOf('.');
			if (index == -1) {
				throw new ParsingException("Decision settings '"+decisionSettings+"' contain an item '"+decisionElements[i]+"' that does not follow the format {TableHandler}.{Table}. ");
			}
			if (decisionElements[i].substring(0,index).equals("T")) {
				if (!getTableHandlers().containsKey("T")) {
					getTableHandlers().put("T", getTransitionTableHandler());
				}
				if (decisionElements[i].substring(index+1).equals("TRANS")) {
					if (nTrans == 0) {
						TransitionTable ttable = (TransitionTable)getTransitionTableHandler().addSymbolTable("TRANS");
						addAvailableTransitionToTable(ttable);
					} else {
						throw new ParsingException("Illegal decision settings '"+decisionSettings+"'");
					}
					nTrans++;
				}  
			} else if (decisionElements[i].substring(0,index).equals("A")) {
				if (!getTableHandlers().containsKey("A")) {
					getTableHandlers().put("A", symbolTableHandler);
				}
			} else {
				throw new ParsingException("The decision settings '"+decisionSettings+"' contains an unknown table handler '"+decisionElements[i].substring(0,index)+"'. " +
						"Only T (Transition table handler) and A (ArcLabel table handler) is allowed. ");
			}
		}
	}
	
	public void copyAction(GuideUserAction source, GuideUserAction target) throws MaltChainedException {
		source.getAction(actionContainers);
		target.addAction(actionContainers);
	}
	
	public HashMap<String, TableHandler> getTableHandlers() {
		return tableHandlers;
	}

	public TransitionTableHandler getTransitionTableHandler() {
		return transitionTableHandler;
	}
	
	public String getActionString(GuideUserAction action) throws MaltChainedException {
		StringBuilder sb = new StringBuilder();
		action.getAction(actionContainers);
		TransitionTable ttable = (TransitionTable)getTransitionTableHandler().getSymbolTable("TRANS");
		sb.append(ttable.getSymbolCodeToString(transActionContainer.getActionCode()));
		for (int i = 0; i < arcLabelActionContainers.length; i++) {
			if (arcLabelActionContainers[i].getActionCode() != -1) {
				sb.append(' ');
				sb.append(arcLabelActionContainers[i].getTable().getSymbolCodeToString(arcLabelActionContainers[i].getActionCode()));
			}
		}
		return sb.toString();
	}
}
