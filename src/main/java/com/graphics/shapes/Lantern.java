package com.graphics.shapes;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.Flow.Subscriber;
import java.util.concurrent.Flow.Subscription;

import org.apache.commons.lang3.tuple.Pair;

import com.google.common.collect.ImmutableList;
import com.graphics.lib.Facet;
import com.graphics.lib.WorldCoord;
import com.graphics.lib.canvas.CanvasObject;
import com.graphics.lib.lightsource.LightSource;
import com.graphics.lib.lightsource.ObjectTiedLightSource;
import com.graphics.lib.util.ShapeReader;

public class Lantern extends CanvasObject implements Subscriber<String> {
    private static final String RESOURCE = "lantern";
    private LightSource tied;
    private Subscription subscription;
    
	public Lantern() {
	    super(Lantern::init);    
	}
	
	public void attachLightsource(ObjectTiedLightSource<? extends LightSource> tied) {
	    if (Objects.nonNull(subscription)) {
	        subscription.cancel();
	    }
	    this.setColour(tied.getLightSource().getColour());
        this.setCastsShadow(false);
        tied.tieTo(this);
        tied.getLightSource().subscribe(this);
        this.tied = tied.getLightSource();
        
    }
	
	private static Pair<ImmutableList<WorldCoord>, ImmutableList<Facet>> init() {
        ImmutableList<WorldCoord> vertexList = generateVertexList();
        return Pair.of(vertexList, generateFacetList(vertexList));
    }
	
	private static ImmutableList<WorldCoord> generateVertexList()
    {
//        return ImmutableList.of(new WorldCoord(0, -10, 0), //0
//                                new WorldCoord(5, 0, 5),//1
//                                new WorldCoord(5, 0, -5), //2
//                                new WorldCoord(-5, 0, -5), //3
//                                new WorldCoord(-5, 0, 5), //4
//                                new WorldCoord(0, 10, 0) //5
//                                );
	    return ShapeReader.getWorldCoordsFromResource(RESOURCE);
    }
    
    private static ImmutableList<Facet> generateFacetList(List<WorldCoord> vertexList)
    {
//        return ImmutableList.of(new Facet(vertexList.get(0), vertexList.get(3), vertexList.get(2)),
//                                new Facet(vertexList.get(5), vertexList.get(2), vertexList.get(3)),
//                                new Facet(vertexList.get(0), vertexList.get(2), vertexList.get(1)),
//                                new Facet(vertexList.get(5), vertexList.get(1), vertexList.get(2)),
//                                new Facet(vertexList.get(0), vertexList.get(1), vertexList.get(4)),
//                                new Facet(vertexList.get(5), vertexList.get(4), vertexList.get(1)),
//                                new Facet(vertexList.get(0), vertexList.get(4), vertexList.get(3)),
//                                new Facet(vertexList.get(5), vertexList.get(3), vertexList.get(4))
//                                );
        return ShapeReader.getFacetsFromResource(RESOURCE, vertexList);
    }

    @Override
    public void onSubscribe(Subscription subscription) {
        this.subscription = subscription;
        subscription.request(1);
    }

    @Override
    public void onNext(String item) {
        setColour(tied.getActualColour());
        subscription.request(1);
    }

    @Override
    public void onError(Throwable throwable) {
        throwable.printStackTrace();
    }

    @Override
    public void onComplete() {
        subscription = null;
    }
}
