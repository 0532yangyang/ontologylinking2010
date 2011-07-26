package freebase.preprocess3;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.IndexWriterConfig.OpenMode;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Version;

import multir.util.delimited.Sort;

import javatools.administrative.D;
import javatools.filehandlers.DelimitedReader;
import javatools.filehandlers.DelimitedWriter;
import javatools.webapi.LuceneSearch;

public class ParseFreebaseDump2Visible {
	public static void split() throws IOException {
		DelimitedReader dr = new DelimitedReader(Main.file_fbdump);
		DelimitedWriter dw1 = new DelimitedWriter(Main.file_fbdump_1_tuple);
		DelimitedWriter dw2 = new DelimitedWriter(Main.file_fbdump_2_len4);
		DelimitedWriter dw3 = new DelimitedWriter(Main.file_fbdump_3_str);
		String[] l;
		int count = 0;
		while ((l = dr.read()) != null) {
			count++;
			//if(count>100000)break;
			if (l.length != 4) {
				continue;
			}
			if (l[3].length() > 0 && l[2].length() > 0) {
				dw2.write(l);
			} else if (l[2].startsWith("/m/")) {
				dw1.write(l);
			} else {
				String str = l[2];
				if (str.length() == 0) {
					str = l[3];
				}
				dw3.write(l[0], l[1], str);
			}
		}
		dr.close();
		dw1.close();
		dw2.close();
		dw3.close();
	}

	public static void removeHiddenFromFbdump_3_str() throws IOException {
		DelimitedReader dr = new DelimitedReader(Main.file_fbdump_3_str);
		DelimitedWriter dw = new DelimitedWriter(Main.file_fbdump_3_str + ".nohidden");
		String[] hiddenrel = new String[] { "/type/object/type", "/common/topic/notable_types",
				"/common/topic/notable_for" };
		Set<String> set_hiddenrel = new HashSet<String>();
		for (String a : hiddenrel)
			set_hiddenrel.add(a);
		String[] l;
		int count = 0;
		while ((l = dr.read()) != null) {
			count++;
			//if(count>100000)break;
			if (!set_hiddenrel.contains(l[1])) {
				dw.write(l);
			}
		}
		dr.close();
		dw.close();
	}

	static void sort() throws IOException {
		Sort.sort(Main.file_fbdump_1_tuple, Main.file_fbdump_1_tuple + ".sb2", Main.dir + "/temp1",
				new Comparator<String[]>() {
					@Override
					public int compare(String[] arg0, String[] arg1) {
						// TODO Auto-generated method stub
						return arg0[2].compareTo(arg1[2]);
					}
				});

		Sort.sort(Main.file_fbdump_1_tuple, Main.file_fbdump_1_tuple + ".sb1", Main.dir + "/temp2",
				new Comparator<String[]>() {
					@Override
					public int compare(String[] arg0, String[] arg1) {
						// TODO Auto-generated method stub
						return arg0[0].compareTo(arg1[0]);
					}
				});

		Sort.sort(Main.file_fbdump_3_str + ".nohidden", Main.file_fbdump_3_str + ".nohidden.sb1", Main.dir + "/temp3",
				new Comparator<String[]>() {
					@Override
					public int compare(String[] arg0, String[] arg1) {
						// TODO Auto-generated method stub
						return arg0[0].compareTo(arg1[0]);
					}
				});
	}

	static void getName() throws IOException {
		DelimitedReader dr = new DelimitedReader(Main.file_fbdump_2_len4);
		DelimitedWriter dw = new DelimitedWriter(Main.file_fbenglishname);
		String[] l;
		int count = 0;
		while ((l = dr.read()) != null) {
			//			count++;
			//			if (count > 10000)
			//				break;
			if (l[1].equals("/type/object/name") && l[2].equals("/lang/en")) {
				dw.write(l[0], l[3]);
			}
		}
		dw.close();
		dr.close();
		Sort.sort(Main.file_fbenglishname, Main.file_fbenglishname_sbmid, Main.dir + "/temp3",
				new Comparator<String[]>() {
					@Override
					public int compare(String[] o1, String[] o2) {
						// TODO Auto-generated method stub
						return o1[0].compareTo(o2[0]);
					}
				});
	}

