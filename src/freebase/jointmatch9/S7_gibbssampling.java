package freebase.jointmatch9;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;

import javatools.administrative.D;
import javatools.datatypes.MySparseVector;
import javatools.datatypes.QuickSort;
import javatools.filehandlers.DelimitedReader;
import javatools.filehandlers.DelimitedWriter;
import javatools.mydb.StringTable;

//class Assignment {
//	//	List<String> nellnamelist = new ArrayList<String>();
//	//	List<List<Integer>> mgVarIdList = new ArrayList<List<Integer>>();
//	//	List<List<Double>> mgProbList = new ArrayList<List<Double>>();
//	HashMap<Integer, Double> varId2StaticProb = new HashMap<Integer, Double>();
//
//	public void add(String nellname, List<Integer> varIdOneMg, List<Double> probOneMg) {
//		//		nellnamelist.add(nellname);
//		//		mgVarIdList.add(varIdOneMg);
//		//		mgProbList.add(probOneMg);
//		for (int i = 0; i < varIdOneMg.size(); i++) {
//			varId2StaticProb.put(varIdOneMg.get(i), probOneMg.get(i));
//		}
//	}
//}

class MatchGroup {
	String nellname;
	String matchtype; //relation; type; entity
	List<Integer> variables = new ArrayList<Integer>();
	List<String> variables_str = new ArrayList<String>();
	List<Double> staticweights = new ArrayList<Double>();

	HashSet<Integer> postVariables = new HashSet<Integer>();// x in p(x|...); all other variables are prior
	List<List<Integer>> settings = new ArrayList<List<Integer>>();
	List<Double> weightedSum4Settings = new ArrayList<Double>();
	final int topK = 10;

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
			double delta = Math.exp(staticweights.get(i));
			dis[i] = delta;
		}
		{
			int[] index = QuickSort.quicksort(dis, true);
			int i = 0;
			for (; i < index.length && i < topK; i++) {
				sum += dis[i];
			}
			for (; i < index.length; i++) {
				dis[i] = 0;
			}
			for (i = 0; i < dis.length; i++)
				dis[i] /= sum;
		}
		{
			int[] index = QuickSort.quicksort(dis, true);
			for (int i = 0; i < index.length; i++) {
				varList.add(variables.get(index[i]));
				probList.add(dis[index[i]]);
			}
		}
		//D.p(variables_str.get(index[0]),dis[index[0]]);
	}

	public String toString() {
		return nellname + "-->" + variables_str + " " + staticweights;
	}
}

public class S7_gibbssampling {
	static HashMap<Integer, String> varId2Name = new HashMap<Integer, String>();
	static HashMap<String, MatchGroup> map_nellname2match = new HashMap<String, MatchGroup>();
	static List<String[]> jointclause = new ArrayList<String[]>();

	static MySparseVector[] literals;
	static MySparseVector[] clauses;
	static double[] cweights;
	static int csize;
	static int vsize;

	static HashSet<Integer> firedVar = new HashSet<Integer>();
	//	static HashSet<Integer> firedClause = new HashSet<Integer>();
	//static int firedVar[];

