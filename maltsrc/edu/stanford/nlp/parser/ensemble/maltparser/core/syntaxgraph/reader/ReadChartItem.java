package edu.stanford.nlp.parser.ensemble.maltparser.core.syntaxgraph.reader;

import java.io.File;


import edu.stanford.nlp.parser.ensemble.maltparser.Constants;
import edu.stanford.nlp.parser.ensemble.maltparser.core.exception.MaltChainedException;
import edu.stanford.nlp.parser.ensemble.maltparser.core.flow.FlowChartInstance;
import edu.stanford.nlp.parser.ensemble.maltparser.core.flow.item.ChartItem;
import edu.stanford.nlp.parser.ensemble.maltparser.core.flow.spec.ChartItemSpecification;
import edu.stanford.nlp.parser.ensemble.maltparser.core.helper.Util;
import edu.stanford.nlp.parser.ensemble.maltparser.core.io.dataformat.DataFormatException;
import edu.stanford.nlp.parser.ensemble.maltparser.core.io.dataformat.DataFormatInstance;
import edu.stanford.nlp.parser.ensemble.maltparser.core.options.OptionManager;
import edu.stanford.nlp.parser.ensemble.maltparser.core.syntaxgraph.TokenStructure;

public class ReadChartItem extends ChartItem {
	private String inputFormatName;
	private String inputFileName;
	private String inputCharSet;
	private String readerOptions;
	private int iterations;
	private Class<? extends SyntaxGraphReader> graphReaderClass;
	
	private String nullValueStrategy;
	private String rootLabels;
	
	private SyntaxGraphReader reader;
	private String targetName;
	private String optiongroupName;
	private DataFormatInstance inputDataFormatInstance;
	private TokenStructure cachedGraph = null;
	
	public ReadChartItem() { super(); }

	public void initialize(FlowChartInstance flowChartinstance, ChartItemSpecification chartItemSpecification) throws MaltChainedException {
		super.initialize(flowChartinstance, chartItemSpecification);
		
		for (String key : chartItemSpecification.getChartItemAttributes().keySet()) {
			if (key.equals("target")) {
				targetName = chartItemSpecification.getChartItemAttributes().get(key);
			} else if (key.equals("optiongroup")) {
				optiongroupName = chartItemSpecification.getChartItemAttributes().get(key);
			}
		}
		
		if (targetName == null) {
			targetName = getChartElement("read").getAttributes().get("target").getDefaultValue();
		} else if (optiongroupName == null) {
			optiongroupName = getChartElement("read").getAttributes().get("optiongroup").getDefaultValue();
		}
		
		setInputFormatName(OptionManager.instance().getOptionValue(getOptionContainerIndex(), optiongroupName, "format").toString());
		setInputFileName(OptionManager.instance().getOptionValue(getOptionContainerIndex(), optiongroupName, "infile").toString());
		setInputCharSet(OptionManager.instance().getOptionValue(getOptionContainerIndex(), optiongroupName, "charset").toString());
		setReaderOptions(OptionManager.instance().getOptionValue(getOptionContainerIndex(), optiongroupName, "reader_options").toString());
		if (OptionManager.instance().getOptionValue(getOptionContainerIndex(), optiongroupName, "iterations") != null) {
			setIterations((Integer)OptionManager.instance().getOptionValue(getOptionContainerIndex(), optiongroupName, "iterations"));
		} else {
			setIterations(1);
		}
		setSyntaxGraphReaderClass((Class<?>)OptionManager.instance().getOptionValue(getOptionContainerIndex(), optiongroupName, "reader"));

		setNullValueStrategy(OptionManager.instance().getOptionValue(getOptionContainerIndex(), "singlemalt", "null_value").toString());
		setRootLabels(OptionManager.instance().getOptionValue(getOptionContainerIndex(), "graph", "root_label").toString());
		
		
		initInput(getNullValueStrategy(), getRootLabels());
		initReader(getSyntaxGraphReaderClass(), getInputFileName(), getInputCharSet(), getReaderOptions(), iterations);
	}
	
	public int preprocess(int signal) throws MaltChainedException {
		return signal;
	}
	
	public int process(int signal) throws MaltChainedException {
		if (cachedGraph == null) {
			cachedGraph = (TokenStructure)flowChartinstance.getFlowChartRegistry(edu.stanford.nlp.parser.ensemble.maltparser.core.syntaxgraph.TokenStructure.class, targetName);
		}
		int prevIterationCounter = reader.getIterationCounter();
		boolean moreInput = reader.readSentence(cachedGraph);
//		System.out.println(cachedGraph);
//		System.exit(1);
		if (!moreInput) {
			return ChartItem.TERMINATE;
		} else if (prevIterationCounter < reader.getIterationCounter()) {
			return ChartItem.NEWITERATION;
		}
		return ChartItem.CONTINUE;
//		return continueNextSentence && moreInput;
	}
	
