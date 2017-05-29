package myGameEngine;

import graphicslib3D.Matrix3D;
import graphicslib3D.Vector3D;
import sage.camera.ICamera;
import sage.input.action.AbstractInputAction;
import sage.input.action.IAction;

public class MoveRXAxis extends AbstractInputAction implements IAction {
	private ICamera camera;
	private float speed;
	
	public MoveRXAxis(ICamera c, float s) {
		camera = c;
		speed = s;
	}
	
	@Override
	public void performAction(float time, net.java.games.input.Event e) {
//		System.out.println(e.getValue());
		Matrix3D rotationAmt = new Matrix3D();
//		float amount = e.getValue();
		
		if (e.getValue() < -0.2) { // left
			
			rotationAmt.setToIdentity();
			rotationAmt.rotate(speed, new Vector3D(0, 1, 0));
			 
			camera.setUpAxis(camera.getUpAxis()
					.mult(rotationAmt)
					.normalize()
					);
			camera.setViewDirection(camera.getViewDirection()
					.mult(rotationAmt)
					.normalize());
			camera.setRightAxis(camera.getRightAxis()
					.mult(rotationAmt)
					.normalize());
			
			
		} else {
			if (e.getValue() > 0.2) { // right
				
				rotationAmt.setToIdentity();
				rotationAmt.rotate(-speed, new Vector3D(0, 1, 0));
				 
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
				
				
			} else {
				
			}
		}
		

	}

}
