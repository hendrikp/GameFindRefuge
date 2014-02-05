package game;
/**
 * @author hpolczynski 02.02.2014
 * oop game project
 */

import game.GameModel;
import game.GameView;

/**
 * Main class (Game Startup)
 */
public class Game {
	public static void main(String[] args) {
		// Read config
		GameConfiguration.readGameConfig();
		
		// Controller
		Controller control = new Controller();
		
		// Model
		GameModel model = new GameModel(control);
		
		// View
		GameView gameView = new GameView(model, control);
		
		// setup mvc communication
		control.addControllable(model);
		control.addControllable(gameView);
	}
}
