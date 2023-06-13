package com.graphics.lib.canvas;

import java.util.HashSet;
import javax.swing.SwingUtilities;

import com.graphics.lib.camera.Camera;
import com.graphics.lib.interfaces.ICanvasObject;
import com.graphics.lib.interfaces.ICanvasUpdateListener;
import com.graphics.lib.shader.IShaderFactory;

/**
 * Provides another view of the same scene in the parent
 * 
 * (This sometimes seems to paint double on start, not sure why yet)
 * @author paul
 *
 */
public class SlaveCanvas3D extends AbstractCanvas implements ICanvasUpdateListener {
	private static final long serialVersionUID = 1L;
	
	public SlaveCanvas3D(Camera camera)
	{
		super(camera);	
	}

	private void processShape(Canvas3D source, ICanvasObject obj, IShaderFactory shader)
	{
		
		if (obj.isVisible())
		{
			this.getCamera().getView(obj);
			
			this.getzBuffer().add(obj, shader, this.getCamera(), source.getHorizon());
		}
		
		for (ICanvasObject child : new HashSet<>(obj.getChildren()))
		{
			this.processShape(source, child, shader);
		}
	}
	
	@Override
    public void update(Canvas3D source, CanvasEvent event, ICanvasObject obj) {
        if (event.equals(CanvasEvent.PAINT)) {
            this.getzBuffer().refreshBuffer();
            SwingUtilities.invokeLater(this::repaint);
        }
        else if (event.equals(CanvasEvent.PREPARE_BUFFER)) {
            prepareZBuffer();
        }
        else {
            this.processShape(source, obj, source.getShader(obj, getCamera()));
        }
    }
}
