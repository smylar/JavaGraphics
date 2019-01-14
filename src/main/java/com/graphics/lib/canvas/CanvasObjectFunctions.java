package com.graphics.lib.canvas;

public enum CanvasObjectFunctions {
	DEFAULT(new CanvasObjectFunctionsImpl()),
	TORUS(new TorusFunctionsImpl()),
	SPHERE(new SphereFunctionsImpl());
	
	private CanvasObjectFunctionsImpl impl;
	
	private CanvasObjectFunctions (CanvasObjectFunctionsImpl impl) {
		this.impl = impl;
	}
	
	public CanvasObjectFunctionsImpl get() {
		return impl;
	}

}
