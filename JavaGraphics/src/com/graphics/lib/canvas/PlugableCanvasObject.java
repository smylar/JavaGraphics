package com.graphics.lib.canvas;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.graphics.lib.plugins.IPlugin;

/**
 * Wrapper for a canvas object providing plugin functionality, i.e. Anonymous functions can be registered with the object 
 * to be triggered at the end of each draw cycle, or in response to other events
 * 
 * @author Paul Brandon
 *
 * @param <T> Type of the CanvasObject being wrapped
 */
public class PlugableCanvasObject<T extends CanvasObject> extends CanvasObjectWrapper<T> {
	private Map<String,IPlugin<PlugableCanvasObject<?>,?>> plugins = new HashMap<String,IPlugin<PlugableCanvasObject<?>,?>>();
	private List<String> afterDrawPlugins = new ArrayList<String>();
	private List<String> singleAfterDrawPlugins = new ArrayList<String>();
	
	public PlugableCanvasObject() {
		super();
	}
	
	public PlugableCanvasObject(T obj) {
		super(obj);
	}

	@Override
	public void onDrawComplete()
	{
		if (this.getWrappedObject() != null) this.getWrappedObject().onDrawComplete();
		else super.onDrawComplete();
		
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
