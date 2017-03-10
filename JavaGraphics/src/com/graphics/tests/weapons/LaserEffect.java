package com.graphics.tests.weapons;

import java.awt.Color;

import com.graphics.lib.Axis;
import com.graphics.lib.Facet;
import com.graphics.lib.Vector;
import com.graphics.lib.WorldCoord;
import com.graphics.shapes.Cylinder;

public class LaserEffect extends Cylinder {

	private int tickLife = 15;
	private int currentTick = 0;
	private double length = 1000;
	private double curLength = 1000;
	private Cylinder subCylinder;
	private WorldCoord startPoint;
	private WorldCoord endPoint;
	
	public LaserEffect(double length) {
		super(2, length , 36);
		this.length = length;
		this.curLength = length;
		this.setColour(new Color(255, 0, 255, 100));
		this.setCastsShadow(false);
		this.setBaseIntensity(1);
		this.setProcessBackfaces(true);		
		
		subCylinder = new Cylinder(1,length,36);
		
		for (Facet f : subCylinder.getFacetList()){
			f.setColour(new Color(255,255,0, 200));
		}
		
		this.getVertexList().addAll(subCylinder.getVertexList());
		
		this.getFacetList().addAll(subCylinder.getFacetList());
		
		//rotate from upright to forwards
		this.addTransform(Axis.X.getRotation(90));
		//seems to be a slight issue facet edges forming a line straight down the middle of the screen
		//it sometimes draws an extended line to the top of the screen (not sure why yet, haven't noticed on other objects)
		//this Z rotation mitigates that, so facet edges aren't dead centre
		this.addTransform(Axis.Z.getRotation(5)); 
		this.applyTransforms();
		
		startPoint = new WorldCoord(0,0,0);
		startPoint.addTag("laserStartMarker");
		endPoint = new WorldCoord(0,0,length);
		endPoint.addTag("laserEndMarker");
		this.getVertexList().add(startPoint);
		this.getVertexList().add(endPoint);
		
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
		if (++currentTick >= tickLife){
			this.setDeleted(true);
		}else{
			super.onDrawComplete();
		}
		
		for (Facet f : subCylinder.getFacetList()){
			f.setColour(new Color(255,255 - (255/tickLife * currentTick),0, 200));
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
			wc.x = wcprev.x + (v.x * length);
			wc.y = wcprev.y + (v.y * length);
			wc.z = wcprev.z + (v.z * length);
		}
	}

	public void resetLength() {
		if (curLength != this.length) setCurLength(this.length);
		
	}
	
}
