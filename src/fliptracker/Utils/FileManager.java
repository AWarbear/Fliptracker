package fliptracker.Utils;

import fliptracker.UIComponents.Controllers.GuiController;
import fliptracker.UIComponents.ItemPanel;
import javafx.stage.FileChooser;

import java.io.*;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

/**
 * Handles all the file actions
 */
public class FileManager {

    private File file;
    private BufferedWriter bw;
    private BufferedReader br;

    /**
     * Load a margins file (opens up a file chooser dialog)
     * @param activeItems the list to add loaded active flips to
     * @param logItems the list to add loaded logged flips to
     * @param controller gui controller
     */
    public void load(List<ItemPanel> activeItems, List<ItemPanel> logItems, GuiController controller) {
        this.file = null;
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Open File");
        fileChooser.getExtensionFilters().addAll(new FileChooser.ExtensionFilter("CSV files", "*.csv", "*.CSV"));
        fileChooser.setInitialDirectory(new File(System.getProperty("user.home")));
        this.file = fileChooser.showOpenDialog(null);
        if (this.file != null) {
            try {
                this.br = new BufferedReader(new FileReader(this.file));
            }
            catch (FileNotFoundException e1) {
                e1.printStackTrace();
            }
            try {
                this.br.skip(1);
                String line = this.br.readLine();
                activeItems.clear();
                logItems.clear();
                while (line != null) {
                    String[] values = line.split(",");
                    if (values[0].equals("Active")) {
                        controller.addItem(values[4], values[1], Integer.parseInt(values[2]), Integer.parseInt(values[3]), Long.parseLong(values[5]));
                    }
                    if (values[0].equals("Log")) {
                        controller.addLogItem(values[4], values[1], Integer.parseInt(values[2]), Integer.parseInt(values[3]), Long.parseLong(values[5]), values.length == 7 ? Integer.parseInt(values[6]) : 0);
                    }
                    line = this.br.readLine();
                }
                this.br.close();
            }
            catch (IOException e1) {
                e1.printStackTrace();
            }
        }
    }

    /**
     * Load a margins file (from a specified file)
     * @param activeItems the list to add loaded active flips to
     * @param logItems the list to add loaded logged flips to
     * @param logFile the file to load from
     * @param controller gui controller
     */
    public void load(List<ItemPanel> activeItems, List<ItemPanel> logItems, File logFile, GuiController controller) {
        if (logFile != null) try {
            if (!logFile.exists()) {
                logFile.createNewFile();
            }
            this.br = new BufferedReader(new FileReader(logFile));
            this.br.skip(1);
            String line = this.br.readLine();
            activeItems.clear();
            logItems.clear();
            while (line != null) {
                String[] values = line.split(",");
                if (values[0].equals("Active"))
                    controller.addItem(values[4], values[1], Integer.parseInt(values[2]), Integer.parseInt(values[3]), Long.parseLong(values[5]));
                if (values[0].equals("Log"))
                    controller.addLogItem(values[4], values[1], Integer.parseInt(values[2]), Integer.parseInt(values[3]), Long.parseLong(values[5]), values.length == 7 ? Integer.parseInt(values[6]) : 0);
                line = this.br.readLine();
            }
            this.br.close();
        } catch (IOException e1) {
            e1.printStackTrace();
        }
    }

