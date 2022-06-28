package edu.skku.map.pa2;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.navigation.NavigationView;
import com.google.errorprone.annotations.Var;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.lang.reflect.Array;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class AddPostActivity extends AppCompatActivity{
    private DrawerLayout drawer;
    FirebaseFirestore db = FirebaseFirestore.getInstance(); // Acces Firestroefdsafdsa
    private static final String TAG = "AddPostActivity"; // Log.d debug asdasdasd

    private static final int PICK_IMAGE = 777;

    private String id_val;
    Uri currentImageUri;
    boolean check;
    boolean ispost = true;
    private int ispersonal = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_post);

        check = false;

        final CheckBox checkBox = (CheckBox)findViewById(R.id.checkBox);
        Intent get_intent = getIntent();
        id_val = get_intent.getStringExtra("id_val");

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false); //initial del
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);  //button
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.menu); //icon

        drawer = findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        final EditText tags_edit = (EditText) findViewById(R.id.tags);
        final EditText contents_edit = (EditText) findViewById(R.id.contents);

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        final MenuItem nav_full_name = navigationView.getMenu().findItem(R.id.nav_full_name);
        final MenuItem nav_birthday = navigationView.getMenu().findItem(R.id.nav_birthday);
        final MenuItem nav_email = navigationView.getMenu().findItem(R.id.nav_email);
        final TextView set_name = navigationView.getHeaderView(0).findViewById(R.id.set_name);
        final ImageView profile_img = navigationView.getHeaderView(0).findViewById(R.id.profile_img);
        set_name.setText(id_val);

        final LoginUser loginuser = new LoginUser();
        loginuser.setUsername(id_val);
        retData retdata = new retData() {
            @Override
            public void retDataFunc(LoginUser loginuser) {
                nav_full_name.setTitle(loginuser.getFname());
                nav_birthday.setTitle(loginuser.getBirth());
                nav_email.setTitle(loginuser.getMail());
                retData retdata2 = new retData() {
                    @Override
                    public void retDataFunc(LoginUser loginuser) {
                        profile_img.setImageBitmap(loginuser.getIcon());
                    }
                };
                loginuser.setupicon(retdata2);
            }
        };
        loginuser.setupVal(retdata);

        profile_img.setClickable(true);
        profile_img.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                ispost = false;
                Intent gallery = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.INTERNAL_CONTENT_URI);
                startActivityForResult(gallery, PICK_IMAGE);
            }
        });

        ImageButton select_img = (ImageButton) findViewById(R.id.select_img);
        select_img.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                Intent gallery = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.INTERNAL_CONTENT_URI);
                startActivityForResult(gallery, PICK_IMAGE);
            }
        });

        Button button = findViewById(R.id.create_post);
        button.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                if (checkBox.isChecked()){
                    ispersonal = 1;
                }
                final String tags = tags_edit.getText().toString();
                final String contents = contents_edit.getText().toString();
                if(contents.getBytes().length <= 0){
                    Toast.makeText(AddPostActivity.this, "Please input contents", Toast.LENGTH_LONG).show();
                }
                else{
                    if(check){
                        final StorageReference mStorageRef = FirebaseStorage.getInstance().getReference("Images").child(currentImageUri.getLastPathSegment());

                        mStorageRef.putFile(currentImageUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                            @Override
                            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                                Map<String, Object> posts = new HashMap<>();
                                posts.put("username", id_val);
                                posts.put("tags", tags);
                                posts.put("pic_link", currentImageUri.getLastPathSegment());
                                posts.put("contents", contents);
                                posts.put("ispersonal", ispersonal);

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


                                Intent intent = new Intent(AddPostActivity.this, main_pageActivity.class);
                                if (intent.resolveActivity(getPackageManager()) != null) {
                                    intent.putExtra("id_val", String.valueOf(id_val));
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
                        posts.put("username", id_val);
                        posts.put("tags", tags);
                        posts.put("pic_link", "");
                        posts.put("contents", contents);
                        posts.put("ispersonal", ispersonal);

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


                        Intent intent = new Intent(AddPostActivity.this, main_pageActivity.class);
                        if (intent.resolveActivity(getPackageManager()) != null) {
                            intent.putExtra("id_val", String.valueOf(id_val));
                            startActivity(intent);
                        }
                    }
                }

            }
        });




        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener()
        {
            @Override
            public boolean onNavigationItemSelected (@NonNull MenuItem item){
                switch (item.getItemId()) {
                    case R.id.nav_full_name:
                        break;
                    case R.id.nav_birthday:
                        break;
                    case R.id.nav_email:
                        break;
                }
                DrawerLayout drawer = findViewById(R.id.drawer_layout);
                drawer.closeDrawer(GravityCompat.START);
                return false;
            }
        });
    }

    @Override
    public void onBackPressed(){
        if (drawer.isDrawerOpen(GravityCompat.START)){
            drawer.closeDrawer(GravityCompat.START);
        } else{
            super.onBackPressed();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data){
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == PICK_IMAGE){
            if(ispost){
                ImageButton button = (ImageButton)findViewById(R.id.select_img);
                currentImageUri = data.getData();
                check = true;
                button.setImageURI(currentImageUri);
            }
            else{
                ImageView icon = (ImageView)findViewById(R.id.profile_img);
                currentImageUri = data.getData();
                check = true;
                icon.setImageURI(currentImageUri);
                itsprofile();
                ispost = true;
            }

        }
    }

    public void itsprofile(){
        final StorageReference mStorageRef = FirebaseStorage.getInstance().getReference("Icons").child(currentImageUri.getLastPathSegment());

        mStorageRef.putFile(currentImageUri).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.w(TAG,"error");
            }
        }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                CollectionReference userRef = db.collection("users");
                Query query = userRef.whereEqualTo("username", id_val);

                query.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                String document_path = document.getId();
                                update_database(document_path);
                            }
                        } else {
                            Log.w("??", "Error adding document");
                        }
                    }
                });
            }
        });
    }

    public void update_database(String document_path){
        DocumentReference updateRef = db.collection("users").document(document_path);
        updateRef.update("iconlink", currentImageUri.getLastPathSegment()).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                Log.d(TAG, "DocumentSnapshot successfully updated!");
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.w(TAG, "Error updating document", e);
            }
        });
    }
}
