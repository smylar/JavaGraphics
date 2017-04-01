package com.graphics.lib.canvas;

import java.awt.Color;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Observable;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collector;
import java.util.stream.Collectors;

import com.graphics.lib.Facet;
import com.graphics.lib.GeneralPredicates;
import com.graphics.lib.Point;
import com.graphics.lib.Vector;
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
	
	private BaseData data;
	//private Set<ITrait> traits = new HashSet<>(); moving to BaseData, should move back here once all object wrappers converted to traits
	private int objectId = nextId++;
	protected Optional<ICanvasObject> wrappedObject = Optional.empty();
	
	
	public CanvasObject()
	{
		setData(getData());
	}
	
	public CanvasObject(ICanvasObject obj)
	{
		setWrappedObject(obj);
	}
	
	@Override
	public final Set<ITrait> getTraits() {
	    return getData().getTraits();
	}
	
	public <T extends ITrait> T addTrait(T trait) {
        trait.setParent(this);
        //traits.add(instance);
        getData().getTraits().add(trait);
        return trait;

	}
	
	@Override
	public final <T extends ITrait> Optional<T> getTrait(Class<T> trait) {
	    return getData().getTraits().stream().filter(t -> trait.isAssignableFrom(t.getClass())).map(trait::cast).findFirst();
	}
	
	/**
	 * Get this object as the given class as long as that class is found in the wrapped object hierarchy
	 * 
	 * @param cl - The class to look for
	 * @return A canvas object as the given type or null if it cannot be found
	 */
	@Override
	public final <C extends ICanvasObject> Optional<C> getObjectAs(Class<C> cl)
	{		
	    //TO go once all wrappers converted to traits
		if (cl.isAssignableFrom(this.getClass())){
			return Optional.of(cl.cast(this));
		}else{
			if (wrappedObject.isPresent()) {
				return wrappedObject.get().getObjectAs(cl);
			}
			
			return Optional.empty(); //may prefer to throw exception?
		}
	}
	
	protected final void setWrappedObject(ICanvasObject obj){
		wrappedObject = Optional.ofNullable(obj);
		wrappedObject.ifPresent(o -> this.setData(o.getData()));
	}
	
	
	protected final void setFunctions(CanvasObjectFunctionsImpl functions) {
		this.getData().setFunctions(functions);
	}
	
	@Override
	public final CanvasObjectFunctionsImpl getFunctions() {
		return this.getData().getFunctions();
	}
	
	/**
	 * All data members are held within the BaseData class for easy sharing with wrappers.
	 * <br/>
	 * This method returns that data
	 * 
	 * @see BaseData
	 * 
	 * @return The data for the root canvas object
	 */
	@Override
	public final BaseData getData()
	{
		if (data == null)
			data = new BaseData("obj" + objectId);
		return data;
	}
	
	@Override
	public final boolean is(ICanvasObject obj) {
		return obj == null ? false : getData() == obj.getData();
	}
	
	/**
	 * Set the data for the root canvas object
	 * 
	 * @param data - BaseData object containing the data for this object
	 */
	protected final void setData(BaseData data)
	{
		this.data = data;
	}

	@Override
	public final String getObjectTag(){
		return getData().getObjTag();
	}
	
	@Override
	public final void setAnchorPoint(Point p)
	{
		this.getData().setAnchorPoint(p);
	}
	
	@Override
	public Point getAnchorPoint(){
		return this.getData().getAnchorPoint()== null ? this.getCentre() : this.getData().getAnchorPoint();
	}
	
	@Override
	public final void addFlag(String flag)
	{
		getData().getFlags().add(flag);
	}
	
	@Override
	public final void removeFlag(String flag)
	{
		getData().getFlags().remove(flag);
	}
	
	@Override
	public final boolean hasFlag(String flag)
	{
		return getData().getFlags().contains(flag);
	}
	
	@Override
	public final Set<ICanvasObject> getChildren() {
		return getData().getChildren();
	}

	@Override
	public final boolean isVisible() {
		return getData().isVisible();
	}

	@Override
	public final void setVisible(boolean isVisible) {
		getData().setVisible(isVisible);
		doNotify();
	}

	@Override
	public final boolean isDeleted() {
		return getData().isDeleted();
	}

	@Override
	public void setDeleted(boolean isDeleted) {
		getData().setDeleted(isDeleted);
		wrappedObject.ifPresent(w -> {
			w.setDeleted(isDeleted); 
		});
		
		this.setChanged();
		this.notifyObservers();
	}

	@Override
	public final boolean getCastsShadow() {
		return getData().isCastsShadow();
	}

	public final void setCastsShadow(boolean castsShadow) {
		getData().setCastsShadow(castsShadow);
	}
	
	@Override
	public final Color getColour() {
		return getData().getColour();
	}

	@Override
	public final void setColour(Color colour) {
		getData().setColour(colour);
	}
	
	@Override
	public final List<WorldCoord> getVertexList() {
		return getData().getVertexList();
	}

	@Override
	public final void setVertexList(List<WorldCoord> vertexList) {
		getData().setVertexList(vertexList);
	}

	@Override
	public final List<Facet> getFacetList() {
		return getData().getFacetList();
	}

	@Override
	public final void setFacetList(List<Facet> facetList) {
		getData().setFacetList(facetList);
	}
	
	@Override
	public final boolean isProcessBackfaces() {
		return getData().isProcessBackfaces();
	}

	@Override
	public final void setProcessBackfaces(boolean processBackfaces) {
		getData().setProcessBackfaces(processBackfaces);
	}

	@Override
	public final void cancelTransforms()
	{
		synchronized(getData().getTransforms()) {
			getData().getTransforms().forEach(Transform::cancel);
		}
	}
	
	@Override
	public final void cancelNamedTransform(String key)
	{
		if (key == null) {
		    return;
		}
		synchronized(getData().getTransforms()) {
			getData().getTransforms().stream().filter(t -> key.equals(t.getName())).forEach(Transform::cancel);
		}
	}
	
	@Override
	public final Map<Point, ArrayList<Facet>> getVertexFacetMap() {
		return getData().getVertexFacetMap();
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
		synchronized(getData().getTransforms()) {
			return getData().getTransforms().stream().filter(t -> type.isAssignableFrom(t.getClass())).collect(Collector.of(
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
		synchronized(getData().getTransforms()) {
			getData().getTransforms().add(transform);
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
		//synchronized(getData().getVertexList()) {
			transform.doTransform(getData().getVertexList().stream().map(v -> v.getTransformed(c)).collect(Collectors.toList()));
		//}
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
		return !getData().getTransforms().isEmpty();
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
		synchronized(getData().getTransforms()) {
			return getData().getTransforms().stream().anyMatch(t -> name.equals(t.getName()));
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
		synchronized(getData().getTransforms()) {
			return getData().getTransforms().stream()
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
		t.doTransform(getData().getVertexList());
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
		if (getData().getVertexList() == null || getData().getVertexList().isEmpty() || getData().isDeleted()) {
		    return;
		}
		
		synchronized(getData().getTransforms()) {
			this.beforeTransforms();
			getData().getTransforms().stream().filter(t -> !t.isCancelled()).forEach(this::applyTransform);
			
			getData().getTransforms().removeIf(t -> {
									t.getDependencyList().removeIf(Transform::isComplete);
									return t.isComplete() && t.getDependencyList().size() == 0;
									});
		}
		if (getData().getTransforms().isEmpty() && getData().isDeleteAfterTransforms()){
			if (!getData().getChildren().isEmpty()) {
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
		if (wrappedObject.isPresent()) {
			wrappedObject.get().afterTransforms(); 
		} else {
			synchronized(getData().getChildren()) {
				this.getChildren().parallelStream().forEach(ICanvasObject::applyTransforms);
			}
        }
	}
	
	@Override
	public void beforeTransforms(){
		if (wrappedObject.isPresent()) {
			wrappedObject.get().beforeTransforms(); 
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
		return getData().getVnFinder();
	}
	
	@Override
    public final void setVertexNormalFinder(IVertexNormalFinder finder)
    {
        getData().setVnFinder(finder);
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
		if (wrappedObject.isPresent()) {
		    return wrappedObject.get().getCentre();
		}
		
		//default, average of all un-tagged points
		CentreFinder centre = getData().getVertexList().stream()
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
		getData().setDeleteAfterTransforms(true);
	}
	
	/**
	 * Observe another object and match its movements, <s>observed item will call update in this item via the Observer/Observable interface when it changes</s>
	 * <br/>
	 * Changed to add objects vertex list to the parent, avoids issues such as double counting when reusing a transform
	 * <br/>
	 * <br/>
	 * Note this object will be made a child of that it is observing so that it is always processed after the observed item
	 * 
	 * @param o
	 */
	/*@Override
	public final void observeAndMatch (ICanvasObject o){
	    //TODO separate out observers
		synchronized(data.getVertexList()){
			getData().setObserving(o);
			o.getChildren().add(this);
			this.getVertexList().forEach(v -> v.addTag(this.getObjectTag()));
			o.getVertexList().addAll(this.getVertexList());
		}
		
	}
	
	@Override
	public final void stopObserving(){
		if (getData().getObserving() != null){
			synchronized(data.getVertexList()) {
				getData().getObserving().getChildren().remove(this);
				getData().getObserving().getVertexList().removeIf(v -> v.hasTag(this.getObjectTag()));
				this.getVertexList().forEach(v -> v.removeTag(this.getObjectTag()));
				getData().setObserving(null);
			}
		}
	}*/
	
	/**
	 * This method will be executed once all draw operations (across all objects) are complete
	 */
	@Override
	public void onDrawComplete(){
		
		if (wrappedObject.isPresent()) {
			wrappedObject.get().onDrawComplete();
		}
		else{
			//if (this.isDeleted()) {
			//	this.stopObserving();
			//}
			synchronized(data.getChildren()) {
				this.getChildren().removeIf(ICanvasObject::isDeleted);
				this.getChildren().parallelStream().forEach(ICanvasObject::onDrawComplete);
			}
		}
	}
	
	@Override
	public ILightIntensityFinder getLightIntensityFinder()
	{
		return getData().getLiFinder();
	}
	
	@Override
	public void setLightIntensityFinder(ILightIntensityFinder liFinder)
	{
		getData().setLiFinder(liFinder);
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
		getData().setVertexFacetMap(new HashMap<>());
		
		for (Facet f : getData().getFacetList())
		{
			for (WorldCoord w : f.getAsList()){
				this.addPointFacetToMap(w, f);
			}
		}

	 	//remove anything where the facet group is highly divergent (will then use facet normal when shading)
	 	for (List<Facet> facetList : getData().getVertexFacetMap().values())
	 	{
	 		boolean isDivergent;
	 		Vector normal = facetList.get(0).getNormal();

	 		isDivergent = facetList.stream().anyMatch(f -> {
	 			double answer = normal.dotProduct(f.getNormal());
	 			if (Math.toDegrees(Math.acos(answer)) > divergenceLimit) return true;
	 			return false;
	 		});
	 		
	 		if (isDivergent) {
	 		    facetList.clear();
	 		}
	 	}

	}
	
	public void doNotify() {
		doNotify(null);
	}
	
	@Override
	public void doNotify(Object arg) {
		this.setChanged();
		this.notifyObservers(arg);
		wrappedObject.ifPresent(o -> o.doNotify(arg));
	}
	
	private void addPointFacetToMap(Point p, Facet f)
	{
		if (!getData().getVertexFacetMap().containsKey(p)){
			getData().getVertexFacetMap().put(p, new ArrayList<Facet>());
		}
		getData().getVertexFacetMap().get(p).add(f);
	}
	
	@Override
	public void setBaseIntensity(double intensity)
	{
		getData().getFacetList().forEach(f -> f.setBaseIntensity(intensity));
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
