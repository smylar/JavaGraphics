package com.graphics.lib.interfaces;

import com.graphics.lib.plugins.IPlugin;

public interface IPlugable extends ICanvasObject {

	void registerSingleAfterDrawPlugin(String key,
			IPlugin<IPlugable, ?> plugin);

	void registerPlugin(String key, IPlugin<IPlugable, ?> plugin, boolean doAfterDraw);

	void removePlugin(String key);

	void removePlugins();

	Object executePlugin(String key);

}