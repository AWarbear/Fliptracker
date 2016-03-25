
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

public class GuiController {
    public final AudioHandler audioHandler = new AudioHandler();
    public final FileManager fileManager = new FileManager();
    public final ProfileManager profileManager;
    public String notes;
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
    private final Runnable limitCheck;
    private final Runnable saveTask;
    public final Runnable minuteTask;
    private final Thread taskThread;
    public Stage stage;
    private final DateFormat dayFormat = new SimpleDateFormat("dd/MM");
    private final DateFormat dateOfyear = new SimpleDateFormat("DD");
    private final boolean runThreads = true;
    private int totalProfit = 0;
    private final TextInputDialog input;
    private final Alert alert;

    public GuiController() {
        this.profileManager = new ProfileManager(this);
        this.input = new TextInputDialog("");
        this.alert = new Alert(Alert.AlertType.INFORMATION);
        this.saveTask = () -> {
            GuiController.this.profileManager.save();
            GuiController.this.updateProfits();
        };
        this.minuteTask = () -> {
            for (int i = 0; i < GuiController.this.activeItems.getItems().size(); ++i) {
                ItemPanel item = GuiController.this.activeItems.getItems().get(i);
                item.duration = (int) ((GuiController.this.getDate().getTime() - item.getTime()) / 1000 / 60);
                item.durationLabel.setText("" + item.getDuration() + " mins");
                if (item.getDuration() != GuiController.this.profileManager.ruleTime || !GuiController.this.profileManager.useRuleTimer || !GuiController.this.profileManager.useSound)
                    continue;
                if (GuiController.this.profileManager.ruleSound == null) {
                    GuiController.this.audioHandler.playSound("Rule");
                    continue;
                }
                GuiController.this.audioHandler.playSound("Custom");
            }
        };
        this.limitCheck = () -> {
            for (int i = 0; i < GuiController.this.logItems.getItems().size(); ++i) {
                ItemPanel item = GuiController.this.logItems.getItems().get(i);
                item.durationLabel.setText("" + item.getDuration() + " mins");
                if (item.getType().equals("Sell")) continue;
                if (!item.isOnCooldown()) {
                    item.setOnCooldown(false);
                    continue;
                }
                if (GuiController.this.getDate().getTime() > item.getTime() + 14400000) {
                    if (GuiController.this.profileManager.useSound) {
                        GuiController.this.audioHandler.playSound("Done");
                    }
                    item.setOnCooldown(false);
                    continue;
                }
                item.setOnCooldown(true);
            }
            GuiController.this.profitLabel.setText("Total profit: " + GuiController.this.totalProfit);
            GuiController.this.profileManager.saveMargins();
        };
        this.taskThread = new Thread() {

            @Override
            public void run() {
                int minuteTick = 0;
                int mediumTick = 0;
                while (GuiController.this.runThreads) {
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    if (minuteTick == 60) {
                        Platform.runLater(GuiController.this.minuteTask);
                        minuteTick = 0;
                    } else {
                        ++minuteTick;
                    }
                    if (mediumTick == 10) {
                        Platform.runLater(GuiController.this.saveTask);
                    } else {
                        ++mediumTick;
                    }
                    Platform.runLater(GuiController.this.limitCheck);
                }
            }
        };
    }

