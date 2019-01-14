package com.graphics.tests.weapons;

import java.util.Optional;

import com.graphics.lib.Point;
import com.graphics.lib.canvas.CanvasObject;
import com.graphics.lib.interfaces.IOrientation;
import com.sound.ClipLibrary;

public abstract class Projectile {
	
	private double speed = 15;
	private int range = 1000;
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
	
	public Optional<ClipLibrary> getClipLibrary() {
		return Optional.ofNullable(clipLibrary);
	}

	public Projectile setClipLibary(ClipLibrary clipLibrary) {
		this.clipLibrary = clipLibrary;
		return this;
	}

	public abstract CanvasObject get(IOrientation orientation, Point startPoint, double parentSpeed);
}
