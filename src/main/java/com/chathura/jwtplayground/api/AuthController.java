package com.chathura.jwtplayground.api;

import com.chathura.jwtplayground.dto.request.AuthRequestDto;
import com.chathura.jwtplayground.dto.response.SecureDataResponseDto;
import com.chathura.jwtplayground.service.JwtService;
import com.chathura.jwtplayground.util.StandardResponseDto;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private JwtService jwtService;

    @PostMapping
    public ResponseEntity<StandardResponseDto> login(@RequestBody AuthRequestDto dto) {
        if (jwtService.authenticate(dto.getUsername(), dto.getPassword())) {
            String token = jwtService.generateToken(dto.getUsername(), dto.getRole());
            return new ResponseEntity<>(
                    new StandardResponseDto(200, "Login successful", token),
                    HttpStatus.OK
            );
        } else {
            return new ResponseEntity<>(
                    new StandardResponseDto(401, "Invalid username or password", null),
                    HttpStatus.UNAUTHORIZED
            );
        }
    }

    @GetMapping
    public ResponseEntity<StandardResponseDto> getSecureData(
            @RequestHeader(value = "Authorization", required = false)
            String authHeader
    ) {
        if(authHeader == null || !authHeader.startsWith("Bearer ")) {
            return new ResponseEntity<>(
                    new StandardResponseDto(401, "Missing or invalid Authorization header", null),
                    HttpStatus.UNAUTHORIZED
            );
        }

        String token = authHeader.substring(7);
        try{
            if (jwtService.isTokenExpired(token)){
                return new ResponseEntity<>(
                        new StandardResponseDto(401, "Token has expired", null),
                        HttpStatus.UNAUTHORIZED
                );
            }
            Claims claims = jwtService.extractClaims(token);
            SecureDataResponseDto secureData = SecureDataResponseDto.builder()
                    .userName(claims.getSubject())
                    .role(claims.get("role", String.class))
                    .build();
            return new ResponseEntity<>(
                    new StandardResponseDto(
                            200,"Secure data access granted", secureData),
                    HttpStatus.OK
            );
        }catch (JwtException e){
            return new ResponseEntity<>(
                    new StandardResponseDto(401, "Invalid token", null),
                    HttpStatus.UNAUTHORIZED
            );
        }
    }
}
