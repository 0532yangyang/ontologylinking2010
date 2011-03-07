package multir.util;

import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;

public class HashList <T>{
	HashMap<T,Integer> map = new HashMap<T,Integer>();
	List<T> list = new ArrayList<T>();

	public	HashList(){}

	public int add(T e){
		if(!map.containsKey(e)){
			int newid = list.size();
			map.put(e,newid);
			list.add(e);
			return newid;
		}else{
			return map.get(e);
		}
	}

	public int size(){
		return list.size();
	}

	public int getId(T e){
		if(!map.containsKey(e)){
			return -1;
		}else{
			return map.get(e);
		}
	}
	public T getElement(int id){
		if(id<0 ||id> list.size()){return null;}
		else{
			return list.get(id);
		}
	}
}
