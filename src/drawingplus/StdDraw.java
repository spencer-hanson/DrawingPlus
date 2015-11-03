package drawingplus;
/*************************************************************************
 *  Compilation:  javac StdDraw.java
 *  Execution:    java StdDraw
 *
 *  Standard drawing library. This class provides a basic capability for
 *  creating drawings with y-our programs. It uses a simple graphics model that
 *  allows you to create drawings consisting of points, lines, and curves
 *  in a window on your computer and to save the drawings to a file.
 *
 *  Todo
 *  ----
 *    -  Add support for gradient fill, etc.
 *
 *  Remarks
 *  -------
 *    -  don't use AffineTransform for rescaling since it inverts
 *       images and strings
 *    -  careful using setFont in inner loop within an animation -
 *       it can cause flicker
 *
 *************************************************************************/

import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import java.awt.image.*;
import java.io.*;
import java.net.*;
import java.util.LinkedList;
import java.util.TreeSet;
import javax.imageio.ImageIO;
import javax.swing.*;
import java.util.*;
/**
 *  <i>Standard draw</i>. This class provides a basic capability for
 *  creating drawings with your programs. It uses a simple graphics model that
 *  allows you to create drawings consisting of points, lines, and curves
 *  in a window on your computer and to save the drawings to a file.
 *  <p>
 *  For additional documentation, see <a href="http://introcs.cs.princeton.edu/15inout">Section 1.5</a> of
 *  <i>Introduction to Programming in Java: An Interdisciplinary Approach</i> by Robert Sedgewick and Kevin Wayne.
 */

//Edited by Spencer Hanson
public final class StdDraw implements ActionListener, MouseListener, MouseMotionListener, KeyListener {

    // pre-defined colors
    public static final Color BLACK      = Color.BLACK;
    public static final Color BLUE       = Color.BLUE;
    public static final Color CYAN       = Color.CYAN;
    public static final Color DARK_GRAY  = Color.DARK_GRAY;
    public static final Color GRAY       = Color.GRAY;
    public static final Color GREEN      = Color.GREEN;
    public static final Color LIGHT_GRAY = Color.LIGHT_GRAY;
    public static final Color MAGENTA    = Color.MAGENTA;
    public static final Color ORANGE     = Color.ORANGE;
    public static final Color PINK       = Color.PINK;
    public static final Color RED        = Color.RED;
    public static final Color WHITE      = Color.WHITE;
    public static final Color YELLOW     = Color.YELLOW;

    // default colors
    private static final Color DEFAULT_PEN_COLOR   = BLACK;
    private static final Color DEFAULT_CLEAR_COLOR = WHITE;

    // current pen color
    private static Color penColor;

    // default canvas size is DEFAULT_SIZE-by-DEFAULT_SIZE
    private static final int DEFAULT_SIZE = 512;
    private static int width  = DEFAULT_SIZE;
    private static int height = DEFAULT_SIZE;

    // default pen radius
    private static final double DEFAULT_PEN_RADIUS = 0.002;

    // current pen radius
    private static double penRadius;

    // show we draw immediately or wait until next show?
    private static boolean defer = false;

    // boundary of drawing canvas, 5% border
    private static final double BORDER = 0.05;
    private static final double DEFAULT_XMIN = 0.0;
    private static final double DEFAULT_XMAX = 1.0;
    private static final double DEFAULT_YMIN = 0.0;
    private static final double DEFAULT_YMAX = 1.0;
    private static double xmin, ymin, xmax, ymax;

    // for synchronization
    private static Object mouseLock = new Object();
    private static Object keyLock = new Object();

    // default font
    private static final Font DEFAULT_FONT = new Font("SansSerif", Font.PLAIN, 16);

    // current font
    private static Font font;

    // double buffered graphics
    public static BufferedImage[] drawImage;
    public static Graphics2D[] drawGraphics;
    
    public static BufferedImage previewImage;
    public static Graphics2D previewGraphics;
    public static int previewLayer = 0;
    public static int layers = 0;
    
    private static BufferedImage offscreenImage, onscreenImage, colorImage, colorImage2;
    private static Graphics2D offscreen, onscreen, color, color2;

    // singleton for callbacks: avoids generation of extra .class files
    private static StdDraw std = new StdDraw();

    // the frame for drawing to the screen
    private static JFrame frame;
    
    // mouse state
    private static boolean mouseRelease = false;
    private static boolean mouseDrag = false;
    private static boolean mousePressed = false;
    public static boolean right = false;
    private static double mouseX = 0;
    private static double mouseY = 0;
    

    // queue of typed key characters
    private static LinkedList<Character> keysTyped = new LinkedList<Character>();

    // set of key codes currently pressed down
    private static TreeSet<Integer> keysDown = new TreeSet<Integer>();
  

    // not instantiable
    private StdDraw() { }


    // static initializer
    static { init(); }

    /**
     * Set the window size to the default size 512-by-512 pixels.
     */
    public static void setCanvasSize() {
        setCanvasSize(DEFAULT_SIZE, DEFAULT_SIZE);
    }

    /**
     * Set the window size to w-by-h pixels.
     *
     * @param w the width as a number of pixels
     * @param h the height as a number of pixels
     * @throws a RunTimeException if the width or height is 0 or negative
     */
    public static void setCanvasLocation(int x, int y) {
    	frame.setLocation(x, y);
    }
    
    public static void setCanvasSize(int w, int h) {
        if (w < 1 || h < 1) throw new RuntimeException("width and height must be positive");
        width = w;
        height = h;
        init();
    }
    
    public static void setCanvasSize(Dimension d) {
    	setCanvasSize(d.width, d.height);
    }
    
    public static BufferedImage getForegroundIconColor() {
    	return colorImage;
    }
   
    public static void setForegroundIconColor(Color foreground) {
    	color.setColor(foreground);
    	color.fillRect(0, 0, 32, 32);
    }
    
    public static BufferedImage getBackgroundIconColor() {
    	return colorImage2;
    }
   
    public static void setBackgroundIconColor(Color background) {
    	color2.setColor(background);
    	color2.fillRect(0, 0, 32, 32);
    }
    
