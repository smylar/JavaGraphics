package com.graphics.lib;

import java.util.function.Supplier;

import com.graphics.lib.interfaces.IZBuffer;
import com.graphics.lib.zbuffer.ZBuffer;

public enum ZBufferEnum {
	DEFAULT(() -> new ZBuffer()); 
    //interesting - when using method reference for the constructor, pointcut in aspect only gets run once, whereas this triggers it the correct number of times
	
	private Supplier<IZBuffer> supplier;
	
	private ZBufferEnum(Supplier<IZBuffer> supplier) {
		this.supplier = supplier;
	}
	
	public IZBuffer get() {
		return supplier.get();
	}
}
