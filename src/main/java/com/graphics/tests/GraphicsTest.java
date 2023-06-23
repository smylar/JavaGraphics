package com.graphics.tests;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;

import com.graphics.lib.Axis;
import com.graphics.lib.Point;
import com.graphics.lib.Utils;
import com.graphics.lib.Vector;
import com.graphics.lib.camera.Camera;
import com.graphics.lib.camera.ViewAngleCamera;
import com.graphics.lib.canvas.Canvas3D;
import com.graphics.lib.canvas.CanvasObject;
import com.graphics.lib.canvas.SlaveCanvas3D;
import com.graphics.lib.canvas.effects.BoundingBox;
import com.graphics.lib.canvas.effects.Reticule;
import com.graphics.lib.canvas.effects.RollMarker;
import com.graphics.lib.canvas.effects.ScreenEffectsAspect;
import com.graphics.lib.interfaces.ICanvasObject;
import com.graphics.lib.interfaces.IOrientable;
import com.graphics.lib.interfaces.IOrientation;
import com.graphics.lib.interfaces.IPlugable;
import com.graphics.lib.lightsource.DirectionalLightSource;
import com.graphics.lib.orientation.SimpleOrientation;
import com.graphics.lib.plugins.Events;
import com.graphics.lib.plugins.IPlugin;
import com.graphics.lib.scene.SceneFrame;
import com.graphics.lib.scene.SceneMap;
import com.graphics.lib.shader.ScanlineShaderFactory;
import com.graphics.lib.texture.BmpTexture;
import com.graphics.lib.traits.OrientableTrait;
import com.graphics.lib.traits.PlugableTrait;
import com.graphics.lib.traits.TraitHandler;
import com.graphics.lib.transform.MovementTransform;
import com.graphics.lib.transform.PanCamera;
import com.graphics.lib.transform.Translation;
import com.graphics.lib.zbuffer.ZBuffer;
import com.graphics.shapes.Cuboid;
import com.graphics.shapes.Surface;
import com.graphics.tests.shapes.Ship;
import com.graphics.tests.weapons.AmmoTracker;
import com.graphics.tests.weapons.AutoAmmoProxy;
import com.graphics.tests.weapons.BouncyProjectile;
import com.graphics.tests.weapons.DefaultAmmoHandler;
//import com.graphics.tests.weapons.DefaultAmmoHandler;
import com.graphics.tests.weapons.DeflectionProjectile;
import com.graphics.tests.weapons.ExplodingProjectile;
import com.graphics.tests.weapons.ExtendedMagazineWeapon;
import com.graphics.tests.weapons.GattlingRound;
import com.graphics.tests.weapons.LaserWeapon;
import com.graphics.tests.weapons.Projectile;
import com.graphics.tests.weapons.ProjectileWeapon;
import com.graphics.tests.weapons.TrackingProjectile;
import com.sound.ClipLibrary;

import io.reactivex.Observable;
import io.reactivex.disposables.Disposable;

public class GraphicsTest extends JFrame {

	private static final long serialVersionUID = 1L;
	private static ClipLibrary clipLibrary;
    
	private final transient Disposable drawSubscription;
	private boolean chaseCam = false;
	private JFrame slave;
	private Canvas3D canvas;
	private AtomicReference<ICanvasObject> selectedObject = new AtomicReference<>();
	private MouseEvent select = null;
	

	public static void main (String[] args) {
		try {
		    clipLibrary = ClipLibrary.getInstance();
			SwingUtilities.invokeAndWait(GraphicsTest::new);
			clipLibrary.playMusic();
		} catch (Exception e1) {
            e1.printStackTrace();
        }
		
	}
	
	public GraphicsTest() {
		super("Super Graphics Test");
		this.setLayout(new BorderLayout());
		this.setSize(700, 700);
		
		this.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);

		SceneMap sceneMap = new SceneMap();

		//N.B. world coords follow the inverted Y screen coords, really should've had these the right way up
		//and transformed in the camera transform but we're a bit far down the line here
		ViewAngleCamera cam = new ViewAngleCamera(new SimpleOrientation(OrientableTrait.ORIENTATION_TAG));
		cam.setPosition(new Point(350, 280, -200));
		//FocusPointCamera cam = new FocusPointCamera();
		//cam.setFocusPoint(new Point(300, 300, 1000));
		Canvas3D cnv = Canvas3D.get(cam, sceneMap);
		ZBuffer zBuf = new ZBuffer();
		cnv.setzBuffer(zBuf);
		
