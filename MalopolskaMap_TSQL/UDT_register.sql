CREATE DATABASE MalopolskaMap;
GO
USE MalopolskaMap;
GO
CREATE SCHEMA Primitives;
GO
CREATE SCHEMA Areas;
GO
CREATE SCHEMA UserCred;
GO
EXEC sp_configure 'clr enabled', 1;
EXEC sp_configure 'show advanced options', 1;
RECONFIGURE;
EXEC sp_configure 'clr strict security', 0;
RECONFIGURE;
GO
DROP PROCEDURE IF EXISTS dbo.ScaleArea;
DROP FUNCTION IF EXISTS dbo.AreaWithRescaledPolygon;
DROP FUNCTION IF EXISTS dbo.ChangePolygonScale;
DROP FUNCTION IF EXISTS dbo.Concatenate;
DROP FUNCTION IF EXISTS dbo.ConcatenateInPlace;
DROP FUNCTION IF EXISTS dbo.IntersectLine;
DROP FUNCTION IF EXISTS dbo.IsInsideLine;
DROP FUNCTION IF EXISTS dbo.Direction;
DROP FUNCTION IF EXISTS dbo.Distance;
DROP FUNCTION IF EXISTS dbo.IsInside;
DROP FUNCTION IF EXISTS dbo.GetBoundingBox;
DROP FUNCTION IF EXISTS dbo.Area;
DROP FUNCTION IF EXISTS dbo.CreatePolyOutOfUnorderedMultilines;
DROP FUNCTION IF EXISTS GetAreasSurroundingPoint;
DROP FUNCTION IF EXISTS CheckIfPointIsInside;
DROP TYPE IF EXISTS Point;
DROP TYPE IF EXISTS Line;
DROP TYPE IF EXISTS MultiLine;
DROP TYPE IF EXISTS Polygon;
DROP ASSEMBLY IF EXISTS MalopolskaMap;
GO
CREATE ASSEMBLY MalopolskaMap FROM '/malopolska/MalopolskaMap.dll'
WITH PERMISSION_SET = SAFE;
GO
CREATE TYPE dbo.Point EXTERNAL NAME MalopolskaMap.Point;
CREATE TYPE dbo.Line EXTERNAL NAME MalopolskaMap.Line;
CREATE TYPE dbo.MultiLine EXTERNAL NAME MalopolskaMap.MultiLine;
CREATE TYPE dbo.Polygon EXTERNAL NAME MalopolskaMap.Polygon;
GO
CREATE FUNCTION dbo.Concatenate(@core dbo.MultiLine, @ml dbo.MultiLine) RETURNS dbo.MultiLine EXTERNAL NAME MalopolskaMap.Functions.Concatenate;
GO
CREATE FUNCTION dbo.ConcatenateInPlace(@core dbo.MultiLine, @ml dbo.MultiLine) RETURNS BIT EXTERNAL NAME MalopolskaMap.Functions.ConcatenateInPlace;
GO
--CREATE FUNCTION dbo.IntersectLine(@core dbo.Line, @l dbo.Line) RETURNS BIT EXTERNAL NAME MalopolskaMap.Functions.IntersectLine;
--GO
--CREATE FUNCTION dbo.IsInsideLine(@core dbo.Line, @p dbo.Point) RETURNS BIT EXTERNAL NAME MalopolskaMap.Functions.IsInsideLine;
--GO
--CREATE FUNCTION dbo.Direction(@core dbo.Line, @p dbo.Point) RETURNS INT EXTERNAL NAME MalopolskaMap.Functions.Direction;
GO
CREATE FUNCTION dbo.Distance(@core dbo.Point, @p dbo.Point) RETURNS FLOAT EXTERNAL NAME MalopolskaMap.Functions.Distance;
GO
CREATE FUNCTION dbo.IsInside(@core dbo.Polygon, @p dbo.Point) RETURNS BIT EXTERNAL NAME MalopolskaMap.Functions.IsInside;
GO
CREATE FUNCTION dbo.GetBoundingBox(@core dbo.Polygon) RETURNS dbo.Polygon EXTERNAL NAME MalopolskaMap.Functions.GetBoundingBox;
GO
CREATE FUNCTION dbo.Area(@core dbo.Polygon) RETURNS FLOAT EXTERNAL NAME MalopolskaMap.Functions.Area;
GO
CREATE FUNCTION dbo.CreatePolyOutOfUnorderedMultilines(@area_id BIGINT) RETURNS dbo.Polygon EXTERNAL NAME MalopolskaMap.Functions.CreatePolyOutOfUnorderedMultilines;
GO
CREATE FUNCTION dbo.ChangePolygonScale(@poly dbo.Polygon, @accuracy INT) RETURNS dbo.Polygon EXTERNAL NAME MalopolskaMap.Functions.ChangePolygonScale;
