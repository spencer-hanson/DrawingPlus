package drawingplus;
import java.awt.*;
import java.awt.image.*;

import javax.swing.*;

public class DrawingLayer extends JComponent {
	private static final long serialVersionUID = 1L;
	BufferedImage img;
	JLabel canvas;
    ImageIcon icon;
    Graphics2D g2d;
	
  
    DrawingLayer(int w, int h) {
		img = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
		g2d = img.createGraphics();
		icon = new ImageIcon(img);
		canvas = new JLabel(icon);
		 
	}
	
    public Graphics2D getGraphics() {
    	return g2d;
    }
    
	public BufferedImage getImg() {
		return img;
	}
	
	public void setBufImg(BufferedImage imgImage) {
		this.img = imgImage;
	}
	
	public JLabel getCanvas() {
		return canvas;
	}
	
	public void setCanvas(JLabel canvas) {
		this.canvas = canvas;
	}
	
	
}