	static HashMap<Integer, Double> varId2StaticProb = new HashMap<Integer, Double>();

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
			}
			jointclause.add(l);
		}
		dr.close();

		csize = jointclause.size();
		cweights = new double[csize + 1];

		literals = new MySparseVector[vsize + 1];
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
				varId2StaticProb.put(varIdList.get(i), probList.get(i));
			}
		}
		firedVar.clear();
		for (Entry<String, MatchGroup> e : map_nellname2match.entrySet()) {
			MatchGroup mg = e.getValue();
			double largepr = 0;
			int largevar = -1;
			for (int i = 0; i < mg.variables.size(); i++) {
				int var = mg.variables.get(i);
				double pr = varId2StaticProb.get(var);
				if (pr > largepr) {
					largepr = pr;
					largevar = var;
				}
			}
			assert (largevar >= 0);
			firedVar.add(largevar);
			for (int i = 0; i < mg.variables.size(); i++) {
				int var = mg.variables.get(i);
				if (var != largevar) {
					firedVar.add(-1 * var);
				}
			}
		}
		for (Entry<String, MatchGroup> e : map_nellname2match.entrySet()) {
			MatchGroup mg = e.getValue();
			List<List<Integer>> settings = new ArrayList<List<Integer>>();
			for (int i = 0; i < mg.variables.size(); i++) {
				int vid = mg.variables.get(i);
				if (varId2StaticProb.get(vid) == 0) {
					continue;// the variable always false, if its staticProb is too low
				}
				mg.postVariables.add(vid);
				{
					/**get 2^n different settings*/
					if (settings.size() == 0) {
						List<Integer> n1 = new ArrayList<Integer>();
						List<Integer> n2 = new ArrayList<Integer>();
						n1.add(vid);
						n2.add(-1 * vid);
						settings.add(n1);
						settings.add(n2);
						continue;
					}
					Iterator<List<Integer>> it = settings.iterator();
					List<List<Integer>> newsettings = new ArrayList<List<Integer>>();
					while (it.hasNext()) {
						List<Integer> old = it.next();
						List<Integer> n1 = new ArrayList<Integer>();
						List<Integer> n2 = new ArrayList<Integer>();
						n1.addAll(old);
						n2.addAll(old);
						n1.add(vid);
						n2.add(-1 * vid);
						newsettings.add(n1);
						newsettings.add(n2);
					}
					settings = newsettings;
				}
			}
			mg.settings = settings;
		}
	}

	//	static void printFired(String file) throws IOException {
	//		DelimitedWriter dw = new DelimitedWriter(file);
	//		int numChanged = 0;
	//		List<String[]> towrite = new ArrayList<String[]>();
	//		for (int i = 0; i < firedVar.length; i++) {
	//			if (firedVar[i] == 1)
	//				towrite.add(new String[] { i + "", varId2Name.get(i) });
	//		}
	//		for (int i = 0; i < firedClause.length; i++) {
	//			if (firedClause[i] == 1)
	//				towrite.add(jointclause.get(i));
	//		}
	//		StringTable.sortByColumn(towrite, new int[] { 0 });
	//		for (String[] t : towrite) {
	//			dw.write(t);
	//		}
	//		D.p("number of changed assignment in this turn", numChanged);
	//		dw.close();
	//	}

	//	static void printDis(String file) throws IOException {
	//
	//		List<String[]> towrite = new ArrayList<String[]>();
	//		for (Entry<String, MatchGroup> e : map_nellname2match.entrySet()) {
	//			MatchGroup mg = e.getValue();
	//			List<Integer> varList = new ArrayList<Integer>();
	//			List<Double> probList = new ArrayList<Double>();
	//			mg.assignmentByDynamicWeight(varList, probList);
	//			for (int i = 0; i < varList.size(); i++) {
	//				int varId = varList.get(i);
	//				towrite.add(new String[] { mg.matchtype, mg.nellname, varId2Name.get(varId), probList.get(i) + "" });
	//			}
	//		}
	//		StringTable.sortByColumn(towrite, new int[] { 0, 1, 3 }, new boolean[] { false, false, true });
	//		DelimitedWriter dw = new DelimitedWriter(file);
	//		for (String[] t : towrite) {
	//			dw.write(t);
	//		}
	//		dw.close();
	//	}

	static HashMap<String, Set<String>> clausesNot2ConsiderWhenUpdate = new HashMap<String, Set<String>>();

	//	static HashSet<String>clausesNot2ConsiderWhenUpdateType = new HashSet<String>();
	//	static HashSet<String>clausesNot2ConsiderWhenUpdateEntity = new HashSet<String>();
	//	static HashSet<String>clausesNot2ConsiderWhenUpdateRelation = new HashSet<String>();

	public static void initClause2Consider() {
		{
			Set<String> entitynot = new HashSet<String>();
			entitynot.add("type_instance");
			entitynot.add("relation_instance");
			clausesNot2ConsiderWhenUpdate.put("entity", entitynot);
		}
		{
			Set<String> entitynot = new HashSet<String>();
			entitynot.add("relation_instance_inv");
			clausesNot2ConsiderWhenUpdate.put("relation", entitynot);
		}
		{
			Set<String> entitynot = new HashSet<String>();
			//entitynot.add("type_instance_inv");
			clausesNot2ConsiderWhenUpdate.put("type", entitynot);
		}
	}

	public static void update(String vartype, boolean atleastone) throws IOException {
		System.err.println("UPDATE\t" + vartype);
		for (Entry<String, MatchGroup> e : map_nellname2match.entrySet()) {
			MatchGroup mg = e.getValue();
			String nellname = mg.nellname;
			if (nellname.equals("Keanu Reeves")) {
				System.err.println(nellname);
			}
			if (!mg.matchtype.equals(vartype)) {
				continue;
			}

			List<MySparseVector> cc = new ArrayList<MySparseVector>();
			List<Double> ccw = new ArrayList<Double>();
			List<String[]> explain = new ArrayList<String[]>();
			for (int var : mg.postVariables) {
				MySparseVector msv = literals[var];
				for (int clauseId : msv.getDims()) {
					MySparseVector c = clauses[Math.abs(clauseId)];
					double cw = cweights[Math.abs(clauseId)];
					String[] exp = jointclause.get(Math.abs(clauseId));
					MySparseVector compressed = new MySparseVector();
					boolean clauseValDecided = false;
					for (int d : c.getDims()) {
						if (mg.postVariables.contains(Math.abs(d))) {
							compressed.add(d, 1);
						} else {
							/** checked the value of d, if it is true, then I don't need the clause at all; the clause is added
							 * into considerclause only if all d are false*/
							if (firedVar.contains(d)) {
								clauseValDecided = true;
							}
						}
					}
					if (!clauseValDecided && !clausesNot2ConsiderWhenUpdate.get(vartype).contains(exp[0])) {
						cc.add(compressed);
						ccw.add(cw);
						explain.add(exp);
					}
				}
			}
			assert (cc.size() == ccw.size());
			mg.weightedSum4Settings.clear();
			double maxw = -10000000;
			List<Integer> bestsetting = null;
			for (List<Integer> s : mg.settings) {
				double sumw = weightedSat(s, cc, ccw);
				mg.weightedSum4Settings.add(sumw);
				int numOfM = numOfMatch(s);
				if (atleastone && numOfM == 0)
					continue;
				if (sumw > maxw) {
					maxw = sumw;
					bestsetting = s;
				}
			}
			//update
			for (int x : bestsetting) {
				firedVar.remove(x);
				firedVar.remove(-1 * x);
				firedVar.add(x);
			}
			{
				D.p("@@@@@@@");
				for (int i = 0; i < explain.size(); i++) {
					D.p(cc.get(i), explain.get(i));
				}
				for (int b : bestsetting) {
					D.p(b, varId2Name.get(Math.abs(b)));
				}
				D.p("@@@@@@@" + mg.nellname, bestsetting, maxw);

				if (mg.nellname.equals("cityLocatedInCountry")) {
					List<String[]> table = new ArrayList<String[]>();
					for (int i = 0; i < mg.settings.size(); i++) {
						table.add(new String[] { mg.nellname, mg.settings.get(i).toString(),
								mg.weightedSum4Settings.get(i).toString() });
					}
					StringTable.sortByColumn(table, new int[] { 2, 1 }, new boolean[] { true, false });
					for (String[] t : table) {
						D.p(t);
					}
				}
			}
		}
	}

	private static double weightedSat(List<Integer> s, List<MySparseVector> cc, List<Double> ccw) {
		HashSet<Integer> satvar = new HashSet<Integer>();
		satvar.addAll(s);
		double sumw = 0;
		for (int i = 0; i < cc.size(); i++) {
			MySparseVector msv = cc.get(i);
			double w = ccw.get(i);
			boolean satisfied = false;
			for (int d : msv.getDims()) {
				if (satvar.contains(d))
					satisfied = true;
			}
			if (satisfied)
				sumw += w;
		}
		/**prior: i don't want s contain too many TRUE*/
		for (int i = 0; i < s.size(); i++) {
			if (s.get(i) >0)
				sumw--;
		}
		return sumw;
	}

	private static int numOfMatch(List<Integer> s) {
		int res = 0;
		for (int a : s) {
			if (a > 0) {
				res++;
			}
		}
		return res;
	}

	static void printVariable(String output) throws IOException {
		List<String[]> table = new ArrayList<String[]>();
		for (Entry<String, MatchGroup> e : map_nellname2match.entrySet()) {
			String nellname = e.getKey();
			MatchGroup mg = e.getValue();
			for (int i = 0; i < mg.variables.size(); i++) {
				int varid = mg.variables.get(i);
				boolean fired = firedVar.contains(varid);
				if (fired) {
					table.add(new String[] { mg.matchtype, varid + "", varId2Name.get(varid) });
				}
			}
		}

		DelimitedWriter dw = new DelimitedWriter(output);
		StringTable.sortByIntColumn(table, new int[] { 1 });
		for (String[] w : table) {
			dw.write(w);
		}
		dw.close();
	}

	public static void printView(String output) throws IOException {
		HashMap<String, List<String>> mapresult = new HashMap<String, List<String>>();
		for (int fv : firedVar) {
			if (fv > 0) {
				String firedvarstr = varId2Name.get(fv);
				String[] ab = firedvarstr.split("::");
				StringTable.mapKey2SetAdd(mapresult, ab[0], ab[1], true);
			}
		}
		DelimitedWriter dw = new DelimitedWriter(output);
		int viewid = 1;
		for (NellRelation nr : Main.no.nellRelationList) {
			String domain = nr.domain;
			String range = nr.range;
			String relname = nr.relation_name;
			int viewnumforthisrel = 0;
			List<String> relMap = mapresult.get(relname);
			List<String> rangeMap = mapresult.get(range);
			List<String> domainMap = mapresult.get(domain);

			if (relMap != null && rangeMap != null && domainMap != null) {
				for (String rm : relMap) {
					for (String rgm : rangeMap) {
						for (String dm : domainMap) {
							dw.write(viewid, relname, rm, dm, rgm);
							viewnumforthisrel++;
							viewid++;
						}
					}
				}
			}
			if (viewnumforthisrel == 0) {
				D.p("NO VIEW:", nr.relation_name);
			}
		}
		dw.close();
	}

	static String debugfile = Main.file_jointclause + ".debug";

	//static DelimitedWriter dwdebug;

	public static void main(String[] args) throws IOException {
		System.setOut(new PrintStream(new FileOutputStream(debugfile)));
		//dwdebug = new DelimitedWriter(debugfile);
		initClause2Consider();
		loadClause(Main.file_jointclause + ".1");
		init();
		for (int i = 0; i < 3; i++) {
			update("type", false);
			update("relation", false);
			update("entity", true);
		}
		{
			printVariable(Main.file_jointclause + ".gs.variable");
			printView(Main.file_jointclause + ".gs.view");
		}
		//printDis(Main.file_jointclause + ".relaxlabel");
		//dwdebug.close();

	}
}
