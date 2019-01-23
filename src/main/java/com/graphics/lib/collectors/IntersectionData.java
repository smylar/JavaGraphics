package com.graphics.lib.collectors;

import com.graphics.lib.Facet;
import com.graphics.lib.Point;

/**
 * Stores information on an intersection with a vector
 * 
 * @author paul.brandon
 *
 * @param <T>
 */
public final class IntersectionData<T> {
	private final T parent;
	
	private final Facet facet;
	
	private final Point intersection;
	private final Double distanceAway;

	public IntersectionData(T parent, Facet facet, Point intersection, Double distanceAway) {
		this.parent = parent;
		this.facet = facet;
		this.intersection = intersection;
		this.distanceAway = distanceAway;
	}

	public T getParent() {
		return parent;
	}

	public Facet getFacet() {
		return facet;
	}

	public Point getIntersection() {
		return intersection;
	}
	
	public Double getDistanceAway() {
	    return distanceAway;
	}
}
