package javatools.datatypes;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.Map.Entry;

class NameValue<T> {
	T name;
	double value;
}

public class Hash2Value<T> implements Iterable<Entry<T, Double>>, Iterator<Entry<T, Double>> {

	/**
	 * @param args
	 */
	HashMap<T, Double> value = new HashMap<T, Double>();
	List<NameValue> nclist;

	public void add(T a, double v) {
		if (!value.containsKey(a)) {
			value.put(a, 0.0);
		}
		double x = value.get(a);
		value.put(a, x + v);
	}

	public double see(T a) {
		if (value.containsKey(a)) {
			return value.get(a);
		} else {
			return 0;
		}
	}

	public void sort() {
		nclist = new ArrayList<NameValue>();
		for (Entry<T, Double> e : value.entrySet()) {
			NameValue<T> nc = new NameValue<T>();
			nc.value = e.getValue();
			nc.name = e.getKey();
			nclist.add(nc);
		}
		Collections.sort(nclist, new Comparator<NameValue>() {
			public int compare(NameValue nc1, NameValue nc2) {
				double x = nc1.value-nc2.value;
				int y = 0;
				if(x>0){
					y = 1;
				}else if(x<0){
					y = -1;
				}else{
					y = 0;
				}
				return y;
			}
		});
	}

	public void printAll() {
		if (nclist == null)
			sort();
		for (NameValue<T> nc : nclist) {
			System.out.println(nc.name + "\t" + nc.value);
		}
	}

	public void getAll(List<T> ids, List<Double> values) {
		if (nclist == null)
			sort();
		for (NameValue<T> nc : nclist) {
			ids.add(nc.name);
			values.add(nc.value);
		}
	}

	public List<String[]> getAll() {
		if (nclist == null)
			sort();
		List<String[]> result = new ArrayList<String[]>();

		for (NameValue<T> nc : nclist) {
			String[] a = new String[2];
			a[0] = (String) nc.name;
			a[1] = "" + nc.value;
			result.add(a);
		}
		return result;
	}

	public int size() {
		return value.size();
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

	Iterator<Entry<T, Double>> it;

	@Override
	public boolean hasNext() {
		// TODO Auto-generated method stub
		return it.hasNext();
	}

	@Override
	public Entry<T, Double> next() {
		// TODO Auto-generated method stub

		return it.next();
	}

	@Override
	public void remove() {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException();
	}

	@Override
	public Iterator<Entry<T, Double>> iterator() {
		// TODO Auto-generated method stub
		it = this.value.entrySet().iterator();
		return it;
	}

}
