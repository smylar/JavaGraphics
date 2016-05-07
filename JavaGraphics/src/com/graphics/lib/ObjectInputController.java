package com.graphics.lib;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import com.graphics.lib.canvas.Canvas3D;
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
	protected Canvas3D cnv;
	protected Map<Integer,Method> onPressMap = new HashMap<Integer,Method>();
	protected Map<Integer,Method> onReleaseMap = new HashMap<Integer,Method>();
	
	public ObjectInputController(T controlledObject, Canvas3D cnv, KeyConfiguration config) throws Exception{
		this.controlledObject = controlledObject;
		this.cnv = cnv;
		for(KeyConfigurationItem item : config.getKeyList()){
			Method[] methods = this.getClass().getMethods();
			for (int i = 0 ; i < methods.length ; i++) //allow for case-less match
			{
				if (methods[i].getName().toLowerCase().equals(item.getMethodName().toLowerCase())){
					if (item.getEventType() == KeyConfigurationItem.EVENT_TYPE.PRESS){
						onPressMap.put(item.getKeyCode(), methods[i]);
					}
					else if (item.getEventType() == KeyConfigurationItem.EVENT_TYPE.RELEASE){
						onReleaseMap.put(item.getKeyCode(), methods[i]);
					}
					break;
				}
			}
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
		Method m = this.onPressMap.get(key.getExtendedKeyCode());
		if (m != null){
			try {
				m.invoke(this);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
	@Override
	public void keyReleased(KeyEvent key) {
		Method m = this.onReleaseMap.get(key.getExtendedKeyCode());
		if (m != null){
			try {
				m.invoke(this);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
	@Override
	public void keyTyped(KeyEvent arg0) {}

}
