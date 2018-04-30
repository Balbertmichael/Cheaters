package assignment7;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import javafx.geometry.Insets;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;


public class Graph extends Pane{
	
	private List<Document> docs;
	private List<SuspectPair> pairs;
	
	private List<Node> nodes = new ArrayList<>();
	private List<Edge> edges = new ArrayList<>();
	private Random rand = new Random();
	
	private UserMenu menu;
	
	public void clearGraph() {
		nodes.clear();
		edges.clear();
		getChildren().clear();
		Node.clearCounter();
	}
	
	public void draw() {
		clearGraph();
		Node.setTotalNodeCount(docs.size());
		
		for(Document d : docs) {
			Node n = new Node(d);
			nodes.add(n);
		}
		
		for(SuspectPair p : pairs) {
			Edge.setBound(menu.getBound());
			
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
			getChildren().add(n);
		}
		
	}
	
	
	public Graph(UserMenu menu) {
		this.menu = menu;
	}
	
	public void setGraph(List<Document> docs, List<SuspectPair> pairs) {
		
		setStyle("-fx-background-color: #35383d;");
		setPadding(new Insets(20));
		
		this.docs = docs;
		this.pairs = pairs;	
		
	}
	
}

