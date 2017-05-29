package rocketBallClient.events;

import sage.input.action.AbstractInputAction;
import sage.input.action.IAction;
import sage.app.AbstractGame;
import net.java.games.input.Event;

public class QuitGameAction extends AbstractInputAction implements IAction {

	private AbstractGame game;
	
	public QuitGameAction(AbstractGame g) {
		// constructor
		this.game = g;
	}
	
	// Sets the "game over" flag in the game associated with this
	// IAction to true. The time and event parameters are ignored.
	
	@Override
	public void performAction(float arg0, Event arg1) {
		game.setGameOver(true);
	}

}
