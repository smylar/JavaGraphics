package com.graphics.tests.weapons;

import java.awt.Color;

import com.graphics.lib.Point;
import com.graphics.lib.Vector;
import com.graphics.lib.canvas.CanvasObject;
import com.graphics.lib.canvas.CanvasObjectFunctions;
import com.graphics.lib.interfaces.ICanvasObject;
import com.graphics.lib.plugins.Events;
import com.graphics.lib.plugins.PluginLibrary;
import com.graphics.lib.traits.PlugableTrait;
import com.graphics.lib.transform.MovementTransform;
import com.graphics.shapes.Sphere;
import com.graphics.tests.TestUtils;

public class DeflectionProjectile extends TargetedProjectile {
	
	@Override
	public CanvasObject get(Vector initialVector, double parentSpeed) {
	    Sphere proj = new Sphere(18,20);
		proj.setBaseIntensity(1);
		proj.setColour(new Color(255, 0, 255, 80));
		proj.setCastsShadow(false);
		proj.deleteAfterTransforms();
		proj.setProcessBackfaces(true);

		proj.addTrait(new PlugableTrait()).registerPlugin(Events.CHECK_COLLISION, PluginLibrary.hasCollided(TestUtils.getFilteredObjectList(), Events.EXPLODE, Events.EXPLODE), true)
		                                  .registerPlugin(Events.EXPLODE, TestUtils.getExplodePlugin(this.getClipLibrary()), false);
		
		ICanvasObject target = this.getTargetFinder().find();
		Point startPoint = this.getStartPoint().find();
		Vector vTrackee = (target == null || startPoint == null) ? initialVector : CanvasObjectFunctions.DEFAULT.get().plotDeflectionShot(target, startPoint, this.getSpeed()).getLeft();
		
		MovementTransform move = new MovementTransform(vTrackee, 20); 
		move.moveUntil(t -> t.getDistanceMoved() > this.getRange());
		proj.addTransform(move);
		
		return proj;
	}

}
