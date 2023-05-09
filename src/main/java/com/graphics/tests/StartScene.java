package com.graphics.tests;

import java.awt.Color;
import java.awt.event.KeyEvent;
import java.util.Optional;

import com.graphics.lib.Axis;
import com.graphics.lib.Point;
import com.graphics.lib.Vector;
import com.graphics.lib.canvas.CanvasObject;
import com.graphics.lib.canvas.CanvasObjectFunctions;
import com.graphics.lib.interfaces.IOrientable;
import com.graphics.lib.interfaces.IPlugable;
import com.graphics.lib.lightsource.DirectionalLightSource;
import com.graphics.lib.lightsource.LightSource;
import com.graphics.lib.lightsource.ObjectTiedLightSource;
import com.graphics.lib.orientation.SimpleOrientation;
import com.graphics.lib.plugins.Events;
import com.graphics.lib.plugins.IPlugin;
import com.graphics.lib.scene.FlooredFrame;
import com.graphics.lib.scene.SceneObject;
import com.graphics.lib.shader.ScanlineShaderFactory;
import com.graphics.lib.texture.BmpTexture;
import com.graphics.lib.texture.OvoidTextureMapper;
import com.graphics.lib.traits.OrientableTrait;
import com.graphics.lib.traits.PlugableTrait;
import com.graphics.lib.traits.TexturableTrait;
import com.graphics.lib.traits.TraitHandler;
import com.graphics.lib.transform.MovementTransform;
import com.graphics.lib.transform.RepeatingTransform;
import com.graphics.lib.transform.ScaleTransform;
import com.graphics.lib.transform.SequenceTransform;
import com.graphics.lib.transform.Transform;
import com.graphics.shapes.Cuboid;
import com.graphics.shapes.Lantern;
import com.graphics.shapes.Sphere;
import com.graphics.shapes.Whale;
import com.graphics.tests.shapes.FlapTest;
import com.graphics.tests.shapes.Gate;
import com.graphics.tests.shapes.TexturedCuboid;
import com.graphics.tests.weapons.AmmoTracker;
import com.sound.ClipLibrary;

public class StartScene extends FlooredFrame {

    private ObjectTiedLightSource<DirectionalLightSource> l3;
    private Lantern lantern3;
    
    public StartScene(Color floorColour, double floorLevel) {
        super(floorColour, floorLevel);
    }
    
