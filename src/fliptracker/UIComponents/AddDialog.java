/*
 * Decompiled with CFR 0_102.
 * 
 * Could not load the following classes:
 *  javafx.scene.Scene
 *  javafx.stage.Stage
 */
package fliptracker.UIComponents;

import fliptracker.UIComponents.Controllers.AddDialogController;
import fliptracker.UIComponents.Controllers.GuiController;
import fliptracker.UIComponents.ItemPanel;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class AddDialog
extends Stage {
    public AddDialog(Scene scene, GuiController controller, AddDialogController control) {
        this.setScene(scene);
        this.setTitle("Add item");
        control.setValues(controller, this);
        this.show();
    }

    public AddDialog(Scene scene, GuiController controller, AddDialogController control, ItemPanel panel) {
        this.setScene(scene);
        this.setTitle("Edit item");
        control.setValues(controller, this, panel);
        this.show();
    }
}

