package com.graphics.lib.interfaces;

/**
 * Interface for an object that can be animated
 * 
 * @author paul.brandon
 *
 */
public interface IAnimatable extends ITrait {
    /**
     * Start an animation sequence
     * @param key The sequence name
     */
	public void startAnimation(String key);
	
	/**
	 * Stop an animation sequence
	 * @param key The sequence name
	 */
	public void stopAnimation(String key);
}
