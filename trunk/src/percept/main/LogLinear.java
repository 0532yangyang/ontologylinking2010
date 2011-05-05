package percept.main;

import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Random;

import javatools.administrative.D;
import javatools.filehandlers.DelimitedWriter;

import percept.learning.algorithm.CRFParameters;
import percept.learning.algorithm.CollinsTraining;
import percept.learning.algorithm.Model;
import percept.learning.data.Dataset;
import percept.learning.data.MemoryDataset;
import percept.preprocess.ConvertToExamples;
import percept.preprocess.Mappings;

public class LogLinear {
	// store everything
	String dir = "";
	String trainfile = "";

	Model model;
	public CRFParameters params;
	CollinsTraining ct;
	Random random;
	Mappings mappings;

	/** temp files */
	String mappingfile;
	String binaryTrain;
	String mappingFile;
	String modelFile;

	public LogLinear(String dir, String trainfile) {
		this.dir = dir;
		this.trainfile = trainfile;
		{
			String[] a = trainfile.split("/");
			String filehand = a[a.length - 1];
			binaryTrain = dir + "/" + filehand + ".tempbinarytrain";
			mappingFile = dir + "/" + filehand + ".tempmapping";
			modelFile = dir + "/" + filehand + ".tempmodel";

			try {
				ConvertToExamples.convert(trainfile, binaryTrain, mappingFile, true, true);
				model = new Model();
				mappings = new Mappings();
				mappings.read(mappingFile);
				model.numStates = mappings.numStates();
				model.numFeaturesPerState = new int[model.numStates];
				for (int i = 0; i < model.numStates; i++)
					model.numFeaturesPerState[i] = mappings.numFeatures();
				model.write(modelFile);

				Random random = new Random((new Date()).getTime());

				Model model = new Model();
				model.read(modelFile);

				ct = new CollinsTraining(model, random);

				Dataset train = new MemoryDataset(random, binaryTrain);

				// D.p("starting training", trainfile);
				params = ct.train(train);

			} catch (IOException e) {
				// TODO Auto-generated catch block
				System.err.println("Fail to train the log linear model!!!");
				e.printStackTrace();
			}
		}
	}

	public LogLinear(String modelfile) {

	}

