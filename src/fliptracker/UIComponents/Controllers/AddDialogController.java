package fliptracker.UIComponents.Controllers;

import fliptracker.UIComponents.ItemPanel;
import fliptracker.Utils.Logger;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;

import java.util.ArrayList;

/**
 * Controller for the add item dialog, also used for editing
 */

public class AddDialogController {

    public CheckBox cBox;
    public TextField priceField;
    public TextField amountField;
    public TextField timeSinceStartedField;
    public Label timeLabel;
    public ComboBox<String> searchBox;
    public Pane rootPane;
    public Button add;

    private GuiController guiController;
    private Stage window;
    private ItemPanel itemPanel;

    /**
     * Item data
     */
    private int price;
    private int amount;
    private int timeAfter;
    private String itemName;
    private String type;

    /**
     * Set the style according to the profile managers chosen theme
     */
    private void setStylesheet() {
        rootPane.getStylesheets().clear();
        try {
            rootPane.getStylesheets().add(guiController.profileManager.cssUrls.get(guiController.profileManager.currentTheme));
        } catch (IndexOutOfBoundsException iobe) {
            //No value so go default
        }
    }

    /**
     * Apply the default values
     *
     * @param controller guiController
     * @param window     the stage for this
     */
    public void setValues(GuiController controller, Stage window) {
        guiController = controller;
        this.window = window;
        this.window.setResizable(false);
        window.setAlwaysOnTop(controller.getStage().isAlwaysOnTop());
        setStylesheet();
        if (guiController.profileManager.useSearch)
            addSearch();
        searchBox.requestFocus();
        window.setAlwaysOnTop(true);
        window.toFront();
    }

    /**
     * Add the live search function to the text box, only call this if selected on settings
     */
    private void addSearch() {
        searchBox.getEditor().textProperty().addListener((observable, oldValue, newValue) -> {
            if (!searchBox.getItems().contains(searchBox.getValue()))
                handleSearch(searchBox.getEditor().getText());
        });
    }

    /**
     * Search for the item
     *
     * @param string keyword
     */
    private void handleSearch(String string) {
        if (string == null || string.isEmpty()) {
            return;
        }
        ArrayList<String> keyArray = new ArrayList<>();
        keyArray.addAll(guiController.profileManager.limitMap.keySet());
        ArrayList<String> matches = new ArrayList<>();
        for (String key : keyArray) {
            if (!key.toLowerCase().contains(string.toLowerCase())) continue;
            matches.add(key);
            if (matches.size() <= 15) continue;
            Logger.Log("Too many, stop searching");
            matches.clear();
            return;
        }
        if (matches.size() == 0)
            return;
        if (searchBox.getItems() != null)
            searchBox.getItems().clear();
        for (String match : matches) {
            if (match != null)
                searchBox.getItems().add(match);
            Logger.Log("Search match: " + match);
        }
        searchBox.show();
    }

    /**
     * Apply the values gotten from the selected item panel (for editing)
     *
     * @param controller guiController
     * @param window     the stage
     * @param panel      the item panel to read data from
     */
    public void setValues(GuiController controller, Stage window, ItemPanel panel) {
        if (panel == null) {
            Logger.Log("no panel selected, let's ignore");
            return;
        }
        guiController = controller;
        this.window = window;
        window.setResizable(false);
        itemPanel = panel;
        add.setText("Edit");
        window.setAlwaysOnTop(controller.getStage().isAlwaysOnTop());
        setStylesheet();
        searchBox.setValue(itemPanel.itemName);
        priceField.setText("" + itemPanel.price);
        cBox.setSelected(itemPanel.getType().equals("Buy"));
        amountField.setText("" + itemPanel.amount);
        timeSinceStartedField.setText("" + itemPanel.timeAfter);
        if (guiController.profileManager.useSearch)
            addSearch();
        searchBox.requestFocus();
        window.setAlwaysOnTop(true);
        window.toFront();
    }

