package fliptracker.Utils;

import fliptracker.Audio.SoundEffect;
import fliptracker.UIComponents.Controllers.GuiController;
import fliptracker.UIComponents.ItemPanel;
import javafx.scene.control.Alert;

import java.io.File;
import java.io.IOException;
import java.util.*;

/**
 * Handles the users profile
 */
public class ProfileManager {

    public final GuiController controller;

    private final Alert alert;

    public final ArrayList<String> cssUrls = new ArrayList<>();
    private final ArrayList<String> saveLines = new ArrayList<>();

    private final String cacheDir = System.getProperty("user.home") + File.separator + "Fliptracker_cache" + File.separator;

    public HashMap<String, Integer> limitMap = new HashMap<>();

    public String profileName = "Default";
    public String recent;
    public String websites = "http://rsaboveexpectations.com/forum/page/8/m/32172226/viewthread/22296783-flipping-thread::http://services.runescape.com/m=forum/forums.ws?17,18::http://www.google.com";
    private String profilePath = cacheDir + "Default" + File.separator;

    private File logFile = null;
    public File marginFile;
    public File ruleSound = null;
    private File profileFile = new File(profilePath);
    private File cacheFile = new File(cacheDir);

    public boolean useItemsWithLimits = false;
    public boolean useSearch = true;
    public boolean useSound = true;
    public boolean useRuleTimer = true;

    public int currentTheme;
    public int ruleTime = 25;

    /**
     * Construct the profile manager and initiate all values
     *
     * @param controller the ui controller
     */
    public ProfileManager(GuiController controller) {
        cssUrls.add(this.getClass().getResource("/fliptracker/res/style.css").toExternalForm());
        cssUrls.add(this.getClass().getResource("/fliptracker/res/caspian.css").toExternalForm());
        alert = new Alert(Alert.AlertType.ERROR);
        this.controller = controller;
        profilePath = this.cacheDir + "Default" + File.separator;
        getSettingsFile();
        getProfileSettingsFile();
        getLimitsFile();
        getMarginsFile();
        loadSettings();
    }

    /**
     * Initiate/Reset all values
     */
    private void setDefaults() {
        logFile = null;
        limitMap = new HashMap<>();
        useItemsWithLimits = false;
        useSearch = true;
        useSound = true;
        useRuleTimer = true;
        marginFile = null;
        ruleSound = null;
        currentTheme = 0;
        ruleTime = 25;
        recent = "";
        controller.activeItems.getItems().clear();
        controller.logItems.getItems().clear();
        controller.noteArea.setText("");
        profilePath = this.cacheDir + "Default" + File.separator;
        profileFile = new File(profilePath);
        cacheFile = new File(cacheDir);
    }

    /**
     * Add an item to recently used items
     *
     * @param item the item to add
     */
    public void addRecent(String item) {
        if (recent == null) {
            Logger.Log("First recent item: " + item);
            recent = item;
            return;
        }
        if (recent.contains(item))
            return;
        recent = recent.split(":").length >= 5 ? item + ":" + recent.substring(0, recent.lastIndexOf(":")) : item + ":" + recent;
        System.out.println("Recent " + recent);
    }

    /**
     * Save profile
     */
    public void save() {
        saveMargins();
        saveProfileSettings();
        saveSettings();
    }

    /**
     * Save profile and margins
     */
    public void saveAll() {
        saveMargins();
        saveProfileSettings();
        saveSettings();
        saveLogFile();
        saveLimits();
    }

    /**
     * Refresh the UI theme
     */
    public void getTheme() {
        controller.rootPane.getStylesheets().clear();
        try {
            controller.rootPane.getStylesheets().add(cssUrls.get(currentTheme));
        }catch(IndexOutOfBoundsException iobe){
            //No theme in list, assume default (light theme)
        }
    }

    /**
     * Get the css url of the theme or null if default
     * @return
     */
    public String getThemeCss() {
        switch(currentTheme){
            case 2:
                return null;
            default:
                return cssUrls.get(currentTheme);
        }
    }

    /**
     * Saves the margins file
     */
    public void saveLogFile() {
        String line;
        ItemPanel item;
        saveLines.clear();
        saveLines.add("state,itemName,price,amount,type,date,duration");
        for (int i = 0; i < controller.activeItems.getItems().size(); ++i) {
            item = controller.activeItems.getItems().get(i);
            line = "Active," + item.itemName + "," + item.price + "," + item.amount + "," + item.type + "," + item.getTime();
            saveLines.add(line);
        }
        for (int i = 0; i < controller.logItems.getItems().size(); ++i) {
            item = controller.logItems.getItems().get(i);
            line = "Log," + item.itemName + "," + item.price + "," + item.amount + "," + item.type + "," + item.getTime() + "," + item.getDuration();
            saveLines.add(line);
        }
        controller.fileManager.save(saveLines, logFile);
        saveLines.clear();
    }

