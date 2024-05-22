package pl.edu.agh.fis.bd2.backend;

import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pl.edu.agh.fis.bd2.spatialTypes.Area;
import pl.edu.agh.fis.bd2.spatialTypes.Point;

/**
 * Class providing endpoints to perform basic operations on the voivodeship
 */
@RestController
@RequestMapping("voivodeshipborder")
public class MapController extends DatabaseController{
	static int accuracy = 1;
	static protected Area VoivodeshipArea;
	static{
		int rescaledAreas = conn.sql("EXEC dbo.ScaleArea " + accuracy + ";").update();
		System.out.println("Rescaled " + rescaledAreas + " areas with accuracy: " + accuracy + " km.");
		VoivodeshipArea = new Area(conn, "województwo ma%opolskie", MapController.accuracy);
	}

	/**
	 * Basic endpoint providing borders of the Voivodeship
	 * @return returns list of points that create whole border of Lesser Poland voivodeship
	 */
	@GetMapping("/")
	protected ResponseEntity<String> VoivodeshipArea(){
		JSONObject body = new JSONObject(VoivodeshipArea);
		return ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON).body(body.toString());
	}

	/**
	 * Endpoint providing bounding box of the voivodeship to allow proper resizing
	 * @return Returns a pair of points that spans the bounding box
	 */
	@GetMapping("/boundingbox")
	protected ResponseEntity<String> VoivodeshipBoundingBox(){
//		Polygon boundingBox = (Polygon) conn.sql("""
//				SELECT dbo.GetBoundingBox(
//				(SELECT TOP(1) borderPolygon FROM Areas.AdministrativeArea
//				WHERE name LIKE '""" + VoivodeshipArea.getAreaName() + "'));")
//				.query((ResultSet rs) ->{
//					rs.next();
//					Polygon bBox = new Polygon();
//					try {
//						bBox.setPoints(rs.getBinaryStream(1));
//					} catch (IOException e) {
//						return ResponseEntity.internalServerError().contentType(MediaType.APPLICATION_JSON).body(e.getMessage());
//					}
//					System.out.println(bBox.getPoints().toString());
//					return bBox;
//				});
		Point[] resBody = new Point[]{ new Point(1.107,9.442), new Point(261.296,158.701)};
		return ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON).body(new JSONArray(resBody).toString());
//		return ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON).body(new JSONArray(boundingBox.getPoints()).toString());
	}
}
