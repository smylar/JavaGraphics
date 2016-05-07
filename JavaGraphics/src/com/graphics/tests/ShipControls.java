package com.graphics.tests;

import com.graphics.lib.KeyConfiguration;
import com.graphics.lib.ObjectInputController;
import com.graphics.lib.canvas.Canvas3D;
import com.graphics.lib.canvas.OrientableCanvasObject;
import com.graphics.lib.transform.MovementTransform;
import com.graphics.lib.transform.RepeatingTransform;
import com.graphics.lib.transform.Rotation;
import com.graphics.lib.transform.Transform;
import com.graphics.lib.transform.XRotation;
import com.graphics.lib.transform.YRotation;
import com.graphics.lib.transform.ZRotation;
import com.graphics.tests.shapes.Ship;
import com.graphics.tests.weapons.BouncyProjectile;
import com.graphics.tests.weapons.DeflectionProjectile;
import com.graphics.tests.weapons.ExplodingProjectile;
import com.graphics.tests.weapons.LaserWeapon;
import com.graphics.tests.weapons.Projectile;
import com.graphics.tests.weapons.ProjectileWeapon;
import com.graphics.tests.weapons.TrackingProjectile;

public class ShipControls extends ObjectInputController<OrientableCanvasObject<Ship>> {

	Ship ship;
	
	public ShipControls(OrientableCanvasObject<Ship> controlledObject, Canvas3D cnv, KeyConfiguration config) throws Exception {
		super(controlledObject, cnv, config);
		ship = controlledObject.getObjectAs(Ship.class);
	}

	public void increaseSpeed(){
		getMovement(FORWARD).setAcceleration(ship.getAcceleration());
	}
	
	public void decreaseSpeed(){
		getMovement(FORWARD).setAcceleration(-ship.getAcceleration());
	}
	
	public void stopAccelerating(){
		getMovement(FORWARD).setAcceleration(0);
	}
	
	public void panRight()
	{
		if (this.controlledObject.hasNamedTransform(PAN_RIGHT)) return;
		
		Rotation<?> r = new Rotation<YRotation>(YRotation.class, ship.getPanRate())
		{
			@Override
			public void beforeTransform(){
				super.beforeTransform();
				controlledObject.toBaseOrientation();
			}
			
			@Override
			public void afterTransform(){
				super.afterTransform();	
				controlledObject.reapplyOrientation();
			}
		};
		Transform rot = new RepeatingTransform<Rotation<?>>(r, -1);
		rot.setName(PAN_RIGHT);
		this.controlledObject.addTransformAboutCentre(rot);
	}
	
	public void panLeft()
	{
		if (this.controlledObject.hasNamedTransform(PAN_LEFT)) return;
		Rotation<?> r = new Rotation<YRotation>(YRotation.class, -ship.getPanRate())
		{
			@Override
			public void beforeTransform(){
				super.beforeTransform();
				controlledObject.toBaseOrientation();
			}
			
			@Override
			public void afterTransform(){
				super.afterTransform();	
				controlledObject.reapplyOrientation();
			}
		};
		Transform rot = new RepeatingTransform<Rotation<?>>(r, -1);
		rot.setName(PAN_LEFT);
		this.controlledObject.addTransformAboutCentre(rot);
	}
	
	public void panDown()
	{
		if (this.controlledObject.hasNamedTransform(PAN_DOWN)) return;
		Rotation<?> r = new Rotation<XRotation>(XRotation.class, -ship.getPanRate())
		{
			@Override
			public void beforeTransform(){
				super.beforeTransform();
				controlledObject.toBaseOrientation();
			}
			
			@Override
			public void afterTransform(){
				super.afterTransform();	
				controlledObject.reapplyOrientation();
			}
		};
		Transform rot = new RepeatingTransform<Rotation<?>>(r, -1);
		rot.setName(PAN_DOWN);
		this.controlledObject.addTransformAboutCentre(rot);
	}
	
