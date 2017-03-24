package com.graphics.lib.shader;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.function.Supplier;

public enum ShaderFactory{
		GORAUD(GoraudShader::new, 16), 
		TEXGORAUD(TexturedGoraudShader::new, 16), 
		FLAT(FlatShader::new, 16), 
		NONE(DefaultShader::new, 16);

	private LinkedBlockingQueue<IShader> pool;
	
	private ShaderFactory(Supplier<IShader> shader, int poolSize) {
        pool = new LinkedBlockingQueue<>(poolSize);
        for (int i = 0 ; i < poolSize ; i++) {
            IShader shaderImpl = shader.get();
            shaderImpl.setCloseAction(pool::add);
            pool.add(shaderImpl);
        }
	}
	
	public IShader getShader() {	    
	    try {
            return pool.take();
        } catch (InterruptedException e) {
            return null;
        }
	}
}
