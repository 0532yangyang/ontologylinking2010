package edu.stanford.nlp.parser.ensemble.maltparser.core.syntaxgraph.writer;



import edu.stanford.nlp.parser.ensemble.maltparser.Constants;
import edu.stanford.nlp.parser.ensemble.maltparser.core.exception.MaltChainedException;
import edu.stanford.nlp.parser.ensemble.maltparser.core.flow.FlowChartInstance;
import edu.stanford.nlp.parser.ensemble.maltparser.core.flow.item.ChartItem;
import edu.stanford.nlp.parser.ensemble.maltparser.core.flow.spec.ChartItemSpecification;
import edu.stanford.nlp.parser.ensemble.maltparser.core.io.dataformat.DataFormatException;
import edu.stanford.nlp.parser.ensemble.maltparser.core.io.dataformat.DataFormatInstance;
import edu.stanford.nlp.parser.ensemble.maltparser.core.options.OptionManager;
import edu.stanford.nlp.parser.ensemble.maltparser.core.syntaxgraph.TokenStructure;

/**
*
*
* @author Johan Hall
*/
public class WriteChartItem extends ChartItem {
	private String outputFormatName;
	private String outputFileName;
	private String outputCharSet;
	private String writerOptions;
	private Class<? extends SyntaxGraphWriter> graphWriterClass;
	
	private String nullValueStrategy;
	private String rootLabels;
	
	private SyntaxGraphWriter writer;
	private String sourceName;
	private String optiongroupName;
	private DataFormatInstance outputDataFormatInstance;
	private TokenStructure cachedGraph = null;
	
	public WriteChartItem() { super(); }
	
	public void initialize(FlowChartInstance flowChartinstance, ChartItemSpecification chartItemSpecification) throws MaltChainedException {
		super.initialize(flowChartinstance, chartItemSpecification);
		
		for (String key : chartItemSpecification.getChartItemAttributes().keySet()) {
			if (key.equals("source")) {
				sourceName = chartItemSpecification.getChartItemAttributes().get(key);
			} else if (key.equals("optiongroup")) {
				optiongroupName = chartItemSpecification.getChartItemAttributes().get(key);
			}
		}
		
		if (sourceName == null) {
			sourceName = getChartElement("write").getAttributes().get("source").getDefaultValue();
		} else if (optiongroupName == null) {
			optiongroupName = getChartElement("write").getAttributes().get("optiongroup").getDefaultValue();
		}
		
		setOutputFormatName(OptionManager.instance().getOptionValue(getOptionContainerIndex(), optiongroupName, "format").toString());
		setOutputFileName(OptionManager.instance().getOptionValue(getOptionContainerIndex(), optiongroupName, "outfile").toString());
		setOutputCharSet(OptionManager.instance().getOptionValue(getOptionContainerIndex(), optiongroupName, "charset").toString());
		setWriterOptions(OptionManager.instance().getOptionValue(getOptionContainerIndex(), optiongroupName, "writer_options").toString());
		setSyntaxGraphWriterClass((Class<?>)OptionManager.instance().getOptionValue(getOptionContainerIndex(), optiongroupName, "writer"));

		setNullValueStrategy(OptionManager.instance().getOptionValue(getOptionContainerIndex(), "singlemalt", "null_value").toString());
		setRootLabels(OptionManager.instance().getOptionValue(getOptionContainerIndex(), "graph", "root_label").toString());

		initOutput(getNullValueStrategy(), getRootLabels());
		initWriter(getSyntaxGraphWriterClass(), getOutputFileName(), getOutputCharSet(), getWriterOptions());
	}
	
	public int preprocess(int signal) throws MaltChainedException {
		return signal;
	}
	
	public int process(int signal) throws MaltChainedException {
		if (cachedGraph == null) {
			cachedGraph = (TokenStructure)flowChartinstance.getFlowChartRegistry(edu.stanford.nlp.parser.ensemble.maltparser.core.syntaxgraph.TokenStructure.class, sourceName);
			writer.writeProlog();
		}
		writer.writeSentence(cachedGraph);
		if (signal == ChartItem.TERMINATE) {
			writer.writeEpilog();
		}
		return signal;
	}
	
	public int postprocess(int signal) throws MaltChainedException {
		return signal;
	}
	
	public void terminate() throws MaltChainedException {
		if (writer != null) {
			writer.close();
			writer = null;
		}
		outputDataFormatInstance = null;
		cachedGraph = null;
	}
	
	public String getOutputFormatName() {
		if (outputFormatName == null) {
			return Constants.APPDATA_PATH + "/dataformat/conllx.xml";
		}
		return outputFormatName;
	}

	public void setOutputFormatName(String outputFormatName) {
		this.outputFormatName = outputFormatName;
	}

