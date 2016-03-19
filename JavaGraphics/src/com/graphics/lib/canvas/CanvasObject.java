package com.graphics.lib.canvas;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Observable;
import java.util.Observer;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collector;
import java.util.stream.Collectors;

import com.graphics.lib.Facet;
import com.graphics.lib.GeneralPredicates;
import com.graphics.lib.Point;
import com.graphics.lib.Utils;
import com.graphics.lib.Vector;
import com.graphics.lib.WorldCoord;
import com.graphics.lib.camera.Camera;
import com.graphics.lib.interfaces.ILightIntensityFinder;
import com.graphics.lib.interfaces.IPointFinder;
import com.graphics.lib.interfaces.IVertexNormalFinder;
import com.graphics.lib.transform.Transform;
import com.graphics.lib.transform.Translation;

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
public class CanvasObject extends Observable implements Observer{
	private static int nextId = 0;
	
	private BaseData data;
	private int objectId = nextId++;
	private Set<String> flags = new HashSet<String>();
	
	/**
	 * Get this object as the given class as long as that class is found in the wrapped object hierarchy
	 * 
	 * @param cl - The class to look for
	 * @return A canvas object as the given type or null if it cannot be found
	 */
	public <C extends CanvasObject> C getObjectAs(Class<C> cl)
	{		
		if (cl.isAssignableFrom(this.getClass())){
			return cl.cast(this);
		}else{
			CanvasObject wrapped = getWrappedObject();
			if (wrapped == null) return null;
			return wrapped.getObjectAs(cl);
		}
	}
	
	/**
	 * Get the object wrapped by this object, or this object if it isn't wrapped
	 * <br/>
	 * Wrapper objects must override this method so as not to return itself
	 * 
	 * @return The canvas object directly wrapped by this wrapper
	 */
	protected CanvasObject getWrappedObject()
	{
		return null;
	}
	
