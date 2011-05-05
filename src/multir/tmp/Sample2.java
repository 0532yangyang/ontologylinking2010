package multir.tmp;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.zip.GZIPInputStream;

import multir.util.delimited.DelimitedReader;
import multir.util.delimited.DelimitedWriter;
import cc.factorie.protobuf.DocumentProtos.Relation;
import cc.factorie.protobuf.DocumentProtos.Relation.RelationMentionRef;

/*
 * VBA CODE FOR HIGHLIGHTING IN EXCEL

Sub Test()

   For i = 1 To 2000
      s = ActiveSheet.Cells(i, 2).Value
      j = InStr(1, s, "[")
      Do While j > 0
        e = InStr(j + 1, s, "]")
        ActiveSheet.Cells(i, 2).Characters(j + 1, e - j - 1).Font.Color = RGB(0, 0, 255)
        ActiveSheet.Cells(i, 2).Characters(e + 1, 1).Font.Color = RGB(255, 0, 0)
        j = InStr(e + 1, s, "[")
      Loop
   Next i

End Sub


 * 
 * 
 */



public class Sample2 {

	static String dir = "/projects/pardosa/data14/raphaelh/t";
	static String exp = "/ftexp4";
	
	static int PAIRS = 1000; //600
	
	public static void main(String[] args) throws IOException {

		String input = dir + "/data/subset06.100.pb.gz";
		
		// count pairs
		List<Integer> posPairs = new ArrayList<Integer>();

		int totalPairs = 0;
		int totalSentences = 0;
		
		{
		    InputStream is = new GZIPInputStream(new BufferedInputStream
		    		(new FileInputStream(input)));
		    Relation r = null;
		    int i=0;
		    while ((r = Relation.parseDelimitedFrom(is))!=null) {
				totalPairs++;
				totalSentences += r.getMentionCount();
		    	if (!r.getRelType().equals("NA")) posPairs.add(i);
		    	i++;
		    }
		    is.close();
		}
		
		// sample from pairs
		Random random = new Random(7);
		HashSet<Integer> ts = new HashSet<Integer>();
		while (ts.size() < PAIRS)
			ts.add(posPairs.get(random.nextInt(posPairs.size())));
		
		// count sentences
		HashMap<String,Integer> rels = new HashMap<String,Integer>();
		int smpSentences = 0;
		int posSentences = 0;
		{
		    InputStream is = new GZIPInputStream(new BufferedInputStream
		    		(new FileInputStream(input)));
		    Relation r = null;
			int i = 0;
		    while ((r = Relation.parseDelimitedFrom(is))!=null) {
				if (!ts.contains(i++)) continue;

				smpSentences += r.getMentionCount();				

				String rt = r.getRelType();
				String[] rs = rt.split(",");
				for (String rss : rs) {
					Integer c = rels.get(rss);
					if (c == null) c = 0;
					c++;
					rels.put(rss, c);
					posSentences++;
				}
			}
			is.close();
		}
		
		System.out.println(totalPairs);
		System.out.println(totalSentences);
		System.out.println(posPairs.size());
		System.out.println(smpSentences);
		for (Map.Entry<String,Integer> e : rels.entrySet()) {
			System.out.println("R" + e.getKey() + "\t" + e.getValue());
		}
		
		System.out.println(rels.size());
		System.out.println(posSentences);
		
		
		// write to disk
		{
			DelimitedWriter w = new DelimitedWriter(dir + "/manual3/ids");
		    InputStream is = new GZIPInputStream(new BufferedInputStream
		    		(new FileInputStream(input)));
		    Relation r = null;
			int i = 0;
		    while ((r = Relation.parseDelimitedFrom(is))!=null) {
				if (!ts.contains(i++)) continue;
				for (int m=0; m < r.getMentionCount(); m++) {
					RelationMentionRef rmf = r.getMention(m);
					w.write(r.getSourceGuid(), r.getDestGuid(), rmf.getFilename(), r.getRelType(), toString(rmf.getFeatureList()));
				}
			}
			w.close();
			is.close();
		}
		
		// read from disk
		HashMap<Integer,List<String[]>> m = new HashMap<Integer,List<String[]>>();
		{
			DelimitedReader r = new DelimitedReader(dir + "/manual3/ids");
			String[] t = null;
			while ((t = r.read())!= null) {
				int sentenceID = Integer.parseInt(t[2]);
				List<String[]> l = m.get(sentenceID);
				if (l == null) {
					l = new ArrayList<String[]>();
					m.put(sentenceID, l);
				}
				l.add(t);
			}
			r.close();
		}
		
		
		{
			BufferedWriter w = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(
					dir + "/manual3/labelsX"), "utf-8"));
			BufferedReader r = new BufferedReader(new InputStreamReader
					(new FileInputStream(dir + "/raw/sentences.tokens"), "utf-8"));
			String line = null;
			while ((line = r.readLine())!= null) {
				String[] cols = line.split("\t");
				int sentenceID = Integer.parseInt(cols[0]);
				List<String[]> l = m.get(sentenceID);
				if (l == null) continue;
				
				for (String[] t : l) {
					String sentence = cols[1];					
					sentence = sentence.replaceAll(t[0], "[" + t[0] + "]1");
					sentence = sentence.replaceAll(t[1], "[" + t[1] + "]2");
					w.write(sentenceID + "\t" + t[0] + "\t" + t[1] + "\t" + sentence + "\t" + t[3] + "\t" + t[4] + "\n" );
				}

			}
			w.close();
			r.close();
		}
		

		
		
		
		
		
		
		/*
		// sample 1000 IDs
		
		List<Integer> all = new ArrayList<Integer>();
		
		//int count = 0;
		{
			BufferedReader r = new BufferedReader(new InputStreamReader
					(new FileInputStream(dir + "/raw/sentences.tokens"), "utf-8"));
			String line = null;
			while ((line = r.readLine())!= null) {
				int sep = line.indexOf('\t');
				int sentenceID = Integer.parseInt(line.substring(0,sep));
				if (sentenceID >= startSentenceID2006 && sentenceID < endSentenceID2006) 
					all.add(sentenceID);
					//count++;
			}
			r.close();
		}
		//System.out.println(count);
		
		Collections.shuffle(all);
		all = all.subList(0, Math.min(1000, all.size()));
		
		Collections.sort(all);
		
		{
			BufferedWriter w = new BufferedWriter(new OutputStreamWriter
					(new FileOutputStream(dir + "/manual/ids")));
			for (Integer i : all) w.write(i + "\n");
			w.close();
		}
		
		HashSet<Integer> hs = new HashSet<Integer>();
		{
			BufferedReader r = new BufferedReader(new InputStreamReader
					(new FileInputStream(dir + "/manual/ids")));
			String line = null;
			while ((line = r.readLine())!= null)
				hs.add(Integer.parseInt(line));
			r.close();
		}

		{
			BufferedWriter w = new BufferedWriter(new OutputStreamWriter
					(new FileOutputStream(dir + "/manual/test.html")));
			w.write("<html><body>");

			
			BufferedReader r1 = new BufferedReader(new InputStreamReader
					(new FileInputStream(dir + "/raw/sentences.tokens"), "utf-8"));
			BufferedReader r2 = new BufferedReader(new InputStreamReader
					(new FileInputStream(dir + "/raw/sentences.ner"), "utf-8"));
			String l1 = r1.readLine(), l2 = r2.readLine();
			int i1, i2;
			List<String[]> entities = new ArrayList<String[]>();
			
			for (int id : all) {
				// there is exactly one sentence, but there may be 0 or more entities
				while (l1 != null && (i1 = Integer.parseInt(l1.substring(0, l1.indexOf('\t')))) < id) l1 = r1.readLine();
				while (l2 != null && (i2 = Integer.parseInt(l2.substring(0, l2.indexOf('\t')))) < id) l2 = r2.readLine();
				
				entities.clear();
				while (l2 != null && (i2 = Integer.parseInt(l2.substring(0, l2.indexOf('\t')))) == id) {
					entities.add(l2.split("\t"));
					l2 = r2.readLine();
				}
				
				String[] tokens = l1.substring(l1.indexOf('\t')).split("\t");
				
				w.write("<p>" + l1 + "</p>");
				
				w.write("<ul>");
				for (int i=0; i < entities.size(); i++)
					w.write("<li>" + entities.get(i)[0] + " " + entities.get(i)[1] + " " +entities.get(i)[2] + " " + entities.get(i)[3] + "</li>");
				w.write("</ul>");
					
				/*
				int ne = 0;
				for (int i=0; i < tokens.length; i++) {
					if (ne < entities.size()) {
						int st = Integer.parseInt(entities.get(0)[1]);
						int en = Integer.parseInt(entities.get(0)[2]);
						if (i < st)
							w.write(tokens[i] + " ");
						else if (i >= en) {
							i--;
							ne++;
						} else {
							w.write("<b>" + tokens[i] + "</b>" + " ");
						}
					} else {
						w.write(tokens[i] + " ");
					}
				}
				w.write("<br>");
				*/
		/*
			}
			r1.close();
			r2.close();
			
			w.write("</body></html>");
			w.close();
		}
		*/
	}
	
	private static String toString(int[] a) {
		if (a.length == 0) return "";
		StringBuilder sb = new StringBuilder();
		for (int i=0; i < a.length; i++) {
			if (i > 0) sb.append(",");
			sb.append(a[i]);
		}
		return sb.toString();
	}
	
	private static String toString(List<String> l) {
		if (l.size() == 0) return "";
		StringBuilder sb = new StringBuilder();
		for (int i=0; i < l.size(); i++) {
			if (i > 0) sb.append("; ");
			sb.append(l.get(i));
		}
		return sb.toString();
	}
	
}
