package freebase.relationmatch;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javatools.administrative.D;
import javatools.filehandlers.DelimitedReader;
import javatools.filehandlers.DelimitedWriter;
import javatools.string.StringUtil;

public class S0_wex {
	/**
	 * Clean the infobox <param name="nat">Australia</param> Into Australia only
	 * */
	public static void cleanWexTemplate_parse() {
		try {
			DelimitedReader dr = new DelimitedReader(Main.file_wex_infobox_value);
			DelimitedReader dr0 = new DelimitedReader(Main.file_wex_infobox_call);
			String[] call = dr0.read();
			DelimitedWriter dw = new DelimitedWriter(Main.file_wex_template_clean);
			String[] l;
			int num = 0;
			while ((l = dr.read()) != null) {
//				if (num++ > 10000)
//					break;
				int cid = Integer.parseInt(l[0]);
				while ((Integer.parseInt(call[1]) < cid && (call = dr0.read()) != null)) {

				}
				if (Integer.parseInt(call[1]) == cid) {
					int wid = Integer.parseInt(call[0]);
					int sectionId = Integer.parseInt(call[2]);
					String templateName = call[3].replace("Template:", "").replaceAll(" ", "_");
					List<String[]> a = cleanString(l[2]);
					for (String[] b : a) {
						if (StringUtil.doesContainLetter(l[1])) {
							dw.write(wid, sectionId, cid, templateName, l[1], b[0], b[1]);
						}
					}
				}
			}
			dr.close();
			dw.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public static void filterInfoboxOnly(){
		try {
			DelimitedReader dr = new DelimitedReader(Main.file_wex_template_clean);
			DelimitedWriter dw = new DelimitedWriter(Main.file_infobox_clean);
			String []l;
			while((l = dr.read())!=null){
				if(l[3].toLowerCase().contains("infobox")){
					dw.write(l);
				}
			}
			dr.close();
			dw.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	static Pattern p = Pattern.compile("\\((.*?)\\)");

	private static List<String[]> cleanString(String a) {
		a = a.replaceAll("<.*?>", "; ");
		String s[] = a.split(";");
		List<String[]> result = new ArrayList<String[]>();
		for (int i = 0; i < s.length; i++) {
			String s0 = s[i].trim();
			if (s0.length() >= 1) {
				Matcher m = p.matcher(s0);
				if (m.find()) {
					String disc = m.group(1);
					String other = s0.replaceAll("\\(.*?\\)", "");
					result.add(new String[] { other, disc });
				} else {
					result.add(new String[] { s0, "" });
				}
			}
		}
		return result;
	}

	public static void main(String[] args) {
		if (Main.s0_cleanWexInfobox) {
			cleanWexTemplate_parse();
			filterInfoboxOnly();
		}
	}
}
