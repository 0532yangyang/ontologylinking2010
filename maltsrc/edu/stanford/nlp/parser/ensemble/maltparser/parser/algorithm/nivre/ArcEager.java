package edu.stanford.nlp.parser.ensemble.maltparser.parser.algorithm.nivre;

import java.util.Stack;


import edu.stanford.nlp.parser.ensemble.maltparser.core.exception.MaltChainedException;
import edu.stanford.nlp.parser.ensemble.maltparser.core.syntaxgraph.edge.Edge;
import edu.stanford.nlp.parser.ensemble.maltparser.core.syntaxgraph.node.DependencyNode;
import edu.stanford.nlp.parser.ensemble.maltparser.parser.ParserConfiguration;
import edu.stanford.nlp.parser.ensemble.maltparser.parser.TransitionSystem;
import edu.stanford.nlp.parser.ensemble.maltparser.parser.history.GuideUserHistory;
import edu.stanford.nlp.parser.ensemble.maltparser.parser.history.History;
import edu.stanford.nlp.parser.ensemble.maltparser.parser.history.action.ComplexDecisionAction;
import edu.stanford.nlp.parser.ensemble.maltparser.parser.history.action.GuideUserAction;
import edu.stanford.nlp.parser.ensemble.maltparser.parser.transition.TransitionTable;
/**
 * @author Johan Hall
 *
 */
public class ArcEager extends TransitionSystem {
	protected static final int SHIFT = 1;
	protected static final int REDUCE = 2;
	protected static final int RIGHTARC = 3;
	protected static final int LEFTARC = 4;
	
	public ArcEager() throws MaltChainedException {
		super();
	}
	
	public void apply(GuideUserAction currentAction, ParserConfiguration config) throws MaltChainedException {
		NivreConfig nivreConfig = (NivreConfig)config;
		Stack<DependencyNode> stack = nivreConfig.getStack();
		Stack<DependencyNode> input = nivreConfig.getInput();
		currentAction.getAction(actionContainers);
		Edge e = null;
		switch (transActionContainer.getActionCode()) {
		case LEFTARC:
			e = nivreConfig.getDependencyStructure().addDependencyEdge(input.peek().getIndex(), stack.peek().getIndex());
			addEdgeLabels(e);
			stack.pop();
			break;
		case RIGHTARC:
			e = nivreConfig.getDependencyStructure().addDependencyEdge(stack.peek().getIndex(), input.peek().getIndex());
			addEdgeLabels(e);
			stack.push(input.pop());
			break;
		case REDUCE:
			stack.pop();
			break;
		default:
			stack.push(input.pop()); 
			break;
		}
	}
	
	public GuideUserAction getDeterministicAction(GuideUserHistory history, ParserConfiguration config) throws MaltChainedException {
		NivreConfig nivreConfig = (NivreConfig)config;
		if (nivreConfig.getRootHandling() != NivreConfig.NORMAL && nivreConfig.getStack().peek().isRoot()) {
			return updateActionContainers(history, ArcEager.SHIFT, null);
		}
		return null;
	}
	
	protected void addAvailableTransitionToTable(TransitionTable ttable) throws MaltChainedException {
		ttable.addTransition(SHIFT, "SH", false, null);
		ttable.addTransition(REDUCE, "RE", false, null);
		ttable.addTransition(RIGHTARC, "RA", true, null);
		ttable.addTransition(LEFTARC, "LA", true, null);
	}
	
	protected void initWithDefaultTransitions(GuideUserHistory history) throws MaltChainedException {
		GuideUserAction currentAction = new ComplexDecisionAction((History)history);
		
		transActionContainer.setAction(SHIFT);
		transActionContainer.setAction(REDUCE);
		for (int i = 0; i < arcLabelActionContainers.length; i++) {
			arcLabelActionContainers[i].setAction(-1);
		}
		currentAction.addAction(actionContainers);
	}
	
	public String getName() {
		return "nivreeager";
	}

	public boolean permissible(GuideUserAction currentAction, ParserConfiguration config) throws MaltChainedException {
		currentAction.getAction(actionContainers);
		int trans = transActionContainer.getActionCode();
		DependencyNode stackPeek = ((NivreConfig)config).getStack().peek();
		int rootHandling = ((NivreConfig)config).getRootHandling();
		if ((trans == LEFTARC || trans == RIGHTARC) && !isActionContainersLabeled()) {
			return false;
		}
		if ((trans == LEFTARC || trans == REDUCE) && stackPeek.isRoot()) { 
			return false;
		}
		if (trans == LEFTARC && stackPeek.hasHead()) { 
			return false;
		}
		if (trans == REDUCE && !stackPeek.hasHead() && rootHandling == NivreConfig.STRICT) {
			return false;
		}
		return true;
	}
	
	public GuideUserAction defaultAction(GuideUserHistory history, ParserConfiguration configuration) throws MaltChainedException {
		return updateActionContainers(history, ArcEager.SHIFT, null);
	}
}