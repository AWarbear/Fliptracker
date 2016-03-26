package fliptracker.UIComponents;

import fliptracker.UIComponents.Controllers.AddDialogController;
import fliptracker.UIComponents.Controllers.GuiController;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class AddDialog extends Stage {
    /**
     * Create the add dialog for adding a new item
     * @param scene the scene
     * @param controller guiController
     * @param control the controller of this
     */
    public AddDialog(Scene scene, GuiController controller, AddDialogController control) {
        setScene(scene);
        setTitle("Add item");
        control.setValues(controller, this);
        show();
    }

    /**
     * Create an add dialog for editing an item
     * @param scene the scene
     * @param controller guiController
     * @param control the controller of this
     * @param panel the item panel to edit
     */
    public AddDialog(Scene scene, GuiController controller, AddDialogController control, ItemPanel panel) {
        setScene(scene);
        setTitle("Edit item");
        control.setValues(controller, this, panel);
        show();
    }
}

