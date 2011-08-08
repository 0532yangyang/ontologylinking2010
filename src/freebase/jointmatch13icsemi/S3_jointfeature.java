package freebase.jointmatch13icsemi;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Map.Entry;
import java.util.logging.FileHandler;
import java.util.logging.Logger;

import javatools.administrative.D;
import javatools.filehandlers.DelimitedReader;
import javatools.filehandlers.DelimitedWriter;
import javatools.mydb.StringTable;

class ClauseWeight {
	double ENTNAME = 1.0;
	double RANK = 1.0;
	double TYPENAME = 1.0;
	double RELNAME = 1.0;
	double LEN = -1.0;
	double MID = 0;
	double EXP = -1.0;
}

public class S3_jointfeature {

	static HashMap<Integer, String> map_varid_name = new HashMap<Integer, String>();
	static HashMap<String, Integer> map_name_varid = new HashMap<String, Integer>();
	static HashMap<String, String> map_varname_type = new HashMap<String, String>();//type can be relation; type & entity;

	static void load() throws IOException {
		dwjointname = new DelimitedWriter(Main.file_jointclause + ".name");
		loadOne(Main.file_relationmatch_candidate, "relation");
		loadOne(Main.file_typematch_candidate, "type");
		loadOne(Main.file_entitymatch_candidate, "entity");
		dwjointname.close();
	}

	private static void loadOne(String input, String vartype) throws IOException {
		{
			/**load id*/
			DelimitedReader dr = new DelimitedReader(input);
			String[] l;
			while ((l = dr.read()) != null) {
				int varid = Integer.parseInt(l[0]);
				String varname = l[1] + "::" + l[2];
				map_varid_name.put(varid, varname);
				map_name_varid.put(varname, varid);
				map_varname_type.put(varname, vartype);
				dwjointname.write(varid, varname);

			}
			dr.close();
		}
	}

	static List<String[]> relationInstanceEvi = new ArrayList<String[]>();
	static List<String[]> typeInstanceEvi = new ArrayList<String[]>();

	static void collectFeatureRelation(String input, double weight) throws IOException {
		{
			/**load id*/
			DelimitedReader dr = new DelimitedReader(input);
			String[] l;
			HashSet<String> appear = new HashSet<String>();
			//musicianPlaysInstrument::Yo Yo Ma::cello	/music/artist/track_contributions|/music/track_contribution/role	/m/011zf2	/m/01xqw	j1	/m/0dpsh0s
			while ((l = dr.read()) != null) {
				if (l[0].contains("Brian Urlacher")) {
					//D.p(l[0]);
				}
				String[] abc = l[0].split("::");
				String nellrel = abc[0];
				String arg1 = abc[1];
				String arg2 = abc[2];
				String fbrel = l[1];
				String arg1mapentity = l[2];
				String arg2mapentity = l[3];
				String arg1var = arg1 + "::" + arg1mapentity;
				String arg2var = arg2 + "::" + arg2mapentity;
				String relvarstr = nellrel + "::" + fbrel;

				try {
					//some relations has already been given up because it only hit one example in the seeds
					int arg1varId = map_name_varid.get(arg1var);
					int arg2varId = map_name_varid.get(arg2var);
					int relvarid = map_name_varid.get(relvarstr);
					String key = arg1varId + "\t" + arg2varId + "\t" + relvarid;
					if (!appear.contains(key)) {
						//						dwjoint.write("relation_instance", weight, -1 * arg1varId, -1 * arg2varId, relvarid, 0,
						//								arg1var, arg2var, relvarstr);
						relationInstanceEvi.add(new String[] { arg1var, arg2var, relvarstr });
						//dwjoint.write("relation_instance_inv", weight, arg1varId, -1 * relvarid, 0, arg1var, relvarstr);
						//dwjoint.write("relation_instance_inv", weight, arg2varId, -1 * relvarid, 0, arg2var, relvarstr);
						appear.add(key);
					}
				} catch (Exception e) {

				}
			}
			dr.close();
		}
	}

