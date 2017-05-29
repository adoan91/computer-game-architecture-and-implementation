package a2;

import java.awt.Color;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Random;
import graphicslib3D.Matrix3D;
import graphicslib3D.Point3D;
import graphicslib3D.Vector3D;
import myGameEngine.MyDisplaySystem;
import myGameEngine.MyScaleController;
import myGameEngine.MySpinController;
import myGameEngine.OrbitCameraController;
import myGameEngine.BackwardAction;
import myGameEngine.CrashEvent;
import myGameEngine.ForwardAction;
import myGameEngine.LeftAction;
import myGameEngine.MoveXAxis;
import myGameEngine.MoveYAxis;
import myGameEngine.QuitGameAction;
import myGameEngine.RightAction;
import sage.app.BaseGame;
import sage.camera.ICamera;
import sage.camera.JOGLCamera;
import sage.display.IDisplaySystem;
import sage.event.EventManager;
import sage.event.IEventManager;
import sage.input.IInputManager;
import sage.input.InputManager;
import sage.input.action.IAction;
import sage.renderer.IRenderer;
import sage.scene.Controller;
import sage.scene.Group;
import sage.scene.HUDString;
import sage.scene.SceneNode;
import sage.scene.shape.Cube;
import sage.scene.shape.Line;
import sage.scene.shape.Rectangle;
import sage.scene.shape.Sphere;
import myGameEngine.MyDisplaySystem;


public class MyGame extends BaseGame {
	private MySpinController mtc;
	private MyScaleController msc;
	private IDisplaySystem display;
	private ICamera camera1, camera2;
	private IInputManager im;
	private IEventManager eventMgr;
	
	private IRenderer renderer;
	private SceneNode player1, player2;
	private OrbitCameraController orbit0Controller, orbit1Controller;
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
	
