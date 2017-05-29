package rocketBallClient.networking;


import graphicslib3D.Matrix3D;
import graphicslib3D.Point3D;
import graphicslib3D.Vector3D;
import rocketBallClient.RocketBall;
import rocketBallClient.sceneGraph.gameObjects.GhostNPC;

import java.io.IOException;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;
import java.util.Vector;

//import objects.Avatar;
import sage.networking.client.GameConnectionClient;
import sage.scene.Model3DTriMesh;
import sage.scene.TriMesh;
import sage.scene.bounding.BoundingVolume;
import sage.scene.shape.Cube;
import sage.scene.shape.Pyramid;
import sage.scene.shape.Sphere;

public class MyClient extends GameConnectionClient {
	private RocketBall game;
	private UUID id;
	private HashMap<UUID, Model3DTriMesh> ghostAvatars;
	private HashMap <Integer, Cube> ghostNPCs;
	private int ghostAvatarCounter = 0;
	private ArrayList<Integer> npcsHit = new ArrayList<Integer>();
	
	public MyClient(InetAddress remAddr, int remPort, ProtocolType pType, RocketBall game) throws IOException {
		super(remAddr, remPort, pType);
		this.game = game;
		this.id = UUID.randomUUID();
		this.ghostAvatars = new HashMap<UUID, Model3DTriMesh>();
		this.ghostNPCs = new HashMap<Integer, Cube>();
	}

	// override
	protected void processPacket(Object msg) {
		// extract incoming message into substrings. Then process:
		
		String message = (String) msg;
		String [] msgTokens = message.split(",");
				

		
		if (msgTokens[0].compareTo("join") == 0) { // receive "join"
			// format: join, success or join, failure
			
			if (msgTokens[1].compareTo("success") == 0) {
				game.setIsConnected(true);
				sendCreateMessage(game.getPlayerPosition());
				this.sendWantsMessage();
				
			}
			if (msgTokens[1].compareTo("failure") == 0){
				game.setIsConnected(false);
			}
		}
		
		if (msgTokens[0].compareTo("bye") == 0) { // receive "bye"
			// format: bye, remoteId
			UUID ghostID = UUID.fromString(msgTokens[1]);
			removeGhostAvatar(ghostID);
		}
		
		if (msgTokens[0].compareTo("dsfr") == 0) { // receive "details for"
			// format: create, remoteId, x, y, z or dsfr, remoteId, x, y, z
			
			UUID ghostID = UUID.fromString(msgTokens[1]);
			if (this.ghostAvatars.containsKey(ghostID) == false) {
				String [] ghostPosition = { msgTokens[2], msgTokens[3], msgTokens[4] };
				// extract ghost x, y, z position from message, then:
				createGhostAvatar(ghostID, ghostPosition);
			}
			
			
		}
		
		if (msgTokens[0].compareTo("create") == 0) { // receive "create"
			UUID ghostID = UUID.fromString(msgTokens[1]);
			
			if (this.ghostAvatars.containsKey(ghostID) == false) {
				String [] ghostPos = { msgTokens[2], msgTokens[3], msgTokens[4] };
				createGhostAvatar(ghostID, ghostPos);
			}
		}
		
		if (msgTokens[0].compareTo("wsds") == 0) { // receive "wants"
			UUID ghostID = UUID.fromString(msgTokens[1]);
			Vector3D pos = game.getPlayerPosition();
			sendDetailsForMessage(ghostID, pos);
		}
		
		if (msgTokens[0].compareTo("move") == 0) { // receive "move"
			UUID ghostID = UUID.fromString(msgTokens[1]);
			
			if (this.ghostAvatars.containsKey(ghostID) == true) {
				TriMesh ghost = ghostAvatars.get(ghostID);
				float x = Float.parseFloat(msgTokens[2]);
				float y = Float.parseFloat(msgTokens[3]);
				float z = Float.parseFloat(msgTokens[4]);
				Matrix3D translate = new Matrix3D();
				translate.translate(x, y, z);
				ghost.setLocalTranslation(translate);
			}
		}
		
		if (msgTokens[0].compareTo("mnpc") == 0) { 
			int ghostID = Integer.parseInt(msgTokens[1]);
			
			if (npcsHit.contains(ghostID) == false) {
				Vector3D ghostPosition = new Vector3D();
				ghostPosition.setX(Double.parseDouble(msgTokens[2]));
				ghostPosition.setY(Double.parseDouble(msgTokens[3]));
				ghostPosition.setZ(Double.parseDouble(msgTokens[4]));
//				System.out.println(ghostPosition.toString());
				if (game.playerPeek().getWorldBound().contains(new Point3D(ghostPosition.getX(), ghostPosition.getY(), ghostPosition.getZ()))) {
					System.out.println("collided");
					npcsHit.add(ghostID);
					try {
						sendPacket(new String("collide," + id.toString() + "," + ghostID));
					} catch (IOException e) {
						e.printStackTrace();
					}
				} else {
					updateGhostNPC(ghostID, ghostPosition);
				}
				
				
			}
			
			
		}

	}
	
