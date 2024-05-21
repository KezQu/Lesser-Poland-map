package pl.edu.agh.fis.bd2.backend;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * Class to help identify CSRF token inside a request body, along spring boot pipeline
 */
public class CsrfCookieFilter extends OncePerRequestFilter {
	/**
	 * Overrided method that allows us to create custem filter in Spring boot filter chain
	 * @param req Request sent by a client
	 * @param res Response that will presented to user
	 * @param chain Spring boot filter chain that allows us to attach our own filter
	 * @throws ServletException Exception thrown while respone or request are damaged / invalid
	 * @throws IOException Exception thrown if filtering cannot be performed
	 */
	@Override
	protected void  doFilterInternal(HttpServletRequest req, HttpServletResponse res, FilterChain chain) throws ServletException, IOException {
		CsrfToken token = (CsrfToken) req.getAttribute("_csrf");

		chain.doFilter(req, res);
	}
}
