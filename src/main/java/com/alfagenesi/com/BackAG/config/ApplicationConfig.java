package com.alfagenesi.com.BackAG.config;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.UserRecord;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.ArrayList;

@Configuration
@RequiredArgsConstructor
public class ApplicationConfig {
    @Bean
    public AuthenticationManager authenticationManager
            (AuthenticationConfiguration confg) throws Exception {
        return confg.getAuthenticationManager();
    }

    @Bean
    public AuthenticationProvider authenticationProvider(){
        DaoAuthenticationProvider authenticationProvider = new DaoAuthenticationProvider();
        authenticationProvider.setUserDetailsService(userDetailService());
        authenticationProvider.setPasswordEncoder(passwordEncode());
        return  authenticationProvider;
    }
    @Bean
    public PasswordEncoder passwordEncode() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public UserDetailsService userDetailService() {
        return username -> {
            try {
                UserRecord userRecord = FirebaseAuth.getInstance().getUserByEmail(username);
                if (userRecord != null) {
// Puedes                     mapear UserRecord a tu propia clase de UserDetails si es necesario
                    return new org.springframework.security.core.userdetails.User(
                            userRecord.getEmail(),
                            "", // Las contraseñas no se almacenan de esta manera en Firebase
                            new ArrayList<>() // Agrega roles si es necesario
                    );
                } else {
                    throw new UsernameNotFoundException("User not found");
                }
            } catch (FirebaseAuthException e) {
                throw new UsernameNotFoundException("User not found", e);
            }
        };
    }
}

