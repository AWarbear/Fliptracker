/*
 * Decompiled with CFR 0_102.
 */
package fliptracker.Utils;

import java.text.SimpleDateFormat;
import java.util.Date;

public class Logger {
    public static void Log(String message) {
        System.out.println(Logger.getTime() + message);
    }

    private static String getTime() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm:ss");
        Date date = new Date();
        return "[" + dateFormat.format(date) + "] - ";
    }
}

