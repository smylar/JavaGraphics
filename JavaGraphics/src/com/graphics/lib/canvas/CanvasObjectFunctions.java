package com.graphics.lib.canvas;


public enum CanvasObjectFunctions {
	DEFAULT(new CanvasObjectFunctionsImpl()),
	SPHERE(new SphereFunctionsImpl());
	
	private CanvasObjectFunctionsImpl impl;
	
	private CanvasObjectFunctions (CanvasObjectFunctionsImpl impl) {
		this.impl = impl;
	}
	
	public CanvasObjectFunctionsImpl get() {
		return impl;
	}

}
