package edu.stanford.nlp.parser.ensemble.maltparser.parser.guide.decision;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;


import edu.stanford.nlp.parser.ensemble.maltparser.core.exception.MaltChainedException;
import edu.stanford.nlp.parser.ensemble.maltparser.core.feature.FeatureModel;
import edu.stanford.nlp.parser.ensemble.maltparser.core.feature.FeatureVector;
import edu.stanford.nlp.parser.ensemble.maltparser.core.syntaxgraph.DependencyStructure;
import edu.stanford.nlp.parser.ensemble.maltparser.parser.guide.ClassifierGuide;
import edu.stanford.nlp.parser.ensemble.maltparser.parser.guide.GuideException;
import edu.stanford.nlp.parser.ensemble.maltparser.parser.guide.instance.AtomicModel;
import edu.stanford.nlp.parser.ensemble.maltparser.parser.guide.instance.FeatureDivideModel;
import edu.stanford.nlp.parser.ensemble.maltparser.parser.guide.instance.InstanceModel;
import edu.stanford.nlp.parser.ensemble.maltparser.parser.history.action.GuideDecision;
import edu.stanford.nlp.parser.ensemble.maltparser.parser.history.action.MultipleDecision;
import edu.stanford.nlp.parser.ensemble.maltparser.parser.history.action.SingleDecision;
import edu.stanford.nlp.parser.ensemble.maltparser.parser.history.container.TableContainer.RelationToNextDecision;
/**
*
* @author Johan Hall
* @since 1.1
**/
public class BranchedDecisionModel implements DecisionModel {
	private ClassifierGuide guide;
	private String modelName;
	private FeatureModel featureModel;
	private InstanceModel instanceModel;
	private int decisionIndex;
	private DecisionModel parentDecisionModel;
	private HashMap<Integer,DecisionModel> children;
	private String branchedDecisionSymbols;
	
	public BranchedDecisionModel(ClassifierGuide guide, FeatureModel featureModel) throws MaltChainedException {
		this.branchedDecisionSymbols = "";
		setGuide(guide);
		setFeatureModel(featureModel);
		setDecisionIndex(0);
		setModelName("bdm"+decisionIndex);
		setParentDecisionModel(null);
	}
	
	public BranchedDecisionModel(ClassifierGuide guide, DecisionModel parentDecisionModel, String branchedDecisionSymbol) throws MaltChainedException {
		if (branchedDecisionSymbol != null && branchedDecisionSymbol.length() > 0) {
			this.branchedDecisionSymbols = branchedDecisionSymbol;
		} else {
			this.branchedDecisionSymbols = "";
		}
		setGuide(guide);
		setParentDecisionModel(parentDecisionModel);
		setDecisionIndex(parentDecisionModel.getDecisionIndex() + 1);
		setFeatureModel(parentDecisionModel.getFeatureModel());
		if (branchedDecisionSymbols != null && branchedDecisionSymbols.length() > 0) {
			setModelName("bdm"+decisionIndex+branchedDecisionSymbols);
		} else {
			setModelName("bdm"+decisionIndex);
		}
		this.parentDecisionModel = parentDecisionModel;
	}
	
	public void updateFeatureModel() throws MaltChainedException {
		featureModel.update();
	}
	
	public void updateCardinality() throws MaltChainedException {
		featureModel.updateCardinality();
	}
	

	public void finalizeSentence(DependencyStructure dependencyGraph) throws MaltChainedException {
		if (instanceModel != null) {
			instanceModel.finalizeSentence(dependencyGraph);
		}
		if (children != null) {
			for (DecisionModel child : children.values()) {
				child.finalizeSentence(dependencyGraph);
			}
		}
	}
	
	public void noMoreInstances() throws MaltChainedException {
		if (guide.getGuideMode() == ClassifierGuide.GuideMode.CLASSIFY) {
			throw new GuideException("The decision model could not create it's model. ");
		}
		featureModel.updateCardinality();
		if (instanceModel != null) {
			instanceModel.noMoreInstances();
			instanceModel.train();
		}
		if (children != null) {
			for (DecisionModel child : children.values()) {
				child.noMoreInstances();
			}
		}
	}

	public void terminate() throws MaltChainedException {
		if (instanceModel != null) {
			instanceModel.terminate();
			instanceModel = null;
		}
		if (children != null) {
			for (DecisionModel child : children.values()) {
				child.terminate();
			}
		}
	}
	