	public void askForNPCinfo() {
		try {
			sendPacket(new String("needNPC," + id.toString()));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
//	private void createGhostNPC(int id, Vector3D position) {
//		GhostNPC newNPC = new GhostNPC(id, position);
//		ghostNPCs.add(newNPC);
//		game.addGhostNPCtoGameWorld(newNPC);
//	}
	
	private void updateGhostNPC(int id, Vector3D position/*, int danger*/) {
//		if (ghostNPCs.size() > id) {
//			ghostNPCs.get(id).setPosition(position);
//			createGhostNPC(ghostNPCs.get(id).id, ghostNPCs.get(id).getWorldTranslation().getCol(3));
//		}
		Cube c = new Cube();
		Matrix3D m = new Matrix3D();
		
		m.translate(position.getX(), position.getY(), position.getZ());
		c.setLocalTranslation(m);
		
		if (ghostNPCs.containsKey(id)) {
			ghostNPCs.get(id).setLocalTranslation(m);
//			game.npcGameOver(ghostNPCs.get(id));
			
		} else {
			ghostNPCs.put(id, c);
			game.addGhostNPCtoGameWorld(c);
//			game.npcGameOver(ghostNPCs.get(id));
		}
//		System.out.println(game.playerPeek().getWorldBound());
//		if (ghostNPCs.get(id) != null) {
//			System.out.println(c.getWorldBound());
//			boolean s = game.npcGameOver(c.getWorldBound());
//			game.setGameOver(s);
//		}
//		System.out.println(id);
		
	}
	
//	public boolean npcGameOver(BoundingVolume b) {
//		for (int i = 0; ghostNPCs.size() > i; i++) {
//			if (ghostNPCs.get(i) != null) {
//				boolean s = b.intersects(ghostNPCs.get(i).getWorldBound());
//				if (s == true) {
//					return true;
//				}
//			}
//		}
//		return false;
//	}
	

	public void sendCreateMessage(Vector3D pos) {
		// format: (create, localid, x, y, z)
		try {
			String message = new String("create," + id.toString());
			message += "," + pos.getX() + "," + pos.getY() + "," + pos.getZ();
			sendPacket(message);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void sendJoinMessage() {
		// format: join, localid
		try {
			sendPacket(new String("join," + id.toString()));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void sendByeMessage() {
		try {
			sendPacket(new String("bye," + id.toString()));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}


	public void sendDetailsForMessage(UUID remId, Vector3D pos) {
		try {
			String message = new String("dsfr," + id.toString() + "," + remId.toString());
			message += "," + pos.getX() + "," + pos.getY() + "," + pos.getZ();
			sendPacket(message);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	
	public void sendMoveMessage(Vector3D pos) {
		try {
			String message = new String("move," + id.toString());
			message += "," + pos.getX() + "," + pos.getY() + "," + pos.getZ();
			sendPacket(message);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}


	private void createGhostAvatar(UUID ghostID, String[] position) {
		float x = Float.parseFloat(position[0]);
		float y = Float.parseFloat(position[1]);
		float z = Float.parseFloat(position[2]);
		TriMesh ghost = game.addGhostToGame(x, y, z);
		// ghosts will be pyramids for now, will make into something else later
		ghostAvatars.put(ghostID, (Model3DTriMesh) /*(Pyramid)*/ ghost);
		this.ghostAvatarCounter++;
	}

	private void removeGhostAvatar(UUID id) {
		TriMesh ghost = ghostAvatars.get(id);
		if(ghost != null){
			game.removeGhostFromGame(ghost);
		}
	}

	
	
	public void sendWantsMessage() {
		try {
			sendPacket(new String("wsds," + id.toString()));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	


	
	

}
