package com.graphics.lib;

import java.awt.Color;
import java.awt.Graphics;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Predicate;

import org.apache.commons.lang3.tuple.Pair;

import com.google.common.collect.ImmutableList;
import com.graphics.lib.canvas.CanvasObject;
import com.graphics.lib.canvas.FunctionHandler;
import com.graphics.lib.interfaces.ICanvasObject;
import com.graphics.lib.interfaces.ICanvasObjectList;
import com.graphics.lib.interfaces.ILightIntensityFinder;
import com.graphics.lib.lightsource.ILightSource;
import com.graphics.lib.transform.MovementTransform;

import io.reactivex.Observable;

/**
 * Random orphaned utilities that need a proper home
 * 
 * @author paul
 *
 */
public class Utils {	
	public static ILightIntensityFinder getShadowLightIntensityFinder(final ICanvasObjectList objectsToCheck)
	{
		//EXPERIMENTAL
		return (ls, obj, p, v, bf) -> {
			IntensityComponents maxIntensity = new IntensityComponents();
			
			ls.stream().filter(l -> l.isOn()).forEach(l ->
			{
				IntensityComponents intComps = l.getIntensityComponents(p);
				if (intComps.hasNoIntensity()) return;
				
				Vector lightVector = l.getPosition().vectorToPoint(p).getUnitVector();
				
				if (isBlockingObjectPresent(objectsToCheck.get(), obj, l, p, lightVector)) return;
							
				double percent = 0;
				
				double deg = v.angleBetween(lightVector);
				
				if (deg > 90 && !bf)
				{			
					percent = (deg-90) / 90;
				}
				else if (bf)
				{
					//light on the rear of the facet for if we are processing backfaces - which implies the rear may be visible
					percent = (90-deg) / 90;
				}
				
				final double pc = percent;
                IntensityComponents.forEach(comp -> {
                    double intensity = intComps.get(comp) * pc;
                    if (intensity > maxIntensity.get(comp)) 
                        maxIntensity.set(comp, intensity);
                });
			});
			return maxIntensity;
		};
	}
	
	public static BiConsumer<Pair<Integer,Integer>, Graphics> drawCircle(final int radius, final Color startColour, final Color endColour){
		//drawing a circle without using drawCircle, just because :)
		return (p,g) -> {
			double rs = Math.pow(radius,2);
			int redDiff = endColour.getRed() - startColour.getRed();
			int blueDiff = endColour.getBlue() - startColour.getBlue();
			int greenDiff = endColour.getGreen() - startColour.getGreen();
			int alphaDiff = endColour.getAlpha() - startColour.getAlpha();
			for(int i = 0 ; i <= radius ; i++)
			{
				int length = (int)Math.round(Math.sqrt(rs - Math.pow(i,2))) ;
				for (int j = 0 ; j <= length ; j++ ) {
					double pc = Math.sqrt(Math.pow(j, 2) + Math.pow(i, 2)) / (double)radius;
					if (pc > 1) pc = 1;
					int cRed = (int)Math.floor(startColour.getRed() + ((double)redDiff * pc));
					int cGreen = (int)Math.floor(startColour.getGreen() + ((double)greenDiff * pc));
					int cBlue = (int)Math.floor(startColour.getBlue() + ((double)blueDiff * pc));
					int cAlpha = (int)Math.floor(startColour.getAlpha() + ((double)alphaDiff * pc));
			
					g.setColor(new Color(cRed, cGreen, cBlue, cAlpha));
			
					g.drawLine(p.getLeft()+j, p.getRight()-i, p.getLeft()+j, p.getRight()-i); 
					g.drawLine(p.getLeft()+j, p.getRight()+i, p.getLeft()+j, p.getRight()+i);
					g.drawLine(p.getLeft()-j, p.getRight()-i, p.getLeft()-j, p.getRight()-i); 
					g.drawLine(p.getLeft()-j, p.getRight()+i, p.getLeft()-j, p.getRight()+i); 
					//this will form a crosshair when using different alpha values, as we'll be drawing twice in some cases (unless we check for j = 0 etc)
					//is a nice effect so keeping for now
				}
			}
		};
	}
	
	/**
	 * Generate a point based on the distance to 2 known points
	 * (intersecting circles problem) currently applies 2D only
	 * 
	 * @param p1 First known point
	 * @param p2 Second known point
	 * @param r1 Distance to first known point (radius of first circle)
	 * @param r2 Distance to second known point (radius of first circle)
	 * @return
	 */
	public static Optional<Pair<Point, Point>> getPointFromKnownPoints(Point p1, Point p2, double r1, double r2) {
		double dist = p1.distanceTo(p2);
		
		if (r1 + r2 < dist || dist == 0) {
			//cannot overlap
			return Optional.empty();
		}
		
		if (r1 + r2 == dist) {
			//may need some tolerance
			//circles touch at one point
			Vector v = p1.vectorToPoint(p2).getUnitVector();
			Point p3 = new Point(p1.x + (v.getX()*r1) , p1.y + (v.getY()*r1), 0);
			return Optional.of(Pair.of(p3, p3));
		}
		
		double a = (r1*r1 - r2*r2 + dist*dist)/(2*dist);
		double h = Math.sqrt(r1*r1 - a*a);
		double p3x = p1.x + (p2.x - p1.x)*(a/dist);
		double p3y = p1.y + (p2.y - p1.y)*(a/dist);
		Point p3 = new Point(p3x, p3y, 0);
		
		return Optional.of(Pair.of(
				new Point(p3.x + h*(p2.y - p1.y)/dist, p3.y - h*(p2.x - p1.x)/dist, 0),
				new Point(p3.x - h*(p2.y - p1.y)/dist, p3.y + h*(p2.x - p1.x)/dist, 0)
		));
	}
	
