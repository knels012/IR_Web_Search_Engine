import java.io.*;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.*;
import java.util.Scanner;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

//Java.net.URL also useful

public class Crawler implements Runnable {
    
    //argument variables
    private static Path seedPath;				//the path to the document holding the starting urls
    private static int numPagesToCrawl;
    private static int numLevels;
    private static Path storagePath;
    
    //Thread variables
    private Thread t;
    private String threadName;
    private static AtomicInteger pages;
    
    //URL queue. Many threads will access it.
    private static ConcurrentLinkedQueue<String> frontier = new ConcurrentLinkedQueue<String>();
    //long holding number of created documents, used to generate document names
    long DocNameCount = 0;
    
    Crawler(String name){
        threadName = name;
        System.out.println("Creating " + threadName);
    }
    
    
    //given a URL, generates a filename
    private String generateFileName(String url) {
    	//TODO: make sure no counts can be lost (concurrency issue!)
    	DocNameCount++;
        return DocNameCount + ".html";
    }
    
    //saves the contents of a page into a file "filename."
    //returns true on success, false otherwise
    private boolean saveAsFile(String fileName, String htmlContent){
    	//TODO: implement this method
    	//System.out.println("filename is " + fileName);
        return true;
    }
    
    //downloads the page at the specified URL's location
    private String downloadFile(String url) throws IOException {
        //create the Connection
        Connection connection = Jsoup.connect(url);
        
        //request page with HTTP get
        Document doc = connection.get();
        
        //get the HTML content
        String htmlContent = doc.html();
        
        //saves the page in a file
        if(!saveAsFile(generateFileName(url), htmlContent)){
            System.out.println("error saving document. url: " + url);
        }
        
        //Gets all the links in the page
        //System.out.println(url);
        Elements urlLinks = doc.select("a[href]");
        for(Element e : urlLinks){
            //System.out.println(url + ": " + e.attr("href"));
        	//TODO: add valid urls to frontier
        }
        
        return htmlContent;
    }
    
    
    
	public static void main(String[] args) {
	    
	    //initializing the variables
	    seedPath = null;
	    numPagesToCrawl = 0;
	    numLevels = 0;
	    storagePath = null;
	    //pages = 0;
	    
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
	        numLevels = Integer.parseInt(args[2]);
	        
	        if(args.length ==  4) storagePath = Paths.get(args[3]);
            else storagePath = Paths.get("./crawledPages");
	        
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
        
        //creates Crawlers to be used as threads
	    Crawler[] c = new Crawler[4];
	    for(int i = 0; i < 1; i++){						//currently set to 1 thread
	        c[i] = new Crawler("Thread " + i);
	        //starts running the thread
	        c[i].start();
	    }
	    
	    //loops until frontier is empty
	    //TODO: should run until we have crawled numPagesToCrawl or we crawl all of our levels
	    while(!frontier.isEmpty());
	    
	    System.out.println(frontier.toString());
	}
	
	//This handles the actions of the thread
    @Override
    public void run() {
        /* note: this while loop currently runs until the frontier is empty
         * It should instead run until we have crawled numPagesToCrawl or we crawl all of our levels */
        while(!frontier.isEmpty()){
            
            //url = head of the frontier; pops the head too
            String url = frontier.poll();
            
            //downloads the page located at url if it is a valid link
            if(url != null){
            	//if the url doesn't have a protocol, attempts to fix it
                if(!url.startsWith("http://") && !url.startsWith("https://")){
                    System.out.println("ERROR: URL DOESN'T HAVE PROTOCOL! Attemping recovery by prepending protocol");
                    url  = "http://" + url;
                }
                
                
                try {
                	//TODO: check if url is a valid webpage, and thus add to frontier
                	System.out.println(threadName + ": " + url);
                  	String contents = downloadFile(url);
                  	
                } catch (IOException e) {
                    e.printStackTrace();
                }
                
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
