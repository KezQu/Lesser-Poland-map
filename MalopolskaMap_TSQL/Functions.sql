USE MalopolskaMap;
GO
CREATE OR ALTER PROCEDURE dbo.CompareProvidedAndBuiltInAreaCalculation AS
BEGIN
	SELECT TOP(10)
	name, geometry::STGeomFromText(
	REPLACE(REPLACE(REPLACE(REPLACE(borderPolygon.ToString(), ',', ' '), ');(', ', '), '[', 'POLYGON('), ']', ')'),
	0).MakeValid().STArea() AS BuiltInArea, dbo.Area(borderPolygon) AS OwnArea
	FROM Areas.AdministrativeArea WHERE borderPolygon IS NOT NULL;
END
GO
CREATE OR ALTER PROCEDURE dbo.CompareProvidedAndBuiltInDistanceCalculation(@beginPoint dbo.Point) AS
BEGIN
	SELECT TOP(10)
	geometry::STGeomFromText(
	REPLACE('POINT' + @beginPoint.ToString(), ',', ' '),
	0).STDistance(
	geometry::STGeomFromText(
	REPLACE('POINT' + point.ToString(), ',', ' '),
	0))
	AS BuiltInDistance, dbo.Distance(@beginPoint, point) AS OwnDistance
	FROM Primitives.Point WHERE point IS NOT NULL;
END
GO
CREATE OR ALTER PROCEDURE dbo.CompareProvidedAndBuiltInContainsPoint(@searchPoint dbo.Point) AS
BEGIN
	SELECT TOP(5)
	name, geometry::STGeomFromText(
	REPLACE(REPLACE(REPLACE(REPLACE(borderPolygon.ToString(), ',', ' '), ');(', ', '), '[', 'POLYGON('), ']', ')'),
	0).MakeValid().STContains(geometry::STGeomFromText(
	REPLACE('POINT' + @searchPoint.ToString(), ',', ' '),
	0)) AS BuiltInSearch,
	dbo.IsInside(borderPolygon, @searchPoint) AS OwnSearch
	FROM Areas.AdministrativeArea WHERE borderPolygon IS NOT NULL;
END
GO
CREATE OR ALTER FUNCTION dbo.AreaWithRescaledPolygon() RETURNS TABLE AS
	RETURN
	(SELECT AA.id, AA.name, AA.name_prefix, AA.population, RA.borderPolygon, AA.admin_centre_id
	FROM Areas.AdministrativeArea AA
	JOIN Areas.RescaledArea RA ON Ra.id = AA.id);
GO
CREATE OR ALTER PROCEDURE dbo.ScaleArea(@accuracy INT) AS
	MERGE INTO Areas.RescaledArea RA
	USING Areas.AdministrativeArea AA
	ON AA.id = RA.id
	WHEN MATCHED THEN
		UPDATE SET
			RA.borderPolygon = dbo.ChangePolygonScale(AA.borderPolygon, @accuracy);
GO
CREATE OR ALTER FUNCTION dbo.CheckIfPointIsInside(@point dbo.Point, @bottom BIGINT, @top BIGINT) RETURNS TABLE AS
	RETURN 
	(SELECT *
	FROM dbo.AreaWithRescaledPolygon()
	WHERE (name NOT LIKE 'województwo %' AND borderPolygon IS NOT NULL AND dbo.IsInside(borderPolygon, @point) = 1) AND (id >= @bottom AND id < @top));
GO
CREATE OR ALTER FUNCTION dbo.CheckAdminCentre(@admin_id VARCHAR(100)) RETURNS BIGINT AS
BEGIN
	DECLARE @id BIGINT = NULL;
	IF LEN(@admin_id) > 0
		SELECT @id = id FROM Areas.Place WHERE point_id = CONVERT(BIGINT, @admin_id);
	IF @id IS NULL
		SELECT @id = id FROM Areas.Place WHERE name LIKE 'None';
	RETURN @id;
END
GO
CREATE OR ALTER FUNCTION dbo.GetAreasSurroundingPoint(@point dbo.Point) RETURNS TABLE AS
	RETURN (SELECT AA.id, AA.name, AA.name_prefix AS prefix, AA.population, AA.borderPolygon, P.name AS AdminCentreName, P.place_type AS type, P.population AS AdminCentrePopulation, P.postal_code
		FROM Areas.AdministrativeArea AA 
		JOIN Areas.Place P ON P.id = AA.admin_centre_id
		WHERE dbo.IsInside(AA.borderPolygon, @point) = 1);
GO
CREATE OR ALTER PROCEDURE dbo.Register(@email NVARCHAR(200), @password NVARCHAR(200), @roles NVARCHAR(MAX)) AS
BEGIN
	DECLARE @uid BIGINT;
	DECLARE @currRole NVARCHAR(100);
	IF (SELECT COUNT(*) FROM UserCred.UserLoginData WHERE email LIKE @email) = 0
	BEGIN
		INSERT INTO UserCred.UserLoginData VALUES (@email, @password);
		INSERT INTO UserCred.Roles
			SELECT REPLACE(value, ' ', '') FROM STRING_SPLIT(@roles, ',')
			EXCEPT
			SELECT role FROM UserCred.Roles;

		SELECT @uid = id FROM UserCred.UserLoginData WHERE email = @email;
		DECLARE roleCursor CURSOR LOCAL FOR SELECT REPLACE(value, ' ', '') FROM STRING_SPLIT(@roles, ',');
		OPEN roleCursor;
		FETCH NEXT FROM roleCursor INTO @currRole;
		WHILE @@FETCH_STATUS = 0
		BEGIN 
			INSERT INTO UserCred.UserRoles VALUES (@uid, (SELECT id FROM UserCred.Roles WHERE role = @currRole));
			FETCH NEXT FROM roleCursor INTO @currRole;
		END
		CLOSE roleCursor;
		DEALLOCATE roleCursor;
	END
	ELSE
		THROW 60000, 'User with specified email exists in the database', 1;
END
GO
CREATE OR ALTER FUNCTION dbo.Login(@email NVARCHAR(200), @password NVARCHAR(200)) RETURNS NVARCHAR(MAX) AS
BEGIN
	DECLARE @realPass NVARCHAR(200);
	SELECT @realPass = password FROM UserCred.UserLoginData WHERE email LIKE @email;
	IF @realPass = @password
	BEGIN
		RETURN (SELECT role FROM UserCred.Roles R
			JOIN UserCred.UserRoles UR ON UR.role_id = R.id
			JOIN UserCred.UserLoginData U ON U.id = UR.user_id 
			WHERE U.email = @email FOR XML PATH(''));
	END
	RETURN '';
END
GO
CREATE OR ALTER FUNCTION dbo.GetAllUsersFromDB() RETURNS TABLE AS
	RETURN (SELECT email, password, (SELECT STRING_AGG(role, ';') FROM UserCred.Roles R JOIN UserCred.UserRoles UR ON UR.role_id = R.id WHERE Ur.user_id = U.id) AS roles FROM UserCred.UserLoginData U);