		this.add(cnv, BorderLayout.CENTER);
		
		slave = new JFrame("Slave");
		slave.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
		slave.setLayout(new BorderLayout());
		slave.setSize(300, 300);
		slave.setLocation(750, 50);
		
		IPlugin<IPlugable, Void> explode = TestUtils.getExplodePlugin(Optional.ofNullable(clipLibrary));
		
		ViewAngleCamera slaveCam = new ViewAngleCamera(new SimpleOrientation(OrientableTrait.ORIENTATION_TAG));
		slaveCam.setPosition(new Point(1550, 200, 350));
		slaveCam.addTransform("INIT", new PanCamera(Axis.Y, -90));
		slaveCam.doTransforms();
		SlaveCanvas3D scnv = new SlaveCanvas3D(slaveCam);
		slave.add(scnv);
		slave.setVisible(true);
		cnv.addObserver(scnv);
		this.setVisible(true);

		double floorLevel = 700;
		StartScene scene = new StartScene(new Color(246,215,176), floorLevel);
		scene.addFloorTexture(() -> new BmpTexture("river", Color.white));
		sceneMap.add(0, 0, scene);

		sceneMap.add(1,0, new MountainScene(new Color(20, 85, 20), floorLevel));
		
		Ship ship = new Ship (100, 100, 50);
		
		ship.setColour(new Color(50, 50, 50));
		TraitHandler.INSTANCE.registerTrait(ship, OrientableTrait.class).setOrientation(new SimpleOrientation(OrientableTrait.ORIENTATION_TAG));
		addWeapons(ship, cam);
		ship.applyTransform(Axis.Y.getRotation(180));
		cnv.registerObject(ship, new Point(350, 350, -50), ScanlineShaderFactory.GORAUD.getDefaultSelector());
		
	    TraitHandler.INSTANCE.registerTrait(ship, PlugableTrait.class)
          .registerPlugin("CHECK_FLOOR", 
                  plugable -> {
                      double ydif = plugable.getParent().getCentre().y - cnv.getFloorPlane().first().y; //ideally check for specific point that has gone through floor
                      if (ydif > 1) {
                          ship.applyTransform(new Translation(0, -ydif, 0));
                      }
                      return null;   
                  }, true);
		
		Surface wall = new Surface(40,40);
		wall.setColour(new Color(240, 240, 240));
		wall.setLightIntensityFinder(Utils.getShadowLightIntensityFinder(cnv::getShapes));
		wall.setVisible(false);
		cnv.registerObject(wall, new Point(350,350,700), ScanlineShaderFactory.GORAUD.getDefaultSelector());
		
		DirectionalLightSource l4 = new DirectionalLightSource();
		l4.setPosition(cam::getPosition);
		l4.setDirection(() -> cam.getOrientation().getForward().getUnitVector());
		l4.setLightConeAngle(45);
		l4.setColour(new Color(255, 255, 255));
		//l4.setRange(0);
		cnv.addLightSource(l4);
		
