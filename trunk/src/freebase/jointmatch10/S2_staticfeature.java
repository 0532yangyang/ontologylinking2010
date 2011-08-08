package freebase.jointmatch10;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;

import percept.util.delimited.Sort;

import javatools.administrative.D;
import javatools.datatypes.HashCount;
import javatools.filehandlers.DelimitedReader;
import javatools.filehandlers.DelimitedWriter;
import javatools.mydb.StringTable;
import javatools.parsers.PlingStemmer;
import javatools.string.StringUtil;
import javatools.webapi.BingApi;
import javatools.webapi.FBSearchEngine;
import javatools.webapi.LuceneSearch;

class FeatureValue {
	String fname;
	double value;

	FeatureValue(String fname, double value) {
		this.fname = fname;
		this.value = value;
	}

	public String toString() {
		return this.fname + ":" + this.value;

	}
}

public class S2_staticfeature {
	static HashMap<Integer, String> map_varid_name = new HashMap<Integer, String>();
	static HashMap<Integer, List<FeatureValue>> entity_features = new HashMap<Integer, List<FeatureValue>>();
	static HashMap<Integer, List<FeatureValue>> type_features = new HashMap<Integer, List<FeatureValue>>();
	static HashMap<Integer, List<FeatureValue>> relation_features = new HashMap<Integer, List<FeatureValue>>();

	/**For entity features, 1. rank; 2. name
	 * @throws IOException */
	public static void entityfeature(String input, String output) throws IOException {
		{
			/**load id*/
			DelimitedReader dr = new DelimitedReader(input);
			String[] l;
			while ((l = dr.read()) != null) {
				int varid = Integer.parseInt(l[0]);
				String varname = l[1] + "::" + l[2];
				map_varid_name.put(varid, varname);
				entity_features.put(varid, new ArrayList<FeatureValue>());
				//				List<String> t1 = StringUtil.tokenize(nrname.replace("_inverse", ""), new char[] { '_' });
				//				List<String> t2 = StringUtil.tokenize(fbname, new char[] { ' ', '_', '/', '|' });
				//				int share = StringUtil.numOfShareWords(t1, t2, new boolean[] { true, true, true });
			}
		}
		HashMap<String, String[]> mid2names = new HashMap<String, String[]>();
		{
			/**Load names for mid*/
			DelimitedReader dr = new DelimitedReader(Main.file_mid_wid_type_name_alias);
			String[] l;
			while ((l = dr.read()) != null) {
				String[] names = l[4].split("::");
				mid2names.put(l[0], names);
			}
			dr.close();
		}
		{
			/**name*/
			for (Entry<Integer, List<FeatureValue>> e : entity_features.entrySet()) {
				int varid = e.getKey();
				List<FeatureValue> values = e.getValue();
				String[] name_ab = map_varid_name.get(varid).split("::");
				String nellname = name_ab[0];
				String mid = name_ab[1];
				String[] fbnames = mid2names.get(mid);
				if (fbnames == null || fbnames.length == 0) {
					values.add(new FeatureValue("ENTNAME", 0));
				} else {
					double maxvalue = 0;
					for (String fn : fbnames) {
						//						List<String> t1 = StringUtil.tokenize(fn, new char[] { '_', ' ' });
						//						List<String> t2 = StringUtil.tokenize(nellname, new char[] { '_', ' ' });
						int[] par_return = new int[2];
						int sharewords = StringUtil.numOfShareWords(fn, nellname, par_return);
						double value = sharewords * 1.0 / Math.max(par_return[0], par_return[1]);
						if (value > maxvalue)
							maxvalue = value;
					}
					values.add(new FeatureValue("ENTNAME", maxvalue));
				}
				//D.p(name_ab[0],name_ab[1]);
			}
		}
		{
			/**rank*/
			DelimitedReader dr = new DelimitedReader(input);
			List<String[]> b;
			while ((b = dr.readBlock(1)) != null) {
				double v = 0;
				for (String[] b0 : b) {
					int varid = Integer.parseInt(b0[0]);
					List<FeatureValue> values = entity_features.get(varid);
					values.add(new FeatureValue("RANK", v));
					v = v - 1;
				}
			}
		}
		{
			/**print*/
			List<String[]> table = new ArrayList<String[]>();
			DelimitedWriter dw = new DelimitedWriter(output);

			for (Entry<Integer, List<FeatureValue>> e : entity_features.entrySet()) {
				StringBuilder sb = new StringBuilder();
				for (FeatureValue fv : e.getValue()) {
					sb.append(fv.toString() + " ");
				}
				int varid = e.getKey();
				table.add(new String[] { varid + "", map_varid_name.get(varid), sb.toString() });
				//dw.write(varid, map_varid_name.get(varid), sb.toString());
			}
			StringTable.sortByIntColumn(table, new int[] { 0 });
			for (String[] t : table) {
				dw.write(t);
			}
			dw.close();
		}
	}

