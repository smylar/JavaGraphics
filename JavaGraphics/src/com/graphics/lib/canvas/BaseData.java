package com.graphics.lib.canvas;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.graphics.lib.Facet;
import com.graphics.lib.LightIntensityFinderEnum;
import com.graphics.lib.Point;
import com.graphics.lib.VertexNormalFinderEnum;
import com.graphics.lib.WorldCoord;
import com.graphics.lib.interfaces.ICanvasObject;
import com.graphics.lib.interfaces.ILightIntensityFinder;
import com.graphics.lib.interfaces.IVertexNormalFinder;
import com.graphics.lib.transform.Transform;

public class BaseData {
    private String objTag;
    private List<WorldCoord> vertexList = new ArrayList<>();
    private List<Facet> facetList = new ArrayList<>();
    private Color colour = new Color(255, 0, 0);
    private List<Transform> transforms = new ArrayList<>();
    private List<CanvasObject> children = Collections.synchronizedList(new ArrayList<CanvasObject>());
    private Map<Point, ArrayList<Facet>> vertexFacetMap;
    private boolean processBackfaces = false;
    private boolean isVisible = true;
    private boolean isDeleted = false;
    private boolean castsShadow = true;
    //TODO - is solid or phased when invisible?
    private boolean deleteAfterTransforms = false;
    private ICanvasObject observing = null;
    private Point anchorPoint = null;
    private Set<String> flags = new HashSet<>();
    private CanvasObjectFunctionsImpl functions = CanvasObjectFunctions.DEFAULT.get();
    private ILightIntensityFinder liFinder = LightIntensityFinderEnum.DEFAULT.get();    
    private IVertexNormalFinder vnFinder = VertexNormalFinderEnum.DEFAULT.get(); 
    
    public BaseData(String objTag) {
        this.objTag = objTag;
    }

    public String getObjTag() {
        return objTag;
    }

    public void setObjTag(String objTag) {
        this.objTag = objTag;
    }

    public List<WorldCoord> getVertexList() {
        return vertexList;
    }

    public void setVertexList(List<WorldCoord> vertexList) {
        this.vertexList = vertexList;
    }

    public List<Facet> getFacetList() {
        return facetList;
    }

    public void setFacetList(List<Facet> facetList) {
        this.facetList = facetList;
    }

    public Color getColour() {
        return colour;
    }

    public void setColour(Color colour) {
        this.colour = colour;
    }

    public List<Transform> getTransforms() {
        return transforms;
    }

    public void setTransforms(List<Transform> transforms) {
        this.transforms = transforms;
    }

    public List<CanvasObject> getChildren() {
        return children;
    }

    public void setChildren(List<CanvasObject> children) {
        this.children = children;
    }

    public Map<Point, ArrayList<Facet>> getVertexFacetMap() {
        return vertexFacetMap;
    }

    public void setVertexFacetMap(Map<Point, ArrayList<Facet>> vertexFacetMap) {
        this.vertexFacetMap = vertexFacetMap;
    }

    public boolean isProcessBackfaces() {
        return processBackfaces;
    }

    public void setProcessBackfaces(boolean processBackfaces) {
        this.processBackfaces = processBackfaces;
    }

    public boolean isVisible() {
        return isVisible;
    }

    public void setVisible(boolean isVisible) {
        this.isVisible = isVisible;
    }

    public boolean isDeleted() {
        return isDeleted;
    }

    public void setDeleted(boolean isDeleted) {
        this.isDeleted = isDeleted;
    }

    public boolean isCastsShadow() {
        return castsShadow;
    }

    public void setCastsShadow(boolean castsShadow) {
        this.castsShadow = castsShadow;
    }

    public boolean isDeleteAfterTransforms() {
        return deleteAfterTransforms;
    }

    public void setDeleteAfterTransforms(boolean deleteAfterTransforms) {
        this.deleteAfterTransforms = deleteAfterTransforms;
    }

    public ICanvasObject getObserving() {
        return observing;
    }

    public void setObserving(ICanvasObject observing) {
        this.observing = observing;
    }

    public Point getAnchorPoint() {
        return anchorPoint;
    }

    public void setAnchorPoint(Point anchorPoint) {
        this.anchorPoint = anchorPoint;
    }

    public Set<String> getFlags() {
        return flags;
    }

    public void setFlags(Set<String> flags) {
        this.flags = flags;
    }

    public CanvasObjectFunctionsImpl getFunctions() {
        return functions;
    }

    public void setFunctions(CanvasObjectFunctionsImpl functions) {
        this.functions = functions;
    }

    public ILightIntensityFinder getLiFinder() {
        return liFinder;
    }

    public void setLiFinder(ILightIntensityFinder liFinder) {
        this.liFinder = liFinder;
    }

    public IVertexNormalFinder getVnFinder() {
        return vnFinder;
    }

    public void setVnFinder(IVertexNormalFinder vnFinder) {
        this.vnFinder = vnFinder;
    }
    
    
}
