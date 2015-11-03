package drawingplus;
import java.awt.*;
import java.util.*;
import java.awt.event.ActionEvent;
import java.awt.image.BufferedImage;

import javax.swing.*;
import javax.swing.colorchooser.*;

@SuppressWarnings("serial")
public class HistoryColorChooser  extends AbstractColorChooserPanel {  //custom color chooser       
	public static int current = 0;
	public static Color[] colors;
	public void buildChooser() {
		Panel panel = new Panel();
		panel.setLayout(new GridLayout());
		for(int i = 1;i<=3;i++) {
	    	for(int x = 1;x<=2;x++) {
	    		panel.add(addButton(i*x + "", Color.yellow));
	    	}
	    }
		add(panel);
	    
	  }
	  
	  	HistoryColorChooser() {
	  		super();
	  		colors = new Color[9];
	  	}

	  	public static void addColor(Color color) {
	  		colors[current] = color;
	  		current++;
	  		if(current > 8) {
	  			current = 0;
	  		}
	  	}
		  public void updateChooser() {
			//buttons[0].setIcon(makeIcon(new Color(0, 255, 0), 50, 20));
		  }

		  public String getDisplayName() {
		    return "ColorHistory";
		  }

		  public Icon getSmallDisplayIcon() {
		    return null;
		  }
		  public Icon getLargeDisplayIcon() {
		    return null;
		  }
		  
		  public Icon makeIcon(Color color, int width, int height) {
			  BufferedImage temp = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB); //create buffered icon image
			  Graphics2D temp2 = temp.createGraphics();
			  temp2.setColor(color);
			  temp2.fillRect(0, 0, width, height); //draw in buffer
			  Icon icon = new ImageIcon(temp);
			  return icon;
		  }
		  private JButton addButton(String name, Color color) {
		    JButton button = new JButton(name);
		    button.setBackground(color);
		    //button.setIcon(makeIcon(color, 50, 20));
		    button.setAction(setColorAction);
		    add(button);
		    return button;
		  
		  }

		  Action setColorAction = new AbstractAction() {
		    public void actionPerformed(ActionEvent evt) {
		      JButton button = (JButton) evt.getSource();
		      getColorSelectionModel().setSelectedColor(button.getBackground());
		    }
		  };
		}