	/**
	 * Get the wrapped object at the root of the hierarchy, or this object if it isn't wrapped
	 * 
	 * @return The root canvas object wrapped by this wrapper
	 */
	public CanvasObject getBaseObject()
	{
		return getWrappedObject() == null ? this : getWrappedObject().getBaseObject();
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
	protected final BaseData getData()
	{
		if (data == null)
			data = new BaseData();
		return data;
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

	public final void addFlag(String flag)
	{
		this.flags.add(flag);
	}
	
	public final void removeFlag(String flag)
	{
		this.flags.remove(flag);
	}
	
	public final boolean hasFlag(String flag)
	{
		return this.flags.contains(flag);
	}
	
	public final List<CanvasObject> getChildren() {
		return getData().children;
	}

	public final boolean isVisible() {
		return getData().isVisible;
	}

	public final void setVisible(boolean isVisible) {
		getData().isVisible = isVisible;
		this.setChanged();
		this.notifyObservers();
	}

	public final boolean isDeleted() {
		return getData().isDeleted;
	}

	public final void setDeleted(boolean isDeleted) {
		getData().isDeleted = isDeleted;
		this.stopObserving();
		this.setChanged();
		this.notifyObservers();
	}

	public final boolean isObserving(){
		return getData().observing != null;
	}
	
	public final boolean getCastsShadow() {
		return getData().castsShadow;
	}

	public final void setCastsShadow(boolean castsShadow) {
		getData().castsShadow = castsShadow;
	}
	
	public final Color getColour() {
		return getData().colour;
	}

	public final void setColour(Color colour) {
		getData().colour = colour;
	}
	
	public final List<WorldCoord> getVertexList() {
		return getData().vertexList;
	}

	public final void setVertexList(List<WorldCoord> vertexList) {
		getData().vertexList = vertexList;
	}

	public final List<Facet> getFacetList() {
		return getData().facetList;
	}

	public final void setFacetList(List<Facet> facetList) {
		getData().facetList = facetList;
	}
	
	public final boolean isProcessBackfaces() {
		return getData().processBackfaces;
	}

	public final void setProcessBackfaces(boolean processBackfaces) {
		getData().processBackfaces = processBackfaces;
	}

	public final synchronized void cancelTransforms()
	{
		getData().transforms.forEach(t -> t.cancel());
	}
	
	public final synchronized void cancelNamedTransform(String key)
	{
		if (key == null) return;
		getData().transforms.stream().filter(t -> key.equals(t.getName())).forEach(t -> t.cancel());
	}
	
	public final Map<Point, ArrayList<Facet>> getVertexFacetMap() {
		return getData().vertexFacetMap;
	}

	/**
	 * Gets a list of transforms that are of the given type that are currently in the transform list
	 * 
	 * @see Transform
	 * 
	 * @param type - Class type of the transforms to retrieve
	 * @return List of transforms found
	 */
	public final <T> List<T> getTransformsOfType(Class<T> type)
	{

		List<T> mTrans = getData().transforms.stream().filter(t -> type.isAssignableFrom(t.getClass())).collect(Collector.of(
				ArrayList::new,
				(trans, orig) -> trans.add(type.cast(orig)),
				(left, right) -> {left.addAll(right); return left;},
				Collector.Characteristics.CONCURRENT
				));

		return mTrans;
	}
	
	public final synchronized void addTransform(Transform transform)
	{
		getData().transforms.add(transform);
	}
	
	/**
	 * Apply camera transforms so the object is represented as seen from the camera
	 * 
	 * @param transform
	 */
	public final void applyCameraTransform(Transform transform, Camera c)
	{
		transform.doTransform(getData().vertexList.stream().collect(Collector.of(
				ArrayList::new,
				(trans, world) -> trans.add(world.getTransformed(c)),
				(left, right) -> {left.addAll(right); return left;},
				Collector.Characteristics.CONCURRENT
				))
		);
	}
	
	public final boolean hasTransforms()
	{
		return getData().transforms.size() > 0;
	}
	
	public final boolean hasNamedTransform(String name){
		if (name == null) return false;
		return getData().transforms.stream().anyMatch(t -> name.equals(t.getName()));
	}
	
	/**
	 * Retrieve a named transform from the transform list if it exists
	 * 
	 * @param key - Name of the transform
	 * @return The transform found or null
	 */
	public final Transform getTransform(String key) {
		Optional<Transform> tr = getData().transforms.stream().filter(t -> t.getName() == key).findFirst();
		if (tr.isPresent()) return tr.get();
		return null;
	}
	
	/**
	 * Apply a given transform (e.g. Moving or rotating the object) immediately to this object
	 * 
	 * @see Transform
	 * 
	 * @param t - Transform to apply
	 */
	public final synchronized void applyTransform(Transform t)
	{
		//can be called directly for singular ad-hoc adjustments, without adding to the transform list, 
		//this should generally only be done on objects that haven't been registered to a canvas yet
		t.doTransform(getData().vertexList);
		this.setChanged();
		this.notifyObservers(t);
	}
	
	/**
	 * Apply the transforms currently held within the transform list to this object.
	 * <br/>
	 * Any transform that is complete will be removed from the list at this point
	 * 
	 */
	public final synchronized void applyTransforms()
	{
		if (getData().vertexList == null || getData().vertexList.size() == 0 || getData().isDeleted) return;
		
		this.beforeTransforms();
		getData().transforms.stream().filter(t -> !t.isCancelled()).forEach(t ->
		{
			applyTransform(t);
		});
		
		getData().transforms.removeIf(t -> {
								t.getDependencyList().removeIf(d -> d.isComplete());
								return t.isComplete() && t.getDependencyList().size() == 0;
								});

		if (getData().transforms.size() == 0 && getData().deleteAfterTransforms){
			if (getData().children.size() > 0) this.setVisible(false);
			else this.setDeleted(true);
		}
		
		this.afterTransforms();
	}
	
	
	public void afterTransforms(){
		if (this.getBaseObject() != this) this.getBaseObject().afterTransforms(); 
	}
	
	public void beforeTransforms(){
		if (this.getBaseObject() != this) this.getBaseObject().beforeTransforms(); 
	}
	/**
	 * Describes how to generate a normal vector for a Vertex 
	 * 
	 * @see IVertexNormalFinder
	 * 
	 * @return
	 */
	public IVertexNormalFinder getVertexNormalFinder()
	{
		if (this.getBaseObject() != this) return this.getBaseObject().getVertexNormalFinder(); //TODO will need to do this with all overridable methods if using wrappers - have only applied to those actually overridden in shapes for now
		return Utils.getVertexNormalFinder(getData().vnFinder);
	}
	
	/**
	 * Gets the centre point of this object
	 * 
	 * @see Point
	 * 
	 * @return Point representing the centre of this object
	 */
	public Point getCentre()
	{
		if (this.getBaseObject() != this) return this.getBaseObject().getCentre();
		
		//default, average of all un-tagged points
		Point pt = getData().vertexList.get(0);
		double maxX = pt.x;
		double maxY = pt.y;
		double maxZ = pt.z;
		double minX = pt.x;
		double minY = pt.y;
		double minZ = pt.z;
		for (Point p : getData().vertexList)
		{
			if (p.getTag().length() > 0) continue;
			if (p.x > maxX) maxX = p.x;
			if (p.x < minX) minX = p.x;
			if (p.y > maxY) maxY = p.y;
			if (p.y < minY) minY = p.y;
			if (p.z > maxZ) maxZ = p.z;
			if (p.z < minZ) minZ = p.z;
		};
		
		return new Point(minX + ((maxX - minX)/2), minY + ((maxY - minY)/2), minZ + ((maxZ - minZ)/2));
	}
	
	
	public final void addTransformAboutCentre(Transform...t)
	{
		this.addTransformAboutPoint(() -> this.getCentre(), t);
	}
	
	public final void addTransformAboutPoint(Point p, Transform...transform)
	{
		this.addTransformAboutPoint(() -> p, transform);
	}
	
	/**
	 * Add a transform sequence where the given point is moved to the origin, the required transforms applied, and then moved back to the original point
	 * 
	 * @param pFinder - Anonymous method that generates the point to transform about
	 * @param transform - List of transforms to apply
	 */
	public final void addTransformAboutPoint(IPointFinder pFinder, Transform...transform)
	{
		Translation temp = new Translation(){
			@Override
			public void beforeTransform(){
				Point p = pFinder.find();
				transX = -p.x;
				transY = -p.y;
				transZ = -p.z;
			}
		};
		
		Transform temp2 = new Translation(){
			@Override
			public void beforeTransform(){
				transX = -temp.transX;
				transY = -temp.transY;
				transZ = -temp.transZ;
			}
		};
		this.addTransform(temp);
		for (Transform t : transform)
		{
			temp2.addDependency(t);
			temp.addDependency(t);
			this.addTransform(t);
		}
		this.addTransform(temp2);
	}
	
	/**
	 * Sets a flag indicating if this object should be set as deleted once all its Transforms are complete
	 */
	public final void deleteAfterTransforms()
	{
		getData().deleteAfterTransforms = true;
	}
	
	/**
	 * Observe another object and match its movements, observed item will call update in this item via the Observer/Observable interface when it changes
	 * <br/>
	 * Note this object will be made a child of that it is observing so that it is always processed after the observed item
	 * 
	 * @param o
	 */
	public final void observeAndMatch (CanvasObject o){
		getData().observing = o;
		o.addObserver(this);
		o.getChildren().add(this);
	}
	
	public final void stopObserving(){
		if (getData().observing != null){
			getData().observing.deleteObserver(this);
			getData().observing.getChildren().remove(this);
			getData().observing = null;
		}
	}
	
	/**
	 * Tests whether a point is inside this object
	 * 
	 * @param p - Point to test
	 * @return <code>True</code> if point is inside object, <code>False</code> otherwise
	 */
	public boolean isPointInside(Point p)
	{
		if (this.getBaseObject() != this) return this.getBaseObject().isPointInside(p);
		//should do for most simple objects - can override it for something shape specific - e.g. it doesn't work for the whale, and shapes such as spheres can can work this out much quicker
		//checks if all facets appears as backfaces to the tested point
		for (Facet f : getData().facetList)
		{
			Vector vecPointToFacet = p.vectorToPoint(f.point1).getUnitVector();
			double deg = Math.toDegrees(Math.acos(vecPointToFacet.dotProduct(f.getNormal())));
			if (deg >= 90) return false;
		}
		return true;
		//Sub shapes???
	}
	
	/**
	 * Get the facet that a vector from a given point intersects.
	 * 
	 * @param start	- Start point of the vector
	 * @param v		- The vector
	 * @param getAny	- True if any intersected facet should be returned, false if the nearest should be returned
	 * @return		- The intersected facet or null if none is intersected
	 */
	public Facet getIntersectedFacet(Point start, Vector v, boolean getAny)
	{
		if (this.getBaseObject() != this) return this.getBaseObject().getIntersectedFacet(start, v, getAny);
		
		//get quick estimate for those that are nowhere near - check if hit sphere centred on centre point with diameter of largest dimension	
		if (!vectorIntersectsRoughly(start, v)) return null;
		
		Facet closest = null;
		double closestDist = 0;
		//the brute force generalised approach, check each front facing (to point) facet for intersection - will be horrible for something with a high facet count
		for (Facet f : this.getFacetList().stream().filter(GeneralPredicates.isFrontface(start)).collect(Collectors.toList()))
		{
			if (f.isPointWithin(f.getIntersectionPointWithFacetPlane(start, v))){
				if (getAny) return f;
			
				double dist = (start.distanceTo(f.point1) + start.distanceTo(f.point2) + start.distanceTo(f.point3)) / 3;
				if (closest == null || dist < closestDist){
					closest = f;
					closestDist = dist;
				}
			}
		}
		return closest;
	}
	
	/**
	 * Get list of all intersected facets that a vector from a given point intersects.
	 * 
	 * @param start	- Start point of the vector
	 * @param v		- The vector
	 * @return		- List of intersected facets
	 */
	public List<Facet> getIntersectedFacets(Point start, Vector v)
	{
		if (this.getBaseObject() != this) return this.getBaseObject().getIntersectedFacets(start, v);
		List<Facet> list = new ArrayList<Facet>();

		if (!vectorIntersectsRoughly(start, v)) return list;
		
		for (Facet f : this.getFacetList())
		{
			if (f.isPointWithin(f.getIntersectionPointWithFacetPlane(start, v, true))){
				list.add(f);
			}
		}
		return list;
	}
	
	/**
	 * Check if a vector intersects this object
	 * 
	 * @param start - Point vector originates from
	 * @param v		- The vector
	 * @return		- <code>True</code> if vector intersects sphere, <code>False</code> otherwise
	 */
	public boolean vectorIntersects(Point start, Vector v){
		if (this.getBaseObject() != this) return this.getBaseObject().vectorIntersects(start, v);
		
		return this.getIntersectedFacet(start, v, true) != null;
	}
	
	/**
	 * Check if hit sphere centred on centre point with diameter of largest dimension
	 * 
	 * @param start - Point vector originates from
	 * @param v		- The vector
	 * @return		- <code>True</code> if vector intersects sphere, <code>False</code> otherwise
	 */
	public final boolean vectorIntersectsRoughly(Point start, Vector v)
	{
		double dCentre = start.distanceTo(getCentre());
		double angle = Math.atan(this.getMaxExtent()/dCentre);
		Vector vCentre = start.vectorToPoint(getCentre()).getUnitVector();
		
		double vAngle = Math.acos(v.dotProduct(vCentre));
		
		if (vAngle >= angle) return false;
		
		return true;
	}
	
	/**
	 * Gets the maximum distance from the centre of an object to a vertex
	 *	<br/>
	 * The base implementation will do for most uniform shapes, irregular shapes will need to override this to be accurate
	 * 
	 * @return
	 */
	public double getMaxExtent()
	{
		if (this.getBaseObject() != this) return this.getBaseObject().getMaxExtent();
		return this.getCentre().distanceTo(this.getVertexList().get(0));
	}
	
	/**
	 * This method will be executed once all draw operations (across all objects) are complete
	 */
	public void onDrawComplete(){
		getData().children.removeIf(c -> c.isDeleted());
		if (this.getBaseObject() != this) this.getBaseObject().onDrawComplete();
	}
	
	public ILightIntensityFinder getLightIntensityFinder()
	{
		return getData().liFinder;
	}
	
	public void setLightIntensityFinder(ILightIntensityFinder liFinder)
	{
		getData().liFinder = liFinder;
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
		getData().vertexFacetMap = new HashMap<Point, ArrayList<Facet>>();
		
		for (Facet f : getData().facetList)
		{
			this.addPointFacetToMap(f.point1, f);
			this.addPointFacetToMap(f.point2, f);
			this.addPointFacetToMap(f.point3, f);
		}

	 	//remove anything where the facet group is highly divergent (will then use facet normal when shading)
	 	for (List<Facet> facetList : getData().vertexFacetMap.values())
	 	{
	 		boolean isDivergent = false;
	 		Vector normal = facetList.get(0).getNormal();

	 		isDivergent = facetList.stream().anyMatch(f -> {
	 			double answer = normal.dotProduct(f.getNormal());
	 			if (Math.toDegrees(Math.acos(answer)) > divergenceLimit) return true;
	 			return false;
	 		});
	 		
	 		if (isDivergent) facetList.clear();
	 	}

	}
	
	private void addPointFacetToMap(Point p, Facet f)
	{
		if (!getData().vertexFacetMap.containsKey(p)){
			getData().vertexFacetMap.put(p, new ArrayList<Facet>());
		}
		getData().vertexFacetMap.get(p).add(f);
	}

	@Override
	public synchronized void update(Observable arg0, Object arg1) {
		if (this != arg0 && arg1 instanceof Transform)
		{
			getData().vertexList.stream().forEach(p -> {
				((Transform) arg1).doTransformSpecific().accept(p);
			});
		}
		
	}
	
	public void setBaseIntensity(double intensity)
	{
		getData().facetList.forEach(f -> {
			f.setBaseIntensity(intensity);
		});
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
		CanvasObject other = (CanvasObject) obj;
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
		if (isDeleted() != other.isDeleted())
			return false;
		if (isVisible() != other.isVisible())
			return false;
		if (this.getVertexList() == null) {
			if (other.getVertexList() != null)
				return false;
		} else if (!this.getVertexList().equals(other.getVertexList()))
			return false;
		return true;
	}

	protected class BaseData
	{
		protected List<WorldCoord> vertexList = new ArrayList<WorldCoord>();
		protected List<Facet> facetList = new ArrayList<Facet>();
		protected Color colour = new Color(255, 0, 0);
		protected List<Transform> transforms = new ArrayList<Transform>();
		protected List<CanvasObject> children = Collections.synchronizedList(new ArrayList<CanvasObject>());
		protected Map<Point, ArrayList<Facet>> vertexFacetMap;
		protected boolean processBackfaces = false;
		protected boolean isVisible = true;
		protected boolean isDeleted = false;
		protected boolean castsShadow = true;
		//TODO - is solid or phased when invisible?
		protected boolean deleteAfterTransforms = false;
		protected CanvasObject observing = null;
		//public Utils.LightIntensityFinderEnum liFinder = Utils.LightIntensityFinderEnum.DEFAULT;
		public ILightIntensityFinder liFinder = Utils.getLightIntensityFinder(Utils.LightIntensityFinderEnum.DEFAULT);
		
		public Utils.VertexNormalFinderEnum vnFinder = Utils.VertexNormalFinderEnum.DEFAULT; 
		//TODO this class as POJO
	}
}
