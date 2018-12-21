package com.graphics.lib.util;

public enum NumberUtils {
    
    NUMBERS;
    
    public double toPostive (double number) {
        return number < 0 ? -number : number;
    }

}
