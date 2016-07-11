package com.graphics.lib.collectors;

import java.util.Collections;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;
import java.util.function.BiConsumer;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collector;

import com.graphics.lib.Facet;
import com.graphics.lib.Point;
import com.graphics.lib.Vector;
import com.graphics.lib.canvas.CanvasObject;

public class NearestIntersectedFacetFinder<T extends CanvasObject> implements Collector<T, Map<Double,Facet>, Entry<Double,Facet>>{

	private Vector v;
	private Point p;
	private double maxDist;
	
	public NearestIntersectedFacetFinder(Vector v, Point p, double maxDist)
	{
		this.v = v;
		this.p = p;
		this.maxDist = maxDist;
	}
	
	@Override
	public BiConsumer<Map<Double,Facet>, T> accumulator() {
		return (acc, elem) -> {
			Map<Facet,Point> mfp = elem.getIntersectedFacets(p, v);
			for (Entry<Facet,Point> entry : mfp.entrySet())
			{
				double dist = p.distanceTo(entry.getValue());
				if (dist < maxDist)
				{
					acc.put(dist, entry.getKey());
				}
			}
			
		};
	}

	@Override
	public Set<java.util.stream.Collector.Characteristics> characteristics() {
		return Collections.emptySet();
	}

	@Override
	public BinaryOperator<Map<Double,Facet>> combiner() {
		return (acc1, acc2) -> {
		    throw new UnsupportedOperationException(); //fine if we do not use a parallel stream
		  };
	}

	@Override
	public Function<Map<Double,Facet>, Entry<Double,Facet>> finisher() {
		return (acc) -> {
			TreeMap<Double,Facet> tm = (TreeMap<Double,Facet>)acc;
			if (acc.size() > 0) return tm.firstEntry();
			return null;
		};
	}

	@Override
	public Supplier<Map<Double,Facet>> supplier() {
		return TreeMap::new;
	}

}
