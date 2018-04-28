package assignment7;

import javafx.beans.value.ObservableValue;
import javafx.geometry.Bounds;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.text.Text;

public class Node extends StackPane{
	private Document doc;
	Circle circle;
	Text id;
	
	public Node(Document doc) {
		this.doc = doc;
		
		circle = new Circle(50, Color.CORNFLOWERBLUE);
		id = new Text(Integer.toString(this.doc.getId()));
		
		getChildren().addAll(circle, id);
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
