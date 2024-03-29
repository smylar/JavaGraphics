package com.graphics.lib.canvas;

import java.io.Serial;
import java.util.HashSet;
import javax.swing.SwingUtilities;

import com.graphics.lib.camera.Camera;
import com.graphics.lib.interfaces.ICanvasObject;
import com.graphics.lib.interfaces.ISecondaryCamera;
import com.graphics.lib.scene.SceneMap;
import com.graphics.lib.scene.SceneWithOffset;
import com.graphics.lib.shader.IShaderFactory;

/**
 * Provides another view of the same scene in the parent
 * (This sometimes seems to paint double on start, not sure why yet)
 * @author paul
 *
 */
public class SlaveCanvas3D extends AbstractCanvas implements ISecondaryCamera {
	@Serial
	private static final long serialVersionUID = 1L;
	private SceneWithOffset scene;
	
	public SlaveCanvas3D(Camera camera)
	{
		super(camera);	
	}

	private void processShape(Canvas3D source, ICanvasObject obj, IShaderFactory shader)
	{
		if (obj.isVisible()) {
			getCamera().getView(obj);

			getzBuffer().add(obj, shader, getCamera(), source.getHorizon(), source.getLightSources(scene.scene()));
		}

		for (ICanvasObject child : new HashSet<>(obj.getChildren())) {
			processShape(source, child, shader);
		}
	}
	
	@Override
    public void update(Canvas3D source, CanvasEvent event, ICanvasObject obj) {
        if (event.equals(CanvasEvent.PAINT)) {
            getzBuffer().refreshBuffer();
            SwingUtilities.invokeLater(this::repaint);
        }
        else if (event.equals(CanvasEvent.PREPARE_BUFFER)) {
            prepareZBuffer();
        }
        else if (scene.scene().getFrameObjects().stream().anyMatch(o -> o.object() == obj) || source.isUnbound(obj)) {
            processShape(source, obj, source.getShader(obj, getCamera()));
        }
    }

	@Override
	public SceneWithOffset getAndSetRelevantFrame(SceneMap sceneMap) {
		scene = sceneMap.getFrameFromPoint(getCamera().getPosition());
		return scene;
	}
}
