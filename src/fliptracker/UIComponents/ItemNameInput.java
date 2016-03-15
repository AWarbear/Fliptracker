/*
 * Decompiled with CFR 0_102.
 * 
 * Could not load the following classes:
 *  javafx.beans.property.StringProperty
 *  javafx.beans.value.ChangeListener
 *  javafx.beans.value.ObservableValue
 *  javafx.collections.ObservableList
 *  javafx.event.ActionEvent
 *  javafx.event.Event
 *  javafx.event.EventHandler
 *  javafx.geometry.Insets
 *  javafx.scene.Node
 *  javafx.scene.Parent
 *  javafx.scene.Scene
 *  javafx.scene.control.Alert
 *  javafx.scene.control.Alert$AlertType
 *  javafx.scene.control.Button
 *  javafx.scene.control.ComboBox
 *  javafx.scene.control.Label
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

import java.util.ArrayList;

import javafx.event.ActionEvent;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.stage.Stage;

public class ItemNameInput
extends Stage {
    public static final int COOLDOWN = 1;
    public static final int MARGIN = 2;
    private final GuiController controller;
    private ItemPanel item;
    private final int type;
    private final ComboBox<String> inputField;

    public ItemNameInput(GuiController controller, int type) {
        this.controller = controller;
        this.type = type;
        Button complete = new Button("Submit");
        complete.setOnAction(this::handleButtonPress
        );
        complete.setMinWidth(80.0);
        this.inputField = new ComboBox();
        Label textLabel = new Label("");
        textLabel.setWrapText(true);
        switch (type) {
            case 1: {
                textLabel.setText("Enter the name of the item of which cooldown you want to check");
                this.setTitle("Cooldown check");
                break;
            }
            case 2: {
                textLabel.setText("Enter the name of the item of which margin you want to check");
                this.setTitle("Margin check");
                break;
            }
            default: {
                this.setTitle("ItemName input dialog");
                textLabel.setText("ItemName input dialog");
            }
        }
        this.inputField.setOnShown(e -> {
            if (this.inputField.getEditor().getText().isEmpty() && this.inputField.getItems() != null) {
                this.inputField.getItems().clear();
                this.inputField.getItems().addAll(controller.profileManager.recent.split(":"));
            }
        }
        );
        GridPane.setConstraints(complete, 2, 1);
        GridPane.setConstraints(this.inputField, 0, 1);
        GridPane.setConstraints(textLabel, 0, 0);
        GridPane.setColumnSpan(textLabel, 2);
        this.inputField.setEditable(true);
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
        this.addSearch();
        this.setScene(new Scene(container, 350.0, 100.0));
        this.getScene().getStylesheets().add(controller.profileManager.cssUrls.get(controller.profileManager.currentTheme));
        this.setResizable(false);
        this.setAlwaysOnTop(true);
        this.show();
    }

    private void addSearch() {
        this.inputField.getEditor().textProperty().addListener((observable, oldValue, newValue) -> {
            if (!ItemNameInput.this.inputField.getItems().contains(ItemNameInput.this.inputField.getValue())) {
                ItemNameInput.this.handleSearch(ItemNameInput.this.inputField.getEditor().getText());
            }
        });
    }

    private void handleSearch(String string) {
        if (string == null || string.isEmpty()) {
            return;
        }
        ArrayList<String> keyArray = new ArrayList<>();
        keyArray.addAll(this.controller.profileManager.limitMap.keySet());
        ArrayList<String> matches = new ArrayList<>();
        for (String key : keyArray) {
            if (!key.toLowerCase().contains(string.toLowerCase())) continue;
            matches.add(key);
            if (matches.size() <= 15) continue;
            Logger.Log("Too many, stop searching");
            matches.clear();
            return;
        }
        if (matches.size() == 0) {
            return;
        }
        if (this.inputField.getItems() != null) {
            this.inputField.getItems().clear();
        }
        for (String match : matches) {
            if (match != null) {
                this.inputField.getItems().add(match);
            }
            Logger.Log("Search match: " + match);
        }
        this.inputField.show();
    }

    private void handleButtonPress(ActionEvent event) {
        if (event.getSource().getClass().equals(Button.class)) {
            Button button = (Button)event.getSource();
            block3 : switch (button.getText()) {
                case "Submit": {
                    Alert alert = new Alert(Alert.AlertType.INFORMATION);
                    switch (this.type) {
                        case 1: {
                            if (this.inputField.getValue().isEmpty()) {
                                return;
                            }
                            alert.setTitle("Check cooldown");
                            alert.setHeaderText(null);
                            alert.setContentText(this.controller.checkCooldown(this.inputField.getValue()));
                            this.close();
                            alert.showAndWait();
                            break block3;
                        }
                        case 2: {
                            if (this.inputField.getValue().isEmpty()) {
                                return;
                            }
                            int[] margin = this.controller.getMargin(this.inputField.getValue());
                            alert.setTitle("Get margin");
                            alert.setHeaderText(null);
                            alert.setContentText("Margin for " + this.inputField.getValue() + " is " + margin[0] + "-" + margin[1]);
                            this.close();
                            alert.showAndWait();
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

