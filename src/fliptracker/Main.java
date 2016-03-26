package fliptracker;

import fliptracker.UIComponents.Controllers.GuiController;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

/**
 * The main class
 */
public class Main extends Application {

    /**
     * Start the program
     *
     * @param primaryStage
     * @throws Exception
     */
    public void start(Stage primaryStage) throws Exception {
        primaryStage.getIcons().add(new Image(this.getClass().getClassLoader().getResourceAsStream("fliptracker/res/Images/Money-icon48.png")));
        String version = "Version 1.1.29";
        primaryStage.setTitle("Fliptracker " + version);
        FXMLLoader fxmlLoader = new FXMLLoader(this.getClass().getResource("UIComponents/FXML/FxGUI.fxml"));
        Parent root = fxmlLoader.load();
        GuiController controller = fxmlLoader.getController();
        controller.setStage(primaryStage);
        controller.initFields();
        primaryStage.setScene(new Scene(root, 800.0, 500.0));
        primaryStage.show();
        primaryStage.getScene().getWindow().setOnCloseRequest(e -> controller.doClose()
        );
    }

    public static void main(String[] args) {
        Main.launch((String[]) args);
    }
}

