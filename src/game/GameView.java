package game;
/**
 * @author hpolczynski 02.02.2014
 * oop game project
 */

import java.awt.Color;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.HashMap;
import java.util.Observable;
import java.util.concurrent.ConcurrentHashMap;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JLayeredPane;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingConstants;
import javax.swing.text.DefaultCaret;

/**
 * Game view class, handling the display of the game and movement input
 */
@SuppressWarnings("serial")
public class GameView extends JFrame implements IGameView, KeyListener, FocusListener, MouseListener {
	private static int FRAME_WIDTH = GameConfiguration.gameSizeX; //!< width of frame
	private static int FRAME_HEIGHT = GameConfiguration.gameSizeY; //!< height of frame
	private static final int BORDER = 10; //!< border around frame
	
	private JButton buttonStart; //!< start/end button
	private JButton buttonHost; //!< host game
	private JButton buttonConnect; //!< connect game
	
	private boolean connected; //!< indicate game is connected to a host
	private boolean stop; //! state of game
	
	private JTextField serverUrl; //!< servertext
	private JTextField nickname; //!< nickname label
	private JScrollPane logSP; //!< chatlog area
	private JTextArea log; //!< chatlog
	private JLabel chatLabel; //!< chat label
	private JTextField chatinput; //!< chat input field
	private JLabel levelLabel; //!< level display
	
	private JLabel gameOver; //!< game over display
	
	private JLabel helpLabel; //!< game help text
	
	private JLayeredPane gameLayers; //!< game layers for the frame rendering
	private TileTexture gameBackground; //!< game background graphic
	
	private HashMap<Integer, JLabel> gameObjectLabels; //!< storage of gameobject images
	private HashMap<Integer, ClientVisuals> clientVisuals; //!< client names
	
	private Controller control; //!< the controller
	private IGameModel model; //!< the game model interface
	
	private Icon iconGhost; //!< icon of death player
	private Icon iconEnemyAR; //!< icon of enemy x r
	private Icon iconEnemyAL; //!< icon of enemy x l
	private Icon iconEnemyBR; //!< icon of enemy y up
	private Icon iconEnemyBL; //!< icon of enemy y down
	private Icon iconHorseR; //!< icon of enemy xy r/up
	private Icon iconHorseL; //!< icon of enemy xy l/down
	private Icon iconAntagonistR; //!< icon of enemy smart r
	private Icon iconAntagonistL; //!< icon of enemy smart l
	private Icon iconTree; //!< icon of tree
	private Icon iconHouse; //!< icon of house
	private Icon iconIndicator; //!< icon of player indicator
	private JLabel playerIndicator; //!< player indicator label
	
	private BackgroundMusic music; //!< background music player
	
	/**
	 * cleanup of the current game field
	 */
	private void cleanupGameLabels()
	{
		gameLayers.setVisible(false);
		for(JLabel lbl : gameObjectLabels.values())
		{
			gameLayers.remove(lbl);
		}
		gameObjectLabels.clear();
		clientVisuals.clear();
		
		playerIndicator.setVisible(false);
		gameLayers.setVisible(true);
		gameLayers.invalidate();
		gameLayers.repaint();
	}
	
	/**
	 * notify the view about the game state
	 * @param command controller command or Finish
	 * @param param optional string param
	 * @param param2 optional string param
	 * @param param2 optional objct param
	 */
	@Override
	public void notify(String command, String param, String param2, Object param3) {
		switch (command) {			
			case "Started" : {
				// Start
				cleanupGameLabels();
				stop = false;
				gameLayers.grabFocus();
				gameLayers.requestFocusInWindow();
				music.start();
				resetKeys();
				gameOver.setVisible(false);
				break;
			}
			
			case "LevelReceived": {
				cleanupGameLabels();
				resetKeys();
				levelLabel.setVisible(true);
				levelLabel.setText(param);
				break;
			}
			
			case "Ended": {
				// Finish
				cleanupGameLabels();
				stop = true;
				music.stop();
				
		        gameOver.setText("<html><center>Game Over<br><br>You reached " + levelLabel.getText() + "<br>Press Start to try again</center></html>");
		        gameOver.setVisible(true);
		        
				levelLabel.setVisible(false);
				levelLabel.setText("");
				break; 
			}
			
			case "Connected": {
				connected = true;
				setTitle(model.getGameName() + " - " + param2);
				
		        gameOver.setText("Press Start");
		        gameOver.setVisible(true);
				break;
			}
			
			case "Disconnected": {
				connected = false;
				log.append("[System] Disconnected: " + param + "\n");
				break;
			}
			
			case "Message": {
				log.append(param);
				break;
			}
		}
	
		buttonStart.setText(stop ? "Start" : "End");
		buttonStart.setVisible(connected);
		gameBackground.setVisible(!stop);
		
		buttonHost.setVisible(!connected);
		buttonConnect.setVisible(!connected);
		nickname.setVisible(!connected);
		serverUrl.setVisible(!connected);

        chatinput.setVisible(connected);
        chatLabel.setVisible(connected);
	}
	
