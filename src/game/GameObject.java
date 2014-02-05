package game;
/**
 * @author hpolczynski 02.02.2014
 * oop game project
 */

import java.awt.Rectangle;
import java.io.Serializable;

/**
 * game object information that gets transmitted each frame to all clients
 */
public class GameObject implements Serializable {
	private static final long serialVersionUID = 1L;
	
	/**
	 * constructor
	 */
	public GameObject()
	{
		bounds = new Rectangle();
	}
	
	int id; //!< game object id
	int clientId; //!< client id (If it is a player object)
	String name; //!< client name (if it is the player)
	
	boolean visible; //!< object visibility
	boolean destroyed; //!< object destroyed state
	
	int type; //!< @see GameConstants
	
	int speedx; //!< Left = -1, 0=None, Right=1
	int speedy; //!< Up = -1, 0=None, Down=1
	
	Rectangle bounds; //!< position and size for collision detection and display
}
