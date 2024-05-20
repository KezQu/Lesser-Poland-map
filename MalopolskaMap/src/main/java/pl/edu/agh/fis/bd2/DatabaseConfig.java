package pl.edu.agh.fis.bd2;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jdbc.repository.config.AbstractJdbcConfiguration;
import org.springframework.data.jdbc.repository.config.EnableJdbcRepositories;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

import javax.sql.DataSource;

@Configuration
@EnableJdbcRepositories
public class DatabaseConfig extends AbstractJdbcConfiguration {
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
