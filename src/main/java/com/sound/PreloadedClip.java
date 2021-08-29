package com.sound;

import java.io.BufferedInputStream;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
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
    
    public PreloadedClip (String resource) {
        
        Clip newClip = null;
        try {
            newClip = (Clip)AudioSystem.getLine(new Line.Info(Clip.class));           
            
            InputStream audioFile = new BufferedInputStream(getClass().getClassLoader().getResourceAsStream(resource));
            newClip.open(AudioSystem.getAudioInputStream(audioFile));
            
        } catch (Exception e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
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
