package pl.edu.agh.fis.bd2.backend;

import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pl.edu.agh.fis.bd2.DatabaseConfig;

import javax.sql.DataSource;

/**
 * Base backend class that provides connection to the MSSQL Server database
 */
@RestController
@RequestMapping
abstract public class DatabaseController {
	/**
	 * Source of connection properties to the database
	 */
	static protected final DataSource dataSource = DatabaseConfig.dataSource();
	/**
	 * Connection made to the MSSQL Server using JDBC driver using dataSource connection properties
	 */
	static protected final JdbcClient conn = JdbcClient.create(dataSource);
}
