package com.graphics.lib.control;

import java.awt.event.KeyEvent;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.util.List;
import java.util.Optional;

import org.apache.commons.lang3.StringUtils;

import com.google.common.collect.Lists;

public class KeyConfigurationItem {

	private int keyCode;
	
	private String onPress;
	
	private String onRelease;

	private List<String> pressParams;
	
	private List<String> releaseParams;
	
	private Optional<Method> onPressMethod;
	
	private Optional<Method> onReleaseMethod;

	public int getKeyCode() {
		return keyCode;
	}

	public void setKeyCode(String keyCode) {
		if (keyCode.length() == 1) {
	    	 this.keyCode = KeyEvent.getExtendedKeyCodeForChar((int)keyCode.charAt(0));
	     } else {
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
		this.onPressMethod = getMethod(cl, onPress);
	}

	public void setOnReleaseMethod(Class<?> cl) {
		this.onReleaseMethod = getMethod(cl, onRelease);
	}
	
	public void invokePress(Object obj) {
	    onPressMethod.ifPresent(m -> invoke(obj, m, pressParams));
	}
	
	public void invokeRelease(Object obj) {
	    onReleaseMethod.ifPresent(m -> invoke(obj, m, releaseParams));
	}
	
	private void invoke(Object obj, Method method, List<String> params){
		if (method != null && obj != null) {
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
	
	private Optional<Method> getMethod(final Class<?> cl, final String name) {
		if (StringUtils.isEmpty(name)) {
		    return Optional.empty();
		}
	    
		return Lists.newArrayList(cl.getMethods())
        		    .stream()
        		    .filter(method -> matchMethod(method, name))
        		    .findFirst();

	}
	
	private boolean matchMethod(Method method, String name) {
	  //allow for case-less match
	    return method.getName().equalsIgnoreCase(name) && (method.getParameterCount() == 0 ||
                    (method.getParameterCount() == 1 && List.class.isAssignableFrom(method.getParameters()[0].getType())
                            && method.getGenericParameterTypes().length == 1 && String.class.isAssignableFrom((Class<?>)((ParameterizedType)method.getGenericParameterTypes()[0]).getActualTypeArguments()[0])));
	}

	
}
