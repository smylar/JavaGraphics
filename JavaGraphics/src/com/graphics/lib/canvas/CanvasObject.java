package com.graphics.lib.canvas;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Observable;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collector;
import java.util.stream.Collectors;

import com.graphics.lib.Facet;
import com.graphics.lib.GeneralPredicates;
import com.graphics.lib.LightIntensityFinderEnum;
import com.graphics.lib.Point;
import com.graphics.lib.Vector;
import com.graphics.lib.VertexNormalFinderEnum;
import com.graphics.lib.WorldCoord;
import com.graphics.lib.camera.Camera;
import com.graphics.lib.collectors.CentreFinder;
import com.graphics.lib.interfaces.ICanvasObject;
import com.graphics.lib.interfaces.ILightIntensityFinder;
import com.graphics.lib.interfaces.ITrait;
import com.graphics.lib.interfaces.IVertexNormalFinder;
import com.graphics.lib.transform.Transform;

/**
 * CanvasObject provides the basic functionality for representing an object and moving it around on the screen 
 * 
 * This class has been designed to be wrapped by other classes extending CanvasObject, this allows us if you wish,
 * to in effect inherit from multiple classes, so we have the flexibility to selectively add functionality to a base object.
 * <br/>
 *  e.g. We can add plugin and orientation functionality to a Sphere object without the Sphere having to provide that functionality
 *  <br>
 *  This would be done as follows (though it looks a little horrid especially if we start adding more):
 *  <br/>
 *  <code>PlugableCanvasObject&lt;?> obj = new PlugableCanvasObject&lt;CanvasObject>(new OrientableCanvasObject&lt;Ovoid>(new Sphere(20,30)));</code>
 *  <br/>
 *  <br/>
 *  However, we can still extend in the normal way so that Sphere could include this functionality
 * 
 * @author Paul Brandon
 *
 */
public class CanvasObject extends Observable implements ICanvasObject{
	private static int nextId = 0;
	
	private List<WorldCoord> vertexList = new ArrayList<>();
    private List<Facet> facetList = new ArrayList<>();
    private Color colour = new Color(255, 0, 0);
    private List<Transform> transforms = Collections.synchronizedList(new ArrayList<>());
    private Set<ICanvasObject> children = Collections.synchronizedSet(new HashSet<ICanvasObject>());
    private Map<Point, ArrayList<Facet>> vertexFacetMap;
    private boolean processBackfaces = false;
    private boolean isVisible = true;
    private boolean isDeleted = false;
    private boolean castsShadow = true;
    private boolean deleteAfterTransforms = false;
    private Optional<Point> anchorPoint = Optional.empty();
    private Set<String> flags = new HashSet<>();
    private ILightIntensityFinder liFinder = LightIntensityFinderEnum.DEFAULT.get();    
    private IVertexNormalFinder vnFinder = VertexNormalFinderEnum.DEFAULT.get();
    
	private Set<ITrait> traits = new HashSet<>(); //may move traits completely external of canvas object, possibly others too like functions
	private int objectId = nextId++;
	
	@Override
	public final Set<ITrait> getTraits() {
	    return traits;
	}
	
	@Override
	public final <T extends ITrait> T addTrait(T trait) {
        trait.setParent(this);
        traits.add(trait);
        return trait;

	}
	
	@Override
	public final <T extends ITrait> Optional<T> getTrait(Class<T> trait) {
	    return traits.stream().filter(t -> trait.isAssignableFrom(t.getClass())).map(trait::cast).findFirst();
	}
	
	@Override
	public final boolean is(ICanvasObject obj) {
	    //need to do this because of the use of proxies, == may not always work
		return obj == null ? false : getObjectTag().equals(obj.getObjectTag());
	}

	@Override
	public <C extends ICanvasObject> Optional<C> getObjectAs(Class<C> target) {
        return target.isAssignableFrom(this.getClass()) ? Optional.of(target.cast(this)) : Optional.empty();
    }
	
	@Override
	public final String getObjectTag(){
		return "object"+objectId;
	}
	
	@Override
	public final void setAnchorPoint(Point p)
	{
		this.anchorPoint = Optional.ofNullable(p);
	}
	
	@Override
	public Point getAnchorPoint(){
	    return this.anchorPoint.orElse(getCentre());
	}
	
	@Override
	public final void addFlag(String flag)
	{
		this.flags.add(flag);
	}
	
	@Override
	public final void removeFlag(String flag)
	{
	    this.flags.remove(flag);
	}
	
	@Override
	public final boolean hasFlag(String flag)
	{
		return this.flags.contains(flag);
	}
	
	@Override
	public final Set<ICanvasObject> getChildren() {
		return this.children;
	}

	@Override
	public final boolean isVisible() {
		return this.isVisible;
	}

	@Override
	public final void setVisible(boolean isVisible) {
		this.isVisible = isVisible;
		doNotify();
	}

	@Override
	public final boolean isDeleted() {
		return this.isDeleted;
	}

