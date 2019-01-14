package com.graphics.lib.collectors;

import com.graphics.lib.Facet;
import com.graphics.lib.Point;

public final class IntersectionData<T> {
	private final T parent;
	
	private final Facet facet;
	
	private final Point intersection;

	public IntersectionData(T parent, Facet facet, Point intersection) {
		this.parent = parent;
		this.facet = facet;
		this.intersection = intersection;
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
}
