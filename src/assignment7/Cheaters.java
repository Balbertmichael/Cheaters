package assignment7;

/* CHEATERS Cheaters.java
 * EE422C Project 7 submission by
 * Replace <...> with your actual data.
 * <Student1 Angelique Bautista>
 * <Student1 ab54429>
 * <Student1 15465>
 * <Student2 Albert Bautista>
 * <Student2 abb2639>
 * <Student2 15505>
 * Slip days used: <0>
 * Spring 2018
 */

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Scanner;
import java.util.regex.Pattern;

public class Cheaters {
	private final Pattern UNWANTED_PUNCTUATION = Pattern.compile("[^A-Za-z0-9]"); // pattern that should be filtered out

	// Hash Table of (word: list of documents it was found in)
	private int[][] sameWords; // Similarity Matrix
	private ArrayList<SuspectPair> suspiciousDocs = new ArrayList<SuspectPair>();

	// Parameters needed for the program to run
	private File folder; // folder that holds documents (assumes no sub-directories)
	private File[] listOfFiles; // list of files in the folder
	private int numWords; // number of words that count as a similarity
	private int miniBound; // number of similarities that count as dangerous

	// Concurrency Stuff:
	private int cores = Runtime.getRuntime().availableProcessors();
	private final Object o = new Object();
	private Iterator<File> listIter;
	private Iterator<String> keySet;

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
	private void getFileList() {
		// Grab list of files in directory
		listOfFiles = folder.listFiles();
		listIter = Arrays.asList(listOfFiles).iterator();
		// If files do not exist, return
		if (listOfFiles == null) {
			System.out.println("No files found. Check directory name");
			return;
		}
	}

	/**
	 * Scans the files in the subdirectory and populates the hash table The hash
	 * table: key: phrases of <numWords> words that count as a similarity value:
	 * linked list of documents that contain that particular phrase similarity
	 * Single-Threaded method
	 */
	private void scanFiles(Hashtable<String, List<Document>> wordCombos) {
		// Iterate through files and input into hash table
		for (File file : listOfFiles) {
			populateHashTable(file, wordCombos);
		}
	}

	/**
	 * Build the hash table from the given <numWords> chunks
	 * 
	 * @param file
	 *            The file to process over in order to have a generic file
	 *            processing
	 */
	private void populateHashTable(File file, Hashtable<String, List<Document>> wordCombos) {
		Scanner sc;
		String key;
		Document fDocument;

		synchronized (o) {
			fDocument = new Document(file.getName());
		}

		try {
			String encoding = "UTF-8";
			sc = new Scanner(new BufferedReader(new InputStreamReader(new FileInputStream(file), encoding)));
			ArrayList<String> keyArr = new ArrayList<String>();
			for (int k = 0; k < numWords; ++k) {
				if (sc.hasNext()) {
					keyArr.add(addNextKey(sc));
				}
			}
			while (sc.hasNext()) {
				key = "";
				for (String s : keyArr) {
					key += s;
				}

				addKeyToHash(key, fDocument, wordCombos);
				keyArr.remove(0);
				keyArr.add(addNextKey(sc));
			}
			sc.close();
		} catch (FileNotFoundException e) {
			System.out.println("File not found");
			e.printStackTrace();
		} catch (UnsupportedEncodingException e) {
			System.out.println("Encoding not supported");
			e.printStackTrace();
		}
	}

	private String addNextKey(Scanner sc) {
		String newKey = "";
		while (sc.hasNext() && newKey.equals("")) { // Cycles through empty keys to get rid of stray marks
			newKey = sc.next();
			// Cleans key (get rid of punctuation/turn to all upper case)
			newKey = UNWANTED_PUNCTUATION.matcher(newKey).replaceAll("");
			newKey = newKey.toUpperCase();
		}
		return newKey;
	}

	/**
	 * Abstracted layer of the word Processing to bring into the HashMap of Cheaters
	 * 
	 * @param key
	 *            The numWords chunk
	 * @param fDocument
	 *            The identifier for the document
	 * @param wordCombos
	 *            Needed to modify the HashTable
	 */
	private void addKeyToHash(String key, Document fDocument, Hashtable<String, List<Document>> wordCombos) {

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
	}

