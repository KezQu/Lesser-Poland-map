USE MalopolskaMap;
GO
EXEC dbo.CompareProvidedAndBuiltInAreaCalculation;
GO
DECLARE @beginPoint dbo.Point = CONVERT(dbo.Point, '(0,0)');
EXEC dbo.CompareProvidedAndBuiltInDistanceCalcualtion @beginPoint;
GO
DECLARE @searchPoint dbo.Point = CONVERT(dbo.Point, '(96.045,107.69)');
EXEC dbo.CompareProvidedAndBuiltInContainsPoint @searchPoint;