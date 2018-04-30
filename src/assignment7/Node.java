package assignment7;

import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.text.Text;

public class Node extends StackPane{
	private final double PI = 3.14;
	private Document doc;
	Circle circle;
	Text id;
	private double rad;

	private Coordinates loc = new Coordinates();
	private int layer = 0;
	
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
		id = new Text(Integer.toString(this.doc.getId()));
		
		getChildren().addAll(circle);
		relocate(loc.getX(), loc.getY());
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
