package com.graphics.lib;

import java.awt.Color;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.UnaryOperator;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

public final class IntensityComponents implements UnaryOperator<Color>{
    
    private final double defaultIntensity;
    
    public enum ColourComponent {
        RED,
        GREEN,
        BLUE;
    }
    
    private final Map<ColourComponent, Double> intensities = Maps.newEnumMap(ColourComponent.class);
    
    public IntensityComponents() {
        this(0d);
    }
    
    public IntensityComponents(double defaultIntensity) {
        this.defaultIntensity = defaultIntensity;
    }
    
    public double get(ColourComponent comp) {
        return intensities.getOrDefault(comp, defaultIntensity);
    }
    
    public void set(ColourComponent comp, double val) {
        double valCheck = val;
        if (valCheck > 1) 
            valCheck = 1;
        else if (valCheck < 0) 
            valCheck = 0;
        
        intensities.put(comp, valCheck);
    }
	
	public boolean hasNoIntensity() {
	    return intensities.isEmpty() || intensities.values().stream().allMatch(val -> val.doubleValue() <= 0);
	}
	
	@Override
	public Color apply(Color colour) {
		return new Color((int)((double)colour.getRed() * get(ColourComponent.RED)), 
				(int)((double)colour.getGreen() * get(ColourComponent.GREEN)), 
				(int)((double)colour.getBlue() * get(ColourComponent.BLUE)),
				colour.getAlpha());
	}
	
	public static void forEach(Consumer<ColourComponent> consumer) {
        Lists.newArrayList(IntensityComponents.ColourComponent.values()).stream().forEach(consumer::accept);
    }
}
