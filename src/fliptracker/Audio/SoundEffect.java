package fliptracker.Audio;

import javax.sound.sampled.*;
import java.io.File;
import java.net.URL;

/**
 * SoundEffect class used for holding data about sound effects
 */
public class SoundEffect {

    Clip clip;

    private AudioInputStream audioInputStream;
    private String name;

    /**
     * Create the soundeffect from name and path
     * @param name
     * @param Path
     */
    SoundEffect(String name, String Path) {
        this.name = name;
        try {
            audioInputStream = AudioSystem.getAudioInputStream(SoundEffect.getResource("Sounds/" + Path));
            AudioFormat format = audioInputStream.getFormat();
            DataLine.Info info = new DataLine.Info(Clip.class, format);
            clip = (Clip)AudioSystem.getLine(info);
            clip.open(this.audioInputStream);
        }
        catch (Exception ex) {
            System.out.println("Error with loading sound.");
            ex.printStackTrace();
        }
    }

    /**
     * Create a sound effect from file
     * @param file
     */
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

    /**
     * Fetch the resource path of a file
     * @param res resource name/path
     * @return the path to the resource
     */
    private static URL getResource(String res) {
        return SoundEffect.class.getResource("/fliptracker/res/" + res);
    }
}

