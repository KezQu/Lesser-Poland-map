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
import pl.edu.agh.fis.bd2.spatialTypes.Point;
import pl.edu.agh.fis.bd2.spatialTypes.Polygon;

@RestController
@RequestMapping("operation")
public class OperationController extends DatabaseController{
	@PostMapping("/area")
	public ResponseEntity<String> AreaPost(@RequestBody String reqBody){
		JSONArray bodyJSON = new JSONArray(reqBody);
		Polygon markedArea = new Polygon(bodyJSON);
		var calculatedArea = conn.sql("SELECT dbo.Area(CONVERT(dbo.Polygon, '" + markedArea.toStringForQuery() + "')) AS area;").query().singleRow();
		return ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON).body(new JSONObject(calculatedArea).toString());
	}
	@PostMapping("/distance")
	public ResponseEntity<String> DistancePost(@RequestBody String reqBody){
		JSONObject bodyJSON = new JSONObject(reqBody);
		Line markedLine = new Line(bodyJSON);
		var calculatedDistance = conn.sql("SELECT dbo.Distance(CONVERT(dbo.Point, '" + markedLine.getP1().toStringForQuery() + "'), CONVERT(dbo.Point, '" + markedLine.getP2().toStringForQuery() + "')) AS distance;").query().singleRow();
		return ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON).body(new JSONObject(calculatedDistance).toString());
	}
}
