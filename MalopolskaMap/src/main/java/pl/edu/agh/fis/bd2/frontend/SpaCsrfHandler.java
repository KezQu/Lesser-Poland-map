package pl.edu.agh.fis.bd2.frontend;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.security.web.csrf.CsrfTokenRequestAttributeHandler;
import org.springframework.security.web.csrf.XorCsrfTokenRequestAttributeHandler;
import org.springframework.util.StringUtils;

import java.util.function.Supplier;

public class SpaCsrfHandler extends CsrfTokenRequestAttributeHandler {
	private final XorCsrfTokenRequestAttributeHandler tokenHandler = new XorCsrfTokenRequestAttributeHandler();
	@Override
	public void handle(HttpServletRequest req, HttpServletResponse res, Supplier<CsrfToken> token){
		this.tokenHandler.handle(req, res, token);
	}
	@Override
	public String resolveCsrfTokenValue(HttpServletRequest req, CsrfToken token){
		if(StringUtils.hasText(req.getHeader(token.getHeaderName()))){
			return super.resolveCsrfTokenValue(req, token);
		}
		return this.tokenHandler.resolveCsrfTokenValue(req, token);
	}
}