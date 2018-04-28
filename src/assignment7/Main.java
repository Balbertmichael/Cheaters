package assignment7;

import java.awt.Button;
import java.util.ArrayList;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;
import javafx.stage.Stage;

public class Main extends Application {

	@Override
	public void start(Stage primaryStage) {
		String[] args = {"C:\\Users\\anisa\\Desktop\\Sp18\\EE 422C\\Programming Assignments\\7Plagiarism\\sm_doc_set\\sm_doc_set", "3", "50"};

	    Pane canvas = new Pane();
	    Pane circleCanvas = new StackPane();
	    
	    canvas.setStyle("-fx-background-color: #35383d;");
	    Circle circle = new Circle(50,Color.BLUE);
	    circle.relocate(20, 20);
	    circleCanvas.getChildren().addAll(circle, new Text("1"));
	    circleCanvas.relocate(500, 500);
	    Rectangle rectangle = new Rectangle(100,100,Color.RED);
	    rectangle.relocate(70,70);
	    canvas.getChildren().addAll(circleCanvas,rectangle);
		
	    Cheaters cheaters = new Cheaters(args);
	    cheaters.processFiles();
	    cheaters.createSuspectList(50);
	    Graph graph = new Graph(cheaters.getSuspiciousDocs(), cheaters.getSuspiciousPairsOfDocs(), 600, 600);
	    graph.setStyle("-fx-background-color: #35383d;");
	    graph.draw();
	    
		Scene sc = new Scene(graph, 800, 800);
		primaryStage.setTitle("Cheaters");
		primaryStage.setScene(sc);
		primaryStage.show();
		
	}

	public static void main(String[] args) {
		launch(args);
	}
}