	static void instanceFeatureType(double weight) throws IOException {
		DelimitedReader dr = new DelimitedReader(Main.file_fbsearch2);
		String[] l;
		HashSet<String> appear = new HashSet<String>();
		while ((l = dr.read()) != null) {
			String entityname = l[0];
			Set<String> entityclass = Main.no.entity2class.get(entityname);
			//			if (entityclass.contains("writer")) {
			//				D.p(entityname, l[3]);
			//			}
			entityclass.remove("NEG_NA");
			for (String nelltype : entityclass) {
				String fbtype = l[3];
				String argstr = l[0] + "::" + l[1];
				String typestr = nelltype + "::" + fbtype;
				try {
					int argvarid = map_name_varid.get(argstr);
					int typevarid = map_name_varid.get(typestr);
					String key = argvarid + "\t" + typevarid;
					if (!appear.contains(key)) {
						dwjoint.write("type_instance", weight, -1 * argvarid, typevarid, 0, argstr, typestr);
						typeInstanceEvi.add(new String[] { argstr, typestr });
						//dwjoint.write("type_instance_inv", weight, argvarid, -1 * typevarid, 0, argstr, typestr);
					}
				} catch (Exception e) {

				}
			}
		}
	}

	static void instanceTypeInverse(double weight) throws IOException {
		List<String[]> table = new ArrayList<String[]>();
		for (String[] l : typeInstanceEvi) {
			String typestr = l[1];
			String argstr = l[0];
			String nellname = argstr.split("::")[0];
			table.add(new String[] { nellname + "\t" + typestr, argstr, typestr });
		}
		StringTable.sortUniq(table);
		StringTable.sortByColumn(table, new int[] { 0 });
		List<List<String[]>> blocks = StringTable.toblock(table, 0);
		for (List<String[]> b : blocks) {
			List<String> towrite = new ArrayList<String>();
			List<String> comment = new ArrayList<String>();
			towrite.add("type_instance_inv");
			towrite.add(weight + "");
			String typestr = b.get(0)[2];
			int typevarid = map_name_varid.get(typestr);
			towrite.add(-1 * typevarid + "");//type
			comment.add(typestr);
			for (String[] b0 : b) {
				String argstr = b0[1];
				int entityvarid = map_name_varid.get(argstr);
				towrite.add(entityvarid + "");
				comment.add(argstr);
			}
			towrite.add("0");
			towrite.addAll(comment);
			String[] l = new String[towrite.size()];
			for (int i = 0; i < towrite.size(); i++)
				l[i] = towrite.get(i);
			dwjoint.write(l);
		}
	}

	static void writeInstanceRelation(double weight) throws IOException {
		List<String[]> table = new ArrayList<String[]>();
		for (String[] l : relationInstanceEvi) {
			String relstr = l[2];
			String argstr1 = l[0];
			String argstr2 = l[1];
			String nellrelname = l[2].split("::")[0];
			table.add(new String[] { nellrelname + "\t" + argstr1 + "\t" + argstr2, argstr1, argstr2, relstr });
		}
		StringTable.sortUniq(table);
		StringTable.sortByColumn(table, new int[] { 0 });
		List<List<String[]>> blocks = StringTable.toblock(table, 0);
		for (List<String[]> b : blocks) {
			List<String> towrite = new ArrayList<String>();
			List<String> comment = new ArrayList<String>();
			towrite.add("relation_instance");
			towrite.add(weight + "");
			String arg1str = b.get(0)[1];
			String arg2str = b.get(0)[2];
			int arg1varid = map_name_varid.get(arg1str);
			int arg2varid = map_name_varid.get(arg2str);
			towrite.add(-1*arg1varid+"");
			towrite.add(-1*arg2varid+"");
			comment.add(arg1str);
			comment.add(arg2str);
			
			
			
			for (String[] b0 : b) {
				String relstr = b0[3];
				int relvarid = map_name_varid.get(relstr);
				towrite.add(relvarid + "");
				comment.add(relstr);
			}
			towrite.add("0");
			towrite.addAll(comment);
			String[] l = new String[towrite.size()];
			for (int i = 0; i < towrite.size(); i++)
				l[i] = towrite.get(i);
			dwjoint.write(l);
		}
	}

