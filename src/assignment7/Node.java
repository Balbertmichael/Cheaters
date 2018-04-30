package assignment7;

import java.util.ArrayList;

import javafx.event.EventHandler;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;

public class Node extends StackPane{
	private ArrayList<Node> connectedNodes = new ArrayList<Node>();
	private ArrayList<Edge> connectedEdges = new ArrayList<Edge>();
	
	public void addConnectedEdge(Node n, Edge e) {
		boolean nFlag = false;
		boolean eFlag = false;
		
		for(Node node : connectedNodes) {
			if(node.equals(n)) {
				nFlag = true;
			}
		}
		
		for(Edge edge : connectedEdges) {
			if(edge.equals(n)) {
				eFlag = true;
			}
		}
		
		if(!nFlag) {
			connectedNodes.add(n);
		}
		
		if(!eFlag) {
			connectedEdges.add(e);
		}
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((doc == null) ? 0 : doc.hashCode());
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
		Node other = (Node) obj;
		if (doc == null) {
			if (other.doc != null)
				return false;
		} else if (!doc.equals(other.doc))
			return false;
		return true;
	}

	private final double PI = 3.14;
	private Document doc;
	Circle circle;
	Text id;
	private double rad;

	private Coordinates loc = new Coordinates();
	private int layer = 0;
	
	private Text label;
	
	private static double halfParentSize;
	private static int nodeCounter = 0;
	private static int totalNodes = 0;
	private static int totalLayers = 0;
	private static double layerLength;
	
	public static void clearCounter() {
		nodeCounter = 0;
	}
	
	public static void setTotalNodeCount(int count) {
		totalNodes = count;
		totalLayers = ((totalNodes - 1) / 8) + 2;
	}
	
	private void calcLoc() {
		double origin = halfParentSize;
		
		if(nodeCounter == 1) {
			loc.setX(origin);
			loc.setY(origin);

			return;
		}
		
		double radius = layerLength;
		double offset = (layer % 2) * (PI / 8);
		double angle = ((nodeCounter) % 8) * (PI / 4);
		
		loc.setX( origin + (layer * radius * Math.cos( angle + offset )));
		loc.setY( origin + (layer * radius * Math.sin( angle + offset )));
	}

	
	public Node(Document doc) {

		this.doc = doc;
		draw();
		
	}
	
	private void draw() {
		halfParentSize = (Main.getSceneSize() * 0.90) / 2;
		layerLength = halfParentSize / totalLayers;
		
		
		nodeCounter++;
		layer = (nodeCounter / 8) + 1;
		
		calcLoc();
		rad = layerLength / 4;
		if(rad > 25) {
			rad = 25;
		}
		else if(rad < 15) {
			rad = 15;
		}		
		circle = new Circle(rad, Color.CORNFLOWERBLUE);
		circle.setStroke(Color.MIDNIGHTBLUE);
		id = new Text(Integer.toString(this.doc.getId()));
		
		getChildren().addAll(circle);
		relocate(loc.getX(), loc.getY());
		
		this.setOnMouseEntered(new EventHandler<MouseEvent>(){

			@Override
			public void handle(MouseEvent arg0) {
				showDoc();
				for(Node n : connectedNodes) {
					n.showDoc();
				}
				
				for(Edge e : connectedEdges) {
					e.highlight();
				}
			}
			
		});
		
		this.setOnMouseExited(new EventHandler<MouseEvent>() {

			@Override
			public void handle(MouseEvent event) {
				hideDoc();
				for(Node n : connectedNodes) {
					n.hideDoc();
				}
				
				for(Edge e : connectedEdges) {
					e.deHighlight();
				}
			}
			
		});
	}
	
	public void showDoc() {
		label = new Text(this.doc.getName());
		label.relocate(loc.getX(), loc.getY());
		label.setFill(Color.WHITE);
		label.setStroke(Color.BLACK);
		label.setFont(Font.font("Verdana", FontWeight.BOLD, 18));
		circle.setRadius(rad * 1.25);
		
		getChildren().add(label);
	};
	
	public void hideDoc() {
		getChildren().remove(label);
		circle.setRadius(rad);
	}
	
	public double getRadius() {
		return rad;
	}
	
	public Document getDoc() {
		return doc;
	}
	
	public Coordinates getLoc() {
		return loc;
	}
}
