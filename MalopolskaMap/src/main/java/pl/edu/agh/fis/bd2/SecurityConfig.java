package pl.edu.agh.fis.bd2;

import jakarta.servlet.DispatcherType;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.logout.HeaderWriterLogoutHandler;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;
import org.springframework.security.web.csrf.*;
import org.springframework.security.web.header.writers.ClearSiteDataHeaderWriter;
import pl.edu.agh.fis.bd2.backend.CsrfCookieFilter;
import pl.edu.agh.fis.bd2.frontend.SpaCsrfHandler;

import javax.sql.DataSource;
import java.util.ArrayList;
import java.util.List;

@Configuration
@EnableWebSecurity
public class SecurityConfig {
	public static final InMemoryUserDetailsManager inMemoryUserDetailsManager = LoadUsersFromDBtoMemory();
	@Bean
	public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
		http
				.csrf((csrf) -> csrf
//								.disable()
						.csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse())
						.csrfTokenRequestHandler(new SpaCsrfHandler())
				)
				.addFilterAfter(new CsrfCookieFilter(), BasicAuthenticationFilter.class)
				.authorizeHttpRequests((authorize) -> authorize
						.requestMatchers("/css/**").permitAll()
						.requestMatchers("/js/**").permitAll()
						.requestMatchers("/voivodeshipborder/*").permitAll()
						.requestMatchers("/operation/*").permitAll()
						.requestMatchers("/register").permitAll()
						.dispatcherTypeMatchers(DispatcherType.FORWARD, DispatcherType.ERROR).permitAll()
						.anyRequest().authenticated()
				)
				.formLogin((login) -> login
						.loginPage("/login").permitAll()
						.failureUrl("/login")
						.defaultSuccessUrl("/", true)
				)
				.logout((logout) -> logout.permitAll()
						.logoutUrl("/logout")
						.logoutSuccessUrl("/login")
						.addLogoutHandler(new HeaderWriterLogoutHandler(new ClearSiteDataHeaderWriter(ClearSiteDataHeaderWriter.Directive.ALL)))
				)
		;
		return http.build();
	}
	@Bean
	public UserDetailsService userDetailsService() {
		return inMemoryUserDetailsManager;
	}

	private static InMemoryUserDetailsManager LoadUsersFromDBtoMemory(){
		final DataSource dataSource = DatabaseConfig.dataSource();
		JdbcClient conn = JdbcClient.create(dataSource);
		var users = conn.sql("""
				SELECT * FROM dbo.GetAllUsersFromDB();""").query(UserData.GetExtractor());
		List<UserDetails> userDetailsList = new ArrayList<>();
		for(var user : users){
			userDetailsList.add(User.withDefaultPasswordEncoder()
					.username(user.getEmail())
					.password(user.getPassword())
					.roles(user.getRoles().split(","))
					.build());
		}
		return new InMemoryUserDetailsManager(userDetailsList);
	}
}
