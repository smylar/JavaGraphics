package com.graphics.tests.weapons;

import java.awt.Color;

import com.graphics.lib.Vector;
import com.graphics.lib.canvas.CanvasObject;
import com.graphics.lib.canvas.PlugableCanvasObject;
import com.graphics.lib.plugins.Events;
import com.graphics.lib.plugins.PluginLibrary;
import com.graphics.lib.transform.MovementTransform;
import com.graphics.shapes.Sphere;
import com.graphics.tests.TestUtils;

public class TrackingProjectile extends TargetedProjectile {

	@Override
	public CanvasObject get(Vector initialVector, double parentSpeed) {
		if (this.getTargetFinder() == null || this.getTargetFinder().find() == null) return null;
		
		PlugableCanvasObject proj = new PlugableCanvasObject(new Sphere(18,20));
		proj.setBaseIntensity(1);
		proj.setColour(new Color(255, 255, 0, 80));
		proj.setCastsShadow(false);
		proj.deleteAfterTransforms();
		proj.setProcessBackfaces(true);

		MovementTransform move = new MovementTransform(initialVector, this.getSpeed());
		move.moveUntil(t -> t.getDistanceMoved() > this.getRange());
		proj.addTransform(move);

		proj.registerPlugin(Events.CHECK_COLLISION, PluginLibrary.hasCollided(TestUtils.getFilteredObjectList(), Events.EXPLODE, Events.EXPLODE), true);
		proj.registerPlugin(Events.EXPLODE, TestUtils.getExplodePlugin(this.getClipLibrary()), false);
		proj.registerPlugin("Track", PluginLibrary.track(this.getTargetFinder().find(), 1), true); 
		return proj;
	}

}
