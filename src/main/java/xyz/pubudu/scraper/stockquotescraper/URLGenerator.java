package xyz.pubudu.scraper.stockquotescraper;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Date;
import java.util.Scanner;

/**
 * Created by pubudu on 2/4/17.
 */
public class URLGenerator {
    private final static String PART1 = "http://content1.edgar-online.com/cfeed/ext/charts.dll?81-0-0-0-0-909301600-03NA000000";
    private final static String PART2 = "&SF:1000-FREQ=1-STOK=";

    public void generate(String symbols, String token) throws IOException {
        File urls = new File("urls-" + new Date(System.currentTimeMillis()));
        File symbolFile = new File(symbols);
        Scanner in = new Scanner(symbolFile);
        FileWriter out = new FileWriter(urls);

        while (in.hasNextLine()) {
            String temp = in.nextLine().toUpperCase();
            out.write(PART1 + temp + PART2 + token + "\n");
        }

        out.flush();
        out.close();
    }
}
