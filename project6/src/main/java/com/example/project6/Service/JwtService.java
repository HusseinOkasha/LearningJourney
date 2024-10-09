package com.example.project6.Service;

import com.example.project6.security.CustomUserDetails;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;

import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.core.env.Environment;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.security.Key;
import java.util.Date;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;

@Service
public class JwtService {

    private final Environment env;

    @Autowired
    public JwtService(Environment env) {
        this.env = env;
    }

    public UUID extractAccountUuid(String token) {
        return UUID.fromString(extractClaim(token, Claims::getSubject));
    }

    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    public String generateToken(Map<String, Object> extraClaims, String email){
        return Jwts
                .builder()
                .claims(extraClaims)
                .subject(email)
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis() + 1000 * 60 * 24))
                .signWith(getSiginKey())
                .compact();

    }
    public boolean isTokenValid(String token, CustomUserDetails userDetails) {

        // checks that the token belongs to the given user && if the token is expired.
        final UUID accountUuid = extractAccountUuid(token);
        return (accountUuid.equals(userDetails.getAccountUuid())) && !isTokenExpired(token);
    }

    private boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    private Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }
    public Claims extractAllClaims(String token){
        SecretKey secretKey = (SecretKey) getSiginKey();
        return Jwts
                .parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    private Key getSiginKey(){
        // helper method that gets the secret key from the environment variables
        // returns a signature based on the provided secret.

        String secretKey = env.getProperty("SECRET_KEY");
        if(secretKey == null || secretKey.isEmpty()){
            throw new IllegalArgumentException("couldn't find JWT_SECRET in the environment variables");
        }
        return Keys
                .hmacShaKeyFor(
                        Decoders
                                .BASE64
                                .decode(secretKey)
                );
    }
}

