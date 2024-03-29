package com.graphics.tests.weapons;

import java.awt.Color;

import com.graphics.lib.Point;
import com.graphics.lib.canvas.CanvasObject;
import com.graphics.lib.interfaces.IOrientation;
import com.graphics.lib.plugins.Events;
import com.graphics.lib.plugins.ExplosionSettings;
import com.graphics.lib.plugins.PluginLibrary;
import com.graphics.lib.traits.OrientableTrait;
import com.graphics.lib.traits.PlugableTrait;
import com.graphics.lib.traits.TraitHandler;
import com.graphics.lib.transform.MovementTransform;
import com.graphics.shapes.Sphere;
import com.graphics.tests.TestUtils;

public class TrackingProjectile extends TargetedProjectile {

	@Override
	public CanvasObject get(IOrientation orientation, Point startPoint, double parentSpeed) {
		if (this.getTargetFinder() == null || this.getTargetFinder().find() == null) 
		    return null;
		
		Sphere proj = new Sphere(18,20);
		
		proj.setBaseIntensity(1);
		proj.setColour(new Color(255, 255, 0, 80));
		proj.setCastsShadow(false);
		proj.deleteAfterTransforms();
		proj.setProcessBackfaces(true);

		MovementTransform move = new MovementTransform(orientation::getForward, this.getSpeed());
		move.moveUntil(t -> t.getDistanceMoved() > this.getRange());
		proj.addTransform(move);

		TraitHandler.INSTANCE.registerTrait(proj, PlugableTrait.class)
		  .registerPlugin(Events.CHECK_COLLISION, PluginLibrary.hasCollided(TestUtils.getFilteredObjectList(), Events.EXPLODE, Events.EXPLODE, true), true)
		  .registerPlugin(Events.EXPLODE, TestUtils.getExplodePlugin(this.getClipLibrary(), ExplosionSettings.getDefault()), false)
		  .registerPlugin("Track", PluginLibrary.track(this.getTargetFinder().find(), 2), true);

		TraitHandler.INSTANCE.registerTrait(proj, OrientableTrait.class).setOrientation(orientation);
		return proj;
	}

}
