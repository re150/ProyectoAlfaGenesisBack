package com.alfagenesi.com.BackAG.service;


import com.alfagenesi.com.BackAG.model.*;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.api.client.json.Json;
import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.*;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.UserRecord;
import com.google.firebase.cloud.FirestoreClient;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.*;
import java.util.concurrent.ExecutionException;

import org.springframework.beans.factory.annotation.Value;

@Service
@RequiredArgsConstructor
public class AuthService {

    @Value("${apiKey}")
    private String apiKey;
    private static final Logger logger = LoggerFactory.getLogger(AuthService.class);
    private Long noProfile;
    private static final String COLLECTION_NAME = "user";
    private static final String COLLECTION_PROFILE = "profile";
    private static final String COLLECTION_MEMBERS = "members";
    private static final String NUM_PROFILE = "noProfile";
    private static final String NUM_STARS = "stars";
    private static final String COLLECTION_TEAMS = "teams";
    private static final String COLLECTION_TEAMNAME = "nameTeam";
    private static final String COLLECTION_TEAMNOM = "nomMembers";


    public String add(Login user) throws FirebaseAuthException {
        Firestore dbFirestore = FirestoreClient.getFirestore();
        ApiFuture<QuerySnapshot> future = dbFirestore.collection(COLLECTION_NAME)
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
        String id = UUID.randomUUID().toString();

        User data = User.builder()
                .id(id)
                .email(user.getEmail())
                .noProfile(0L)
                .build();

        ApiFuture<WriteResult> collectionApiFuture = dbFirestore.collection(COLLECTION_NAME)
                .document(id).set(data);

        try {
            collectionApiFuture.get();
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException(e);
        }
        UserRecord.CreateRequest request = new UserRecord.CreateRequest()
                .setEmail(user.getEmail())
                .setPassword(user.getPassword());
        FirebaseAuth.getInstance().createUser(request);
        return "ok";
    }

    public String login(Login request) throws JsonProcessingException {
        String url = "https://identitytoolkit.googleapis.com/v1/accounts:signInWithPassword?key=" + apiKey;
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        Map<String, Object> payload = new HashMap<>();
        payload.put("email", request.getEmail());
        payload.put("password", request.getPassword());
        payload.put("returnSecureToken", true);

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(payload, headers);
        ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.POST, entity, String.class);
        ObjectMapper mapper = new ObjectMapper();
        Map<String, Object> responseMap = mapper.readValue(response.getBody(), Map.class);
        Map<String, Object> responseEnd = new HashMap<>();

