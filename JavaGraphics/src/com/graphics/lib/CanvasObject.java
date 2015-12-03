package com.graphics.lib;

import java.awt.Color;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Observable;
import java.util.Observer;
import java.util.Optional;
import java.util.stream.Collector;

import com.graphics.lib.interfaces.ILightIntensityFinder;
import com.graphics.lib.interfaces.IPointFinder;
import com.graphics.lib.interfaces.IVertexNormalFinder;
import com.graphics.lib.lightsource.LightSource;
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
 *  This would be done as follows:
 *  <br/>
 *  <code>PlugableCanvasObject<OrientableCanvasObject<?>> obj = new PlugableCanvasObject<OrientableCanvasObject<?>>(new OrientableCanvasObject<Ovoid>(new Sphere(20,30)));</code>
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
	
	/**
	 * Get this object as the given class as long as that class is found in the wrapped object hierarchy
	 * 
	 * @param cl - The class to look for
	 * @return A canvas object as the given type or null if it cannot be found
	 */
	public <C extends CanvasObject> C getObjectAs(Class<C> cl)
	{		
		if (this.getClass().isAssignableFrom(cl)){
			return cl.cast(this);
		}else{
			CanvasObject wrapped = getWrappedObject();
			if (wrapped == this) return null;
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
		return this;
	}
	
	/**
	 * Get the wrapped object at the root of the hierarchy, or this object if it isn't wrapped
	 * <br/>
	 * Wrapper objects must override this method so as not to return itself
	 * 
	 * @return The root canvas object wrapped by this wrapper
	 */
	protected CanvasObject getBaseObject()
	{
		return this;
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
	protected BaseData getData()
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
	protected void setData(BaseData data)
	{
		this.data = data;
	}
	
	public List<CanvasObject> getChildren() {
		getData().children.removeIf(c -> c.isDeleted());
		return getData().children;
	}

	public boolean isVisible() {
		return getData().isVisible;
	}

	public void setVisible(boolean isVisible) {
		this.setChanged();
		this.notifyObservers();
		getData().isVisible = isVisible;
	}

	public boolean isDeleted() {
		return getData().isDeleted;
	}

	public void setDeleted(boolean isDeleted) {
		getData().isDeleted = isDeleted;
		this.setChanged();
		this.notifyObservers();
	}

	public Color getColour() {
		return getData().colour;
	}

	public void setColour(Color colour) {
		getData().colour = colour;
	}
	
	public List<WorldCoord> getVertexList() {
		return getData().vertexList;
	}

	public void setVertexList(List<WorldCoord> vertexList) {
		getData().vertexList = vertexList;
	}

	public List<Facet> getFacetList() {
		return getData().facetList;
	}

	public void setFacetList(List<Facet> facetList) {
		getData().facetList = facetList;
	}
	
	public boolean isProcessBackfaces() {
		return getData().processBackfaces;
	}

	public void setProcessBackfaces(boolean processBackfaces) {
		getData().processBackfaces = processBackfaces;
	}

	public synchronized void cancelTransforms()
	{
		getData().transforms.forEach(t -> t.cancel());
	}
	
	public Map<Point, ArrayList<Facet>> getVertexFacetMap() {
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
	public <T> List<T> getTransformsOfType(Class<T> type)
	{

		List<T> mTrans = getData().transforms.stream().filter(t -> t.getClass() == type).collect(Collector.of(
				ArrayList::new,
				(trans, orig) -> trans.add(type.cast(orig)),
				(left, right) -> {left.addAll(right); return left;},
				Collector.Characteristics.CONCURRENT
				));

		return mTrans;
	}
	
	public synchronized void addTransform(Transform transform)
	{
		getData().transforms.add(transform);
	}
	
	/**
	 * Apply camera transforms so the object is represented as seen from the camera
	 * 
	 * @param transform
	 */
	public void applyCameraTransform(Transform transform)
	{
		transform.doTransform(getData().vertexList.stream().collect(Collector.of(
				ArrayList::new,
				(trans, world) -> trans.add(world.getTransformed()),
				(left, right) -> {left.addAll(right); return left;},
				Collector.Characteristics.CONCURRENT
				))
		);
	}
	
	public boolean hasTransforms()
	{
		return getData().transforms.size() > 0;
	}
	
	/**
	 * Retrieve a named transform from the transform list if it exists
	 * 
	 * @param key - Name of the transform
	 * @return The transform found or null
	 */
	public Transform getTransform(String key) {
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
	public synchronized void applyTransform(Transform t)
	{
		//can be called directly for singular ad-hoc adjustments, without adding to the transform list
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
	public synchronized void applyTransforms()
	{
		if (getData().vertexList == null || getData().vertexList.size() == 0 || getData().isDeleted) return;
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
	
	
	public void addTransformAboutCentre(Transform...t)
	{
		this.addTransformAboutPoint(() -> this.getCentre(), t);
	}
	
	public void addTransformAboutPoint(Point p, Transform...transform)
	{
		this.addTransformAboutPoint(() -> p, transform);
	}
	
	/**
	 * Add a transform sequence where the given point is moved to the origin, the required transforms applied, and then moved back to the original point
	 * 
	 * @param pFinder - Anonymous method that generates the point to transform about
	 * @param transform - List of transforms to apply
	 */
	public void addTransformAboutPoint(IPointFinder pFinder, Transform...transform)
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
	public void deleteAfterTransforms()
	{
		getData().deleteAfterTransforms = true;
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
		//should do for most simple objects - can override it for something shape specific
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
	 * This method will be executed once all draw operations (across all objects) are complete
	 */
	public void onDrawComplete(){
		if (this.getBaseObject() != this) this.getBaseObject().onDrawComplete();
	}
	
	public ILightIntensityFinder getLightIntensityFinder(List<LightSource> ls)
	{
		return Utils.getLightIntensityFinder(ls, getData().liFinder);
	}
	
	/**
	 * On calling this method, a map will be generated with the normals for each vertex as the average of the normal vector of the facets that share this vertex
	 * <br/>
	 * Depending upon the vertex normal finder being used, this map will then be used to find vertex normals
	 * 
	 * @param divergenceLimit - If the angle between two facets is larger than this value, than a value will not be mapped
	 */
	protected void UseAveragedNormals(int divergenceLimit)
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
	public void update(Observable arg0, Object arg1) {
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
		protected List<CanvasObject> children = new ArrayList<CanvasObject>();
		protected Map<Point, ArrayList<Facet>> vertexFacetMap;
		protected boolean processBackfaces = false;
		protected boolean isVisible = true;
		protected boolean isDeleted = false;
		//TODO - is solid or phased when invisible?
		protected boolean deleteAfterTransforms = false;
		public Utils.LightIntensityFinderEnum liFinder = Utils.LightIntensityFinderEnum.DEFAULT;
		public Utils.VertexNormalFinderEnum vnFinder = Utils.VertexNormalFinderEnum.DEFAULT; //TODO this class as POJO
	}
}
