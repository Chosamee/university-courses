package edu.skku.map.pa2;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.HashMap;
import java.util.Map;

public class Sign_upActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        final FirebaseFirestore db = FirebaseFirestore.getInstance();

        Button button = findViewById(R.id.sign_up_button);
        button.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                EditText username = findViewById(R.id.username);
                EditText password = findViewById(R.id.password);
                EditText fullname = findViewById(R.id.fullname);
                EditText birthday = findViewById(R.id.birthday);
                EditText email = findViewById(R.id.email);

                final String user = username.getText().toString();
                final String pw = password.getText().toString();
                final String fname = fullname.getText().toString();
                final String birth = birthday.getText().toString();
                final String mail = email.getText().toString();

                final Map<String, Object> users = new HashMap<>();
                users.put("username", user);
                users.put("password", pw);
                users.put("fullname", fname);
                users.put("birthday", birth);
                users.put("email", mail);
                users.put("iconlink", "");

                CollectionReference userRef = db.collection("users");
                Query query = userRef.whereEqualTo("username", user);
                query.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            QuerySnapshot document = task.getResult();
                            if (document.isEmpty()) {
                                if (user.getBytes().length <= 0 || pw.getBytes().length <= 0 || fname.getBytes().length <= 0 ||
                                        birth.getBytes().length <= 0 || mail.getBytes().length <= 0) {
                                    Toast.makeText(Sign_upActivity.this, "Please fill all blanks", Toast.LENGTH_LONG).show();
                                } else {
                                    // Add a new document with a generated ID
                                    db.collection("users")
                                            .add(users)
                                            .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                                                @Override
                                                public void onSuccess(DocumentReference documentReference) {
                                                }
                                            })
                                            .addOnFailureListener(new OnFailureListener() {
                                                @Override
                                                public void onFailure(@NonNull Exception e) {
                                                }
                                            });

                                    Intent intent = new Intent(Sign_upActivity.this, LoginActivity.class);
                                    intent.putExtra("username", user);
                                    if (intent.resolveActivity(getPackageManager()) != null)
                                        startActivity(intent);
                                }
                            }
                            else{
                                Toast.makeText(Sign_upActivity.this, "Please use another username", Toast.LENGTH_LONG).show();
                            }
                        }
                        else{
                            Toast.makeText(Sign_upActivity.this, "error", Toast.LENGTH_LONG).show();
                        }
                    }
                });
            }
        });
    }
}
