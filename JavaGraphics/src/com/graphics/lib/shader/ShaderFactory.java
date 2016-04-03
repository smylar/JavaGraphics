package com.graphics.lib.shader;


public class ShaderFactory {
	public static enum ShaderEnum{
		GORAUD, TEXGORAUD, FLAT, NONE
	}
	
	public static IShader GetShader(ShaderEnum type){
		switch(type){
			case GORAUD: return new GoraudShader();
			case TEXGORAUD: return new TexturedGoraudShader();
			case FLAT: return new FlatShader();
			default: return null;
		}
	}
}
