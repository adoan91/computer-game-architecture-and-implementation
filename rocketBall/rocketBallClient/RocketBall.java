package rocketBallClient;

import java.awt.Color;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.net.InetAddress;
import java.rmi.UnknownHostException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Random;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

import com.jogamp.opengl.GL2;

import graphicslib3D.Matrix3D;
import graphicslib3D.Point3D;
import graphicslib3D.Vector3D;
import rocketBallClient.display.MyDisplaySystem;
import rocketBallClient.display.camera.OrbitCameraController;
import rocketBallClient.events.CrashEvent;
import rocketBallClient.events.QuitGameAction;
import rocketBallClient.inputActions.BackwardAction;
import rocketBallClient.inputActions.ForwardAction;
import rocketBallClient.inputActions.LeftAction;
import rocketBallClient.inputActions.RightAction;
import rocketBallClient.networking.MyClient;
import rocketBallClient.sceneGraph.gameObjects.CustomShape;
import rocketBallClient.sceneGraph.gameObjects.GhostNPC;
import rocketBallClient.sceneGraph.gameObjects.MyPyramid;
import rocketBallClient.sceneNodeControllers.MyScaleController;
import rocketBallClient.sceneNodeControllers.MySpinController;
import sage.app.BaseGame;
import sage.audio.AudioManagerFactory;
import sage.audio.AudioResource;
import sage.audio.AudioResourceType;
import sage.audio.IAudioManager;
import sage.audio.Sound;
import sage.audio.SoundType;
import sage.camera.ICamera;
import sage.camera.JOGLCamera;
import sage.display.IDisplaySystem;
import sage.event.EventManager;
import sage.event.IEventManager;
import sage.input.IInputManager;
import sage.input.InputManager;
import sage.input.action.IAction;
import sage.model.loader.OBJLoader;
import sage.model.loader.ogreXML.OgreXMLParser;
import sage.networking.IGameConnection.ProtocolType;
import sage.physics.IPhysicsEngine;
import sage.physics.IPhysicsObject;
import sage.physics.PhysicsEngineFactory;
import sage.renderer.IRenderer;
import sage.scene.Controller;
import sage.scene.Group;
import sage.scene.HUDString;
import sage.scene.Leaf;
import sage.scene.Model3DTriMesh;
import sage.scene.SceneNode;
import sage.scene.SkyBox;
import sage.scene.TriMesh;
import sage.scene.bounding.BoundingVolume;
import sage.scene.shape.Cube;
import sage.scene.shape.Line;
import sage.scene.shape.Pyramid;
import sage.scene.shape.Rectangle;
import sage.scene.shape.Sphere;
import sage.scene.state.RenderState.RenderStateType;
import sage.scene.state.TextureState;
import sage.terrain.TerrainBlock;
import sage.texture.Texture;
import sage.texture.TextureManager;


public class RocketBall extends BaseGame {
	IAudioManager audioMgr;
	Sound waterSound, npcSound, backgroundSound;
	
	private Group model;
	private MySpinController mtc;
	private MyScaleController msc;
	private IDisplaySystem display;
	private ICamera camera;
	private IInputManager im;
	private IEventManager eventMgr;
	
	private IRenderer renderer;
	private Model3DTriMesh /*player1, */player;
	private OrbitCameraController /*orbit0Controller, */orbit1Controller;
	private Group plants1, other;
	private String gpName, kb, mouseName;
	
	private Random rand = new Random();
	private MyPyramid [] plants = new MyPyramid [10];
	private Matrix3D [] plantsMatrix = new Matrix3D [10];
	private boolean [] plantsCapturedTracker = new boolean [plants.length];
	private Sphere aSphere;
	private CustomShape myShape;
	
	private Rectangle flatGroundPlane;
	private Matrix3D msM, sphereMatrix;
	
	private double scale = 1;
	private int numCrashes = 0, 
			plantsCaptured = 0, 
			p1Score = 0, 
			p2Score = 0;
	private float time = 0; // game elapsed time
	private HUDString scoreString, scoreString2, timeString;
	
	private boolean sphereGotHit = false, gameOverFlag = false;
	