	static void getVisible2() throws IOException {
		String file_tuple_sb2 = Main.file_visible + ".3";
		String file_tuple_sb1 = Main.file_fbdump_1_tuple_sb1;
		String file_argRelName = Main.file_fbdump_3_str_noh_sb1;
		String file_name = Main.file_fbenglishname_sbmid;
		String output = Main.file_visible + ".4";
		String output_debug = Main.file_visible + ".debug";
		DelimitedWriter dw = new DelimitedWriter(output);
		DelimitedWriter dw_debug = new DelimitedWriter(output_debug);
		DelimitedReader dr = new DelimitedReader(file_tuple_sb2);
		DelimitedReader drname = new DelimitedReader(file_name);
		DelimitedReader drargRelName = new DelimitedReader(file_argRelName);
		DelimitedReader drtuplesb1 = new DelimitedReader(file_tuple_sb1);
		List<String[]> block;
		List<String[]> bname = drname.readBlock(0);
		List<String[]> bargRelName = drargRelName.readBlock(0);
		List<String[]> btuplesb1 = drtuplesb1.readBlock(0);
		int count = 0;
		while ((block = dr.readBlock(2)) != null) {
			if (count++ % 100000 == 0) {
				D.p("count", count);
				//break;
			}
			String mid2 = block.get(0)[2];
			boolean writeSomething = false;
			while (bname != null && bname.size() > 0 && bname.get(0)[0].compareTo(mid2) < 0) {
				bname = drname.readBlock(0);
			}
			while (bargRelName != null && bargRelName.size() > 0 && bargRelName.get(0)[0].compareTo(mid2) < 0) {
				bargRelName = drargRelName.readBlocklimited(0, 100);
			}
			while (btuplesb1 != null && btuplesb1.size() > 0 && btuplesb1.get(0)[0].compareTo(mid2) < 0) {
				btuplesb1 = drtuplesb1.readBlocklimited(0, 100);
			}

			//mid2 having a name; stop extending 
			if (bname != null && bname.size() > 0 && bname.get(0)[0].equals(mid2)) {
				//				String name = bname.get(0)[1];
				//				for (String[] b : block) {
				//					dw.write(b[0], "t0", b[1], b[2]);
				//					writeSomething = true;
				//				}
			} else {
				List<String[]> extend = new ArrayList<String[]>();
				if (bargRelName != null && bargRelName.size() > 0 && bargRelName.get(0)[0].equals(mid2)) {
					extend.addAll(bargRelName);
				}
				if (btuplesb1 != null && btuplesb1.size() > 0 && btuplesb1.get(0)[0].equals(mid2)) {
					extend.addAll(btuplesb1);
				}
				if (extend.size() > 90) {
					dw_debug.write(mid2);
					continue;
				}
				for (String[] b : block) {
					for (String[] e : extend) {
						String newrel = b[1] + "|" + e[1];
						dw.write(b[0], "t1", newrel, e[2], b[2]);
						writeSomething = true;
					}
				}
			}
			if (!writeSomething) {
				for (String[] b : block) {
					dw_debug.write(b);
				}
			}
		}
		dr.close();
		drname.close();
		drargRelName.close();
		drtuplesb1.close();
		dw.close();

	}

	static void getVisible1() throws IOException {
		String file_tuple_sb2 = Main.file_fbdump_1_tuple_sb2;
		String file_tuple_sb1 = Main.file_fbdump_1_tuple_sb1;
		String file_name = Main.file_fbenglishname_sbmid;
		String output = Main.file_visible + ".2";
		String output2 = Main.file_visible + ".3";
		{
			DelimitedWriter dw = new DelimitedWriter(output);
			DelimitedWriter dw2 = new DelimitedWriter(output2);
			DelimitedReader dr = new DelimitedReader(file_tuple_sb2);
			DelimitedReader drname = new DelimitedReader(file_name);
			String[] l;
			List<String[]> bname = drname.readBlock(0);
			int count = 0;
			while ((l = dr.read()) != null) {
				//			if (count++ > 100000)
				//				break;
				String mid2 = l[2];
				boolean writeSomething = false;
				while (bname != null && bname.size() > 0 && bname.get(0)[0].compareTo(mid2) < 0) {
					bname = drname.readBlock(0);
				}
				//mid2 having a name; stop extending 
				if (bname != null && bname.size() > 0 && bname.get(0)[0].equals(mid2)) {
					String name = bname.get(0)[1];
					dw.write(l[0], "t0", l[1], l[2]);
					writeSomething = true;
				} else {
					dw2.write(l);
				}
			}
			dr.close();
			dw2.close();
			drname.close();
			dw.close();
		}
	}

