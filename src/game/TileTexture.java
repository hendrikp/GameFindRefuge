package game;
/**
 * @author hpolczynski 02.02.2014
 * oop game project
 */

import java.awt.Graphics;

import javax.swing.ImageIcon;
import javax.swing.JLabel;

/**
 * handle tillable textures (background graphic)
 */
@SuppressWarnings("serial")
public class TileTexture extends JLabel {  
    ImageIcon tile; //!< single tile
   
    /**
     * creates a new tillable texture based on an ImageIcon
     * @param icon texture to tile
     */
    public TileTexture(ImageIcon icon) {  
        tile = icon;
    }  
   
    /**
     * paint the tillable icon
     * @param g the graphics interface
     */
    protected void paintComponent(Graphics g) {  
        int pW = getWidth();  
        int pH = getHeight();
        int iW = tile.getIconWidth();  
        int iH = tile.getIconHeight();  
   
        // repeat icon 
        for (int x = 0; x < pW; x += iW) {  
            for (int y = 0; y < pH; y += iH) {
                g.drawImage(tile.getImage(), x, y, this);  
            }  
        }  
    }
}