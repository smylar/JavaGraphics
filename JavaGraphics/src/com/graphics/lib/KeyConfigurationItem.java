package com.graphics.lib;

public class KeyConfigurationItem {
	public static enum EVENT_TYPE{
		PRESS, RELEASE
	};
	
	private int keyCode;
	
	private EVENT_TYPE eventType;
	
	private String methodName;

	public int getKeyCode() {
		return keyCode;
	}

	public void setKeyCode(int keyCode) {
		this.keyCode = keyCode;
	}

	public EVENT_TYPE getEventType() {
		return eventType;
	}

	public void setEventType(EVENT_TYPE eventType) {
		this.eventType = eventType;
	}

	public String getMethodName() {
		return methodName;
	}

	public void setMethodName(String methodName) {
		this.methodName = methodName;
	}
}