	private IDisplaySystem createDisplaySystem() {
		display = new MyDisplaySystem(
				1920,
				1200,
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
		display.close();
		// other shutdown methods here as necessary
	}
	
	protected void initSystem() {
		// call a local method to create a DisplaySystem object
		IDisplaySystem disp = createDisplaySystem();
		setDisplaySystem(disp);
	
		// create an Input Manager
		IInputManager im = new InputManager();
		setInputManager(im);
		
		// create an (empty) gameworld
		ArrayList<SceneNode> gameWorld = new ArrayList<SceneNode>();
		setGameWorld(gameWorld);
	}

	@Override
	protected void initGame() {
		
		display = getDisplaySystem();
		getDisplaySystem().setTitle("Space Farming 3D2");
		renderer = display.getRenderer();
		eventMgr = EventManager.getInstance();
		createPlayers();
		initGameObjects();
		im = getInputManager();
		kb = im.getKeyboardName();
		gpName = im.getFirstGamepadName();
		mouseName = im.getMouseName();
		
		orbit1Controller = new OrbitCameraController(
				camera2,
				player2,
				im,
				kb
				);
		
		IAction xAxisMove = new MoveXAxis(player1);
		IAction yAxisMove = new MoveYAxis(player1);
		
		IAction mvForward = new ForwardAction(player2);
		IAction mvBackward = new BackwardAction(player2);
		IAction mvLeft = new LeftAction(player2);
		IAction mvRight = new RightAction(player2);
		
		IAction quit = new QuitGameAction(this);
		
		if (gpName != null) {
			
			orbit0Controller = new OrbitCameraController(
					camera1,
					player1,
					im,
					gpName
					);
			
			im.associateAction( // left and right
					gpName, 
					net.java.games.input.Component.Identifier.Axis.X, 
					xAxisMove,
					IInputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN
					);
			
			im.associateAction( // forward and backward
					gpName, 
					net.java.games.input.Component.Identifier.Axis.Y, 
					yAxisMove,
					IInputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN
					);
		}

		im.associateAction(
				kb, 
				net.java.games.input.Component.Identifier.Key.W, 
				mvForward,
				IInputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN
				);
		
		im.associateAction(
				kb, 
				net.java.games.input.Component.Identifier.Key.A, 
				mvLeft,
				IInputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN
				);
		
		im.associateAction(
				kb, 
				net.java.games.input.Component.Identifier.Key.D, 
				mvRight,
				IInputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN
				);
		
		im.associateAction(
				kb, 
				net.java.games.input.Component.Identifier.Key.S, 
				mvBackward,
				IInputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN
				);
				
		im.associateAction(
				kb, 
				net.java.games.input.Component.Identifier.Key.ESCAPE, 
				quit,
				IInputManager.INPUT_ACTION_TYPE.ON_PRESS_ONLY
				);

		super.update(0.0f);
	}
	
	private void createPlayers() {
		player1 = new Cube("Player 1");
		player1.scale(0.25f, 0.25f, 0.25f);
		player1.translate(0, 1, 50);
		player1.rotate(180, new Vector3D(0,1,0));
		addGameWorldObject(player1);
		
		camera1 = new JOGLCamera(renderer);
		camera1.setPerspectiveFrustum(60, 1, 1, 1000);
		camera1.setViewport(0.0, 0.5, 0.0, 1.0);
		
		player2 = new Cube("Player 2");
		player2.scale(0.25f, 0.25f, 0.25f);
		player2.translate(50, 1, 0);
		player2.rotate(0, new Vector3D(0,1,0));
		addGameWorldObject(player2);
		
		camera2 = new JOGLCamera(renderer);
		camera2.setPerspectiveFrustum(60, 1, 1, 1000);
		camera2.setViewport(0.5, 1.0, 0.0, 1.0);
		createPlayerHUDs();
	}

	private void createPlayerHUDs() {

		timeString = new HUDString("Time = " +
				time
				);
		timeString.setLocation(0, 0.1); // (0,0) [lower-left] to (1,1)

		addGameWorldObject(timeString);
		
		scoreString = new HUDString ("Score = " +
				p1Score
				); //default is (0,0)
		scoreString.setName("p1Score");
		scoreString.setLocation(0.01, 0.04);
		scoreString.setRenderMode(sage.scene.SceneNode.RENDER_MODE.ORTHO);
		scoreString.setColor(Color.red);
		scoreString.setCullMode(sage.scene.SceneNode.CULL_MODE.NEVER);
		camera1.addToHUD(scoreString);
		
		scoreString2 = new HUDString ("Score = " +
				p2Score
				); //default is (0,0)
		scoreString2.setName("p2Score");
		scoreString2.setLocation(0.01, 0.04);
		scoreString2.setRenderMode(sage.scene.SceneNode.RENDER_MODE.ORTHO);
		scoreString2.setColor(Color.red);
		scoreString2.setCullMode(sage.scene.SceneNode.CULL_MODE.NEVER);
		camera2.addToHUD(scoreString2);
		
		HUDString player1ID = new HUDString("Player 1");
		player1ID.setName("Player1ID");
		player1ID.setLocation(0.01, 0.06);
		player1ID.setRenderMode(sage.scene.SceneNode.RENDER_MODE.ORTHO);
		player1ID.setColor(Color.red);
		player1ID.setCullMode(sage.scene.SceneNode.CULL_MODE.NEVER);
		camera1.addToHUD(player1ID);
		
		HUDString player2ID = new HUDString("Player 2");
		player2ID.setName("Player2ID");
		player2ID.setLocation(0.01, 0.06);
		player2ID.setRenderMode(sage.scene.SceneNode.RENDER_MODE.ORTHO);
		player2ID.setColor(Color.red);
		player2ID.setCullMode(sage.scene.SceneNode.CULL_MODE.NEVER);
		camera2.addToHUD(player2ID);
	}

	public void checkGameOver() {
		
		if ((p1Score + p2Score) == 11) {
			// game over
			gameOverFlag = true;
			System.out.print("You win");
		}
	}
	
	protected void render() {
		renderer.setCamera(camera1);
		super.render();
		
		renderer.setCamera(camera2);
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
		
		if (p1Score + p2Score == 11) {
			if (p1Score > p2Score) {
				camera1.removeFromHUD(camera1.getHUD().getChild("p1Score"));
				camera2.removeFromHUD(camera2.getHUD().getChild("p2Score"));
				scoreString = new HUDString ("YOU WIN!"
						); //default is (0,0)
				scoreString.setName("p1Score");
				scoreString.setLocation(0.01, 0.04);
				scoreString.setRenderMode(sage.scene.SceneNode.RENDER_MODE.ORTHO);
				scoreString.setColor(Color.red);
				scoreString.setCullMode(sage.scene.SceneNode.CULL_MODE.NEVER);
				camera1.addToHUD(scoreString);
				
				scoreString2 = new HUDString ("Score = " +
						p2Score
						); //default is (0,0)
				scoreString2.setName("p2Score");
				scoreString2.setLocation(0.01, 0.04);
				scoreString2.setRenderMode(sage.scene.SceneNode.RENDER_MODE.ORTHO);
				scoreString2.setColor(Color.red);
				scoreString2.setCullMode(sage.scene.SceneNode.CULL_MODE.NEVER);
				camera2.addToHUD(scoreString2);
			}
			else if (p2Score > p1Score) {
				camera1.removeFromHUD(camera1.getHUD().getChild("p1Score"));
				camera2.removeFromHUD(camera2.getHUD().getChild("p2Score"));
				scoreString = new HUDString ("Score = " +
						p1Score
						); //default is (0,0)
				scoreString.setName("p1Score");
				scoreString.setLocation(0.01, 0.04);
				scoreString.setRenderMode(sage.scene.SceneNode.RENDER_MODE.ORTHO);
				scoreString.setColor(Color.red);
				scoreString.setCullMode(sage.scene.SceneNode.CULL_MODE.NEVER);
				camera1.addToHUD(scoreString);
				
				scoreString2 = new HUDString ("YOU WIN!"
						); //default is (0,0)
				scoreString2.setName("p2Score");
				scoreString2.setLocation(0.01, 0.04);
				scoreString2.setRenderMode(sage.scene.SceneNode.RENDER_MODE.ORTHO);
				scoreString2.setColor(Color.red);
				scoreString2.setCullMode(sage.scene.SceneNode.CULL_MODE.NEVER);
				camera2.addToHUD(scoreString2);
			} else { // this won't happen unless I add more plants in the future
				camera1.removeFromHUD(camera1.getHUD().getChild("p1Score"));
				camera2.removeFromHUD(camera2.getHUD().getChild("p2Score"));
				scoreString = new HUDString ("TIE GAME!"
						); //default is (0,0)
				scoreString.setName("p1Score");
				scoreString.setLocation(0.01, 0.04);
				scoreString.setRenderMode(sage.scene.SceneNode.RENDER_MODE.ORTHO);
				scoreString.setColor(Color.red);
				scoreString.setCullMode(sage.scene.SceneNode.CULL_MODE.NEVER);
				camera1.addToHUD(scoreString);
				
				scoreString2 = new HUDString ("TIE GAME!"
						); //default is (0,0)
				scoreString2.setName("p2Score");
				scoreString2.setLocation(0.01, 0.04);
				scoreString2.setRenderMode(sage.scene.SceneNode.RENDER_MODE.ORTHO);
				scoreString2.setColor(Color.red);
				scoreString2.setCullMode(sage.scene.SceneNode.CULL_MODE.NEVER);
				camera2.addToHUD(scoreString2);
			}
			
		} else {
			camera1.removeFromHUD(camera1.getHUD().getChild("p1Score"));
			camera2.removeFromHUD(camera2.getHUD().getChild("p2Score"));
			scoreString = new HUDString ("Score = " +
					p1Score
					); //default is (0,0)
			scoreString.setName("p1Score");
			scoreString.setLocation(0.01, 0.04);
			scoreString.setRenderMode(sage.scene.SceneNode.RENDER_MODE.ORTHO);
			scoreString.setColor(Color.red);
			scoreString.setCullMode(sage.scene.SceneNode.CULL_MODE.NEVER);
			camera1.addToHUD(scoreString);
			
			scoreString2 = new HUDString ("Score = " +
					p2Score
					); //default is (0,0)
			scoreString2.setName("p2Score");
			scoreString2.setLocation(0.01, 0.04);
			scoreString2.setRenderMode(sage.scene.SceneNode.RENDER_MODE.ORTHO);
			scoreString2.setColor(Color.red);
			scoreString2.setCullMode(sage.scene.SceneNode.CULL_MODE.NEVER);
			camera2.addToHUD(scoreString2);
		}
		
	}
	
	@Override
	public void update(float elapsedTimeMS) {
		orbit1Controller.update(elapsedTimeMS);
		
		if (gpName != null) {
			orbit0Controller.update(elapsedTimeMS);
		}
		
		if (gameOverFlag != true) {
			
			if (sphereGotHit == false) {
				
				if (aSphere.getWorldBound().intersects(player2.getWorldBound())) {
					sphereHit();
					p2Score++;
					updateScore();
				}
				else if (aSphere.getWorldBound().intersects(player1.getWorldBound())) {
					sphereHit();
					p1Score++;
					updateScore();
				}
				
			}
			
			int i = 0;
			while (i < plants.length) {
				
				if (plants[i]
						.getWorldBound()
						.intersects(player2.getWorldBound()) && 
						plantsCapturedTracker[i] == false) {
				
					plantHit(i);
					p2Score++;
					updateScore();
				}
				else if (plants[i]
						.getWorldBound()
						.intersects(player1.getWorldBound()) && 
						plantsCapturedTracker[i] == false) {
					
					plantHit(i);
					p1Score++;
					updateScore();
				}
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
		
		super.update(elapsedTimeMS);
	}
	
	private void initGameObjects() {
		plants1 = new Group("rootNode");
		other = new Group("otherNode");

		flatGroundPlane = new Rectangle(300.0f, 300.0f);
		flatGroundPlane.setColor(Color.ORANGE);
		flatGroundPlane.rotate(90, new Vector3D(1, 0, 0));
		
		addGameWorldObject(flatGroundPlane);
		
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
		
		mtc = new MySpinController();
		msc = new MyScaleController();
		
		connectController(other, mtc);
		connectController(plants1, msc);
	}
	
	public void connectController(Group g, Controller c) {
		c.addControlledNode(g);
		g.addController(c);
	}
	
	public static void main(String [] args) {
//		new MyGame().start();
		
	}
	
}
