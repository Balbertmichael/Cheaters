package assignment7;

import javafx.scene.Group;
import javafx.scene.control.Label;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;

public class Edge extends Pane{
	private static int bound;
	public static void setBound(int boundInput) {
		bound = boundInput;
	}
	
	private Node source;
	private Node target;
	
	protected int weight;
	Line line;
	
	public Edge(Node source, Node target, int weight) {
		this.source = source;
		this.target = target;
		this.weight = weight;
				
		line = new Line();
		
		line.startXProperty().bind(source.layoutXProperty().add(source.getBoundsInParent().getWidth() / 2.0));
		line.startYProperty().bind(source.layoutYProperty().add(source.getBoundsInParent().getHeight() / 2.0));
		
		line.endXProperty().bind(target.layoutXProperty().add(target.getBoundsInParent().getWidth() / 2.0));
		line.endYProperty().bind(target.layoutYProperty().add(target.getBoundsInParent().getHeight() / 2.0));
		line.setStroke(Color.BLACK);
		
		line.setStrokeWidth(weight / bound);
		
		getChildren().addAll(line);
	}
	
}
