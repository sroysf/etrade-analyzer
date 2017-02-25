package com.codechronicle.etrade;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.TreeMap;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.codechronicle.AppMain;

import au.com.bytecode.opencsv.CSVReader;

public class EtradeExportFilesProcessor {

	private static final String DATA_FILE_DIR = "/home/sroy/tmp/etrade";
	private static final String OUTPUT_FILE_NAME = "aggregatePortfolio.csv";
	private static final String CATEGORIES_FILE_NAME = "categories.props";

	private static Logger log = LoggerFactory.getLogger(EtradeExportFilesProcessor.class);

	private static Map<String,Float> stockInfoMap = new TreeMap<String, Float>();
	private static Map<String,Float> categoryTotalsMap = new TreeMap<String, Float>();
	private static Properties categoryInfo = new Properties();
	
	public static void execute() {
		
		loadCategories();
		
		List<File> files = findFiles(DATA_FILE_DIR);
		for (File file : files) {
			log.info("Processing file : " + file);
			processFile(file);
			
			log.info("Stock totals    : " + stockInfoMap);
			log.info("Category totals : " + categoryTotalsMap);
			log.info("\n");
		}
		
		generateOutputFile();
	}
	
	private static void loadCategories() {

		InputStream inputStream = null;
		try {
	        inputStream = EtradeExportFilesProcessor.class.getClassLoader().getResourceAsStream(CATEGORIES_FILE_NAME);

	        if (inputStream == null) {
	            throw new RuntimeException("Property file '" + CATEGORIES_FILE_NAME
	                + "' not found in the classpath");
	        }

	        categoryInfo.load(inputStream);
	        
		} catch (Exception ex) {
			ex.printStackTrace();
		} finally {
			if (inputStream != null) {
				IOUtils.closeQuietly(inputStream);
			}
		}
	}

	private static void generateOutputFile() {
		
		FileWriter fw = null;
		
		try {
			File dir = new File(DATA_FILE_DIR);
			File outputFile = new File(dir, OUTPUT_FILE_NAME);
			
			fw = new FileWriter(outputFile);
			Set<String> keys = stockInfoMap.keySet();
			
			for (String key : keys) {
				fw.write(String.format("%s,%s,%s\n", key, stockInfoMap.get(key), categoryInfo.getProperty(key)));
			}
			
			fw.write("\n");
			
			// Write rows for categories
			keys = categoryTotalsMap.keySet();
			for (String key : keys) {
				fw.write("\"" + key + "\"" + "," + categoryTotalsMap.get(key) + "\n");
			}
			
			log.info("Wrote output file to : " + outputFile.getAbsolutePath());
			
		} catch (Exception ex) {
			ex.printStackTrace();
		} finally {
			if (fw != null) {
				IOUtils.closeQuietly(fw);
			}
		}
	}

	private static void processFile(File file) {

		FileReader reader = null;

		try {
			reader = new FileReader(file);
			CSVReader csvReader = new CSVReader(reader);
			List<String[]> csvContents = csvReader.readAll();

			boolean processingHeaders = true;
			for (String[] tokens : csvContents) {
				String symbol = tokens[0];

				if (processingHeaders) {
					if (symbol.trim().equalsIgnoreCase("Symbol")) {
						processingHeaders = false;
					}
				} else {
					// Examine the row in detail
					if (!shouldIgnoreRow(symbol)) {
						log.debug(getTokenString(tokens));
						processRow(tokens);
					}
				}
			}

		} catch (Exception ex) {
			ex.printStackTrace();
		} finally {
			IOUtils.closeQuietly(reader);
		}
	}

	private static void processRow(String[] tokens) {
		String stockSymbol = tokens[0];
		
		Float currentTotal = stockInfoMap.get(stockSymbol);
		if (currentTotal == null) {
			currentTotal = 0.0f;
		}
		
		float rowAmount = findLastFloatValue(tokens);
		
		currentTotal += rowAmount;
		
		stockInfoMap.put(stockSymbol, currentTotal);
		
		// Update category totals as well
		String category = categoryInfo.getProperty(stockSymbol);
		if (category == null) {
            log.info("Uncategorized symbol encountered: " + stockSymbol);
			category = "Other";
		}
		
		Float currentCategoryTotal = categoryTotalsMap.get(category);
		if (currentCategoryTotal == null) {
			currentCategoryTotal = 0.0f;
		}
		
		currentCategoryTotal += rowAmount;
		categoryTotalsMap.put(category, currentCategoryTotal);
	}

	private static float findLastFloatValue(String[] tokens) {
		
		for (int i=tokens.length-1; i>=0; i--) {
			String rowAmountString = tokens[i].trim();
			if (rowAmountString.length() > 0) {
				rowAmountString = rowAmountString.replaceAll(",", "");
				float rowAmount = Float.parseFloat(rowAmountString);
				return rowAmount;
			}
		}
		
		throw new RuntimeException("Unable to find total amount from row : " + getTokenString(tokens));
	}

	private static String getTokenString(String[] tokens) {
		
		StringBuilder sbuf = new StringBuilder();
		
		for (String token : tokens) {
			sbuf.append(token + " ");
		}
		
		return sbuf.toString();
	}

	private static boolean shouldIgnoreRow(String symbol) {
		return (symbol.trim().length() == 0) ||
				("N/A".equals(symbol)) ||
				("TOTAL".equals(symbol)) ||
				symbol.startsWith("Generated") ||
				isDate(symbol);
	}

	private static boolean isDate(String symbol) {
		return symbol.matches("[0-9]{2}/[0-9]{2}/[0-9]{4}");
	}

	private static List<File> findFiles(String dataFileDir) {

		List<File> files = new ArrayList<File>();

		File dir = new File(dataFileDir);
		if (!dir.isDirectory() || !dir.exists() || !dir.canRead()) {
			throw new RuntimeException("Cannot read from directory : "
					+ dataFileDir);
		}

		File[] fileListing = dir.listFiles();
		for (File file : fileListing) {
			if (file.isDirectory()) {
				files.addAll(findFiles(file.getAbsolutePath()));
			} else {
				if (file.getName().endsWith("csv") && !(file.getName().equals(OUTPUT_FILE_NAME))) {
					files.add(file);
				}
			}
		}

		return files;
	}
}
