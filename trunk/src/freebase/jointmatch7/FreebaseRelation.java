package freebase.jointmatch7;

import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javatools.administrative.D;
import javatools.filehandlers.DelimitedReader;
import javatools.filehandlers.DelimitedWriter;
import javatools.mydb.StringTable;
import javatools.string.StringUtil;

class SingleRelation {

	String nellRelationName;
	String fbRelationName;
	HashSet<String> instances = new HashSet<String>();
	HashMap<String, Double> mapArg1TypeWeight = new HashMap<String, Double>();
	HashMap<String, Double> mapArg2TypeWeight = new HashMap<String, Double>();

	public SingleRelation(List<String[]> b) {
		nellRelationName = b.get(0)[0];
		fbRelationName = b.get(0)[1];
		for (String[] l : b) {
			putArg1Type(l[2], 1);
			putArg2Type(l[3], 1);
			instances.add(l[4] + "\t" + l[5]);
		}
	}

	public SingleRelation(String relationname) {
		this.fbRelationName = relationname;
	}

	public void putArg1Type(String arg1type, double value) {
		if (!mapArg1TypeWeight.containsKey(arg1type)) {
			mapArg1TypeWeight.put(arg1type, value);
		} else {
			double oldval = mapArg1TypeWeight.get(arg1type);
			mapArg1TypeWeight.put(arg1type, value + oldval);
		}
	}

	public void putArg2Type(String arg2type, double value) {
		if (!mapArg2TypeWeight.containsKey(arg2type)) {
			mapArg2TypeWeight.put(arg2type, value);
		} else {
			double oldval = mapArg2TypeWeight.get(arg2type);
			mapArg2TypeWeight.put(arg2type, value + oldval);
		}
	}

	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append(fbRelationName + "; {Arg1Type= ");
		for (Entry<String, Double> e : mapArg1TypeWeight.entrySet()) {
			sb.append(e.getKey() + ":" + e.getValue() + " ");
		}
		sb.append("}; {Arg2Type= ");
		for (Entry<String, Double> e : mapArg2TypeWeight.entrySet()) {
			sb.append(e.getKey() + ":" + e.getValue() + " ");
		}
		sb.append("}");
		return sb.toString();
	}
}

enum UnionRelationScoreIndex {
	SHAREWORDS, //share words
	SHAREINSTANCE, //share instance
	EXPLOSIVEJOIN, //explosive when join
	SEEDNEGATIVE, //negative seeds
	WIKILINK, //wikipedia link
	NUMINSTANCE, //number of instances
	JOINLEN,//join length
}

class UnionRelation {
	boolean isGood = false;
	int unionRelId = 0;
	HashSet<String> instances = new HashSet<String>();
	String nellRelationName;
	String unionRelationName;
	List<SingleRelation> listSingleRelation = new ArrayList<SingleRelation>();

	HashMap<UnionRelationScoreIndex, Double> scores = new HashMap<UnionRelationScoreIndex, Double>();
	static HashMap<UnionRelationScoreIndex, Double> weightForScore = new HashMap<UnionRelationScoreIndex, Double>();

	public UnionRelation(int id, String unionRelationName, List<SingleRelation> srlist) {
		this.unionRelId = id;
		this.unionRelationName = unionRelationName;
		this.nellRelationName = srlist.get(0).nellRelationName;
		this.listSingleRelation = srlist;
		for (SingleRelation sr : srlist) {
			instances.addAll(sr.instances);
		}
	}

	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append(nellRelationName + "::" + instances.size() + "::" + unionRelationName + "\n");
		for (SingleRelation sr : listSingleRelation) {
			sb.append("\tSINGLE: " + sr.toString() + "\n");
		}
		return sb.toString();
	}

	public String[] getSingleRelNames() {
		String[] res = new String[listSingleRelation.size()];
		for (int i = 0; i < res.length; i++) {
			res[i] = listSingleRelation.get(i).fbRelationName;
		}
		return res;
	}

	public void setScore(UnionRelationScoreIndex uri, double value) {
		this.scores.put(uri, value);
	}

	public static void setWeight(UnionRelationScoreIndex uri, double value) {
		weightForScore.put(uri, value);
	}

	public static double getWeight(UnionRelationScoreIndex uri) {
		return weightForScore.get(uri);
	}
}

public class FreebaseRelation {