	static void getVisible_help_avoidhugeBlock(String file, int key) throws IOException {
		DelimitedReader dr = new DelimitedReader(file);
		DelimitedWriter dw = new DelimitedWriter(file + ".blow");
		String[] l;
		String last = "";
		int count = 0;
		while ((l = dr.read()) != null) {
			if (l[key].equals(last)) {
				count++;
			} else {
				if (count > 100) {
					dw.write(last, count);
					//D.p(last, count);
				}
				last = l[key];
				count = 1;
			}
		}
		dr.close();
		dw.close();
	}

	static void getVisible3() throws IOException {
		//read fbdump_3_str and visible.4 to get all visible_str
		DelimitedWriter dw = new DelimitedWriter(Main.file_visible_str);
		{
			DelimitedReader dr = new DelimitedReader(Main.file_fbdump_3_str_noh_sb1);
			String[] l;
			while ((l = dr.read()) != null) {
				dw.write(l[0], "str0", l[1], l[2]);
			}
			dr.close();
		}
		{
			DelimitedReader dr = new DelimitedReader(Main.file_visible + ".4");
			String[] l;
			while ((l = dr.read()) != null) {
				if (!l[3].startsWith("/m/")) {
					dw.write(l[0], "str1", l[2], l[3], l[4]);
				}
			}
			dr.close();
		}
		dw.close();
	}

	static void getVisible4() throws IOException {
		DelimitedWriter dw = new DelimitedWriter(Main.file_visible_jump);
		{
			DelimitedReader dr = new DelimitedReader(Main.file_visible + ".2");
			String[] l;
			while ((l = dr.read()) != null) {
				dw.write(l[0], "jump0", l[2], l[3]);
			}
			dr.close();
		}

		{
			DelimitedReader dr = new DelimitedReader(Main.file_visible + ".4");
			String[] l;
			while ((l = dr.read()) != null) {
				if (l[3].startsWith("/m/")) {
					dw.write(l[0], "jump1", l[2], l[3], l[4]);
				}
			}
			dr.close();
		}
		dw.close();
	}

	/**Give up this function, not necessary*/
	static void idlize() throws IOException {
		/**give mid who has name a id, for other mid, keep using mid to identify them*/
		//		HashMap<String, Integer> namedmid2name = new HashMap<String, Integer>(20000000);
		//		HashMap<String, Integer> relationname2id = new HashMap<String, Integer>(1000000);
		//		DelimitedWriter dw = new DelimitedWriter(Main.dir + "/visible_idize");
		//		DelimitedWriter dw_debug = new DelimitedWriter(Main.dir + "/visible_debug");
		//		{
		//			DelimitedReader dr = new DelimitedReader(Main.file_fbenglishname);
		//			String[] l;
		//			while ((l = dr.read()) != null) {
		//				namedmid2name.put(l[0], namedmid2name.size());
		//			}
		//			D.p("load mid2name finished");
		//			dr.close();
		//		}
		//		{
		//			DelimitedReader dr = new DelimitedReader(Main.file_visible_str);
		//			String[] l;
		//			while ((l = dr.read()) != null) {
		//				String mid = l[0];
		//				Integer gid = namedmid2name.get(mid);
		//				if (gid == null) {
		//					dw_debug.write(l);
		//					continue;
		//				}
		//				String relationname = l[2];
		//				if (!relationname2id.containsKey(relationname)) {
		//					relationname2id.put(relationname, relationname2id.size());
		//				}
		//				int relid = relationname2id.get(relationname);
		//				dw.write(l[1], gid, relid, l[3]);
		//			}
		//			dr.close();
		//		}
		//		{
		//			DelimitedReader dr = new DelimitedReader(Main.file_visible_str);
		//			String[] l;
		//			while ((l = dr.read()) != null) {
		//				String mid1 = l[0];
		//				String mid2 = l[3];
		//				Integer gid1 = namedmid2name.get(mid1);
		//				Integer gid2 = namedmid2name.get(mid2);
		//				if (gid1 == null || gid2 == null) {
		//					dw_debug.write(l);
		//					continue;
		//				}
		//				String relationname = l[2];
		//				if (!relationname2id.containsKey(relationname)) {
		//					relationname2id.put(relationname, relationname2id.size());
		//				}
		//				int relid = relationname2id.get(relationname);
		//				dw.write(l[1], gid1, relid, gid2, l[3]);
		//			}
		//			dr.close();
		//		}
		//		dw.close();
		//		{
		//			DelimitedWriter dwrel = new DelimitedWriter(Main.dir + "/visible_relations.temp");
		//			for (Entry<String, Integer> e : relationname2id.entrySet()) {
		//				dwrel.write(e.getValue(), e.getKey());
		//			}
		//			dwrel.close();
		//			Sort.sort(Main.dir + "/visible_relations.temp", Main.dir + "/visible_relations", Main.dir,
		//					new Comparator<String[]>() {
		//						@Override
		//						public int compare(String[] o1, String[] o2) {
		//							// TODO Auto-generated method stub
		//							return Integer.parseInt(o1[0]) - Integer.parseInt(o2[0]);
		//						}
		//
		//					});
		//		}
		//		{
		//			DelimitedWriter dwmid = new DelimitedWriter(Main.dir + "/visible_mid2gid.temp");
		//			for (Entry<String, Integer> e : namedmid2name.entrySet()) {
		//				dwmid.write(e.getValue(), e.getKey());
		//			}
		//			dwmid.close();
		//			Sort.sort(Main.dir + "/visible_mid2gid.temp", Main.dir + "/visible_mid2gid", Main.dir,
		//					new Comparator<String[]>() {
		//						@Override
		//						public int compare(String[] o1, String[] o2) {
		//							// TODO Auto-generated method stub
		//							return Integer.parseInt(o1[0]) - Integer.parseInt(o2[0]);
		//						}
		//
		//					});
		//		}
	}

