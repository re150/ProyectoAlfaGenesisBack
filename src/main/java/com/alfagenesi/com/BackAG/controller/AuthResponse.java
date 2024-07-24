package com.alfagenesi.com.BackAG.controller;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

// permite crear los get and set de maneautomatica
// para construir despues los objetos
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AuthResponse {
     String token;
}
