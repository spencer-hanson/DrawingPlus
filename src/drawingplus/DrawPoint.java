package drawingplus;

public class DrawPoint {
	double x;
	double y;
	
	DrawPoint() {
		x = 0;
		y = 0;
	}
	
	DrawPoint(double x, double y) {
		this.x = x;
		this.y = y;
	}
	
	public double getX() {
		return x;
	}
	
	public double getY() {
		return y;
	}
	
	public void setX(double x) {
		this.x = x;
	}
	
	public void setY(double y) {
		this.y = y;
	}

}
