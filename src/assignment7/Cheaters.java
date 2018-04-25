package assignment7;

import java.io.File;
import java.io.FileNotFoundException;
import java.nio.file.Paths;
import java.util.Scanner;

public class Cheaters {

	
	
	/**
	 * Given a directory of essays, will determine similarities between essays 
	 * @param args [0] path to file, [1] number of words
	 */	
	public static void main(String[] args) {
		File folder;
		File[] listOfFiles;
		String folderName;
		int numWords;
		int limit;
		
		// Get folder with files
		if(args.length < 3) {
			// Default parameters: Use when testing 
			folderName = Paths.get("").toAbsolutePath().toString();
			folder = new File(folderName);
			numWords = 6;
			limit = 200;
		} else {
			// User input parameters
			// Assumes folder of only text entries and no subdirectories
			folder = new File(args[0]);
			numWords = Integer.parseInt(args[1]);
			limit = Integer.parseInt(args[2]);
		}
		
		// DEBUG
		System.out.println(folder.getPath());
		System.out.println(numWords);
		
		// Grab list of files in directory
		listOfFiles = folder.listFiles();
		// If files do not exist, return
		if(listOfFiles == null) {
			System.out.println("No files found. Check directory name");
			return;
		}
		
		Scanner sc;
		String key;
		// Iterate through files
		for(File file: listOfFiles) {
			System.out.println("FILE: " + file.getName());
			
			try {
				
				// Must rotate through number of words, each time offset by one
				for(int k = 0; k < numWords; ++k) {
					System.out.println("=========================================");
					System.out.println("=========================================");
					System.out.println("=========================================");
					sc  = new Scanner(file);
					sc.useDelimiter(" ");
					
					while(sc.hasNext()) {
						
						// On second run, start from second word. On third run, start from third ... 
						for(int i = 0; i < k; ++i) {
							if(!sc.hasNext()) {
								break;
							}
							sc.next();
						}
						
						// Hash the keys into hashtable/map
						key = "";
						for(int i = 0; i < numWords; ++i) {
							if(!sc.hasNext()){
								break;
							}
							key += sc.next();
						}
						System.out.println(key);
					}
				}

			} catch (FileNotFoundException e) {
				System.out.println("File not found");
				e.printStackTrace();
			}
		}

		
	}
}
