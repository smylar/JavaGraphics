package com.graphics.lib;

import java.util.function.Supplier;

import com.graphics.lib.interfaces.IZBuffer;
import com.graphics.lib.zbuffer.ZBuffer;

public enum ZBufferEnum {
	DEFAULT(ZBuffer::new);
	
	private Supplier<IZBuffer> supplier;
	
	private ZBufferEnum(Supplier<IZBuffer> supplier) {
		this.supplier = supplier;
	}
	
	public IZBuffer get() {
		return supplier.get();
	}
}
