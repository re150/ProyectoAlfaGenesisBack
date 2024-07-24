package com.alfagenesi.com.BackAG.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/next/alfa")
public class WindosController {
    @PostMapping("/v1")
    public String welcom (){
        return "next windos";
    }
}
