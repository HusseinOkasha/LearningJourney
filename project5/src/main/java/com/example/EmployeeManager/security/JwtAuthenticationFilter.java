package com.example.EmployeeManager.security;

import com.example.EmployeeManager.service.EmployeeDetailsService;
import com.example.EmployeeManager.service.JwtService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final EmployeeDetailsService employeeDetailsService;

    @Autowired
    public JwtAuthenticationFilter(JwtService jwtService, EmployeeDetailsService employeeDetailsService) {
        this.jwtService = jwtService;
        this.employeeDetailsService = employeeDetailsService;
    }

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
                                    @NonNull HttpServletResponse response,
                                    @NonNull FilterChain filterChain)
            throws ServletException, IOException {


        final String authHeader = request.getHeader("Authorization");
        final String jwtToken;
        final String userEmail;

        // checks that the authentication header contains a bearer token.
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        // extract the jwt token.
        // 7 : is the length of the Bearer + the space after it
        jwtToken = authHeader.substring(7);

        //extract the user email from the jwt token.
        userEmail = jwtService.extractUsername(jwtToken);

        // check if the user is already authenticated.
        if (userEmail != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            UserDetails userDetails = this.employeeDetailsService.loadUserByUsername(userEmail);

            if (jwtService.isTokenValid(jwtToken, userDetails)) {
                UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                        userDetails,
                        null,
                        userDetails.getAuthorities()
                );
                authToken.setDetails(
                        new WebAuthenticationDetailsSource().buildDetails(request)
                );
                SecurityContextHolder.getContext().setAuthentication(authToken);
            }
        }
        filterChain.doFilter(request, response);
    }
}





