package com.graphics.lib.traits;

import java.util.Map;
import java.util.Observable;
import java.util.Observer;
import java.util.Optional;
import java.util.Set;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.graphics.lib.ObjectStatus;
import com.graphics.lib.interfaces.ICanvasObject;
import com.graphics.lib.interfaces.ITrait;

/**
 * Add traits (additional functionality) to an object
 * 
 * @author paul.brandon
 *
 */
public class TraitHandler implements Observer {
    public static final TraitHandler INSTANCE = new TraitHandler();
    
    private Map<ICanvasObject,Set<ITrait>> traitMap = Maps.newHashMap();
    
    private TraitHandler() {}

    /**
     * Associate an object with a trait
     * 
     * @param obj   Object to gain trait
     * @param trait Trait to give to object
     * @return      The trait
     */
    public <T extends ITrait> T registerTrait(ICanvasObject obj, T trait) {
        traitMap.computeIfAbsent(obj, key -> Sets.newConcurrentHashSet()).add(trait);
        trait.setParent(obj);
        obj.addObserver(this);
        return trait;
    }
    
    public <T extends ITrait> T registerTrait(ICanvasObject obj, Class<T> traitClass) {
        T trait = null;
        try {
            trait = traitClass.getConstructor(ICanvasObject.class).newInstance(obj);
            traitMap.computeIfAbsent(obj, key -> Sets.newConcurrentHashSet()).add(trait);
            obj.addObserver(this);
        } catch (Exception e) {
            e.printStackTrace();
        }
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
        if (traitMap.containsKey(obj)) {
            return traitMap.get(obj).stream().filter(t -> trait.isAssignableFrom(t.getClass())).map(trait::cast).findFirst();
        }
        
        return Optional.empty();
    }
    
    @Override
    public void update(Observable o, Object arg) {
        if (arg == ObjectStatus.DELETED) {
            traitMap.remove(o);
        }
    }

}
