package com.sound;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLDecoder;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;

import javax.sound.sampled.Clip;
import javax.sound.sampled.FloatControl;

/**
 * Pre-loads sound clips that can be played repeatedly on demand.
 * <br/>
 * Will automatically load sounds detailed in sounds.txt on instantiation, which will refer to sound resources in the same com.sound package
 * <br/>
 * sounds.txt simply consists of a key name and a file name separated by a colon (:)
 * 
 * @author Paul Brandon
 *
 */
public class ClipLibrary implements AutoCloseable {
	//private Map<String,Clip> clips = new HashMap<>();
	private Map<String,Supplier<Optional<Clip>>> clipSupplier = new HashMap<>();
	
	public ClipLibrary(String soundFile, String musicFile) {
	    loadClips(soundFile, PreloadedClip.class);
	    loadClips(musicFile, OndemandClip.class);
	}
	
	private <T extends Supplier<Optional<Clip>>> void loadClips(String soundFile, Class<T> clazz) {
	    URL soundList = getClass().getResource(soundFile);
        if (soundList == null) 
            return;
        
        try (BufferedReader br = new BufferedReader(new FileReader(URLDecoder.decode(soundList.getFile(),"UTF-8"))))
        {
            String line;
            while ((line = br.readLine()) != null) {
                String[] split = line.split(":");
                if (split.length != 2) 
                    continue;
                
                parseSound(split[1]).ifPresent(sound -> addClip(split[0].trim(), sound, clazz));
            }
        }
        catch (Exception ex)
        {
            System.out.println("ClipLibrary:: " + ex.getMessage());
        }
	}
	
//	public ClipLibrary(String soundFile) {
//		URL soundList = getClass().getResource(soundFile);
//		if (soundList == null) return;
//		
//		try (BufferedReader br = new BufferedReader(new FileReader(URLDecoder.decode(soundList.getFile(),"UTF-8"))))
//		{
//			String line;
//			while ((line = br.readLine()) != null){
//				String[] split = line.split(":");
//				if (split.length != 2) continue;
//				
//				URL sound = getClass().getResource(split[1].trim());
//				if (sound == null) continue;
//				
//				addSound(split[0].trim(), new File(URLDecoder.decode(sound.getFile(),"UTF-8")));
//			}
//		}
//		catch (Exception ex)
//		{
//			System.out.println("ClipLibrary:: " + ex.getMessage());
//		}
//	}

//	/**
//	 * Add a sound to the library
//	 * 
//	 * @param key	Key of the new sound
//	 * @param file	File containing the sound
//	 * @return		<code>True</code> if sound was successfully added <code>False</code> otherwise
//	 */
//	public boolean addSound(String key, File file){
//		try{
//			Clip clip = (Clip)AudioSystem.getLine(new Line.Info(Clip.class));			
//			
//			clip.open(AudioSystem.getAudioInputStream(file));
//			
//			clips.put(key, clip);
//		}
//		catch(Exception ex)
//		{
//			System.out.println("ClipLibrary::addSound:: " + ex.getMessage());
//			return false;
//		}
//		return true;
//	}
	
	public void playSound(String key){
		playSound(key, false, 0);
	}
	
	public void playSound(String key, float gain){
		playSound(key, false, gain);
	}
	
	public void playSound(String key, boolean wait){
		playSound(key, wait, 0);
	}
	
	/**
	 * Play sound only once from the start of the sound. If another request is made to play this sound while running, then the running sound is stopped and restarted
	 * 
	 * @param key	Key of the sound to play
	 * @param wait	Flag indicating whether method should wait for sound to finish before returning
	 * @param gain	Decibel modification
	 */
	public void playSound(String key, boolean wait, float gain){
		try{		
			Clip clip = clipSupplier.get(key).get().orElse(null); //TODO complete refactor for optional
			 
			if (clip != null && clip.isOpen()){
				clip.stop();
				clip.setFramePosition(0);
				FloatControl gainControl =  (FloatControl) clip.getControl(FloatControl.Type.MASTER_GAIN);
				gainControl.setValue(gain);
				clip.start();
				if (wait) {
				    clip.drain();
				}
			}
		}
		catch(Exception ex)
		{
			System.out.println("ClipLibrary::playSound:: " + ex.getMessage());
		}
	}
	
	public void loopSound(String key){
		loopSound(key,0);
	}
	
	/**
	 * Play sound continuously until stopped, if this sound is already playing it will not attempt to start it again
	 * 
	 * @param key	Key of the sound to play
	 * @param gain	Decibel modification
	 */
	public void loopSound(String key, float gain){
		try{
		    Clip clip = clipSupplier.get(key).get().orElse(null);
			if (clip != null && clip.isOpen() && !clip.isRunning()){
				clip.setFramePosition(0);
				FloatControl gainControl =  (FloatControl) clip.getControl(FloatControl.Type.MASTER_GAIN);
				gainControl.setValue(gain);
				clip.loop(Clip.LOOP_CONTINUOUSLY);
			}
		}
		catch(Exception ex)
		{
			System.out.println("ClipLibrary::loopSound:: " + ex.getMessage());
		}
	}
	
	/**
	 * Stop a sound playing
	 * 
	 * @param key	Key of the sound to stop
	 */
	public void stopSound(String key){
		Clip clip = clipSupplier.get(key).get().orElse(null);
		if (clip != null && clip.isRunning()){
			clip.stop();
		}
	}
	
	/**
	 * If a sound was stopped before completing resume will start it again from where it stopped
	 * <br/>
	 * N.B. If sound is looped it will stop looping using resume, in that case just call {@link #loopSound(String, float)} again
	 * 
	 * @param key	Key of the sound to resume
	 */
	public void resumeSound(String key){
		try{
		    Clip clip = clipSupplier.get(key).get().orElse(null);
			if (clip != null && clip.isOpen() && !clip.isRunning()){
				clip.start();
			}
		}
		catch(Exception ex)
		{
			System.out.println("ClipLibrary::resumeSound:: " + ex.getMessage());
		}
	}
	
	@Override
	public void close() throws Exception {
//		for(Clip clip : clip.values()){
//			clip.close();
//		}
//		clips.clear();
		
	    clipSupplier.values().stream()
	                         .map(Supplier::get)
	                         .filter(Optional::isPresent)
	                         .map(Optional::get)
	                         .forEach(Clip::close);
	    clipSupplier.clear();
	}
	
	private Optional<String> parseSound(String line) throws UnsupportedEncodingException {
        
        URL sound = getClass().getResource(line.trim());
        if (sound == null) 
            return Optional.empty();
        
        return Optional.of(URLDecoder.decode(sound.getFile(),"UTF-8"));
	}
	
	private <T extends Supplier<Optional<Clip>>> void addClip(String name, String sound, Class<T> clazz) {
	    try {
            clipSupplier.put(name, clazz.getDeclaredConstructor(File.class).newInstance(new File(sound)));
        } catch (Exception e) {
            e.printStackTrace();
        }
	}
}
