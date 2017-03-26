package com.graphics.lib.canvas;

import java.util.HashSet;

import com.graphics.lib.ZBufferEnum;
import com.graphics.lib.camera.Camera;
import com.graphics.lib.interfaces.ICanvasObject;
import com.graphics.lib.interfaces.ICanvasUpdateListener;
import com.graphics.lib.shader.ShaderFactory;

/**
 * Provides another view of the same scene in the parent
 * 
 * (This sometimes seems to paint double on start, not sure why yet)
 * @author paul
 *
 */
public class SlaveCanvas3D extends Canvas3D implements ICanvasUpdateListener {
	private static final long serialVersionUID = 1L;
	
	//Canvas3D parent;
	
	public SlaveCanvas3D(Camera camera)
	{
		super(camera);	
		//this.parent = parent;
		//parent.addObserver(this);
	}
	
	/*@Override
	public void doDraw(CanvasObject obj)
	{
		while(this.isOkToPaint()){
			try {
				Thread.sleep(1);
			} catch (InterruptedException e) {}
		}
		
		if (this.getzBuffer() == null)
			this.setzBuffer(Utils.getDefaultZBuffer());

		this.getzBuffer().setDimensions(this.getWidth(), this.getHeight());

		Set<CanvasObject> processShapes = new HashSet<CanvasObject>(parent.getShapes());
		processShapes.removeIf(s -> s.isDeleted() || s.isObserving());
		
		processShapes.parallelStream().forEach(s -> {
			this.processShape(s, this.getzBuffer(), parent.getShader(s));
		});
		this.setOkToPaint(true);
		this.repaint();
		
	}*/
	

	private void processShape(Canvas3D source, ICanvasObject obj, ShaderFactory shader)
	{
		//if (this.isOkToPaint()) return;
		while(this.isOkToPaint()){
			//while isOkToPaint() is true then repaint has not yet happened since the last draw cycle
			try {
				Thread.sleep(1);
			} catch (InterruptedException e) {}
		}
		
		if (this.getzBuffer() == null)
			this.setzBuffer(ZBufferEnum.DEFAULT.get());

		this.getzBuffer().setDimensions(this.getWidth(), this.getHeight());
		
		if (obj.isVisible())
		{
			this.getCamera().getView(obj);
			
			this.getzBuffer().add(obj, shader, this.getCamera(), source.getHorizon());
		}
		for (ICanvasObject child : new HashSet<ICanvasObject>(obj.getChildren()))
		{
			this.processShape(source, child, shader);
		};
	}

	@Override
	public void update(Canvas3D source, ICanvasObject obj) {
		if (obj == null){
			this.setOkToPaint(true);
			this.repaint();
		}
		else{
			this.processShape(source, obj, source.getShader(obj));
		}
	}
}
