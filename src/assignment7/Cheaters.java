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
	private static final Pattern UNWANTED_PUNCTUATION = Pattern.compile("\\p{P}");
	public static Hashtable<String, LinkedList<Document>> wordCombos = new Hashtable<String, LinkedList<Document>>();

	/**
	 * Given a directory of essays, will determine similarities between essays
	 * 
	 * @param args
	 *            [0] path to file, [1] number of words
	 */
	public static void main(String[] args) {

		File folder;
		File[] listOfFiles;
		String folderName;
		int numWords;
		int miniBound;

		// Get folder with files
		if (args.length < 3) {
			// Default parameters: Use when testing
			folderName = Paths.get("").toAbsolutePath().toString();
			folder = new File(folderName);
			numWords = 6;
			miniBound = 200;
		} else {
			// User input parameters
			// Assumes folder of only text entries and no subdirectories
			folder = new File(args[0]);
			numWords = Integer.parseInt(args[1]);
			miniBound = Integer.parseInt(args[2]);
		}

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

		Scanner sc;
		String key;
		// Iterate through files
		for (File file : listOfFiles) {
			Document fDocument = new Document(file.getName());
			System.out.println("FILE: " + fDocument);

			try {

				// Must rotate through number of words, each time offset by one
				for (int k = 0; k < numWords; ++k) {
					System.out.println("=========================================");
					System.out.println("=========================================");
					System.out.println("=========================================");
					sc = new Scanner(file);
					// sc.useDelimiter(" ");

					while (sc.hasNext()) {

						// On second run, start from second word. On third run, start from third ...
						for (int i = 0; i < k; ++i) {
							if (!sc.hasNext()) {
								break;
							}
							sc.next();
						}

						// Hash the keys into hashtable/map
						key = "";
						for (int i = 0; i < numWords; ++i) {
							if (!sc.hasNext()) {
								break;
							}
							key += sc.next();
						}
						key = UNWANTED_PUNCTUATION.matcher(key).replaceAll("");
						key = key.toUpperCase();

						if (wordCombos.containsKey(key)) {
							LinkedList<Document> list = wordCombos.get(key);
							list.add(fDocument);
						} else {
							LinkedList<Document> value = new LinkedList<>();
							value.add(fDocument);
							wordCombos.put(key, value);
						}

						System.out.println(key);
					}
				}

			} catch (FileNotFoundException e) {
				System.out.println("File not found");
				e.printStackTrace();
			}
		}
		System.out.println(wordCombos);
		int[][] sameCombo = new int[Document.counter][Document.counter];
		for (String k : wordCombos.keySet()) {
			LinkedList<Document> match = wordCombos.get(k);
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
		for (int i = 0; i < sameCombo.length; ++i) {
			String lineOut = "";
			for (int j = 0; j < sameCombo.length; ++j) {
				lineOut += sameCombo[i][j] + " ";
			}
			System.out.println(lineOut);
		}
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
}

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
