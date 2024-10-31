package com.alfagenesi.com.BackAG.controller;

import com.alfagenesi.com.BackAG.model.TemplateProfile;
import com.alfagenesi.com.BackAG.service.AuthService;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/next/alfa")
public class AccountController {
    @Autowired
    private AuthService authService;
    @PostMapping("/NewProfile")
    public ResponseEntity<?> newProfile (@RequestBody TemplateProfile request){
        return ResponseEntity.ok(authService.createProfile(request));
    }
    @GetMapping("/showProfile/{email}")
    public  ResponseEntity<?> showProfile(@PathVariable String email) throws JsonProcessingException {
        return ResponseEntity.ok(authService.showProfiles(email));
    }
    @PatchMapping("/Punctuation")
    public ResponseEntity<?> punctuation (@RequestBody String request){
        return  ResponseEntity.ok(authService.updateStars(request));
    }
    @GetMapping("/GetPunctuation/{id}/{name}")
    public ResponseEntity<?> getPunctuation(@PathVariable String id, @PathVariable String name) {
        return  ResponseEntity.ok(authService.getStars(id, name));
    }
    @PatchMapping("/editProfile")
    public ResponseEntity<?> edit(@RequestBody String request){
        return ResponseEntity.ok(authService.updateImg(request));
    }
}
