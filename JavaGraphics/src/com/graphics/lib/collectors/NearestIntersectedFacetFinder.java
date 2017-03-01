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
import com.graphics.lib.interfaces.ICanvasObject;

public class NearestIntersectedFacetFinder<T extends ICanvasObject> implements Collector<T, Map<Double, IntersectionData<T>>, Entry<Double, IntersectionData<T>>>{

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
	public BiConsumer<Map<Double, IntersectionData<T>>, T> accumulator() {
		return (acc, elem) -> {
			Map<Facet,Point> mfp = elem.getFunctions().getIntersectedFacets(elem, p, v);
			for (Entry<Facet,Point> entry : mfp.entrySet())
			{
				double dist = p.distanceTo(entry.getValue());
				if (dist < maxDist)
				{
					acc.put(dist, new IntersectionData<T>(elem, entry.getKey(), entry.getValue()));
				}
			}
			
		};
	}

	@Override
	public Set<java.util.stream.Collector.Characteristics> characteristics() {
		return Collections.emptySet();
	}

	@Override
	public BinaryOperator<Map<Double, IntersectionData<T>>> combiner() {
		return (acc1, acc2) -> {
		    throw new UnsupportedOperationException(); //fine if we do not use a parallel stream
		  };
	}

	@Override
	public Function<Map<Double, IntersectionData<T>>, Entry<Double, IntersectionData<T>>> finisher() {
		return (acc) -> {
			TreeMap<Double, IntersectionData<T>> tm = (TreeMap<Double, IntersectionData<T>>)acc;
			if (acc.size() > 0) return tm.firstEntry();
			return null;
		};
	}

	@Override
	public Supplier<Map<Double, IntersectionData<T>>> supplier() {
		return TreeMap::new;
	}

}
