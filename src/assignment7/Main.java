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
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;
import javafx.stage.Stage;

public class Main extends Application {

	private final static int SCENE_SIZE = 800;
	private static UserMenu menu;
	private static Graph graph;
	private static Cheaters cheaters;	
	
	public static int getSceneSize() {
		return SCENE_SIZE;
	}
	
	public static void setCheaters(Cheaters c) {
		cheaters = c;
	}
	
	public static Cheaters getCheaters() {
		return cheaters;
	}
	
	public static UserMenu getUserMenu() {
		return menu;
	}
	
	public static Graph getGraph() {
		return graph;
	}
	
	@Override
	public void start(Stage primaryStage) {
	    
	    VBox root = new VBox();
	    Scene sc = new Scene(root, SCENE_SIZE, SCENE_SIZE);
		
	    menu = new UserMenu(primaryStage);
	    menu.createUserMenu();
	    
	    graph = new Graph(menu);

	    root.getChildren().addAll(menu, graph);
	    root.setSpacing(20);
	    
		primaryStage.setTitle("Cheaters");
		primaryStage.setScene(sc);
		primaryStage.show();
		
	}

	public static void main(String[] args) {
		launch(args);
	}
}