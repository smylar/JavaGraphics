package com.graphics.lib.traits;

import java.util.*;
import java.util.function.Function;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.graphics.lib.interfaces.ICanvasObject;
import com.graphics.lib.interfaces.ITrait;

/**
 * Add traits (additional functionality) to an object
 * 
 * @author paul.brandon
 *
 */
public class TraitHandler {
    public static final TraitHandler INSTANCE = new TraitHandler();
    
    private final Map<ICanvasObject,Set<ITrait>> traitMap = Maps.newHashMap();
    
    private TraitHandler() {}
    
    /**
     * Associate an object with a trait
     * 
     * @param obj   Object to gain trait
     * @param traitClass Class of trait to give to object
     * @return      The trait
     */
    public <T extends ITrait> T registerTrait(final ICanvasObject obj, final Class<T> traitClass) {
        try {
            T trait = traitClass.getConstructor(ICanvasObject.class).newInstance(obj);
            return registerTrait(obj, co -> trait);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null; //refactor for optional!
    }

    public <T extends ITrait> T registerTrait(final ICanvasObject obj, final Function<ICanvasObject,T> traitObject) {
        T trait = traitObject.apply(obj);

        synchronized (traitMap) {
            traitMap.computeIfAbsent(obj, key -> Sets.newConcurrentHashSet()).add(trait);
        }
        obj.observeDeath().subscribe(d -> traitMap.remove(obj));

        return trait;
    }
    
    /**
     * Retrieve a trait from an object
     * 
     * @param obj   The object to get the trait from
     * @param trait The trait class to retrieve
     * @return      Optional trait, empty if trait not registered against object
     */
    public <T extends ITrait> Optional<T> getTrait(ICanvasObject obj, Class<T> trait) {
            return traitMap.getOrDefault(obj, Set.of())
            			   .stream()
            			   .dropWhile(t -> !trait.isAssignableFrom(t.getClass()))
            			   .findFirst()
            			   .map(trait::cast);
    }

}
