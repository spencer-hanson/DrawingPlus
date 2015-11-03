package drawingplus;
import java.awt.*;
import java.awt.event.*;

import javax.swing.*;
import javax.swing.event.*;


public class ColorChooser implements ChangeListener, ActionListener {
	
	public static JDialog colors;
	protected JColorChooser tcc;
	public static boolean background = false;
	public Color newColor;
	public JButton ok;
	public JButton cancel;
	public static JButton[] history;
	public static int current = 0;
	public ColorChooser() {
		init();
	}
	
	
	public static void open() {
		colors.setVisible(true);
	}
	
	public void init() {
			if (colors != null) colors.setVisible(false);
			colors = new JDialog();
			colors.setResizable(false);
			colors.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
			colors.setTitle("Color Picker");
			colors.setSize(400, 320);
			
			tcc = new JColorChooser(StdDraw.getPenColor());
	        tcc.getSelectionModel().addChangeListener(this);
	    	tcc.remove(tcc.getComponent(1));
	    	String temp;
	    	for(int i = 0;i<=tcc.getChooserPanels().length-1;i++) {
	    		temp = tcc.getChooserPanels()[i].getDisplayName();
	    		if(!temp.equals("HSB")) {
	    			tcc.removeChooserPanel((tcc.getChooserPanels()[i]));  //remove other coloring tabs in color picker
	    		}
	    	}
	    	
	    	Panel buttons = new Panel();
	    	
	    	cancel = new JButton("Cancel");
	    	ok = new JButton("Ok");
	    	ok.setActionCommand("ok");
	    	
	    	ok.addActionListener(this);
	    	cancel.addActionListener(this);
	    	buttons.setLayout(new FlowLayout());
	    	buttons.add(ok);
	    	buttons.add(cancel);
	    	
	    	colors.add(tcc);
	    	colors.add(buttons, BorderLayout.SOUTH);
	    	colors.setVisible(false);
	}
	
	public void setLocation(Point p) {
		colors.setLocation(p);
	}
	
	public void stateChanged(ChangeEvent e) {
        newColor = tcc.getColor();
    }

	@Override
	public void actionPerformed(ActionEvent e) {
		if(e.getActionCommand().equals("ok")) {
			if(background) {
				DrawingThread.setBackgroundColor(newColor);
		    } else {
		    	DrawingThread.setForegroundColor(newColor);
		    }
			StdDraw.setBackgroundIconColor(newColor);
	    	StdDraw.setForegroundIconColor(newColor);
	    }
		colors.setVisible(false);
	}
}
