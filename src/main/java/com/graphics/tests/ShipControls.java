package com.graphics.tests;

import java.util.function.Consumer;
import java.util.List;
import java.util.Optional;

import com.graphics.lib.Axis;
import com.graphics.lib.canvas.CanvasObjectFunctions;
import com.graphics.lib.control.ObjectInputController;
import com.graphics.lib.interfaces.IEffector;
import com.graphics.lib.interfaces.IOrientable;
import com.graphics.lib.traits.TraitHandler;
import com.graphics.lib.transform.MovementTransform;
import com.graphics.lib.transform.RepeatingTransform;
import com.graphics.lib.transform.Rotation;
import com.graphics.tests.shapes.Ship;

/**
 * Methods for moving a ship around in the scene
 * 
 * @author paul.brandon
 *
 */
public final class ShipControls extends ObjectInputController<Ship> {

	private final IOrientable orientable;
	
	public ShipControls(Ship controlledObject) throws Exception {
		super(controlledObject);
		orientable = TraitHandler.INSTANCE.getTrait(controlledObject, IOrientable.class).orElseThrow();
	}

	public void increaseSpeed() {
		getMovement(FORWARD).setAcceleration(this.controlledObject.getAcceleration());
	}
	
	public void decreaseSpeed() {
		getMovement(FORWARD).setAcceleration(-this.controlledObject.getAcceleration());
	}
	
	public void stopAccelerating() {
		getMovement(FORWARD).setAcceleration(0);
	}
	
	public void panRight() {
		addRotation(new Rotation(Axis.Y, this.controlledObject.getPanRate()), PAN_RIGHT);
	}
	
	public void panLeft() {
		addRotation(new Rotation(Axis.Y, -this.controlledObject.getPanRate()), PAN_LEFT);
	}
	
	public void panDown() {
		addRotation(new Rotation(Axis.X, -this.controlledObject.getPanRate()), PAN_DOWN);
	}
	
	public void panUp() {
		addRotation(new Rotation(Axis.X, this.controlledObject.getPanRate()), PAN_UP);
	}
	
	public void rollRight() {
		addRotation(new Rotation(Axis.Z, 4), ROLL_RIGHT);
	}
	
	public void rollLeft() {
		addRotation(new Rotation(Axis.Z, -4), ROLL_LEFT);
	}
	
	public void moveUp() {
	    addTranslation(new MovementTransform(() -> orientable.getOrientation().getUp(), 4), UP);
	}
	
	public void moveRight() {
	    addTranslation(new MovementTransform(() -> orientable.getOrientation().getRight(), 4), RIGHT);
	}
	
	public void moveDown() {
	    addTranslation(new MovementTransform(() -> orientable.getOrientation().getDown(), 4), DOWN);
	}
	
	public void moveLeft() {
	    addTranslation(new MovementTransform(() -> orientable.getOrientation().getLeft(), 4), LEFT);
	}
	
	public void stopPanDown() {
		this.controlledObject.cancelNamedTransform(PAN_DOWN);
	}
	
	public void stopPanUp() {
		this.controlledObject.cancelNamedTransform(PAN_UP);
	}
	
	public void stopPanRight() {
		this.controlledObject.cancelNamedTransform(PAN_RIGHT);
	}
	
	public void stopPanLeft() {
		this.controlledObject.cancelNamedTransform(PAN_LEFT);
	}
	
	public void stopRollRight() {
		this.controlledObject.cancelNamedTransform(ROLL_RIGHT);
	}
	
	public void stopRollLeft() {
		this.controlledObject.cancelNamedTransform(ROLL_LEFT);
	}
	
	public void stopMoveUp() {
		this.controlledObject.cancelNamedTransform(UP);
	}
	
	public void stopMoveRight() {
		this.controlledObject.cancelNamedTransform(RIGHT);
	}
	public void stopMoveDown() {
		this.controlledObject.cancelNamedTransform(DOWN);
	}
	
	public void stopMoveLeft() {
		this.controlledObject.cancelNamedTransform(LEFT);
	}
	
	public void stopAll() {
		this.controlledObject.cancelTransforms();
	}
	
	public void fireWeapon(List<String> params) {
	    weaponAction(Optional.ofNullable(params), IEffector::activate);
	}
	
	public void deactivateWeapon(List<String> params) {
	    weaponAction(Optional.ofNullable(params), IEffector::deActivate);
	}
	
	private void weaponAction(Optional<List<String>> params, Consumer<IEffector> action) {
	    params.ifPresent(p -> 
	              p.stream().map(controlledObject::getWeapon)
	                        .flatMap(Optional::stream)
	                        .forEach(action::accept)
	    );
	}
	
	private MovementTransform getMovement(String tag) {
		return this.controlledObject.getTransform(tag, MovementTransform.class).orElseGet(() -> {
			MovementTransform transform = new MovementTransform(() -> orientable.getOrientation().getForward(), 0d);
			transform.setName(tag);
			this.controlledObject.addTransform(transform);
			return transform;
		});
	}
	
	private void addRotation(Rotation transform, String name) {
	    if (!this.controlledObject.hasNamedTransform(name)) {
	        controlledObject.addTransform(orientable.toBaseOrientationTransform().setName(name));
	        CanvasObjectFunctions.DEFAULT.get().addTransformAboutCentre(controlledObject, new RepeatingTransform<>(transform, -1).setName(name));
	        controlledObject.addTransform(orientable.reapplyOrientationTransform().setName(name));
	    }
	}
	
	private void addTranslation(MovementTransform transform, String name) {
	    if (!this.controlledObject.hasNamedTransform(name)) {
	        transform.setName(name);
            this.controlledObject.addTransform(transform);
	    }
	}
}
