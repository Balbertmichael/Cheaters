package assignment7;

import java.io.File;
import java.nio.file.Paths;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

public class UserMenu extends FlowPane{
	private String dir = Paths.get("").toAbsolutePath().toString();
	private String phrase = "6";
	private String bound = "50";
	private Stage stage;
	
	public UserMenu(Stage stage) {
		this.stage = stage;
	}
	
	public int getBound() {
		return Integer.parseInt(bound);
	}
	
	public void createUserMenu() {
		
		HBox fileInputGroup = new HBox();
		HBox numWordsInputGroup = new HBox();
		HBox boundLimGroup = new HBox();
		
		fileInputGroup.setPadding(new Insets(0));
		numWordsInputGroup.setPadding(new Insets(0));
		boundLimGroup.setPadding(new Insets(0));
		
		
		TextField dirInput = new TextField("Input directory here");
		Button dirChooser = new Button("Browse");
		fileInputGroup.getChildren().addAll(dirInput, dirChooser);
		
		dirChooser.setOnAction(new EventHandler<ActionEvent>() {
	            @Override
	            public void handle(ActionEvent event) {
	                DirectoryChooser directoryChooser = new DirectoryChooser();
	                directoryChooser.setInitialDirectory(new File(Paths.get("").toAbsolutePath().toString()));
	                
	                
	                File selectedDirectory = 
	                        directoryChooser.showDialog(stage);
	                 
	                if(selectedDirectory == null){
	                	dirInput.setText("No Directory selected");
	                }else{
	                    dirInput.setText(selectedDirectory.getAbsolutePath());
	                }
	            }
	    });
		
		
		Label numWordsLabel = new Label("# Words per Phrase:  ");
		TextField numWordsInput = new TextField("6");
		numWordsInputGroup.getChildren().addAll(numWordsLabel, numWordsInput);
		
		Label minBoundLabel = new Label("Min Bound:  ");
		TextField minBoundInput = new TextField("50");
		boundLimGroup.getChildren().addAll(minBoundLabel, minBoundInput);
		
		Button run = new Button("Run");
		run.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent arg0) {
				String [] args = new String[3];
				args[0] = dirInput.getText();
				args[1] = numWordsInput.getText();
				args[2] = minBoundInput.getText();
				
				Cheaters cheaters = null;
				if(!(args[0].equals(dir) && args[1].equals(phrase)) || Main.getCheaters() == null) {
				    cheaters = new Cheaters(args);
				    Main.setCheaters(cheaters);
				    cheaters.processFiles();
				    cheaters.createSuspectList(Integer.parseInt(args[2]));
				    
				} else if(!args[2].equals(bound)) {
					cheaters = Main.getCheaters();
					cheaters.createSuspectList(Integer.parseInt(args[2]));
				}
				
				if(!(cheaters == null)) {
					Graph graph = Main.getGraph();
				    graph.setGraph(cheaters.getSuspiciousDocs(), cheaters.getSuspiciousPairsOfDocs());
				    graph.draw();
				}

			}
		});
		
		
		setHgap(10);
		getChildren().addAll(fileInputGroup, numWordsInputGroup, boundLimGroup, run);
		setAlignment(Pos.BASELINE_CENTER);
		setPadding(new Insets(20));
	}
}
