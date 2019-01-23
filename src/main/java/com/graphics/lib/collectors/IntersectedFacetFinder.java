package com.graphics.lib.collectors;

import java.util.Collections;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeSet;
import java.util.function.BiConsumer;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collector;

import com.graphics.lib.Facet;
import com.graphics.lib.Point;
import com.graphics.lib.Vector;
import com.graphics.lib.canvas.FunctionHandler;
import com.graphics.lib.interfaces.ICanvasObject;

/**
 * Get the intersected facets along the given vector from the given point for a given distance
 * <br/>
 * All facets are returned in distance order, this allows the possible processing of penetrating effects
 * where we might want to know about facets behind the the initially effected one.
 * 
 * @author paul.brandon
 *
 * @param <T>
 */
public class IntersectedFacetFinder<T extends ICanvasObject> implements Collector<T, TreeSet<IntersectionData<T>>, TreeSet<IntersectionData<T>>> {

	private Vector v;
	private Point p;
	private double maxDist;
	
	public IntersectedFacetFinder(Vector v, Point p, double maxDist)
	{
		this.v = v;
		this.p = p;
		this.maxDist = maxDist;
	}
	
	@Override
	public BiConsumer<TreeSet<IntersectionData<T>>, T> accumulator() {
		return (acc, elem) -> {
			Map<Facet,Point> mfp = FunctionHandler.getFunctions(elem).getIntersectedFacets(elem, p, v);
			for (Entry<Facet,Point> entry : mfp.entrySet())
			{
				double dist = p.distanceTo(entry.getValue());
				if (dist < maxDist)
				{
					acc.add(new IntersectionData<T>(elem, entry.getKey(), entry.getValue(), dist));
				}
			}
			
		};
	}

	@Override
	public Set<java.util.stream.Collector.Characteristics> characteristics() {
		return Collections.emptySet();
	}

	@Override
	public BinaryOperator<TreeSet<IntersectionData<T>>> combiner() {
		return (acc1, acc2) -> {
		    acc1.addAll(acc2);
		    return acc1;
		  };
	}

	@Override
	public Function<TreeSet<IntersectionData<T>>, TreeSet<IntersectionData<T>>> finisher() {
	    return acc -> acc;
	}

	@Override
	public Supplier<TreeSet<IntersectionData<T>>> supplier() {
	    return () -> new TreeSet<IntersectionData<T>>((o1,o2) -> o1.getDistanceAway().compareTo(o2.getDistanceAway()));
	}

}
