package com.graphics.tests;

import java.awt.Color;

import com.graphics.lib.Facet;
import com.graphics.lib.transform.Rotation;
import com.graphics.lib.transform.Translation;
import com.graphics.lib.transform.XRotation;
import com.graphics.shapes.Cylinder;

public class Laser extends Cylinder {

	private int tickLife = 8;
	private int currentTick = 0;
	private Cylinder subCylinder;
	
	public Laser() {
		super(2, 1000, 36);
		this.setColour(new Color(255, 0, 255, 100));
		this.addFlag("PHASED");
		this.setCastsShadow(false);
		this.setBaseIntensity(1);
		this.setProcessBackfaces(true);		
		
		subCylinder = new Cylinder(1,1000,36);
		
		subCylinder.addFlag("PHASED");
		subCylinder.setCastsShadow(false);
		subCylinder.setBaseIntensity(1);
		
		for (Facet f : subCylinder.getFacetList()){
			f.setColour(new Color(255,255,0, 200));
		}
		
		this.getVertexList().addAll(subCylinder.getVertexList());
		this.getFacetList().addAll(subCylinder.getFacetList());
		
		//rotate from upright to forwards
		this.addTransform(new Rotation<XRotation>(XRotation.class, 90));
		this.addTransform(new Translation(0,0,1));
		this.applyTransforms();
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

}
