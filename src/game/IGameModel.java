package game;
/**
 * @author hpolczynski 02.02.2014
 * oop game project
 */

import java.util.Observable;

/**
 * abstract class for the game model
 */
public abstract class IGameModel extends Observable implements Controllable {
	/**
	 * set size of the game field
	 * @param width x
	 * @param height y
	 */
	public abstract void setGameSize(int width, int height);
	
	/**
	 * get the local player id
	 * @return local player id
	 */
	public abstract int getLocalPlayerId();
	
	/**
	 * get the rmi url
	 * @return local rmi url
	 */
	public abstract String getLocalUrl();
	
	/**
	 * get game name
	 * @return name of the game
	 */
	public abstract String getGameName();
}
