package com.graphics.tests.weapons;

import com.graphics.lib.interfaces.IObjectFinder;

public abstract class TargetedProjectile extends Projectile {
	private IObjectFinder targetFinder;

	public IObjectFinder getTargetFinder() {
		return targetFinder;
	}

	public void setTargetFinder(IObjectFinder targetFinder) {
		this.targetFinder = targetFinder;
	}
	
}