	public void panUp()
	{
		if (this.controlledObject.hasNamedTransform(PAN_UP)) return;
		Rotation<?> r = new Rotation<XRotation>(XRotation.class, ship.getPanRate())
		{
			@Override
			public void beforeTransform(){
				super.beforeTransform();
				controlledObject.toBaseOrientation();
			}
			
			@Override
			public void afterTransform(){
				super.afterTransform();	
				controlledObject.reapplyOrientation();
			}
		};
		Transform rot = new RepeatingTransform<Rotation<?>>(r, -1);
		rot.setName(PAN_UP);
		this.controlledObject.addTransformAboutCentre(rot);
	}
	
	public void rollRight()
	{
		if (this.controlledObject.hasNamedTransform(ROLL_RIGHT)) return;
		Rotation<?> r = new Rotation<ZRotation>(ZRotation.class, 4)
		{
			@Override
			public void beforeTransform(){
				super.beforeTransform();
				controlledObject.toBaseOrientation();
			}
			
			@Override
			public void afterTransform(){
				super.afterTransform();	
				controlledObject.reapplyOrientation();
			}
		};
		Transform rot = new RepeatingTransform<Rotation<?>>(r, -1);
		rot.setName(ROLL_RIGHT);
		this.controlledObject.addTransformAboutCentre(rot);
	}
	
	public void rollLeft()
	{
		if (this.controlledObject.hasNamedTransform(ROLL_LEFT)) return;
		Rotation<?> r = new Rotation<ZRotation>(ZRotation.class, -4)
		{
			@Override
			public void beforeTransform(){
				super.beforeTransform();
				controlledObject.toBaseOrientation();
			}
			
			@Override
			public void afterTransform(){
				super.afterTransform();	
				controlledObject.reapplyOrientation();
			}
		};
		Transform rot = new RepeatingTransform<Rotation<?>>(r, -1);
		rot.setName(ROLL_LEFT);
		this.controlledObject.addTransformAboutCentre(rot);
	}
	
	public void moveUp()
	{
		if (this.controlledObject.hasNamedTransform(UP)) return;
		Transform move = new MovementTransform(() -> this.controlledObject.getOrientation().getUp(), 4);
		move.setName(UP);
		this.controlledObject.addTransform(move);
	}
	
	public void moveRight()
	{
		if (this.controlledObject.hasNamedTransform(RIGHT)) return;
		Transform move = new MovementTransform(() -> this.controlledObject.getOrientation().getRight(), 4);
		move.setName(RIGHT);
		this.controlledObject.addTransform(move);
	}
	
	public void moveDown()
	{
		if (this.controlledObject.hasNamedTransform(DOWN)) return;
		Transform move = new MovementTransform(() -> this.controlledObject.getOrientation().getDown(), 4);
		move.setName(DOWN);
		this.controlledObject.addTransform(move);
	}
	
	public void moveLeft()
	{
		if (this.controlledObject.hasNamedTransform(LEFT)) return;
		Transform move = new MovementTransform(() -> this.controlledObject.getOrientation().getLeft(), 4);
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
	
	public void fireLaser()
	{
		ship.getWeapons().forEach(w -> {
			if (w instanceof LaserWeapon){
				w.activate();
			}
		});
	}
	
	public void fireBouncy()
	{
		fire(BouncyProjectile.class);
	}
	
	public void fireDeflection()
	{
		fire(DeflectionProjectile.class);
	}
	
	public void fireTracking()
	{
		fire(TrackingProjectile.class);
	}
	
	public void fireExploding()
	{
		fire(ExplodingProjectile.class);
	}
	
	public void fire(Class<? extends Projectile> cl){
		ship.getWeapons().forEach(w -> {
			if (w instanceof ProjectileWeapon){
				if (((ProjectileWeapon)w).getProjectile().getClass().equals(cl) ){
					w.activate();
				}
			}
		});
	}
	
	private MovementTransform getMovement(String tag){
		Transform transform = this.controlledObject.getTransform(tag);
		if (transform == null || !(transform instanceof MovementTransform)){
			transform = new MovementTransform(() -> this.controlledObject.getOrientation().getForward(), 0d);
			transform.setName(tag);
			this.controlledObject.addTransform(transform);
		}
		return (MovementTransform)transform;
	}
}
