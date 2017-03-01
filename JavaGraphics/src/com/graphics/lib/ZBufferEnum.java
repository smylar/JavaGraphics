package com.graphics.lib;

import com.graphics.lib.interfaces.IZBuffer;
import com.graphics.lib.zbuffer.ZBuffer;

public enum ZBufferEnum {
	DEFAULT(ZBuffer.class);
	
	private Class<? extends IZBuffer> clazz;
	
	private ZBufferEnum(Class<? extends IZBuffer> clazz) {
		this.clazz = clazz;
	}
	
	public IZBuffer get() {
		try {
			return this.clazz.newInstance();
		} catch (InstantiationException | IllegalAccessException e) {
			throw new RuntimeException(e);
		}
	}
}
