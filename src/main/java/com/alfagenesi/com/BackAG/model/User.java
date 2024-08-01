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
public class User /*implements UserDetails*/ {
     @NotNull(message = "El campo password no puede ser nulo")
     int grade;
     String group;
     String name;
     String email;
     String password;
     Role role;
     /*
     @Override
     public Collection<? extends GrantedAuthority> getAuthorities() {
          return null;
     }

     @Override
     public String getUsername() {
          return getName();
     }

     @Override
     public boolean isAccountNonExpired() {
          return UserDetails.super.isAccountNonExpired();
     }

     @Override
     public boolean isAccountNonLocked() {
          return UserDetails.super.isAccountNonLocked();
     }

     @Override
     public boolean isCredentialsNonExpired() {
          return UserDetails.super.isCredentialsNonExpired();
     }

     @Override
     public boolean isEnabled() {
          return UserDetails.super.isEnabled();
     }*/
}
