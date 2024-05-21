package pl.edu.agh.fis.bd2.backend;

import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pl.edu.agh.fis.bd2.spatialTypes.Line;
import pl.edu.agh.fis.bd2.spatialTypes.Polygon;

import java.util.Map;

/**
 * Class providing endpoints to perform operations on the specified points
 */
@RestController
@RequestMapping("operation")
public class OperationController extends DatabaseController{
	/**
	 * Endpoint that allows to calculate area of selected polygon
	 * @param reqBody Request body that contains points that create valid polygon
	 * @return Returns calculated value of an area or error message if request was invalid
	 */
	@PostMapping("/area")
	public ResponseEntity<String> AreaPost(@RequestBody String reqBody){
		Polygon markedArea;
		Map<String, Object> calculatedArea;
		try{
			JSONArray bodyJSON = new JSONArray(reqBody);
			markedArea = new Polygon(bodyJSON);
			calculatedArea = conn.sql("SELECT dbo.Area(CONVERT(dbo.Polygon, '" + markedArea.toStringForQuery() + "')) AS area;").query().singleRow();
		}catch (RuntimeException e){
			return ResponseEntity.badRequest().contentType(MediaType.TEXT_PLAIN).body(e.getMessage());
		}
		return ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON).body(new JSONObject(calculatedArea).toString());
	}

	/**
	 * Endpoint that allows to calculate distance between two provided points
	 * @param reqBody Body of the request that contains two points p1 and p2
	 * @return Returns calculated distance between two points or error if request was invalid
	 */
	@PostMapping("/distance")
	public ResponseEntity<String> DistancePost(@RequestBody String reqBody){
		Line markedLine;
		Map<String, Object> calculatedDistance;
		try{
			JSONObject bodyJSON = new JSONObject(reqBody);
			markedLine = new Line(bodyJSON);
			calculatedDistance = conn.sql("SELECT dbo.Distance(CONVERT(dbo.Point, '" + markedLine.getP1().toStringForQuery() + "'), CONVERT(dbo.Point, '" + markedLine.getP2().toStringForQuery() + "')) AS distance;").query().singleRow();
		}catch (RuntimeException e){
			return ResponseEntity.badRequest().contentType(MediaType.TEXT_PLAIN).body(e.getMessage());
		}
		return ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON).body(new JSONObject(calculatedDistance).toString());
	}
}
