package com.graphics.shapes;

import java.util.List;
import java.util.Observable;
import java.util.Observer;

import org.apache.commons.lang3.tuple.Pair;

import com.google.common.collect.ImmutableList;
import com.graphics.lib.Facet;
import com.graphics.lib.WorldCoord;
import com.graphics.lib.canvas.CanvasObject;
import com.graphics.lib.lightsource.LightSource;
import com.graphics.lib.lightsource.ObjectTiedLightSource;

public class Lantern extends CanvasObject implements Observer {
    
	public Lantern() {
	    super(Lantern::init);
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
	
	private static Pair<ImmutableList<WorldCoord>, ImmutableList<Facet>> init() {
        ImmutableList<WorldCoord> vertexList = generateVertexList();
        return Pair.of(vertexList, generateFacetList(vertexList));
    }
	
	private static ImmutableList<WorldCoord> generateVertexList()
    {
        return ImmutableList.of(new WorldCoord(0, -10, 0), //0
                                new WorldCoord(5, 0, 5),//1
                                new WorldCoord(5, 0, -5), //2
                                new WorldCoord(-5, 0, -5), //3
                                new WorldCoord(-5, 0, 5), //4
                                new WorldCoord(0, 10, 0) //5
                                );
    }
    
    private static ImmutableList<Facet> generateFacetList(List<WorldCoord> vertexList)
    {
        return ImmutableList.of(new Facet(vertexList.get(0), vertexList.get(3), vertexList.get(2)),
                                new Facet(vertexList.get(5), vertexList.get(2), vertexList.get(3)),
                                new Facet(vertexList.get(0), vertexList.get(2), vertexList.get(1)),
                                new Facet(vertexList.get(5), vertexList.get(1), vertexList.get(2)),
                                new Facet(vertexList.get(0), vertexList.get(1), vertexList.get(4)),
                                new Facet(vertexList.get(5), vertexList.get(4), vertexList.get(1)),
                                new Facet(vertexList.get(0), vertexList.get(4), vertexList.get(3)),
                                new Facet(vertexList.get(5), vertexList.get(3), vertexList.get(4))
                                );
    }
}