	public static void merge() throws IOException {
		DelimitedWriter dw = new DelimitedWriter(Main.file_visible);
		{
			DelimitedReader dr = new DelimitedReader(Main.file_visible_str);
			String[] l;
			while ((l = dr.read()) != null) {
				String type = l[1].replace("str", "s");
				if (l.length == 4) {
					dw.write(type, l[0], l[2], l[3], "");
				} else {
					dw.write(type, l[0], l[2], l[3], l[4]);
				}
			}
		}
		{
			DelimitedReader dr = new DelimitedReader(Main.file_visible_jump);
			String[] l;
			while ((l = dr.read()) != null) {
				String type = l[1].replace("jump", "j");
				if (l.length == 4) {
					dw.write(type, l[0], l[2], l[3], "");
				} else {
					dw.write(type, l[0], l[2], l[3], l[4]);
				}
			}
		}
		dw.close();
	}

	public static void filter() throws IOException {
		//get mid to enid
		//		HashSet<String> wikimid = new HashSet<String>();
		//		{
		//			DelimitedWriter dw = new DelimitedWriter(Main.file_mid2wid);
		//			DelimitedReader dr = new DelimitedReader(Main.file_fbdump_2_len4);
		//			String[] l;
		//			while ((l = dr.read()) != null) {
		//				if (l[1].equals("/type/object/key") && l[2].equals("/wikipedia/en_id")) {
		//					dw.write(l[0], l[3]);
		//					wikimid.add(l[0]);
		//				}
		//			}
		//			dr.close();
		//			dw.close();
		//			D.p("wiki id size is", wikimid.size());
		//		}
		//		DelimitedWriter dw = new DelimitedWriter(Main.file_visible + ".filter");
		//		{
		//			DelimitedReader dr = new DelimitedReader(Main.file_visible);
		//			String[] l;
		//			int count = 0, write = 0;
		//			while ((l = dr.read()) != null) {
		//				count++;
		//				if (count % 100000 == 0) {
		//					D.p("count vs write", count, write);
		//				}
		//				String rel = l[2];
		//				if (rel.startsWith("/type") || rel.startsWith("/user") || rel.startsWith("/common")
		//						|| rel.startsWith("/base")) {
		//					continue;
		//				}
		//				if (l[0].startsWith("s") && !wikimid.contains(l[1])) {
		//					continue;
		//				}
		//				if (l[0].startsWith("j") && (!wikimid.contains(l[1]) || !wikimid.contains(l[3]))) {
		//					continue;
		//				}
		//				dw.write(l);
		//				write++;
		//			}
		//
		//		}
		//		dw.close();
		//		Sort.sort(Main.file_visible + ".filter", Main.file_visible + ".filter.sbmid", Main.dir,
		//				new Comparator<String[]>() {
		//
		//					@Override
		//					public int compare(String[] arg0, String[] arg1) {
		//						// TODO Auto-generated method stub
		//						return arg0[1].compareTo(arg1[1]);
		//					}
		//
		//				});
		//		Sort.sort(Main.file_visible + ".filter", Main.file_visible + ".filter.sbmid2", Main.dir,
		//				new Comparator<String[]>() {
		//
		//					@Override
		//					public int compare(String[] arg0, String[] arg1) {
		//						// TODO Auto-generated method stub
		//						return arg0[3].compareTo(arg1[3]);
		//					}
		//
		//				});
		Sort.sort(Main.file_visible + ".filter", Main.file_visible + ".filter.sbrel", Main.dir,
				new Comparator<String[]>() {

					@Override
					public int compare(String[] arg0, String[] arg1) {
						// TODO Auto-generated method stub
						return arg0[2].compareTo(arg1[2]);
					}

				});
	}

