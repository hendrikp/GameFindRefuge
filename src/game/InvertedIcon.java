package game;
/**
 * @author hpolczynski 02.02.2014
 * oop game project
 */

import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.net.URL;

import javax.swing.ImageIcon;

/**
 * mirrored version of imageicon
 */
@SuppressWarnings("serial")
class InvertedIcon extends ImageIcon {
	/**
	 * create new mirrored image
	 * @param resource url of image
	 */
    public InvertedIcon(URL resource) {
		super(resource);
	}

    /**
     * paint the image mirrored
     */
	@Override
    public void paintIcon(Component comp, Graphics g, int x, int y) {
    	Graphics2D grap = (Graphics2D)g.create();
    	grap.translate(getIconWidth(), 0);
    	grap.scale(-1, 1);
    	super.paintIcon(comp, grap, x, y);
    }
}