package com.lg;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collector;
import java.util.stream.Collectors;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.csv.CSVRecord;

import info.debatty.java.stringsimilarity.JaroWinkler;

public class CSVDup {
	static double SIM = 0.9;

	public static void main(String[] args) throws IOException {

		String convertedFile = "1.Converted.csv";

		// String name = "DA RTS automation cases-2019-4-15_Audit Manager.csv";
		String path = "C:\\Users\\truong.pham\\Downloads\\PartnerInfo\\Infrastructure\\Testcases\\Infra_Testcases-1\\DA\\Done\\";
		String convertFile = "12.Converted.csv";

		String[] HEADERS = { "Automated", "Test Case", "Test Importance", "Preconditions", "Number of Preconditions",
				"Step actions", "Number of Step", "Dup Step","Dup Step Detail","Expected Results", "Number of Expected" };

		FileWriter out = new FileWriter(path + convertFile);
		CSVPrinter printer = new CSVPrinter(out, CSVFormat.DEFAULT.withHeader(HEADERS));

		File folder = new File(path);
		File[] listOfFiles = folder.listFiles();

		List<String> listOfSteps = new ArrayList<>();

		List<String> listOfDupSteps = new ArrayList<>();

		Map<Integer, List<String>> maps = new HashMap<Integer, List<String>>();

		// Each file in folder
		// for (int i = 0; i < listOfFiles.length; i++) {
		// if (listOfFiles[i].isFile()) {
		String name = path + convertedFile;
		try {
			System.out.println("File " + name);
			Reader reader = Files.newBufferedReader(Paths.get(name), StandardCharsets.ISO_8859_1);
			CSVParser csvParser = new CSVParser(reader, CSVFormat.DEFAULT);
			List<String> lstStep;
			int start = 0;
			for (CSVRecord csvRecord : csvParser) {

				if (start == 0) {
					start++;
					continue;

				}

				// Accessing Values by Column Index
				lstStep = new ArrayList<>();
				String steps = csvRecord.get(5);

				String[] lines = steps.split("\r\n|\r|\n");
				for (String s : lines) {
					s = s.trim();
					Pattern p = Pattern.compile("^[0-9]{1,2}\\.");
					Matcher matcher = p.matcher(s);
					if (matcher.find()) {
						s = s.replaceAll("^[0-9]{1,2}\\.", "").trim();
						lstStep.add(s);
					}

				}
				/*
				 * String expected = csvRecord.get(6); String automated = csvRecord.get(11); if
				 * ("test case steps".equals(steps.toLowerCase().trim()) && start == 0) { start
				 * = 1; continue; } if (start == 1) { if (!steps.isEmpty()) {
				 * //printer.printRecord(automated, summary, pr, preconditions,
				 * countLines(preconditions), steps, //countLines(steps), expected,
				 * countLines(expected)); } }
				 */
				maps.put(start, lstStep);
				start++;

			}
			reader.close();

			reader = Files.newBufferedReader(Paths.get(name), StandardCharsets.ISO_8859_1);
			csvParser = new CSVParser(reader, CSVFormat.DEFAULT);
			// Check duplication step
			start = 0;			
			for (CSVRecord csvRecord : csvParser) {
				// Accessing Values by Column Index
				if (start == 0) {
					start++;
					continue;

				}
				// maps.remove(start);

				System.out.println(csvRecord.getRecordNumber());
				String automated = csvRecord.get(0);
				String testcase = csvRecord.get(1);
				String pr = csvRecord.get(2);
				String preconditions = csvRecord.get(3);
				String numpreconditions = csvRecord.get(3);
				String steps = csvRecord.get(5);
				String numsteps = csvRecord.get(6);
				String ex = csvRecord.get(7);
				String numex = csvRecord.get(8);

				int numberOfDupStep = 0;
				StringBuffer bu = new StringBuffer();
				listOfSteps = getList(maps, start);
				String[] lines = steps.split("\r\n|\r|\n");
				for (String s : lines) {
					s = s.trim();
					Pattern p = Pattern.compile("^[0-9]{1,2}\\.");
					Matcher matcher = p.matcher(s);
					if (matcher.find()) {
						s = s.replaceAll("^[0-9]{1,2}\\.", "").trim();
						for (int i = 0; i < listOfSteps.size(); i++) {
							if (match(s, listOfSteps.get(i)) && !s.toLowerCase().contains("repeat")) {							
								numberOfDupStep++;
								//System.out.println(s);
								bu.append(numberOfDupStep +". "+ s).append("\n");
								break;
							}

						}
					}

				}				
				//System.out.println("Number of duplication steps of TC " + csvRecord.get(1) + ": " + numberOfDupStep);
				/*
				 * String expected = csvRecord.get(6); String automated = csvRecord.get(11); if
				 * ("test case steps".equals(steps.toLowerCase().trim()) && start == 0) { start
				 * = 1; continue; } if (start == 1) { if (!steps.isEmpty()) {
				 * //printer.printRecord(automated, summary, pr, preconditions,
				 * countLines(preconditions), steps, //countLines(steps), expected,
				 * countLines(expected)); } }
				 */

				printer.printRecord(automated, testcase, pr, preconditions, numpreconditions, steps, numsteps,
						numberOfDupStep,bu.toString(), ex, numex);
				start++;
			}

			System.out.println(start);
			System.out.println(listOfSteps.size());
			reader.close();
			printer.flush();
			printer.close();
			// Files.move(Paths.get(name), Paths.get(path + "Done\\" +
			// listOfFiles[i].getName()));

		} catch (Exception e) {
			// System.out.println("Error with file: " + listOfFiles[i].getName());
			e.printStackTrace();
		}
		// }
		// }

		// printer.flush();

	}

	public static List<String> getList(Map<Integer, List<String>> maps, int end) {
		List<String> re = new ArrayList<>();
		for (int i = 1; i < end; i++) {
			re.addAll(maps.get(i));
		}
		return re;
	}

	public static boolean checkdup(List<String> b, String s) {
		List<String> res = b.stream().parallel().filter(x -> match(x, s)).collect(Collectors.toList());
		return res.size() > 0;
	}

	public static boolean match(String a, String b) {
		JaroWinkler jaroWinkler = new JaroWinkler();
		return jaroWinkler.similarity(a, b) >= SIM;
	}

}