	public void addInstance(GuideDecision decision) throws MaltChainedException {
		if (decision instanceof SingleDecision) {
			throw new GuideException("A branched decision model expect more than one decisions. ");
		}
		updateFeatureModel();
		final SingleDecision singleDecision = ((MultipleDecision)decision).getSingleDecision(decisionIndex);
		if (instanceModel == null) {
			initInstanceModel(singleDecision.getTableContainer().getTableContainerName());
		}
		
		instanceModel.addInstance(singleDecision);
		if (decisionIndex+1 < decision.numberOfDecisions()) {
			if (singleDecision.continueWithNextDecision()) {
				if (children == null) {
					children = new HashMap<Integer,DecisionModel>();
				}
				DecisionModel child = children.get(singleDecision.getDecisionCode());
				if (child == null) {
					child = initChildDecisionModel(((MultipleDecision)decision).getSingleDecision(decisionIndex+1), 
							branchedDecisionSymbols+(branchedDecisionSymbols.length() == 0?"":"_")+singleDecision.getDecisionSymbol());
					children.put(singleDecision.getDecisionCode(), child);
				}
				child.addInstance(decision);
			}
		}
	}
	
	public boolean predict(GuideDecision decision) throws MaltChainedException {
		if (decision instanceof SingleDecision) {
			throw new GuideException("A branched decision model expect more than one decisions. ");
		}
		updateFeatureModel();
		final SingleDecision singleDecision = ((MultipleDecision)decision).getSingleDecision(decisionIndex);
		if (instanceModel == null) {
			initInstanceModel(singleDecision.getTableContainer().getTableContainerName());
		}
		instanceModel.predict(singleDecision);
		if (decisionIndex+1 < decision.numberOfDecisions()) {
			if (singleDecision.continueWithNextDecision()) {
				if (children == null) {
					children = new HashMap<Integer,DecisionModel>();
				}
				DecisionModel child = children.get(singleDecision.getDecisionCode());
				if (child == null) {
					child = initChildDecisionModel(((MultipleDecision)decision).getSingleDecision(decisionIndex+1), 
							branchedDecisionSymbols+(branchedDecisionSymbols.length() == 0?"":"_")+singleDecision.getDecisionSymbol());
					children.put(singleDecision.getDecisionCode(), child);
				}
				child.predict(decision);
			}
		}

		return true;
	}
	
	public FeatureVector predictExtract(GuideDecision decision) throws MaltChainedException {
		if (decision instanceof SingleDecision) {
			throw new GuideException("A branched decision model expect more than one decisions. ");
		}
		updateFeatureModel();
		final SingleDecision singleDecision = ((MultipleDecision)decision).getSingleDecision(decisionIndex);
		if (instanceModel == null) {
			initInstanceModel(singleDecision.getTableContainer().getTableContainerName());
		}
		FeatureVector fv = instanceModel.predictExtract(singleDecision);
		if (decisionIndex+1 < decision.numberOfDecisions()) {
			if (singleDecision.continueWithNextDecision()) {
				if (children == null) {
					children = new HashMap<Integer,DecisionModel>();
				}
				DecisionModel child = children.get(singleDecision.getDecisionCode());
				if (child == null) {
					child = initChildDecisionModel(((MultipleDecision)decision).getSingleDecision(decisionIndex+1), 
							branchedDecisionSymbols+(branchedDecisionSymbols.length() == 0?"":"_")+singleDecision.getDecisionSymbol());
					children.put(singleDecision.getDecisionCode(), child);
				}
				child.predictExtract(decision);
			}
		}

		return fv;
	}
	
	public FeatureVector extract() throws MaltChainedException {
		updateFeatureModel();
		return instanceModel.extract(); // TODO handle many feature vectors
	}
	
	public boolean predictFromKBestList(GuideDecision decision) throws MaltChainedException {
		if (decision instanceof SingleDecision) {
			throw new GuideException("A branched decision model expect more than one decisions. ");
		}
		
		boolean success = false;
		final SingleDecision singleDecision = ((MultipleDecision)decision).getSingleDecision(decisionIndex);
		if (decisionIndex+1 < decision.numberOfDecisions()) {
			if (singleDecision.continueWithNextDecision()) {
				if (children == null) {
					children = new HashMap<Integer,DecisionModel>();
				}
				DecisionModel child = children.get(singleDecision.getDecisionCode());
				if (child != null) {
					success = child.predictFromKBestList(decision);
				}
				
			}
		}
		if (!success) {
			success = singleDecision.updateFromKBestList();
			if (decisionIndex+1 < decision.numberOfDecisions()) {
				if (singleDecision.continueWithNextDecision()) {
					if (children == null) {
						children = new HashMap<Integer,DecisionModel>();
					}
					DecisionModel child = children.get(singleDecision.getDecisionCode());
					if (child == null) {
						child = initChildDecisionModel(((MultipleDecision)decision).getSingleDecision(decisionIndex+1), 
								branchedDecisionSymbols+(branchedDecisionSymbols.length() == 0?"":"_")+singleDecision.getDecisionSymbol());
						children.put(singleDecision.getDecisionCode(), child);
					}
					child.predict(decision);
				}
			}
		}
		return success;
	}
	

