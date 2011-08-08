package javatools.xml;

import javatools.administrative.D;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.NodeList;
import org.w3c.dom.Node;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ReadXMLFile {

	public static void main2(String argv[]) {
		try {
			String dir = "O:/unix/projects/pardosa/s5/clzhang/ACE2005/temp";
			File fXmlFile = new File(dir + "/AFP_ENG_20030304.0250.apf.xml");
			DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
			Document doc = dBuilder.parse(fXmlFile);
			//doc.getDocumentElement().normalize();

			System.out.println("Root element :" + doc.getDocumentElement().getNodeName());
			NodeList nlroot = doc.getChildNodes();
			Element doce = doc.getDocumentElement();
			NodeList nList = doc.getElementsByTagName("relation");
			System.out.println("-----------------------");

			for (int temp = 0; temp < nList.getLength(); temp++) {

				Node nNode = nList.item(temp);
				if (nNode.getNodeType() == Node.ELEMENT_NODE) {

					Element eElement = (Element) nNode;
					NodeList relargs = eElement.getElementsByTagName("relation_argument");

					D.p(eElement.getAttribute("TYPE"), eElement.getAttribute("SUBTYPE"));
					D.p(((Element) relargs.item(0)).getAttribute("REFID"));
					D.p(((Element) relargs.item(1)).getAttribute("REFID"));
					//System.out.println("First Name : " + getTagValue("TYPE", eElement));
					//					System.out.println("Last Name : " + getTagValue("lastname", eElement));
					//					System.out.println("Nick Name : " + getTagValue("nickname", eElement));
					//					System.out.println("Salary : " + getTagValue("salary", eElement));

				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void main(String argv[]) throws Exception {
		String dir = "O:/unix/projects/pardosa/s5/clzhang/ACE2005/temp";
		//File fXmlFile = new File(dir + "/AFP_ENG_20030304.0250.apf.xml");
		parseAce2005Relation(dir + "/AFP_ENG_20030304.0250.apf.xml");
	}

	public static List<String> getChain(Element e, String[] list_nodename, boolean[] isTextNode, int nodename_id) {
		if (nodename_id == 3) {
			D.p(nodename_id);
		}
		List<String> result = new ArrayList<String>();
		NodeList nlist = e.getElementsByTagName(list_nodename[nodename_id]);
		//		if (list_nodename.length - 1 == nodename_id)
		//			return result;
		for (int i = 0; i < nlist.getLength(); i++) {
			Element subn = (Element) nlist.item(i);
			NamedNodeMap nnm = subn.getAttributes();
			StringBuilder sb = new StringBuilder();
			for (int j = 0; j < nnm.getLength(); j++) {
				Node attn = nnm.item(j);
				sb.append(list_nodename[nodename_id] + "::" + attn + ";;");
			}
			if (isTextNode[nodename_id]) {
				String text = subn.getFirstChild().getTextContent();
				sb.append(list_nodename[nodename_id] + "::text=" + text.replaceAll("\\s", " "));
			}

			if (nodename_id + 1 == list_nodename.length) {
				result.add(sb.toString());
			} else {
				List<String> son = getChain(subn, list_nodename, isTextNode, nodename_id + 1);
				for (String x : son) {
					result.add(sb.toString() + "\t" + x);
				}
			}
		}
		return result;
	}

	public static List<String> bfs(Node n) {
		List<String> result = new ArrayList<String>();
		StringBuilder sb = new StringBuilder();
		if (n.getNodeType() == Node.TEXT_NODE) {
			String text = n.getTextContent();

			if (!text.toLowerCase().equals(text.toUpperCase())) {
				sb.append(n.getNodeName() + "::text=" + text.replaceAll("\\s", " "));
			}
		}
		NamedNodeMap nnm = n.getAttributes();
		if (nnm != null) {
			for (int j = 0; j < nnm.getLength(); j++) {
				Node attn = nnm.item(j);
				sb.append(n.getNodeName() + "::" + attn + ";;");
			}
		}

		NodeList nlist = n.getChildNodes();
		for (int i = 0; i < nlist.getLength(); i++) {
			Node subn = nlist.item(i);
			List<String> explain = bfs(subn);
			for (String s : explain) {
				result.add(sb.toString() + "\t" + s);
			}
		}
		if (result.size() == 0) {
			result.add(sb.toString());
		}
		return result;
	}



	private static String getTagValue(String sTag, Element eElement) {
		NodeList nlList = eElement.getElementsByTagName(sTag).item(0).getChildNodes();
		Node nValue = (Node) nlList.item(0);

		return nValue.getNodeValue();
	}

}