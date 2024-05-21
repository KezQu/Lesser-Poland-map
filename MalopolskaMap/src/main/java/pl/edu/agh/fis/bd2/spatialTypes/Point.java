package pl.edu.agh.fis.bd2.spatialTypes;

import org.json.JSONObject;

import java.io.Serializable;
/**
 * Class providing mapping of point from MSSQL Server database to spring boot application
 */
public class Point implements Serializable {
	private double X, Y;

	public Point(){}

	/**
	 * Creates point with specified ccoordinates
	 * @param X x coordinate
	 * @param Y y coordinate
	 */
	public Point(double X, double Y){
		this.X = X;
		this.Y = Y;
	}

	/**
	 * Creates point from provided json
	 * @param json json to parse into point
	 */
	public Point(JSONObject json){
		this.X = json.getDouble("x");
		this.Y = json.getDouble("y");
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
