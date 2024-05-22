package pl.edu.agh.fis.bd2.backend;

import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pl.edu.agh.fis.bd2.spatialTypes.Area;
import pl.edu.agh.fis.bd2.spatialTypes.Line;
import pl.edu.agh.fis.bd2.spatialTypes.Point;
import pl.edu.agh.fis.bd2.spatialTypes.Polygon;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Vector;

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
	/**
	 * Endpoint that allows user to check is selected point is inside areas contained within database
	 * @param reqBody Request body containing coordinates of a point to be checked
	 * @return Returns borders of areas that include requested point or error message if request was invalid
	 */
	@PostMapping("/isinside")
	protected ResponseEntity<String> IsInsideArea(@RequestBody String reqBody){
		Vector<Area> AreasContainingPoint = new Vector<>();
		try{
			JSONObject bodyJSON = new JSONObject(reqBody);
			Point point = new Point(bodyJSON);

			var ids = conn.sql("""
					SELECT id FROM Areas.AdministrativeArea ORDER BY id;""").query().listOfRows();
			int granularity = 30;
			List<Thread> threadPool = new ArrayList<>();
			for(int i = 0; i < (ids.size() / granularity + 1); i++){
				int finalI = i;
				threadPool.add(new Thread(()->{
					Long bottom = (Long)ids.get(finalI * granularity).get("id");
					Long top = (Long)ids.get((finalI + 1) * granularity > ids.size() ? (ids.size() -1): ((finalI + 1) * granularity)).get("id");
					List<Area> foundAreas = conn.sql("""
					SELECT * FROM dbo.CheckIfPointIsInside(CONVERT(dbo.Point,'"""
							+ point.toStringForQuery() + "')," + bottom + ", " + top + " );").query(Area.GetExtractor());
					AreasContainingPoint.addAll(foundAreas);
				}));
				threadPool.getLast().start();
			}
			try{
				for(var thread : threadPool) {
					thread.join();
				}
			} catch (InterruptedException ignored){}
		}catch (RuntimeException e){
			return ResponseEntity.badRequest().contentType(MediaType.TEXT_PLAIN).body(e.getMessage());
		}
		JSONArray resBody = new JSONArray(AreasContainingPoint);
		return ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON).body(resBody.toString());
	}
}