	/**
	 * @param args
	 */
	private List<UnionRelation> listUnionRelation = new ArrayList<UnionRelation>();
	List<UnionRelation> filteredUnionRelations = new ArrayList<UnionRelation>();
	HashMap<Integer, UnionRelation> map_id2filteredUnionRelations = new HashMap<Integer, UnionRelation>();

	public FreebaseRelation(String file) throws IOException {
		init(file);
		filter_takeTopK();

	}

	public void init(String file) throws IOException {
		//		List<String[]> all = (new DelimitedReader(file)).readAll();
		//		getSingleName2UnionName(all);
		D.p("Merge Relations Done");

		{
			DelimitedReader dr = new DelimitedReader(file);
			//int unionRelId = 0;
			List<String[]> bigb;
			while ((bigb = dr.readBlock(new int[] { 0 })) != null) {
				/**One bigb is about one Nellrelation,
				 * one small block is about one rellrelation and one singlefreebaserelation*/
				List<List<String[]>> blocks = StringTable.toblock(bigb, 1);
				List<String> relationnamelist = new ArrayList<String>();
				{
					HashSet<String> temp = new HashSet<String>();
					for (List<String[]> b : blocks) {
						String singleRelationName = b.get(0)[1];
						temp.add(singleRelationName);
					}
					relationnamelist.addAll(temp);
				}
				HashMap<String, String> singlerelname2unionrelationname = mergeRelations(relationnamelist);
				HashMap<String, List<SingleRelation>> unionrelationname2singlerelations = new HashMap<String, List<SingleRelation>>();
				for (Entry<String, String> e : singlerelname2unionrelationname.entrySet()) {
					String unionRelationName = e.getValue();
					if (!unionrelationname2singlerelations.containsKey(unionRelationName)) {
						unionrelationname2singlerelations.put(unionRelationName, new ArrayList<SingleRelation>());
					}
				}
				/**small block: about the same nellrelation and fbrelation*/
				for (List<String[]> b : blocks) {
					SingleRelation sr = new SingleRelation(b);
					//String singlerelationname = sr.fbRelationName;
					String unionrelationname = singlerelname2unionrelationname.get(sr.fbRelationName);
					unionrelationname2singlerelations.get(unionrelationname).add(sr);
				}
				for (Entry<String, List<SingleRelation>> e : unionrelationname2singlerelations.entrySet()) {
					String unionrelationname = e.getKey();
					UnionRelation ur = new UnionRelation(listUnionRelation.size(), unionrelationname, e.getValue());
					listUnionRelation.add(ur);
				}

			}
		}
		for (UnionRelation ur : listUnionRelation) {
			//D.p(ur.toString());
		}
		//		for (Entry<String, String> e : singlerelname2unionrelationname.entrySet()) {
		//			D.p(e.getKey(), e.getValue());
		//		}
	}

	public void filter_takeTopK() throws IOException {
		HashMap<String, List<UnionRelation>> nellstr2urlist = new HashMap<String, List<UnionRelation>>();
		for (UnionRelation ur : listUnionRelation) {
			if (!nellstr2urlist.containsKey(ur.nellRelationName)) {
				nellstr2urlist.put(ur.nellRelationName, new ArrayList<UnionRelation>());
			}
			if (ur.instances.size() > 1) {
				nellstr2urlist.get(ur.nellRelationName).add(ur);
			}
		}
		for (Entry<String, List<UnionRelation>> e : nellstr2urlist.entrySet()) {
			List<UnionRelation> list = e.getValue();
			Collections.sort(list, new Comparator<UnionRelation>() {
				@Override
				public int compare(UnionRelation arg0, UnionRelation arg1) {
					// TODO Auto-generated method stub
					return arg1.instances.size() - arg0.instances.size();
				}
			});
			for (int i = 0; i < list.size(); i++) {
				UnionRelation ur = list.get(i);
				filteredUnionRelations.add(ur);
				map_id2filteredUnionRelations.put(ur.unionRelId, ur);
			}
		}

	}

	public UnionRelation getUnionRelationById(int urid) {
		return map_id2filteredUnionRelations.get(urid);
	}

