package com.alfagenesi.com.BackAG.controller;

import com.alfagenesi.com.BackAG.model.User;
import com.alfagenesi.com.BackAG.service.AuthService;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.UserRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

@RestController
@RequestMapping("/api")
public class AuthController {
    @Autowired
    private AuthService authService;


    @PostMapping("/SingUp") // ResponseEntity representa todas las respuestas
    public ResponseEntity<?> signup(@Valid @RequestBody User request) throws FirebaseAuthException {
        return ResponseEntity.ok(authService.add(request));
    }

   @PostMapping("/login1")
    public ResponseEntity<?> l1ogin (@RequestBody User request) {
        return ResponseEntity.ok(authService.login(request));
    }

}