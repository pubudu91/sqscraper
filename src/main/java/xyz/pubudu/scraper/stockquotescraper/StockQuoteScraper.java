package xyz.pubudu.scraper.stockquotescraper;

import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.util.Date;
import java.util.Scanner;
import java.util.StringTokenizer;

/**
 * Created by pubudu on 1/28/17.
 */
public class StockQuoteScraper {
    public static final Logger log = LogManager.getLogger(StockQuoteScraper.class);

    public static void main(String[] args) throws IOException {
        switch (args[0]) {
            case "-u":
                new URLGenerator().generate(args[1], args[2]);
                break;
            default:
                new StockQuoteScraper().runScraper(args[0]);
        }
    }

    public void runScraper(String urlsFile) throws IOException {
        CloseableHttpClient httpclient = HttpClients.createDefault();
        File urls = new File(urlsFile);
        Scanner in = new Scanner(urls);

        while (in.hasNextLine()) {
            String url = in.nextLine();
            log.info("Fetching URL: " + url);

            HttpGet httpGet = new HttpGet(url);
            httpGet.setHeader("Referer", "http://content1.edgar-online.com/chartiq/edgr/edgar-chartiq.html");
            httpGet.setHeader("Host", "content1.edgar-online.com");
            httpGet.setHeader("User-Agent", "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/51.0.2704.106 Safari/537.36");
            CloseableHttpResponse response = httpclient.execute(httpGet);
            processResponse(response);

            try {
                Thread.sleep(1000); // To space out the requests
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public static void processResponse(CloseableHttpResponse response) throws IOException {
        try {
            HttpEntity entity = response.getEntity();
            InputStream inputStream = entity.getContent();

            DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
            Document document = documentBuilder.parse(inputStream);

            document.getDocumentElement().normalize();

            NodeList nodeList = document.getElementsByTagName("securityData");
            Element securityNode = (Element) document.getElementsByTagName("security").item(0);
            String symbol = securityNode.getAttribute("symbol");

            log.info("Processing data for symbol: " + symbol);

            String timestamp = new Date(System.currentTimeMillis()).toString().replace(' ', '_');
            File output = new File(symbol + "-" + timestamp + ".csv");
            FileWriter writer = new FileWriter(output);

            for (int i = 0; i < nodeList.getLength(); i++) {
                Element element = (Element) nodeList.item(i);
                String tradeDate = element.getAttribute("tradeDate");
                log.info("Processing data of " + symbol + " for the trading date " + tradeDate);
                String formatted = processString(element.getTextContent().trim(), tradeDate);
                writer.write(formatted);
            }

            writer.flush();
            writer.close();
            log.info("Finished writing the formatted data for " + symbol);

            EntityUtils.consume(entity);
        } catch (ParserConfigurationException e) {
            e.printStackTrace();
        } catch (SAXException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            response.close();
        }
    }

    public static String processString(String data, String tradeDate) {
        StringTokenizer st = new StringTokenizer(data, "~");
        String year = tradeDate.substring(0, 4);
        String month = tradeDate.substring(4, 6);
        String day = tradeDate.substring(6);

        String date = year + "-" + month + "-" + day;

        StringBuilder stringBuilder = new StringBuilder();

        while (st.hasMoreTokens()) {
            String temp = st.nextToken();
            temp = temp.replace('|', ',');
            temp = date + " " + temp + "\n";
            stringBuilder.append(temp);
        }

        return stringBuilder.toString();
    }
}
