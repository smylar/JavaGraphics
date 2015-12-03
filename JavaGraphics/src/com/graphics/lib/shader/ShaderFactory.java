package com.graphics.lib.shader;


public class ShaderFactory {
	public static enum ShaderEnum{
		GORAUD, FLAT, NONE
	}
	
	public static IShader GetShader(ShaderEnum type){
		switch(type){
			case GORAUD: return new GoraudShader();
			case FLAT: return new FlatShader();
			default: return null;
		}
	}
}
