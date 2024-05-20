package pl.edu.agh.fis.bd2.spatialTypes;

import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.jdbc.core.simple.JdbcClient;

import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class Area implements Serializable {
	private String areaName;
	private String areaNamePrefix;
	private int population;
	private Polygon borderPolygon = new Polygon();
	private Point center = new Point(0,0);

	public Area(){}
	public Area(String name, String namePrefix, int population, Polygon borderPoly, Point center){
		this.areaName = name;
		this.areaNamePrefix = namePrefix;
		this.population = population;
		this.borderPolygon = borderPoly;
		this.center = center;
	}
	public Area(JdbcClient conn, String AreaName, int accuracy) {
		Area a = conn.sql("""
				SELECT * FROM dbo.AreaWithRescaledPolygon() WHERE name LIKE '"""
				+ AreaName + "';").query(Area.GetExtractor()).getFirst();
		this.areaName = AreaName;
		this.areaNamePrefix = a.areaNamePrefix;
		this.population = a.population;
		this.borderPolygon = a.borderPolygon;
		this.center = a.center;
	}
	public Point CalculateCenter(){
		double cx = 0,cy = 0;
		for (int i = 0; i < borderPolygon.getPoints().size();){
				cx += borderPolygon.getPoints().get(i).getX();
				cy += borderPolygon.getPoints().get(i).getY();
				i++;
			}
		return new Point(cx / borderPolygon.getPoints().size(), cy / borderPolygon.getPoints().size());
	}
	public static ResultSetExtractor<List<Area>> GetExtractor(){
		return (ResultSet rs) ->{
			List<Area> areaList = new ArrayList<>();
			while(rs.next()){
				areaList.add(Area.AreaParser(rs));
			}
			return areaList;
		};
	}
	private static Area AreaParser(ResultSet rs){
		try{
			Area area = new Area();
			area.areaName = rs.getNString(2);
			area.areaNamePrefix = rs.getNString(3);
			area.population = rs.getInt(4);
			////////////////////////////////////////////////////////////
			area.borderPolygon.setPoints(rs.getBinaryStream(5));
			return area;
		}catch (IOException | SQLException e){
			throw new RuntimeException("Unable to create map of malopolska " + e.getMessage() + " " + e.getCause());
		}
	}

	public void setAreaName(String name) {
		this.areaName = name;
	}

	public void setAreaNamePrefix(String areaNamePrefix) {
		this.areaNamePrefix = areaNamePrefix;
	}

	public void setPopulation(int population) {
		this.population = population;
	}

	public void setBorderPolygon(Polygon borderPolygon) {
		this.borderPolygon = borderPolygon;
	}

	public void setCenter(Point center) {
		this.center = center;
	}

	public String getAreaName() {
		return areaName;
	}

	public String getAreaNamePrefix() {
		return areaNamePrefix;
	}

	public int getPopulation() {
		return population;
	}

	public Polygon getBorderPolygon() {
		return borderPolygon;
	}

	public Point getCenter() {
		return center;
	}
}
