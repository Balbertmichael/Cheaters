package assignment7;

import java.util.ArrayList;

/**
 * Document class: Helper Class that represents a file in the directory
 * Each file will have an ID that will help map it to the similarity array
 * The document title are also kept
 * Holds both ID and document title
 */
public class Document {
	private int id;
	private String name;
	private static int counter = 0;
	private static ArrayList<Document> masterList = new ArrayList<Document>();

	public Document() {
		id = counter++;
		this.name = "testingdoc#" + counter;
		masterList.add(this);
	}
	
	public Document(String name) {
		id = counter++;
		this.name = name;
		masterList.add(this);
	}

	public static int getCounter() {
		return counter;
	}
	
	public static ArrayList<Document> getMasterList(){
		return masterList;
	}
	
	public int getId() {
		return id;
	}
	
	public String getName() {
		return name;
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
