package multir.tmp;

import java.io.IOException;

import multir.util.delimited.DelimitedReader;

public class Ambiguous {

	/**
	 * @param args
	 */
	public static void main(String[] args) throws IOException {

		int a = 0;
		
		DelimitedReader r = new DelimitedReader("/projects/pardosa/data14/raphaelh/t/freebase-names");
		String[] t = null;
		String[] p = null;
		while ((t = r.read())!= null) {
			if (p != null && p[1].equals(t[1])) a++;
			
			p = t;
		}
		r.close();
		
		System.out.println(a);
	}

}
