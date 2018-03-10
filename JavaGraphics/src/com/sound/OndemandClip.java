package com.sound;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.util.Objects;
import java.util.Observable;
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
public class OndemandClip extends Observable implements Supplier<Optional<Clip>>, Closeable {

    private final File file;
    private Clip clip;
    
    public OndemandClip(File file) {
        this.file = file;
    }
    
    @Override
    public Optional<Clip> get() {   
        if (Objects.nonNull(clip)) {
            return Optional.ofNullable(clip);
        }
        
            try {
                clip = (Clip)AudioSystem.getLine(new Line.Info(Clip.class));           
                clip.addLineListener(l -> {
                    if (l.getType() == Type.STOP) {
                        clip.close();
                        clip = null;
                        this.setChanged();
                        this.notifyObservers();
                    }
                 });
                clip.open(AudioSystem.getAudioInputStream(file));
                
                return Optional.of(clip);
            } catch (Exception e) {
                System.out.println(e.getMessage());
            }
            
            return Optional.empty();
    }

    @Override
    public void close() throws IOException {
        if (Objects.nonNull(clip)) {
            clip.close();
        }
        this.deleteObservers();
    }

}
