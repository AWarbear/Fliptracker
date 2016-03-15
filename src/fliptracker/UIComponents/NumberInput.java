/*
 * Decompiled with CFR 0_102.
 * 
 * Could not load the following classes:
 *  javafx.collections.ObservableList
 *  javafx.event.ActionEvent
 *  javafx.event.Event
 *  javafx.event.EventHandler
 *  javafx.geometry.Insets
 *  javafx.scene.Node
 *  javafx.scene.Parent
 *  javafx.scene.Scene
 *  javafx.scene.control.Button
 *  javafx.scene.control.Label
 *  javafx.scene.control.ListView
 *  javafx.scene.control.TextField
 *  javafx.scene.layout.ColumnConstraints
 *  javafx.scene.layout.GridPane
 *  javafx.scene.layout.Priority
 *  javafx.stage.Stage
 */
package fliptracker.UIComponents;

import fliptracker.UIComponents.Controllers.GuiController;
import fliptracker.UIComponents.ItemPanel;
import fliptracker.Utils.Logger;
import javafx.event.ActionEvent;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.stage.Stage;

class NumberInput
extends Stage {
    static final int COMPLETE = 1;
    private final GuiController controller;
    private ItemPanel item;
    private final int type;
    private final TextField inputField;

    public NumberInput(GuiController controller) {
        this.controller = controller;
        this.type = 1;
        Button complete = new Button("Submit");
        complete.setOnAction(this::handleButtonPress
        );
        complete.setMinWidth(80.0);
        this.inputField = new TextField("");
        Label textLabel = new Label("");
        textLabel.setWrapText(true);
        switch (1) {
            case 1: {
                textLabel.setText("Enter the number of items to complete");
                this.setTitle("Complete offer");
                break;
            }
            default: {
                this.setTitle("Number input dialog");
                textLabel.setText("Number input dialog");
            }
        }
        GridPane.setConstraints(complete, 2, 1);
        GridPane.setConstraints(this.inputField, 0, 1);
        GridPane.setConstraints(textLabel, 0, 0);
        GridPane container = new GridPane();
        ColumnConstraints columnConstraints = new ColumnConstraints();
        columnConstraints.setFillWidth(true);
        columnConstraints.setHgrow(Priority.ALWAYS);
        container.getColumnConstraints().add(columnConstraints);
        container.setPadding(new Insets(20.0, 10.0, 10.0, 10.0));
        container.setHgap(5.0);
        container.setVgap(10.0);
        container.getChildren().addAll(complete, this.inputField, textLabel);
        container.setId("addDialog");
        this.setScene(new Scene(container, 350.0, 100.0));
        this.getScene().getStylesheets().add(controller.profileManager.cssUrls.get(controller.profileManager.currentTheme));
        this.setResizable(false);
        this.setAlwaysOnTop(true);
        this.show();
    }

    public void setItem(ItemPanel item) {
        this.item = item;
    }

    private void handleButtonPress(ActionEvent event) {
        if (event.getSource().getClass().equals(Button.class)) {
            Button button = (Button)event.getSource();
            switch (button.getText()) {
                case "Submit": {
                    switch (this.type) {
                        case 1: {
                            int amount;
                            if (this.item == null) {
                                return;
                            }
                            try {
                                amount = Integer.parseInt(this.inputField.getText());
                            }
                            catch (NumberFormatException nfe) {
                                Logger.Log("Invalid number don't do anything");
                                return;
                            }
                            if (amount <= 0) {
                                Logger.Log("Negative amount -> return");
                                return;
                            }
                            if (this.item.amount - amount <= 0) {
                                this.controller.addLogItem(this.item);
                                this.controller.activeItems.getItems().remove(this.item);
                                this.close();
                            } else {
                                this.item.amount-=amount;
                                ItemPanel pane = new ItemPanel(this.item.itemName, this.item.price, amount, this.item.type, this.item.getTime(), this.controller);
                                pane.duration = this.item.duration;
                                this.controller.addLogItem(pane);
                                this.item.update();
                                this.close();
                            }
                            this.controller.profileManager.saveLogFile();
                        }
                    }
                    break;
                }
                default: {
                    Logger.Log("Unknown button " + button.getText());
                }
            }
        }
    }
}