		try {
			this.addKeyListener(new ShipControls(ship));
		} catch (Exception e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
		this.addKeyListener(new KeyListener() {
			@Override
			public void keyTyped(KeyEvent key) {
				if (key.getKeyChar() == 'l') l4.toggle();
				
				else if (key.getKeyChar() == 'y'){
				    Cuboid movingTarget = new Cuboid(20,20,20);
					MovementTransform move = new MovementTransform(new Vector(1,1,0).getUnitVector(), 4);
					move.moveUntil(t -> t.getDistanceMoved() > 5000);
					movingTarget.addTransform(move);
					movingTarget.setCastsShadow(false);
					movingTarget.deleteAfterTransforms();
					TraitHandler.INSTANCE.registerTrait(movingTarget, PlugableTrait.class).registerPlugin(Events.EXPLODE, explode, false);
					cnv.registerObject(movingTarget , new Point(0, 0, 0), ScanlineShaderFactory.FLAT.getDefaultSelector());
				}
				
				else if (key.getKeyChar() == 'z') {
					double viewAngle = cam.getViewAngle() - 5;
					cam.setViewAngle(viewAngle < 5 ? 5 : viewAngle);
				}
				
				else if (key.getKeyChar() == 'x') {
					double viewAngle = cam.getViewAngle() + 5;
					cam.setViewAngle(viewAngle > 85 ? 85 : viewAngle);
				}
				
				else if (key.getKeyChar() == 'c') {
					double angle = l4.getLightConeAngle() - 5;
					l4.setLightConeAngle(angle < 5 ? 5 : angle);
				}
				
				else if (key.getKeyChar() == 'v') {
					double angle = l4.getLightConeAngle() + 5;
					l4.setLightConeAngle(angle > 85 ? 85 : angle);
				}
				
				else if (key.getKeyChar() == 'n') {
					wall.setVisible(!wall.isVisible());
				}
				
				else if (key.getKeyChar() == 'm') {
					cnv.setDrawShadows(!cnv.isDrawShadows());
				}

				else if (key.getKeyChar() == '/') {
					chaseCam = !chaseCam;
				}
				else if (key.getKeyChar() == 'i') {
					System.out.println(l4.getPosition());
				}
				else {
				    SceneFrame currentFrame = cnv.getCurrentScene();
				    if (currentFrame != null) {
				        currentFrame.processInput(key);
				    }
				}
				
			}
			
			@Override
			public void keyPressed(KeyEvent key) {
				checkEngineSound(ship);
			}
			
			@Override
			public void keyReleased(KeyEvent key) {
				checkEngineSound(ship);
			}
			
		});
		
		
		cnv.addMouseListener(new MouseAdapter() {
			@Override
			public void mousePressed(MouseEvent e) {
				select = e;
			}
		});
		
		addUIOverlay(cnv, cam);
		
		cam.setTiedTo(ship, (o, c) -> {
			Point p = o.getVertexList().get(0);
			if (chaseCam) {
			    moveBack(new Point(p), c);
			} else {
			    c.setPosition(new Point(p));
			}
		});
		
		scnv.setVisible(true);
		cnv.setVisible(true);
		this.requestFocus();
		this.canvas = cnv;
		
		drawSubscription = subscribeToCanvas(50);

	}
	
	private void moveBack(Point p, Camera cam) {
	    Vector backward = cam.getOrientation().getBack();
	    Vector up = cam.getOrientation().getUp();
	    p.x += backward.x() * 130 + up.x() * 55;
	    p.y += backward.y() * 130 + up.y() * 55;
	    p.z += backward.z() * 130 + up.z() * 55;
	    cam.setPosition(p);
	}
	
	private ICanvasObject getSelectedObject() {
		return selectedObject.get();
	}

	private void setSelectedObject(ICanvasObject selectedObject) {
		this.selectedObject.set(selectedObject);
	}

	private void checkEngineSound(CanvasObject source){
		if (source.getTransformsOfType(MovementTransform.class).stream().anyMatch(t -> !t.isCancelled() && !t.isComplete() && t.getAcceleration() != 0)){
			clipLibrary.loopSound("ENGINE", -30f);
		} else {
			clipLibrary.stopSound("ENGINE");
		}
	}
	
	private Disposable subscribeToCanvas(long refreshms) {
	    return Observable.interval(refreshms, TimeUnit.MILLISECONDS)
                .flatMap(t -> canvas.doDraw().doOnComplete(this::selectObject))
                .doOnDispose(() -> System.out.println("Bye bye"))
                .subscribe();
	}
	
	private void selectObject() {
	    if (select != null) {
            canvas.getObjectAt(select.getX(), select.getY()).ifPresent(this::setSelectedObject);
            select = null;
        }
	}
	
