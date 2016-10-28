import java.io.*;
import java.nio.file.*;
import java.util.Scanner;
import java.util.concurrent.*;

//we need jsoup library for html parsing
//Java.net.URL also useful

public class Crawler {
    
	public static void main(String[] args){
	    
	    //This is our URL queue
	    //Initialize it with a seed and watch it go!
	    ConcurrentLinkedQueue<String> frontier;
	    frontier = new ConcurrentLinkedQueue<String>();
	    
	    //argument variables
	    Path path = null;
	    int numPagesToCrawl = 0;
	    int numLevels = 0;
	    
	    //set the argument variables
	    if(args.length == 3){
            try{
                path = Paths.get(args[0]);
	            numPagesToCrawl = Integer.parseInt(args[1]);
	            numLevels = Integer.parseInt(args[2]);
            }catch(NumberFormatException e){
	            e.printStackTrace();
	        }catch(InvalidPathException e){
	            e.printStackTrace();
	        }

	    }
	    //prints error message if arguments are wrong
	    else{
	        System.out.println("Incorrect arguments passed.\n"
	                + "Arguments are of the form: \n"
	                + "[Seed file path] [# Pages to Crawl] [# of Levels]");
            return;
	    }
	    
	    //prints out the arguments
	    System.out.println("path: " + path);
	    System.out.println("Number of Pages to Crawl: " + numPagesToCrawl);
	    System.out.println("Number of levels: " + numLevels);
	    
	    //initialize the frontier
	    Scanner s = null;
        try {
            s = new Scanner(path);
            while(s.hasNext()){
                String URL = s.next();
                frontier.add(URL);
            }
            s.close();
            
        } catch (IOException e) {
            e.printStackTrace();
        }
        
        //prints the contents of frontier
        System.out.println("\nSeeds:");
	    for(String URL : frontier){
	        System.out.println(URL);
	    }
	}
}
