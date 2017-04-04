package com.graphics.shapes;

import java.util.Observable;
import java.util.Observer;

import com.graphics.lib.Facet;
import com.graphics.lib.WorldCoord;
import com.graphics.lib.canvas.CanvasObject;
import com.graphics.lib.lightsource.LightSource;
import com.graphics.lib.lightsource.ObjectTiedLightSource;

public class Lantern extends CanvasObject implements Observer {
    
	public Lantern() {
	    super();
		this.getVertexList().add(new WorldCoord(0, -10, 0)); //0
		this.getVertexList().add(new WorldCoord(5, 0, 5)); //1
		this.getVertexList().add(new WorldCoord(5, 0, -5)); //2
		this.getVertexList().add(new WorldCoord(-5, 0, -5)); //3
		this.getVertexList().add(new WorldCoord(-5, 0, 5)); //4
		this.getVertexList().add(new WorldCoord(0, 10, 0)); //5
		
		this.getFacetList().add(new Facet(this.getVertexList().get(0), this.getVertexList().get(3), this.getVertexList().get(2)));
		this.getFacetList().add(new Facet(this.getVertexList().get(5), this.getVertexList().get(2), this.getVertexList().get(3)));
		
		this.getFacetList().add(new Facet(this.getVertexList().get(0), this.getVertexList().get(2), this.getVertexList().get(1)));
		this.getFacetList().add(new Facet(this.getVertexList().get(5), this.getVertexList().get(1), this.getVertexList().get(2)));
		
		this.getFacetList().add(new Facet(this.getVertexList().get(0), this.getVertexList().get(1), this.getVertexList().get(4)));
		this.getFacetList().add(new Facet(this.getVertexList().get(5), this.getVertexList().get(4), this.getVertexList().get(1)));
		
		this.getFacetList().add(new Facet(this.getVertexList().get(0), this.getVertexList().get(4), this.getVertexList().get(3)));
		this.getFacetList().add(new Facet(this.getVertexList().get(5), this.getVertexList().get(3), this.getVertexList().get(4)));
	}
	
	public void attachLightsource(ObjectTiedLightSource<?> ls){
		this.setColour(ls.getLightSource().getColour());
		this.setCastsShadow(false);
		ls.tieTo(this);
		ls.getLightSource().addObserver(this);
	}
	
	@Override
	public synchronized void update(Observable arg0, Object arg1) {
		if (arg0 instanceof LightSource){
			this.setColour(((LightSource)arg0).getActualColour());
		}
	}
}
