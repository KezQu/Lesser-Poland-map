package pl.edu.agh.fis.bd2.spatialTypes;

import org.json.JSONObject;

import java.io.Serializable;

public class Point implements Serializable {
	private double X, Y;

	public Point(){}
	public Point(double X, double Y){
		this.X = X;
		this.Y = Y;
	}
	public Point(JSONObject json){
		this.X = json.getDouble("x");
		this.Y = json.getDouble("y");
	}
	public double Distance(Point p){
		return Math.sqrt((X - p.X) * (X - p.X) + (Y - p.Y) * (Y - p.Y));
	}

	public String toString(){
		return "[" + X + "," + Y + "]";
	}
	public String toStringForQuery(){
		return "(" + X + "," + Y + ")";
	}
	public void setX(double x) {
		X = x;
	}

	public void setY(double y) {
		Y = y;
	}
	public double getX() {
		return X;
	}

	public double getY() {
		return Y;
	}
}
