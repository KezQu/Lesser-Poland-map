package pl.edu.agh.fis.bd2.backend;

import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pl.edu.agh.fis.bd2.DatabaseConfig;

import javax.sql.DataSource;

@RestController
@RequestMapping
public class DatabaseController {
	static protected final DataSource dataSource = DatabaseConfig.dataSource();
	static protected final JdbcClient conn = JdbcClient.create(dataSource);
}