	static void writeInstanceRelationInverse(double weight, int whicharg) throws IOException {
		List<String[]> table = new ArrayList<String[]>();
		for (String[] l : relationInstanceEvi) {
			String relstr = l[2];
			String argstr = l[whicharg];
			String nellname = argstr.split("::")[0];
			table.add(new String[] { nellname + "\t" + relstr, argstr, relstr });
		}
		StringTable.sortUniq(table);
		StringTable.sortByColumn(table, new int[] { 0 });
		List<List<String[]>> blocks = StringTable.toblock(table, 0);
		for (List<String[]> b : blocks) {
			List<String> towrite = new ArrayList<String>();
			List<String> comment = new ArrayList<String>();
			towrite.add("relation_instance_inv");
			towrite.add(weight + "");
			String relstr = b.get(0)[2];
			int typevarid = map_name_varid.get(relstr);
			towrite.add(-1 * typevarid + "");//type
			comment.add(relstr);
			for (String[] b0 : b) {
				String argstr = b0[1];
				int entityvarid = map_name_varid.get(argstr);
				towrite.add(entityvarid + "");
				comment.add(argstr);
			}
			towrite.add("0");
			towrite.addAll(comment);
			String[] l = new String[towrite.size()];
			for (int i = 0; i < towrite.size(); i++)
				l[i] = towrite.get(i);
			dwjoint.write(l);
		}
	}

	static void typeConstrain() throws IOException {

	}

	static void entitymutualexclusive(double weight) throws IOException {
		List<String[]> table = new ArrayList<String[]>();
		for (Entry<Integer, String> e : map_varid_name.entrySet()) {
			int id = e.getKey();
			String varname = e.getValue();
			if (!map_varname_type.get(varname).equals("entity")) {
				continue;
			}
			String[] ab = e.getValue().split("::");
			table.add(new String[] { id + "", ab[0], ab[1] });
		}
		{
			StringTable.sortByColumn(table, new int[] { 1 });
			List<List<String[]>> blocks = StringTable.toblock(table, 1);
			for (List<String[]> b : blocks) {
				for (int i = 0; i < b.size(); i++) {
					for (int j = i + 1; j < b.size(); j++) {
						dwjoint.write("mutual", weight, (-1 * Integer.parseInt(b.get(i)[0])) + "",
								(-1 * Integer.parseInt(b.get(j)[0])) + "", 0, b.get(i)[1] + "::" + b.get(i)[2],
								b.get(j)[1] + "::" + b.get(j)[2]);
					}
				}
			}
		}
	}

	static void relationmutex(double weight) throws IOException {
		List<String[]> temp = new ArrayList<String[]>();
		for (String[] a : relationInstanceEvi) {
			temp.add(new String[] { a[0] + "\t" + a[1] + "\t" + a[2].split("::")[0], a[2] });
		}
		StringTable.sortByColumn(temp, new int[] { 0 });
		List<List<String[]>> blocks = StringTable.toblock(temp, 0);
		for (List<String[]> b : blocks) {
			String var12[] = b.get(0)[0].split("\t");
			int varid1 = map_name_varid.get(var12[0]);
			int varid2 = map_name_varid.get(var12[1]);
			for (int i = 0; i < b.size(); i++) {
				String matchr1 = b.get(i)[1];
				int matchr1id = map_name_varid.get(matchr1);
				for (int j = i + 1; j < b.size(); j++) {
					String matchr2 = b.get(j)[1];
					int matchr2id = map_name_varid.get(matchr2);
					dwjoint.write("relmutex", weight, -1 * varid1 + "", -1 * varid2 + "", -1 * matchr1id + "", -1
							* matchr2id, 0, var12[0], var12[1], matchr1, matchr2);
				}
			}
		}
	}

	static void knownNegative(String input, double weight) throws IOException {
		{
			/**load id*/
			DelimitedReader dr = new DelimitedReader(input);
			String[] l;
			HashSet<String> appear = new HashSet<String>();
			//musicianPlaysInstrument::Yo Yo Ma::cello	/music/artist/track_contributions|/music/track_contribution/role	/m/011zf2	/m/01xqw	j1	/m/0dpsh0s
			while ((l = dr.read()) != null) {
				if(l[0].equals("stateHasCapital::Chicago::Illinois")){
					D.p(l[0]);
				}
				String[] abc = l[0].split("::");
				String nellrel = abc[0];
				String arg1 = abc[1];
				String arg2 = abc[2];
				String fbrel = l[1];
				String arg1mapentity = l[2];
				String arg2mapentity = l[3];
				String arg1var = arg1 + "::" + arg1mapentity;
				String arg2var = arg2 + "::" + arg2mapentity;
				String relvarstr = nellrel + "::" + fbrel;

				try {
					//some relations has already been given up because it only hit one example in the seeds
					int arg1varId = map_name_varid.get(arg1var);
					int arg2varId = map_name_varid.get(arg2var);
					int relvarid = map_name_varid.get(relvarstr);
					String key = arg1varId + "\t" + arg2varId + "\t" + relvarid;
					if (!appear.contains(key)) {
						dwjoint.write("knownneg", weight, -1 * arg1varId, -1 * arg2varId, relvarid, 0, arg1var, arg2var,
								relvarstr);
						appear.add(key);
					}
				} catch (Exception e) {

				}
			}
			dr.close();
		}
	}

