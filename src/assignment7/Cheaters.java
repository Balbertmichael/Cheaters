package assignment7;

import java.io.File;
import java.io.FileNotFoundException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Scanner;
import java.util.regex.Pattern;

public class Cheaters {
	private ArrayList<SuspectPair> suspectPairs = new ArrayList<SuspectPair>();
	private ArrayList<Document> suspectDocs = new ArrayList<Document>();
	
	private final Pattern UNWANTED_PUNCTUATION = Pattern.compile("\\p{P}"); // pattern that should be filtered out
	private Hashtable<String, List<Document>> wordCombos = new Hashtable<String, List<Document>>();
	// Hash Table of (word: list of documents it was found in)
	private int[][] sameCombo;
	
	private File folder; // folder that holds documents (assumes no sub-directories)
	private File[] listOfFiles; // list of files in the folder
	private Iterator<File> listIter;
	private Iterator<String> keySet;
	private int numWords; // number of words that count as a similarity
	private int miniBound; // number of similarities that count as dangerous
	private int cores = Runtime.getRuntime().availableProcessors();

	private Object o = new Object();

	// DEBUG: time
	private long startTime;
	private long endTime;

	/**
	 * Initializes default arguments
	 */
	public Cheaters() {
		// Default parameters: Use when testing
		startTime = System.currentTimeMillis(); // Keeps track of how long program takes to run
		folder = new File(Paths.get("").toAbsolutePath().toString());
		numWords = 6;
		miniBound = 200;
		getFileList();
	}

	/**
	 * Initializes user arguments as class variables
	 */
	public Cheaters(String[] args) {
		startTime = System.currentTimeMillis(); // Keeps track of how long program takes to run
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
	private synchronized void getFileList() {
		// DEBUG
		// System.out.println(folder.getPath());
		// System.out.println(numWords);

		// Grab list of files in directory
		listOfFiles = folder.listFiles();
		listIter = Arrays.asList(listOfFiles).iterator();
		// If files do not exist, return
		if (listOfFiles == null) {
			System.out.println("No files found. Check directory name");
			return;
		}
	}

	private synchronized File getFromFileList() {
		if (listIter.hasNext()) {
			return listIter.next();
		} else {
			return null;
		}
	}

	/**
	 * Scans the files in the subdirectory and populates the hash table The hash
	 * table: key: phrases of <numWords> words that count as a similarity value:
	 * linked list of documents that contain that particular phrase similarity
	 */
	private void scanFiles() {
		// Iterate through files and input into hash table
		for (File file : listOfFiles) {
			populateHashTable(file);
		}
	}

	private void populateHashTable(File file) {
		Scanner sc;
		String key;
		Document fDocument;
		synchronized (o) {
			fDocument = new Document(file.getName());
		}

		// DEBUG: Check if filename properly grabbed
		// System.out.println("FILE: " + fDocument);

		try {

			// Must rotate through number of words, each time offset by one
			for (int k = 0; k < numWords; ++k) {
				sc = new Scanner(file);
				// sc.useDelimiter(" ");

				while (sc.hasNext()) {

					// Start with offset (On second run, start from second word. On third run, start
					// from third ...)
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
						key += sc.next(); // get x number of words
					}

					// Clean key (get rid of punctuation/turn to all upper case)
					key = UNWANTED_PUNCTUATION.matcher(key).replaceAll("");
					key = key.toUpperCase();

					// Add key to hash table (key = words : value = list of documents)
					if (wordCombos.containsKey(key)) { // key already in table
						List<Document> list = wordCombos.get(key);

						// Only add if document is not already there
						if (!list.contains(fDocument)) {
							list.add(fDocument);
						}

					} else { // key not yet in hash table (include key and init list of documents)
						List<Document> value = Collections.synchronizedList(new LinkedList<Document>());
						value.add(fDocument);
						wordCombos.put(key, value);
					}

					// DEBUG: check if key was extracted and altered properly
					// System.out.println(key);
				}
			}

		} catch (FileNotFoundException e) {
			System.out.println("File not found");
			e.printStackTrace();
		}
	}

