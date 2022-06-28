package edu.skku.map.pa2;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;
import android.widget.ImageView;

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
    private String iconlink = "";
    private String fname;
    private String birth;
    private String mail;
    private Bitmap icon;

    public void setIconlink(String iconlink){
        this.iconlink = iconlink;
    }

    public void setUsername(String username){
        this.username = username;
    }

    public String getFname(){
        return this.fname;
    }
    public String getBirth(){
        return this.birth;
    }
    public String getMail(){
        return this.mail;
    }
    public Bitmap getIcon(){
        return this.icon;
    }
    public String getIconlink(){
        return this.iconlink;
    }
    public void setFname(String fullname){
        this.fname = fullname;
    }
    public void setBirth(String birthday){
        this.birth = birthday;
    }
    public void setMail(String email){
        this.mail = email;
    }

    public void setupVal(final retData retdata) {
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
                    fname = (String) userdata.get(0).get("fullname");
                    birth = (String) userdata.get(0).get("birthday");
                    mail = (String) userdata.get(0).get("email");
                    iconlink = (String) userdata.get(0).get("iconlink");
                    retdata.retDataFunc(loginuser);
                } else {
                    Log.w("??", "Error adding document");
                }
            }
        });
    }

    public void setupicon(final retData retdata2){
        final LoginUser loginuser = this;
        if(this.iconlink != null){
            if(this.iconlink.getBytes().length > 0){
                StorageReference mStorageRef = FirebaseStorage.getInstance().getReference("Icons").child(iconlink);
                final long ONE_MEGABYTE = 1024 * 1024;
                mStorageRef.getBytes(ONE_MEGABYTE).addOnSuccessListener(new OnSuccessListener<byte[]>() {
                    @Override
                    public void onSuccess(byte[] bytes) {
                        Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                        icon = bitmap;
                        retdata2.retDataFunc(loginuser);
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception exception) {
                        // Handle any errors
                    }
                });
            }
        }
    }
}

interface retData{
    void retDataFunc(LoginUser loginuser);
}
