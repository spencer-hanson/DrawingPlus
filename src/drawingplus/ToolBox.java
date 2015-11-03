package drawingplus;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import java.awt.image.BufferedImage;

@SuppressWarnings("serial")
public class ToolBox extends JPanel implements ActionListener {
	 private static JDialog tools;
	 
	 static String currentTool;
	 BufferedImage image;
	 GridBagConstraints gbc;
	 JButton lineButton;
	 JButton freeDrawButton;
	 JButton eraserButton;
	 JButton selectButton;
	 static JButton colorButton;
	 static JButton colorButton2;
	
	 ToolBox() {
		init();
	}
	 
	public void init() {
		if (tools != null) tools.setVisible(false);
		Icon icon;
		String path = "src/drawingplus/images/"; //TODO fix image loading
		currentTool = "Pencil";
		
		tools = new JDialog();
        tools.setSize(100, 500);
        tools.setResizable(false);
        tools.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        tools.setTitle("ToolBox");
        tools.setLayout(new GridBagLayout());
        
        gbc = new GridBagConstraints();
        
        lineButton = new JButton();
        freeDrawButton = new JButton();
        eraserButton = new JButton();
        selectButton = new JButton();
        colorButton = new JButton();
        colorButton2 = new JButton();
        
        //Create toolbox gui
        
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.PAGE_START;
        gbc.insets = new Insets(10,10,10,10);
        gbc.gridwidth = 1;
        gbc.ipady = 0;
        gbc.gridx = 0;
        gbc.gridy = 0;
        icon = new ImageIcon(path + "line.png");
        tools.getContentPane().add(parseButton(lineButton, icon, "Line"),gbc);
        gbc.gridx = 1;
        gbc.gridy = 0;
        icon = new ImageIcon(path + "pencil.png");
        tools.getContentPane().add(parseButton(freeDrawButton,icon, "Pencil"),gbc);
        gbc.gridx = 0;
        gbc.gridy = 1;
        icon = new ImageIcon(path + "eraser.png");
        tools.getContentPane().add(parseButton(eraserButton,icon, "Eraser"),gbc);
        gbc.gridx = 1;
        gbc.gridy = 1;
        icon = new ImageIcon(path + "selection.png"); //Clear screen
        tools.getContentPane().add(parseButton(selectButton, icon, "Clear"),gbc);
        gbc.gridx = 0;
        gbc.gridy = 2;
        icon = new ImageIcon(StdDraw.getForegroundIconColor());
        tools.getContentPane().add(parseButton(colorButton, icon, "Color"), gbc);
        gbc.gridx = 1;
        gbc.gridy = 2;
        icon = new ImageIcon(StdDraw.getBackgroundIconColor());
        tools.getContentPane().add(parseButton(colorButton2, icon, "Color2"), gbc);
        
        
        tools.pack();
        tools.setVisible(true);
    }
	
	
	public static void setIconColor(BufferedImage colorImage, BufferedImage colorImage2) {
		Icon icon = new ImageIcon(colorImage);
		colorButton.setIcon(icon);
		icon = new ImageIcon(colorImage2);
		colorButton2.setIcon(icon);
	}
	
	public void actionPerformed(ActionEvent e) {
		if(e.getActionCommand().equals("Color")) {
			ColorChooser.background = false;
			ColorChooser.open();
		} else if(e.getActionCommand().equals("Color2")) {
			ColorChooser.background = true;
			ColorChooser.open();
		} else {
		    System.out.println("Setting tool: " + e.getActionCommand());
			setTool(e.getActionCommand());
			DrawingThread.setBrush(getTool());
		}
	}

	public static void setTool(String str) {
		currentTool = str;
	}
	
	public static String getTool() {
		return currentTool;
	}
	
	public void pack() {
		tools.pack();
	}
	
	public void setVisible(boolean b) {
		tools.setVisible(b);
	}
	
	public void paint(Graphics g) {
	     super.paint(g);
	}
	
	public static void addToolboxButton(JButton j) {
		tools.add(j);
	}
	    
	public static JDialog getToolbox() {
	    return tools;
	}
	    
	public static void setToolBoxSize(Dimension d) {
	    setToolBoxSize((int)d.getWidth(), (int)d.getHeight());
	}
	    
	public static Dimension getToolboxSize() {
	    return tools.getSize();
	}
	    
	public static void setToolsLocation(int x, int y) {
	    tools.setLocation(x, y);
	}
	    
	public static void setToolBoxSize(int w, int h) {
	    tools.setSize(w, h);
	 }
	
	public JButton parseButton(JButton b, Icon icon, String action) {
		int length = (int)getToolboxSize().getWidth()/2;
		b.setSize(length, length);
		b.setActionCommand(action);
		b.addActionListener(this);
		b.setIcon(icon);
		return b;
	}
	
}




 