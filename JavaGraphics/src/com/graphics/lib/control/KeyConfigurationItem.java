package com.graphics.lib.control;

import java.awt.event.KeyEvent;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
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

	public void setOnPress(String onPress) {
		this.onPress = onPress;
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
		this.invoke(obj, onPressMethod, pressParams);
	}
	
	public void invokeRelease(Object obj){
		this.invoke(obj, onReleaseMethod, releaseParams);
	}
	
	private void invoke(Object obj, Method method, List<String> params){
		if (method != null && obj != null){
			try {
				if (method.getParameterCount() == 1)
					method.invoke(obj, params);
				else if (method.getParameterCount() == 0)
					method.invoke(obj);
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
			if (methods[i].getName().toLowerCase().equals(name.toLowerCase()) && (methods[i].getParameterCount() == 0 ||
					(methods[i].getParameterCount() == 1 && List.class.isAssignableFrom(methods[i].getParameters()[0].getType())
							&& methods[i].getGenericParameterTypes().length == 1 && String.class.isAssignableFrom((Class<?>)((ParameterizedType)methods[i].getGenericParameterTypes()[0]).getActualTypeArguments()[0])))){
				return methods[i];
			}
		}
		
		return null;
	}

	
}
