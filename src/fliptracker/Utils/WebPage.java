/*
 * Decompiled with CFR 0_102.
 */
package fliptracker.Utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;

class WebPage {
    private URL url;
    private ArrayList<String> lines;

    public WebPage(String url) throws MalformedURLException {
        if (!url.startsWith("http://")) {
            url = "http://" + url;
            this.url = new URL(url);
        }
        this.url = new URL(url);
    }

    public void load() throws IOException {
        String line;
        this.lines = new ArrayList();
        URLConnection c = this.url.openConnection();
        c.setReadTimeout(3000);
        BufferedReader stream = new BufferedReader(new InputStreamReader(c.getInputStream()));
        while ((line = stream.readLine()) != null) {
            this.lines.add(line);
        }
        stream.close();
    }

    public ArrayList<String> getLines() {
        return this.lines;
    }
}