	/**name*/
	public static void typeFeatures(String input, String output) throws IOException {
		{
			/**load id*/
			DelimitedReader dr = new DelimitedReader(input);
			String[] l;
			while ((l = dr.read()) != null) {
				int varid = Integer.parseInt(l[0]);
				String varname = l[1] + "::" + l[2];
				map_varid_name.put(varid, varname);
				type_features.put(varid, new ArrayList<FeatureValue>());
			}
		}
		{
			/**name*/
			for (Entry<Integer, List<FeatureValue>> e : type_features.entrySet()) {
				int varid = e.getKey();
				List<FeatureValue> values = e.getValue();
				String[] name_ab = map_varid_name.get(varid).split("::");
				String nelltypename = name_ab[0];
				String fbtypename = name_ab[1];

				List<String> t1 = StringUtil.tokenize(nelltypename, new char[] { ' ' });
				List<String> t2 = StringUtil.tokenize(fbtypename, new char[] { '_', ' ', '/' });
				Collections.sort(t1);
				Collections.sort(t2);
				//				if(nelltypename.equals("actor")){
				//					D.p(nelltypename);
				//				}
				int sharewords = StringUtil.numOfShareWords(t1, t2, new boolean[]{true,true,true});
				double value = sharewords * 1.0 / t1.size();
				values.add(new FeatureValue("TYPENAME", value));
			}
		}
		{
			/**print*/
			List<String[]> table = new ArrayList<String[]>();
			DelimitedWriter dw = new DelimitedWriter(output);

			for (Entry<Integer, List<FeatureValue>> e : type_features.entrySet()) {
				StringBuilder sb = new StringBuilder();
				for (FeatureValue fv : e.getValue()) {
					sb.append(fv.toString() + " ");
				}
				int varid = e.getKey();
				table.add(new String[] { varid + "", map_varid_name.get(varid), sb.toString() });
				//dw.write(varid, map_varid_name.get(varid), sb.toString());
			}
			StringTable.sortByIntColumn(table, new int[] { 0 });
			for (String[] t : table) {
				dw.write(t);
			}
			dw.close();
		}
	}

