﻿using Microsoft.SqlServer.Server;
using System;
using System.Collections.Generic;
using System.Data.SqlClient;
using System.Linq;
using System.Reflection;
using System.Text;
using System.Threading.Tasks;

public class Functions {
	/*----------------------------------------------------------------------------------------------------------*/
	[SqlFunction(DataAccess = DataAccessKind.Read)]
	public static MultiLine Concatenate(MultiLine core, MultiLine ml) {
		MultiLine newML = new MultiLine(core.POINTS);
		if (newML.POINTS.Last() == ml.POINTS.First()) {
			newML.POINTS.AddRange(ml.POINTS.GetRange(1, ml.POINTS.Count - 1));
		}
		else if (newML.POINTS.Last() == ml.POINTS.Last()) {
			var tmpML = ml.POINTS;
			tmpML.Reverse();
			newML.POINTS.AddRange(tmpML.GetRange(1, tmpML.Count - 1));
			newML.CalcSize();
		}
		else {
			throw new ArgumentException("Multilines doesn't have connecting point");
		}
		return newML;
	}
	[SqlProcedure()]
	public static bool ConcatenateInPlace(MultiLine core, MultiLine ml) {
		if (core.POINTS.Last() == ml.POINTS.First()) {
			core.POINTS.AddRange(ml.POINTS.GetRange(1, ml.POINTS.Count - 1));
		}
		else if (core.POINTS.Last() == ml.POINTS.Last()) {
			var tmpML = ml.POINTS;
			tmpML.Reverse();
			core.POINTS.AddRange(tmpML.GetRange(1, tmpML.Count - 1));
			core.CalcSize();
		}
		else {
			throw new ArgumentException("Multilines doesn't have connecting point");
		}
		return true;
	}
	/*----------------------------------------------------------------------------------------------------------*/
	[SqlFunction(DataAccess = DataAccessKind.Read)]
	public static Double Distance(Point core, Point point) {
		Double squaredDiffx = Math.Pow(core.X - point.X, 2);
		Double squaredDiffy = Math.Pow(core.Y - point.Y, 2);
		return Math.Sqrt(squaredDiffx + squaredDiffy);
	}
	/*----------------------------------------------------------------------------------------------------------*/
	[SqlFunction(DataAccess = DataAccessKind.Read)]
	public static bool IsInside(Polygon core, Point p) {
		double alpha = 0;

		var points = core.POLYGON.POINTS;
		Point p1, p0;
		for (int i = 1; i < points.Count; i++) {
            p0 = new Point(points[i-1].X - p.X, points[i-1].Y - p.Y);
			p1 = new Point(points[i].X - p.X, points[i].Y - p.Y);
			
			double beta = Math.Atan2(p0.Y, p0.X);
			double gamma = Math.Atan2(p1.Y, p1.X);
			double delta = (gamma - beta);
            if (delta > Math.PI) {
				alpha += delta - 2 * Math.PI;
			}
			else if (delta < - Math.PI) {
                alpha += delta + 2 * Math.PI;
            }
			else {
				alpha += delta;
            }
            beta = gamma;
        }

		if (Math.Abs(Math.Abs(alpha) - Math.PI * 2) < 1e-5)
			return true;
		else
			return false;
	}
	[SqlFunction(DataAccess = DataAccessKind.Read)]
	public static Polygon GetBoundingBox(Polygon core) {
		Polygon p = new Polygon(new MultiLine(new List<Point>()));
		SortedSet<Point> Xsort = new SortedSet<Point>(Comparer<Point>.Create((p1, p2) => p1.X.CompareTo(p2.X)));
		SortedSet<Point> Ysort = new SortedSet<Point>(Comparer<Point>.Create((p1, p2) => p1.Y.CompareTo(p2.Y)));
		for(int i = 0; i < core.POLYGON.POINTS.Count; i++) {
			Xsort.Add(core.POLYGON.POINTS[i]);
			Ysort.Add(core.POLYGON.POINTS[i]);
		}
		double xSmall = Xsort.First().X;
		double xBig = Xsort.Last().X;
		double ySmall = Ysort.First().Y;
		double yBig = Ysort.Last().Y;
		p.POLYGON.POINTS.Add(new Point(xSmall, ySmall));
		p.POLYGON.POINTS.Add(new Point(xSmall, yBig));
		p.POLYGON.POINTS.Add(new Point(xBig, yBig));
		p.POLYGON.POINTS.Add(new Point(xBig, ySmall));
		p.POLYGON.POINTS.Add(new Point(xSmall, ySmall));
		p.CalcSize();
		return p;
	}
	[SqlFunction(DataAccess = DataAccessKind.Read)]
	public static double Area(Polygon core) {
		double area = 0;
		for (int i = 0; i < core.POLYGON.POINTS.Count - 1; i++) {
			area += (core.POLYGON.POINTS[i].X * core.POLYGON.POINTS[i + 1].Y) - (core.POLYGON.POINTS[i].Y * core.POLYGON.POINTS[i + 1].X);
		}
		return Math.Abs(area / 2);
	}
	[SqlFunction(DataAccess = DataAccessKind.Read)]
	public static Polygon CreatePolyOutOfUnorderedMultilines(Int64 area_id) {
		List<MultiLine> lines = new List<MultiLine>();

		using (SqlConnection conn = new SqlConnection("context connection=true")) {
			conn.Open();
			using (SqlCommand selectMultilines = new SqlCommand(
				"SELECT multiline.ToString() " +
				"FROM Areas.Border B " +
				"JOIN Primitives.MultiLine ML ON ML.id = B.multiline_id " +
				"WHERE B.area_id = " + area_id + ";", conn)) {
				using (SqlDataReader MLReader = selectMultilines.ExecuteReader()) {
					while (MLReader.Read()) {
						lines.Add(MultiLine.Parse(MLReader.GetString(0)));
					}
				}
			}
		}
		if (lines.Count == 0) {
			return Polygon.Null;
		}
		Polygon poly = new Polygon(lines[0]);

		lines.Remove(lines[0]);

		while (lines.Count > 0) {
			bool found = false;
			for (int i = 0; i < lines.Count && !found; i++) {
				var currPoints = lines.ElementAt(i);
				if (currPoints.POINTS.First() == poly.POLYGON.POINTS.Last()) {
					found = true;
				}
				else if (currPoints.POINTS.Last() == poly.POLYGON.POINTS.Last()) {
					currPoints.POINTS.Reverse();
					found = true;
				}
				if (found) {
					ConcatenateInPlace(poly.POLYGON, currPoints);
					lines.Remove(currPoints);
				}
			}
			if (!found) {
				break;
			}
		}

		if (poly.POLYGON.POINTS.First() != poly.POLYGON.POINTS.Last()) {
			return Polygon.Null;
		}
		poly.CalcSize();
		return poly;
	}
	[SqlFunction(DataAccess = DataAccessKind.Read)]
	public static Polygon ChangePolygonScale(Polygon core, int accuracy) {
		if(accuracy == 0) {
			return new Polygon(new MultiLine(core.POLYGON));
		}
		if(core.IsNull) 
			return Polygon.Null;
		List<Point> rescaledMap = new List<Point>();

		rescaledMap.Add(core.POLYGON.POINTS.First());
		for (int i = 0; i < core.POLYGON.POINTS.Count;) {
			while (i < core.POLYGON.POINTS.Count && Distance(rescaledMap.Last(), core.POLYGON.POINTS[i]) < accuracy) {
				i++;
			}
			rescaledMap.Add(i == core.POLYGON.POINTS.Count ? core.POLYGON.POINTS[i - 1] : core.POLYGON.POINTS[i]);
		}
		return new Polygon(new MultiLine(rescaledMap));
	}
	/*-------------------------------------------DEBUGGING--------------------------------------------*/
	/*-------------------------------------------DEBUGGING--------------------------------------------*/
	/*-------------------------------------------DEBUGGING--------------------------------------------*/
	//public static Polygon CreatePolyOutOfUnorderedMultilines(List<MultiLine> lines) {
	//	if (lines.Count == 0) {
	//		return Polygon.Null;
	//	}
	//	Polygon poly = new Polygon(lines[0]);

	//	lines.Remove(lines[0]);

	//	while (lines.Count > 0) {
	//		bool found = false;
	//		for (int i = 0; i < lines.Count && !found; i++) {
	//			var currPoints = lines.ElementAt(i);
	//			if (currPoints.POINTS.First() == poly.POLYGON.POINTS.Last()) {
	//				found = true;
	//			}
	//			else if (currPoints.POINTS.Last() == poly.POLYGON.POINTS.Last()) {
	//				currPoints.POINTS.Reverse();
	//				found = true;
	//			}
	//			if (found) {
	//				ConcatenateInPlace(poly.POLYGON, currPoints);
	//				lines.Remove(currPoints);
	//			}
	//		}
	//		if (!found) {
	//			break;
	//		}
	//	}

	//	if (poly.POLYGON.POINTS.First() != poly.POLYGON.POINTS.Last()) {
	//		//Console.WriteLine("Provided multilines does not create closed polygon");
	//		return Polygon.Null;
	//	}
	//	poly.CalcSize();
	//	return poly;
	//}
}