package com.graphics.tests.weapons;

import java.awt.Color;

import com.graphics.lib.Point;
import com.graphics.lib.Vector;
import com.graphics.lib.canvas.CanvasObject;
import com.graphics.lib.canvas.CanvasObjectFunctions;
import com.graphics.lib.interfaces.ICanvasObject;
import com.graphics.lib.interfaces.IOrientation;
import com.graphics.lib.plugins.Events;
import com.graphics.lib.plugins.ExplosionSettings;
import com.graphics.lib.plugins.PluginLibrary;
import com.graphics.lib.traits.PlugableTrait;
import com.graphics.lib.traits.TraitHandler;
import com.graphics.lib.transform.MovementTransform;
import com.graphics.shapes.Sphere;
import com.graphics.tests.TestUtils;

public class DeflectionProjectile extends TargetedProjectile {
	
	@Override
	public CanvasObject get(IOrientation orientation, Point startPoint, double parentSpeed) {
	    Sphere proj = new Sphere(18,20);
		proj.setBaseIntensity(1);
		proj.setColour(new Color(255, 0, 255, 80));
		proj.setCastsShadow(false);
		proj.deleteAfterTransforms();
		proj.setProcessBackfaces(true);

		TraitHandler.INSTANCE.registerTrait(proj, PlugableTrait.class)
		                     .registerPlugin(Events.CHECK_COLLISION, PluginLibrary.hasCollided(TestUtils.getFilteredObjectList(), Events.EXPLODE, Events.EXPLODE, true), true)
		                     .registerPlugin(Events.EXPLODE, TestUtils.getExplodePlugin(this.getClipLibrary(), ExplosionSettings.getDefault()), false);
		
		ICanvasObject target = this.getTargetFinder().find();
		Vector vTrackee = (target == null || startPoint == null) ? orientation.getForward() : CanvasObjectFunctions.DEFAULT.get().plotDeflectionShot(target, startPoint, this.getSpeed()).getLeft();
		
		MovementTransform move = new MovementTransform(vTrackee, 20); 
		move.moveUntil(t -> t.getDistanceMoved() > this.getRange());
		proj.addTransform(move);
		
		return proj;
	}

}