    public void initFields() {
        this.loadUp();
        this.forums.getEngine().loadContent("<html><body style=''><h1 align=center style=\"font-size:4em; text-shadow: 1px 5px 2px #333;\n\">Fliptracker</h1>\n<h2 align=center style=\"text-decoration:underline;\">Update notes</h2>\n<ul>\n" + this.fileManager.readUpdates() + "</ul></body></html>");
        this.marginshare.getEngine().load("http://marginsharers.byethost9.com");
        this.taskThread.start();
        this.themeBox.getItems().addAll("Dark", "Gray","Light");
        this.ruleBox.getItems().addAll("15", "30", "45", "60", "75", "90", "105", "120", "135", "150", "165", "180");
        this.ruleBox.setValue(("" + this.profileManager.ruleTime));
        this.themeBox.valueProperty().addListener((observable, oldValue, newValue) -> {
            GuiController.this.profileManager.currentTheme = GuiController.this.themeBox.getSelectionModel().getSelectedIndex();
            GuiController.this.profileManager.getTheme();
        });
        this.ruleBox.getEditor().textProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue.isEmpty()) {
                return;
            }
            try {
                GuiController.this.profileManager.ruleTime = Integer.parseInt(newValue);
            } catch (NumberFormatException nfe) {
                GuiController.this.ruleBox.getEditor().setText(oldValue);
            }
        });
        this.ruleBox.valueProperty().addListener((observable, oldValue, newValue) -> {
            GuiController.this.profileManager.ruleTime = Integer.parseInt(newValue);
        });
        this.addressBox.valueProperty().addListener((observable, oldValue, newValue) -> {
            GuiController.this.forums.getEngine().load(GuiController.this.addressBox.getValue());
        });
        try {
            this.themeBox.setValue(this.themeBox.getItems().get(this.profileManager.currentTheme));
            this.profileManager.getTheme();
        }catch(IndexOutOfBoundsException iobe){
            Logger.Log("Error finding the correct theme! Maybe it was deleted, falling back to default.");
        }
        Platform.runLater(this.minuteTask);
        this.updateProfits();
    }

    public void updateProfits() {
        ObservableList lineChartData = FXCollections.observableArrayList();
        XYChart.Series series1 = new XYChart.Series();
        series1.setName("Daily profit");
        XYChart.Series profitSeries = new XYChart.Series();
        profitSeries.setName("Total profit");
        this.totalProfit = 0;
        long date = this.getDate().getTime();
        int[] amountHandled = new int[this.logItems.getItems().size()];
        int profitToday = 0;
        for (int i = 0; i < 5; ++i) {
            int currentDay = Integer.parseInt(this.dateOfyear.format(new Date(date - (long) (86400000 * i))));
            block1:
            for (int x = 0; x < this.logItems.getItems().size(); ++x) {
                ItemPanel item =  this.logItems.getItems().get(x);
                if (Integer.parseInt(this.dateOfyear.format(new Date(item.getTime()))) != currentDay || !item.type.equals("Buy"))
                    continue;
                int amountCounted = 0;
                for (int z = x; z >= 0; --z) {
                    ItemPanel item2 =  this.logItems.getItems().get(z);
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
            this.totalProfit += profitToday;
            series1.getData().add(new XYChart.Data(this.dayFormat.format(new Date(date - (long) (86400000 * i))), profitToday));
            profitToday = 0;
        }
        lineChartData.add(series1);
        this.profitChart.getData().clear();
        this.profitChart.setData(lineChartData);
        this.profitChart.createSymbolsProperty();
    }

    public void loadUp() {
        this.useWikiBox.setSelected(this.profileManager.useItemsWithLimits);
        this.useSoundBox.setSelected(this.profileManager.useSound);
        this.useSearchBox.setSelected(this.profileManager.useSearch);
        this.useRuleTimerBox.setSelected(this.profileManager.useRuleTimer);
        this.profileManager.updateMargins();
        this.profileField.setText(this.profileManager.profileName);
        this.notesField.setText("" + this.profileManager.marginFile);
        this.ruleField.setText(this.profileManager.ruleSound == null ? "Default" : "" + this.profileManager.ruleSound);
        if (this.profileManager.ruleSound != null) {
            this.audioHandler.soundEffect.add(2, new SoundEffect(this.profileManager.ruleSound));
        }
        this.fileManager.load( this.activeItems.getItems(),  this.logItems.getItems(), this.profileManager.getLogFile(), this.profileManager.controller);
        if (this.addressBox.getItems() != null) {
            this.addressBox.getItems().clear();
        }
        this.addressBox.getItems().addAll(this.profileManager.websites.split("::"));
    }

    public void addItem(String type, String itemName, int price, int amount, long date) {
        ItemPanel pane = new ItemPanel(itemName, price, amount, type, date, this);
        this.activeItems.getItems().add(pane);
    }

    public void addLogItem(String type, String itemName, int price, int amount, long date, int duration) {
        ItemPanel pane = new ItemPanel(itemName, price, amount, type, date, this);
        pane.duration = duration;
        pane.setLog();
        this.logItems.getItems().add(pane);
    }

    public void addLogItem(ItemPanel itemPanel) {
        itemPanel.setDate(this.getDate().getTime());
        itemPanel.setLog();
        this.logItems.getItems().add(0, itemPanel);
    }

    public Date getDate() {
        return new Date();
    }

    public void doClose() {
        this.profileManager.save();
        this.profileManager.saveAll();
        Logger.Log("Close button clicked: Shutting down..");
        System.exit(0);
    }

    public int[] getMargin(String itemName) {
        int sellPrice = 0;
        int buyPrice = 0;
        for (int i = 0; i < this.logItems.getItems().size(); ++i) {
            if (!this.logItems.getItems().get(i).itemName.equals(itemName)) continue;
            if (this.logItems.getItems().get(i).type.equals("Buy") && buyPrice == 0) {
                buyPrice = this.logItems.getItems().get(i).price;
                continue;
            }
            if (sellPrice != 0) continue;
            sellPrice = this.logItems.getItems().get(i).price;
        }
        return new int[]{buyPrice, sellPrice};
    }

    public String checkCooldown(String itemName) {
        String message;
        int onCooldown = 0;
        HashMap<String, Integer> limitMap = this.profileManager.getLimits();
        int limit = 0;
        if (limitMap.containsKey(itemName)) {
            limit = limitMap.get(itemName);
        }
        for (int i = 0; i < this.logItems.getItems().size(); ++i) {
            ItemPanel item = this.logItems.getItems().get(i);
            if (!item.itemName.equalsIgnoreCase(itemName) || !item.getType().equals("Buy") || !item.isOnCooldown())
                continue;
            onCooldown += item.amount;
        }
        message = limit != 0 ? "" + onCooldown + " " + itemName + "(s) on cooldown, limit is " + limit + " (" + (limit - onCooldown) + " left)" : "" + onCooldown + " " + itemName + "(s) on cooldown, no limit set for this item";
        return message;
    }

    public void setStage(Stage stage) {
        this.stage = stage;
    }

    @FXML
    protected void handleSettingsClick(ActionEvent event) {
        if (event.getSource().getClass().equals(CheckBox.class)) {
            CheckBox checkBox = (CheckBox) event.getSource();
            switch (checkBox.getText()) {
                case "Only allow items with limits": {
                    this.profileManager.useItemsWithLimits = checkBox.isSelected();
                    Logger.Log("Wiki command");
                    break;
                }
                case "Always on top": {
                    if (this.stage != null) {
                        this.stage.setAlwaysOnTop(this.alwaysOnTop.isSelected());
                    }
                    Logger.Log("Always on top command");
                    break;
                }
                case "Use sound": {
                    this.profileManager.useSound = this.useSoundBox.isSelected();
                    Logger.Log("Use sound command");
                    break;
                }
                case "Use search": {
                    this.profileManager.useSearch = this.useSearchBox.isSelected();
                    Logger.Log("Use search command");
                    break;
                }
                case "Use rule timer": {
                    this.profileManager.useRuleTimer = this.useRuleTimerBox.isSelected();
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
                    if (this.profileField.getText().isEmpty()) {
                        return;
                    }
                    this.profileManager.loadProfile(this.profileField.getText(), true);
                    Logger.Log("Load command");
                    break;
                }
                case "Choose": {
                    this.profileManager.chooseNotesFile();
                    Logger.Log("Choose notes command");
                    break;
                }
                case "Select": {
                    this.profileManager.chooseRuleFile();
                    Logger.Log("Choose rule sound");
                    break;
                }
                default: {
                    Logger.Log("Invalid command");
                }
            }
        }
    }

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

    @FXML
    protected void handleButtonAction(ActionEvent event) {
        Stage nameInput2;
        Optional itemName;
        if (event.getSource().getClass().equals(MenuItem.class)) {
            MenuItem item = (MenuItem) event.getSource();
            switch (item.getText()) {
                case "Close": {
                    this.doClose();
                    break;
                }
                case "Dump wiki limits": {
                    this.profileManager.dumpWikiLimits();
                    this.alert.setAlertType(Alert.AlertType.INFORMATION);
                    this.alert.setTitle("Dump limits");
                    this.alert.setHeaderText(null);
                    this.alert.setContentText("All wiki limits dumped!");
                    this.alert.showAndWait();
                    this.profileManager.saveLimits();
                    break;
                }
                case "New": {
                    this.input.setHeaderText("Enter the profile name");
                    this.input.setTitle("Create profile");
                    this.input.showAndWait().ifPresent(this.profileManager::createProfile
                    );
                    Logger.Log("Create profile");
                    break;
                }
                case "Load": {
                    this.input.setHeaderText("Enter the profile name to load");
                    this.input.setTitle("Load profile");
                    this.input.showAndWait().ifPresent(profile -> this.profileManager.loadProfile(profile, true)
                    );
                    Logger.Log("Load profile");
                    break;
                }
                case "Add limit": {
                    this.input.setHeaderText("Enter the item name");
                    this.input.setTitle("Add limit");
                    itemName = this.input.showAndWait();
                    if (!itemName.isPresent()) {
                        return;
                    }
                    this.input.setHeaderText("Enter the item limit");
                    Optional limitStr = this.input.showAndWait();
                    if (!limitStr.isPresent()) {
                        return;
                    }
                    int limit = 0;
                    try {
                        limit = Integer.parseInt(limitStr.get().toString());
                    } catch (NumberFormatException nfe) {
                        nfe.printStackTrace();
                    }
                    this.profileManager.addLimit((String) itemName.get(), limit);
                    this.profileManager.saveLimits();
                    break;
                }
                case "Get margin": {
                    ItemNameInput nameInput = new ItemNameInput(this, 2);
                    break;
                }
                case "Check cooldown": {
                    nameInput2 = new ItemNameInput(this, 1);
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
                    this.activeItems.getItems().remove(this.activeItems.getSelectionModel().getSelectedItem());
                    this.profileManager.saveLogFile();
                    Logger.Log("Delete command");
                    break;
                }
                case "Save": {
                    this.fileManager.save( this.activeItems.getItems(),  this.logItems.getItems(), this);
                    Logger.Log("Save command");
                    break;
                }
                case "Load": {
                    this.fileManager.load( this.activeItems.getItems(),  this.logItems.getItems(), this);
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