	public static void idlize1() throws IOException {
		DelimitedReader dr = new DelimitedReader(Main.file_visible + ".filter.sbrel");
		HashMap<String, Integer> map_rel2myid = new HashMap<String, Integer>();
		HashMap<String, Integer> map_mid2myid = new HashMap<String, Integer>();
		DelimitedWriter dwmatrix = new DelimitedWriter(Main.file_graph_matrix);
		DelimitedWriter dwstring = new DelimitedWriter(Main.file_graph_string);
		DelimitedWriter dwrelid = new DelimitedWriter(Main.file_graph_rel2myid);
		DelimitedWriter dwmidid = new DelimitedWriter(Main.file_graph_mid2myid);

		String[] l;
		while ((l = dr.read()) != null) {
			if (!map_rel2myid.containsKey(l[2])) {
				map_rel2myid.put(l[2], map_rel2myid.size() + 1);
			}
			if (!map_mid2myid.containsKey(l[1])) {
				map_mid2myid.put(l[1], map_mid2myid.size() + 1);
			}
			if (l[0].startsWith("s")) {
				int relid = map_rel2myid.get(l[2]);
				int mid1 = map_mid2myid.get(l[1]);
				dwstring.write(mid1, l[3], relid);
			} else {
				if (!map_mid2myid.containsKey(l[3])) {
					map_mid2myid.put(l[3], map_mid2myid.size() + 1);
				}
				int relid = map_rel2myid.get(l[2]);
				int mid1 = map_mid2myid.get(l[1]);
				int mid2 = map_mid2myid.get(l[3]);
				dwmatrix.write(mid1, mid2, relid);
			}
		}
		dr.close();
		for (Entry<String, Integer> e : map_rel2myid.entrySet()) {
			dwrelid.write(e.getKey(), e.getValue());
		}
		for (Entry<String, Integer> e : map_mid2myid.entrySet()) {
			dwmidid.write(e.getKey(), e.getValue());
		}
		dwmatrix.close();
		dwstring.close();
		dwrelid.close();
		dwmidid.close();
	}

	public static void luceneIndexMid() {
		String file = Main.file_visible + ".filter";
		String dirIndex = Main.file_visible + ".luceneIndex";
		Date start = new Date();

		try {
			if ((new File(dirIndex)).exists()) {
				(new File(dirIndex)).delete();
			}
			Directory dir = FSDirectory.open(new File(dirIndex));
			Analyzer analyzer = new StandardAnalyzer(Version.LUCENE_31);
			IndexWriterConfig iwc = new IndexWriterConfig(Version.LUCENE_31, analyzer);
			iwc.setOpenMode(OpenMode.CREATE);
			IndexWriter writer = new IndexWriter(dir, iwc);

			{
				DelimitedReader dr = new DelimitedReader(file);
				String[] l;
				int count = 0;
				while ((l = dr.read()) != null) {
					//					if (count++ > 10000)
					//						break;
					Document doc = new Document();
					StringBuilder sbp = new StringBuilder();
					for (int a = 0; a < l.length; a++) {
						String path = l[a];
						sbp.append(path).append("\t");
					}
					Field pathField = new Field("path", sbp.toString(), Field.Store.YES,
							Field.Index.NOT_ANALYZED_NO_NORMS);
					pathField.setOmitTermFreqAndPositions(true);
					doc.add(pathField);
					//doc.add(new Field("contents", new StringReader(l[indexColumn])));
					String key = l[1];
					if (l[3].startsWith("/m/")) {
						key += " " + l[3];
					}
					doc.add(new Field("contents", key, Field.Store.YES, Field.Index.ANALYZED));
					writer.addDocument(doc);
				}
				dr.close();
			}
			writer.close();

			Date end = new Date();
			System.out.println(end.getTime() - start.getTime() + " total milliseconds");

		} catch (IOException e) {
			System.out.println(" caught a " + e.getClass() + "\n with message: " + e.getMessage());
		}
	}