	/**
	 * Creates a m x m matrix (where m = number of documents) that keeps track of
	 * the number of similarities between two docs Iterates through hash table and
	 * update a similarity array counting documents hashed to the same key
	 */
	private void fillSimilarityMatrix() {
		// Create an similarity matrix
		sameCombo = new int[Document.getCounter()][Document.getCounter()];
		keySet = wordCombos.keySet().iterator();

		// DEBUG:
		cores = 0;

		if (cores > 1) {
			Thread[] processThreads = new Thread[cores];
			for (int i = 0; i < cores; ++i) {
				processThreads[i] = new Thread(new MapIterate());
				processThreads[i].start();
			}
			for (int i = 0; i < cores; ++i) {
				try {
					processThreads[i].join();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		} else {
			for (String k : wordCombos.keySet()) {
				List<Document> match = wordCombos.get(k);
				processSimilarityMatrix(match);
			}
		}
	}

	private synchronized List<Document> getListFromKeySet() {
		if (keySet.hasNext()) {
			return wordCombos.get(keySet.next());
		} else {
			return null;
		}
	}

	private void processSimilarityMatrix(List<Document> match) {
		// For each document in the list, increment array corresponding to doc numbers
		// Doc with smaller ID will be x value, larger ID will be y value
		for (int i = 0; i < match.size(); ++i) {
			for (int j = i + 1; j < match.size(); ++j) {
				Document d1 = match.get(i);
				Document d2 = match.get(j);
				if (d1.equals(d2)) {
					continue;
				}
				int id1 = d1.getId();
				int id2 = d2.getId();
				if (id1 > id2) {
					int temp = id2;
					id2 = id1;
					id1 = temp;
				}
				sameCombo[id1][id2]++;
			}
		}
	}

	/**
	 * Use for Debugging: Prints array
	 */
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

	/**
	 * Outputs the documents that have more similarities than the min bound
	 */
	private void consoleOutput() {
		// Output documents that have more than the minimum bound of similar words
		for (int i = 0; i < sameCombo.length; ++i) {
			for (int j = i + 1; j < sameCombo.length; ++j) {
				int matchNum = sameCombo[i][j];
				if (matchNum > miniBound) {
					Document d1 = Document.getMasterList().get(i);
					Document d2 = Document.getMasterList().get(j);
					System.out.println(d1.getId() + "," + d2.getId() + ": " + matchNum);
				}
			}
		}
	}

	/**
	 * Creates a list of suspicious pairs of documents Meant to be used by view to
	 * create a graphic representation
	 * 
	 * @param bound
	 * @return an array list of suspicious pairs of documents
	 */
	private ArrayList<SuspectPair> createList(int bound) {

		for (int i = 0; i < sameCombo.length; ++i) {
			for (int j = i + 1; j < sameCombo.length; ++j) {
				int matchNum = sameCombo[i][j];
				if (matchNum > bound) {
					Document d1 = Document.getMasterList().get(i);
					Document d2 = Document.getMasterList().get(j);
					suspectPairs.add(new SuspectPair(d1, d2, matchNum));
				}
			}
		}
		return suspectPairs;
	}

	private void outputRunTime() {
		// Output run time
		endTime = System.currentTimeMillis();
		long totalTime = endTime - startTime;
		System.out.println("Total " + totalTime + " ms");
	}

	/**
	 * Processes the files into hash table and generates similarity matrix
	 * Originally main function before GUI Implementation
	 */
	public void processFiles() {
		// DEBUG
		// cores = 0;
		if (cores > 1) {
			Thread[] scanThreads = new Thread[cores];
			for (int i = 0; i < cores; ++i) {
				scanThreads[i] = new Thread(new MapPopulate());
				scanThreads[i].start();
			}
			for (int i = 0; i < cores; ++i) {
				try {
					scanThreads[i].join();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		} else {
			scanFiles();
		}
		fillSimilarityMatrix();
		// DEBUG
		// cheaters.printSimilarityMatrix();
		consoleOutput();
		outputRunTime();
		
	}
	
	
	/**
	 * Creates a list of suspicious pairs of documents Meant to be used by view to
	 * create a graphic representation
	 * 
	 * @param bound
	 * @return an array list of suspicious pairs of documents
	 */
	public void createSuspectList(int bound) {

		for (int i = 0; i < sameCombo.length; ++i) {
			for (int j = i + 1; j < sameCombo.length; ++j) {
				int matchNum = sameCombo[i][j];
				if (matchNum > bound) {
					Document d1 = Document.getMasterList().get(i);
					Document d2 = Document.getMasterList().get(j);
					if(!suspectDocs.contains(d1)) {
						suspectDocs.add(d1);
					}
					if(!suspectDocs.contains(d2)) {
						suspectDocs.add(d2);
					}
					suspectPairs.add(new SuspectPair(d1, d2, matchNum));
				}
			}
		}
	}

	/**
	 * Returns suspicious pairs of documents
	 * @return
	 */
	public ArrayList<SuspectPair> getSuspiciousPairsOfDocs() {
		return suspectPairs;
	}
	
	/**
	 * Returns suspicious documents
	 */
	public ArrayList<Document> getSuspiciousDocs(){
		return suspectDocs;
	}
	
	/**
	 * Given a directory of essays, will determine similarities between essays
	 * 
	 * @param args
	 *            [0] path to file, [1] number of words
	 */
	public static void main(String[] args) {
		int stress = 20;
		for (int j = 0; j < stress; ++j) {
			Cheaters cheaters;
			if (args.length < 3) {
				cheaters = new Cheaters();
			} else {
				cheaters = new Cheaters(args);
			}
			cheaters.processFiles();
		}
	}

	private class MapPopulate implements Runnable {

		@Override
		public void run() {
			File file = getFromFileList();

			while (file != null) {
				populateHashTable(file);
				file = getFromFileList();
			}
		}
	}

	private class MapIterate implements Runnable {
		@Override
		public void run() {
			List<Document> match = getListFromKeySet();
			while (match != null) {
				processSimilarityMatrix(match);
				match = getListFromKeySet();
			}
		}
	}
}

