package com.graphics.lib.camera;

import java.util.HashMap;
import java.util.Map;
import java.util.Observable;
import java.util.Observer;
import java.util.function.BiConsumer;

import com.graphics.lib.Point;
import com.graphics.lib.Vector;
import com.graphics.lib.canvas.CanvasObject;
import com.graphics.lib.canvas.OrientableCanvasObject;
import com.graphics.lib.interfaces.IOrientable;
import com.graphics.lib.interfaces.IOrientation;
import com.graphics.lib.orientation.OrientationTransform;
import com.graphics.lib.transform.*;

/**
 * Abstract camera providing base functionality of a camera, such as setting its position and moving it around.
 * Extending classes must implement getViewSpecific() to generate what is seen on the screen
 * 
 * @author Paul Brandon
 *
 */
public abstract class Camera extends Observable implements IOrientable, Observer {
	public static final String CAMERA_MOVED = "cameraMoved";
	private Point position = new Point(0,0,0);
	private CanvasObject tiedTo = null;
	private BiConsumer<CanvasObject, Camera> tiedObjectLocator;
	private IOrientation orientation;
	private boolean tiedObjectUpdated = false;
	private Map<String, CameraTransform> transforms = new HashMap<String, CameraTransform>();
	private OrientationTransform ot = new OrientationTransform();

	protected double dispwidth = 0;
	protected double dispheight = 0;
	
	public Camera(IOrientation orientation){
		this.setOrientation(orientation);
	}
	
	public CameraTransform getTransform(String key) {
		return this.transforms.get(key);
	}
	
	public CanvasObject getTiedTo() {
		return tiedTo;
	}

	/**
	 * Tie this camera to a canvas object, as that object moves then so will the camera
	 * 
	 * @param tiedTo				Canvas Object the camera is tied to
	 * @param tiedObjectLocator		Anonymous function defining the camera's position in relation to the tied object
	 */
	public void setTiedTo(CanvasObject tiedTo, BiConsumer<CanvasObject, Camera> tiedObjectLocator) {
		this.tiedTo = tiedTo;
		this.tiedObjectLocator = tiedObjectLocator;
		tiedObjectLocator.accept(tiedTo, this);
		OrientableCanvasObject<?> o = tiedTo.getObjectAs(OrientableCanvasObject.class);
		tiedTo.addObserver(this);
		if (o != null){
			o.setOrientation(orientation);
		}
	}

	/**
	 * Gets the angle between the forward vector of the camera and a horizontal plane
	 * 
	 * @return Angle in degrees
	 */
	public double getPitch(){
		return Math.toDegrees(Math.asin(this.orientation.getForward().y));
	}
	
	/**
	 * Gets the angle between Vector 0,0,1 and the forward vector in the horizontal plane
	 * 
	 * @return Angle in degrees
	 */
	public double getBearing(){
		Vector forward = this.orientation.getForward();
		double angle = Math.toDegrees(Math.asin(forward.x));
		if (forward.z < 0 && angle < 0) return -180 - angle;
		if (forward.z < 0) return 180 - angle;
		return angle;
	}

	@Override
	public IOrientation getOrientation() {
		return this.orientation;
	}

	@Override
	public void setOrientation(IOrientation orientation) {
		this.orientation = orientation;
		ot.saveCurrentTransforms(orientation);
	}
	
	public Point getPosition() {
		return this.position;
	}
	public void setPosition(Point position) {
		this.position = position;
	}
	
	public void setViewport(double width, double height)
	{
		this.dispheight = height;
		this.dispwidth = width;
	}
	
	public synchronized void addTransform(String key, CameraTransform transform)
	{
		this.transforms.put(key, transform);
	}
	
	public synchronized void removeTransform(String key)
	{
		this.transforms.remove(key);
	}
	
	/**
	 * Apply transformations to this camera
	 */
	public synchronized void doTransforms()
	{
		if (this.tiedObjectUpdated){
			tiedObjectLocator.accept(tiedTo, this);
			this.tiedObjectUpdated = false;
		}
		else if (transforms.size() == 0) return;
		
		for (CameraTransform t : this.transforms.values())
		{
			t.doTransform(this);
		}

		ot.saveCurrentTransforms(orientation);
		
		this.setChanged();
		this.notifyObservers(CAMERA_MOVED);
	}
	
	public void alignShapeToCamera(CanvasObject obj)
	{
		obj.applyCameraTransform(new Translation(-position.x, -position.y, -position.z));
		this.matchCameraRotation(obj);
		obj.applyCameraTransform(new Translation(position.x, position.y, position.z));
	}
	
	public void matchCameraRotation(CanvasObject obj)
	{
		obj.applyCameraTransform(new Rotation<YRotation>(YRotation.class, -ot.getyRot()));
		obj.applyCameraTransform(new Rotation<XRotation>(XRotation.class, -ot.getxRot()));
		obj.applyCameraTransform(new Rotation<ZRotation>(ZRotation.class, -ot.getzRot()));
	}
	
	public void addCameraRotation(CanvasObject obj)
	{
		ot.addRotation(obj);
	}
	
	public void removeCameraRotation(CanvasObject obj)
	{	
		ot.removeRotation(obj);
	}
	
	public final void getView(CanvasObject obj){
		this.getViewSpecific(obj);
	}
	
	@Override
	public void update(Observable arg0, Object arg1) {
		if (arg0 == tiedTo){
			this.tiedObjectUpdated = true;
		}
		
	}
	
	public abstract void getViewSpecific(CanvasObject obj);
	
}