	public String getOutputFileName() {
		if (outputFileName == null) {
			return "/dev/stdout";
		}
		return outputFileName;
	}

	public void setOutputFileName(String outputFileName) {
		this.outputFileName = outputFileName;
	}

	public String getOutputCharSet() {
		if (outputCharSet == null) {
			return "UTF-8";
		}
		return outputCharSet;
	}

	public void setOutputCharSet(String outputCharSet) {
		this.outputCharSet = outputCharSet;
	}

	public String getWriterOptions() {
		if (writerOptions == null) {
			return "";
		}
		return writerOptions;
	}

	public void setWriterOptions(String writerOptions) {
		this.writerOptions = writerOptions;
	}

	public Class<? extends SyntaxGraphWriter> getSyntaxGraphWriterClass() {
		return graphWriterClass;
	}

	public void setSyntaxGraphWriterClass(Class<?> graphWriterClass) throws MaltChainedException {
		try {
			if (graphWriterClass != null) {
				this.graphWriterClass = graphWriterClass.asSubclass(edu.stanford.nlp.parser.ensemble.maltparser.core.syntaxgraph.writer.SyntaxGraphWriter.class);
			}
		} catch (ClassCastException e) {
			throw new DataFormatException("The class '"+graphWriterClass.getName()+"' is not a subclass of '"+edu.stanford.nlp.parser.ensemble.maltparser.core.syntaxgraph.writer.SyntaxGraphWriter.class.getName()+"'. ", e);
		}
	}

	public String getNullValueStrategy() {
		if (nullValueStrategy == null) {
			return "one";
		}
		return nullValueStrategy;
	}

	public void setNullValueStrategy(String nullValueStrategy) {
		this.nullValueStrategy = nullValueStrategy;
	}

	public String getRootLabels() {
		if (nullValueStrategy == null) {
			return "ROOT";
		}
		return rootLabels;
	}

	public void setRootLabels(String rootLabels) {
		this.rootLabels = rootLabels;
	}
	
	
	public void initOutput(String nullValueStategy, String rootLabels) throws MaltChainedException {
		if (flowChartinstance.getDataFormatInstances().size() == 0 || flowChartinstance.getDataFormatManager().getInputDataFormatSpec() != flowChartinstance.getDataFormatManager().getOutputDataFormatSpec()) {
			outputDataFormatInstance = flowChartinstance.getDataFormatManager().getOutputDataFormatSpec().createDataFormatInstance(flowChartinstance.getSymbolTables(), nullValueStategy, rootLabels);
			if (!flowChartinstance.getDataFormatInstances().containsKey(flowChartinstance.getDataFormatManager().getOutputDataFormatSpec().getDataFormatName())) {
				flowChartinstance.getDataFormatInstances().put(flowChartinstance.getDataFormatManager().getOutputDataFormatSpec().getDataFormatName(), outputDataFormatInstance);
			}
		} else {
			outputDataFormatInstance = flowChartinstance.getDataFormatInstances().get(flowChartinstance.getDataFormatManager().getInputDataFormatSpec().getDataFormatName());
		}
	}
	
	public void initWriter(Class<? extends SyntaxGraphWriter> syntaxGraphWriterClass, String outputFile, String outputCharSet, 
			String writerOption) throws MaltChainedException {
		try {	
			writer = syntaxGraphWriterClass.newInstance();
			if (outputFile == null || outputFile.length() == 0 || outputFile.equals("/dev/stdout")) {
				writer.open(System.out, outputCharSet);
			} else {
				writer.open(outputFile, outputCharSet);
			}
			writer.setDataFormatInstance(outputDataFormatInstance);
			writer.setOptions(writerOption);
		} catch (InstantiationException e) {
			throw new DataFormatException("The data writer '"+syntaxGraphWriterClass.getName()+"' cannot be initialized. ", e);
		} catch (IllegalAccessException e) {
			throw new DataFormatException("The data writer '"+syntaxGraphWriterClass.getName()+"' cannot be initialized. ", e);
		}
	}

	public Class<? extends SyntaxGraphWriter> getGraphWriterClass() {
		return graphWriterClass;
	}

	public SyntaxGraphWriter getWriter() {
		return writer;
	}

	public String getSourceName() {
		return sourceName;
	}

	public DataFormatInstance getOutputDataFormatInstance() {
		return outputDataFormatInstance;
	}
	
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		return obj.toString().equals(this.toString());
	}
	
	public int hashCode() {
		return 217 + (null == toString() ? 0 : toString().hashCode());
	}
	
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("    write ");
		sb.append("source:");
		sb.append(sourceName);
		sb.append(' ');
		sb.append("optiongroup:");
		sb.append(optiongroupName);
		return sb.toString();
	}
}
