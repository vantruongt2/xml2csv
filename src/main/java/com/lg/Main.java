package com.lg;

import java.io.File;
import java.io.FileWriter;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.lang3.StringUtils;
import org.jdom2.Content;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.input.SAXBuilder;
import org.jdom2.util.IteratorIterable;

public class Main {

	@SuppressWarnings("resource")
	public static void main(String[] args) throws Exception {

		String[] HEADERS = { "Component", "Function", "Test Case", "Preconditions", "Number of Preconditions",
				"Step actions", "Number of Step", "Expected Results", "Number of Expected", "Type" };

		FileWriter out = new FileWriter("D:\\pas.csv");
		CSVPrinter printer = new CSVPrinter(out, CSVFormat.DEFAULT.withHeader(HEADERS));

		File inputFile = new File("D:\\test.xml");
		SAXBuilder saxBuilder = new SAXBuilder();
		Document document = saxBuilder.build(inputFile);
		Element root = document.getRootElement();

		List<Element> componentList = root.getChildren("testsuite");

		for (Element component : componentList) {
			String compoName = component.getAttributeValue("name");
			List<Element> functionList = component.getChildren("testsuite");

			for (Element function : functionList) {
				String funcName = function.getAttributeValue("name");
				IteratorIterable<Content> test = function.getDescendants();
				for (Content descendant : test) {
					if (descendant.getCType().equals(Content.CType.Element)) {
						Element element = (Element) descendant;
						if (element.getName().equals("testcase")) {

							String testCase = element.getAttributeValue("name") + " - "
									+ element.getChild("summary").getText().replaceAll("<[^>]*>", "");
							String preCon = element.getChild("preconditions").getText();
							String type = element.getChild("execution_type").getText();
							if (type.equals("1")) {
								type = "Manual";
							} else if (type.equals("2")) {
								type = "Automation";
							} else {
								type = "All";
							}
							String steps = "";
							String expected = "";
							Element st = element.getChild("steps");
							if (st != null) {
								steps = st.getChild("step").getChild("actions").getText();// .replaceAll("<[^>]*>",
																							// "");
								expected = st.getChild("step").getChild("expectedresults").getText();// .replaceAll("<[^>]*>",
																										// "");
							}
							printer.printRecord(compoName, funcName, testCase, removeHtml(preCon), count(preCon),
									removeHtml(steps), count(steps), removeHtml(expected), count(expected), type);

						}
					}
				}

			}
		}
		printer.flush();
	}

	private static int count(String text) {
		text = text.replaceAll("\n\t", "");
		Pattern p = Pattern.compile("<p>[0-9]{1,2}\\.");
		Matcher matcher = p.matcher(text);
		int count = 0;
		while (matcher.find())
			count++;
		if (count == 0) {
			p = Pattern.compile("<div>[0-9]{1,2}\\.");
			matcher = p.matcher(text);
			while (matcher.find())
				count++;
		}
		return count;
	}

	private static String removeHtml(String input) {
		return input.replaceAll("<[^>]*>", "");
	}

}
