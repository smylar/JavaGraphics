package com.graphics.tests;

import java.util.List;

import com.graphics.lib.Axis;
import com.graphics.lib.canvas.CanvasObjectFunctions;
import com.graphics.lib.control.ObjectInputController;
import com.graphics.lib.interfaces.IEffector;
import com.graphics.lib.interfaces.IOrientable;
import com.graphics.lib.traits.TraitHandler;
import com.graphics.lib.transform.MovementTransform;
import com.graphics.lib.transform.RepeatingTransform;
import com.graphics.lib.transform.Rotation;
import com.graphics.lib.transform.Transform;
import com.graphics.tests.shapes.Ship;

public final class ShipControls extends ObjectInputController<Ship> {

	private final IOrientable orientable;
	
	public ShipControls(Ship controlledObject) throws Exception {
		super(controlledObject);
		orientable = TraitHandler.INSTANCE.getTrait(controlledObject, IOrientable.class).get();
	}

	public void increaseSpeed(){
		getMovement(FORWARD).setAcceleration(this.controlledObject.getAcceleration());
	}
	
	public void decreaseSpeed(){
		getMovement(FORWARD).setAcceleration(-this.controlledObject.getAcceleration());
	}
	
	public void stopAccelerating(){
		getMovement(FORWARD).setAcceleration(0);
	}
	
	public void panRight()
	{
		if (this.controlledObject.hasNamedTransform(PAN_RIGHT)) return;
		
		addRotation(new Rotation(Axis.Y, this.controlledObject.getPanRate()), PAN_RIGHT);
	}
	
	public void panLeft()
	{
		if (this.controlledObject.hasNamedTransform(PAN_LEFT)) return;
		
		addRotation(new Rotation(Axis.Y, -this.controlledObject.getPanRate()), PAN_LEFT);
	}
	
	public void panDown()
	{
		if (this.controlledObject.hasNamedTransform(PAN_DOWN)) return;
		
		addRotation(new Rotation(Axis.X, -this.controlledObject.getPanRate()), PAN_DOWN);
	}
	
	public void panUp()
	{
		if (this.controlledObject.hasNamedTransform(PAN_UP)) return;
		
		addRotation(new Rotation(Axis.X, this.controlledObject.getPanRate()), PAN_UP);
	}
	
	public void rollRight()
	{
		if (this.controlledObject.hasNamedTransform(ROLL_RIGHT)) return;
		
		addRotation(new Rotation(Axis.Z, 4), ROLL_RIGHT);
	}
	
	public void rollLeft()
	{
		if (this.controlledObject.hasNamedTransform(ROLL_LEFT)) return;
		
		addRotation(new Rotation(Axis.Z, -4), ROLL_LEFT);
	}
	
	public void moveUp()
	{
		if (this.controlledObject.hasNamedTransform(UP)) return;
		Transform move = new MovementTransform(() -> orientable.getOrientation().getUp(), 4);
		move.setName(UP);
		this.controlledObject.addTransform(move);
	}
	
	public void moveRight()
	{
		if (this.controlledObject.hasNamedTransform(RIGHT)) return;
		Transform move = new MovementTransform(() -> orientable.getOrientation().getRight(), 4);
		move.setName(RIGHT);
		this.controlledObject.addTransform(move);
	}
	
	public void moveDown()
	{
		if (this.controlledObject.hasNamedTransform(DOWN)) return;
		Transform move = new MovementTransform(() -> orientable.getOrientation().getDown(), 4);
		move.setName(DOWN);
		this.controlledObject.addTransform(move);
	}
	
	public void moveLeft()
	{
		if (this.controlledObject.hasNamedTransform(LEFT)) return;
		Transform move = new MovementTransform(() -> orientable.getOrientation().getLeft(), 4);
		move.setName(LEFT);
		this.controlledObject.addTransform(move);
	}
	
	public void stopPanDown()
	{
		this.controlledObject.cancelNamedTransform(PAN_DOWN);
	}
	
	public void stopPanUp()
	{
		this.controlledObject.cancelNamedTransform(PAN_UP);
	}
	
	public void stopPanRight()
	{
		this.controlledObject.cancelNamedTransform(PAN_RIGHT);
	}
	
	public void stopPanLeft()
	{
		this.controlledObject.cancelNamedTransform(PAN_LEFT);
	}
	
	public void stopRollRight()
	{
		this.controlledObject.cancelNamedTransform(ROLL_RIGHT);
	}
	
	public void stopRollLeft()
	{
		this.controlledObject.cancelNamedTransform(ROLL_LEFT);
	}
	
	public void stopMoveUp()
	{
		this.controlledObject.cancelNamedTransform(UP);
	}
	
	public void stopMoveRight()
	{
		this.controlledObject.cancelNamedTransform(RIGHT);
	}
	public void stopMoveDown()
	{
		this.controlledObject.cancelNamedTransform(DOWN);
	}
	
	public void stopMoveLeft()
	{
		this.controlledObject.cancelNamedTransform(LEFT);
	}
	
	public void stopAll()
	{
		this.controlledObject.cancelTransforms();
	}
	
	public void fireWeapon(List<String> params){
		if (params == null) return;
		for (String param : params)
		{
			this.controlledObject.getWeapon(param)
			                     .ifPresent(IEffector::activate);
		}
	}
	
	public void deactivateWeapon(List<String> params){
		if (params == null) return;
		for (String param : params)
		{
		    this.controlledObject.getWeapon(param)
                                 .ifPresent(IEffector::deActivate);
		}
	}
	
	private MovementTransform getMovement(String tag){
		return this.controlledObject.getTransform(tag, MovementTransform.class).orElseGet(() -> {
			MovementTransform transform = new MovementTransform(() -> orientable.getOrientation().getForward(), 0d);
			transform.setName(tag);
			this.controlledObject.addTransform(transform);
			return transform;
		});
	}
	
	private void addRotation(Rotation transform, String name) {
		controlledObject.addTransform(orientable.toBaseOrientationTransform().setName(name));
		CanvasObjectFunctions.DEFAULT.get().addTransformAboutCentre(controlledObject, new RepeatingTransform<Rotation>(transform, -1).setName(name));
		controlledObject.addTransform(orientable.reapplyOrientationTransform().setName(name));
	}
}
