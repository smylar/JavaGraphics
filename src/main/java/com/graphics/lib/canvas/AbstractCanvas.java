package com.graphics.lib.canvas;

import java.awt.Graphics;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.BiConsumer;

import javax.swing.JPanel;

import com.graphics.lib.ZBufferEnum;
import com.graphics.lib.camera.Camera;
import com.graphics.lib.interfaces.IZBuffer;

public class AbstractCanvas extends JPanel {

    private static final long serialVersionUID = 1L;
    private IZBuffer zBuffer;
    private List<BiConsumer<AbstractCanvas,Graphics>> drawPlugins = new ArrayList<>();
    private Camera camera;

    public AbstractCanvas(Camera camera) {
        super();
        this.camera = camera;
    }
    
    /**
     * Allows anonymous functions to be registered in order to customise the display
     * 
     * @param operation A paint operation
     */
    public void addDrawOperation(BiConsumer<AbstractCanvas,Graphics> operation) {
    	drawPlugins.add(operation);
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
    
    		drawPlugins.forEach(op -> op.accept(this, g));
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