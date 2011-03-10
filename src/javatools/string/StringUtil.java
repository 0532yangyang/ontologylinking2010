package javatools.string;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

import javatools.administrative.D;

public class StringUtil {
	
	public static List<String> sortAndRemoveDuplicate(List<String> list){
		HashSet<String> temp = new HashSet<String>();
		for(String a: list){
			temp.add(a.toLowerCase());
		}
		ArrayList<String>result = new ArrayList<String>();
		result.addAll(temp);
		Collections.sort(result);
//		String previous = "";
//		Iterator<String> it = list.iterator();
//		while(it.hasNext()){
//			String cur = it.next();
//			if(cur.equals(previous)){
//				it.remove();
//			}else{
//				previous = cur;
//			}
//		}
		return result;
	}
	public static List<String> tokenize(String str, char []stopChar){
		List<String>result = new ArrayList<String>();
		HashSet<Character> stopCharSet = new HashSet<Character>();
		for(char a: stopChar)stopCharSet.add(a);
		char []cs = str.toCharArray();
		int bufferStart = 0;
		for(int i=0;i<cs.length;i++){
			if(stopCharSet.contains(cs[i])){
				addWord(result,bufferStart,i,str);
				bufferStart= i+1;//give up this char because it is stop char
			}else if(Character.isUpperCase(cs[i])){
				if(i>0 && Character.isLowerCase(cs[i-1])){
					addWord(result,bufferStart,i,str);
					bufferStart = i;
				}
			}else if(Character.isLowerCase(cs[i])){
				//if two previous characters are both Upper case, then shoot the word
				if(i>1 && Character.isUpperCase(cs[i-1]) && Character.isUpperCase(cs[i-2])){
					addWord(result,bufferStart,i,str);
					bufferStart = i;
				}
			}else{
				//joke haha
			}
		}
		addWord(result,bufferStart,cs.length,str);
		return result;
	}
	
	public static List<String> tokenize(String str){
		return tokenize(str, new char[]{' ', '\t'});
	}
	
	private static void addWord(List<String>list, int start, int end, String str){
		if(end>start)list.add(str.substring(start,end));
	}

	public static int numOfShareWords(List<String>sortedList1, List<String>sortedList2){
		int i=0,j=0,num=0;
		while(i<sortedList1.size() && j<sortedList2.size()){
			String a = sortedList1.get(i);
			String b = sortedList2.get(j);
			int c = a.compareTo(b);
			if(c==0){
				num++;
				i++;j++;
			}else if(c < 0){
				i++;
			}else{
				j++;
			}
		}
		return num;
	}
	public static int numOfShareWords(String str1, String str2, int []par_return){
		List<String> l1 = tokenize(str1,new char[]{' ','_'});
		List<String> l2 = tokenize(str2,new char[]{' ','_'});
		List<String>sorted_l1 = new ArrayList<String>();
		List<String>sorted_l2 = new ArrayList<String>();
		for(String a:l1)sorted_l1.add(a.toLowerCase());
		for(String a:l2)sorted_l2.add(a.toLowerCase());
		Collections.sort(sorted_l1);
		Collections.sort(sorted_l2);
		par_return[0] = l1.size();
		par_return[1] = l2.size();
		return numOfShareWords(sorted_l1,sorted_l2);
	}
	
	public static int numOfShareWords(String str1, String str2){
		List<String> l1 = tokenize(str1,new char[]{' ','_'});
		List<String> l2 = tokenize(str2,new char[]{' ','_'});
		return numOfShareWords(l1,l2);
	}
	
	public static int numOfShareInteger(List<Integer>sortedList1, List<Integer>sortedList2){
		int i=0,j=0,num=0;
		while(i<sortedList1.size() && j<sortedList2.size()){
			int a = sortedList1.get(i);
			int b = sortedList2.get(j);
			int c = a-b;
			if(c==0){
				num++;
				i++;j++;
			}else if(c < 0){
				i++;
			}else{
				j++;
			}
		}
		return num;
	}
	public static void main(String []args){
		D.p(tokenize("/business/business_operation/industry|/business/industry/companies|",new char[]{'|','/','_'}));
	}
}
