package com.alfagenesi.com.BackAG.controller;

import com.alfagenesi.com.BackAG.service.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@RestController
@RequestMapping("/next/alfa/teams")
public class TeamsController {
    @Autowired
    private AuthService authService;
    @GetMapping("/ShowProfiles")
    public ResponseEntity<?> showStudens (){
        return ResponseEntity.ok(authService.showAllProfile());
    }
    @PostMapping("/CreateTeam")
    public ResponseEntity<?> addMembers (@RequestBody String data){
        return ResponseEntity.ok(authService.createTeam(data));
    }
    @GetMapping("/showTeams")
    public ResponseEntity<?> showteam (){return  ResponseEntity.ok(authService.showTeam());}

    @PostMapping("/DeleteMember")
    public ResponseEntity<?> deleteMember (@Valid @RequestBody String request) {
        return ResponseEntity.ok(authService.deleteMember(request));
    }
    @PostMapping("/DeleteTeam")
    public ResponseEntity<?> deleteTeam (@Valid @RequestBody String request) {
        return ResponseEntity.ok(authService.deleteTeam(request));
    }
    @PostMapping("/AddMember")
    public ResponseEntity<?> addMember (@RequestBody String request){
        return ResponseEntity.ok(authService.addMember(request));
    }
    @PatchMapping("/PunctuationTeam")
    public ResponseEntity<?> punctuationTeam (@RequestBody String request){
        return  ResponseEntity.ok(authService.updateStarsTeam(request));
    }
}
