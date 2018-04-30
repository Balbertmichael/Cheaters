package assignment7;

import javafx.event.EventHandler;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
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
	Text label;
	
	public Edge(Node source, Node target, int weight) {
		this.source = source;
		this.target = target;
		this.weight = weight;
		
		source.addConnectedEdge(target, this);
		target.addConnectedEdge(source, this);
		
		line = new Line();
		
		line.startXProperty().bind(source.layoutXProperty().add(source.getBoundsInParent().getWidth() / 2.0));
		line.startYProperty().bind(source.layoutYProperty().add(source.getBoundsInParent().getHeight() / 2.0));
		
		line.endXProperty().bind(target.layoutXProperty().add(target.getBoundsInParent().getWidth() / 2.0));
		line.endYProperty().bind(target.layoutYProperty().add(target.getBoundsInParent().getHeight() / 2.0));
		line.setStroke(Color.BLACK);
		
		line.setStrokeWidth(weight / bound);
		
		centerLoc.setX(line.getStartX() + ((line.getEndX() - line.getStartX()) / 2));
		centerLoc.setY(line.getStartY() + ((line.getEndY() - line.getStartY()) / 2));
		

		getChildren().add(line);
		showLabel();
		
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((source == null) ? 0 : source.hashCode());
		result = prime * result + ((target == null) ? 0 : target.hashCode());
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
		Edge other = (Edge) obj;
		if (source == null) {
			if (other.source != null)
				return false;
		} else if (!source.equals(other.source))
			return false;
		if (target == null) {
			if (other.target != null)
				return false;
		} else if (!target.equals(other.target))
			return false;
		return true;
	}

	private void showLabel() {
		label = new Text(Integer.toString(weight));
		label.relocate(centerLoc.getX() - 10, centerLoc.getY());
		label.setFill(Color.RED);
		getChildren().add(label);
	}
	
	private void hideLabel() {
		getChildren().remove(label);
	}
	
	public void highlight() {
		hideLabel();
		showLabel();
		label.setFont(Font.font(Font.getDefault().toString(), FontWeight.BOLD, 18));
	}
	
	public void deHighlight() {
		hideLabel();
		showLabel();
	}
	
}
