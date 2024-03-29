package edu.stanford.nlp.parser.ensemble.maltparser.parser.algorithm.stack;

import java.util.Stack;


import edu.stanford.nlp.parser.ensemble.maltparser.core.exception.MaltChainedException;
import edu.stanford.nlp.parser.ensemble.maltparser.core.syntaxgraph.DependencyStructure;
import edu.stanford.nlp.parser.ensemble.maltparser.core.syntaxgraph.node.DependencyNode;
import edu.stanford.nlp.parser.ensemble.maltparser.parser.DependencyParserConfig;
import edu.stanford.nlp.parser.ensemble.maltparser.parser.Oracle;
import edu.stanford.nlp.parser.ensemble.maltparser.parser.ParserConfiguration;
import edu.stanford.nlp.parser.ensemble.maltparser.parser.history.GuideUserHistory;
import edu.stanford.nlp.parser.ensemble.maltparser.parser.history.action.GuideUserAction;
/**
 * @author Johan Hall
 *
 */
public class ProjectiveOracle  extends Oracle {
	public ProjectiveOracle(DependencyParserConfig manager, GuideUserHistory history) throws MaltChainedException {
		super(manager, history);
		setGuideName("projective");
	}
	
	public GuideUserAction predict(DependencyStructure gold, ParserConfiguration configuration) throws MaltChainedException {
		StackConfig config = (StackConfig)configuration;
		Stack<DependencyNode> stack = config.getStack();

		if (stack.size() < 2) {
			return updateActionContainers(Projective.SHIFT, null);
		} else {
			DependencyNode left = stack.get(stack.size()-2);
			int leftIndex = left.getIndex();
			int rightIndex = stack.get(stack.size()-1).getIndex();
			if (!left.isRoot() && gold.getTokenNode(leftIndex).getHead().getIndex() == rightIndex) {
				return updateActionContainers(Projective.LEFTARC, gold.getTokenNode(leftIndex).getHeadEdge().getLabelSet());
			} else if (gold.getTokenNode(rightIndex).getHead().getIndex() == leftIndex && checkRightDependent(gold, config.getDependencyGraph(), rightIndex)) {
				return updateActionContainers(Projective.RIGHTARC, gold.getTokenNode(rightIndex).getHeadEdge().getLabelSet());
			} else {
				return updateActionContainers(Projective.SHIFT, null);
			} // Solve the problem with non-projective input.
		}
	}
	
	private boolean checkRightDependent(DependencyStructure gold, DependencyStructure parseDependencyGraph, int index) throws MaltChainedException {
		if (gold.getTokenNode(index).getRightmostDependent() == null) {
			return true;
		} else if (parseDependencyGraph.getTokenNode(index).getRightmostDependent() != null) {
			if (gold.getTokenNode(index).getRightmostDependent().getIndex() == parseDependencyGraph.getTokenNode(index).getRightmostDependent().getIndex()) {
				return true;
			}
		}
		return false;
	}
	
	public void finalizeSentence(DependencyStructure dependencyGraph) throws MaltChainedException {
		
	}
	
	public void terminate() throws MaltChainedException {
		
	}
}
