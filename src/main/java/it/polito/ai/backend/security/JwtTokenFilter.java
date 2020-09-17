package it.polito.ai.backend.security;
import io.jsonwebtoken.ExpiredJwtException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.GenericFilterBean;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class JwtTokenFilter extends OncePerRequestFilter {

    private  JwtTokenProvider jwtTokenProvider;
    public JwtTokenFilter(JwtTokenProvider jwtTokenProvider){
        this.jwtTokenProvider=jwtTokenProvider;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {

        try {
            // JWT Token is in the form "Bearer token". Remove Bearer word and
            // get only the Token
            String jwtToken = jwtTokenProvider.resolveToken(request);

            if (StringUtils.hasText(jwtToken) && jwtTokenProvider.validateToken(jwtToken)) {
                Authentication authentication = jwtTokenProvider.getAuthentication(jwtToken);
                SecurityContextHolder.getContext().setAuthentication(authentication);
            }
            chain.doFilter(request, response);

        } catch (ExpiredJwtException ex) {
		 String isRefreshToken = request.getHeader("isRefreshToken");
            String requestURL = request.getRequestURL().toString();
            // allow for Refresh Token creation if following conditions are true.
            if (isRefreshToken != null && isRefreshToken.equals("true") && requestURL.contains("refreshtoken")) {
                allowForRefreshToken(ex, request);
            } else
                request.setAttribute("exception", ex);

        } catch (InvalidJwtAuthenticationException e){
            HttpServletResponse httpServletResponse= response;
            httpServletResponse.sendError(HttpServletResponse.SC_UNAUTHORIZED,"Please log-in again");;
        }

    }

    private void allowForRefreshToken(ExpiredJwtException ex, HttpServletRequest request) {
        String jwtToken = jwtTokenProvider.resolveToken(request);

        Authentication authentication = jwtTokenProvider.getAuthentication(jwtToken);
        SecurityContextHolder.getContext().setAuthentication(authentication);

        request.setAttribute("claims", ex.getClaims());
    }





}