	/**
	 * Creates a m x m matrix (where m = number of documents) that keeps track of
	 * the number of similarities between two docs Iterates through hash table and
	 * update a similarity array counting documents hashed to the same key
	 * 
	 * @param wordCombos
	 *            Uses the HashMap to generate the similarity Matrix
	 */
	private void fillSimilarityMatrix(Hashtable<String, List<Document>> wordCombos) {
		// Create an similarity matrix
		sameWords = new int[Document.getCounter()][Document.getCounter()];
		keySet = wordCombos.keySet().iterator();

		if (cores > 1) {
			Thread[] processThreads = new Thread[cores];
			for (int i = 0; i < cores; ++i) {
				processThreads[i] = new Thread(new MapIterate(wordCombos));
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

	/**
	 * Generic processing layer to use for both single and multi-threaded methods
	 * 
	 * @param match
	 *            Basically uses different documents from the list as coordinates to
	 *            add to the matrix
	 */
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
					int temp = id2; // In order to map to the upper triangle of the matrix to not have to do a
									// double check or fold over values and just check the upper triangular matrix
					id2 = id1;
					id1 = temp;
				}
				sameWords[id1][id2]++;
			}
		}
	}

	/**
	 * Use for Debugging: Prints array
	 */
	private void printSimilarityMatrix() {
		// DEBUG: Print similarity Matrix
		for (int i = 0; i < sameWords.length; ++i) {
			String lineOut = "";
			for (int j = 0; j < sameWords.length; ++j) {
				lineOut += sameWords[i][j] + " ";
			}
			System.out.println(lineOut);
		}
	}

	/**
	 * Outputs the documents that have more similarities than the min bound
	 */
	private void consoleOutput() {
		// Output documents that have more than the minimum bound of similar words
		for (int i = 0; i < sameWords.length; ++i) {
			for (int j = i + 1; j < sameWords.length; ++j) { // i + 1 to skip over checking the diagonal of the matrix
				int matchNum = sameWords[i][j];
				if (matchNum > miniBound) {
					Document d1 = Document.getMasterList().get(i);
					Document d2 = Document.getMasterList().get(j);
					System.out.println(d1.getName() + "," + d2.getName() + ": " + matchNum);
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

		for (int i = 0; i < sameWords.length; ++i) {
			for (int j = i + 1; j < sameWords.length; ++j) {
				int matchNum = sameWords[i][j];
				if (matchNum > bound) {
					Document d1 = Document.getMasterList().get(i);
					Document d2 = Document.getMasterList().get(j);
					// if(d1.getName().equals("bef1121") && d2.getName().equals("edo14")) {
					// System.out.println();
					// }
					suspiciousDocs.add(new SuspectPair(d1, d2, matchNum));
				}
			}
		}
		return suspiciousDocs;
	}

	/**
	 * For testing runtime in debugging and making sure timing works fine
	 */
	private void outputRunTime() {
		// Output run time
		endTime = System.currentTimeMillis();
		long totalTime = endTime - startTime;
		System.out.println("Total " + totalTime + " ms");
	}

	/**
	 * Wrapper for the file processing part from the HashMap creation to the matrix
	 * addition (Also not have to call cheaters.func for every function
	 */
	public void processFiles() {
		cores = 0;
		fillSimilarityMatrix(generateHashTable());
		consoleOutput();
		outputRunTime();
	}

	private Hashtable<String, List<Document>> generateHashTable() {
		// Data Structure to hold the needed map
		Hashtable<String, List<Document>> wordCombos = new Hashtable<String, List<Document>>();
		if (cores > 1) {
			Thread[] scanThreads = new Thread[cores];
			for (int i = 0; i < cores; ++i) {
				scanThreads[i] = new Thread(new MapPopulate(wordCombos));
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
			scanFiles(wordCombos);
		}
		return wordCombos;
	}

	/**
	 * Given a directory of essays, will determine similarities between essays
	 * 
	 * @param args
	 *            [0] path to file, [1] number of words, [2] is minimum similarity
	 *            to show up in output
	 */
	public static void main(String[] args) {
		Cheaters cheaters;
		if (args.length < 3) {
			cheaters = new Cheaters();
		} else {
			cheaters = new Cheaters(args);
		}
		cheaters.processFiles();
	}

	/**
	 * Concurrency class for building the HashMap
	 * 
	 * @author balbe
	 *
	 */
	private class MapPopulate implements Runnable {
		Hashtable<String, List<Document>> wordCombos;

		public MapPopulate(Hashtable<String, List<Document>> wordCombos) {
			this.wordCombos = wordCombos;
		}

		@Override
		public void run() {
			File file = getFromFileList();
			while (file != null) {
				populateHashTable(file, wordCombos);
				file = getFromFileList();
			}
		}

	}

	/**
	 * Concurrency synchronized method to process multiple files at once to put into
	 * the HashTable
	 * 
	 * @return A file if there is one available
	 */
	private synchronized File getFromFileList() {
		if (listIter.hasNext()) {
			return listIter.next();
		} else {
			return null;
		}
	}

	/**
	 * Concurrency class for processing over the HashMap
	 * 
	 * @author balbe
	 *
	 */
	private class MapIterate implements Runnable {
		Hashtable<String, List<Document>> wordCombos;

		public MapIterate(Hashtable<String, List<Document>> wordCombos) {
			this.wordCombos = wordCombos;
		}

		@Override
		public void run() {
			List<Document> match = getListFromKeySet(wordCombos);
			while (match != null) {
				processSimilarityMatrix(match);
				match = getListFromKeySet(wordCombos);
			}
		}
	}

	/**
	 * Concurrency method for processing over the HashMap
	 * 
	 * @return The List of the documents with a match to the key
	 */
	private synchronized List<Document> getListFromKeySet(Hashtable<String, List<Document>> wordCombos) {
		if (keySet.hasNext()) {
			return wordCombos.get(keySet.next());
		} else {
			return null;
		}
	}

}
