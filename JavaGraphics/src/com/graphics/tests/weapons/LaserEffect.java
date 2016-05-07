package com.graphics.tests.weapons;

import java.awt.Color;

import com.graphics.lib.Facet;
import com.graphics.lib.transform.Rotation;
import com.graphics.lib.transform.XRotation;
import com.graphics.shapes.Cylinder;

public class LaserEffect extends Cylinder {

	private int tickLife = 15;
	private int currentTick = 0;
	private double length = 1000;
	private Cylinder subCylinder;
	
	public LaserEffect(double length) {
		super(2, length , 36);
		this.length = length;
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
		this.addTransform(new Rotation<XRotation>(XRotation.class, 90));
		this.applyTransforms();
		this.setAnchorPoint(this.getVertexList().get(0));
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
		return length;
	}

	
}
