package assignment7;

import javafx.beans.value.ObservableValue;
import javafx.geometry.Bounds;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.text.Text;

public class Node extends StackPane{
	private final double PI = 3.14;
	private Document doc;
	Circle circle;
	Text id;
	private double x;
	private double y;
	private int layer = 0;
	
	private static double parentSize;
	private static int nodeCounter = 0;
	private static int totalNodes = 0;
	private static int totalLayers = 0;
	private static double layerLength;
	
	public static void clearCounter() {
		nodeCounter = 0;
	}
	
	public static void setTotalNodeCount(int count) {
		totalNodes = count;
		totalLayers = ((totalNodes - 1) / 8) + 5;
	}
	
	private void calcDimensions() {
		double origin = parentSize / 2;
		
		if(nodeCounter == 1) {
			x = origin + 50;
			y = origin + 50;
			return;
		}
		
		double radius = layerLength;
		double offset = (layer % 2) * (PI / 8);
		double angle = ((nodeCounter - 1) % 8) * (PI / 4);
		x = origin + 50 + (layer * radius * Math.cos( angle + offset ));
		y = origin + 50 + (layer * radius * Math.sin( angle + offset ));	
	}

	
	public Node(Document doc) {
		parentSize = Main.getSceneSize() * 0.75;
		layerLength = parentSize / totalLayers;
		
		nodeCounter++;
		layer = (nodeCounter / 8) + 1;
		
		calcDimensions();
		
		this.doc = doc;
		
		double rad = layerLength / 4;
		if((rad) > 25) {
			rad = 25;
		}
		
		
		circle = new Circle(rad, Color.CORNFLOWERBLUE);
		id = new Text(Integer.toString(this.doc.getId()));
		
		getChildren().addAll(circle, id);
		relocate(x, y);
//		System.out.println("Parent size: " + parentSize);
//		System.out.println("Node #: " + nodeCounter + "layer: " + layer + "X: " + this.x + " Y: " + this.y + " ID: " + this.doc.getId());
	}
	
	public Document getDoc() {
		return doc;
	}
	
	public double getX(){
		return 0;
	}
	
	public double getY(){
		return 0;
	}
}
