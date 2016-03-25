/*
 * Decompiled with CFR 0_102.
 * 
 * Could not load the following classes:
 *  javafx.application.Platform
 *  javafx.beans.property.StringProperty
 *  javafx.beans.value.ChangeListener
 *  javafx.beans.value.ObservableValue
 *  javafx.collections.ObservableList
 *  javafx.event.ActionEvent
 *  javafx.event.Event
 *  javafx.fxml.FXML
 *  javafx.scene.control.Alert
 *  javafx.scene.control.Alert$AlertType
 *  javafx.scene.control.Button
 *  javafx.scene.control.CheckBox
 *  javafx.scene.control.ComboBox
 *  javafx.scene.control.Label
 *  javafx.scene.control.TextField
 *  javafx.scene.layout.Pane
 *  javafx.stage.Stage
 */
package fliptracker.UIComponents.Controllers;

import fliptracker.UIComponents.ItemPanel;
import fliptracker.Utils.Logger;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;

import java.util.ArrayList;

@SuppressWarnings("ALL")
public class AddDialogController {
    public CheckBox cBox;
    public TextField priceField;
    public TextField amountField;
    public TextField timeSinceStartedField;
    public Label timeLabel;
    public ComboBox<String> searchBox;
    public Pane rootPane;
    private GuiController guiController;
    private Stage window;
    private ItemPanel itemPanel;
    public Button add;
    int price;
    int amount;
    int timeAfter;
    String itemName;
    String type;

    public void setStylesheet(){
        this.rootPane.getStylesheets().clear();
        try {
            this.rootPane.getStylesheets().add(guiController.profileManager.cssUrls.get(guiController.profileManager.currentTheme));
        }catch(IndexOutOfBoundsException iobe){
            //No value so go default
        }
    }

    public void setValues(GuiController controller, Stage window) {
        this.guiController = controller;
        this.window = window;
        this.window.setResizable(false);
        window.setAlwaysOnTop(controller.stage.isAlwaysOnTop());
        setStylesheet();
        if (this.guiController.profileManager.useSearch) {
            this.addSearch();
        }
        this.searchBox.requestFocus();
        window.setAlwaysOnTop(true);
        window.toFront();
    }

