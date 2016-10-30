import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.*;
import java.util.Scanner;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.Semaphore;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;


public class Crawler implements Runnable {
    
    //argument variables
    private static Path seedPath;				//the path to the document holding the starting URLs
    private static int numPagesToCrawl;   //carries permits equal to the number of pages we will crawl
    private static int numLevels;               //how many levels deep we will crawl
    private static Path storagePath;            //path to where we store the html files
    
    //Thread variables
    private Thread t;                               //the Crawler object's thread
    private String threadName;                      //name of the thread
    private static AtomicInteger pagesCrawled;      //# of pages we have crawled
    private static Semaphore pagesLeft;             //number of pages left to crawl
    //private static AtomicInteger levelsCrawled;     //# of levels we have crawled
    private static int numThreads;                  //number of threads to create
    
    //URL queue. Many threads will access it.
    private static ConcurrentLinkedQueue<String> frontier = new ConcurrentLinkedQueue<String>();
    //long holding number of created documents, used to generate document names
    private static AtomicLong docCount;
    //used to hold all url-document mappings
    private static String url_doc_map = " ";
    //lock used with url_doc_map
    private static final Object lock = new Object();
    
    //Crawler constructor
    Crawler(String name){
        threadName = name;
        System.out.println("Creating " + threadName);
    }
    
    //Uses a base URL to normalize the given URL. Also cleans the URL of useless things.
    private String normalizeURL(String base, String url) throws MalformedURLException{
        URL context = new URL(base);
        URL normalizedURL = new URL(context, url);
        
        String protocol = normalizedURL.getProtocol();
        String host = normalizedURL.getHost();
        String path = normalizedURL.getPath();
        
        return protocol + "://" + host + path;
    }
    
    //checks if we have already crawled this URL
    private boolean isDuplicate(String url){
        //TODO: check if we have already crawled this URL
        return false;
    }
    
    //Returns false if the URL is not valid (i.e. we dont want it in the frontier)
    private boolean isValidURL(String url){
        if(url.startsWith("http://") && !isDuplicate(url)){
            return true;
        }
        return false;
    }
    
    //given a URL, generates a filename
    private String generateFileName(String url) {
        return docCount.incrementAndGet() + ".html";
    }
    
    //saves the contents of a page into a file "filename." Uses the storangePath variable
    //returns true on success, false otherwise
    private boolean saveAsFile(String fileName, String htmlContent){
    	//System.out.println("filename is " + fileName);
    	try{
    	    PrintWriter writer = new PrintWriter(storagePath + "/"+ fileName);
    	    writer.println(htmlContent);
    	    writer.close();
    	} catch (FileNotFoundException e) {
    		e.printStackTrace();
    		return false;
    	}
        return true;
    }

    ///*
    private static boolean writeMapTxt() {
    	String[] curr;
    	synchronized (lock) {
    		curr = url_doc_map.split(" ");
    		url_doc_map = "";
    	}
    	//TODO: create txt file, add strings two by two for lines
    	//error checking if curr has odd number length or is empty
    	/*
    	System.out.println("==================");	
    	for (int i = 1; i < curr.length - 1; i = i+2) {
    		System.out.println(curr[i] + " " + curr[i+1]);
    	}
    	System.out.println("==================");
    	*/
    	///*
    	try{
    	    PrintWriter writer = new PrintWriter(storagePath + "/"+ "A_url_doc_map" + docCount.get() + ".txt");
    	    for (int i = 1; i < curr.length - 1; i = i+2) {
    	    	writer.println(curr[i] + " " + curr[i+1]);
        	}
    	    writer.close();
    	} catch (Exception e) {
    		System.out.println("writeMapTxt: Failed to save file");
    		return false;
    	}//*/
    	return true;
    }
	//*/

    //downloads the page at the specified URL's location
    //returns true on success
    private boolean downloadFile(String url) throws IOException {
        boolean success = false;

        //create the Connection
        Connection connection = Jsoup.connect(url);
        
        //request page with HTTP get
        Document doc = connection.get();
        
        //get the HTML content
        String htmlContent = doc.html();
        String FileName = generateFileName(url);
        
        //saves the page in a file
        if(success = saveAsFile(FileName, htmlContent)){
            //succeeded in saving html file, now add to url-doc_map string list
            synchronized (lock) {
                url_doc_map = url_doc_map + url + " " + FileName + " ";
            }
            //System.out.println("--" + threadName + ": " + url_doc_map);
        }
        else {
            System.out.println("error saving document. url: " + url);
        }
        
        //Gets all the links in the page
        Elements urlLinks = doc.select("a[href]");
        for(Element e : urlLinks){
            String hrefURL = e.attr("href");
            String normalizedURL = normalizeURL(url, hrefURL);
            if(isValidURL(normalizedURL)){
                //System.out.println(normalizedURL);
                //TODO: add URLs to frontier
                try{
                    frontier.add(normalizedURL);
                } catch(NullPointerException ex){
                    ex.printStackTrace();
                }
            }
        }
        
        return success;
    }
    