	/**
	 * game frame received
	 * @param o Observable/Broadcaster
	 * @param arg frame data
	 */
	@Override
	public void update(Observable o, Object arg) {
		if(!stop && arg != null) {
			@SuppressWarnings("unchecked")
			ConcurrentHashMap<Integer, GameObject> gameObjects = (ConcurrentHashMap<Integer, GameObject>) arg;
			
			boolean levelchange = gameObjectLabels.size() == 0;
			
			if(levelchange)
			{
				gameLayers.setVisible(false);
			}
			
			for(GameObject go : gameObjects.values())
			{
				JLabel lbl = gameObjectLabels.get(go.id);
				
				// create if non-existent
				if(lbl == null)
				{
					lbl = new JLabel();
					gameObjectLabels.put(go.id, lbl);
					gameLayers.add( lbl, JLayeredPane.PALETTE_LAYER);
					
					ClientVisuals cv = new ClientVisuals(getClass().getResource("images/player.gif"), 5);
					clientVisuals.put(go.clientId, cv);
				}

				lbl.setVisible(go.visible);

				Icon nextIcon = null;
				switch(go.type)
				{
					case GameConstants.GT_PLAYER: {
						ClientVisuals cv = clientVisuals.get(go.clientId);
						
						if(go.clientId == model.getLocalPlayerId())
						{
							playerIndicator.setVisible(go.visible);
							playerIndicator.setBounds((int) (go.bounds.getCenterX() - playerIndicator.getWidth() /2), go.bounds.y - playerIndicator.getHeight() / 2, 16, 9);
						}
						
						SheetTexture st = cv.icon;
						if(go.speedx == 0 && go.speedy == 0)
							st.setCell(0);
						else if(go.speedx < 0)
							st.setCell(3);
						else if(go.speedx > 0)
							st.setCell(2);
						else if(go.speedy < 0)
							st.setCell(4);
						else if(go.speedy > 0)
							st.setCell(1);
						
						nextIcon = st;
						break;
					}
					case GameConstants.GT_ENEMY_X: nextIcon = go.speedx < 0 ? iconEnemyAL : iconEnemyAR; break;
					case GameConstants.GT_ENEMY_Y: nextIcon = go.speedx < 0 ? iconEnemyBL : iconEnemyBR; break;
					case GameConstants.GT_ENEMY_XY: nextIcon = go.speedx < 0 ? iconHorseL : iconHorseR; break;
					case GameConstants.GT_ENEMY_SMART: nextIcon = go.speedx < 0 ? iconAntagonistL : iconAntagonistR; break;
					
					case GameConstants.GT_HOUSE: nextIcon = iconHouse; break;
					case GameConstants.GT_TREE: nextIcon = iconTree; break;
				}

				if(go.visible)
				{
					if(go.destroyed)
					{
						nextIcon = iconGhost;
					}
					
					lbl.setBounds(go.bounds);
					lbl.setIcon(nextIcon);
				}
			}
			
			if(levelchange)
			{
				gameLayers.setVisible(true);
				gameLayers.invalidate();
				gameLayers.repaint();
				gameLayers.grabFocus();
				gameLayers.requestFocusInWindow();
			}
		}
    }
	
	public int kALeft; //!< key state left
	public int kARight; //!< key state rigth
	public int kAUp; //!< key state up
	public int kADown; //!< key state down
	
	/**
	 * handle press keys
	 */
	@Override
	public void keyPressed(KeyEvent e) {
		switch( e.getKeyCode() ) {
			case KeyEvent.VK_LEFT: kALeft = -1; break;
			case KeyEvent.VK_RIGHT: kARight = 1; break;
			case KeyEvent.VK_UP: kAUp = -1; break;
			case KeyEvent.VK_DOWN: kADown = 1; break;
		}
		
		control.actionPerformed("Movement", new Point(kALeft+kARight, kAUp+kADown ));
	}

