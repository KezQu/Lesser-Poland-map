using Microsoft.SqlServer.Server;
using System;
using System.Collections.Generic;
using System.Data.SqlClient;
using System.Data.SqlTypes;
using System.IO;
using System.IO.MemoryMappedFiles;
using System.Linq;
using System.Runtime.InteropServices;
using System.Security.Authentication.ExtendedProtection;
using System.Text;
using System.Text.RegularExpressions;
using System.Threading.Tasks;

[Serializable]
[SqlUserDefinedTypeAttribute(Format.UserDefined, IsByteOrdered = true, MaxByteSize = -1, ValidationMethodName = "Validate")]
public struct Polygon : INullable, IBinarySerialize {
	private MultiLine _closedCurve;
	private bool _Null;

	public bool IsNull {
		get {
			return this._Null;
		}
	}
	[return: SqlFacet(MaxSize = -1)]
	public override string ToString() {
		if(_Null) {
			return "NULL";
		}
		return _closedCurve.ToString();
	}
	public Polygon(MultiLine ml) {
		_Null = false;
		_closedCurve = ml;
	}
	[SqlMethod(OnNullCall = false)]
	public static Polygon Parse(SqlString sql) {
		Regex sqlVerifier = new Regex("\\[((\\(-?\\d+(\\.\\d*)?,-?\\d+(\\.\\d*)?\\));)+(\\(-?\\d+(\\.\\d*)?,-?\\d+(\\.\\d*)?\\))\\]");
		if (!sqlVerifier.IsMatch(sql.Value)) {
			throw new ArgumentException("Incorrect input format should be [(x1,y1);(x2,y2)...]");
		}
		if (sql == null) {
			return Polygon.Null;
		}
		else {
			Polygon newPoly = new Polygon();
			newPoly._Null = false;
			newPoly._closedCurve = new MultiLine(new List<Point>());
			List<string> points = new List<string>(sql.Value.Trim(new char[] { '[', ']' }).Split(';'));
			if(points.First() != points.Last()) {
				throw new ArgumentException("Multiline is not closed");
			}

			for (int i = 0; i < points.Count(); i++) {
				newPoly._closedCurve.POINTS.Add(Point.Parse(points[i]));
			}
			newPoly.CalcSize();

			if (!newPoly.Validate()) {
				throw new ArgumentException("Incorrect input values");
			}
			return newPoly;
		}
	}
	public static Polygon Null {
		get {
			Polygon p = new Polygon();
			p._Null = true;
			p._closedCurve = MultiLine.Null;
			return p;
		}
	}
	public MultiLine POLYGON {
		get { return this._closedCurve; }
		set { this._closedCurve = value.Validate() ? throw new ArithmeticException("Provided value is incorrect") : value; }
	}
	public bool Validate() { 
		return _closedCurve.Validate() && _closedCurve.POINTS.Last() == _closedCurve.POINTS.First(); 
	}
	public void Read(BinaryReader r) {
		_closedCurve.Read(r);
		_Null = r.ReadBoolean();
	}

	public void Write(BinaryWriter w) {
		_closedCurve.Write(w);
		w.Write(_Null);
	}
    public void CalcSize() {
        _closedCurve.SIZE = ToString().Length + 64;
    }
}
