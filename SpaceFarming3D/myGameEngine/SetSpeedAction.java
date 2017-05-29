package myGameEngine;

import net.java.games.input.Event;
import sage.input.action.AbstractInputAction;
import sage.input.action.IAction;

public class SetSpeedAction extends AbstractInputAction implements IAction {
	private boolean running;
	
	public SetSpeedAction() {
		running = false;
	}
	
	public boolean isRunning() {
		return running;
	}
	
	@Override
	public void performAction(float arg0, Event arg1) {
		System.out.println("changed");
		running = !running;
	}
	
}
