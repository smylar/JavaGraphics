package com.graphics.lib;

import java.util.List;
import java.util.Objects;

import com.graphics.lib.interfaces.IVertexNormalFinder;

public enum VertexNormalFinderEnum {
    DEFAULT((obj, p, f) -> {
            if (obj.getVertexFacetMap() != null){
                List<Facet> facetList = obj.getVertexFacetMap().get(p);
                if (Objects.nonNull(facetList) && !facetList.isEmpty())
                {    
                    return facetList.stream().map(facet -> new Vector(facet.getNormal()))
                                             .reduce(Vector.ZERO_VECTOR, (left, right) -> left.combine(right))
                                             .getUnitVector(); 
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
