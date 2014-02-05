package game;

/**
 * @author hpolczynski 02.02.2014
 * oop game project
 */

/**
 * constants used in the game (static class singleton)
 */
public class GameConstants {
	private GameConstants() {}
	
	// player
	public final static int GT_PLAYER = 1;
	
	// enemy
	public final static int GT_ENEMY_X = 2;
	public final static int GT_ENEMY_Y = 3;
	public final static int GT_ENEMY_XY = 4;
	public final static int GT_ENEMY_SMART = 5;
	
	// enemy bounds
	public final static int GT_ENEMY_BEGIN = GT_ENEMY_X;
	public final static int GT_ENEMY_END = GT_ENEMY_SMART;
	
	// static structures
	public final static int GT_HOUSE = 10;
	public final static int GT_TREE = 20;
	
	// static bounds
	public final static int GT_STATIC_BEGIN = GT_HOUSE;
}
