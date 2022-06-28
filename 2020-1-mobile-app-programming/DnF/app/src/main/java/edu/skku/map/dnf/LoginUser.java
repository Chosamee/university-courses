package edu.skku.map.dnf;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.Map;

public class LoginUser {
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    private String username;
    private String characterName;
    private String password;
    private String serverId;
    private String characterId;
    private String serverName;
    private Bitmap characterPic;

    public Bitmap getCharacterPic() {
        return characterPic;
    }

    public void setCharacterPic(Bitmap characterPic) {
        this.characterPic = characterPic;
    }

    public String getServerName() {
        return serverName;
    }

    public void setServerName(String serverName) {
        this.serverName = serverName;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getCharacterName() {
        return characterName;
    }

    public void setCharacterName(String characterName) {
        this.characterName = characterName;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getServerId() {
        return serverId;
    }

    public void setServerId(String serverId) {
        this.serverId = serverId;
    }

    public String getCharacterId() {
        return characterId;
    }

    public void setCharacterId(String characterId) {
        this.characterId = characterId;
    }

    public void setupVal() {
        final ArrayList<Map<String, Object>> userdata = new ArrayList<>();
        final LoginUser loginuser = this;
        CollectionReference userRef = db.collection("users");
        Query query = userRef.whereEqualTo("username", this.username);
        query.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    for (QueryDocumentSnapshot document : task.getResult()) {
                        userdata.add(document.getData());
                        Log.d("dat", document.getId() + " => " + document.getData());
                    }
                    try{
                        serverName = (String) userdata.get(0).get("serverName");
                        characterName = (String) userdata.get(0).get("characterName");
                    }
                    catch (Exception e){
                        Log.w("??", "Error adding document");
                    }
                } else {
                    Log.w("??", "Error adding document");
                }
            }
        });
    }
}