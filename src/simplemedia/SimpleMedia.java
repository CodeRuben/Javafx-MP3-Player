package simplemedia;

import simplemedia.view.PlayerController;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.AnchorPane;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

/**
 *
 * @author ruben
 */
public class SimpleMedia extends Application {
    private AnchorPane root;
    private Stage stage;
    private PlayerController controller;
    
    @Override
    public void start(Stage primaryStage) throws Exception {
        this.stage = primaryStage;
        stage.setTitle("Media Player");
        
        initLayout();
        
		// Pass a reference of the main class to the controller instance.
        controller.setSimpleMedia(this);
    }
    
	/**
	 * Sets the stage, loads the interface and creates a controller instance.
	 * Creates an undecorated stage.
	 */
    public void initLayout() {
        try {
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(SimpleMedia.class.getResource("/simplemedia/view/Player.fxml"));
            root = (AnchorPane) loader.load();

	    	controller = loader.getController();

            Scene scene = new Scene(root,425,223);
            stage.setScene(scene);
			stage.getScene().setFill(Color.TRANSPARENT);
			stage.initStyle(StageStyle.TRANSPARENT);
            stage.show();
        }
        catch (IOException ex) {
            Logger.getLogger(SimpleMedia.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
   
	/**
	 * Returns a reference to the stage variable. 
	 * @return the stage instance variable. 
	 */
    public Stage getStage() {
		return this.stage;
    }
    
    public static void main(String args[]) {
        launch(args);
    }
}
