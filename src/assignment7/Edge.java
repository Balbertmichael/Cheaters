package assignment7;

import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import javafx.scene.text.Text;

public class Edge extends Pane{
	private static int bound;
	public static void setBound(int boundInput) {
		bound = boundInput;
	}
	
	private Node source;
	private Node target;
	
	Coordinates centerLoc = new Coordinates();
	
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
		
		centerLoc.setX(line.getStartX() + ((line.getEndX() - line.getStartX()) / 2));
		centerLoc.setY(line.getStartY() + ((line.getEndY() - line.getStartY()) / 2));
		
		Text label = new Text(Integer.toString(weight));
		label.relocate(centerLoc.getX() - 10, centerLoc.getY());
		label.setFill(Color.RED);
		
		getChildren().addAll(line, label);
	}
	
}
