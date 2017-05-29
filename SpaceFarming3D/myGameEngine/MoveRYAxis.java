package myGameEngine;

import graphicslib3D.Matrix3D;
import sage.camera.ICamera;
import sage.input.action.AbstractInputAction;
import sage.input.action.IAction;

public class MoveRYAxis extends AbstractInputAction implements IAction {
	private ICamera camera;
	private float speed;
	
	public MoveRYAxis(ICamera c, float s) {
		camera = c;
		speed = s;
	}
	
	@Override
	public void performAction(float time, net.java.games.input.Event e) {
		Matrix3D rotationAmt = new Matrix3D();
//		System.out.println(e.getValue());
		if (e.getValue() < -0.2) { // up
			rotationAmt.setToIdentity();
			rotationAmt.rotate(speed, camera.getRightAxis());
			 
			camera.setUpAxis(camera.getUpAxis()
					.mult(rotationAmt)
					.normalize()
					);
			camera.setViewDirection(camera.getViewDirection()
					.mult(rotationAmt)
					.normalize());
		} else {
			if (e.getValue() > 0.2) { // down
				
				rotationAmt.setToIdentity();
				rotationAmt.rotate(-speed, camera.getRightAxis());
				 
				camera.setUpAxis(camera.getUpAxis()
						.mult(rotationAmt)
						.normalize()
						);
				camera.setViewDirection(camera.getViewDirection()
						.mult(rotationAmt)
						.normalize());
			} else {
				
			}
		}
		
	}

}
