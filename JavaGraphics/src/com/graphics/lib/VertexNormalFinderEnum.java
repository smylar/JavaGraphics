package com.graphics.lib;

import java.util.List;

import com.graphics.lib.interfaces.IVertexNormalFinder;

public enum VertexNormalFinderEnum {
	DEFAULT((obj, p, f) -> {
			if (obj.getVertexFacetMap() != null){
				List<Facet> facetList = obj.getVertexFacetMap().get(p);
				if (facetList != null && facetList.size() > 0)
				{	
					Vector normal = new Vector(0,0,0);
					for(Facet facet : facetList){
						normal.addVector(facet.getNormal());
					}
					return normal.getUnitVector(); //could possibly store for reuse between transforms
				}
			}
			
			return f.getNormal();
	}), 
	
	CENTRE_TO_POINT((obj, p, f) -> {
			Point centre = obj.getCentre();
			return new Vector(p.x - centre.x, p.y - centre.y, p.z - centre.z).getUnitVector();
	});
	
	private IVertexNormalFinder finder;
	
	private VertexNormalFinderEnum(IVertexNormalFinder finder) {
		this.finder = finder;
	}
	
	public IVertexNormalFinder get() {
		return this.finder;
	}
}
