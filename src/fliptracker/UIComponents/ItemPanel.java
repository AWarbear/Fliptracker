/*
 * Decompiled with CFR 0_102.
 * 
 * Could not load the following classes:
 *  javafx.collections.ObservableList
 *  javafx.event.ActionEvent
 *  javafx.event.Event
 *  javafx.event.EventHandler
 *  javafx.scene.Node
 *  javafx.scene.control.Button
 *  javafx.scene.control.Label
 *  javafx.scene.control.ListView
 *  javafx.scene.input.MouseEvent
 *  javafx.scene.layout.ColumnConstraints
 *  javafx.scene.layout.GridPane
 *  javafx.scene.layout.Priority
 */
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

public class ItemPanel
extends GridPane {
    public String itemName;
    public String type;
    public int price;
    public int amount;
    public int duration;
    public int timeAfter;
    private final Label nameLabel = new Label("Null");
    private final Label buyLabel = new Label("buy");
    private final Label priceLabel = new Label("0000");
    private final Label amountLabel = new Label("0000");
    private final Label timeLabel = new Label("00:00:00 00.00");
    private final Label cooldownLabel = new Label("On Cooldown!");
    public final Label durationLabel = new Label("0 Mins");
    public final Button complete = new Button("Complete");
    private final DateFormat timeFormat = new SimpleDateFormat("HH:mm:ss");
    private long date;
    private boolean onCooldown = true;
    private final GuiController controller;

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

    public void update() {
        this.priceLabel.setText("Price:" + this.price);
        this.nameLabel.setText("Item: " + this.itemName);
        this.buyLabel.setText("  " + this.type);
        this.amountLabel.setText("Amount:" + this.amount);
        this.timeLabel.setText(this.timeFormat.format(this.date));
        this.cooldownLabel.setText("");
    }

    public void setDate(long date) {
        this.date = date;
        this.timeLabel.setText(this.timeFormat.format(date));
    }

    public long getTime() {
        return this.date;
    }

    public boolean isOnCooldown() {
        return this.onCooldown;
    }

    public void setOnCooldown(boolean cooldown) {
        this.cooldownLabel.setText(cooldown ? "On cooldown!" : "Not on cooldown");
        this.onCooldown = cooldown;
    }

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

    public void setLog() {
        this.durationLabel.setText("" + this.getDuration() + " mins");
        this.complete.setText("Remove");
        this.complete.setOnAction(e -> {
            this.controller.logItems.getItems().remove(this);
            this.controller.profileManager.saveLogFile();
        }
        );
        if (this.type.equals("Buy")) {
            this.cooldownLabel.setText("On cooldown!");
        }
        this.controller.updateProfits();
    }

    public String getType() {
        return this.type;
    }

    public int getDuration() {
        return this.duration;
    }
}