    /**
     * Open file chooser dialogue and return the selected file
     * @return the file selected
     */
    public File getChosenFile() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Open File");
        fileChooser.setInitialDirectory(new File(System.getProperty("user.home")));
        return fileChooser.showOpenDialog(null);
    }

    /**
     * Open a file chooser dialogue and return a file (Only allows wav files)
     * @return the audio file chosen
     */
    public File getWavFile() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Open File");
        fileChooser.getExtensionFilters().addAll(new FileChooser.ExtensionFilter("WAV files", "*.wav", "*.WAV", "*.Wav"));
        fileChooser.setInitialDirectory(new File(System.getProperty("user.home")));
        return fileChooser.showOpenDialog(null);
    }

    /**
     * Save margins to a file specified by a file chooser dialogue
     * @param activeItems the active flips list to write to the file
     * @param logItems the log flips list to write to the file
     * @param controller gui controller
     */
    public void save(List<ItemPanel> activeItems, List<ItemPanel> logItems, GuiController controller) {
        this.file = null;
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Save File");
        this.file = fileChooser.showSaveDialog(null);
        controller.profileManager.setLogFile(this.file);
        if (this.file != null) {
            try {
                String line;
                int i;
                ItemPanel item;
                this.bw = this.file.getAbsolutePath().toLowerCase().endsWith(".csv") ? new BufferedWriter(new FileWriter(this.file, false)) : new BufferedWriter(new FileWriter(this.file + ".csv", false));
                this.bw.write("state,itemName,price,amount,type,date,duration");
                this.bw.newLine();
                for (i = 0; i < activeItems.size(); ++i) {
                    item = activeItems.get(i);
                    line = "Active," + item.itemName + "," + item.price + "," + item.amount + "," + item.type + "," + item.getTime() + "," + item.getDuration();
                    this.bw.write(line);
                    this.bw.newLine();
                }
                for (i = 0; i < logItems.size(); ++i) {
                    item = logItems.get(i);
                    line = "Log," + item.itemName + "," + item.price + "," + item.amount + "," + item.type + "," + item.getTime() + item.getDuration();
                    this.bw.write(line);
                    this.bw.newLine();
                }
                this.bw.flush();
                this.bw.close();
            } catch (IOException ie) {
                ie.printStackTrace();
            }
        }
    }

    /**
     * Write to a file
     * @param lines to write (line/line in file)
     * @param targetFile the file to write to
     */
    public void save(ArrayList<String> lines, File targetFile) {
        if (targetFile == null) {
            Logger.Log("File is null");
            return;
        }
        try {
            this.bw = new BufferedWriter(new FileWriter(targetFile, false));
            for (String line : lines) {
                this.bw.write(line);
                this.bw.newLine();
            }
            this.bw.flush();
            this.bw.close();
        }
        catch (IOException ie) {
            ie.printStackTrace();
        }
    }

    /**
     * Read the updates from the update file online
     * @return the update lines (a bunch of html tags)
     */
    public String readUpdates() {
        String result = "";
        try {
            String line;
            URL updateUrl = new URL("http://sissas.eu5.org/Fliptracker/updateLog.txt");
            BufferedReader in = new BufferedReader(new InputStreamReader(updateUrl.openStream()));
            while ((line = in.readLine()) != null) {
                result = result + "<li>" + line + "</li>";
            }
        }
        catch (IOException ioe) {
            ioe.printStackTrace();
        }
        return result;
    }

    /**
     * Return the lines in a file, used for loading settings
     * @param file file to load from
     * @return the contents of the file in lines
     */
    public ArrayList<String> getFileLines(File file) {
        ArrayList<String> lines = new ArrayList<>();
        if (file != null) {
            try {
                this.br = new BufferedReader(new FileReader(file));
                String curLine = this.br.readLine();
                if (curLine == null) {
                    Logger.Log("No lines!");
                    this.br.close();
                    return null;
                }
                while (curLine != null) {
                    lines.add(curLine);
                    curLine = this.br.readLine();
                }
                this.br.close();
            }
            catch (IOException ioe) {
                ioe.printStackTrace();
            }
        }
        return lines;
    }

    /**
     * Used for dumping runewiki prices
     * @param limits map to dump the limits to
     */
    public void dumpPrices(HashMap<String, Integer> limits) {
        Logger.Log("Start dumping wiki limits");
        long time = System.currentTimeMillis();
        int oldSize = limits.size();
        try {
            WebPage page = new WebPage("http://runescape.wikia.com/wiki/Grand_Exchange/Buying_limits");
            page.load();
            ArrayList<String> lines = page.getLines();
            Iterator<String> iterator = lines.iterator();
            try {
                while (iterator.hasNext()) {
                    int limit;
                    String line = iterator.next();
                    if (!line.startsWith("<td><a href=")) continue;
                    String itemName = line.substring(line.indexOf("title=\"Exchange:") + "title=\"Exchange:".length(), line.indexOf("\">"));
                    line = iterator.next();
                    String lmt = line.substring("</td><td>".length()).replace(",", "");
                    try {
                        limit = Integer.parseInt(lmt);
                    } catch (NumberFormatException nfe) {
                        limit = 0;
                    }
                    limits.put(itemName, limit);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        Logger.Log("Finished dumping wiki limits, took: " + (System.currentTimeMillis()-time)/1000 + " seconds.");
        Logger.Log("Added: " + (limits.size()-oldSize) + " new limits");
    }

}