	public static void get_notable_type() throws IOException {
		{
			DelimitedReader dr = new DelimitedReader(Main.file_fbdump_3_str);
			DelimitedWriter dw = new DelimitedWriter(Main.file_mid2notabletype);
			String[] l;
			while ((l = dr.read()) != null) {
				if (l[1].equals("/common/topic/notable_for")) {
					String mid = l[0];
					String[] ab = l[2].split(" ");
					try {
						String x = ab[1].split(":")[1].replace("\"", "").replace(",", "");
						dw.write(mid, x);
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}
			dr.close();
			dw.close();
		}
	}

	static void getMidWidNames() throws IOException {
		//mid, wid, notabletype, name, alias
		HashMap<String, String[]> mid2other = new HashMap<String, String[]>();
		{
			DelimitedReader dr = new DelimitedReader(Main.file_mid2wid);
			String[] l;
			while ((l = dr.read()) != null) {
				if (mid2other.containsKey(l[0])) {
					String[] s = mid2other.get(l[0]);
					s[1] = s[1] + "::" + l[1];
				} else {
					String[] s = new String[5];
					s[0] = l[0] + "";
					s[1] = l[1] + "";
					s[2] = "";
					s[3] = "";
					s[4] = "";
					mid2other.put(l[0], s);
				}
			}
			dr.close();
		}
		//load notable type
		{
			DelimitedReader dr = new DelimitedReader(Main.file_mid2notabletype);
			String[] l;
			while ((l = dr.read()) != null) {
				String mid = l[0];
				if (mid2other.containsKey(mid)) {
					String[] s = mid2other.get(mid);
					if (s[2].equals("")) {
						s[2] = l[1];
					} else {
						s[2] = s[2] + "::" + l[1];
					}
				}
			}
			dr.close();
		}
		//load names & alias
		{
			DelimitedReader dr = new DelimitedReader(Main.file_fbdump_2_len4);
			String[] l;
			while ((l = dr.read()) != null) {
				// set name
				if (l[1].equals("/type/object/name") && l[2].equals("/lang/en")) {
					String mid = l[0];
					if (mid2other.containsKey(mid)) {
						String[] s = mid2other.get(mid);
						if (s[3].equals("")) {
							s[3] = l[3];
						} else {
							s[3] = s[3] + "::" + l[3];
						}
						if (s[4].equals("")) {
							s[4] = l[3];
						} else {
							s[4] = s[4] + "::" + l[3];
						}
					}
				}
				if (l[1].equals("/common/topic/alias") && l[2].equals("/lang/en")) {
					String mid = l[0];
					if (mid2other.containsKey(mid)) {
						String[] s = mid2other.get(mid);
						if (s[4].equals("")) {
							s[4] = l[3];
						} else {
							s[4] = s[4] + "::" + l[3];
						}

					}
				}
			}
		}
		//write
		{
			DelimitedWriter dw = new DelimitedWriter(Main.file_midWidTypeNameAlias);
			for (Entry<String, String[]> e : mid2other.entrySet()) {
				dw.write(e.getValue());
			}
			dw.close();
		}
	}

	public static void main(String[] args) throws IOException {
		//split();
		//removeHiddenFromFbdump_3_str();
		//sort();
		//getName();
		//getVisible1();
		//getVisible2();
		//getVisible3();
		//getVisible4();
		//merge();
		//filter();
		idlize1();
		//get_notable_type();
		//getMidWidNames();

	}
}
