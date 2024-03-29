package edu.stanford.nlp.parser.ensemble.maltparser.core.feature;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.Stack;
import java.util.regex.Pattern;


import edu.stanford.nlp.parser.ensemble.maltparser.core.config.ConfigurationRegistry;
import edu.stanford.nlp.parser.ensemble.maltparser.core.exception.MaltChainedException;
import edu.stanford.nlp.parser.ensemble.maltparser.core.feature.function.AddressFunction;
import edu.stanford.nlp.parser.ensemble.maltparser.core.feature.function.FeatureFunction;
import edu.stanford.nlp.parser.ensemble.maltparser.core.feature.function.Function;
import edu.stanford.nlp.parser.ensemble.maltparser.core.feature.spec.SpecificationModel;
import edu.stanford.nlp.parser.ensemble.maltparser.core.feature.spec.SpecificationSubModel;
import edu.stanford.nlp.parser.ensemble.maltparser.core.feature.system.FeatureEngine;

/**
*
*
* @author Johan Hall
*/
public class FeatureModel extends HashMap<String, FeatureVector> {
	public final static long serialVersionUID = 3256444702936019250L;
	protected SpecificationModel specModel;
	protected final ArrayList<AddressFunction> addressFunctionCache;
	protected final ArrayList<FeatureFunction> featureFunctionCache;
	protected ConfigurationRegistry registry;
	protected FeatureEngine featureEngine;
	protected FeatureVector mainFeatureVector = null; 
	protected final Pattern splitPattern;
	
	public FeatureModel(SpecificationModel specModel, ConfigurationRegistry registry, FeatureEngine engine) throws MaltChainedException {
		setSpecModel(specModel);
		setRegistry(registry);
		setFeatureEngine(engine);
		addressFunctionCache = new ArrayList<AddressFunction>();
		featureFunctionCache = new ArrayList<FeatureFunction>();
		splitPattern = Pattern.compile("\\(|\\)|\\[|\\]|,");
		for (SpecificationSubModel subModel : specModel) {
			FeatureVector fv = new FeatureVector(this, subModel);
			if (mainFeatureVector == null) {
				if (subModel.getSubModelName().equals("MAIN")) {
					mainFeatureVector = fv;
				} else {
					mainFeatureVector = fv;
					put(subModel.getSubModelName(), fv);
				}
			} else {
				put(subModel.getSubModelName(), fv);
			}
		}
	}

	public SpecificationModel getSpecModel() {
		return specModel;
	}

	public void setSpecModel(SpecificationModel specModel) {
		this.specModel = specModel;
	}
	
	public ArrayList<AddressFunction> getAddressFunctionCache() {
		return addressFunctionCache;
	}

	public ArrayList<FeatureFunction> getFeatureFunctionCache() {
		return featureFunctionCache;
	}
	
	public ConfigurationRegistry getRegistry() {
		return registry;
	}

	public void setRegistry(ConfigurationRegistry registry) {
		this.registry = registry;
	}

	public FeatureEngine getFeatureEngine() {
		return featureEngine;
	}

	public void setFeatureEngine(FeatureEngine featureEngine) {
		this.featureEngine = featureEngine;
	}
	
	public FeatureVector getMainFeatureVector() {
		return mainFeatureVector;
	}
	
	public FeatureVector getFeatureVector(String subModelName) {
		return get(subModelName);
	}
	
	public void update() throws MaltChainedException {
		for (int i = 0, n = addressFunctionCache.size(); i < n; i++) {
			addressFunctionCache.get(i).update();
		}
		
		for (int i = 0, n = featureFunctionCache.size(); i < n; i++) {
			featureFunctionCache.get(i).update();
		}
	}
	
	public void update(Object[] arguments) throws MaltChainedException {
		for (int i = 0, n = addressFunctionCache.size(); i < n; i++) {
			addressFunctionCache.get(i).update(arguments);
		}
		
		for (int i = 0, n = featureFunctionCache.size(); i < n; i++) {
			featureFunctionCache.get(i).update();
		}
	}
	
	public void updateCardinality() throws MaltChainedException {
		for (int i = 0, n = featureFunctionCache.size(); i < n; i++) {
			featureFunctionCache.get(i).updateCardinality();
		}
	}
	
	public FeatureFunction identifyFeature(String spec) throws MaltChainedException {
		String[] items =splitPattern.split(spec);
		Stack<Object> objects = new Stack<Object>();
		for (int i = items.length-1; i >= 0; i--) {
			if (items[i].trim().length() != 0) {
				objects.push(items[i].trim());
			}
		}
		identifyFeatureFunction(objects);
		if (objects.size() != 1 || !(objects.peek() instanceof FeatureFunction) || (objects.peek() instanceof AddressFunction)) {
			throw new FeatureException("The feature specification '"+spec+"' were not recognized properly. ");
		}
		return (FeatureFunction)objects.pop();
	}
	
	protected void identifyFeatureFunction(Stack<Object> objects) throws MaltChainedException {
		Function function = featureEngine.newFunction(objects.peek().toString(), registry);
		if (function != null) {
			objects.pop();
			if (!objects.isEmpty()) {
				identifyFeatureFunction(objects);
			}
			initializeFunction(function, objects);
		} else {
			if (!objects.isEmpty()) {
				Object o = objects.pop();
				if (!objects.isEmpty()) {
					identifyFeatureFunction(objects);
				}
				objects.push(o);
			}
		}
	}
	
	protected void initializeFunction(Function function, Stack<Object> objects) throws MaltChainedException {
		Class<?>[] paramTypes = function.getParameterTypes();
		Object[] arguments = new Object[paramTypes.length];
		for (int i = 0; i < paramTypes.length; i++) {
			if (paramTypes[i] == java.lang.Integer.class) {
				if (objects.peek() instanceof String) {
					try {
						objects.push(Integer.parseInt(((String)objects.pop())));
					} catch (NumberFormatException e) {
						throw new FeatureException("Could not cast string to integer. ", e);
					}
				} else {
					throw new FeatureException("Could not cast string to integer. ");
				}
			} else if (paramTypes[i] == java.lang.Double.class) {
				if (objects.peek() instanceof String) {
					try {
						objects.push(Double.parseDouble(((String)objects.pop())));
					} catch (NumberFormatException e) {
						throw new FeatureException("Could not cast string to double. ", e);
					}
				} else {
					throw new FeatureException("Could not cast string to double. ");
				}
			} else if (paramTypes[i] == java.lang.Boolean.class) {
				if (objects.peek() instanceof String) {
					objects.push(Boolean.parseBoolean(((String)objects.pop())));
				} else {
					throw new FeatureException("Could not cast string to boolean. ");
				}
			}
			if (!paramTypes[i].isInstance(objects.peek())) {
				throw new FeatureException("The function cannot be initialized. ");
			}
			arguments[i] = objects.pop();
		}
		function.initialize(arguments);
		if (function instanceof AddressFunction) {
			int index = getAddressFunctionCache().indexOf(function);
			if (index != -1) {
				function = getAddressFunctionCache().get(index);
			} else {
				getAddressFunctionCache().add((AddressFunction)function);
			}
		} else if (function instanceof FeatureFunction) {
			int index = getFeatureFunctionCache().indexOf(function);
			if (index != -1) {
				function = getFeatureFunctionCache().get(index);
			} else {
				getFeatureFunctionCache().add((FeatureFunction)function);
			}
		}
		objects.push(function);
	}
	
	public String toString() {
		return specModel.toString();
	}
}