    /**
     * Save the buy limits file
     */
    public void saveLimits() {
        saveLines.clear();
        for (Map.Entry<String, Integer> entry : limitMap.entrySet()) {
            String line = entry.getKey() + "," + entry.getValue();
            saveLines.add(line);
        }
        controller.fileManager.save(saveLines, getLimitsFile());
        saveLines.clear();
    }

    /**
     * Save the profile settings
     */
    private void saveProfileSettings() {
        saveLines.clear();
        saveLines.add("Current log," + getLogFile());
        saveLines.add("useItemsWithLimits," + useItemsWithLimits);
        saveLines.add("Current margins," + getMarginsFile());
        saveLines.add("Recent items," + recent);
        saveLines.add("Sound," + useSound);
        saveLines.add("Search," + useSearch);
        saveLines.add("RuleTimer," + useRuleTimer);
        saveLines.add("RuleTime," + ruleTime);
        saveLines.add("RuleSound," + ruleSound);
        controller.fileManager.save(saveLines, getProfileSettingsFile());
        saveLines.clear();
    }

    /**
     * Save program settings
     */
    private void saveSettings() {
        saveLines.clear();
        saveLines.add("Default Profile," + controller.profileManager.profileName);
        saveLines.add("Theme," + currentTheme);
        saveLines.add("Sites," + websites);
        controller.fileManager.save(saveLines, getSettingsFile());
        saveLines.clear();
    }

    /**
     * Load program settings
     */
    private void loadSettings() {
        ArrayList<String> settings = controller.fileManager.getFileLines(getSettingsFile());
        if (settings == null) {
            loadProfile("Default", false);
            currentTheme = 0;
            Logger.Log("No settings do default");
            return;
        }
        for (String line : settings) {
            String[] curLine = line.split(",");
            switch (curLine[0]) {
                case "Default Profile": {
                    if (curLine.length < 2) {
                        Logger.Log("No profile set, setting default");
                        loadProfile("Default", false);
                        continue;
                    }
                    loadProfile(curLine[1], false);
                    Logger.Log("Load profile");
                    continue;
                }
                case "Theme": {
                    if (curLine.length < 2) {
                        Logger.Log("No theme set, setting default");
                        currentTheme = 0;
                        continue;
                    }
                    currentTheme = Integer.parseInt(curLine[1]);
                    Logger.Log("Set theme");
                    continue;
                }
                case "Sites": {
                    if (curLine.length != 2) {
                        websites = "http://rsaboveexpectations.com/forum/page/8/m/32172226/viewthread/22296783-flipping-thread::http://services.runescape.com/m=forum/forums.ws?17,18::http://www.google.com";
                        return;
                    }
                    if (curLine[1].equals("null")) {
                        websites = "http://rsaboveexpectations.com/forum/page/8/m/32172226/viewthread/22296783-flipping-thread::http://services.runescape.com/m=forum/forums.ws?17,18::http://www.google.com";
                        return;
                    }
                    websites = curLine[1];
                    continue;
                }
            }
            Logger.Log("Invalid line");
        }
    }

