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
import java.util.Random;
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

public class S7_gibbssampling2 {
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
			entitynot.add("knownneg");//Jim Thome::/m/03ldpc, Tony La Russa::/m/046vz9, athleteCoach::/baseball/baseball_player/batting_stats|/baseball/batting_statistics/team||/baseball/baseball_team/current_manager
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

	static double globalBestSettingWeight = 0;
	static HashSet<Integer> globalBestSetting = new HashSet<Integer>();

	//	public static void update2(String vartype, boolean atleastone) throws IOException {
	//		System.err.println("UPDATE\t" + vartype);
	//		for (Entry<String, MatchGroup> e : map_nellname2match.entrySet()) {
	//			MatchGroup mg = e.getValue();
	//			String nellname = mg.nellname;
	//			if (!mg.matchtype.equals(vartype)) {
	//				continue;
	//			}
	//
	//			List<MySparseVector> cc = new ArrayList<MySparseVector>();
	//			List<Double> ccw = new ArrayList<Double>();
	//			List<String[]> explain = new ArrayList<String[]>();
	//			for (int i = 0; i < clauses.length; i++) {
	//				MySparseVector c0 = clauses[i];
	//				boolean c0IsTrue = false;
	//				MySparseVector compressed = new MySparseVector();
	//				for (int d : c0.getDims()) {
	//					int absd = Math.abs(d);
	//					if (mg.postVariables.contains(absd)) {
	//						compressed.add(d, 1);
	//					} else {
	//						if (firedVar.contains(d)) {
	//							c0IsTrue = true;
	//							break;
	//						}
	//					}
	//				}
	//				if (!c0IsTrue) {
	//					cc.add(compressed);
	//					ccw.add(cweights[i]);
	//					String[] exp = jointclause.get(i);
	//					explain.add(exp);
	//				}
	//			}
	//			assert (cc.size() == ccw.size());
	//			mg.weightedSum4Settings.clear();
	//			double maxw = -10000000;
	//			List<Integer> bestsetting = null;
	//			List<Integer> sampledsetting = null;
	//			int bestk = 0;
	//			for (int k = 0; k < mg.settings.size(); k++) {
	//				List<Integer> s = mg.settings.get(k);
	//				double sumw = weightedSat(s, cc, ccw);
	//				mg.weightedSum4Settings.add(sumw);
	//				int numOfM = numOfMatch(s);
	//				if (atleastone && numOfM == 0)
	//					continue;
	//				if (sumw > maxw) {
	//					maxw = sumw;
	//					bestsetting = s;
	//					bestk = k;
	//				}
	//			}
	//			int sampledK = sampler(mg.weightedSum4Settings);
	//			sampledsetting = mg.settings.get(sampledK);
	//
	//			//update
	//			double sumweightbeforehand = totalWeightWithCurrentSetting();
	//			for (int x : sampledsetting) {
	//				firedVar.remove(x);
	//				firedVar.remove(-1 * x);
	//				firedVar.add(x);
	//			}
	//			double sumweightafterhand = totalWeightWithCurrentSetting();
	//
	//			printDebugInfo(explain, cc, bestsetting, mg, maxw, new String[] { "athleteCoach" }, bestk, sampledK,
	//					sampledsetting, sumweightbeforehand, sumweightafterhand);
	//
	//			if (sumweightafterhand > globalBestSettingWeight) {
	//				globalBestSettingWeight = sumweightafterhand;
	//				globalBestSetting.clear();
	//				globalBestSetting.addAll(firedVar);
	//			}
	//
	//		}
	//	}

