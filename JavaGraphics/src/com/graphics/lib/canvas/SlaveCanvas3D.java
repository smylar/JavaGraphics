package com.graphics.lib.canvas;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import com.graphics.lib.Utils;
import com.graphics.lib.camera.Camera;
import com.graphics.lib.interfaces.ICanvasUpdateListener;
import com.graphics.lib.interfaces.IZBuffer;
import com.graphics.lib.shader.IShader;

public class SlaveCanvas3D extends Canvas3D implements ICanvasUpdateListener {
	private static final long serialVersionUID = 1L;
	
	Canvas3D parent;
	
	public SlaveCanvas3D(Camera camera, Canvas3D parent)
	{
		super(camera);	
		this.parent = parent;
		parent.addObserver(this);
	}
	
	@Override
	public void doDraw()
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
	}
	

	private void processShape(CanvasObject obj, IZBuffer zBuf, IShader shader)
	{
		if (obj.isVisible())
		{
			this.getCamera().getView(obj);
			if (shader != null) shader.setLightsources(parent.getLightSources());
			
			zBuf.Add(obj, shader, this.getCamera(), parent.getHorizon());
		}
		for (CanvasObject child : new ArrayList<CanvasObject>(obj.getChildren()))
		{
			this.processShape(child, zBuf, shader);
		};
	}

	@Override
	public void update(Canvas3D source) {
		this.doDraw();
	}
}
