package javatools.xml;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import javatools.administrative.D;
import javatools.filehandlers.DelimitedWriter;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

public class ParseACE2005 {

	public static void main(String[] args) throws Exception {
		//collectFile();
		String outputdir = "/projects/pardosa/s5/clzhang/ACE2005/congleparse";
		String outputfile = "/projects/pardosa/s5/clzhang/ACE2005/relation_parsebycongle0803";
		parseACE2005RelationDir(outputdir, outputfile);
	}

	public static void collectFile() {
		String dir = "/projects/pardosa/s5/clzhang/ACE2005/data/English";
		String outputdir = "/projects/pardosa/s5/clzhang/ACE2005/congleparse";
		(new File(outputdir)).deleteOnExit();
		String[] level1 = new String[] { "bc", "bn", "cts", "nw", "un", "wl" };
		//String[] level2 = new String[] { "fp1", "fp2", "adj", "timex2norm" };
		String[] level2 = new String[] { "fp1" };
		int totalXml = 0;
		for (int i = 0; i < level1.length; i++) {
			for (int j = 0; j < level2.length; j++) {
				String subdirstr = dir + "/" + level1[i] + "/" + level2[j];
				File subdir = new File(subdirstr);
				D.p(subdirstr, subdir.list().length);
				for (String f0 : subdir.list()) {
					String file_apfxml = subdir + "/" + f0;
					if (file_apfxml.endsWith("apf.xml")) {
						//String outputfile = outputdir + "/" + f0 + "_" + level1[i] + "_" + level2[j];
						String outputfile = outputdir + "/" + f0;
						copyfile(file_apfxml, outputfile);
						totalXml++;
					}
				}
			}
		}
		D.p(totalXml);

	}

	private static void copyfile(String srFile, String dtFile) {
		try {
			File f1 = new File(srFile);
			File f2 = new File(dtFile);
			InputStream in = new FileInputStream(f1);

			//For Append the file.
			//  OutputStream out = new FileOutputStream(f2,true);

			//For Overwrite the file.
			OutputStream out = new FileOutputStream(f2);

			byte[] buf = new byte[1024];
			int len;
			while ((len = in.read(buf)) > 0) {
				out.write(buf, 0, len);
			}
			in.close();
			out.close();
			System.out.println("File copied.");
		} catch (FileNotFoundException ex) {
			System.out.println(ex.getMessage() + " in the specified directory.");
			System.exit(0);
		} catch (IOException e) {
			System.out.println(e.getMessage());
		}
	}

	public static void parseACE2005RelationDir(String dir, String output) throws Exception {
		File file_dir = new File(dir);
		DelimitedWriter dw = new DelimitedWriter(output);
		for (String file : file_dir.list()) {
			if (file.endsWith("xml")) {
				parseAce2005Relation(dir + "/" + file, dw);
			}
		}
		dw.close();
	}

	public static void parseAce2005Relation(String file, DelimitedWriter dw) throws Exception {
		File fXmlFile = new File(file);
		DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
		Document doc = dBuilder.parse(fXmlFile);
		Element roote = doc.getDocumentElement();
		//List<String[]> result = new ArrayList<String[]>();
		NodeList rl = roote.getElementsByTagName("relation");
		for (int i = 0; i < rl.getLength(); i++) {
			List<String> instance = new ArrayList<String>();
			Element element = (Element) rl.item(i);
			String idstr = element.getAttribute("ID");
			String[] ab = idstr.split("-");
			instance.add(ab[0]);
			instance.add(ab[1]);
			//addAttributeToInstance(instance,element,"ID");

			addAttributeToInstance(instance, element, "TYPE");
			addAttributeToInstance(instance, element, "SUBTYPE");
			NodeList nl_rm = element.getElementsByTagName("relation_mention");
			for (int rmi = 0; rmi < nl_rm.getLength(); rmi++) {
				Element e_rm = (Element)nl_rm.item(rmi);
				List<String>rminstance = new ArrayList<String>();
				rminstance.add(e_rm.getAttribute("LEXICALCONDITION"));
				NodeList nl_charseq = e_rm.getElementsByTagName("charseq");
				for (int k = 0; k < nl_charseq.getLength(); k++) {
					Element e_charseq = (Element) nl_charseq.item(k);
					addAttributeToInstance(rminstance, e_charseq, "START");
					addAttributeToInstance(rminstance, e_charseq, "END");
					rminstance.add(e_charseq.getTextContent().replaceAll("\\s", " "));
				}
				List<String>towrite = new ArrayList<String>();
				towrite.addAll(instance);
				towrite.addAll(rminstance);
				dw.write(towrite.toArray());
			}

		}
	}

	private static void addAttributeToInstance(List<String> instance, Element element, String attname) {
		if (element.hasAttribute(attname)) {
			instance.add(element.getAttribute(attname));
		}
	}
}
