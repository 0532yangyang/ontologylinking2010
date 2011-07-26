package freebase.jointmatch9;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map.Entry;

import javatools.administrative.D;
import javatools.datatypes.MySparseVector;
import javatools.datatypes.QuickSort;
import javatools.filehandlers.DelimitedReader;
import javatools.filehandlers.DelimitedWriter;
import javatools.mydb.StringTable;

class Assignment {
	//	List<String> nellnamelist = new ArrayList<String>();
	//	List<List<Integer>> mgVarIdList = new ArrayList<List<Integer>>();
	//	List<List<Double>> mgProbList = new ArrayList<List<Double>>();
	HashMap<Integer, Double> varId2TrueProb = new HashMap<Integer, Double>();

	public void add(String nellname, List<Integer> varIdOneMg, List<Double> probOneMg) {
		//		nellnamelist.add(nellname);
		//		mgVarIdList.add(varIdOneMg);
		//		mgProbList.add(probOneMg);
		for (int i = 0; i < varIdOneMg.size(); i++) {
			varId2TrueProb.put(varIdOneMg.get(i), probOneMg.get(i));
		}
	}
}

class MatchGroup {
	String nellname;
	String matchtype; //relation; type; entity
	List<Integer> variables = new ArrayList<Integer>();
	List<String> variables_str = new ArrayList<String>();
	List<Double> staticweights = new ArrayList<Double>();
	List<Double> dynamicweights = new ArrayList<Double>();

	public MatchGroup(String nellname, String matchtype) {
		this.nellname = nellname;
		this.matchtype = matchtype;
	}

	public void addCandidate(int varId, String varStr, double w) {
		variables_str.add(varStr);
		variables.add(varId);
		staticweights.add(w);
	}

	public void assignmentByStaticWeight(List<Integer> varList, List<Double> probList) {
		double[] dis = new double[variables.size()];
		double sum = 0;
		for (int i = 0; i < variables.size(); i++) {
			double delta = 1.0 / Math.exp(-1 * staticweights.get(i) + 0.1);
			dis[i] = delta;
			sum += dis[i];
		}
		for (int i = 0; i < dis.length; i++)
			dis[i] /= sum;
		int[] index = QuickSort.quicksort(dis, true);
		for (int i = 0; i < index.length; i++) {
			varList.add(variables.get(index[i]));
			probList.add(dis[index[i]]);
		}
		//D.p(variables_str.get(index[0]),dis[index[0]]);
	}

	public void assignmentByDynamicWeight(List<Integer> varList, List<Double> probList) {
		if (this.nellname.equals("David Heckerman")) {
			D.p("a");
		}
		double[] dis = new double[variables.size()];
		double[] normalweight = new double[variables.size()];
		double wmax = 1, probsum = 0;
		for (int i = 0; i < variables.size(); i++) {
			double w = staticweights.get(i) + dynamicweights.get(i);
			normalweight[i] = w;
			//wmax = Math.max(Math.abs(w), wmax);
		}
		for (int i = 0; i < normalweight.length; i++) {
			double w = normalweight[i];
			double delta = 1.0 / (Math.exp(-1 * w) + 0.1);
			dis[i] = delta;
			probsum += dis[i];
		}
		for (int i = 0; i < dis.length; i++)
			dis[i] /= probsum;
		int[] index = QuickSort.quicksort(dis, true);
		for (int i = 0; i < index.length; i++) {
			varList.add(variables.get(index[i]));
			probList.add(dis[index[i]]);
		}
	}

	public String toString() {
		return nellname + "-->" + variables_str + " " + staticweights;
	}
}

public class S5_relaxlabel {
	static HashMap<Integer, String> varId2Name = new HashMap<Integer, String>();
	static HashMap<String, MatchGroup> map_nellname2match = new HashMap<String, MatchGroup>();
	static List<String[]> jointclause = new ArrayList<String[]>();

	static MySparseVector[] literals;
	static MySparseVector[] clauses;
	static double[] cweights;
	static int csize;
	static int vsize;