	@Override
	public void setDeleted(boolean isDeleted) {
		this.isDeleted = isDeleted;
		this.doNotify();
	}

	@Override
	public final boolean getCastsShadow() {
		return this.castsShadow;
	}

	public final void setCastsShadow(boolean castsShadow) {
		this.castsShadow = castsShadow;
	}
	
	@Override
	public final Color getColour() {
		return this.colour;
	}

	@Override
	public final void setColour(Color colour) {
		this.colour = colour;
	}
	
	@Override
	public final List<WorldCoord> getVertexList() {
		return this.vertexList;
	}

	@Override
	public final void setVertexList(List<WorldCoord> vertexList) {
		this.vertexList = vertexList;
	}

	@Override
	public final List<Facet> getFacetList() {
		return this.facetList;
	}

	@Override
	public final void setFacetList(List<Facet> facetList) {
		this.facetList = facetList;
	}
	
	@Override
	public final boolean isProcessBackfaces() {
		return this.processBackfaces;
	}

	@Override
	public final void setProcessBackfaces(boolean processBackfaces) {
		this.processBackfaces = processBackfaces;
	}

	@Override
	public final void cancelTransforms()
	{
		synchronized(this.transforms) {
			this.transforms.forEach(Transform::cancel);
		}
	}
	
	@Override
	public final void cancelNamedTransform(String key)
	{
		if (key == null) {
		    return;
		}
		synchronized(this.transforms) {
			this.transforms.stream().filter(t -> key.equals(t.getName())).forEach(Transform::cancel);
		}
	}
	
	@Override
	public final Map<Point, ArrayList<Facet>> getVertexFacetMap() {
		return this.vertexFacetMap;
	}

	/**
	 * Gets a list of transforms that are of the given type that are currently in the transform list
	 * 
	 * @see Transform
	 * 
	 * @param type - Class type of the transforms to retrieve
	 * @return List of transforms found
	 */
	@Override
	public final <T> List<T> getTransformsOfType(Class<T> type)
	{
		synchronized(this.transforms) {
			return this.transforms.stream().filter(t -> type.isAssignableFrom(t.getClass())).collect(Collector.of(
													ArrayList::new,
													(trans, orig) -> trans.add(type.cast(orig)),
													(left, right) -> {left.addAll(right); return left;},
													Collector.Characteristics.CONCURRENT
													));
		}
	}
	
	@Override
	public final void addTransform(Transform transform)
	{
		synchronized(this.transforms) {
		    this.transforms.add(transform);
		}
	}
	
	/**
	 * Apply camera transforms so the object is represented as seen from the camera
	 * 
	 * @param transform
	 */
	@Override
	public final void applyCameraTransform(Transform transform, Camera c)
	{
			transform.doTransform(this.vertexList.stream().map(v -> v.getTransformed(c)).collect(Collectors.toList()));
	}
	
	/**
	 * Check if any transforms are present in the objects transform list
	 * @see Transform
	 * 
	 * @return <code>True</code> if transform(s) present, <code>False</code> otherwise
	 */
	@Override
	public final boolean hasTransforms()
	{
		return !this.transforms.isEmpty();
	}
	
	/**
	 * Check if any transforms with a specified name are present in the objects transform list
	 * @see Transform
	 * 
	 * @return <code>True</code> if transform(s) present, <code>False</code> otherwise
	 */
	@Override
	public final boolean hasNamedTransform(String name){
		if (name == null) {
		    return false;
		}
		synchronized(this.transforms) {
			return this.transforms.stream().anyMatch(t -> name.equals(t.getName()));
		}
	}
	
	/**
	 * Retrieve a named transform from the transform list if it exists
	 * 
	 * @param key - Name of the transform
	 * @return The transform found or null
	 */
	@Override
	public final <T extends Transform> Optional<T> getTransform(String key, Class<T> clazz) {
		synchronized(this.transforms) {
			return this.transforms.stream()
            						.filter(t -> t.getName() == key && clazz.isAssignableFrom(t.getClass()))
            						.map(clazz::cast)
            						.findFirst();
		}
	}
	
	/**
	 * Apply a given transform (e.g. Moving or rotating the object) immediately to this object
	 * 
	 * @see Transform
	 * 
	 * @param t - Transform to apply
	 */
	@Override
	public final void applyTransform(Transform t)
	{
		//can be called directly for singular ad-hoc adjustments, without adding to the transform list, 
		//this should generally only be done on objects that haven't been registered to a canvas yet
		t.doTransform(this.vertexList);
		doNotify(t);
	}
	
