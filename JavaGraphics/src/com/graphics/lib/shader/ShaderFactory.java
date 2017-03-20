package com.graphics.lib.shader;

import java.util.function.Supplier;

public enum ShaderFactory{
		GORAUD(GoraudShader::new), 
		TEXGORAUD(TexturedGoraudShader::new), 
		FLAT(FlatShader::new), 
		NONE(null);

	
	private Supplier<IShader> shader;
	
	private ShaderFactory(Supplier<IShader> shader) {
	    this.shader = shader;
	}
	
	public IShader getShader(){
		return shader.get();
	}
}
