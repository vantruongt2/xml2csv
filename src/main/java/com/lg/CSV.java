package com.lg;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.csv.CSVRecord;

public class CSV {

	public static void main(String[] args) throws IOException {
		// String name = "DA RTS automation cases-2019-4-15_Audit Manager.csv";
		String path = "C:\\Users\\truong.pham\\Downloads\\PartnerInfo\\Infrastructure\\Testcases\\Infra_Testcases-1\\ZPA\\";
		String convertFile = "Done\\1.Converted.csv";

		String[] HEADERS = { "Automated", "Test Case", "Test Importance", "Preconditions", "Number of Preconditions",
				"Step actions", "Number of Step", "Expected Results", "Number of Expected" };

		FileWriter out = new FileWriter(path + convertFile);
		CSVPrinter printer = new CSVPrinter(out, CSVFormat.DEFAULT.withHeader(HEADERS));

		File folder = new File(path);
		File[] listOfFiles = folder.listFiles();

		// Each file in folder
		for (int i = 0; i < listOfFiles.length; i++) {
			if (listOfFiles[i].isFile()) {
				String name = listOfFiles[i].getAbsolutePath();
				try {
					System.out.println("File " + name);
					Reader reader = Files.newBufferedReader(Paths.get(name), StandardCharsets.ISO_8859_1);
					CSVParser csvParser = new CSVParser(reader, CSVFormat.DEFAULT);

					int start = 0;
					for (CSVRecord csvRecord : csvParser) {
						// Accessing Values by Column Index
						String summary = csvRecord.get(2);
						String pr = csvRecord.get(3);
						String preconditions = csvRecord.get(4);
						String steps = csvRecord.get(5);
						String expected = csvRecord.get(6);
						String automated = csvRecord.get(11);
						if ("test case steps".equals(steps.toLowerCase().trim()) && start == 0) {
							start = 1;
							continue;
						}
						if (start == 1) {
							if (!steps.isEmpty()) {
								printer.printRecord(automated, summary, pr, preconditions,
										countLines(preconditions), steps, countLines(steps), expected,
										countLines(expected));
							}
						}
					}
					reader.close();
					Files.move(Paths.get(name), Paths.get(path + "Done\\" + listOfFiles[i].getName()));

				} catch (Exception e) {
					System.out.println("Error with file: " + listOfFiles[i].getName());
					e.printStackTrace();
				}
			}
		}

		printer.flush();

	}

	private static int countLines(String str) {
		int count = 0;
		if (!str.isEmpty()) {
			String[] lines = str.split("\r\n|\r|\n");

			for (String s : lines) {
				s = s.trim();
				Pattern p = Pattern.compile("^[0-9]{1,2}\\.");
				Matcher matcher = p.matcher(s);
				if (matcher.find()) {
					count++;
				}
			}
			count = count == 0 ? 1 : count;
		}
		return count;
	}

}
