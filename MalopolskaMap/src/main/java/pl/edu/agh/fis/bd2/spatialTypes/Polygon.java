package pl.edu.agh.fis.bd2.spatialTypes;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;
/**
 * Class providing mapping of polygon from MSSQL Server database to spring boot application
 */
public class Polygon implements Serializable {
	List<Point> points;
	public Polygon(){
		this.points = new LinkedList<>();
	}

	/**
	 * Creates Polygon from provided list of points
	 * @param points points to parse into polygon
	 */
	public Polygon(List<Point> points){
		this.points = points;
	}

	/**
	 * Creates polygon of out json
	 * @param json json to parse into Polygon
	 */
	public Polygon(JSONArray json){
		this();
		for(int i = 0 ; i < json.length(); i++){
			this.points.add(new Point(json.getJSONObject(i)));
		}
	}
	public String toString(){
		StringBuilder string = new StringBuilder("[");
		for(Point p : points){
			string.append(p.toString()).append(";");
		}
		string.deleteCharAt(string.length() - 1);
		string.append("]");
		return  string.toString();
	}
	public String toStringForQuery(){
		StringBuilder string = new StringBuilder("[");
		for(Point p : points){
			string.append(p.toStringForQuery()).append(";");
		}
		string.deleteCharAt(string.length() - 1);
		string.append("]");
		return  string.toString();
	}
	public List<Point> getPoints() {
		return points;
	}
	public void setPoints(List<Point> points) {
		this.points = points;
	}

	/**
	 * Allows to set points of a polygon using stream of bytes of serialized polygon
	 * @param stream Stream of bytes to parse into polygon
	 * @throws IOException Exception in case of invalid input stream
	 */
	public void setPoints(InputStream stream) throws IOException {
		if(stream != null){
			byte[] test = stream.readAllBytes();
			StringBuilder dataParser = new StringBuilder();
			int maxSize = 0;
			for (int i = 0; i < 8; i++) {
				maxSize += (test[i] & 0xFF);
			}
			for(int i = 8 ; i < test.length; i++){
				dataParser.append((char)(test[i]));
			}
			String [] points = dataParser.toString().replaceAll("\\[|\\]", "").split(";");
			for (String point : points) {
				String[] p = point.replaceAll("\\(|\\)", "").split(",");
				this.points.add(new Point(Double.parseDouble(p[0]), Double.parseDouble(p[1])));
			}
		}
	}
}
