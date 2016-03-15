/*
 * Decompiled with CFR 0_102.
 * 
 * Could not load the following classes:
 *  javafx.collections.ObservableList
 *  javafx.stage.FileChooser
 *  javafx.stage.FileChooser$ExtensionFilter
 *  javafx.stage.Window
 */
package fliptracker.Utils;

import fliptracker.UIComponents.Controllers.GuiController;
import fliptracker.UIComponents.ItemPanel;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import javafx.stage.FileChooser;

public class FileManager {
    private File file;
    private BufferedWriter bw;
    private BufferedReader br;

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

    public File getChosenFile() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Open File");
        fileChooser.setInitialDirectory(new File(System.getProperty("user.home")));
        return fileChooser.showOpenDialog(null);
    }

    public File getWavFile() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Open File");
        fileChooser.getExtensionFilters().addAll(new FileChooser.ExtensionFilter("WAV files", "*.wav", "*.WAV", "*.Wav"));
        fileChooser.setInitialDirectory(new File(System.getProperty("user.home")));
        return fileChooser.showOpenDialog(null);
    }

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
}

