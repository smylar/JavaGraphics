package com.graphics.lib;

import java.awt.event.KeyEvent;
import java.lang.reflect.Method;
import java.util.List;

public class KeyConfigurationItem {

	private int keyCode;
	
	private String onPress;
	
	private String onRelease;

	private List<String> pressParams;
	
	private List<String> releaseParams;
	
	private Method onPressMethod;
	
	private Method onReleaseMethod;

	public int getKeyCode() {
		return keyCode;
	}

	public void setKeyCode(String keyCode) {
		if (keyCode.length() == 1){
	    	 this.keyCode = KeyEvent.getExtendedKeyCodeForChar((int)keyCode.charAt(0));
	     }else{
	    	 this.keyCode = Integer.parseInt(keyCode);
	     }
	}

	public String getOnPress() {
		return onPress;
	}

	public void setOnPress(String onPress) {
		this.onPress = onPress;
	}

	public String getOnRelease() {
		return onRelease;
	}

	public void setOnRelease(String onRelease) {
		this.onRelease = onRelease;
	}

	public void setPressParams(List<String> pressParams) {
		this.pressParams = pressParams;
	}

	public void setReleaseParams(List<String> releaseParams) {
		this.releaseParams = releaseParams;
	}

	public void setOnPressMethod(Class<?> cl) {
		this.onPressMethod = this.getMethod(cl, onPress);
	}

	public void setOnReleaseMethod(Class<?> cl) {
		this.onReleaseMethod = this.getMethod(cl, onRelease);
	}
	
	public void invokePress(Object obj){
		if (this.onPressMethod != null){
			try {
				if (this.onPressMethod.getParameterCount() > 0)
					this.onPressMethod.invoke(obj, this.pressParams);
				else
					this.onPressMethod.invoke(obj);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
	
	public void invokeRelease(Object obj){
		if (this.onReleaseMethod != null){
			try {
				if (this.onReleaseMethod.getParameterCount() > 0)
					this.onReleaseMethod.invoke(obj, this.releaseParams);
				else
					this.onReleaseMethod.invoke(obj);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
	
	private Method getMethod(Class<?> cl, String name){
		if (name == null || name.length() == 0) return null;
		
		Method[] methods = cl.getMethods();
		for (int i = 0 ; i < methods.length ; i++) //allow for case-less match
		{
			if (methods[i].getName().toLowerCase().equals(name.toLowerCase())){
				return methods[i];
			}
		}
		
		return null;
	}

	
}
