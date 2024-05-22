USE MalopolskaMap;
GO
--------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
--------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
DECLARE @MalopolskaMap xml;
SET @MalopolskaMap = (SELECT * FROM OPENROWSET(
BULK '/malopolska/MalopolskaMap.osm', SINGLE_BLOB)
AS MalopolskaMap);

	
INSERT INTO Primitives.Point
SELECT node.value('@id', 'BIGINT') AS id,
CONVERT(dbo.Point, '(' + node.value('@lon', 'NVARCHAR(20)') + ',' + node.value('@lat', 'NVARCHAR(20)') + ')').ConvertFromCoordinates() AS point
FROM @MalopolskaMap.nodes('/osm/node') AS map(node);

PRINT 'Points loaded Successfully'
GO
--------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
--------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
INSERT INTO Primitives.Point VALUES (0, CONVERT(dbo.Point, '(0,0)'));
GO
--------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
--------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
DROP TABLE IF EXISTS TmpMultiLine;
	
DECLARE @MalopolskaMap xml;
SET @MalopolskaMap = (SELECT * FROM OPENROWSET(
BULK '/malopolska/MalopolskaMap.osm', SINGLE_BLOB)
AS MalopolskaMap);

SELECT 
map.ways.value('@id', 'BIGINT') AS multiline_id,
(SELECT (SELECT point.ToString() FROM Primitives.Point WHERE id = CONVERT(BIGINT, value)) + ';' FROM string_split(CONVERT(NVARCHAR(MAX), map.ways.query('
	for $ND in ./nd
		return string($ND/@ref)
')), ' ') FOR XML PATH('')) AS multiline_str
INTO TmpMultiLine
FROM
@MalopolskaMap.nodes('/osm/way') AS map(ways);

INSERT INTO Primitives.MultiLine
SELECT
multiline_id,
CONVERT(dbo.MultiLine, '[' + SUBSTRING(multiline_str, 1, LEN(multiline_str) - 1) + ']') 
FROM TmpMultiLine;

DROP TABLE IF EXISTS TmpMultiLine;
PRINT 'Multilines loaded Successfully'
GO
--------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
--------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
DROP TABLE IF EXISTS TmpPlace;
	
DECLARE @MalopolskaMap xml;
SET @MalopolskaMap = (SELECT * FROM OPENROWSET(
BULK '/malopolska/MalopolskaMap.osm', SINGLE_BLOB)
AS MalopolskaMap);

SELECT
map.nodes.value('@id', 'BIGINT') AS point_id,
REPLACE(REPLACE(CONVERT(NVARCHAR(MAX), map.nodes.query('
	for $Tag in ./tag
		return <kv>{data($Tag/@k)}={data($Tag/@v)}</kv>')), '<kv>',''), '</kv>', '&') AS tags
INTO TmpPlace
FROM @MalopolskaMap.nodes('/osm/node') AS map(nodes);

INSERT INTO Areas.Place
SELECT 
(SELECT TOP(1) SUBSTRING(value, CHARINDEX('=', value) + 1, LEN(value)) FROM STRING_SPLIT(tags, '&') WHERE value LIKE 'name=%' OR value LIKE 'name:pl=%') AS name, 
(SELECT SUBSTRING(value, CHARINDEX('=', value) + 1, LEN(value)) FROM STRING_SPLIT(tags, '&') WHERE value LIKE 'place=%') AS place, 
(SELECT CONVERT(BIGINT, SUBSTRING(value, CHARINDEX('=', value) + 1, LEN(value))) FROM STRING_SPLIT(tags, '&') WHERE value LIKE 'population=%') AS population, 
(SELECT SUBSTRING(value, CHARINDEX('=', value) + 1, LEN(value)) FROM STRING_SPLIT(tags, '&') WHERE value LIKE 'postal_code=%') AS postal_code, 
point_id
FROM TmpPlace WHERE (SELECT TOP(1) SUBSTRING(value, CHARINDEX('=', value) + 1, LEN(value)) FROM STRING_SPLIT(tags, '&') WHERE value LIKE 'name=%' OR value LIKE 'name:pl=%') IS NOT NULL;

DROP TABLE IF EXISTS TmpPlace;
PRINT 'Places loaded Successfully'
GO
--------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
--------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
INSERT INTO Areas.Place VALUES ('None', 'None', 0, '00-000', 0);
GO
--------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
--------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
DROP TABLE IF EXISTS TmpAdminArea;
DECLARE @MalopolskaMap xml;
SET @MalopolskaMap = (SELECT * FROM OPENROWSET(
BULK '/malopolska/MalopolskaMap.osm', SINGLE_BLOB)
AS MalopolskaMap);
	
SELECT
map.nodes.value('@id', 'BIGINT') AS area_id,
CONVERT(NVARCHAR(MAX), map.nodes.query('
	for $Tag in ./tag
		return if($Tag[@k="name"])
		then
			data($Tag/@v)
		else()')) AS name,
CONVERT(NVARCHAR(MAX), map.nodes.query('
	for $Tag in ./tag
		return if($Tag[@k="name:prefix"])
		then
			data($Tag/@v)
		else()')) AS name_prefix,
CONVERT(VARCHAR(20), map.nodes.query('
	for $Tag in ./tag
		return if($Tag[@k="population"])
		then
			data($Tag/@v)
		else()')) AS population,
CONVERT(VARCHAR(100), map.nodes.query('
	for $Mem in ./member
		return if($Mem[@role="admin_centre"])
		then
			data($Mem/@ref)
		else()')) AS admin_centre_id
INTO TmpAdminArea
FROM @MalopolskaMap.nodes('/osm/relation') AS map(nodes);

INSERT INTO Areas.AdministrativeArea
SELECT
area_id,
name AS n,
(CASE WHEN LEN(name_prefix) > 0 THEN name_prefix ELSE NULL END) AS np,
(CASE WHEN LEN(population) > 0 THEN CONVERT(BIGINT, population) ELSE NULL END) AS p,
NULL,
dbo.CheckAdminCentre(admin_centre_id) AS aci
FROM TmpAdminArea AA WHERE LEN(name) > 0;


DROP TABLE IF EXISTS TmpAdminArea;
PRINT 'Administrative areas loaded successfully'
GO
--------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
--------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
DROP TABLE IF EXISTS TmpBorderTable;
DECLARE @MalopolskaMap xml;
SET @MalopolskaMap = (SELECT * FROM OPENROWSET(
BULK '/malopolska/MalopolskaMap.osm', SINGLE_BLOB)
AS MalopolskaMap);
	
DECLARE @TmpBorder VARCHAR(MAX);
SET @TmpBorder = CONVERT(VARCHAR(MAX), @MalopolskaMap.query('
	for $rel in /osm/relation, $Mem in $rel/member
		return
		if($Mem[@type="way"])
		then
			concat(string($rel/@id), string(";"), string($Mem/@ref))
		else ()
		'));
SELECT 
CONVERT(BIGINT, SUBSTRING(value, 1, CHARINDEX(';', value) - 1)) AS area_id,
CONVERT(BIGINT, SUBSTRING(value, CHARINDEX(';', value) + 1, LEN(value))) AS border_id 
INTO TmpBorderTable
FROM STRING_SPLIT(@TmpBorder, ' ');
	
INSERT INTO Areas.Border
SELECT area_id, border_id FROM TmpBorderTable 
JOIN Areas.AdministrativeArea AA ON AA.id = area_id
JOIN Primitives.MultiLine ML ON ML.id = border_id
WHERE area_id IS NOT NULL AND border_id IS NOT NULL;

DROP TABLE IF EXISTS TmpBorderTable;
PRINT 'Borders loaded successfully'
GO
--------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
--------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
DROP TABLE IF EXISTS TmpSubAreaTable;
	
DECLARE @MalopolskaMap xml;
SET @MalopolskaMap = (SELECT * FROM OPENROWSET(
BULK '/malopolska/MalopolskaMap.osm', SINGLE_BLOB)
AS MalopolskaMap);
DECLARE @TmpSubAreas VARCHAR(MAX);
SET @TmpSubAreas = CONVERT(VARCHAR(MAX), @MalopolskaMap.query('
	for $rel in /osm/relation, $Mem in $rel/member
		return
		if($Mem[@role="subarea"])
		then
			concat(string($rel/@id), string(";"), string($Mem/@ref))
		else ()
		'));
SELECT
CONVERT(BIGINT, SUBSTRING(value, 1, CHARINDEX(';', value) - 1)) AS area_id,
CONVERT(BIGINT, SUBSTRING(value, CHARINDEX(';', value) + 1, LEN(value))) AS sub_area_id
INTO TmpSubAreaTable
FROM STRING_SPLIT(@TmpSubAreas, ' ');
	
INSERT INTO Areas.SubAreas
SELECT area_id, sub_area_id FROM TmpSubAreaTable 
JOIN Areas.AdministrativeArea AA1 ON AA1.id = area_id
JOIN Areas.AdministrativeArea AA2 ON AA2.id = sub_area_id
WHERE area_id IS NOT NULL AND sub_area_id IS NOT NULL;

DROP TABLE IF EXISTS TmpSubAreaTable;
PRINT 'Subareas loaded successfully'
GO
--------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
--------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
UPDATE Areas.AdministrativeArea SET borderPolygon = dbo.CreatePolyOutOfUnorderedMultilines(id);
INSERT INTO Areas.RescaledArea
SELECT id, borderPolygon FROM Areas.AdministrativeArea;
--------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
--------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
