package edu.skku.map.pa2;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager.widget.ViewPager;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.auth.api.signin.internal.Storage;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.lang.ref.Reference;
import java.sql.Ref;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;


public class main_pageActivity extends AppCompatActivity {
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    private static final String TAG = "main_pageActivity"; // Log.d debug asdasdasd
    private static final int PICK_IMAGE = 777;
    boolean check = false;
    Uri currentImageUri;

    private ListView personal_posts_list;
    private ListView public_posts_list;

    private StorageReference mStorgaeReference;
    private String id_val;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_page);


        Intent get_intent = getIntent();
        id_val = get_intent.getStringExtra("id_val");

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false); //initial del
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);  //button
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.menu); //icon

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        final ViewPager viewPager = (ViewPager) findViewById(R.id.viewPager);
        final VPAdapter adapter = new VPAdapter(getSupportFragmentManager(), 2, id_val);
        viewPager.setAdapter(adapter);


        TabLayout tabs = findViewById(R.id.tabs);
        tabs.addOnTabSelectedListener(new TabLayout.ViewPagerOnTabSelectedListener(viewPager));
        viewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabs));

        Button button3 = findViewById(R.id.add_post_button);
        button3.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(main_pageActivity.this, AddPostActivity.class);
                if (intent.resolveActivity(getPackageManager()) != null)
                    intent.putExtra("id_val", String.valueOf(id_val));
                    startActivity(intent);
            }
        });

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

        final ArrayList<Map<String, Object>> postdata =new ArrayList<>();

        profile_img.setClickable(true);
        profile_img.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                Intent gallery = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.INTERNAL_CONTENT_URI);
                startActivityForResult(gallery, PICK_IMAGE);
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
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data){
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == PICK_IMAGE){
            ImageView imageview = (ImageView)findViewById(R.id.profile_img);
            currentImageUri = data.getData();
            check = true;
            imageview.setImageURI(currentImageUri);

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

//    public void init(){
//        RecyclerView recyclerView = findViewById(R.id.recyclerView);
//
//        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
//        recyclerView.setLayoutManager(linearLayoutManager);
//
//        adapter = new RecyclerAdapter();
//        recyclerView.setAdapter(adapter);
//    }
}
