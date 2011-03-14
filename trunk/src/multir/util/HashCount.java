package multir.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Random;
import java.util.Map.Entry;

class NameCount<T> {
	T name;
	int count;
}

public class HashCount<T> {

	/**
	 * @param args
	 */
	HashMap<T, Integer> count = new HashMap<T, Integer>();
	List<NameCount> nclist;

	public void add(T a) {
		if (!count.containsKey(a)) {
			count.put(a, 0);
		}
		int x = count.get(a);
		count.put(a, x + 1);
	}

	public int see(T a) {
		if (count.containsKey(a)) {
			return count.get(a);
		} else {
			return 0;
		}
	}

	public void sort() {
		nclist = new ArrayList<NameCount>();
		for (Entry<T, Integer> e : count.entrySet()) {
			NameCount<T> nc = new NameCount<T>();
			nc.count = e.getValue();
			nc.name = e.getKey();
			nclist.add(nc);
		}
		Collections.sort(nclist, new Comparator<NameCount>() {
			public int compare(NameCount nc1, NameCount nc2) {
				return nc2.count - nc1.count;
			}
		});
	}

	public void printAll() {
		if (nclist == null)
			sort();
		for (NameCount<T> nc : nclist) {
			System.out.println(nc.name + "\t" + nc.count);
		}
	}

	public void getAll(List<T>ids, List<Integer>counts){
		if(nclist == null)sort();
		for(NameCount<T> nc : nclist) {
			ids.add(nc.name);
			counts.add(nc.count);
		}
	}
	public List<String[]> getAll(){
		if(nclist == null)sort();
		List<String[]>result = new ArrayList<String[]>();
		
		for(NameCount<T> nc : nclist) {
			String []a = new String[2];
			a[0] = (String)nc.name;
			a[1] = ""+nc.count;
			result.add(a);
		}
		return result;
	}
	public int size() {
		return count.size();
	}

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		HashCount<Integer> hc = new HashCount<Integer>();
		Random r = new Random();
		for (int i = 0; i < 100; i++) {
			hc.add(r.nextInt(10));
		}
		hc.printAll();
	}

}
