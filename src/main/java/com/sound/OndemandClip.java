package com.sound;

import java.io.File;
import java.util.Optional;
import java.util.function.Supplier;

import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.Line;
import javax.sound.sampled.LineEvent.Type;

/**
 * Loads clips from file when requested and closes the line when finished
 * Use with longer sounds, like music tracks
 * 
 * @author paul.brandon
 *
 */
public class OndemandClip implements Supplier<Optional<Clip>> {

    private final File file;
    
    public OndemandClip(File file) {
        this.file = file;
    }
    
    @Override
    public Optional<Clip> get() {   
            try {
                Clip clip = (Clip)AudioSystem.getLine(new Line.Info(Clip.class));           
                clip.addLineListener(l -> {
                    if (l.getType() == Type.STOP) {
                        clip.close();
                    }
                 });
                clip.open(AudioSystem.getAudioInputStream(file));
                
                return Optional.of(clip);
            } catch (Exception e) {
                System.out.println(e.getMessage());
            }
            
            return Optional.empty();
    }
}