    /**
     * Load the profile settings
     */
    private void loadProfileSettings() {
        ArrayList<String> settings = controller.fileManager.getFileLines(getProfileSettingsFile());
        if (settings == null) {
            useItemsWithLimits = false;
            Logger.Log("No profile settings, do default");
            return;
        }
        for (String line : settings) {
            String[] curLine = line.split(",");
            switch (curLine[0]) {
                case "useItemsWithLimits": {
                    if (curLine.length < 2) {
                        Logger.Log("Not set, assume no");
                        useItemsWithLimits = false;
                        return;
                    }
                    useItemsWithLimits = Boolean.parseBoolean(curLine[1]);
                    continue;
                }
                case "Current log": {
                    if (curLine.length < 2) {
                        Logger.Log("no file");
                        logFile = new File(profileFile, "log.csv");
                        try {
                            logFile.createNewFile();
                        } catch (IOException ioe) {
                            ioe.printStackTrace();
                        }
                        return;
                    }
                    if (curLine[1].equals("null")) {
                        Logger.Log("no log file set lets go default");
                        logFile = new File(profileFile, "log.csv");
                        try {
                            logFile.createNewFile();
                        } catch (IOException ioe) {
                            ioe.printStackTrace();
                        }
                        return;
                    }
                    logFile = new File(curLine[1]);
                    continue;
                }
                case "Current margins": {
                    if (curLine.length < 2 || curLine[1].equals("null")) {
                        Logger.Log("no file");
                        marginFile = new File(profileFile, "margins.txt");
                        if (!marginFile.exists()) {
                            try {
                                marginFile.createNewFile();
                            } catch (IOException ioe) {
                                ioe.printStackTrace();
                            }
                        }
                        return;
                    }
                    marginFile = new File(curLine[1]);
                    getMarginsFile();
                    continue;
                }
                case "Recent items": {
                    if (curLine.length != 2) {
                        recent = null;
                        return;
                    }
                    if (curLine[1].equals("null")) {
                        recent = null;
                        return;
                    }
                    recent = curLine[1];
                    continue;
                }
                case "Sound": {
                    if (curLine.length != 2 || curLine[1].equals("null")) {
                        useSound = true;
                        return;
                    }
                    useSound = Boolean.parseBoolean(curLine[1]);
                    continue;
                }
                case "Search": {
                    if (curLine.length != 2 || curLine[1].equals("null")) {
                        useSearch = true;
                        return;
                    }
                    useSearch = Boolean.parseBoolean(curLine[1]);
                    continue;
                }
                case "RuleTimer": {
                    if (curLine.length != 2 || curLine[1].equals("null")) {
                        useRuleTimer = true;
                        return;
                    }
                    useRuleTimer = Boolean.parseBoolean(curLine[1]);
                    continue;
                }
                case "RuleTime": {
                    if (curLine.length != 2 || curLine[1].equals("null")) {
                        ruleTime = 25;
                        return;
                    }
                    ruleTime = Integer.parseInt(curLine[1]);
                    continue;
                }
                case "RuleSound": {
                    if (curLine.length < 2 || curLine[1].equals("null")) {
                        ruleSound = null;
                        return;
                    }
                    ruleSound = new File(curLine[1]);
                    continue;
                }
            }
            Logger.Log("Unhandled line " + Arrays.toString(curLine));
        }
    }

    /**
     * Fetch item buy limits from runewiki
     */
    public void dumpWikiLimits() {
        controller.fileManager.dumpPrices(getLimits());
        saveLimits();
    }

    /**
     * Save the margins file (the text field in margins tab)
     */
    public void saveMargins() {
        if (controller.noteArea.getText() == null) {
            Logger.Log("Note area hasn't initiated so let's ignore");
            return;
        }
        saveLines.clear();
        Collections.addAll(saveLines, controller.noteArea.getText().split(System.getProperty("line.separator")));
        controller.fileManager.save(saveLines, getMarginsFile());
        saveLines.clear();
    }

    /**
     * Update the margins area
     */
    public void updateMargins() {
        if (controller.getNotes() == null) {
            return;
        }
        controller.noteArea.setText(controller.getNotes());
    }

    /**
     * Load the margins file
     */
    private void loadMargins() {
        ArrayList<String> limits = controller.fileManager.getFileLines(getMarginsFile());
        if (limits == null) {
            controller.setNotes("");
            return;
        }
        String text = "";
        for (String limit : limits) {
            text = text + limit + System.getProperty("line.separator");
        }
        controller.setNotes(text);
    }

