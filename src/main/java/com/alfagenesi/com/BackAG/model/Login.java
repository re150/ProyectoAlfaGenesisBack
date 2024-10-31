package com.alfagenesi.com.BackAG.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data // permite crear los get and set de maneautomatica
@Builder // para construir despues los objetos
@AllArgsConstructor
@NoArgsConstructor
public class Login {
    String email;
    String password;
}
