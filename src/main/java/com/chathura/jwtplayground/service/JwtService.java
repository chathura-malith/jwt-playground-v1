package com.chathura.jwtplayground.service;

import io.jsonwebtoken.Claims;

public interface JwtService {
    public String generateToken(String username,String role);
    public Claims extractClaims(String token);
    public boolean isTokenExpired(String token);
}
