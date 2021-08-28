package com.graphics.lib.util;

public enum DrawMode {
    NORMAL(false),
    POINT(true),
    WIRE(true);
    
    private final boolean fixedShader;
    
    private DrawMode(boolean fixedShader) {
        this.fixedShader = fixedShader;
    }

    public boolean fixedShader() {
        return fixedShader;
    }
}
