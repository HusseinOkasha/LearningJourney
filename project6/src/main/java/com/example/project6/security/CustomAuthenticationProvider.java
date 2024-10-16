package com.example.project6.security;

import com.example.project6.Service.UserDetailsService;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class CustomAuthenticationProvider implements AuthenticationProvider {

    private final BCryptPasswordEncoder bCryptPasswordEncoder;
    private final UserDetailsService userDetailsService;

    public CustomAuthenticationProvider(BCryptPasswordEncoder bCryptPasswordEncoder, UserDetailsService userDetailsService) {
        this.bCryptPasswordEncoder = bCryptPasswordEncoder;
        this.userDetailsService = userDetailsService;
    }

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        String email = authentication.getName();
        String password = authentication.getCredentials().toString();

        CustomUserDetails customUserDetails =  userDetailsService.loadUserByUsername(email);

        return checkPassword(customUserDetails, password, bCryptPasswordEncoder);
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return UsernamePasswordAuthenticationToken.class.isAssignableFrom(authentication);
    }

    private Authentication checkPassword(CustomUserDetails customUserDetails, String rawPassword, PasswordEncoder encoder) {

        if(encoder.matches(rawPassword, customUserDetails.getPassword())){
            return new UsernamePasswordAuthenticationToken(
                    customUserDetails.getAccountUuid(),
                    customUserDetails.getPassword(),
                    customUserDetails.getAuthorities()
            );
        }
        else{
            throw new BadCredentialsException("Bad Credentials");
        }
    }
}
