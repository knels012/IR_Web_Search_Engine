import java.io.*;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.*;
import java.util.Scanner;
import java.util.concurrent.*;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

//we need jsoup library for html parsing
//Java.net.URL also useful

public class Crawler{
    
    //argument variables
    private static Path seedPath;
    private static int numPagesToCrawl;
    private static int numLevels;
    private static Path storagePath;
    
    //URL queue. Many threads will access it.
    private static ConcurrentLinkedQueue<String> frontier = new ConcurrentLinkedQueue<String>();
    
    //given a URL, generates a filename
    private static String generateFileName(String url) {
        return "FileName.html";
    }
    
    //saves the contents of a page into a file "filename."
    //returns true on success, false otherwise
    private static boolean saveAsFile(String fileName, String htmlContent){
        return true;
    }
    
    //downloads the page at the specified URL's location
    public static String downloadFile(String url) throws IOException {
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
        
        return htmlContent;
    }
    
    
    
	public static void main(String[] args) {
	    
	    //initializing the variables
	    seedPath = null;
	    numPagesToCrawl = 0;
	    numLevels = 0;
	    storagePath = null;
	    
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
        Scanner seedScanner = null;
        try {
            seedScanner = new Scanner(seedPath);
            while(seedScanner.hasNext()){
                String URL = seedScanner.next();
                frontier.add(URL);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            seedScanner.close();
        }
        
        //main for loop for traversing the queue
	    while(!frontier.isEmpty()){
	        
	        //checks if the URL string begins with a valid protocol. Adds one if it doesn't have one
	        //technically unnecessary because all links that enter should be properly normalized by this point
	        String url = frontier.remove();
	        if(!url.startsWith("http://") && !url.startsWith("https://")){
    	        System.out.println("ERROR: FOUND A URL WITHOUT PROTOCOL! Attemping recovery by prepending protocol");
	            url  = "http://" + url;
	        }
	        
	        try {
	            System.out.println(url);
	            String contents = downloadFile(url);
	            //System.out.println(contents);
            } catch (IOException e) {
                e.printStackTrace();
            }
	    }
	}
}
