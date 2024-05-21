using Microsoft.SqlServer.Server;
using System;
using System.Collections.Generic;
using System.Data.SqlTypes;
using System.Linq;
using System.Text;
using System.Text.RegularExpressions;
using System.Threading.Tasks;

[Serializable]
[SqlUserDefinedTypeAttribute(Format.Native, IsByteOrdered = true, ValidationMethodName = "Validate")]
public struct Line : INullable{
	private Point _p1;
	private Point _p2;
	private bool _Null;

	public bool IsNull {
		get {
			return this._Null;
		}
	}
	public Line(Point p1, Point p2) {
		if(p1.IsNull || p2.IsNull) {
			_p1 = new Point();
			_p2 = new Point();
			_Null = true;
		}
		else {
			_p1 = p1;
			_p2 = p2;
			_Null = false;
		}
	}
	public override string ToString() {
		if(_Null) {
			return "NULL";
		}
		return "[" + _p1 + ";" + _p2 + "]";
	}
	[SqlMethod(OnNullCall = false)]
	public static Line Parse(SqlString sql) {
		Regex sqlVerifier = new Regex("\\[(\\(-?\\d+(\\.\\d*)?,-?\\d+(\\.\\d*)?\\));(\\(-?\\d+(\\.\\d*)?,-?\\d+(\\.\\d*)?\\))\\]");
		if (!sqlVerifier.IsMatch(sql.Value)) {
			throw new ArgumentException("Incorrect input format should be [(x1,y1);(x2,y2)]");
		}
		if (sql == null) {
			return Line.Null;
		}
		else {
			Line newLine = new Line();
			newLine._Null = false;
			String[] points = sql.Value.Trim(new char[] { '[', ']' }).Split(';');
			
			newLine._p1 = Point.Parse(points[0]);
			newLine._p2 = Point.Parse(points[1]);
			if (!newLine.Validate()) {
				throw new ArgumentException("Incorrect input values");
			}
			return newLine;
		}
	}
	public static Line Null {
		get {
			Line l = new Line();
			l._Null = true;
			return l;
		}
	}
	public Point P1 {
		get { return this._p1; }
		set { this._p1 = value.Validate() ? throw new ArithmeticException("Provided value is incorrect") : value; }
	}
	public Point P2 {
		get { return this._p2; }
		set { this._p2 = value.Validate() ? throw new ArithmeticException("Provided value is incorrect") : value; }
	}
	public bool Validate() { return (!this._p1.Validate() || !this._p2.Validate()) ? false : true; }
}
