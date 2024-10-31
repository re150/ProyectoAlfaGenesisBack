package com.alfagenesi.com.BackAG.controller;

import com.alfagenesi.com.BackAG.model.Login;
import com.alfagenesi.com.BackAG.service.AuthService;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.firebase.auth.FirebaseAuthException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@RestController
@RequestMapping("/api")
public class AuthController {
    @Autowired
    private AuthService authService;
    @PostMapping("/signUp")
    public ResponseEntity<?> signup(@Valid @RequestBody Login request) throws FirebaseAuthException {
        return ResponseEntity.ok(authService.add(request));
    }
   @PostMapping("/login")
    public ResponseEntity<?> login (@RequestBody Login request) throws JsonProcessingException {
        return ResponseEntity.ok(authService.login(request));
    }
}