package fliptracker.UIComponents.Controllers;

import fliptracker.Audio.AudioHandler;
import fliptracker.Audio.SoundEffect;
import fliptracker.Main;
import fliptracker.UIComponents.AddDialog;
import fliptracker.UIComponents.ItemNameInput;
import fliptracker.UIComponents.ItemPanel;
import fliptracker.Utils.FileManager;
import fliptracker.Utils.Logger;
import fliptracker.Utils.ProfileManager;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;
import javafx.scene.web.WebView;
import javafx.stage.Stage;

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Optional;

/**
 * Controller for the main UI
 */
public class GuiController {

    public AnchorPane rootPane;
    public ListView<ItemPanel> activeItems;
    public ListView<ItemPanel> logItems;
    public TextArea noteArea;
    public TextField profileField;
    public TextField notesField;
    public TextField ruleField;
    public Label profitLabel;
    public CheckBox useWikiBox;
    public CheckBox alwaysOnTop;
    public CheckBox useSearchBox;
    public CheckBox useSoundBox;
    public CheckBox useRuleTimerBox;
    public ChoiceBox<String> themeBox;
    public ComboBox<String> addressBox;
    public ComboBox<String> ruleBox;
    public LineChart<String, Integer> profitChart;
    public WebView forums;
    public WebView marginshare;

    private final DateFormat dayFormat = new SimpleDateFormat("dd/MM");
    private final DateFormat dateOfyear = new SimpleDateFormat("DD");

    public final AudioHandler audioHandler = new AudioHandler();
    public final FileManager fileManager = new FileManager();
    public final ProfileManager profileManager;

    private final TextInputDialog input;
    private final Alert alert;

    private final Runnable limitCheck;
    private final Runnable saveTask;
    private final Runnable minuteTask;

    private final Thread taskThread;

    private boolean runThreads = true;

    private int totalProfit = 0;

    private String notes;

    private Stage stage;

    /**
     * Construct the controller, initiate settings etc.
     */
    public GuiController() {
        profileManager = new ProfileManager(this);
        input = new TextInputDialog("");
        alert = new Alert(Alert.AlertType.INFORMATION);
        saveTask = () -> {
            profileManager.save();
            updateProfits();
        };
        minuteTask = () -> {
            for (int i = 0; i < activeItems.getItems().size(); ++i) {
                ItemPanel item = activeItems.getItems().get(i);
                item.duration = (int) ((getDate().getTime() - item.getTime()) / 1000 / 60);
                item.durationLabel.setText("" + item.getDuration() + " mins");
                if (item.getDuration() != profileManager.ruleTime || !profileManager.useRuleTimer || !profileManager.useSound)
                    continue;
                if (profileManager.ruleSound == null) {
                    audioHandler.playSound("Rule");
                    continue;
                }
                audioHandler.playSound("Custom");
            }
        };
        limitCheck = () -> {
            for (int i = 0; i < logItems.getItems().size(); ++i) {
                ItemPanel item = logItems.getItems().get(i);
                item.durationLabel.setText("" + item.getDuration() + " mins");
                if (item.getType().equals("Sell")) continue;
                if (!item.isOnCooldown()) {
                    item.setOnCooldown(false);
                    continue;
                }
                if (getDate().getTime() > item.getTime() + 14400000) {
                    if (profileManager.useSound)
                        audioHandler.playSound("Done");
                    item.setOnCooldown(false);
                    continue;
                }
                item.setOnCooldown(true);
            }
            profitLabel.setText("Total profit: " + totalProfit);
            profileManager.saveMargins();
        };
        taskThread = new Thread() {
            @Override
            public void run() {
                int minuteTick = 0;
                int mediumTick = 0;
                while (runThreads) {
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    if (minuteTick == 60) {
                        Platform.runLater(minuteTask);
                        minuteTick = 0;
                    } else {
                        ++minuteTick;
                    }
                    if (mediumTick == 10) {
                        Platform.runLater(saveTask);
                    } else {
                        ++mediumTick;
                    }
                    Platform.runLater(limitCheck);
                }
            }
        };
    }

