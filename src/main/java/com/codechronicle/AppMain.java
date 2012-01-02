package com.codechronicle;

import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.codechronicle.etrade.EtradeExportFilesProcessor;

import au.com.bytecode.opencsv.CSVReader;

public class AppMain {

	private static Logger log = LoggerFactory.getLogger(AppMain.class);

	public static void main(String[] args) {
		EtradeExportFilesProcessor.execute();
	}
}
