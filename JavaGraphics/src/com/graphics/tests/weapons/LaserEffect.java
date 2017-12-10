package com.graphics.tests.weapons;

import java.awt.Color;
import java.util.function.Function;

import org.apache.commons.lang3.tuple.Pair;

import com.google.common.collect.ImmutableList;
import com.graphics.lib.Axis;
import com.graphics.lib.Facet;
import com.graphics.lib.Vector;
import com.graphics.lib.WorldCoord;
import com.graphics.lib.canvas.CanvasObject;
import com.graphics.shapes.Cylinder;

public final class LaserEffect extends CanvasObject {

	private int tickLife = 15;
	private int tickLifeStart = 15;
	private double curLength;
	private double length;
	private Cylinder subCylinder;
	private WorldCoord startPoint;
	private WorldCoord endPoint;
	
	public LaserEffect(double length) {
		super(init(length), LaserEffect.class);
		this.length = length;
		this.curLength = length;
		this.setColour(new Color(255, 0, 255, 100));
		this.setCastsShadow(false);
		this.setBaseIntensity(1);
		this.setProcessBackfaces(true);		
		
		for (Facet f : subCylinder.getFacetList()){
			f.setColour(new Color(255,255,0, 200));
		}

		//seems to be a slight issue facet edges forming a line straight down the middle of the screen
		//it sometimes draws an extended line to the top of the screen (not sure why yet, haven't noticed on other objects)
		//this Z rotation mitigates that, so facet edges aren't dead centre

		startPoint.addTag("laserStartMarker");
		endPoint.addTag("laserEndMarker");
		
		this.setAnchorPoint(startPoint);
	}

	public int getTickLife() {
		return tickLife;
	}

	public void setTickLife(int tickLife) {
		this.tickLife = tickLife;
	}
	
	@Override
	public void onDrawComplete(){
		super.onDrawComplete();
		for (Facet f : subCylinder.getFacetList()){
			f.setColour(new Color(255, 255/tickLifeStart * tickLife ,0, 200));
		}
	}

	public double getLength() {
		return this.length;
	}

	public void setCurLength(double length)
	{
		this.curLength = length;
		Vector v = startPoint.vectorToPoint(endPoint).getUnitVector();
		for(int i = 1 ; i < this.getVertexList().size() ; i+=2)
		{
			WorldCoord wc = this.getVertexList().get(i);
			if (wc.hasTag("laserEndMarker")) continue;
			
			WorldCoord wcprev = this.getVertexList().get(i-1);
			wc.x = wcprev.x + (v.getX() * length);
			wc.y = wcprev.y + (v.getY() * length);
			wc.z = wcprev.z + (v.getZ() * length);
		}
	}

	public void resetLength() {
		if (curLength != this.length) setCurLength(this.length);
		
	}

	private static Function<LaserEffect, Pair<ImmutableList<WorldCoord>, ImmutableList<Facet>>> init(final double length) {
		return self -> {
			ImmutableList.Builder<WorldCoord> vertexList = ImmutableList.builder();
			ImmutableList.Builder<Facet> facets = ImmutableList.builder();
			Cylinder main = new Cylinder(2, length , 36);
			vertexList.addAll(main.getVertexList());
			facets.addAll(main.getFacetList());
			
			self.subCylinder = new Cylinder(1,length,36);
			
			vertexList.addAll(self.subCylinder.getVertexList());
			facets.addAll(self.subCylinder.getFacetList());
			
			main.applyTransform(Axis.X.getRotation(90));
			main.applyTransform(Axis.Z.getRotation(5)); 
			self.subCylinder.applyTransform(Axis.X.getRotation(90));
			self.subCylinder.applyTransform(Axis.Z.getRotation(5)); 
			
			self.startPoint = new WorldCoord(0,0,0);
			self.endPoint = new WorldCoord(0,0,length);
			vertexList.add(self.startPoint, self.endPoint);
			
			return Pair.of(vertexList.build(), facets.build());
		};
	}
	
}
