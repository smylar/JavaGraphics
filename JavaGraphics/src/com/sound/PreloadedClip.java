package com.sound;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.util.Optional;
import java.util.function.Supplier;

import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.Line;

/**
 * Load sounds into active lines.
 * Use for short repeatedly used sounds requiring fast response (i.e. Sound effects)
 * 
 * @author paul.brandon
 *
 */
public final class PreloadedClip implements Supplier<Optional<Clip>>, Closeable {

    private final Optional<Clip> clip;
    
    public PreloadedClip (File file) {
        
        Clip newClip = null;
        try {
            newClip = (Clip)AudioSystem.getLine(new Line.Info(Clip.class));           
            
            newClip.open(AudioSystem.getAudioInputStream(file));
            
        } catch (Exception e) {
            System.out.println(e.getMessage());
            newClip = null;
        }
        
        this.clip = Optional.ofNullable(newClip);
    }
    
    @Override
    public Optional<Clip> get() {
        return clip;
    }

    @Override
    public void close() throws IOException {
        clip.ifPresent(Clip::close);
    }

}
