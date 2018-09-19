package com.graphics.lib.canvas;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Observable;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collector;
import java.util.stream.Collectors;

import org.apache.commons.lang3.tuple.Pair;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.graphics.lib.Facet;
import com.graphics.lib.GeneralPredicates;
import com.graphics.lib.LightIntensityFinderEnum;
import com.graphics.lib.ObjectStatus;
import com.graphics.lib.Point;
import com.graphics.lib.Utils;
import com.graphics.lib.Vector;
import com.graphics.lib.VertexNormalFinderEnum;
import com.graphics.lib.WorldCoord;
import com.graphics.lib.camera.Camera;
import com.graphics.lib.collectors.CentreFinder;
import com.graphics.lib.interfaces.ICanvasObject;
import com.graphics.lib.interfaces.ILightIntensityFinder;
import com.graphics.lib.interfaces.IVertexNormalFinder;
import com.graphics.lib.transform.Transform;

import io.reactivex.subjects.PublishSubject;
import io.reactivex.subjects.Subject;

/**
 * CanvasObject provides the basic functionality for representing an object and moving it around on the screen 
 * 
 * @author Paul Brandon
 *
 */
public class CanvasObject extends Observable implements ICanvasObject {
	private static int nextId = 0;
	
	private final List<WorldCoord> vertexList;
    private final List<Facet> facetList;
    private Color colour = new Color(255, 0, 0);
    private final List<Transform> transforms = Collections.synchronizedList(new ArrayList<>());
    private final Set<ICanvasObject> children = Collections.synchronizedSet(new HashSet<ICanvasObject>());
    private Map<Point, List<Facet>> vertexFacetMap;
    private boolean processBackfaces = false;
    private boolean isVisible = true;
    private boolean isDeleted = false;
    private boolean castsShadow = true;
    private boolean deleteAfterTransforms = false;
    private Optional<Point> anchorPoint = Optional.empty();
    private final Set<String> flags = new HashSet<>();
    private ILightIntensityFinder liFinder = LightIntensityFinderEnum.DEFAULT.get();    
    private IVertexNormalFinder vnFinder = VertexNormalFinderEnum.DEFAULT.get();
    private Optional<WorldCoord> fixedCentre = Optional.empty();
    
    //eventually want to refactor away from Observable and use reactive streams, both will coexist until refactoring complete
    private final Subject<Transform> transformSubject = PublishSubject.create();
    private final Subject<ObjectStatus> statusSubject = PublishSubject.create();
    
	private final int objectId = nextId++;
	
	public CanvasObject(Supplier<Pair<ImmutableList<WorldCoord>, ImmutableList<Facet>>> initMesh) {
		Pair<ImmutableList<WorldCoord>, ImmutableList<Facet>> mesh = initMesh.get();
		vertexList = mesh.getLeft();
		facetList = mesh.getRight();
	}
	
	public <T extends CanvasObject> CanvasObject(Function<T, Pair<ImmutableList<WorldCoord>, ImmutableList<Facet>>> initMesh, Class<T> clazz) {
        Pair<ImmutableList<WorldCoord>, ImmutableList<Facet>> mesh = initMesh.apply(clazz.cast(this));
        vertexList = mesh.getLeft();
        facetList = mesh.getRight();
    }
	
	/**
	 * Use to save a fixed centre point so it is not recalculated every time
	 */
	@Override
	public void fixCentre() {
		fixedCentre = Optional.of(generateCentre());
	}
	
	@Override
	public boolean equals(Object obj) {
	    return Utils.cast(obj, ICanvasObject.class)
        	         .map(o -> getObjectTag().equals(o.getObjectTag()))
        	         .orElse(false);

	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + objectId;
		return result;
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
		doNotify(ObjectStatus.VISIBLE);
		statusSubject.onNext(ObjectStatus.VISIBLE);
	}

	@Override
	public final boolean isDeleted() {
		return this.isDeleted;
	}

	@Override
	public void setDeleted(boolean isDeleted) {
		this.isDeleted = isDeleted;
		if (isDeleted) {
		    this.doNotify(ObjectStatus.DELETED);
		    this.deleteObservers();
		    transformSubject.onComplete();
		    statusSubject.onComplete();
		}
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
	public final List<Facet> getFacetList() {
		return this.facetList;
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
	public final Map<Point, List<Facet>> getVertexFacetMap() {
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
			fixedCentre.ifPresent(centre -> transform.replay(centre.getTransformed(c)));
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
	public final void applyTransform(Transform...transforms)
	{
		//can be called directly for singular ad-hoc adjustments, without adding to the transform list, 
		//this should generally only be done on objects that haven't been registered to a canvas yet
	    Lists.newArrayList(transforms).forEach(t -> {
	        t.doTransform(this.vertexList);
	        fixedCentre.ifPresent(t::replay);
	        doNotify(t);
	        transformSubject.onNext(t);
	    });
		
	}
	
	@Override
	public final void replayTransform(Transform t)
	{
		t.replay(this.vertexList);
		fixedCentre.ifPresent(t::replay);
		doNotify(t);
		transformSubject.onNext(t);
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
		doNotify(ObjectStatus.TRANSFORMS_APPLIED);
		statusSubject.onNext(ObjectStatus.TRANSFORMS_APPLIED);
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
		return fixedCentre.orElse(generateCentre());
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
		doNotify(ObjectStatus.DRAW_COMPLETE);
		statusSubject.onNext(ObjectStatus.DRAW_COMPLETE);
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
		Map<Point,List<Facet>> vfMap = Maps.newHashMap();
		
		for (Facet f : this.facetList)
		{
			for (WorldCoord w : f.getAsList()){
				this.addPointFacetToMap(vfMap, w, f);
			}
		}

	 	//remove anything where the facet group is highly divergent (will then use facet normal when shading)
	 	for (List<Facet> fl : vfMap.values())
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
	 	
	 	this.vertexFacetMap = ImmutableMap.copyOf(vfMap);

	}
	
	@Deprecated //moving to reactive streams
	public void doNotify(Object arg) {
		this.setChanged();
		this.notifyObservers(arg);
	}
	
	@Override
	public Subject<Transform> observeTransforms() {
	    return transformSubject;
	}
	
	@Override
	public Subject<ObjectStatus> observeStatus() {
	    return statusSubject;
	}
	
	@Override
	public void setBaseIntensity(double intensity)
	{
		this.getFacetList().forEach(f -> f.setBaseIntensity(intensity));
	}
	
	protected WorldCoord generateCentre()
	{
		//default, average of all un-tagged points
		CentreFinder centre = this.vertexList.stream()
				.filter(GeneralPredicates.untagged(this))
				.collect(CentreFinder::new, CentreFinder::accept, CentreFinder::combine);
		
		return new WorldCoord(centre.result());
	}
	
	private void addPointFacetToMap(Map<Point, List<Facet>> vfMap, Point p, Facet f)
	{
	    vfMap.computeIfAbsent(p, key -> Lists.newArrayList()).add(f);
	}
}
