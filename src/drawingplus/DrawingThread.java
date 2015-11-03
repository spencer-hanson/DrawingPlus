package drawingplus;
import java.awt.*;
public class DrawingThread extends Thread {
	static String brush;
	static double brushSize;
	
	public static Color foregroundColor;
	public static Color backgroundColor;
	
	DrawPoint oldPoint;
	DrawPoint currentPoint;
	
	boolean firstClick;
	boolean letGo;
	static boolean erase;
	public int currentLayer = 0;
	
	DrawingThread() {
		firstClick = true;
		erase = false;
		brush = "Pencil";
	}
	
	public void run() {
		
	update();
	while(true) {
		StdDraw.clear(); //clear and update screen
		
		update();
		
		if(brush.equals("Clear")) {
			if(!erase) {
				StdDraw.setPenColor(StdDraw.WHITE);
				StdDraw.filledRectangle(0, 0, 900, 900, StdDraw.drawGraphics[getLayer()]);	
				erase = true;
				brush = "Pencil";
			}
			
		}
		
		if(brush.equals("Select")) { //not implemented yet 
			erase = false;
			if(firstClick && StdDraw.mousePressed()) {
				oldPoint = new DrawPoint(StdDraw.mouseX(), StdDraw.mouseY());
				firstClick = false;
			}
			
			if(StdDraw.mousePressed() && !firstClick) { //if this is a new line
				currentPoint = new DrawPoint(StdDraw.mouseX(), StdDraw.mouseY());
				StdDraw.previewLayer = getLayer();
				//Graphics2D g = (Graphics2D)StdDraw.previewGraphics.create();
				StdDraw.previewGraphics.setColor(new Color(0, 0, 0));
				StdDraw.previewGraphics.setStroke(new BasicStroke((float) 1, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
				StdDraw.line(oldPoint.getX(), oldPoint.getY(), oldPoint.getX(), currentPoint.getY(), StdDraw.previewGraphics);
				StdDraw.line(oldPoint.getX(), currentPoint.getY(), currentPoint.getX(), currentPoint.getY(), StdDraw.previewGraphics);
				
				StdDraw.line(oldPoint.getX(), oldPoint.getY(), currentPoint.getX(), oldPoint.getY(), StdDraw.previewGraphics);
				StdDraw.line(currentPoint.getX(), oldPoint.getY(), currentPoint.getX(), currentPoint.getY(), StdDraw.previewGraphics);
				
				//StdDraw.line(oldPoint.getX(), oldPoint.getY(), currentPoint.getX(), currentPoint.getY(), StdDraw.previewGraphics);
				//StdDraw.line(oldPoint.getX(), oldPoint.getY(), currentPoint.getX(), currentPoint.getY(), StdDraw.previewGraphics);
			}
			
			if(StdDraw.mouseReleased() && !firstClick) { //if this isn't the first time, and let go of the mouse
				firstClick = true;
				StdDraw.right = false;	
				StdDraw.setMouseReleased(false);	
				}
				
			
		} 
		
		if(brush.equals("Eraser")) { //erase, basically draw with white
			erase = true;
			
			if(firstClick && StdDraw.mousePressed()) {
				oldPoint = new DrawPoint(StdDraw.mouseX(), StdDraw.mouseY());
				firstClick = false;
			}
		
			if(StdDraw.mouseDragged() && StdDraw.mousePressed() && !firstClick) {
				if(letGo) {
					oldPoint = new DrawPoint(StdDraw.mouseX(), StdDraw.mouseY());
					letGo = false;
				}
				currentPoint = new DrawPoint(StdDraw.mouseX(), StdDraw.mouseY());
				//draw a line from the last time the screen updated to the next time where the mouse is
				StdDraw.line(oldPoint.getX(), oldPoint.getY(), currentPoint.getX(), currentPoint.getY(), StdDraw.drawGraphics[getLayer()]);
				oldPoint = currentPoint;
		}
		
			if(StdDraw.mouseReleased() && !firstClick) { 
				letGo = true;
				firstClick = true;
				StdDraw.right = false;	
				StdDraw.setMouseReleased(false);
				}
			
		}

		if(brush.equals("Pencil")) {
				erase = false;
				if(firstClick && StdDraw.mousePressed()) {
					oldPoint = new DrawPoint(StdDraw.mouseX(), StdDraw.mouseY());
					firstClick = false;
				}
			
				if(StdDraw.mouseDragged() && StdDraw.mousePressed() && !firstClick) {
					if(letGo) {
						oldPoint = new DrawPoint(StdDraw.mouseX(), StdDraw.mouseY());
						letGo = false;
					}
					currentPoint = new DrawPoint(StdDraw.mouseX(), StdDraw.mouseY());
					StdDraw.line(oldPoint.getX(), oldPoint.getY(), currentPoint.getX(), currentPoint.getY(), StdDraw.drawGraphics[getLayer()]);
					oldPoint = currentPoint;
			}
			
				if(StdDraw.mouseReleased() && !firstClick) {
					letGo = true;
					firstClick = true;
					StdDraw.right = false;	
					StdDraw.setMouseReleased(false);
					}
				
				} 

			if(brush.equals("Line")) { //draw a line from point a to point b
					erase = false;
					if(firstClick && StdDraw.mousePressed()) {
						oldPoint = new DrawPoint(StdDraw.mouseX(), StdDraw.mouseY());
						firstClick = false;
					}
					
					if(StdDraw.mousePressed() && !firstClick) {
						currentPoint = new DrawPoint(StdDraw.mouseX(), StdDraw.mouseY());
						StdDraw.previewLayer = getLayer();
						StdDraw.line(oldPoint.getX(), oldPoint.getY(), currentPoint.getX(), currentPoint.getY(), StdDraw.previewGraphics);
					}
					
					if(StdDraw.mouseReleased() && !firstClick) {
						firstClick = true;
						if(StdDraw.right) { 
						StdDraw.line(oldPoint.getX(), oldPoint.getY(), currentPoint.getX(), currentPoint.getY(), StdDraw.drawGraphics[getLayer()], getBackgroundColor());
						} else {
							StdDraw.line(oldPoint.getX(), oldPoint.getY(), currentPoint.getX(), currentPoint.getY(), StdDraw.drawGraphics[getLayer()]);
						}
						StdDraw.setMouseReleased(false);
					StdDraw.right = false;	
					}
					
				}
			StdDraw.show(10);	
			}
		}

	public int getLayer() {
		return currentLayer;
	}
	
    public static void update() {
    	StdDraw.setPenRadius(getBrushSize()/130); 
    	if(erase) {
    		StdDraw.setPenColor(new Color(255, 255, 255)); //set white color
    	} else if(StdDraw.right) {
    		StdDraw.setPenColor(getBackgroundColor());
    	} else {
    		StdDraw.setPenColor(getForegroundColor());	
    	}
    	
    	StdDraw.setForegroundIconColor(getForegroundColor());
    	StdDraw.setBackgroundIconColor(getBackgroundColor());
    	ToolBox.setIconColor(StdDraw.getForegroundIconColor(), StdDraw.getBackgroundIconColor()); //set Icons
    }
	
	public static double getBrushSize() {
		return brushSize;
	}
	
	public void setBrushSize(double brushSize) {
		this.brushSize = brushSize;
		update();
	}
	
	public static void setBrush(String str) {
		brush = str;
	}
	
	public static String getBrush() {
		return brush;
	}
	
	public static void setForegroundColor(Color c) {
		foregroundColor = c;
		update();
	}
	
	public static Color getForegroundColor() {
		return foregroundColor;
	}
	
	public static Color getBackgroundColor() {
		return backgroundColor;
	}
	
	public static void setBackgroundColor(Color c) {
		backgroundColor = c;
		update();
	}
	
}