	//	public void getSingleName2UnionName(List<String[]> all) throws IOException {
	//		List<String> relationnames = new ArrayList<String>();
	//		{
	//			HashSet<String> temp = new HashSet<String>();
	//			for (String[] l : all) {
	//				temp.add(l[1]);
	//			}
	//			relationnames.addAll(temp);
	//		}
	//		map_singlename2unionname = mergeRelations(relationnames);
	//	}
	//
	public void selfTest(String file) throws IOException {
		BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file), "utf-8"));
		for (UnionRelation ur : filteredUnionRelations) {
			bw.write("Relation Id\t" + ur.unionRelId + "\n");
			bw.write(ur.toString());
			bw.write("\n");
		}
		bw.close();
	}

	private static boolean canTheyMerge(String fbrel1, String fbrel2) {
		String[] joint1 = fbrel1.split("\\|");
		String[] joint2 = fbrel2.split("\\|");
		if (joint1.length != joint2.length) {
			return false;
		}
		/**for every sub prop, their last token must be the same, for example, below are the same
		 * /ice_hockey/hockey_player/current_team|/ice_hockey/hockey_roster_position/team|/ice_hockey/hockey_team/coach|
		 * /basketball/basketball_player/team|/basketball/basketball_roster_position/team|/basketball/basketball_team/head_coach|
		 * */
		for (int i = 0; i < joint1.length; i++) {
			List<String> tok1 = StringUtil.tokenize(joint1[i], new char[] { '/', '_', ' ' });
			List<String> tok2 = StringUtil.tokenize(joint2[i], new char[] { '/', '_', ' ' });
			String a = tok1.get(tok1.size() - 1);
			String b = tok2.get(tok2.size() - 1);
			if (!a.equals(b)) {
				return false;
			}
		}
		return true;
	}

	/**return the mapping of the relations*/
	public static HashMap<String, String> mergeRelations(List<String> relations) {
		HashMap<String, String> map = new HashMap<String, String>();
		for (int i = 0; i < relations.size(); i++) {
			String rel1 = relations.get(i);
			if (map.containsKey(rel1)) {
				continue;
			}
			HashSet<String> sames = new HashSet<String>();
			sames.add(rel1);
			for (int j = i + 1; j < relations.size(); j++) {
				String rel2 = relations.get(j);
				if (map.containsKey(rel2)) {
					continue;
				}
				if (canTheyMerge(rel1, rel2)) {
					sames.add(rel2);
				}
			}
			StringBuilder sb = new StringBuilder();
			for (String r : sames) {
				sb.append(r + "@");
			}
			String newrel = sb.toString();
			newrel = newrel.substring(0, newrel.length() - 1);
			for (String r : sames) {
				map.put(r, newrel);
				//D.p(r, newrel);
			}
		}
		return map;
	}

	//
	//	public static void test() throws IOException {
	//		{
	//			List<String[]> all = (new DelimitedReader(Main.file_seedbfspath_show_group)).readAll();
	//			DelimitedWriter dw = new DelimitedWriter(Main.file_seedbfspath_merge);
	//			List<List<String[]>> blocks = StringTable.toblock(all, 0);
	//			for (List<String[]> b : blocks) {
	//				HashSet<Integer> forget = new HashSet<Integer>();
	//				for (int i = 0; i < b.size(); i++) {
	//					if (forget.contains(i))
	//						continue;
	//					String[] l = b.get(i);
	//					String fbr1 = b.get(i)[2];
	//					StringBuilder sb = new StringBuilder();
	//					int cnt = Integer.parseInt(l[3]);
	//					sb.append(fbr1);
	//					for (int j = i + 1; j < b.size(); j++) {
	//						if (forget.contains(j))
	//							continue;
	//						String fbr2 = b.get(j)[2];
	//						if (canTheyMerge(fbr1, fbr2)) {
	//							sb.append("&").append(fbr2);
	//							cnt += Integer.parseInt(b.get(j)[3]);
	//							forget.add(j);
	//						}
	//					}
	//					String newrel = sb.toString();
	//					if (!newrel.equals(fbr1)) {
	//						dw.write(l[0], sb.toString(), cnt);
	//					}
	//				}
	//			}
	//			dw.close();
	//		}
	//
	//	}

	public static void main(String[] args) throws IOException {
		// TODO Auto-generated method stub
		//		boolean x = canTheyMerge(
		//				"/ice_hockey/hockey_player/current_team|/ice_hockey/hockey_roster_position/team|/ice_hockey/hockey_team/coach|",
		//				"/basketball/basketball_player/team|/basketball/basketball_roster_position/team|/basketball/basketball_team/head_coach|");
		//		System.out.println(x);
		//		test();
	}

}