    public static void setAntiAlias(boolean b) {
    	if(!b) {
    		RenderingHints hints = new RenderingHints(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
    		hints.put(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
    		offscreen.addRenderingHints(hints);
    		for(int i = 0;i<=layers;i++) {
    			drawGraphics[i].addRenderingHints(hints);	
    		}
    		previewGraphics.addRenderingHints(hints);
    		return;
    	} else if(b) {
    		RenderingHints hints = new RenderingHints(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            hints.put(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
            offscreen.addRenderingHints(hints);
            for(int i = 0;i<=layers;i++) {
    			drawGraphics[i].addRenderingHints(hints);	
    		}
            previewGraphics.addRenderingHints(hints);
            return;
        }
    }
    
    private static void init() {
        if (frame != null) frame.setVisible(false);
        frame = new JFrame();
        drawImage = new BufferedImage[6];
        drawGraphics = new Graphics2D[6];
        for(int i = 0;i < drawImage.length; i++) {
        	drawImage[i] = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        	drawGraphics[i] = drawImage[i].createGraphics();
        	drawGraphics[i].setColor(new Color(0, 0, 0));
        }
        
        previewImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        previewGraphics = previewImage.createGraphics();
        
        
        offscreenImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        onscreenImage  = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        
        colorImage = new BufferedImage(32, 32, BufferedImage.TYPE_INT_ARGB);
        color = colorImage.createGraphics();
        
        colorImage2 = new BufferedImage(32, 32, BufferedImage.TYPE_INT_ARGB);
        color2 = colorImage2.createGraphics();
        
        offscreen = offscreenImage.createGraphics();
        onscreen  = onscreenImage.createGraphics();
        setXscale();
        setYscale();
        offscreen.setColor(DEFAULT_CLEAR_COLOR);
        
        //offscreen.fillRect(0, 0, width, height);
        setPenColor();
        setPenRadius();
        setFont();
        //clear();

        setAntiAlias(true);
        
        ImageIcon icon = new ImageIcon(onscreenImage);

        JLabel draw = new JLabel(icon);
        draw.addMouseListener(std);
        draw.addMouseMotionListener(std);
        
        frame.setContentPane(draw);
        frame.addKeyListener(std);    // JLabel cannot get keyboard focus
        frame.setResizable(false);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);            // closes all windows
        // frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);      // closes only current window
        frame.setTitle("DrawingPlus");
        frame.setJMenuBar(createMenuBar());
        frame.pack();
        frame.requestFocusInWindow();
     }
    
    public static void visible(boolean b) {
    	frame.setVisible(b);
    	setForegroundIconColor(DrawingThread.getForegroundColor());
    	setBackgroundIconColor(DrawingThread.getBackgroundColor());       
    }

    // create the menu bar (changed to private)
    private static JMenuBar createMenuBar() {
        JMenuBar menuBar = new JMenuBar();
        /*
        //Begin DrawingPlus menu
        JMenu drawMenu = new JMenu("DrawingPlus");
        
        JMenuItem aboutMenuItem = new JMenuItem("About");
        JMenuItem quitMenuItem = new JMenuItem("Quit");
        
        quitMenuItem.addActionListener(std);
        quitMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Q, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
        quitMenuItem.setActionCommand("Quit");
        
        aboutMenuItem.addActionListener(std);
        aboutMenuItem.setActionCommand("About");
        
        drawMenu.add(aboutMenuItem);
        drawMenu.add(quitMenuItem);
        
        menuBar.add(drawMenu);
        
         TODO Fix Save and Open
         * //Begin file menu
         
        JMenu fileMenu = new JMenu("File");
        	
        JMenuItem saveMenuItem = new JMenuItem("Save");
        JMenuItem openMenuItem = new JMenuItem("Open");
        
        openMenuItem.addActionListener(std);
        openMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_O, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
        openMenuItem.setActionCommand("Open");
        
        saveMenuItem.addActionListener(std);
        saveMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
        saveMenuItem.setActionCommand("Save");
        
        fileMenu.add(saveMenuItem);
        fileMenu.add(openMenuItem);
        
        menuBar.add(fileMenu);
        
        //Begin edit menu
        JMenu editMenu = new JMenu("Edit");
        
        menuBar.add(editMenu);
        
        */
        
        
        
        return menuBar;
    }


   /*************************************************************************
    *  User and screen coordinate systems
    *************************************************************************/

    /**
     * Set the x-scale to be the default (between 0.0 and 1.0).
     */
    public static void setXscale() { setXscale(DEFAULT_XMIN, DEFAULT_XMAX); }

    /**
     * Set the y-scale to be the default (between 0.0 and 1.0).
     */
    public static void setYscale() { setYscale(DEFAULT_YMIN, DEFAULT_YMAX); }

    /**
     * Set the x-scale (a 10% border is added to the values)
     * @param min the minimum value of the x-scale
     * @param max the maximum value of the x-scale
     */
    public static void setXscale(double min, double max) {
        double size = max - min;
        xmin = min - BORDER * size;
        xmax = max + BORDER * size;
    }

    /**
     * Set the y-scale (a 10% border is added to the values).
     * @param min the minimum value of the y-scale
     * @param max the maximum value of the y-scale
     */
    public static void setYscale(double min, double max) {
        double size = max - min;
        ymin = min - BORDER * size;
        ymax = max + BORDER * size;
    }

    /**
     * Set the x-scale and y-scale (a 10% border is added to the values)
     * @param min the minimum value of the x- and y-scales
     * @param max the maximum value of the x- and y-scales
     */
    public static void setScale(double min, double max) {
        setXscale(min, max);
        setYscale(min, max);
    }

    // helper functions that scale from user coordinates to screen coordinates and back
    private static double  scaleX(double x) { return width  * (x - xmin) / (xmax - xmin); }
    private static double  scaleY(double y) { return height * (ymax - y) / (ymax - ymin); }
    private static double factorX(double w) { return w * width  / Math.abs(xmax - xmin);  }
    private static double factorY(double h) { return h * height / Math.abs(ymax - ymin);  }
    private static double   userX(double x) { return xmin + x * (xmax - xmin) / width;    }
    private static double   userY(double y) { return ymax - y * (ymax - ymin) / height;   }


    /**
     * Clear the screen to the default color (white).
     */
    public static void clear() { clear(DEFAULT_CLEAR_COLOR); }
    /**
     * Clear the screen to the given color.
     * @param color the Color to make the background
     */
    public static void clear(Color color) {
       previewImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
       previewGraphics = previewImage.createGraphics();
       setAntiAlias(true);
       previewGraphics.setColor(penColor);
       offscreen.setColor(color);
       offscreen.fillRect(0, 0, width, height);
       offscreen.setColor(penColor);
        draw();
    }

    /**
     * Get the current pen radius.
     */
    public static double getPenRadius() { return penRadius; }

    /**
     * Set the pen size to the default (.002).
     */
    public static void setPenRadius() { setPenRadius(DEFAULT_PEN_RADIUS); }
    /**
     * Set the radius of the pen to the given size.
     * @param r the radius of the pen
     * @throws RuntimeException if r is negative
     */
    public static void setPenRadius(double r) {
        if (r < 0) throw new RuntimeException("pen radius must be positive");
        penRadius = r * DEFAULT_SIZE;
        BasicStroke stroke = new BasicStroke((float) penRadius, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND);
        // BasicStroke stroke = new BasicStroke((float) penRadius);
        offscreen.setStroke(stroke);
        for(int i = 0;i<=layers;i++) {
        	drawGraphics[i].setStroke(stroke);
        }
        previewGraphics.setStroke(stroke);
    }
    
    /**
     * Get the current pen color.
     */
    public static Color getPenColor() { return penColor; }

    /**
     * Set the pen color to the default color (black).
     */
    public static void setPenColor() { setPenColor(DEFAULT_PEN_COLOR); }
    /**
     * Set the pen color to the given color. The available pen colors are
     * BLACK, BLUE, CYAN, DARK_GRAY, GRAY, GREEN, LIGHT_GRAY, MAGENTA,
     * ORANGE, PINK, RED, WHITE, and YELLOW.
     * @param color the Color to make the pen
     */
    public static void setPenColor(Color color) {
        penColor = color;
        for(int i = 0;i<=layers;i++) {
        	drawGraphics[i].setColor(penColor);
        }
        offscreen.setColor(penColor);
        previewGraphics.setColor(penColor);
        setForegroundIconColor(penColor);
    }
    

    /**
     * Get the current font.
     */
    public static Font getFont() { return font; }

    /**
     * Set the font to the default font (sans serif, 16 point).
     */
    public static void setFont() { setFont(DEFAULT_FONT); }

    /**
     * Set the font to the given value.
     * @param f the font to make text
     */
    public static void setFont(Font f) { font = f; }


   /*************************************************************************
    *  Drawing geometric shapes.
    *************************************************************************/

    /**
     * Draw a line from (x0, y0) to (x1, y1).
     * @param x0 the x-coordinate of the starting point
     * @param y0 the y-coordinate of the starting point
     * @param x1 the x-coordinate of the destination point
     * @param y1 the y-coordinate of the destination point
     */
    public static void line(double x0, double y0, double x1, double y1) {
        offscreen.draw(new Line2D.Double(scaleX(x0), scaleY(y0), scaleX(x1), scaleY(y1)));
        draw();
    	
    }
    
    public static void line(double x0, double y0, double x1, double y1, Graphics2D g2d) {
        g2d.draw(new Line2D.Double(scaleX(x0), scaleY(y0), scaleX(x1), scaleY(y1)));
        draw();
    	
    }
    
    public static void line(double x0, double y0, double x1, double y1, Graphics2D g2d, Color color) {
        g2d.setColor(color);
    	g2d.draw(new Line2D.Double(scaleX(x0), scaleY(y0), scaleX(x1), scaleY(y1)));
        draw();
    	
    }


    /**
     * Draw one pixel at (x, y).
     * @param x the x-coordinate of the pixel
     * @param y the y-coordinate of the pixel
     */
    private static void pixel(double x, double y) {
        offscreen.fillRect((int) Math.round(scaleX(x)), (int) Math.round(scaleY(y)), 1, 1);
    }
    
    private static void pixel(double x, double y, Graphics2D g2d) {
        g2d.fillRect((int) Math.round(scaleX(x)), (int) Math.round(scaleY(y)), 1, 1);
    }

    /**
     * Draw a point at (x, y).
     * @param x the x-coordinate of the point
     * @param y the y-coordinate of the point
     */
    public static void point(double x, double y) {
        double xs = scaleX(x);
        double ys = scaleY(y);
        double r = penRadius;
        // double ws = factorX(2*r);
        // double hs = factorY(2*r);
        // if (ws <= 1 && hs <= 1) pixel(x, y);
        if (r <= 1) pixel(x, y);
        else offscreen.fill(new Ellipse2D.Double(xs - r/2, ys - r/2, r, r));
        draw();
    }
    
    public static void point(double x, double y, Graphics2D g2d) {
        double xs = scaleX(x);
        double ys = scaleY(y);
        double r = penRadius;
        // double ws = factorX(2*r);
        // double hs = factorY(2*r);
        // if (ws <= 1 && hs <= 1) pixel(x, y);
        if (r <= 1) pixel(x, y, g2d);
        else g2d.fill(new Ellipse2D.Double(xs - r/2, ys - r/2, r, r));
        draw();
    }

    /**
     * Draw a circle of radius r, centered on (x, y).
     * @param x the x-coordinate of the center of the circle
     * @param y the y-coordinate of the center of the circle
     * @param r the radius of the circle
     * @throws RuntimeException if the radius of the circle is negative
     */
    public static void circle(double x, double y, double r) {
        if (r < 0) throw new RuntimeException("circle radius can't be negative");
        double xs = scaleX(x);
        double ys = scaleY(y);
        double ws = factorX(2*r);
        double hs = factorY(2*r);
        if (ws <= 1 && hs <= 1) pixel(x, y);
        else offscreen.draw(new Ellipse2D.Double(xs - ws/2, ys - hs/2, ws, hs));
        draw();
    }
    
    public static void circle(double x, double y, double r, Graphics2D g2d) {
        if (r < 0) throw new RuntimeException("circle radius can't be negative");
        double xs = scaleX(x);
        double ys = scaleY(y);
        double ws = factorX(2*r);
        double hs = factorY(2*r);
        if (ws <= 1 && hs <= 1) pixel(x, y, g2d);
        else g2d.draw(new Ellipse2D.Double(xs - ws/2, ys - hs/2, ws, hs));
        draw();
    }

    /**
     * Draw filled circle of radius r, centered on (x, y).
     * @param x the x-coordinate of the center of the circle
     * @param y the y-coordinate of the center of the circle
     * @param r the radius of the circle
     * @throws RuntimeException if the radius of the circle is negative
     */
    public static void filledCircle(double x, double y, double r) {
        if (r < 0) throw new RuntimeException("circle radius can't be negative");
        double xs = scaleX(x);
        double ys = scaleY(y);
        double ws = factorX(2*r);
        double hs = factorY(2*r);
        if (ws <= 1 && hs <= 1) pixel(x, y);
        else offscreen.fill(new Ellipse2D.Double(xs - ws/2, ys - hs/2, ws, hs));
        draw();
    }
    
    public static void filledCircle(double x, double y, double r, Graphics2D g2d) {
        if (r < 0) throw new RuntimeException("circle radius can't be negative");
        double xs = scaleX(x);
        double ys = scaleY(y);
        double ws = factorX(2*r);
        double hs = factorY(2*r);
        if (ws <= 1 && hs <= 1) pixel(x, y, g2d);
        else g2d.fill(new Ellipse2D.Double(xs - ws/2, ys - hs/2, ws, hs));
        draw();
    }


    /**
     * Draw an ellipse with given semimajor and semiminor axes, centered on (x, y).
     * @param x the x-coordinate of the center of the ellipse
     * @param y the y-coordinate of the center of the ellipse
     * @param semiMajorAxis is the semimajor axis of the ellipse
     * @param semiMinorAxis is the semiminor axis of the ellipse
     * @throws RuntimeException if either of the axes are negative
     */
    public static void ellipse(double x, double y, double semiMajorAxis, double semiMinorAxis) {
        if (semiMajorAxis < 0) throw new RuntimeException("ellipse semimajor axis can't be negative");
        if (semiMinorAxis < 0) throw new RuntimeException("ellipse semiminor axis can't be negative");
        double xs = scaleX(x);
        double ys = scaleY(y);
        double ws = factorX(2*semiMajorAxis);
        double hs = factorY(2*semiMinorAxis);
        if (ws <= 1 && hs <= 1) pixel(x, y);
        else offscreen.draw(new Ellipse2D.Double(xs - ws/2, ys - hs/2, ws, hs));
        draw();
    }
    
    public static void ellipse(double x, double y, double semiMajorAxis, double semiMinorAxis, Graphics2D g2d) {
        if (semiMajorAxis < 0) throw new RuntimeException("ellipse semimajor axis can't be negative");
        if (semiMinorAxis < 0) throw new RuntimeException("ellipse semiminor axis can't be negative");
        double xs = scaleX(x);
        double ys = scaleY(y);
        double ws = factorX(2*semiMajorAxis);
        double hs = factorY(2*semiMinorAxis);
        if (ws <= 1 && hs <= 1) pixel(x, y, g2d);
        else g2d.draw(new Ellipse2D.Double(xs - ws/2, ys - hs/2, ws, hs));
        draw();
    }

    /**
     * Draw an ellipse with given semimajor and semiminor axes, centered on (x, y).
     * @param x the x-coordinate of the center of the ellipse
     * @param y the y-coordinate of the center of the ellipse
     * @param semiMajorAxis is the semimajor axis of the ellipse
     * @param semiMinorAxis is the semiminor axis of the ellipse
     * @throws RuntimeException if either of the axes are negative
     */
    public static void filledEllipse(double x, double y, double semiMajorAxis, double semiMinorAxis) {
        if (semiMajorAxis < 0) throw new RuntimeException("ellipse semimajor axis can't be negative");
        if (semiMinorAxis < 0) throw new RuntimeException("ellipse semiminor axis can't be negative");
        double xs = scaleX(x);
        double ys = scaleY(y);
        double ws = factorX(2*semiMajorAxis);
        double hs = factorY(2*semiMinorAxis);
        if (ws <= 1 && hs <= 1) pixel(x, y);
        else offscreen.fill(new Ellipse2D.Double(xs - ws/2, ys - hs/2, ws, hs));
        draw();
    }
    
    public static void filledEllipse(double x, double y, double semiMajorAxis, double semiMinorAxis, Graphics2D g2d) {
        if (semiMajorAxis < 0) throw new RuntimeException("ellipse semimajor axis can't be negative");
        if (semiMinorAxis < 0) throw new RuntimeException("ellipse semiminor axis can't be negative");
        double xs = scaleX(x);
        double ys = scaleY(y);
        double ws = factorX(2*semiMajorAxis);
        double hs = factorY(2*semiMinorAxis);
        if (ws <= 1 && hs <= 1) pixel(x, y, g2d);
        else g2d.fill(new Ellipse2D.Double(xs - ws/2, ys - hs/2, ws, hs));
        draw();
    }


    /**
     * Draw an arc of radius r, centered on (x, y), from angle1 to angle2 (in degrees).
     * @param x the x-coordinate of the center of the circle
     * @param y the y-coordinate of the center of the circle
     * @param r the radius of the circle
     * @param angle1 the starting angle. 0 would mean an arc beginning at 3 o'clock.
     * @param angle2 the angle at the end of the arc. For example, if
     *        you want a 90 degree arc, then angle2 should be angle1 + 90.
     * @throws RuntimeException if the radius of the circle is negative
     */
    public static void arc(double x, double y, double r, double angle1, double angle2) {
        if (r < 0) throw new RuntimeException("arc radius can't be negative");
        while (angle2 < angle1) angle2 += 360;
        double xs = scaleX(x);
        double ys = scaleY(y);
        double ws = factorX(2*r);
        double hs = factorY(2*r);
        if (ws <= 1 && hs <= 1) pixel(x, y);
        else offscreen.draw(new Arc2D.Double(xs - ws/2, ys - hs/2, ws, hs, angle1, angle2 - angle1, Arc2D.OPEN));
        draw();
    }
    
    public static void arc(double x, double y, double r, double angle1, double angle2, Graphics2D g2d) {
        if (r < 0) throw new RuntimeException("arc radius can't be negative");
        while (angle2 < angle1) angle2 += 360;
        double xs = scaleX(x);
        double ys = scaleY(y);
        double ws = factorX(2*r);
        double hs = factorY(2*r);
        if (ws <= 1 && hs <= 1) pixel(x, y, g2d);
        else g2d.draw(new Arc2D.Double(xs - ws/2, ys - hs/2, ws, hs, angle1, angle2 - angle1, Arc2D.OPEN));
        draw();
    }

    /**
     * Draw a square of side length 2r, centered on (x, y).
     * @param x the x-coordinate of the center of the square
     * @param y the y-coordinate of the center of the square
     * @param r radius is half the length of any side of the square
     * @throws RuntimeException if r is negative
     */
    public static void square(double x, double y, double r) {
        if (r < 0) throw new RuntimeException("square side length can't be negative");
        double xs = scaleX(x);
        double ys = scaleY(y);
        double ws = factorX(2*r);
        double hs = factorY(2*r);
        if (ws <= 1 && hs <= 1) pixel(x, y);
        else offscreen.draw(new Rectangle2D.Double(xs - ws/2, ys - hs/2, ws, hs));
        draw();
    }

    public static void square(double x, double y, double r, Graphics2D g2d) {
        if (r < 0) throw new RuntimeException("square side length can't be negative");
        double xs = scaleX(x);
        double ys = scaleY(y);
        double ws = factorX(2*r);
        double hs = factorY(2*r);
        if (ws <= 1 && hs <= 1) pixel(x, y, g2d);
        else g2d.draw(new Rectangle2D.Double(xs - ws/2, ys - hs/2, ws, hs));
        draw();
    }

    /**
     * Draw a filled square of side length 2r, centered on (x, y).
     * @param x the x-coordinate of the center of the square
     * @param y the y-coordinate of the center of the square
     * @param r radius is half the length of any side of the square
     * @throws RuntimeException if r is negative
     */
    public static void filledSquare(double x, double y, double r) {
        if (r < 0) throw new RuntimeException("square side length can't be negative");
        double xs = scaleX(x);
        double ys = scaleY(y);
        double ws = factorX(2*r);
        double hs = factorY(2*r);
        if (ws <= 1 && hs <= 1) pixel(x, y);
        else offscreen.fill(new Rectangle2D.Double(xs - ws/2, ys - hs/2, ws, hs));
        draw();
    }

    public static void filledSquare(double x, double y, double r, Graphics2D g2d) {
        if (r < 0) throw new RuntimeException("square side length can't be negative");
        double xs = scaleX(x);
        double ys = scaleY(y);
        double ws = factorX(2*r);
        double hs = factorY(2*r);
        if (ws <= 1 && hs <= 1) pixel(x, y, g2d);
        else g2d.fill(new Rectangle2D.Double(xs - ws/2, ys - hs/2, ws, hs));
        draw();
    }

    /**
     * Draw a rectangle of given half width and half height, centered on (x, y).
     * @param x the x-coordinate of the center of the rectangle
     * @param y the y-coordinate of the center of the rectangle
     * @param halfWidth is half the width of the rectangle
     * @param halfHeight is half the height of the rectangle
     * @throws RuntimeException if halfWidth or halfHeight is negative
     */
    public static void rectangle(double x, double y, double halfWidth, double halfHeight) {
        if (halfWidth  < 0) throw new RuntimeException("half width can't be negative");
        if (halfHeight < 0) throw new RuntimeException("half height can't be negative");
        double xs = scaleX(x);
        double ys = scaleY(y);
        double ws = factorX(2*halfWidth);
        double hs = factorY(2*halfHeight);
        if (ws <= 1 && hs <= 1) pixel(x, y);
        else offscreen.draw(new Rectangle2D.Double(xs - ws/2, ys - hs/2, ws, hs));
        draw();
    }
    
    public static void rectangle(double x, double y, double halfWidth, double halfHeight, Graphics2D g2d) {
        if (halfWidth  < 0) throw new RuntimeException("half width can't be negative");
        if (halfHeight < 0) throw new RuntimeException("half height can't be negative");
        double xs = scaleX(x);
        double ys = scaleY(y);
        double ws = factorX(2*halfWidth);
        double hs = factorY(2*halfHeight);
        if (ws <= 1 && hs <= 1) pixel(x, y, g2d);
        else g2d.draw(new Rectangle2D.Double(xs - ws/2, ys - hs/2, ws, hs));
        draw();
    }

    /**
     * Draw a filled rectangle of given half width and half height, centered on (x, y).
     * @param x the x-coordinate of the center of the rectangle
     * @param y the y-coordinate of the center of the rectangle
     * @param halfWidth is half the width of the rectangle
     * @param halfHeight is half the height of the rectangle
     * @throws RuntimeException if halfWidth or halfHeight is negative
     */
    public static void filledRectangle(double x, double y, double halfWidth, double halfHeight) {
        if (halfWidth  < 0) throw new RuntimeException("half width can't be negative");
        if (halfHeight < 0) throw new RuntimeException("half height can't be negative");
        double xs = scaleX(x);
        double ys = scaleY(y);
        double ws = factorX(2*halfWidth);
        double hs = factorY(2*halfHeight);
        if (ws <= 1 && hs <= 1) pixel(x, y);
        else offscreen.fill(new Rectangle2D.Double(xs - ws/2, ys - hs/2, ws, hs));
        draw();
    }
    
    public static void filledRectangle(double x, double y, double halfWidth, double halfHeight, Graphics2D g2d) {
        if (halfWidth  < 0) throw new RuntimeException("half width can't be negative");
        if (halfHeight < 0) throw new RuntimeException("half height can't be negative");
        double xs = scaleX(x);
        double ys = scaleY(y);
        double ws = factorX(2*halfWidth);
        double hs = factorY(2*halfHeight);
        if (ws <= 1 && hs <= 1) pixel(x, y, g2d);
        else g2d.fill(new Rectangle2D.Double(xs - ws/2, ys - hs/2, ws, hs));
        draw();
    }

    /**
     * Draw a polygon with the given (x[i], y[i]) coordinates.
     * @param x an array of all the x-coordindates of the polygon
     * @param y an array of all the y-coordindates of the polygon
     */
    public static void polygon(double[] x, double[] y) {
        int N = x.length;
        GeneralPath path = new GeneralPath();
        path.moveTo((float) scaleX(x[0]), (float) scaleY(y[0]));
        for (int i = 0; i < N; i++)
            path.lineTo((float) scaleX(x[i]), (float) scaleY(y[i]));
        path.closePath();
        offscreen.draw(path);
        draw();
    }
    
    public static void polygon(double[] x, double[] y, Graphics2D g2d) {
        int N = x.length;
        GeneralPath path = new GeneralPath();
        path.moveTo((float) scaleX(x[0]), (float) scaleY(y[0]));
        for (int i = 0; i < N; i++)
            path.lineTo((float) scaleX(x[i]), (float) scaleY(y[i]));
        path.closePath();
        g2d.draw(path);
        draw();
    }

    /**
     * Draw a filled polygon with the given (x[i], y[i]) coordinates.
     * @param x an array of all the x-coordindates of the polygon
     * @param y an array of all the y-coordindates of the polygon
     */
    public static void filledPolygon(double[] x, double[] y) {
        int N = x.length;
        GeneralPath path = new GeneralPath();
        path.moveTo((float) scaleX(x[0]), (float) scaleY(y[0]));
        for (int i = 0; i < N; i++)
            path.lineTo((float) scaleX(x[i]), (float) scaleY(y[i]));
        path.closePath();
        offscreen.fill(path);
        draw();
    }
    
    public static void filledPolygon(double[] x, double[] y, Graphics2D g2d) {
        int N = x.length;
        GeneralPath path = new GeneralPath();
        path.moveTo((float) scaleX(x[0]), (float) scaleY(y[0]));
        for (int i = 0; i < N; i++)
            path.lineTo((float) scaleX(x[i]), (float) scaleY(y[i]));
        path.closePath();
        g2d.fill(path);
        draw();
    }


   /*************************************************************************
    *  Drawing images.
    *************************************************************************/

    // get an image from the given filename
    private static Image getImage(String filename) {

        // to read from file
        ImageIcon icon = new ImageIcon(filename);

        // try to read from URL
        if ((icon == null) || (icon.getImageLoadStatus() != MediaTracker.COMPLETE)) {
            try {
                URL url = new URL(filename);
                icon = new ImageIcon(url);
            } catch (Exception e) { /* not a url */ }
        }

        // in case file is inside a .jar
        if ((icon == null) || (icon.getImageLoadStatus() != MediaTracker.COMPLETE)) {
            URL url = StdDraw.class.getResource(filename);
            if (url == null) throw new RuntimeException("image " + filename + " not found");
            icon = new ImageIcon(url);
        }

        return icon.getImage();
    }

    /**
     * Draw picture (gif, jpg, or png) centered on (x, y).
     * @param x the center x-coordinate of the image
     * @param y the center y-coordinate of the image
     * @param s the name of the image/picture, e.g., "ball.gif"
     * @throws RuntimeException if the image is corrupt
     */
    public static void picture(double x, double y, String s) {
        Image image = getImage(s);
        double xs = scaleX(x);
        double ys = scaleY(y);
        int ws = image.getWidth(null);
        int hs = image.getHeight(null);
        if (ws < 0 || hs < 0) throw new RuntimeException("image " + s + " is corrupt");

        offscreen.drawImage(image, (int) Math.round(xs - ws/2.0), (int) Math.round(ys - hs/2.0), null);
        draw();
    }
    
    public static void picture(double x, double y, String s, Graphics2D g2d) {
        Image image = getImage(s);
        double xs = scaleX(x);
        double ys = scaleY(y);
        int ws = image.getWidth(null);
        int hs = image.getHeight(null);
        if (ws < 0 || hs < 0) throw new RuntimeException("image " + s + " is corrupt");

        g2d.drawImage(image, (int) Math.round(xs - ws/2.0), (int) Math.round(ys - hs/2.0), null);
        draw();
    }

    /**
     * Draw picture (gif, jpg, or png) centered on (x, y),
     * rotated given number of degrees
     * @param x the center x-coordinate of the image
     * @param y the center y-coordinate of the image
     * @param s the name of the image/picture, e.g., "ball.gif"
     * @param degrees is the number of degrees to rotate counterclockwise
     * @throws RuntimeException if the image is corrupt
     */
    public static void picture(double x, double y, String s, double degrees) {
        Image image = getImage(s);
        double xs = scaleX(x);
        double ys = scaleY(y);
        int ws = image.getWidth(null);
        int hs = image.getHeight(null);
        if (ws < 0 || hs < 0) throw new RuntimeException("image " + s + " is corrupt");

        offscreen.rotate(Math.toRadians(-degrees), xs, ys);
        offscreen.drawImage(image, (int) Math.round(xs - ws/2.0), (int) Math.round(ys - hs/2.0), null);
        offscreen.rotate(Math.toRadians(+degrees), xs, ys);

        draw();
    }
    
    public static void picture(double x, double y, String s, double degrees, Graphics2D g2d) {
        Image image = getImage(s);
        double xs = scaleX(x);
        double ys = scaleY(y);
        int ws = image.getWidth(null);
        int hs = image.getHeight(null);
        if (ws < 0 || hs < 0) throw new RuntimeException("image " + s + " is corrupt");

        g2d.rotate(Math.toRadians(-degrees), xs, ys);
        g2d.drawImage(image, (int) Math.round(xs - ws/2.0), (int) Math.round(ys - hs/2.0), null);
        g2d.rotate(Math.toRadians(+degrees), xs, ys);

        draw();
    }

    /**
     * Draw picture (gif, jpg, or png) centered on (x, y), rescaled to w-by-h.
     * @param x the center x coordinate of the image
     * @param y the center y coordinate of the image
     * @param s the name of the image/picture, e.g., "ball.gif"
     * @param w the width of the image
     * @param h the height of the image
     * @throws RuntimeException if the width height are negative
     * @throws RuntimeException if the image is corrupt
     */
    public static void picture(double x, double y, String s, double w, double h) {
        Image image = getImage(s);
        double xs = scaleX(x);
        double ys = scaleY(y);
        if (w < 0) throw new RuntimeException("width is negative: " + w);
        if (h < 0) throw new RuntimeException("height is negative: " + h);
        double ws = factorX(w);
        double hs = factorY(h);
        if (ws < 0 || hs < 0) throw new RuntimeException("image " + s + " is corrupt");
        if (ws <= 1 && hs <= 1) pixel(x, y);
        else {
            offscreen.drawImage(image, (int) Math.round(xs - ws/2.0),
                                       (int) Math.round(ys - hs/2.0),
                                       (int) Math.round(ws),
                                       (int) Math.round(hs), null);
        }
        draw();
    }
    
    public static void picture(double x, double y, String s, double w, double h, Graphics2D g2d) {
        Image image = getImage(s);
        double xs = scaleX(x);
        double ys = scaleY(y);
        if (w < 0) throw new RuntimeException("width is negative: " + w);
        if (h < 0) throw new RuntimeException("height is negative: " + h);
        double ws = factorX(w);
        double hs = factorY(h);
        if (ws < 0 || hs < 0) throw new RuntimeException("image " + s + " is corrupt");
        if (ws <= 1 && hs <= 1) pixel(x, y);
        else {
            g2d.drawImage(image, (int) Math.round(xs - ws/2.0),
                                       (int) Math.round(ys - hs/2.0),
                                       (int) Math.round(ws),
                                       (int) Math.round(hs), null);
        }
        draw();
    }



    /**
     * Draw picture (gif, jpg, or png) centered on (x, y), rotated
     * given number of degrees, rescaled to w-by-h.
     * @param x the center x-coordinate of the image
     * @param y the center y-coordinate of the image
     * @param s the name of the image/picture, e.g., "ball.gif"
     * @param w the width of the image
     * @param h the height of the image
     * @param degrees is the number of degrees to rotate counterclockwise
     * @throws RuntimeException if the image is corrupt
     */
    public static void picture(double x, double y, String s, double w, double h, double degrees) {
        Image image = getImage(s);
        double xs = scaleX(x);
        double ys = scaleY(y);
        double ws = factorX(w);
        double hs = factorY(h);
        if (ws < 0 || hs < 0) throw new RuntimeException("image " + s + " is corrupt");
        if (ws <= 1 && hs <= 1) pixel(x, y);

        offscreen.rotate(Math.toRadians(-degrees), xs, ys);
        offscreen.drawImage(image, (int) Math.round(xs - ws/2.0),
                                   (int) Math.round(ys - hs/2.0),
                                   (int) Math.round(ws),
                                   (int) Math.round(hs), null);
        offscreen.rotate(Math.toRadians(+degrees), xs, ys);

        draw();
    }
    
    public static void picture(double x, double y, String s, double w, double h, double degrees, Graphics2D g2d) {
        Image image = getImage(s);
        double xs = scaleX(x);
        double ys = scaleY(y);
        double ws = factorX(w);
        double hs = factorY(h);
        if (ws < 0 || hs < 0) throw new RuntimeException("image " + s + " is corrupt");
        if (ws <= 1 && hs <= 1) pixel(x, y);

        g2d.rotate(Math.toRadians(-degrees), xs, ys);
        g2d.drawImage(image, (int) Math.round(xs - ws/2.0),
                                   (int) Math.round(ys - hs/2.0),
                                   (int) Math.round(ws),
                                   (int) Math.round(hs), null);
        g2d.rotate(Math.toRadians(+degrees), xs, ys);

        draw();
    }


   /*************************************************************************
    *  Drawing text.
    *************************************************************************/

    /**
     * Write the given text string in the current font, centered on (x, y).
     * @param x the center x-coordinate of the text
     * @param y the center y-coordinate of the text
     * @param s the text
     */
    public static void text(double x, double y, String s) {
        offscreen.setFont(font);
        FontMetrics metrics = offscreen.getFontMetrics();
        double xs = scaleX(x);
        double ys = scaleY(y);
        int ws = metrics.stringWidth(s);
        int hs = metrics.getDescent();
        offscreen.drawString(s, (float) (xs - ws/2.0), (float) (ys + hs));
        draw();
    }
    
    public static void text(double x, double y, String s, Graphics2D g2d) {
        g2d.setFont(font);
        FontMetrics metrics = g2d.getFontMetrics();
        double xs = scaleX(x);
        double ys = scaleY(y);
        int ws = metrics.stringWidth(s);
        int hs = metrics.getDescent();
        g2d.drawString(s, (float) (xs - ws/2.0), (float) (ys + hs));
        draw();
    }

    /**
     * Write the given text string in the current font, centered on (x, y) and
     * rotated by the specified number of degrees  
     * @param x the center x-coordinate of the text
     * @param y the center y-coordinate of the text
     * @param s the text
     * @param degrees is the number of degrees to rotate counterclockwise
     */
    public static void text(double x, double y, String s, double degrees) {
        double xs = scaleX(x);
        double ys = scaleY(y);
        offscreen.rotate(Math.toRadians(-degrees), xs, ys);
        text(x, y, s);
        offscreen.rotate(Math.toRadians(+degrees), xs, ys);
    }

    public static void text(double x, double y, String s, double degrees, Graphics2D g2d) {
        double xs = scaleX(x);
        double ys = scaleY(y);
        g2d.rotate(Math.toRadians(-degrees), xs, ys);
        text(x, y, s, g2d);
        g2d.rotate(Math.toRadians(+degrees), xs, ys);
    }

    /**
     * Write the given text string in the current font, left-aligned at (x, y).
     * @param x the x-coordinate of the text
     * @param y the y-coordinate of the text
     * @param s the text
     */
    public static void textLeft(double x, double y, String s) {
        offscreen.setFont(font);
        FontMetrics metrics = offscreen.getFontMetrics();
        double xs = scaleX(x);
        double ys = scaleY(y);
        int hs = metrics.getDescent();
        offscreen.drawString(s, (float) (xs), (float) (ys + hs));
        draw();
    }
    
    public static void textLeft(double x, double y, String s, Graphics2D g2d) {
        g2d.setFont(font);
        FontMetrics metrics = g2d.getFontMetrics();
        double xs = scaleX(x);
        double ys = scaleY(y);
        int hs = metrics.getDescent();
        g2d.drawString(s, (float) (xs), (float) (ys + hs));
        draw();
    }
    
    
    
    /**
     * Write the given text string in the current font, right-aligned at (x, y).
     * @param x the x-coordinate of the text
     * @param y the y-coordinate of the text
     * @param s the text
     */
    public static void textRight(double x, double y, String s) {
        offscreen.setFont(font);
        FontMetrics metrics = offscreen.getFontMetrics();
        double xs = scaleX(x);
        double ys = scaleY(y);
        int ws = metrics.stringWidth(s);
        int hs = metrics.getDescent();
        offscreen.drawString(s, (float) (xs - ws), (float) (ys + hs));
        draw();
    }

    public static void textRight(double x, double y, String s, Graphics2D g2d) {
        g2d.setFont(font);
        FontMetrics metrics = g2d.getFontMetrics();
        double xs = scaleX(x);
        double ys = scaleY(y);
        int ws = metrics.stringWidth(s);
        int hs = metrics.getDescent();
        g2d.drawString(s, (float) (xs - ws), (float) (ys + hs));
        draw();
    }



    /**
     * Display on screen, pause for t milliseconds, and turn on
     * <em>animation mode</em>: subsequent calls to
     * drawing methods such as <tt>line()</tt>, <tt>circle()</tt>, and <tt>square()</tt>
     * will not be displayed on screen until the next call to <tt>show()</tt>.
     * This is useful for producing animations (clear the screen, draw a bunch of shapes,
     * display on screen for a fixed amount of time, and repeat). It also speeds up
     * drawing a huge number of shapes (call <tt>show(0)</tt> to defer drawing
     * on screen, draw the shapes, and call <tt>show(0)</tt> to display them all
     * on screen at once).
     * @param t number of milliseconds
     */
    public static void show(int t) {
        defer = false;
        draw();
        try { Thread.currentThread().sleep(t); }
        catch (InterruptedException e) { System.out.println("Error sleeping"); }
        defer = true;
    }

    /**
     * Display on-screen and turn off animation mode:
     * subsequent calls to
     * drawing methods such as <tt>line()</tt>, <tt>circle()</tt>, and <tt>square()</tt>
     * will be displayed on screen when called. This is the default.
     */
    public static void show() {
        defer = false;
        draw();
    }

    // draw onscreen if defer is false
    private static void draw() {
        if (defer) return;
        onscreen.drawImage(offscreenImage, 0, 0, null);
        
        for(int i = 0;i <= layers;i++) {
    		onscreen.drawImage(drawImage[i], 0, 0, null);
    	}
        onscreen.drawImage(previewImage, 0, 0, null);
        color.drawImage(colorImage, 0, 0, null);
    	color2.drawImage(colorImage2, 0, 0, null);
    	frame.repaint();
    }


   /*************************************************************************
    *  Save drawing to a file.
    *************************************************************************/

    /**
     * Save onscreen image to file - suffix must be png, jpg, or gif.
     * @param filename the name of the file with one of the required suffixes
     */
    public static void save(String filename) {
        File file = new File(filename);
        String suffix = filename.substring(filename.lastIndexOf('.') + 1);

        if (suffix.toLowerCase().equals("png")) {
            try { ImageIO.write(onscreenImage, suffix, file); }
            catch (IOException e) { e.printStackTrace(); }
        } else if (suffix.toLowerCase().equals("jpg")) {
            WritableRaster raster = onscreenImage.getRaster();
            WritableRaster newRaster;
            newRaster = raster.createWritableChild(0, 0, width, height, 0, 0, new int[] {0, 1, 2});
            DirectColorModel cm = (DirectColorModel) onscreenImage.getColorModel();
            DirectColorModel newCM = new DirectColorModel(cm.getPixelSize(), cm.getRedMask(), cm.getGreenMask(), cm.getBlueMask());
            BufferedImage rgbBuffer = new BufferedImage(newCM, newRaster, false,  null);
            try { ImageIO.write(rgbBuffer, suffix, file); }
            catch (IOException e) { e.printStackTrace(); }
        }
        else if(suffix.toLowerCase().equals("")) { //|| filename.lastIndexOf('.') == 0) {
        	System.out.println("Index: " + filename.lastIndexOf('.'));
        } else {
            System.out.println("Invalid image file type: " + suffix);
        }
    }


    /**
     * This method cannot be called directly.
     */
    public void actionPerformed(ActionEvent e) {
    	
    	if(e.getActionCommand().equals("Save")) {
        FileDialog chooser = new FileDialog(StdDraw.frame, "Save your drawing", FileDialog.SAVE);
        chooser.setVisible(true);
        String filename = chooser.getFile();
	        if (filename != null) {
        		StdDraw.save(chooser.getDirectory() + File.separator + chooser.getFile());
        	}
    	} else if(e.getActionCommand().equals("Open")) {
    		FileDialog chooser = new FileDialog(StdDraw.frame, "Open an image", FileDialog.LOAD);
            chooser.setVisible(true);
            String filename = chooser.getFile();
            if (filename != null) {
        		//StdDraw.save(chooser.getDirectory() + File.separator + chooser.getFile());
        	}
    	}
    	
    }


   /*************************************************************************
    *  Mouse interactions.
    *************************************************************************/

    /**
     * Is the mouse being pressed?
     * @return true or false
     */
    public static boolean mousePressed() {
        synchronized (mouseLock) {
            return mousePressed;
        }
    }

    public static boolean mouseDragged() {
        synchronized (mouseLock) {
            return mouseDrag;
        }
    }
    
    public static void setMouseDragged(boolean b) {
    	synchronized (mouseLock) {
    		mouseDrag = b;
    	}
    }
    
    public static void setMouseReleased(boolean b) {
    	synchronized (mouseLock) {
    		mouseRelease = b;
    	}
    }

    public static boolean mouseReleased() {
        synchronized (mouseLock) {
            return mouseRelease;
        }
    }
    /**
     * What is the x-coordinate of the mouse?
     * @return the value of the x-coordinate of the mouse
     */
    public static double mouseX() {
        synchronized (mouseLock) {
            return mouseX;
        }
    }

    /**
     * What is the y-coordinate of the mouse?
     * @return the value of the y-coordinate of the mouse
     */
    public static double mouseY() {
        synchronized (mouseLock) {
            return mouseY;
        }
    }


    /**
     * This method cannot be called directly.
     */
    public void mouseClicked(MouseEvent e) { }

    /**
     * This method cannot be called directly.
     */
    public void mouseEntered(MouseEvent e) { }

    /**
     * This method cannot be called directly.
     */
    public void mouseExited(MouseEvent e) { }

    /**
     * This method cannot be called directly.
     */
    
    public void mousePressed(MouseEvent e) {
        synchronized (mouseLock) {
            mouseX = StdDraw.userX(e.getX());
            mouseY = StdDraw.userY(e.getY());
            if(e.getButton() == MouseEvent.BUTTON3) {
            	right = true;
            }
            mousePressed = true;
        }
    }

    /**
     * This method cannot be called directly.
     */
    public void mouseReleased(MouseEvent e) {
        synchronized (mouseLock) {
            mousePressed = false;
            mouseRelease = true;
            
        }
    }

    /**
     * This method cannot be called directly.
     */
    public void mouseDragged(MouseEvent e)  {
        synchronized (mouseLock) {
            mouseX = StdDraw.userX(e.getX());
            mouseY = StdDraw.userY(e.getY());
            mouseDrag = true;
        }
    }

    /**
     * This method cannot be called directly.
     */
    public void mouseMoved(MouseEvent e) {
        synchronized (mouseLock) {
            mouseX = StdDraw.userX(e.getX());
            mouseY = StdDraw.userY(e.getY());
        }
    }


   /*************************************************************************
    *  Keyboard interactions.
    *************************************************************************/

    /**
     * Has the user typed a key?
     * @return true if the user has typed a key, false otherwise
     */
    public static boolean hasNextKeyTyped() {
        synchronized (keyLock) {
            return !keysTyped.isEmpty();
        }
    }

    /**
     * What is the next key that was typed by the user? This method returns
     * a Unicode character corresponding to the key typed (such as 'a' or 'A').
     * It cannot identify action keys (such as F1
     * and arrow keys) or modifier keys (such as control).
     * @return the next Unicode key typed
     */
    public static char nextKeyTyped() {
        synchronized (keyLock) {
            return keysTyped.removeLast();
        }
    }

    /**
     * Is the keycode currently being pressed? This method takes as an argument
     * the keycode (corresponding to a physical key). It can handle action keys
     * (such as F1 and arrow keys) and modifier keys (such as shift and control).
     * See <a href = "http://download.oracle.com/javase/6/docs/api/java/awt/event/KeyEvent.html">KeyEvent.java</a>
     * for a description of key codes.
     * @return true if keycode is currently being pressed, false otherwise
     */
    public static boolean isKeyPressed(int keycode) {
        return keysDown.contains(keycode);
    }


    /**
     * This method cannot be called directly.
     */
    public void keyTyped(KeyEvent e) {
        synchronized (keyLock) {
            keysTyped.addFirst(e.getKeyChar());
        }
    }

    /**
     * This method cannot be called directly.
     */
    public void keyPressed(KeyEvent e) {
        keysDown.add(e.getKeyCode());
    }

    /**
     * This method cannot be called directly.
     */
    public void keyReleased(KeyEvent e) {
        keysDown.remove(e.getKeyCode());
    }

}