	public static void update(String vartype, boolean atleastone) throws IOException {
		System.err.println("UPDATE\t" + vartype);
		for (Entry<String, MatchGroup> e : map_nellname2match.entrySet()) {
			MatchGroup mg = e.getValue();
			String nellname = mg.nellname;
			if (!mg.matchtype.equals(vartype)) {
				continue;
			}
			if (mg.nellname.equals("athleteCoach")) {
				System.err.println();
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
			List<Integer> sampledsetting = null;
			int bestk = 0;
			for (int k = 0; k < mg.settings.size(); k++) {
				List<Integer> s = mg.settings.get(k);
				double sumw = weightedSat(s, cc, ccw);
				mg.weightedSum4Settings.add(sumw);
				int numOfM = numOfMatch(s);
				if (atleastone && numOfM == 0)
					continue;
				if (sumw > maxw) {
					maxw = sumw;
					bestsetting = s;
					bestk = k;
				}
			}
			int sampledK = sampler(mg.weightedSum4Settings);
			sampledsetting = mg.settings.get(sampledK);

			//update
			double sumweightbeforehand = totalWeightWithCurrentSetting();
			for (int x : sampledsetting) {
				firedVar.remove(x);
				firedVar.remove(-1 * x);
				firedVar.add(x);
			}
			double sumweightafterhand = totalWeightWithCurrentSetting();
			
			printDebugInfo(explain, cc, bestsetting, mg, maxw, new String[] { "cityLocatedInState" }, bestk, sampledK,
					sampledsetting, sumweightbeforehand, sumweightafterhand);

			if (sumweightafterhand > globalBestSettingWeight) {
				globalBestSettingWeight = sumweightafterhand;
				globalBestSetting.clear();
				globalBestSetting.addAll(firedVar);
			}
			dw_weightchange.write(xaxis_weightchange++, globalBestSettingWeight);
		}
	}

	static int sampler(List<Double> weights) {
		//smallest weight 
		double biggest = -1000000;
		for (int i = 0; i < weights.size(); i++) {
			if (weights.get(i) > biggest)
				biggest = weights.get(i);
		}
		double[] norw = new double[weights.size()];
		double[] norprob = new double[weights.size()];
		double powersum = 0;
		for (int i = 0; i < weights.size(); i++) {
			norw[i] = weights.get(i) - biggest;//make sure exp(norw[i]) will not blow up
			powersum += Math.exp(norw[i]);
		}
		for (int i = 0; i < weights.size(); i++) {
			norprob[i] = Math.exp(norw[i]) / powersum;
		}
		Random r0 = new Random();
		double rnum = r0.nextDouble();
		for (int i = 0; i < weights.size(); i++) {
			rnum -= norprob[i];
			if (rnum < 0) {
				return i;
			}
		}
		return weights.size() - 1;
	}

	static void printDebugInfo(List<String[]> explain, List<MySparseVector> cc, List<Integer> bestsetting,
			MatchGroup mg, double maxw, String[] interests, int bestK, int sampledK, List<Integer> sampledsetting,
			double sumweightbeforehand, double sumweightafterhand) {
		HashSet<String> specialinterestset = new HashSet<String>();
		for (String a : interests)
			specialinterestset.add(a);

		D.p("@@@@@@@");
		for (int i = 0; i < explain.size(); i++) {
			D.p(cc.get(i), explain.get(i));
		}
		for (int b : bestsetting) {
			D.p(b, varId2Name.get(Math.abs(b)));
		}
		D.p("@@@@@@@" + mg.nellname, bestsetting, maxw);

		if (specialinterestset.contains(mg.nellname)) {
			List<String[]> table = new ArrayList<String[]>();
			for (int i = 0; i < mg.settings.size(); i++) {
				table.add(new String[] { mg.nellname, mg.settings.get(i).toString(),
						mg.weightedSum4Settings.get(i).toString() });
			}
			StringTable.sortByColumn(table, new int[] { 2, 1 }, new boolean[] { true, false });
			for (int i = 0; i < table.size() && i < 10; i++) {
				String[] t = table.get(i);
				D.p(t);
			}
		}

		if (bestK != sampledK) {
			D.p("bestsetting vs sampledsetting", bestsetting, sampledsetting);
		}
		D.p("before & after updating", sumweightbeforehand, sumweightafterhand);

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
			if (s.get(i) > 0)
				sumw--;
		}
		return sumw;
	}