    /**
     * Show dropdown menu action
     * @param event the action event
     */
    @FXML
    protected void showAction(Event event) {
        if (searchBox.getEditor().getText().isEmpty() && searchBox.getItems() != null) {
            searchBox.getItems().clear();
            searchBox.getItems().addAll(guiController.profileManager.recent.split(":"));
        }
    }

    /**
     * Handle button actions
     * @param event the action event
     */
    @FXML
    protected void handleAction(ActionEvent event) {
        if (event.getSource().getClass().equals(Button.class)) {
            Button button = (Button) event.getSource();
            switch (button.getText()) {
                case "Add": {
                    try {
                        if (guiController.profileManager.useItemsWithLimits && !guiController.profileManager.limitMap.containsKey(searchBox.getValue())) {
                            Alert alert = new Alert(Alert.AlertType.ERROR);
                            alert.setTitle("Item not found");
                            alert.setContentText("Item given has no limit, please add a limit to it or" + System.getProperty("line.separator") + "un-tick the" + " 'only allow items with limit' box" + System.getProperty("line.separator") + "from settings");
                            alert.showAndWait();
                            return;
                        }
                        priceField.setText(priceField.getText().toLowerCase().replaceAll("k", "000"));
                        priceField.setText(priceField.getText().toLowerCase().replaceAll("m", "000000"));
                        type = cBox.isSelected() ? "Buy" : "Sell";
                        itemName = searchBox.getValue();
                        price = Integer.parseInt(priceField.getText());
                        int amt = Integer.parseInt(amountField.getText());
                        if (amt <= 0) {
                            Logger.Log("Invalid number");
                            return;
                        }
                        amount = amt;
                        try {
                            timeAfter = Integer.parseInt(timeSinceStartedField.getText());
                        } catch (NumberFormatException nfe) {
                            timeAfter = 0;
                        }
                        guiController.addItem(type, itemName, price, amount, guiController.getDate().getTime() - (long) (timeAfter * 1000 * 60));
                        guiController.profileManager.addRecent(itemName);
                        window.close();
                        Platform.runLater(guiController.getMinuteTask());
                        guiController.profileManager.saveLogFile();
                        break;
                    } catch (NumberFormatException e) {
                        Logger.Log("Invalid number");
                        return;
                    }
                }
                case "Edit": {
                    try {
                        if (guiController.profileManager.useItemsWithLimits && !guiController.profileManager.limitMap.containsKey(searchBox.getValue())) {
                            Alert alert = new Alert(Alert.AlertType.ERROR);
                            alert.setTitle("Item not found");
                            alert.setContentText("Item given has no limit, please add a limit to it or" + System.getProperty("line.separator") + "un-tick the" + " 'only allow items with limit' box" + System.getProperty("line.separator") + "from settings");
                            alert.showAndWait();
                            return;
                        }
                        priceField.setText(priceField.getText().toLowerCase().replaceAll("k", "000"));
                        priceField.setText(priceField.getText().toLowerCase().replaceAll("m", "000000"));
                        type = cBox.isSelected() ? "Buy" : "Sell";
                        itemName = searchBox.getValue();
                        price = Integer.parseInt(priceField.getText());
                        int amt = Integer.parseInt(amountField.getText());
                        if (amt <= 0) {
                            Logger.Log("Invalid number");
                            return;
                        }
                        amount = amt;
                        try {
                            timeAfter = Integer.parseInt(timeSinceStartedField.getText());
                        } catch (NumberFormatException nfe) {
                            timeAfter = 0;
                        }
                        if (itemPanel.complete.getText().equals("Complete")) {
                            itemPanel.edit(itemName, price, amount, type, guiController.getDate().getTime(), timeAfter);
                        } else {
                            itemPanel.edit(itemName, price, amount, type, itemPanel.getTime(), timeAfter);
                        }
                        window.close();
                        Platform.runLater(guiController.getMinuteTask());
                        guiController.profileManager.saveLogFile();
                        return;
                    } catch (NumberFormatException e) {
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

