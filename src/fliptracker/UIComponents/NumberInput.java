package fliptracker.UIComponents;

import fliptracker.UIComponents.Controllers.GuiController;
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

/**
 * Used for receiving a number input by UI
 */
class NumberInput extends Stage {

    /**
     * Input type
     */
    private static final int COMPLETE = 1;


    private final GuiController controller;

    private final TextField inputField;

    private final int type;

    private ItemPanel item;

    /**
     * Create the input field
     *
     * @param controller the ui controller
     */
    NumberInput(GuiController controller) {
        this.controller = controller;
        type = NumberInput.COMPLETE;
        Button complete = new Button("Submit");
        complete.setOnAction(this::handleButtonPress);
        complete.setMinWidth(80.0);
        inputField = new TextField("");
        Label textLabel = new Label("");
        textLabel.setWrapText(true);

        //set the prompts based on type
        switch (type) {
            case NumberInput.COMPLETE:
                textLabel.setText("Enter the number of items to complete");
                setTitle("Complete offer");
                break;
            default:
                setTitle("Number input dialog");
                textLabel.setText("Number input dialog");
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
        setScene(new Scene(container, 350.0, 100.0));

        //Set the theme
        String theme = controller.profileManager.getThemeCss();
        if (theme == null) getScene().getStylesheets().clear();
        else getScene().getStylesheets().add(controller.profileManager.getThemeCss());

        setResizable(false);
        setAlwaysOnTop(true);
        show();
    }

    /**
     * Set the item to input number to
     *
     * @param item new item
     */
    public void setItem(ItemPanel item) {
        this.item = item;
    }

    /**
     * Handle buttons
     *
     * @param event the action event
     */
    private void handleButtonPress(ActionEvent event) {
        if (event.getSource().getClass().equals(Button.class)) {
            Button button = (Button) event.getSource();
            switch (button.getText()) {
                case "Submit": {
                    switch (type) {
                        case 1: {
                            int amount;
                            if (item == null) return;
                            try {
                                amount = Integer.parseInt(inputField.getText());
                            } catch (NumberFormatException nfe) {
                                Logger.Log("Invalid number don't do anything");
                                return;
                            }
                            if (amount <= 0) {
                                Logger.Log("Negative amount -> return");
                                return;
                            }
                            if (item.amount - amount <= 0) {
                                controller.addLogItem(item);
                                controller.activeItems.getItems().remove(item);
                                close();
                            } else {
                                item.amount -= amount;
                                ItemPanel pane =
                                        new ItemPanel(item.itemName, item.price, amount, item.type, item.getTime(), controller);
                                pane.duration = item.duration;
                                controller.addLogItem(pane);
                                item.update();
                                close();
                            }
                            controller.profileManager.saveLogFile();
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