	private static double totalWeightWithCurrentSetting() {
		double sumw = 0;
		for (int i = 0; i < clauses.length; i++) {
			MySparseVector msv = clauses[i];
			double w = cweights[i];
			boolean satisfied = false;
			for (int d : msv.getDims()) {
				if (firedVar.contains(d))
					satisfied = true;
			}
			if (satisfied)
				sumw += w;
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
				boolean fired = globalBestSetting.contains(varid);
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
		for (int fv : globalBestSetting) {
			if (fv > 0) {
				String firedvarstr = varId2Name.get(fv);
				String[] ab = firedvarstr.split("::");
				StringTable.mapKey2SetAdd(mapresult, ab[0], ab[1], true);
			}
		}
		HashMap<Integer, String> entity2type = new HashMap<Integer, String>();
		HashSet<String> possibleconsistant = new HashSet<String>();
		{
			//load consistant information
			for (int i = 0; i < jointclause.size(); i++) {
				String[] jc = jointclause.get(i);
				if (jc[0].equals("type_instance")) {
					int entityvarid = Math.abs(Integer.parseInt(jc[2]));
					if (globalBestSetting.contains(entityvarid)) {
						String fbType = jc[6].split("::")[1];
						entity2type.put(entityvarid, fbType);
					}
				}
			}
			for (int i = 0; i < jointclause.size(); i++) {
				String[] jc = jointclause.get(i);
				if (jc[0].equals("relation_instance")) {
					int entity1varid = Math.abs(Integer.parseInt(jc[2]));
					int entity2varid = Math.abs(Integer.parseInt(jc[3]));
					if (globalBestSetting.contains(entity1varid) && globalBestSetting.contains(entity2varid)) {
						String fbtype1 = entity2type.get(entity1varid);
						String fbtype2 = entity2type.get(entity2varid);
						for (int j = 4; j < jc.length; j++) {
							if (jc[j].equals("0"))
								break;
							int relvarid = Integer.parseInt(jc[j]);
							String fbrel = varId2Name.get(relvarid).split("::")[1];
							if (fbrel != null && fbtype1 != null && fbtype2 != null) {
								possibleconsistant.add(fbrel + "\t" + fbtype1 + "\t" + fbtype2);
							}
						}
					}
				}
			}
		}
		DelimitedWriter dw = new DelimitedWriter(output);
		int viewid = 1;
		for (NellRelation nr : Main.no.nellRelationList) {
			//			HashSet<String> typeappeartogether = new HashSet<String>();
			//			for (String[] a : nr.seedInstances) {
			//				String fbt1 = entity2type.get(a[0]);
			//				String fbt2 = entity2type.get(a[1]);
			//				if (fbt1 != null && fbt2 != null) {
			//					typeappeartogether.add(fbt1 + "\t" + fbt2);
			//				}
			//			}
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
							if (possibleconsistant.contains(rm + "\t" + dm + "\t" + rgm)) {
								dw.write(viewid, relname, rm, dm, rgm);
								viewnumforthisrel++;
								viewid++;
							}
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

	public static void printGoldView(){
		
	}
	static String file_debug = Main.file_jointclause + ".gs.debug";
	static String file_weightchange = Main.file_jointclause + ".gs.weight";
	static DelimitedWriter dw_weightchange;
	static int xaxis_weightchange = 1;

	//static DelimitedWriter dwdebug;

	public static void main(String[] args) throws IOException {
		System.setOut(new PrintStream(new FileOutputStream(file_debug)));
		dw_weightchange = new DelimitedWriter(file_weightchange);
		//dwdebug = new DelimitedWriter(debugfile);
		initClause2Consider();
		loadClause(Main.file_jointclause + ".1");
		init();
		for (int i = 0; i < 10; i++) {
			update("type", false);
			update("relation", false);
			update("entity", true);
			//dw_weightchange.write(xaxis_weightchange++, globalBestSettingWeight);
			//D.p("globalbestsetting is", globalBestSettingWeight);
		}
		printVariable(Main.file_jointclause + ".gs.variable");
		printView(Main.file_jointclause + ".gs.view");
		dw_weightchange.close();
		//printDis(Main.file_jointclause + ".relaxlabel");
		//dwdebug.close();

	}
}
