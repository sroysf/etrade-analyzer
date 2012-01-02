package com.codechronicle;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.codechronicle.etrade.EtradeExportFilesProcessor;

public class AppMain {

	private static Logger log = LoggerFactory.getLogger(AppMain.class);

	public static void main(String[] args) {
		EtradeExportFilesProcessor.execute();
	}
}
