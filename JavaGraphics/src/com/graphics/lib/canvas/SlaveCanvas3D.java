package com.graphics.lib.canvas;

import java.util.ArrayList;

import com.graphics.lib.Utils;
import com.graphics.lib.camera.Camera;
import com.graphics.lib.interfaces.ICanvasUpdateListener;
import com.graphics.lib.shader.IShader;

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
	

	private void processShape(Canvas3D source, CanvasObject obj, IShader shader)
	{
		//if (this.isOkToPaint()) return;
		while(this.isOkToPaint()){
			//while isOkToPaint() is true then repaint has not yet happened since the last draw cycle
			try {
				Thread.sleep(1);
			} catch (InterruptedException e) {}
		}
		
		if (this.getzBuffer() == null)
			this.setzBuffer(Utils.getDefaultZBuffer());

		this.getzBuffer().setDimensions(this.getWidth(), this.getHeight());
		
		if (obj.isVisible())
		{
			this.getCamera().getView(obj);
			if (shader != null) shader.setLightsources(source.getLightSources());
			
			this.getzBuffer().Add(obj, shader, this.getCamera(), source.getHorizon());
		}
		for (CanvasObject child : new ArrayList<CanvasObject>(obj.getChildren()))
		{
			this.processShape(source, child, shader);
		};
	}

	@Override
	public void update(Canvas3D source, CanvasObject obj) {
		if (obj == null){
			this.setOkToPaint(true);
			this.repaint();
		}
		else{
			this.processShape(source, obj, source.getShader(obj));
		}
	}
}