	/**
	 * handle released keys
	 */
	@Override
	public void keyReleased(KeyEvent e) {
		switch( e.getKeyCode() ) {
			case KeyEvent.VK_LEFT: kALeft = 0; break;
			case KeyEvent.VK_RIGHT: kARight = 0; break;
			case KeyEvent.VK_UP: kAUp = 0; break;
			case KeyEvent.VK_DOWN: kADown = 0; break;
		}
		
		control.actionPerformed("Movement", new Point(kALeft+kARight, kAUp+kADown ));
	}

	/**
	 * resets the key state
	 */
	public void resetKeys() {
		kALeft = 0;
		kARight = 0;
		kAUp = 0;
		kADown = 0;
		
		control.actionPerformed("Movement", new Point(kALeft+kARight, kAUp+kADown ));
	}
	
	/**
	 * reset key state on lost focus
	 * @see java.awt.event.FocusListener#focusLost(java.awt.event.FocusEvent)
	 */
	@Override
	public void focusLost(FocusEvent arg0) {
		resetKeys();
	}
	
	/**
	 * set focus on game when its clicked
	 */
	@Override
	public void mouseReleased(MouseEvent arg0) {
		gameLayers.grabFocus();
		gameLayers.requestFocusInWindow();
	}
	
	@Override
	public void mouseClicked(MouseEvent arg0) {
	}

	@Override
	public void mouseEntered(MouseEvent arg0) {
	}

	@Override
	public void mouseExited(MouseEvent arg0) {
	}

	@Override
	public void mousePressed(MouseEvent arg0) {
	}
	
	@Override
	public void focusGained(FocusEvent arg0) {
	}

	@Override
	public void keyTyped(KeyEvent e) {
	}

	/**
	 * Constructor (set up display)
	 * @param _model the game model interface
	 * @param _control the controller interface
	 */
	public GameView(IGameModel _model, Controller _control) {
		
		// refresh from config
		FRAME_WIDTH = GameConfiguration.gameSizeX; //!< width of frame
		FRAME_HEIGHT = GameConfiguration.gameSizeY;
		
		// set mvc data
		model = _model;
		model.addObserver(this);
		control = _control;
		
		// init game data holders
		gameObjectLabels = new HashMap<Integer, JLabel>();
		clientVisuals = new HashMap<Integer, ClientVisuals>();
		
		// Frame
		setTitle(model.getGameName());
		setSize(FRAME_WIDTH, FRAME_HEIGHT);
		setResizable(false);
        setLayout(null);
		
        // Music
        music = new BackgroundMusic(getClass().getResource("sounds/onclassical_demo_fiati-di-parma_thuille_terzo-tempo_sestetto_small-version.wav"));
        
        // Icons
    	iconGhost = new ImageIcon(getClass().getResource("images/ghost.gif"));
    	iconEnemyAR = new InvertedIcon(getClass().getResource("images/enemya.gif"));
    	iconEnemyAL = new ImageIcon(getClass().getResource("images/enemya.gif"));
    	iconEnemyBR = new ImageIcon(getClass().getResource("images/enemyb.gif"));
    	iconEnemyBL = iconEnemyBR;
    	iconHorseR = new ImageIcon(getClass().getResource("images/horse.gif"));
    	iconHorseL = new InvertedIcon(getClass().getResource("images/horse.gif"));
    	iconAntagonistR = new ImageIcon(getClass().getResource("images/antagonist.gif"));
    	iconAntagonistL = new InvertedIcon(getClass().getResource("images/antagonist.gif"));
    	iconHouse = new ImageIcon(getClass().getResource("images/house.png"));
    	iconTree = new ImageIcon(getClass().getResource("images/tree.png"));
    	iconIndicator = new ImageIcon(getClass().getResource("images/player_indicator.gif"));
    	
        // Server url
        serverUrl = new JTextField(model.getLocalUrl());
        serverUrl.setBounds(BORDER, BORDER, 200, 25);
        super.add(serverUrl);
        
        // Nickname
        nickname = new JTextField("User" + Math.round(Math.random() *1000.0));
        nickname.setBounds(serverUrl.getX()+serverUrl.getWidth()+BORDER, serverUrl.getY(), 100, 25);
        super.add(nickname);
    
        // Message/Chat area
        log = new JTextArea();
        chatLabel = new JLabel("Chat:");
        chatinput = new JTextField();
    
        log.setEditable(false);
        DefaultCaret caret = (DefaultCaret)log.getCaret();
        caret.setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE); // scroll down on append
        
        logSP = new JScrollPane(log);
        logSP.setBounds(BORDER, FRAME_HEIGHT-(100+BORDER+50), 350, 100);
        logSP.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
        