        responseEnd.put("idToken", responseMap.get("idToken"));
        return mapper.writeValueAsString(responseEnd);
    }

    public String createProfile(TemplateProfile request) {
        Firestore dbFirestore = FirestoreClient.getFirestore();

        ApiFuture<QuerySnapshot> future = dbFirestore.collection(COLLECTION_NAME)
                .whereEqualTo("email", request.getEmail())
                .get();

        try {
            List<QueryDocumentSnapshot> documents = future.get().getDocuments();
            if (!documents.isEmpty()) {
                QueryDocumentSnapshot document = documents.get(0);
                request.setId(document.getId());
                noProfile = document.getLong(NUM_PROFILE);
                if (noProfile <= 5) {
                    noProfile += 1;
                    dbFirestore.collection(COLLECTION_NAME)
                            .document(request.getId())
                            .update(NUM_PROFILE, noProfile);
                } else {
                    throw new RuntimeException("Excede el numero de perfiles");
                }
            }
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException("Error al verificar la existencia del usuario", e);
        }

        Profile data = new Profile();
        data.setId(request.getId());
        data.setName(request.getName());
        data.setGrado(request.getGrado());
        data.setGrupo(request.getGrupo());
        data.setImgUrl(request.getImgUrl());
        data.setLevel(request.getLevel());
        data.setStars(request.getStars());
        data.setTeamStatus(request.getTeamStatus());

        dbFirestore.collection(COLLECTION_NAME).document(request.getId())
                .collection(COLLECTION_PROFILE).document(data.getName()).set(data);

        dbFirestore.collection(COLLECTION_NAME)
                .document(request.getId())
                .update("noProfile", noProfile);

        return "Nuevo perfil";
    }

    public String showProfiles(String request) {
        List<Profile> userProfiles = new ArrayList<>();
        String userId = getProfileIdByEmail(request);
        Firestore dbFirestore = FirestoreClient.getFirestore();

        ApiFuture<QuerySnapshot> future = dbFirestore.collection(COLLECTION_NAME)
                .document(userId)
                .collection(COLLECTION_PROFILE)
                .get();
        try {
            List<QueryDocumentSnapshot> documents = future.get().getDocuments();
            for (QueryDocumentSnapshot document : documents) {
                Profile profile = document.toObject(Profile.class);
                userProfiles.add(profile);
            }
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException("Error al obtener los perfiles del usuario", e);
        }

        Gson gson = new Gson();
        return gson.toJson(userProfiles);
    }

    private String getProfileIdByEmail(String email) {
        Firestore dbFirestore = FirestoreClient.getFirestore();
        Query query = dbFirestore.collection(COLLECTION_NAME)
                .whereEqualTo("email", email);

        ApiFuture<QuerySnapshot> future = query.get();

        try {
            List<QueryDocumentSnapshot> documents = future.get().getDocuments();
            if (!documents.isEmpty()) {
                return documents.get(0).getId();
            } else {
                throw new RuntimeException("No profile found with the given email.");
            }
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException("Error fetching profile ID", e);
        }
    }

    public String updateImg (String dataUser){
        Firestore db = FirestoreClient.getFirestore();
        ObjectMapper objectMapper = new ObjectMapper();
        String id = "";
        String name = "";
        String  url = "";

        try{
            JsonNode node = objectMapper.readTree(dataUser);
            JsonNode iduser = node.get("id");
            id = (iduser != null) ? iduser.asText() : null;

            JsonNode nameUser = node.get("name");
            name = (nameUser != null) ? nameUser.asText() : null;

            JsonNode userStars = node.get("imgUrl");
            url = (userStars != null) ?userStars.asText() : null;

            if(id != null && name != null){
                ApiFuture<QuerySnapshot> futureProfile = db.collection(COLLECTION_NAME)
                        .document(id)
                        .collection(COLLECTION_PROFILE)
                        .whereEqualTo("name", name)
                        .get();


                List<QueryDocumentSnapshot> profileDocs = futureProfile.get().getDocuments();
                QueryDocumentSnapshot profileDoc = profileDocs.get(0);
                DocumentReference profileRef = profileDoc.getReference();
                ApiFuture<WriteResult> updateProfile = profileRef.update("imgUrl",url);
                updateProfile.get();
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        return "Se actualizó la imagen";
    }

    public String showAllProfile() {
        Firestore db = FirestoreClient.getFirestore();
        Query query = db.collection(COLLECTION_NAME);
        ApiFuture<QuerySnapshot> querySnapshot = query.get();
        List<String> profiles = new ArrayList<>();

        try {
            for (DocumentSnapshot document : querySnapshot.get().getDocuments()) {
                String id = document.getId();
                db.collection(COLLECTION_NAME)
                        .whereEqualTo("id", id)
                        .get();
                noProfile = document.getLong(NUM_PROFILE);
                if (noProfile != 0) {
                    profiles.add(getProfiles(id).toString());
                }
            }
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }

        Gson gson = new Gson();
        return gson.toJson(profiles);
    }

    private List<String> getProfiles(String request) {
        List<String> userProfiles = new ArrayList<>();
        String userId = request;

        Firestore dbFirestore = FirestoreClient.getFirestore();
        ApiFuture<QuerySnapshot> future = dbFirestore.collection(COLLECTION_NAME)
                .document(userId)
                .collection(COLLECTION_PROFILE)
                .get();

        try {
            //logger.info("np: {}",queryNoProfiles.get().getLong(NUM_PROFILE));
            List<QueryDocumentSnapshot> documents = future.get().getDocuments();
            for (QueryDocumentSnapshot document : documents) {
                Profile profile = document.toObject(Profile.class);
                userProfiles.add(profile.toString());
            }
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException("Error al obtener los perfiles del usuario", e);
        }

        return userProfiles;
    }

    public String createTeam(String jsonData) {
        Firestore db = FirestoreClient.getFirestore();

        ObjectMapper objectMapper = new ObjectMapper();
        List<Profile> members = new ArrayList<>();
        int nomMembers = 0;
        String nameT = null;

        try {
            JsonNode jsonNode = objectMapper.readTree(jsonData);

            JsonNode nomMembersNode = jsonNode.get("NomMembers");
            nomMembers = (nomMembersNode != null) ? nomMembersNode.asInt() : 0;

            JsonNode nameTNode = jsonNode.get("nameTeam");
            nameT = (nameTNode != null) ? nameTNode.asText() : null;

            for (int i = 1; i <= nomMembers; i++) {
                String memberKey = "member" + i;
                JsonNode memberNode = jsonNode.get(memberKey);
                if (memberNode != null) {
                    String memberId = memberNode.get("id") != null ? memberNode.get("id").asText() : null;
                    String memberName = memberNode.get("name") != null ? memberNode.get("name").asText() : null;
                    String memberImgUrl = memberNode.get("imgUrl") != null ? memberNode.get("imgUrl").asText() : null;
                    int memberGrado = memberNode.get("grado") != null ? memberNode.get("grado").asInt() : null;
                    int memberLevel = memberNode.get("level") != null ? memberNode.get("level").asInt() : null;
                    int memberStars = memberNode.get("stars") != null ? memberNode.get("stars").asInt() : null;
                    String memberGrupo = memberNode.get("grupo") != null ? memberNode.get("grupo").asText() : null;
                    boolean memberteamStatus = memberNode.get("teamStatus") != null ? memberNode.get("teamStatus").asBoolean() : null;

                    if (memberId != null && memberName != null) {
                        Profile data = new Profile();
                        data.setId(memberId);
                        data.setName(memberName);
                        data.setImgUrl(memberImgUrl);
                        data.setGrado(memberGrado);
                        data.setLevel(memberLevel);
                        data.setStars(memberStars);
                        data.setGrupo(memberGrupo);
                        data.setStars(memberStars);
                        data.setTeamStatus(memberteamStatus);
                        members.add(data);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        String id = UUID.randomUUID().toString();
        DataTeam teamD = new DataTeam();
        teamD.setId(id);
        teamD.setNomMembers(nomMembers);
        teamD.setNameTeam(nameT);

        db.collection(COLLECTION_TEAMS).document(id).set(teamD);
        if (nomMembers <= 5) {
            for (int i = 0; i < members.size(); i++) {
                int no = i + 1;

                db.collection(COLLECTION_NAME).document(members.get(i).getId())
                        .collection(COLLECTION_PROFILE).document(members.get(i).getName())
                        .update("teamStatus", true);

                db.collection(COLLECTION_TEAMS).document(id)
                        .collection(COLLECTION_MEMBERS).document("member" + i)
                        .set(members.get(i));
            }
        }

        return id;
    }

    public String showTeam() {
        Firestore db = FirestoreClient.getFirestore();
        Query query = db.collection(COLLECTION_TEAMS);
        ApiFuture<QuerySnapshot> querySnapshot = query.get();
        List<Map<String, Object>> teamsList = new ArrayList<>();

        try {
            for (DocumentSnapshot document : querySnapshot.get().getDocuments()) {
                Map<String, Object> teamData = new HashMap<>();
                teamData.put("nameTeam", document.getString(COLLECTION_TEAMNAME));
                teamData.put("id", document.getId());
                teamData.put("nomMembers", Math.toIntExact(document.getLong(COLLECTION_TEAMNOM)));

                List<Map<String, Object>> membersList = getMembers(document.getId());
                teamData.put("members", membersList);
                teamsList.add(teamData);
            }
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }

        Gson gson = new Gson();
        return gson.toJson(teamsList);
    }

    private List<Map<String, Object>> getMembers(String teamId) {
        List<Map<String, Object>> membersList = new ArrayList<>();
        Firestore dbFirestore = FirestoreClient.getFirestore();
        ApiFuture<QuerySnapshot> future = dbFirestore.collection(COLLECTION_TEAMS)
                .document(teamId)
                .collection(COLLECTION_MEMBERS)
                .get();

        try {
            List<QueryDocumentSnapshot> documents = future.get().getDocuments();

            for (QueryDocumentSnapshot document : documents) {
                Map<String, Object> memberData = document.getData();
                membersList.add(memberData);
            }
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException("Error al obtener los perfiles del usuario", e);
        }
        return membersList;
    }

    public String deleteMember(String dataMember) {
        Firestore db = FirestoreClient.getFirestore();
        ObjectMapper objectMapper = new ObjectMapper();
        String idTema = "";
        String name = "";
        String idUser = "";

        try {
            JsonNode jsonNode = objectMapper.readTree(dataMember);

            JsonNode idTeamJ = jsonNode.get("id");
            idTema = (idTeamJ != null) ? idTeamJ.asText() : null;
            logger.info("data: {}", idTema);
            JsonNode nameTNode = jsonNode.get("name");
            name = (nameTNode != null) ? nameTNode.asText() : null;

            JsonNode idUserJ = jsonNode.get("idUser");
            idUser = (idUserJ != null) ? idUserJ.asText() : null;


            ApiFuture<QuerySnapshot> future = db.collection(COLLECTION_TEAMS)
                    .document(idTema)
                    .collection(COLLECTION_MEMBERS)
                    .whereEqualTo("id", idUser)
                    .whereEqualTo("name", name)
                    .get();

            changeStatusMember(idUser, name, "Delete");
            List<QueryDocumentSnapshot> documents = future.get().getDocuments();
            QueryDocumentSnapshot document = documents.get(0);
            DocumentReference docRef = document.getReference();
            ApiFuture<WriteResult> writeResult = docRef.delete();
            writeResult.get();

        } catch (Exception e) {
            e.printStackTrace();
        }

        return "Member deleted";
    }

    public String addMember(String dataMember) {
        Firestore db = FirestoreClient.getFirestore();
        ObjectMapper objectMapper = new ObjectMapper();
        String idTema = "";
        int nom = 0;
        Profile newMemer = new Profile();

        try {
            JsonNode jsonNode = objectMapper.readTree(dataMember);

            JsonNode nomA = jsonNode.get("nom");
            nom = (nomA != null) ? nomA.asInt() : null;

            JsonNode idTeamJ = jsonNode.get("id");
            idTema = (idTeamJ != null) ? idTeamJ.asText() : null;

            JsonNode id = jsonNode.get("idUser");
            newMemer.setId((id != null) ? id.asText() : null);

            JsonNode name = jsonNode.get("name");
            newMemer.setName((name != null) ? name.asText() : null);

            JsonNode img = jsonNode.get("imgUrl");
            newMemer.setImgUrl((img != null) ? img.asText() : null);

            JsonNode grado = jsonNode.get("grado");
            newMemer.setGrado((grado != null) ? grado.asInt() : null);

            JsonNode level = jsonNode.get("level");
            newMemer.setLevel((level != null) ? level.asInt() : null);

            JsonNode stars = jsonNode.get("level");
            newMemer.setLevel((stars != null) ? level.asInt() : null);

            JsonNode grupo = jsonNode.get("grupo");
            newMemer.setGrupo((grupo != null) ? grupo.asText() : null);
            newMemer.setTeamStatus(true);


            db.collection(COLLECTION_TEAMS).document(idTema)
                    .collection(COLLECTION_MEMBERS)
                    .document("member" + (nom + 1))
                    .set(newMemer);

        } catch (Exception e) {
            e.printStackTrace();
        }
        changeStatusMember(newMemer.getId(), newMemer.getName(), "ADD");
        return "Member deleted";
    }

    public String changeStatusMember(String idUser, String nameUser, String status) {
        Firestore db = FirestoreClient.getFirestore();

        String name = nameUser;
        String id = idUser;
        boolean newStatus = !status.equals("Delete");
        try {
            if (!newStatus) {
                ApiFuture<QuerySnapshot> futureProfile = db.collection(COLLECTION_NAME)
                        .document(idUser)
                        .collection(COLLECTION_PROFILE)
                        .whereEqualTo("name", name)
                        .get();


                List<QueryDocumentSnapshot> profileDocs = futureProfile.get().getDocuments();
                QueryDocumentSnapshot profileDoc = profileDocs.get(0);
                DocumentReference profileRef = profileDoc.getReference();
                ApiFuture<WriteResult> updateProfile = profileRef.update("teamStatus", false);
                updateProfile.get();
            } else {
                ApiFuture<QuerySnapshot> futureProfile = db.collection(COLLECTION_NAME)
                        .document(idUser)
                        .collection(COLLECTION_PROFILE)
                        .whereEqualTo("name", name)
                        .get();


                List<QueryDocumentSnapshot> profileDocs = futureProfile.get().getDocuments();
                QueryDocumentSnapshot profileDoc = profileDocs.get(0);
                DocumentReference profileRef = profileDoc.getReference();
                ApiFuture<WriteResult> updateProfile = profileRef.update("teamStatus", true);
                updateProfile.get();
            }

        } catch (Exception e) {

            e.printStackTrace();
            return "Failed to change status: " + e.getMessage();
        }

        return "Status changed successfully";
    }

    public String deleteTeam(String dataMember) {
        Firestore db = FirestoreClient.getFirestore();
        ObjectMapper objectMapper = new ObjectMapper();
        String id = "";

        try {
            JsonNode jsonNode = objectMapper.readTree(dataMember);

            JsonNode idTeamJ = jsonNode.get("id");
            id = (idTeamJ != null) ? idTeamJ.asText() : null;

            if (id != null) {
                DocumentReference docRef = db.collection(COLLECTION_TEAMS).document(id);
                ApiFuture<WriteResult> writeResult = docRef.delete();
                WriteResult result = writeResult.get();
            } else {
                System.out.println("No se proporcionó un ID válido.");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "Member deleted";
    }
    public String updateStars (String dataUser){
        Firestore db = FirestoreClient.getFirestore();
        ObjectMapper objectMapper = new ObjectMapper();
        String id = "";
        String name = "";
        int stars = 0;
        int actualStars = 0;

        try{
            JsonNode node = objectMapper.readTree(dataUser);
            JsonNode iduser = node.get("id");
            id = (iduser != null) ? iduser.asText() : null;

            JsonNode nameUser = node.get("name");
            name = (nameUser != null) ? nameUser.asText() : null;

            JsonNode userStars = node.get("stars");
            stars = (userStars != null) ?userStars.asInt() : 0;

            if(id != null && name != null){
                ApiFuture<QuerySnapshot> futureProfile = db.collection(COLLECTION_NAME)
                        .document(id)
                        .collection(COLLECTION_PROFILE)
                        .whereEqualTo("name", name)
                        .get();


                List<QueryDocumentSnapshot> profileDocs = futureProfile.get().getDocuments();
                QueryDocumentSnapshot profileDoc = profileDocs.get(0);
                actualStars = Math.toIntExact(profileDoc.getLong(NUM_STARS));
                actualStars += stars;
                DocumentReference profileRef = profileDoc.getReference();
                ApiFuture<WriteResult> updateProfile = profileRef.update("stars",actualStars);
                updateProfile.get();
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        return "El nuemro de estrellas se actualizo";
    }
    public int getStars (String id, String name){
        Firestore db = FirestoreClient.getFirestore();
        String idUser = id;
        String nameUser = name;
        int actualStars = 0;

        try{
            if(id != null && name != null){
                ApiFuture<QuerySnapshot> futureProfile = db.collection(COLLECTION_NAME)
                        .document(idUser)
                        .collection(COLLECTION_PROFILE)
                        .whereEqualTo("name", nameUser)
                        .get();
                List<QueryDocumentSnapshot> profileDocs = futureProfile.get().getDocuments();
                QueryDocumentSnapshot profileDoc = profileDocs.get(0);
                actualStars = Math.toIntExact(profileDoc.getLong(NUM_STARS));
                return  actualStars;
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        return actualStars;
    }
    public String updateStarsTeam (String dataUser){
        Firestore db = FirestoreClient.getFirestore();
        ObjectMapper objectMapper = new ObjectMapper();
        String id = "";
        int stars = 0;
        List<String> memberInfoList = new ArrayList<>();
        try{
            JsonNode node = objectMapper.readTree(dataUser);
            JsonNode iduser = node.get("id");
            id = (iduser != null) ? iduser.asText() : null;

            JsonNode userStars = node.get("stars");
            stars = (userStars != null) ?userStars.asInt() : 0;

            if(id != null ){
                ApiFuture<QuerySnapshot> futureProfile = db.collection(COLLECTION_TEAMS)
                        .document(id)
                        .collection(COLLECTION_MEMBERS)
                        .get();

                List<QueryDocumentSnapshot> profileDocs = futureProfile.get().getDocuments();

                for (QueryDocumentSnapshot profileDoc : profileDocs) {
                    String memberId = profileDoc.getString("id");
                    String memberName = profileDoc.getString("name");

                    JsonObject memberJson = new JsonObject();
                    memberJson.addProperty("id", memberId);
                    memberJson.addProperty("name", memberName);
                    memberJson.addProperty("stars", stars);

                    memberInfoList.add(memberJson.toString());
                }

                for(String user: memberInfoList){
                    updateStars(user);
                }
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        return "El nuemro de estrellas se actualizo";
    }
}