	/**name, length of the relation; num of middle entities*/
	public static void relationFeatures(String input, String output) throws IOException {
		{
			/**load id*/
			DelimitedReader dr = new DelimitedReader(input);
			String[] l;
			while ((l = dr.read()) != null) {
				int varid = Integer.parseInt(l[0]);
				String varname = l[1] + "::" + l[2];
				map_varid_name.put(varid, varname);
				relation_features.put(varid, new ArrayList<FeatureValue>());
			}
		}
		{
			/**name*/
			for (Entry<Integer, List<FeatureValue>> e : relation_features.entrySet()) {
				int varid = e.getKey();
				List<FeatureValue> values = e.getValue();
				String[] name_ab = map_varid_name.get(varid).split("::");
				String nellrelname = name_ab[0];
				String fbrelname = name_ab[1];

				List<String> t1 = StringUtil.tokenize(nellrelname, new char[] { ' ' });
				List<String> t2 = StringUtil.tokenize(fbrelname, new char[] { '_', ' ', '/' });
//				Collections.sort(t1);
//				Collections.sort(t2);
				//				if(nelltypename.equals("actor")){
				//					D.p(nelltypename);
				//				}
				int sharewords = StringUtil.numOfShareWords(t1, t2,new boolean[]{true,true,true});
				double value = sharewords * 1.0 / t1.size();
				values.add(new FeatureValue("RELNAME", value));
			}
		}
		{
			/**length of the relation*/
			for (Entry<Integer, List<FeatureValue>> e : relation_features.entrySet()) {
				int varid = e.getKey();
				List<FeatureValue> values = e.getValue();
				String[] name_ab = map_varid_name.get(varid).split("::");
				String fbrelname = name_ab[1];
				String[] xy = fbrelname.split("\\|\\|");

				values.add(new FeatureValue("LEN", xy.length));
			}
		}
		{
			/**number of middle element (i.e. A,C,B, num of C), avoid extreme join, take log_10*/
			HashMap<String, Integer> fbrel_maxmiddle = new HashMap<String, Integer>();
			DelimitedReader dr = new DelimitedReader(Main.file_fbsearchmatch_len2);
			List<String[]> all = new ArrayList<String[]>();
			String[] l;
			while ((l = dr.read()) != null) {
				all.add(new String[] { l[1], l[2], l[4], l[3] });
			}
			StringTable.sortUniq(all);
			List<String[]> table = StringTable.squeeze(all, new int[] { 0, 1, 2 });
			for (String[] t : table) {
				int num = Integer.parseInt(t[0]);
				String fbrel = t[1];
				if (!fbrel_maxmiddle.containsKey(fbrel)) {
					fbrel_maxmiddle.put(fbrel, num);
				}
			}
			for (Entry<Integer, List<FeatureValue>> e : relation_features.entrySet()) {
				int varid = e.getKey();
				List<FeatureValue> values = e.getValue();
				String[] name_ab = map_varid_name.get(varid).split("::");
				String fbrelname = name_ab[1];
				if (fbrel_maxmiddle.containsKey(fbrelname)) {
					values.add(new FeatureValue("MID", Math.log10(fbrel_maxmiddle.get(fbrelname))));
				} else {
					values.add(new FeatureValue("MID", 0));
				}
			}
		}
		{
			/**explode*/
			HashMap<String, Double> exploderelation = new HashMap<String, Double>();
			{
				DelimitedReader dr = new DelimitedReader(Main.file_fbsql2instances + ".explode");
				String[] l;
				while ((l = dr.read()) != null) {
					int x = Integer.parseInt(l[2]);
					int y = Integer.parseInt(l[3]);
					String rel = l[0];
					if (exploderelation.containsKey(rel)) {
						double old = exploderelation.get(rel);
						double newv = Math.log10(x*y);
						if(newv>old){
							exploderelation.put(rel, Math.log10(x*y));
						}
					} else {
						exploderelation.put(rel, Math.log10(x*y));
					}
				}
				dr.close();
			}
			for (Entry<Integer, List<FeatureValue>> e : relation_features.entrySet()) {
				int varid = e.getKey();
				List<FeatureValue> values = e.getValue();
				String[] name_ab = map_varid_name.get(varid).split("::");
				String fbrelname = name_ab[1];
				if (exploderelation.containsKey(fbrelname)) {
					double val = exploderelation.get(fbrelname);
					values.add(new FeatureValue("EXP", val));
				}
			}
		}
		{
			/**print*/
			List<String[]> table = new ArrayList<String[]>();
			DelimitedWriter dw = new DelimitedWriter(output);
			for (Entry<Integer, List<FeatureValue>> e : relation_features.entrySet()) {
				StringBuilder sb = new StringBuilder();
				for (FeatureValue fv : e.getValue()) {
					sb.append(fv.toString() + " ");
				}
				int varid = e.getKey();
				table.add(new String[] { varid + "", map_varid_name.get(varid), sb.toString() });
				//dw.write(varid, map_varid_name.get(varid), sb.toString());
			}
			StringTable.sortByIntColumn(table, new int[] { 0 });
			for (String[] t : table) {
				dw.write(t);
			}
			dw.close();
		}
	}

	public static void main(String[] args) throws IOException {
		entityfeature(Main.file_entitymatch_candidate, Main.file_entitymatch_staticfeature);
		typeFeatures(Main.file_typematch_candidate, Main.file_typematch_staticfeature);
		relationFeatures(Main.file_relationmatch_candidate, Main.file_relationmatch_staticfeature);
	}
}
