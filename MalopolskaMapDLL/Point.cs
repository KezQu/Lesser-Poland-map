
using Microsoft.SqlServer.Server;
using System;
using System.CodeDom;
using System.Data.SqlTypes;
using System.Globalization;
using System.Text.RegularExpressions;

[Serializable]
[SqlUserDefinedTypeAttribute(Format.Native, IsByteOrdered = true, ValidationMethodName = "Validate")]
public struct Point : INullable {
	private bool _Null;
	private Double _x;
	private Double _y;
	public bool IsNull {
		get { return _Null; }
	}
	public Point(Double x, Double y) {
		_x = x;
		_y = y;
		_Null = false;
	}
    public Point(Point p) {
        _x = p.X;
        _y = p.Y;
        _Null = p._Null;
    }
    public override string ToString() {
		if (_Null) {
			return "NULL";
		}
		return "(" + _x + "," + _y + ")";
	}
	[SqlMethod(OnNullCall = false)]
	public static Point Parse(SqlString sql) {
		Regex sqlVerifier = new Regex("(\\(\\d+(\\.\\d*)?,\\d+(\\.\\d*)?\\))");
		if (!sqlVerifier.IsMatch(sql.Value)) {
			throw new ArgumentException("Incorrect input format should be (lon,lat) or (x,y)");
		}
		if (sql == null) {
			return Point.Null;
		}
		else {
			Point newP = new Point();
			newP._Null = false;
			string[] coords = sqlVerifier.Match(sql.Value).Value.Trim(new char[] { '(', ')' }).Split(',');
			newP._x = Double.Parse(coords[0], CultureInfo.InvariantCulture);
			newP._y = Double.Parse(coords[1], CultureInfo.InvariantCulture);
			if (!newP.Validate()) {
				throw new ArgumentException("Incorrect input values");
			}
			return newP;
		}
	}
	public static Point Null {
		get {
			Point p = new Point();
			p._Null = true;
			return p;
		}
	}
	public Double X {
		get { return this._x; }
		set { this._x = value < 0 ? throw new ArithmeticException("Provided value is incorrect") : value; }
	}
	public Double Y {
		get { return this._y; }
		set { this._y = value < 0 ? throw new ArithmeticException("Provided value is incorrect") : value; }
	}
	public bool Validate() { return (this._x < 0 || this._y < 0) ? false : true; }

	[SqlMethod(OnNullCall = false)]
	public Point ConvertFromCoordinates() {
		double minLon = 19.0731649;
		double minLat = 49.0935552;
		int R = 6371;
		_x = Math.Truncate(R * Math.Tan((_x - minLon) * Math.PI / 180) * 1e+3) * 1e-3;
		_y = Math.Truncate(R * Math.Tan((_y - minLat) * Math.PI / 180) * 1e+3) * 1e-3;
		return this;
	}
	public static bool operator ==(Point p1, Point p2) {
		if(p1.IsNull || p2.IsNull) return false;
		if(p1.X == p2.X && p1.Y == p2.Y) return true;
		return false;
	}
	public static bool operator !=(Point p1, Point p2) {
		if(p1.IsNull || p2.IsNull) return false;
		return !(p1 == p2);
	}
}