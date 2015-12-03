package com.graphics.lib;

import java.awt.Graphics;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.stream.Stream;

import javax.swing.JPanel;

import com.graphics.lib.camera.Camera;
import com.graphics.lib.interfaces.ICanvasUpdateListener;
import com.graphics.lib.interfaces.IZBuffer;
import com.graphics.lib.lightsource.LightSource;
import com.graphics.lib.shader.IShader;
import com.graphics.lib.transform.Translation;

public class Canvas3D extends JPanel{

	private static final long serialVersionUID = 1L;
	
	//private List<CanvasObject> shapes = new ArrayList<CanvasObject>();
	private Map<CanvasObject, IShader> shapes = new HashMap<CanvasObject, IShader>();
	private List<LightSource> lightSources = new ArrayList<LightSource>(); 
	private Camera camera;
	private IZBuffer zBuffer;
	private double horizon = 8000;
	private Set<ICanvasUpdateListener> slaves = new HashSet<ICanvasUpdateListener>();
	private List<BiConsumer<Canvas3D,Graphics>> drawPlugins = new ArrayList<BiConsumer<Canvas3D,Graphics>>();

	public Canvas3D(Camera camera)
	{
		this.camera = camera;	
	}
	
	public void addDrawOperation(BiConsumer<Canvas3D,Graphics> operation){
		drawPlugins.add(operation);
	}
	
	public double getHorizon() {
		return horizon;
	}

	public void setHorizon(double horizon) {
		this.horizon = horizon;
	}

	public void addLightSource(LightSource lightSource) {
		this.lightSources.add(lightSource);
	}

	public void registerObject(CanvasObject obj, Point position)
	{
		this.registerObject(obj, position, false);
	}
	
	public Set<CanvasObject> getShapes() {
		return shapes.keySet();
	}
	
	public IShader getShader(CanvasObject obj){
		return shapes.get(obj);
	}
	
	public void replaceShader(CanvasObject obj, IShader shader){
		this.shapes.replace(obj, shader);
	}

	public List<LightSource> getLightSources() {
		return lightSources;
	}

	public Camera getCamera() {
		return camera;
	}

	public void setCamera(Camera camera) {
		this.camera = camera;
	}
	
	public void addObserver(ICanvasUpdateListener l){
		this.slaves.add(l);
	}
	
	protected void setzBuffer(IZBuffer zBuffer) {
		this.zBuffer = zBuffer;
	}
	
	@Override
	public void setVisible(boolean flag)
	{
		super.setVisible(flag);
		this.camera.setViewport(this.getWidth(), this.getHeight());
	}

	public void registerObject(CanvasObject obj, Point position, boolean drawNow){
		this.registerObject(obj, position, drawNow, null);
	}
	
	public synchronized void registerObject(CanvasObject obj, Point position, boolean drawNow, IShader shader)
	{
		if (this.shapes.containsKey(obj)) return;

		this.shapes.put(obj, shader);
		obj.applyTransform(new Translation(position));
		if (drawNow)
		{
			this.doDraw();
		}
	}
	
	public synchronized void doDraw()
	{
		IZBuffer zBuffer = null;
		if (this.zBuffer == null)
			zBuffer = Utils.getDefaultZBuffer(); //default, may put in a factory
		else
			try {
				zBuffer = this.zBuffer.getClass().newInstance();
			} catch (Exception e) {
				return;
			}
		
		zBuffer.setDispHeight(this.getHeight());
		zBuffer.setDispWidth(this.getWidth());
		this.lightSources.removeIf(l -> l.isDeleted());
		
		//this.shapes.removeIf(s -> s.isDeleted());
		Set<CanvasObject> temp = new HashSet<CanvasObject>(this.getShapes());
		temp.stream().filter(s -> s.isDeleted()).forEach(s -> this.shapes.remove(s));
		
		this.camera.doTransforms();
		
		Set<CanvasObject> processShapes = this.getShapes();
		final IZBuffer fZBuffer = zBuffer;
		processShapes.parallelStream().forEach(s -> {
			this.processShape(s, fZBuffer, getShader(s));
		});
		this.zBuffer = zBuffer;
		this.repaint(); this.slaves.forEach(s -> s.update(this));
			
		processShapes.parallelStream().forEach(s -> {
			s.onDrawComplete();
		});
	}
	
	private void processShape(CanvasObject obj, IZBuffer zBuf, IShader shader)
	{
		obj.applyTransforms();
		if (obj.isVisible())
		{
			this.camera.getView(obj);
			if (shader != null) shader.setLightsources(lightSources);
			Stream<Facet> facetStream = obj.getFacetList().stream();
			if (!obj.isProcessBackfaces()){
				facetStream = facetStream.filter(GeneralPredicates.isFrontface(this.camera));
			}
			facetStream.filter(GeneralPredicates.isOverHorizon(this.camera, this.horizon).negate()).forEach(f ->{
				f.setFrontFace(GeneralPredicates.isFrontface(this.camera).test(f));
				zBuf.Add(f, obj, shader);
			});
		}
		for (CanvasObject child : obj.getChildren())
		{
			this.processShape(child, zBuf, shader);
		};
	}
	
	@Override
	public void paint(Graphics g)
	{
		super.paint(g);

		if (this.zBuffer == null) return;
		
		this.zBuffer.getBuffer().keySet().stream().forEach(x -> {
			Map<Integer, ZBufferItem> item = this.zBuffer.getBuffer().get(x);
			if (item != null){
			for (Entry<Integer, ZBufferItem> entry : item.entrySet())
			{
				g.setColor(entry.getValue().getColour());
				g.drawLine(x,entry.getKey(), x,entry.getKey());
			}
			}
		});
		/*this.zBuffer.getBuffer().stream().forEach(item -> {
			if (item != null){
				g.setColor(item.getColour());
				g.drawLine(item.getX(), item.getY(), item.getX(), item.getY());
			}
		});*/
		
		
		for(BiConsumer<Canvas3D,Graphics> op : drawPlugins){
			op.accept(this,g);
		}
		
	}

}
