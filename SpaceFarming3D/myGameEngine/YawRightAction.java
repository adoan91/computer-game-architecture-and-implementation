package myGameEngine;

import graphicslib3D.Matrix3D;
import graphicslib3D.Vector3D;
import net.java.games.input.Event;
import sage.camera.ICamera;
import sage.input.action.AbstractInputAction;
import sage.input.action.IAction;

public class YawRightAction extends AbstractInputAction implements IAction {
	
	private ICamera camera;
	private SetSpeedAction runAction;
	
	public YawRightAction(ICamera c, SetSpeedAction r) {
		camera = c;
		runAction = r;
	}

	@Override
	public void performAction(float arg0, Event arg1) {
		float moveAmount;
		
		if (runAction.isRunning()) {
			moveAmount = (float) -0.99;
		} else {
			moveAmount = (float) -0.25;
		}
		
		Matrix3D rotationAmt = new Matrix3D();
				
		rotationAmt.rotate(moveAmount, new Vector3D(0, 1, 0));
		 
		camera.setUpAxis(camera.getUpAxis()
				.mult(rotationAmt)
				.normalize()
				);
		camera.setViewDirection(camera.getViewDirection()
				.mult(rotationAmt)
				.normalize()
				);
		camera.setRightAxis(camera.getRightAxis()
				.mult(rotationAmt)
				.normalize()
				);
		
	}

}