package com.graphics.tests.weapons;

import com.graphics.lib.Vector;
import com.graphics.lib.canvas.CanvasObject;
import com.graphics.lib.interfaces.IPointFinder;
import com.sound.ClipLibrary;

public abstract class Projectile {
	
	private double speed = 15;
	private int range = 1000;
	private IPointFinder startPoint;
	private ClipLibrary clipLibrary;

	public double getSpeed() {
		return speed;
	}

	public Projectile setSpeed(double speed) {
		this.speed = speed;
		return this;
	}

	public int getRange() {
		return range;
	}

	public Projectile setRange(int range) {
		this.range = range;
		return this;
	}
	
	public IPointFinder getStartPoint() {
		return startPoint;
	}

	public Projectile setStartPoint(IPointFinder startPoint) {
		this.startPoint = startPoint;
		return this;
	}
	
	public ClipLibrary getClipLibrary() {
		return clipLibrary;
	}

	public Projectile setClipLibary(ClipLibrary clipLibrary) {
		this.clipLibrary = clipLibrary;
		return this;
	}

	public abstract CanvasObject get(Vector initialVector, double parentSpeed);
}