	private Point3D x1, x2, y1, y2, z1, z2;
	private Line xAxis, yAxis, zAxis;
	
	private String serverAddress;
	private int serverPort;
	private ProtocolType serverProtocol;
	private MyClient thisClient;
	private boolean isConnected;
	private SkyBox skybox;
	private TerrainBlock terrain;
	
	private TriMesh ball;
	private IPhysicsEngine physicsEngine;
	private IPhysicsObject ballP, groundPlaneP;
	
	private boolean running;
	
	public RocketBall(/*String serverAddr, int sPort*/) {
		super();
		this.serverAddress = "localhost";//serverAddr;
		this.serverPort = 50001;//sPort;
		this.serverProtocol = ProtocolType.TCP;
	}

	
	private IDisplaySystem createDisplaySystem() {
		display = new MyDisplaySystem(
				800,
				600,
				24,
				20,
				true,
				"sage.renderer.jogl.JOGLRenderer"
				);
		System.out.print("\nWaiting for display creation...");
		int count = 0;
		
		// wait until display creation completes or a timeout occurs
		while (!display.isCreated()) {
			try {
				Thread.sleep(10);
			} catch (InterruptedException e) {
				throw new RuntimeException("Display creation interrupted");
			}
			
			count++;
			System.out.print("+");
			if (count % 80 == 0) {
				System.out.println();
			}
			
			if (count > 2000) { // 20 seconds (approx.)
				throw new RuntimeException("Unable to create display");
			}
		}
		
		System.out.println();
		return display;
	}
	
