package edu.stanford.nlp.parser.ensemble.maltparser.core.syntaxgraph.feature;


import edu.stanford.nlp.parser.ensemble.maltparser.core.exception.MaltChainedException;
import edu.stanford.nlp.parser.ensemble.maltparser.core.feature.function.AddressFunction;
import edu.stanford.nlp.parser.ensemble.maltparser.core.feature.value.AddressValue;
import edu.stanford.nlp.parser.ensemble.maltparser.core.io.dataformat.DataFormatInstance;
import edu.stanford.nlp.parser.ensemble.maltparser.core.symbol.nullvalue.NullValues.NullValueId;
import edu.stanford.nlp.parser.ensemble.maltparser.core.syntaxgraph.SyntaxGraphException;
import edu.stanford.nlp.parser.ensemble.maltparser.core.syntaxgraph.node.DependencyNode;

/**
 *
 *
 * @author Johan Hall
 */
public class OutputColumnFeature extends ColumnFeature {
	protected AddressFunction addressFunction;
	protected DataFormatInstance dataFormatInstance;
	
	public OutputColumnFeature(DataFormatInstance dataFormatInstance) throws MaltChainedException {
		super();
		setDataFormatInstance(dataFormatInstance);
	}
	
	public void initialize(Object[] arguments) throws MaltChainedException {
		if (arguments.length != 2) {
			throw new SyntaxGraphException("Could not initialize OutputColumnFeature: number of arguments are not correct. ");
		}
		if (!(arguments[0] instanceof String)) {
			throw new SyntaxGraphException("Could not initialize OutputColumnFeature: the first argument is not a string. ");
		}
		if (!(arguments[1] instanceof AddressFunction)) {
			throw new SyntaxGraphException("Could not initialize OutputColumnFeature: the second argument is not an address function. ");
		}
		setColumn(dataFormatInstance.getColumnDescriptionByName((String)arguments[0]));
		setAddressFunction((AddressFunction)arguments[1]);
	}
	
	public Class<?>[] getParameterTypes() {
		Class<?>[] paramTypes = { java.lang.String.class, edu.stanford.nlp.parser.ensemble.maltparser.core.feature.function.AddressFunction.class };
		return paramTypes; 
	}

	public void update()  throws MaltChainedException {
		final AddressValue a = addressFunction.getAddressValue();
		
		if (a.getAddress() == null) {
			featureValue.setCode(column.getSymbolTable().getNullValueCode(NullValueId.NO_NODE));
			featureValue.setSymbol(column.getSymbolTable().getNullValueSymbol(NullValueId.NO_NODE));
			featureValue.setKnown(true);
			featureValue.setNullValue(true);			
		} else {
//			try { 
//				a.getAddressClass().asSubclass(org.maltparser.core.syntaxgraph.node.DependencyNode.class);
				final DependencyNode node = (DependencyNode)a.getAddress();
				if (!node.isRoot()) {
					if (node.hasHead()) {
						featureValue.setCode(node.getHeadEdge().getLabelCode(column.getSymbolTable()));
						featureValue.setSymbol(column.getSymbolTable().getSymbolCodeToString(node.getHeadEdge().getLabelCode(column.getSymbolTable())));
						featureValue.setKnown(column.getSymbolTable().getKnown(node.getHeadEdge().getLabelCode(column.getSymbolTable())));
						featureValue.setNullValue(false);
					} else {
						featureValue.setCode(column.getSymbolTable().getNullValueCode(NullValueId.NO_VALUE));
						featureValue.setSymbol(column.getSymbolTable().getNullValueSymbol(NullValueId.NO_VALUE));
						featureValue.setKnown(true);
						featureValue.setNullValue(true);
					}	
				} else {
					featureValue.setCode(column.getSymbolTable().getNullValueCode(NullValueId.ROOT_NODE));
					featureValue.setSymbol(column.getSymbolTable().getNullValueSymbol(NullValueId.ROOT_NODE));
					featureValue.setKnown(true);
					featureValue.setNullValue(true);
				}
//			} catch (ClassCastException e) {
//				featureValue.setCode(column.getSymbolTable().getNullValueCode(NullValueId.NO_NODE));
//				featureValue.setSymbol(column.getSymbolTable().getNullValueSymbol(NullValueId.NO_NODE));
//				featureValue.setKnown(true);
//				featureValue.setNullValue(true);
//			}
		}
	}
	
	public AddressFunction getAddressFunction() {
		return addressFunction;
	}

	public void setAddressFunction(AddressFunction addressFunction) {
		this.addressFunction = addressFunction;
	}
	
	public DataFormatInstance getDataFormatInstance() {
		return dataFormatInstance;
	}

	public void setDataFormatInstance(DataFormatInstance dataFormatInstance) {
		this.dataFormatInstance = dataFormatInstance;
	}

	
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		return obj.toString().equals(toString());
	}
	
	public int hashCode() {
		return 217 + (null == toString() ? 0 : toString().hashCode());
	}
	
	public String toString() {
		final StringBuilder sb = new StringBuilder();
		sb.append("OutputColumn(");
		sb.append(super.toString());
		sb.append(", ");
		sb.append(addressFunction.toString());
		sb.append(")");
		return sb.toString();
	}
}