    /**
     * Create a new profile
     *
     * @param profileName the name of the new profile
     */
    public void createProfile(String profileName) {
        if (profileName.isEmpty() || profileName.contains("/")) {
            alert.setAlertType(Alert.AlertType.ERROR);
            alert.setTitle("Error!");
            alert.setHeaderText("Error creating profile");
            alert.setContentText("Profile name can't be empty, and can't contain /");
            alert.showAndWait();
            return;
        }
        File file = new File(cacheDir + profileName + File.separator);
        file.mkdirs();
        file = new File(file.getAbsolutePath() + File.separator + "limits.txt");
        try {
            file.createNewFile();
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
        alert.setAlertType(Alert.AlertType.INFORMATION);
        alert.setTitle("Profile created");
        alert.setHeaderText("Profile with the name " + profileName + " has been created!");
        alert.showAndWait();
        setDefaults();
        loadProfile(profileName, true);
    }

    /**
     * Load a profile file
     *
     * @param profileName name of the profile
     * @param needsUpdate if we should update the gui, eg if this is happening after startup(from settings).
     */
    public void loadProfile(String profileName, boolean needsUpdate) {
        if (needsUpdate) {
            save();
        }
        profilePath = cacheDir + profileName + File.separator;
        File file = new File(profilePath);
        if (!file.exists()) {
            alert.setAlertType(Alert.AlertType.ERROR);
            alert.setTitle("Error!");
            alert.setHeaderText("Error loading profile");
            alert.setContentText("No profile file was found for given profile name");
            alert.showAndWait();
            return;
        }
        this.profileName = profileName;
        profileFile = new File(profilePath);
        logFile = new File(profileFile, "log.csv");
        marginFile = new File(profileFile, "margins.txt");
        loadProfileSettings();
        loadLimits();
        loadMargins();
        if (needsUpdate) {
            controller.loadUp();
        }
    }

    /**
     * Ask the user for the path of the notes file (via FileChooser)
     */
    public void chooseNotesFile() {
        File file = controller.fileManager.getChosenFile();
        if (file == null)
            return;
        try {
            if (!file.exists()) {
                file.getParentFile().mkdirs();
                file.createNewFile();
            }
            marginFile = file;
            controller.notesField.setText("" + file);
            getMarginsFile();
            loadMargins();
            updateMargins();
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
    }

    /**
     * Ask the user for the path of the rule alert sound file
     */
    public void chooseRuleFile() {
        File file = controller.fileManager.getWavFile();
        if (file == null) {
            return;
        }
        if (!file.exists()) {
            ruleSound = null;
            controller.ruleField.setText("Default");
            return;
        }
        ruleSound = file;
        controller.ruleField.setText("" + file);
        controller.audioHandler.soundEffect.add(2, new SoundEffect(ruleSound));
    }

    /**
     * Load the item buy limits from cache folder (Settings folder)
     */
    private void loadLimits() {
        ArrayList<String> limits = controller.fileManager.getFileLines(getLimitsFile());
        if (limits == null)
            return;
        for (String line : limits) {
            String[] currentLimit = line.split(",");
            try {
                if (currentLimit.length == 1) {
                    limitMap.put(currentLimit[0], 0);
                    continue;
                }
                limitMap.put(currentLimit[0], Integer.parseInt(currentLimit[1]));
            } catch (NumberFormatException nfe) {
                nfe.printStackTrace();
                Logger.Log("Error parsing limit integer");
            }
        }
    }

    /**
     * Add a new limit to the limit map
     *
     * @param name  item name
     * @param limit the buy limit
     */
    public void addLimit(String name, int limit) {
        if (limit == 0) {
            alert.setAlertType(Alert.AlertType.ERROR);
            alert.setTitle("Error!");
            alert.setHeaderText("Error adding limit");
            alert.setContentText("Please enter a valid limit");
            alert.showAndWait();
            return;
        }
        limitMap.put(name, limit);
        saveLimits();
        alert.setAlertType(Alert.AlertType.INFORMATION);
        alert.setTitle("Limit added");
        alert.setHeaderText("Buy limit of " + limit + " has been added to " + name);
        alert.showAndWait();
    }

    /**
     * Get the limits map
     *
     * @return returns the hashmap containing the item buy limits
     */
    public HashMap<String, Integer> getLimits() {
        return limitMap;
    }

    /**
     * Get the profiles limits file
     *
     * @return the limits file
     */
    private File getLimitsFile() {
        File file = new File(profileFile, "limits.txt");
        if (!file.exists()) {
            try {
                profileFile.mkdir();
                file = new File(profileFile, "limits.txt");
                file.createNewFile();
            } catch (IOException ioe) {
                ioe.printStackTrace();
            }
        }
        return file;
    }

    /**
     * Get the marins/notes file
     *
     * @return the margins file, by default margins.txt in profile folder or specified by user
     */
    private File getMarginsFile() {
        try {
            if (marginFile == null)
                marginFile = new File(profileFile, "margins.txt");
            if (!marginFile.exists())
                marginFile.createNewFile();
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
        return marginFile;
    }

    /**
     * Get the settings file of the profile
     *
     * @return the settings file of this profile
     */
    private File getProfileSettingsFile() {
        profileFile = new File(cacheFile + File.separator + profileName);
        File file = new File(profileFile, "settings.txt");
        if (!file.exists()) {
            try {
                profileFile.mkdir();
                file.createNewFile();
            } catch (IOException ioe) {
                ioe.printStackTrace();
            }
        }
        return file;
    }

    /**
     * Get the settings file for the program
     *
     * @return the settings file of the program
     */
    private File getSettingsFile() {
        File file = new File(cacheFile, "settings.txt");
        if (!file.exists()) {
            try {
                cacheFile.mkdir();
                file.createNewFile();
            } catch (IOException ioe) {
                ioe.printStackTrace();
            }
        }
        return file;
    }

    /**
     * Get the flips log file
     *
     * @return the log file
     */
    public File getLogFile() {
        if (logFile == null)
            logFile = new File(profileFile, "log.csv");
        return logFile;
    }

    /**
     * Set the log file of this profile
     * @param file the file to set the file to
     */
    void setLogFile(File file) {
        this.logFile = file;
    }
}

