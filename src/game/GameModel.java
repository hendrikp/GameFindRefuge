package game;

/**
 * @author hpolczynski 02.02.2014
 * oop game project
 */

import java.awt.Point;
import java.awt.Rectangle;
import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.concurrent.ConcurrentHashMap;

import rmi.GameClient;
import rmi.GameClientInfo;
import rmi.RMIClient;
import rmi.RMIController;
import rmi.RMIServer;

/**
 * game model that handles the state of the game
 * implements the client and server model (and switches between them based on which is active)
 */
public class GameModel extends IGameModel {
	private static final String localUrl = "//127.0.0.1/FindRefuge"; //!< local rmi url
	private static final String gameName = "FindRefuge"; //!< the name of the game
	
	/**
	 * get the rmi url
	 * @return local rmi url
	 */
	public String getLocalUrl() {
		return localUrl;
	}
	
	/**
	 * get game name
	 * @return name of the game
	 */
	public String getGameName() {
		return gameName;
	}
	
	private int gameWidth; //!< game field width
	private int gameHeight; //!< game field height
	
	/**
	 * set size of the game field
	 * @param width x
	 * @param height y
	 */
	public void setGameSize(int width, int height)
	{
		gameWidth = width;
		gameHeight = height;
	}
	
	private ConcurrentHashMap<Integer, GameObject> gameObjects; //!< all game objects
	private Controller control; //!< the controller
	
	private boolean server; //!< is this model a server or client?
	
	private RMIServer localServer; //!< local(real) server if present
	private RMIController remoteServer; //!< remote server rmi
	private RMIClient localClient; //! local client rmi

	private boolean running; //!< indicates the game is started
	private int level; //!< the current level

	private static GameModel currentInstance; //!< holds the current game model instance

	private int localClientId; //!< the client id of the local player
	/**
	 * get the local player id
	 * @return local player id
	 */
	@Override
	public int getLocalPlayerId() {
		return localClientId;
	}
	
