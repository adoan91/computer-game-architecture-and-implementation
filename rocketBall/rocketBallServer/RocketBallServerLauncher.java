package rocketBallServer;

import java.io.IOException;

public class RocketBallServerLauncher {
	
	public static void main(String[] args) throws IOException {
//		new GameServer(Integer.parseInt(args[0]));
		new GameServer(50001);
	}
}