	public List<String> predict(String testfile) {
		List<String> predicts = new ArrayList<String>();
		try {
			String[] temp = testfile.split("/");
			String binaryTest = dir + "/" + temp[temp.length - 1] + ".binarytest";
			ConvertToExamples.convert(testfile, binaryTest, mappingFile, false, false);
			Dataset test = new MemoryDataset(random, binaryTest);
			List<Integer> intres = ct.testUnknown(test, params);
			for (int i = 0; i < intres.size(); i++) {
				predicts.add(mappings.getStateName(intres.get(i)));
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return predicts;
	}

	public List<String> binarypredict(String testfile) {
		List<String> predicts = new ArrayList<String>();
		try {
			DecimalFormat df = new DecimalFormat("#.###");
			int[][] confusion = new int[mappings.numStates()][mappings.numStates()];
			String[] temp = testfile.split("/");
			String binaryTest = dir + "/" + temp[temp.length - 1] + ".binarytest";
			ConvertToExamples.convert(testfile, binaryTest, mappingFile, false, false);
			Dataset test = new MemoryDataset(random, binaryTest);
			List<Integer> preds = ct.testUnknown(test, params, confusion);
			int all = 0, error = 0;
			for (int i = 0; i < preds.size(); i++) {
				predicts.add(mappings.getStateName(preds.get(i)));
			}
			int pp = 0, pn = 0, np = 0;
			for (int i = 0; i < mappings.numStates(); i++) {
				String namei = mappings.getStateName(i);
				int sum = 0;
				for (int j = 0; j < mappings.numStates(); j++) {
					String namej = mappings.getStateName(j);
					if (i == j) {
						if (!namei.startsWith("NEG")) {
							pp += confusion[i][j];
						}
					} else {
						if (namei.startsWith("NEG")) {
							np += confusion[i][j];
						} else {
							pn += confusion[i][j];
						}
					}

				}

			}
			double precision = pp * 1.0 / (pp + np);
			double recall = pp * 1.0 / (pp + pn);
			D.p("Precision/Recall", df.format(precision), df.format(recall));
		} catch (IOException e) {
			e.printStackTrace();
		}
		return predicts;
	}

	public List<String> predict(String testfile, String debugfile) {
		List<String> predicts = new ArrayList<String>();
		try {
			DecimalFormat df = new DecimalFormat("#.###");
			DelimitedWriter dw = new DelimitedWriter(debugfile + ".org");
			int[][] confusion = new int[mappings.numStates()][mappings.numStates()];
			String[] temp = testfile.split("/");
			String binaryTest = dir + "/" + temp[temp.length - 1] + ".binarytest";
			ConvertToExamples.convert(testfile, binaryTest, mappingFile, false, false);
			Dataset test = new MemoryDataset(random, binaryTest);
			List<Integer> preds = ct.testUnknown(test, params, confusion);
			int all = 0, error = 0;
			int pp = 0, pn = 0, np = 0;
			for (int i = 0; i < preds.size(); i++) {
				predicts.add(mappings.getStateName(preds.get(i)));
			}
			for (int i = 0; i < mappings.numStates(); i++) {
				int sum = 0;
				for (int j = 0; j < mappings.numStates(); j++) {
					sum += confusion[i][j];
				}
				String rightname = mappings.getStateName(i);
				dw.write("* Total of " + rightname + ":", sum);
				for (int j = 0; j < mappings.numStates(); j++) {
					if (confusion[i][j] > 0) {
						String wrongname = mappings.getStateName(j);
						Double x = confusion[i][j] * 1.0 / sum;
						dw.write("**", rightname, wrongname, confusion[i][j], df.format(x));
						if (i != j) {
							error += confusion[i][j];
						}
						all += confusion[i][j];
					}
				}
			}
			D.p("Error Rate", error, all);
			precisionrecall(confusion, mappings.getStateName(), dw);
			dw.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return predicts;
	}

	public void precisionrecall(int[][] confusion, List<String> names, DelimitedWriter dw) {

		int len = confusion.length;
		int[] pp = new int[len];
		int[] pn = new int[len];// precision
		int[] np = new int[len];// recall

		for (int i = 1; i < len; i++) {
			np[i] += confusion[i][0];
		}
		for (int j = 0; j < len; j++) {
			pn[j] += confusion[0][j];
		}
		for (int i = 1; i < confusion.length; i++) {
			for (int j = 1; j < confusion.length; j++) {
				if (i == j) {
					pp[i] += confusion[i][j];
				} else {
					pn[j] += confusion[i][j];
				}
			}
		}
		try {
			int pp0 = 0, np0 = 0, pn0 = 0;
			for (int i = 1; i < len; i++) {
				pp0 += pp[i];
				np0 += np[i];
				pn0 += pn[i];

				double prec = pp[i] * 1.0 / (pp[i] + pn[i]);
				double recall = pp[i] * 1.0 / (pp[i] + np[i]);

				dw.write(names.get(i), prec, recall);

			}
			double prec = pp0 * 1.0 / (pp0 + pn0);
			double recall = pp0 * 1.0 / (pp0 + np0);
			D.p("OVERALL", prec, recall);
			dw.write("OVERALL", prec, recall);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void printDebug(String outputfile) throws IOException {
		DelimitedWriter dw = new DelimitedWriter(outputfile);
		dw.write("mapping file");
		dw.write(this.mappings.list_states.size());
		dw.write(this.mappings.list_fts.size());
		double[][] v = new double[this.mappings.list_states.size()][this.mappings.list_fts.size()];
		for (int i = 0; i < mappings.list_states.size(); i++) {
			for (int j = 0; j < mappings.list_fts.size(); j++) {
				if (i < params.stateParameters.length && j < params.stateParameters[i].vals.length) {
					v[i][j] = params.stateParameters[i].vals[j];
				}
			}
		}
		for (int j = 0; j < this.mappings.list_fts.size(); j++) {
			try {
				if (this.mappings.list_fts.get(j) == null)
					break;
				StringBuilder sb = new StringBuilder();
				for (int i = 0; i < this.mappings.list_states.size(); i++) {
					if (v[i][j] > 0) {
						sb.append(mappings.list_states.get(i)).append(":").append(v[i][j]).append(" ");
					}
				}
				dw.write(this.mappings.list_fts.get(j), sb.toString());
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		//		for (int j = 0; j < mappings.list_fts.size(); j++) {
		//			StringBuilder sb = new StringBuilder();
		//			for (int i = 0; i < params.stateParameters.length; i++) {
		//				double v[] = params.stateParameters[i].vals;
		//				if (v != null && v.length > j && v[j] > 0) {
		//					sb.append(mappings.list_states.get(i)).append(":").append(v[j]).append(" ");
		//				}
		//			}
		//			dw.write(mappings.list_fts.get(j), sb.toString());
		//		}
		dw.close();
	}

	public static void main(String[] args) {
		String dir = "/scratch/Downloads/example1";
		LogLinear ll = new LogLinear(dir, dir + "/train.dat");
		List<String> predicts = ll.predict(dir + "/test.dat", dir + "/debug");
		D.p(predicts);
	}
}
