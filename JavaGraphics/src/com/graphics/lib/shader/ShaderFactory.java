package com.graphics.lib.shader;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.function.Supplier;

/**
 * Sets up various shaders that can be used, they are placed on a queue to enable re-use
 * The user of the shader should ensure that close is called once done with it, so that it is returned to the queue 
 * 
 * @author Paul Brandon
 *
 */
public enum ShaderFactory implements IShaderFactory{
		GORAUD(GoraudShader::new, 8), 
		TEXGORAUD(TexturedGoraudShader::new, 8), 
		FLAT(FlatShader::new, 8), 
		NONE(DefaultShader::new, 8);

	private LinkedBlockingQueue<IShader> pool;
	
	private ShaderFactory(Supplier<IShader> shader, int poolSize) {
        pool = new LinkedBlockingQueue<>(poolSize);
        for (int i = 0 ; i < poolSize ; i++) {
            IShader shaderImpl = shader.get();
            shaderImpl.setCloseAction(pool::add);
            pool.add(shaderImpl);
        }
	}
	
	@Override
	public IShader getShader() {	 
	    try {
            return pool.take();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
	    return null;
	}
}
