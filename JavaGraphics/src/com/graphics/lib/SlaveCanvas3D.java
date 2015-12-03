package com.graphics.lib;

import java.util.stream.Stream;

import com.graphics.lib.Canvas3D;
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
		ZBuffer zBuffer = new ZBuffer();
		zBuffer.setDispHeight(this.getHeight());
		zBuffer.setDispWidth(this.getWidth());

		parent.getShapes().parallelStream().forEach(s -> {
			this.processShape(s, zBuffer, parent.getShader(s));
		});
		this.setzBuffer(zBuffer);
		this.repaint();
	}
	

	private void processShape(CanvasObject obj, IZBuffer zBuf, IShader shader)
	{
		if (obj.isVisible())
		{
			this.getCamera().getView(obj);
			Stream<Facet> facetStream = obj.getFacetList().stream();
			if (shader != null) shader.setLightsources(parent.getLightSources());
			if (!obj.isProcessBackfaces()){
				facetStream = facetStream.filter(GeneralPredicates.isFrontface(this.getCamera()));
			}
			facetStream.filter(GeneralPredicates.isOverHorizon(this.getCamera(), this.getHorizon()).negate()).forEach(f ->{
				f.setFrontFace(GeneralPredicates.isFrontface(this.getCamera()).test(f));
				zBuf.Add(f, obj, shader);
			});
		}
		for (CanvasObject child : obj.getChildren())
		{
			this.processShape(child, zBuf, shader);
		};
	}

	@Override
	public void update(Canvas3D source) {
		this.doDraw();
	}
}
