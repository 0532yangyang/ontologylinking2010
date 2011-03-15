package freebase.match;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javatools.filehandlers.DelimitedReader;
import javatools.filehandlers.DelimitedWriter;


public class GetNellEntityFreebaseId3 {

	static Map<String, String> url2mid = new HashMap<String, String>();
	static Map<String, Integer> mid2zclid = new HashMap<String, Integer>();
	static Map<String, String> seed2url = new HashMap<String, String>();

	static void step1() throws IOException {
		String dir = "/projects/pardosa/s5/clzhang/ontologylink";
		String input1 = dir + "/mid2enurl";
		String input2 = dir + "/mid2zclid";
		String input3 = dir + "/tmp2/nellSeedsWrittenInUrl";
		String output = dir + "/tmp2/nellseed2zclid";
		DelimitedWriter dw = new DelimitedWriter(output);
		String[] line;
		{
			DelimitedReader dr = new DelimitedReader(input1);
			int ln = 0;
			while ((line = dr.read()) != null) {
				if (line.length < 2)
					continue;
				if (ln++ % 100000 == 0)
					System.out.print(ln + "\r");
				String url = line[1];
				String mid = line[0];
				url2mid.put(url, mid);
			}
			dr.close();
		}
		{
			DelimitedReader dr = new DelimitedReader(input2);
			int ln = 0;
			while ((line = dr.read()) != null) {
				if (ln++ % 100000 == 0)
					System.out.print(ln + "\r");
				String mid = line[0];
				int zclid = Integer.parseInt(line[1]);
				mid2zclid.put(mid, zclid);
			}
			dr.close();
		}
		{
			DelimitedReader dr = new DelimitedReader(input3);
			while ((line = dr.read()) != null) {
				String arg1 = line[0], arg2 = line[1], rel = line[2], url1 = line[3], url2 = line[4];
				String mid1 = "", mid2 = "";
				int zclid1 = -1, zclid2 = -1;
				try {
					zclid1 = getZclid(url1);
					zclid2 = getZclid(url2);
					mid1 = getMid(url1);
					mid2 = getMid(url2);
				} catch (Exception e) {
					System.err.println(url1 + "\t" + url2);
				} finally {
					if (zclid1 >= 0 && zclid2 >= 0) {
						dw.write(zclid1, zclid2, arg1, arg2, rel, mid1, mid2);
					}
				}
			}
		}
		dw.close();

	}

	static void step1_neg() throws IOException {
		String dir = "/projects/pardosa/s5/clzhang/ontologylink";
		String input1 = dir + "/mid2enurl";
		String input2 = dir + "/mid2zclid";
		String input3 = dir + "/tmp2/nellSeedsWrittenInUrlNeg";
		String output = dir + "/tmp2/nellseed2zclidNeg";
		DelimitedWriter dw = new DelimitedWriter(output);
		String[] line;
		{
			DelimitedReader dr = new DelimitedReader(input1);
			int ln = 0;
			while ((line = dr.read()) != null) {
				if (line.length < 2)
					continue;
				if (ln++ % 100000 == 0)
					System.out.print(ln + "\r");
				String url = line[1];
				String mid = line[0];
				url2mid.put(url, mid);
			}
			dr.close();
		}
		{
			DelimitedReader dr = new DelimitedReader(input2);
			int ln = 0;
			while ((line = dr.read()) != null) {
				if (ln++ % 100000 == 0)
					System.out.print(ln + "\r");
				String mid = line[0];
				int zclid = Integer.parseInt(line[1]);
				mid2zclid.put(mid, zclid);
			}
			dr.close();
		}
		{
			DelimitedReader dr = new DelimitedReader(input3);
			while ((line = dr.read()) != null) {
				String arg1 = line[0], arg2 = line[1], rel = line[2], url1 = line[3], url2 = line[4];
				String mid1 = "", mid2 = "";
				int zclid1 = -1, zclid2 = -1;
				try {
					zclid1 = getZclid(url1);
					zclid2 = getZclid(url2);
					mid1 = getMid(url1);
					mid2 = getMid(url2);
				} catch (Exception e) {
					System.err.println(url1 + "\t" + url2);
				} finally {
					if (zclid1 >= 0 && zclid2 >= 0) {
						dw.write(zclid1, zclid2, arg1, arg2, rel, mid1, mid2);
					}
				}
			}
		}
		dw.close();

	}

	
	
	public static String getMid(String url) {
		if (url.startsWith("/en/")) {
			url = url.replace("/en/", "");
			if (url2mid.containsKey(url)) {
				return url2mid.get(url);
			}
		} else if (url.startsWith("/m/")) {
			return url;
		}
		return "";
	}

	public static int getZclid(String url) {
		if (url.startsWith("/en/")) {
			url = url.replace("/en/", "");
			if (url2mid.containsKey(url)) {
				return mid2zclid.get(url2mid.get(url));
			}
		} else if (url.startsWith("/m/")) {
			if (mid2zclid.containsKey(url)) {
				return mid2zclid.get(url);
			}
		}
		return -1;
	}

	public static void main(String[] args) throws IOException {
		//step1();
		step1_neg();
	}
}
