package com.graphics.tests.weapons;

import java.awt.Color;

import com.graphics.lib.Vector;
import com.graphics.lib.canvas.CanvasObject;
import com.graphics.lib.plugins.Events;
import com.graphics.lib.plugins.PluginLibrary;
import com.graphics.lib.traits.PlugableTrait;
import com.graphics.lib.transform.MovementTransform;
import com.graphics.shapes.Sphere;
import com.graphics.tests.TestUtils;

public class GattlingRound extends Projectile {

	@Override
	public CanvasObject get(Vector initialVector, double parentSpeed) {
		Sphere proj = new Sphere(4, 45);
		proj.setBaseIntensity(1);
		proj.setColour(new Color(255,165,0));
		proj.setCastsShadow(false);
		proj.addTrait(new PlugableTrait()).registerPlugin(Events.STOP, PluginLibrary.delete(), false)
										  .registerPlugin(Events.CHECK_COLLISION, PluginLibrary.hasCollidedNew(TestUtils.getFilteredObjectList(), Events.STOP, null), true);
		
		MovementTransform move = new MovementTransform(initialVector, this.getSpeed() + parentSpeed);
		
		move.moveUntil(t -> t.getDistanceMoved() >= this.getRange());
		proj.addTransform(move);
		
		proj.deleteAfterTransforms();
		proj.setProcessBackfaces(false);
		
		return proj;
	}

}