        chatLabel.setBounds(logSP.getX(), logSP.getY() + logSP.getBounds().height, chatLabel.getPreferredSize().width, 25);
        chatinput.setBounds(chatLabel.getX() + chatLabel.getBounds().width + BORDER, chatLabel.getY(), logSP.getBounds().width-(chatLabel.getBounds().width+BORDER), 25);
        
        chatinput.setVisible(false);
        chatLabel.setVisible(false);
        
        super.add(chatinput);
        super.add(chatLabel);
        super.add(logSP);
        
        // Send chat messages
        chatinput.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent action) {
				control.actionPerformed("MessageInput", chatinput.getText());
				chatinput.setText("");
		}});
        
        // Player Indicator
        playerIndicator = new JLabel();
        playerIndicator.setIcon(iconIndicator);
        
        // Game bg
        gameBackground = new TileTexture(new ImageIcon(getClass().getResource("images/pflasterstein.jpg")));
        gameBackground.setBounds(0,0, FRAME_WIDTH, logSP.getY()-BORDER);
        gameBackground.setVisible(false);
        
        // Game Layer
        gameLayers = new JLayeredPane();
        gameLayers.setBounds(gameBackground.getBounds());
        gameLayers.add(gameBackground);
        gameLayers.add(playerIndicator, JLayeredPane.DRAG_LAYER);
        gameLayers.setFocusable(true);
        gameLayers.addKeyListener((KeyListener) this);
        gameLayers.addFocusListener((FocusListener) this);
        
        gameBackground.addMouseListener((MouseListener) this);
        
        super.add(gameLayers);
        
        // set game size
        model.setGameSize(gameLayers.getWidth(), gameLayers.getHeight());
        
        gameOver = new JLabel();
        gameOver.setHorizontalAlignment(SwingConstants.CENTER);
        gameOver.setBounds(gameLayers.getBounds());
        gameOver.setOpaque(true);
        gameOver.setBackground(Color.black);
        gameOver.setForeground(Color.white);
        super.add(gameOver);
        gameOver.setText("Press Start");
        gameOver.setVisible(false);
        
        // Start/End Button
		buttonStart = new JButton("Start");
		buttonStart.setBounds(logSP.getX()+logSP.getBounds().width+BORDER, logSP.getY(), buttonStart.getPreferredSize().width, buttonStart.getPreferredSize().height);
		super.add(buttonStart);
		stop = true;
		buttonStart.setVisible(false);
		
		// Level
		levelLabel = new JLabel();
		levelLabel.setBounds(buttonStart.getX(), (int) (buttonStart.getBounds().getMaxY() + BORDER), 100, 25);
		levelLabel.setVisible(false);
		super.add(levelLabel);
		
		// Help
		helpLabel = new JLabel("<html>First enter your nickname at the top."
				+ "Then host a new game or connect to an existing game by entering the URL."
				+ "Wait for other players to arrive and then start the game.<br>"
				+ "<br>"
				+ "Use the arrow keys to flee to the house (avoid all enemys). You are highlighted with the arrow.<br>"
				+ "You can revive other players by moving to them.(if you haven't entered the house yet)<br>"
				+ "Hide in trees to avoid the enemys. Work together to lure smart enemys away.<br>"
				+ "The next level can be reached if all players enter the house.<br>"
				+ "You can also communicate with the other players by chatting</html>");
		
		helpLabel.setBounds(buttonStart.getX() + buttonStart.getBounds().width + BORDER, buttonStart.getY(), FRAME_WIDTH -(buttonStart.getX() + buttonStart.getBounds().width + BORDER)- BORDER , FRAME_HEIGHT - buttonStart.getY()-BORDER*3);
		super.add(helpLabel);
		
		// Connect/Host
		buttonConnect = new JButton("Connect");
		buttonConnect.setBounds(nickname.getX()+nickname.getWidth()+BORDER, nickname.getY(), buttonConnect.getPreferredSize().width, buttonConnect.getPreferredSize().height);
		buttonHost = new JButton("Host");
		buttonHost.setBounds(buttonConnect.getX() + buttonConnect.getWidth() + BORDER, buttonConnect.getY(), buttonHost.getPreferredSize().width, buttonHost.getPreferredSize().height );
		super.add(buttonConnect);
		super.add(buttonHost);
	
		// Controller
		buttonStart.addActionListener(control);
		
		// Additional data required for the other actions
		buttonHost.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent action) {
				control.actionPerformed(action.getActionCommand(), serverUrl.getText(), nickname.getText());
		}});
		buttonConnect.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent action) {
				control.actionPerformed(action.getActionCommand(), serverUrl.getText(), nickname.getText());
		}});

		// Show
		setVisible(true);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	}

}