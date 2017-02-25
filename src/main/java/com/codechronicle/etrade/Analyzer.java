package com.codechronicle.etrade;

import au.com.bytecode.opencsv.CSVReader;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by sroy on 2/25/17.
 */
public class Analyzer {
    public static void main(String[] args) throws IOException {

        Map<String,AtomicInteger> totals = new HashMap<String, AtomicInteger>();

        File file = new File("/home/sroy/Desktop/currentAggregatePortfolio.csv");
        FileReader reader = new FileReader(file);
        CSVReader csvReader = new CSVReader(reader);
        List<String[]> csvContents = csvReader.readAll();

        int grandTotal = 0;
        for (String[] csvContent : csvContents) {
            String category = csvContent[2];
            if (category.length() == 0) {
                continue;
            }
            AtomicInteger total = totals.get(category);
            int amount = (int) Float.parseFloat(csvContent[1]);
            if (total == null) {
                total = new AtomicInteger(amount);
                totals.put(category, total);
            } else {
                total.addAndGet(amount);
            }
            grandTotal += amount;
        }

        final int portfolioTotal = grandTotal;
        totals.forEach((category, amount) -> {
            float percent = (float)(amount.get()) * 100.00f / (float)portfolioTotal;
            System.out.printf("%s,%d,%3.2f\n", category, amount.get(), percent);
        });
    }
}
