package com.graphics.tests.weapons;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.stream.Collectors;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.graphics.lib.interfaces.IEffector;

/**
 * Track ammo status of different weapons
 * 
 * @author paul
 *
 */
public class AmmoTracker {
    //TODO will probably want to extend group by owning object if we add weapons to something other than the ship
    public static final AmmoTracker INSTANCE = new AmmoTracker();
    private HashMap<IEffector, AmmoHandler> tracker = Maps.newHashMap();
    
    private AmmoTracker() {}
    
    public void add(IEffector effector, AmmoHandler handler) {
        tracker.put(effector, handler);
    }
    
    public Map<IEffector, AmmoHandler> getTracked() {
        return ImmutableMap.copyOf(tracker);
    }
    
    public AmmoHandler getTracked(IEffector key) {
        return tracker.get(key);
    }
    
    public Set<AmmoHandler> getTracked(Class<? extends IEffector> clazz) {
        return tracker.entrySet().stream().filter(e -> e.getKey().getClass().equals(clazz))
                                           .map(Entry::getValue)
                                           .collect(Collectors.toSet());
    }
}
