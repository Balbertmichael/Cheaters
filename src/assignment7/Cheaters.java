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
	private final Pattern UNWANTED_PUNCTUATION = Pattern.compile("\\p{P}"); // pattern that should be filtered out

	// Data Structures to hold the needed map, matrix and pairs
	private Hashtable<String, List<Document>> wordCombos = new Hashtable<String, List<Document>>();
	// Hash Table of (word: list of documents it was found in)
	private int[][] sameCombo;
	private ArrayList<SuspectPair> suspiciousDocs = new ArrayList<SuspectPair>();

	// Parameters needed for the program to run
	private File folder; // folder that holds documents (assumes no sub-directories)
	private File[] listOfFiles; // list of files in the folder
	private int numWords; // number of words that count as a similarity
	private int miniBound; // number of similarities that count as dangerous

	// Concurrency Stuff:
	private int cores = Runtime.getRuntime().availableProcessors();
	private Object o = new Object();
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

	/**
	 * Scans the files in the subdirectory and populates the hash table The hash
	 * table: key: phrases of <numWords> words that count as a similarity value:
	 * linked list of documents that contain that particular phrase similarity
	 * Single-Threaded method
	 */
	private void scanFiles() {
		// Iterate through files and input into hash table
		for (File file : listOfFiles) {
			populateHashTable(file);
		}
	}

	/**
	 * Build the hash table from the given <numWords> chunks
	 * 
	 * @param file
	 *            The file to process over in order to have a generic file
	 *            processing
	 */
	private void populateHashTable(File file) {
		Scanner sc;
		String key;
		Document fDocument;

		synchronized (o) {
			fDocument = new Document(file.getName());
		}

		try {
			sc = new Scanner(file);
			ArrayList<String> keyArr = new ArrayList<String>();
			for (int k = 0; k < numWords; ++k) {
				if (sc.hasNext()) {
					keyArr.add(sc.next());
				}
			}
			while (sc.hasNext()) {
				// Error checking just in case
				if (keyArr.size() > numWords) {
					System.out.println("Error size: " + keyArr.size());
					System.exit(0);
				}
				key = "";
				for (String s : keyArr) {
					key += s;
				}
				addKeyToHash(key, fDocument);
				keyArr.remove(0);
				keyArr.add(sc.next());
			}
			sc.close();
		} catch (FileNotFoundException e) {
			System.out.println("File not found");
			e.printStackTrace();
		}
	}

	/**
	 * Abstracted layer of the word Processing to bring into the HashMap of Cheaters
	 * 
	 * @param key
	 *            The <numWords> chunk
	 * @param fDocument
	 *            The identifier for the document
	 */
	private void addKeyToHash(String key, Document fDocument) {

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
			// list.add(fDocument);

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
	 */
	private void fillSimilarityMatrix() {
		// Create an similarity matrix
		sameCombo = new int[Document.counter][Document.counter];
		keySet = wordCombos.keySet().iterator();

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
				int id1 = d1.id;
				int id2 = d2.id;
				if (id1 > id2) {
					int temp = id2; // In order to map to the upper triangle of the matrix to not have to do a
									// double check or fold over
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
			for (int j = i + 1; j < sameCombo.length; ++j) { // i + 1 to skip over checking the diagonal of the matrix
				int matchNum = sameCombo[i][j];
				if (matchNum > miniBound) {
					Document d1 = Document.masterList.get(i);
					Document d2 = Document.masterList.get(j);
					System.out.println(d1.name + "," + d2.name + ": " + matchNum);
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
					Document d1 = Document.masterList.get(i);
					Document d2 = Document.masterList.get(j);
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
	 * Given a directory of essays, will determine similarities between essays
	 * 
	 * @param args
	 *            [0] path to file, [1] number of words
	 */
	public static void main(String[] args) {
		Cheaters cheaters;
		if (args.length < 3) {
			cheaters = new Cheaters();
		} else {
			cheaters = new Cheaters(args);
		}
		if (cheaters.cores > 1) {
			Thread[] scanThreads = new Thread[cheaters.cores];
			for (int i = 0; i < cheaters.cores; ++i) {
				scanThreads[i] = new Thread(cheaters.new MapPopulate());
				scanThreads[i].start();
			}
			for (int i = 0; i < cheaters.cores; ++i) {
				try {
					scanThreads[i].join();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		} else {
			cheaters.scanFiles();
		}
		cheaters.fillSimilarityMatrix();

		// DEBUG
		// cheaters.printSimilarityMatrix();
		cheaters.consoleOutput();
		cheaters.outputRunTime();
	}

	/**
	 * Concurrency class for building the HashMap
	 * 
	 * @author balbe
	 *
	 */
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
		@Override
		public void run() {
			List<Document> match = getListFromKeySet();
			while (match != null) {
				processSimilarityMatrix(match);
				match = getListFromKeySet();
			}
		}
	}

	/**
	 * Concurrency method for processing over the HashMap
	 * 
	 * @return The List of the documents with a match to the key
	 */
	private synchronized List<Document> getListFromKeySet() {
		if (keySet.hasNext()) {
			return wordCombos.get(keySet.next());
		} else {
			return null;
		}
	}
}

/**
 * Document class: Helper Class that represents a file in the directory Each
 * file will have an ID that will help map it to the similarity array The
 * document title are also kept Holds both ID and document title
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

/**
 * Matcher class in order to make processing for the GUI simpler
 * 
 * @author balbe
 *
 */
class SuspectPair {
	private Document d1;
	private Document d2;
	private int numSimilarities;

	public SuspectPair(Document d1, Document d2, int numSim) {
		this.d1 = d1;
		this.d2 = d2;
		this.numSimilarities = numSim;
	}

	public Document getD1() {
		return d1;
	}

	public Document getD2() {
		return d2;
	}

	public int getNumSame() {
		return numSimilarities;
	}
}