	protected void shutdown() {
		super.shutdown();
		if (thisClient != null) {
			thisClient.sendByeMessage();
			try {
				// shutdown() is inherited
				thisClient.shutdown();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
		display.close();
		
	}
	
	protected void initSystem() {
		// call a local method to create a DisplaySystem object
		IDisplaySystem display = createDisplaySystem();
		setDisplaySystem(display);
	
		// create an Input Manager
		IInputManager im = new InputManager();
		setInputManager(im);
		
		// create an (empty) gameworld
		ArrayList<SceneNode> gameWorld = new ArrayList<SceneNode>();
		setGameWorld(gameWorld);
	}

	@Override
	protected void initGame() {
		// items as before, plus initializing network:
		try {
			thisClient = new MyClient(InetAddress.getByName(serverAddress),
					serverPort, serverProtocol, this);
		} catch (UnknownHostException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		if (thisClient != null) {
			thisClient.sendJoinMessage();
		}
		
		display = getDisplaySystem();
		getDisplaySystem().setTitle("Rocket Ball");
		renderer = display.getRenderer();
		eventMgr = EventManager.getInstance();
		
		
		initGameObjects();
		createPlayers();
		
		createGraphicsScene();
		initPhysicsSystem();
		createSagePhysicsWorld();
		running = true;
		
		
		im = getInputManager();
		kb = im.getKeyboardName();
		gpName = im.getFirstGamepadName();
		mouseName = im.getMouseName();
		System.out.println(kb);
		orbit1Controller = new OrbitCameraController( camera, player, im, kb );
		
		
		IAction mvForward = new ForwardAction(player);
		IAction mvBackward = new BackwardAction(player);
		IAction mvLeft = new LeftAction(player);
		IAction mvRight = new RightAction(player);
		
		IAction quit = new QuitGameAction(this);

		im.associateAction(
				kb, 
				net.java.games.input.Component.Identifier.Key.W, 
//				mvForward,
				mvBackward,
				IInputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN
				);
		
		im.associateAction(
				kb, 
				net.java.games.input.Component.Identifier.Key.A, 
				mvRight,
//				mvLeft,
				IInputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN
				);
		
		im.associateAction(
				kb, 
				net.java.games.input.Component.Identifier.Key.D, 
				mvLeft,
//				mvRight,
				IInputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN
				);
		
		im.associateAction(
				kb, 
				net.java.games.input.Component.Identifier.Key.S, 
				mvForward,
//				mvBackward,
				IInputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN
				);
				
		im.associateAction(
				kb, 
				net.java.games.input.Component.Identifier.Key.ESCAPE, 
				quit,
				IInputManager.INPUT_ACTION_TYPE.ON_PRESS_ONLY
				);
		
		if (gpName != null) {
			// button Y
			im.associateAction(
					gpName, 
					net.java.games.input.Component.Identifier.Button._3,
					mvBackward,
					IInputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN
					);
			
			// button X
			im.associateAction(
					gpName, 
					net.java.games.input.Component.Identifier.Button._2,
					mvRight,
					IInputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN
					);
			
			// button B
			im.associateAction(
					gpName, 
					net.java.games.input.Component.Identifier.Button._1,
					mvLeft,
					IInputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN
					);
			
			// button A
			im.associateAction(
					gpName, 
					net.java.games.input.Component.Identifier.Button._0,
					mvForward,
					IInputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN
					);

		}
		
		initAudio();
//		Iterator<SceneNode> itr = model.getChildren();
//		while (itr.hasNext()) {
//			Model3DTriMesh sa = ((Model3DTriMesh) itr.next());
//			sa.startAnimation("Wings_Flap");
//		}
		
		
		super.update(0.0f);
	}
	
	private void initAudio() {
	
		AudioResource resource1, resource2, r3;
		audioMgr = AudioManagerFactory.createAudioManager("sage.audio.joal.JOALAudioManager");
		
		if (!audioMgr.initialize()) {
			System.out.println("Audio Manager failed to initialize!");
			return;
		}
		
		resource1 = audioMgr.createAudioResource("name.wav", AudioResourceType.AUDIO_STREAM);
		resource2 = audioMgr.createAudioResource("name2.wav", AudioResourceType.AUDIO_STREAM);
		r3 = audioMgr.createAudioResource("name3.wav", AudioResourceType.AUDIO_STREAM);

		npcSound = new Sound(resource1, SoundType.SOUND_EFFECT, 100, true);
		npcSound.initialize(audioMgr);
		npcSound.setMaxDistance(50.0f);
		npcSound.setMinDistance(3.0f);
		npcSound.setRollOff(5.0f);
		npcSound.setLocation(new Point3D(ball.getWorldTranslation().getCol(3))); // at ball location
		
		
		waterSound = new Sound(resource2, SoundType.SOUND_EFFECT, 100, true);
		waterSound.initialize(audioMgr);
		waterSound.setMaxDistance(50.0f);
		waterSound.setMinDistance(3.0f);
		waterSound.setRollOff(5.0f);
		waterSound.setLocation(new Point3D(myShape.getWorldTranslation().getCol(3)));
		
		backgroundSound = new Sound(r3, SoundType.SOUND_EFFECT, 100, true);
		backgroundSound.initialize(audioMgr);
		backgroundSound.setMaxDistance(50.0f);
		backgroundSound.setMinDistance(3.0f);
		backgroundSound.setRollOff(5.0f);
		backgroundSound.setLocation(new Point3D(player.getWorldTranslation().getCol(3)));
		
		setEarParameters();
	}

	private void setEarParameters() {
		Matrix3D avDir = (Matrix3D) (this.player.getWorldRotation().clone());
		float camAz = orbit1Controller.getAzimuth();
		avDir.rotateY(180.0f - camAz);
		Vector3D camDir = new Vector3D(0, 0, 1);
		camDir = camDir.mult(avDir);
		
		audioMgr.getEar().setLocation(this.camera.getLocation());
		audioMgr.getEar().setOrientation(camDir, new Vector3D(0, 1, 0));		
	}

	private void createSagePhysicsWorld() {
		// add the ball physics
		float mass = 1.0f;
		ballP = physicsEngine.addSphereObject(physicsEngine.nextUID(), 
				mass, 
				ball.getWorldTransform().getValues(), 
				1.0f
				);
		ballP.setBounciness(1.0f);
		ball.setPhysicsObject(ballP);
		
		// add the ground plane physics
		float up [] = {0,1,0};//{ -0.05f, 0.95f, 0 }; // {0,1,0} is flat
		groundPlaneP = physicsEngine.addStaticPlaneObject(physicsEngine.nextUID(),
				terrain.getWorldTransform().getValues(), 
				up, 
				0.0f
				);
		groundPlaneP.setBounciness(1.0f);
		terrain.setPhysicsObject(groundPlaneP);
		// should also set damping, friction, etc
		
	}

	private void createGraphicsScene() {
		OBJLoader loader = new OBJLoader();
		ball = loader.loadModel("untitled.obj");//new Sphere(1.0, 16, 16, Color.red);
		Matrix3D xform = new Matrix3D();
		xform.translate(50, 10, 0);
		Texture t =  TextureManager.loadTexture2D("a.png");
		ball.setTexture(t);
		ball.setLocalTranslation(xform);
		addGameWorldObject(ball);
		ball.updateGeometricState(1.0f, true);
		
	}

	protected void initPhysicsSystem() {
		String engine = "sage.physics.JBullet.JBulletPhysicsEngine";
		physicsEngine = PhysicsEngineFactory.createPhysicsEngine(engine);
		physicsEngine.initSystem();
		float [] gravity = { 0.0f, -1.0f, 0.0f };
		physicsEngine.setGravity(gravity);
	}
	
	private void createPlayers() {
		
		player = getPlayerAvatar();
//		Iterator<SceneNode> itr = model.getChildren();
//		while (itr.hasNext()) {
//			Model3DTriMesh mesh = ((Model3DTriMesh) itr.next());
//			mesh.startAnimation("Wings_Flap");
//		}
		
		
		Matrix3D xform = new Matrix3D();
		xform.translate(50, 1, 0);
		
		player.setLocalTranslation(xform);
		
		player.scale(0.5f, 0.5f, 0.5f);
		player.rotate(180, new Vector3D(0,1,0));
		player.startAnimation("Wings_Flap");
		addGameWorldObject(player);
		
		camera = new JOGLCamera(renderer);
		camera.setPerspectiveFrustum(60, 2, 1, 1000);
		createPlayerHUDs();
	}
	
	
	private Model3DTriMesh getPlayerAvatar() {
		Model3DTriMesh m = null;
		OgreXMLParser loader = new OgreXMLParser();
		loader.setVerbose(true);
		try {
			String slash = File.separator;
			
			model = loader.loadModel("models" + slash + "Cube.001.mesh.xml", 
					"materials" + slash + "Material.001.material", 
					"models" + slash + "Cube.001.skeleton.xml");
			model.updateGeometricState(0, true);
			
			Iterator<SceneNode> itr = model.getChildren();
			m = (Model3DTriMesh) itr.next();

			
			
			
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(1);
		}
		return m;
	}
	
	
	@Override
	public void update(float elapsedTimeMS) {
		
		if (thisClient != null) {
			thisClient.sendMoveMessage(getPlayerPosition());
			thisClient.processPackets();
		}
		
		
		orbit1Controller.update(elapsedTimeMS);
		
		Point3D camLoc = camera.getLocation();
		Matrix3D camTranslation = new Matrix3D();
		camTranslation.translate(camLoc.getX(), camLoc.getY(), camLoc.getZ());
		
		skybox.setLocalTranslation(camTranslation);
		
		
		if (gameOverFlag != true) {
			
			Matrix3D mat;
			physicsEngine.update(20.0f);
			
			for (SceneNode s : getGameWorld()) {
				if (s.getPhysicsObject() != null) {
					mat = new Matrix3D(s.getPhysicsObject().getTransform());
					s.getLocalTranslation().setCol(3, mat.getCol(3));
					// should also get and apply rotation
				}
			}
			
			
			if (sphereGotHit == false) {
				
				if (aSphere.getWorldBound().intersects(player.getWorldBound())) {
					sphereHit();
					p2Score++;
					updateScore();
				}
//				else if (aSphere.getWorldBound().intersects(player1.getWorldBound())) {
//					sphereHit();
//					p1Score++;
//					updateScore();
//				}
				
			}
			
			int i = 0;
			while (i < plants.length) {
				
				if (plants[i]
						.getWorldBound()
						.intersects(player.getWorldBound()) && 
						plantsCapturedTracker[i] == false) {
				
					plantHit(i);
					p2Score++;
					updateScore();
				}
//				else if (plants[i]
//						.getWorldBound()
//						.intersects(player1.getWorldBound()) && 
//						plantsCapturedTracker[i] == false) {
//					
//					plantHit(i);
//					p1Score++;
//					updateScore();
//				}
				i++;
			}
			i = 0;
			
			time += elapsedTimeMS;
			DecimalFormat df = new DecimalFormat("0.0");
			timeString.setText("Time = " +
					df.format(time / 1000)
					);

		} else {
			// you win
			checkGameOver();
		}
		npcSound.setLocation(new Point3D(ball.getWorldTranslation().getCol(3))); // at ball location
		waterSound.setLocation(new Point3D(myShape.getWorldTranslation().getCol(3)));
		setEarParameters();
		
		if (npcSound.getIsPlaying() == false) {
			npcSound.play();
		}
		if (waterSound.getIsPlaying() == false) {
			waterSound.play();
		}
		if (backgroundSound.getIsPlaying() == false) {
			backgroundSound.play();
		}
		
		//Iterator<SceneNode> itr = model.getChildren();
//		while (itr.hasNext()) {
//			Model3DTriMesh submesh = ((Model3DTriMesh) itr.next());
//			submesh.updateAnimation(elapsedTimeMS);
//		}
		//player2 = ((Model3DTriMesh) itr.next());
		player.updateAnimation(elapsedTimeMS);
		player.updateGeometricState(elapsedTimeMS, true);
		
		//setGameOver(thisClient.npcGameOver(player2.getWorldBound()));
		
		super.update(elapsedTimeMS);
	}

	private void createPlayerHUDs() {

		timeString = new HUDString("Time = " +
				time
				);
		timeString.setLocation(0, 0.1); // (0,0) [lower-left] to (1,1)

		addGameWorldObject(timeString);
		
//		scoreString = new HUDString ("Score = " +
//				p1Score
//				); //default is (0,0)
//		scoreString.setName("p1Score");
//		scoreString.setLocation(0.01, 0.04);
//		scoreString.setRenderMode(sage.scene.SceneNode.RENDER_MODE.ORTHO);
//		scoreString.setColor(Color.red);
//		scoreString.setCullMode(sage.scene.SceneNode.CULL_MODE.NEVER);
//		camera1.addToHUD(scoreString);
		
		scoreString2 = new HUDString ("Score = " +
				p2Score
				); //default is (0,0)
		scoreString2.setName("p2Score");
		scoreString2.setLocation(0.01, 0.04);
		scoreString2.setRenderMode(sage.scene.SceneNode.RENDER_MODE.ORTHO);
		scoreString2.setColor(Color.red);
		scoreString2.setCullMode(sage.scene.SceneNode.CULL_MODE.NEVER);
		camera.addToHUD(scoreString2);
		
//		HUDString player1ID = new HUDString("Player 1");
//		player1ID.setName("Player1ID");
//		player1ID.setLocation(0.01, 0.06);
//		player1ID.setRenderMode(sage.scene.SceneNode.RENDER_MODE.ORTHO);
//		player1ID.setColor(Color.red);
//		player1ID.setCullMode(sage.scene.SceneNode.CULL_MODE.NEVER);
//		camera1.addToHUD(player1ID);
		
		HUDString player2ID = new HUDString("Player");
		player2ID.setName("Player2ID");
		player2ID.setLocation(0.01, 0.06);
		player2ID.setRenderMode(sage.scene.SceneNode.RENDER_MODE.ORTHO);
		player2ID.setColor(Color.red);
		player2ID.setCullMode(sage.scene.SceneNode.CULL_MODE.NEVER);
		camera.addToHUD(player2ID);
	}

	public void checkGameOver() {
		
		if ((/*p1Score + */p2Score) == 11) {
			// game over
			gameOverFlag = true;
			System.out.print("You win");
		}
	}
	
	protected void render() {
		renderer.setCamera(camera);
		super.render();
	}
	
	public void sphereHit() {
		numCrashes++;
		CrashEvent newCrash = new CrashEvent(numCrashes);
		eventMgr.triggerEvent(newCrash);
						
		sphereMatrix.setToIdentity();
		sphereMatrix.translate(5, -10 + (plantsCaptured + 5), 0);
		aSphere.setLocalTranslation(sphereMatrix);
		plantsCaptured++;
		
		scale += 0.01;
		
//		msM.scale(scale,
//				scale,
//				scale
//				);
//		myShape.setLocalTranslation(msM);
		myShape.nextColor(); // change to random color
		
		checkGameOver();
		sphereGotHit = true;
	}
	
	public void plantHit(int i) {
		numCrashes++;
		CrashEvent newCrash = new CrashEvent(numCrashes);
		eventMgr.triggerEvent(newCrash);
		
		plantsMatrix[i].setToIdentity();
		plantsMatrix[i].translate(5, (plantsCaptured + 5), 0);
		
		plants[i].setLocalTranslation(plantsMatrix[i]);
		plantsCaptured++;
		plantsCapturedTracker[i] = true;
		checkGameOver();
		
		// gets a bit bigger each time we get a plant
		scale += 0.01;
		
		msM.scale(scale,
				scale,
				scale
				);
		myShape.setLocalTranslation(msM);
		myShape.nextColor(); // change to random color
	}
	
	public void updateScore() {
		
		if (/*p1Score + */p2Score == 11) {
//			if (p1Score > p2Score) {
//				camera1.removeFromHUD(camera1.getHUD().getChild("p1Score"));
//				camera2.removeFromHUD(camera2.getHUD().getChild("p2Score"));
//				scoreString = new HUDString ("YOU WIN!"
//						); //default is (0,0)
//				scoreString.setName("p1Score");
//				scoreString.setLocation(0.01, 0.04);
//				scoreString.setRenderMode(sage.scene.SceneNode.RENDER_MODE.ORTHO);
//				scoreString.setColor(Color.red);
//				scoreString.setCullMode(sage.scene.SceneNode.CULL_MODE.NEVER);
//				camera1.addToHUD(scoreString);
//				
//				scoreString2 = new HUDString ("Score = " +
//						p2Score
//						); //default is (0,0)
//				scoreString2.setName("p2Score");
//				scoreString2.setLocation(0.01, 0.04);
//				scoreString2.setRenderMode(sage.scene.SceneNode.RENDER_MODE.ORTHO);
//				scoreString2.setColor(Color.red);
//				scoreString2.setCullMode(sage.scene.SceneNode.CULL_MODE.NEVER);
//				camera2.addToHUD(scoreString2);
//			}
//			else if (p2Score > p1Score) {
//				camera1.removeFromHUD(camera1.getHUD().getChild("p1Score"));
//				camera2.removeFromHUD(camera2.getHUD().getChild("p2Score"));
//				scoreString = new HUDString ("Score = " +
//						p1Score
//						); //default is (0,0)
//				scoreString.setName("p1Score");
//				scoreString.setLocation(0.01, 0.04);
//				scoreString.setRenderMode(sage.scene.SceneNode.RENDER_MODE.ORTHO);
//				scoreString.setColor(Color.red);
//				scoreString.setCullMode(sage.scene.SceneNode.CULL_MODE.NEVER);
//				camera1.addToHUD(scoreString);
//				
//				scoreString2 = new HUDString ("YOU WIN!"
//						); //default is (0,0)
//				scoreString2.setName("p2Score");
//				scoreString2.setLocation(0.01, 0.04);
//				scoreString2.setRenderMode(sage.scene.SceneNode.RENDER_MODE.ORTHO);
//				scoreString2.setColor(Color.red);
//				scoreString2.setCullMode(sage.scene.SceneNode.CULL_MODE.NEVER);
//				camera2.addToHUD(scoreString2);
//			} else { // this won't happen unless I add more plants in the future
//				camera1.removeFromHUD(camera1.getHUD().getChild("p1Score"));
//				camera2.removeFromHUD(camera2.getHUD().getChild("p2Score"));
//				scoreString = new HUDString ("TIE GAME!"
//						); //default is (0,0)
//				scoreString.setName("p1Score");
//				scoreString.setLocation(0.01, 0.04);
//				scoreString.setRenderMode(sage.scene.SceneNode.RENDER_MODE.ORTHO);
//				scoreString.setColor(Color.red);
//				scoreString.setCullMode(sage.scene.SceneNode.CULL_MODE.NEVER);
//				camera1.addToHUD(scoreString);
//				
//				scoreString2 = new HUDString ("TIE GAME!"
//						); //default is (0,0)
//				scoreString2.setName("p2Score");
//				scoreString2.setLocation(0.01, 0.04);
//				scoreString2.setRenderMode(sage.scene.SceneNode.RENDER_MODE.ORTHO);
//				scoreString2.setColor(Color.red);
//				scoreString2.setCullMode(sage.scene.SceneNode.CULL_MODE.NEVER);
//				camera2.addToHUD(scoreString2);
//			}
			
			
			camera.removeFromHUD(camera.getHUD().getChild("p2Score"));
			
			
			scoreString2 = new HUDString ("YOU WIN!"
					); //default is (0,0)
			scoreString2.setName("p2Score");
			scoreString2.setLocation(0.01, 0.04);
			scoreString2.setRenderMode(sage.scene.SceneNode.RENDER_MODE.ORTHO);
			scoreString2.setColor(Color.red);
			scoreString2.setCullMode(sage.scene.SceneNode.CULL_MODE.NEVER);
			camera.addToHUD(scoreString2);
			
		} else {
			camera.removeFromHUD(camera.getHUD().getChild("p2Score"));
			
			scoreString2 = new HUDString ("Score = " + p2Score ); //default is (0,0)
			scoreString2.setName("p2Score");
			scoreString2.setLocation(0.01, 0.04);
			scoreString2.setRenderMode(sage.scene.SceneNode.RENDER_MODE.ORTHO);
			scoreString2.setColor(Color.red);
			scoreString2.setCullMode(sage.scene.SceneNode.CULL_MODE.NEVER);
			camera.addToHUD(scoreString2);
		}
		
	}
	
	
	
	private void initGameObjects() {
		plants1 = new Group("rootNode");
		other = new Group("otherNode");

		skybox = new SkyBox("SkyBox", 200.0f, 200.0f, 200.0f);
		skybox.setTexture(SkyBox.Face.North, TextureManager.loadTexture2D("images/skybox/center.jpg"));
		skybox.setTexture(SkyBox.Face.South, TextureManager.loadTexture2D("images/skybox/back.jpg"));
		skybox.setTexture(SkyBox.Face.East, TextureManager.loadTexture2D("images/skybox/right.jpg"));
		skybox.setTexture(SkyBox.Face.West, TextureManager.loadTexture2D("images/skybox/left.jpg"));
		skybox.setTexture(SkyBox.Face.Up, TextureManager.loadTexture2D("images/skybox/top.jpg"));
		skybox.setTexture(SkyBox.Face.Down, TextureManager.loadTexture2D("images/skybox/bottom.jpg"));
		this.addGameWorldObject(skybox);
		
		ScriptEngineManager factory = new ScriptEngineManager();
		//String scriptFileName = "t.js";
		String scriptFileName = "rocketBallClient/javaScript/t.js";
		
		// get the JavaScript engine
		ScriptEngine jsEngine = factory.getEngineByName("js");
		this.executeScript(jsEngine, scriptFileName);
		// get terrain
		terrain = (TerrainBlock) jsEngine.get("terrain");
		
		// get grassTexture
		Texture grassTexture = (Texture) jsEngine.get("grassTexture");
		grassTexture.setApplyMode(sage.texture.Texture.ApplyMode.Replace);
		TextureState grassState;
		grassState = (TextureState) renderer.createRenderState(RenderStateType.Texture);
		grassState.setTexture(grassTexture, 0);
		grassState.setEnabled(true);
		terrain.setRenderState(grassState);
		addGameWorldObject(terrain);
		
		
		
		
		aSphere = new Sphere();
		sphereMatrix = aSphere.getLocalTranslation();
		sphereMatrix.scale(1 + rand.nextDouble(),
				1 + rand.nextDouble(),
				1 + rand.nextDouble()
				);
		sphereMatrix.translate((rand.nextInt(40) - 20),
				1,
				(rand.nextInt(40) - 20)
				);
		aSphere.setLocalTranslation(sphereMatrix);
		
		aSphere.updateWorldBound();
		
		plants1.addChild(aSphere);

		int i = 0;
		while (i < plants.length) {
			plantsCapturedTracker[i] = false;
			plants[i] = new MyPyramid();
			plantsMatrix[i] = plants[i].getLocalTranslation();
			plantsMatrix[i].scale((1 + rand.nextDouble()),
					(1 + rand.nextDouble()),
					(1 + rand.nextDouble())
					);
			plantsMatrix[i].translate((rand.nextInt(40) - 20),
					1,
					(rand.nextInt(40) - 20)
					);
			plants[i].setLocalTranslation(plantsMatrix[i]);
			plants[i].updateWorldBound();
			eventMgr.addListener(plants[i], CrashEvent.class);
			plants1.addChild(plants[i]);
			i++;
		}
		i = 0;
		
		myShape = new CustomShape();
		msM = myShape.getLocalTranslation();
		msM.translate(5, 1, 0);
		msM.rotateY(180);
		msM.scale(10, 10, 10);
		myShape.setLocalTranslation(msM);
		eventMgr.addListener(myShape, CrashEvent.class);
		other.addChild(myShape);
		
		x1 = new Point3D(-100, 0, 0);
		x2 = new Point3D(100, 0, 0);
		y1 = new Point3D(0, -100, 0);
		y2 = new Point3D(0, 100, 0);
		z1 = new Point3D(0, 0, -100);
		z2 = new Point3D(0, 0, 100);
		xAxis = new Line(x1, x2, Color.red, 2);
		yAxis = new Line(y1, y2, Color.green, 2);
		zAxis = new Line(z1, z2, Color.blue, 2);

		addGameWorldObject(xAxis);
		addGameWorldObject(yAxis);
		addGameWorldObject(zAxis);
		
		addGameWorldObject(other);
		addGameWorldObject(plants1);
		
//		mtc = new MySpinController();
//		msc = new MyScaleController();
//		
//		connectController(other, mtc);
//		connectController(plants1, msc);
	}
	
	private void executeScript(ScriptEngine engine, String scriptFileName) {
		try {
			FileReader fileReader = new FileReader(scriptFileName);
			engine.eval(fileReader);
			fileReader.close();
		} catch (FileNotFoundException e1) {
			System.out.println(scriptFileName + " not found " + e1);
		} catch (IOException e2) {
			System.out.println("IO problem with " + scriptFileName + e2);
		} catch (ScriptException e3) {
			System.out.println("ScriptException in " + scriptFileName + e3);
		} catch (NullPointerException e4) {
			System.out.println("Null ptr exception in " + scriptFileName + e4);
		}
	}
	
	public void connectController(Group g, Controller c) {
		c.addControlledNode(g);
		g.addController(c);
	}
	
//	public static void main(String [] args) {
//		new RocketBall().start();
//		
//	}

	public void setIsConnected(boolean b) {
		this.isConnected = b;		
	}

	public Vector3D getPlayerPosition() {
		Vector3D out = player.getWorldTranslation().getCol(3);
		return out;
	}
	
	public Model3DTriMesh playerPeek() {
		return player;
		
	}

//	public boolean npcGameOver(BoundingVolume c) {
//		System.out.print(c);
//		if (c.intersects(player2.getWorldBound())) {
//			return true;
//		} else {
//			return false;
//		}
//	}
	
	public TriMesh addGhostToGame(float x, float y, float z) {
//		Pyramid ghost = new Pyramid();
		Model3DTriMesh ghost = getPlayerAvatar();
		Matrix3D m = new Matrix3D();
		
		m.translate(x, y, z);
		ghost.setLocalTranslation(m);
		this.addGameWorldObject(ghost);
		return ghost;
	}

	public void removeGhostFromGame(TriMesh ghost) {
		removeGameWorldObject(ghost);
		
	}


	public void addGhostNPCtoGameWorld(Cube c) {
		addGameWorldObject(c);
		
	}
	
}
