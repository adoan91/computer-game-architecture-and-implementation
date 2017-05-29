package rocketBallClient.sceneNodeControllers;

import graphicslib3D.Matrix3D;
import sage.scene.Controller;
import sage.scene.SceneNode;

public class MySpinController extends Controller {

	private double translationRate = 0.02; // movement per second
	private double cycleTime = 10000.0; // default cycle time
	private double totalTime;
	private double direction = 1.0;
	
	public void setCycleTime(double c) {
		cycleTime = c;
	}
	
	@Override
	public void update(double time) { // example controller
		totalTime += time;
		double transAmount = translationRate * time;
		
		if(totalTime > cycleTime) {
			direction = -direction;
			totalTime = 0.0;
		}
		
		transAmount = direction * transAmount;
		
		Matrix3D newTrans = new Matrix3D();
		newTrans.rotate(transAmount, 0, 0);
		
		for (SceneNode node : controlledNodes) {
			Matrix3D curTrans = node.getLocalRotation();
			curTrans.concatenate(newTrans);
			node.setLocalRotation(curTrans);
		}
	}
	
}
