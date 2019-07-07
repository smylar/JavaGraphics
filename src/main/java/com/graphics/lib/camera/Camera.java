package com.graphics.lib.camera;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.BiConsumer;

import com.graphics.lib.Axis;
import com.graphics.lib.Point;
import com.graphics.lib.Vector;
import com.graphics.lib.interfaces.ICanvasObject;
import com.graphics.lib.interfaces.IOrientable;
import com.graphics.lib.interfaces.IOrientableCamera;
import com.graphics.lib.interfaces.IOrientation;
import com.graphics.lib.orientation.OrientationData;
import com.graphics.lib.traits.TraitHandler;
import com.graphics.lib.transform.CameraTransform;
import com.graphics.lib.transform.ReapplyOrientationTransform;
import com.graphics.lib.transform.Translation;

/**
 * Abstract camera providing base functionality of a camera, such as setting its position and moving it around.
 * Extending classes must implement getViewSpecific() to generate what is seen on the screen
 * 
 * @author Paul Brandon
 *
 */
public abstract class Camera implements IOrientableCamera {
	public static final String CAMERA_MOVED = "cameraMoved";
	private Point position = new Point(0,0,0);
	private Optional<ICanvasObject> tiedTo = Optional.empty();
	private BiConsumer<ICanvasObject, Camera> tiedObjectLocator;
	private IOrientation orientation;
	private boolean tiedObjectUpdated = false;
	private Map<String, CameraTransform> transforms = new HashMap<>();
	private OrientationData ot = new OrientationData();

	protected double dispwidth = 0;
	protected double dispheight = 0;
	
	public Camera(IOrientation orientation){
		this.setOrientation(orientation);
	}
	
	public CameraTransform getCameraTransform(String key) {
		return this.transforms.get(key);
	}
	
	public Optional<ICanvasObject> getTiedTo() {
		return tiedTo;
	}

	/**
	 * Tie this camera to a canvas object, as that object moves then so will the camera
	 * 
	 * @param tiedTo				Canvas Object the camera is tied to
	 * @param tiedObjectLocator		Anonymous function defining the camera's position in relation to the tied object
	 */
	public void setTiedTo(final ICanvasObject tiedTo, BiConsumer<ICanvasObject, Camera> tiedObjectLocator) {
		this.tiedTo = Optional.of(tiedTo);
		this.tiedObjectLocator = tiedObjectLocator;
		tiedObjectLocator.accept(tiedTo, this);
		tiedTo.observeStatus()
              .takeUntil(s -> !this.tiedTo.isPresent() || this.tiedTo.get() != tiedTo)
              .subscribe(s -> tiedObjectUpdated = true);
		
		TraitHandler.INSTANCE.getTrait(tiedTo, IOrientable.class).ifPresent(o -> o.setOrientation(orientation));
	}

	/**
	 * Gets the angle between the forward vector of the camera and a horizontal plane
	 * 
	 * @return Angle in degrees
	 */
	public double getPitch(){
		return Math.toDegrees(Math.asin(this.orientation.getForward().getY()));
	}
	
	/**
	 * Gets the angle between Vector 0,0,1 and the forward vector in the horizontal plane
	 * 
	 * @return Angle in degrees
	 */
	public double getBearing(){
		Vector forward = this.orientation.getForward();
		double angle = Math.toDegrees(Math.asin(forward.getX()));
		if (forward.getZ() < 0 && angle < 0) 
		    return -180 - angle;
		if (forward.getZ() < 0) 
		    return 180 - angle;
		
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
		return position;
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
		transforms.put(key, transform);
	}
	
	public synchronized void removeTransform(String key)
	{
		transforms.remove(key);
	}
	
	/**
	 * Apply transformations to this camera
	 */
	public synchronized void doTransforms()
	{
		if (tiedObjectUpdated && tiedTo.isPresent()) {
			tiedObjectLocator.accept(tiedTo.get(), this);
			tiedObjectUpdated = false;
		}
		
		transforms.values().forEach(t -> t.doTransform(this));

		ot.saveCurrentTransforms(orientation);
	}
	
	public void alignShapeToCamera(ICanvasObject obj)
	{
		obj.applyCameraTransform(new Translation(-position.x, -position.y, -position.z), this);
		matchCameraRotation(obj);
		obj.applyCameraTransform(new Translation(position.x, position.y, position.z), this);
	}
	
	public void matchCameraRotation(ICanvasObject obj)
	{
		obj.applyCameraTransform(Axis.Y.getRotation(-ot.getyRot()), this);
		obj.applyCameraTransform(Axis.X.getRotation(-ot.getxRot()), this);
		obj.applyCameraTransform(Axis.Z.getRotation(-ot.getzRot()), this);
	}
	
	public void addCameraRotation(ICanvasObject obj)
	{
		obj.applyTransform(new ReapplyOrientationTransform(ot));
	}
	
	public final void getView(ICanvasObject obj) {
		getViewSpecific(obj);
	}
	
	
	/**
	 * Must be implemented to provide the screen coordinates for the scene, i.e. Set the transformed coordinate for the vertices in the given object
	 * 
	 * @param obj - Canvas Object to generate coordinates for
	 */
	public abstract void getViewSpecific(ICanvasObject obj);
	
}
