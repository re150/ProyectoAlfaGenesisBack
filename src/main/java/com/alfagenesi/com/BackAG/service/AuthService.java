package com.alfagenesi.com.BackAG.service;


import com.alfagenesi.com.BackAG.model.Role;
import com.alfagenesi.com.BackAG.model.User;

import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.QueryDocumentSnapshot;
import com.google.cloud.firestore.QuerySnapshot;
import com.google.cloud.firestore.WriteResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.UserRecord;
import com.google.firebase.cloud.FirestoreClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutionException;

@Service
@RequiredArgsConstructor
public class AuthService {
    private static final Logger logger = LoggerFactory.getLogger(AuthService.class);
    private static final String COLLECTION_NAME = "user";

    public UserRecord add (User  user) throws FirebaseAuthException {
        Firestore dbFirestore = FirestoreClient.getFirestore();
        ApiFuture<QuerySnapshot> future = dbFirestore.collection("user")
                .whereEqualTo("email", user.getEmail())
                .get();

        try {
            List<QueryDocumentSnapshot> documents = future.get().getDocuments();
            if (!documents.isEmpty()) {
                throw new RuntimeException("El usuario con este email ya existe");
            }
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException("Error al verificar la existencia del usuario", e);
        }
        User data = User.builder()
                .name(user.getName())
                .email(user.getEmail())
                .password(user.getPassword())
                .grade(user.getGrade())
                .group(user.getGroup())
                .role(Role.USER)
                .build();

        String id = UUID.randomUUID().toString();
        ApiFuture<WriteResult> collectionApiFuture = dbFirestore.collection("user")
                .document(id).set(data);

        try {
            // Esperar a que la escritura en Firestore se complete
            collectionApiFuture.get();
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException(e);
        }

       UserRecord.CreateRequest request =  new UserRecord.CreateRequest()
               .setEmail(user.getEmail())
               .setPassword(user.getPassword());
        return FirebaseAuth.getInstance().createUser(request);
    }

    public ResponseEntity<?> login( User request ) {
        try {
            UserRecord userRecord = FirebaseAuth.getInstance().getUserByEmail(request.getEmail());
            String customToken = String.valueOf(FirebaseAuth.getInstance().verifyIdToken(userRecord.getUid()));
            return ResponseEntity.ok().body(customToken);
        } catch (FirebaseAuthException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Login failed: " + e.getMessage());
        }
    }

}
