package drawingplus;
import java.awt.*;

public class DrawingPlus {
	DrawingThread drawThread;
	ColorChooser chooser;
	ToolBox tools;
	
	DrawingPlus() {
		init();
	}
	
	@SuppressWarnings("static-access")
	public void init() {
		//Create gui
		Dimension size = new Dimension(800, 500);
		Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
		
		int x = (int)((screen.getWidth()-size.getWidth())/2);
		int y = (int)((screen.getHeight()-size.getHeight())/2);
	    
		tools = new ToolBox(); //create a position toolbox next to other window
		tools.setToolsLocation((int)size.getWidth() + x - tools.getWidth() + 10, y);
		
		chooser = new ColorChooser(); //prepare color chooser
		chooser.setLocation(new Point(x + chooser.colors.getWidth()/2,y + chooser.colors.getHeight()/2 - 20));
		
		StdDraw.setCanvasSize(size);//set up StdDraw's canvas and scale
		StdDraw.setCanvasLocation(x, y);
		StdDraw.setXscale(0, size.getWidth());
		StdDraw.setYscale(0, size.getHeight());
		
		drawThread = new DrawingThread(); //start the drawing thread
		drawThread.start();
		drawThread.setBrushSize(5);
		drawThread.setForegroundColor(new Color(0, 0, 0));
		drawThread.setBackgroundColor(new Color(255, 0, 0));
		
		
		StdDraw.visible(true); //make window visible
		update();
	}
	
	
	public void update() {
		
	}
	
	public static void main(String[] args) {
		new DrawingPlus();
	}
	
	
	
	
}
