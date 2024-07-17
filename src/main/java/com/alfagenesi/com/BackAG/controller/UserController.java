package com.alfagenesi.com.BackAG.controller;

import com.alfagenesi.com.BackAG.model.User;
import com.alfagenesi.com.BackAG.service.UserService;

import java.util.concurrent.ExecutionException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping({"/api"})
public class UserController {
    @Autowired
    private UserService userService;

    public UserController() {
    }

    @PostMapping({"/save"})
    public String save(@RequestBody User user) throws ExecutionException, InterruptedException {
        return this.userService.save(user);
    }
}