    @Override
    public void buildFrame() {
        if (isLoaded()) return;
        
        super.buildFrame();
        ObjectTiedLightSource<LightSource> l1 = new ObjectTiedLightSource<>(LightSource.class, 0,0,-500);
        l1.getLightSource().setColour(new Color(255, 0, 0));
        addSceneLightSource(l1.getLightSource());
        Lantern lantern1 = new Lantern();
        lantern1.attachLightsource(l1);
        addSceneObject(new SceneObject(lantern1, new Point(0,0,-500), ScanlineShaderFactory.NONE));
        
        ObjectTiedLightSource<LightSource> l2 = new ObjectTiedLightSource<>(LightSource.class, 500,200,-100);
        l2.getLightSource().setColour(new Color(0, 255, 0));
        addSceneLightSource(l2.getLightSource());
        Lantern lantern2 = new Lantern();
        lantern2.attachLightsource(l2);
        addSceneObject(new SceneObject(lantern2, new Point(500,200,-100), ScanlineShaderFactory.NONE));
        
        l3 = new ObjectTiedLightSource<>(DirectionalLightSource.class, 400,100,100);
        l3.getLightSource().setColour(new Color(0, 0, 255));
        addSceneLightSource(l3.getLightSource());
        
        lantern3 = new Lantern();        
        TraitHandler.INSTANCE.registerTrait(lantern3, OrientableTrait.class).setOrientation(new SimpleOrientation(OrientableTrait.ORIENTATION_TAG));
        addSceneObject(new SceneObject(lantern3, new Point(400,100,100), ScanlineShaderFactory.NONE));
        l3.getLightSource().setDirection(() -> TraitHandler.INSTANCE.getTrait(lantern3, IOrientable.class).get().getOrientation().getForward());
        l3.getLightSource().setLightConeAngle(40);
        lantern3.attachLightsource(l3);
        lantern3.getFacetList().stream().filter(f -> f.getNormal().z() <= 0).forEach(f -> f.setColour(Color.BLACK));
        Transform l3spin = new RepeatingTransform<>(Axis.X.getRotation(4), 0);
        CanvasObjectFunctions.DEFAULT.get().addTransformAboutCentre(lantern3, l3spin);
        
        CanvasObject camcube = new Cuboid(20,20,20);
        addSceneObject(new SceneObject(camcube, new Point(1560, 200, 350), ScanlineShaderFactory.FLAT));
        
        IPlugin<IPlugable, Void> explode = TestUtils.getExplodePlugin(Optional.ofNullable(ClipLibrary.getInstance()));
        
        Whale whale = new Whale(); 
        TraitHandler.INSTANCE.registerTrait(whale, PlugableTrait.class).registerPlugin(Events.EXPLODE, explode, false);
        whale.setColour(Color.cyan);
        addSceneObject(new SceneObject(whale, new Point(1515, 300, 400), ScanlineShaderFactory.GORAUD));
        
        FlapTest flap = new FlapTest(); 
        flap.setColour(Color.ORANGE);
        addSceneObject(new SceneObject(flap, new Point(1000, 500, 200), ScanlineShaderFactory.GORAUD));
        CanvasObjectFunctions.DEFAULT.get().addTransformAboutPoint(flap, new Point(1200, 500, 200), new RepeatingTransform<>(Axis.Y.getRotation(2),0));
        
        Gate torus = new Gate(50,50,20);
        TraitHandler.INSTANCE.registerTrait(torus, PlugableTrait.class).registerPlugin(Events.EXPLODE, explode, false);
        torus.setColour(new Color(250, 250, 250));
        addSceneObject(new SceneObject(torus, new Point(200,200,450), ScanlineShaderFactory.GORAUD));
        Transform torust1 = new RepeatingTransform<>(Axis.Y.getRotation(3), 60);
        Transform torust2 = new RepeatingTransform<>(Axis.X.getRotation(3), 60);
        SequenceTransform torust = new SequenceTransform();
        torust.add(torust1).add(torust2);
        CanvasObjectFunctions.DEFAULT.get().addTransformAboutCentre(torus, torust);
        torus.addFlag(Events.EXPLODE_PERSIST);
        //torus.setCastsShadow(false);
        
        torus.setPassThroughPluginForGate(obj -> {
            Transform rot = new RepeatingTransform<>(Axis.X.getRotation(3), 60);
            CanvasObjectFunctions.DEFAULT.get().addTransformAboutCentre(obj, rot);
            return null;
        });
        
        torus.setPassThroughPluginForObject(obj -> {
            obj.setColour(Color.pink);
            AmmoTracker.INSTANCE.getTracked().values().forEach(h -> h.addAmmo(5));
            return null;
        });
        
        TexturedCuboid cube = new TexturedCuboid(200,200,200);
        TraitHandler.INSTANCE.registerTrait(cube, PlugableTrait.class);
        addSceneObject(new SceneObject(cube, new Point(500,500,500), ScanlineShaderFactory.TEXGORAUD));
        Transform cubet2 = new RepeatingTransform<>(Axis.Z.getRotation(3), 30);
        CanvasObjectFunctions.DEFAULT.get().addTransformAboutCentre(cube, cubet2);
        cube.addFlag(Events.STICKY);
        
        MovementTransform cubet = new MovementTransform(new Vector(-1,0,0), 5);
        cubet.moveUntil(t -> t.getDistanceMoved() >= 350);
        
        cube.addTransform(cubet);
        
        Sphere ball = new Sphere(100,15);
        TraitHandler.INSTANCE.registerTrait(ball, co -> new TexturableTrait(co, new OvoidTextureMapper())).addTexture(new BmpTexture("smily"));
        TraitHandler.INSTANCE.registerTrait(ball, PlugableTrait.class).registerPlugin(Events.EXPLODE, explode, false);
        ball.setColour(new Color(255, 255, 0));
        ball.addFlag(Events.EXPLODE_PERSIST);
        addSceneObject(new SceneObject(ball, new Point(500,200,450), ScanlineShaderFactory.TEXGORAUD));
        
        for (int i = 0; i < ball.getFacetList().size() ; i++)
        {
            if (i % (ball.getPointsPerCircle()/3) == 1 || i % (ball.getPointsPerCircle()/3) == 0)
            {
                ball.getFacetList().get(i).setColour(new Color(150,0,150));
            }
        }  
        
        ScaleTransform st = new ScaleTransform(0.95);
        RepeatingTransform<ScaleTransform> rpt = new RepeatingTransform<>(st,15){
            @Override
            public void onComplete(){
                st.setScaling(1.05);
            }
        };
        rpt.setResetAfterComplete(true);
        RepeatingTransform<?> spheret = new RepeatingTransform<>(rpt,30);
            
        CanvasObjectFunctions.DEFAULT.get().addTransformAboutCentre(ball, spheret);
    }
    
    @Override
    public void processInput(KeyEvent key) {
        if (key.getKeyChar() == '.' && l3.getLightSource().getIntensity() <= 0.9 && l3.getLightSource().isOn()) {
            l3.getLightSource().setIntensity(l3.getLightSource().getIntensity() + 0.1);
            lantern3.setColour(l3.getLightSource().getActualColour());
        }
        
        else if (key.getKeyChar() == ',' && l3.getLightSource().getIntensity() >= 0.1 && l3.getLightSource().isOn()) {
            l3.getLightSource().setIntensity(l3.getLightSource().getIntensity() - 0.1);
            lantern3.setColour(l3.getLightSource().getActualColour());
        }
        
        else if (key.getKeyChar() == '1') {
            getFrameLightsources().get(0).toggle();
        }
        else if (key.getKeyChar() == '2') {
            getFrameLightsources().get(1).toggle();
        }
        else if (key.getKeyChar() == '3') {
            getFrameLightsources().get(2).toggle();
        }
    }
    
    @Override
    public void destroyFrame() {
        super.destroyFrame();
        l3 = null;
        lantern3 = null;
    }


}
