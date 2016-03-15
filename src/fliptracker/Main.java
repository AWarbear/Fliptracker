/*
 * Decompiled with CFR 0_102.
 * 
 * Could not load the following classes:
 *  javafx.application.Application
 *  javafx.collections.ObservableList
 *  javafx.event.Event
 *  javafx.event.EventHandler
 *  javafx.fxml.FXMLLoader
 *  javafx.scene.Parent
 *  javafx.scene.Scene
 *  javafx.scene.image.Image
 *  javafx.stage.Stage
 *  javafx.stage.Window
 *  javafx.stage.WindowEvent
 */
package fliptracker;

import fliptracker.UIComponents.Controllers.GuiController;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

public class Main
extends Application {

    public void start(Stage primaryStage) throws Exception {
        primaryStage.getIcons().add(new Image(this.getClass().getClassLoader().getResourceAsStream("fliptracker/res/Images/Money-icon48.png")));
        String version = "Version 1.1.28";
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
        Main.launch((String[])args);
    }
}

