package com.sound;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.net.URLDecoder;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Optional;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import javax.sound.sampled.Clip;
import javax.sound.sampled.FloatControl;
import javax.sound.sampled.LineEvent.Type;

import org.apache.commons.io.IOUtils;

import com.google.common.base.Charsets;
import com.graphics.lib.Utils;
import com.graphics.lib.properties.Property;
import com.graphics.lib.properties.PropertyInject;
import com.graphics.lib.properties.PropertyInjected;

/**
 * Class for handling various sounds
 * 
 * @author Paul Brandon
 *
 */
@PropertyInject
public class ClipLibrary implements AutoCloseable, PropertyInjected {
    private static ClipLibrary INSTANCE = null;
	private final Map<String,Supplier<Optional<Clip>>> clipSupplier = new HashMap<>();
	private final ExecutorService musicExecutor = Executors.newSingleThreadExecutor();
	private Optional<Clip> currentTrack;
	
	@Property(name="sounds.effects.location", defaultValue="sounds/")
	private String soundResource;
	@Property(name="sounds.music.location", defaultValue="music/")
	private String musicResource;
	
	private ClipLibrary() {}
	
	public static ClipLibrary getInstance() {
	    if (Objects.isNull(INSTANCE)) {
	        INSTANCE = new ClipLibrary();
	    }
	    return INSTANCE;
	}
	
	public void playMusic() {
	    //TODO stop music
	    currentTrack = getRandomTrack();
	    currentTrack.map(clip -> {
	        clip.addLineListener(l -> {
                    if (l.getType() == Type.STOP) {
                        musicExecutor.execute(this::playMusic);
                    }
             });
	        FloatControl gainControl = (FloatControl) clip.getControl(FloatControl.Type.MASTER_GAIN);
            gainControl.setValue(-25);
	        return clip;
	    }).ifPresent(Clip::start);
	}
	
	public Optional<Clip> playSound(String key) {
		return playSound(key, 0);
	}
	
	/**
	 * Play sound only once from the start of the sound. If another request is made to play this sound while running, then the running sound is stopped and restarted
	 * 
	 * @param key	Key of the sound to play
	 * @param wait	Flag indicating whether method should wait for sound to finish before returning
	 * @param gain	Decibel modification
	 */
	public Optional<Clip> playSound(String key, float gain) {
		try {		
			return getClip(key)
			           .filter(c -> !c.isRunning())
                       .filter(Clip::isOpen)
                       .map(clip -> {
                           clip.setFramePosition(0);
                           FloatControl gainControl = (FloatControl) clip.getControl(FloatControl.Type.MASTER_GAIN);
                           gainControl.setValue(gain);
                           clip.start();
                           return clip;
                       });
		}
		catch(Exception ex)
		{
			System.out.println("ClipLibrary::playSound:: " + ex.getMessage());
		}
		return Optional.empty();
	}
	
	public Optional<Clip> loopSound(String key){
		return loopSound(key, 0, Clip.LOOP_CONTINUOUSLY);
	}
	
	public Optional<Clip> loopSound(String key, float gain){
        return loopSound(key, gain, Clip.LOOP_CONTINUOUSLY);
    }
	
	/**
	 * Play sound continuously until stopped, if this sound is already playing it will not attempt to start it again
	 * 
	 * @param key	Key of the sound to play
	 * @param gain	Decibel modification
	 */
	public Optional<Clip> loopSound(String key, float gain, int loops) {
		try {
		    return getClip(key)
                       .filter(clip -> clip.isOpen() && !clip.isRunning())
                       .map(clip -> {
                           clip.setFramePosition(0);
                           FloatControl gainControl =  (FloatControl) clip.getControl(FloatControl.Type.MASTER_GAIN);
                           gainControl.setValue(gain);
                           clip.loop(loops);
                           return clip;
                       });
		}
		catch(Exception ex)
		{
			System.out.println("ClipLibrary::loopSound:: " + ex.getMessage());
		}
		return Optional.empty();
	}
	
	/**
	 * Stop a sound playing
	 * 
	 * @param key	Key of the sound to stop
	 */
	public void stopSound(String key) {
	    getClip(key)
        .filter(Clip::isRunning)
        .ifPresent(Clip::stop);
	}
	
	/**
	 * If a sound was stopped before completing resume will start it again from where it stopped
	 * <br/>
	 * N.B. If sound is looped it will stop looping using resume, in that case just call {@link #loopSound(String, float)} again
	 * 
	 * @param key	Key of the sound to resume
	 */
	public void resumeSound(String key) {
		try {
		    getClip(key)
            .filter(clip -> clip.isOpen() && !clip.isRunning())
            .ifPresent(Clip::start);
		}
		catch(Exception ex)
		{
			System.out.println("ClipLibrary::resumeSound:: " + ex.getMessage());
		}
	}
	
	@Override
	public void close() throws Exception {
	    currentTrack.ifPresent(Clip::close);
	    musicExecutor.shutdown();
	    clipSupplier.values().stream()
	                         .flatMap(obj -> Utils.cast(obj, Closeable.class).stream())
	                         .forEach(c -> {
                                try {
                                    c.close();
                                } catch (IOException e) { }
                            });
	    clipSupplier.clear();
	}
	
	private Optional<Clip> getClip(String key) {
	    return clipSupplier.getOrDefault(key.toUpperCase(), () -> Optional.empty()).get();
	}
	
	private <T extends Supplier<Optional<Clip>>> void loadClips(String resource, Class<T> clazz) {
        
        try {
            IOUtils.readLines(getClass().getClassLoader()
                    .getResourceAsStream(resource), Charsets.UTF_8)
                    .forEach(line -> 
                        addClip(line.replaceAll("^(.*?)\\..*$", "$1").toUpperCase(), resource + line, clazz)
                    );
        } catch (Exception ex) {
            System.out.println("ClipLibrary:: " + ex.getMessage());
        }
    }
	
	private <T extends Supplier<Optional<Clip>>> void addClip(String name, String sound, Class<T> clazz) {
	    try {
            clipSupplier.put(name, clazz.getDeclaredConstructor(File.class)
                                        .newInstance(new File(URLDecoder.decode(getClass().getClassLoader().getResource(sound).getFile(), "UTF-8"))));
        } catch (Exception e) {
            e.printStackTrace();
        }
	}
	
	private Optional<Clip> getRandomTrack() {
	    //TODO might be nice to take a custom selector
	    List<String> keys = clipSupplier.entrySet()
                    	                .stream()
                    	                .filter(entry -> OndemandClip.class.isAssignableFrom(entry.getValue().getClass()))
                    	                .map(Entry::getKey)
                    	                .collect(Collectors.toList());
	    
	    if (!keys.isEmpty()) {
	        return getClip(keys.get(new Random().nextInt(keys.size())));
	    }
	    
	    return Optional.empty();
	}

    @Override
    public void afterPropertiesSet() {
        loadClips(soundResource, PreloadedClip.class);
        loadClips(musicResource, OndemandClip.class);
    }
}