	public ClassifierGuide getGuide() {
		return guide;
	}

	public String getModelName() {
		return modelName;
	}
	
	public FeatureModel getFeatureModel() {
		return featureModel;
	}

	public int getDecisionIndex() {
		return decisionIndex;
	}

	public DecisionModel getParentDecisionModel() {
		return parentDecisionModel;
	}

	private void setFeatureModel(FeatureModel featureModel) {
		this.featureModel = featureModel;
	}
	
	private void setDecisionIndex(int decisionIndex) {
		this.decisionIndex = decisionIndex;
	}
	
	private void setParentDecisionModel(DecisionModel parentDecisionModel) {
		this.parentDecisionModel = parentDecisionModel;
	}

	private void setModelName(String modelName) {
		this.modelName = modelName;
	}
	
	private void setGuide(ClassifierGuide guide) {
		this.guide = guide;
	}
	
	
	private DecisionModel initChildDecisionModel(SingleDecision decision, String branchedDecisionSymbol) throws MaltChainedException {
		Class<?> decisionModelClass = null;
		if (decision.getRelationToNextDecision() == RelationToNextDecision.SEQUANTIAL) {
			decisionModelClass = edu.stanford.nlp.parser.ensemble.maltparser.parser.guide.decision.SeqDecisionModel.class;
		} else if (decision.getRelationToNextDecision() == RelationToNextDecision.BRANCHED) {
			decisionModelClass = edu.stanford.nlp.parser.ensemble.maltparser.parser.guide.decision.BranchedDecisionModel.class;
		} else if (decision.getRelationToNextDecision() == RelationToNextDecision.NONE) {
			decisionModelClass = edu.stanford.nlp.parser.ensemble.maltparser.parser.guide.decision.OneDecisionModel.class;
		}

		if (decisionModelClass == null) {
			throw new GuideException("Could not find an appropriate decision model for the relation to the next decision"); 
		}
		
		try {
			Class<?>[] argTypes = { edu.stanford.nlp.parser.ensemble.maltparser.parser.guide.ClassifierGuide.class, edu.stanford.nlp.parser.ensemble.maltparser.parser.guide.decision.DecisionModel.class, 
						java.lang.String.class };
			Object[] arguments = new Object[3];
			arguments[0] = getGuide();
			arguments[1] = this;
			arguments[2] = branchedDecisionSymbol;
			Constructor<?> constructor = decisionModelClass.getConstructor(argTypes);
			return (DecisionModel)constructor.newInstance(arguments);
		} catch (NoSuchMethodException e) {
			throw new GuideException("The decision model class '"+decisionModelClass.getName()+"' cannot be initialized. ", e);
		} catch (InstantiationException e) {
			throw new GuideException("The decision model class '"+decisionModelClass.getName()+"' cannot be initialized. ", e);
		} catch (IllegalAccessException e) {
			throw new GuideException("The decision model class '"+decisionModelClass.getName()+"' cannot be initialized. ", e);
		} catch (InvocationTargetException e) {
			throw new GuideException("The decision model class '"+decisionModelClass.getName()+"' cannot be initialized. ", e);
		}
	}
	
	private void initInstanceModel(String subModelName) throws MaltChainedException {
		FeatureVector fv = featureModel.getFeatureVector(branchedDecisionSymbols+"."+subModelName);
		if (fv == null) {
			fv = featureModel.getFeatureVector(subModelName);
		}
		if (fv == null) {
			fv = featureModel.getMainFeatureVector();
		}
		if (guide.getConfiguration().getOptionValue("guide", "data_split_column").toString().length() == 0) {
			instanceModel = new AtomicModel(-1, fv, this);
		} else {
			instanceModel = new FeatureDivideModel(fv, this);
		}
	}
	
	public String toString() {
		final StringBuilder sb = new StringBuilder();
		sb.append(modelName + ", ");
		for (DecisionModel model : children.values()) {
			sb.append(model.toString() + ", ");
		}
		return sb.toString();
	}
}
