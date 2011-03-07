package javatools.filehandlers;
import java.util.*;

public class MergeReadRes {
	public ArrayList<String[]>line1_list =new ArrayList<String[]>();
	public ArrayList<String[]>line2_list =new ArrayList<String[]>();
	public int key_value;
	
	public void clear(){
		this.line1_list = null;
		this.line2_list = null;
	}
}
