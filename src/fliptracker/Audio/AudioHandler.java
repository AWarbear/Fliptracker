package fliptracker.Audio;

import java.util.ArrayList;

/**
 * Audio handler for handling sound effects
 */
public class AudioHandler {

    public final ArrayList<SoundEffect> soundEffect = new ArrayList<>();

    /**
     * Initiate sound effects
     */
    public AudioHandler() {
        soundEffect.add(new SoundEffect("Done", "Ding.wav"));
        soundEffect.add(new SoundEffect("Rule", "Beep.wav"));
    }

    /**
     * Play a sound with the give name
     * @param soundName the effect name
     */
    public void playSound(String soundName) {
        SoundEffect effect;
        switch (soundName) {
            case "Done": {
                effect = soundEffect.get(0);
                break;
            }
            case "Rule": {
                effect = soundEffect.get(1);
                break;
            }
            case "Custom": {
                effect = soundEffect.get(2);
                break;
            }
            default: {
                return;
            }
        }
        effect.clip.stop();
        effect.clip.setFramePosition(0);
        effect.clip.start();
    }
}