	//	static HashSet<Integer> firedVar = new HashSet<Integer>();
	//	static HashSet<Integer> firedClause = new HashSet<Integer>();
	static int firedVar[];
	static int firedClause[];

	static HashMap<Integer, Double> varId2TrueProb = new HashMap<Integer, Double>();

	public static void loadClause(String file_clause) throws IOException {
		DelimitedReader dr = new DelimitedReader(file_clause);
		String[] l;
		while ((l = dr.read()) != null) {
			if (l[0].startsWith("static_")) {
				double staticweight = Double.parseDouble(l[1]);
				String matchtype = l[0].replaceAll("static_", "");
				int varId = Integer.parseInt(l[2]);
				vsize = Math.max(vsize, varId);
				addToMatchGroup(staticweight, varId, l[4], matchtype);
				varId2Name.put(varId, l[4]);
			} else if (!l[0].equals("mutual")) {
				jointclause.add(l);
			}
		}
		dr.close();

		csize = jointclause.size();
		cweights = new double[csize + 1];
		firedClause = new int[csize + 1];
		literals = new MySparseVector[vsize + 1];
		firedVar = new int[vsize + 1];
		clauses = new MySparseVector[csize];
		for (int i = 0; i <= vsize; i++) {
			literals[i] = new MySparseVector();
		}
		for (int i = 0; i < jointclause.size(); i++) {
			clauses[i] = new MySparseVector();
			String[] raw = jointclause.get(i);
			cweights[i] = Double.parseDouble(raw[1]);
			for (int k = 2; k < raw.length; k++) {
				int litId = Integer.parseInt(raw[k]);
				if (litId == 0)
					break;
				clauses[i].add(litId, 1);
				//varId can be less than 0
				int varId = Math.abs(litId);
				int signal = litId / varId;
				literals[varId].add(signal * i, 1);
			}
		}
		D.p("finish");
	}

	private static void addToMatchGroup(double weight, int varId, String varStr, String matchtype) {
		String[] ab = varStr.split("::");
		String nellname = ab[0];
		if (map_nellname2match.containsKey(nellname)) {
			MatchGroup mg = map_nellname2match.get(nellname);
			mg.addCandidate(varId, varStr, weight);
		} else {
			MatchGroup mg = new MatchGroup(nellname, matchtype);
			mg.addCandidate(varId, varStr, weight);
			map_nellname2match.put(nellname, mg);
		}
	}

	public static void init() throws IOException {
		for (Entry<String, MatchGroup> e : map_nellname2match.entrySet()) {
			MatchGroup mg = e.getValue();
			List<Integer> varIdList = new ArrayList<Integer>();
			List<Double> probList = new ArrayList<Double>();
			mg.assignmentByStaticWeight(varIdList, probList);
			for (int i = 0; i < varIdList.size(); i++) {
				varId2TrueProb.put(varIdList.get(i), probList.get(i));
			}
		}
	}

