using Microsoft.SqlServer.Server;
using System;
using System.Collections.Generic;
using System.Data.SqlTypes;
using System.IO;
using System.Linq;
using System.Text;
using System.Text.RegularExpressions;
using System.Threading.Tasks;

[Serializable]
[SqlUserDefinedTypeAttribute(Format.UserDefined, IsByteOrdered = true, MaxByteSize = -1, ValidationMethodName = "Validate")]
public struct MultiLine : INullable, IBinarySerialize {
	private Int32 _size;
	private List<Point> _points;
	private bool _Null;
	public bool IsNull {
		get {
			return this._Null;
		}
	}
    public MultiLine(List<Point> points) {
		_size = 0;
        _points = points;
		_Null = false;
        CalcSize();
	}
    public MultiLine(MultiLine ml) {
        _size = ml._size;
        _Null = ml._Null;
		_points = new List<Point>();
		for(int i = 0;i < ml.POINTS.Count; i++) {
			_points.Add(new Point(ml.POINTS[i]));
		}
    }
    [return: SqlFacet(MaxSize = -1)]
	public override string ToString() {
		if (_Null) {
			return "NULL";
		}
		string result = "[";
		for (int i = 0; i < _points.Count(); i++) {
			result += _points[i].ToString() + ";";
		}
		result = result.Remove(result.Length - 1, 1);
		result += "]";
		return result;
	}
	[SqlMethod(OnNullCall = false)]
	public static MultiLine Parse(SqlString sql) {
		Regex sqlVerifier = new Regex("\\[((\\(\\d+(\\.\\d*)?,\\d+(\\.\\d*)?\\));)+(\\(\\d+(\\.\\d*)?,\\d+(\\.\\d*)?\\))\\]");
        if (!sqlVerifier.IsMatch(sql.Value)) {
			throw new ArgumentException("Incorrect input format should be [(x1,y1);(x2,y2)...]");
		}
		if (sql == null) {
			return MultiLine.Null;
		}
		else {
            MultiLine newMultiLine = new MultiLine(new List<Point>());
			String[] points = sqlVerifier.Match(sql.Value).Value.Trim().Trim(new char[] { '[', ']' }).Split(';');

			for (int i = 0; i < points.Count(); i++) {
				newMultiLine._points.Add(Point.Parse(points[i]));
			}
			newMultiLine.CalcSize();

			if (!newMultiLine.Validate()) {
				throw new ArgumentException("Incorrect input values");
			}
			return newMultiLine;
		}
	}
	public static MultiLine Null {
		get {
            MultiLine newMultiLine = new MultiLine();
            newMultiLine._Null = true;
            newMultiLine._points = new List<Point>();
            return newMultiLine;
		}
	}
	public List<Point> POINTS {
		get { return this._points; }
		set { this._points = this.Validate() ? throw new ArithmeticException("Provided value is incorrect") : value; }
    }
	public Int32 SIZE {
        get { return this._size; }
        set { this._size = value; }
    }
    public bool Validate() {
		for (int i = 0; i < _points.Count(); i++) {
			if (!_points[i].Validate()) {
				return false;
			}
		}
		return true;
	}
    public void Read(BinaryReader r) {
		_size = r.ReadInt32();
		char[] pointChars;
		int end;
		pointChars = r.ReadChars(_size);
		end = Array.IndexOf(pointChars, '\0');
		if(end <= 0) {
			_Null = true;
			return;
		}
		//string pointsString = new string(pointChars, Array.IndexOf(pointChars, '['), end);
		string pointsString = new string(pointChars, 0, end);
		this._Null = r.ReadBoolean();
		this = Parse(pointsString);
	}

	public void Write(BinaryWriter w) {
		Int32 stringSize = _size;
		string PointValues = ToString();
		string paddedPointValues = PointValues.PadRight(stringSize, '\0');

		w.Write(stringSize);
		w.Write(paddedPointValues);
		w.Write(_Null);
	}
	public void CalcSize() {
		_size = ToString().Length + 64;
	}
}
