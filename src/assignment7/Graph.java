package assignment7;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import javafx.scene.layout.Pane;


public class Graph extends Pane{

	private int numCols;
	private int numRows;
	private double width;
	private double height;
	private boolean[][] occupied; 
	
	private List<Document> docs;
	private List<SuspectPair> pairs;
	
	private List<Node> nodes = new ArrayList<>();
	private List<Edge> edges = new ArrayList<>();
	private Random rand = new Random();
	
	
	public void setGrid() {
		
	}
	
	private Location pickNodeLoc() {
		double row;
		double col;
	
		row = rand.nextDouble() * width;
		col = rand.nextDouble() * height;

		return new Location(row, col);
	}
	
	public void draw() {
		setGrid();
		
		for(Document d : docs) {
			Node n = new Node(d);
			nodes.add(n);
		}
		
		for(SuspectPair p : pairs) {
			
			Document d1 = p.getD1();
			Document d2 = p.getD2();
			
			int i1 = 0;
			int i2 = 0;
			
			for(int i = 0; i < nodes.size(); ++i) {
				Document d = nodes.get(i).getDoc();
				if(d1.equals(d)) {
					i1 = i;
				}
				if(d2.equals(d)) {
					i2 = i;
				}
			}
			
			Edge e = new Edge(nodes.get(i1), nodes.get(i2), p.getNumSame());
			edges.add(e);
		}
		
		for(Edge e : edges) {
			getChildren().add(e);			
		}
		
		for(Node n : nodes) {
			Location loc = pickNodeLoc();
			n.relocate(loc.getX(), loc.getY());
			getChildren().add(n);
		}
		
	}
	
	
	
	public Graph(List<Document> docs, List<SuspectPair> pairs, double width, double height) {
		this.width = width;
		this.height = height;
		
		this.docs = docs;
		this.pairs = pairs;	
		
		System.out.println(docs.size());
		numRows = docs.size();
		numCols = docs.size();
		occupied = new boolean[numRows][numCols];
	}
	
}

class Location {
	private double x;
	private double y;
	
	public Location(double x,double y) {
		this.x = x;
		this.y = y;
	}
	
	public double getX() {
		return this.x;
	}
	
	public double getY() {
		return this.y;
	}
}