	static void updateFired() throws IOException {
		int[] oldfiredvar = new int[firedVar.length];
		int[] oldfiredClause = new int[firedClause.length];
		{
			//clean up the old setting
			for (int i = 0; i < firedVar.length; i++) {
				oldfiredvar[i] = firedVar[i];
				firedVar[i] = 0;
			}
			for (int i = 0; i < firedClause.length; i++) {
				oldfiredClause[i] = firedClause[i];
				firedClause[i] = 0;
			}
		}
		for (Entry<String, MatchGroup> e : map_nellname2match.entrySet()) {
			MatchGroup mg = e.getValue();
			//List<Integer> varIdList = mg.variables;
			double max = 0;
			int maxVarId = 0;
			for (int i = 0; i < mg.variables.size(); i++) {

				double v = varId2TrueProb.get(mg.variables.get(i));
				if (v > max) {
					maxVarId = mg.variables.get(i);
					max = v;
				}
			}
			if (maxVarId == 0) {
				D.p("a");
			}
			assert maxVarId != 0 : "maxVarId is becomming zero!!!";
			firedVar[maxVarId] = 1;
		}
		for (int i = 0; i < clauses.length; i++) {
			List<Integer> lits = clauses[i].getDims();
			//number of positive literals\
			int numPosLit = 0;
			boolean fired = true;// the clause is fired only if all its negative literals are fired
			for (int lit : lits) {
				if (lit > 0) {
					numPosLit++;
				} else {
					int tmp = -1 * lit;
					if (firedVar[tmp] == 0) {
						fired = false;
					}
				}
			}
			assert numPosLit == 1 : "the value of numPosLit is:" + numPosLit;
			assert lits.size() != 1 : "the size of lits is:" + lits.size();
			if (fired) {
				firedClause[i] = 1;
			}
		}

		//print fired variables and clauses
		{
			int numChanged = 0;
			List<String[]> towrite = new ArrayList<String[]>();
			for (int i = 0; i < firedVar.length; i++) {
				if (firedVar[i] == 1)
					towrite.add(new String[] { i + "", varId2Name.get(i) });
				if (oldfiredvar[i] != firedVar[i])
					numChanged++;

			}
			for (int i = 0; i < firedClause.length; i++) {
				if (firedClause[i] == 1)
					towrite.add(jointclause.get(i));
			}
			StringTable.sortByColumn(towrite, new int[] { 0 });
			for (String[] t : towrite) {
				dwdebug.write(t);
			}
			D.p("number of changed assignment in this turn", numChanged);
		}
	}

	static void printFired(String file) throws IOException {
		DelimitedWriter dw = new DelimitedWriter(file);
		int numChanged = 0;
		List<String[]> towrite = new ArrayList<String[]>();
		for (int i = 0; i < firedVar.length; i++) {
			if (firedVar[i] == 1)
				towrite.add(new String[] { i + "", varId2Name.get(i) });
		}
		for (int i = 0; i < firedClause.length; i++) {
			if (firedClause[i] == 1)
				towrite.add(jointclause.get(i));
		}
		StringTable.sortByColumn(towrite, new int[] { 0 });
		for (String[] t : towrite) {
			dw.write(t);
		}
		D.p("number of changed assignment in this turn", numChanged);
		dw.close();
	}

	static void printDis(String file) throws IOException {

		List<String[]> towrite = new ArrayList<String[]>();
		for (Entry<String, MatchGroup> e : map_nellname2match.entrySet()) {
			MatchGroup mg = e.getValue();
			List<Integer> varList = new ArrayList<Integer>();
			List<Double> probList = new ArrayList<Double>();
			mg.assignmentByDynamicWeight(varList, probList);
			for (int i = 0; i < varList.size(); i++) {
				int varId = varList.get(i);
				towrite.add(new String[] { mg.matchtype, mg.nellname, varId2Name.get(varId), probList.get(i) + "" });
			}
		}
		StringTable.sortByColumn(towrite, new int[] { 0, 1, 3 }, new boolean[] { false, false, true });
		DelimitedWriter dw = new DelimitedWriter(file);
		for (String[] t : towrite) {
			dw.write(t);
		}
		dw.close();
	}

	public static void update(String matchtype) throws IOException {
		dwdebug.write("UPDATE", matchtype);
		for (Entry<String, MatchGroup> e : map_nellname2match.entrySet()) {
			MatchGroup mg = e.getValue();
			String nellname = mg.nellname;
			if (nellname.equals("city")) {
				D.p("city");
			}
			if (!mg.matchtype.equals(matchtype)) {
				continue;
			}
			mg.dynamicweights.clear();
			for (int i = 0; i < mg.variables.size(); i++) {
				int var = mg.variables.get(i);
				MySparseVector msv = literals[var];
				double weightnew = 0;
				for (int c : msv.getDims()) {
					if (c > 0 && firedClause[c] == 1) {
						weightnew += cweights[c];
					}
				}
				mg.dynamicweights.add(weightnew);
				//dwdebug.write(varId2Name.get(var), weightnew);
			}
			assert (mg.dynamicweights.size() == mg.staticweights.size());
			List<Integer> varList = new ArrayList<Integer>();
			List<Double> probList = new ArrayList<Double>();
			mg.assignmentByDynamicWeight(varList, probList);
			for (int i = 0; i < varList.size(); i++) {
				varId2TrueProb.put(varList.get(i), probList.get(i));
			}
		}
	}

