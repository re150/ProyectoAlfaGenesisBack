package com.alfagenesi.com.BackAG.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import javax.validation.constraints.NotNull;
import java.util.Collection;

@Data // permite crear los get and set de maneautomatica
@Builder // para construir despues los objetos
@AllArgsConstructor
@NoArgsConstructor
public class User {
     @NotNull(message = "El campo password no puede ser nulo")
     String id;
     String email;
     Long noProfile;
     //Role role;
}
