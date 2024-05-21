package pl.edu.agh.fis.bd2.spatialTypes;

import org.json.JSONObject;

import java.io.Serializable;
/**
 * Class providing mapping of line from MSSQL Server database to spring boot application
 */
public class Line implements Serializable {
	private Point P1, P2;
	public Line(){};

	/**
	 * Creates line from two points
	 * @param P1 starting point
	 * @param P2 ending point
	 */
	public Line(Point P1, Point P2){
		this.P1 = P1;
		this.P2 = P2;
	}

	/**
	 * Creates line from provded json
	 * @param json json to parse into line
	 */
	public Line(JSONObject json){
		this.P1 = new Point(json.getJSONObject("p1"));
		this.P2 = new Point(json.getJSONObject("p2"));
	}
	public String toString(){
		return "[" + P1.toString() + "," + P2.toString() + "]";
	}
	public String toStringForQuery(){
		return "[" + P1.toStringForQuery() + ";" + P2.toStringForQuery() + "]";
	}
	public void setP1(Point p1) {
		P1 = p1;
	}

	public void setP2(Point p2) {
		P2 = p2;
	}

	public Point getP1() {
		return P1;
	}

	public Point getP2() {
		return P2;
	}
}
