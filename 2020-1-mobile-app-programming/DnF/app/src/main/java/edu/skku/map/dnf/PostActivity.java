package edu.skku.map.dnf;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.HashMap;
import java.util.Map;

public class PostActivity extends AppCompatActivity {
    FirebaseFirestore db = FirebaseFirestore.getInstance(); // Acces Firestroefdsafdsa
    private static final String TAG = "AddPostActivity"; // Log.d debug asdasdasd

    private static final int PICK_IMAGE = 777;

    Uri currentImageUri;
    boolean check;
    boolean isLogIn = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post);

        TextView loginButton = (TextView)findViewById(R.id.loginButton);
        try{
            String characterName = MyGlobals.getInstance().getData().getCharacterName();
            isLogIn = true;
            loginButton.setText("어서오세요 " + characterName + " 님");

        }catch (Exception e){
            Log.d(TAG, "not login");
        }

        check = false;

        ImageView goHome = (ImageView) findViewById(R.id.goHome);
        goHome.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent1 = new Intent(PostActivity.this, MainActivity.class);
                startActivity(intent1);
            }
        });

        final EditText tags_edit = (EditText) findViewById(R.id.tags);
        final EditText contents_edit = (EditText) findViewById(R.id.contents);

        final LoginUser loginuser = new LoginUser();

        ImageButton select_img = (ImageButton) findViewById(R.id.select_img);
        select_img.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                Intent gallery = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.INTERNAL_CONTENT_URI);
                startActivityForResult(gallery, PICK_IMAGE);
            }
        });

        Button button = findViewById(R.id.createPost);
        button.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                final String tags = tags_edit.getText().toString();
                final String contents = contents_edit.getText().toString();
                if(contents.getBytes().length <= 0){
                    Toast.makeText(PostActivity.this, "Please input contents", Toast.LENGTH_LONG).show();
                }
                else{
                    if(check){
                        final StorageReference mStorageRef = FirebaseStorage.getInstance().getReference("Images").child(currentImageUri.getLastPathSegment());

                        mStorageRef.putFile(currentImageUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                            @Override
                            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                                Map<String, Object> posts = new HashMap<>();
                                posts.put("username", MyGlobals.getInstance().getData().getUsername());
                                posts.put("characterName", MyGlobals.getInstance().getData().getCharacterName());
                                posts.put("serverName", MyGlobals.getInstance().getData().getServerId());
                                posts.put("title", tags);
                                posts.put("pic_link", currentImageUri.getLastPathSegment());
                                posts.put("contents", contents);

                                // Add a new document with a generated ID
                                db.collection("posts")
                                        .add(posts)
                                        .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                                            @Override
                                            public void onSuccess(DocumentReference documentReference) {
                                                Log.d(TAG, "DocumentSnapshot added with ID: " + documentReference.getId());
                                            }
                                        })
                                        .addOnFailureListener(new OnFailureListener() {
                                            @Override
                                            public void onFailure(@NonNull Exception e) {
                                                Log.w(TAG, "Error adding document", e);
                                            }
                                        });


                                Intent intent = new Intent(PostActivity.this, MainActivity.class);
                                if (intent.resolveActivity(getPackageManager()) != null) {
                                    startActivity(intent);
                                }
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                e.getMessage();
                            }
                        });
                    }
                    else{
                        Map<String, Object> posts = new HashMap<>();
                        posts.put("username", MyGlobals.getInstance().getData().getUsername());
                        posts.put("characterName", MyGlobals.getInstance().getData().getCharacterName());
                        posts.put("serverName", MyGlobals.getInstance().getData().getServerId());
                        posts.put("title", tags);
                        posts.put("pic_link", "");
                        posts.put("contents", contents);

                        // Add a new document with a generated ID
                        db.collection("posts")
                                .add(posts)
                                .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                                    @Override
                                    public void onSuccess(DocumentReference documentReference) {
                                        Log.d(TAG, "DocumentSnapshot added with ID: " + documentReference.getId());
                                    }
                                })
                                .addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        Log.w(TAG, "Error adding document", e);
                                    }
                                });


                        Intent intent = new Intent(PostActivity.this, MainActivity.class);
                        if (intent.resolveActivity(getPackageManager()) != null) {
                            startActivity(intent);
                        }
                    }
                }

            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data){
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == PICK_IMAGE){
                ImageButton button = (ImageButton)findViewById(R.id.select_img);
                currentImageUri = data.getData();
                check = true;
                button.setImageURI(currentImageUri);
        }
    }
}