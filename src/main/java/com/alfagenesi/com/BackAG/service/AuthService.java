package com.alfagenesi.com.BackAG.service;


import com.alfagenesi.com.BackAG.controller.AuthResponse;
import com.alfagenesi.com.BackAG.jwt.JwtService;
import com.alfagenesi.com.BackAG.model.User;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutionException;

import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.*;
import com.google.firebase.cloud.FirestoreClient;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {
    private static final Logger logger = LoggerFactory.getLogger(AuthService.class);
    private static final String COLLECTION_NAME = "user";
    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;

    public AuthResponse login(User request) {
        Firestore dbFirestore = FirestoreClient.getFirestore();
        ApiFuture<QuerySnapshot> future = dbFirestore.collection("user")
                .whereEqualTo("email", request.getEmail())
                .get();
        try {
            List<QueryDocumentSnapshot> documents = future.get().getDocuments();
            if (documents.isEmpty()) {
                throw new RuntimeException("Usuario no encontrado");
            }

            // Obtener el documento del usuario
            QueryDocumentSnapshot document = documents.get(0);
            UserDetails storedUser = document.toObject(User.class);

            // Verificar la contraseña
            if (!storedUser.getPassword().equals(request.getPassword())) {
                throw new RuntimeException("Contraseña incorrecta");
            }
            // Generar el token JWT
            String token = jwtService.getToken(request);

            // Devolver la respuesta de autenticación
            return AuthResponse.builder()
                    .token(token)
                    .build();
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException("Error al verificar el usuario", e);
        }
    }

    public AuthResponse register(User request) {
        Firestore dbFirestore = FirestoreClient.getFirestore();
        // Verificar si el usuario ya existe
        ApiFuture<QuerySnapshot> future = dbFirestore.collection("user")
                .whereEqualTo("email", request.getEmail())
                .get();

        try {
            List<QueryDocumentSnapshot> documents = future.get().getDocuments();
            if (!documents.isEmpty()) {
                throw new RuntimeException("El usuario con este email ya existe");
            }
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException("Error al verificar la existencia del usuario", e);
        }
        // Guardar el usuario en Firestore
        String id = UUID.randomUUID().toString();
        ApiFuture<WriteResult> collectionApiFuture = dbFirestore.collection("user")
                .document(id).set(request);

        try {
            // Esperar a que la escritura en Firestore se complete
            collectionApiFuture.get();
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException(e);
        }

        return AuthResponse.builder()
                .token(jwtService.getToken(request))
                .build();
    }
}