	static void staticfeature() throws IOException {
		HashMap<String, Double> feature_weight = new HashMap<String, Double>();
		staticfeature_setweight(feature_weight);
		staticfeatureOne(Main.file_relationmatch_staticfeature, feature_weight, "relation");
		staticfeatureOne(Main.file_entitymatch_staticfeature, feature_weight, "entity");
		staticfeatureOne(Main.file_typematch_staticfeature, feature_weight, "type");
	}

	static void staticfeature_setweight(HashMap<String, Double> feature_weight) throws IOException {
		feature_weight.put("ENTNAME", 1.0);
		feature_weight.put("RANK", 1.0);
		feature_weight.put("TYPENAME", 1.0);
		feature_weight.put("RELNAME", 1.0);
		feature_weight.put("LEN", -1.0);
		feature_weight.put("MID", 0.0);//use explode to replace this
		feature_weight.put("EXP", -1.0);
	}

	static void staticfeatureOne(String input, HashMap<String, Double> feature_weight, String appendName)
			throws IOException {
		DelimitedReader dr = new DelimitedReader(input);
		String[] l;
		while ((l = dr.read()) != null) {
			int varid = Integer.parseInt(l[0]);

			String varname = l[1];
			String[] fv = l[2].split(" |:");
			double sum = 0;
			for (int i = 0; i < fv.length; i += 2) {
				String fname = fv[i];
				double val = Double.parseDouble(fv[i + 1]);
				double weight = feature_weight.get(fname);
				sum += val * weight;
			}
			dwjoint.write("static_" + appendName, sum, varid, 0, varname);
		}
	}

	public static void convert2wcnf() throws IOException {
		DelimitedWriter dw = new DelimitedWriter(Main.file_jointclause);
		List<String[]> all = (new DelimitedReader(Main.file_jointclause + ".1")).readAll();
		int num_variables = 0, num_clauses = 0;
		double lowest = 0;
		for (String[] a : all) {
			num_clauses++;
			if (a[0].contains("_inv")) {
				continue;
			}
			double w = Double.parseDouble(a[1]);
			lowest = Math.min(w, lowest);
			for (int k = 2; k < a.length; k++) {
				if (a[k].equals("0")) {
					break;
				} else {
					int x = Math.abs(Integer.parseInt(a[k]));
					if (x > num_variables)
						num_variables = x;
				}
			}
		}
		dw.write("p wcnf " + num_variables + " " + num_clauses);
		for (String[] a : all) {
			StringBuilder sb = new StringBuilder();
			int w = (int) Math.round((Double.parseDouble(a[1]) - lowest) * 10);
			sb.append(w + " ");
			for (int k = 2; k < a.length; k++) {
				sb.append(a[k] + " ");
				if (a[k].equals("0"))
					break;
			}
			dw.write(sb.toString());
		}
		dw.close();
	}

	static DelimitedWriter dwjoint;
	static DelimitedWriter dwjointname;

	public static void main(String[] args) throws IOException {
		load();
		{
			dwjoint = new DelimitedWriter(Main.file_jointclause + ".1");
			collectFeatureRelation(Main.file_fbsearchmatch_len1, 1);
			collectFeatureRelation(Main.file_fbsearchmatch_len2, 1);
			instanceFeatureType(1);
			instanceTypeInverse(1);
			
			writeInstanceRelation(1);
			writeInstanceRelationInverse(1, 0);
			writeInstanceRelationInverse(1, 1);
			
			entitymutualexclusive(100);
			//relationmutex(1);
			knownNegative(Main.file_knownnegative + ".match.len1", -100);
			knownNegative(Main.file_knownnegative + ".match.len2", -100);
			staticfeature();
			dwjoint.close();
			dwjointname.close();
		}
		convert2wcnf();

	}
}
