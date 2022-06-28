package edu.skku.map.pa2;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

public class LoginActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        final FirebaseFirestore db = FirebaseFirestore.getInstance();
        final EditText id_val = findViewById(R.id.id_val);
        final EditText password = findViewById(R.id.password);

        try {
            Intent intent = getIntent();
            String text = intent.getStringExtra("username");
            id_val.setText(text);
        }catch(Exception e){

        }

        Button button = findViewById(R.id.login_button);
        button.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                String id_val_string = id_val.getText().toString();
                String pw = password.getText().toString();

                if(id_val_string.getBytes().length <= 0){
                    Toast.makeText(LoginActivity.this, "Wrong Username", Toast.LENGTH_LONG).show();
                }
                else{
                    CollectionReference userRef = db.collection("users");
                    Query query = userRef.whereEqualTo("username", id_val_string).whereEqualTo("password", pw);
                    query.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            QuerySnapshot document = task.getResult();
                            if (document.isEmpty()) {
                                Toast.makeText(LoginActivity.this, "Wrong Password", Toast.LENGTH_LONG).show();
                            } else {
                                Intent intent = new Intent(LoginActivity.this, main_pageActivity.class);
                                if (intent.resolveActivity(getPackageManager()) != null)
                                    intent.putExtra("id_val", String.valueOf(id_val.getText()));
                                startActivity(intent);
                            }
                        } else {
                            Toast.makeText(LoginActivity.this, "error", Toast.LENGTH_LONG).show();
                        }
                    }
                    });
                }
            }
        });

        TextView sign_up = findViewById(R.id.sign_up_button);
        sign_up.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                Intent intent2 = new Intent(LoginActivity.this, Sign_upActivity.class);
                if (intent2.resolveActivity(getPackageManager()) != null)
                    startActivity(intent2);
            }
        });
    }




}