	public static void fromProp2RealMatching(double threshold, String matchtype,
			HashMap<String, List<String>> mapresult, String file) throws IOException {
		List<String[]> towrite = new ArrayList<String[]>();
		for (Entry<String, MatchGroup> e : map_nellname2match.entrySet()) {
			MatchGroup mg = e.getValue();
			if (!mg.matchtype.equals(matchtype))
				continue;
			List<Integer> varList = new ArrayList<Integer>();
			List<Double> probList = new ArrayList<Double>();
			mg.assignmentByDynamicWeight(varList, probList);
			List<String[]> temp = new ArrayList<String[]>();
			for (int i = 0; i < varList.size(); i++) {
				int varId = varList.get(i);
				temp.add(new String[] { varId2Name.get(varId), probList.get(i) + "" });
				//towrite.add(new String[] { mg.nellname, varId2Name.get(varId), probList.get(i) + "" });
			}
			StringTable.sortByColumn(temp, new int[] { 1 }, new boolean[] { true }, new boolean[] { false });
			double sumup = 0, last = 0;
			for (int i = 0; i < temp.size(); i++) {
				double now = Double.parseDouble(temp.get(i)[1]);
				if (sumup < threshold && now > 0.5 * last) {
					towrite.add(new String[] { mg.nellname, temp.get(i)[0], temp.get(i)[1] });
					sumup += now;
					last = now;
				}
			}
		}
		DelimitedWriter dw = new DelimitedWriter(file);
		for (String[] t : towrite) {
			StringTable.mapKey2SetAdd(mapresult, t[0], t[1], true);
			dw.write(t);
		}
		dw.close();
	}

	public static void printView(HashMap<String, List<String>> mapresult, String output) throws IOException {
		DelimitedWriter dw = new DelimitedWriter(output);
		int viewid = 1;
		for (NellRelation nr : Main.no.nellRelationList) {
			String domain = nr.domain;
			String range = nr.range;
			String relname = nr.relation_name;
			List<String> relMap = mapresult.get(relname);
			List<String> rangeMap = mapresult.get(range);
			List<String> domainMap = mapresult.get(domain);
			
			if (relMap != null && rangeMap != null && domainMap != null) {
				for (String rm : relMap) {
					for (String rgm : rangeMap) {
						for (String dm : domainMap) {
							dw.write(viewid, relname, rm.split("::")[1], dm.split("::")[1],rgm.split("::")[1] );
							viewid++;
						}
					}
				}
			} else {
				//dw.write(relname,"","","");
			}
		}
		dw.close();
	}

	static String debugfile = Main.file_jointclause + ".debug";
	static DelimitedWriter dwdebug;

	public static void main(String[] args) throws IOException {
		dwdebug = new DelimitedWriter(debugfile);
		loadClause(Main.file_jointclause + ".1");
		init();
		for (int i = 0; i < 10; i++) {
			updateFired();
			update("type");
			update("relation");
			update("entity");
		}
		printFired(Main.file_jointclause + ".debug2");
		{
			HashMap<String, List<String>> mapresult = new HashMap<String, List<String>>();
			fromProp2RealMatching(0.8, "relation", mapresult, Main.file_jointclause + ".relaxlabel.relation");
			fromProp2RealMatching(0.8, "type", mapresult, Main.file_jointclause + ".relaxlabel.type");
			fromProp2RealMatching(0.1, "entity", mapresult, Main.file_jointclause + ".relaxlabel.entity");
			printView(mapresult, Main.file_jointclause + ".relaxlabel.view");
		}
		//printDis(Main.file_jointclause + ".relaxlabel");
		dwdebug.close();

	}
}