	/**
	 * Constructor of game (also starts game thread)
	 * @param _control the controller
	 */
	GameModel(Controller _control)
	{
		control = _control;
		localClientId = -1;
		running = false;
		currentInstance = this;
		
		new Thread()
		{
		    public void run() {
		    	try {
		    		GameModel current = GameModel.getInstance();
		    		while(true)
		    		{
			    		current.calcFrame();
						Thread.sleep(GameConfiguration.gameCycleTime);
		    		}
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
		    }
		}.start();
	}
	
	/**
	 * get current instance of model
	 * @return current model
	 */
	public static GameModel getInstance()
	{
		return currentInstance;
	}
	
	/**
	 * handle state of game
	 * @param command controller command or End
	 * @param param optional string parameter
	 * @param param2 optional string parameter
	 * @param param3 optional object parameter
	 */
	@SuppressWarnings("unchecked")
	@Override
	public void notify(String command, String param, String param2, Object param3) {
		switch (command) {
			case "Started" : {
				ready();
				running = true;
				break;
			}
			
			case "Ended" : {
				running = false;
				break;
			}
			
			case "Start" : {
				try {
					remoteServer.start(localClientId);
				} catch (RemoteException e) {
					e.printStackTrace();
				}
				break;
			}
			
			case "End" : {
				try {
					remoteServer.end(localClientId);
				} catch (RemoteException e) {
					e.printStackTrace();
				}
				break;
			}
			
			case "Host" : {
				try {
					// Anlegen des Namensdienstes (Registry):
					LocateRegistry.createRegistry(Registry.REGISTRY_PORT);
					
					// Namen an die Registry uebermitteln:
					localServer = new RMIServer();
					Naming.rebind(gameName, localServer);
					
					// Verbinde nun zu lokalem Server
					control.actionPerformed("Connect", localUrl, param2);
					server = true;
					control.addControllable(localServer);
				} catch (MalformedURLException e) {
					System.out.println(e.getMessage());
					control.actionPerformed("Disconnected", e.getMessage());
				} catch (RemoteException e) {
					System.out.println(e.getMessage());
					control.actionPerformed("Disconnected", e.getMessage());
				} // catch
				break;
			}
			
			case "Connect": {
				server = false;
				try {
					remoteServer = (RMIController) Naming.lookup(param);
					
					localClient = new GameClient();
					localClientId = remoteServer.registerPlayer(param2, localClient);
				
					control.actionPerformed("Connected", param, param2);
				} catch (Exception ex) {
					control.actionPerformed("Disconnected", ex.getMessage());
					ex.printStackTrace();
				} // catch
				
				break;
			}
			
			case "MessageInput": {
				try {
					remoteServer.writeMessage(localClientId, param);
				} catch(Exception ex) {
					ex.printStackTrace();
				}
			}
			
			case "FrameReceived": {
				if(!server)
				{
					gameObjects = (ConcurrentHashMap<Integer, GameObject>) param3;
				}
				setChanged();
				notifyObservers(gameObjects);
				break;
			}
			
			case "Movement": {
				if(running)
				{
					try {
						remoteServer.move(localClientId, (Point) param3);
					} catch (RemoteException e) {
						e.printStackTrace();
					}
				}
				break;
			}
		}
	}
	
	/**
	 * calculate the next frame (only on server)
	 */
	public void calcFrame() {
		if(running)
		{
			// only do work on the server
			if(server)
			{	
				for(GameObject o : gameObjects.values())
				{
					// Limit everything to playfield
					if(o.bounds.getMaxX() > gameWidth) 
						o.bounds.x = gameWidth - o.bounds.width;
					if(o.bounds.x < 0)
						o.bounds.x = 0;
					if(o.bounds.getMaxY() > gameHeight) 
						o.bounds.y = gameHeight - o.bounds.height;
					if(o.bounds.y < 0)
						o.bounds.y = 0;
					
					// Handle types
					switch(o.type)
					{
						case GameConstants.GT_PLAYER: {
							// as long as player isn't in the house
							if(o.visible)
							{
								if(!o.destroyed)
								{
									o.speedx = localServer.clients.get(o.clientId).moveParams.x * GameConfiguration.playerSpeed;
									o.speedy = localServer.clients.get(o.clientId).moveParams.y * GameConfiguration.playerSpeed;
	
									o.bounds.x += o.speedx;
									o.bounds.y += o.speedy;
								}
								
								for(GameObject o2 : gameObjects.values())
								{
									// If caught by monster -> death
									if(o2.type >= GameConstants.GT_ENEMY_BEGIN && o2.type <= GameConstants.GT_ENEMY_END)
									{
										if(o2.bounds.intersects(o.bounds))
										{
											// check if area is at least n pxs
											Rectangle collision = o2.bounds.intersection(o.bounds);
											int area = collision.width * collision.height;
											if(area >= GameConfiguration.minCollisionArea)
											{
												o.destroyed = true;
											}
										}
									}
									
									// Revive player
									if(o2.type == GameConstants.GT_PLAYER && o != o2)
									{
										if(o2.bounds.intersects(o.bounds))
											o.destroyed = false;
									}
								}
							}
							break;
						}
					
						case GameConstants.GT_ENEMY_X: {
							o.bounds.x += o.speedx;
							if(o.bounds.getMaxX() > gameWidth || o.bounds.x < 0) 
								o.speedx *= -1;
							
							break;
						}
						
						case GameConstants.GT_ENEMY_Y: {
							o.bounds.y += o.speedy;
							if(o.bounds.getMaxY() > gameHeight || o.bounds.y < 0) 
								o.speedy *= -1;
							
							break;
						}
						
						case GameConstants.GT_ENEMY_XY: {
							o.bounds.x += o.speedx;
							if(o.bounds.getMaxX() > gameWidth || o.bounds.x < 0) 
								o.speedx *= -1;
		
							o.bounds.y += o.speedy;
							if(o.bounds.getMaxY() > gameHeight || o.bounds.y < 0) 
								o.speedy *= -1;
							
							break;
						}
						
						case GameConstants.GT_ENEMY_SMART: {
							
							// Find nearest player
							GameObject nearestPlayer = null;
							double minDistance = gameHeight * gameWidth; // max
							
							for(GameClientInfo client : localServer.clients.values())
							{
								GameObject player = gameObjects.get(client.gameObjectId);
								if(player != null)
								{
									if(player.visible && !player.destroyed)
									{
										double currentDistance = o.bounds.getLocation().distance(player.bounds.getLocation());
										if(currentDistance < minDistance)
										{
											minDistance = currentDistance;
											nearestPlayer = player;
										}
									}
								}
							}

							if(nearestPlayer != null)
							{
								// Calculate distance
								double diffX = nearestPlayer.bounds.getCenterX() - o.bounds.getCenterX();
								double diffY = nearestPlayer.bounds.getCenterY() - o.bounds.getCenterY();
								
								// Walking direction
								o.speedx = diffX > 0 ? 1 : -1;
								if(diffX < 0.01 && diffX > 0.01)
									o.speedx = 0;
								
								o.speedy = diffY > 0 ? 1 : -1;
								if(diffY < 0.01 && diffY > 0.01)
									o.speedy = 0;
							}
							
							if(o.bounds.getMaxX() > gameWidth || o.bounds.x < 0) 
								o.speedx *= -1;

							if(o.bounds.getMaxY() > gameHeight || o.bounds.y < 0) 
								o.speedy *= -1;
							
							// Don't enter houses/trees
							for(GameObject o2 : gameObjects.values())
							{			
								if(o2.type >= GameConstants.GT_STATIC_BEGIN && o.bounds.intersects(o2.bounds))
								{
									// Calculate distance
									double diffX = o2.bounds.getCenterX() - o.bounds.getCenterX();
									double diffY = o2.bounds.getCenterY() - o.bounds.getCenterY();
									
									// Walking correction
									int revx = diffX > 0 ? -1 : 1;
									if(diffX < 0.01 && diffX > 0.01)
										revx = 0;
									
									int revy = diffY > 0 ? -1 : 1;
									if(diffY < 0.01 && diffY > 0.01)
										revy = 0;
									
									o.bounds.y += revy;
									o.bounds.x += revx;
								}
							}
							
							// Walk
							o.bounds.y += o.speedy;
							o.bounds.x += o.speedx;
							
							break;
						}
						
						case GameConstants.GT_TREE: {
							// Monsters can't climb trees
							for(GameObject o2 : gameObjects.values())
							{
								if(o2.type >= GameConstants.GT_ENEMY_BEGIN && o2.type <= GameConstants.GT_ENEMY_END)
								{
									if(o2.bounds.intersects(o.bounds))
									{
										o2.speedx *= -1;
										o2.speedy *= -1;
									}
								}
							}
							break;
						}
						
						case GameConstants.GT_HOUSE: {
							// Check if player is in house
							for(GameObject o2 : gameObjects.values())
							{			
								if(o2.type == GameConstants.GT_PLAYER )
								{
									if( o.bounds.contains(o2.bounds.getCenterX(), o2.bounds.getCenterY()) )
									{
										o2.visible = false;
										o2.speedx = 0;
										o2.speedy = 0;
									}
								// Monster can't enter house
								} else if(o2.type >= GameConstants.GT_ENEMY_BEGIN && o2.type <= GameConstants.GT_ENEMY_END)
								{
									if( o.bounds.intersects(o2.bounds) )
									{
										o2.speedx *= -1;
										o2.speedy *= -1;
									}
								}
							}
							
							break;
						}
					}
				}
				
				control.actionPerformed("FrameCalculated", gameObjects);
				
				// Check end condition
				int playersSucceeded = 0;
				int playersDeath = 0;
				for(GameClientInfo client : localServer.clients.values())
				{
					GameObject player = gameObjects.get(client.gameObjectId);
					if(player != null)
					{
						if(!player.visible)
						{
							playersSucceeded++;
						}
						
						if(player.destroyed)
						{
							playersDeath++;
						}
					}
				}
				
				// Win? -> next level
				if( playersSucceeded >= localServer.clients.size() )
				{
					++level;
					setupLevel();
					
				// Game Over
				} else if( playersDeath + playersSucceeded >= localServer.clients.size() )
				{
					control.actionPerformed("End");
				}
			}
		}
	}
	
	private int nextObjectId; //!< next free game object id
	
	/**
	 * ready the game (setup level on server)
	 */
	public void ready() {
		gameObjects = new ConcurrentHashMap<Integer,GameObject>();
		if(server)
		{
			level = 1;
			nextObjectId = 1;
			setupLevel();
		}
	}
	
	/**
	 * setup of the current level
	 */
	public void setupLevel() {		
		// cleanup
		gameObjects.clear();
		
		// add players
		for(GameClientInfo client : localServer.clients.values())
		{
			GameObject obj = new GameObject();
			obj.id = nextObjectId++;
			obj.clientId = client.id;
			obj.name = client.name;
			client.gameObjectId = obj.id;
			
			obj.visible = true;
			obj.type = GameConstants.GT_PLAYER;
			
			obj.bounds.height = 32;
			obj.bounds.width = 32;

			obj.bounds.y = 0;
			do {
				obj.bounds.x = (int) Math.max(0, Math.random() * gameWidth - obj.bounds.width);
			} while(checkCollision(obj));
			
			gameObjects.put(obj.id, obj);
		}
		
		// add houses
		int countHouses = Math.max(1, GameConfiguration.levelStartHouses - level);
		for(int i = 0; i < countHouses; ++i)
		{
			GameObject obj = new GameObject();
			obj.id = nextObjectId++;
			obj.visible = true;
			obj.type = GameConstants.GT_HOUSE;
			
			obj.bounds.height = 88;
			obj.bounds.width = 136;
			
			int tries = 0;
			do {
				obj.bounds.x = (int) Math.max(0, Math.random() * gameWidth - obj.bounds.width);
				obj.bounds.y = (int) Math.max(0, ((gameHeight / 10) * (8 + (Math.random() * 2) )) - obj.bounds.height);
			} while(checkCollision(obj) && ++tries < 10);
			if(tries >= 10)
				continue;

			gameObjects.put(obj.id, obj);
		}
		
		// add trees
		int countTrees = Math.max(0, GameConfiguration.levelStartTrees - level);
		for(int i = 0; i < countTrees; ++i)
		{
			GameObject obj = new GameObject();
			obj.id = nextObjectId++;
			obj.visible = true;
			obj.type = GameConstants.GT_TREE;

			obj.bounds.height = 68;
			obj.bounds.width = 56;
			int tries = 0;
			do {
				obj.bounds.x = (int) Math.max(0, Math.random() * gameWidth - obj.bounds.width);
				obj.bounds.y = (int) Math.max(0, Math.random() * gameHeight - obj.bounds.height);
			} while(checkCollision(obj) && ++tries < 10);
			if(tries >= 10)
				continue;
			
			gameObjects.put(obj.id, obj);
		}
		
		// Add monsters
		int enemys = level * GameConfiguration.levelEnemyMultiplier + GameConfiguration.levelMinEnemys;
		int rows = level * 2;
		for(int row = 1; row <= rows; row++)
		{
			int yPos = GameConfiguration.levelSafeZone + ((gameHeight-(GameConfiguration.levelSafeZone +60)) / rows) * row;
			
			int enemysRow = (int)(enemys / rows);
			
			for(int enemy = 0; enemy < enemysRow; enemy++)
			{
				GameObject enemyObj = new GameObject();
				
				enemyObj.id = nextObjectId++;
				enemyObj.visible = true;

				// 1 player 2> enemy
				enemyObj.type = GameConstants.GT_ENEMY_BEGIN + (int) Math.round( Math.random() * (GameConstants.GT_ENEMY_END-GameConstants.GT_ENEMY_BEGIN));
				
				switch(enemyObj.type)
				{
					case GameConstants.GT_ENEMY_X:
						enemyObj.bounds.height = 35;
						enemyObj.bounds.width = 66;
						enemyObj.speedx = Math.round(Math.random()) == 1 ? 1 : -1; 
						break;
					case GameConstants.GT_ENEMY_Y:
						enemyObj.bounds.height = 39;
						enemyObj.bounds.width = 34;
						enemyObj.speedy = Math.round(Math.random()) == 1 ? 1 : -1;
						break;
					case GameConstants.GT_ENEMY_XY:
						enemyObj.bounds.height = 50;
						enemyObj.bounds.width = 47;
						enemyObj.speedx = Math.round(Math.random()) == 1 ? 1 : -1; 
						enemyObj.speedy = Math.round(Math.random()) == 1 ? 1 : -1; 
						break;
					case GameConstants.GT_ENEMY_SMART:
						enemyObj.bounds.height = 46;
						enemyObj.bounds.width = 40;
						enemyObj.speedx = 1; 
						enemyObj.speedy = 1; 
						break;
				}

				int tries = 0;
				enemyObj.bounds.y = yPos;
				do {
					enemyObj.bounds.x = (int) Math.max(0, Math.random() * gameWidth - enemyObj.bounds.width);
				} while(checkCollision(enemyObj) && ++tries < 10);
				if(tries >= 10)
					continue;
				
				// add
				gameObjects.put(enemyObj.id, enemyObj);
			}
		}
		
		control.actionPerformed("Level", "Level "+ level);
	}
	
	/**
	 * check collisions of one object with all other game objects
	 * (used for initial placement)
	 * @param o2 the object
	 * @return true if collisions occured
	 */
	private boolean checkCollision(GameObject o2)
	{
		for(GameObject o : gameObjects.values())
		{	
			if(o != o2)
			{
				if(o.bounds.intersects(o2.bounds))
					return true;
			}
		}
		return false;
	}
}
