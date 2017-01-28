# Stock Quote Scraper
A simple stock quote scraper for scraping intraday stock price updates. 

To build:  ```$ mvn clean install```

To scrape data, either copy the jar file to the folder where you want to save the scraped data or run the scraper from within that folder. Provide a file containing the URLs for the XML files containing the data of the desired NASDAQ symbols. 
```
$ java -jar stock-quote-scraper-1.0.jar <path-to-url-file>
```
