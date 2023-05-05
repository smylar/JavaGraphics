package com.graphics.lib.texture;

import java.util.HashMap;
import java.util.Map;

import com.graphics.lib.Point;
import com.graphics.lib.WorldCoord;
import com.graphics.shapes.Ovoid;

public class OvoidTextureMapper extends TextureMapper<Ovoid> {

	public OvoidTextureMapper() {
		super(Ovoid.class);
	}

	@Override
	protected Map<WorldCoord, Point> mapImpl(Ovoid obj, Texture texture) {

		Map<WorldCoord, Point> textureMap = new HashMap<>();
		//apply to half spheres only because of problem of start and end point being the same, TODO add option to apply on either half
		int angleProgression = obj.getAngleProgression();
		int pointsarc = 180 / angleProgression;
		int points = 360 / angleProgression;
		double maxLength = Math.PI * obj.getRadius();
		
		for (int i = 0 ; i < pointsarc-1 ; i++){
			double curAngle = (i+1d) * angleProgression;
			double circleRad = Math.sin(Math.toRadians(curAngle)) * obj.getRadius();
			if (circleRad < 0) {
			    circleRad = circleRad * -1 ;
			}
			double length = Math.PI * circleRad;
			
			double teY = texture.getHeight() - (texture.getHeight() * (curAngle / 180));
			double teWidth = length * (texture.getWidth() / maxLength);
			double teIncr = teWidth / pointsarc;
			double teX = (texture.getWidth() - teWidth) / 2;		

			for (int j = 0 ; j < pointsarc+1 ; j++){
				int index = (i * points) + j;
				textureMap.put(obj.getVertexList().get(index), new Point(teX, teY, 0));
				teX += teIncr;
			}
		}
		return textureMap;
	}

}