	/**
	 * Apply the transforms currently held within the transform list to this object.
	 * <br/>
	 * Any transform that is complete will be removed from the list at this point
	 * 
	 */
	@Override
	public final void applyTransforms()
	{
		if (this.vertexList == null || this.vertexList.isEmpty() || this.isDeleted) {
		    return;
		}
		
		synchronized(this.transforms) {
			this.beforeTransforms();
			this.transforms.stream().filter(t -> !t.isCancelled()).forEach(this::applyTransform);
			
			this.transforms.removeIf(t -> {
									t.getDependencyList().removeIf(Transform::isComplete);
									return t.isComplete() && t.getDependencyList().size() == 0;
									});
		}
		if (this.transforms.isEmpty() && this.deleteAfterTransforms){
			if (!this.children.isEmpty()) {
			    this.setVisible(false);
			}
			else {
			    this.setDeleted(true);
			}
		}
		
		this.afterTransforms();
	}
	
	
	@Override
	public void afterTransforms(){
		synchronized(this.children) {
			this.children.parallelStream().forEach(ICanvasObject::applyTransforms);
		}
	}
	
	/**
	 * Describes how to generate a normal vector for a Vertex 
	 * 
	 * @see IVertexNormalFinder
	 * 
	 * @return
	 */
	@Override
	public final IVertexNormalFinder getVertexNormalFinder()
	{
		return this.vnFinder;
	}
	
	@Override
    public final void setVertexNormalFinder(IVertexNormalFinder finder)
    {
        this.vnFinder = finder;
    }
	
	/**
	 * Gets the centre point of this object
	 * 
	 * @see Point
	 * 
	 * @return Point representing the centre of this object
	 */
	@Override
	public Point getCentre()
	{
		//default, average of all un-tagged points
		CentreFinder centre = this.vertexList.stream()
				.filter(GeneralPredicates.untagged(this))
				.collect(CentreFinder::new, CentreFinder::accept, CentreFinder::combine);
		
		return centre.result();
	}
	
	/**
	 * Sets a flag indicating if this object should be set as deleted once all its Transforms are complete
	 */
	@Override
	public final void deleteAfterTransforms()
	{
		this.deleteAfterTransforms = true;
	}
	
	/**
	 * This method will be executed once all draw operations (across all objects) are complete
	 */
	@Override
	public void onDrawComplete(){
		synchronized(this.getChildren()) {
			this.getChildren().removeIf(ICanvasObject::isDeleted);
			this.getChildren().parallelStream().forEach(ICanvasObject::onDrawComplete);
		}
	}
	
	@Override
	public ILightIntensityFinder getLightIntensityFinder()
	{
		return this.liFinder;
	}
	
	@Override
	public void setLightIntensityFinder(ILightIntensityFinder liFinder)
	{
		this.liFinder = liFinder;
	}
	
	/**
	 * On calling this method, a map will be generated with the normals for each vertex as the average of the normal vector of the facets that share this vertex
	 * <br/>
	 * Depending upon the vertex normal finder being used, this map will then be used to find vertex normals
	 * 
	 * @param divergenceLimit - If the angle between two facets is larger than this value, than a value will not be mapped
	 */
	public final void useAveragedNormals(int divergenceLimit)
	{
		//create map for getting all the facets attached to a specific vertex
		this.vertexFacetMap = new HashMap<>();
		
		for (Facet f : this.facetList)
		{
			for (WorldCoord w : f.getAsList()){
				this.addPointFacetToMap(w, f);
			}
		}

	 	//remove anything where the facet group is highly divergent (will then use facet normal when shading)
	 	for (List<Facet> fl : this.vertexFacetMap.values())
	 	{
	 		boolean isDivergent;
	 		Vector normal = fl.get(0).getNormal();

	 		isDivergent = fl.stream().anyMatch(f -> {
	 			double answer = normal.dotProduct(f.getNormal());
	 			if (Math.toDegrees(Math.acos(answer)) > divergenceLimit) return true;
	 			return false;
	 		});
	 		
	 		if (isDivergent) {
	 		    fl.clear();
	 		}
	 	}

	}
	
	public void doNotify() {
		doNotify(null);
	}
	
	public void doNotify(Object arg) {
		this.setChanged();
		this.notifyObservers(arg);
	}
	
	private void addPointFacetToMap(Point p, Facet f)
	{
		if (!this.vertexFacetMap.containsKey(p)){
		    this.vertexFacetMap.put(p, new ArrayList<Facet>());
		}
		this.vertexFacetMap.get(p).add(f);
	}
	
	@Override
	public void setBaseIntensity(double intensity)
	{
		this.getFacetList().forEach(f -> f.setBaseIntensity(intensity));
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + this.getClass().getName().hashCode();
		result = prime * result + objectId;
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		ICanvasObject other = (ICanvasObject) obj;
		if (this.getColour() == null) {
			if (other.getColour()!= null)
				return false;
		} else if (!this.getColour().equals(other.getColour()))
			return false;
		if (this.getFacetList() == null) {
			if (other.getFacetList() != null)
				return false;
		} else if (!this.getFacetList().equals(other.getFacetList()))
			return false;
		if (this.getVertexList() == null) {
			if (other.getVertexList() != null)
				return false;
		} else if (!this.getVertexList().equals(other.getVertexList()))
			return false;
		return true;
	}
}
