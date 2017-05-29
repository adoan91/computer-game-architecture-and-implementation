package myGameEngine;

import graphicslib3D.Point3D;
import graphicslib3D.Vector3D;
import sage.camera.ICamera;
import sage.input.action.AbstractInputAction;
import sage.input.action.IAction;

public class MoveYAxis extends AbstractInputAction implements IAction {
	private ICamera camera;
	private float speed;
	
	public MoveYAxis(ICamera c, float s) {
		camera = c;
		speed = s;
	}
	
	@Override
	public void performAction(float time, net.java.games.input.Event e) {
		
		Vector3D viewDir = camera.getViewDirection().normalize();
		Vector3D curLocVector = new Vector3D(camera.getLocation());
	
//		float amount = e.getValue();
		
		Vector3D newLocVector = new Vector3D();
//		System.out.println(e.getValue());
		
		if (e.getValue() < -0.2) { // forward
			newLocVector = curLocVector.add(viewDir.mult(speed * time));
		} else {
			if (e.getValue() > 0.2) { // back
				newLocVector = curLocVector.minus(viewDir.mult(speed * time));
			} else {
				newLocVector = curLocVector;
			}
		}
		
		// create a point for the new location
		
		double newX = newLocVector.getX();
		double newY = newLocVector.getY();
		double newZ = newLocVector.getZ();
		Point3D newLoc = new Point3D(newX, newY, newZ);
		camera.setLocation(newLoc);
	}

}
