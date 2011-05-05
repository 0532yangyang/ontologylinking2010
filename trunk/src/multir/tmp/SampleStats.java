package multir.tmp;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import multir.util.delimited.DelimitedReader;

public class SampleStats {
	
	static String dir = "/projects/pardosa/data14/raphaelh/t";
	
	static HashMap<String,RelStat> m = new HashMap<String,RelStat>();
	
	public static void main(String[] args) throws IOException {
		
		{
			DelimitedReader r = new DelimitedReader(dir + "/manual3/labelsX");
			// read block of sentences for same pair
			String[] t = null;
			List<String[]> l = new ArrayList<String[]>();
			while ((t = r.read())!= null) {
				if (t[4].equals("")) continue; // not yet labeled
				
				
				if (l.isEmpty()) {
					l.add(t);
					continue;
				}
				
				
				if ((t[1] + "\t" + t[2]).equals(l.get(0)[1] + "\t" + l.get(0)[2])) {
					l.add(t);
					continue;
				}
				
				processBlock(l);
				
				l.clear();
				l.add(t);
			}
			if (!l.isEmpty()) processBlock(l);
			r.close();
		}

		
		for (Map.Entry<String,RelStat> e : m.entrySet()) {
			e.getValue().print();
		}
		
	}
	
	private static void processBlock(List<String[]> l) {
		String[] heuRels = l.get(0)[5].split(",");
		for (String heuRel : heuRels) {
			RelStat rs = m.get(heuRel);
			if (rs == null) {
				rs = new RelStat(heuRel);
				m.put(heuRel, rs);
			}
			rs.process(l);
		}
	}
	
	
	static class RelStat {
		String key;
		Map<Integer,Stat> m = new HashMap<Integer,Stat>();
		
		RelStat(String key) {
			this.key = key;
		}
		
		void process(List<String[]> l) {
			// examples tagged with this key
			int tagged = 0;
			for (String[] t : l) {
				String[] ks = t[4].split(",");
				boolean found = false;
				for (String k : ks) if (k.equals(key)) found = true;
				if (found) tagged++;
			}
			Stat s = m.get(l.size());
			if (s == null) {
				s = new Stat(l.size());
				m.put(l.size(), s);
			}
			s.n.add(tagged);
		}
		
		void print() {
			System.out.println(key);
			for (Map.Entry<Integer,Stat> e : m.entrySet()) {
				System.out.println("   " + e.getKey() + "\t" + e.getValue().n.size() + " examples\t" + e.getValue().mean() + "\t" + e.getValue().atLeastOnceHolds());
			}
		}
		
	}
	
	static class Stat {
		List<Integer> n = new ArrayList<Integer>();
		int total;
		
		Stat(int total) {
			this.total = total;
		}
		
		double mean() {
			double sum = 0;
			for (int i : n) sum += i;
			return sum / (n.size()*total);
		}
		
		double atLeastOnceHolds() {
			double sum = 0;
			for (int i : n) if (i > 0) sum++;
			return sum / (n.size());			
		}
	}
}
