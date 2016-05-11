package com.graphics.lib.control;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.graphics.lib.canvas.CanvasObject;

/**
 * Handles input that relates to a given object.
 * Key configurations are held in a separate file to allow for key re-mapping.
 * Key mappings detail which method is called within the concrete ObjectInputController object using that file
 * 
 * @author Paul Brandon
 *
 * @param <T> The object being controlled
 */
public abstract class ObjectInputController<T extends CanvasObject> implements KeyListener {
	public static final String FORWARD = "FORWARD";
	public static final String BACKWARD = "BACKWARD";
	public static final String UP = "UP";
	public static final String DOWN = "DOWN";
	public static final String LEFT = "LEFT";
	public static final String RIGHT = "RIGHT";
	public static final String PAN_LEFT = "PAN_LEFT";
	public static final String PAN_RIGHT = "PAN_RIGHT";
	public static final String PAN_UP = "PAN_UP";
	public static final String PAN_DOWN = "PAN_DOWN";
	public static final String ROLL_LEFT = "ROLL_LEFT";
	public static final String ROLL_RIGHT = "ROLL_RIGHT";
	
	protected T controlledObject;

	protected Map<Integer,KeyConfigurationItem> keyMap = new HashMap<Integer,KeyConfigurationItem>();
	
	/**
	 * Create controller for the controlled object using a given key mappings resource
	 * 
	 * @param controlledObject - The object being controlled
	 * @param resource - Resource containing the key mappings
	 * @throws Exception
	 */
	public ObjectInputController(T controlledObject, String resource) throws Exception{
		this.controlledObject = controlledObject;
		this.readResource(resource);
	}
	
	/**
	 * Create controller for the controlled object using default resource
	 * (class name of the concrete ObjectInputController).kcf within the same package as that class
	 * 
	 * @param controlledObject - The object being controlled
	 * @throws Exception
	 */
	public ObjectInputController(T controlledObject) throws Exception{
		this.controlledObject = controlledObject;
		this.readResource(this.getClass().getSimpleName() + ".kcf");
	}
	
	/**
	 * Read the key configuration resource into internal map
	 * 
	 * @param resource - The JSON file resource containing the key mappings
	 * @throws Exception
	 */
	public void readResource(String resource) throws Exception{
		ObjectMapper mapper = new ObjectMapper();
		JsonNode root = mapper.readTree(this.getClass().getResourceAsStream(resource));
		KeyConfiguration config = mapper.readValue(root.at("/config").toString(), KeyConfiguration.class);
		
		for (KeyConfigurationItem item : config.getKeyList()){
			item.setOnPressMethod(this.getClass());
			item.setOnReleaseMethod(this.getClass());
			keyMap.put(item.getKeyCode(), item);
		}
	}
	
	/**
	 * @return The object being controlled by this controller
	 */
	public T getControlledObject() {
		return controlledObject;
	}

	/**
	 * Retrieve method for the pressed key from the map and invoke it if it exists
	 */
	@Override
	public void keyPressed(KeyEvent key) {
		KeyConfigurationItem item = this.keyMap.get(key.getExtendedKeyCode());
		if (item != null){
			item.invokePress(this);
		}
	}
	
	/**
	 * Retrieve method for the released key from the map and invoke it if it exists
	 */
	@Override
	public void keyReleased(KeyEvent key) {
		KeyConfigurationItem item = this.keyMap.get(key.getExtendedKeyCode());
		if (item != null){
			item.invokeRelease(this);
		}
	}
	
	/**
	 * Not used
	 */
	@Override
	public void keyTyped(KeyEvent arg0) {}

}
