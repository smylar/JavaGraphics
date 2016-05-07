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

	public void setSpeed(double speed) {
		this.speed = speed;
	}

	public int getRange() {
		return range;
	}

	public void setRange(int range) {
		this.range = range;
	}
	
	public IPointFinder getStartPoint() {
		return startPoint;
	}

	public void setStartPoint(IPointFinder startPoint) {
		this.startPoint = startPoint;
	}
	
	public ClipLibrary getClipLibrary() {
		return clipLibrary;
	}

	public void setClipLibary(ClipLibrary clipLibrary) {
		this.clipLibrary = clipLibrary;
	}

	public abstract CanvasObject get(Vector initialVector, double parentSpeed);
}
