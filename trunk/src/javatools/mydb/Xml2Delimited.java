package javatools.mydb;

import java.io.*;
import javax.xml.parsers.*;

import org.w3c.dom.*;
import org.xml.sax.*;

public class Xml2Delimited {
	public static void parse(String file) throws Exception {
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		// Use the factory to create a builder
		DocumentBuilder builder = factory.newDocumentBuilder();
		Document doc = builder.parse(file);
		NodeList list = doc.getElementsByTagName("*");

	}

	static public void main(String[] arg) {

	}

	static public void main2(String[] arg) {
		try {
			BufferedReader bf = new BufferedReader(new InputStreamReader(System.in));
			System.out.print("Enter XML File name: ");
			String xmlFile = bf.readLine();
			File file = new File(xmlFile);
			if (file.exists()) {
				// Create a factory
				DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
				// Use the factory to create a builder
				DocumentBuilder builder = factory.newDocumentBuilder();
				Document doc = builder.parse(xmlFile);
				// Get a list of all elements in the document
				NodeList list = doc.getElementsByTagName("*");
				System.out.println("XML Elements: ");
				for (int i = 0; i < list.getLength(); i++) {
					// Get element
					Element element = (Element) list.item(i);
					System.out.println(element.getNodeName());
				}
			} else {
				System.out.print("File not found!");
			}
		} catch (Exception e) {
			System.exit(1);
		}
	}
}