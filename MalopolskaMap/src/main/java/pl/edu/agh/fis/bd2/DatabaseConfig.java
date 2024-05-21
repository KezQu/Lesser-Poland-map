package pl.edu.agh.fis.bd2;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jdbc.repository.config.AbstractJdbcConfiguration;
import org.springframework.data.jdbc.repository.config.EnableJdbcRepositories;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

import javax.sql.DataSource;

/**
 * Class providing tools and properties to connect to MSSQL Server database
 */
@Configuration
@EnableJdbcRepositories
public class DatabaseConfig extends AbstractJdbcConfiguration {
	/**
	 * Method providing source of connection properties as well as JDBC driver manager
	 * @return Data source providing tools to create connections to the database
	 */
	@Bean
	public static DataSource dataSource(){
		DriverManagerDataSource dataSource = new DriverManagerDataSource();
		dataSource.setDriverClassName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
		dataSource.setUrl("jdbc:sqlserver://localhost:1433;encrypt=false");
		dataSource.setUsername("SA");
		dataSource.setPassword("Passw0rd");
		dataSource.setCatalog("MalopolskaMap");
		return dataSource;
	}
}