    /**
     * Initiate all ui values, has to be done after the UI has loaded
     */
    public void initFields() {
        loadUp();
        forums.getEngine().loadContent("<html><body style=''><h1 align=center style=\"font-size:4em; text-shadow: 1px 5px 2px #333;\n\">Fliptracker</h1>\n<h2 align=center style=\"text-decoration:underline;\">Update notes</h2>\n<ul>\n" + this.fileManager.readUpdates() + "</ul></body></html>");
        marginshare.getEngine().load("http://marginsharers.byethost9.com");
        taskThread.start();
        themeBox.getItems().addAll("Dark", "Gray", "Light");
        ruleBox.getItems().addAll("15", "30", "45", "60", "75", "90", "105", "120", "135", "150", "165", "180");
        ruleBox.setValue(("" + profileManager.ruleTime));
        themeBox.valueProperty().addListener((observable, oldValue, newValue) -> {
            profileManager.currentTheme = themeBox.getSelectionModel().getSelectedIndex();
            profileManager.getTheme();
        });
        ruleBox.getEditor().textProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue.isEmpty()) {
                return;
            }
            try {
                profileManager.ruleTime = Integer.parseInt(newValue);
            } catch (NumberFormatException nfe) {
                ruleBox.getEditor().setText(oldValue);
            }
        });
        ruleBox.valueProperty().addListener((observable, oldValue, newValue) -> {
            profileManager.ruleTime = Integer.parseInt(newValue);
        });
        addressBox.valueProperty().addListener((observable, oldValue, newValue) -> {
            forums.getEngine().load(addressBox.getValue());
        });
        try {
            themeBox.setValue(themeBox.getItems().get(profileManager.currentTheme));
            profileManager.getTheme();
        } catch (IndexOutOfBoundsException iobe) {
            Logger.Log("Error finding the correct theme! Maybe it was deleted, falling back to default.");
        }
        Platform.runLater(minuteTask);
        updateProfits();
    }

    /**
     * Update the profits graph
     */
    public void updateProfits() {
        ObservableList<XYChart.Series<String, Integer>> lineChartData = FXCollections.observableArrayList();
        XYChart.Series<String, Integer> series1 = new XYChart.Series<>();
        series1.setName("Daily profit");
        XYChart.Series profitSeries = new XYChart.Series<>();
        profitSeries.setName("Total profit");
        totalProfit = 0;
        long date = this.getDate().getTime();
        int[] amountHandled = new int[logItems.getItems().size()];
        int profitToday = 0;
        for (int i = 0; i < 5; ++i) {
            int currentDay = Integer.parseInt(dateOfyear.format(new Date(date - (long) (86400000 * i))));
            block1:
            for (int x = 0; x < logItems.getItems().size(); ++x) {
                ItemPanel item = logItems.getItems().get(x);
                if (Integer.parseInt(dateOfyear.format(new Date(item.getTime()))) != currentDay || !item.type.equals("Buy"))
                    continue;
                int amountCounted = 0;
                for (int z = x; z >= 0; --z) {
                    ItemPanel item2 = logItems.getItems().get(z);
                    if (!item2.getType().equals("Sell") || !item2.itemName.equals(item.itemName) || item2.amount == amountHandled[z])
                        continue;
                    if (item2.amount - amountHandled[z] >= item.amount - amountCounted) {
                        amountHandled[z] = amountHandled[z] + (item.amount - amountCounted);
                        profitToday -= (item.amount - amountCounted) * item.price;
                        profitToday += (item.amount - amountCounted) * item2.price;
                        continue block1;
                    }
                    profitToday -= (item2.amount - amountHandled[z]) * item.price;
                    profitToday += (item2.amount - amountHandled[z]) * item2.price;
                    amountHandled[z] = item2.amount;
                    amountCounted += item2.amount - amountHandled[z];
                }
            }
            totalProfit += profitToday;
            series1.getData().add(new XYChart.Data<>(dayFormat.format(new Date(date - (long) (86400000 * i))), profitToday));
            profitToday = 0;
        }
        lineChartData.add(series1);
        profitChart.getData().clear();
        profitChart.setData(lineChartData);
        profitChart.createSymbolsProperty();
    }

    /**
     * Set the UI properties
     */
    public void loadUp() {
        useWikiBox.setSelected(profileManager.useItemsWithLimits);
        useSoundBox.setSelected(profileManager.useSound);
        useSearchBox.setSelected(profileManager.useSearch);
        useRuleTimerBox.setSelected(profileManager.useRuleTimer);
        profileManager.updateMargins();
        profileField.setText(profileManager.profileName);
        notesField.setText("" + profileManager.marginFile);
        ruleField.setText(profileManager.ruleSound == null ? "Default" : "" + profileManager.ruleSound);
        if (profileManager.ruleSound != null) {
            audioHandler.soundEffect.add(2, new SoundEffect(profileManager.ruleSound));
        }
        fileManager.load(activeItems.getItems(), logItems.getItems(), profileManager.getLogFile(), profileManager.controller);
        if (addressBox.getItems() != null) {
            addressBox.getItems().clear();
        }
        addressBox.getItems().addAll(profileManager.websites.split("::"));
    }

    /**
     * Add a flip item to the active items
     *
     * @param type     buy/sell
     * @param itemName name of the item
     * @param price    price of the item
     * @param amount   number of the items
     * @param date     the time of the offer
     */
    public void addItem(String type, String itemName, int price, int amount, long date) {
        ItemPanel pane = new ItemPanel(itemName, price, amount, type, date, this);
        activeItems.getItems().add(pane);
    }

    /**
     * Add a flip item to the active items
     *
     * @param type     buy/sell
     * @param itemName name of the item
     * @param price    price of the item
     * @param amount   number of the items
     * @param date     the time of the offer
     */
    public void addLogItem(String type, String itemName, int price, int amount, long date, int duration) {
        ItemPanel pane = new ItemPanel(itemName, price, amount, type, date, this);
        pane.duration = duration;
        pane.setLog();
        logItems.getItems().add(pane);
    }

    /**
     * Add a flip to the log
     *
     * @param itemPanel the panel containing the to be logged flips data
     */
    public void addLogItem(ItemPanel itemPanel) {
        itemPanel.setDate(getDate().getTime());
        itemPanel.setLog();
        logItems.getItems().add(0, itemPanel);
    }

    /**
     * Return the current date
     *
     * @return date
     */
    Date getDate() {
        return new Date();
    }

    /**
     * Handle the shutdown of the program
     */
    public void doClose() {
        profileManager.save();
        profileManager.saveAll();
        runThreads = false;
        Logger.Log("Close button clicked: Shutting down..");
        System.exit(0);
    }

    /**
     * Fetch the latest margin on a specific item
     *
     * @param itemName the name of the item
     * @return the margin
     */
    public int[] getMargin(String itemName) {
        int sellPrice = 0;
        int buyPrice = 0;
        for (int i = 0; i < logItems.getItems().size(); ++i) {
            if (!logItems.getItems().get(i).itemName.equals(itemName)) continue;
            if (logItems.getItems().get(i).type.equals("Buy") && buyPrice == 0) {
                buyPrice = logItems.getItems().get(i).price;
                continue;
            }
            if (sellPrice != 0) continue;
            sellPrice = logItems.getItems().get(i).price;
        }
        return new int[]{buyPrice, sellPrice};
    }

    /**
     * Check the cooldown on a specific item
     *
     * @param itemName the name of the item
     * @return the cooldown (how long until its refreshed and how many items still remain from the cap)
     */
    public String checkCooldown(String itemName) {
        String message;
        int onCooldown = 0;
        HashMap<String, Integer> limitMap = profileManager.getLimits();
        int limit = 0;
        if (limitMap.containsKey(itemName))
            limit = limitMap.get(itemName);
        for (int i = 0; i < logItems.getItems().size(); ++i) {
            ItemPanel item = logItems.getItems().get(i);
            if (!item.itemName.equalsIgnoreCase(itemName) || !item.getType().equals("Buy") || !item.isOnCooldown())
                continue;
            onCooldown += item.amount;
        }
        message = limit != 0 ? "" + onCooldown + " " + itemName + "(s) on cooldown, limit is " + limit + " (" + (limit - onCooldown) + " left)" : "" + onCooldown + " " + itemName + "(s) on cooldown, no limit set for this item";
        return message;
    }

    /**
     * Set the stage of this window
     *
     * @param stage
     */
    public void setStage(Stage stage) {
        this.stage = stage;
    }

    /**
     * Fetch the stage of this window
     *
     * @return stage
     */
    Stage getStage() {
        return stage;
    }

    /**
     * Fetch the task to be ran once a minute
     *
     * @return minuteTask
     */
    Runnable getMinuteTask() {
        return minuteTask;
    }

    /**
     * Set the text of the notes area
     * @param notes
     */
    public void setNotes(String notes){
        this.notes = notes;
    }

    /**
     * Fetch the text of the notes area
     * @return
     */
    public String getNotes(){
        return notes;
    }

    /**
     * Handle settings tab buttons
     * @param event
     */
    @FXML
    protected void handleSettingsClick(ActionEvent event) {
        if (event.getSource().getClass().equals(CheckBox.class)) {
            CheckBox checkBox = (CheckBox) event.getSource();
            switch (checkBox.getText()) {
                case "Only allow items with limits": {
                    profileManager.useItemsWithLimits = checkBox.isSelected();
                    Logger.Log("Wiki command");
                    break;
                }
                case "Always on top": {
                    if (stage != null)
                        stage.setAlwaysOnTop(alwaysOnTop.isSelected());
                    Logger.Log("Always on top command");
                    break;
                }
                case "Use sound": {
                    profileManager.useSound = useSoundBox.isSelected();
                    Logger.Log("Use sound command");
                    break;
                }
                case "Use search": {
                    profileManager.useSearch = useSearchBox.isSelected();
                    Logger.Log("Use search command");
                    break;
                }
                case "Use rule timer": {
                    profileManager.useRuleTimer = useRuleTimerBox.isSelected();
                    Logger.Log("Use rule timer");
                    break;
                }
                default: {
                    Logger.Log("Invalid command " + checkBox.getText());
                }
            }
        }
        if (event.getSource().getClass().equals(Button.class)) {
            Button button = (Button) event.getSource();
            switch (button.getText()) {
                case "Load": {
                    if (profileField.getText().isEmpty())
                        return;
                    profileManager.loadProfile(profileField.getText(), true);
                    Logger.Log("Load command");
                    break;
                }
                case "Choose": {
                    profileManager.chooseNotesFile();
                    Logger.Log("Choose notes command");
                    break;
                }
                case "Select": {
                    profileManager.chooseRuleFile();
                    Logger.Log("Choose rule sound");
                    break;
                }
                default: {
                    Logger.Log("Invalid command");
                }
            }
        }
    }

    /**
     * Open the edit dialogue for the data of the specified itemPanel
     * @param itemPanel the itemPanel
     */
    public void editCommand(ItemPanel itemPanel) {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(Main.class.getResource("UIComponents/FXML/AddItem.fxml"));
            Parent root = fxmlLoader.load();
            AddDialogController controller = fxmlLoader.getController();
            AddDialog addDialog = new AddDialog(new Scene(root, 320.0, 138.0), this, controller, itemPanel);
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
    }

    /**
     * Handle main GUI buttons
     * @param event
     */
    @FXML
    protected void handleButtonAction(ActionEvent event) {
        Stage nameInput2;
        Optional<String> itemName;
        if (event.getSource().getClass().equals(MenuItem.class)) {
            MenuItem item = (MenuItem) event.getSource();
            switch (item.getText()) {
                case "Close": {
                    doClose();
                    break;
                }
                case "Dump wiki limits": {
                    profileManager.dumpWikiLimits();
                    alert.setAlertType(Alert.AlertType.INFORMATION);
                    alert.setTitle("Dump limits");
                    alert.setHeaderText(null);
                    alert.setContentText("All wiki limits dumped!");
                    alert.showAndWait();
                    profileManager.saveLimits();
                    break;
                }
                case "New": {
                    input.setHeaderText("Enter the profile name");
                    input.setTitle("Create profile");
                    input.showAndWait().ifPresent(profileManager::createProfile
                    );
                    Logger.Log("Create profile");
                    break;
                }
                case "Load": {
                    input.setHeaderText("Enter the profile name to load");
                    input.setTitle("Load profile");
                    input.showAndWait().ifPresent(profile -> profileManager.loadProfile(profile, true)
                    );
                    Logger.Log("Load profile");
                    break;
                }
                case "Add limit": {
                    input.setHeaderText("Enter the item name");
                    input.setTitle("Add limit");
                    itemName = input.showAndWait();
                    if (!itemName.isPresent()) {
                        return;
                    }
                    input.setHeaderText("Enter the item limit");
                    Optional<String> limitStr = input.showAndWait();
                    if (!limitStr.isPresent()) {
                        return;
                    }
                    int limit = 0;
                    try {
                        limit = Integer.parseInt(limitStr.get().toString());
                    } catch (NumberFormatException nfe) {
                        nfe.printStackTrace();
                    }
                    profileManager.addLimit(itemName.get(), limit);
                    profileManager.saveLimits();
                    break;
                }
                case "Get margin": {
                    ItemNameInput nameInput = new ItemNameInput(this, ItemNameInput.MARGIN);
                    break;
                }
                case "Check cooldown": {
                    nameInput2 = new ItemNameInput(this, ItemNameInput.COOLDOWN);
                    break;
                }
                default: {
                    Logger.Log("Invalid Command");
                }
            }
        }
        if (event.getSource().getClass().equals(Button.class)) {
            Button button = (Button) event.getSource();
            switch (button.getText()) {
                case "Add": {
                    try {
                        FXMLLoader fxmlLoader = new FXMLLoader(Main.class.getResource("UIComponents/FXML/AddItem.fxml"));
                        Parent root = fxmlLoader.load();
                        AddDialogController controller = fxmlLoader.getController();
                        nameInput2 = new AddDialog(new Scene(root, 320.0, 138.0), this, controller);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    Logger.Log("Add command");
                    break;
                }
                case "Delete": {
                    activeItems.getItems().remove(activeItems.getSelectionModel().getSelectedItem());
                    profileManager.saveLogFile();
                    Logger.Log("Delete command");
                    break;
                }
                case "Save": {
                    fileManager.save(activeItems.getItems(), logItems.getItems(), this);
                    Logger.Log("Save command");
                    break;
                }
                case "Load": {
                    fileManager.load(activeItems.getItems(), logItems.getItems(), this);
                    Logger.Log("Load command");
                    break;
                }
                default: {
                    Logger.Log("Invalid command");
                }
            }
        }
    }
}

