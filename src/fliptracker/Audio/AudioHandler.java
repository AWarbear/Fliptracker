/*
 * Decompiled with CFR 0_102.
 */
package fliptracker.Audio;

import fliptracker.Audio.SoundEffect;

import java.util.ArrayList;

public class AudioHandler {
    public final ArrayList<SoundEffect> soundEffect = new ArrayList<>();

    public AudioHandler() {
        this.soundEffect.add(new SoundEffect("Done", "Ding.wav"));
        this.soundEffect.add(new SoundEffect("Rule", "Beep.wav"));
    }

    public void playSound(String soundName) {
        SoundEffect effect;
        switch (soundName) {
            case "Done": {
                effect = this.soundEffect.get(0);
                break;
            }
            case "Rule": {
                effect = this.soundEffect.get(1);
                break;
            }
            case "Custom": {
                effect = this.soundEffect.get(2);
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

