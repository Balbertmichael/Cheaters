package assignment7;

import java.io.File;
import java.io.FileNotFoundException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.Scanner;
import java.util.regex.Pattern;

public class Cheaters {
	private final Pattern UNWANTED_PUNCTUATION = Pattern.compile("\\p{P}");	// pattern that should be filtered out
	private Hashtable<String, LinkedList<Document>> wordCombos = new Hashtable<String, LinkedList<Document>>();	// hashtable of (word: list of documents it was found in)
	private int[][] sameCombo;
	
	private File folder;			// folder that holds documents (assumes no subdirectories)
	private File[] listOfFiles;	// list of files in the folder
	private int numWords;		// number of words that count as a similarity
	private int miniBound;		// number of similarities that count as dangerous
	
	// DEBUG: time
	private long startTime;
	private long endTime;
	
	/**
	 * Initializes default arguments
	 */
	public Cheaters(){
		// Default parameters: Use when testing
		startTime = System.currentTimeMillis();	// Keeps track of how long program takes to run
		folder = new File(Paths.get("").toAbsolutePath().toString());
		numWords = 6;
		miniBound = 200;
		getFileList();
	}
	
	/**
	 * Initializes user arguments as class variables
	 */
	public Cheaters(String[] args) {
		startTime = System.currentTimeMillis();	// Keeps track of how long program takes to run
		// User input parameters
		// Assumes folder of only text entries and no subdirectories
		folder = new File(args[0]);
		numWords = Integer.parseInt(args[1]);
		miniBound = Integer.parseInt(args[2]);
		getFileList();
	}
	
	/**
	 * Grabs the list of files within the chosen folder
	 */
	private void getFileList() {
		// DEBUG
		System.out.println(folder.getPath());
		System.out.println(numWords);
		
		// Grab list of files in directory
		listOfFiles = folder.listFiles();
		// If files do not exist, return
		if (listOfFiles == null) {
			System.out.println("No files found. Check directory name");
			return;
		}
	}
	
	/**
	 * Scans the files in the subdirectory and populates the hash table
	 * The hash table:
	 * 		key: phrases of <numWords> words that count as a similarity
	 * 		value: linked list of documents that contain that particular phrase similarity
	 */
	private void scanFiles() {
		/**
		 * Scan through files and input into hash table
		 */
		Scanner sc;
		String key;
		
		// Iterate through files and input into hash table
		for (File file : listOfFiles) {
			Document fDocument = new Document(file.getName());
			
			//DEBUG: Check if filename properly grabbed
//			System.out.println("FILE: " + fDocument);

			try {

				// Must rotate through number of words, each time offset by one
				for (int k = 0; k < numWords; ++k) {
					sc = new Scanner(file);
					// sc.useDelimiter(" ");

					while (sc.hasNext()) {

						// Start with offset (On second run, start from second word. On third run, start from third ...)
						for (int i = 0; i < k; ++i) {
							if (!sc.hasNext()) {
								break;
							}
							sc.next();
						}

						// Hash the keys into hash table/map
						// Extract key from file
						key = "";
						for (int i = 0; i < numWords; ++i) {
							if (!sc.hasNext()) {
								break;
							}
							key += sc.next();	// get x number of words
						}
						
						// Clean key (get rid of punctuation/turn to all upper case)
						key = UNWANTED_PUNCTUATION.matcher(key).replaceAll("");
						key = key.toUpperCase();

						// Add key to hash table (key = words : value = list of documents)
						if (wordCombos.containsKey(key)) {	// key already in table
							LinkedList<Document> list = wordCombos.get(key);
							
							// Only add if document is not already there
							if(!list.contains(fDocument)) {
								list.add(fDocument);
							}
							
						} else {	// key not yet in hash table (include key and init list of documents)
							LinkedList<Document> value = new LinkedList<>();
							value.add(fDocument);
							wordCombos.put(key, value);
						}

						// DEBUG: check if key was extracted and altered properly
//						System.out.println(key);
					}
				}

			} catch (FileNotFoundException e) {
				System.out.println("File not found");
				e.printStackTrace();
			}
		}
	}
	
	private void fillSimilarityMatrix() {
		/**
		 * Iterate through hash table and update a similarity array counting documents hashed to the same key
		 */
		// Create an similarity matrix
		sameCombo = new int[Document.counter][Document.counter];
		for (String k : wordCombos.keySet()) {
			LinkedList<Document> match = wordCombos.get(k);
			// For each document in the list, increment array corresponding to doc numbers
			// Doc with smaller ID will be x value, larger ID will be y value
			for (int i = 0; i < match.size(); ++i) {
				for (int j = i + 1; j < match.size(); ++j) {
					Document d1 = match.get(i);
					Document d2 = match.get(j);
					if (d1.equals(d2)) {
						continue;
					}
					int id1 = d1.id;
					int id2 = d2.id;
					if (id1 > id2) {
						int temp = id2;
						id2 = id1;
						id1 = temp;
					}
					sameCombo[id1][id2]++;
				}
			}
		}
	}
	
	private void printSimilarityMatrix() {
		// DEBUG: Print similarity Matrix
		for (int i = 0; i < sameCombo.length; ++i) {
			String lineOut = "";
			for (int j = 0; j < sameCombo.length; ++j) {
				lineOut += sameCombo[i][j] + " ";
			}
			System.out.println(lineOut);
		}
	}
	
	
	private void consoleOutput() {
		// Output documents that have more than the minimum bound of similar words
		for (int i = 0; i < sameCombo.length; ++i) {
			for (int j = i + 1; j < sameCombo.length; ++j) {
				int matchNum = sameCombo[i][j];
				if (matchNum > miniBound) {
					Document d1 = Document.masterList.get(i);
					Document d2 = Document.masterList.get(j);
					System.out.println(d1.name + "," + d2.name + ": " + matchNum);
				}
			}
		}
	}
	
	private void outputRunTime() {
		// Output run time
		long endTime   = System.currentTimeMillis();
		long totalTime = endTime - startTime;
		System.out.println("Total " + totalTime + " ms");
	}
	
	/**
	 * Given a directory of essays, will determine similarities between essays
	 * 
	 * @param args
	 *            [0] path to file, [1] number of words
	 */
	public static void main(String[] args) {
		Cheaters cheaters;
		if(args.length < 3) {
			cheaters = new Cheaters();
		}
		else {
			cheaters = new Cheaters(args);
		}
		
		cheaters.scanFiles();
		cheaters.fillSimilarityMatrix();
		
		// DEBUG
//		cheaters.printSimilarityMatrix();
		
		cheaters.consoleOutput();
		cheaters.outputRunTime();
	}
}

/**
 * Document class: Helper Class that represents a file in the directory
 * Each file will have an ID that will help map it to the similarity array
 * The document title are also kept
 * Holds both ID and document title
 */
class Document {
	public int id;
	public String name;
	public static int counter = 0;
	public static ArrayList<Document> masterList = new ArrayList<Document>();

	public Document(String name) {
		id = counter++;
		this.name = name;
		masterList.add(this);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + id;
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Document other = (Document) obj;
		if (id != other.id)
			return false;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "ID: " + id + ", Name: " + name;
	}
}
