package com.graphics.lib;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.graphics.lib.canvas.CanvasObject;

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
	
	public ObjectInputController(T controlledObject, String resource) throws Exception{
		this.controlledObject = controlledObject;
		this.readResource(resource);
	}
	
	public ObjectInputController(T controlledObject) throws Exception{
		this.controlledObject = controlledObject;
		this.readResource(this.getClass().getSimpleName() + ".kcf");
	}
	
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
	
	public T getControlledObject() {
		return controlledObject;
	}
	
	public void setControlledObject(T controlledObject) {
		this.controlledObject = controlledObject;
	}

	@Override
	public void keyPressed(KeyEvent key) {
		KeyConfigurationItem item = this.keyMap.get(key.getExtendedKeyCode());
		if (item != null){
			item.invokePress(this);
		}
	}
	
	@Override
	public void keyReleased(KeyEvent key) {
		KeyConfigurationItem item = this.keyMap.get(key.getExtendedKeyCode());
		if (item != null){
			item.invokeRelease(this);
		}
	}
	
	@Override
	public void keyTyped(KeyEvent arg0) {}

}
