package com.graphics.lib.traits;

import java.util.Map;
import java.util.Optional;
import java.util.Set;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.graphics.lib.interfaces.ICanvasObject;
import com.graphics.lib.interfaces.ITrait;

/**
 * Link traits to objects
 * 
 * @author paul
 *
 */
public enum TraitManager {
	TRAITS;
	
	private final Map<ICanvasObject,Set<ITrait>> objectTraits = Maps.newHashMap();
	
	public final Set<ITrait> getTraits(ICanvasObject obj) {
	    return objectTraits.getOrDefault(obj, Sets.newHashSet());
	}
	
	public final <T extends ITrait> T addTrait(ICanvasObject obj, T trait) {
        trait.setParent(obj);
        objectTraits.putIfAbsent(obj, Sets.newHashSet());
        objectTraits.get(obj).add(trait);
        return trait;

	}
	
	public final <T extends ITrait> Optional<T> getTrait(ICanvasObject obj, Class<T> trait) {
	    return getTraits(obj).stream().filter(t -> trait.isAssignableFrom(t.getClass())).map(trait::cast).findFirst();
	}
}
