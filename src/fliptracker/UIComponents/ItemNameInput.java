package fliptracker.UIComponents;

import fliptracker.UIComponents.Controllers.GuiController;
import fliptracker.Utils.Logger;
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

import java.util.ArrayList;

/**
 * Item name input, used for receiving string values
 */
public class ItemNameInput extends Stage {

    /**
     * Input types
     */
    public static final int COOLDOWN = 1;
    public static final int MARGIN = 2;

    private final GuiController controller;

    private final int type;
    private final ComboBox<String> inputField;

    /**
     * Create the input form
     * @param controller guiController
     * @param type the type of the item,
     */
    public ItemNameInput(GuiController controller, int type) {
        this.controller = controller;
        this.type = type;
        Button complete = new Button("Submit");
        complete.setOnAction(this::handleButtonPress);
        complete.setMinWidth(80.0);
        inputField = new ComboBox<>();
        Label textLabel = new Label("");
        textLabel.setWrapText(true);
        switch (type) {
            case 1: {
                textLabel.setText("Enter the name of the item of which cooldown you want to check");
                setTitle("Cooldown check");
                break;
            }
            case 2: {
                textLabel.setText("Enter the name of the item of which margin you want to check");
                setTitle("Margin check");
                break;
            }
            default: {
                setTitle("ItemName input dialog");
                textLabel.setText("ItemName input dialog");
            }
        }
        inputField.setOnShown(e -> {
                    if (inputField.getEditor().getText().isEmpty() && inputField.getItems() != null) {
                        inputField.getItems().clear();
                        inputField.getItems().addAll(controller.profileManager.recent.split(":"));
                    }
                }
        );
        GridPane.setConstraints(complete, 2, 1);
        GridPane.setConstraints(inputField, 0, 1);
        GridPane.setConstraints(textLabel, 0, 0);
        GridPane.setColumnSpan(textLabel, 2);
        inputField.setEditable(true);
        GridPane container = new GridPane();
        ColumnConstraints columnConstraints = new ColumnConstraints();
        columnConstraints.setFillWidth(true);
        columnConstraints.setHgrow(Priority.ALWAYS);
        container.getColumnConstraints().add(columnConstraints);
        container.setPadding(new Insets(20.0, 10.0, 10.0, 10.0));
        container.setHgap(5.0);
        container.setVgap(10.0);
        container.getChildren().addAll(complete, inputField, textLabel);
        container.setId("addDialog");
        addSearch();
        setScene(new Scene(container, 350.0, 100.0));
        getScene().getStylesheets().add(controller.profileManager.cssUrls.get(controller.profileManager.currentTheme));
        setResizable(false);
        setAlwaysOnTop(true);
        show();
    }

    /**
     * Add the search function
     */
    private void addSearch() {
        this.inputField.getEditor().textProperty().addListener((observable, oldValue, newValue) -> {
            if (!inputField.getItems().contains(inputField.getValue()))
                handleSearch(inputField.getEditor().getText());
        });
    }

    /**
     * Search items with the given keyword
     * @param string the keyword
     */
    private void handleSearch(String string) {
        if (string == null || string.isEmpty())
            return;
        ArrayList<String> keyArray = new ArrayList<>();
        keyArray.addAll(controller.profileManager.limitMap.keySet());
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
        if (inputField.getItems() != null)
            inputField.getItems().clear();
        for (String match : matches) {
            if (match != null)
                inputField.getItems().add(match);
            Logger.Log("Search match: " + match);
        }
        inputField.show();
    }

    /**
     * Handle button clicks
     * @param event
     */
    private void handleButtonPress(ActionEvent event) {
        if (event.getSource().getClass().equals(Button.class)) {
            Button button = (Button) event.getSource();
            block3:
            switch (button.getText()) {
                case "Submit": {
                    Alert alert = new Alert(Alert.AlertType.INFORMATION);
                    switch (type) {
                        case 1: {
                            if (inputField.getValue().isEmpty()) {
                                return;
                            }
                            alert.setTitle("Check cooldown");
                            alert.setHeaderText(null);
                            alert.setContentText(controller.checkCooldown(inputField.getValue()));
                            close();
                            alert.showAndWait();
                            break block3;
                        }
                        case 2: {
                            if (inputField.getValue().isEmpty()) {
                                return;
                            }
                            int[] margin = controller.getMargin(inputField.getValue());
                            alert.setTitle("Get margin");
                            alert.setHeaderText(null);
                            alert.setContentText("Margin for " + inputField.getValue() + " is " + margin[0] + "-" + margin[1]);
                            close();
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

