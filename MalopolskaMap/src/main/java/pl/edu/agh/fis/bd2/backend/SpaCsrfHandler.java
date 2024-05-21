package pl.edu.agh.fis.bd2.backend;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.security.web.csrf.CsrfTokenRequestAttributeHandler;
import org.springframework.security.web.csrf.XorCsrfTokenRequestAttributeHandler;
import org.springframework.util.StringUtils;

import java.util.function.Supplier;

/**
 * Class exposing CSRF token to be available to SPA functionality
 */
public class SpaCsrfHandler extends CsrfTokenRequestAttributeHandler {
	private final XorCsrfTokenRequestAttributeHandler tokenHandler = new XorCsrfTokenRequestAttributeHandler();

	/**
	 * Overrided method that allows to process CSRF token with specified handler
	 * @param req Client request
	 * @param res Server response
	 * @param token CSRF token to be handled
	 */
	@Override
	public void handle(HttpServletRequest req, HttpServletResponse res, Supplier<CsrfToken> token){
		this.tokenHandler.handle(req, res, token);
	}

	/**
	 * Overrided method that allows server to Get access to CSRF token hidden in either header or body
	 * @param req Client request containing CSRF token
	 * @param token CRSF token, which value need to be processed
	 * @return CSRF token value
	 */
	@Override
	public String resolveCsrfTokenValue(HttpServletRequest req, CsrfToken token){
		if(StringUtils.hasText(req.getHeader(token.getHeaderName()))){
			return super.resolveCsrfTokenValue(req, token);
		}
		return this.tokenHandler.resolveCsrfTokenValue(req, token);
	}
}