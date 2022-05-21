package com.lemty.server.filter;

import com.lemty.server.LemtyApplication;
import com.lemty.server.repo.UserRepo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.*;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

public class CustomAuthenticationFilter extends UsernamePasswordAuthenticationFilter{
    Logger logger = LoggerFactory.getLogger(LemtyApplication.class);
    private final AuthenticationManager authenticationManager;
    private final UserRepo userRepo;

    public CustomAuthenticationFilter(AuthenticationManager authenticationManager, UserRepo userRepo){
        this.authenticationManager = authenticationManager;
        this.userRepo = userRepo;
    }

    @Override
    public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response) throws AuthenticationException {
        UsernameAndPasswordAuthenticationRequest authenticationRequest = null;
        try {
            authenticationRequest = new ObjectMapper().readValue(request.getInputStream(), UsernameAndPasswordAuthenticationRequest.class);
        } catch (IOException e) {
            e.printStackTrace();
        }
        Authentication authentication = new UsernamePasswordAuthenticationToken(authenticationRequest.getUsername(), authenticationRequest.getPassword());
        Authentication authenticate = authenticationManager.authenticate(authentication);
        return authenticate;
    }

    @Override
    protected void successfulAuthentication(HttpServletRequest request, HttpServletResponse response, FilterChain chain, Authentication authResult) throws IOException, ServletException {
        User user = (User)authResult.getPrincipal();
        Algorithm algorithm = Algorithm.HMAC256("secret".getBytes());
        String access_token = JWT.create()
                .withSubject(user.getUsername())
                // .withExpiresAt(new Date(System.currentTimeMillis() + 10 * 60 * 1000))
                .withIssuer(request.getRequestURL().toString())
                .withClaim("roles", user.getAuthorities().stream().map(GrantedAuthority::getAuthority).collect(Collectors.toList()))
                .sign(algorithm);

//         String refresh_token = JWT.create()
//                 .withSubject(user.getUsername())
//                 .withExpiresAt(new Date(System.currentTimeMillis() + 30 * 60 * 1000))
//                 .withIssuer(request.getRequestURL().toString())
//                 .sign(algorithm);

        String user2 = userRepo.findByUsername(user.getUsername()).getId().toString();

        // tokens.put("refresh_token", refresh_token);
        Cookie cookie = new Cookie("access_token", access_token);
        Cookie user_id_cookie = new Cookie("userId", user2);

        // optional properties
        cookie.setSecure(false);
        cookie.setHttpOnly(false);
        cookie.setMaxAge(90 * 60 * 60 * 24);
        cookie.setPath("/");

        user_id_cookie.setSecure(false);
        user_id_cookie.setHttpOnly(false);
        user_id_cookie.setMaxAge(90 * 60 * 60 * 24);
        user_id_cookie.setPath("/");

        response.addCookie(cookie);
        response.addCookie(user_id_cookie);
        response.setContentType(APPLICATION_JSON_VALUE);

        Map<String, String> data = new HashMap<>();
        data.put("access_token", access_token);
        data.put("user_id", user2);
        new ObjectMapper().writeValue(response.getOutputStream(), data);
        // new ObjectMapper().writeValue(response.getOutputStream(), tokens);
        logger.info(user2);
    }
}
