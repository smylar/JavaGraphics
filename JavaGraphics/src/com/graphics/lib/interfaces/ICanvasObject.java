package com.graphics.lib.interfaces;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Observer;
import java.util.Optional;

import com.graphics.lib.Facet;
import com.graphics.lib.Point;
import com.graphics.lib.WorldCoord;
import com.graphics.lib.camera.Camera;
import com.graphics.lib.canvas.BaseData;
import com.graphics.lib.canvas.CanvasObject;
import com.graphics.lib.canvas.CanvasObjectFunctionsImpl;
import com.graphics.lib.transform.Transform;

/**
 * Currently a straight extract from CanvasObject, not all of these will be needed here, will cut down evetually
 * @author paul
 *
 */
public interface ICanvasObject {

	/**
	 * Get this object as the given class as long as that class is found in the wrapped object hierarchy
	 * 
	 * @param cl - The class to look for
	 * @return A canvas object as the given type or null if it cannot be found
	 */
	<C extends ICanvasObject> Optional<C> getObjectAs(Class<C> cl);

	/**
	 * Get the wrapped object at the root of the hierarchy, or this object if it isn't wrapped
	 * 
	 * @return The root canvas object wrapped by this wrapper
	 */
	ICanvasObject getBaseObject();

	String getObjectTag();

	void setAnchorPoint(Point p);

	Point getAnchorPoint();

	boolean hasFlag(String flag);

	List<CanvasObject> getChildren();

	boolean isVisible();

	void setVisible(boolean isVisible);

	boolean isDeleted();

	void setDeleted(boolean isDeleted);

	boolean getCastsShadow();

	Color getColour();

	void setColour(Color colour);

	List<WorldCoord> getVertexList();

	void setVertexList(List<WorldCoord> vertexList);

	List<Facet> getFacetList();

	void setFacetList(List<Facet> facetList);

	boolean isProcessBackfaces();

	void setProcessBackfaces(boolean processBackfaces);

	void cancelTransforms();

	void cancelNamedTransform(String key);

	Map<Point, ArrayList<Facet>> getVertexFacetMap();

	/**
	 * Gets a list of transforms that are of the given type that are currently in the transform list
	 * 
	 * @see Transform
	 * 
	 * @param type - Class type of the transforms to retrieve
	 * @return List of transforms found
	 */
	<T> List<T> getTransformsOfType(Class<T> type);

	void addTransform(Transform transform);

	/**
	 * Apply camera transforms so the object is represented as seen from the camera
	 * 
	 * @param transform
	 */
	void applyCameraTransform(Transform transform, Camera c);

	/**
	 * Check if any transforms are present in the objects transform list
	 * @see Transform
	 * 
	 * @return <code>True</code> if transform(s) present, <code>False</code> otherwise
	 */
	boolean hasTransforms();

	/**
	 * Check if any transforms with a specified name are present in the objects transform list
	 * @see Transform
	 * 
	 * @return <code>True</code> if transform(s) present, <code>False</code> otherwise
	 */
	boolean hasNamedTransform(String name);

	/**
	 * Retrieve a named transform from the transform list if it exists
	 * 
	 * @param key - Name of the transform
	 * @return The transform found or null
	 */
	Transform getTransform(String key);

	/**
	 * Apply a given transform (e.g. Moving or rotating the object) immediately to this object
	 * 
	 * @see Transform
	 * 
	 * @param t - Transform to apply
	 */
	void applyTransform(Transform t);

	/**
	 * Apply the transforms currently held within the transform list to this object.
	 * <br/>
	 * Any transform that is complete will be removed from the list at this point
	 * 
	 */
	void applyTransforms();

	void afterTransforms();

	void beforeTransforms();

	/**
	 * Describes how to generate a normal vector for a Vertex 
	 * 
	 * @see IVertexNormalFinder
	 * 
	 * @return
	 */
	IVertexNormalFinder getVertexNormalFinder();
	
	void setVertexNormalFinder(IVertexNormalFinder finder);

	/**
	 * Gets the centre point of this object
	 * 
	 * @see Point
	 * 
	 * @return Point representing the centre of this object
	 */
	Point getCentre();

	/**
	 * Sets a flag indicating if this object should be set as deleted once all its Transforms are complete
	 */
	void deleteAfterTransforms();

	/**
	 * This method will be executed once all draw operations (across all objects) are complete
	 */
	void onDrawComplete();

	ILightIntensityFinder getLightIntensityFinder();

	void setLightIntensityFinder(ILightIntensityFinder liFinder);

	void setBaseIntensity(double intensity);

	CanvasObjectFunctionsImpl getFunctions();
	
	void addObserver(Observer o);
	
	void deleteObserver(Observer o);

    BaseData getData();

    void addFlag(String flag);

    void removeFlag(String flag);

}