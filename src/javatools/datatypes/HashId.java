package javatools.datatypes;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;

public class HashId<T> implements Iterable<T>, Iterator<T>{

	/**
	 * @param args
	 */
	HashMap<T, Integer> h;
	List<T> list;
	//= new HashMap<T, Integer>();

	public HashId() {
		h = new HashMap<T, Integer>();
		list = new ArrayList<T>();
	}

	public int getId(T element, boolean addOrNot) {
		if (h.containsKey(element)) {
			return h.get(element);
		} else {
			if (addOrNot) {
				int newid = h.size();
				h.put(element, newid);
				list.add(element);
				return newid;
			} else {
				return -1;
			}
		}
	}

	public int add(T element) {
		return getId(element, true);
	}

	public int seeId(T element) {
		return getId(element, false);
	}
	public T getElement(int id){
		if(list.size()<id){
			return list.get(id);
		}else{
			return null;
		}
	}


	Iterator<T> it;
	@Override
	public boolean hasNext() {
		// TODO Auto-generated method stub
		
		return it.hasNext();
	}

	@Override
	public T next() {
		// TODO Auto-generated method stub
		return it.next();
	}

	@Override
	public void remove() {
		// TODO Auto-generated method stub
		it.remove();
	}

	@Override
	public Iterator<T> iterator() {
		// TODO Auto-generated method stub
		it = this.list.iterator();
		return this;
	}
	

	public static void main(String[] args) {
		// TODO Auto-generated method stub
	}

}
