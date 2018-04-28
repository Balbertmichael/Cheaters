package assignment7;

/**
 * Helper Class: Suspicious pairs of documents and the number of similarities
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
