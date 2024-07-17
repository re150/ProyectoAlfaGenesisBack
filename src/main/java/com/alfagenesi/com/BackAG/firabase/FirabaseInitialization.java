package com.alfagenesi.com.BackAG.firabase;

import java.io.FileInputStream;
import java.io.IOException;
import javax.annotation.PostConstruct;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import org.springframework.context.annotation.Configuration;

@Configuration
public class FirabaseInitialization {
    public FirabaseInitialization() {
    }

    @PostConstruct
    public void inicializtion() {
        FileInputStream serviceAccount = null;

        try {
            serviceAccount = new FileInputStream("./servicesAccounKey.json");
            FirebaseOptions options = (new FirebaseOptions.Builder()).setCredentials(GoogleCredentials.fromStream(serviceAccount)).build();
            FirebaseApp.initializeApp(options);
        } catch (IOException var3) {
            var3.printStackTrace();
        }

    }
}
