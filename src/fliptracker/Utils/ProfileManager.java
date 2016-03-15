/*
 * Decompiled with CFR 0_102.
 * 
 * Could not load the following classes:
 *  javafx.collections.ObservableList
 *  javafx.scene.control.Alert
 *  javafx.scene.control.Alert$AlertType
 *  javafx.scene.control.ListView
 *  javafx.scene.control.TextArea
 *  javafx.scene.control.TextField
 *  javafx.scene.layout.AnchorPane
 */
package fliptracker.Utils;

import fliptracker.Audio.SoundEffect;
import fliptracker.UIComponents.Controllers.GuiController;
import fliptracker.UIComponents.ItemPanel;
import javafx.scene.control.Alert;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class ProfileManager {
    public final GuiController controller;
    public String profileName = "Default";
    private File logFile = null;
    public HashMap<String, Integer> limitMap = new HashMap<>();
    public final ArrayList<String> cssUrls = new ArrayList<>();
    public boolean useItemsWithLimits = false;
    public boolean useSearch = true;
    public boolean useSound = true;
    public boolean useRuleTimer = true;
    public File marginFile;
    public File ruleSound = null;
    public int currentTheme;
    public int ruleTime = 25;
    public String recent;
    public String websites = "http://rsaboveexpectations.com/forum/page/8/m/32172226/viewthread/22296783-flipping-thread::http://services.runescape.com/m=forum/forums.ws?17,18::http://www.google.com";
    private final ArrayList<String> saveLines = new ArrayList<>();
    private final Alert alert;
    private final String cacheDir = System.getProperty("user.home") + File.separator + "Fliptracker_cache" + File.separator;
    private String profilePath = this.cacheDir + "Default" + File.separator;
    private File profileFile = new File(this.profilePath);
    private File cacheFile = new File(this.cacheDir);

    public ProfileManager(GuiController controller) {
        this.cssUrls.add(this.getClass().getResource("/fliptracker/res/style.css").toExternalForm());
        this.cssUrls.add(this.getClass().getResource("/fliptracker/res/caspian.css").toExternalForm());
        this.alert = new Alert(Alert.AlertType.ERROR);
        this.controller = controller;
        this.profilePath = this.cacheDir + "Default" + File.separator;
        this.getSettingsFile();
        this.getProfileSettingsFile();
        this.getLimitsFile();
        this.getMarginsFile();
        this.loadSettings();
    }

    private void setDefaults() {
        this.logFile = null;
        this.limitMap = new HashMap<>();
        this.useItemsWithLimits = false;
        this.useSearch = true;
        this.useSound = true;
        this.useRuleTimer = true;
        this.marginFile = null;
        this.ruleSound = null;
        this.currentTheme = 0;
        this.ruleTime = 25;
        String recent = "";
        this.controller.activeItems.getItems().clear();
        this.controller.logItems.getItems().clear();
        this.controller.noteArea.setText("");
        this.profilePath = this.cacheDir + "Default" + File.separator;
        this.profileFile = new File(this.profilePath);
        this.cacheFile = new File(this.cacheDir);
    }

    public HashMap<String, Integer> getLimits() {
        return this.limitMap;
    }

    public void addRecent(String item) {
        if (this.recent == null) {
            Logger.Log("First recent item: " + item);
            this.recent = item;
            return;
        }
        if (this.recent.contains(item)) {
            return;
        }
        this.recent = this.recent.split(":").length >= 5 ? item + ":" + this.recent.substring(0, this.recent.lastIndexOf(":")) : item + ":" + this.recent;
        System.out.println("Recent " + this.recent);
    }

    public void save() {
        this.saveMargins();
        this.saveProfileSettings();
        this.saveSettings();
    }

    public void saveAll() {
        this.saveMargins();
        this.saveProfileSettings();
        this.saveSettings();
        this.saveLogFile();
        this.saveLimits();
    }

    public void getTheme() {
        this.controller.rootPane.getStylesheets().clear();
        this.controller.rootPane.getStylesheets().add(this.cssUrls.get(this.currentTheme));
    }

    public void saveLogFile() {
        String line;
        int i;
        ItemPanel item;
        this.saveLines.clear();
        this.saveLines.add("state,itemName,price,amount,type,date,duration");
        for (i = 0; i < this.controller.activeItems.getItems().size(); ++i) {
            item = this.controller.activeItems.getItems().get(i);
            line = "Active," + item.itemName + "," + item.price + "," + item.amount + "," + item.type + "," + item.getTime();
            this.saveLines.add(line);
        }
        for (i = 0; i < this.controller.logItems.getItems().size(); ++i) {
            item = this.controller.logItems.getItems().get(i);
            line = "Log," + item.itemName + "," + item.price + "," + item.amount + "," + item.type + "," + item.getTime() + "," + item.getDuration();
            this.saveLines.add(line);
        }
        this.controller.fileManager.save(this.saveLines, this.logFile);
        this.saveLines.clear();
    }

    public void saveLimits() {
        this.saveLines.clear();
        for (Map.Entry<String, Integer> entry : this.limitMap.entrySet()) {
            String line = entry.getKey() + "," + entry.getValue();
            this.saveLines.add(line);
        }
        this.controller.fileManager.save(this.saveLines, this.getLimitsFile());
        this.saveLines.clear();
    }

    private void saveProfileSettings() {
        this.saveLines.clear();
        this.saveLines.add("Current log," + this.getLogFile());
        this.saveLines.add("useItemsWithLimits," + this.useItemsWithLimits);
        this.saveLines.add("Current margins," + this.getMarginsFile());
        this.saveLines.add("Recent items," + this.recent);
        this.saveLines.add("Sound," + this.useSound);
        this.saveLines.add("Search," + this.useSearch);
        this.saveLines.add("RuleTimer," + this.useRuleTimer);
        this.saveLines.add("RuleTime," + this.ruleTime);
        this.saveLines.add("RuleSound," + this.ruleSound);
        this.controller.fileManager.save(this.saveLines, this.getProfileSettingsFile());
        this.saveLines.clear();
    }

    private void saveSettings() {
        this.saveLines.clear();
        this.saveLines.add("Default Profile," + this.controller.profileManager.profileName);
        this.saveLines.add("Theme," + this.currentTheme);
        this.saveLines.add("Sites," + this.websites);
        this.controller.fileManager.save(this.saveLines, this.getSettingsFile());
        this.saveLines.clear();
    }

    private void loadSettings() {
        ArrayList<String> settings = this.controller.fileManager.getFileLines(this.getSettingsFile());
        if (settings == null) {
            this.loadProfile("Default", false);
            this.currentTheme = 0;
            Logger.Log("No settings do default");
            return;
        }
        block10 : for (String line : settings) {
            String[] curLine = line.split(",");
            switch (curLine[0]) {
                case "Default Profile": {
                    if (curLine.length < 2) {
                        Logger.Log("No profile set, setting default");
                        this.loadProfile("Default", false);
                        continue block10;
                    }
                    this.loadProfile(curLine[1], false);
                    Logger.Log("Load profile");
                    continue block10;
                }
                case "Theme": {
                    if (curLine.length < 2) {
                        Logger.Log("No theme set, setting default");
                        this.currentTheme = 0;
                        continue block10;
                    }
                    this.currentTheme = Integer.parseInt(curLine[1]);
                    Logger.Log("Set theme");
                    continue block10;
                }
                case "Sites": {
                    if (curLine.length != 2) {
                        this.websites = "http://rsaboveexpectations.com/forum/page/8/m/32172226/viewthread/22296783-flipping-thread::http://services.runescape.com/m=forum/forums.ws?17,18::http://www.google.com";
                        return;
                    }
                    if (curLine[1].equals("null")) {
                        this.websites = "http://rsaboveexpectations.com/forum/page/8/m/32172226/viewthread/22296783-flipping-thread::http://services.runescape.com/m=forum/forums.ws?17,18::http://www.google.com";
                        return;
                    }
                    this.websites = curLine[1];
                    continue block10;
                }
            }
            Logger.Log("Invalid line");
        }
    }

    private void loadProfileSettings() {
        ArrayList<String> settings = this.controller.fileManager.getFileLines(this.getProfileSettingsFile());
        if (settings == null) {
            this.useItemsWithLimits = false;
            Logger.Log("No profile settings, do default");
            return;
        }
        block28 : for (String line : settings) {
            String[] curLine = line.split(",");
            switch (curLine[0]) {
                case "useItemsWithLimits": {
                    if (curLine.length < 2) {
                        Logger.Log("Not set, assume no");
                        this.useItemsWithLimits = false;
                        return;
                    }
                    this.useItemsWithLimits = Boolean.parseBoolean(curLine[1]);
                    continue block28;
                }
                case "Current log": {
                    if (curLine.length < 2) {
                        Logger.Log("no file");
                        this.logFile = new File(this.profileFile, "log.csv");
                        try {
                            this.logFile.createNewFile();
                        }
                        catch (IOException ioe) {
                            ioe.printStackTrace();
                        }
                        return;
                    }
                    if (curLine[1].equals("null")) {
                        Logger.Log("no log file set lets go default");
                        this.logFile = new File(this.profileFile, "log.csv");
                        try {
                            this.logFile.createNewFile();
                        }
                        catch (IOException ioe) {
                            ioe.printStackTrace();
                        }
                        return;
                    }
                    this.logFile = new File(curLine[1]);
                    continue block28;
                }
                case "Current margins": {
                    if (curLine.length < 2 || curLine[1].equals("null")) {
                        Logger.Log("no file");
                        this.marginFile = new File(this.profileFile, "margins.txt");
                        if (!this.marginFile.exists()) {
                            try {
                                this.marginFile.createNewFile();
                            }
                            catch (IOException ioe) {
                                ioe.printStackTrace();
                            }
                        }
                        return;
                    }
                    this.marginFile = new File(curLine[1]);
                    this.getMarginsFile();
                    continue block28;
                }
                case "Recent items": {
                    if (curLine.length != 2) {
                        this.recent = null;
                        return;
                    }
                    if (curLine[1].equals("null")) {
                        this.recent = null;
                        return;
                    }
                    this.recent = curLine[1];
                    continue block28;
                }
                case "Sound": {
                    if (curLine.length != 2 || curLine[1].equals("null")) {
                        this.useSound = true;
                        return;
                    }
                    this.useSound = Boolean.parseBoolean(curLine[1]);
                    continue block28;
                }
                case "Search": {
                    if (curLine.length != 2 || curLine[1].equals("null")) {
                        this.useSearch = true;
                        return;
                    }
                    this.useSearch = Boolean.parseBoolean(curLine[1]);
                    continue block28;
                }
                case "RuleTimer": {
                    if (curLine.length != 2 || curLine[1].equals("null")) {
                        this.useRuleTimer = true;
                        return;
                    }
                    this.useRuleTimer = Boolean.parseBoolean(curLine[1]);
                    continue block28;
                }
                case "RuleTime": {
                    if (curLine.length != 2 || curLine[1].equals("null")) {
                        this.ruleTime = 25;
                        return;
                    }
                    this.ruleTime = Integer.parseInt(curLine[1]);
                    continue block28;
                }
                case "RuleSound": {
                    if (curLine.length < 2 || curLine[1].equals("null")) {
                        this.ruleSound = null;
                        return;
                    }
                    this.ruleSound = new File(curLine[1]);
                    continue block28;
                }
            }
            Logger.Log("Unhandled line " + Arrays.toString(curLine));
        }
    }

    public void dumpWikiLimits() {
        this.controller.fileManager.dumpPrices(this.getLimits());
        this.saveLimits();
    }

    public void saveMargins() {
        if (this.controller.noteArea.getText() == null) {
            Logger.Log("Note area hasn't initiated so let's ignore");
            return;
        }
        this.saveLines.clear();
        Collections.addAll(this.saveLines, this.controller.noteArea.getText().split(System.getProperty("line.separator")));
        this.controller.fileManager.save(this.saveLines, this.getMarginsFile());
        this.saveLines.clear();
    }

    public void updateMargins() {
        if (this.controller.notes == null) {
            return;
        }
        this.controller.noteArea.setText(this.controller.notes);
    }

    private void loadMargins() {
        ArrayList<String> limits = this.controller.fileManager.getFileLines(this.getMarginsFile());
        if (limits == null) {
            this.controller.notes = "";
            return;
        }
        String text = "";
        for (String limit : limits) {
            text = text + limit + System.getProperty("line.separator");
        }
        this.controller.notes = text;
    }

    public void createProfile(String profileName) {
        if (profileName.isEmpty() || profileName.contains("/")) {
            this.alert.setAlertType(Alert.AlertType.ERROR);
            this.alert.setTitle("Error!");
            this.alert.setHeaderText("Error creating profile");
            this.alert.setContentText("Profile name can't be empty, and can't contain /");
            this.alert.showAndWait();
            return;
        }
        File file = new File(this.cacheDir + profileName + File.separator);
        file.mkdirs();
        file = new File(file.getAbsolutePath() + File.separator + "limits.txt");
        try {
            file.createNewFile();
        }
        catch (IOException ioe) {
            ioe.printStackTrace();
        }
        this.alert.setAlertType(Alert.AlertType.INFORMATION);
        this.alert.setTitle("Profile created");
        this.alert.setHeaderText("Profile with the name " + profileName + " has been created!");
        this.alert.showAndWait();
        this.setDefaults();
        this.loadProfile(profileName, true);
    }

    public void loadProfile(String profileName, boolean needsUpdate) {
        if (needsUpdate) {
            this.save();
        }
        this.profilePath = this.cacheDir + profileName + File.separator;
        File file = new File(this.profilePath);
        if (!file.exists()) {
            this.alert.setAlertType(Alert.AlertType.ERROR);
            this.alert.setTitle("Error!");
            this.alert.setHeaderText("Error loading profile");
            this.alert.setContentText("No profile file was found for given profile name");
            this.alert.showAndWait();
            return;
        }
        this.profileName = profileName;
        this.profileFile = new File(this.profilePath);
        this.logFile = new File(this.profileFile, "log.csv");
        this.marginFile = new File(this.profileFile, "margins.txt");
        this.loadProfileSettings();
        this.loadLimits();
        this.loadMargins();
        if (needsUpdate) {
            this.controller.loadUp();
        }
    }

    public void chooseNotesFile() {
        File file = this.controller.fileManager.getChosenFile();
        if (file == null) {
            return;
        }
        try {
            if (!file.exists()) {
                file.getParentFile().mkdirs();
                file.createNewFile();
            }
            this.marginFile = file;
            this.controller.notesField.setText("" + file);
            this.getMarginsFile();
            this.loadMargins();
            this.updateMargins();
        }
        catch (IOException ioe) {
            ioe.printStackTrace();
        }
    }

    public void chooseRuleFile() {
        File file = this.controller.fileManager.getWavFile();
        if (file == null) {
            return;
        }
        if (!file.exists()) {
            this.ruleSound = null;
            this.controller.ruleField.setText("Default");
            return;
        }
        this.ruleSound = file;
        this.controller.ruleField.setText("" + file);
        this.controller.audioHandler.soundEffect.add(2, new SoundEffect(this.ruleSound));
    }

    private void loadLimits() {
        ArrayList<String> limits = this.controller.fileManager.getFileLines(this.getLimitsFile());
        if (limits == null) {
            return;
        }
        for (String line : limits) {
            String[] currentLimit = line.split(",");
            try {
                if (currentLimit.length == 1) {
                    this.limitMap.put(currentLimit[0], 0);
                    continue;
                }
                this.limitMap.put(currentLimit[0], Integer.parseInt(currentLimit[1]));
            }
            catch (NumberFormatException nfe) {
                nfe.printStackTrace();
                Logger.Log("Error parsing limit integer");
            }
        }
    }

    public void addLimit(String name, int limit) {
        if (limit == 0) {
            this.alert.setAlertType(Alert.AlertType.ERROR);
            this.alert.setTitle("Error!");
            this.alert.setHeaderText("Error adding limit");
            this.alert.setContentText("Please enter a valid limit");
            this.alert.showAndWait();
            return;
        }
        this.limitMap.put(name, limit);
        this.saveLimits();
        this.alert.setAlertType(Alert.AlertType.INFORMATION);
        this.alert.setTitle("Limit added");
        this.alert.setHeaderText("Buy limit of " + limit + " has been added to " + name);
        this.alert.showAndWait();
    }

    private File getLimitsFile() {
        File file = new File(this.profileFile, "limits.txt");
        if (!file.exists()) {
            try {
                this.profileFile.mkdir();
                file = new File(this.profileFile, "limits.txt");
                file.createNewFile();
            }
            catch (IOException ioe) {
                ioe.printStackTrace();
            }
        }
        return file;
    }

    private File getMarginsFile() {
        try {
            if (this.marginFile == null) {
                this.marginFile = new File(this.profileFile, "margins.txt");
            }
            if (!this.marginFile.exists()) {
                this.marginFile.createNewFile();
            }
        }
        catch (IOException ioe) {
            ioe.printStackTrace();
        }
        return this.marginFile;
    }

    private File getProfileSettingsFile() {
        this.profileFile = new File(this.cacheFile + File.separator + this.profileName);
        File file = new File(this.profileFile, "settings.txt");
        if (!file.exists()) {
            try {
                this.profileFile.mkdir();
                file.createNewFile();
            }
            catch (IOException ioe) {
                ioe.printStackTrace();
            }
        }
        return file;
    }

    private File getSettingsFile() {
        File file = new File(this.cacheFile, "settings.txt");
        if (!file.exists()) {
            try {
                this.cacheFile.mkdir();
                file.createNewFile();
            }
            catch (IOException ioe) {
                ioe.printStackTrace();
            }
        }
        return file;
    }

    public File getLogFile() {
        if (this.logFile == null) {
            this.logFile = new File(this.profileFile, "log.csv");
        }
        return this.logFile;
    }

    public void setLogFile(File file) {
        this.logFile = file;
    }
}