	public static void reflect (MovementTransform move, Vector surfaceNormal) {
		Vector moveVector = move.getVector();
	    //I know r=d-2(d.n)n is reflection vector in 2 dimension (hopefully it'll work on 3)
        double multiplier = moveVector.dotProduct(surfaceNormal) * -2;
             
        move.setVector(Vector.builder()
        					 .x(moveVector.getX() + (surfaceNormal.getX() * multiplier))
        					 .y(moveVector.getY() + (surfaceNormal.getY() * multiplier))
        					 .z(moveVector.getZ() + (surfaceNormal.getZ() * multiplier))
        					 .build());
	}
	
	public static CanvasObject getParticle(final Point p, final double particleSize) {
        return new CanvasObject(() -> {
            ImmutableList<WorldCoord> vertexList = ImmutableList.of(new WorldCoord(p.x , p.y, p.z),
                                                                    new WorldCoord(p.x + particleSize , p.y, p.z),
                                                                    new WorldCoord(p.x, p.y + particleSize, p.z));
            ImmutableList<Facet> facetList = ImmutableList.of(new Facet(vertexList.get(0), vertexList.get(1), vertexList.get(2)));
            return Pair.of(vertexList, facetList);
        });
    }
    
    public static CanvasObject getFragment(Facet facet) {
        return new CanvasObject(() -> {
            ImmutableList<WorldCoord> vertexList = ImmutableList.of(new WorldCoord(facet.first().x , facet.first().y, facet.first().z),
                                                                    new WorldCoord(facet.second().x , facet.second().y, facet.second().z),
                                                                    new WorldCoord(facet.third().x , facet.third().y, facet.third().z));
            ImmutableList<Facet> facetList = ImmutableList.of(new Facet(vertexList.get(0), vertexList.get(1), vertexList.get(2)));
            return Pair.of(vertexList, facetList);
        });
    }

    public static CanvasObject getCoordAsCanvasObject(WorldCoord coord) {
        return new CanvasObject(() -> {
            ImmutableList<WorldCoord> vertexList = ImmutableList.of(coord);
            ImmutableList<Facet> facetList = ImmutableList.of();
            return Pair.of(vertexList, facetList);
        });
    }
    
    public static <T> boolean allMatchAny(List<T> list, List<Predicate<T>> tests) {
        for (Predicate<T> test : tests) {
            if (list.stream().allMatch(test)) {
                return true;
            }
        }
        
        return false;
    }
    
    public static <T> Optional<T> cast(Object obj, Class<T> clazz) {
        return Optional.ofNullable(obj)
                       .filter(o -> clazz.isAssignableFrom(o.getClass()))
                       .map(clazz::cast);
    }
    
    
    public static <T,R> R recurse(final Iterable<T> iteratable, final BiFunction<T,R,R> function, final R initital) {
        //actually quite similar to using stream().collect() or .reduce()
        return recurse(iteratable.iterator(), function, initital);
    }
    
    public static <T,R> R recurse(final Iterable<T> iteratable, final BiFunction<T,R,R> function) {
        return recurse(iteratable.iterator(), function, null);
    }
    
    public static <T,R> R recurse(final Iterator<T> iterator, final BiFunction<T,R,R> function, final R prevResult) {
        return iterator.hasNext() ? recurse(iterator, function, function.apply(iterator.next(), prevResult)) : prevResult;
    }
    
    private static boolean isBlockingObjectPresent(Collection<ICanvasObject> objectsToCheck, ICanvasObject parent, ILightSource l, Point p, Vector lightVector) {
        return Observable.fromIterable(objectsToCheck)
                         .filter(o -> o != parent && o.getCastsShadow() && o.isVisible())
                         .filter(o -> {
                              Vector v1 = l.getPosition().vectorToPoint(o.getCentre()).getUnitVector();
                              Vector v2 = p.vectorToPoint(o.getCentre()).getUnitVector();
                              return v1.dotProduct(v2) < 0;
                         })
                         .map(o -> FunctionHandler.getFunctions(o).vectorIntersects(o, l.getPosition(), lightVector))
                         .filter(b -> b)
                         .blockingFirst(false);
    }
}
