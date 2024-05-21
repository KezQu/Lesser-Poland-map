USE MalopolskaMap;
GO
--DROP TABLE IF EXISTS UserCred.UserRoles;
--DROP TABLE IF EXISTS UserCred.UserLoginData;
--DROP TABLE IF EXISTS UserCred.Roles;

DROP TABLE IF EXISTS Areas.Border;
DROP TABLE IF EXISTS Areas.SubAreas;
DROP TABLE IF EXISTS Areas.RescaledArea;
DROP TABLE IF EXISTS Areas.AdministrativeArea;
DROP TABLE IF EXISTS Areas.Place;
DROP TABLE IF EXISTS Primitives.MultiLine;
DROP TABLE IF EXISTS Primitives.Point;
GO
--------------------------------------------------------------------------------------------
CREATE TABLE Primitives.Point(
	id BIGINT,
	point dbo.Point NOT NULL,
	CONSTRAINT PK_Point PRIMARY KEY (id)
);
--------------------------------------------------------------------------------------------
CREATE TABLE Primitives.MultiLine(
	id BIGINT,
	multiline dbo.MultiLine NOT NULL,
	CONSTRAINT PK_MultiLine PRIMARY KEY (id)
);
--------------------------------------------------------------------------------------------
CREATE TABLE Areas.Place(
	id BIGINT IDENTITY(1,1),
	name NVARCHAR(120) NOT NULL,
	place_type NVARCHAR(30),
	population INT,
	postal_code VARCHAR(30),
	point_id BIGINT,
	CONSTRAINT UQ_NameLoc UNIQUE (name, point_id),
	CONSTRAINT PK_Place PRIMARY KEY (id),
	CONSTRAINT FK_Point FOREIGN KEY (point_id) REFERENCES Primitives.Point(id)
);
--------------------------------------------------------------------------------------------
CREATE TABLE Areas.AdministrativeArea(
	id BIGINT,
	name NVARCHAR(120) NOT NULL,
	name_prefix NVARCHAR(100),
	population INT,
	borderPolygon dbo.Polygon,
	admin_centre_id BIGINT,
	CONSTRAINT PK_AdminArea PRIMARY KEY (id),
	CONSTRAINT FK_AdminCentre FOREIGN KEY (admin_centre_id) REFERENCES Areas.Place(id)
);
--------------------------------------------------------------------------------------------
CREATE TABLE Areas.RescaledArea(
	id BIGINT,
	borderPolygon dbo.Polygon,
	CONSTRAINT FK_RescaledArea FOREIGN KEY (id) REFERENCES Areas.AdministrativeArea(id)
);
--------------------------------------------------------------------------------------------
CREATE TABLE Areas.SubAreas(
	main_area_id BIGINT,
	sub_area_id BIGINT,
	CONSTRAINT PK_SubArea PRIMARY KEY (main_area_id, sub_area_id),
	CONSTRAINT FK_MainArea FOREIGN KEY (main_area_id) REFERENCES Areas.AdministrativeArea(id),
	CONSTRAINT FK_SubArea FOREIGN KEY (sub_area_id) REFERENCES Areas.AdministrativeArea(id)
);
--------------------------------------------------------------------------------------------
CREATE TABLE Areas.Border(
	area_id BIGINT NOT NULL,
	multiline_id BIGINT NOT NULL,
	CONSTRAINT PK_Border PRIMARY KEY (area_id, multiline_id),
	CONSTRAINT FK_Area FOREIGN KEY (area_id) REFERENCES Areas.AdministrativeArea(id),
	CONSTRAINT FK_MultiLine FOREIGN KEY (multiline_id) REFERENCES Primitives.MultiLine(id)
);
--------------------------------------------------------------------------------------------
--CREATE TABLE UserCred.UserLoginData(
--	id BIGINT IDENTITY(1,1) NOT NULL,
--	email NVARCHAR(200) NOT NULL,
--	password NVARCHAR(200) NOT NULL,
--	CONSTRAINT PK_userID PRIMARY KEY (id),
--	CONSTRAINT UQ_email UNIQUE (email)
--);
----------------------------------------------------------------------------------------------
--CREATE TABLE UserCred.Roles(
--	id BIGINT IDENTITY(1,1) NOT NULL,
--	role NVARCHAR(100) NOT NULL,
--	CONSTRAINT PK_roleID PRIMARY KEY (id),
--	CONSTRAINT UQ_roleType UNIQUE (role)
--);
----------------------------------------------------------------------------------------------
--CREATE TABLE UserCred.UserRoles(
--	user_id BIGINT NOT NULL,
--	role_id BIGINT NOT NULL,
--	CONSTRAINT PK_userRole PRIMARY KEY(user_id, role_id),
--	CONSTRAINT FK_userID FOREIGN KEY(user_id) REFERENCES UserCred.UserLoginData(id),
--	CONSTRAINT FK_roleID FOREIGN KEY(role_id) REFERENCES UserCred.Roles(id),
--);
--------------------------------------------------------------------------------------------
