package com.alfagenesi.com.BackAG.service;


import com.alfagenesi.com.BackAG.model.User;

import java.util.UUID;
import java.util.concurrent.ExecutionException;

import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.Firestore;
import com.google.firebase.cloud.FirestoreClient;
import org.springframework.stereotype.Service;
import com.google.cloud.firestore.WriteResult;

@Service
public class UserService {
    private static final String COLLECTION_NAME = "user";

    public UserService() {
    }

    public String save(User usuario) {
        Firestore dbFirestore = FirestoreClient.getFirestore();
        String id = UUID.randomUUID().toString();
        ApiFuture<WriteResult> collectionApiFuture = dbFirestore.collection("user").document(id).set(usuario);

        try {
            return ((WriteResult) collectionApiFuture.get()).getUpdateTime().toString();
        } catch (InterruptedException var6) {
            throw new RuntimeException(var6);
        } catch (ExecutionException var7) {
            throw new RuntimeException(var7);
        }
    }
}