    @Override
    public void dispose() {
            try {
                clipLibrary.close();
            } catch (Exception e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            drawSubscription.dispose();
            super.dispose();
            slave.dispose();
    }	
	
	private void addWeapons(final Ship ship, final Camera cam) {
		
	  //Supplier<IOrientation> forward = () -> cam.getOrientation().copy();
		//Supplier<IOrientation> forward = () -> TraitHandler.INSTANCE.getTrait(ship, IOrientable.class).map(o -> o.getOrientation().copy()).get();
		//hmm, having switched from the camera vector to the ship vector there might be an issue
		//this can sometimes return null, presumably because the orientation is undergoing a transformation
		//will need to consider when weapon activation takes place (needs to be in the draw complete stage?? or prior to transforming??)
	    final IOrientable shipOrientation = TraitHandler.INSTANCE.getTrait(ship, IOrientable.class).orElseThrow();
        Supplier<IOrientation> forward = () -> shipOrientation.getOrientation().copy(); //could be a problem getting the trait?
		
		ship.addWeapon(Ship.Hardpoints.CENTRE, new LaserWeapon("LASER",
				() -> cam.getOrientation().getForward(), ship));
			
		
		Projectile bp = new BouncyProjectile();
		bp.setClipLibary(clipLibrary);
		ship.addWeapon(Ship.Hardpoints.LEFT, new ProjectileWeapon("BOUNCY", bp, forward, ship));
		
		DeflectionProjectile dp = new DeflectionProjectile();
		dp.setSpeed(20);
		dp.setRange(8000);
		dp.setClipLibary(clipLibrary);
		dp.setTargetFinder(this::getSelectedObject);
		ship.addWeapon(Ship.Hardpoints.LEFT, new ProjectileWeapon("DEFLECTION", dp, forward, ship));
		
		TrackingProjectile tp = new TrackingProjectile();
		tp.setSpeed(20);
		tp.setRange(8000);
		tp.setClipLibary(clipLibrary);
		tp.setTargetFinder(this::getSelectedObject);
		ship.addWeapon(Ship.Hardpoints.RIGHT, new ProjectileWeapon("TRACKING", tp, forward, ship));
		
		ExplodingProjectile ep = new ExplodingProjectile();
		ep.setSpeed(20);
		ep.setRange(1200);
		ep.setClipLibary(clipLibrary);

		ship.addWeapon(Ship.Hardpoints.RIGHT, new ExtendedMagazineWeapon("TORPEDO1", ep, forward, ship));
		
		ship.addWeapon(Ship.Hardpoints.LEFT, new ExtendedMagazineWeapon("TORPEDO2", ep, forward, ship));
		
		ship.addWeapon(Ship.Hardpoints.CENTRE, AutoAmmoProxy.weaponWithAmmo(new ProjectileWeapon("GAT", new GattlingRound().setRange(800).setSpeed(60),
				forward,
				ship
				), new DefaultAmmoHandler(1000).setTicksBetweenShots(2)));
		        //), new KotlinAmmoHandler(1000,2)));
	}
	
	private void addUIOverlay(Canvas3D cnv, Camera cam) {
	    ScreenEffectsAspect.addAction(TestUtils.showMarkers());
        ScreenEffectsAspect.addAction(new Reticule());
        ScreenEffectsAspect.addAction(new RollMarker());
	    
	    ScreenEffectsAspect.addAction(new BoundingBox(this::getSelectedObject));
        
        /*cnv.addDrawOperation((c, g) -> {
            //messing with collectors - puts the count of each object type at the bottom of the screen
            Map<String, Long> objectCounts = c.getShapes().stream().collect(
                        Collectors.groupingBy(s -> s.getBaseObject().getClass().getSimpleName(), Collectors.counting())
                    );
            g.setColor(Color.LIGHT_GRAY);
            g.drawString(objectCounts.toString(), 40, c.getHeight()-5);
        });*/
	    ScreenEffectsAspect.addAction((c, g) -> {
            //more messing with collectors for ammo counts
            Map<String, Integer> ammoCounts = AmmoTracker.INSTANCE.getTracked()
                                                  .entrySet()
                                                  .stream()
                                                  .collect(
                                                        Collectors.groupingBy(t -> t.getKey().getEffectClass().orElse(t.getKey().getClass()).getSimpleName(),
                                                                Collectors.summingInt(t -> t.getValue().getAmmoCount()))
                                                  );
            g.setColor(new Color(0,0,0,150));
            g.fillRect(0, c.getHeight() - 20, c.getWidth(), 20);
            g.setColor(Color.WHITE);
            g.drawString(ammoCounts.toString(), 40, c.getHeight()-5);
        });
	}
}
