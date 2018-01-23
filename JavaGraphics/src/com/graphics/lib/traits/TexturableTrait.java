package com.graphics.lib.traits;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Observable;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import com.graphics.lib.Point;
import com.graphics.lib.WorldCoord;
import com.graphics.lib.interfaces.ICanvasObject;
import com.graphics.lib.interfaces.ITexturable;
import com.graphics.lib.texture.Texture;
import com.graphics.lib.texture.TextureMapper;

/**
 * Allows a texture to be applied to an object
 * 
 * @author paul
 *
 */
public class TexturableTrait implements ITexturable {
	//note currently can't apply textures on a per facet (or set of facets) basis, though may be able to achieve that with the mapper used
	private Map<Texture, Map<WorldCoord, Point>> textureMap = new HashMap<>();
	private ICanvasObject parent;
	
	@Override
	public ITexturable addTexture(Texture texture) {
		textureMap.put(texture, new HashMap<>());
		return this;
	}
	
	@Override
	public ITexturable mapTexture(TextureMapper<?> mapper) {
		textureMap.entrySet().forEach(e -> mapper.map(parent, e.getValue(), e.getKey()));
		return this;
	}
	
	@Override
	public Set<Texture> getTextures() {
		return textureMap.keySet();
	}
	
	@Override
	public Optional<Point> getTextureCoord(Texture texture, WorldCoord vertex) {
		if (textureMap.containsKey(texture)) {
			return Optional.ofNullable(textureMap.get(texture).get(vertex));
		}
		
		return Optional.empty();
	}

	@Override
	public Set<Texture> getTextures(final WorldCoord vertex) {
	    return textureMap.entrySet().stream()
	                                .filter(entry -> entry.getValue().containsKey(vertex))
	                                .map(Entry::getKey)
	                                .collect(Collectors.toSet());
	}

    @Override
    public void setParent(ICanvasObject parent) {
        this.parent = parent;
    }

    @Override
    public void update(Observable o, Object arg) {
        // not required
        
    }

}
