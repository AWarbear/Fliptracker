/*
 * Decompiled with CFR 0_102.
 */
package fliptracker.Audio;

import java.io.File;
import java.net.URL;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.DataLine;

public class SoundEffect {
    public Clip clip;
    private AudioInputStream audioInputStream;

    public SoundEffect(String Name, String Path) {
        String name = Name;
        try {
            this.audioInputStream = AudioSystem.getAudioInputStream(SoundEffect.getResource("Sounds/" + Path));
            AudioFormat format = this.audioInputStream.getFormat();
            DataLine.Info info = new DataLine.Info(Clip.class, format);
            this.clip = (Clip)AudioSystem.getLine(info);
            this.clip.open(this.audioInputStream);
        }
        catch (Exception ex) {
            System.out.println("Error with loading sound.");
            ex.printStackTrace();
        }
    }

    public SoundEffect(File file) {
        try {
            this.audioInputStream = AudioSystem.getAudioInputStream(file);
            AudioFormat format = this.audioInputStream.getFormat();
            DataLine.Info info = new DataLine.Info(Clip.class, format);
            this.clip = (Clip)AudioSystem.getLine(info);
            this.clip.open(this.audioInputStream);
        }
        catch (Exception ex) {
            System.out.println("Error with loading sound.");
            ex.printStackTrace();
        }
    }

    private static URL getResource(String res) {
        return SoundEffect.class.getResource("/fliptracker/res/" + res);
    }
}

