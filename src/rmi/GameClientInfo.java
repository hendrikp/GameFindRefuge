package rmi;
/**
 * @author hpolczynski 02.02.2014
 * oop game project
 */

import java.awt.Point;
import java.util.Date;

/**
 * holds the client information on the server
 */
public class GameClientInfo {
	/**
	 * constructor
	 */
	public GameClientInfo()
	{
		lastActiveTime = new Date();
		moveParams = new Point();
	}
	
	public RMIClient client; //!< rmi client interface
	public int id; //!< the client id
	public int gameObjectId; //!< game object id used in frame data
	public String name; //!< name of the player
	public String ip; //!< ip of the player
	public Date lastActiveTime; //!< last activity from the client
	public Point moveParams; //!< last movement parameters from client
}