	public int postprocess(int signal) throws MaltChainedException {
		return signal;
	}
	
	public void terminate() throws MaltChainedException {
		if (reader != null) {
			reader.close();
			reader = null;
		}
		cachedGraph = null;
		inputDataFormatInstance = null;
	}
	
	public String getInputFormatName() {
		if (inputFormatName == null) {
			return Constants.APPDATA_PATH + "/dataformat/conllx.xml";
		}
		return inputFormatName;
	}

	public void setInputFormatName(String inputFormatName) {
		this.inputFormatName = inputFormatName;
	}

	public String getInputFileName() {
		if (inputFileName == null) {
			return "/dev/stdin";
		}
		return inputFileName;
	}

	public void setInputFileName(String inputFileName) {
		this.inputFileName = inputFileName;
	}

	public String getInputCharSet() {
		if (inputCharSet == null) {
			return "UTF-8";
		}
		return inputCharSet;
	}

	public void setInputCharSet(String inputCharSet) {
		this.inputCharSet = inputCharSet;
	}

	public String getReaderOptions() {
		if (readerOptions == null) {
			return "";
		}
		return readerOptions;
	}

	public void setReaderOptions(String readerOptions) {
		this.readerOptions = readerOptions;
	}

	
	public int getIterations() {
		return iterations;
	}

	public void setIterations(int iterations) {
		this.iterations = iterations;
	}

	public Class<? extends SyntaxGraphReader> getSyntaxGraphReaderClass() {
		return graphReaderClass;
	}

	public void setSyntaxGraphReaderClass(Class<?> graphReaderClass) throws MaltChainedException {
		try {
			if (graphReaderClass != null) {
				this.graphReaderClass = graphReaderClass.asSubclass(edu.stanford.nlp.parser.ensemble.maltparser.core.syntaxgraph.reader.SyntaxGraphReader.class);
			}
		} catch (ClassCastException e) {
			throw new DataFormatException("The class '"+graphReaderClass.getName()+"' is not a subclass of '"+edu.stanford.nlp.parser.ensemble.maltparser.core.syntaxgraph.reader.SyntaxGraphReader.class.getName()+"'. ", e);
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
	

	public String getTargetName() {
		return targetName;
	}

	public void setTargetName(String targetName) {
		this.targetName = targetName;
	}

	public SyntaxGraphReader getReader() {
		return reader;
	}

	public DataFormatInstance getInputDataFormatInstance() {
		return inputDataFormatInstance;
	}

	public void initInput(String nullValueStategy, String rootLabels) throws MaltChainedException {
		inputDataFormatInstance = flowChartinstance.getDataFormatManager().getInputDataFormatSpec().createDataFormatInstance(flowChartinstance.getSymbolTables(), nullValueStategy, rootLabels);
		if (!flowChartinstance.getDataFormatInstances().containsKey(flowChartinstance.getDataFormatManager().getInputDataFormatSpec().getDataFormatName())) {
			flowChartinstance.getDataFormatInstances().put(flowChartinstance.getDataFormatManager().getInputDataFormatSpec().getDataFormatName(), inputDataFormatInstance);
		}
	}
	
	public void initReader(Class<? extends SyntaxGraphReader> syntaxGraphReader, String inputFile, String inputCharSet, String readerOptions, int iterations) throws MaltChainedException {
		try {	
			reader = syntaxGraphReader.newInstance();
			if (inputFile == null || inputFile.length() == 0 || inputFile.equals("/dev/stdin")) {
				reader.open(System.in, inputCharSet);
			} else if (new File(inputFile).exists()) {
				reader.setNIterations(iterations);
				reader.open(inputFile, inputCharSet);
			} else {
				reader.setNIterations(iterations);
				reader.open(Util.findURL(inputFile), inputCharSet);
			}
			reader.setDataFormatInstance(inputDataFormatInstance); 
			reader.setOptions(readerOptions);
		} catch (InstantiationException e) {
			throw new DataFormatException("The data reader '"+syntaxGraphReader.getName()+"' cannot be initialized. ", e);
		} catch (IllegalAccessException e) {
			throw new DataFormatException("The data reader '"+syntaxGraphReader.getName()+"' cannot be initialized. ", e);
		} 	
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
		final StringBuilder sb = new StringBuilder();
		sb.append("    read ");
		sb.append("target:");
		sb.append(targetName);
		sb.append(' ');
		sb.append("optiongroup:");
		sb.append(optiongroupName);
		return sb.toString();
	}
}