	public static void main(String[] args) {
	    
	    //initializing the variables
	    seedPath = null;
	    numPagesToCrawl = 0;
	    numLevels = 0;
	    storagePath = null;
	    docCount = new AtomicLong(0);
	    pagesCrawled = new AtomicInteger(0);
	    
	    //prints error message if arguments are wrong then exits
	    if(args.length < 3 || args.length > 4){
	        System.out.println("Incorrect arguments passed. Arguments are of the form: \n"
                    + "[Seed file path] [# Pages to Crawl] [# of Levels][optional: page storage path]");
            return;
	    }
	    
	    //sets the variables to the arguments
        try {
            seedPath = Paths.get(args[0]);
            numPagesToCrawl = Integer.parseInt(args[1]);
	        pagesLeft = new Semaphore(numPagesToCrawl);
	        numLevels = Integer.parseInt(args[2]);
	        
	        if(args.length ==  4) storagePath = Paths.get(args[3]);
            else storagePath = Paths.get("./crawledPages");
	        //TODO: remove any '/' at end
	        
	        //Creates a folder to store crawled pages
	        File dir = storagePath.toFile();
            if(dir.mkdirs()) System.out.println("Storage folder successfully created");
            else if(!dir.exists()){
                System.out.println("Storage folder creation failed. Exiting...");
                return;
            }
        }catch(NumberFormatException e){
	        System.out.println("Are arg[1] and arg[2] numbers?");
            e.printStackTrace();
	    }catch(InvalidPathException e){
	        System.out.println("Invalid path");
	        e.printStackTrace();
	    }
        
	    ////prints out the variables
	    //System.out.println("Seed File: " + seedPath);
	    //System.out.println("Number of Pages to Crawl: " + numPagesToCrawl);
	    //System.out.println("Number of levels: " + numLevels);
	    //System.out.println("Storage Path: " + storagePath);
	    
	    //initialize the frontier
        //frontier is a global queue
        Scanner seedScanner = null;
        try {
            seedScanner = new Scanner(seedPath);
            //gets all lines from the document specified in seedPath
            //enters each line into frontier queue
            while(seedScanner.hasNext()){
                String URL = seedScanner.next();
                frontier.add(URL);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            seedScanner.close();
        }
        
        //creates Crawlers to be used as threads then runs them
        numThreads = 16;
	    Crawler[] c = new Crawler[numThreads];
	    for(int i = 0; i < numThreads; i++){						//currently set to 4 thread
	        c[i] = new Crawler("Thread " + i);
	        c[i].start();
	    }
	    
	    //Writes url-doc maps into a file once DocName Count reaches required amount
	    if (!url_doc_map.isEmpty() && docCount.get() == numPagesToCrawl) {
	    	writeMapTxt();
	    	System.out.println("Finished Crawler");
    	}
	}
	
	//This handles the actions of the thread
    @Override
    public void run() {
        while(pagesCrawled.get() < numPagesToCrawl){
            if(pagesLeft.tryAcquire()){
                //url = head of the frontier; pops the head too
                String url = frontier.poll();
                
                if(url != null) {
                    //if the URL doesn't have a protocol, attempts to fix it
                    if(!url.startsWith("http://") && !url.startsWith("https://")){
                        System.out.println("ERROR: URL DOESN'T HAVE PROTOCOL! Attemping recovery by prepending protocol");
                        url  = "http://" + url;
                    }
                    
                    //downloads the URL
                    try {
                        //System.out.println(threadName + ": " + url);
                        if(pagesCrawled.get() < numPagesToCrawl){
                            boolean success = downloadFile(url);
                            if(success){
                                //keeps track of how many pages we have crawled
                                int p = pagesCrawled.incrementAndGet();
                                System.out.println("Pages Crawled: " + p);
                            }
                            else pagesLeft.release(); //downloadFile failed. Release permit
                        }
                    } catch (IOException e) {
                        //downloadFile threw an IOexception so we must release a permit
                        pagesLeft.release();
                        e.printStackTrace();
                    }
                    
                }
                //releases the permit since it did not "use" it to download a URL
                else pagesLeft.release();
            }
        }
        return;
    }
    
    //call this to start a thread
    public void start() {
        System.out.println("Starting " + threadName);
        if(t == null) {
            t = new Thread(this, threadName);
            t.start();
        }
    }
}
