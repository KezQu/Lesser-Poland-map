//using System;
//using System.Collections.Generic;
//using System.Globalization;
//using System.IO;
//using System.Linq;
//using System.Text;
//using System.Threading.Tasks;

//class Project {
//	static void Main(string[] args) {
//		List<MultiLine> mls = new List<MultiLine>();
//		StreamReader fs = new StreamReader("D:\\Programming\\Java\\BazyDanych2\\Lesser-Poland-map\\MalopolskaMapDLL\\lines.txt");
//		while (!fs.EndOfStream) {
//			mls.Add(MultiLine.Parse(fs.ReadLine()));
//		}
//		var p = MultiLine.Parse("[(-10.0,10);(-10,5)]");
//		//Polygon p = Functions.CreatePolyOutOfUnorderedMultilines(mls);
//		//Console.WriteLine(Functions.IsInside(p, new Point(216, 103)));
//		//Console.WriteLine(Functions.IsInside(p, new Point(220, 110)));
//	}
//}