    public void addSearch() {
        this.searchBox.getEditor().textProperty().addListener(new ChangeListener<String>(){
            public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                if (!AddDialogController.this.searchBox.getItems().contains(AddDialogController.this.searchBox.getValue())) {
                    AddDialogController.this.handleSearch(AddDialogController.this.searchBox.getEditor().getText());
                }
            }
        });
    }

    public void handleSearch(String string) {
        if (string == null || string.isEmpty()) {
            return;
        }
        ArrayList<String> keyArray = new ArrayList<String>();
        keyArray.addAll(this.guiController.profileManager.limitMap.keySet());
        ArrayList<String> matches = new ArrayList<String>();
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
        if (this.searchBox.getItems() != null) {
            this.searchBox.getItems().clear();
        }
        for (String match : matches) {
            if (match != null) {
                this.searchBox.getItems().add(match);
            }
            Logger.Log("Search match: " + match);
        }
        this.searchBox.show();
    }

    public void setValues(GuiController controller, Stage window, ItemPanel panel) {
        if (panel == null) {
            Logger.Log("no panel selected, let's ignore");
            return;
        }
        this.guiController = controller;
        this.window = window;
        this.window.setResizable(false);
        this.itemPanel = panel;
        this.add.setText("Edit");
        window.setAlwaysOnTop(controller.stage.isAlwaysOnTop());
        setStylesheet();
        this.searchBox.setValue(this.itemPanel.itemName);
        this.priceField.setText("" + this.itemPanel.price);
        this.cBox.setSelected(this.itemPanel.getType().equals("Buy"));
        this.amountField.setText("" + this.itemPanel.amount);
        this.timeSinceStartedField.setText("" + this.itemPanel.timeAfter);
        if (this.guiController.profileManager.useSearch) {
            this.addSearch();
        }
        this.searchBox.requestFocus();
        window.setAlwaysOnTop(true);
        window.toFront();
    }

    @FXML
    protected void showAction(Event event) {
        if (this.searchBox.getEditor().getText().isEmpty() && this.searchBox.getItems() != null) {
            this.searchBox.getItems().clear();
            this.searchBox.getItems().addAll(this.guiController.profileManager.recent.split(":"));
        }
    }

    @FXML
    protected void handleAction(ActionEvent event) {
        if (event.getSource().getClass().equals(Button.class)) {
            Button button = (Button)event.getSource();
            switch (button.getText()) {
                case "Add": {
                    try {
                        if (this.guiController.profileManager.useItemsWithLimits && !this.guiController.profileManager.limitMap.containsKey(this.searchBox.getValue())) {
                            Alert alert = new Alert(Alert.AlertType.ERROR);
                            alert.setTitle("Item not found");
                            alert.setContentText("Item given has no limit, please add a limit to it or" + System.getProperty("line.separator") + "un-tick the" + " 'only allow items with limit' box" + System.getProperty("line.separator") + "from settings");
                            alert.showAndWait();
                            return;
                        }
                        this.priceField.setText(this.priceField.getText().toLowerCase().replaceAll("k", "000"));
                        this.priceField.setText(this.priceField.getText().toLowerCase().replaceAll("m", "000000"));
                        this.type = this.cBox.isSelected() ? "Buy" : "Sell";
                        this.itemName = this.searchBox.getValue();
                        this.price = Integer.parseInt(this.priceField.getText());
                        int amt = Integer.parseInt(this.amountField.getText());
                        if (amt <= 0) {
                            Logger.Log("Invalid number");
                            return;
                        }
                        this.amount = amt;
                        try {
                            this.timeAfter = Integer.parseInt(this.timeSinceStartedField.getText());
                        }
                        catch (NumberFormatException nfe) {
                            this.timeAfter = 0;
                        }
                        this.guiController.addItem(this.type, this.itemName, this.price, this.amount, this.guiController.getDate().getTime() - (long)(this.timeAfter * 1000 * 60));
                        this.guiController.profileManager.addRecent(this.itemName);
                        this.window.close();
                        Platform.runLater(this.guiController.minuteTask);
                        this.guiController.profileManager.saveLogFile();
                        break;
                    }
                    catch (NumberFormatException e) {
                        Logger.Log("Invalid number");
                        return;
                    }
                }
                case "Edit": {
                    try {
                        if (this.guiController.profileManager.useItemsWithLimits && !this.guiController.profileManager.limitMap.containsKey(this.searchBox.getValue())) {
                            Alert alert = new Alert(Alert.AlertType.ERROR);
                            alert.setTitle("Item not found");
                            alert.setContentText("Item given has no limit, please add a limit to it or" + System.getProperty("line.separator") + "un-tick the" + " 'only allow items with limit' box" + System.getProperty("line.separator") + "from settings");
                            alert.showAndWait();
                            return;
                        }
                        this.priceField.setText(this.priceField.getText().toLowerCase().replaceAll("k", "000"));
                        this.priceField.setText(this.priceField.getText().toLowerCase().replaceAll("m", "000000"));
                        this.type = this.cBox.isSelected() ? "Buy" : "Sell";
                        this.itemName = this.searchBox.getValue();
                        this.price = Integer.parseInt(this.priceField.getText());
                        int amt = Integer.parseInt(this.amountField.getText());
                        if (amt <= 0) {
                            Logger.Log("Invalid number");
                            return;
                        }
                        this.amount = amt;
                        try {
                            this.timeAfter = Integer.parseInt(this.timeSinceStartedField.getText());
                        }
                        catch (NumberFormatException nfe) {
                            this.timeAfter = 0;
                        }
                        if (this.itemPanel.complete.getText().equals("Complete")) {
                            this.itemPanel.edit(this.itemName, this.price, this.amount, this.type, this.guiController.getDate().getTime(), this.timeAfter);
                        } else {
                            this.itemPanel.edit(this.itemName, this.price, this.amount, this.type, this.itemPanel.getTime(), this.timeAfter);
                        }
                        this.window.close();
                        Platform.runLater(this.guiController.minuteTask);
                        this.guiController.profileManager.saveLogFile();
                        return;
                    }
                    catch (NumberFormatException e) {
                        Logger.Log("Invalid number");
                        return;
                    }
                }
                default: {
                    Logger.Log("Invalid command");
                }
            }
        }
    }

}

