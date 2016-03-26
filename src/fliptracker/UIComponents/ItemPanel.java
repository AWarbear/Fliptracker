package fliptracker.UIComponents;

import fliptracker.UIComponents.Controllers.GuiController;
import fliptracker.Utils.Logger;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;

import java.text.DateFormat;
import java.text.SimpleDateFormat;

/**
 * ItemPanel that holds the item data for a flip
 */
public class ItemPanel extends GridPane {
    
    private final Label nameLabel = new Label("Null");
    private final Label buyLabel = new Label("buy");
    private final Label priceLabel = new Label("0000");
    private final Label amountLabel = new Label("0000");
    private final Label timeLabel = new Label("00:00:00 00.00");
    private final Label cooldownLabel = new Label("On Cooldown!");
    
    public final Button complete = new Button("Complete");
    
    private final DateFormat timeFormat = new SimpleDateFormat("HH:mm:ss");
    
    private final GuiController controller;

    public final Label durationLabel = new Label("0 Mins");
    
    public String itemName;
    public String type;
    
    public int price;
    public int amount;
    public int duration;
    public int timeAfter;
    
    private long date;
    
    private boolean onCooldown = true;
    
    /**
     * Create the item panel
     * @param name item name
     * @param price item price
     * @param amount amount of the item
     * @param type flip type (buy/sell)
     * @param date date
     * @param controller guiController
     */
    public ItemPanel(String name, int price, int amount, String type, long date, GuiController controller) {
        this.itemName = name;
        this.price = price;
        this.amount = amount;
        this.type = type;
        this.setMaxHeight(-1.0);
        this.date = date;
        this.controller = controller;
        this.setId("itemPanel");
        this.priceLabel.setText("Price:" + this.price);
        this.nameLabel.setText("Item: " + this.itemName);
        this.buyLabel.setText("  " + this.type);
        this.amountLabel.setText("Amount:" + this.amount);
        this.timeLabel.setText(this.timeFormat.format(this.date));
        this.cooldownLabel.setText("");
        this.complete.setOnAction(e -> {
            if (this.amount == 1) {
                controller.addLogItem(this);
                controller.activeItems.getItems().remove(this);
                controller.profileManager.saveLogFile();
            } else {
                NumberInput numberInput = new NumberInput(controller);
                numberInput.setItem(this);
            }
        }
        );
        Button edit = new Button("Edit");
        edit.setOnAction(e -> controller.editCommand(this)
        );
        this.setOnMousePressed(e -> {
            switch (e.getClickCount()) {
                case 2: {
                    controller.editCommand(this);
                    Logger.Log("Double click edit");
                }
            }
        }
        );
        edit.setMaxWidth(1.7976931348623157E308);
        GridPane.setConstraints(this.nameLabel, 0, 0);
        GridPane.setConstraints(this.buyLabel, 2, 0);
        GridPane.setConstraints(this.priceLabel, 0, 1);
        GridPane.setConstraints(this.amountLabel, 0, 2);
        GridPane.setConstraints(this.timeLabel, 1, 0);
        GridPane.setConstraints(this.durationLabel, 1, 1);
        GridPane.setConstraints(this.complete, 2, 1);
        GridPane.setConstraints(this.cooldownLabel, 1, 2);
        GridPane.setConstraints(edit, 2, 2);
        ColumnConstraints columnConstraints = new ColumnConstraints();
        columnConstraints.setFillWidth(true);
        columnConstraints.setHgrow(Priority.ALWAYS);
        this.getColumnConstraints().add(columnConstraints);
        this.getChildren().addAll(this.nameLabel, this.buyLabel, this.amountLabel, this.priceLabel, this.complete, this.timeLabel, this.cooldownLabel, edit, this.durationLabel);
    }

    /**
     * Update the UI data
     */
    void update() {
        priceLabel.setText("Price:" + price);
        nameLabel.setText("Item: " + itemName);
        buyLabel.setText("  " + type);
        amountLabel.setText("Amount:" + amount);
        timeLabel.setText(timeFormat.format(date));
        cooldownLabel.setText("");
    }

    /**
     * Set the date
     * @param date the date
     */
    public void setDate(long date) {
        this.date = date;
        timeLabel.setText(timeFormat.format(date));
    }

    /**
     * Fetch the date
     * @return the date
     */
    public long getTime() {
        return date;
    }

    /**
     * Wether the item is on cooldown
     * @return is on cooldown
     */
    public boolean isOnCooldown() {
        return onCooldown;
    }

    /**
     * Set wether this is on cooldown
     * @param cooldown onCooldown
     */
    public void setOnCooldown(boolean cooldown) {
        cooldownLabel.setText(cooldown ? "On cooldown!" : "Not on cooldown");
        onCooldown = cooldown;
    }

    /**
     * Edit the item
     * @param name new name
     * @param price new price
     * @param amount new amount
     * @param type new type
     * @param date new date
     * @param timeAfter the time thats passed since the offer completed
     */
    public void edit(String name, int price, int amount, String type, long date, int timeAfter) {
        this.itemName = name;
        this.price = price;
        this.amount = amount;
        this.type = type;
        this.timeAfter = timeAfter;
        this.date = date - (long)(timeAfter * 60 * 1000);
        this.priceLabel.setText("Price:" + this.price);
        this.nameLabel.setText("Item: " + this.itemName);
        this.buyLabel.setText("  " + this.type);
        this.amountLabel.setText("Amount:" + this.amount);
        this.timeLabel.setText(this.timeFormat.format(this.date));
        this.cooldownLabel.setText("");
        this.controller.updateProfits();
    }

    /**
     * Setlog, make this itemPanel into a log itemPanel, apply the cooldown etc
     */
    public void setLog() {
        durationLabel.setText(getDuration() + " mins");
        complete.setText("Remove");
        complete.setOnAction(e -> {
            controller.logItems.getItems().remove(this);
            controller.profileManager.saveLogFile();
        }
        );
        if (type.equals("Buy"))
            cooldownLabel.setText("On cooldown!");
        controller.updateProfits();
    }

    /**
     * Fetch the type of this flip,
     * @return flipType buy/sell
     */
    public String getType() {
        return type;
    }

    /**
     * Get the duration of this flip
     * @return time after starting this offer
     */
    public int getDuration() {
        return duration;
    }
}

