package com.graphics.lib.canvas;

import java.awt.Graphics;
import java.util.Objects;
import javax.swing.JPanel;

import com.graphics.lib.camera.Camera;
import com.graphics.lib.zbuffer.IZBuffer;
import com.graphics.lib.zbuffer.ZBufferEnum;

public class AbstractCanvas extends JPanel {

    private static final long serialVersionUID = 1L;
    private transient IZBuffer zBuffer;
    private transient Camera camera;

    public AbstractCanvas(Camera camera) {
        super();
        this.camera = camera;
        this.setDoubleBuffered(false); 
        //turned off as we are effectively buffering in zbuffer as it is, may or may not offer a performance improvement
    }

    public Camera getCamera() {
        return camera;
    }

    public void setCamera(Camera camera) {
        this.camera = camera;
    }
    
    public void setzBuffer(IZBuffer zBuffer) {
    	this.zBuffer = zBuffer;
    }

    public IZBuffer getzBuffer() {
    	return zBuffer;
    }

    @Override
    public void setVisible(boolean flag)
    {
        super.setVisible(flag);
        getCamera().setViewport(this.getWidth(), this.getHeight());
    }
    
    @Override
    public void paintComponent(Graphics g) {
    	if (Objects.nonNull(this.zBuffer)) {
    		super.paintComponent(g);
    
    		g.drawImage(this.zBuffer.getBuffer(), 0, 0, null);
    	}
    }
    
    protected void prepareZBuffer() {
        if (Objects.isNull(getzBuffer())) {
            this.setzBuffer(ZBufferEnum.DEFAULT.get());
        } else {
            getzBuffer().clear();
        }
        
        getzBuffer().setDimensions(this.getWidth(), this.getHeight());
    }

}