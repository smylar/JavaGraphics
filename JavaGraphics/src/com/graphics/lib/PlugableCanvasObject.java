package com.graphics.lib;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.graphics.lib.plugins.IPlugin;


public class PlugableCanvasObject<T extends CanvasObject> extends CanvasObject {
	private T wrappedObject;
	private Map<String,IPlugin<PlugableCanvasObject<?>,?>> plugins = new HashMap<String,IPlugin<PlugableCanvasObject<?>,?>>();
	private List<String> afterDrawPlugins = new ArrayList<String>();
	private List<String> singleAfterDrawPlugins = new ArrayList<String>();
	
	public PlugableCanvasObject(T obj)
	{
		wrappedObject = obj;
		this.setData(obj.getData());
	}
	
	@Override
	protected T getWrappedObject()
	{
		return wrappedObject;
	}
	
	@Override
	protected CanvasObject getBaseObject()
	{
		return wrappedObject.getBaseObject();
	}
	
	@Override
	public void onDrawComplete()
	{
		wrappedObject.onDrawComplete();
		
		List<String> pluginList = new ArrayList<String>(this.afterDrawPlugins);
		for (String key : pluginList)
		{
			this.executePlugin(key);
		}
		
		pluginList = new ArrayList<String>(this.singleAfterDrawPlugins);
		for (String key : pluginList)
		{
			this.executePlugin(key);
		}
		this.singleAfterDrawPlugins.clear();
		
		List<CanvasObject> children = new ArrayList<CanvasObject>(this.getChildren());
		for (CanvasObject child : children)
		{
			child.onDrawComplete();
		}
	}
	
	public void registerSingleAfterDrawPlugin(String key, IPlugin<PlugableCanvasObject<?>,?> plugin)
	{
		plugins.put(key, plugin);
		singleAfterDrawPlugins.add(key);
	}
	
	public void registerPlugin(String key, IPlugin<PlugableCanvasObject<?>,?> plugin, boolean doAfterDraw)
	{
		plugins.put(key, plugin);
		if (doAfterDraw) afterDrawPlugins.add(key);
	}
	
	public void removePlugin(String key)
	{
		this.plugins.remove(key);
		this.afterDrawPlugins.remove(key);
		this.singleAfterDrawPlugins.remove(key);
	}
	
	public void removePlugins()
	{
		this.plugins.clear();
		this.afterDrawPlugins.clear();
		this.singleAfterDrawPlugins.clear();
	}
	
	public Object executePlugin(String key)
	{
		if (this.plugins.containsKey(key) && !this.isDeleted())
		{
			return this.plugins.get(key).execute(this);
		}
		return null;
	}
}
