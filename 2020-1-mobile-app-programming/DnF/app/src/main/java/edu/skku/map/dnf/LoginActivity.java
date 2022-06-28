package edu.skku.map.dnf;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Map;

public class LoginActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        ImageView goHome = (ImageView) findViewById(R.id.goHome);
        goHome.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent1 = new Intent(LoginActivity.this, MainActivity.class);
                startActivity(intent1);
            }
        });

        final FirebaseFirestore db = FirebaseFirestore.getInstance();
        final EditText username = findViewById(R.id.username);
        final EditText password = findViewById(R.id.password);

        Button button = findViewById(R.id.loginButton);
        button.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                String usname = username.getText().toString();
                String pw = password.getText().toString();

                if(usname.getBytes().length <= 0){
                    Toast.makeText(LoginActivity.this, "ID를 입력해 주세요", Toast.LENGTH_LONG).show();
                }
                else{
                    final ArrayList<Map<String, Object>> userdata = new ArrayList<>();
                    CollectionReference userRef = db.collection("users");
                    Query query = userRef.whereEqualTo("username", usname).whereEqualTo("password", pw);
                    query.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<QuerySnapshot> task) {
                            if (task.isSuccessful()) {
                                QuerySnapshot documentt = task.getResult();
                                if (documentt.isEmpty()) {
                                    Toast.makeText(LoginActivity.this, "잘못된 비밀번호입니다", Toast.LENGTH_LONG).show();
                                } else {
                                    for (QueryDocumentSnapshot document : task.getResult()) {
                                        userdata.add(document.getData());
                                        Log.d("dat", document.getId() + " => " + document.getData());
                                    }
                                    LoginUser loginUser = new LoginUser();
                                    loginUser.setUsername((String) userdata.get(0).get("username"));
                                    loginUser.setPassword((String) userdata.get(0).get("password"));
                                    loginUser.setCharacterId((String) userdata.get(0).get("characterId"));
                                    loginUser.setCharacterName((String) userdata.get(0).get("characterName"));
                                    loginUser.setServerId((String) userdata.get(0).get("serverId"));
                                    loginUser.setServerName((String) userdata.get(0).get("serverName"));
                                    MyGlobals.getInstance().setData(loginUser);
                                    Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                                    if (intent.resolveActivity(getPackageManager()) != null)
                                        intent.putExtra("id_val", String.valueOf(username.getText()));
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

        TextView sign_up = findViewById(R.id.signUpButton);
        sign_up.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                Intent intent2 = new Intent(LoginActivity.this, SignUpActivity.class);
                if (intent2.resolveActivity(getPackageManager()) != null)
                    startActivity(intent2);
            }
        });
    }
}